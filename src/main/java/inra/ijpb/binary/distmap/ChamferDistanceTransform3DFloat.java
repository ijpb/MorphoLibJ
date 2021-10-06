/**
 * 
 */
package inra.ijpb.binary.distmap;

import static java.lang.Math.min;

import java.util.Collection;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask3D.FloatOffset;
import inra.ijpb.data.image.Images3D;

/**
 * Computes 3D distance transform using the chamfer weights provided by a
 * ChamferMask3D object, and using 32-bits floating-point computation.
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
	ChamferMask3D mask;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to Euclidean, but with 
	 * non integer values. 
	 */
	boolean normalize = true;
	
	
	// ==================================================
	// Constructors 
	
	public ChamferDistanceTransform3DFloat(ChamferMask3D mask)
	{
		this.mask = mask;
	}
	
	public ChamferDistanceTransform3DFloat(ChamferMask3D mask, boolean normalize)
	{
		this.mask = mask;
		this.normalize = normalize;
	}
	

	// ==================================================
	// Implementation of DistanceTransform3D interface 
	
	/**
	 * Computes the distance map from a 3D binary image. 
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
		int[] dims = new int[] {sizeX, sizeY, sizeZ};

		// store wrapper to mask image
		byte[][] maskSlices = Images3D.getByteArrays(image);

		// create new empty image, and fill it with black
		ImageStack buffer = ImageStack.create(sizeX, sizeY, sizeZ, 32);
		float[][] resultSlices = Images3D.getFloatArrays(buffer);
		
		initializeResultSlices(maskSlices, resultSlices);
		
		// Two iterations are enough to compute distance map to boundary
		forwardScan(dims, maskSlices, resultSlices);
		backwardScan(dims, maskSlices, resultSlices);

		// Normalize values by the first weight
		if (this.normalize) 
		{
			normalizeResultSlices(maskSlices, resultSlices); 
		}
				
		fireStatusChanged(this, "");
		return buffer;

	}
	
	// ==================================================
	// Inner computation methods 
	
	/**
	 * Fill result image with zero for background voxels, and Short.MAX for
	 * foreground voxels.
	 */
	private void initializeResultSlices(byte[][] maskSlices, float[][] resultSlices)
	{
		fireStatusChanged(this, "Initialization...");

		// iterate over slices
		int sizeZ = maskSlices.length;
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			byte[] maskSlice = maskSlices[z];
			float[] resultSlice = resultSlices[z];
			
			for (int i = 0; i < maskSlice.length; i++)
			{
				int val = maskSlice[i];
				resultSlice[i] = val == 0 ? 0 : Float.MAX_VALUE;
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void forwardScan(int[] dims, byte[][] maskSlices, float[][] resultSlices) 
	{
		fireStatusChanged(this, "Forward scan..."); 
		
		// retrieve image dimensions
		int sizeX = dims[0];
		int sizeY = dims[1];
		int sizeZ = dims[2];
		
		// create array of forward shifts
		Collection<FloatOffset> offsets = this.mask.getForwardFloatOffsets();

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ); 
			
			byte[] maskSlice = maskSlices[z];
			float[] currentSlice = resultSlices[z];
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int index = sizeX * y + x;

					// check if we need to update current voxel
					if ((maskSlice[index] & 0x00FF) == 0)
						continue;
					
					double value = currentSlice[index];
					
					// iterate over forward offsets defined by ChamferWeights
					double newVal = Float.MAX_VALUE;
					for (FloatOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;
						
						// check that current neighbor is within image
						if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY && z2 >= 0 && z2 < sizeZ)
						{
							newVal = min(newVal, resultSlices[z2][sizeX * y2 + x2] + offset.weight);
						}
						
						if (newVal < value) 
						{
							currentSlice[index] = (float) newVal;
						}
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void backwardScan(int[] dims, byte[][] maskSlices, float[][] resultSlices) 
	{
		fireStatusChanged(this, "Backward scan..."); 
		
		// retrieve image dimensions
		int sizeX = dims[0];
		int sizeY = dims[1];
		int sizeZ = dims[2];
		
		// create array of backward shifts
		Collection<FloatOffset> offsets = this.mask.getBackwardFloatOffsets();
		
		// iterate on image voxels in backward order
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ);
			
			byte[] maskSlice = maskSlices[z];
			float[] currentSlice = resultSlices[z];
			
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int index = sizeX * y + x;

					// check if we need to update current voxel
					if ((maskSlice[index] & 0x00FF) == 0)
						continue;
					
					double value = currentSlice[index];
					
					// iterate over backward offsets defined by ChamferWeights
					double newVal = Float.MAX_VALUE;
					for (FloatOffset offset : offsets)
					{
						int x2 = x + offset.dx;
						int y2 = y + offset.dy;
						int z2 = z + offset.dz;
						
						// check that current neighbor is within image
						if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY && z2 >= 0 && z2 < sizeZ)
						{
							newVal = min(newVal, resultSlices[z2][sizeX * y2 + x2] + offset.weight);
						}
					}

					// Update current value if necessary
					if (newVal < value) 
					{
						currentSlice[index] = (float) newVal;
					}
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	private void normalizeResultSlices(byte[][] maskSlices, float[][] resultSlices)
	{
		fireStatusChanged(this, "Normalize map..."); 
		
		// retrieve the minimum weight
		float w0 = Float.POSITIVE_INFINITY;
		for (FloatOffset offset : this.mask.getFloatOffsets())
		{
			w0 = Math.min(w0, offset.weight);
		}
		
		int sizeZ = maskSlices.length;
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			
			byte[] maskSlice = maskSlices[z];
			float[] resultSlice = resultSlices[z];
			
			for (int i = 0; i < maskSlice.length; i++)
			{
				if (maskSlice[i] != 0)
				{
					resultSlice[i] = resultSlice[i] / w0;
				}
			}
		}
		
		fireProgressChanged(this, 1, 1); 
	}
}
