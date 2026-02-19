/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
/**
 * 
 */
package inra.ijpb.morphology.strel;

import static org.junit.Assert.*;

import org.junit.Test;

import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.morphology.Strel;

/**
 * @author dlegland
 *
 */
public class ChamferStrel2DTest
{

    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel#getSize()}.
     */
    @Test
    public final void testGetSize()
    {
        ChamferMask2D mask = ChamferMask2D.CHESSKNIGHT;
        double radius = 2.0;
        
        Strel strel = new ChamferStrel(mask, radius);
        int[] size = strel.getSize();
        
        // expect size equal to 2*radius+1 in each direction
        assertEquals(size.length, 2);
        assertEquals(size[0], 5);
        assertEquals(size[1], 5);
    }

    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel#getMask()}.
     */
    @Test
    public final void testGetMask()
    {
        ChamferMask2D chamferMask = ChamferMask2D.CHESSKNIGHT;
        double radius = 2.0;
        
        Strel strel = new ChamferStrel(chamferMask, radius);
        
        int[][] mask = strel.getMask();
        assertEquals(mask.length, 5);
        assertEquals(mask[0].length, 5);
        
        assertTrue(mask[0][0] == 0);
        assertTrue(mask[0][4] == 0);
        assertTrue(mask[4][0] == 0);
        assertTrue(mask[4][4] == 0);
        assertTrue(mask[0][1] > 0);
        assertTrue(mask[0][3] > 0);
        assertTrue(mask[1][0] > 0);
        assertTrue(mask[1][4] > 0);
        assertTrue(mask[3][0] > 0);
        assertTrue(mask[3][4] > 0);
        assertTrue(mask[4][1] > 0);
        assertTrue(mask[4][3] > 0);
    }

    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel#getShifts()}.
     */
    @Test
    public final void testGetShifts()
    {
        ChamferMask2D chamferMask = ChamferMask2D.CHESSKNIGHT;
        double radius = 2.0;
        
        Strel strel = new ChamferStrel(chamferMask, radius);
        
        int[][] shifts = strel.getShifts();
        
        assertEquals(shifts.length, 21);
        assertEquals(shifts[0].length, 2);
    }

}
