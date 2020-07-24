package test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* HeartMonitor
 * Created on Nov 12, 2011
 * By Kenneth Evans, Jr.
 */

/**
 * CSPEditImageTool
 * @author Kenneth Evans, Jr.
 */
/**
 * CSPEditImageTool
 * 
 * @author Kenneth Evans, Jr.
 */
public class CSPEditImageTool
{
    public static final String LS = System.getProperty("line.separator");
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    /** Flag to indicate if the database should be deleted first. */
    // private static final boolean deleteDatabase = true;
    /** The static format string to use for formatting dates. */
    // public static final String longFormat = "MMM dd, yyyy HH:mm:ss Z";
    public static final String longFormat = "hh:mm a MMM dd, yyyy";
    public static final SimpleDateFormat longFormatter = new SimpleDateFormat(
        longFormat);

    // private static final String databaseName =
    // "C:/Scratch/Heart Monitor/Heart Monitor 2012-08-08/HeartMonitor.db";
    private static final String databaseName = "C:/Users/evans/Documents/CELSYS/CLIPStudioPaintVer1_5_0/Tool/EditImageTool.todb";
    private static final String databaseURL = "jdbc:sqlite:" + databaseName;
    private static final String driverClass = "org.sqlite.JDBC";
    private static final String TAB = "   ";

    private static HashMap<String, Tool> map;

