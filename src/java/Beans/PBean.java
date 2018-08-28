package Beans;

import Database.DatabaseConnection;
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
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.event.ComponentSystemEvent;

@Named(value = "pBean")
@SessionScoped
public class PBean implements Serializable {

    public PBean() {
    }

    public void clearTable() {
        outputTable = "";
    }

    private int inputTypeID;
    private String outputTable;

    public String getOutputTable() {
        return outputTable;
    }

    public void setOutputTable(String outputTable) {
        this.outputTable = outputTable;
    }

    public int getInputTypeID() {
        return inputTypeID;
    }

    public void setInputTypeID(int inputTypeID) {
        this.inputTypeID = inputTypeID;
    }

    public void getOrdersByTypeID() {

        List<MarketOrders> marketOrders = new ArrayList<>();
        Connection con = DatabaseConnection.connection();
        String result = "";

        if (con == null) {
            result = "connection failure";
            return;
        }

        PreparedStatement selectMarketOrder = null;
        try {
            selectMarketOrder = con.prepareStatement(
                    "SELECT items.typeName, marketorder.locationID, marketorder.isk, marketorder.qty, marketorder.timeFetched, marketorder.isBuyOrder "
                    + "FROM items "
                    + "JOIN marketorder ON marketorder.typeID = items.typeID "
                    + "WHERE items.typeID = ?");
            selectMarketOrder.setInt(1, this.inputTypeID);

            ResultSet rs = selectMarketOrder.executeQuery();

            while (rs.next()) {
                marketOrders.add(new MarketOrders(
                        rs.getString("typeName"),
                        rs.getInt("locationID"),
                        rs.getDouble("isk"),
                        rs.getInt("qty"),
                        rs.getTimestamp("timeFetched"),
                        rs.getBoolean("isBuyOrder")));
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
                        + nf.format(marketOrders.get(i).isk)
                        + "</td>"
                        + "<td>"
                        + nf.format(marketOrders.get(i).qty)
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
        int locationID;
        double isk;
        int qty;
        boolean isBuyOrder;

        public MarketOrders(String typeName, int locationID, double isk, int qty, Timestamp timeFetched, boolean isBuyOrder) {
            this.typeName = typeName;
            this.timeFetched = timeFetched;
            this.locationID = locationID;
            this.isk = isk;
            this.qty = qty;
            this.isBuyOrder = isBuyOrder;
        }

        public boolean isIsBuyOrder() {
            return isBuyOrder;
        }

        public void setIsBuyOrder(boolean isBuyOrder) {
            this.isBuyOrder = isBuyOrder;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public double getIsk() {
            return isk;
        }

        public void setIsk(double isk) {
            this.isk = isk;
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
