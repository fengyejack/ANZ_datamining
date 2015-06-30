package au.edu.unsw.cse.comp9323.anz.analytics.request_readers;

import au.edu.unsw.cse.comp9323.anz.analytics.IGeneralOlapQuery;
import au.edu.unsw.cse.comp9323.anz.analytics.IQuery;
import javax.servlet.http.HttpServletRequest;

public class TimeRelevantReqReader implements IRequestReader {

    private final IGeneralOlapQuery q;

    public TimeRelevantReqReader(IQuery query) {
        q = (IGeneralOlapQuery) query;
    }

    public TimeRelevantReqReader(IGeneralOlapQuery query) {
        q = query;
    }

    @Override
    public void readRequest(HttpServletRequest req) {

        String p = req.getPathInfo();

        q.addMeasure("Transaction Amount");

        if (p.endsWith("/today")) {

            q.groupBy("Hour");
            q.addFilter("Relative Timeframe", "today");

        } else if (p.endsWith("/this_week")) {

            q.groupBy("Day");
            q.addFilter("Relative Timeframe", "this_week");

        } else if (p.endsWith("/this_month")) {

            q.groupBy("Day");
            q.addFilter("Relative Timeframe", "this_month");

        } else if (p.endsWith("/this_year")) {

            q.groupBy("Month");
            q.addFilter("Relative Timeframe", "this_year");

        } else if (p.endsWith("/overall")) {

            q.groupBy("Year");

        }

    }

}
