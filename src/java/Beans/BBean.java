package Beans;

import Database.DatabaseConnection;
import static Database.UpdateDatabase.getAllTypeIDs;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named(value = "bBean")
@SessionScoped
public class BBean implements Serializable {

    public BBean() {
    }

    public void clearTable() {
        outputTable = "";
    }

    private String outputTable;
    private double buyoutPrice;
    private int buyoutQTY;

    public int getBuyOutQTY() {
        return buyoutQTY;
    }

    public void setBuyOutQTY(int buyOutQTY) {
        this.buyoutQTY = buyOutQTY;
    }

    public double getBuyOutPrice() {
        return buyoutPrice;
    }

    public void setBuyOutPrice(double buyOutPrice) {
        this.buyoutPrice = buyOutPrice;
    }

    public String getOutputTable() {
        return outputTable;
    }

    public void setOutputTable(String outputTable) {
        this.outputTable = outputTable;
    }

    public static Connection con = DatabaseConnection.connection();

    public static List<Integer> getAllTypeIDs() {

        String result = "";
        if (con == null) {
            result = "connection failure";
        }

        List<Integer> typeIdList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlStr = "SELECT typeID FROM items";

        try {
            ps = con.prepareStatement(sqlStr);
            rs = ps.executeQuery();
            while (rs.next()) {
                typeIdList.add(Integer.parseInt(rs.getString("typeID")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return typeIdList;
    }

    public void getOrders() {
        List<MarketOrders> marketOrders = new ArrayList<>();
        List<Integer> typeIDList = getAllTypeIDs();
        clearTable();
        
        Connection con = DatabaseConnection.connection();
        String result = "";

        if (con == null) {
            result = "connection failure";
            return;
        }

        PreparedStatement selectMarketOrder = null;
        try {
            for (int i = 0; i < typeIDList.size(); i++) {
                selectMarketOrder = con.prepareStatement(
                        "SELECT items.typeName, "
                                + "SUM(marketorder.isk * marketorder.qty) AS TotalISK, "
                                + "SUM(marketorder.qty) AS TotalQTY, "
                                + "marketorder.timeFetched "
                        + "FROM items "
                        + "JOIN marketorder ON marketorder.typeID = items.typeID "
                        + "WHERE items.typeID = ? AND "
                                + "TotalISK <= ? AND "
                                + "TotalQTY <= ? AND "
                                + "marketorder.locationID = 60003760 AND "
                                + "marketorder.isBuyOrder = FALSE"
                        + "GROUP BY items.typeName, marketorder.timeFetched");
                selectMarketOrder.setInt(1, typeIDList.get(i));
                selectMarketOrder.setDouble(2, buyoutPrice);
                selectMarketOrder.setInt(3, buyoutQTY);

                ResultSet rs = selectMarketOrder.executeQuery();

                while (rs.next()) {
                    marketOrders.add(new MarketOrders(
                            rs.getString("typeName"),
                            rs.getTimestamp("timeFetched"),
                            rs.getInt("TotalQTY"),
                            rs.getDouble("TotalISK")));
                }
            }

            buildOutputTable(marketOrders);

        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();

        } finally {
            try {
                DatabaseConnection.closeDatabaseConnection(con);
                if (selectMarketOrder != null) {
                    selectMarketOrder.close();
                }

            } catch (SQLException sqle) {
                System.err.println(sqle);
                sqle.printStackTrace();
            }
        }
    }

    public void buildOutputTable(List<MarketOrders> marketOrders) {
        outputTable = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String currentTimestamp = new Timestamp(System.currentTimeMillis()).toString();

        for (int i = 1; i < marketOrders.size(); i++) {
            try {
                NumberFormat nf = NumberFormat.getNumberInstance();
                Date d1 = format.parse(currentTimestamp);
                Date d2 = format.parse(marketOrders.get(i).timeFetched.toString());
                long diff = d2.getTime() - d1.getTime();
                long diffMinutes = diff / (60 * 1000) % 60;

                outputTable
                        += "<tr>"
                        + "<td>"
                        + marketOrders.get(i).typeName
                        + "</td>"
                        + "<td>"
                        + nf.format(marketOrders.get(i).totalISK)
                        + "</td>"
                        + "<td>"
                        + nf.format(marketOrders.get(i).totalQTY)
                        + "</td>"
                        + "<td>"
                        + diffMinutes + "minutes ago"
                        + "</td>"
                        + "</tr>";
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public class MarketOrders {

        String typeName;
        Timestamp timeFetched;
        int locationID = 60003760;
        int totalQTY;
        double totalISK;

        public MarketOrders(String typeName, Timestamp timeFetched, int totalQTY, double totalISK) {
            this.typeName = typeName;
            this.timeFetched = timeFetched;
            this.totalQTY = totalQTY;
            this.totalISK = totalISK;
        }
        
        public double getTotalISK() {
            return totalISK;
        }

        public void setTotalISK(double totalISK) {
            this.totalISK = totalISK;
        }

        public int getTotalQTY() {
            return totalQTY;
        }

        public void setTotalQTY(int totalQTY) {
            this.totalQTY = totalQTY;
        }

        public int getLocationID() {
            return locationID;
        }

        public void setLocationID(int locationID) {
            this.locationID = locationID;
        }

        public Timestamp getTimeFetched() {
            return timeFetched;
        }

        public void setTimeFetched(Timestamp timeFetched) {
            this.timeFetched = timeFetched;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }
}
