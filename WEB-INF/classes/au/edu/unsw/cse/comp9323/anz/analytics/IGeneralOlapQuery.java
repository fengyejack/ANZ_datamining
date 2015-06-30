package au.edu.unsw.cse.comp9323.anz.analytics;

import java.util.List;

public interface IGeneralOlapQuery extends IQuery {

    public void addMeasure(String measure);

    public void groupBy(String ruleName);

    public void addFilter(String ruleName, String... values);

    public void addFilter(String ruleName, List<String> values);

    public void sortBy(String ruleName);

}
