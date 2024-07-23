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
package inra.ijpb.label.distmap;

import ij.ImageStack;
import inra.ijpb.algo.Algo;

/**
 * Interface for computing distance maps for 3D label images. 
 */
public interface DistanceTransform3D extends Algo
{
	/**
     * Computes the distance map from a 3D label image.
     * 
     * Distance is computed for each label voxel (with value greater than 0), as
     * the chamfer distance to the nearest voxel with a different value. The
     * other value value can be 0 (the background) or another positive integer
     * value (corresponding to another region).
     * 
     * @param image
     *            a 3D label map, where integer values correspond to region
     *            labels, and 0 value to background.
     * @return a new 3D image containing:
     *         <ul>
     *         <li>0 for each background voxel</li>
     *         <li>the distance to the nearest voxel from another region</li>
     *         </ul>
     */
	public ImageStack distanceMap(ImageStack image);
}
