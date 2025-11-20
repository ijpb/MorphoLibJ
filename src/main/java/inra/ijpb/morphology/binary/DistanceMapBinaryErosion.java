/**
 * 
 */
package inra.ijpb.morphology.binary;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferDistanceTransform2D;

/**
 * Morphological erosion for 2D binary images.
 *
 * @see DistanceMapBinaryDilation
 * @see DistanceMapBinaryClosing
 * @see DistanceMapBinaryOpening
 * @see DistanceMapBinaryErosion3D
 * 
 * @author dlegland
 */
public class DistanceMapBinaryErosion extends DistanceMapBasedOperator
{
	double radius;
	
	/**
	 * Creates a new erosion operator for binary images, using a disk with the
	 * specified radius as structuring element.
	 * 
	 * @param radius
	 *            the radius of the disk structuring element
	 */
	public DistanceMapBinaryErosion(double radius)
	{
		this.radius = radius;
	}

	@Override
	public ByteProcessor processBinary(ByteProcessor image) 
	{
		// compute distance map
		fireStatusChanged(this, "Compute Distance Map");
		ImageProcessor distMap = this.distanceTransform.distanceMap(image);
		
		// Apply threshold on distance map
		fireStatusChanged(this, "Threshold Distance Map");
		double threshold = (radius + 0.5);
		if (this.distanceTransform instanceof ChamferDistanceTransform2D)
		{
			threshold *= ((ChamferDistanceTransform2D) distanceTransform).mask().getNormalizationWeight();
		}
		return Relational.GE.process(distMap, threshold);
	}
}
