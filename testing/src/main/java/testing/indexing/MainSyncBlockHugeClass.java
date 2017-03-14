package testing.indexing;

import intermediateModelHelper.indexing.IndexingFile;
import intermediateModelHelper.indexing.mongoConnector.MongoConnector;
import intermediateModelHelper.indexing.structure.IndexData;
import intermediateModel.structure.ASTClass;
import intermediateModel.visitors.creation.JDTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.Java2AST;

import java.io.IOException;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class MainSyncBlockHugeClass {
	MongoConnector db;
	public static void main(String[] args){
		new MainSyncBlockHugeClass().run();
	}

	private void run() {
		this.db = MongoConnector.getInstance("test");
		String base_path = MainSyncBlockHugeClass.class.getClassLoader().getResource("BuddyPluginViewBetaChat.java").getPath();
		Java2AST a = null;
		try {
			a = new Java2AST(base_path, true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		CompilationUnit result = a.getContextJDT();
		JDTVisitor v = new JDTVisitor(result, base_path);
		result.accept(v);
		for(ASTClass c : v.listOfClasses){
			IndexingFile indexing = new IndexingFile(db);
			IndexData data = indexing.index(c);
			//int i = data.getListOfSyncBlocks().size();
			//System.out.println(i);
		}
	}
}
