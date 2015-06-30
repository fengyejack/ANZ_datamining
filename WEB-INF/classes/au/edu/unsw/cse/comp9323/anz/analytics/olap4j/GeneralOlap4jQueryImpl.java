package au.edu.unsw.cse.comp9323.anz.analytics.olap4j;

import java.sql.Connection;
import java.sql.SQLException;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.olap4j.query.Query;
import org.olap4j.query.QueryAxis;
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;

final public class GeneralOlap4jQueryImpl {

    final private Query q;
    final private QueryDimension measureDim;

    public GeneralOlap4jQueryImpl(Connection c, String cubeName) throws SQLException {
        this(c.unwrap(OlapConnection.class), cubeName);
    }

    public GeneralOlap4jQueryImpl(OlapConnection conn, String cubeName) throws SQLException {
//        System.out.println("catalog=" + conn.getCatalog());
//        System.out.println("schema=" + conn.getSchema());
        Cube cube = conn.getOlapSchema().getCubes().get(cubeName);
        q = new Query("GeneralOLAPQuery", cube);
        measureDim = q.getDimension("Measures");
        q.getAxis(Axis.COLUMNS).addDimension(measureDim);
    }

    public GeneralOlap4jQueryImpl(Cube cube) throws SQLException {
        q = new Query("GeneralOLAPQuery", cube);
        measureDim = q.getDimension("Measures");
        q.getAxis(Axis.COLUMNS).addDimension(measureDim);
    }

    public void close() {
        try {
            q.getCube().getSchema().getCatalog().getDatabase()
                    .getOlapConnection().close();
        } catch (SQLException ex) {
        }
    }

    public void addMeasure(String measure) throws OlapException {
        Member m = q.getCube().lookupMember(
                IdentifierNode.ofNames("Measures", measure)
                .getSegmentList());
        measureDim.include(m);
    }

    public void groupBy(String dimension) throws OlapException {
        groupBy(dimension, null, true);
    }

    public void groupBy(String dimension, boolean byEach) throws OlapException {
        groupBy(dimension, null, byEach);
    }

    public void groupBy(String dimension, String hierarchy, boolean byEach) throws OlapException {
        QueryDimension d = q.getDimension(dimension);
        Hierarchy h;
        if (hierarchy == null) {
            h = d.getDimension().getDefaultHierarchy();
        } else {
            h = d.getDimension().getHierarchies()
                    .get(dimension + "." + hierarchy);
        }
        if (!h.hasAll()) {
            throw new OlapException("does not have an 'all' member");
        }
        if (byEach) {
            d.include(Selection.Operator.CHILDREN, h.getDefaultMember()); // => just show children
        } else {
            d.include(h.getDefaultMember()); // => just show 'all'
        }
        QueryAxis a = q.getAxis(Axis.ROWS);
        if (!a.getDimensions().contains(d)) {
            a.addDimension(d);
        }
    }

    public void groupBy(String dimension, String level) throws OlapException {
        groupBy(dimension, null, level);
    }

    public void groupBy(String dimension, String hierarchy, String level) throws OlapException {
        Hierarchy h;
        QueryDimension d = q.getDimension(dimension);
        if (hierarchy == null) {
            h = d.getDimension().getDefaultHierarchy();
        } else {
            h = d.getDimension().getHierarchies()
                    .get(dimension + "." + hierarchy);
        }
        d.include(h.getLevels().get(level));
        QueryAxis a = q.getAxis(Axis.ROWS);
        if (!a.getDimensions().contains(d)) {
            a.addDimension(d);
        }
    }

    //TODO range: [Time].[2013]:[Time].[2014]
    public void addFilter(String... names) throws OlapException {
        if (names.length == 0) {
            throw new OlapException("no filtering condition");
        }
        QueryDimension d = q.getDimension(names[0]);
        d.include(IdentifierNode.ofNames(names)
                .getSegmentList());
        QueryAxis a = q.getAxis(Axis.FILTER);
        if (!a.getDimensions().contains(d)) {
            a.addDimension(d);
        }
    }

    public void validate() throws OlapException {
        q.validate();
    }

    public CellSet execute() throws OlapException {
        return q.execute();
    }

    @Override
    public String toString() {
        return q.getSelect().toString();
    }

}
