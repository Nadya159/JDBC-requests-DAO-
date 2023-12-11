package by.javaguru.jdbc.dao;

import by.javaguru.jdbc.entity.Flight;
import by.javaguru.jdbc.entity.FlightStatus;
import by.javaguru.jdbc.utils.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FlightDao implements Dao<Long, Flight> {
    private final static FlightDao INSTANCE = new FlightDao();
    private final static AircraftDao aircraftDao = AircraftDao.getInstance();
    private final static AirportDao airportDao = AirportDao.getInstance();

    private final static String SAVE_SQL = """
            INSERT INTO flight
            (flight_no, departure_date, departure_airport_code, arrival_date, arrival_airport_code, aircraft_id, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private final static String DELETE_SQL = """
            DELETE FROM flight
            WHERE id = ?
            """;
    private final static String FIND_ALL_SQL = """
            SELECT id, flight_no, departure_date, departure_airport_code, arrival_date, arrival_airport_code, aircraft_id, status
            FROM flight
            """;
    private final static String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE id = ?
            """;
    private final static String UPDATE_SQL = """
            UPDATE flight
            SET flight_no = ?, 
                departure_date = ?, 
                departure_airport_code = ?,
                arrival_date = ?,
                arrival_airport_code = ?,
                aircraft_id = ?,
                status = ?
            WHERE id = ?
            """;

    @Override
    public boolean update(Flight flight) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, flight.getFlightNo());
            statement.setTimestamp(2, Timestamp.valueOf(flight.getDepartureDate()));
            statement.setString(3, flight.getDepartureAirportCode().getCode());
            statement.setTimestamp(4, Timestamp.valueOf(flight.getArrivalDate()));
            statement.setString(5, flight.getArrivalAirportCode().getCode());
            statement.setInt(6, flight.getAircraft().getId());
            statement.setString(7, String.valueOf(flight.getStatus()));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Flight> findAll() {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            List<Flight> flights = new ArrayList<>();
            var result = statement.executeQuery();
            while (result.next())
                flights.add(buildFlight(result));
            return flights;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Flight buildFlight(ResultSet result) throws SQLException {
        return Flight.builder()
                .id(result.getLong("id"))
                .flightNo(result.getString("flight_no"))
                .departureDate(result.getTimestamp("departure_date").toLocalDateTime())
                .departureAirportCode(airportDao.findById(result.getString("departure_airport_code"),
                        result.getStatement().getConnection()).orElse(null))
                .arrivalDate(result.getTimestamp("arrival_date").toLocalDateTime())
                .arrivalAirportCode(airportDao.findById(result.getString("arrival_airport_code"),
                        result.getStatement().getConnection()).orElse(null))
                .aircraft(aircraftDao.findById(result.getInt("aircraft_id"),
                        result.getStatement().getConnection()).orElse(null))
                .status(FlightStatus.valueOf(result.getString("status")))
                .build();
    }

    public Optional<Flight> findById(Long id, Connection connection) {
        try (var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, id);
            var result = statement.executeQuery();
            Flight flight = null;
            if (result.next())
                flight = buildFlight(result);
            return Optional.ofNullable(flight);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Flight> findById(Long id) {
        try (var connection = ConnectionManager.get()) {
            return findById(id, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Flight save(Flight flight) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, flight.getFlightNo());
            statement.setTimestamp(2, Timestamp.valueOf(flight.getDepartureDate()));
            statement.setString(3, flight.getArrivalAirportCode().getCode());
            statement.setTimestamp(4, Timestamp.valueOf(flight.getArrivalDate()));
            statement.setString(5, flight.getArrivalAirportCode().getCode());
            statement.setInt(6, flight.getAircraft().getId());
            statement.setString(7, String.valueOf(flight.getStatus()));
            statement.executeUpdate();
            var keys = statement.getGeneratedKeys();
            if (keys.next())
                flight.setId(keys.getLong("id"));
            return flight;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(Long id) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static FlightDao getInstance() {
        return INSTANCE;
    }

    private FlightDao() {
    }
}
