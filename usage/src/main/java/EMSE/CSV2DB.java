package EMSE;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CSV2DB {
    static String connectionQuery = "jdbc:postgresql://brock.isys.uni-klu.ac.at:5432/giovanni?user=giovanni&password=giovanni";
    static String sqlInsert = "INSERT INTO public.\"EMSE-TimeMethod\"(\"projectName\", \"className\", \"methodName\", \"signature\")" +
            " VALUES (?, ?, ?, ?);";

    public static void main(String[] args) throws SQLException, IOException {

        if(args.length < 2){
            System.err.println("Usage with [project_name] [csv_file.csv]");
            System.exit(1);
        }

        String projectName = args[0];
        String file = args[1];

        Connection conn = DriverManager.getConnection(connectionQuery);

        Reader in = new FileReader(file);
        Iterable<CSVRecord> csv = CSVFormat.EXCEL.withDelimiter(';').parse(in);
        int i = 0;
        for(CSVRecord r : csv){
            if(i == 0){
                i = 10;
                continue;
            }
            PreparedStatement st = conn.prepareStatement(sqlInsert);
            st.setString(1, projectName);
            st.setString(2, r.get(0));
            st.setString(3, r.get(1));
            st.setString(4, r.get(2));
            st.execute();
        }
    }
}
