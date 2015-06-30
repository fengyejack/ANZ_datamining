package au.edu.unsw.cse.comp9323.anz.analytics;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ANZSimpleOLAPServlet extends HttpServlet {

    private ApiDispatcher dispatcher;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        dispatcher = new ApiDispatcher(config);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Content-Type", "application/json");
        dispatcher.runQuery(req, resp);
    }

}
