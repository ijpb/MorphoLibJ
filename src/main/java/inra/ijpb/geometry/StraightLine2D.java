package inra.ijpb.geometry;

import java.awt.geom.Point2D;

public class StraightLine2D
{
	double x0;
	double y0;
	double dx;
	double dy;
	
	public StraightLine2D(Point2D origin, Vector2D direction)
	{
		this.x0 = origin.getX();
		this.y0 = origin.getY();
		this.dx = direction.getX();
		this.dy = direction.getY();
	}

	public StraightLine2D(Point2D source, Point2D target)
	{
		this.x0 = source.getX();
		this.y0 = source.getY();
		this.dx = target.getX() - this.x0;
		this.dy = target.getY() - this.y0;
	}
	
	
	public Point2D getOrigin()
	{
		return new Point2D.Double(this.x0, this.y0);
	}
	
	public Vector2D getDirection()
	{
		return new Vector2D(this.dx, this.dy);
	}
	

	public double distance(Point2D point)
	{
		// squared norm of direction, to check of validity
		double delta = (this.dx * this.dx + this.dy * this.dy);
		if (delta < 1e-12)
		{
			throw new RuntimeException("Direction vector of line is too small");
		}

		// difference of coordinates between point and line origin 
		double xDiff  = point.getX() - this.x0;
		double yDiff  = point.getY() - this.y0;

		// compute position of points projected on the supporting line, by using
		// normalized dot product
		double pos = (xDiff * this.dx + yDiff * this.dy) / delta;

		// compute distance between point and its projection on the edge
		double dist = Math.hypot(pos * this.dx - xDiff, pos * this.dy - yDiff);
		return dist;
	}
}
