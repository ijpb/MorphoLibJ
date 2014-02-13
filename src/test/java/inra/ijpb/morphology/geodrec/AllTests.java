package inra.ijpb.morphology.geodrec;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	GeodesicReconstructionByDilation3DGray8Test.class,
	GeodesicReconstructionByDilation3DGray8ScanningTest.class,
	GeodesicReconstructionByDilation3DTest.class,
	GeodesicReconstructionByErosion3DGray8Test.class
	})
public class AllTests {
  //nothing
}
