/**
 * 
 */
package inra.ijpb.measure.region2d;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class LargestInscribedCircleTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.LargestInscribedCircle#compute(ij.ImagePlus)}.
	 */
	@Test
	public final void testComputeImagePlus_Rectangles()
	{
		ImageProcessor image = new ByteProcessor(14, 9);
		image.set(1, 1, 1);
		fillRect(image, 3, 4, 1, 2, 2);		// radius 2
		fillRect(image, 1, 1+3, 4, 4+3, 3); // radius 4
		fillRect(image, 6, 6+6, 1, 1+6, 4); // radius 7
		
		ResultsTable table = LargestInscribedCircle.asTable(LargestInscribedCircle.compute(image, new Calibration()));
		
		assertEquals(1, table.getValue("InscrCircle.Radius", 0), .1);
		assertEquals(1, table.getValue("InscrCircle.Radius", 1), .1);
		assertEquals(2, table.getValue("InscrCircle.Radius", 2), .1);
		assertEquals(4, table.getValue("InscrCircle.Radius", 3), .1);
	}
	
	private static final void fillRect(ImageProcessor image, int xmin,
			int xmax, int ymin, int ymax, double value)
	{
		for (int y = ymin; y <= ymax; y++)
		{
			for (int x = xmin; x <= xmax; x++) 
			{
				image.setf(x, y, (float) value);
			}
		}
		
	}
}
