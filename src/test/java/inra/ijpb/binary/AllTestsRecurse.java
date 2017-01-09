package inra.ijpb.binary;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	AllTests.class,
	inra.ijpb.binary.conncomp.AllTests.class,
	inra.ijpb.binary.distmap.AllTests.class,
	inra.ijpb.binary.geodesic.AllTests.class,
	})
public class AllTestsRecurse {
  //nothing
}
