package au.edu.unsw.cse.comp9323.anz.analytics;

import au.edu.unsw.cse.comp9323.anz.analytics.request_readers.IRequestReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

final public class ApiDispatcher {

    private static class Api {

        public enum HttpMethod {

            GET, POST
        };

        public HttpMethod method;
        public String type;
        public String className;
        public String requestReader;

        public Api( /* HttpMethod method,*/String type, String className, String requestReader) {
//            this.method = method;
            this.type = type;
            this.className = className;
            this.requestReader = requestReader;
        }

    }

    final private static String QUERYTYPE_OLAP4J = "olap4j";
    final private static String QUERYTYPE_KEENIO = "keenio";
    final private static Map<String, Api> APIS;

    static {

        APIS = new HashMap<String, Api>();

        //======================================================================
        // general
        APIS.put("/general", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GeneralQueryJsonReqReader"));
        APIS.put("/olap4j/general", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GeneralQueryJsonReqReader"));
        APIS.put("/keenio/general", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GeneralQueryJsonReqReader"));

        //======================================================================
        // time
        // generalized queries (default implementations):
        APIS.put("/time/t_amount/today", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));
        APIS.put("/time/t_amount/this_week", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));
        APIS.put("/time/t_amount/this_month", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));
        // APIS.put("/time/t_amount/this_quarter", new Api(QUERYTYPE_KEENIO,
        //         "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
        //         "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));
        APIS.put("/time/t_amount/this_year", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));
        APIS.put("/time/t_amount/overall", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));

        // generalized queries (olap4j implementations):
        APIS.put("/olap4j/time/t_amount/today", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));
        APIS.put("/olap4j/time/t_amount/this_week", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));
        APIS.put("/olap4j/time/t_amount/this_month", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));
        APIS.put("/olap4j/time/t_amount/this_quarter", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));
        APIS.put("/olap4j/time/t_amount/this_year", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));
        APIS.put("/olap4j/time/t_amount/overall", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.TimeRelevantReqReader"));

        //======================================================================
        // global
        // specialized queries:
        APIS.put("/terminal/suburb/top", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.Terminal_Suburb_Top",
                // specialized query implements request reader by itself
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.Terminal_Suburb_Top"));

        // generalized queries (default implementations):
        APIS.put("/global/tx.amount/by.terminal", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/global/tx.amount/by.terminal:suburb", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));

        APIS.put("/global/tx.amount/by.customer", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/global/tx.amount/by.customer:postcode", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/global/tx.amount/by.customer:gender", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/global/tx.amount/by.customer:age", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));

        APIS.put("/global/tx.amount/by.account_type", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));

        // cross-joins
        APIS.put("/global/tx.amount/by.terminal:suburb/by.customer:postcode", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/global/tx.amount/by.terminal:suburb/by.customer:gender", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/global/tx.amount/by.terminal:suburb/by.customer:age", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/global/tx.amount/by.terminal:suburb/by.account_type", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));

        APIS.put("/global/tx.amount/by.account_type/by.customer:postcode", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/global/tx.amount/by.account_type/by.customer:gender", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/global/tx.amount/by.account_type/by.customer:age", new Api(QUERYTYPE_KEENIO,
                "au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));

        // generalized queries (olap4j implementations):
        APIS.put("/olap4j/global/tx.amount/by.terminal", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/olap4j/global/tx.amount/by.terminal:suburb", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));

        APIS.put("/olap4j/global/tx.amount/by.customer", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/olap4j/global/tx.amount/by.customer:postcode", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/olap4j/global/tx.amount/by.customer:gender", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/olap4j/global/tx.amount/by.customer:age", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));

        APIS.put("/olap4j/global/tx.amount/by.account_type", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));

        // cross-joins
        APIS.put("/olap4j/global/tx.amount/by.terminal:suburb/by.customer:postcode", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/olap4j/global/tx.amount/by.terminal:suburb/by.customer:gender", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/olap4j/global/tx.amount/by.terminal:suburb/by.customer:age", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/olap4j/global/tx.amount/by.terminal:suburb/by.account_type", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));

        APIS.put("/olap4j/global/tx.amount/by.account_type/by.customer:postcode", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/olap4j/global/tx.amount/by.account_type/by.customer:gender", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));
        APIS.put("/olap4j/global/tx.amount/by.account_type/by.customer:age", new Api(QUERYTYPE_OLAP4J,
                "au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQuery",
                "au.edu.unsw.cse.comp9323.anz.analytics.request_readers.GlobalReqReader"));

        //======================================================================
        // customer
        // APIS.put("/customer", new Api(QUERYTYPE_KEENIO, ""));
    }

    private final ServletConfig config;

    public ApiDispatcher(ServletConfig config) {
        this.config = config;
    }

    private IQuery buildQuery(Api a) throws Exception {
        Class<IQuery> cls = (Class<IQuery>) Class.forName(a.className);
        if (a.type.equalsIgnoreCase(QUERYTYPE_OLAP4J)) {
            Class.forName(config.getInitParameter("olap4j_driver_class"));
            return cls.getConstructor(Connection.class, String.class).newInstance(
                    DriverManager.getConnection(
                            config.getInitParameter("olap4j_connection_string")),
                    config.getInitParameter("olap4j_cube_name"));
        }
        if (a.type.equalsIgnoreCase(QUERYTYPE_KEENIO)) {
            return cls.getConstructor(String.class, String.class, String.class).newInstance(
                    config.getInitParameter("keenio_project_id"),
                    config.getInitParameter("keenio_read_key"),
                    config.getInitParameter("keenio_event_collection"));
        }
        throw new Exception("unknown query type");
    }

    private IRequestReader buildRequestReader(Api a, IQuery q) throws Exception {
        Class<IRequestReader> cls = (Class<IRequestReader>) Class.forName(a.requestReader);
        return cls.getConstructor(IQuery.class).newInstance(q);
    }

    public void runQuery(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {

            Api a = APIS.get(req.getPathInfo());
            if (a == null) {
                throw new ServiceException("Unknown API call");
            }

            IQuery q = buildQuery(a);

            // read request
            IRequestReader rr;
            if (a.className.equals(a.requestReader)) {
                rr = (IRequestReader) q;
            } else {
                rr = buildRequestReader(a, q);
            }
            rr.readRequest(req);

            // execute
            q.execute(resp.getWriter());
            q.close();
        } catch (Exception ex) {

            resp.setStatus(400);
            ServiceException srvex = ex instanceof ServiceException
                    ? (ServiceException) ex
                    : new ServiceException(ex);

            PrintWriter pw = resp.getWriter();
            pw.write(srvex.toString());
            pw.close();

        }
    }

}
