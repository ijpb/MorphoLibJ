/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
import inra.ijpb.shape.ImageShape;


/**
 * Implementation basis for planar structuring elements.
 * 
 * The aim of this class is mostly to allow the application of 2D structuring
 * elements to 3D stacks, without having to write specific code in derived
 * classes. Default implementations for 2D morphological operations are also
 * provided.
 * 
 * Morphological operations for stacks are implemented such that the planar
 * strel is applied to each slice, and the result is added to the result stack.
 * 
 * @author David Legland
 *
 */
public abstract class AbstractStrel extends AbstractStrel3D implements Strel
{
    // ==================================================
    // Class variables
    
    /**
     * The name of the channel currently processed. Used for monitoring
     * processing of color images. Default is null.
     */
	private String channelName = null;
	
	
    // ==================================================
    // Utility methods
    
	/**
     * Converts the specified (binary) mask into a series of shifts, assuming
     * the offset of the mask is located at its center.
     * 
     * @param mask
     *            a binary mask as a 2D array (first index is y, second is x)
     * @return the series of 2D shifts from offset/center, as n-by-2 array of
     *         signed integers. The first value corresponds to the shift in the
     *         x direction.
     */
	protected static final int[][] convertMaskToShifts(int[][] mask)
	{
	    // retrieve mask size
        int sizeY = mask.length;
        int sizeX = mask[0].length;
        
        // compute offsets, using automated rounding of division
        int offsetX = (sizeX - 1) / 2;
        int offsetY = (sizeY - 1) / 2;

        // count the number of positive elements
	    int n = 0;
	    for (int y = 0; y < sizeY; y++)
	    {
	        for (int x = 0; x < sizeX; x++)
	        {
	            if (mask[y][x] > 0)
	                n++;
	        }
	    }

	    // allocate result
	    int[][] offsets = new int[n][2];
	    
	    // fill up result array with positive elements
	    int i = 0;
	    for (int y = 0; y < sizeY; y++)
	    {
	        for (int x = 0; x < sizeX; x++)
	        {
	            if (mask[y][x] > 0)
	            {
	                offsets[i][0] = x - offsetX;
	                offsets[i][1] = y - offsetY;
	                i++;
	            }
	        }
	    }

	    return offsets;
	}
	
    /**
     * Adds a border around the image, to avoid edge effects when performing
     * morphological closing or opening. The size of the border is determined by
     * Strel size and offset.
     * 
     * Replicated border strategy is used to expand image.
     * 
     * @see #cropBorder(ImageProcessor)
     * 
     * @param image
     *            the image to pad
     * @return the image with border extended.
     */
	public ImageProcessor addBorder(ImageProcessor image)
	{
        // compute padding for each border
        int[] strelSize = this.getSize();
        int[] strelOffset = this.getOffset();
        int padX0 = strelOffset[0];
        int padY0 = strelOffset[1];
        int padX1 = strelSize[0] - 1 - strelOffset[0];
        int padY1 = strelSize[1] - 1 - strelOffset[1];
        
        // pad image
        return ImageShape.addBorders(image, padX0, padX1, padY0, padY1);
	}
	
	/**
     * Retrieve the portion of image that corresponds to the original image
     * before adding border. The crop parameters are retrieved from strel size
     * and offset.
     * 
     * This method works together with the addBorder method. In practice, we
     * retrieve the input image if we perform the following:
     * 
     * <pre>{@code
     * ImageProcessor result = strel.cropBorder(strel.addBorder(inputImage));
     * }</pre>
     * 
     * @see #addBorder(ImageProcessor)
     * 
     * @param image
     *            the image to crop
     * @return the image with strel padding removed.
     */
    public ImageProcessor cropBorder(ImageProcessor image)
    {
        // retrieve crop params from strel size and offset
        int[] strelSize = this.getSize();
        int[] strelOffset = this.getOffset();
        int padX0 = strelOffset[0];
        int padY0 = strelOffset[1];
        int sizeX = image.getWidth() - strelSize[0] + 1;
        int sizeY = image.getHeight() - strelSize[1] + 1;
        
        // crop result
        return ImageShape.cropRect(image, padX0, padY0, sizeX, sizeY);
    }
    
	
    // ==================================================
    // Default implementation of Strel methods
	
	/**
     * Implements a default algorithm for dilation, that consists in iterating
     * over the neighbors of each pixel to compute the maximum value.
     * The neighbors are obtained via the <code>getShifts()</code> method.
     * 
     * @see #getShifts()
     * @see #erosion(ImageProcessor)
     * 
     * @param image
     *            the input image
     * @return the result of dilation with this structuring element
     */
    @Override
    public ImageProcessor dilation(ImageProcessor image)
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // allocate result
        ImageProcessor res = image.duplicate();
        
