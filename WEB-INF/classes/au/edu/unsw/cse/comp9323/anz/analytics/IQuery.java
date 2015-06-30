package au.edu.unsw.cse.comp9323.anz.analytics;

import java.io.PrintWriter;

public interface IQuery {

    public void execute(PrintWriter writer);

    public void close();
}
