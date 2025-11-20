/**
 * 
 */
package inra.ijpb.binary;

import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;

/**
 * Utility class that inverts a binary image.
 * 
 * @author dlegland
 *
 */
public class BinaryInverter extends AlgoStub
{
	/**
	 * Inverts the specified binary stack by converting each value within the
	 * stack.
	 * 
	 * @param image
	 *            the image to invert.
	 */
	public void processInPlace(ImageStack image)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int nSlices = image.getSize();
		
		for (int z = 0; z < nSlices; z++)
		{
			this.fireProgressChanged(this, z, nSlices);
			
			ImageProcessor ip = image.getProcessor(z + 1);
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					ip.set(x, y, ip.get( x, y ) > 0 ? 0 : 255);
				}
			}
		}

		this.fireProgressChanged(this, 1, 1);
	}

}
