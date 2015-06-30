package au.edu.unsw.cse.comp9323.anz.analytics.keenio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class KeenIODelegate {

    final private static String SERVICE = "https://api.keen.io/3.0/projects/";
    final private static SimpleDateFormat ISO_8601_FORMAT
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private final String projectId;
    private final String readKey;
    private final String eventCollection;

    private final StringBuilder query;

    public KeenIODelegate(String pId, String rkey, String eventCol) {
        projectId = pId;
        readKey = rkey;
        eventCollection = eventCol;
        query = new StringBuilder();
    }

    public void request(String analysisType, PrintWriter writer) throws MalformedURLException, IOException {

        String url = SERVICE + projectId + "/queries/" + analysisType;

        url += "?api_key=" + readKey + "&event_collection=" + eventCollection;

        URL service = new URL(url + query.toString());

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, service.toString());

        HttpURLConnection conn = (HttpURLConnection) service.openConnection();
        InputStream is;
        BufferedReader br;
        try {
            is = conn.getInputStream();
            br = new BufferedReader(
                    new InputStreamReader(is));
        } catch (Exception ex) {
            is = conn.getErrorStream();
            br = new BufferedReader(
                    new InputStreamReader(is));
        }
        // TODO format conversion
        String l;
        while ((l = br.readLine()) != null) {
            writer.println(l);
        }
        writer.close();

    }

    public void set(String property, String value) throws UnsupportedEncodingException {
        query.append('&').append(property).append('=')
                .append(URLEncoder.encode(value, "UTF-8"));
    }

    public void setAbsTimeframe(long start, long end) throws UnsupportedEncodingException {
        JSONObject tf = new JSONObject();
        tf.put("start", ISO_8601_FORMAT.format(
                new Date(start)));
        tf.put("end", ISO_8601_FORMAT.format(
                new Date(end)));
        set("timeframe", tf);
    }

    public void setRelTimeframe(String value) throws UnsupportedEncodingException {
        set("timeframe", value);
    }

    public void set(String property, JSONArray arr) throws UnsupportedEncodingException {
        set(property, arr.toString());
    }

    public void set(String property, JSONObject obj) throws UnsupportedEncodingException {
        set(property, obj.toString());
    }

}
