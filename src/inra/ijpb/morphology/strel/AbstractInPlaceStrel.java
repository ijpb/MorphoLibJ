/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.IJ;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * Implementation stub for in place Structuring elements.
 * Implements operations methods by calling in-place versions.  
 * @author David Legland
 *
 */
public abstract class AbstractInPlaceStrel extends AbstractStrel implements
		InPlaceStrel {
	
	public ImageStack dilation(ImageStack stack) {
		ImageStack result = stack.duplicate();
		this.inPlaceDilation(result);
		return result;
	}
	
	public ImageStack erosion(ImageStack stack) {
		ImageStack result = stack.duplicate();
		this.inPlaceErosion(result);
		return result;
	}
	
	public ImageStack closing(ImageStack stack) {
		ImageStack result = stack.duplicate();
		this.inPlaceDilation(result);
		this.reverse().inPlaceErosion(result);
		return result;
	}
	
	public ImageStack opening(ImageStack stack) {
		ImageStack result = stack.duplicate();
		this.inPlaceErosion(result);
		this.reverse().inPlaceDilation(result);
		return result;
	}
	
	public void inPlaceDilation(ImageStack stack) {
		boolean flag = this.showProgress();
		this.showProgress(false);
		
		int nSlices = stack.getSize();
		for (int i = 1; i <= nSlices; i++) {
			if (flag) {
				IJ.showProgress(i-1, nSlices);
			}
			
			ImageProcessor img = stack.getProcessor(i);
			this.inPlaceDilation(img);
			stack.setProcessor(img, i);
		}
		
		if (flag) {
			IJ.showProgress(1);
		}
		this.showProgress(flag);
	}

	public void inPlaceErosion(ImageStack stack) {
		boolean flag = this.showProgress();
		this.showProgress(false);
		
		int nSlices = stack.getSize();
		for (int i = 1; i <= nSlices; i++) {
			if (flag) {
				IJ.showProgress(i-1, nSlices);
			}
			
			ImageProcessor img = stack.getProcessor(i);
			this.inPlaceErosion(img);
			stack.setProcessor(img, i);
		}

		if (flag) {
			IJ.showProgress(1);
		}
		this.showProgress(flag);
	}

	public ImageProcessor dilation(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		this.inPlaceDilation(result);
		return result;
	}
	
	public ImageProcessor erosion(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		this.inPlaceErosion(result);
		return result;
	}
	
	public ImageProcessor closing(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		this.inPlaceDilation(result);
		this.reverse().inPlaceErosion(result);
		return result;
	}
	
	public ImageProcessor opening(ImageProcessor image) {
		ImageProcessor result = image.duplicate();
		this.inPlaceErosion(result);
		this.reverse().inPlaceDilation(result);
		return result;
	}
}
