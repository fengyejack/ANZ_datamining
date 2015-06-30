package au.edu.unsw.cse.comp9323.anz.analytics.keenio;

import au.edu.unsw.cse.comp9323.anz.analytics.ExecutionException;
import au.edu.unsw.cse.comp9323.anz.analytics.IGeneralOlapQuery;
import au.edu.unsw.cse.comp9323.anz.analytics.RuleNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

final public class GeneralKeenIOQuery implements IGeneralOlapQuery {

    private static class FilteringRule {

        public String property;
        public String operator;
        public String value;

        public FilteringRule(String prop, String op) {
            property = prop;
            operator = op;
            value = null;
        }

        public FilteringRule(FilteringRule rule, String value) {
            this(rule.property, rule.operator);
            this.value = value;
        }
    }

    private static class MeasureRule {

        public String name;
        public String metric;
        public String target;

        public MeasureRule(String name, String metric, String target) {
            this.name = name;
            this.metric = metric;
            this.target = target;
        }
    }

    final private static Map<String, MeasureRule> _measureRules;
    final private static Map<String, String> _groupingRules;
    final private static Map<String, String> _intervalRules;
    final private static Map<String, FilteringRule> _filteringRules;
    // final private static Set<String> _relTimeframe;
    // final private static Set<String> _absTimeframe;

    static {

        _measureRules = new HashMap<String, MeasureRule>();
        _measureRules.put("Transaction Amount", new MeasureRule(
                "Transaction Amount", "sum", "transaction.amount"));
        _measureRules.put("Transaction Count", new MeasureRule(
                "Transaction Count", "sum", "transaction.number"));
        _measureRules.put("Customer Count", new MeasureRule(
                "Customer Count", "count_unique", "customer.id"));
        // TODO ...

        _groupingRules = new HashMap<String, String>();
        _groupingRules.put("Terminal", "retailer.id");
        _groupingRules.put("Terminal Suburb", "retailer.suburb");
        _groupingRules.put("Customer", "customer.id");
        _groupingRules.put("Customer Postcode", "customer.postcode");
        _groupingRules.put("Customer Age", "customer.age");
        _groupingRules.put("Customer Gender", "customer.gender");
        _groupingRules.put("Account Selection", "transaction.account");

        _intervalRules = new HashMap<String, String>();
        _intervalRules.put("Hour", "hourly");
        _intervalRules.put("Day", "daily");
        _intervalRules.put("Week", "weekly");
        _intervalRules.put("Month", "monthly");
        _intervalRules.put("Year", "yearly");

        _filteringRules = new HashMap<String, FilteringRule>();
        _filteringRules.put("Terminal", new FilteringRule(
                "retailer.id", "eq"));
        _filteringRules.put("Terminal Suburb", new FilteringRule(
                "retailer.suburb", "eq"));
        _filteringRules.put("Customer", new FilteringRule(
                "customer.id", "eq"));
        _filteringRules.put("Customer Age", new FilteringRule(
                "customer.age", "eq"));
        _filteringRules.put("Customer Gender", new FilteringRule(
                "customer.gender", "eq"));
        _filteringRules.put("Customer Postcode", new FilteringRule(
                "customer.postcode", "eq"));
        _filteringRules.put("Account Selection", new FilteringRule(
                "transaction.account", "eq"));

        // _relTimeframe = new HashSet<String>();
        // _absTimeframe = new HashSet<String>();
    }

    // private String projectId;
    // private String readKey;
    // private String eventCollection;
    private KeenIODelegate keenio;

    // instance rules
    private List<MeasureRule> measureRules;
    private List<String> groupingRules;
    private String intervalRule;
    private List<FilteringRule> filteringRules;
    private String relTimeframe;
    private long absTimeStart;
    private long absTimeEnd;

