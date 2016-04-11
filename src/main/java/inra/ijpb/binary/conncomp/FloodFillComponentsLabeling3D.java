/**
 * 
 */
package inra.ijpb.binary.conncomp;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.morphology.FloodFill3D;

/**
 * Computes the labels of the connected components in a 3D binary image. The
 * type of result is controlled by the bitDepth option.
 * 
 * Uses a Flood-fill type algorithm. The image voxels are iterated, and each
 * time a foreground voxel not yet associated with a label is encountered, its
 * connected component is associated with a new label.
 * 
 * @see inra.ijpb.morphology.FloodFill3D
 * 
 * @author dlegland
 */
public class FloodFillComponentsLabeling3D extends AlgoStub implements
		ConnectedComponentsLabeling3D
{
	/** 
	 * The connectivity of the components, either 6 (default) or 26.
	 */
	int connectivity = 6;
	
	/**
	 * The number of bits for representing the result label image. Can be 8, 16
	 * (default), or 32.
	 */
	int bitDepth = 16;
	
	/**
	 * Constructor with default connectivity 6 and default output bitdepth equal to 16.  
	 */
	public FloodFillComponentsLabeling3D()
	{
	}
	
	/**
	 * Constructor specifying the connectivity and using default output bitdepth equal to 16.  
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (6 or 26)
	 */
	public FloodFillComponentsLabeling3D(int connectivity)
	{
		this.connectivity = connectivity;
	}
	
	/**
	 * Constructor specifying the connectivity and the bitdepth of result label
	 * image
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (6 or 26)
	 * @param bitDepth
	 *            the bit depth of the result (8, 16, or 32)
	 */
	public FloodFillComponentsLabeling3D(int connectivity, int bitDepth)
	{
		this.bitDepth = bitDepth;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.binary.conncomp.ConnectedComponentsLabeling3D#computeLabels(ij.ImageStack)
	 */
	@Override
	public ImageStack computeLabels(ImageStack image)
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// initialize result image
		fireStatusChanged(this, "Allocate memory...");
		ImageStack labels = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);

		// identify the maximum label index
		int maxLabel;
		switch (this.bitDepth) {
		case 8: 
			maxLabel = 255;
			break; 
		case 16: 
			maxLabel = 65535;
			break;
		case 32:
			maxLabel = 0x01 << 23;
			break;
		default:
			throw new IllegalArgumentException(
					"Bit Depth should be 8, 16 or 32.");
		}

		fireStatusChanged(this, "Compute Labels...");
		
		// Iterate over image voxels. 
		// Each time a white voxel not yet associated
		// with a label is encountered, uses flood-fill to associate its
		// connected component to a new label
		int nLabels = 0;
		for (int z = 0; z < sizeZ; z++) 
		{
			fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					// Do not process background voxels
					if (image.getVoxel(x, y, z) == 0)
						continue;

					// Do not process voxels already labeled
					if (labels.getVoxel(x, y, z) > 0)
						continue;

					// a new label is found: check current label number  
					if (nLabels == maxLabel)
					{
						throw new RuntimeException("Max number of label reached (" + maxLabel + ")");
					}
					
					// increment label index, and propagate
					nLabels++;
					FloodFill3D.floodFillFloat(image, x, y, z, labels, nLabels, this.connectivity);
				}
			}
		}
		
		fireStatusChanged(this, "");
		fireProgressChanged(this, 1, 1);
		return labels;
	}

}
