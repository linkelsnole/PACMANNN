package my.snole.pacmannn.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "mamba";

    private Connection connection;

    public Database() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public void saveScore(int score, int level, boolean youWon) throws SQLException {
        String query = "INSERT INTO scores (score, level, you_won, timestamp) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, score);
            statement.setInt(2, level);
            statement.setBoolean(3, youWon);
            statement.executeUpdate();
        }
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
