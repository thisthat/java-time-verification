import XAL.items.XALAutomaton;
import XAL.items.XALDocument;
import XAL.visitors.CreateXALTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import parser.Java2AST;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 *
 * This test case suite is used to test if the algorithm works correctly when there are nested classes
 *
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public class TestCorrectMethodAssociation {

    @Test
    public void TestOnPredictionModule() throws Exception {

        Java2AST a = new Java2AST( getClass().getResource("PredictionModule.java").getFile() );
        a.convertToAST();
        ParserRuleContext ast = a.getContext();
        ParseTreeWalker walker = new ParseTreeWalker();
        CreateXALTree sv = new CreateXALTree();
        walker.walk(sv, ast);
        List<XALDocument> out = sv.getOutput();
        //I expect 4 automata
        assertEquals(out.size(),4);

        //check the output name
        String[] filenameExpected = {"PredictionModule","SwitchNode","SwitchEdge", "GenerateTopologyAsync"};
        for(int i = 0; i < out.size(); i++){
            assertEquals(out.get(i).getFilename(), filenameExpected[i]);
        }

        //check that each method belong to the correct automata

        //PredictionModule
        List<XALAutomaton> predictionModuleAutomaton = out.get(0).getAutomatons();
        String[] methodPredictionModuleExpected = {
                "getName","isCallbackOrderingPrereq","isCallbackOrderingPostreq", "receive",
                "getModuleServices", "getServiceImpls", "getModuleDependencies", "init",
                "startUp", "getTopology", "createTopology", "checkSwitchDuplicate", "getSwitchPosition",
                "getTopologyGraph", "dot", "json", "setTimeout", "getTimeout", "getPredictionStructure",
                "getMongoDBConnection", "setMongoDBConnection", "getBehaviourStructure"
        };
        assertEquals(predictionModuleAutomaton.size(), methodPredictionModuleExpected.length);
        for(int i = 0; i < methodPredictionModuleExpected.length; i++){
            assertEquals(predictionModuleAutomaton.get(i).getId(), methodPredictionModuleExpected[i]);
        }

        //SwitchNode
        List<XALAutomaton> switchNodeAutomaton = out.get(1).getAutomatons();
        String[] switchNodeModuleExpected = {
                "SwitchNode","getName","toString", "equals"
        };
        assertEquals(switchNodeAutomaton.size(), switchNodeModuleExpected.length);
        for(int i = 0; i < switchNodeModuleExpected.length; i++){
            assertEquals(switchNodeAutomaton.get(i).getId(), switchNodeModuleExpected[i]);
        }


        //SwitchEdge
        List<XALAutomaton> switchEdgeAutomaton = out.get(2).getAutomatons();
        String[] switchEdgeModuleExpected = {
                "SwitchEdge", "getFrom", "getTo", "toString"
        };
        assertEquals(switchEdgeAutomaton.size(), switchEdgeModuleExpected.length);
        for(int i = 0; i < switchEdgeModuleExpected.length; i++){
            assertEquals(switchEdgeAutomaton.get(i).getId(), switchEdgeModuleExpected[i]);
        }

        //GenerateTopologyAsync
        List<XALAutomaton> topologyAutomaton = out.get(3).getAutomatons();
        String[] topologyExpected = {
                "GenerateTopologyAsync", "run", "setRunning"
        };
        assertEquals(topologyAutomaton.size(), topologyExpected.length);
        for(int i = 0; i < topologyExpected.length; i++){
            assertEquals(topologyAutomaton.get(i).getId(), topologyExpected[i]);
        }


    }
}
