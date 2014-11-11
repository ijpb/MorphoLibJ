package inra.ijpb.binary.distmap;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// generic classes
	ChamferDistance3x3FloatTest.class,
	ChamferDistance3x3ShortTest.class,
	ChamferDistance5x5FloatTest.class,
	ChamferDistance5x5ShortTest.class,
})
public class AllTests {
  //nothing
}
