package by.javaguru.jdbc;

import by.javaguru.jdbc.utils.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcRunner {
    public static void main(String[] args) throws SQLException {
        System.out.println("Имена пассажиров, встречающиеся более 2-х раз:" + namesByFrequency(2));
        System.out.println("Имена пассажиров и сколько билетов пассажир купил за все время:" + namesWithTickets());
        System.out.println("Обновление № места по ID тикета: " + updateSeatById(46L, "B1"));
        System.out.println("Обновление в таблицах fligth, ticket по flight_id: " + updateByFlightId(10L));
    }

    public static List<String> namesByFrequency(int frequency) {
        List<String> names = new ArrayList<>();
        String sql = """
                    select split_part(passenger_name, ' ', 1) as name, count(*) as frequency
                    from ticket
                    group by name
                    having count(*) > ?
                    order by frequency desc;
                """;
        try (Connection connection = ConnectionManager.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setMaxRows(3);                                    //ограничение на 3 строки в результате запроса
            statement.setInt(1, frequency);
            var result = statement.executeQuery();
            while (result.next())
                names.add(result.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    public static Map<String, Integer> namesWithTickets() throws SQLException {
        Map<String, Integer> names = new HashMap<>();
        String sql = """
                    select passenger_name as name, count(*) as count
                    from ticket
                    group by name
                    order by count(*) desc;
                """;
        try (Connection connection = ConnectionManager.open();
             var statement = connection.prepareStatement(sql)) {
            var result = statement.executeQuery();
            while (result.next())
                names.put(result.getString("name"), result.getInt("count"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    public static int updateSeatById(Long id, String seat) {
        String sql = """
                    UPDATE ticket
                    SET seat_no = ?
                    WHERE id = ?;
                """;
        try (Connection connection = ConnectionManager.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, seat);
            statement.setLong(2, id);
            int result = statement.executeUpdate();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String updateByFlightId(Long fligthId) throws SQLException {
        String result = "";
        Connection connection = ConnectionManager.open();
        connection.setAutoCommit(false);
        Savepoint savepointOne = connection.setSavepoint("Savepoint");
        try {
            String sql = """
                    INSERT INTO flight (id, flight_no, departure_date, departure_airport_code, arrival_date, 
                        arrival_airport_code, aircraft_id, status)
                    VALUES (?, 'LDN366', '2023-06-14T14:30', 'MSK', '2023-06-14T18:07', 'LDN', 3, 'ARRIVED');
                    """;

            var statement = connection.prepareStatement(sql);
            statement.setLong(1, fligthId);
            statement.executeUpdate();
            sql = """
                        INSERT INTO ticket (passport_no, passenger_name, flight_id, seat_no, cost)
                        VALUES ('322350', 'Эдуард Щеглов', ? , 'B1', 230);
                    """;
            statement = connection.prepareStatement(sql);
            statement.setLong(1, fligthId);
            statement.executeUpdate();
            connection.commit();
            result = "Update OK";
        } catch (SQLException e) {
            result = "SQLException. Executing rollback to savepoint";
            connection.rollback(savepointOne);
        }
        connection.close();
        return result;
    }
}

