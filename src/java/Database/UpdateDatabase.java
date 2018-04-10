/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Database;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.json.*;

/**
 *
 * @author home
 */
public class UpdateDatabase {

    public static void main(String[] args) {
        try {
            readJsonFromUrl("https://esi.tech.ccp.is/latest/markets/"
                    + "10000002/orders/?datasource=tranquility&order_type=sell&"
                    + "type_id=40691");
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public static JsonArray readJsonFromUrl(String url) throws IOException, JsonException {
        long orderId;
        int typeId;
        int volumeRemain;
        double price;
        InputStream is = new URL(url).openStream();
        JsonReader jreader = Json.createReader(is);
        JsonArray jsonArray = jreader.readArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jObject = jsonArray.getJsonObject(i);
            orderId = jObject.getJsonNumber("order_id").longValue();
            typeId = jObject.getJsonNumber("type_id").intValue();
            volumeRemain = jObject.getJsonNumber("volume_remain").intValue();
            price = jObject.getJsonNumber("price").doubleValue();
            System.out.println(
                    "orderid: " + orderId + "\n"
                    + " typeid: " + typeId + "\n"
                    + " volumeremain: " + volumeRemain + "\n"
                    + " price : " + String.format("%.0f", price) + "\n");

            insertMarketOrder(orderId, typeId, volumeRemain, price);
        }
        return jsonArray;
    }

    public static void insertMarketOrder(long orderID, int typeID, int volumeRemain, double price) {
        Connection con = DatabaseConnection.connection();
        String result = "";
        if (con == null) {
            result = "connection failure";
            return;
        }
        PreparedStatement insertMarketOrder = null;
        try {
            insertMarketOrder = con.prepareStatement(
                    "INSERT INTO marketorders (orderID, typeID, volumeRemain, price) "
                    + "VALUES(?, ?, ?, ?)");
            insertMarketOrder.setLong(1, orderID);
            insertMarketOrder.setInt(2, typeID);
            insertMarketOrder.setInt(3, volumeRemain);
            insertMarketOrder.setDouble(4, price);

            int updateCount = insertMarketOrder.executeUpdate();
            result = "number of rows affected " + updateCount;
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
        } finally {
            try {
                DatabaseConnection.closeDatabaseConnection(con);
                if (insertMarketOrder != null) {
                    insertMarketOrder.close();
                }
            } catch (SQLException sqle) {
                System.err.println(sqle);
                sqle.printStackTrace();
            }
        }
    }
}
