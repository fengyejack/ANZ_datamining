package au.edu.unsw.cse.comp9323.anz.analytics.olap4j;

import java.io.PrintWriter;
import org.json.JSONWriter;
import org.olap4j.CellSet;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

public class Olap4jUtils {

    public static void renderCellSet(CellSet cellSet, PrintWriter writer) {

        JSONWriter json = new JSONWriter(writer);
        json.array();
        for (Position row : cellSet.getAxes().get(1)) {
            for (Position column : cellSet.getAxes().get(0)) {

                json.object();

                for (Member member : row.getMembers()) {
                    json.key(member.getHierarchy().getName())
                            .value(member.getName());
                }

                Member member = column.getMembers().get(0);
                json.key("measure")
                        .value(member.getName());
                json.key("value")
                        .value(cellSet.getCell(column, row).getValue());

                json.endObject();

            }
        }
        json.endArray();
        writer.flush();

    }
}
