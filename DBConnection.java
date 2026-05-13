import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {
    
     private static final Logger logger = Logger.getLogger(DBConnection.class.getName());

    private static final String URL      = "jdbc:mysql://localhost:3306/tournament_db";
    private static final String USER     = "root";
    private static final String PASSWORD = ""; 
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "[ERROR] Failed to close connection: ", e.getMessage());
            }
        }
    }
}