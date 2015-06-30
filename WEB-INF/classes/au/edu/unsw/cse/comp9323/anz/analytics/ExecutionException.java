package au.edu.unsw.cse.comp9323.anz.analytics;

public class ExecutionException extends ServiceException {

    public ExecutionException() {
    }

    public ExecutionException(String msg) {
        super(msg);
    }

    public ExecutionException(Throwable t) {
        super(t);
    }

}
