package inra.ijpb.morphology.geodrec;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	GeodesicReconstructionHybridTest.class,
	GeodesicReconstructionScanningTest.class,
	GeodesicReconstruction3DHybrid0Gray8Test.class,
	GeodesicReconstruction3DHybrid0Gray16Test.class,
	GeodesicReconstruction3DHybrid1Image3DTest.class,
	GeodesicReconstructionByDilation3DGray8Test.class,
	GeodesicReconstructionByDilation3DScanningGray8Test.class,
	GeodesicReconstructionByDilation3DScanningTest.class,
	GeodesicReconstructionByDilation3DTest.class,
	GeodesicReconstructionByErosion3DGray8Test.class,
	GeodesicReconstructionByErosion3DScanningTest.class
	})
public class AllTests {
  //nothing
}
