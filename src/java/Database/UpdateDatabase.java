package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import pojo.MarketOrder;

public class UpdateDatabase {

    public static String result;

    public static void insertMarketOrders(List<MarketOrder> marketOrderList) throws SQLException {
        Connection con = DatabaseConnection.connection();
        if (con == null) {
            result = "connection failure";
            return;
        }
        
        PreparedStatement ps = null;
        
        try {
            String sql = "INSERT INTO marketorders "
                + "(duration, is_buy_order, issued, location_id, min_volume, order_id, price, `range`, system_id, type_id, volume_remain, volume_total, time_fetched) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            ps = con.prepareStatement(sql);

            for (MarketOrder marketOrder : marketOrderList) {
                if (marketOrder != null) {
                    ps.setInt(1, marketOrder.getDuration());
                    ps.setBoolean(2, marketOrder.isIs_buy_order());
                    ps.setString(3, marketOrder.getIssued());
                    ps.setLong(4, marketOrder.getLocation_id());
                    ps.setInt(5, marketOrder.getMin_volume());
                    ps.setInt(6, marketOrder.getOrder_id());
                    ps.setDouble(7, marketOrder.getPrice());
                    ps.setString(8, marketOrder.getRange());
                    ps.setInt(9, marketOrder.getSystem_id());
                    ps.setInt(10, marketOrder.getType_id());
                    ps.setInt(11, marketOrder.getVolume_remain());
                    ps.setInt(12, marketOrder.getVolume_total());
                    ps.setTimestamp(13, marketOrder.getTimestamp());
                    ps.addBatch();
                }
            }

            ps.executeBatch();
            
        } finally {
            
            DatabaseConnection.closeDatabaseConnection(con);
            if (ps != null) {
                ps.close();
            }
        }
    }

    public static void truncateTheDatabase() throws SQLException {
        
        Connection con = DatabaseConnection.connection();
        if (con == null) {
            result = "connection failure";
            return;
        }

        PreparedStatement ps = null;
        String sql = "TRUNCATE TABLE marketorders";

        try {
            ps = con.prepareStatement(sql);
            ps.execute();
        } finally {
            DatabaseConnection.closeDatabaseConnection(con);
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void run() {
        try {
            truncateTheDatabase();
            GetDataThreaded getData = new GetDataThreaded();
            getData.runGetData();
            insertMarketOrders(getData.getMarketOrderList());
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        }
    }
}
