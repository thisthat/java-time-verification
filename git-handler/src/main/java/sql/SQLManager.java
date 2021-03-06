package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLManager {
    static String connectionQuery = "jdbc:postgresql://localhost:5432/giovanni?user=giovanni&password=giovanni";
    Connection conn;
    public SQLManager() throws SQLException {
        conn = DriverManager.getConnection(connectionQuery);
    }

    public void close() throws SQLException {
        try {
            conn.close();
        } catch (SQLException e) {
            conn.close();
        }
    }


    public void addCommitInfo(String hash, long time, long prjId) throws SQLException {
        String sqlInsert = "INSERT INTO public.\"CommitInfo\"(\"IDProject\",\"Hash\", \"Date\") VALUES ( ?, ?, ?)";
        PreparedStatement st = conn.prepareStatement(sqlInsert);
        st.setLong(1, prjId);
        st.setString(2, hash);
        st.setLong(  3, time);
        st.execute();
    }

    public void updateCommitDate(String hash, long date) throws SQLException{
        String sqlUpdate = "UPDATE public.\"CommitInfo\" SET \"Date\"=? WHERE \"Hash\" = ?";
        PreparedStatement st = conn.prepareStatement(sqlUpdate);
        st.setLong(1, date);
        st.setString(2, hash);
        st.execute();
    }

    public void addCommitResult(String hash, long timeInit, long timeProcess, int warn, int err, int rec, String warning, String errors, String recommendation, String projectName) throws SQLException {
        String sqlInsert = "INSERT INTO public.\"CommitResultActiveMQ\"(\"Hash\", \"TimeInit\", \"TimeProcess\", \"WARN\", \"ERR\", \"REC\"," +
                "\"Warnings\", \"Errors\", \"Recommendations\", \"ProjectName\") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement st = conn.prepareStatement(sqlInsert);
        st.setString(1, hash);
        st.setLong(2, timeInit);
        st.setLong(3, timeProcess);
        st.setLong(4, warn);
        st.setLong(5, err);
        st.setLong(6, rec);
        st.setString(7, warning);
        st.setString(8, errors);
        st.setString(9, recommendation);
        st.setString(10, projectName);
        st.execute();
    }
}
