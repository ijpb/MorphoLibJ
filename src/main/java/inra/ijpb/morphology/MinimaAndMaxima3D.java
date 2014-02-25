/**
 * 
 */
package inra.ijpb.morphology;

import ij.ImageStack;
import inra.ijpb.morphology.extrema.ExtremaType;
import inra.ijpb.morphology.extrema.RegionalExtrema3DAlgo;
import inra.ijpb.morphology.extrema.RegionalExtrema3DByFlooding;

/**
 * A collection of static methods for computing regional and extended minima and
 * maxima on 3D stacks. Supports integer and floating-point stacks, in 6 and 26
 * connectivities.
 * 
 * Regional extrema algorithms are based on flood-filling-like algorithms,
 * whereas extended extrema and extrema imposition algorithms use geodesic
 * reconstruction algorithm.
 * 
 * See the books of Serra and Soille for further details.
 * 
 * @see MinimaAndMaxima
 * @see GeodesicReconstruction3D
 * @see FloodFill
 * 
 * @author David Legland
 * 
 */
public class MinimaAndMaxima3D {

	/**
	 * The default connectivity used by reconstruction algorithms in 3D images.
	 */
	public final static int DEFAULT_CONNECTIVITY_3D = 6;

	/**
	 * Computes the regional maxima in 3D stack <code>image</code>, 
	 * using the default connectivity.
	 */
	public final static ImageStack regionalMaxima(ImageStack image) {
		return regionalMaxima(image, DEFAULT_CONNECTIVITY_3D);
	}

	/**
	 * Computes the regional maxima in grayscale image <code>stack</code>, 
	 * using the specified connectivity.
	 * @param conn the connectivity for maxima, that should be either 6 or 26
	 */
	public final static ImageStack regionalMaxima(ImageStack image,
			int conn) {
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MAXIMA);
		
