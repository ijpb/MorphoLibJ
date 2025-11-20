/**
 * 
 */
package inra.ijpb.morphology.binary;

import ij.ImageStack;
import inra.ijpb.binary.BinaryInverter;
import inra.ijpb.binary.distmap.ChamferDistanceTransform3D;

/**
 * Morphological closing (dilation followed by erosion) for 3D binary images.
 *
 * @see DistanceMapBinaryDilation3D
 * @see DistanceMapBinaryErosion3D
 * @see DistanceMapBinaryOpening3D
 * @see DistanceMapBinaryClosing
 * 
 * @author dlegland
 */
public class DistanceMapBinaryClosing3D extends DistanceMapBasedOperator3D
{
	double radius;
	
	public DistanceMapBinaryClosing3D(double radius)
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
		ImageStack distMap = this.distanceTransform.distanceMap(imageInv);
		
		// threshold the distance map		
		fireStatusChanged(this, "Threshold Distance Map");
		Relational lt = Relational.LT;
		lt.addAlgoListener(this);
		// compute comparison using previously allocated array
		lt.process(distMap, threshold, imageInv);
		
		// compute distance map on dilated image
		fireStatusChanged(this, "Compute Distance Map on dilated image");
		distMap = this.distanceTransform.distanceMap(imageInv);
		
		// threshold the distance map again
		Relational gt = Relational.GT;
		gt.addAlgoListener(this);
		gt.process(distMap, threshold, imageInv);
		
		return imageInv;
	}
}
