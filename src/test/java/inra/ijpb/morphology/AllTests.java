package inra.ijpb.morphology;


import inra.ijpb.label.LabelImagesTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	StrelTest.class,
	MorphologyTest.class,
	FloodFillTest.class,
	FloodFill3DTest.class,
	LabelImagesTest.class, 
	GeodesicReconstructionTest.class,
	GeodesicReconstruction3DTest.class,
	MinimaAndMaximaTest.class,
	MinimaAndMaxima3DTest.class
	})
public class AllTests {
  //nothing
}
