package inra.ijpb.binary.distmap;

import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;

/**
 * Computes Chamfer distances in a 3x3x3 neighborhood using floating point 
 * calculation.
 * 
 * @author David Legland
 * 
 */
public class DistanceTransform3DFloat extends AlgoStub implements DistanceTransform3D 
{
	private final static int DEFAULT_MASK_LABEL = 255;

	private float[] weights;

	int maskLabel = DEFAULT_MASK_LABEL;

	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to Euclidean, but with 
	 * non integer values. 
	 */
	private boolean normalizeMap = true;

	
	private int sizeX;
	private int sizeY;
	private int sizeZ;

	private byte[][] maskSlices;

	/**
	 * The result image that will store the distance map. The content
	 * of the buffer is updated during forward and backward iterations.
	 */
	private float[][] resultSlices;
	
	
	/**
	 * Default constructor that specifies the chamfer weights.
	 * @param weights an array of two weights for orthogonal and diagonal directions
	 */
	public DistanceTransform3DFloat(float[] weights)
	{
		this.weights = weights;
	}

	/**
	 * Constructor specifying the chamfer weights and the optional normalization.
	 * @param weights
	 *            an array of two weights for orthogonal and diagonal directions
	 * @param normalize
	 *            flag indicating whether the final distance map should be
	 *            normalized by the first weight
	 */
	public DistanceTransform3DFloat(float[] weights, boolean normalize)
	{
		this.weights = weights;
		this.normalizeMap = normalize;
	}

