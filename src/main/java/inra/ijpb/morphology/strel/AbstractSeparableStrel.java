/**
 * 
 */
package inra.ijpb.morphology.strel;

import java.util.Collection;

import ij.IJ;
import ij.process.ImageProcessor;

/**
 * Implementation stub for separable Structuring elements.
 * @author David Legland
 *
 */
public abstract class AbstractSeparableStrel extends AbstractStrel implements SeparableStrel {

	public ImageProcessor dilation(ImageProcessor image) {
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel> strels = this.decompose();
		int n = strels.size();
		
		// Dilation
		int i = 1;
		for (InPlaceStrel strel : strels) {
			if (this.showProgress()) {
				IJ.showStatus("Dilation " + (i++) + "/" + n);
			}
			
			strel.showProgress(this.showProgress());
			strel.inPlaceDilation(result);
		}
		
		// clear status bar
		if (this.showProgress()) {
			IJ.showStatus("");
		}
		
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
			if (this.showProgress()) {
				IJ.showStatus("Erosion " + (i++) + "/" + n);
			}
			
			strel.showProgress(this.showProgress());
			strel.inPlaceErosion(result);
		}
		
		// clear status bar
		if (this.showProgress()) {
			IJ.showStatus("");
		}
		
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
			if (this.showProgress()) {
				IJ.showStatus("Dilation " + (i++) + "/" + n);
			}
			strel.showProgress(this.showProgress());
			strel.inPlaceDilation(result);
		}
		
		// Erosion (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel strel : strels) {
			if (this.showProgress()) {
				IJ.showStatus("Erosion " + (i++) + "/" + n);
			}
			strel.showProgress(this.showProgress());
			strel.inPlaceErosion(result);
		}
		
		// clear status bar
		if (this.showProgress()) {
			IJ.showStatus("");
		}
		
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
			if (this.showProgress()) {
				IJ.showStatus("Erosion " + (i++) + "/" + n);
			}
			
			strel.showProgress(this.showProgress());
			strel.inPlaceErosion(result);
		}
		
		// Dilation (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel strel : strels) {
			if (this.showProgress()) {
				IJ.showStatus("Dilation " + (i++) + "/" + n);
			}
			
			strel.showProgress(this.showProgress());
			strel.inPlaceDilation(result);
		}
		
		// clear status bar
		if (this.showProgress()) {
			IJ.showStatus("");
		}

		return result;
	}
}
