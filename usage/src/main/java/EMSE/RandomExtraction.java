package EMSE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RandomExtraction {
    static String connectionQuery = "jdbc:postgresql://brock.isys.uni-klu.ac.at:5432/giovanni?user=giovanni&password=giovanni";
    static String sqlSelect = "SELECT * FROM public.\"TimeMethods\" ORDER BY RANDOM() LIMIT ?";

    static class Record {
        int id;
        String className;
        String methodName;
        String signature;
        String projectName;
        String filename;

        public Record(int id, String className, String methodName, String signatureName, String projectName, String filename) {
            this.id = id;
            this.className = className;
            this.methodName = methodName;
            this.signature = signatureName;
            this.projectName = projectName;
        }

        public Record(ResultSet rs) throws SQLException {
            id = rs.getInt(1);
            projectName = rs.getString(2);
            className = rs.getString(3);
            methodName = rs.getString(4);
            signature = rs.getString(5);
            filename = rs.getString(6);
        }

        @Override
        public String toString() {
            return String.format("%d;%s;%s;%s;%s;%s\n",id, projectName, className, methodName, signature, filename);
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        int value = 400;
        if(args.length > 0){
            value = Integer.parseInt(args[0]);
        }

        Connection conn = DriverManager.getConnection(connectionQuery);
        PreparedStatement st = conn.prepareStatement(sqlSelect);
        st.setInt(1, value);
        ResultSet rs = st.executeQuery();
        List<Record> out = new ArrayList<>();
        while(rs.next()){
            out.add(new Record(rs));
        }
        out.sort(Comparator.comparingInt(r -> r.id));

        File logFile = new File("evaluation-projects.csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
        writer.write("ID;ProjectName;ClassName;MethodName;Signature;Path\n");
        for(Record r : out){
            writer.write(r.toString());
            writer.flush();
        }
        writer.close();

    }
}
