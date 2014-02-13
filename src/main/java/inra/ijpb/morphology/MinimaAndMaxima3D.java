/**
 * 
 */
package inra.ijpb.morphology;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.morphology.geodrec.GeodesicReconstruction3DAlgo;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByDilation3DGray8Scanning;
import inra.ijpb.morphology.geodrec.GeodesicReconstructionByErosion3DGray8Scanning;

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
	 * Computes the regional maxima in grayscale image <code>stack</code>, 
	 * using the default connectivity.
	 */
	public final static ImageStack regionalMaxima(ImageStack stack) {
		return regionalMaxima(stack, DEFAULT_CONNECTIVITY_3D);
	}

	/**
	 * Computes the regional maxima in grayscale image <code>stack</code>, 
	 * using the specified connectivity.
	 * @param conn the connectivity for maxima, that should be either 6 or 26
	 */
	public final static ImageStack regionalMaxima(ImageStack stack,
			int conn) {
		if (stack.getBitDepth() == 32)
			return regionalMaximaFloat(stack, conn);
		else
			return regionalMaximaInt(stack, conn);
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
		if (stack.getBitDepth() == 32)
			return regionalMaximaFloat( stack, conn, mask );
		else
			return regionalMaximaInt( stack, conn, mask );
	}


	/**
	 * Dispatching method depending on specified connectivity for integer images.
	 */
	private final static ImageStack regionalMaximaInt(
			ImageStack image,
			int conn,
			ImageStack mask ) 
	{
		switch (conn) {
		case 6:
			return regionalMaximaIntC6( image, mask );
		case 26:
			return regionalMaximaIntC26( image, mask );
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}


	/**
	 * Dispatching method depending on specified connectivity for integer images.
	 */
	private final static ImageStack regionalMaximaInt(ImageStack image,
			int conn) {
		switch (conn) {
		case 6:
			return regionalMaximaIntC6(image);
		case 26:
			return regionalMaximaIntC26(image);
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}

	/**
	 * Computes regional maxima in 3D image <code>stack</code>, using
	 * flood-filling-like algorithm with 6 connectivity.
	 */
	private final static ImageStack regionalMaximaIntC6(ImageStack stack) {
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		int nonMaximaMarker = 0;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// check mask
					if ( stack.getVoxel(x, y, z) == 0 )
						continue;
					// current value
					int value = (int) result.getVoxel(x, y, z);
					if (value == nonMaximaMarker)
						continue;

					// maximum value in 6-neighborhood
					int maxVal = 0;
					if (x > 0) 
						maxVal = max(maxVal, (int)stack.getVoxel(x-1, y, z)); 
					if (x < sizeX - 1) 
						maxVal = max(maxVal, (int)stack.getVoxel(x+1, y, z)); 
					if (y > 0) 
						maxVal = max(maxVal, (int)stack.getVoxel(x, y-1, z)); 
					if (y < sizeY - 1) 
						maxVal = max(maxVal, (int)stack.getVoxel(x, y+1, z));
					if (z > 0) 
						maxVal = max(maxVal, (int)stack.getVoxel(x, y, z-1)); 
					if (z < sizeZ - 1) 
						maxVal = max(maxVal, (int)stack.getVoxel(x, y, z+1)); 

					// if one of the neighbors has greater value, the local pixel 
					// is not a maxima. All connected pixels with same value are 
					// set to the marker for maxima.
					if (maxVal > value) {
						FloodFill.floodFillC6(result, x, y, z, nonMaximaMarker);
					}
				}
			}
		}

		// Ensure result is byte stack
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);

		binariseMaxima(result);

		return result;
	}

	/**
	 * Computes regional maxima in 3D image <code>stack</code>, using
	 * flood-filling-like algorithm with 6 connectivity.
	 */
	private final static ImageStack regionalMaximaIntC6(
			ImageStack stack,
			ImageStack mask ) 
	{
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		int nonMaximaMarker = 0;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// check mask
					if ( stack.getVoxel(x, y, z) == 0 )
						continue;
					// current value
					int value = (int) result.getVoxel(x, y, z);
					if (value == nonMaximaMarker)
						continue;

					// maximum value in 6-neighborhood
					int maxVal = 0;
					if ( x > 0 && mask.getVoxel( x-1, y, z ) != 0 ) 
						maxVal = max(maxVal, (int) stack.getVoxel(x-1, y, z)); 

					if (x < sizeX - 1 && mask.getVoxel( x+1, y, z ) != 0 ) 
						maxVal = max(maxVal, (int) stack.getVoxel(x+1, y, z));

					if (y > 0 && mask.getVoxel( x, y-1, z ) != 0 )
						maxVal = max(maxVal, (int) stack.getVoxel(x, y-1, z));

					if (y < sizeY - 1 && mask.getVoxel( x, y+1, z ) != 0 ) 
						maxVal = max(maxVal, (int) stack.getVoxel(x, y+1, z));

					if (z > 0 && mask.getVoxel( x, y, z-1 ) != 0 ) 
						maxVal = max(maxVal, (int) stack.getVoxel(x, y, z-1));

					if (z < sizeZ - 1 && mask.getVoxel( x, y, z+1 ) != 0 ) 
						maxVal = max(maxVal, (int) stack.getVoxel(x, y, z+1));  

					// if one of the neighbors has greater value, the local pixel 
					// is not a maxima. All connected pixels with same value are 
					// set to the marker for maxima.
					if (maxVal > value) {
						FloodFill.floodFillC6(result, x, y, z, nonMaximaMarker);
					}
				}
			}
		}

		// Ensure result is byte stack
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);

		binariseMaxima(result);

		return result;
	}


	/**
	 * Computes regional maxima in grayscale image <code>stack</code>, using
	 * flood-filling-like algorithm with 26 connectivity.
	 */
	private final static ImageStack regionalMaximaIntC26(ImageStack stack) {
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		int nonMaximaMarker = 0;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// current value
					int value = (int) result.getVoxel(x, y, z);
					if (value == nonMaximaMarker)
						continue;

					// maximum value in 26-neighborhood
					int maxVal = 0;
					for (int z2 = max(z-1,0); z2 <= min(z+1, sizeZ-1); z2++) {
						for (int y2 = max(y-1,0); y2 <= min(y+1, sizeY-1); y2++) {
							for (int x2 = max(x-1,0); x2 <= min(x+1, sizeX-1); x2++) {
								maxVal = max(maxVal, (int) stack.getVoxel(x2, y2, z2));
							}
						}
					}

					// if one of the neighbors has greater value, the local pixel 
					// is not a maxima. All connected pixels with same value are 
					// set to the marker for maxima.
					if (maxVal > value) {
						FloodFill.floodFillC26(result, x, y, z, nonMaximaMarker);
					}
				}
			}
		}

		// Ensure result is byte stack
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);

		binariseMaxima(result);

		return result;					
	}

	/**
	 * Computes regional maxima in 3D image <code>stack</code>, using
	 * flood-filling-like algorithm with 26 connectivity.
	 */
	private final static ImageStack regionalMaximaIntC26(
			ImageStack stack,
			ImageStack mask ) 
	{
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		int nonMaximaMarker = 0;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// check mask
					if ( mask.getVoxel(x, y, z) == 0 )
						continue;

					// current value
					int value = (int) result.getVoxel(x, y, z);
					if (value == nonMaximaMarker)
						continue;

					// maximum value in 26-neighborhood
					int maxVal = 0;
					for (int z2 = max(z-1,0); z2 <= min(z+1, sizeZ-1); z2++) {
						for (int y2 = max(y-1,0); y2 <= min(y+1, sizeY-1); y2++) {
							for (int x2 = max(x-1,0); x2 <= min(x+1, sizeX-1); x2++) {
								if ( mask.getVoxel( x2, y2, z2 ) != 0 )
									maxVal = max(maxVal, (int) stack.getVoxel(x2, y2, z2));
							}
						}
					}

					// if one of the neighbors has greater value, the local pixel 
					// is not a maxima. All connected pixels with same value are 
					// set to the marker for maxima.
					if (maxVal > value) {
						FloodFill.floodFillC26(result, x, y, z, nonMaximaMarker);
					}
				}
			}
		}

		// Ensure result is byte stack
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);

		binariseMaxima(result);

		return result;					
	}


	/**
	 * Dispatching method depending on specified connectivity for float images.
	 */
	private final static ImageStack regionalMaximaFloat(ImageStack image,
			int conn) {
		switch (conn) {
		case 6:
			return regionalMaximaFloatC6(image);
		case 26:
			return regionalMaximaFloatC26(image);
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}

	/**
	 * Dispatching method depending on specified connectivity for float images.
	 */
	private final static ImageStack regionalMaximaFloat(
			ImageStack image,
			int conn,
			ImageStack mask ) 
	{
		switch (conn) {
		case 6:
			return regionalMaximaFloatC6( image, mask );
		case 26:
			return regionalMaximaFloatC26( image, mask );
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}


	/**
	 * Computes regional maxima in 3D image <code>stack</code>, using
	 * flood-filling-like algorithm with 6 connectivity.
	 */
	private final static ImageStack regionalMaximaFloatC6(
			ImageStack stack,
			ImageStack mask ) 
	{
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		float nonMaximaMarker = 0;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {

					// check mask
					if ( mask.getVoxel(x, y, z) == 0 )
						continue;

					// current value
					double value = result.getVoxel(x, y, z);
					if (value == nonMaximaMarker)
						continue;

					// maximum value in 6-neighborhood
					double maxVal = 0;
					if ( x > 0 && mask.getVoxel( x-1, y, z ) != 0 ) 
						maxVal = max(maxVal, stack.getVoxel(x-1, y, z)); 

					if (x < sizeX - 1 && mask.getVoxel( x+1, y, z ) != 0 ) 
						maxVal = max(maxVal, stack.getVoxel(x+1, y, z));

					if (y > 0 && mask.getVoxel( x, y-1, z ) != 0 )
						maxVal = max(maxVal, stack.getVoxel(x, y-1, z));

					if (y < sizeY - 1 && mask.getVoxel( x, y+1, z ) != 0 ) 
						maxVal = max(maxVal, stack.getVoxel(x, y+1, z));

					if (z > 0 && mask.getVoxel( x, y, z-1 ) != 0 ) 
						maxVal = max(maxVal, stack.getVoxel(x, y, z-1));

					if (z < sizeZ - 1 && mask.getVoxel( x, y, z+1 ) != 0 ) 
						maxVal = max(maxVal, stack.getVoxel(x, y, z+1)); 

					// if one of the neighbors has greater value, the local pixel 
					// is not a maxima. All connected pixels with same value are 
					// set to the marker for maxima.
					if (maxVal > value) {
						FloodFill.floodFillC6(result, x, y, z, nonMaximaMarker);
					}
				}
			}
		}

		// Ensure result is byte stack
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMaxima(result);

		return result;
	}


	/**
	 * Computes regional maxima in 3D image <code>stack</code>, using
	 * flood-filling-like algorithm with 6 connectivity.
	 */
	private final static ImageStack regionalMaximaFloatC6(ImageStack stack) {
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		float nonMaximaMarker = 0;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// check mask
					if ( stack.getVoxel(x, y, z) == 0 )
						continue;
					// current value
					double value = result.getVoxel(x, y, z);
					if (value == nonMaximaMarker)
						continue;

					// maximum value in 6-neighborhood
					double maxVal = 0;
					if (x > 0) 
						maxVal = max(maxVal, stack.getVoxel(x-1, y, z)); 
					if (x < sizeX - 1) 
						maxVal = max(maxVal, stack.getVoxel(x+1, y, z)); 
					if (y > 0) 
						maxVal = max(maxVal, stack.getVoxel(x, y-1, z)); 
					if (y < sizeY - 1) 
						maxVal = max(maxVal, stack.getVoxel(x, y+1, z));
					if (z > 0) 
						maxVal = max(maxVal, stack.getVoxel(x, y, z-1)); 
					if (z < sizeZ - 1) 
						maxVal = max(maxVal, stack.getVoxel(x, y, z+1)); 

					// if one of the neighbors has greater value, the local pixel 
					// is not a maxima. All connected pixels with same value are 
					// set to the marker for maxima.
					if (maxVal > value) {
						FloodFill.floodFillC6(result, x, y, z, nonMaximaMarker);
					}
				}
			}
		}

		// Ensure result is byte stack
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMaxima(result);

		return result;
	}


	/**
	 * Computes regional maxima in grayscale image <code>stack</code>, using
	 * flood-filling-like algorithm with 26 connectivity.
	 */
	private final static ImageStack regionalMaximaFloatC26(
			ImageStack stack,
			ImageStack mask ) 
	{
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		float nonMaximaMarker = 0;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {

					// check mask
					if ( mask.getVoxel(x, y, z) == 0 )
						continue;

					// current value
					double value = result.getVoxel(x, y, z);
					if (value == nonMaximaMarker)
						continue;

					// maximum value in 8-neighborhood
					double maxVal = 0;
					for (int z2 = max(z-1,0); z2 <= min(z+1, sizeZ-1); z2++) {
						for (int y2 = max(y-1,0); y2 <= min(y+1, sizeY-1); y2++) {
							for (int x2 = max(x-1,0); x2 <= min(x+1, sizeX-1); x2++) {
								if ( mask.getVoxel(x2, y2, z2) != 0 )									
									maxVal = max(maxVal, stack.getVoxel(x2, y2, z2));
							}
						}
					}

					// if one of the neighbors has greater value, the local pixel 
					// is not a maxima. All connected pixels with same value are 
					// set to the marker for maxima.
					if (maxVal > value) {
						FloodFill.floodFillC26(result, x, y, z, nonMaximaMarker);
					}
				}
			}
		}

		// Ensure result is byte stack
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMaxima(result);

		return result;					
	}

	/**
	 * Computes regional maxima in grayscale image <code>stack</code>, using
	 * flood-filling-like algorithm with 26 connectivity.
	 */
	private final static ImageStack regionalMaximaFloatC26(ImageStack stack) {
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		float nonMaximaMarker = 0;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// current value
					double value = result.getVoxel(x, y, z);
					if (value == nonMaximaMarker)
						continue;

					// maximum value in 8-neighborhood
					double maxVal = 0;
					for (int z2 = max(z-1,0); z2 <= min(z+1, sizeZ-1); z2++) {
						for (int y2 = max(y-1,0); y2 <= min(y+1, sizeY-1); y2++) {
							for (int x2 = max(x-1,0); x2 <= min(x+1, sizeX-1); x2++) {
								maxVal = max(maxVal, stack.getVoxel(x2, y2, z2));
							}
						}
					}

					// if one of the neighbors has greater value, the local pixel 
					// is not a maxima. All connected pixels with same value are 
					// set to the marker for maxima.
					if (maxVal > value) {
						FloodFill.floodFillC26(result, x, y, z, nonMaximaMarker);
					}
				}
			}
		}

		// Ensure result is byte stack
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMaxima(result);

		return result;					
	}

	/**
	 *  Transform an image of valued maxima, containing either the maxima value
	 *  or the min value for given image, into a binary image with values 0 for
	 *  non maxima pixels and 255 for regional maxima pixels.
	 */
	private final static void binariseMaxima(ImageStack stack) {
		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					if (stack.getVoxel(x, y, z) > 0)
						stack.setVoxel(x, y, z, 255);
				}
			}
		}
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

		GeodesicReconstruction3DAlgo algo = new GeodesicReconstructionByDilation3DGray8Scanning(conn);
		ImageStack rec = algo.applyTo(stack, mask);

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
	 * Computes the regional minima in grayscale image <code>stack</code>, 
	 * using the specified connectivity.
	 * @param conn the connectivity for minima, that should be either 6 or 26
	 */
	public final static ImageStack regionalMinima(ImageStack stack,
			int conn) {
		if (stack.getBitDepth() == 32)
			return regionalMinimaFloat(stack, conn);
		else
			return regionalMinimaInt(stack, conn);
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
		if (stack.getBitDepth() == 32)
			return regionalMinimaFloat (stack, conn, mask );
		else
			return regionalMinimaInt( stack, conn, mask );
	}


	/**
	 * Dispatching method depending on specified connectivity for integer images.
	 */
	private final static ImageStack regionalMinimaInt(ImageStack image,
			int conn) {
		switch (conn) {
		case 6:
			return regionalMinimaIntC6(image);
		case 26:
			return regionalMinimaIntC26(image);
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}

	/**
	 * Dispatching method depending on specified connectivity for integer images.
	 */
	private final static ImageStack regionalMinimaInt(
			ImageStack image,
			int conn,
			ImageStack mask ) 
	{
		switch (conn) {
		case 6:
			return regionalMinimaIntC6( image, mask );
		case 26:
			return regionalMinimaIntC26( image, mask );
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}


	/**
	 * Dispatching method depending on specified connectivity for float images.
	 */
	private final static ImageStack regionalMinimaFloat(ImageStack image,
			int conn) {
		switch (conn) {
		case 6:
			return regionalMinimaFloatC6(image);
		case 26:
			return regionalMinimaFloatC26(image);
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}

	/**
	 * Dispatching method depending on specified connectivity for float images.
	 */
	private final static ImageStack regionalMinimaFloat(
			ImageStack image,
			int conn,
			ImageStack mask ) 
	{
		switch (conn) {
		case 6:
			return regionalMinimaFloatC6( image, mask );
		case 26:
			return regionalMinimaFloatC26( image, mask );
		default:
			throw new IllegalArgumentException(
					"Unknown connectivity argument: " + conn);
		}
	}

	/**
	 * Computes regional minima in grayscale image <code>image</code>, using
	 * flood-filling-like algorithm with 4 connectivity.
	 */
	private final static ImageStack regionalMinimaIntC6(ImageStack stack) {
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		int nonMinimaMarker;
		switch (stack.getBitDepth()) {
		case 8: nonMinimaMarker = 255; break;
		case 16: nonMinimaMarker = (int)(0x0FFFF); break;
		default:
			throw new IllegalArgumentException("Requires 8- or 16-bits stack as input");
		}

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// current value
					int value = (int) result.getVoxel(x, y, z);
					if (value == nonMinimaMarker)
						continue;

					// maximum value in 6-neighborhood
					int minVal = value;
					if (x > 0) 
						minVal = min(minVal, (int)stack.getVoxel(x-1, y, z)); 
					if (x < sizeX - 1) 
						minVal = min(minVal, (int)stack.getVoxel(x+1, y, z)); 
					if (y > 0) 
						minVal = min(minVal, (int)stack.getVoxel(x, y-1, z)); 
					if (y < sizeY - 1) 
						minVal = min(minVal, (int)stack.getVoxel(x, y+1, z));
					if (z > 0) 
						minVal = min(minVal, (int)stack.getVoxel(x, y, z-1)); 
					if (z < sizeZ - 1) 
						minVal = min(minVal, (int)stack.getVoxel(x, y, z+1)); 

					// if one of the neighbors has lower value, the local pixel 
					// is not a minimum. All connected pixels with same value are 
					// set to the marker for non minimum.
					if (minVal < value) {
						FloodFill.floodFillC6(result, x, y, z, nonMinimaMarker);
					}
				}
			}
		}

		// Convert result to binary ByteProcessor
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMinima(result);

		return result;
	}

	/**
	 * Computes regional minima in grayscale image <code>image</code>, using
	 * flood-filling-like algorithm with 4 connectivity.
	 */
	private final static ImageStack regionalMinimaIntC6(
			ImageStack stack,
			ImageStack mask ) 
	{
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		int nonMinimaMarker;
		switch (stack.getBitDepth()) {
		case 8: nonMinimaMarker = 255; break;
		case 16: nonMinimaMarker = (int)(0x0FFFF); break;
		default:
			throw new IllegalArgumentException("Requires 8- or 16-bits stack as input");
		}

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// check mask
					if ( mask.getVoxel( x, y, z ) == 0 )
						continue;
					// current value
					int value = (int) result.getVoxel(x, y, z);
					if (value == nonMinimaMarker)
						continue;

					// maximum value in 6-neighborhood
					int minVal = value;
					if (x > 0 && mask.getVoxel( x-1, y, z ) != 0 )
						minVal = min(minVal, (int)stack.getVoxel(x-1, y, z)); 

					if (x < sizeX - 1 && mask.getVoxel( x+1, y, z ) != 0 ) 
						minVal = min(minVal, (int)stack.getVoxel(x+1, y, z));

					if (y > 0 && mask.getVoxel( x, y-1, z ) != 0 ) 
						minVal = min(minVal, (int)stack.getVoxel(x, y-1, z)); 

					if (y < sizeY - 1 && mask.getVoxel( x, y+1, z ) != 0 )
						minVal = min(minVal, (int)stack.getVoxel(x, y+1, z));

					if (z > 0 && mask.getVoxel( x, y, z-1 ) != 0 )
						minVal = min(minVal, (int)stack.getVoxel(x, y, z-1));

					if (z < sizeZ - 1 && mask.getVoxel( x, y, z+1 ) != 0 )
						minVal = min(minVal, (int)stack.getVoxel(x, y, z+1)); 

					// if one of the neighbors has greater value, the local pixel 
					// is not a maxima. All connected pixels with same value are 
					// set to the marker for maxima.
					if (minVal < value) {
						FloodFill.floodFillC6(result, x, y, z, nonMinimaMarker);
					}
				}
			}
		}

		// Convert result to binary ByteProcessor
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMinima(result);

		return result;
	}


	/**
	 * Computes regional minima in grayscale image <code>image</code>, using
	 * flood-filling-like algorithm with 8 connectivity.
	 */
	private final static ImageStack regionalMinimaIntC26(ImageStack stack) {
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		int nonMinimaMarker;
		switch (stack.getBitDepth()) {
		case 8: nonMinimaMarker = 255; break;
		case 16: nonMinimaMarker = (int)(0x0FFFF); break;
		default:
			throw new IllegalArgumentException("Requires 8- or 16-bits stack as input");
		}

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// current value
					int value = (int) result.getVoxel(x, y, z);
					if (value == nonMinimaMarker)
						continue;

					// minimum value in 26-neighborhood
					int minVal = value;
					for (int z2 = max(z-1,0); z2 <= min(z+1, sizeZ-1); z2++) {
						for (int y2 = max(y-1,0); y2 <= min(y+1, sizeY-1); y2++) {
							for (int x2 = max(x-1,0); x2 <= min(x+1, sizeX-1); x2++) {
								minVal = min(minVal, (int) stack.getVoxel(x2, y2, z2));
							}
						}
					}

					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (minVal < value) {
						FloodFill.floodFillC26(result, x, y, z, nonMinimaMarker);
					}
				}
			}
		}

		// Convert result to binary ByteProcessor
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMinima(result);

		return result;
	}

	/**
	 * Computes regional minima in grayscale image <code>image</code>, using
	 * flood-filling-like algorithm with 8 connectivity.
	 */
	private final static ImageStack regionalMinimaIntC26(
			ImageStack stack,
			ImageStack mask ) 
	{
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		int nonMinimaMarker;
		switch (stack.getBitDepth()) {
		case 8: nonMinimaMarker = 255; break;
		case 16: nonMinimaMarker = (int)(0x0FFFF); break;
		default:
			throw new IllegalArgumentException("Requires 8- or 16-bits stack as input");
		}

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// check mask
					if ( mask.getVoxel( x, y, z ) == 0 )
						continue;
					// current value
					int value = (int) result.getVoxel(x, y, z);
					if (value == nonMinimaMarker)
						continue;

					// minimum value in 26-neighborhood
					int minVal = value;
					for (int z2 = max(z-1,0); z2 <= min(z+1, sizeZ-1); z2++) {
						for (int y2 = max(y-1,0); y2 <= min(y+1, sizeY-1); y2++) {
							for (int x2 = max(x-1,0); x2 <= min(x+1, sizeX-1); x2++) {
								// check mask
								if ( mask.getVoxel( x2, y2, z2 ) != 0 )
									minVal = min(minVal, (int) stack.getVoxel(x2, y2, z2));
							}
						}
					}

					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (minVal < value) {
						FloodFill.floodFillC26(result, x, y, z, nonMinimaMarker);
					}
				}
			}
		}

		// Convert result to binary ByteProcessor
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMinima(result);

		return result;
	}


	/**
	 * Computes regional minima in float image <code>image</code>, using
	 * flood-filling-like algorithm with 4 connectivity.
	 */
	private final static ImageStack regionalMinimaFloatC6(ImageStack stack) {
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		float nonMinimaMarker = Float.MAX_VALUE;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// current value
					double value = result.getVoxel(x, y, z);
					if (value == nonMinimaMarker)
						continue;

					// maximum value in 4-neighborhood
					double minVal = value;
					if (x > 0) 
						minVal = min(minVal, stack.getVoxel(x-1, y, z)); 
					if (x < sizeX - 1) 
						minVal = min(minVal, stack.getVoxel(x+1, y, z)); 
					if (y > 0) 
						minVal = min(minVal, stack.getVoxel(x, y-1, z)); 
					if (y < sizeY - 1) 
						minVal = min(minVal, stack.getVoxel(x, y+1, z));
					if (z > 0) 
						minVal = min(minVal, stack.getVoxel(x, y, z-1)); 
					if (z < sizeZ - 1) 
						minVal = min(minVal, stack.getVoxel(x, y, z+1)); 

					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (minVal < value) {
						FloodFill.floodFillC6(result, x, y, z, nonMinimaMarker);
					}
				}
			}
		}		
		// Convert result to binary ByteProcessor
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMinima(result);

		return result;
	}

	/**
	 * Computes regional minima in float image <code>image</code>, using
	 * flood-filling-like algorithm with 4 connectivity.
	 */
	private final static ImageStack regionalMinimaFloatC6(
			ImageStack stack,
			ImageStack mask ) 
	{
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		float nonMinimaMarker = Float.MAX_VALUE;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// check mask
					if ( mask.getVoxel( x, y, z ) == 0 )
						continue;
					// current value
					double value = result.getVoxel(x, y, z);
					if (value == nonMinimaMarker)
						continue;

					// maximum value in 4-neighborhood
					double minVal = value;
					if (x > 0 && mask.getVoxel( x-1, y, z ) != 0 )
						minVal = min(minVal, stack.getVoxel(x-1, y, z)); 

					if (x < sizeX - 1 && mask.getVoxel( x+1, y, z ) != 0 ) 
						minVal = min(minVal, stack.getVoxel(x+1, y, z));

					if (y > 0 && mask.getVoxel( x, y-1, z ) != 0 ) 
						minVal = min(minVal, stack.getVoxel(x, y-1, z)); 

					if (y < sizeY - 1 && mask.getVoxel( x, y+1, z ) != 0 )
						minVal = min(minVal, stack.getVoxel(x, y+1, z));

					if (z > 0 && mask.getVoxel( x, y, z-1 ) != 0 )
						minVal = min(minVal, stack.getVoxel(x, y, z-1));

					if (z < sizeZ - 1 && mask.getVoxel( x, y, z+1 ) != 0 )
						minVal = min(minVal, stack.getVoxel(x, y, z+1)); 

					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (minVal < value) {
						FloodFill.floodFillC6(result, x, y, z, nonMinimaMarker);
					}
				}
			}
		}		
		// Convert result to binary ByteProcessor
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMinima(result);

		return result;
	}

	/**
	 * Computes regional minima in float image <code>image</code>, using
	 * flood-filling-like algorithm with 8 connectivity.
	 */
	private final static ImageStack regionalMinimaFloatC26(ImageStack stack) {
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		float nonMinimaMarker = Float.MAX_VALUE;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// current value
					double value = result.getVoxel(x, y, z);

					if (value == nonMinimaMarker)
						continue;

					// minimum value in 26-neighborhood
					double minVal = 0;
					for (int z2 = max(z-1,0); z2 <= min(z+1, sizeZ-1); z2++) {
						for (int y2 = max(y-1,0); y2 <= min(y+1, sizeY-1); y2++) {
							for (int x2 = max(x-1,0); x2 <= min(x+1, sizeX-1); x2++) {
								minVal = min(minVal, stack.getVoxel(x2, y2, z2));
							}
						}
					}

					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (minVal < value) {
						FloodFill.floodFillC26(result, x, y, z, nonMinimaMarker);
					}
				}
			}
		}

		// Convert result to binary ByteProcessor
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMinima(result);

		return result;
	}

	/**
	 * Computes regional minima in float image <code>image</code>, using
	 * flood-filling-like algorithm with 8 connectivity.
	 */
	private final static ImageStack regionalMinimaFloatC26(
			ImageStack stack,
			ImageStack mask ) 
	{
		ImageStack result = stack.duplicate();

		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		float nonMinimaMarker = Float.MAX_VALUE;

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// check mask
					if ( mask.getVoxel( x, y, z ) == 0 )
						continue;
					// current value
					double value = result.getVoxel(x, y, z);

					if (value == nonMinimaMarker)
						continue;

					// minimum value in 26-neighborhood
					double minVal = 0;
					for (int z2 = max(z-1,0); z2 <= min(z+1, sizeZ-1); z2++) {
						for (int y2 = max(y-1,0); y2 <= min(y+1, sizeY-1); y2++) {
							for (int x2 = max(x-1,0); x2 <= min(x+1, sizeX-1); x2++) {
								// check mask
								if ( mask.getVoxel( x2, y2, z2 ) != 0 )
									minVal = min(minVal, stack.getVoxel(x2, y2, z2));
							}
						}
					}

					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (minVal < value) {
						FloodFill.floodFillC26(result, x, y, z, nonMinimaMarker);
					}
				}
			}
		}

		// Convert result to binary ByteProcessor
		if (result.getBitDepth() != 8)
			result = convertToByteStack(result);
		binariseMinima(result);

		return result;
	}

	/**
	 *  Transform an image of valued minima, containing either the minima value
	 *  or the max value for given image, into a binary image with values 0 for
	 *  non minima pixels and 255 for regional minima pixels.
	 */
	private final static void binariseMinima(ImageStack stack) {
		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();

		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					if (stack.getVoxel(x, y, z) == 255)
						stack.setVoxel(x, y, z, 0);
					else
						stack.setVoxel(x, y, z, 255);
				}
			}
		}
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

		GeodesicReconstruction3DAlgo algo = new GeodesicReconstructionByErosion3DGray8Scanning(conn);
		ImageStack rec = algo.applyTo(marker, stack);

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

		GeodesicReconstruction3DAlgo algo = new GeodesicReconstructionByErosion3DGray8Scanning(conn);
		ImageStack rec = algo.applyTo(marker, stack);
		//		ImageStack rec = GeodesicReconstruction3D.reconstructByErosion(marker, stack, conn);

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

	private final static ImageStack convertToByteStack(ImageStack stack) {
		if (stack.getBitDepth() == 8)
			return stack;
		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					result.setVoxel(x, y, z, max(min(stack.getVoxel(x, y, z), 255), 0));
				}
			}
		}
		return result;
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