		return algo.applyTo(image);
	}

	/**
	 * Computes the regional maxima in grayscale image <code>stack</code>, 
	 * using the specified connectivity and binary mask.
	 * @param conn the connectivity for maxima, that should be either 6 or 26
	 */
	public final static ImageStack regionalMaxima(
			ImageStack stack,
			int conn,
			ImageStack mask ) 
	{
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MAXIMA);
		
		return algo.applyTo(stack, mask);
	}

	/**
	 * Computes the regional maxima in 3D image <code>stack</code>, 
	 * using the specified connectivity, and a slower algorithm (used for testing).
	 * @param conn the connectivity for maxima, that should be either 6 or 26
	 */
	public final static ImageStack regionalMaximaByReconstruction(
			ImageStack stack,
			int conn) {
		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		ImageStack mask = stack.duplicate();
		addValue(mask, 1);

//		GeodesicReconstruction3DAlgo algo = new GeodesicReconstructionByDilation3DGray8Scanning(conn);
//		ImageStack rec = algo.applyTo(stack, mask);
		ImageStack rec = GeodesicReconstruction3D.reconstructByDilation(stack, mask, conn);
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
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
	 * Computes the regional minima in 3D image <code>stack</code>, 
	 * using the default connectivity.
	 */
	public final static ImageStack regionalMinima(ImageStack stack) {
		return regionalMinima(stack, DEFAULT_CONNECTIVITY_3D);
	}

	/**
	 * Computes the regional minima in 3D stack <code>image</code>, 
	 * using the specified connectivity.
	 * @param conn the connectivity for minima, that should be either 6 or 26
	 */
	public final static ImageStack regionalMinima(ImageStack image,
			int conn) {
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MINIMA);
		
		return algo.applyTo(image);
	}

	/**
	 * Computes the regional minima in 3D image <code>stack</code>, 
	 * using the specified connectivity.
	 * @param conn the connectivity for minima, that should be either 6 or 26
	 */
	public final static ImageStack regionalMinima(
			ImageStack stack,
			int conn,
			ImageStack mask ) 
	{
		RegionalExtrema3DAlgo algo = new RegionalExtrema3DByFlooding();
		algo.setConnectivity(conn);
		algo.setExtremaType(ExtremaType.MINIMA);
		
		return algo.applyTo(stack, mask);
	}

	/**
	 * Computes the regional minima in grayscale image <code>image</code>, 
	 * using the specified connectivity, and a slower algorithm (used for testing).
	 * @param conn the connectivity for minima, that should be either 4 or 8
	 */
	public final static ImageStack regionalMinimaByReconstruction(ImageStack stack,
			int conn) {

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		ImageStack marker = stack.duplicate();
		addValue(marker, 1);

//		GeodesicReconstruction3DAlgo algo = new GeodesicReconstructionByErosion3DGray8Scanning(conn);
//		ImageStack rec = algo.applyTo(marker, stack);
		ImageStack rec = GeodesicReconstruction3D.reconstructByErosion(marker, stack, conn);
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
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
	 * Computes the extended maxima in grayscale image <code>image</code>, 
	 * keeping maxima with the specified dynamic, and using the default 
	 * connectivity.
	 */
	public final static ImageStack extendedMaxima(ImageStack image,
			int dynamic) {
		return extendedMaxima(image, dynamic, DEFAULT_CONNECTIVITY_3D);
	}

	/**
	 * Computes the extended maxima in the grayscale image <code>image</code>, 
	 * keeping maxima with the specified dynamic, restricted to the non-zero
	 * voxels of the binary mask, and using the default connectivity.
	 * 
	 * @param image input grayscale image
	 * @param dynamic nonnegative scalar defining the depth threshold of maxima removal ("h" value in Soile, 1999) 
	 * @param binaryMask binary mask image to restrict region of application
	 */
	public final static ImageStack extendedMaxima(
			ImageStack image,
			int dynamic, 
			ImageStack binaryMask ) {
		return extendedMaxima( image, dynamic, DEFAULT_CONNECTIVITY_3D, binaryMask );
	}
	
	/**
	 * Computes the extended maxima in grayscale image <code>image</code>, 
	 * keeping maxima with the specified dynamic, and using the specified
	 * connectivity.
	 */
	public final static ImageStack extendedMaxima(ImageStack image,
			int dynamic, int conn) {
		ImageStack mask = image.duplicate();
		addValue(mask, dynamic);

		//		GeodesicReconstruction3DAlgo algo = new GeodesicReconstructionByDilation3DGray8Scanning(conn);
		//		ImageStack rec = algo.applyTo(image, mask);
		ImageStack rec = GeodesicReconstruction3D.reconstructByDilation(image, mask, conn);

		return regionalMaxima(rec, conn);
	}
	
	/**
	 * Computes the extended maxima in the grayscale image <code>image</code>, 
	 * keeping maxima with the specified dynamic, restricted to the non-zero
	 * voxels of the binary mask, and using the specified connectivity.
	 * 
	 * @param image input grayscale image
	 * @param dynamic nonnegative scalar defining the depth threshold of maxima removal ("h" value in Soile, 1999) 
	 * @param conn connectivity value (6 or 26)
	 * @param binaryMask binary mask image to restrict region of application
	 */
	public final static ImageStack extendedMaxima(
			ImageStack image,
			int dynamic, 
			int conn,
			ImageStack binaryMask ) 
	{
		ImageStack mask = image.duplicate();
		addValue( mask, dynamic );

		//		GeodesicReconstruction3DAlgo algo = new GeodesicReconstructionByDilation3DGray8Scanning(conn);
		//		ImageStack rec = algo.applyTo(image, mask);
		ImageStack rec = GeodesicReconstruction3D.reconstructByDilation( image, mask, conn, binaryMask );

		return regionalMaxima(rec, conn);
	}

	/**
	 * Computes the extended minima in grayscale image <code>image</code>, 
	 * keeping minima with the specified dynamic, and using the default 
	 * connectivity.
	 */
	public final static ImageStack extendedMinima(ImageStack stack, int dynamic) {
		return extendedMinima(stack, dynamic, DEFAULT_CONNECTIVITY_3D);
	}

	/**
	 * Computes the extended minima in grayscale image <code>image</code>, 
	 * keeping minima with the specified dynamic, and using the specified 
	 * connectivity.
	 */
	public final static ImageStack extendedMinima(ImageStack stack,
			int dynamic, int conn) {
		ImageStack marker = stack.duplicate();
		addValue(marker, dynamic);

//		GeodesicReconstruction3DAlgo algo = new GeodesicReconstructionByErosion3DGray8Scanning(conn);
//		ImageStack rec = algo.applyTo(marker, stack);
		ImageStack rec = GeodesicReconstruction3D.reconstructByErosion(marker, stack, conn);

		return regionalMinima(rec, conn);
	}

	/**
	 * Imposes the maxima given by marker image into the input image, using 
	 * the default connectivity.
	 */
	public final static ImageStack imposeMaxima(ImageStack stack,
			ImageStack maxima) {
		return imposeMaxima(stack, maxima, DEFAULT_CONNECTIVITY_3D);
	}

	/**
	 * Imposes the maxima given by marker image into the input image, using
	 * the specified connectivity.
	 */
	public final static ImageStack imposeMaxima(ImageStack stack,
			ImageStack maxima, int conn) {

		ImageStack marker = stack.duplicate();
		ImageStack mask = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					if (maxima.getVoxel(x, y, z) > 0) {
						marker.setVoxel(x, y, z, 255);
						mask.setVoxel(x, y, z, 255);
					} else {
						marker.setVoxel(x, y, z, 0);
						mask.setVoxel(x, y, z, stack.getVoxel(x, y, z)-1);
					}
				}
			}
		}

		return GeodesicReconstruction3D.reconstructByDilation(marker, mask, conn);
	}

	/**
	 * Imposes the minima given by marker image into the input image, using 
	 * the default connectivity.
	 */
	public final static ImageStack imposeMinima(ImageStack stack,
			ImageStack minima) {
		return imposeMinima(stack, minima, DEFAULT_CONNECTIVITY_3D);
	}

	/**
	 * Imposes the minima given by marker image into the input image, using 
	 * the specified connectivity.
	 */
	public final static ImageStack imposeMinima(ImageStack stack,
			ImageStack minima, int conn) {

		ImageStack marker = stack.duplicate();
		ImageStack mask = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					if (minima.getVoxel(x, y, z) > 0) {
						marker.setVoxel(x, y, z, 0);
						mask.setVoxel(x, y, z, 0);
					} else {
						marker.setVoxel(x, y, z, 255);
						mask.setVoxel(x, y, z, stack.getVoxel(x, y, z)+1);
					}
				}
			}
		}

		return GeodesicReconstruction3D.reconstructByErosion(marker, mask, conn);
	}

	private final static void addValue(ImageStack stack, double value) {
		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					stack.setVoxel(x, y, z, stack.getVoxel(x, y, z) + value);
				}
			}
		}
	}

}
