package inra.ijpb.binary.geodesic;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	GeodesicDistanceTransformFloatTest.class,
	GeodesicDistanceTransformShortTest.class,
	GeodesicDistanceTransformFloat5x5Test.class,
	GeodesicDistanceTransformShort5x5Test.class,
})
public class AllTests {
  //nothing
}
