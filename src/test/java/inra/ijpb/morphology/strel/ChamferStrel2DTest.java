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
