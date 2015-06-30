package au.edu.unsw.cse.comp9323.anz.analytics.olap4j;

import au.edu.unsw.cse.comp9323.anz.analytics.ExecutionException;
import au.edu.unsw.cse.comp9323.anz.analytics.IGeneralOlapQuery;
import au.edu.unsw.cse.comp9323.anz.analytics.RuleNotFoundException;
import au.edu.unsw.cse.comp9323.anz.analytics.ServiceException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;

final public class GeneralOlap4jQuery implements IGeneralOlapQuery {

    final private static Set<String> _measures;
    final private static Map<String, GroupingRule> _groupingRules;
    final private static Map<String, FilteringRule> _filteringRules;

    private static class GroupingRule {

        /*
         * <pre>
         * - BY_ALL: aggregate data as a whole
         * - BY_DESCENDENTS: aggregate data
         * for each descendent
         * - BY_CHILDREN: aggregate data for direct
         * descendents
         * </pre>
         */
        final public static int MODE_BY_ALL = 0;
        final public static int MODE_BY_DESCENDENTS = 1;
        final public static int MODE_BY_CHILDREN = 2;

        int mode;

        String dimension;
        String hierarchy;
        String level;
        // String member;

        public GroupingRule(String dimension, String hierarchy, int mode) {
            this.dimension = dimension;
            this.hierarchy = hierarchy;
            this.level = null;
            // this.member = null;
            this.mode = mode;
        }

        public GroupingRule(String dimension, String hierarchy, String level) {
            // this(dimension, hierarchy, level, null);
            this.dimension = dimension;
            this.hierarchy = hierarchy;
            this.level = level;
            // this.member = member;
            this.mode = MODE_BY_CHILDREN;
        }

        public void apply(GeneralOlap4jQueryImpl q) throws OlapException {

            if (level == null) {
                q.groupBy(dimension, hierarchy, mode == MODE_BY_CHILDREN);
            } else {
                q.groupBy(dimension, hierarchy, level);
            }

        }

    }

    private static class FilteringRule {

        /*
         * <pre>
         * - EQUAL: select the exact data member
         * - IN: select all data members
         * in/belonging to a parent member
         * - BETWEEN: select data members
         * between two specified data members
         * </pre>
         */
        enum Operator {

            EQUAL,
            IN,
            BETWEEN
        }

        Operator operator;

        /*
         * <pre>
         * - -1: unlimited parameters
         * - 0:  no parameter
         * - >0: number as specified
         * </pre>
         */
        int paramNumber;
        String dimension;
        String hierarchy;
        String level;

        public FilteringRule(Operator op, String dimension, String hierarchy, String level) {
            this(op, 1, dimension, hierarchy, level);
        }

        public FilteringRule(Operator op, int params, String dimension, String hierarchy, String level) {
            this.operator = op;
            this.paramNumber = params;
            this.dimension = dimension;
            this.hierarchy = hierarchy;
            this.level = level;
        }

        public void apply(GeneralOlap4jQueryImpl q, List<String> values) throws OlapException {
            ArrayList<String> names = new ArrayList<String>();
            names.add(dimension);
            if (hierarchy != null) {
                names.add(dimension + "." + hierarchy);
            }
            if (level != null) {
                names.add(level);
            }
            for (int i = 0; i < paramNumber; i++) {
                names.add(values.get(i));// TODO inconsistent
            }
            q.addFilter(names.toArray(new String[0]));
        }

    }

    private static class TimeframeRule extends FilteringRule {

        public TimeframeRule(String hierarchy, String level) {
            // super(FilteringRule.Operator.BETWEEN, 2, "Time", hierarchy, level);
            super(FilteringRule.Operator.IN, 1, "Time", hierarchy, level);
        }

        @Override
        public void apply(GeneralOlap4jQueryImpl q, List<String> values) throws OlapException {

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(values.get(0)));

            ArrayList<String> names = new ArrayList<String>();
            names.add(dimension);
            if (hierarchy != null) {
                names.add(dimension + "." + hierarchy);
            }

