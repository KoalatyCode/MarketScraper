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
        int orderId;
        InputStream is = new URL(url).openStream();
        JsonReader jreader = Json.createReader(is);
        JsonArray jsonArray = jreader.readArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            
            //Null pointer exception
            orderId = Integer.parseInt(jsonArray.getJsonObject(i).get("orderI_id").toString());
            JsonValue typeId = jsonArray.getJsonObject(i).get("type_id");
            JsonValue volumeRemain = jsonArray.getJsonObject(i).get("volume_remain");
            JsonValue price = jsonArray.getJsonObject(i).get("price");
            System.out.println(
                    "orderid: " + orderId + "\n" 
                    + " typeid: " + typeId.toString() + "\n" 
                    + " volumeremain: " + volumeRemain.toString() + "\n"
                    + " price: " + price.toString() + "\n");
            //insertMarketOrder(orderId, typeId, volumeRemain, price);
        }
        return jsonArray;
    }

    public static void insertMarketOrder(int orderID, int typeID, int volumeRemain, String price) {
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
            insertMarketOrder.setInt(1, orderID);
            insertMarketOrder.setInt(2, typeID);
            insertMarketOrder.setInt(3, volumeRemain);
            insertMarketOrder.setString(4, price);

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
