package au.edu.unsw.cse.comp9323.anz.analytics.request_readers;

import au.edu.unsw.cse.comp9323.anz.analytics.IGeneralOlapQuery;
import au.edu.unsw.cse.comp9323.anz.analytics.IQuery;
import javax.servlet.http.HttpServletRequest;

public class GlobalReqReader implements IRequestReader {

    private final IGeneralOlapQuery q;

    public GlobalReqReader(IQuery query) {
        q = (IGeneralOlapQuery) query;
    }

    public GlobalReqReader(IGeneralOlapQuery query) {
        q = query;
    }

    @Override
    public void readRequest(HttpServletRequest req) {

        String p = req.getPathInfo();

        if (p.contains("/tx.amount:avg/")) {
            q.addMeasure("Transaction Average Amount");
        } else if (p.contains("/tx.amount/")) {
            q.addMeasure("Transaction Amount");
        } else if (p.contains("/tx.count/")) {
            q.addMeasure("Transaction Count");
        } else if (p.contains("/customer.count/")) {
            q.addMeasure("Customer Count");
        }

        if (p.contains("/by.terminal:suburb")) {
            q.groupBy("Terminal Suburb");
        } else if (p.contains("/by.terminal")) {
            q.groupBy("Terminal");
        }

        if (p.contains("/by.customer:postcode")) {
            q.groupBy("Customer Postcode");
        } else if (p.contains("/by.customer:gender")) {
            q.groupBy("Customer Gender");
        } else if (p.contains("/by.customer:age")) {
            q.groupBy("Customer Age");
        } else if (p.contains("/by.customer")) {
            q.groupBy("Customer");
        }

        if (p.contains("/by.account_type")) {
            q.groupBy("Account Selection");
        }

        p = req.getParameter("terminal");
        if (p != null) {
            q.addFilter("Terminal", p);
        } else {
            p = req.getParameter("terminal.suburb");
            if (p != null) {
                q.addFilter("Terminal Suburb", p);
            }
        }

        p = req.getParameter("customer");
        if (p != null) {
            q.addFilter("Customer", p);
        } else {
            p = req.getParameter("customer.age");
            if (p != null) {
                q.addFilter("Customer Age", p);
            } else {
                p = req.getParameter("customer.gender");
                if (p != null) {
                    q.addFilter("Customer Gender", p);
                } else {
                    p = req.getParameter("customer.postcode");
                    if (p != null) {
                        q.addFilter("Customer Postcode", p);
                    }
                }
            }
        }

        p = req.getParameter("account_type");
        if (p != null) {
            q.addFilter("Account Selection", p);
        }

        p = req.getParameter("year");
        if (p != null) {
            q.addFilter("Year", p);
        } else {
            p = req.getParameter("quarter");
            if (p != null) {
                q.addFilter("Quarter", p);
            } else {
                p = req.getParameter("month");
                if (p != null) {
                    q.addFilter("Month", p);
                } else {
                    p = req.getParameter("week");
                    if (p != null) {
                        q.addFilter("Week", p);
                    } else {
                        p = req.getParameter("day");
                        if (p != null) {
                            q.addFilter("Day", p);
                        } else {
                            p = req.getParameter("hour");
                            if (p != null) {
                                q.addFilter("Hour", p);
                            }
                        }
                    }
                }
            }
        }

    }

}
