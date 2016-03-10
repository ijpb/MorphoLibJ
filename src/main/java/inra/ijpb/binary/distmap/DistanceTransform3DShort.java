package inra.ijpb.binary.distmap;

import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;

/**
 * Computes Chamfer distances in a 3x3x3 neighborhood using short point 
 * calculation.
 * 
 * In practice, computations are done with floats, but result is stored in a
 * 3D short image, thus requiring less memory than floating point. 
 * 
 * @author David Legland
 * 
 */
public class DistanceTransform3DShort extends AlgoStub implements DistanceTransform3D 
{
	private final static int DEFAULT_MASK_LABEL = 255;

	private short[] weights;

	private int width;
	private int height;
	private int depth;

	private ImageStack maskProc;

	int maskLabel = DEFAULT_MASK_LABEL;

	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to euclidean, but with 
	 * non integer values. 
	 */
	private boolean normalizeMap = true;

	/**
	 * The inner buffer that will store the distance map. The content
	 * of the buffer is updated during forward and backward iterations.
	 */
	private ImageStack buffer;
	
	/**
	 * Default constructor that specifies the chamfer weights.
	 * @param weights an array of two weights for orthogonal and diagonal directions
	 */
	public DistanceTransform3DShort(short[] weights)
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
	public DistanceTransform3DShort(short[] weights, boolean normalize)
	{
		this.weights = weights;
		this.normalizeMap = normalize;
	}

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
	public ImageStack distanceMap(ImageStack image) 
	{
		// size of image
		width = image.getWidth();
		height = image.getHeight();
		depth = image.getSize();
		
		// update mask
		this.maskProc = image;

		// create new empty image, and fill it with black
		buffer = ImageStack.create(width, height, depth, 16);
		fireStatusChanged(this, "Initialization..."); 
		
		// initialize empty image with either 0 (background) or Inf (foreground)
		for (int k = 0; k < depth; k++) 
		{
			fireProgressChanged(this, k, depth); 
			for (int j = 0; j < height; j++) 
			{
				for (int i = 0; i < width; i++) 
				{
					double val = image.getVoxel(i, j, k);
					buffer.setVoxel(i, j, k, val == 0 ? 0 : Short.MAX_VALUE);
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
			for (int k = 0; k < depth; k++) 
			{
				fireProgressChanged(this, k, depth); 
				for (int j = 0; j < height; j++) 
				{
					for (int i = 0; i < width; i++) 
					{
						if (maskProc.getVoxel(i, j, k) != 0)
						{
							buffer.setVoxel(i, j, k, buffer.getVoxel(i, j, k) / weights[0]);
						}
					}
				}
			}
			fireProgressChanged(this, 1, 1); 
		}
				
		return buffer;
	}

	private void forwardIteration() 
	{
		fireStatusChanged(this, "Forward scan..."); 
		// iterate on image voxels
		for (int z = 0; z < depth; z++)
		{
			fireProgressChanged(this, z, depth); 
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					// check if we need to update current voxel
					if (maskProc.getVoxel(x, y, z) != maskLabel)
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
								diag3 = Math.min(diag3, buffer.getVoxel(x - 1, y - 1, z - 1));
							}
							diago = Math.min(diago, buffer.getVoxel(x, y - 1, z - 1));
							if (x < width - 1) 
							{
								diag3 = Math.min(diag3, buffer.getVoxel(x + 1, y - 1, z - 1));
							}
						}
						
						// voxels in the y line of the (z-1) plane
						if (x > 0) 
						{
							diago = Math.min(diago, buffer.getVoxel(x - 1, y, z - 1));
						}
						ortho = Math.min(ortho, buffer.getVoxel(x, y, z - 1));
						if (x < width - 1) 
						{
							diago = Math.min(diago, buffer.getVoxel(x + 1, y, z - 1));
						}
					}
					
					// voxels in the (y-1) line of the z-plane
					if (y > 0)
					{
						if (x > 0) 
						{
							diago = Math.min(diago, buffer.getVoxel(x - 1, y - 1, z));
						}
						ortho = Math.min(ortho, buffer.getVoxel(x, y - 1, z));
						if (x < width - 1) 
						{
							diago = Math.min(diago, buffer.getVoxel(x + 1, y - 1, z));
						}
					}
					
					// pixel to the left of the current voxel
					if (x > 0) 
					{
						ortho = Math.min(ortho, buffer.getVoxel(x - 1, y, z));
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
		for (int z = depth - 1; z >= 0; z--)
		{
			fireProgressChanged(this, depth-1-z, depth); 
			for (int y = height - 1; y >= 0; y--)
			{
				for (int x = width - 1; x >= 0; x--)
				{
					// check if we need to update current voxel
					if (maskProc.getVoxel(x, y, z) != maskLabel)
						continue;
					
					// init new values for current voxel
					double ortho = Double.MAX_VALUE;
					double diago = Double.MAX_VALUE;
					double diag3 = Double.MAX_VALUE;
					
					// process (z+1) slice
					if (z < depth - 1) 
					{
						if (y < height - 1)
						{
							// voxels in the (y+1) line of  the (z+1) plane
							if (x < width - 1) 
							{
								diag3 = Math.min(diag3, buffer.getVoxel(x + 1, y + 1, z + 1));
							}
							diago = Math.min(diago, buffer.getVoxel(x, y + 1, z + 1));
							if (x > 0) 
							{
								diag3 = Math.min(diag3, buffer.getVoxel(x - 1, y + 1, z + 1));
							}
						}
						
						// voxels in the y line of the (z+1) plane
						if (x < width - 1) 
						{
							diago = Math.min(diago, buffer.getVoxel(x + 1, y, z + 1));
						}
						ortho = Math.min(ortho, buffer.getVoxel(x, y, z + 1));
						if (x > 0) 
						{
							diago = Math.min(diago, buffer.getVoxel(x - 1, y, z + 1));
						}
					}
					
					// voxels in the (y+1) line of the z-plane
					if (y < height - 1)
					{
						if (x < width - 1) 
						{
							diago = Math.min(diago, buffer.getVoxel(x + 1, y + 1, z));
						}
						ortho = Math.min(ortho, buffer.getVoxel(x, y + 1, z));
						if (x > 0) 
						{
							diago = Math.min(diago, buffer.getVoxel(x - 1, y + 1, z));
						}
					}
					
					// pixel to the left of the current voxel
					if (x < width - 1) 
					{
						ortho = Math.min(ortho, buffer.getVoxel(x + 1, y, z));
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
		double value = buffer.getVoxel(i, j, k);
		if (newVal < value) 
		{
			buffer.setVoxel(i, j, k, newVal);
		}
	}
}
