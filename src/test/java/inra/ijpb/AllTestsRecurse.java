package inra.ijpb;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	inra.ijpb.binary.distmap.AllTests.class,
	inra.ijpb.measure.AllTests.class,
	inra.ijpb.morphology.AllTestsRecurse.class,
	})
public class AllTestsRecurse {
  //nothing
}
