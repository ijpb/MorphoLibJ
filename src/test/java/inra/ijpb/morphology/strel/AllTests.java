package inra.ijpb.morphology.strel;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	// First linear strels
	LinearHorizontalStrelTest.class, 
	LinearVerticalStrelTest.class,
	LinearDiagDownStrelTest.class, 
	LinearDiagUpStrelTest.class,
	// compound of linear 
	SquareStrelTest.class, 
	OctagonStrelTest.class,
	// add crosses and diamonds
	Cross3x3StrelTest.class, 
	ShiftedCross3x3Strel_LeftTest.class,
	ShiftedCross3x3Strel_RightTest.class,
	DiamondStrelTest.class,
	// Also Disk strel, based on rank filters
	DiskStrelTest.class,
})
public class AllTests {
  //nothing
}
