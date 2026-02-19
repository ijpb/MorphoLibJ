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

import ij.ImageStack;
import inra.ijpb.binary.distmap.ChamferDistanceTransform3D;

/**
 * Morphological erosion for 3D binary images.
 *
 * @see DistanceMapBinaryDilation3D
 * @see DistanceMapBinaryClosing3D
 * @see DistanceMapBinaryOpening3D
 * @see DistanceMapBinaryDilation
 * 
 * @author dlegland
 */
public class DistanceMapBinaryErosion3D extends DistanceMapBasedOperator3D
{
	double radius;
	
	/**
	 * Creates a new erosion operator for 3D binary images, using a ball with
	 * the specified radius as structuring element.
	 * 
	 * @param radius
	 *            the radius of the ball structuring element
	 */
	public DistanceMapBinaryErosion3D(double radius)
	{
		this.radius = radius;
	}

	@Override
	public ImageStack processBinary(ImageStack image)
	{
		// compute distance map
		fireStatusChanged(this, "Compute Distance Map");
		ImageStack distMap = this.distanceTransform.distanceMap(image);

		// compute the threshold value
		double threshold = (radius + 0.5);
		if (this.distanceTransform instanceof ChamferDistanceTransform3D)
		{
			threshold *= ((ChamferDistanceTransform3D) distanceTransform).mask().getNormalizationWeight();
		}

		// threshold the distance map
		fireStatusChanged(this, "Threshold Distance Map");
		Relational ge = Relational.GE;
		ge.addAlgoListener(this);
		// compute comparison using previously allocated array
		return ge.process(distMap, threshold);
	}
}
