package inra.ijpb;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	inra.ijpb.OpenResourceImage.class, 
	inra.ijpb.binary.AllTestsRecurse.class,
	inra.ijpb.label.AllTests.class,
	inra.ijpb.measure.AllTests.class,
	inra.ijpb.morphology.AllTestsRecurse.class,
	inra.ijpb.util.AllTests.class,
	})
public class AllTestsRecurse {
  //nothing
}
