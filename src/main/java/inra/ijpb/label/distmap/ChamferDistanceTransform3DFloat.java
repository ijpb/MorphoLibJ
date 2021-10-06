/**
 * 
 */
package inra.ijpb.label.distmap;

import java.util.Collection;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.distmap.ChamferMask3D.FloatOffset;
import inra.ijpb.binary.distmap.ChamferMask3D.ShortOffset;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;

/**
 * Computes 3D distance transform using the chamfer weights provided by a
 * ChamferWeights3D object, and using floating-point computation.
 * 
 * This version works also for label images. The implementation is a little bit
 * different compared to the version for binary images, in particular, it makes
 * use of the "Image3D" interface.
 * 
 * @author David Legland
 * 
 */
public class ChamferDistanceTransform3DFloat extends AlgoStub implements DistanceTransform3D
{
	// ==================================================
	// Class variables

	/**
	 * The chamfer weights used to propagate distances to neighbor voxels.
	 */
	ChamferMask3D chamferMask;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to Euclidean, but with 
	 * non integer values. 
	 */
	boolean normalize = true;
	
	
	// ==================================================
	// Constructors 
	
	public ChamferDistanceTransform3DFloat(ChamferMask3D chamferMask)
	{
		this.chamferMask = chamferMask;
	}
	
	public ChamferDistanceTransform3DFloat(ChamferMask3D chamferMask, boolean normalize)
	{
		this.chamferMask = chamferMask;
		this.normalize = normalize;
	}
	

	// ==================================================
	// Implementation of DistanceTransform3D interface 
	
	/**
	 * Computes the distance map from a 3D label image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @param image a 3D binary image with white pixels (255) as foreground
	 * @return a new 3D image containing: <ul>
	 * <li> 0 for each background pixel </li>
	 * <li> the distance to the nearest background pixel otherwise</li>
	 * </ul>
	 */
	@Override
	public ImageStack distanceMap(ImageStack image)
	{
		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// store wrapper to mask image
		Image3D labels = Images3D.createWrapper(image);

		// create new empty image, and fill it with black
		ImageStack resultStack = ImageStack.create(sizeX, sizeY, sizeZ, 32);
		Image3D distMap = Images3D.createWrapper(resultStack);
		
		initializeResultSlices(labels, distMap);
		
		// Two iterations are enough to compute distance map to boundary
		forwardScan(labels, distMap);
		backwardScan(labels, distMap);

		// Normalize values by the first weight
		if (this.normalize) 
		{
			normalizeResultSlices(labels, distMap); 
		}
				
		fireStatusChanged(this, "");
		return resultStack;
	}

	
	// ==================================================
	// Inner computation methods 
	
	/**
	 * Fill result image with zero for background voxels, and Short.MAX for
	 * foreground voxels.
	 */
	private void initializeResultSlices(Image3D labels, Image3D distMap)
	{
		fireStatusChanged(this, "Initialization...");
		
		// retrieve image dimensions
		int sizeX = labels.getSize(0);
		int sizeY = labels.getSize(1);
		int sizeZ = labels.getSize(2);

		// iterate over slices
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int label = (int) labels.getValue(x, y, z);
					distMap.setValue(x, y, z, label == 0 ? 0 : Float.MAX_VALUE);
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}

	private void forwardScan(Image3D labels, Image3D distMap) 
	{
		fireStatusChanged(this, "Forward scan..."); 
		
		// retrieve image dimensions
		int sizeX = labels.getSize(0);
		int sizeY = labels.getSize(1);
		int sizeZ = labels.getSize(2);
		
		// create array of forward shifts
		Collection<FloatOffset> offsets = this.chamferMask.getForwardFloatOffsets();

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ); 
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					// get current label
					int label = (int) labels.getValue(x, y, z);
					
					// do not process background pixels
					if (label == 0)
						continue;
					
					// current distance value
					double currentDist = distMap.getValue(x, y, z);
					double newDist = currentDist;

					// iterate over forward offsets defined by ChamferWeights
					for (FloatOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;
						
						// check bounds
						if (x2 < 0 || x2 >= sizeX)
							continue;
						if (y2 < 0 || y2 >= sizeY)
							continue;
						if (z2 < 0 || z2 >= sizeZ)
							continue;
						
						if (((int) labels.getValue(x2, y2, z2)) != label)
						{
							// Update with distance to nearest different label
							newDist = offset.weight;
						}
						else
						{
							// Increment distance
							newDist = Math.min(newDist, distMap.getValue(x2, y2, z2) + offset.weight);
						}
					}
					
					if (newDist < currentDist) 
					{
						distMap.setValue(x, y, z, newDist);
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void backwardScan(Image3D labels, Image3D distMap) 
	{
		fireStatusChanged(this, "Backward scan..."); 
		
		// retrieve image dimensions
		int sizeX = labels.getSize(0);
		int sizeY = labels.getSize(1);
		int sizeZ = labels.getSize(2);
		
		// create array of backward shifts
		Collection<FloatOffset> offsets = this.chamferMask.getBackwardFloatOffsets();
		
		// iterate on image voxels in backward order
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ);
			
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					// get current label
					int label = (int) labels.getValue(x, y, z);
					
					// do not process background pixels
					if (label == 0)
						continue;
					
					// current distance value
					double currentDist = distMap.getValue(x, y, z);
					double newDist = currentDist;
					
					// iterate over backward offsets defined by ChamferWeights
					for (FloatOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;
						
						// check bounds
						if (x2 < 0 || x2 >= sizeX)
							continue;
						if (y2 < 0 || y2 >= sizeY)
							continue;
						if (z2 < 0 || z2 >= sizeZ)
							continue;
						
						if (((int) labels.getValue(x2, y2, z2)) != label)
						{
							// Update with distance to nearest different label
							newDist = offset.weight;
						}
						else
						{
							// Increment distance
							newDist = Math.min(newDist, distMap.getValue(x2, y2, z2) + offset.weight);
						}
					}
					
					if (newDist < currentDist) 
					{
						distMap.setValue(x, y, z, newDist);
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void normalizeResultSlices(Image3D labels, Image3D distMap)
	{
		fireStatusChanged(this, "Normalize map..."); 
		
		// retrieve the minimum weight
		double w0 = Double.POSITIVE_INFINITY;
		for (ShortOffset offset : this.chamferMask.getOffsets())
		{
			w0 = Math.min(w0, offset.weight);
		}
		
		// retrieve image dimensions
		int sizeX = labels.getSize(0);
		int sizeY = labels.getSize(1);
		int sizeZ = labels.getSize(2);

		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if (((int) labels.get(x, y, z)) != 0)
					{
						distMap.setValue(x, y, z, distMap.getValue(x, y, z) / w0);
					}
				}
			}
		}
		
		fireProgressChanged(this, 1, 1); 
	}

}
