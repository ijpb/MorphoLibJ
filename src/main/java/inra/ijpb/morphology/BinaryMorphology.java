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
package inra.ijpb.morphology;

import ij.ImageStack;
import ij.process.ByteProcessor;
import inra.ijpb.morphology.binary.DistanceMapBinaryDilation;
import inra.ijpb.morphology.binary.DistanceMapBinaryDilation3D;
import inra.ijpb.morphology.binary.DistanceMapBinaryErosion;
import inra.ijpb.morphology.binary.DistanceMapBinaryErosion3D;

/**
 * A collection of static method for performing morphological filters (erosion,
 * dilation) on binary 2D or 3D images.
 * 
 * @see inra.ijpb.morphology.binary.DistanceMapBinaryDilation
 * @see inra.ijpb.morphology.binary.DistanceMapBinaryErosion
 * @see inra.ijpb.morphology.binary.DistanceMapBinaryDilation3D
 * @see inra.ijpb.morphology.binary.DistanceMapBinaryErosion3D
 * 
 * @author dlegland
 *
 */
public class BinaryMorphology
{
	/**
	 * Computes the dilation of the binary image using as structuring element a
	 * disk with the specified radius.
	 * 
	 * Implementation relies on computation of distance transform. The shape of
	 * the structuring element will depends on the algorithm used for distance
	 * transform computation, usually chamfer-based.
	 * 
	 * @param image
	 *            the binary image to dilate
	 * @param radius
	 *            the radius of the disk structuring element
	 * @return the result of dilation.
	 */
	public static final ByteProcessor dilationDisk(ByteProcessor image, double radius)
	{
	    DistanceMapBinaryDilation algo = new DistanceMapBinaryDilation(radius);
	    return algo.processBinary(image);
	}
	
	/**
	 * Computes the erosion of the binary image using as structuring element a
	 * disk with the specified radius.
	 * 
	 * Implementation relies on computation of distance transform. The shape of
	 * the structuring element will depends on the algorithm used for distance
	 * transform computation, usually chamfer-based.
	 * 
	 * @param image
	 *            the binary image to erode
	 * @param radius
	 *            the radius of the disk structuring element
	 * @return the result of erosion.
	 */
	public static final ByteProcessor erosionDisk(ByteProcessor image, double radius)
	{
        DistanceMapBinaryErosion algo = new DistanceMapBinaryErosion(radius);
        return algo.processBinary(image);
	}
	

	/**
	 * Computes the dilation of the binary stack using as structuring element a
	 * ball with the specified radius.
	 * 
	 * Implementation relies on computation of distance transforms. The shape of
	 * the structuring element will depends on the algorithm used for distance
	 * transform computation, usually chamfer-based.
	 * 
	 * @param image
	 *            the binary 3D image to dilate
	 * @param radius
	 *            the radius of the ball structuring element
	 * @return the result of dilation.
	 */
	public static final ImageStack dilationBall(ImageStack image, double radius)
	{
	    DistanceMapBinaryDilation3D algo = new DistanceMapBinaryDilation3D(radius);
        return algo.processBinary(image);
	}
	
	/**
	 * Computes the erosion of the binary stack using as structuring element a
	 * ball with the specified radius.
	 * 
	 * Implementation relies on computation of distance transforms. The shape of
	 * the structuring element will depends on the algorithm used for distance
	 * transform computation, usually chamfer-based.
	 * 
	 * @param image
	 *            the binary 3D image to erode
	 * @param radius
	 *            the radius of the ball structuring element
	 * @return the result of erosion.
	 */
	public static final ImageStack erosionBall(ImageStack image, double radius)
	{
	    DistanceMapBinaryErosion3D algo = new DistanceMapBinaryErosion3D(radius);
	    return algo.processBinary(image);
	}
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private BinaryMorphology()
	{
	}
}
