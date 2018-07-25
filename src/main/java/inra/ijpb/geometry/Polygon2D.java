/**
 * 
 */
package inra.ijpb.geometry;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * A polygon shape in the plane.
 * 
 * Polygon is assumed to be simple: only one connected component, no hole.
 * 
 * @author dlegland
 *
 */
public class Polygon2D implements Iterable <Point2D>
{
    // ====================================================
    // Class variables
    
	ArrayList<Point2D> vertices;

	
	// ====================================================
    // Constructors
    
	/**
	 * Creates a new empty polygon.
	 * 
	 */
	public Polygon2D()
	{
		this.vertices = new ArrayList<Point2D>(10);
	}
	
	/**
	 * Creates a new empty polygon by allocating enough space for storing vertex
	 * coordinates.
	 * 
	 * @param n
	 *            then number of vertices
	 */
	public Polygon2D(int n)
	{
		this.vertices = new ArrayList<Point2D>(n);
	}
	
	/**
	 * Creates a new polygon from the coordinates of its vertices.
	 * 
	 * @param xCoords 
	 *            the x-coordinates of the vertices
	 * @param yCoords 
	 *            the y-coordinates of the vertices
	 */
	public Polygon2D(double[] xCoords, double[] yCoords)
	{
		int n = xCoords.length;
		if (yCoords.length != n)
		{
			throw new IllegalArgumentException("Coordinate arrays must have same length");
		}
		this.vertices = new ArrayList<Point2D>(n);
		for (int i = 0; i < n; i++)
		{
			this.vertices.add(new Point2D.Double(xCoords[i], yCoords[i]));
		}
	}
	
	/**
	 * Creates a new polygon from a collection of vertices. A new collection is
	 * created.
	 * 
	 * @param vertices
	 *            the polygon vertices
	 */
	public Polygon2D(Collection<Point2D> vertices)
	{
		this.vertices = new ArrayList<Point2D>(vertices.size());
		this.vertices.addAll(vertices);
	}
	
	
    // ====================================================
    // General methods
    
	/**
	 * Returns the bounding box of this polygon.
	 * 
	 * @return the bounding box of this polygon.
	 */
	public Box2D boundingBox()
	{
		double xmin = Double.POSITIVE_INFINITY;
		double xmax = Double.NEGATIVE_INFINITY;
		double ymin = Double.POSITIVE_INFINITY;
		double ymax = Double.NEGATIVE_INFINITY;
		for (Point2D vertex : this.vertices)
		{
			double x = vertex.getX();
			double y = vertex.getY();
			xmin = Math.min(xmin, x);
			xmax = Math.max(xmax, x);
			ymin = Math.min(ymin, y);
			ymax = Math.max(ymax, y);
		}
		return new Box2D(xmin, xmax, ymin, ymax);
	}
	
	/**
	 * Computes the area of this polygon
	 * 
	 * @see #signedArea()
	 * @return the area of this polygon
	 */
	public double area()
	{
		// simply returns the absolute value of the signed area
		return Math.abs(signedArea());
	}
	
	/**
	 * Computes the signed area of this polygon
	 * 
	 * @see #area()
	 * @return the signed area of this polygon  
	 */
	public double signedArea()
	{
		// accumulators
		double area = 0;
		
		// iterate on vertex pairs
		int n = vertices.size();
		for (int i = 0; i < n; i++)
		{
			Point2D p1 = this.vertices.get(i);
			Point2D p2 = this.vertices.get((i + 1) % n);
			double x1 = p1.getX();
			double y1 = p1.getY();
			double x2 = p2.getX();
			double y2 = p2.getY();
			area += x1 * y2 - x2 * y1;
		}
		
		// the area is the sum of the common factors divided by 2
		return area / 2;
	}
	
	/**
	 * Computes the centroid of this polygon
	 * 
	 * @return the centroid position of the polygon.
	 */
	public Point2D centroid()
	{
		// accumulators
		double sumC = 0;
		double sumX = 0;
		double sumY = 0;
		
		// iterate on vertex pairs
		int n = vertices.size();
		for (int i = 1; i <= n; i++)
		{
			Point2D p1 = this.vertices.get(i - 1);
			Point2D p2 = this.vertices.get(i % n);
			double x1 = p1.getX();
			double y1 = p1.getY();
			double x2 = p2.getX();
			double y2 = p2.getY();
			double common = x1 * y2 - x2 * y1;
			
			sumX += (x1 + x2) * common;
			sumY += (y1 + y2) * common;
			sumC += common;
		}
		
		// the area is the sum of the common factors divided by 2, 
		// but we need to divide by 6 for centroid computation, 
		// resulting in a factor 3.
		sumC *= 6 / 2;
		return new Point2D.Double(sumX / sumC, sumY / sumC);
	}
	
