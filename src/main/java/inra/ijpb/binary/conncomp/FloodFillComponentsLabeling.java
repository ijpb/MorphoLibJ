/**
 * 
 */
package inra.ijpb.binary.conncomp;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.morphology.FloodFill;

/**
 * Computes the labels of the connected components in a binary image. The type
 * of result is controlled by the bitDepth option.
 * 
 * Uses a Flood-fill type algorithm. The image pixels are iterated, and each
 * time a foreground pixel not yet associated with a label is encountered, its
 * connected component is associated with a new label.
 *
 * @see inra.ijpb.morphology.FloodFill
 * @author dlegland
 *
 */
public class FloodFillComponentsLabeling extends AlgoStub implements
		ConnectedComponentsLabeling
{
	/** 
	 * The connectivity of the components, either 4 (default) or 8.
	 */
	int connectivity = 4;

	/**
	 * The number of bits for representing the result label image. Can be 8, 16
	 * (default), or 32.
	 */
	int bitDepth = 16;
	
	/**
	 * Constructor with default connectivity 4 and default output bitdepth equal to 16.  
	 */
	public FloodFillComponentsLabeling()
	{
	}
	
	/**
	 * Constructor specifying the connectivity and using default output bitdepth equal to 16.  
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (4 or 8)
	 */
	public FloodFillComponentsLabeling(int connectivity)
	{
		this.connectivity = connectivity;
	}
	
	/**
	 * Constructor specifying the connectivity and the bitdepth of result label
	 * image
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (4 or 8)
	 * @param bitDepth
	 *            the bit depth of the result (8, 16, or 32)
	 */
	public FloodFillComponentsLabeling(int connectivity, int bitDepth)
	{
		this.connectivity = connectivity;
		this.bitDepth = bitDepth;
	}
	
	/* (non-Javadoc)
	 * @see inra.ijpb.binary.conncomp.ConnectedComponentsLabeling#computeLabels(ij.process.ImageProcessor)
	 */
	@Override
	public ImageProcessor computeLabels(ImageProcessor image)
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		int maxLabel;

		// Depending on bitDepth, create result image, and choose max label 
		// number
		ImageProcessor labels;
		switch (this.bitDepth) {
		case 8: 
			labels = new ByteProcessor(width, height);
			maxLabel = 255;
			break; 
		case 16: 
			labels = new ShortProcessor(width, height);
			maxLabel = 65535;
			break;
		case 32:
			labels = new FloatProcessor(width, height);
			maxLabel = 0x01 << 23 - 1;
			break;
		default:
			throw new IllegalArgumentException(
					"Bit Depth should be 8, 16 or 32.");
		}

		// the label counter
		int nLabels = 0;

		// iterate on image pixels to fin new regions
		for (int y = 0; y < height; y++) 
		{
			this.fireProgressChanged(this, y, height);
			for (int x = 0; x < width; x++) 
			{
				if (image.get(x, y) == 0)
					continue;
				if (labels.get(x, y) > 0)
					continue;

				// a new label is found: check current label number  
				if (nLabels == maxLabel)
				{
					throw new RuntimeException("Max number of label reached (" + maxLabel + ")");
				}
				
				// increment label index, and propagate
				nLabels++;
				FloodFill.floodFillFloat(image, x, y, labels, nLabels, this.connectivity);
			}
		}
		this.fireProgressChanged(this, 1, 1);

		labels.setMinAndMax(0, nLabels);
		return labels;
	}

}
