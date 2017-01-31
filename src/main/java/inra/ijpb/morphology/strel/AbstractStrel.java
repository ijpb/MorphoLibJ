/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package inra.ijpb.morphology.strel;

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
	
	private String channelName = null;
	
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
	
	/**
	 * Sets the name of the currently processed channel.
	 */
	public void setChannelName(String channelName) 
	{
		this.channelName = channelName;
	}

	/**
	 * Returns the name of the channel currently processed, or null by default.
	 */
	public String getChannelName() {
		return this.channelName;
		
	}
	public ImageStack dilation(ImageStack stack) {
		boolean flag = this.showProgress();
		this.showProgress(false);
		
		ImageStack result = stack.duplicate();
		
		int nSlices = stack.getSize();
		for (int i = 1; i <= nSlices; i++) {
			this.showProgress(flag);
			fireProgressChanged(this, i-1, nSlices);
			this.showProgress(false);
			
			ImageProcessor img = stack.getProcessor(i);
			img = dilation(img);
			result.setProcessor(img, i);
		}
		
		// notify end of slices progression
		this.showProgress(flag);
		fireProgressChanged(this, nSlices, nSlices);
		
		return result;
	}
	
	public ImageStack erosion(ImageStack stack) {
		boolean flag = this.showProgress();
		
		int nSlices = stack.getSize();
		ImageStack result = stack.duplicate();
		for (int i = 1; i <= nSlices; i++) {
			this.showProgress(flag);
			fireProgressChanged(this, i-1, nSlices);
			this.showProgress(false);
			
			ImageProcessor img = stack.getProcessor(i);
			img = erosion(img);
			result.setProcessor(img, i);
		}
		
		// notify end of slices progression
		this.showProgress(flag);
		fireProgressChanged(this, nSlices, nSlices);
		
		return result;
	}
	
	public ImageStack closing(ImageStack stack) {
		boolean flag = this.showProgress();
		this.showProgress(false);
		
		int nSlices = stack.getSize();
		ImageStack result = stack.duplicate();
		for (int i = 1; i <= nSlices; i++) {
			this.showProgress(flag);
			fireProgressChanged(this, i-1, nSlices);
			this.showProgress(false);
			
			ImageProcessor img = stack.getProcessor(i);
			img = closing(img);
			result.setProcessor(img, i);
		}
		
		// notify end of slices progression
		this.showProgress(flag);
		fireProgressChanged(this, nSlices, nSlices);
		
		return result;
	}
	
	public ImageStack opening(ImageStack stack) {
		boolean flag = this.showProgress();
		this.showProgress(false);
		
		int nSlices = stack.getSize();
		ImageStack result = stack.duplicate();
		for (int i = 1; i <= nSlices; i++) {
			this.showProgress(flag);
			fireProgressChanged(this, i-1, nSlices);
			this.showProgress(false);
			
			ImageProcessor img = stack.getProcessor(i);
			img = opening(img);
			result.setProcessor(img, i);
		}
		
		// notify end of slices progression
		this.showProgress(flag);
		fireProgressChanged(this, nSlices, nSlices);
		
		return result;
	}

}
