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
package inra.ijpb.label.conncomp;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.morphology.FloodFill3D;

/**
 * Computes the labels of the different connected components of a region within
 * a 3D label image. The type of result is controlled by the bitDepth option.
 * 
 * 
 * Uses a Flood-fill type algorithm. The image voxels are iterated, and each
 * time a foreground voxel not yet associated with a label is encountered, its
 * connected component is associated with a new label.
 * 
 * @see inra.ijpb.binary.conncomp.FloodFillComponentsLabeling
 * @see inra.ijpb.morphology.FloodFill3D
 * 
 * @author dlegland
 */
public class FloodFillRegionComponentsLabeling3D extends AlgoStub
{
	/** 
	 * The connectivity of the components, either 6 (default) or 26.
	 */
	int connectivity = 6;
	
	/**
	 * The number of bits for representing the result label image. Can be 8, 16
	 * (default), or 32.
	 */
	int bitDepth = 16;
	
	/**
	 * Constructor with default connectivity 6 and default output bitdepth equal to 16.  
	 */
	public FloodFillRegionComponentsLabeling3D()
	{
	}
	
	/**
	 * Constructor specifying the connectivity and using default output bitdepth equal to 16.  
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (6 or 26)
	 */
	public FloodFillRegionComponentsLabeling3D(int connectivity)
	{
		this.connectivity = connectivity;
	}
	
	/**
	 * Constructor specifying the connectivity and the bitdepth of result label
	 * image
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (6 or 26)
	 * @param bitDepth
	 *            the bit depth of the result (8, 16, or 32)
	 */
	public FloodFillRegionComponentsLabeling3D(int connectivity, int bitDepth)
	{
	    this.connectivity = connectivity;
	    this.bitDepth = bitDepth;
	}

    /**
     * Computes labels corresponding to connected components of a region within
     * a label map.
     * 
     * @param image
     *            the label map containing index of each region.
     * @param regionLabel
     *            the label of the region whose connected components have to be computed.
     * @return a new label map with the labels of the connected components of
     *         the specified region.
     */
	public ImageStack computeLabels(ImageStack image, int regionLabel)
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// initialize result image
		fireStatusChanged(this, "Allocate memory...");
		ImageStack labels = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);

		// identify the maximum label index
		int maxLabel;
		switch (this.bitDepth) {
		case 8: 
			maxLabel = 255;
			break; 
		case 16: 
			maxLabel = 65535;
			break;
		case 32:
			maxLabel = 0x01 << 23;
			break;
		default:
			throw new IllegalArgumentException(
					"Bit Depth should be 8, 16 or 32.");
		}

		fireStatusChanged(this, "Compute Labels...");
		
		// Iterate over image voxels. 
		// Each time a white voxel not yet associated
		// with a label is encountered, uses flood-fill to associate its
		// connected component to a new label
		int nLabels = 0;
		for (int z = 0; z < sizeZ; z++) 
		{
            if (Thread.currentThread().isInterrupted())
	        {
	            return null;
	        }
            fireProgressChanged(this, z, sizeZ);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					// Process only voxels belonging to the specified region
					if (image.getVoxel(x, y, z) != regionLabel)
						continue;

					// Do not process voxels already labeled
					if (labels.getVoxel(x, y, z) > 0)
						continue;

					// a new label is found: check current label number  
					if (nLabels == maxLabel)
					{
						throw new RuntimeException("Max number of label reached (" + maxLabel + ")");
					}
					
					// increment label index, and propagate
					nLabels++;
					fireStatusChanged(this, "Process label " + nLabels);
					FloodFill3D.floodFillFloat(image, x, y, z, labels, nLabels, this.connectivity);
				}
			}
		}
		
		fireStatusChanged(this, "");
		fireProgressChanged(this, 1, 1);
		return labels;
	}

}
