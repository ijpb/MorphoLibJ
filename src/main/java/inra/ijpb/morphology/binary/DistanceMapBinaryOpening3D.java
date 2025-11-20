/**
 * 
 */
package inra.ijpb.morphology.binary;

import ij.ImageStack;
import inra.ijpb.binary.distmap.ChamferDistanceTransform3D;

/**
 * Morphological opening (erosion followed by dilation) for 3D binary images.
 *
 * @see DistanceMapBinaryDilation3D
 * @see DistanceMapBinaryErosion3D
 * @see DistanceMapBinaryClosing3D
 * @see DistanceMapBinaryOpening
 * 
 * @author dlegland
 */
public class DistanceMapBinaryOpening3D extends DistanceMapBasedOperator3D
{
	double radius;
	
	/**
	 * Creates a new opening operator for 3D binary images, using a ball with
	 * the specified radius as structuring element.
	 * 
	 * @param radius
	 *            the radius of the ball structuring element
	 */
	public DistanceMapBinaryOpening3D(double radius)
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
		
		// compute distance map
		fireStatusChanged(this, "Compute Distance Map");
		ImageStack distMap = distanceTransform.distanceMap(image);
		
		// allocate memory for result array
		ImageStack imageInv = image.duplicate();
		
		// apply threshold on distance map, and invert (using LT instead of GE)
		fireStatusChanged(this, "Threshold Distance Map");
		Relational lt = Relational.LT;
		lt.addAlgoListener(this);
		// compute comparison using previously allocated array
		lt.process(distMap, threshold, imageInv);
		
		// compute distance map on eroded image
		fireStatusChanged(this, "Compute Distance Map on eroded image");
		distMap = distanceTransform.distanceMap(imageInv);
		
		// compute threshold on distance map
		fireStatusChanged(this, "Threshold Distance Map");
		// compute comparison using previously allocated array
		lt.process(distMap, threshold, imageInv);

		return imageInv;
	}
}
