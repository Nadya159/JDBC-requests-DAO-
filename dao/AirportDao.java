package by.javaguru.jdbc.dao;

import by.javaguru.jdbc.entity.Airport;
import by.javaguru.jdbc.utils.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AirportDao implements Dao<String, Airport> {
    private final static AirportDao INSTANCE = new AirportDao();
    private final static String SAVE_SQL = """
            INSERT INTO airport (code, country, city)
            VALUES (?, ?, ?)
            """;
    private final static String DELETE_SQL = """
            DELETE FROM airport
            WHERE code = ?
            """;
    private final static String FIND_ALL_SQL = """
            SELECT code, country, city
            FROM airport
            """;
    private final static String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE code = ?
            """;
    private final static String UPDATE_SQL = """
            UPDATE airport
            SET code = ?, 
                country = ?, 
                city = ?
            WHERE code = ?
            """;

    @Override
    public boolean update(Airport airport) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, airport.getCode());
            statement.setString(2, airport.getCountry());
            statement.setString(3, airport.getCity());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Airport> findAll() {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            List<Airport> airports = new ArrayList<>();
            var result = statement.executeQuery();
            while (result.next())
                airports.add(buildAirport(result));
            return airports;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Airport buildAirport(ResultSet result) throws SQLException {
        return Airport.builder()
                .code(result.getString("code"))
                .country(result.getString("country"))
                .city(result.getString("city"))
                .build();
    }

    public Optional<Airport> findById(String departureAirportCode, Connection connection) {
        try (var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setString(1, departureAirportCode);
            var result = statement.executeQuery();
            Airport airport = null;
            if (result.next())
                airport = buildAirport(result);
            return Optional.ofNullable(airport);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Airport> findById(String code) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setString(1, code);
            var result = statement.executeQuery();
            Airport airport = null;
            if (result.next())
                airport = buildAirport(result);
            return Optional.ofNullable(airport);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Airport save(Airport airport) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, airport.getCode());
            statement.setString(2, airport.getCountry());
            statement.setString(3, airport.getCity());
            statement.executeUpdate();
            var keys = statement.getGeneratedKeys();
            if (keys.next())
                airport.setCode(keys.getString("code"));
            return airport;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(String code) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setString(1, code);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static AirportDao getInstance() {
        return INSTANCE;
    }

    private AirportDao() {
    }
}
