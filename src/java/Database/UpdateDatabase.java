package Database;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.json.*;
import pojo.MarketOrder;

public class UpdateDatabase {

    public static String result;

    public static void insertMarketOrders(List<MarketOrder> marketOrderList) {
        Connection con = DatabaseConnection.connection();
        if (con == null) {
            result = "connection failure";
            return;
        }
        PreparedStatement ps = null;
        try {
            StringBuilder sqlSB = new StringBuilder();
            sqlSB.append("INSERT INTO marketorders (duration, is_buy_order, issued, location_id, min_volume, order_id, price, `range`, system_id, type_id, volume_remain, volume_total, time_fetched) VALUES");

            for (int i = 0; i < marketOrderList.size() - 1; i++) {
                sqlSB.append(" (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?),");
            }

            sqlSB.setLength(sqlSB.length() - 1);

            ps = con.prepareStatement(sqlSB.toString());

            int valueCounter = 0;

            for (int i = 0; i < marketOrderList.size(); i++) {
                if (marketOrderList.get(i) != null) {
                    if(i == marketOrderList.size() - 1)
                    {
                        System.out.println("pause");
                    }
                    ps.setInt(++valueCounter, marketOrderList.get(i).getDuration());
                    ps.setBoolean(++valueCounter, marketOrderList.get(i).isIs_buy_order());
                    ps.setString(++valueCounter, marketOrderList.get(i).getIssued());
                    ps.setInt(++valueCounter, marketOrderList.get(i).getLocation_id());
                    ps.setInt(++valueCounter, marketOrderList.get(i).getMin_volume());
                    ps.setInt(++valueCounter, marketOrderList.get(i).getOrder_id());
                    ps.setDouble(++valueCounter, marketOrderList.get(i).getPrice());
                    ps.setString(++valueCounter, marketOrderList.get(i).getRange());
                    ps.setInt(++valueCounter, marketOrderList.get(i).getSystem_id());
                    ps.setInt(++valueCounter, marketOrderList.get(i).getType_id());
                    ps.setInt(++valueCounter, marketOrderList.get(i).getVolume_remain());
                    ps.setInt(++valueCounter, marketOrderList.get(i).getVolume_total());
                    ps.setTimestamp(++valueCounter, marketOrderList.get(i).getTimeStamp());
                    System.out.println(valueCounter);
                }
            }

            ps.executeUpdate();
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
    }

    public static void truncateTheDatabase() {
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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
    }

    public static void main(String[] args) {
        try {
            truncateTheDatabase();
            GetDataThreaded getData = new GetDataThreaded();
            getData.runGetData();
            insertMarketOrders(getData.getMarketOrderList());
        } catch (Exception ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
    }
}
