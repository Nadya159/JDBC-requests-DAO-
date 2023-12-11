package by.javaguru.jdbc;

import by.javaguru.jdbc.dao.*;
import by.javaguru.jdbc.entity.*;

public class JdbcRunner2 {
    public static void main(String[] args) {
        var aircraftDao = AircraftDao.getInstance();
        var seatDao = SeatDao.getInstance();
        var airportDao = AirportDao.getInstance();
        var ticketDao = TicketDao.getInstance();
        var flightDao = FlightDao.getInstance();

        Aircraft aircraft = aircraftDao.findById(2).get();
        Seat seat = seatDao.findById(aircraft).orElseThrow();
        Airport airport = airportDao.findById("BSL").get();
        Flight flight = flightDao.findById(6L).get();
        Ticket ticket = ticketDao.findById(29L).orElseThrow();
        System.out.println("-->" + aircraft + "\n-->" + seat + "\n-->" +
                           airport + "\n-->" + flight + "\n-->" + ticket);
    }
}
