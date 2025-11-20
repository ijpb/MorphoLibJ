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
 * @see net.ijt.binary.ops.DistanceMapBinaryDilation;
 * @see net.ijt.binary.ops.DistanceMapBinaryErosion;
 * @see net.ijt.binary.ops.DistanceMapBinaryDilation3D;
 * @see net.ijt.binary.ops.DistanceMapBinaryErosion3D;
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
