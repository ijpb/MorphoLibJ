/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.process.ImageProcessor;
import inra.ijpb.event.ProgressEvent;
import inra.ijpb.event.ProgressListener;

import java.util.Collection;

/**
 * Implementation stub for separable Structuring elements.
 * @author David Legland
 *
 */
public abstract class AbstractSeparableStrel extends AbstractStrel 
implements SeparableStrel, ProgressListener {

	public ImageProcessor dilation(ImageProcessor image) {
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel> strels = this.decompose();
		int n = strels.size();
		
		// Dilation
		int i = 1;
		for (InPlaceStrel strel : strels) {
			fireStatusChanged(this, "Dilation " + (i++) + "/" + n);
			runDilation(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageProcessor erosion(ImageProcessor image) {
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel> strels = this.decompose();
		int n = strels.size();
		
		// Erosion
		int i = 1;
		for (InPlaceStrel strel : strels) {
			fireStatusChanged(this, "Erosion " + (i++) + "/" + n);
			runErosion(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageProcessor closing(ImageProcessor image) {
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel> strels = this.decompose();
		int n = strels.size();
		
		// Dilation
		int i = 1;
		for (InPlaceStrel strel : strels) {
			fireStatusChanged(this, "Dilation " + (i++) + "/" + n);
			runDilation(result, strel);
		}
		
		// Erosion (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel strel : strels) {
			fireStatusChanged(this, "Erosion " + (i++) + "/" + n);
			runErosion(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageProcessor opening(ImageProcessor image) {
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel> strels = this.decompose();
		int n = strels.size();
		
		// Erosion
		int i = 1;
		for (InPlaceStrel strel : strels) {
			fireStatusChanged(this, "Erosion " + (i++) + "/" + n);
			runErosion(result, strel);
		}
		
		// Dilation (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel strel : strels) {
			fireStatusChanged(this, "Dilation " + (i++) + "/" + n);
			runDilation(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");

		return result;
	}
	
	private void runDilation(ImageProcessor image, InPlaceStrel strel) {
		strel.showProgress(this.showProgress());
		strel.addProgressListener(this);
		strel.inPlaceDilation(image);
		strel.removeProgressListener(this);
	}
	
	private void runErosion(ImageProcessor image, InPlaceStrel strel) {
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
