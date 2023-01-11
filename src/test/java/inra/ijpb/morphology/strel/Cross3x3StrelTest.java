/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
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