    /**
     * Prints the names of the tables in the database.
     * 
     * @param conn
     */
    public static void getTableNames(Connection conn) {
        try {
            System.out.println("\nTable Names");
            Statement statement = conn.createStatement();
            ResultSet res = statement.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table'");
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
     * Gets a HashMap of the tools.
     * 
     * @param conn
     */
    public static void getData(Connection conn) {
        map = new HashMap<>();
        Tool tool;
        try {
            // System.out.println("\nData");
            Statement statement = conn.createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM Node");
            long id, nodeVariantID;
            String nodeName;
            byte[] nodeUuid, nodeFirstChildUuid, nodeNextUuid, nodeSelectedUuid;
            // String dateMod, edited;
            while(res.next()) {
                id = res.getLong("_PW_ID");
                nodeVariantID = res.getLong("NodeVariantID");
                nodeName = res.getString("NodeName");
                nodeUuid = res.getBytes("NodeUuid");
                nodeFirstChildUuid = res.getBytes("NodeFirstChildUuid");
                nodeNextUuid = res.getBytes("NodeNextUuid");
                nodeSelectedUuid = res.getBytes("NodeSelectedUuid");
                tool = new Tool(id, nodeVariantID, nodeName,
                    getHexString(nodeUuid), getHexString(nodeFirstChildUuid),
                    getHexString(nodeNextUuid), getHexString(nodeSelectedUuid));
                map.put(getHexString(nodeUuid), tool);
            }
        } catch(Exception ex) {
            System.out.println("Error listing data");
            ex.printStackTrace();
        }
        // Sort the list
        map = sortByValue(map);
    }

    /**
     * Prints out a list of the elements in the HashMap.
     */
    public static void listData() {
        if(map == null) {
            return;
        }
        System.out
            .println(String.format("%-5s %-5s %-45s %-32s %-32s %-32s %-32s",
                "PW_ID", "VarID", "NodeName", "NodeUuid", "NodeFirstChildUuid",
                "NodeNextUuid", "NodeSelectedUuid"));
        // List the data
        for(Map.Entry<String, Tool> entry : map.entrySet()) {
            System.out.println(entry.getValue().info());
        }
    }

    /**
     * Prints the hierarchy starting from the Tool with no nodeName, which is
     * the top.
     */
    public static void listHierarchy() {
        if(map == null) {
            return;
        }
        Tool tool, firstChild, firstChild1, firstChild2;
        int nTools = 0, nGroups = 0, nSubTools = 0;
        for(Map.Entry<String, Tool> entry : map.entrySet()) {
            tool = entry.getValue();
            // Only process the top level which has a blank name and _PW_ID=1
            if(tool.nodeName.length() != 0) {
                continue;
            }
            String nodeFirstChildUuid = tool.nodeFirstChildUuid;
            if(nodeFirstChildUuid == null
                || nodeFirstChildUuid.length() != 32) {
                continue;
            }
            firstChild = map.get(nodeFirstChildUuid);
            // Get the tools
            while(firstChild != null && firstChild.nodeUuid.length() == 32) {
                // System.out.println("Tool: " + firstChild.nodeName
                // + " nodeUuid=" + firstChild.nodeUuid
                // + " nodeFirstChildUuid=" + firstChild.nodeFirstChildUuid);
                nTools++;
                System.out.println("Tool: " + firstChild.nodeName);

                // Get the groups
                firstChild1 = map.get(firstChild.nodeFirstChildUuid);
                while(firstChild1 != null
                    && firstChild1.nodeUuid.length() == 32) {
                    // System.out.println(
                    // TAB + "Group: " + firstChild1.nodeName + " nodeUuid="
                    // + firstChild1.nodeUuid + " nodeFirstChildUuid="
                    // + firstChild1.nodeFirstChildUuid);
                    nGroups++;
                    System.out.println(TAB + "Group: " + firstChild1.nodeName);

                    // Get the sub tools
                    firstChild2 = map.get(firstChild1.nodeFirstChildUuid);
                    while(firstChild2 != null
                        && firstChild2.nodeUuid.length() == 32) {
                        // System.out.println(TAB + TAB + "SubTool: "
                        // + firstChild2.nodeName + " nodeUuid="
                        // + firstChild2.nodeUuid + " nodeFirstChildUuid="
                        // + firstChild2.nodeFirstChildUuid);
                        nSubTools++;
                        System.out.println(
                            TAB + TAB + "SubTool: " + firstChild2.nodeName);
                        firstChild2 = map.get(firstChild2.nodeNextUuid);
                    }

                    firstChild1 = map.get(firstChild1.nodeNextUuid);
                }
                firstChild = map.get(firstChild.nodeNextUuid);
            }
        }
        System.out.println(LS + "nTools=" + nTools + " nGroups=" + nGroups
            + " nSubTools=" + nSubTools);
    }

    // Utilities

    public static String getHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for(int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static HashMap<String, Tool> sortByValue(HashMap<String, Tool> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Tool>> list = new LinkedList<Map.Entry<String, Tool>>(
            hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Tool>>() {
            public int compare(Map.Entry<String, Tool> o1,
                Map.Entry<String, Tool> o2) {
                return (o1.getValue().nodeName)
                    .compareTo(o2.getValue().nodeName);
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Tool> temp = new LinkedHashMap<String, Tool>();
        for(Map.Entry<String, Tool> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
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
     * Gets the computer name
     * 
     * @return
     */
    private static String getComputerName() {
        Map<String, String> env = System.getenv();
        if(env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else if(env.containsKey("HOSTNAME"))
            return env.get("HOSTNAME");
        else
            return "Unknown Computer";
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Running " + CSPEditImageTool.class.getSimpleName());
        System.out.println(new Date());
        System.out.println(getComputerName());

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
            conn = DriverManager.getConnection(databaseURL, null, null);
        } catch(Exception ex) {
            System.out.println("Error getting connection");
            ex.printStackTrace();
            return;
        }
        if(false) {
            getColumnNames(conn, "Node");
        }
        if(true) {
            getData(conn);
        }
        if(true) {
            listData();
        }
        if(true) {
            listHierarchy();
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

    /**
     * Tool is a class to hold the relevant data for a CSP Tool.
     * 
     * @author Kenneth Evans, Jr.
     */
    public static class Tool
    {
        public long id;
        public long nodeVariantID;
        public String nodeName;
        public String nodeUuid;
        public String nodeFirstChildUuid;
        public String nodeNextUuid;
        public String nodeSelectedUuid;

        Tool(long id, long nodeVariantID, String nodeName, String nodeUuid,
            String nodeFirstChildUuid, String nodeNextUuid,
            String nodeSelectedUuid) {
            this.id = id;
            this.nodeVariantID = nodeVariantID;
            this.nodeName = nodeName;
            this.nodeUuid = nodeUuid;
            this.nodeFirstChildUuid = nodeFirstChildUuid;
            this.nodeNextUuid = nodeNextUuid;
            this.nodeSelectedUuid = nodeSelectedUuid;
        }

        String info() {
            return String.format("%5d %5d %-45s %-32s %-32s %-32s %-32s", id,
                nodeVariantID, nodeName, nodeUuid, nodeFirstChildUuid,
                nodeNextUuid, nodeSelectedUuid);
        }
    }

}
