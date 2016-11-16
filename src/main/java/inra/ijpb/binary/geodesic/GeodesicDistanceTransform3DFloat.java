/**
 * 
 */
package inra.ijpb.binary.geodesic;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;

/**
 * Computation of geodesic distance transform for 3D images, using floating point computation.
 * 
 * @author dlegland
 *
 */
public class GeodesicDistanceTransform3DFloat extends AlgoStub implements GeodesicDistanceTransform3D
{
	private final static int DEFAULT_MASK_LABEL = 255;

	// ==================================================
	// Class variables
	
	float[] weights;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to euclidean, but with non integer values. 
	 */
	boolean normalizeMap = true;
	
	/** 
	 * The value assigned to result pixels that do not belong to the mask. 
	 * Default is Float.MAX_VALUE.
	 */
	float backgroundValue = Float.POSITIVE_INFINITY;
	
	int maskLabel = DEFAULT_MASK_LABEL;

	ImageStack maskProc;
	Image3D result;
	
	int sizeX;
	int sizeY;
	int sizeZ;

	boolean modif;

	
	// ==================================================
	// Constructors
	
	public GeodesicDistanceTransform3DFloat(float[] weights)
	{
		this.weights = weights;
	}

	public GeodesicDistanceTransform3DFloat(float[] weights, boolean normalizeMap)
	{
		this.weights = weights;
		this.normalizeMap = normalizeMap;
	}

	public GeodesicDistanceTransform3DFloat(ChamferWeights3D weights, boolean normalizeMap)
	{
		this.weights = weights.getFloatWeights();
		this.normalizeMap = normalizeMap;
	}


	// ==================================================
	// Methods
	
	/* (non-Javadoc)
	 * @see inra.ijpb.binary.geodesic.GeodesicDistanceTransform3D#geodesicDistanceMap(ij.ImageStack, ij.ImageStack)
	 */
	@Override
	public ImageStack geodesicDistanceMap(ImageStack marker, ImageStack mask)
	{
		this.maskProc = mask;
		
		this.sizeX = mask.getWidth();
		this.sizeY = mask.getHeight();
		this.sizeZ = mask.getSize();
		
		fireStatusChanged(this, "Initialization..."); 
		
		// create new empty image, and fill it with black
		ImageStack resultStack = ImageStack.create(sizeX, sizeY, sizeZ, 32);
		this.result = Images3D.createWrapper(resultStack);
		
		// initialize empty image with either 0 (foreground) or Inf (background)
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					if (marker.getVoxel(x, y, z) == 0)
					{
						result.setValue(x, y, z, backgroundValue);
					}
				}
			}
		}
		
		// Iterate forward and backward passes until no more modification occur
		int iter = 0;
		do 
		{
			modif = false;

			// forward iteration
			fireStatusChanged(this, "Forward iteration " + iter);
			forwardIteration();

			// backward iteration
			fireStatusChanged(this, "Backward iteration " + iter); 
			backwardIteration();

			// Iterate while pixels have been modified
			iter++;
		} while (modif);

		// Normalize values by the first weight value
		if (this.normalizeMap) 
		{
			fireStatusChanged(this, "Normalize map"); 
			for (int z = 0; z < sizeZ; z++)
			{
				for (int y = 0; y < sizeY; y++)
				{
					for (int x = 0; x < sizeX; x++)
					{
						double val = result.getValue(x, y, z) / weights[0];
						result.setValue(x, y, z, val);
					}
				}
			}
		}

//		// Compute max value within the mask
//		fireStatusChanged(this, "Normalize display"); 
//		float maxVal = 0;
//		for (int z = 0; z < sizeZ; z++) 
//		{
//			for (int y = 0; y < sizeY; y++) 
//			{
//				for (int x = 0; x < sizeX; x++)
//				{
//					float val = (float) result.getValue(x, y, z);
//					if (Float.isFinite(val))
//					{
//						maxVal = Math.max(maxVal, val);
//					}
//				}
//			}
//		}
		
//		// update and return resulting Image processor
//		result.setFloatArray(array);
//		result.setMinAndMax(0, maxVal);
//		// Forces the display to non-inverted LUT
//		if (result.isInvertedLut())
//			result.invertLut();
//		return result;

		return resultStack;
	}

	private void forwardIteration()
	{
		int[][] shifts = new int[][]{
				{-1, -1, -1},
				{ 0, -1, -1},
				{+1, -1, -1},
				{-1,  0, -1},
				{ 0,  0, -1},
				{+1,  0, -1},
				{-1, +1, -1},
				{ 0, +1, -1},
				{+1, +1, -1},
				{-1, -1,  0},
				{ 0, -1,  0},
				{+1, -1,  0},
				{-1,  0,  0},
		};
		
		double[] shiftWeights = new double[]{
				weights[2], weights[1], weights[2], 
				weights[1], weights[0], weights[1], 
				weights[2], weights[1], weights[2], 
				weights[1], weights[0], weights[1], 
				weights[0]
		};
		
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					// process only voxels within the mask
					if (maskProc.getVoxel(x, y, z) != maskLabel)
					{
						continue;
					}
					
					double value = result.getValue(x, y, z);
					double ref = value;
					
					// find minimal value in forward neighborhood
					for (int i = 0; i < shifts.length; i++)
					{
						int[] shift = shifts[i];
						int x2 = x + shift[0];
						int y2 = y + shift[1];
						int z2 = z + shift[2];
						
						if (x2 < 0 || x2 >= sizeX)
							continue;
						if (y2 < 0 || y2 >= sizeY)
							continue;
						if (z2 < 0 || z2 >= sizeZ)
							continue;
						
						double newVal = result.getValue(x2, y2, z2) + shiftWeights[i];
						value = Math.min(value, newVal);
					}
					
					if (value < ref)
					{
						modif = true;
						result.setValue(x, y, z, value);
					}
				}
			}
		}
		
		fireProgressChanged(this, 1, 1);
	}

	private void backwardIteration()
	{
		int[][] shifts = new int[][]{
				{+1, +1, +1},
				{ 0, +1, +1},
				{-1, +1, +1},
				{+1,  0, +1},
				{ 0,  0, +1},
				{-1,  0, +1},
				{+1, -1, +1},
				{ 0, -1, +1},
				{-1, -1, +1},
				{+1, +1,  0},
				{ 0, +1,  0},
				{-1, +1,  0},
				{+1,  0,  0},
		};
		
		double[] shiftWeights = new double[]{
				weights[2], weights[1], weights[2], 
				weights[1], weights[0], weights[1], 
				weights[2], weights[1], weights[2], 
				weights[1], weights[0], weights[1], 
				weights[0]
		};
		
		for (int z = sizeZ-1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ);
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					// process only voxels within the mask
					if (maskProc.getVoxel(x, y, z) != maskLabel)
					{
						continue;
					}
					
					double value = result.getValue(x, y, z);
					double ref = value;
					
					// find minimal value in backward neighborhood
					for (int i = 0; i < shifts.length; i++)
					{
						int[] shift = shifts[i];
						int x2 = x + shift[0];
						int y2 = y + shift[1];
						int z2 = z + shift[2];
						
						if (x2 < 0 || x2 >= sizeX)
							continue;
						if (y2 < 0 || y2 >= sizeY)
							continue;
						if (z2 < 0 || z2 >= sizeZ)
							continue;
						
						double newVal = result.getValue(x2, y2, z2) + shiftWeights[i];
						value = Math.min(value, newVal);
					}
					
					if (value < ref)
					{
						modif = true;
						result.setValue(x, y, z, value);
					}
				}
			}
		}	
		
		fireProgressChanged(this, 1, 1);
	}
}
