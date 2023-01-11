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
/**
 * 
 */
package inra.ijpb.measure.region2d;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class RegionBoundariesTest
{

    /**
     * Test method for {@link inra.ijpb.measure.region2d.RegionBoundaries#runLengthsCorners(ij.process.ImageProcessor)}.
     */
    @Test
    public final void testRunLengthsCorners()
    {
        // create a simple 3x3 image with a pixel at position (1,1).
        ImageProcessor image = new ByteProcessor(3,3);
        image.set(1, 1, 255);
        
        ArrayList<Point2D> points = RegionBoundaries.runLengthsCorners(image);
        
        assertEquals(4, points.size());
        assertTrue(points.contains(new Point2D.Double(1.0, 1.0))); // top-left corner
        assertTrue(points.contains(new Point2D.Double(2.0, 1.0))); // top-right corner
        assertTrue(points.contains(new Point2D.Double(1.0, 2.0))); // bottom-left corner
        assertTrue(points.contains(new Point2D.Double(2.0, 2.0))); // bottom-right corner
    }

    /**
     * Test method for {@link inra.ijpb.measure.region2d.RegionBoundaries#boundaryPixelsMiddleEdges(ij.process.ImageProcessor)}.
     */
    @Test
    public final void testBoundaryPixelsMiddleEdges()
    {
        // create a simple 3x3 image with a pixel at position (1,1).
        ImageProcessor image = new ByteProcessor(3,3);
        image.set(1, 1, 255);
        
        ArrayList<Point2D> points = RegionBoundaries.boundaryPixelsMiddleEdges(image);
        
        assertEquals(4, points.size());
        assertTrue(points.contains(new Point2D.Double(1.5, 1.0))); // upper corner
        assertTrue(points.contains(new Point2D.Double(1.0, 1.5))); // left corner
        assertTrue(points.contains(new Point2D.Double(2.0, 1.5))); // right corner
        assertTrue(points.contains(new Point2D.Double(1.5, 2.0))); // bottom corner
    }

    /**
     * Test method for {@link inra.ijpb.measure.region2d.RegionBoundaries#boundaryPixelsMiddleEdges(ij.process.ImageProcessor)}.
     */
    @Test
    public final void testBoundaryPixelsMiddleEdges_FourLabels()
    {
        // create a 6x6 image with a four 2x2 labels
        ImageProcessor image = new ByteProcessor(6,6);
        for (int y = 0; y < 2; y++)
        {
            for (int x = 0; x < 2; x++)
            {
                image.set(x + 1, y + 1, 2);
                image.set(x + 3, y + 1, 3);
                image.set(x + 1, y + 3, 4);
                image.set(x + 3, y + 3, 7);
            }
        }
        
        int[] labels = new int[] {2, 3, 4, 7};
        ArrayList<Point2D>[] arrays = RegionBoundaries.boundaryPixelsMiddleEdges(image, labels);
        
        assertEquals(4, arrays.length);
        
        ArrayList<Point2D> points2 = arrays[0]; 
        assertEquals(8, points2.size());
        
        assertTrue(points2.contains(new Point2D.Double(1.5, 1.0))); // upper corner
        assertTrue(points2.contains(new Point2D.Double(1.0, 1.5))); // left corner
        assertTrue(points2.contains(new Point2D.Double(3.0, 2.5))); // right corner
        assertTrue(points2.contains(new Point2D.Double(2.5, 3.0))); // bottom corner
    }
}
