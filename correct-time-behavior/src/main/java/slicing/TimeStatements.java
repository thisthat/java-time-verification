package slicing;

import intermediateModel.interfaces.IASTStm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 10/07/2017.
 */
public class TimeStatements {
    private static TimeStatements instance = null;

    private List<IASTStm> stms = new ArrayList<>();

    public static synchronized TimeStatements getInstance(){
        if(instance == null){
            instance = new TimeStatements();
        }
        return instance;
    }

    public synchronized void addStatements(IASTStm stm){
        if(stms.contains(stm)){
            return;
        }
        stms.add(stm);
    }

    public synchronized List<IASTStm> getStms(){
        return stms;
    }

    public synchronized void clear(){
        stms.clear();
    }
}
