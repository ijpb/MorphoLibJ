package inra.ijpb.morphology;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	AllTests.class,
	inra.ijpb.morphology.geodrec.AllTests.class,
	inra.ijpb.morphology.strel.AllTests.class
	})
public class AllTestsRecurse {
  //nothing
}
