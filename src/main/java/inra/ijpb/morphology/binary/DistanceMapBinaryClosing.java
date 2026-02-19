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
 * Morphological closing (dilation followed by erosion) for 2D binary images.
 *
 * @see DistanceMapBinaryErosion
 * @see DistanceMapBinaryDilation
 * @see DistanceMapBinaryOpening
 * @see DistanceMapBinaryClosing3D
 * 
 * @author dlegland
 */
public class DistanceMapBinaryClosing extends DistanceMapBasedOperator
{
	double radius;

	/**
	 * Creates a new closing operator for binary images, using a disk with the
	 * specified radius as structuring element.
	 * 
	 * @param radius
	 *            the radius of the disk structuring element
	 */
	public DistanceMapBinaryClosing(double radius)
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

		// need to invert
		fireStatusChanged(this, "Invert image");
		ImageProcessor imageInv = image.duplicate();
		imageInv.invert();
		
		// compute distance map
		fireStatusChanged(this, "Compute Distance Map");
		ImageProcessor distMap = this.distanceTransform.distanceMap(imageInv);
		
		fireStatusChanged(this, "Threshold Distance Map");
		ByteProcessor dilated = Relational.LT.process(distMap, threshold);
		
		// compute distance map on dilated image
		fireStatusChanged(this, "Compute Distance Map on dilated image");
		distMap = this.distanceTransform.distanceMap(dilated);
		
		// Apply threshold on distance map
		fireStatusChanged(this, "Threshold Distance Map");
		return Relational.GE.process(distMap, threshold);
	}
}
