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
package inra.ijpb.geometry;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class Polygon2DTest
{

	/**
	 * Test method for {@link inra.ijpb.geometry.Polygon2D#boundingBox()}.
	 */
	@Test
	public final void testBoundingBox()
	{
		Polygon2D poly = new Polygon2D();
		poly.addVertex(new Point2D.Double(40, 70));
		poly.addVertex(new Point2D.Double(40, 50));
		poly.addVertex(new Point2D.Double(20, 50));
		poly.addVertex(new Point2D.Double(60, 10));
		poly.addVertex(new Point2D.Double(60, 30));
		poly.addVertex(new Point2D.Double(80, 30));
		
		Box2D exp = new Box2D(20, 80, 10, 70);
		Box2D box = poly.boundingBox();
		assertEquals(exp.xmin, box.xmin, .01);
		assertEquals(exp.xmax, box.xmax, .01);
		assertEquals(exp.ymin, box.ymin, .01);
		assertEquals(exp.ymax, box.ymax, .01);
	}

	/**
	 * Test method for {@link inra.ijpb.geometry.Polygon2D#area()}.
	 */
	@Test
	public final void testArea_Rect1()
	{
		Polygon2D poly = new Polygon2D(4);
		poly.addVertex(new Point2D.Double(10, 20));
		poly.addVertex(new Point2D.Double(10+30, 20));
		poly.addVertex(new Point2D.Double(10+30, 20+40));
		poly.addVertex(new Point2D.Double(10, 20+40));
		
		double exp = 30.0 * 40.0;
		double area = poly.area();
		
		assertEquals(exp, area, .01);
	}

	/**
	 * Test method for {@link inra.ijpb.geometry.Polygon2D#area()}.
	 */
	@Test
	public final void testArea_Rect1Reverse()
	{
		Polygon2D poly = new Polygon2D(4);
		poly.addVertex(new Point2D.Double(10, 20));
		poly.addVertex(new Point2D.Double(10, 20+40));
		poly.addVertex(new Point2D.Double(10+30, 20+40));
		poly.addVertex(new Point2D.Double(10+30, 20));
		
		double exp = 30.0 * 40.0;
		double area = poly.area();
		
		assertEquals(exp, area, .01);
	}
	
	/**
	 * Test method for {@link inra.ijpb.geometry.Polygon2D#area()}.
	 */
	@Test
	public final void testInvert_Rect()
	{
		Polygon2D poly = new Polygon2D(4);
		poly.addVertex(new Point2D.Double(10, 20));
		poly.addVertex(new Point2D.Double(10+30, 20));
		poly.addVertex(new Point2D.Double(10+30, 20+40));
		poly.addVertex(new Point2D.Double(10, 20+40));
		
		double exp = 30.0 * 40.0;
		double area = poly.signedArea();
		assertEquals(exp, area, .01);
		
		poly = poly.invert();
		area = poly.signedArea();
		assertEquals(-exp, area, .01);
		
		// First vertex should be the same
		assertEquals(0, poly.getVertex(0).distance(10, 20), .01);
	}

	/**
	 * Test method for {@link inra.ijpb.geometry.Polygon2D#centroid()}.
	 */
	@Test
	public final void testCentroid_Rect1()
	{
		Polygon2D poly = new Polygon2D(4);
		poly.addVertex(new Point2D.Double(10, 20));
		poly.addVertex(new Point2D.Double(10+30, 20));
		poly.addVertex(new Point2D.Double(10+30, 20+40));
		poly.addVertex(new Point2D.Double(10, 20+40));
		
		Point2D.Double exp = new Point2D.Double(25, 40);
		Point2D centroid = poly.centroid();
		
		assertEquals(exp.getX(), centroid.getX(), .01);
		assertEquals(exp.getY(), centroid.getY(), .01);
	}

	/**
	 * The same polygon, with identical first and last vertex
	 * 
	 * Test method for {@link inra.ijpb.geometry.Polygon2D#centroid()}.
	 */
	@Test
	public final void testCentroid_Rect2()
	{
		Polygon2D poly = new Polygon2D(5);
		poly.addVertex(new Point2D.Double(10, 20));
		poly.addVertex(new Point2D.Double(10+30, 20));
		poly.addVertex(new Point2D.Double(10+30, 20+40));
		poly.addVertex(new Point2D.Double(10, 20+40));
		poly.addVertex(new Point2D.Double(10, 20));
		
		Point2D.Double exp = new Point2D.Double(25, 40);
		Point2D centroid = poly.centroid();
		
		assertEquals(exp.getX(), centroid.getX(), .01);
		assertEquals(exp.getY(), centroid.getY(), .01);
	}

}
