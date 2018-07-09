/**
 * 
 */
package inra.ijpb.geometry;

import static org.junit.Assert.*;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class Polygons2DTest
{
	@Test
	public void polygonCentroidTest_Rect1()
	{
		ArrayList<Point2D> vertices = new ArrayList<Point2D>(4);
		vertices.add(new Point2D.Double(10, 20));
		vertices.add(new Point2D.Double(10+30, 20));
		vertices.add(new Point2D.Double(10+30, 20+40));
		vertices.add(new Point2D.Double(10, 20+40));
		
		Point2D.Double exp = new Point2D.Double(25, 40);
		Point2D centroid = Polygons2D.centroid(vertices);
		
		assertEquals(exp.getX(), centroid.getX(), .01);
		assertEquals(exp.getY(), centroid.getY(), .01);
	}

	/**
	 * The same polygon, with identical first and last vertex
	 */
	@Test
	public void polygonCentroidTest_Rect2()
	{
		ArrayList<Point2D> vertices = new ArrayList<Point2D>(5);
		vertices.add(new Point2D.Double(10, 20));
		vertices.add(new Point2D.Double(10+30, 20));
		vertices.add(new Point2D.Double(10+30, 20+40));
		vertices.add(new Point2D.Double(10, 20+40));
		vertices.add(new Point2D.Double(10, 20));
		
		Point2D.Double exp = new Point2D.Double(25, 40);
		Point2D centroid = Polygons2D.centroid(vertices);
		
		assertEquals(exp.getX(), centroid.getX(), .01);
		assertEquals(exp.getY(), centroid.getY(), .01);
	}

	public void testConvexHull_jarvis_int()
	{
		ArrayList<Point> polygon = new ArrayList<Point>();
		polygon.add(new Point( 30,  90));
		polygon.add(new Point(110,  10));
		polygon.add(new Point( 60,  80));
		polygon.add(new Point( 40,  30));
		polygon.add(new Point( 50, 150));
		polygon.add(new Point( 80, 110));
		polygon.add(new Point( 10,  60));
		polygon.add(new Point( 70,  40));
		polygon.add(new Point( 90,  70));
		polygon.add(new Point(140,  50));
		polygon.add(new Point(100, 130));
		polygon.add(new Point(160, 140));
		polygon.add(new Point(150,  20));
		polygon.add(new Point(130, 160));
		polygon.add(new Point( 20, 120));
		polygon.add(new Point(120, 100));

		ArrayList<Point> convHull = Polygons2D.convexHull_jarvis_int(polygon);
		
		int i = 0;
		for (Point p : convHull)
		{
			System.out.println(String.format("Vertex %2d = (%3d, %3d)", i++, p.x, p.y));
		}
	}

}
