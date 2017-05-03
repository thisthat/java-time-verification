package uppaal.replaceParameter;

import com.rits.cloning.Cloner;
import org.javatuples.Pair;
import uppaal.Automaton;
import uppaal.Declaration;
import uppaal.NTA;
import uppaal.Transition;
import uppaal.labels.Guard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by giovanni on 02/05/2017.
 */
public class ReplaceTraces {

    String file;
    NTA model;

    public ReplaceTraces(String templatePath) {
        this.file = templatePath;
        open();
    }

    private void open() {
        model = new NTA(this.file);
    }

    /**
     * Create a new model where it replace all the guards with the value given in replaces
     * @param replaces List of pairs: &lt;Variable,Value&gt;. It replaces every <i>Variable</i> with its <i>Value</i>.
     * @param notClock If true, it will replace only variables that are not defined as clock variables.
     * @return A new NTA with the replacements.
     */
    public NTA replace(List<Pair<String,String>> replaces, boolean notClock){
        Cloner cloner = new Cloner();
        NTA clone = cloner.deepClone(this.model);
        List<String> clockVars = new ArrayList<>();
        String pattern = "clock (.*);";
        Pattern r = Pattern.compile(pattern);
        for(Automaton a : clone.getAutomata()){
            Declaration declarations = a.getDeclaration();
            for(String dec : declarations.getStrings()){
                Matcher m = r.matcher(dec);
                if(m.find()){
                    clockVars.add(m.group(1));
                }
            }
        }
        for(Pair<String,String> p : replaces){
            String var = p.getValue0();
            String val = p.getValue1();
            if(notClock && clockVars.contains(var)){
                continue;
            }
            for(Automaton a : clone.getAutomata()){
                for(Transition t : a.getTransitions()){
                    Guard g  = t.getGuard();
                    if(g != null)
                        t.setGuard(g.toString().replace(var,val));
                }
            }
        }
        return clone;
    }

    /**
     * See {@link #replace(List, boolean)}.
     * It will not replace clock variables.
     * @param replaces List of pairs: &lt;Variable,Value&gt;. It replaces every <i>Variable</i> with its <i>Value</i>.
     * @return A new NTA with the replacements.
     */
    public NTA replace(List<Pair<String,String>> replaces){
        return replace(replaces,true);
    }
}
