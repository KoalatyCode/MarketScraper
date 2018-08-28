package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection connection() {
        Connection con = null;
        String databaseName = "marketdatabase";
        String userName = "root";
        String password = "";
        con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("JDBC Driver Loaded");
        } catch (Exception ex) {
            System.err.println("Unable to load database driver. ");
            System.err.println("Details:" + ex);
            return null;
        }
        String ip = "localhost";
        String url = "jdbc:mysql://" + ip + ":3306/" + databaseName;
        try {
            con = DriverManager.getConnection(url, userName, password);
            con.setReadOnly(false);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            return null;
        }

        System.out.println("Connection Success");
        return con;
    }

    public static void closeDatabaseConnection(Connection con) {
        try {
            if (con != null) {
                con.close();
            }
            System.out.println("Connection Close Success");
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
