package inra.ijpb.measure.region2d;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import inra.ijpb.geometry.Ellipse;

public class InertiaEllipseTest
{
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.InertiaEllipse#compute(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testProcess_circles()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
	
		InertiaEllipse algo = new InertiaEllipse();
		
		Map<Integer,Ellipse> ellipses = algo.compute(imagePlus);

		assertEquals(1, ellipses.size());
		
		Ellipse ell = ellipses.get(255);
		
		assertEquals(152, ell.radius1(), 1.0);
		assertEquals(47, ell.radius2(), 1.0);
		assertEquals(56, ell.orientation(), 1.0);
		
		ResultsTable table = InertiaEllipse.asTable(ellipses);
		assertEquals(1, table.size());
	}

	/**
	 * Test method for {@link inra.ijpb.measure.region2d.InertiaEllipse#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testProcess_riceGrains()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-med-WTH-lbl.tif").getFile());
	
		InertiaEllipse algo = new InertiaEllipse();
		
		Map<Integer,Ellipse> ellipses = algo.compute(imagePlus);
		assertEquals(96, ellipses.size());
		
		ResultsTable table = InertiaEllipse.asTable(ellipses);
		assertEquals(96, table.size());
	}
}
