import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';
import { FirebaseService } from '../services/firebase.service';
import { HttpClient } from '@angular/common/http';
// Removed Firebase Auth imports
import { getStorage, ref, uploadBytes, getDownloadURL } from 'firebase/storage';

interface ImageFile {
  file: File;
  preview: string;
}

@Component({
  selector: 'app-trips',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './trips.component.html',
  styleUrls: ['./trips.component.scss']
})
export class TripsComponent implements OnInit {
  posts: any[] = [];
  trendingDestinations: any[] = [];
  suggestedUsers: any[] = [];

  // Properties
  userAvatar: string | null = null;
  newPostContent: string = '';
  selectedImages: { file: File, preview: string }[] = [];
  selectedLocation: string | null = null;
  isPosting: boolean = false;
  postTitle: string = '';
  showTitleInput: boolean = false;

  @ViewChild('fileInput') fileInput!: ElementRef;

  constructor(
    private firebaseService: FirebaseService,
    private http: HttpClient
  ) {}

  // Use JWT for authentication state
  isAuthenticated: boolean = false;
  userId: string | null = null;

  ngOnInit() {
    this.checkAuthState();
    this.loadUserProfile();
    this.loadPosts();
    this.loadTrendingDestinations();
    this.loadSuggestedUsers();
  }

  checkAuthState() {
    const token = localStorage.getItem('token');
    this.isAuthenticated = !!token;
    // Optionally, decode token to get userId if needed
    if (this.isAuthenticated) {
      // If you store userId separately, retrieve it here
      this.userId = localStorage.getItem('userId');
    }
    console.log('Authentication state:', this.isAuthenticated ? 'Logged in' : 'Not logged in');
    if (this.isAuthenticated) {
      this.loadUserProfile();
    }
  }

  async createPost() {
    if ((!this.newPostContent && this.selectedImages.length === 0) || this.isPosting) {
      return;
    }

    this.isPosting = true;

    try {
      if (!this.isAuthenticated) {
        alert('Please log in to create a post');
        this.isPosting = false;
        return;
      }

      const token = localStorage.getItem('token');
      if (!token) {
        alert('Authentication error. Please log in again.');
        this.isPosting = false;
        return;
      }

      const formData = new FormData();
      formData.append('text', this.newPostContent);
      if (this.postTitle) {
        formData.append('title', this.postTitle);
      }
      if (this.selectedLocation) {
        formData.append('tags', this.selectedLocation);
      }
      if (this.selectedImages.length > 0) {
        formData.append('image', this.selectedImages[0].file);
      }
      // If you need to send userId, add it here
      if (this.userId) {
        formData.append('userId', this.userId);
      }

      const response = await this.http.post(
        'http://localhost:8080/api/posts',
        formData,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      ).toPromise();

      console.log('Post created successfully:', response);

      this.newPostContent = '';
      this.postTitle = '';
      this.selectedImages = [];
      this.selectedLocation = null;
      this.showTitleInput = false;
      this.loadPosts();

    } catch (error) {
      alert('Failed to create post. Please try again.');
    } finally {
      this.isPosting = false;
    }
  }

  triggerFileInput() {
    this.fileInput.nativeElement.click();
  }

  onImagesSelected(event: any) {
    const files = event.target.files;
    if (files && files.length > 0) {
      const remainingSlots = 4 - this.selectedImages.length;
      const filesToAdd = Array.from(files).slice(0, remainingSlots);
      for (const file of filesToAdd) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.selectedImages.push({
            file: file as File,
            preview: e.target.result
          });
        };
        reader.readAsDataURL(file as Blob);
      }
    }
  }

  removeImage(index: number) {
    this.selectedImages.splice(index, 1);
  }

  openLocationPicker() {
    const location = prompt('Enter your location:');
    if (location) {
      this.selectedLocation = location;
    }
  }

  removeLocation() {
    this.selectedLocation = null;
  }

  loadUserProfile() {
    // You can use your backend or Firestore for user profile
    // Example: fetch from backend using userId and token
    if (this.userId && this.isAuthenticated) {
      const token = localStorage.getItem('token');
      this.http.get(`http://localhost:8080/api/users/${this.userId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }).subscribe((profile: any) => {
        if (profile && profile.photoUrl) {
          this.userAvatar = profile.photoUrl;
        }
      }, error => {
        console.error('Error loading user profile:', error);
      });
    }
  }

  loadPosts() {
    // Implement this to load posts from backend or Firestore
    // For now, we'll use dummy data
    this.posts = [
      {
        id: 1,
        userName: 'Sarah Johnson',
        userAvatar: 'https://randomuser.me/api/portraits/women/65.jpg',
        location: 'Santorini, Greece',
        timeAgo: '2 hours ago',
        description: 'Just arrived in Santorini and the views are absolutely breathtaking! The white-washed buildings against the blue sea is a sight to behold. Can\'t wait to explore more of this beautiful island!',
        images: [
          'https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?q=80&w=2574&auto=format&fit=crop',
          'https://images.unsplash.com/photo-1613395877344-13d4a8e0d49e?q=80&w=2535&auto=format&fit=crop'
        ],
        likes: 245,
        comments: 32,
        recentComments: [
          {
            userName: 'Michael Chen',
            userAvatar: 'https://randomuser.me/api/portraits/men/22.jpg',
            text: 'Looks amazing! Which hotel are you staying at?'
          }
        ]
      }
    ];
  }

  loadTrendingDestinations() {
    this.trendingDestinations = [
      {
        name: 'Bali, Indonesia',
        image: 'https://images.unsplash.com/photo-1537996194471-e657df975ab4?q=80&w=2938&auto=format&fit=crop',
        posts: 1549
      },
      {
        name: 'Paris, France',
        image: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?q=80&w=2073&auto=format&fit=crop',
        posts: 1343
      },
      {
        name: 'New York, USA',
        image: 'https://images.unsplash.com/photo-1522083165195-3424ed129620?q=80&w=2960&auto=format&fit=crop',
        posts: 987
      }
    ];
  }

  loadSuggestedUsers() {
    this.suggestedUsers = [
      {
        name: 'Carlos Rodriguez',
        avatar: 'https://randomuser.me/api/portraits/men/45.jpg',
        followers: 1245
      },
      {
        name: 'Lisa Chen',
        avatar: 'https://randomuser.me/api/portraits/women/76.jpg',
        followers: 876
      }
    ];
  }
} 