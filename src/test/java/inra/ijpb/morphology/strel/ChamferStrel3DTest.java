/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.ImageStack;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.Strel3D;

/**
 * @author dlegland
 *
 */
public class ChamferStrel3DTest
{
    
    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel3D#getSize()}.
     */
    @Test
    public final void testGetSize_Borgefors_Radius2()
    {
        ChamferMask3D mask = ChamferMask3D.BORGEFORS;
        double radius = 2.0;
        
        Strel3D strel = new ChamferStrel3D(mask, radius);
        
        int[] size = strel.getSize();
        assertEquals(3, size.length);
        assertEquals(5, size[0]);
        assertEquals(5, size[1]);
        assertEquals(5, size[2]);
    }
    
    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel3D#getOffset()}.
     */
    @Test
    public final void testGetOffset_Borgefors_Radius2()
    {
        ChamferMask3D mask = ChamferMask3D.BORGEFORS;
        double radius = 2.0;
        
        Strel3D strel = new ChamferStrel3D(mask, radius);
        
        int[] offset = strel.getOffset();
        assertEquals(3, offset.length);
        assertEquals(2, offset[0]);
        assertEquals(2, offset[1]);
        assertEquals(2, offset[2]);
    }

    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel3D#getMask3D()}.
     */
    @Test
    public final void testGetMask3D_Borgefors_Radius2()
    {
        ChamferMask3D chamferMask = ChamferMask3D.BORGEFORS;
        double radius = 2.0;
        
        Strel3D strel = new ChamferStrel3D(chamferMask, radius);
        
        int[][][] mask = strel.getMask3D();
        assertEquals(5, mask.length);
        assertEquals(5, mask[0].length);
        assertEquals(5, mask[0][0].length);
        
        assertTrue(mask[0][0][2] == 0);
        assertTrue(mask[0][2][0] == 0);
        assertTrue(mask[0][2][2] > 0);
        assertTrue(mask[0][2][4] == 0);
        assertTrue(mask[0][4][2] == 0);
        
        assertTrue(mask[2][0][0] == 0);
        assertTrue(mask[2][0][2] > 0);
        assertTrue(mask[2][0][4] == 0);
        
        assertTrue(mask[2][2][0] > 0);
        assertTrue(mask[2][2][4] > 0);
        
        assertTrue(mask[2][4][0] == 0);
        assertTrue(mask[2][4][2] > 0);
        assertTrue(mask[2][4][4] == 0);

        assertTrue(mask[4][0][2] == 0);
        assertTrue(mask[4][2][0] == 0);
        assertTrue(mask[4][2][2] > 0);
        assertTrue(mask[4][2][4] == 0);
        assertTrue(mask[4][4][2] == 0);
    }
    
    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel3D#getMask3D()}.
     */
    @Test
    public final void testGetMask3D_Svensson_Radius2()
    {
        ChamferMask3D chamferMask = ChamferMask3D.SVENSSON_3_4_5_7;
        double radius = 2.0;
        
        Strel3D strel = new ChamferStrel3D(chamferMask, radius);
        
        int[][][] mask = strel.getMask3D();
        assertEquals(5, mask.length);
        assertEquals(5, mask[0].length);
        assertEquals(5, mask[0][0].length);
        
        assertTrue(mask[0][0][2] == 0);
        assertTrue(mask[0][2][0] == 0);
        assertTrue(mask[0][2][2] > 0);
        assertTrue(mask[0][2][4] == 0);
        assertTrue(mask[0][4][2] == 0);
        
        assertTrue(mask[2][0][0] == 0);
        assertTrue(mask[2][0][2] > 0);
        assertTrue(mask[2][0][4] == 0);
        
        assertTrue(mask[2][2][0] > 0);
        assertTrue(mask[2][2][4] > 0);
        
        assertTrue(mask[2][4][0] == 0);
        assertTrue(mask[2][4][2] > 0);
        assertTrue(mask[2][4][4] == 0);

        assertTrue(mask[4][0][2] == 0);
        assertTrue(mask[4][2][0] == 0);
        assertTrue(mask[4][2][2] > 0);
        assertTrue(mask[4][2][4] == 0);
        assertTrue(mask[4][4][2] == 0);
        
        
//        for (int z = 0; z < mask.length; z++)
//        {
//            System.out.println("Slice " + z + " / " + mask.length);
//            for (int y = 0; y < mask[0].length; y++)
//            {
//                for (int x = 0; x < mask[0][0].length; x++)
//                {
//                    System.out.print((mask[z][y][x] > 0) ? "X" : ".");
//                }
//                System.out.println("");
//            }
//        }
    }
    
    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel3D#getMask3D()}.
     */
    @Test
    public final void testGetMask3D_Svensson_Radius3()
    {
        ChamferMask3D chamferMask = ChamferMask3D.SVENSSON_3_4_5_7;
        double radius = 3.0;
        
        Strel3D strel = new ChamferStrel3D(chamferMask, radius);
        
        int[][][] mask = strel.getMask3D();
        assertEquals(7, mask.length);
        assertEquals(7, mask[0].length);
        assertEquals(7, mask[0][0].length);
    }
    
    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel3D#getShifts3D()}.
     */
    @Test
    public final void testGetShifts3D_Svensson_Radius2()
    {
        ChamferMask3D chamferMask = ChamferMask3D.SVENSSON_3_4_5_7;
        double radius = 2.0;
        
        Strel3D strel = new ChamferStrel3D(chamferMask, radius);
        
        int[][] shifts = strel.getShifts3D();
        
        // expect:
        // 21 voxels (=5*5-4) for each of the 3 middle slices -> 63
        //  9 voxels for each of the extreme slices -> 18
        assertEquals(81, shifts.length);
    }
    
    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel3D#dilation(ImageStack)}.
     */
    @Test
    public final void testDilation_Svensson_Radius2_singleDot()
    {
        ChamferMask3D chamferMask = ChamferMask3D.SVENSSON_3_4_5_7;
        double radius = 3.0;
        
        Strel3D strel = new ChamferStrel3D(chamferMask, radius);
        
        ImageStack image = ImageStack.create(7, 7, 7, 8);
        image.setVoxel(3, 3, 3, 255);
        
        ImageStack res = strel.dilation(image);
        
        assertEquals(image.getWidth(), res.getWidth());
        assertEquals(image.getHeight(), res.getHeight());
        assertEquals(image.getSize(), res.getSize());
        
        assertTrue(res.getVoxel(0, 0, 0) == 0);
        assertTrue(res.getVoxel(3, 0, 0) == 0);
        assertTrue(res.getVoxel(6, 0, 0) == 0);
        assertTrue(res.getVoxel(0, 3, 0) == 0);
        assertTrue(res.getVoxel(3, 3, 0) > 0);
        assertTrue(res.getVoxel(6, 3, 0) == 0);
        assertTrue(res.getVoxel(0, 6, 0) == 0);
        assertTrue(res.getVoxel(3, 6, 0) == 0);
        assertTrue(res.getVoxel(6, 6, 0) == 0);

        
        assertTrue(res.getVoxel(0, 0, 3) == 0);
        assertTrue(res.getVoxel(3, 0, 3) > 0);
        assertTrue(res.getVoxel(6, 0, 3) == 0);
        assertTrue(res.getVoxel(0, 3, 3) > 0);
        assertTrue(res.getVoxel(6, 3, 3) > 0);
        assertTrue(res.getVoxel(0, 6, 3) == 0);
        assertTrue(res.getVoxel(3, 6, 3) > 0);
        assertTrue(res.getVoxel(6, 6, 3) == 0);
        
        
        assertTrue(res.getVoxel(0, 0, 6) == 0);
        assertTrue(res.getVoxel(3, 0, 6) == 0);
        assertTrue(res.getVoxel(6, 0, 6) == 0);
        assertTrue(res.getVoxel(0, 3, 6) == 0);
        assertTrue(res.getVoxel(3, 3, 6) > 0);
        assertTrue(res.getVoxel(6, 3, 6) == 0);
        assertTrue(res.getVoxel(0, 6, 6) == 0);
        assertTrue(res.getVoxel(3, 6, 6) == 0);
        assertTrue(res.getVoxel(6, 6, 6) == 0);
    }
    
