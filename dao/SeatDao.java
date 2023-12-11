package by.javaguru.jdbc.dao;

import by.javaguru.jdbc.entity.Aircraft;
import by.javaguru.jdbc.entity.Seat;
import by.javaguru.jdbc.utils.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeatDao implements Dao<Aircraft, Seat> {

    private final static SeatDao INSTANCE = new SeatDao();
    private final static AircraftDao aircraftDao = AircraftDao.getInstance();
    private final static String SAVE_SQL = """
            INSERT INTO seat (aircraft_id, seat_no)
            VALUES (?, ?)
            """;
    private final static String DELETE_SQL = """
            DELETE FROM seat
            WHERE aircraft_id = ?
            """;
    private final static String FIND_ALL_SQL = """
            SELECT aircraft_id, seat_no
            FROM seat
            """;
    private final static String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE aircraft_id = ?
            """;
    private final static String UPDATE_SQL = """
            UPDATE seat
            SET aircraft_id = ?, 
                seat_no = ?
            WHERE aircraft_id = ?
            """;

    @Override
    public boolean update(Seat seat) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setInt(1, seat.getAircraft().getId());
            statement.setString(2, seat.getSeatNo());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Seat> findAll() {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_ALL_SQL)) {
            List<Seat> seats = new ArrayList<>();
            var result = statement.executeQuery();
            while (result.next())
                seats.add(buildSeat(result));
            return seats;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Seat buildSeat(ResultSet result) throws SQLException {
        return Seat.builder()
                .aircraft(aircraftDao.findById(result.getInt("aircraft_id"),
                        result.getStatement().getConnection()).orElse(null))
                .seatNo(result.getString("seat_no"))
                .build();
    }

    @Override
    public Optional<Seat> findById(Aircraft aircraft) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, aircraft.getId());
            var result = statement.executeQuery();
            Seat seat = null;
            if (result.next())
                seat = buildSeat(result);
            return Optional.ofNullable(seat);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Seat save(Seat seat) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(SAVE_SQL)) {
            statement.setInt(1, seat.getAircraft().getId());
            statement.setString(2, seat.getSeatNo());
            statement.executeUpdate();
            return seat;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(Aircraft aircraft) {
        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, aircraft.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static SeatDao getInstance() {
        return INSTANCE;
    }

    private SeatDao() {
    }

}
