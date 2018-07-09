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
