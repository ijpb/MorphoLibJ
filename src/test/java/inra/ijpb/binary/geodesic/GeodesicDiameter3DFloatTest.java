/**
 * 
 */
package inra.ijpb.binary.geodesic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;
import inra.ijpb.binary.ChamferWeights3D;

/**
 * @author dlegland
 *
 */
public class GeodesicDiameter3DFloatTest
{
	/**
	 * Test method for {@link inra.ijpb.binary.geodesic.GeodesicDiameterFloat#analyzeImage(ij.process.ImageProcessor)}.
	 */
	@Test
	public void testAnalyzeImage_BatCochlea()
	{
		String fileName = getClass().getResource("/files/bat-cochlea-volume.tif").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		
		assertNotNull(imagePlus);
	
		assertTrue(imagePlus.getStackSize() > 0);
	
		ImageStack image = imagePlus.getStack();
	
		GeodesicDiameter3D algo = new GeodesicDiameter3DFloat(ChamferWeights3D.BORGEFORS);
		ResultsTable table = algo.process(image);

		assertEquals(1, table.getCounter());
	}
}
