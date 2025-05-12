package com.SmarTrip.smarTrip_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.SmarTrip.smarTrip_backend")
public class SmarTripBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmarTripBackendApplication.class, args);
	}

}
