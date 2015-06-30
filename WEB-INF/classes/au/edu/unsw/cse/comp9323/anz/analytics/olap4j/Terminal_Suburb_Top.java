package au.edu.unsw.cse.comp9323.anz.analytics.olap4j;

import au.edu.unsw.cse.comp9323.anz.analytics.request_readers.IRequestReader;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.olap4j.OlapConnection;

public class Terminal_Suburb_Top extends SpecificOlap4jQuery implements IRequestReader {

    public Terminal_Suburb_Top(Connection c, String cubeName) throws SQLException {
        super(c, cubeName);
    }

    public Terminal_Suburb_Top(OlapConnection c, String cubeName) {
        super(c, cubeName);
    }

    @Override
    protected String buildMdx() {
        return "";
//        return "WITH MEMBER [Measures].[Count_customers] AS Count(DISTINCT([d_customer].[id]))"
//                + "Select " + "{[Terminal].[Terminal Hash].[" + merchant_id + "]} ON COLUMNS,"
//                + "ORDER ( {[d_customer].[postcode].MEMBERS},[Measure].[" + mode_mdx + "], BDESC)  ON ROWS"
//                + " FROM [f_transaction]"
//                + " WHERE ([Measures].[Count_customers],[d_time].[" + time_level + "].[" + time_frame1 + "]:[" + "[d_time].[" + time_level + "].[" + time_frame2 + "])";

    }

    @Override
    public void readRequest(HttpServletRequest req) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
