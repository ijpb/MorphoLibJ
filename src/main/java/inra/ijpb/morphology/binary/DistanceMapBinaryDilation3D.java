/**
 * 
 */
package inra.ijpb.morphology.binary;

import ij.ImageStack;
import inra.ijpb.binary.BinaryInverter;
import inra.ijpb.binary.distmap.ChamferDistanceTransform3D;

/**
 * Morphological dilation for 3D binary images.
 *
 * @see DistanceMapBinaryErosion3D
 * @see DistanceMapBinaryClosing3D
 * @see DistanceMapBinaryOpening3D
 * @see DistanceMapBinaryDilation
 * 
 * @author dlegland
 */
public class DistanceMapBinaryDilation3D extends DistanceMapBasedOperator3D
{
	double radius;

	/**
	 * Creates a new dilation operator for 3D binary images, using a ball with
	 * the specified radius as structuring element.
	 * 
	 * @param radius
	 *            the radius of the ball structuring element
	 */
	public DistanceMapBinaryDilation3D(double radius)
	{
		this.radius = radius;
	}

	@Override
	public ImageStack processBinary(ImageStack image)
	{
		// compute the threshold value
		double threshold = (radius + 0.5);
		if (this.distanceTransform instanceof ChamferDistanceTransform3D)
		{
			threshold *= ((ChamferDistanceTransform3D) distanceTransform).mask().getNormalizationWeight();
		}

		// need to invert
		fireStatusChanged(this, "Invert image");
		BinaryInverter inverter = new BinaryInverter();
		inverter.addAlgoListener(this);
		ImageStack imageInv = image.duplicate();
		inverter.processInPlace(imageInv);
		
		// compute distance map
		fireStatusChanged(this, "Compute Distance Map");
		ImageStack distMap = distanceTransform.distanceMap(imageInv);

		// threshold the distance map		
		fireStatusChanged(this, "Threshold Distance Map");
		Relational lt = Relational.LT;
		lt.addAlgoListener(this);
		// compute comparison using previously allocated array
		lt.process(distMap, threshold, imageInv);
		
		return imageInv;
	}
}
