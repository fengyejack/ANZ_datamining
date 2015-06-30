package au.edu.unsw.cse.comp9323.anz.analytics.request_readers;

import javax.servlet.http.HttpServletRequest;

public interface IRequestReader {

    public void readRequest(HttpServletRequest req);
}