    public GeneralKeenIOQuery(String pId, String rkey, String eventCol) {
        keenio = new KeenIODelegate(pId, rkey, eventCol);
        measureRules = new ArrayList<MeasureRule>();
        groupingRules = new ArrayList<String>();
        intervalRule = null;
        filteringRules = new ArrayList<FilteringRule>();
        absTimeStart = -1;
        absTimeEnd = -1;
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void addMeasure(String measure) {
        MeasureRule r = _measureRules.get(measure);
        if (r == null) {
            throw new RuleNotFoundException("measure rule: " + measure + " not found");
        }

        measureRules.add(r);
    }

    @Override
    public void groupBy(String ruleName) {
        String g = _groupingRules.get(ruleName);
        if (g != null) {
            groupingRules.add(g);
        } else {
            // otherwise, check time-related grouping rules
            g = _intervalRules.get(ruleName);
            if (g == null) // still not found
            {
                throw new RuleNotFoundException("group-by rule: " + ruleName + " not found");
            }
            intervalRule = g;
        }
    }

    @Override
    public void addFilter(String ruleName, String... values) {
        addFilter(ruleName, Arrays.asList(values));
    }

    @Override
    public void addFilter(String ruleName, List<String> values) {
        FilteringRule f = _filteringRules.get(ruleName);
        if (f != null) {
            filteringRules.add(new FilteringRule(f, values.get(0)));
        } else if (ruleName.equalsIgnoreCase("Time Between")) {
            // Keen IO specific
            // e.g. {"Time Between" : [1411743193733, 1411743196733]}
            absTimeStart = Long.parseLong(values.get(0));
            absTimeEnd = Long.parseLong(values.get(1));
        } else if (ruleName.equalsIgnoreCase("Relative Timeframe")) {
            // Keen IO specific
            switch (values.size()) {
                case 1:
                    // e.g. {"Relative Timeframe" : ["this_week"]}
                    relTimeframe = values.get(0);
                    break;
                case 2:
                    // e.g. {"Relative Timeframe" : ["previous_n_years", 3]}
                    relTimeframe = values.get(0).replace(
                            "_n_",
                            "_" + Integer.parseInt(values.get(1)) + "_");
                    break;
            }
        } else {

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(values.get(0)));
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            if (ruleName.equalsIgnoreCase("Hour")) {
                absTimeStart = cal.getTimeInMillis();
                cal.add(Calendar.HOUR_OF_DAY, 1);
                absTimeEnd = cal.getTimeInMillis();
            } else {
                cal.clear(Calendar.HOUR_OF_DAY);

                if (ruleName.equalsIgnoreCase("Day")) {
                    absTimeStart = cal.getTimeInMillis();
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    absTimeEnd = cal.getTimeInMillis();
                } else if (ruleName.equalsIgnoreCase("Week")) {
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    absTimeStart = cal.getTimeInMillis();
                    cal.add(Calendar.WEEK_OF_YEAR, 1);
                    absTimeEnd = cal.getTimeInMillis();
                } else {
                    cal.clear(Calendar.DAY_OF_MONTH);

                    if (ruleName.equalsIgnoreCase("Month")) {
                        absTimeStart = cal.getTimeInMillis();
                        cal.add(Calendar.MONTH, 1);
                        absTimeEnd = cal.getTimeInMillis();
                    } else if (ruleName.equalsIgnoreCase("Quarter")) {
                        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) % 4 * 3 + 1);
                        absTimeStart = cal.getTimeInMillis();
                        cal.add(Calendar.MONTH, 3);
                        absTimeEnd = cal.getTimeInMillis();
                    } else if (ruleName.equalsIgnoreCase("Year")) {
                        cal.clear(Calendar.MONTH);
                        absTimeStart = cal.getTimeInMillis();
                        cal.add(Calendar.YEAR, 1);
                        absTimeEnd = cal.getTimeInMillis();
                    } else {
                        throw new RuleNotFoundException("filtering rule: " + ruleName + " not found");
                    }

                }

            }

        }
    }

    @Override
    public void sortBy(String ruleName) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void execute(PrintWriter writer) {

        try {

            String analysisType;

            // multi-analysis or single
            if (measureRules.size() > 1) {

                JSONObject analyses = new JSONObject();
                for (MeasureRule r : measureRules) {
                    JSONObject m = new JSONObject();
                    m.put("analysis_name", r.metric);
                    if (r.target != null) {
                        m.put("target_property", r.target);
                    }
                    analyses.put(r.name, m);
                }
                analysisType = "multi-analysis";
                keenio.set("analyses", analyses);

            } else {

                MeasureRule r = measureRules.get(0);
                if (r.target != null) {
                    keenio.set("target_property", r.target);
                }
                analysisType = r.metric;

            }

            // group by
            if (!groupingRules.isEmpty()) {

                if (groupingRules.size() == 1) {
                    keenio.set("group_by", groupingRules.get(0));
                } else {
                    JSONArray grouping = new JSONArray();
                    for (String r : groupingRules) {
                        grouping.put(r);
                    }
                    keenio.set("group_by", grouping);
                }

            }

            // interval
            if (intervalRule != null) {
                keenio.set("interval", intervalRule);
            }

            // filters
            if (!filteringRules.isEmpty()) {

                JSONArray filters = new JSONArray();
                for (FilteringRule f : filteringRules) {
                    JSONObject fobj = new JSONObject();
                    fobj.put("property_name", f.property);
                    fobj.put("operator", f.operator);
                    fobj.put("property_value", f.value);
                    filters.put(fobj);
                }
                keenio.set("filters", filters);

            }

            // timeframe
            if (relTimeframe != null) {

                keenio.setRelTimeframe(relTimeframe);

            } else if (absTimeStart > -1 && absTimeEnd > -1) {

                keenio.setAbsTimeframe(absTimeStart, absTimeEnd);

            }

            // request
            keenio.request(analysisType, writer);

        } catch (Exception ex) {
            Logger.getLogger(GeneralKeenIOQuery.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new ExecutionException(ex);
        }

    }

}
