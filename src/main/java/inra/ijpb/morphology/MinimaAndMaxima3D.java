/**
 * 
 */
package inra.ijpb.morphology;

import ij.ImageStack;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.extrema.ExtremaType;
import inra.ijpb.morphology.extrema.RegionalExtrema3DAlgo;
import inra.ijpb.morphology.extrema.RegionalExtrema3DByFlooding;

/**
 * <p>
 * A collection of static methods for computing regional and extended minima and
 * maxima on 3D stacks. Supports integer and floating-point stacks, in 6 and 26
 * connectivities.
 * </p>
 * 
 * <p>
 * Regional extrema algorithms are based on flood-filling-like algorithms,
 * whereas extended extrema and extrema imposition algorithms use geodesic
 * reconstruction algorithm.
 * </p>
 * 
 * <p>
 * See the books of Serra and Soille for further details.
 * </p>
 * 
 * @see MinimaAndMaxima
 * @see GeodesicReconstruction3D
 * @see FloodFill
 * 
 * @author David Legland
 * 
 */
public class MinimaAndMaxima3D 
{
	/**
	 * The default connectivity used by reconstruction algorithms in 3D images.
	 */
	private final static int DEFAULT_CONNECTIVITY = 6;

	/**
	 * Private constructor to prevent class instantiation.
	 */
	private MinimaAndMaxima3D()
	{
	}

	/**
	 * Computes the regional maxima in 3D stack <code>image</code>, 
	 * using the default connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @return the regional maxima of input image
	 */
	public final static ImageStack regionalMaxima(ImageStack image) 
	{
		return regionalMaxima(image, DEFAULT_CONNECTIVITY);
	}

	/**
	 * Computes the regional maxima in grayscale image <code>stack</code>, 
	 * using the specified connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param conn
	 *            the connectivity for maxima, that should be either 6 or 26
	 * @return the regional maxima of input image
	 */
	public final static ImageStack regionalMaxima(ImageStack image,
			int conn) 
	{
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MAXIMA);
		DefaultAlgoListener.monitor(algo);
		
