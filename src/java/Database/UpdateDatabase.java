package Database;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.json.*;

public class UpdateDatabase {

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

    public static class JSONData implements Runnable {

        String currentURL = "https://esi.tech.ccp.is/latest/markets/10000002/orders/?datasource=tranquility&order_type=sell&type_id=";
        int currentTypeID;

        public JSONData(int currentTypeID) {
            this.currentTypeID = currentTypeID;
        }

        @Override
        public void run() {
            try {
                jsonFromUrl(currentURL + currentTypeID);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        public void jsonFromUrl(String url) throws IOException, JsonException {
            String isBuyOrder;
            int locationID;
            long orderID;
            double isk;
            int systemID;
            int typeID;
            int qty;
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

            InputStream is = new URL(url).openStream();

            JsonReader jreader = Json.createReader(is);
            JsonArray jsonArray = jreader.readArray();
            try {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jObject = jsonArray.getJsonObject(i);
                    isBuyOrder = jObject.get("is_buy_order").toString();
                    locationID = jObject.getJsonNumber("location_id").intValue();
                    orderID = jObject.getJsonNumber("order_id").longValue();
                    isk = jObject.getJsonNumber("price").doubleValue();
                    systemID = jObject.getJsonNumber("system_id").intValue();
                    typeID = jObject.getJsonNumber("type_id").intValue();
                    qty = jObject.getJsonNumber("volume_remain").intValue();

                    System.out.println(
                            "typeID: " + typeID + "\n"
                            + " qty: " + qty + "\n"
                            + " price : " + String.format("%.0f", isk) + "\n");

                    insertMarketOrder(isBuyOrder, locationID, orderID, isk, systemID, typeID, qty, currentTimestamp);
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        public void insertMarketOrder(String isBuyOrder, int locationID, long orderID, double isk, int systemID, int typeID, int qty, Timestamp currentTimestamp) {
            String result = "";
            if (con == null) {
                result = "connection failure";
                return;
            }
            PreparedStatement insertMarketOrder = null;
            try {
                insertMarketOrder = con.prepareStatement(
                        "INSERT INTO marketorder (isBuyOrder, locationID, orderID, isk, systemID, typeID, qty, timeFetched) "
                        + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
                insertMarketOrder.setString(1, isBuyOrder);
                insertMarketOrder.setInt(2, locationID);
                insertMarketOrder.setLong(3, orderID);
                insertMarketOrder.setDouble(4, isk);
                insertMarketOrder.setInt(5, systemID);
                insertMarketOrder.setInt(6, typeID);
                insertMarketOrder.setInt(7, qty);
                insertMarketOrder.setTimestamp(8, currentTimestamp);

                int updateCount = insertMarketOrder.executeUpdate();
                result = "number of rows affected " + updateCount;
            } catch (Exception ex) {
                System.err.println(ex);
                ex.printStackTrace();
            }
        }
    }

    public static void truncateTheDatabase() {
        try {
            PreparedStatement ps = con.prepareStatement("TRUNCATE TABLE marketorder");
            ps.execute();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    static List<Integer> typeIDList = getAllTypeIDs();

    public static void main(String[] args) {
        try {
            truncateTheDatabase();
            ExecutorService executor = Executors.newFixedThreadPool(50);

            for (int typeID : typeIDList) {
                executor.execute(new JSONData(typeID));
            }
            executor.shutdown();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
