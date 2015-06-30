package au.edu.unsw.cse.comp9323.anz.analytics.olap4j;

import au.edu.unsw.cse.comp9323.anz.analytics.ExecutionException;
import au.edu.unsw.cse.comp9323.anz.analytics.IQuery;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;

/**
 *
 * Example:
 * <pre>
 * public class ASpecificQuery extends SpecificOlap4jQuery implements IRequestReader {
 *      @Override
 *      protected String buildMdx() {
 *          return ...;
 *      }
 * @Override public void readRequest(HttpServletRequest req) { // ... } }
 * </pre>
 */
abstract public class SpecificOlap4jQuery implements IQuery {

    private OlapConnection connection;

    public SpecificOlap4jQuery(Connection c, String cubeName) throws SQLException {
        this(c.unwrap(OlapConnection.class), cubeName);
    }

    public SpecificOlap4jQuery(OlapConnection conn, String cubeName) {
        connection = conn;
    }

    abstract protected String buildMdx();

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
        }
    }

    @Override
    public void execute(PrintWriter writer) {

        OlapStatement stmt = null;
        try {

            stmt = connection.createStatement();
            CellSet cellset = stmt.executeOlapQuery(buildMdx());
            Olap4jUtils.renderCellSet(cellset, writer);

        } catch (Exception ex) {
            Logger.getLogger(SpecificOlap4jQuery.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new ExecutionException(ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
        }

    }

}
