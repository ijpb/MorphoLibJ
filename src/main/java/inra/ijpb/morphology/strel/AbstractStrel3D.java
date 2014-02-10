/**
 * 
 */
package inra.ijpb.morphology.strel;

import inra.ijpb.morphology.Strel3D;


/**
 * Implementation basis for structuring elements, that mainly manages the 
 * flag for progress display.
 * @author David Legland
 *
 */
public abstract class AbstractStrel3D implements Strel3D {

	private boolean showProgress = true;
	
	public boolean showProgress() {
		return showProgress;
	}
	
	public void showProgress(boolean b) {
		this.showProgress = b;
	}
}
