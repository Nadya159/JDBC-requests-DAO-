package by.javaguru.jdbc.entity;

import lombok.*;

import java.time.LocalDateTime;
 @Builder @Data
public class Flight {
    private Long id;
    private String flightNo;
    private LocalDateTime departureDate;
    private Airport departureAirportCode;
    private LocalDateTime arrivalDate;
    private Airport arrivalAirportCode;
    private Aircraft aircraft;
    private FlightStatus status;
}
