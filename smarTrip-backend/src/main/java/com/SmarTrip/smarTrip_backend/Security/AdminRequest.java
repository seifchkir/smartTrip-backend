package com.SmarTrip.smarTrip_backend.Security;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
}