	/**
	 * Computes the distance map from a 3D binary image. Distance is computed
	 * for each foreground (white) pixel, as the chamfer distance to the nearest
	 * background (black) pixel.
	 * 
	 * @param image
	 *            a 3D binary image with white pixels (255) as foreground
	 * @return a new 3D image containing:
	 *         <ul>
	 *         <li>0 for each background pixel</li>
	 *         <li>the distance to the nearest background pixel otherwise</li>
	 *         </ul>
	 */
	public ImageStack distanceMap(ImageStack image) 
	{
		// size of image
		sizeX = image.getWidth();
		sizeY = image.getHeight();
		sizeZ = image.getSize();
		
		// store wrapper to mask image
		this.maskSlices = getByteArrays(image);

		// create new empty image, and fill it with black
		ImageStack buffer = ImageStack.create(sizeX, sizeY, sizeZ, 32);
		this.resultSlices = getFloatArrays(buffer);

		// initialize empty image with either 0 (background) or max value (foreground)
		fireStatusChanged(this, "Initialization..."); 
		for (int z = 0; z < sizeZ; z++) 
		{
			byte[] maskSlice = this.maskSlices[z];
			float[] resultSlice = this.resultSlices[z];
			
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int index = sizeX * y + x;
					int val = maskSlice[index];
					resultSlice[index] = val == 0 ? 0 : Float.MAX_VALUE;
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
		
		// Two iterations are enough to compute distance map to boundary
		forwardIteration();
		backwardIteration();

		// Normalize values by the first weight
		if (this.normalizeMap) 
		{
			fireStatusChanged(this, "Normalize map..."); 
			for (int z = 0; z < sizeZ; z++) 
			{
				fireProgressChanged(this, z, sizeZ); 

				byte[] maskSlice = this.maskSlices[z];
				float[] resultSlice = this.resultSlices[z];
				
				for (int y = 0; y < sizeY; y++) 
				{
					for (int x = 0; x < sizeX; x++) 
					{
						int index = sizeX * y + x;
						if (maskSlice[index] != 0)
						{
							resultSlice[index] = resultSlice[index] / weights[0];
						}
					}
				}
			}
			fireProgressChanged(this, 1, 1); 
		}
				
		return buffer;
	}

	private static final byte[][] getByteArrays(ImageStack stack)
	{
		// Initialize result array
		int size = stack.getSize();
		byte[][] slices = new byte[size][];
		
		// Extract inner slice array and apply type conversion
		Object[] array = stack.getImageArray();
		for (int i = 0; i < size; i++)
		{
			slices[i] = (byte[]) array[i];
		}
		
		// return slices
		return slices;
	}
	
	private static final float[][] getFloatArrays(ImageStack stack)
	{
		// Initialize result array
		int size = stack.getSize();
		float[][] slices = new float[size][];
		
		// Extract inner slice array and apply type conversion
		Object[] array = stack.getImageArray();
		for (int i = 0; i < size; i++)
		{
			slices[i] = (float[]) array[i];
		}
		
		// return slices
		return slices;
	}
	
	private void forwardIteration() 
	{
		fireStatusChanged(this, "Forward scan..."); 
		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++)
		{
			fireProgressChanged(this, z, sizeZ);
			
			byte[] maskSlice = this.maskSlices[z];
			float[] resultSlice = this.resultSlices[z];
			
			float[] resultSlice2 = null;
			if (z > 0) 
			{
				resultSlice2 = this.resultSlices[z - 1];
			}
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int index = sizeX * y + x;

					// check if we need to update current voxel
					if ((maskSlice[index] & 0x00FF) != maskLabel)
						continue;
					
					// init new values for current voxel
					double ortho = Double.MAX_VALUE;
					double diago = Double.MAX_VALUE;
					double diag3 = Double.MAX_VALUE;
					
					// process (z-1) slice
					if (z > 0) 
					{
						if (y > 0)
						{
							// voxels in the (y-1) line of  the (z-1) plane
							if (x > 0) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y - 1) + x - 1]);
							}
							diago = Math.min(diago, resultSlice2[sizeX * (y - 1) + x]);
							if (x < sizeX - 1) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y - 1) + x + 1]);
							}
						}
						
						// voxels in the y line of the (z-1) plane
						if (x > 0) 
						{
							diago = Math.min(diago, resultSlice2[sizeX * y + x - 1]);
						}
						ortho = Math.min(ortho, resultSlice2[sizeX * y + x]);
						if (x < sizeX - 1) 
						{
							diago = Math.min(diago, resultSlice2[sizeX * y + x + 1]);
						}

						if (y < sizeX - 1)
						{
							// voxels in the (y+1) line of  the (z-1) plane
							if (x > 0) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y + 1) + x - 1]);
							}
							diago = Math.min(diago, resultSlice2[sizeX * (y + 1) + x]);
							if (x < sizeX - 1) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y + 1) + x + 1]);
							}
						}
						
					}
					
					// voxels in the (y-1) line of the z-plane
					if (y > 0)
					{
						if (x > 0) 
						{
							diago = Math.min(diago, resultSlice[sizeX * (y - 1) + x - 1]);
						}
						ortho = Math.min(ortho, resultSlice[sizeX * (y - 1) + x]);
						if (x < sizeX - 1) 
						{
							diago = Math.min(diago, resultSlice[sizeX * (y - 1) + x + 1]);
						}
					}
					
					// pixel to the left of the current voxel
					if (x > 0) 
					{
						ortho = Math.min(ortho, resultSlice[index - 1]);
					}
					
					double newVal = min3w(ortho, diago, diag3);
					updateIfNeeded(x, y, z, newVal);
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}

	private void backwardIteration() 
	{
		fireStatusChanged(this, "Backward scan..."); 
		// iterate on image voxels in backward order
		for (int z = sizeZ - 1; z >= 0; z--)
		{
			fireProgressChanged(this, sizeZ-1-z, sizeZ); 
			
			byte[] maskSlice = this.maskSlices[z];
			float[] resultSlice = this.resultSlices[z];
			
			float[] resultSlice2 = null;
			if (z < sizeZ - 1) 
			{
				resultSlice2 = this.resultSlices[z + 1];
			}
			
			for (int y = sizeY - 1; y >= 0; y--)
			{
				for (int x = sizeX - 1; x >= 0; x--)
				{
					int index = sizeX * y + x;

					// check if we need to update current voxel
					if ((maskSlice[index] & 0x00FF) != maskLabel)
						continue;
					
					// init new values for current voxel
					double ortho = Double.MAX_VALUE;
					double diago = Double.MAX_VALUE;
					double diag3 = Double.MAX_VALUE;
					
					// process (z+1) slice
					if (z < sizeZ - 1) 
					{
						if (y < sizeY - 1)
						{
							// voxels in the (y+1) line of  the (z+1) plane
							if (x < sizeX - 1) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y + 1) + x + 1]);
							}
							diago = Math.min(diago, resultSlice2[sizeX * (y + 1) + x]);
							if (x > 0) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y + 1) + x - 1]);
							}
						}
						
						// voxels in the y line of the (z+1) plane
						if (x < sizeX - 1) 
						{
							diago = Math.min(diago, resultSlice2[sizeX * y + x + 1]);
						}
						ortho = Math.min(ortho, resultSlice2[sizeX * y + x]);
						if (x > 0) 
						{
							diago = Math.min(diago, resultSlice2[sizeX * y + x - 1]);
						}

						if (y > 0)
						{
							// voxels in the (y-1) line of  the (z+1) plane
							if (x < sizeX - 1) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y - 1) + x + 1]);
							}
							diago = Math.min(diago, resultSlice2[sizeX * (y - 1) + x]);
							if (x > 0) 
							{
								diag3 = Math.min(diag3, resultSlice2[sizeX * (y - 1) + x - 1]);
							}
						}
					}
					
					// voxels in the (y+1) line of the z-plane
					if (y < sizeY - 1)
					{
						if (x < sizeX - 1) 
						{
							diago = Math.min(diago, resultSlice[sizeX * (y + 1) + x + 1]);
						}
						ortho = Math.min(ortho, resultSlice[sizeX * (y + 1) + x]);
						if (x > 0) 
						{
							diago = Math.min(diago, resultSlice[sizeX * (y + 1) + x - 1]);
						}
					}
					
					// pixel to the left of the current voxel
					if (x < sizeX - 1) 
					{
						ortho = Math.min(ortho, resultSlice[index + 1]);
					}
					
					double newVal = min3w(ortho, diago, diag3);
					updateIfNeeded(x, y, z, newVal);
				}
			}
		}
		fireProgressChanged(this, 1, 1); 
	}
	
	/**
	 * Computes the weighted minima of orthogonal, diagonal, and 3D diagonal
	 * values.
	 */
	private double min3w(double ortho, double diago, double diag2)
	{
		return min(min(ortho + weights[0], diago + weights[1]), 
				diag2 + weights[2]);
	}
	
	/**
	 * Update the pixel at position (i,j,k) with the value newVal. If newVal is
	 * greater or equal to current value at position (i,j,k), do nothing.
	 */
	private void updateIfNeeded(int i, int j, int k, double newVal)
	{
		int index = j * sizeX + i;
		double value = resultSlices[k][j * sizeX + i];
		if (newVal < value) 
		{
			resultSlices[k][index] = (float) newVal;
		}
	}
}
