package EMSE.rq5;

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
    static String connectionQuery = "jdbc:postgresql://localhost:5432/rq5?user=giovanni&password=giovanni";
    static String sqlInsert = "INSERT INTO public.\"CCTable\"(\"project\", \"path\", \"ccvalue\")" +
            " VALUES (?, ?, ?);";

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

        for(CSVRecord r : csv){
            PreparedStatement st = conn.prepareStatement(sqlInsert);
            st.setString(1, projectName);
            st.setString(2, r.get(0));
            st.setInt(3, Integer.parseInt(r.get(1)));
            st.execute();
        }
    }
}
