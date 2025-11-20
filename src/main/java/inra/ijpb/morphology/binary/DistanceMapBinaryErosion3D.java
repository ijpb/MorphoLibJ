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
