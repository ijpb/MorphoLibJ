package inra.ijpb.morphology.extrema;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.ImageStack;
import inra.ijpb.morphology.FloodFill;

public class RegionalExtremaByFlooding3D extends RegionalExtremaAlgo3D {

	@Override
	public ImageStack applyTo(ImageStack inputImage) {
		switch (this.connectivity) {
		case 6:
			return regionalExtremaFloatC6(inputImage);
		case 26:
			return regionalExtremaFloatC26(inputImage);
		default:
			throw new IllegalArgumentException(
					"Connectivity must be either 6 or 26, not "
							+ this.connectivity);
		}
	}

	/**
	 * Computes regional minima in float image <code>image</code>, using
	 * flood-filling-like algorithm with 4 connectivity.
	 */
	private ImageStack regionalExtremaFloatC6(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// create binary image for result, filled with 255.
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		fillStack(result, 255);

		// initialize local data depending on extrema type
		final int sign = this.extremaType == ExtremaType.MINIMA ? 1 : -1;

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// Check if current voxel was already processed
					if (result.getVoxel(x, y, z) == 0)
						continue;
					
					// current value
					double currentValue = image.getVoxel(x, y, z) * sign;
					
					// compute extremum value in 6-neighborhood
					double value = currentValue;
					if (x > 0) 
						value = min(value, image.getVoxel(x-1, y, z) * sign); 
					if (x < sizeX - 1) 
						value = min(value, image.getVoxel(x+1, y, z) * sign); 
					if (y > 0) 
						value = min(value, image.getVoxel(x, y-1, z) * sign); 
					if (y < sizeY - 1) 
						value = min(value, image.getVoxel(x, y+1, z) * sign);
					if (z > 0) 
						value = min(value, image.getVoxel(x, y, z-1) * sign); 
					if (z < sizeZ - 1) 
						value = min(value, image.getVoxel(x, y, z+1) * sign); 

					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (value < currentValue) {
						FloodFill.floodFillFloat(image, x, y, z, result, 0, 6);
					}
				}
			}
		}		

		return result;
	}

	private ImageStack regionalExtremaFloatC26(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// create binary image for result, filled with 255.
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		fillStack(result, 255);

		// initialize local data depending on extrema type
		final int sign = this.extremaType == ExtremaType.MINIMA ? 1 : -1;

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// Check if current voxel was already processed
					if (result.getVoxel(x, y, z) == 0)
						continue;
					
					// current value
					double currentValue = image.getVoxel(x, y, z);
					
					// compute extremum value in 26-neighborhood
					double value = currentValue * sign;
					for (int z2 = max(z-1, 0); z2 <= min(z+1, sizeZ-1); z2++) {
						for (int y2 = max(y-1, 0); y2 <= min(y+1, sizeY-1); y2++) {
							for (int x2 = max(x-1, 0); x2 <= min(x+1, sizeX-1); x2++) {
								value = min(value, image.getVoxel(x2, y2, z2) * sign);
							}
						}
					}
					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (value < currentValue * sign) {
						FloodFill.floodFillFloat(image, x, y, z, result, 0, 26);
					}
				}
			}
		}		

		return result;
	}
	
	@Override
	public ImageStack applyTo(ImageStack inputImage, ImageStack maskImage) {
		switch (this.connectivity) {
		case 6:
			return regionalExtremaFloatC6(inputImage, maskImage);
		case 26:
			return regionalExtremaFloatC26(inputImage, maskImage);
		default:
			throw new IllegalArgumentException(
					"Connectivity must be either 6 or 26, not "
							+ this.connectivity);
		}
	}
	
	/**
	 * Computes regional minima in float image <code>image</code>, using
	 * flood-filling-like algorithm with 4 connectivity.
	 */
	private ImageStack regionalExtremaFloatC6(ImageStack image, ImageStack mask) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// create binary image for result, filled with 255.
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		fillStack(result, 255);

		// initialize local data depending on extrema type
		final int sign = this.extremaType == ExtremaType.MINIMA ? 1 : -1;

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// Check if the voxel is in the binary mask
					if (mask.getVoxel(x, y, z) == 0)
						continue;

					// Check if current voxel was already processed
					if (result.getVoxel(x, y, z) == 0)
						continue;
					
					// current value
					double currentValue = image.getVoxel(x, y, z) * sign;
					
					// compute extremum value in 6-neighborhood
					double value = currentValue;
					if (x > 0 && mask.getVoxel(x - 1, y, z) != 0)
						value = min(value, image.getVoxel(x - 1, y, z) * sign);
					
					if (x < sizeX - 1 && mask.getVoxel(x + 1, y, z) != 0)
						value = min(value, image.getVoxel(x + 1, y, z) * sign);
					
					if (y > 0 && mask.getVoxel(x, y - 1, z) != 0)
						value = min(value, image.getVoxel(x, y - 1, z) * sign);
					
					if (y < sizeY - 1 && mask.getVoxel(x, y + 1, z) != 0)
						value = min(value, image.getVoxel(x, y + 1, z) * sign);
					
					if (z > 0 && mask.getVoxel(x, y, z - 1) != 0)
						value = min(value, image.getVoxel(x, y, z - 1) * sign);
					
					if (z < sizeZ - 1 && mask.getVoxel(x, y, z + 1) != 0)
						value = min(value, image.getVoxel(x, y, z + 1) * sign);

					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (value < currentValue) {
						FloodFill.floodFillFloat(image, x, y, z, result, 0, 6);
					}
				}
			}
		}		

		return result;
	}

	private ImageStack regionalExtremaFloatC26(ImageStack image, ImageStack mask) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// create binary image for result, filled with 255.
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		fillStack(result, 255);

		// initialize local data depending on extrema type
		final int sign = this.extremaType == ExtremaType.MINIMA ? 1 : -1;

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// Check if the voxel is in the binary mask
					if (mask.getVoxel(x, y, z) == 0)
						continue;

					// Check if current voxel was already processed
					if (result.getVoxel(x, y, z) == 0)
						continue;
					
					// current value
					double currentValue = image.getVoxel(x, y, z);
					
					// compute extremum value in 26-neighborhood
					double value = currentValue * sign;
					for (int z2 = max(z-1, 0); z2 <= min(z+1, sizeZ-1); z2++) {
						for (int y2 = max(y-1, 0); y2 <= min(y+1, sizeY-1); y2++) {
							for (int x2 = max(x-1, 0); x2 <= min(x+1, sizeX-1); x2++) {
								if (mask.getVoxel(x2, y2, z2) != 0)
									value = min(value, image.getVoxel(x2, y2, z2) * sign);
							}
						}
					}
					// if one of the neighbors has lower value, the local pixel 
					// is not a minima. All connected pixels with same value are 
					// set to the marker for non-minima.
					if (value < currentValue * sign) {
						FloodFill.floodFillFloat(image, x, y, z, result, 0, 26);
					}
				}
			}
		}		

		return result;
	}
	
	private void fillStack(ImageStack image, double value) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					image.setVoxel(x,  y,  z,  value);
				}
			}
		}
	}
}
