package au.edu.unsw.cse.comp9323.anz.analytics.request_readers;

import au.edu.unsw.cse.comp9323.anz.analytics.IGeneralOlapQuery;
import au.edu.unsw.cse.comp9323.anz.analytics.IQuery;
import javax.servlet.http.HttpServletRequest;

public class TerminalRelevantReqReader implements IRequestReader {

    private final IGeneralOlapQuery q;

    public TerminalRelevantReqReader(IQuery query) {
        q = (IGeneralOlapQuery) query;
    }

    public TerminalRelevantReqReader(IGeneralOlapQuery query) {
        q = query;
    }

    @Override
    public void readRequest(HttpServletRequest req) {

// TODO fetch terminal information => get data for similar terminals (in the same suburb)
        String p = req.getPathInfo();

    }

}
