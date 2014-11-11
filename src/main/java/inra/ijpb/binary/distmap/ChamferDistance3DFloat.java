package inra.ijpb.binary.distmap;

import static java.lang.Math.min;
import ij.ImagePlus;
import ij.ImageStack;

/**
 * Computes Chamfer distances in a 3x3 neighborhood using ShortProcessor object
 * for storing result.
 * 
 * @author David Legland
 * 
 */
public class ChamferDistance3DFloat implements ChamferDistance3D {
	
	private final static int DEFAULT_MASK_LABEL = 255;

	float[] weights;

	int width;
	int height;
	int depth;

	ImageStack maskProc;

	int maskLabel = DEFAULT_MASK_LABEL;

	/** 
	 * The value assigned to result pixels that do not belong to the input
	 * image.
	 * Default is short.MAX_VALUE.
	 */
	short backgroundValue = Short.MAX_VALUE;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to euclidean, but with 
	 * non integer values. 
	 */
	boolean normalizeMap = true;

	/**
	 * The inner buffer that will store the distance map. The content
	 * of the buffer is updated during forward and backward iterations.
	 */
	ImageStack buffer;
	
	/**
	 * Default constructor that specifies the chamfer weights.
	 * @param weights an array of two weights for orthogonal and diagonal directions
	 */
	public ChamferDistance3DFloat(float[] weights) {
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
	public ChamferDistance3DFloat(float[] weights, boolean normalize) {
		this.weights = weights;
		this.normalizeMap = normalize;
	}

	/**
	 * @return the backgroundValue
	 */
	public short getBackgroundValue() {
		return backgroundValue;
	}

	/**
	 * @param backgroundValue the backgroundValue to set
	 */
	public void setBackgroundValue(short backgroundValue) {
		this.backgroundValue = backgroundValue;
	}

	public ImagePlus distanceMap(ImagePlus mask, String newName) {

		// size of image
		width = mask.getWidth();
		height = mask.getHeight();

		// get image processors
		maskProc = mask.getStack();
		
		// Compute distance map
		ImageStack rp = distanceMap(maskProc);
			
		// Create image plus for storing the result
		ImagePlus result = new ImagePlus(newName, rp);
		return result;
	}

	/**
	 * Computes the distance map of the distance to the nearest boundary pixel.
	 * The function returns a new short processor the same size as the input,
	 * with values greater or equal to zero. 
	 */
	public ImageStack distanceMap(ImageStack mask) {

		// size of image
		width = mask.getWidth();
		height = mask.getHeight();
		depth = mask.getSize();
		
		// update mask
		this.maskProc = mask;

		// create new empty image, and fill it with black
		buffer = ImageStack.create(width, height, depth, 32);
		
		// initialize empty image with either 0 (background) or Inf (foreground)
		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				for (int k = 0; k < depth; k++) 
				{
					double val = mask.getVoxel(i, j, k);
					buffer.setVoxel(i, j, k, val == 0 ? 0 : backgroundValue);
				}
			}
		}
		
		// Two iterations are enough to compute distance map to boundary
		forwardIteration();
		backwardIteration();

		// Normalize values by the first weight
		if (this.normalizeMap) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					for (int k = 0; k < depth; k++) {
						if (maskProc.getVoxel(i, j, k) != 0) {
							buffer.setVoxel(i, j, k, buffer.getVoxel(i, j, k) / weights[0]);
						}
					}
				}
			}
		}
				
		return buffer;
	}

	private void forwardIteration() {
		// iterate on image voxels
		for (int z = 0; z < depth; z++)
		{
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
	}

	private void backwardIteration() {
		// iterate on image voxels in backward order
		for (int z = depth - 1; z >= 0; z--)
		{
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
	}
	
	/**
	 * Computes the weighted minima of orthogonal, diagonal, and 3D diagonal
	 * values.
	 */
	private double min3w(double ortho, double diago, double diag2) {
		return min(min(ortho + weights[0], diago + weights[1]), 
				diag2 + weights[2]);
	}
	
	/**
	 * Update the pixel at position (i,j,k) with the value newVal. If newVal is
	 * greater or equal to current value at position (i,j,k), do nothing.
	 */
	private void updateIfNeeded(int i, int j, int k, double newVal) {
		double value = buffer.getVoxel(i, j, k);
		if (newVal < value) {
			buffer.setVoxel(i, j, k, newVal);
		}
	}
}
