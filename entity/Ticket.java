package by.javaguru.jdbc.entity;

import lombok.*;

import java.math.BigDecimal;
@Data @Builder
public class Ticket {
    private Long id;
    private String passportNo;
    private String passengerName;
    private Flight flight;
    private String seatNo;
    private BigDecimal cost;

}
