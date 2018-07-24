/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import ij.process.ImageProcessor;

/**
 * Utility functions for computing position of boundary points/corners of
 * regions within binary or label images.
 * 
 * @author dlegland
 *
 */
public class RegionBoundaries
{
	/**
	 * private constructor to prevent instantiations.
	 */
	private RegionBoundaries()
	{
	}

	/**
	 * Returns a set of points located at the corners of each region.
	 * Point coordinates are integer (ImageJ locates pixels in a [0 1]^d area.
	 * 
	 * @param image
	 *            a binary image representing the particle
	 * @param labels
	 *            the list of labels to process
	 * @return a list of points that can be used for convex hull computation
	 */
	public final static Map<Integer, ArrayList<Point2D>> runLengthsCornersMap(ImageProcessor image, int[] labels)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
        // For each label, create a list of corner points
        Map<Integer, ArrayList<Point2D>> labelCornerPoints = new TreeMap<Integer, ArrayList<Point2D>>();
        for (int label : labels)
        {
        	labelCornerPoints.put(label, new ArrayList<Point2D>());
        }
		
		// for each row, add corner point for first and last pixel of each run-length
		for (int y = 0; y < sizeY; y++)
		{
			// start from background
			int previous = 0;

			// Identify transition inside and outside the each label
			for (int x = 0; x < sizeX; x++)
			{
				int current = (int) image.getf(x, y);

				// check if we have a transition 
				if (current != previous)
				{
					// if leave a region, add a new corner points for the end of the region
					if (previous > 0)
					{
						ArrayList<Point2D> corners = labelCornerPoints.get(previous);
						Point2D p = new Point2D.Double(x, y);
						if (!corners.contains(p))
						{
							corners.add(p);
						}
						corners.add(new Point2D.Double(x, y+1));
					}
					
					// transition into a new region
					if (current > 0)
					{
						// add a new corner points for the beginning of the new region
						ArrayList<Point2D> corners = labelCornerPoints.get(current);
						Point2D p = new Point2D.Double(x, y);
						if (!corners.contains(p))
						{
							corners.add(p);
						}
						corners.add(new Point2D.Double(x, y+1));
					}
					
					// update current label
					previous = current;
				}
			}
			
			// if particle touches right border, add another point
			if (previous > 0)
			{
				ArrayList<Point2D> corners = labelCornerPoints.get(previous);
				Point2D p = new Point2D.Double(sizeX, y);
				if (!corners.contains(p))
				{
					corners.add(p);
				}
				corners.add(new Point2D.Double(sizeX, y+1));
			}
		}
		
		return labelCornerPoints;
	}

	/**
	 * Returns a set of points located at the corners of a binary particle.
	 * Point coordinates are integer (ImageJ locates pixels in a [0 1]^d area.
	 * 
	 * @param image
	 *            a binary image representing the particle
	 * @param labels
	 *            the list of labels to process
	 * @return for each label, an array of points
	 */
	public final static ArrayList<Point2D>[] runlengthsCorners(ImageProcessor image, int[] labels)
	{
		// Compute corner points for each label
		Map<Integer, ArrayList<Point2D>> cornerPointsMap = runLengthsCornersMap(image, labels);
		
		// allocate array
		int nLabels = labels.length;
		@SuppressWarnings("unchecked")
		ArrayList<Point2D>[] labelCornerPoints = (ArrayList<Point2D>[]) new ArrayList<?>[nLabels];
		
		// convert map to array
		for (int i = 0; i < nLabels; i++)
		{
			labelCornerPoints[i] = cornerPointsMap.get(labels[i]);
		}		
		
		return labelCornerPoints;
	}

	/**
	 * Returns a set of boundary points from a binary image.
	 * 
	 * @param image
	 *            a binary image representing the particle
	 * @return a list of points that can be used for convex hull computation
	 */
	public final static ArrayList<Point> runLengthsBoundaryPixels(ImageProcessor image)
	{
		// size of input image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		ArrayList<Point> points = new ArrayList<Point>();
		
		// try to find a pair of points for each row
		for (int y = 0; y < sizeY; y++)
		{
			// Identify transition inside and outside the particle 
			boolean inside = false;
			for (int x = 0; x < sizeX; x++)
			{
				if (image.get(x, y) > 0 && !inside)
				{
					// transition from background to foreground
					points.add(new Point(x, y));
					inside = true;
				} 
				else if (image.get(x, y) == 0 && inside)
				{
					// transition from foreground to background 
					points.add(new Point(x-1, y));
					inside = false;
				}
			}
			
			// if particle touches right border, add another point
			if (inside)
			{
				points.add(new Point(sizeX-1, y));
			}
		}
		
		return points;
	}
	
	/**
	 * Returns a set of points located at the corners of a binary particle.
	 * Point coordinates are integer (ImageJ locates pixels in a [0 1]^d area.
	 * 
	 * @param image
	 *            a binary image representing the particle
	 * @return a list of points that can be used for convex hull computation
	 */
	public final static ArrayList<Point2D> runLengthsCorners(ImageProcessor image)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		
		// try to find a pair of points for each row
		for (int y = 0; y < sizeY; y++)
		{
			// Identify transition inside and outside the particle 
			boolean inside = false;
			for (int x = 0; x < sizeX; x++)
			{
				int pixel = image.get(x, y);
				if (pixel > 0 && !inside)
				{
					// transition from background to foreground
					Point2D p = new Point2D.Double(x, y);
					if (!points.contains(p))
					{
						points.add(p);
					}
					points.add(new Point2D.Double(x, y+1));
					inside = true;
				} 
				else if (pixel == 0 && inside)
				{
					// transition from foreground to background 
					Point2D p = new Point2D.Double(x, y);
					if (!points.contains(p))
					{
						points.add(p);
					}
					points.add(new Point2D.Double(x, y+1));
					inside = false;
				}
			}
			
			// if particle touches right border, add another point
			if (inside)
			{
				Point2D p = new Point2D.Double(sizeX, y);
				if (!points.contains(p))
				{
					points.add(p);
				}
				points.add(new Point2D.Double(sizeX, y+1));
			}
		}
		
		return points;
	}

}
