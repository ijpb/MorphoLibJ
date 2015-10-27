/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.ImageStack;
import inra.ijpb.algo.ProgressEvent;
import inra.ijpb.algo.ProgressListener;

import java.util.Collection;

/**
 * Implementation stub for separable Structuring elements.
 * @author David Legland
 *
 */
public abstract class AbstractSeparableStrel3D extends AbstractStrel3D
		implements SeparableStrel3D, ProgressListener {

	public ImageStack dilation(ImageStack stack) {
		// Allocate memory for result
		ImageStack result = stack.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel3D> strels = this.decompose();
		int n = strels.size();
		
		// Dilation
//		long t0 = System.currentTimeMillis();
		int i = 1;
		for (InPlaceStrel3D strel : strels) {
			fireStatusChanged(this, "Dilation " + (i++) + "/" + n);
			runDilation(result, strel);
			
//			long t = System.currentTimeMillis();
//			long dt = t - t0;
//			System.out.println("elapsed time: " + (dt / 1000) + " s.");
//			t0 = t;
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageStack erosion(ImageStack stack) {
		// Allocate memory for result
		ImageStack result = stack.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel3D> strels = this.decompose();
		int n = strels.size();
		
		// Erosion
		int i = 1;
		for (InPlaceStrel3D strel : strels) {
			fireStatusChanged(this, "Erosion " + (i++) + "/" + n);
			runErosion(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageStack closing(ImageStack stack) {
		// Allocate memory for result
		ImageStack result = stack.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel3D> strels = this.decompose();
		int n = strels.size();
		
		// Dilation
		int i = 1;
		for (InPlaceStrel3D strel : strels) {
			fireStatusChanged(this, "Dilation " + (i++) + "/" + n);
			runDilation(result, strel);
		}
		
		// Erosion (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel3D strel : strels) {
			fireStatusChanged(this, "Erosion " + (i++) + "/" + n);
			runErosion(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageStack opening(ImageStack stack) {
		// Allocate memory for result
		ImageStack result = stack.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel3D> strels = this.decompose();
		int n = strels.size();
		
		// Erosion
		int i = 1;
		for (InPlaceStrel3D strel : strels) {
			fireStatusChanged(this, "Erosion " + (i++) + "/" + n);
			runErosion(result, strel);
		}
		
		// Dilation (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel3D strel : strels) {
			fireStatusChanged(this, "Dilation " + (i++) + "/" + n);
			runDilation(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");

		return result;
	}
	
	private void runDilation(ImageStack image, InPlaceStrel3D strel) {
		strel.showProgress(this.showProgress());
		strel.addProgressListener(this);
		strel.inPlaceDilation(image);
		strel.removeProgressListener(this);
	}
	
	private void runErosion(ImageStack image, InPlaceStrel3D strel) {
		strel.showProgress(this.showProgress());
		strel.addProgressListener(this);
		strel.inPlaceErosion(image);
		strel.removeProgressListener(this);
	}
	
	/**
	 * Propagates the event by changing the source.
	 */
	public void progressChanged(ProgressEvent evt) {
		this.fireProgressChange(this, evt.getStep(), evt.getTotal());
	}
}
