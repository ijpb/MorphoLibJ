package inra.ijpb.morphology;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	StrelTest.class,
	MorphologyTest.class,
	FloodFillTest.class,
	LabelImagesTest.class, 
	GeodesicReconstructionTest.class,
	GeodesicReconstruction3DTest.class,
	MinimaAndMaximaTest.class,
	MinimaAndMaxima3DTest.class
	})
public class AllTests {
  //nothing
}
