package au.edu.unsw.cse.comp9323.anz.analytics.request_readers;

import au.edu.unsw.cse.comp9323.anz.analytics.IGeneralOlapQuery;
import au.edu.unsw.cse.comp9323.anz.analytics.IQuery;
import javax.servlet.http.HttpServletRequest;

public class CustomerRelevantReqReader implements IRequestReader {

    private final IGeneralOlapQuery q;

    public CustomerRelevantReqReader(IQuery query) {
        q = (IGeneralOlapQuery) query;
    }

    public CustomerRelevantReqReader(IGeneralOlapQuery query) {
        q = query;
    }

    @Override
    public void readRequest(HttpServletRequest req) {
// TODO fetch customer information => get data for similar customers (of the same age/gender/postcode)
        String p = req.getPathInfo();

    }

}
