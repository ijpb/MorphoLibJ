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
package inra.ijpb.label.distmap;

import java.util.Collection;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.ChamferWeights3D.ShortOffset;

/**
 * Apply a dilation by a specified radius to each label of a label map by
 * constraining the dilation. Labels can not dilate over existing labels.
 * 
 * 
 * @author dlegland
 * 
 */
public class LabelDilation3D4WShort extends AlgoStub 
{
	// ==================================================
	// Class variables
	
	short[] weights = new short[]{5, 7, 11};

	
	// ==================================================
	// Constructors 
	
	public LabelDilation3D4WShort(ChamferWeights3D weights) 
	{
		this(weights.getShortWeights());
	}

	public LabelDilation3D4WShort(short[] weights) 
	{
		this.weights = weights;
		
		// ensure the number of weight is at least 4.
		if (weights.length < 4)
		{
			throw new IllegalArgumentException("Weights array must have length equal to 4");
		}
	}

	// ==================================================
	// Methods 
	
	/**
	 * Computes the geodesic distance function for each pixel in mask label
	 * image, using the given binary marker image. Mask and marker should be
	 * ImageProcessor the same size and containing integer values.
	 * 
	 * The function returns a new ShortProcessor the same size as the input,
	 * with values greater or equal to zero.
	 *
	 * @param marker
	 *            the binary marker image
	 * @param mask
	 *            the label image used as mask
	 * @return the geodesic distance map from the marker image within each label
	 *         of the mask
	 * @see inra.ijpb.binary.geodesic.GeodesicDistanceTransform#geodesicDistanceMap(ij.process.ImageProcessor,
	 *      ij.process.ImageProcessor)
	 */
	public ImageStack process(ImageStack labelImage, double distMax)
	{
		// update inner data
		double maxDist = distMax * this.weights[0];
		
		fireStatusChanged(this, "Initialization..."); 
		// the instance of ImageProcessor storing the result label map
		ImageStack res = labelImage.duplicate();
		// the distance map to the closest label
		ImageStack distMap = initialize(labelImage);


		// forward iteration
		fireStatusChanged(this, "Forward iteration");
		forwardIteration(res, distMap, maxDist);

		// backward iteration
		fireStatusChanged(this, "Backward iteration"); 
		backwardIteration(res, distMap, maxDist);
		
		return res;
	}

	private ImageStack initialize(ImageStack marker)
	{
		// size of image
		int sizeX = marker.getWidth();
		int sizeY = marker.getHeight();
		int sizeZ = marker.getSize();
		
		ImageStack distMap = ImageStack.create(sizeX, sizeY, sizeZ, 16);

		// initialize empty image with either 0 (foreground) or Inf (background)
		for (int z = 0; z < sizeZ; z++) 
		{
			this.fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int val = (int) marker.getVoxel(x, y, z);
					distMap.setVoxel(x, y, z, val == 0 ? Short.MAX_VALUE : 0);
				}
			}
		}
		return distMap;
	}
	
	private void forwardIteration(ImageStack res, ImageStack distMap, double distMax) 
	{
		// size of image
		int sizeX = res.getWidth();
		int sizeY = res.getHeight();
		int sizeZ = res.getSize();

		// compute offsets of the neighborhood in forward direction
		Collection<ShortOffset> offsets = ChamferWeights3D.getForwardOffsets(weights);
		
		// Iterate over voxels
		for (int z = 0; z < sizeZ; z++) 
		{
			this.fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					// current distance value
					int currentDist = (int) distMap.getVoxel(x, y, z);

					// do not process within labels of original image
					if (currentDist == 0)
						continue;
					
					int minDist = currentDist;
					int closestLabel = 0;

					// iterate over neighbors
					for (ShortOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;

						// check bounds
						if (x2 < 0 || x2 >= sizeX)
							continue;
						if (y2 < 0 || y2 >= sizeY)
							continue;
						if (z2 < 0 || z2 >= sizeZ)
							continue;

						// distance from neighbor
						int dist = (int) (distMap.getVoxel(x2, y2, z2) + offset.weight);

						if (dist < minDist)
						{
							minDist = dist;
							closestLabel = (int) res.getVoxel(x2, y2, z2);
						}
					}

					// update current pixel if necessary
					if (minDist < currentDist && minDist < distMax)
					{
						distMap.setVoxel(x, y, z, minDist);
						res.setVoxel(x, y, z, closestLabel);
					}
				}
			}
		}		
		this.fireProgressChanged(this, sizeY, sizeY);
	}

	private void backwardIteration(ImageStack res, ImageStack distMap, double distMax)
	{
		// size of image
		int sizeX = res.getWidth();
		int sizeY = res.getHeight();
		int sizeZ = res.getSize();

		// compute offsets of the neighborhood in backward direction
		Collection<ShortOffset> offsets = ChamferWeights3D.getBackwardOffsets(weights);
		
		// Iterate over voxels
		for (int z = 0; z < sizeZ; z++) 
		{
			this.fireProgressChanged(this, z, sizeZ);
			for (int y = sizeY-1; y >= 0; y--)
			{
				for (int x = sizeX-1; x >= 0; x--)
				{
					// current distance value
					int currentDist = (int) distMap.getVoxel(x, y, z);

					// do not process within labels of original image
					if (currentDist == 0)
						continue;
					
					int minDist = currentDist;
					int closestLabel = 0;

					// iterate over neighbors
					for (ShortOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;

						// check bounds
						if (x2 < 0 || x2 >= sizeX)
							continue;
						if (y2 < 0 || y2 >= sizeY)
							continue;
						if (z2 < 0 || z2 >= sizeZ)
							continue;

						// distance from neighbor
						int dist = (int) (distMap.getVoxel(x2, y2, z2) + offset.weight);

						if (dist < minDist)
						{
							minDist = dist;
							closestLabel = (int) res.getVoxel(x2, y2, z2);
						}
					}

					// update current pixel if necessary
					if (minDist < currentDist && minDist < distMax)
					{
						distMap.setVoxel(x, y, z, minDist);
						res.setVoxel(x, y, z, closestLabel);
					}
				}
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
	}
}
