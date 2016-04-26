/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.segment.Threshold;

/**
 * Computes area opening using naive algorithm. Iterate over the list of
 * thresholds, compute binary image, apply binary area opening, and concatenate
 * the results.
 * 
 * @author dlegland
 */
public class AreaOpeningNaive extends AlgoStub implements AreaOpening
{

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.attrfilt.AreaOpening#process(ij.process.ImageProcessor, int)
	 */
	@Override
	public ImageProcessor process(ImageProcessor image, int minArea)
	{
		fireStatusChanged(this, "Initialize");
		
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		ByteProcessor result = new ByteProcessor(sizeX, sizeY);
		
		fireStatusChanged(this, "Compute thesholds");
		for (int level = 1; level <= 255; level++)
		{
			fireStatusChanged(this, "Threshold: " + level);
			fireProgressChanged(this, level-1, 255);
			
			// threshold
			ImageProcessor binary = Threshold.threshold(image, level, 255);
			
			// keep only components with size larger than minArea
			binary = BinaryImages.areaOpening(binary, minArea);
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					if (binary.get(x, y) > 0)
					{
						result.set(x, y, level);
					}
				}
			}
		}
		
		fireStatusChanged(this, "");
		fireProgressChanged(this, 1, 1);
		
		return result;
	}

}
