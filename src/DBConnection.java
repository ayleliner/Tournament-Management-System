import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/tournament_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "";  //matic na this XAMPP default will be equivalent to blank

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("[ERROR] Failed to close connection: " + e.getMessage());
            }
        }
    }
}
