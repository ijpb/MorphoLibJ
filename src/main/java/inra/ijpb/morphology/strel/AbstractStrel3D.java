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
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.morphology.Strel3D;


/**
 * Implementation basis for 3D structuring elements
 * 
 * @author David Legland
 */
public abstract class AbstractStrel3D extends AlgoStub implements Strel3D
{
    // ==================================================
    // Utility methods
    
    /**
     * Converts the specified (binary) mask into a series of shifts, assuming
     * the offset of the mask is located at its center.
     * 
     * @param mask
     *            a binary mask as a 3D array (first index is z, second is y,
     *            last one is x)
     * @return the series of 3D shifts from offset/center, as n-by-2 array of
     *         signed integers. The first value corresponds to the shift in the
     *         x direction.
     */
    protected static final int[][] convertMaskToShifts(int[][][] mask)
    {
        // retrieve mask size
        int sizeZ = mask.length;
        int sizeY = mask[0].length;
        int sizeX = mask[0][0].length;
        
        // compute offsets, using automated rounding of division
        int offsetX = (sizeX - 1) / 2;
        int offsetY = (sizeY - 1) / 2;
        int offsetZ = (sizeZ - 1) / 2;

        // count the number of positive elements
        int n = 0;
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    if (mask[z][y][x] > 0)
                        n++;
                }
            }
        }

        // allocate result
        int[][] offsets = new int[n][3];
        
        // fill up result array with positive elements
        int i = 0;
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    if (mask[z][y][x] > 0)
                    {
                        offsets[i][0] = x - offsetX;
                        offsets[i][1] = y - offsetY;
                        offsets[i][2] = z - offsetZ;
                        i++;
                    }
                }
            }
        }
        return offsets;
    }
    

    // ===================================================================
	// Class variables
	
	/**
	 * Local flag indicating whether this algorithm should display progress or
	 * not. This can be used to toggle progress of nested strels operations.
	 */
	private boolean showProgress = true;

	
	// ===================================================================
	// Setter and getters
	
	public boolean showProgress()
	{
		return showProgress;
	}

	public void showProgress(boolean b)
	{
		this.showProgress = b;
	}


	// ===================================================================
	// Default implementation of some methods
	
    /**
     * Implements a default algorithm for dilation, that consists in iterating
     * over the neighbors of each pixel to compute the maximum value.
     * The neighbors are obtained via the <code>getShifts()</code> method.
     * 
     * @see #getShifts3D()
     * @see #erosion(ImageStack)
     * 
     * @param image
     *            the input image
     * @return the result of dilation with this structuring element
     */
	@Override
	public ImageStack dilation(ImageStack image)
	{
	    // retrieve image size
	    int sizeX = image.getWidth();
	    int sizeY = image.getHeight();
	    int sizeZ = image.getSize();

	    // allocate result
	    ImageStack res = ImageStack.create(sizeX, sizeY, sizeZ, image.getBitDepth());

	    // iterate over pixels
	    int[][] shifts = getShifts3D();
	    for (int z = 0; z < sizeZ; z++)
	    {
	        for (int y = 0; y < sizeY; y++)
	        {
	            for (int x = 0; x < sizeX; x++)
	            {
	                double value = image.getVoxel(x, y, z);

	                // iterate over neighbors
	                for (int[] shift : shifts)
	                {
	                    int x2 = x + shift[0];
	                    int y2 = y + shift[1];
	                    int z2 = z + shift[2];
	                    if (x2 < 0 || x2 >= sizeX) continue;
	                    if (y2 < 0 || y2 >= sizeY) continue;
	                    if (z2 < 0 || z2 >= sizeZ) continue;

	                    value = Math.max(value, image.getVoxel(x2, y2, z2));
	                }

	                res.setVoxel(x, y, z, value);
	            }
	        }
	    }

	    return res;
	}

    /**
     * Implements a default algorithm for erosion, that consists in iterating
     * over the neighbors of each pixel to compute the minimum value.
     * The neighbors are obtained via the <code>getShifts()</code> method.
     * 
     * @see #getShifts3D()
     * @see #dilation(ImageStack)
     * 
     * @param image
     *            the input image
     * @return the result of erosion with this structuring element
     */
	@Override
	public ImageStack erosion(ImageStack image)
	{
	    // retrieve image size
	    int sizeX = image.getWidth();
	    int sizeY = image.getHeight();
	    int sizeZ = image.getSize();

	    // allocate result
	    ImageStack res = ImageStack.create(sizeX, sizeY, sizeZ, image.getBitDepth());

	    // iterate over pixels
	    int[][] shifts = getShifts3D();
	    for (int z = 0; z < sizeZ; z++)
	    {
	        for (int y = 0; y < sizeY; y++)
	        {
	            for (int x = 0; x < sizeX; x++)
	            {
	                double value = image.getVoxel(x, y, z);

	                // iterate over neighbors
	                for (int[] shift : shifts)
	                {
	                    int x2 = x + shift[0];
	                    int y2 = y + shift[1];
	                    int z2 = z + shift[2];
	                    if (x2 < 0 || x2 >= sizeX) continue;
	                    if (y2 < 0 || y2 >= sizeY) continue;
	                    if (z2 < 0 || z2 >= sizeZ) continue;

	                    value = Math.min(value, image.getVoxel(x2, y2, z2));
	                }

	                res.setVoxel(x, y, z, value);
	            }
	        }
	    }

	    return res;
	}

	public ImageStack closing(ImageStack stack)
	{
		return this.reverse().erosion(this.dilation(stack));
	}

	public ImageStack opening(ImageStack stack)
	{
		return this.reverse().dilation(this.erosion(stack));
	}
	
	
	// ===================================================================
	// Management of progress status
	
	protected void fireProgressChanged(Object source, double step, double total)
	{
		if (showProgress)
			super.fireProgressChanged(source, step, total);
	}

	protected void fireProgressChanged(AlgoEvent evt)
	{
		if (showProgress)
			super.fireProgressChanged(evt);
	}

	protected void fireStatusChanged(Object source, String message)
	{
		if (showProgress)
			super.fireStatusChanged(source, message);
	}

	protected void fireStatusChanged(AlgoEvent evt)
	{
		if (showProgress)
			super.fireStatusChanged(evt);
	}
}
