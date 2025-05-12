package com.SmarTrip.smarTrip_backend.Service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AmadeusService {

    private final Amadeus amadeus;

    public FlightOfferSearch[] searchFlights(String originLocationCode, 
                                            String destinationLocationCode, 
                                            String departureDate, 
                                            String returnDate, 
                                            Integer adults, 
                                            String currencyCode) {
        try {
            // Build parameters for flight search
            Params params = Params.with("originLocationCode", originLocationCode)
                    .and("destinationLocationCode", destinationLocationCode)
                    .and("departureDate", departureDate)
                    .and("adults", adults);
            
            // Add optional parameters if provided
            if (returnDate != null && !returnDate.isEmpty()) {
                params.and("returnDate", returnDate);
            }
            
            if (currencyCode != null && !currencyCode.isEmpty()) {
                params.and("currencyCode", currencyCode);
            }
            
            // Execute the flight search
            return amadeus.shopping.flightOffersSearch.get(params);
            
        } catch (ResponseException e) {
            System.err.println("Amadeus API error: " + e.getMessage());
            e.printStackTrace();
            return new FlightOfferSearch[0];
        }
    }
    
    public Map<String, String> searchAirportsByKeyword(String keyword) {
        try {
            Map<String, String> airportsMap = new HashMap<>();
            
            // Search for airports by keyword
            com.amadeus.resources.Location[] locations = amadeus.referenceData.locations.get(
                    Params.with("keyword", keyword)
                          .and("subType", "AIRPORT"));
            
            // Convert to a simple map of code -> name
            for (com.amadeus.resources.Location location : locations) {
                airportsMap.put(location.getIataCode(), location.getName());
            }
            
            return airportsMap;
        } catch (ResponseException e) {
            System.err.println("Amadeus API error: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}