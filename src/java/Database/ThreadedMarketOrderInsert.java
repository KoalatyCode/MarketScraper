package Database;

import static Database.UpdateDatabase.result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import pojo.MarketOrder;

public class ThreadedMarketOrderInsert {

    private static Connection con = DatabaseConnection.connection();
    private static List<MarketOrder> marketOrderList;

    public static List<MarketOrder> getMarketOrderList() {
        return marketOrderList;
    }

    public static void setMarketOrderList(List<MarketOrder> marketOrderList) {
        ThreadedMarketOrderInsert.marketOrderList = marketOrderList;
    }
    
    public ThreadedMarketOrderInsert(List<MarketOrder> marketOrderList) {
        this.marketOrderList = marketOrderList;
    }

    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 0; i < marketOrderList.size(); i++) {
            executor.execute(new InsertMarketOrder(marketOrderList.get(i)));
        }
        executor.shutdown();

        while (!executor.isTerminated()) {
        }
    }

    private static class InsertMarketOrder implements Runnable {
        MarketOrder marketOrder;

        public MarketOrder getMarketOrder() {
            return marketOrder;
        }

        public void setMarketOrder(MarketOrder marketOrder) {
            this.marketOrder = marketOrder;
        }
        
        public InsertMarketOrder(MarketOrder marketOrder) {
            this.marketOrder = marketOrder;
        }

        @Override
        public void run() {
            try {
                insertMarketOrder(marketOrder);
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }

    }

    public static void insertMarketOrder(MarketOrder marketOrder) throws SQLException{
        if (con == null) {
            result = "connection failure";
            return;
        }

        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(
                "INSERT INTO marketorders "
                + "(duration, is_buy_order, issued, location_id, min_volume, order_id, price, `range`, system_id, type_id, volume_remain, volume_total, time_fetched) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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

            ps.executeUpdate();

        } finally
        {
            if(ps != null)
            {
                ps.close();
            }
        }
    }
}
