/**
 * 
 */
package inra.ijpb.label.distmap;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;

/**
 * Computes Euclidean distance transform of a 2D binary array using algorithm of
 * Saito and Toriwaki (1994). Uses floating point computations.
 * 
 * Details:
 * <ul>
 * <li>works for 3D</li>
 * <li>uses floating point computations</li>
 * <li>can manage spatial calibration</li>
 * </ul>
 */
public class SaitoToriwakiDistanceTransform3DFloat extends AlgoStub implements DistanceTransform3D
{
	boolean normalize;
	
	/**
	 * Default empty constructor for (normalized) Euclidean distance transform
	 * algorithm.
	 */
	public SaitoToriwakiDistanceTransform3DFloat()
	{
		this(true);
	}
	
	/**
	 * Consructor that specifies whether the resulting distance map should be
	 * normalized (corresponding to Euclidean distance), or not (corresponding
	 * to the square of the Euclidean distance).
	 * 
	 * @param normalize
	 *            boolean flag for normalizing the result to Euclidean distance.
	 *            If not, returns the squared distance transform.
	 */
	public SaitoToriwakiDistanceTransform3DFloat(boolean normalize)
	{
		this.normalize = normalize;
	}
	
    @Override
	public ImageStack distanceMap(ImageStack stack)
	{
		return distanceMap(stack, new double[] { 1.0, 1.0, 1.0 });
	}

	/**
     * Computes the distance map of the specified binary array and the specified
     * array of spacings between array elements.
     * 
     * @param stack
     *            the binary stack to process
     * @param spacings
     *            the spacings between array elements (with as many elements as
     *            array dimensionality)
     * @return the ImageStack containing the distance map
     */
	public ImageStack distanceMap(ImageStack stack, double[] spacings)
	{
        if (spacings.length != 3)
        {
            throw new IllegalArgumentException("Spacing array must have length 3");
        }
        
        this.fireStatusChanged(this, "Allocate memory");
        ImageStack output = ImageStack.create(stack.getWidth(), stack.getHeight(), stack.getSize(), 32);
        
        distanceMapSquared3d(stack, output, spacings);

        // convert squared distance to distance
        if (normalize)
        {
        	normalize(output);
        }
        
        return output;
    }
    
    /**
     * Computes the squared distance map of the specified binary array and the
     * specified array of spacings between array elements.
     * 
     * @param array
     *            the binary array to process
     * @param output
     *            the array of scalar values used to store the computation result
     * @param spacings
     *            the spacings between array elements (with as many elements as
     *            array dimensionality)
     * @return the squared distance map
     */
    public double distanceMapSquared3d(ImageStack array, ImageStack output, double[] spacings)
    {
        if (spacings.length != 3)
        {
            throw new IllegalArgumentException("Spacing array must have length 3");
        }
        
        processStep1(array, output, spacings[0]);
        processStep2(array, output, spacings[1]);
        return processStep3(array, output, spacings[2]);
    }
    
    private void processStep1(ImageStack array, ImageStack output, double spacingX)
    {
        // retrieve size of array
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        int sizeZ = array.getSize();
        
        double sx = spacingX;
        double absoluteMaximum = (sizeX + sizeY) * sx;
        
        // forward scan
        this.fireStatusChanged(this, "Process X-direction, forward pass");
        for (int z = 0; z < sizeZ; z++)
        {
            this.fireProgressChanged(this, z, sizeZ);
            for (int y = 0; y < sizeY; y++)
            {
                double df = absoluteMaximum;
                for (int x = 0; x < sizeX; x++)
                {
                    // either increment or reset current distance
                    df = array.getVoxel(x, y, z) > 0 ? df + sx : 0;
                    output.setVoxel(x, y, z, df * df);
                }
            }
        }
        
        // backward scan
        this.fireStatusChanged(this, "Process X-direction, backward pass");
        for (int z = 0; z < sizeZ; z++)
        {
            this.fireProgressChanged(this, z, sizeZ);
            for (int y = sizeY - 1; y >= 0; y--)
            {
                double db = absoluteMaximum;
                for (int x = sizeX - 1; x >= 0; x--)
                {
                    // either increment or reset current distance
                    db = array.getVoxel(x, y, z) > 0 ? db + sx : 0;
                    output.setVoxel(x, y, z, Math.min(output.getVoxel(x, y, z), db * db));
                }
            }
        }
    }

