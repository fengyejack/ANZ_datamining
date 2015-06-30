package au.edu.unsw.cse.comp9323.anz.analytics.request_readers;

import au.edu.unsw.cse.comp9323.anz.analytics.IGeneralOlapQuery;
import au.edu.unsw.cse.comp9323.anz.analytics.IQuery;
import au.edu.unsw.cse.comp9323.anz.analytics.ServiceException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeneralQueryJsonReqReader implements IRequestReader {

    private final IGeneralOlapQuery q;

    public GeneralQueryJsonReqReader(IQuery query) {
        q = (IGeneralOlapQuery) query;
    }

    public GeneralQueryJsonReqReader(IGeneralOlapQuery query) {
        q = query;
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder s = new StringBuilder();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(is));
        String l;
        while ((l = br.readLine()) != null) {
            s.append(l);
        }
        return s.toString();
    }

    @Override
    public void readRequest(HttpServletRequest req) {
        try {

            JSONObject obj = new JSONObject(readStream(req.getInputStream()));
            JSONArray a;
            JSONObject o;
            String s;

            // measures
            /*
             "measures" : ["measure1", "measure2", ...]
             or
             "measure" : "measure"
             */
            a = obj.optJSONArray("measures");
            if (a != null) {
                for (int i = 0; i < a.length(); i++) {
                    q.addMeasure(a.getString(i));
                }
            } else {
                // at least one measure
                s = obj.getString("measure");
                q.addMeasure(s);
            }

            // group by
            /*
             "group_by" : ["rule1", "rule2", ...]
             or
             "group_by" : "rule"
             */
            a = obj.optJSONArray("group_by");
            if (a != null) {
                for (int i = 0; i < a.length(); i++) {
                    q.groupBy(a.getString(i));
                }
            } else {
                s = obj.optString("group_by");
                if (s != null) {
                    q.groupBy(s);
                }
            }

            // filters
            /*
             "filters" : {
             "{filter name}": [args],
             "..." : [...],
             ...
             }
             */
            o = obj.optJSONObject("filters");
            if (o != null) {
                for (String key : o.keySet()) {
                    List<String> args = new ArrayList<String>();
                    a = o.optJSONArray(key);
                    if (a != null) {
                        for (int i = 0; i < a.length(); i++) {
                            args.add(a.getString(i));
                        }
                    } else {
                        args.add(o.getString(key));
                    }
                    q.addFilter(key, args);
                }
            }

            // sort by
            /*
             "sort_by" : "sorting rule"
             */
            s = obj.optString("sort_by");
            if (s != null) {
                q.sortBy(s);
            }

        } catch (Exception ex) {
            if (ex instanceof ServiceException) {
                throw (ServiceException) ex;
            }
            throw new ServiceException(ex);
        }
    }

}
