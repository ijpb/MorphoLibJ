/**
 * 
 */
package inra.ijpb.morphology.strel;

import inra.ijpb.algo.AlgoStub;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.morphology.Strel3D;


/**
 * Implementation basis for structuring elements, that mainly manages the 
 * flag for progress display.
 * @author David Legland
 *
 */
public abstract class AbstractStrel3D extends AlgoStub implements Strel3D {

	private boolean showProgress = true;
	
	public boolean showProgress() {
		return showProgress;
	}
	
	public void showProgress(boolean b) {
		this.showProgress = b;
	}
	
	protected void fireProgressChanged(Object source, double step, double total) {
		if (showProgress)
			super.fireProgressChanged(source, step, total);
	}

	protected void fireProgressChanged(AlgoEvent evt) {
		if (showProgress)
			super.fireProgressChanged(evt);
	}
	
	protected void fireStatusChanged(Object source, String message) {
		if (showProgress)
			super.fireStatusChanged(source, message);
	}

	protected void fireStatusChanged(AlgoEvent evt) {
		if (showProgress)
			super.fireStatusChanged(evt);
	}
}
