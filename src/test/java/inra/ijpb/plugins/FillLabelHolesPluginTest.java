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
package inra.ijpb.plugins;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ShortProcessor;

/**
 * Tests the hole filling of label images containing a single label.
 */
public class FillLabelHolesPluginTest
{
    /** Square region with value v, containing a square hole. */
    private static ByteProcessor createSquareWithHole(int v)
    {
        ByteProcessor ip = new ByteProcessor(20, 20);
        for (int y = 4; y < 16; y++)
            for (int x = 4; x < 16; x++)
                ip.set(x, y, v);
        for (int y = 8; y < 11; y++)
            for (int x = 8; x < 11; x++)
                ip.set(x, y, 0);
        return ip;
    }

    private static void checkFilled2d(ByteProcessor ip, int v)
    {
        // hole filled
        assertEquals(v, ip.get(9, 9));
        assertEquals(v, ip.get(8, 8));
        // outer background preserved
        assertEquals(0, ip.get(0, 0));
        assertEquals(0, ip.get(19, 19));
        assertEquals(0, ip.get(2, 10));
        // region size: 12x12
        int count = 0;
        for (int i = 0; i < ip.getPixelCount(); i++)
            if (ip.get(i) != 0) count++;
        assertEquals(144, count);
    }

    @Test
    public void testSingleLabel2d()
    {
        ByteProcessor ip = createSquareWithHole(3);
        FillLabelHolesPlugin.process2d(ip, 4, 16);
        checkFilled2d(ip, 3);
    }

    @Test
    public void testBinary2d()
    {
        ByteProcessor ip = createSquareWithHole(255);
        FillLabelHolesPlugin.process2d(ip, 4, 16);
        checkFilled2d(ip, 255);
    }

    @Test
    public void testSingleLabel2dShort()
    {
        ShortProcessor ip = new ShortProcessor(20, 20);
        for (int y = 4; y < 16; y++)
            for (int x = 4; x < 16; x++)
                ip.set(x, y, 7);
        ip.set(9, 9, 0);
        FillLabelHolesPlugin.process2d(ip, 8, 16);
        assertEquals(7, ip.get(9, 9));
        assertEquals(0, ip.get(0, 0));
    }

    @Test
    public void testSingleLabel3d()
    {
        ImageStack stack = ImageStack.create(20, 20, 20, 8);
        for (int z = 4; z < 16; z++)
            for (int y = 4; y < 16; y++)
                for (int x = 4; x < 16; x++)
                    stack.setVoxel(x, y, z, 5);
        for (int z = 8; z < 11; z++)
            for (int y = 8; y < 11; y++)
                for (int x = 8; x < 11; x++)
                    stack.setVoxel(x, y, z, 0);

        FillLabelHolesPlugin.process3d(stack, 6, 16);

        assertEquals(5, stack.getVoxel(9, 9, 9), 0);
        assertEquals(0, stack.getVoxel(0, 0, 0), 0);
        assertEquals(0, stack.getVoxel(2, 10, 10), 0);
        int count = 0;
        for (int z = 0; z < 20; z++)
            for (int y = 0; y < 20; y++)
                for (int x = 0; x < 20; x++)
                    if (stack.getVoxel(x, y, z) != 0) count++;
        assertEquals(12 * 12 * 12, count);
    }
}
