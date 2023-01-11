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
package inra.ijpb.geometry;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;

import org.junit.Test;

public class StraightLine2DTest
{

	@Test
	public void test_distancePoint2D_horizontalLine()
	{
		StraightLine2D line = new StraightLine2D(new Point2D.Double(20, 0), new Vector2D(3, 0));
		
		Point2D p1 = new Point2D.Double(10, 0);
		double d1 = line.distance(p1);
		assertEquals(0, d1, 1e-8);
		
		Point2D p2 = new Point2D.Double(10, 10);
		double d2 = line.distance(p2);
		assertEquals(10, d2, 1e-8);
	}


	@Test
	public void test_distancePoint2D_verticalLine()
	{
		StraightLine2D line = new StraightLine2D(new Point2D.Double(0, 20), new Vector2D(0, 3));
		
		Point2D p1 = new Point2D.Double(0, 10);
		double d1 = line.distance(p1);
		assertEquals(0, d1, 1e-8);
		
		Point2D p2 = new Point2D.Double(10, 10);
		double d2 = line.distance(p2);
		assertEquals(10, d2, 1e-8);
	}


	@Test
	public void test_distancePoint2D_diagonalLine()
	{
		StraightLine2D line = new StraightLine2D(new Point2D.Double(20, 20), new Vector2D(3, 3));
		
		Point2D p1 = new Point2D.Double(10, 10);
		double d1 = line.distance(p1);
		assertEquals(0, d1, 1e-8);
		
		Point2D p2 = new Point2D.Double(20, 0);
		double d2 = line.distance(p2);
		assertEquals(10 * Math.sqrt(2), d2, 1e-8);
	}

}
