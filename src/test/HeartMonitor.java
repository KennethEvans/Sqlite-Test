package test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;

/* HeartMonitor
 * Created on Nov 12, 2011
 * By Kenneth Evans, Jr.
 */

public class HeartMonitor
{
    /** Flag to indicate if the database should be deleted first. */
    // private static final boolean deleteDatabase = true;
    /** The static format string to use for formatting dates. */
    // public static final String longFormat = "MMM dd, yyyy HH:mm:ss Z";
    public static final String longFormat = "hh:mm a MMM dd, yyyy";
    public static final SimpleDateFormat longFormatter = new SimpleDateFormat(
        longFormat);

    // private static final String databaseName =
    // "C:/Scratch/Heart Monitor/Heart Monitor 2012-08-08/HeartMonitor.db";
    private static final String databaseName = "C:/Scratch/Heart Monitor/Heart Monitor 2012-12-08/HeartMonitor.db";
    private static final String databaseURL = "jdbc:sqlite:" + databaseName;
    private static final String driverClass = "org.sqlite.JDBC";

    // private static final String driverClass = "SQLite.JDBCDriver";

    // /**
    // * Create the database.
    // *
    // * @return
    // */
    // // @SuppressWarnings("deprecation")
    // private static boolean createDatabase() {
    // Connection conn = null;
    // try {
    // conn = DriverManager.getConnection(databaseURL);
    // // java.lang.reflect.Method m = conn.getClass().getMethod(
    // // "getSQLiteDatabase", null);
    // // db = (SQLite.Database)m.invoke(conn, null);
    // Statement tableStatement = conn.createStatement();
    // tableStatement.execute("CREATE TABLE bob(id INTEGER, name TEXT)");
    // tableStatement
    // .execute("CREATE TABLE tower(tower_id INTEGER, latitude TEXT, longitude TEXT)");
    // tableStatement
    // .execute("CREATE TABLE usage(usage_id INTEGER, tower_id INTEGER, timestamp TEXT)");
    //
    // int id = 0;
    // PreparedStatement statement = conn
    // .prepareStatement("INSERT INTO bob VALUES(?, ?)");
    // for(String name : names) {
    // statement.setInt(1, id++);
    // statement.setString(2, name);
    // statement.execute();
    // }
    //
    // // Create the faked tower data.
    // statement = conn
    // .prepareStatement("INSERT INTO tower VALUES(?, ?, ?)");
    //
    // statement.setInt(1, 1);
    // statement.setString(2, "45.928237");
    // statement.setString(3, "-89.696066");
    // statement.execute();
    //
    // statement.setInt(1, 2);
    // statement.setString(2, "46.000000");
    // statement.setString(3, "-89.000000");
    // statement.execute();
    //
    // // Create the faked tower usage.
    // statement = conn
    // .prepareStatement("INSERT INTO usage VALUES(?, ?, ?)");
    //
    // int usage = 0;
    // for(int tower = 0; tower < 7; tower++) {
    // statement.setInt(1, usage++);
    // statement.setInt(2, (usage % 3) + 1);
    // Calendar calendar = Calendar.getInstance();
    // calendar.set(2011, tower, tower);
    // statement.setString(3, calendar.getTime().toString());
    // statement.execute();
    // }
    // } catch(Exception ex) {
    // ex.printStackTrace();
    // return false;
    // } finally {
    // try {
    // if(conn != null) {
    // conn.close();
    // }
    // } catch(Exception ex) {
    // ex.printStackTrace();
    // return false;
    // }
    // }
    // return true;
    // }

    // /**
    // * Deletes the database from the file system.
    // */
    // private static boolean deleteDatabase() {
    // File file = new File(databaseName);
    // if(file.exists()) {
    // if(!file.canWrite()) {
    // System.out.println("File cannot be written " + databaseName);
    // return false;
    // }
    // boolean res = file.delete();
    // if(res) {
    // System.out.println("Delete succeeded for " + databaseName);
    // } else {
    // System.out.println("Delete failed for " + databaseName);
    // System.out.println("    Check if it is in use");
    // return false;
    // }
    //
    // // try {
    // // Thread.sleep(60000);
    // // } catch(InterruptedException ex) {
    // // System.out.println("Sleep was interrupted");
    // // }
    //
    // // Check if it really worked
    // file = new File(databaseName);
    // if(file.exists()) {
    // System.out.println("Delete actually did not work.  "
    // + "Check if it is open somewhere.");
    // return false;
    // }
    // } else {
    // System.out
    // .println("File does not exist so does not need to be deleted"
    // + databaseName);
    // }
    // return true;
    // }