    private void processStep2(ImageStack array, ImageStack output, double spacingY)
    {
        // retrieve size of array
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        int sizeZ = array.getSize();

        double sy2 = spacingY * spacingY;
        
        double[] buffer = new double[sizeY];
        double distMax = 0.0;

        // iterate over y-columns of the array
        this.fireStatusChanged(this, "Process Y-direction");
        for (int z = 0; z < sizeZ; z++)
        {
            this.fireProgressChanged(this, z, sizeZ);
            for (int x = 0; x < sizeX; x++)
            {
                // init buffer
                for (int y = 0; y < sizeY; y++)
                {
                    buffer[y] = output.getVoxel(x, y, z);
                }

                for (int y = 0; y < sizeY; y++)
                {
                    double dist = buffer[y];
                    if (dist > 0)
                    {
                        // compute bounds of interval to look for min distance 
                        int rMax = (int) Math.ceil(Math.sqrt(dist) / spacingY + 1);
                        int rStart = Math.min(rMax, y);
                        int rEnd = Math.min(rMax, sizeY - y);

                        for (int n = -rStart; n < rEnd; n++)
                        {
                            double w = buffer[y + n] + n * n * sy2;
                            if (w < dist) dist = w;
                        }

                        if (dist < buffer[y])
                        {
                            output.setVoxel(x, y, z, dist);
                        }

                        if (dist > distMax)
                        {
                            distMax = dist;
                        }
                    }
                }
            }
        }
    }

    private double processStep3(ImageStack array, ImageStack output, double spacingZ)
    {
        // retrieve size of array
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        int sizeZ = array.getSize();

        double sz2 = spacingZ * spacingZ;
        
        double[] buffer = new double[sizeZ];
        double distMax = 0.0;

        // iterate over y-columns of the array
        this.fireStatusChanged(this, "Process Z-direction");
        for (int y = 0; y < sizeY; y++)
        {
            this.fireProgressChanged(this, y, sizeY);
            for (int x = 0; x < sizeX; x++)
            {
                // init buffer
                for (int z = 0; z < sizeZ; z++)
                {
                    buffer[z] = output.getVoxel(x, y, z);
                }

                for (int z = 0; z < sizeZ; z++)
                {
                    double dist = buffer[z];
                    if (dist > 0)
                    {
                        // compute bounds of interval to look for min distance 
                        int rMax = (int) Math.ceil(Math.sqrt(dist) / spacingZ + 1);
                        int rStart = Math.min(rMax, z);
                        int rEnd = Math.min(rMax, sizeZ - z);

                        for (int n = -rStart; n < rEnd; n++)
                        {
                            double w = buffer[z + n] + n * n * sz2;
                            if (w < dist) dist = w;
                        }

                        if (dist < buffer[z])
                        {
                            output.setVoxel(x, y, z, dist);
                        }

                        if (dist > distMax)
                        {
                            distMax = dist;
                        }
                    }
                }
            }
        }

        return distMax;
    }

	private static final void normalize(ImageStack array)
	{
        // convert squared distance to distance
        for (int z = 0; z < array.getSize(); z++)
		{
			for (int y = 0; y < array.getHeight(); y++)
			{
				for (int x = 0; x < array.getWidth(); x++)
				{
					array.setVoxel(x, y, z, Math.sqrt(array.getVoxel(x, y, z)));
				}
			}
		}
	}
}
