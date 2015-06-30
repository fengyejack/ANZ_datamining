package au.edu.unsw.cse.comp9323.anz.analytics;

import au.edu.unsw.cse.comp9323.anz.analytics.keenio.GeneralKeenIOQuery;
import java.io.PrintWriter;

public class TestKeenIO {

//    private String connectString;
    private static final String project_ID = "54085861709a391177000006";
    private static final String read_Key = "3ac1a533c60bbe25d22db2e0882efadfbb4cc80b19f516b27db52b1fce26eb508e7e42fe47beef724bbb374251329cc24e1615cc8283d8a0ecb9159b54b5403e80605184ead917f43b552761bd2a1f05b36a91f6aaac06acf4428bc4810a9f33e75c8d5475d7bc8dc4f40e23634737f1";
    private static final String enent_Collection = "ANZ";
//    private String write_Key = "bc33f20a6fcee9c6201b610e0fc8aa8a8a706559a2e5e6be70130cbca7cec9fd1c2bccbf2a54d8fb39b478bc474a9ce7bb61767c5a5a0c77896194549861ebcd4746ef5b69c8801a4655ec557f7daeba3476360699278705c4b6384069506882ca5c69eade28ea20439abb873f884844";
//	private KeenProject project = new KeenProject(project_ID, write_Key, read_Key);
//	private KeenClient client = new JavaKeenClientBuilder().build();
//    private String query1 = "https://api.keen.io/3.0/projects/" + project_ID + "/queries/";

    public static void main(String[] args) {
        GeneralKeenIOQuery q = new GeneralKeenIOQuery(project_ID, read_Key, enent_Collection);
        q.addMeasure("Transaction Amount");
        q.groupBy("Customer Age");
//        q.groupBy("Month");
        q.execute(new PrintWriter(System.out));
    }
}
