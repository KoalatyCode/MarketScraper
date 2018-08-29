package Database;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.json.JsonObject;
import pojo.MarketOrder;
import javax.json.*;

public class GetDataThreaded {

    public List<MarketOrder> marketOrderList = new ArrayList<>();

    public List<MarketOrder> getMarketOrderList() {
        return marketOrderList;
    }

    public void runGetData() {
        int pages = 250;
        Map<String, List<String>> responseHeaders = getResponseHeaders();
        
        for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            if (entry.getKey() != null) {
                if (entry.getKey().equals("X-Pages")) {
                    String str = entry.getValue().toString();
                    str = str.replaceAll("\\[", "").replaceAll("\\]","");
                    pages = Integer.parseInt(str);
                }
            }
        }
        
        ExecutorService executor = Executors.newFixedThreadPool(100);
        for (int i = 1; i <= pages; i++) {
            executor.execute(new GetDataFromPage(i));
        }
        
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        
        System.out.println(marketOrderList.size());
    }

    public static Map<String, List<String>> getResponseHeaders() {
        try {
            URL obj = new URL("https://esi.tech.ccp.is/latest/markets/10000002/orders/?datasource=tranquility");
            URLConnection conn = obj.openConnection();
            Map<String, List<String>> map = conn.getHeaderFields();

            return map;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class GetDataFromPage implements Runnable {

        private int page;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public GetDataFromPage(int page) {
            this.page = page;
        }

        @Override
        public void run() {
            GetData getData = new GetData(page);
            getData.fetchData();
        }

    }

    private class GetData {
        int page;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }
        
        public GetData(int page) {
            this.page = page;
        }

        public void fetchData() {
            try {
                String url = "https://esi.tech.ccp.is/latest/markets/10000002/orders/?datasource=tranquility&page=";
                InputStream is = new URL(url + page).openStream();
                JsonReader jreader = Json.createReader(is);
                JsonArray jsonArray = jreader.readArray();

                for (int i = 0; i < jsonArray.size(); i++) {
                    MarketOrder marketOrder = new MarketOrder();
                    JsonObject jObject = jsonArray.getJsonObject(i);

                    marketOrder.setDuration(jObject.getInt("duration"));
                    marketOrder.setIs_buy_order(jObject.getBoolean("is_buy_order"));
                    marketOrder.setIssued(jObject.getString("issued"));
                    marketOrder.setLocation_id(jObject.getInt("location_id"));
                    marketOrder.setMin_volume(jObject.getInt("min_volume"));
                    marketOrder.setOrder_id(jObject.getInt("order_id"));
                    marketOrder.setPrice(jObject.getJsonNumber("price").doubleValue());
                    marketOrder.setRange(jObject.getString("range"));
                    marketOrder.setSystem_id(jObject.getInt("system_id"));
                    marketOrder.setType_id(jObject.getInt("type_id"));
                    marketOrder.setVolume_remain(jObject.getInt("volume_remain"));
                    marketOrder.setVolume_total(jObject.getInt("volume_total"));
                    marketOrder.setTimeStamp(new Timestamp(System.currentTimeMillis()));

                    marketOrderList.add(marketOrder);

                    System.out.println("Market Order Type ID: " + marketOrder.getType_id() + "\n Page Number: " + page);

                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
