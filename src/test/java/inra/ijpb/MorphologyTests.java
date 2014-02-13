package inra.ijpb;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	inra.ijpb.OpenResourceImage.class, 
	inra.ijpb.binary.distmap.AllTests.class,
	inra.ijpb.morphology.AllTests.class,
	inra.ijpb.morphology.strel.AllTests.class,
})
public class MorphologyTests {
  //nothing
}
