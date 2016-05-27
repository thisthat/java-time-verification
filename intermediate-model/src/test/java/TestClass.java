import IntermediateModel.interfaces.IASTHasStms;
import IntermediateModel.interfaces.IASTMethod;
import IntermediateModel.structure.*;
import IntermediateModel.visitors.CreateIntemediateModel;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Before;
import org.junit.Test;
import parser.Java2AST;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestClass {
    String filename = "ExportChangesJob.java";
    List<ASTClass> intemediateModel;
    List<ASTClass> manuallyCreated = new ArrayList<>();

    @Before
    public void init() throws Exception {
        Java2AST a = new Java2AST( getClass().getResource(filename).getFile() );
        a.convertToAST();
        ParserRuleContext ast = a.getContext();
        ParseTreeWalker walker = new ParseTreeWalker();
        CreateIntemediateModel sv = new CreateIntemediateModel();
        walker.walk(sv, ast);
        intemediateModel = sv.listOfClasses;
    }

    @Before
    public void initModel(){
        Token t = new CommonToken(0);
        ASTClass c = new ASTClass(t,t, "at.aau.service.jobs", "ExportChangesJob", ASTClass.Visibility.PUBLIC, "Thread", new ArrayList<>());
        manuallyCreated.add(c);
        //contructor
        List<ASTVariable> pars = new ArrayList<>();
        pars.add(new ASTVariable(t,t,"jobService","JobService"));
        pars.add(new ASTVariable(t,t,"jobId","Long"));
        pars.add(new ASTVariable(t,t,"projectService","ProjectService"));
        pars.add(new ASTVariable(t,t,"projectId","Long"));
        List<String> exs = new ArrayList<String>();
        ASTConstructor con = new ASTConstructor(t,t, "ExportChangesJob", pars,exs);
        //5 instruction for the constructor
        for(int i = 0; i < 5; i++){
            con.addStms(new ASTRE(t,t));
        }
        c.addMethod(con);

        //run method
        pars = new ArrayList<>();
        ASTMethod run = new ASTMethod(t,t, "run", "void", pars, exs);
        c.addMethod(run);

        //first try
        ASTTry firstTry = new ASTTry(t,t);
        run.addStms(firstTry);
        ASTTry.ASTTryBranch firstTryBranch = firstTry.new ASTTryBranch(t,t);
        firstTry.setTryBranch(firstTryBranch);
        //add the map
        ASTRE map = new ASTRE(t,t);
        firstTryBranch.addStms(map);
        //add the inner try
        ASTTry secondTry = new ASTTry(t,t);
        firstTryBranch.addStms(secondTry);
        //add catch external try
        ASTTry.ASTCatchBranch firstCatch = firstTry.new ASTCatchBranch(t,t, new ASTVariable(t,t,"e","Exception"));
        firstTry.addCatchBranch(firstCatch);
        //two instructions
        for(int i = 0; i < 2; i++){
            firstCatch.addStms(new ASTRE(t,t));
        }

        //inner try
        ASTTry.ASTTryBranch innerTryBranch = secondTry.new ASTTryBranch(t,t);
        secondTry.setTryBranch(innerTryBranch);
		//four instruction
        for(int i = 0; i < 4; i++){
            innerTryBranch.addStms(new ASTRE(t,t));
        }
        //first foreach
        ASTForEach firstFor = new ASTForEach(t,t,
                new ASTVariable(t,t, "cp", "CommitPair"),
                new ASTRE(t,t) );
        innerTryBranch.addStms(firstFor);
        for(int i = 0; i < 3; i++){
            firstFor.addStms(new ASTRE(t,t));
        }
        ASTFor forwithoutInit = new ASTFor(t,t,
                null,
                new ASTRE(t,t),
                new ASTRE(t,t));
        firstFor.addStms(forwithoutInit);
        for(int i = 0; i < 2; i++){
            forwithoutInit.addStms(new ASTRE(t,t));
        }
        //while
        ASTWhile firstWhile = new ASTWhile(t,t, new ASTRE(t,t));
        firstFor.addStms(firstWhile);
        for(int i = 0; i < 2; i++){
            firstWhile.addStms(new ASTRE(t,t));
        }
        firstFor.addStms(new ASTRE(t,t));
        //end first for

        for(int i = 0; i < 8; i++){
            innerTryBranch.addStms(new ASTRE(t,t));
        }

        ASTFor simplefor = new ASTFor(t,t,
                new ASTRE(t,t),
                new ASTRE(t,t),
                new ASTRE(t,t));
        innerTryBranch.addStms(simplefor);
        simplefor.addStms(new ASTRE(t,t));
        innerTryBranch.addStms(new ASTRE(t,t));
        //another foreach
        ASTForEach YAFE = new ASTForEach(t,t,
                new ASTVariable(t,t, "cp","CommitPair"),
                new ASTRE(t,t));
        innerTryBranch.addStms(YAFE);
        for(int i = 0; i < 3; i++){
            YAFE.addStms(new ASTRE(t,t));
        }
        ASTForEach innerYAFE = new ASTForEach(t,t,
                new ASTVariable(t,t, "change","String"),
                new ASTRE(t,t));
        YAFE.addStms(innerYAFE);
        for(int i = 0; i < 2; i++){
            innerYAFE.addStms(new ASTRE(t,t));
        }
        //while
        ASTWhile innerWhile = new ASTWhile(t,t, new ASTRE(t,t));
        YAFE.addStms(innerWhile);
        for(int i = 0; i < 2; i++){
            innerWhile.addStms(new ASTRE(t,t));
        }
        firstFor.addStms(new ASTRE(t,t));
        //end foreach
        innerTryBranch.addStms(new ASTRE(t,t));
        for(int i = 0; i < 4; i++){
            innerTryBranch.addStms(new ASTRE(t,t));
        }
        ASTForEach almostLast = new ASTForEach(t,t,
                new ASTVariable(t,t, "distinctChange", "String"),
                new ASTRE(t,t));
        innerTryBranch.addStms(almostLast);
        for(int i = 0; i < 2; i++){
            almostLast.addStms(new ASTRE(t,t));
        }
        innerTryBranch.addStms(new ASTRE(t,t));
        //last for
        ASTForEach last = new ASTForEach(t,t,
                new ASTVariable(t,t, "commitPair", "CommitPair"),
                new ASTRE(t,t));
        innerTryBranch.addStms(last);
        last.addStms(new ASTRE(t,t));
        last.addStms(new ASTRE(t,t));

        //lol last inner for
        ASTForEach last_FOR_REAL = new ASTForEach(t,t,
                new ASTVariable(t,t, "distinctChange", "String"),
                new ASTRE(t,t));
        last.addStms(last_FOR_REAL);
        for(int i = 0; i < 2; i++){
            last_FOR_REAL.addStms(new ASTRE(t,t));
        }
        last.addStms(new ASTRE(t,t));
        //catch
        ASTTry.ASTCatchBranch innerCatch = secondTry.new ASTCatchBranch(t,t, new ASTVariable(t,t,"e","Exception"));
        secondTry.addCatchBranch(innerCatch);
        innerCatch.addStms(new ASTRE(t,t));
        firstTryBranch.addStms(new ASTRE(t,t));
        firstTryBranch.addStms(new ASTRE(t,t));


        //last two private methods
        pars = new ArrayList<>();
        pars.add(new ASTVariable(t,t,"changesPerCommit","Map<CommitPair, List<String>>"));
        ASTMethod findMaxDistinctChanges = new ASTMethod(t,t,"findMaxDistinctChanges", "int", pars, exs);
        c.addMethod(findMaxDistinctChanges);
        findMaxDistinctChanges.addStms(new ASTRE(t,t));
        ASTForEach findForeach = new ASTForEach(t,t,
                new ASTVariable(t,t, "cp", "CommitPair"),
                new ASTRE(t,t));
        findMaxDistinctChanges.addStms(findForeach);
        findForeach.addStms(new ASTRE(t,t));
        findMaxDistinctChanges.addStms(new ASTReturn(t,t, new ASTRE(t,t)));

        //Last Method
        pars = new ArrayList<>();
        pars.add(new ASTVariable(t,t,"changesPerCommit","Map<CommitPair, List<String>>"));
        ASTMethod findMaxChanges = new ASTMethod(t,t,"findMaxChanges", "int", pars, exs);
        c.addMethod(findMaxChanges);
        findMaxChanges.addStms(new ASTRE(t,t));
        ASTForEach findChangesForeach = new ASTForEach(t,t,
                new ASTVariable(t,t, "cp", "CommitPair"),
                new ASTRE(t,t));
        findMaxChanges.addStms(findChangesForeach);
        findChangesForeach.addStms(new ASTRE(t,t));
        findMaxChanges.addStms(new ASTReturn(t,t, new ASTRE(t,t)));
    }

    @Test
    public void TestNumberOfClasses() {
        assertEquals(intemediateModel.size(), manuallyCreated.size());
        for(int i = 0; i < intemediateModel.size(); i++){
            ASTClass c1 = intemediateModel.get(i);
            ASTClass c2 = manuallyCreated.get(i);
            assertEquals(c1.getPackageName(), c2.getPackageName());
            assertEquals(c1.getName(), c2.getName());
            assertEquals(c1.getAccessRight(), c2.getAccessRight());
            assertEquals(c1.getImplmentsInterfaces(), c2.getImplmentsInterfaces());
            assertEquals(c1.getExtendClass(), c2.getExtendClass());
            assertEquals(c1.getMethods().size(), c2.getMethods().size());
        }
    }

    @Test
    public void TestConstructNameEqualClassName() {
        for(ASTClass c : intemediateModel){
            String classname = c.getName();
			for(IASTMethod m : c.getMethods()){
				if(m instanceof ASTConstructor){
					assertEquals(((ASTConstructor) m).getName(), classname);
				}
			}
        }
    }

    @Test
    public void TestParametersMethods(){
		for(int i = 0; i < intemediateModel.size(); i++){
			ASTClass c1 = intemediateModel.get(i);
			ASTClass c2 = manuallyCreated.get(i);
			for(int j = 0; j < c1.getMethods().size(); j++){
				IASTMethod m1 = c1.getMethods().get(j);
				IASTMethod m2 = c2.getMethods().get(j);
				assertEquals(m1.getParameters(), m2.getParameters());
				if(m1 instanceof ASTMethod){
					assertEquals(((ASTMethod) m1).getReturnType(), ((ASTMethod)m2).getReturnType());
				}
				assertEquals(m1.getName(), m2.getName());

			}
		}
    }

	/*
    @Test
    public void TestSameInstructions() throws Exception {
		for(int i = 0; i < intemediateModel.size(); i++){
			ASTClass c1 = intemediateModel.get(i);
			ASTClass c2 = manuallyCreated.get(i);
			for(int j = 0; j < c1.getMethods().size(); j++){
				IASTMethod m1 = c1.getMethods().get(j);
				IASTMethod m2 = c2.getMethods().get(j);
				if(!m1.equals(m2))
					throw new Exception("");
			}
		}
    }
    */
}
