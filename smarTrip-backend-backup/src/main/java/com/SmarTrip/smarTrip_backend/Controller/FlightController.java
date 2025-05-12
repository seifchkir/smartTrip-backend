package com.SmarTrip.smarTrip_backend.Controller;

import com.SmarTrip.smarTrip_backend.Service.AmadeusService;
import com.amadeus.resources.FlightOfferSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final AmadeusService amadeusService;

    @GetMapping("/search")
    public ResponseEntity<?> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String departureDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam(defaultValue = "1") Integer adults,
            @RequestParam(required = false, defaultValue = "EUR") String currency) {
        
        System.out.println("Searching flights from " + origin + " to " + destination);
        
        try {
            FlightOfferSearch[] flightOffers = amadeusService.searchFlights(
                    origin, destination, departureDate, returnDate, adults, currency);
            
            if (flightOffers.length == 0) {
                return ResponseEntity.ok(Map.of(
                    "message", "No flights found for the given criteria",
                    "status", "NO_CONTENT"
                ));
            }
            
            return ResponseEntity.ok(flightOffers);
        } catch (Exception e) {
            System.err.println("Error searching flights: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error searching flights");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "ERROR");
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/airports")
    public ResponseEntity<?> searchAirports(@RequestParam String keyword) {
        System.out.println("Searching airports with keyword: " + keyword);
        
        try {
            Map<String, String> airports = amadeusService.searchAirportsByKeyword(keyword);
            
            if (airports.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "message", "No airports found for keyword: " + keyword,
                    "status", "NO_CONTENT"
                ));
            }
            
            return ResponseEntity.ok(airports);
        } catch (Exception e) {
            System.err.println("Error searching airports: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error searching airports");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", "ERROR");
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // Add a simple test endpoint to check if the controller is working
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        System.out.println("Flight controller test endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Flight controller is working");
        return ResponseEntity.ok(response);
    }
    
    // Add a root endpoint to check if the base path is working
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> rootEndpoint() {
        System.out.println("Flight controller root endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Flight controller base path is working");
        return ResponseEntity.ok(response);
    }
}