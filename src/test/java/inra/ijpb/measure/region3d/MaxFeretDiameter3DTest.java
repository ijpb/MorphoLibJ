/**
 * 
 */
package inra.ijpb.measure.region3d;

import static org.junit.Assert.assertEquals;
import ij.ImageStack;
import ij.measure.Calibration;
import inra.ijpb.geometry.PointPair3D;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class MaxFeretDiameter3DTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.region3d.MaxFeretDiameter3D#analyzeRegions(ij.ImageStack, int[], ij.measure.Calibration)}.
	 */
	@Test
	public void testAnalyzeRegionsImageStackIntArrayCalibration()
	{
		ImageStack stack = ImageStack.create(10,  10, 10, 8);
		for (int i = 1; i < 9; i++)
		{
			stack.setVoxel(i, i, i, 255);
		}
		int[] labels = new int[]{255};
		Calibration calib = new Calibration();
		
		MaxFeretDiameter3D algo = new MaxFeretDiameter3D(); 
		PointPair3D[] result = algo.analyzeRegions(stack, labels, calib);
		
		assertEquals(1, result.length);
		PointPair3D pair = result[0];
		assertEquals(8*Math.sqrt(3), pair.diameter(), .1);
	}

}
