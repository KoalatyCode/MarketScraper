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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import pojo.MarketOrder;

@Named(value = "pBean")
@SessionScoped
public class PBean implements Serializable {

    public PBean() {
    }

    private int inputTypeID;
    private String inputTypeName;
    private String outputTable;
    public static String result = "";

    public String getOutputTable() {
        return outputTable;
    }

    public int getInputTypeID() {
        return inputTypeID;
    }

    public void setInputTypeID(int inputTypeID) {
        this.inputTypeID = inputTypeID;
    }

    public void setOutputTable(String outputTable) {
        this.outputTable = outputTable;
    }

    public String getInputTypeName() {
        return inputTypeName;
    }

    public void setInputTypeName(String inputTypeName) {
        this.inputTypeName = inputTypeName;
    }

    public void clearTable() {
        outputTable = "";
    }

    public void getWildCards() throws SQLException {
        Connection con = DatabaseConnection.connection();

        if (con == null) {
            result = "connection failure";
            return;
        }
        
        PreparedStatement ps = null;

        try {

        } finally {
            DatabaseConnection.closeDatabaseConnection(con);
            if (ps != null) {
                ps.close();
            }
        }

    }

    public void getOrdersByTypeName(String typeName) throws SQLException {
        Connection con = DatabaseConnection.connection();

        if (con == null) {
            result = "connection failure";
            return;
        }

        List<MarketOrder> marketOrders = new ArrayList<>();
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(
                    "SELECT items.type_id, items.type_name, marketorders.price, marketOrders.time_fetched"
                    + "FROM items "
                    + "JOIN marketorders ON marketorders.type_id = items.type_id "
                    + "WHERE items.type_name LIKE ? AND "
                    + "marketorders.is_buy_order = 1");
            ps.setString(1, typeName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                marketOrders.add(new MarketOrder(
                        rs.getString("type_name"),
                        rs.getDouble("price"),
                        rs.getInt("volume_remain"),
                        rs.getTimestamp("time_fetched")));
            }

            buildOutputTable(marketOrders);

        } finally {
            DatabaseConnection.closeDatabaseConnection(con);
            if (ps != null) {
                ps.close();
            }
        }
    }
    
    public void getOrdersByTypeID() throws SQLException {
        Connection con = DatabaseConnection.connection();

        if (con == null) {
            result = "connection failure";
            return;
        }

        List<MarketOrder> marketOrders = new ArrayList<>();
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(
                    "SELECT items.type_id, items.type_name, marketorders.price, marketorders.volume_remain, marketOrders.time_fetched "
                    + "FROM items "
                    + "JOIN marketorders ON marketorders.type_id = items.type_id "
                    + "WHERE marketorders.type_id = ? AND "
                    + "marketorders.is_buy_order = 0");
            ps.setInt(1, inputTypeID);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                marketOrders.add(new MarketOrder(
                        rs.getString("type_name"),
                        rs.getDouble("price"),
                        rs.getInt("volume_remain"),
                        rs.getTimestamp("time_fetched")));
            }

            buildOutputTable(marketOrders);

        } finally {
            DatabaseConnection.closeDatabaseConnection(con);
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void buildOutputTable(List<MarketOrder> marketOrders) {
        outputTable = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String currentTimestamp = new Timestamp(System.currentTimeMillis()).toString();

        for (int i = 1; i < marketOrders.size(); i++) {
            try {
                NumberFormat nf = NumberFormat.getNumberInstance();
                Date d1 = format.parse(currentTimestamp);
                Date d2 = format.parse(marketOrders.get(i).getTimestamp().toString());
                long diff = d2.getTime() - d1.getTime();
                long diffMinutes = diff / (60 * 1000) % 60;

                outputTable
                        += "<tr>"
                        + "<td>"
                        + "<img src=\"https://image.eveonline.com/Type/" + this.inputTypeID + "_32.png\" width=\"32\" height=\"32\"/>"
                        + "</td>"
                        + "<td>"
                        + marketOrders.get(i).getType_name()
                        + "</td>"
                        + "<td>"
                        + nf.format(marketOrders.get(i).getPrice())
                        + "</td>"
                        + "<td>"
                        + nf.format(marketOrders.get(i).getVolume_remain())
                        + "</td>"
                        + "<td>"
                        + diffMinutes + "minutes ago"
                        + "</td>"
                        + "</tr>";
            } catch (ParseException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
