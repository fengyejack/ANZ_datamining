package au.edu.unsw.cse.comp9323.anz.analytics;

import au.edu.unsw.cse.comp9323.anz.analytics.olap4j.GeneralOlap4jQueryImpl;
import au.edu.unsw.cse.comp9323.anz.analytics.olap4j.Olap4jUtils;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.olap4j.CellSet;

public class TestOlap4j {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
        Connection connection
                = DriverManager.getConnection(
                        "jdbc:mondrian:"
                        + "JdbcDrivers=org.apache.derby.jdbc.EmbeddedDriver;JdbcUser=sa;JdbcPassword=sa;PoolNeeded=false;"
                        + "Jdbc=jdbc:derby:C:\\Users\\guzhiji\\tomcat\\webapps\\mondrian-embedded\\WEB-INF\\classes\\foodmart;"
                        + "Catalog=file:C:\\Users\\guzhiji\\tomcat\\webapps\\mondrian-embedded\\WEB-INF\\anz_unsw_2014s2_g2.xml;");

        // group by a dimension of the default hierarchy
        GeneralOlap4jQueryImpl q1 = new GeneralOlap4jQueryImpl(connection, "ANZ Transaction");
        q1.addMeasure("Transaction Amount");
        q1.addMeasure("Transaction Count");
        q1.groupBy("Customer");
        System.out.println(q1.toString());
        print(q1.execute());

        // group by a dimension of the default hierarchy
        GeneralOlap4jQueryImpl q1b = new GeneralOlap4jQueryImpl(connection, "ANZ Transaction");
        q1b.addMeasure("Transaction Amount");
        q1b.addMeasure("Transaction Count");
        q1b.groupBy("Customer", false);
        System.out.println(q1b.toString());
        print(q1b.execute());

        // group by a dimension of a specified hierarchy
        GeneralOlap4jQueryImpl q2 = new GeneralOlap4jQueryImpl(connection, "ANZ Transaction");
        q2.addMeasure("Transaction Amount");
        q2.groupBy("Customer", "ByGender", true);
        System.out.println(q2.toString());
        print(q2.execute());

        // group by a dimension of a specified hierarchy
        GeneralOlap4jQueryImpl q2b = new GeneralOlap4jQueryImpl(connection, "ANZ Transaction");
        q2b.addMeasure("Transaction Amount");
        q2b.groupBy("Customer", "ByGender", false);
        System.out.println(q2b.toString());
        print(q2b.execute());

        // group by a level of the default hierarchy
        GeneralOlap4jQueryImpl q3 = new GeneralOlap4jQueryImpl(connection, "ANZ Transaction");
        q3.addMeasure("Transaction Amount");
        q3.groupBy("Customer", "Customer Hash");
        q3.groupBy("Terminal", "BySuburb", "Suburb");
//        q3.groupBy("Terminal", "BySuburb", true);
        System.out.println(q3.toString());
        print(q3.execute());

        // group by a level of a specified hierarchy
        GeneralOlap4jQueryImpl q4 = new GeneralOlap4jQueryImpl(connection, "ANZ Transaction");
        q4.addMeasure("Transaction Amount");
        q4.groupBy("Customer", "ByGender", "Gender");
        q4.addFilter("Time", "2014");
        q4.addFilter("Terminal", "Terminal.BySuburb", "MACKAY");
        q4.addFilter("Terminal", "Terminal.BySuburb", "FAIRFIELD");
        System.out.println(q4.toString());
        print(q4.execute());

    }

    private static void print(CellSet cellSet) {
        PrintWriter pw = new PrintWriter(System.out);
        Olap4jUtils.renderCellSet(cellSet, pw);
        pw.flush();
        System.out.println();
        System.out.println();
//        for (Position row : cellSet.getAxes().get(1)) {
//            for (Position column : cellSet.getAxes().get(0)) {
//                for (Member member : row.getMembers()) {
//                    System.out.print("[");
//                    System.out.print(member.getUniqueName());
//                    System.out.print("/");
//                    System.out.print(member.getParentMember());
//                    System.out.print("]");
//                }
//                for (Member member : column.getMembers()) {
//                    System.out.print("[");
//                    System.out.print(member.getUniqueName());
//                    System.out.print("]");
//                }
//                System.out.print("=");
//                final Cell cell = cellSet.getCell(column, row);
//                System.out.println(cell.getFormattedValue());
//                System.out.println();
//            }
//        }
    }

}
