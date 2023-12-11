package by.javaguru.jdbc.dao;

import by.javaguru.jdbc.entity.Aircraft;
import by.javaguru.jdbc.utils.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AircraftDao implements Dao<Integer, Aircraft> {
    private final static AircraftDao INSTANCE = new AircraftDao();
    private final static String SAVE_SQL = """
            INSERT INTO aircraft (id, model)
            VALUES (?, ?)
            """;
    private final static String DELETE_SQL = """
            DELETE FROM aircraft
            WHERE id = ?
            """;
    private final static String FIND_ALL_SQL = """
            SELECT id, model
            FROM aircraft
            """;
    private final static String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE id = ?
            """;
    private final static String UPDATE_SQL = """
            UPDATE aircraft
            SET id = ?, 
                model = ?
            WHERE id = ?
            """;

    @Override
    public boolean update(Aircraft aircraft) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setInt(1, aircraft.getId());
            statement.setString(2, aircraft.getModel());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Aircraft> findAll() {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            List<Aircraft> aircrafts = new ArrayList<>();
            var result = statement.executeQuery();
            while (result.next())
                aircrafts.add(buildAircraft(result));
            return aircrafts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Aircraft buildAircraft(ResultSet result) throws SQLException {
        return Aircraft.builder()
                .id(result.getInt("id"))
                .model(result.getString("model"))
                .build();
    }

    public Optional<Aircraft> findById(int aircraftId, Connection connection) {
        try (var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, aircraftId);
            var result = statement.executeQuery();
            Aircraft aircraft = null;
            if (result.next())
                aircraft = buildAircraft(result);
            return Optional.ofNullable(aircraft);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public Optional<Aircraft> findById(Integer id) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, id);
            var result = statement.executeQuery();
            Aircraft aircraft = null;
            if (result.next())
                aircraft = buildAircraft(result);
            return Optional.ofNullable(aircraft);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Aircraft save(Aircraft aircraft) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, aircraft.getId());
            statement.setString(2, aircraft.getModel());
            statement.executeUpdate();
            var keys = statement.getGeneratedKeys();
            if (keys.next())
                aircraft.setId(keys.getInt("id"));
            return aircraft;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static AircraftDao getInstance() {
        return INSTANCE;
    }

    private AircraftDao() {
    }


}
