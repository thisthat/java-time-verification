package IntermediateModel.interfaces;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.grammar.Java8CommentSupportedParser;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface LocalSearch {

	public <T extends ParseTree> T get(ParserRuleContext elm);
}
