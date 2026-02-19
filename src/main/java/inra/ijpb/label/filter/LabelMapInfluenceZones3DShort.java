/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
package inra.ijpb.label.filter;

import java.util.Collection;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.distmap.ChamferMask3D.ShortOffset;

/**
 * Computes influence zone of each label within a label map, by computing a
 * distance map and choosing the label of the closest region.
 * 
 * @see inra.ijpb.binary.distmap.ChamferMask3D
 * @see inra.ijpb.label.filter.ChamferLabelDilation2DShort
 * 
 * @author dlegland
 * 
 */
public class LabelMapInfluenceZones3DShort extends AlgoStub 
{
	// ==================================================
	// Class variables
	
    /**
     * The chamfer mask used to propagate distances.
     */
	ChamferMask3D mask;
	
	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new Influence Zone operator that uses Svensson's (3,4,5,7) 3D
	 * chamfer distance mask for computing distance to nearest region.
	 */
	public LabelMapInfluenceZones3DShort()
	{
		this(ChamferMask3D.SVENSSON_3_4_5_7);
	}
	
	/**
	 * Creates a new Influence Zone operator with the specified chamfer mask for
	 * computing distance to nearest region.
	 * 
	 * @param mask
	 *            the Chamfer mask to use.
	 */
	public LabelMapInfluenceZones3DShort(ChamferMask3D mask)
	{
		this.mask = mask;
	}


	// ==================================================
	// Methods 
	
	/**
	 * Computes dilation of labels within label image by a specified radius.
	 * Labels can not dilate over existing labels.
	 * 
	 * The function returns a new ImageStack the same size and the same type as
	 * the input, with values greater than or equal to zero.
	 *
	 * @param labelImage
	 *            the original label map
	 * @return a new label image where each label is dilated over background
	 *         pixels.
	 */
	public ImageStack process(ImageStack labelImage)
	{
		fireStatusChanged(this, "Initialization..."); 
		// the instance of ImageStack storing the result label map
		ImageStack res = labelImage.duplicate();
		// the distance map to the closest label
		ImageStack distMap = initialize(labelImage);

		// forward iteration
		fireStatusChanged(this, "Forward iteration");
		forwardIteration(res, distMap);

		// backward iteration
		fireStatusChanged(this, "Backward iteration"); 
		backwardIteration(res, distMap);
		
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
	
	private void forwardIteration(ImageStack res, ImageStack distMap) 
	{
		// size of image
		int sizeX = res.getWidth();
		int sizeY = res.getHeight();
		int sizeZ = res.getSize();

		// compute offsets of the neighborhood in forward direction
		Collection<ShortOffset> offsets = mask.getForwardOffsets();
		
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
					if (minDist < currentDist)
					{
						distMap.setVoxel(x, y, z, minDist);
						res.setVoxel(x, y, z, closestLabel);
					}
				}
			}
		}		
		this.fireProgressChanged(this, sizeY, sizeY);
	}

	private void backwardIteration(ImageStack res, ImageStack distMap)
	{
		// size of image
		int sizeX = res.getWidth();
		int sizeY = res.getHeight();
		int sizeZ = res.getSize();

		// compute offsets of the neighborhood in backward direction
		Collection<ShortOffset> offsets = mask.getBackwardOffsets();
		
		// Iterate over voxels
		for (int z = sizeZ-1; z >= 0; z--) 
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
					if (minDist < currentDist)
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
