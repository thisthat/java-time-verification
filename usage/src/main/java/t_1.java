import intermediateModel.interfaces.IASTMethod;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;

/**
 * Created by giovanni on 02/05/2017.
 */


public class t_1 {


    public static void main(String[] args) throws Exception {

        String f = "/Users/giovanni/repository/java-xal/outputDir/JournalPersistenceAdapter.java";
        ASTClass c = JDTVisitor.parse(f).get(0);
        IASTMethod m = c.getMethods().get(0);

        System.out.println(m.getStms().size());

    }
}
