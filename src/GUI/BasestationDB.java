package GUI;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class BasestationDB {

    // the URL where we can create/find the database
    // will always be in the current directory - ".\\"
    static String url = "jdbc:sqlite:.\\";

    /**
     * Connect to database and create a basestation table if one doesn't exist.
     *
     * @param fileName the database file name
     */
    public static void createNewDatabase(String fileName) {
        // append the name of the db to the URL
        if (url.contains(fileName)) {
            // do nothing, DB is already there
        } else {
            //create DB, one doesn't exist yet
            url += fileName;
            // connect to the database
            try (Connection conn = DriverManager.getConnection(url)) {
                if (conn != null) {
                    DatabaseMetaData meta = conn.getMetaData();
                    Statement stmt = conn.createStatement();
                    // create a table for basestations if one doesn't exist
                    String sql = "CREATE TABLE IF NOT EXISTS BASESTATIONS "
                            + "(name VARCHAR(20) not NULL, "
                            + " LATITUDE DOUBLE PRECISION not NULL, "
                            + " LONGITUDE DOUBLE PRECISION not NULL, "
                            + " MEASUREDPOWER DOUBLE PRECISION not NULL, "
                            + " PRIMARY KEY ( name ))";
                    stmt.executeUpdate(sql);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * The method inserts a row into the basestation table. It reuses the
     * connection created in BasestationController, so that the db does not
     * become locked.
     *
     * @param name the name of the basestation
     * @param latitude the latitude of the basestation
     * @param longitude the longitude of the basestation
     * @param measuredPower the measured power of the basestation
     * @param c connection to the database
     */
    public static void insert(String name, Double latitude, Double longitude,
            Double measuredPower, Connection c) {
        String sql = "INSERT INTO basestations(name, latitude, longitude, "
                + "measuredpower) VALUES(?,?,?,?)";
        try {
            // insert the appropiate values into the sql statement
            PreparedStatement pstmt = c.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setDouble(2, latitude);
            pstmt.setDouble(3, longitude);
            pstmt.setDouble(4, measuredPower);
            // execute the sql command
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * The method updates a row into the basestation table. It reuses the
     * connection created in BasestationController, so that the db does not
     * become locked.
     *
     * @param name the name of the basestation
     * @param latitude the latitude of the basestation
     * @param longitude the longitude of the basestation
     * @param measuredPower the measured power of the basestation
     * @param c connection to the database
     */
    public static void update(String name, Double latitude, Double longitude,
            Double measuredPower, Connection c) {
        String sql = "UPDATE basestations SET latitude=?, longitude=?,"
                + "measuredpower=? WHERE name=?";
        try {
            // insert the appropiate values into the sql statement
            PreparedStatement pstmt = c.prepareStatement(sql);
            pstmt.setDouble(1, latitude);
            pstmt.setDouble(2, longitude);
            pstmt.setDouble(3, measuredPower);
            pstmt.setString(4, name);
            // execute the sql command
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // FUTURE -- DELETE
    /**
     * Method deletes a basestation entry from the database.
     *
     * @param name the name of the basestation to be deleted
     * @param c connection to the database
     */
    public static void delete(String name, Connection c) {
        String sql = "DELETE FROM basestations WHERE name=?";
        try {
            // insert the appropiate values into the sql statement
            PreparedStatement pstmt = c.prepareStatement(sql);
            pstmt.setString(1, name);
            // execute the sql command
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void read(Connection c) {
        String sql = "SELECT name FROM basestations;";
        try {
            // insert the appropiate values into the sql statement
            PreparedStatement pstmt = c.prepareStatement(sql);
            // execute the sql command
            pstmt.executeUpdate();
            System.out.println("");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
