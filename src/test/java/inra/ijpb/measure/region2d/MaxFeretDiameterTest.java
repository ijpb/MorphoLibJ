/**
 * 
 */
package inra.ijpb.measure.region2d;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import inra.ijpb.geometry.PointPair2D;

/**
 * @author dlegland
 *
 */
public class MaxFeretDiameterTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.MaxFeretDiameter#process(ij.ImagePlus)}.
	 */
	@Test
	public void testProcess_circles()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
	
		MaxFeretDiameter algo = new MaxFeretDiameter();
		
		Map<Integer, PointPair2D> maxFeretDiams = algo.process(imagePlus);

		assertEquals(1, maxFeretDiams.size());
		
		PointPair2D diam = maxFeretDiams.get(255);
		
		assertEquals(272.7, diam.diameter(), .2);
		
		ResultsTable table = MaxFeretDiameter.asTable(maxFeretDiams);
		assertEquals(1, table.size());
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.MaxFeretDiameter#process(ij.ImagePlus)}.
	 */
	@Test
	public void testProcess_riceGrains()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-med-WTH-lbl.tif").getFile());
	
		MaxFeretDiameter algo = new MaxFeretDiameter();
		Map<Integer, PointPair2D> maxFeretDiams = algo.process(imagePlus);
		
		assertEquals(96, maxFeretDiams.size());
		
		ResultsTable table = MaxFeretDiameter.asTable(maxFeretDiams);
		assertEquals(96, table.size());
	}
}
