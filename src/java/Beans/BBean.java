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
import java.util.List;
import pojo.MarketOrder;

@Named(value = "bBean")
@SessionScoped
public class BBean implements Serializable {

    public BBean() {
    }

    private String outputTable;
    private double buyoutPrice;
    private double buyoutQTY;
    static String result = "";

    public double getBuyOutQTY() {
        return buyoutQTY;
    }

    public void setBuyOutQTY(double buyOutQTY) {
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

    public void clearTable() {
        outputTable = "";
    }

    public void getOrders() throws SQLException {
        List<MarketOrder> marketOrders = new ArrayList<>();
        clearTable();

        Connection con = DatabaseConnection.connection();

        if (con == null) {
            result = "connection failure";
            return;
        }

        PreparedStatement selectMarketOrder = null;
        try {
            selectMarketOrder = con.prepareStatement(
                    "SELECT marketorders.type_id, "
                    + "items.type_name, "
                    + "ROUND(SUM(marketorders.price * marketorders.volume_remain), 2) AS TotalISK, "
                    + "SUM(marketorders.volume_remain) AS TotalQTY "
                    + "FROM items "
                    + "JOIN marketorders ON marketorders.type_id = items.type_id "
                    + "WHERE marketorders.is_buy_order = 0 "
                    + "GROUP BY marketorders.type_id, items.type_name");

            ResultSet rs = selectMarketOrder.executeQuery();

            while (rs.next()) {
                if (rs.getDouble("TotalISK") <= buyoutPrice 
                    && rs.getDouble("TotalQTY") <= buyoutQTY
                    && rs.getDouble("TotalQTY") >= 10
                    && !(rs.getString("type_name").contains("Blueprint"))) {
                    marketOrders.add(new MarketOrder(
                            rs.getInt("type_id"),
                            rs.getString("type_name"),
                            rs.getDouble("TotalISK"),
                            rs.getDouble("TotalQTY")));
                }
            }

            buildOutputTable(marketOrders);

        } finally {
            DatabaseConnection.closeDatabaseConnection(con);
            if (selectMarketOrder != null) {
                selectMarketOrder.close();
            }
        }
    }

    public void buildOutputTable(List<MarketOrder> marketOrders) {
        outputTable = "";

        for (int i = 1; i < marketOrders.size(); i++) {
            try {
                NumberFormat nf = NumberFormat.getNumberInstance();

                outputTable
                        += "<tr>"
                        + "<td>"
                        + "<img src=\"https://image.eveonline.com/Type/" + marketOrders.get(i).getType_id() + "_32.png\" width=\"32\" height=\"32\"/>"
                        + "</td>"
                        + "<td>"
                        + marketOrders.get(i).getType_name()
                        + "</td>"
                        + "<td>"
                        + nf.format(marketOrders.get(i).getBuyout_price())
                        + "</td>"
                        + "<td>"
                        + nf.format(marketOrders.get(i).getBuyout_volume_total())
                        + "</td>"
                        + "</tr>";
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
