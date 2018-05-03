package EMSE;

import intermediateModel.structure.ASTClass;
import intermediateModel.structure.ASTMethod;
import intermediateModel.structure.ASTRE;
import intermediateModel.visitors.ApplyHeuristics;
import intermediateModel.visitors.DefaultASTVisitor;
import intermediateModel.visitors.creation.JDTVisitor;
import intermediateModel.visitors.creation.filter.ElseIf;
import intermediateModelHelper.envirorment.temporal.TemporalInfo;
import intermediateModelHelper.indexing.IndexingProject;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by giovanni on 31/05/2017.
 */
public class SaveMethodsWithTime {


    static class Output {
        String className;
        String methodName;
        String methodSignature;
        String filepath;

        public Output(String className, String methodName, String methodSignature, String filepath) {
            this.className = className;
            this.methodName = methodName;
            this.methodSignature = methodSignature;
            this.filepath = filepath;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getMethodSignature() {
            return methodSignature;
        }

        public String getFilepath() {
            return filepath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Output output = (Output) o;
            return Objects.equals(className, output.className) &&
                    Objects.equals(methodName, output.methodName) &&
                    Objects.equals(methodSignature, output.methodSignature) &&
                    Objects.equals(filepath, output.filepath);
        }

        @Override
        public int hashCode() {

            return Objects.hash(className, methodName, methodSignature, filepath);
        }
    }

    static String connectionQuery = "jdbc:postgresql://brock.isys.uni-klu.ac.at:5432/giovanni?user=giovanni&password=giovanni";
    static String sqlInsert = "INSERT INTO PUBLIC.\"TimeMethods\"(\"ProjectName\", \"ClassName\", \"MethodName\", \"Signature\", \"filepath\") VALUES (?, ?, ?, ?, ?);";
    Connection conn;


    public SaveMethodsWithTime(String path, String name) throws SQLException {
        Iterator<File> i = IndexingProject.getJavaFiles(path);
        int n_file = 0;
        int n_class = 0;
        int n_method = 0;
        int n_method_time = 0;

        conn = DriverManager.getConnection(connectionQuery);

        System.out.println("Project: " + path);
        while (i.hasNext()) {
            String filename = i.next().getAbsolutePath();
            if(!filename.contains("/src/main/java"))
                continue;
            List<ASTClass> result = JDTVisitor.parse(filename, path, ElseIf.filter, false);
            for(ASTClass c : result){
                n_class++;
                n_method += c.getCountMethod();
                //compute time
                ApplyHeuristics.getConstraintV2(c);
                List<Output> l = new ArrayList<>();
                DefaultASTVisitor v = new DefaultASTVisitor(){
                    ASTMethod lastMethod = null;

                    @Override
                    public void enterASTMethod(ASTMethod elm) {
                        lastMethod = elm;
                    }

                    @Override
                    public void enterASTRE(ASTRE elm) {
                        if(lastMethod == null) return;
                        if(elm.isTimeCritical()){
                            String sign = Arrays.toString(lastMethod.getSignature().toArray(new String[0]));
                            Output o = new Output(c.fullName(), lastMethod.getName(), sign, c.getPath());
                            if(!l.contains(o))
                                l.add(o);
                        }
                    }
                };
                v.setExcludeHiddenClass(true);
                c.visit(v);
                if(l.size() > 0)
                    save(l, name);
            //
            }
            n_file++;
        }
        System.out.println("#File:        " + n_file);
        System.out.println("#Class:       " + n_class);
        System.out.println("#Method:      " + n_method);
        conn.close();
    }

    private void save(List<Output> l, String name) throws SQLException {
        for(Output o : l) {
            PreparedStatement st = conn.prepareStatement(sqlInsert);
            st.setString(1, name);
            st.setString(2, o.getClassName());
            st.setString(3, o.getMethodName());
            st.setString(4, o.getMethodSignature());
            st.setString(5, o.getFilepath());
            st.execute();
        }
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 3) {
            System.out.println("Usage with: project_path project_name intermediateModel.types");
            System.exit(1);
        }
        String path = args[0];
        String name = args[1];
        String typ  = args[2];
        TemporalInfo.getInstance().loadUserTypes(typ);
        new SaveMethodsWithTime(path, name);
    }
}