            names.add(String.valueOf(cal.get(Calendar.YEAR)));
            if (!level.equalsIgnoreCase("Year")) {
                if (hierarchy == null) {

                    names.add(String.valueOf(cal.get(Calendar.MONTH) + 1)); // starts from 0
                    if (!level.equalsIgnoreCase("Month")) {
                        names.add(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                        if (!level.equalsIgnoreCase("Day")) {
                            names.add(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
                        }
                    }

                } else if (hierarchy.equalsIgnoreCase("ByQuarter")) {

                    names.add(String.valueOf(cal.get(Calendar.MONTH) / 4 + 1));
                    if (!level.equalsIgnoreCase("Quarter")) {
                        names.add(String.valueOf(cal.get(Calendar.MONTH)));
                        if (!level.equalsIgnoreCase("Month")) {
                            names.add(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                            if (!level.equalsIgnoreCase("Day")) {
                                names.add(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
                            }
                        }
                    }

                } else if (hierarchy.equalsIgnoreCase("ByWeek")) {

                    names.add(String.valueOf(cal.get(Calendar.WEEK_OF_YEAR)));
                    if (!level.equalsIgnoreCase("Week")) {
                        names.add(String.valueOf(cal.get(Calendar.DAY_OF_WEEK)));// TODO starts from Sunday(1)
                        if (!level.equalsIgnoreCase("DayOfWeek")) {
                            names.add(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
                        }
                    }

                }
            }

            q.addFilter(names.toArray(new String[0]));
        }
    }

    static {

        // init measures
        _measures = new HashSet<String>();
        _measures.add("Transaction Amount");
        _measures.add("Transaction Count");
        _measures.add("Transaction Average Amount");
        _measures.add("Customer Count");

        // init grouping rules
        _groupingRules = new HashMap<String, GroupingRule>();

        _groupingRules.put("Account Selection", new GroupingRule(
                "Account Selection", null, GroupingRule.MODE_BY_CHILDREN));
        _groupingRules.put("Terminal", new GroupingRule(
                "Terminal", null, GroupingRule.MODE_BY_CHILDREN));
        _groupingRules.put("Customer", new GroupingRule(
                "Customer", null, GroupingRule.MODE_BY_CHILDREN));

        _groupingRules.put("Terminal Suburb", new GroupingRule(
                "Terminal", "BySuburb", null));
        _groupingRules.put("Customer Postcode", new GroupingRule(
                "Customer", "ByPostcode", null));
        _groupingRules.put("Customer Age", new GroupingRule(
                "Customer", "ByAge", null));
        _groupingRules.put("Customer Gender", new GroupingRule(
                "Customer", "ByGender", null));
        _groupingRules.put("Hour of day", new GroupingRule(
                "Time", null, "Hour"));
        _groupingRules.put("Day of week", new GroupingRule(
                "Time", "ByWeek", "DayOfWeek"));
        _groupingRules.put("Month of year", new GroupingRule(
                "Time", null, "Month"));
        _groupingRules.put("Quarter of year", new GroupingRule(
                "Time", null, "Quarter"));
        _groupingRules.put("Hour", new GroupingRule(
                "Time", null, "Hour")); // TODO 
        _groupingRules.put("Day", new GroupingRule(
                "Time", null, "Day")); // TODO 
        _groupingRules.put("Week", new GroupingRule(
                "Time", null, "Week")); // TODO 
        _groupingRules.put("Month", new GroupingRule(
                "Time", null, "Month")); // TODO 
        _groupingRules.put("Quarter", new GroupingRule(
                "Time", null, "Quarter")); // TODO 
        _groupingRules.put("Year", new GroupingRule(
                "Time", null, "Year"));

        // init filtering rules
        _filteringRules = new HashMap<String, FilteringRule>();
        _filteringRules.put("Hour", new TimeframeRule(null, "Hour"));
        _filteringRules.put("Day", new TimeframeRule(null, "Day"));
        _filteringRules.put("DayOfWeek", new TimeframeRule("ByWeek", "DayOfWeek"));
        _filteringRules.put("Week", new TimeframeRule("ByWeek", "Week"));
        _filteringRules.put("Month", new TimeframeRule(null, "Month"));
        _filteringRules.put("Quarter", new TimeframeRule("ByQuarter", "Quarter"));
        _filteringRules.put("Year", new TimeframeRule(null, "Year"));
//        _filteringRules.put("Hour Between", new FilteringRule(
//                FilteringRule.Operator.BETWEEN, "Time", null, "hour"));
//        _filteringRules.put("Day Between", new FilteringRule(
//                FilteringRule.Operator.BETWEEN, "Time", null, "day"));
//        _filteringRules.put("Month Between", new FilteringRule(
//                FilteringRule.Operator.BETWEEN, "Time", null, "month"));
//        _filteringRules.put("Quarter Between", new FilteringRule(
//                FilteringRule.Operator.BETWEEN, "Time", null, "quarter"));
//        _filteringRules.put("Year Between", new FilteringRule(
//                FilteringRule.Operator.BETWEEN, "Time", null, "year"));
        _filteringRules.put("Terminal", new FilteringRule(
                FilteringRule.Operator.EQUAL, "Terminal", null, null));
        _filteringRules.put("Terminal Suburb", new FilteringRule(
                FilteringRule.Operator.EQUAL, "Terminal", "BySuburb", "Suburb"));
        _filteringRules.put("Customer", new FilteringRule(
                FilteringRule.Operator.EQUAL, "Customer", null, null));
        _filteringRules.put("Customer Age", new FilteringRule(
                FilteringRule.Operator.EQUAL, "Customer", "ByAge", "Age"));
        _filteringRules.put("Customer Gender", new FilteringRule(
                FilteringRule.Operator.EQUAL, "Customer", "ByGender", "Gender"));
        _filteringRules.put("Customer Postcode", new FilteringRule(
                FilteringRule.Operator.EQUAL, "Customer", "ByPostcode", "Postcode"));
        _filteringRules.put("Account Selection", new FilteringRule(
                FilteringRule.Operator.EQUAL, "Account Selection", null, null));

    }

    private GeneralOlap4jQueryImpl q;

    public GeneralOlap4jQuery(OlapConnection conn, String cubeName) throws SQLException {
        q = new GeneralOlap4jQueryImpl(conn, cubeName);
    }

    public GeneralOlap4jQuery(Connection conn, String cubeName) throws SQLException {
        q = new GeneralOlap4jQueryImpl(conn, cubeName);
    }

    @Override
    public void close() {
        q.close();
    }

    @Override
    public void execute(PrintWriter writer) {
        try {

            Logger.getLogger(GeneralOlap4jQuery.class.getName())
                    .log(Level.INFO, q.toString());

            Olap4jUtils.renderCellSet(q.execute(), writer);

        } catch (Exception ex) {

            Logger.getLogger(GeneralOlap4jQuery.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new ExecutionException(ex);
        }
    }

    @Override
    public void addMeasure(String measure) {
        try {

            if (_measures.contains(measure)) {
                q.addMeasure(measure);
            } else {
                throw new RuleNotFoundException("measure rule: " + measure + " not found");
            }

        } catch (OlapException ex) {
            Logger.getLogger(GeneralOlap4jQuery.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new ServiceException(ex);
        }
    }

    @Override
    public void groupBy(String ruleName) {

        GroupingRule r = _groupingRules.get(ruleName);
        if (r == null) {
            throw new RuleNotFoundException("group-by rule: " + ruleName + " not found");
        }

        try {
            r.apply(q);
        } catch (OlapException ex) {
            Logger.getLogger(GeneralOlap4jQuery.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new ServiceException(ex);
        }
    }

    @Override
    public void addFilter(String ruleName, String... values) {
        addFilter(ruleName, Arrays.asList(values));
    }

    @Override
    public void addFilter(String ruleName, List<String> values) {
        FilteringRule r = _filteringRules.get(ruleName);
        if (r == null)
            // throw new RuleNotFoundException("filtering rule: " + ruleName + " not found");
            return;

        try {
            r.apply(q, values);
        } catch (OlapException ex) {
            Logger.getLogger(GeneralOlap4jQuery.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new ServiceException(ex);
        }
    }

    @Override
    public void sortBy(String ruleName) {

    }

}
