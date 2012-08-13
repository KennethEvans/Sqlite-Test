package test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

/*
 * Created on Nov 12, 2011
 * By Kenneth Evans, Jr.
 */

public class Test1
{
    /** Flag to indicate if the database should be deleted first. */
    private static final boolean deleteDatabase = true;

    private static final String databaseName = "C:/Sqlite/test";
    private static final String databaseURL = "jdbc:sqlite:" + databaseName;
    private static final String driverClass = "org.sqlite.JDBC";
    private static String[] names = {"Eli", "Inigo", "Dylan", "Susan"};

    /**
     * Create the database.
     * 
     * @return
     */
    // @SuppressWarnings("deprecation")
    private static boolean createDatabase() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(databaseURL);
            // java.lang.reflect.Method m = conn.getClass().getMethod(
            // "getSQLiteDatabase", null);
            // db = (SQLite.Database)m.invoke(conn, null);
            Statement tableStatement = conn.createStatement();
            tableStatement.execute("CREATE TABLE bob(id INTEGER, name TEXT)");
            tableStatement
                .execute("CREATE TABLE tower(tower_id INTEGER, latitude TEXT, longitude TEXT)");
            tableStatement
                .execute("CREATE TABLE usage(usage_id INTEGER, tower_id INTEGER, timestamp TEXT)");

            int id = 0;
            PreparedStatement statement = conn
                .prepareStatement("INSERT INTO bob VALUES(?, ?)");
            for(String name : names) {
                statement.setInt(1, id++);
                statement.setString(2, name);
                statement.execute();
            }

            // Create the faked tower data.
            statement = conn
                .prepareStatement("INSERT INTO tower VALUES(?, ?, ?)");

            statement.setInt(1, 1);
            statement.setString(2, "45.928237");
            statement.setString(3, "-89.696066");
            statement.execute();

            statement.setInt(1, 2);
            statement.setString(2, "46.000000");
            statement.setString(3, "-89.000000");
            statement.execute();

            // Create the faked tower usage.
            statement = conn
                .prepareStatement("INSERT INTO usage VALUES(?, ?, ?)");

