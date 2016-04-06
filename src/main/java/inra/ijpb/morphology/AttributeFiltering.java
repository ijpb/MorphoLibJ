/**
 * 
 */
package inra.ijpb.morphology;

import ij.IJ;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.segment.Threshold;

/**
 * Several static methods for computation of attribute filtering (opening,
 * thinning...) on gray level images.
 * 
 * @author dlegland
 *
 */
public class AttributeFiltering
{
	public static final ByteProcessor areaOpening(ImageProcessor image, int minArea)
	{
		IJ.showStatus("Initialize");
		
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		ByteProcessor result = new ByteProcessor(sizeX, sizeY);
		
		IJ.showStatus("Compute thresholds");
		for (int level = 1; level <= 255; level++)
		{
			IJ.showProgress(level - 1, 255);
			IJ.showStatus("Thresholds: " + level);
			
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
		IJ.showProgress(1, 1);
		
		return result;
	}
}
