package inra.ijpb.measure.region2d;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import inra.ijpb.geometry.Ellipse;

public class EquivalentEllipseTest
{
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.EquivalentEllipse#analyzeRegions(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testAnalyzeRegions_circles()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/circles.tif").getFile());
	
		EquivalentEllipse algo = new EquivalentEllipse();
		Map<Integer,Ellipse> ellipses = algo.analyzeRegions(imagePlus);

		assertEquals(1, ellipses.size());
		
		Ellipse ell = ellipses.get(255);
		assertEquals(152, ell.radius1(), 1.0);
		assertEquals(47, ell.radius2(), 1.0);
		assertEquals(56, ell.orientation(), 1.0);
		
		ResultsTable table = algo.createTable(ellipses);
		
		assertEquals(1, table.size());
	}

	/**
	 * Test method for {@link ijt.measure.geometric.GeometricMeasures2D#equivalentEllipse(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testAnalyzeRegions_OrientedEllipse() 
	{
		String fileName = getClass().getResource("/files/ellipse_A40_B20_T30.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		
		EquivalentEllipse op = new EquivalentEllipse();
		ResultsTable table = op.computeTable(imagePlus);
		
		assertEquals(49.5, table.getValue("Ellipse.Center.X", 0), .1);
		assertEquals(49.5, table.getValue("Ellipse.Center.Y", 0), .1);
		assertEquals(40, table.getValue("Ellipse.Radius1", 0), .2);
		assertEquals(20, table.getValue("Ellipse.Radius2", 0), .2);
		assertEquals(30, table.getValue("Ellipse.Orientation", 0), 1);
	}
	
	/**
	 * Test method for {@link inra.ijpb.measure.region2d.EquivalentEllipse#analyzeRegions(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testAnalyzeRegions_riceGrains()
	{
		ImagePlus imagePlus = IJ.openImage(getClass().getResource("/files/grains-med-WTH-lbl.tif").getFile());
	
		EquivalentEllipse algo = new EquivalentEllipse();
		
		Map<Integer,Ellipse> ellipses = algo.analyzeRegions(imagePlus);
		assertEquals(96, ellipses.size());
		
		ResultsTable table = algo.createTable(ellipses);
		
		assertEquals(96, table.size());
	}
}
