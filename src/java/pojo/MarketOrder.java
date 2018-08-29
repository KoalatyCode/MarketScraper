package pojo;

import java.sql.Timestamp;

public class MarketOrder {
    int duration;
    boolean is_buy_order;
    String issued;
    int location_id;
    int min_volume;
    int order_id;
    double price;
    String range;
    int system_id;
    int type_id;
    int volume_remain;
    int volume_total;
    Timestamp timeStamp;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isIs_buy_order() {
        return is_buy_order;
    }

    public void setIs_buy_order(boolean is_buy_order) {
        this.is_buy_order = is_buy_order;
    }

    public String getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = issued;
    }

    public int getLocation_id() {
        return location_id;
    }

    public void setLocation_id(int location_id) {
        this.location_id = location_id;
    }

    public int getMin_volume() {
        return min_volume;
    }

    public void setMin_volume(int min_volume) {
        this.min_volume = min_volume;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public int getSystem_id() {
        return system_id;
    }

    public void setSystem_id(int system_id) {
        this.system_id = system_id;
    }

    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
    }

    public int getVolume_remain() {
        return volume_remain;
    }

    public void setVolume_remain(int volume_remain) {
        this.volume_remain = volume_remain;
    }

    public int getVolume_total() {
        return volume_total;
    }

    public void setVolume_total(int volume_total) {
        this.volume_total = volume_total;
    }
    
    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public MarketOrder(int duration, boolean is_buy_order, String issued, int location_id, int min_volume, int order_id, double price, String range, int system_id, int type_id, int volume_remain, int volume_total) {
        this.duration = duration;
        this.is_buy_order = is_buy_order;
        this.issued = issued;
        this.location_id = location_id;
        this.min_volume = min_volume;
        this.order_id = order_id;
        this.price = price;
        this.range = range;
        this.system_id = system_id;
        this.type_id = type_id;
        this.volume_remain = volume_remain;
        this.volume_total = volume_total;
    }

    public MarketOrder() {
    }
    
    
}
