package by.javaguru.jdbc.entity;

import lombok.*;

@Builder @Data
public class Seat {
    private Aircraft aircraft;
    private String seatNo;
}
