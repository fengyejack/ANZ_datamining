package au.edu.unsw.cse.comp9323.anz.analytics;

import org.json.JSONObject;

public class ServiceException extends RuntimeException {

    public ServiceException() {
    }

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(Throwable t) {
        super(t);
    }

    @Override
    public String toString() {

        JSONObject obj = new JSONObject();
        obj.put("message", getMessage());

//        obj.put("errorCode", );
        return obj.toString();

    }
}
