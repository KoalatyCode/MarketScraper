package Database;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.json.*;

public class UpdateDatabase {

    public static void main(String[] args) {
        try {
            List<Integer> typeIdList = getAllTypeIDs();
            for(int typeId : typeIdList)
            {
            jsonFromUrl("https://esi.tech.ccp.is/latest/markets/"
                    + "10000002/orders/?datasource=tranquility&order_type=sell&"
                    + "type_id=" + typeId);
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public static List<Integer> getAllTypeIDs() {
        Connection con = DatabaseConnection.connection();
        String result = "";
        if (con == null) {
            result = "connection failure";
        }
        
        List<Integer> typeIdList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlStr = "SELECT * FROM modlist";

        try {
            ps = con.prepareStatement(sqlStr);
            rs = ps.executeQuery();
            while (rs.next()) {
                typeIdList.add(Integer.parseInt(rs.getString("typeID")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                DatabaseConnection.closeDatabaseConnection(con);
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        return typeIdList;
    }

    public static JsonArray jsonFromUrl(String url) throws IOException, JsonException {
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
