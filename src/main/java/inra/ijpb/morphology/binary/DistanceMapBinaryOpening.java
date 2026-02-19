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
/**
 * 
 */
package inra.ijpb.morphology.binary;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferDistanceTransform2D;

/**
 * Morphological opening (erosion followed by dilation) for 2D binary images.
 *
 * @see DistanceMapBinaryErosion
 * @see DistanceMapBinaryDilation
 * @see DistanceMapBinaryClosing
 * @see DistanceMapBinaryOpening3D
 * 
 * @author dlegland
 */
public class DistanceMapBinaryOpening extends DistanceMapBasedOperator
{
	double radius;
	
	/**
	 * Creates a new opening operator for binary images, using a disk with the
	 * specified radius as structuring element.
	 * 
	 * @param radius
	 *            the radius of the disk structuring element
	 */
	public DistanceMapBinaryOpening(double radius)
	{
		this.radius = radius;
	}

	@Override
	public ByteProcessor processBinary(ByteProcessor image)
	{
		// compute threshold value for distance maps
		double threshold = (radius + 0.5);
		if (this.distanceTransform instanceof ChamferDistanceTransform2D)
		{
			threshold *= ((ChamferDistanceTransform2D) distanceTransform).mask().getNormalizationWeight();
		}

		// compute distance map
		fireStatusChanged(this, "Compute Distance Map");
		ImageProcessor distMap = this.distanceTransform.distanceMap(image);
		
		// apply threshold on distance map, and invert (using LT instead of GE)
		fireStatusChanged(this, "Threshold Distance Map");
		ByteProcessor res = Relational.LT.process(distMap, threshold);
		
		// compute distance map on eroded image
		fireStatusChanged(this, "Compute Distance Map on erosion");
		distMap = this.distanceTransform.distanceMap(res);
		
		// compute distance map
		fireStatusChanged(this, "Threshold distance map");
		return Relational.LT.process(distMap, threshold);
	}
}
