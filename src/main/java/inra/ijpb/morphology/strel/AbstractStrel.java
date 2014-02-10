/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.IJ;
import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;


/**
 * Implementation basis for planar structuring elements. 
 * Morphological operations for stacks are implemented such that the planar
 * strel is applied to each slice, and the result is added to the result
 * stack? 
 * 
 * @author David Legland
 *
 */
public abstract class AbstractStrel extends AbstractStrel3D implements Strel {
	
	public int[][][] getMask3D() {
		int[][][] mask3d = new int[1][][];
		mask3d[0] = getMask();
		return mask3d;
	}
	
	public int[][] getShifts3D() {
		int [][] shifts = getShifts();
		int ns = shifts.length;
		
		int [][] shifts3d = new int[ns][3];
		for (int i = 0; i < ns; i++)
			shifts3d[i] = new int[]{shifts3d[i][0], shifts3d[i][1], 0};
		
		return shifts3d;
	}
	
	public ImageStack dilation(ImageStack stack) {
		boolean flag = this.showProgress();
		this.showProgress(false);
		
		ImageStack result = stack.duplicate();
		
		int nSlices = stack.getSize();
		for (int i = 1; i <= nSlices; i++) {
			if (flag) {
				IJ.showProgress(i-1, nSlices);
			}
			
			ImageProcessor img = stack.getProcessor(i);
			img = dilation(img);
			result.setProcessor(img, i);
		}
		
		if (flag) {
			IJ.showProgress(1);
		}
		this.showProgress(flag);
		
		return result;
	}
	
	public ImageStack erosion(ImageStack stack) {
		boolean flag = this.showProgress();
		this.showProgress(false);
		
		int nSlices = stack.getSize();
		ImageStack result = stack.duplicate();
		for (int i = 1; i <= nSlices; i++) {
			if (flag) {
				IJ.showProgress(i-1, nSlices);
			}
			
			ImageProcessor img = stack.getProcessor(i);
			img = erosion(img);
			result.setProcessor(img, i);
		}
		
		if (flag) {
			IJ.showProgress(1);
		}
		this.showProgress(flag);
		
		return result;
	}
	
	public ImageStack closing(ImageStack stack) {
		boolean flag = this.showProgress();
		this.showProgress(false);
		
		int nSlices = stack.getSize();
		ImageStack result = stack.duplicate();
		for (int i = 1; i <= nSlices; i++) {
			if (flag) {
				IJ.showProgress(i-1, nSlices);
			}
			
			ImageProcessor img = stack.getProcessor(i);
			img = closing(img);
			result.setProcessor(img, i);
		}
		
		if (flag) {
			IJ.showProgress(1);
		}
		this.showProgress(flag);
		
		return result;
	}
	
	public ImageStack opening(ImageStack stack) {
		boolean flag = this.showProgress();
		this.showProgress(false);
		
		int nSlices = stack.getSize();
		ImageStack result = stack.duplicate();
		for (int i = 1; i <= nSlices; i++) {
			if (flag) {
				IJ.showProgress(i-1, nSlices);
			}
			
			ImageProcessor img = stack.getProcessor(i);
			img = opening(img);
			result.setProcessor(img, i);
		}
		
		if (flag) {
			IJ.showProgress(1);
		}
		this.showProgress(flag);
		
		return result;
	}

}
