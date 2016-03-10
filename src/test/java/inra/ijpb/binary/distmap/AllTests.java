package inra.ijpb.binary.distmap;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	DistanceTransform3x3FloatTest.class,
	DistanceTransform3x3ShortTest.class,
	DistanceTransform5x5FloatTest.class,
	DistanceTransform5x5ShortTest.class,
	DistanceTransform3DShortTest.class,
	DistanceTransform3DFloatTest.class,
})
public class AllTests {
  //nothing
}
