package XAL.XALStructure;

import XAL.XALStructure.items.XALState;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface XALAddState {
    void addState(XALState s);
    List<XALState> getStates();

    boolean existState(String s);
    boolean existState(XALState s);
}