    /**
     * Prints the names of the tables in the database.
     * 
     * @param conn
     */
    public static void getTableNames(Connection conn) {
        try {
            System.out.println("\nTable Names");
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
            System.out.println("Error getting table names");
            ex.printStackTrace();
        }
    }

    /**
     * Prints the column names and other information from the CREATE statement
     * for the given table.
     * 
     * @param conn
     * @param table
     */
    public static void getColumnNames(Connection conn, String table) {
        try {
            System.out.println("\nColumns for " + table);
            Statement statement = conn.createStatement();
            ResultSet res = statement
                .executeQuery("SELECT sql FROM sqlite_master "
                    + "WHERE tbl_name = '" + table + "' AND type = 'table'");
            // This returns one item with column information in parentheses
            String s = res.getString(1);
            // Could probably use regex here
            int start = s.indexOf("(");
            int end = s.indexOf(")");
            String cols = s.substring(start + 1, end);
            // System.out.println(cols);
            String[] tokens = cols.split(",");
            int nCols = 0;
            for(String token : tokens) {
                nCols++;
                System.out.println(" Name: " + token.trim());
            }
            System.out.println("Number of columns: " + nCols);
        } catch(Exception ex) {
            System.out.println("Error getting column names");
            ex.printStackTrace();
        }
    }

    /**
     * Prints the data in the data table.
     * 
     * @param conn
     */
    public static void getData(Connection conn) {
        try {
            System.out.println("\nData");
            Statement statement = conn.createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM data");
            String id, date, count, total, comment;
            // String dateMod, edited;
            long dateNum;
            // long dateModNum;
            while(res.next()) {
                id = res.getString("_id");
                dateNum = res.getLong("date");
                date = formatDate(longFormatter, dateNum);
                // dateModNum = res.getLong("datemod");
                // dateMod = formatDate(longFormatter, dateModNum);
                count = res.getString("count");
                total = res.getString("total");
                // edited = res.getString("edited");
                comment = res.getString("comment");
                System.out.printf("%-6s %s/%s \t%s \t%s", id, count, total,
                    date, comment);
                System.out.println();
            }
        } catch(Exception ex) {
            System.out.println("Error listing data");
            ex.printStackTrace();
        }
    }

    /**
     * Format the date using the given format.
     * 
     * @param formatter
     * @param dateNum
     * @return
     * @see #longFormat
     */
    public static String formatDate(SimpleDateFormat formatter, Long dateNum) {
        // Consider using Date.toString() as it might be more locale
        // independent.
        if(dateNum == null) {
            return "<Unknown>";
        }
        if(dateNum == -1) {
            // Means the column was not found in the database
            return "<Date NA>";
        }
        // Consider using Date.toString()
        // It might be more locale independent.
        // return new Date(dateNum).toString();

        // Include the dateNum
        // return dateNum + " " + formatter.format(dateNum);

        return formatter.format(dateNum);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Running " + HeartMonitor.class.getSimpleName());

        // SQLite.Database db = null;
        try {
            // Initialize the JDBC driver
            Class.forName(driverClass).getDeclaredConstructor().newInstance();
        } catch(Exception ex) {
            System.out.println("Failed to initialize " + driverClass);
            return;
        }

        // Check if the database exists
        File file = new File(databaseName);
        System.out.println(databaseName);
        if(!file.exists()) {
            System.out.println("Database does not exist: " + databaseName);
            return;
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

        if(false) {
            getColumnNames(conn, "android_metadata");
        }

        if(true) {
            getColumnNames(conn, "data");
        }

        if(false) {
            getColumnNames(conn, "sqlite_sequence");
        }

        if(true) {
            getData(conn);
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
