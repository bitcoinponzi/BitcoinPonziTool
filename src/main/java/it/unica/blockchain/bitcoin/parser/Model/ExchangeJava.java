package it.unica.blockchain.bitcoin.parser.Model;

import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import org.apache.commons.lang3.time.DateUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ExchangeJava {

    static Date lastDate = new Date(1279411200000l);
    static double price;

    public static double getRate(Date date) {

        if (date.before(new Date(1279411200000l))) return 0;

        if (!DateUtils.isSameDay(date, lastDate)) {
            lastDate = date;

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String sDate = format.format(date);
            StringBuffer text = new StringBuffer();


            try {
                URL url = new URL("http://api.coindesk.com/v1/bpi/historical/close.json?start=" + sDate + "&end=" + sDate);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    text.append(line);
                }
                reader.close();
            } catch (IOException e) {
                return 0;
            }

            JsonParserFactory factory = JsonParserFactory.getInstance();
            JSONParser parser = factory.newJsonParser();
            Map map = parser.parseJson(text.toString());

            Map<String, String> bpi = (Map) map.get("bpi");
            price = Double.parseDouble(bpi.get(sDate));
        }
        return price;

    }
}


