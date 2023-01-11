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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;

/**
 * Utility functions for computing position of boundary points/corners of
 * regions within binary or label images.
 * 
 * The methods in this class are mostly used for computing convex hulls.
 * 
 * @see inra.ijpb.measure.region2d.Convexity
 * 
 * @author dlegland
 *
 */
public class RegionBoundaries
{
	/**
     * Returns a set of points located at the corners of a binary particle.
     * Point coordinates are integer (ImageJ locates pixels in a [0 1]^d area).
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

    /**
     * Returns a set of points located at the corners of a binary particle.
     * Point coordinates are integer (ImageJ locates pixels in a [0 1]^d area).
     * 
     * This methods computes the results as a Map, and converts the result into
     * an array.
     *
     * @see #runLengthsCornersMap(ImageProcessor, int[])
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
	 * Returns a set of points located at the corners of each region.
	 * Point coordinates are integer (ImageJ locates pixels in a [0 1]^2 area).
	 * 
     * @see #runlengthsCorners(ImageProcessor, int[])
     * 
	 * @param image
	 *            a binary image representing the particle
	 * @param labels
	 *            the list of labels to process
	 * @return a list of points that can be used for convex hull computation
	 */
	public final static Map<Integer, ArrayList<Point2D>> runLengthsCornersMap(ImageProcessor image, int[] labels)
	{
	    // get image size
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
					if (previous > 0 && labelCornerPoints.containsKey(previous))
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
					if (current > 0 && labelCornerPoints.containsKey(current))
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
			if (previous > 0 && labelCornerPoints.containsKey(previous))
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
     * Extracts boundary points from a binary region, keeping middle points of
     * pixel edges.
     * 
     * This method considers middle points of pixel edges, assuming a "diamond
     * shape" for pixels. For a single pixel (x,y), ImageJ considers equivalent
     * area to be [x,x+1[ x [y,y+1[, and pixel center at (x+0.5, y+0.5).
     * 
     * The boundaries extracted by this methods have following coordinates:
     * <ul>
     * <li><i>(x+0.5, y)</i>: top boundary</li>
     * <li><i>(x , y+0.5)</i>: left boundary</li>
     * <li><i>(x+1 , y+0.5)</i>: right boundary</li>
     * <li><i>(x+0.5, y+1)</i>: bottom boundary</li>
     * </ul>
     * 
     * @param binaryImage
     *            the image processor containing the binary region
     * @return an array of Point2D, located on the boundary of the region.
     */
	public static final ArrayList<Point2D> boundaryPixelsMiddleEdges(ImageProcessor binaryImage)
	{
		// size of image
		int sizeX = binaryImage.getWidth();
		int sizeY = binaryImage.getHeight();

		ArrayList<Point2D> points = new ArrayList<Point2D>();
		
		// boolean values within top-left, top-right, left, and current pixels
		boolean[] configValues = new boolean[4];
		
		// iterate on image pixel configurations
		for (int y = 0; y < sizeY + 1; y++) 
		{
		    // assume values outside image correspond to background
			configValues[2] = false;
			
			for (int x = 0; x < sizeX + 1; x++) 
			{
        		// update pixel values of configuration
				configValues[1] = x < sizeX & y > 0 ? (int) binaryImage.getf(x, y - 1) > 0: false;
				configValues[3] = x < sizeX & y < sizeY ? (int) binaryImage.getf(x, y) > 0: false;

				// check boundary with upper pixel
				if (configValues[1] != configValues[3])
				{
					points.add(new Point2D.Double(x + .5, y));
				}
				if (configValues[2] != configValues[3])
				{
					points.add(new Point2D.Double(x, y + .5));
				}

				// update values of configuration for next iteration
				configValues[2] = configValues[3];
			}
		}

		return points;
	}
	
    /**
     * Extracts boundary points from the different regions.
     * 
     * This method considers middle points of pixel edges, assuming a "diamond
     * shape" for pixels. For a single pixel (x,y), ImageJ considers equivalent
     * area to be [x,x+1[ x [y,y+1[, and pixel center at (x+0.5, y+0.5).
     * 
     * The boundaries extracted by this methods have following coordinates:
     * <ul>
     * <li><i>(x+0.5, y)</i>: top boundary</li>
     * <li><i>(x , y+0.5)</i>: left boundary</li>
     * <li><i>(x+1 , y+0.5)</i>: right boundary</li>
     * <li><i>(x+0.5, y+1)</i>: bottom boundary</li>
     * </ul>
     * 
     * @param labelImage
     *            the image processor containing the region labels
     * @param labels
     *            the array of region labels
     * @return an array of arrays of boundary points, one array for each label.
     */
    public static final ArrayList<Point2D>[] boundaryPixelsMiddleEdges(ImageProcessor labelImage, int[] labels)
    {
        // size of image
        int sizeX = labelImage.getWidth();
        int sizeY = labelImage.getHeight();
        
        int nLabels = labels.length;
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);
        
        // allocate data structure for storing results
        @SuppressWarnings("unchecked")
        ArrayList<Point2D>[] pointArrays = (ArrayList<Point2D>[]) new ArrayList<?>[nLabels];
        for (int i = 0; i < nLabels; i++)
        {
            pointArrays[i] = new ArrayList<Point2D>();
        }
        
        // labels for current, up, and left pixels.
        int label = 0;
        int labelUp = 0;
        int labelLeft = 0;
        
        // boolean values within top-left, top-right, left, and current pixels
        int[] configValues = new int[4];
        
        // iterate on image pixel configurations
        for (int y = 0; y < sizeY + 1; y++) 
        {
            // assume values outside image correspond to background
            configValues[2] = 0;
            
            for (int x = 0; x < sizeX + 1; x++) 
            {
                // update pixel values of configuration
                label = x < sizeX & y < sizeY ? (int) labelImage.getf(x, y): 0;
                labelUp = x < sizeX & y > 0 ? (int) labelImage.getf(x, y - 1): 0;

                // check boundary with upper pixel
                if (labelUp != label)
                {
                    Point2D p = new Point2D.Double(x + .5, y);
                    if (label != 0)
                    {
                        int index = labelIndices.get(label);
                        pointArrays[index].add(p);
                    }
                    if (labelUp != 0)
                    {
                        int index = labelIndices.get(labelUp);
                        pointArrays[index].add(p);
                    }
                }
                
                // check boundary with left pixel
                if (labelLeft != label)
                {
                    Point2D p = new Point2D.Double(x, y + .5);
                    if (label != 0)
                    {
                        int index = labelIndices.get(label);
                        pointArrays[index].add(p);
                    }
                    if (labelLeft != 0)
                    {
                        int index = labelIndices.get(labelLeft);
                        pointArrays[index].add(p);
                    }
                }

                // update values of left label for next iteration
                labelLeft = label;
            }
        }

        return pointArrays;
    }

    /**
     * private constructor to prevent instantiations.
     */
    private RegionBoundaries()
    {
    }
}
