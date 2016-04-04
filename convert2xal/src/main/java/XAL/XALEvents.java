package XAL;

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
