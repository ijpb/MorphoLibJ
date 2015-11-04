package inra.ijpb.measure;


import inra.ijpb.measure.RegionAdjacencyGraphTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	GeometricMeasures2DTest.class,
	GeometricMeasures3DTest.class,
	GeometryUtilsTest.class,
	RegionAdjacencyGraphTest.class, 
	Vector3dTest.class,
	})
public class AllTests {
  //nothing
}