        // iterate over pixels
        int[][] shifts = getShifts();
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                float value = image.getf(x, y);
                
                // iterate over neighbors
                for (int[] shift : shifts)
                {
                    int x2 = x + shift[0];
                    int y2 = y + shift[1];
                    if (x2 < 0 || x2 >= sizeX) continue;
                    if (y2 < 0 || y2 >= sizeY) continue;
                    
                    value = Math.max(value, image.getf(x2, y2));
                }
                
                res.setf(x, y, value);
            }
        }
        
        return res;
    }

    /**
     * Implements a default algorithm for erosion, that consists in iterating
     * over the neighbors of each pixel to compute the minimum value.
     * The neighbors are obtained via the <code>getShifts()</code> method.
     * 
     * @see #getShifts()
     * @see #dilation(ImageProcessor)
     * 
     * @param image
     *            the input image
     * @return the result of erosion with this structuring element
     */
    @Override
    public ImageProcessor erosion(ImageProcessor image)
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // allocate result
        ImageProcessor res = image.duplicate();
        
        // iterate over pixels
        int[][] shifts = getShifts();
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                float value = image.getf(x, y);
                
                // iterate over neighbors
                for (int[] shift : shifts)
                {
                    int x2 = x + shift[0];
                    int y2 = y + shift[1];
                    if (x2 < 0 || x2 >= sizeX) continue;
                    if (y2 < 0 || y2 >= sizeY) continue;
                    
                    value = Math.min(value, image.getf(x2, y2));
                }
                
                res.setf(x, y, value);
            }
        }
        
        return res;
    }

    /**
     * Performs a morphological closing on the input image, by applying first a
     * dilation, then an erosion with the reversed structuring element.
     * 
     * @param image
     *            the input image
     * @return the result of closing with this structuring element
     * @see #dilation(ij.process.ImageProcessor)
     * @see #erosion(ij.process.ImageProcessor)
     * @see #opening(ij.process.ImageProcessor)
     * @see #reverse()
     */
    @Override
    public ImageProcessor closing(ImageProcessor image)
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // compute padding for each border
        int[] strelSize = this.getSize();
        int[] strelOffset = this.getOffset();
        int padX0 = strelOffset[0];
        int padY0 = strelOffset[1];
        int padX1 = strelSize[0] - strelOffset[0];
        int padY1 = strelSize[1] - strelOffset[1];
        
        // pad image
        image = ImageShape.addBorders(image, padX0, padX1, padY0, padY1);
        
        // compute morphological closing on padded image
        image = this.reverse().erosion(this.dilation(image));
        
        // crop result
        image = ImageShape.cropRect(image, padX0, padY0, sizeX, sizeY);
        
        return image;
    }

    /**
     * Performs a morphological opening on the input image, by applying first an
     * erosion, then a dilation with the reversed structuring element.
     * 
     * @param image
     *            the input image
     * @return the result of opening with this structuring element
     * @see #dilation(ij.process.ImageProcessor)
     * @see #erosion(ij.process.ImageProcessor)
     * @see #closing(ij.process.ImageProcessor)
     * @see #reverse()
     */
    @Override
    public ImageProcessor opening(ImageProcessor image)
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // compute padding for each border
        int[] strelSize = this.getSize();
        int[] strelOffset = this.getOffset();
        int padX0 = strelOffset[0];
        int padY0 = strelOffset[1];
        int padX1 = strelSize[0] - strelOffset[0];
        int padY1 = strelSize[1] - strelOffset[1];
        
        // pad image
        image = ImageShape.addBorders(image, padX0, padX1, padY0, padY1);
        
        // compute morphological opening on padded image
        image = this.reverse().dilation(this.erosion(image));
        
        // crop result
        image = ImageShape.cropRect(image, padX0, padY0, sizeX, sizeY);
        
        return image;
    }
    
    
	// ==================================================
	// Default implementation of Strel3D methods
    
	public int[][][] getMask3D()
	{
		int[][][] mask3d = new int[1][][];
		mask3d[0] = getMask();
		return mask3d;
	}
	
	public int[][] getShifts3D()
	{
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
    public String getChannelName()
    {
        return this.channelName;
    }

    public ImageStack dilation(ImageStack stack)
    {
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
	
    public ImageStack erosion(ImageStack stack)
    {
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
	
    public ImageStack closing(ImageStack stack)
    {
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
	
    public ImageStack opening(ImageStack stack)
    {
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
