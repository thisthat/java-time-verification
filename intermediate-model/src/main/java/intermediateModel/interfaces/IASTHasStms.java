package intermediateModel.interfaces;

import java.util.List;

/**
 * @author Giovanni Liva (@thisthatDC)
 * @version %I%, %G%
 */
public interface IASTHasStms {
	void addStms(IASTStm stm);
	List<IASTStm> getStms();
	int getLineEnd();
}
