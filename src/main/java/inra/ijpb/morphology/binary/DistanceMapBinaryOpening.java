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
