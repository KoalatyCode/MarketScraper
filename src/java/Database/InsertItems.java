package Database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class InsertItems {

    public static void main(String[] args) {        
        JSONParser parser = new JSONParser();
        Connection con = DatabaseConnection.connection();
        PreparedStatement ps = null;
        String sqlStr = "INSERT into items (typeID, typeName) values (?, ?)";
        
        try {
            JSONArray a = (JSONArray) parser.parse(new FileReader("c:/users/home/desktop/devsort/allTypeID.json"));
            
            for (Object o : a) {
                JSONObject item = (JSONObject) o;

                long longTypeID = (long) item.get("typeID");
                System.out.println(longTypeID);
                String typeName = (String) item.get("typeName");
                System.out.println(typeName);
                
                int intTypeID = (int) longTypeID;
                ps = con.prepareStatement(sqlStr);
                ps.setInt(1, intTypeID);
                ps.setString(2, typeName);
                ps.execute();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } 
    }

}
