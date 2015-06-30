package au.edu.unsw.cse.comp9323.anz.analytics;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mondrian.olap.DriverManager;
import mondrian.olap.MondrianProperties;
import mondrian.olap.MondrianServer;
import mondrian.spi.CatalogLocator;
import mondrian.spi.impl.ServletContextCatalogLocator;
import org.olap4j.OlapConnection;

public class ANZMondrianServlet extends HttpServlet {

    protected MondrianServer server;

    @Override
    public void destroy() {
        super.destroy();
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }

    private String connectString;
    private CatalogLocator locator;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        connectString = config.getInitParameter("connectString");
        Enumeration initParameterNames = config.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String name = (String) initParameterNames.nextElement();
            String value = config.getInitParameter(name);
            MondrianProperties.instance().setProperty(name, value);
        }
        locator = new ServletContextCatalogLocator(config.getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPut(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        mondrian.olap.Connection mdxConnection = null;
        try {
            mdxConnection = DriverManager.getConnection(connectString, locator);
            server = MondrianServer.forConnection(mdxConnection);
            OlapConnection conn = server.getConnection("anz_unsw_2014s2_g2", "anz_unsw_2014s2_g2", null);
//            conn.prepareOlapStatement("mdx query").executeQuery()
//            Query q = mdxConnection.parseQuery(queryString);
            //            Result result = mdxConnection.execute(q);
        } catch (Exception ex) {

        }
        resp.setHeader("Content-Type", "application/json");
    }

}