    /**
     * Test method for {@link inra.ijpb.morphology.strel.ChamferStrel3D#erosion(ImageStack)}.
     */
    @Test
    public final void testErosion_Svensson_Radius2_singleDot()
    {
        ChamferMask3D chamferMask = ChamferMask3D.SVENSSON_3_4_5_7;
        double radius = 3.0;
        
        Strel3D strel = new ChamferStrel3D(chamferMask, radius);
        
        ImageStack image = ImageStack.create(7, 7, 7, 8);
        Images3D.fill(image, 255);
        image.setVoxel(3, 3, 3, 0);
        
        ImageStack res = strel.erosion(image);
        
        assertEquals(image.getWidth(), res.getWidth());
        assertEquals(image.getHeight(), res.getHeight());
        assertEquals(image.getSize(), res.getSize());
        
        assertTrue(res.getVoxel(0, 0, 0) > 0);
        assertTrue(res.getVoxel(3, 0, 0) > 0);
        assertTrue(res.getVoxel(6, 0, 0) > 0);
        assertTrue(res.getVoxel(0, 3, 0) > 0);
        assertTrue(res.getVoxel(3, 3, 0) == 0);
        assertTrue(res.getVoxel(6, 3, 0) > 0);
        assertTrue(res.getVoxel(0, 6, 0) > 0);
        assertTrue(res.getVoxel(3, 6, 0) > 0);
        assertTrue(res.getVoxel(6, 6, 0) > 0);

        
        assertTrue(res.getVoxel(0, 0, 3) > 0);
        assertTrue(res.getVoxel(3, 0, 3) == 0);
        assertTrue(res.getVoxel(6, 0, 3) > 0);
        assertTrue(res.getVoxel(0, 3, 3) == 0);
        assertTrue(res.getVoxel(6, 3, 3) == 0);
        assertTrue(res.getVoxel(0, 6, 3) > 0);
        assertTrue(res.getVoxel(3, 6, 3) == 0);
        assertTrue(res.getVoxel(6, 6, 3) > 0);
        
        
        assertTrue(res.getVoxel(0, 0, 6) > 0);
        assertTrue(res.getVoxel(3, 0, 6) > 0);
        assertTrue(res.getVoxel(6, 0, 6) > 0);
        assertTrue(res.getVoxel(0, 3, 6) > 0);
        assertTrue(res.getVoxel(3, 3, 6) == 0);
        assertTrue(res.getVoxel(6, 3, 6) > 0);
        assertTrue(res.getVoxel(0, 6, 6) > 0);
        assertTrue(res.getVoxel(3, 6, 6) > 0);
        assertTrue(res.getVoxel(6, 6, 6) > 0);
    }
    
}
