package inra.ijpb.morphology.extrema;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.extrema.RegionalExtremaAlgo.ExtremaType;

import org.junit.Test;

public class RegionalExtremaByFloodingTest {

	@Test
	public final void testRun() {
		String fileName = getClass().getResource("/files/grains.tif").getFile();
		assertNotNull(fileName);
		
		ImagePlus imagePlus = IJ.openImage(fileName);
		ImageProcessor image = imagePlus.getProcessor();
		
		RegionalExtremaAlgo algo = new RegionalExtremaByFlooding();
		algo.setConnectivity(4);
		algo.setExtremaType(ExtremaType.MAXIMA);

		ImageProcessor output = algo.applyTo(image);
		
		assertNotNull(output);
	}

}
