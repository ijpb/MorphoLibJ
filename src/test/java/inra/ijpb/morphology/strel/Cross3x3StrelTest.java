/**
 * 
 */
package inra.ijpb.morphology.strel;

import static org.junit.Assert.assertEquals;
import inra.ijpb.morphology.Strel;

import org.junit.Test;


/**
 * @author David Legland
 *
 */
public class Cross3x3StrelTest {

	@Test
	public void testMaskAndShifts() {
		Strel strel = new Cross3x3Strel();

		int[][] shifts = strel.getShifts();
		int[][] mask = strel.getMask();
		int[] offset = strel.getOffset();
		
		for (int s = 0; s < shifts.length; s++) {
			int[] shift = shifts[s];
			
			int indX = shift[0] + offset[0];
			int indY = shift[1] + offset[1];
			assertEquals(255, mask[indY][indX]);
		}
	}


}
