package by.javaguru.jdbc.dto;

public record TicketFilter(String passenferName,
                           String seatNo,
                           int limit,
                           int offset) {            //для пагинации вывода результата
}
