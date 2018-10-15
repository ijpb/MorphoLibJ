/**
 * 
 */
package inra.ijpb.measure;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class Microstructure3DTest
{

	/**
	 * Test method for {@link inra.ijpb.measure.Microstructure3D#volumeDensity(ij.ImageStack)}.
	 */
	@Test
	public void testVolumeDensity()
	{
		String fileName = getClass().getResource("/files/microstructure3D_10x10x10.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		// basic check up
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);

		// get image stack and calibration
		ImageStack image = imagePlus.getStack();
//		Calibration calib = imagePlus.getCalibration();

		double volDensity = Microstructure3D.volumeDensity(image);
		assertEquals(0.363, volDensity, .001);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.Microstructure3D#surfaceAreaDensity(ij.ImageStack, ij.measure.Calibration, int)}.
	 */
	@Test
	public void testSurfaceAreaDensity()
	{
		String fileName = getClass().getResource("/files/microstructure3D_10x10x10.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		// basic check up
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);

		// get image stack and calibration
		ImageStack image = imagePlus.getStack();
		Calibration calib = imagePlus.getCalibration();

		// compare with a value computed with Matlab (2018.09.11)
		double surfaceDensity = Microstructure3D.surfaceAreaDensity(image, calib, 13);
		assertEquals(0.56, surfaceDensity, .001);
	}

	/**
	 * Test method for {@link inra.ijpb.measure.Microstructure3D#meanBreadthDensity(ij.ImageStack, ij.measure.Calibration, int, int)}.
	 */
	@Test
	public void testMeanBreadthDensity()
	{
		String fileName = getClass().getResource("/files/microstructure3D_10x10x10.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		// basic check up
		assertNotNull(imagePlus);
		assertTrue(imagePlus.getStackSize() > 0);

		// get image stack and calibration
		ImageStack image = imagePlus.getStack();
		Calibration calib = imagePlus.getCalibration();

		// compare with a value computed with Matlab (2018.09.11)
		double meanBreadthDensity = Microstructure3D.meanBreadthDensity(image, calib, 13, 8);
		assertEquals(-0.014, meanBreadthDensity, .001);
	}

}
