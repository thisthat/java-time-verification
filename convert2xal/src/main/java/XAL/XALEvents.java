package XAL;

import java.util.List;

/**
 * Created by Giovanni Liva on 11.02.16.
 */

/**
 * Interface to implement in order to receive the events for the convertion.
 *
 * @author      Giovanni Liva
 * @version     %I%, %G%
 */
public interface XALEvents {
    void OnGeneratingStart();
    void OnGeneratingFinished();
}
