package intermediateModel.interfaces;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface LocalSearch {

	//public <T extends ParseTree> T get(ParserRuleContext elm);
	static <T extends ParseTree> T get(ParserRuleContext elm, Class<T> type){
		T f = (elm.getClass() == type) ? (T) elm : null;
		for(ParseTree c : elm.children){
			if(c.getClass() == type){
				f = (T) c;
			}
			else if(c instanceof TerminalNode){
				continue;
			}
			else {
				T tmp = get((ParserRuleContext) c, type);
				if(tmp != null){
					f = tmp;
				}
			}
		}
		return f;
	}
}
