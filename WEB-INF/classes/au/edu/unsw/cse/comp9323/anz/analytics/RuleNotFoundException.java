package au.edu.unsw.cse.comp9323.anz.analytics;

public class RuleNotFoundException extends ServiceException {

    public RuleNotFoundException() {
    }

    public RuleNotFoundException(String msg) {
        super(msg);
    }

    public RuleNotFoundException(Throwable t) {
        super(t);
    }

}
