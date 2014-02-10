/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.IJ;
import ij.ImageStack;

import java.util.Collection;

/**
 * Implementation stub for separable Structuring elements.
 * @author David Legland
 *
 */
public abstract class AbstractSeparableStrel3D extends AbstractStrel3D implements SeparableStrel3D {

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
			if (this.showProgress()) {
				IJ.showStatus("Dilation " + (i++) + "/" + n);
			}
			
			strel.showProgress(this.showProgress());
			strel.inPlaceDilation(result);
//			long t = System.currentTimeMillis();
//			long dt = t - t0;
//			System.out.println("elapsed time: " + (dt / 1000) + " s.");
//			t0 = t;
		}
		
		// clear status bar
		if (this.showProgress()) {
			IJ.showStatus("");
		}
		
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

	public ImageStack closing(ImageStack stack) {
		// Allocate memory for result
		ImageStack result = stack.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel3D> strels = this.decompose();
		int n = strels.size();
		
		// Dilation
		int i = 1;
		for (InPlaceStrel3D strel : strels) {
			if (this.showProgress()) {
				IJ.showStatus("Dilation " + (i++) + "/" + n);
			}
			strel.showProgress(this.showProgress());
			strel.inPlaceDilation(result);
		}
		
		// Erosion (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel3D strel : strels) {
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

	public ImageStack opening(ImageStack stack) {
		// Allocate memory for result
		ImageStack result = stack.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel3D> strels = this.decompose();
		int n = strels.size();
		
		// Erosion
		int i = 1;
		for (InPlaceStrel3D strel : strels) {
			if (this.showProgress()) {
				IJ.showStatus("Erosion " + (i++) + "/" + n);
			}
			
			strel.showProgress(this.showProgress());
			strel.inPlaceErosion(result);
		}
		
		// Dilation (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel3D strel : strels) {
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