	public boolean contains(Point2D point)
	{
		// the winding number counter for the point
        int wn = 0; 

        // Extract the last point of the collection
        Point2D previous = vertices.get(vertices.size() - 1);
        double y1 = previous.getY();
        double y2;

        // y-coordinate of query point
        double y = point.getY();

        // Iterate on couple of vertices, starting from couple (last,first)
        for (Point2D current : vertices) 
        {
            // second vertex of current edge
            y2 = current.getY();
            
			if (y1 <= y)
			{
				if (y2 > y) // an upward crossing
					if (isLeft(previous, current, point) > 0)
						wn++;
			} 
			else
			{
				if (y2 <= y) // a downward crossing
					if (isLeft(previous, current, point) < 0)
						wn--;
			}

            // for next iteration
            y1 = y2;
            previous = current;
        }

		if (this.signedArea() > 0)
		{
			return wn == 1;
		} else
		{
			return wn == 0;
		}
	}
	
    /**
	 * Tests if a point is Left|On|Right of an infinite line.
	 * 
	 * Returns:<ul> 
	 * <li> positive value for query point left of the line through P0 and P1</li> 
	 * <li> zero value for query point on the line </li>
	 * <li> negative for query point right of the line</li> 
	 * </ul>
	 * See: the January 2001 Algorithm "Area of 2D and 3D Triangles and Polygons"
	 * 
	 * @param p1
	 *            first point on the line (P1,P2)
	 * @param p2
	 *            second point on the line (P1,P2)
	 * @param pt
	 *            the query point
	 * @return >0 if query point is on left side of the line, <0 if query point
	 *         is on right side, and 0 if query point is on the line
	 */
	private final static int isLeft(Point2D p1, Point2D p2, Point2D pt)
	{
    	double x = p1.getX();
    	double y = p1.getY();
    	return (int) Math.signum(
    			(p2.getX() - x) * (pt.getY() - y) - (pt.getX() - x) * (p2.getY() - y));
    }
    
	/**
	 * Computes the complementary polygon, whose interior is the exterior of
	 * this polygon.
	 * 
	 * Keeps the same vertex as initial vertex.
	 * 
	 * @return the inverted polygon
	 */
	public Polygon2D invert()
	{
		int n = this.vertices.size();
		Polygon2D result = new Polygon2D(n);
		result.addVertex(this.getVertex(0));
		for (int i = n-1; i > 0; i--)
		{
			result.addVertex(vertices.get(i));
		}
		return result;
	}
	
    // ====================================================
    // GUI Tools
    
	/**
	 * Converts this polygon into an ImageJ Polygon ROI.
	 * 
	 * @return the corresponding PolygonRoi
	 */
	public PolygonRoi createRoi()
	{
		// allocate memory for data arrays
		int n = this.vertices.size();
		float[] px = new float[n];
		float[] py = new float[n];
		
		// extract coordinates
		for (int i = 0; i < n; i++)
		{
			Point2D p = this.vertices.get(i);
			px[i] = (float) p.getX();
			py[i] = (float) p.getY();
		}

		// create ROI data structure
		return new PolygonRoi(px, py, n, Roi.POLYGON);
	}

	
	// ====================================================
    // Management of vertices
    
	/**
	 * Returns the number of vertices within this polygon
	 * 
	 * @return the number of vertices within this polygon
	 */
	public int vertexNumber()
	{
		return this.vertices.size();
	}

	/**
	 * Adds a new vertex in this polygon
	 * 
	 * @param position
	 *            the position of the new vertex
	 * @return the index of the newly created vertex
	 */
	public int addVertex(Point2D position)
	{
		int n = this.vertices.size();
		this.vertices.add(position);
		return n;
	}
	
	/**
	 * Returns index at the specific index
	 * 
	 * @param index
	 *            vertex index
	 * @return the vertex at the specified index
	 */
	public Point2D getVertex(int index)
	{
		return this.vertices.get(index);
	}
	
	/**
	 * Changes vertex coordinate at the specified index
	 * 
	 * @param i
	 *            vertex index
	 * @param pos
	 *            the position of the new vertex
	 */
	public void setVertex(int i, Point2D pos)
	{
		this.setVertex(i, pos);
	}

	/**
	 * @return a reference to the inner array of vertices
	 */
	public ArrayList<Point2D> vertices()
	{
		return this.vertices;
	}
	
	/**
	 * @return an iterator over the vertices of this polygon
	 */
	@Override
	public Iterator<Point2D> iterator()
	{
		return this.vertices.iterator();
	}
}
