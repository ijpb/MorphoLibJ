/**
 * 
 */
package inra.ijpb.binary;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.morphology.FloodFill;

/**
 * Several static methods for computing connected components iin binary images. 
 *
 */
public class ConnectedComponents {
	/**
	 * Same algorithm as computeLabels(ImageStack,int,int), but operates on an 
	 * ImagePlus, making it possible to set up min and max values of final
	 * image display.
	 * 
	 * @param imagePlus contains the 3D binary image stack
	 * @param conn the connectivity, either 6 or 26
	 * @param bitDepth the number of bits used to create the result stack (8, 16 or 32)
	 * @return a 3D ImageStack containing the label of each connected component.
	 */
	public final static ImagePlus computeLabels(ImagePlus imagePlus, int conn, int bitDepth) {
		// get 3D image stack
		ImageStack image = imagePlus.getStack();
		
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		IJ.showStatus("Allocate Memory");
		ImageStack labels = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);

		int nLabels = 0;
		
		IJ.showStatus("Compute Labels...");
		for (int z = 0; z < sizeZ; z++) {
			IJ.showProgress(z, sizeZ);
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// Do not process background voxels
					if (image.getVoxel(x, y, z) == 0)
						continue;
					
					// Do not process voxels already labeled
					if (labels.getVoxel(x, y, z) > 0)
						continue;

					// a new label is found: increment label index, and propagate 
					nLabels++;
					FloodFill.floodFillFloat(image, x, y, z, labels, nLabels, conn);
				}
			}
		}
		IJ.showProgress(1);
		
		ImagePlus labelPlus = new ImagePlus("Labels", labels);
		labelPlus.setDisplayRange(0, nLabels);
		return labelPlus;
	}

	public final static ImageProcessor computeLabels(ImageProcessor image, int conn, int bitDepth) {
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();

		ImageProcessor labels;
		switch (bitDepth) {
		case 8: labels = new ByteProcessor(width, height); break; 
		case 16: labels = new ShortProcessor(width, height); break; 
		case 32: labels = new FloatProcessor(width, height); break;
		default: throw new IllegalArgumentException("Bit Depth should be 8, 16 or 32.");
		}

		// the label counter
		int nLabels = 0;

		// iterate on image pixels to fin new regions
		for (int y = 0; y < height; y++) {
			IJ.showProgress(y, height);
			for (int x = 0; x < width; x++) {
				if (image.get(x, y) == 0)
					continue;
				if (labels.get(x, y) > 0)
					continue;

				nLabels++;
				FloodFill.floodFillFloat(image, x, y, labels, nLabels, conn);
			}
		}
		IJ.showProgress(1);

		labels.setMinAndMax(0, nLabels);
		return labels;
	}

	public final static ImageStack computeLabels(ImageStack image, int conn, int bitDepth) {
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		IJ.showStatus("Allocate Memory");
		ImageStack labels = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);

		int nLabels = 0;

		IJ.showStatus("Compute Labels...");
		for (int z = 0; z < sizeZ; z++) {
			IJ.showProgress(z, sizeZ);
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// Do not process background voxels
					if (image.getVoxel(x, y, z) == 0)
						continue;

					// Do not process voxels already labeled
					if (labels.getVoxel(x, y, z) > 0)
						continue;

					// a new label is found: increment label index, and propagate 
					nLabels++;
					FloodFill.floodFillFloat(image, x, y, z, labels, nLabels, conn);
				}
			}
		}
		IJ.showProgress(1);
		return labels;
	}

}