            int usage = 0;
            for(int tower = 0; tower < 7; tower++) {
                statement.setInt(1, usage++);
                statement.setInt(2, (usage % 3) + 1);
                Calendar calendar = Calendar.getInstance();
                calendar.set(2011, tower, tower);
                statement.setString(3, calendar.getTime().toString());
                statement.execute();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                if(conn != null) {
                    conn.close();
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Deletes the database from the file system.
     */
    private static boolean deleteDatabase() {
        File file = new File(databaseName);
        if(file.exists()) {
            if(!file.canWrite()) {
                System.out.println("File cannot be written " + databaseName);
                return false;
            }
            boolean res = file.delete();
            if(res) {
                System.out.println("Delete succeeded for " + databaseName);
            } else {
                System.out.println("Delete failed for " + databaseName);
                System.out.println("    Check if it is in use");
                return false;
            }

            // try {
            // Thread.sleep(60000);
            // } catch(InterruptedException ex) {
            // System.out.println("Sleep was interrupted");
            // }

            // Check if it really worked
            file = new File(databaseName);
            if(file.exists()) {
                System.out.println("Delete actually did not work.  "
                    + "Check if it is open somewhere.");
                return false;
            }
        } else {
            System.out
                .println("File does not exist so does not need to be deleted"
                    + databaseName);
        }
        return true;
    }

    private static void testBob(Connection conn) {
        try {
            System.out.println("\nTesting Bob");
            Statement statement = conn.createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM bob");
            while(res.next()) {
                String name = res.getString("name");
                int id = res.getInt("id");
                System.out.println("id=" + id + " name=" + name);
            }
        } catch(Exception ex) {
            System.out.println("Error in testBob");
            ex.printStackTrace();
        }
    }

    private static void testTower(Connection conn) {
        try {
            System.out.println("\nTesting Tower");
            Statement statement = conn.createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM tower");
            while(res.next()) {
                System.out.println("A Tower " + res.getInt("tower_id"));
            }

            res = statement.executeQuery("SELECT * FROM usage "
                + "ORDER BY tower_id");
            while(res.next()) {
                System.out.println("A Usage " + res.getInt("usage_id")
                    + " on tower " + res.getInt("tower_id") + " at "
                    + res.getString("timestamp"));
            }

            // res = statement
            // .executeQuery("SELECT t.tower_id, t.longitude, t.latitude, "
            // + "u.timestamp " + "FROM tower t, usage u "
            // + "WHERE t.tower_id = u.tower_id "
            // + "ORDER BY t.longitude DESC, t.latitude");
            res = statement
                .executeQuery("SELECT t.tower_id, t.longitude, t.latitude, "
                    + "u.timestamp " + "FROM tower t, usage u "
                    + "WHERE t.tower_id = u.tower_id " + "ORDER BY u.timestamp");
            while(res.next()) {
                int towerId = res.getInt("tower_id");
                String latitude = res.getString("latitude");
                String longitude = res.getString("longitude");
                String timestamp = res.getString("timestamp");
                System.out.println("B Tower " + towerId + " @ " + timestamp
                    + " (lat " + latitude + ", lon " + longitude + ")");
            }
        } catch(Exception ex) {
            System.out.println("Error in testTower");
            ex.printStackTrace();
        }
    }

    public static void testCursor(Connection conn) {
        try {
            System.out.println("\nTesting Cursor");
            Statement statement = conn.createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM tower");
            while(res.next()) {
                System.out.println("A Tower " + res.getInt("tower_id"));
            }

            res = statement.executeQuery("SELECT * FROM usage");
            while(res.next()) {
                System.out.println("A Usage " + res.getInt("usage_id")
                    + " on tower " + res.getInt("tower_id") + " at "
                    + res.getString("timestamp"));
            }

            res = statement
                .executeQuery("SELECT t.tower_id, t.longitude, t.latitude, "
                    + "u.timestamp " + "FROM tower t, usage u "
                    + "WHERE t.tower_id = u.tower_id");
            while(res.next()) {
                int towerId = res.getInt("tower_id");
                String latitude = res.getString("latitude");
                String longitude = res.getString("longitude");
                String timestamp = res.getString("timestamp");
                System.out.println("B Tower " + towerId + " @ " + timestamp
                    + " (lat " + latitude + ", lon " + longitude + ")");
            }
        } catch(Exception ex) {
            System.out.println("Error in testTower");
            ex.printStackTrace();
        }
    }

    public static void getTableNames(Connection conn) {
        try {
            System.out.println("\nTableNames");
            Statement statement = conn.createStatement();
            ResultSet res = statement
                .executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
            int nTables = 0;
            while(res.next()) {
                nTables++;
                System.out.println(" Name: " + res.getString("name"));
            }
            System.out.println("Number of tables: " + nTables);
        } catch(Exception ex) {
            System.out.println("Error in testTower");
            ex.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Running " + Test1.class.getSimpleName());

        // Delete the database if so specified
        if(deleteDatabase) {
            System.out.println("\nDeleting old database");
            boolean res = deleteDatabase();
            if(!res) {
                System.out.println("Aborting");
                return;
            }
        }

        // SQLite.Database db = null;
        try {
            // Initialize the JDBC driver
            Class.forName(driverClass).newInstance();
        } catch(Exception ex) {
            System.out.println("Failed to initialize " + driverClass);
            return;
        }

        // Check if the database exists
        File file = new File(databaseName);
        if(file.exists()) {
            System.out.println("\nDatabase exists: " + databaseName);
        } else {
            System.out.println("\nCreating: " + databaseName);
            boolean res = createDatabase();
            if(!res) {
                System.out.println("Failed to create database");
                return;
            }
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(databaseURL, "test", null);
        } catch(Exception ex) {
            System.out.println("Error getting connection");
            ex.printStackTrace();
            return;
        }

        if(true) {
            getTableNames(conn);
        }

        if(true) {
            testBob(conn);
        }

        if(true) {
            testTower(conn);
        }

        // Close the connection
        try {
            if(conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch(Exception ex) {
            System.out.println("Failed to close connection");
        }
    }

}