		return algo.applyTo(image);
	}

	/**
	 * Computes the regional maxima in 3D image <code>stack</code>, 
	 * using the specified connectivity, and a slower algorithm (used for testing).
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param conn
	 *            the connectivity for maxima, that should be either 6 or 26
	 * @return the regional maxima of input image
	 */
	public final static ImageStack regionalMaximaByReconstruction(
			ImageStack image,
			int conn) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
	
		ImageStack mask = addValue(image, 1);
	
		ImageStack rec = GeodesicReconstruction3D.reconstructByDilation(image, mask, conn);
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
	
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if (mask.getVoxel(x, y, z) > rec.getVoxel(x, y, z)) 
						result.setVoxel(x, y, z, 255);
					else
						result.setVoxel(x, y, z, 0);
				}
			}
		}		
		return result;
	}

	/**
	 * Computes the regional maxima in grayscale image <code>stack</code>, 
	 * using the specified connectivity and binary mask.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param conn
	 *            the connectivity for maxima, that should be either 6 or 26
	 * @param mask
	 *            the binary mask that restricts the processing
	 * @return the regional maxima of input image
	 */
	public final static ImageStack regionalMaxima(
			ImageStack image,
			int conn,
			ImageStack mask ) 
	{
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MAXIMA);
		DefaultAlgoListener.monitor(algo);
		
		return algo.applyTo(image, mask);
	}

	/**
	 * Computes the regional minima in 3D image <code>stack</code>, 
	 * using the default connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @return the regional minima of input image
	 */
	public final static ImageStack regionalMinima(ImageStack image) 
	{
		return regionalMinima(image, DEFAULT_CONNECTIVITY);
	}

	/**
	 * Computes the regional minima in 3D stack <code>image</code>, 
	 * using the specified connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param conn
	 *            the connectivity for minima, that should be either 6 or 26
	 * @return the regional minima of input image
	 */
	public final static ImageStack regionalMinima(ImageStack image,
			int conn) 
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MINIMA);
		DefaultAlgoListener.monitor(algo);
		
		return algo.applyTo(image);
	}

	/**
	 * Computes the regional minima in grayscale image <code>image</code>, 
	 * using the specified connectivity, and a slower algorithm (used for testing).
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param conn
	 *            the connectivity for minima, that should be either 6 or 26
	 * @return the regional minima of input image
	 */
	public final static ImageStack regionalMinimaByReconstruction(ImageStack image,
			int conn)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
	
		ImageStack marker = addValue(image, 1);
	
		ImageStack rec = GeodesicReconstruction3D.reconstructByErosion(marker,
				image, conn);

		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++)
				{
					if (marker.getVoxel(x, y, z) > rec.getVoxel(x, y, z)) 
						result.setVoxel(x, y, z, 0);
					else
						result.setVoxel(x, y, z, 255);
				}
			}
		}		
		return result;
	}

	/**
	 * Computes the regional minima in 3D image <code>stack</code>, 
	 * using the specified connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param conn
	 *            the connectivity for minima, that should be either 6 or 26
	 * @param mask
	 *            the binary mask that restricts processing
	 * @return the regional minima of input image
	 */
	public final static ImageStack regionalMinima(
			ImageStack image,
			int conn,
			ImageStack mask ) 
	{
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MINIMA);
		DefaultAlgoListener.monitor(algo);
		
		return algo.applyTo(image, mask);
	}

	/**
	 * Computes the extended maxima in grayscale image <code>image</code>, 
	 * keeping maxima with the specified dynamic, and using the default 
	 * connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param dynamic
	 *            the difference between maxima and maxima boundary
	 * @return the extended maxima of input image
	 */
	public final static ImageStack extendedMaxima(ImageStack image,
			double dynamic) 
	{
		return extendedMaxima(image, dynamic, DEFAULT_CONNECTIVITY);
	}

	/**
	 * Computes the extended maxima in grayscale image <code>image</code>, 
	 * keeping maxima with the specified dynamic, and using the specified
	 * connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param dynamic
	 *            the difference between maxima and maxima boundary
	 * @param conn
	 *            the connectivity for maxima, that should be either 6 or 26
	 * @return the extended maxima of input image
	 */
	public final static ImageStack extendedMaxima(ImageStack image,
			double dynamic, int conn) 
	{
		ImageStack mask = addValue(image, dynamic);

		ImageStack rec = GeodesicReconstruction3D.reconstructByDilation(image, mask, conn);

		return regionalMaxima(rec, conn);
	}
	
	/**
	 * Computes the extended maxima in the grayscale image <code>image</code>,
	 * keeping maxima with the specified dynamic, restricted to the non-zero
	 * voxels of the binary mask, and using the default connectivity.
	 * 
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param dynamic
	 *            the difference between maxima and maxima boundary
	 * @param binaryMask
	 *            binary mask image to restrict region of application
	 * @return the extended maxima of input image
	 */
	public final static ImageStack extendedMaxima(
			ImageStack image,
			double dynamic, 
			ImageStack binaryMask ) 
	{
		return extendedMaxima( image, dynamic, DEFAULT_CONNECTIVITY, binaryMask );
	}

	/**
	 * Computes the extended maxima in the grayscale image <code>image</code>,
	 * keeping maxima with the specified dynamic, restricted to the non-zero
	 * voxels of the binary mask, and using the specified connectivity.
	 * 
	 * @param image
	 *            input grayscale image
	 * @param dynamic
	 *            nonnegative scalar defining the depth threshold of maxima
	 *            removal ("h" value in Soile, 1999)
	 * @param conn
	 *            connectivity value (6 or 26)
	 * @param binaryMask
	 *            binary mask image to restrict region of application
	 * @return the extended maxima of input image
	 */
	public final static ImageStack extendedMaxima(
			ImageStack image,
			double dynamic, 
			int conn,
			ImageStack binaryMask ) 
	{
		ImageStack mask = addValue(image, dynamic);

		ImageStack rec = GeodesicReconstruction3D.reconstructByDilation( image, mask, conn, binaryMask );

		return regionalMaxima(rec, conn);
	}

	/**
	 * Computes the extended minima in grayscale image <code>image</code>, 
	 * keeping minima with the specified dynamic, and using the default 
	 * connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param dynamic
	 *            the difference between minima and minima boundary
	 * @return the extended minima of input image
	 */
	public final static ImageStack extendedMinima(ImageStack image, double dynamic)
	{
		return extendedMinima(image, dynamic, DEFAULT_CONNECTIVITY);
	}

	/**
	 * Computes the extended minima in grayscale image <code>image</code>, 
	 * keeping minima with the specified dynamic, and using the specified 
	 * connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param dynamic
	 *            the difference between minima and minima boundary
	 * @param conn
	 *            the connectivity for minima, that should be either 6 or 26
	 * @return the extended minima of input image
	 */
	public final static ImageStack extendedMinima(ImageStack image,
			double dynamic, int conn) 
	{
		ImageStack marker = addValue(image, dynamic);

		ImageStack rec = GeodesicReconstruction3D.reconstructByErosion(marker, image, conn);

		if( null == rec )
			return null;
		
		return regionalMinima(rec, conn);
	}

	/**
	 * Imposes the maxima given by marker image into the input image, using 
	 * the default connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param maxima
	 *            a 3D binary image of maxima 
	 * @return the result of maxima imposition
	 */
	public final static ImageStack imposeMaxima(ImageStack image,
			ImageStack maxima)
	{
		return imposeMaxima(image, maxima, DEFAULT_CONNECTIVITY);
	}

	/**
	 * Imposes the maxima given by marker image into the input image, using
	 * the specified connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param maxima
	 *            a 3D binary image of maxima 
	 * @param conn
	 *            the connectivity for maxima, that should be either 6 or 26
	 * @return the result of maxima imposition
	 */
	public final static ImageStack imposeMaxima(ImageStack image,
			ImageStack maxima, int conn)
	{
		ImageStack marker = image.duplicate();
		ImageStack mask = image.duplicate();

		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if (maxima.getVoxel(x, y, z) > 0)
					{
						marker.setVoxel(x, y, z, 255);
						mask.setVoxel(x, y, z, 255);
					} 
					else
					{
						marker.setVoxel(x, y, z, 0);
						mask.setVoxel(x, y, z, image.getVoxel(x, y, z)-1);
					}
				}
			}
		}

		return GeodesicReconstruction3D.reconstructByDilation(marker, mask, conn);
	}

	/**
	 * Imposes the minima given by marker image into the input image, using 
	 * the default connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param minima
	 *            a 3D binary image of minima 
	 * @return the result of minima imposition
	 */
	public final static ImageStack imposeMinima(ImageStack image,
			ImageStack minima) 
	{
		return imposeMinima(image, minima, DEFAULT_CONNECTIVITY);
	}

	/**
	 * Imposes the minima given by marker image into the input image, using 
	 * the specified connectivity.
	 * 
	 * @param image
	 *            the 3D image to process
	 * @param minima
	 *            a 3D binary image of minima 
	 * @param conn
	 *            the connectivity for minima, that should be either 6 or 26
	 * @return the result of minima imposition
	 */
	public final static ImageStack imposeMinima(ImageStack image,
			ImageStack minima, int conn) 
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;

		ImageStack marker = image.duplicate();
		ImageStack mask = image.duplicate();

		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if (minima.getVoxel(x, y, z) > 0) 
					{
						marker.setVoxel(x, y, z, 0);
						mask.setVoxel(x, y, z, 0);
					} 
					else 
					{
						marker.setVoxel(x, y, z, 255);
						mask.setVoxel(x, y, z, image.getVoxel(x, y, z)+1);
					}
				}
			}
		}

		return GeodesicReconstruction3D.reconstructByErosion(marker, mask, conn);
	}

	/**
	 * Adds the specified value to each voxel of the 3D stack.
	 * 
	 * @param image
	 *            the original 3D image
	 * @param value
	 *            the value to add
	 * @return a new ImageStack with same type with value added
	 */
	private final static ImageStack addValue(ImageStack image, double value) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, image.getBitDepth());
		
		Image3D image2 = Images3D.createWrapper(image);
		Image3D result2 = Images3D.createWrapper(result);

		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					result2.setValue(x, y, z, image2.getValue(x, y, z) + value);
				}
			}
		}
		
		return result;
	}
}
