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
 * Morphological dilation for 2D binary images.
 *
 * @see DistanceMapBinaryErosion
 * @see DistanceMapBinaryClosing
 * @see DistanceMapBinaryOpening
 * @see DistanceMapBinaryDilation3D
 * 
 * @author dlegland
 */
public class DistanceMapBinaryDilation extends DistanceMapBasedOperator
{
	double radius;
	
	/**
	 * Creates a new dilation operator for binary images, using a disk with the
	 * specified radius as structuring element.
	 * 
	 * @param radius
	 *            the radius of the disk structuring element
	 */
	public DistanceMapBinaryDilation(double radius)
	{
		this.radius = radius;
	}

	@Override
	public ByteProcessor processBinary(ByteProcessor image) 
	{
		// need to invert
		fireStatusChanged(this, "Invert image");
		ImageProcessor imageInv = image.duplicate();
		imageInv.invert();
		
		// compute distance map
		fireStatusChanged(this, "Compute Distance Map");
		ImageProcessor distMap = this.distanceTransform.distanceMap(imageInv);
		
		// Apply threshold on distance map
		fireStatusChanged(this, "Threshold Distance Map");
		double threshold = (radius + 0.5);
		if (this.distanceTransform instanceof ChamferDistanceTransform2D)
		{
			threshold *= ((ChamferDistanceTransform2D) distanceTransform).mask().getNormalizationWeight();
		}
		return Relational.LT.process(distMap, threshold);
	}
}
