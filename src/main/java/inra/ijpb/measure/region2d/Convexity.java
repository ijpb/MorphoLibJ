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
/**
 * 
 */
package inra.ijpb.measure.region2d;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;

import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.geometry.Box2D;
import inra.ijpb.geometry.Polygon2D;
import inra.ijpb.geometry.Polygons2D;

/**
 * Computes convex area and convexity for regions within a binary or label
 * image.
 * 
 * @author dlegland
 *
 */
public class Convexity extends RegionAnalyzer2D<Convexity.Result>
{
    /**
     * Computes the binary image representing the convex hull of the input
     * binary image.
     *
     * @see inra.ijpb.geometry.Polygons2D
     * 
     * @param binaryImage
     *            the binary image of a region
     * @return the binary image representing the convex hull of the region in
     *         the input image.
     */
	public static final ImageProcessor convexify(ImageProcessor binaryImage)
	{
		// compute convex hull of boundary points around the binary particle
		ArrayList<Point2D> points = RegionBoundaries.boundaryPixelsMiddleEdges(binaryImage);
		Polygon2D convexHull = Polygons2D.convexHull(points);

		// create result image
		int sizeX = binaryImage.getWidth();
		int sizeY = binaryImage.getHeight();
		ImageProcessor result = new ByteProcessor(sizeX, sizeY);
		
		// determines bounds
		Box2D box = convexHull.boundingBox();
		int ymin = (int) Math.max(0, Math.floor(box.getYMin()));
		int ymax = (int) Math.min(sizeY, Math.ceil(box.getYMax()));
		int xmin = (int) Math.max(0, Math.floor(box.getXMin()));
		int xmax = (int) Math.min(sizeX, Math.ceil(box.getXMax()));
		
		// iterate over pixels within bounding box
		for (int y = ymin; y < ymax; y++)
		{
			for (int x = xmin; x < xmax; x++)
			{
				if (convexHull.contains(new Point2D.Double(x + 0.5, y + 0.5)))
				{
					result.set(x, y, 255);
				}
			}
		}
		
		return result;
	}
	
	@Override
	public ResultsTable createTable(Map<Integer, Convexity.Result> results)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : results.keySet())
		{
			// current diameter
			Result res = results.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			table.addValue("Area", res.area);
			table.addValue("ConvexArea", res.convexArea);
			table.addValue("Convexity", res.convexity);
		}
	
		return table;
	}

	@Override
	public Convexity.Result[] analyzeRegions(ImageProcessor image, int[] labels,
			Calibration calib)
	{
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		// calibrated area of a single pixel
		double pixelArea = calib.pixelWidth * calib.pixelHeight;
		
		// create result array
		Convexity.Result[] res = new Convexity.Result[labels.length];
		
        // compute convex hull of boundary points around each region
        ArrayList<Point2D>[] pointArrays = RegionBoundaries.boundaryPixelsMiddleEdges(image, labels);

        // iterate over labels
		for (int i = 0; i < labels.length; i++)
		{
			// compute convex hull of boundary points around the binary particle
            Polygon2D convexHull = Polygons2D.convexHull(pointArrays[i]);

			// determine bounds
			Box2D box = convexHull.boundingBox();
            int xmin = (int) Math.max(0, Math.floor(box.getXMin()));
            int xmax = (int) Math.min(sizeX, Math.ceil(box.getXMax()));
			int ymin = (int) Math.max(0, Math.floor(box.getYMin()));
			int ymax = (int) Math.min(sizeY, Math.ceil(box.getYMax()));
			
			double area = 0;
			double convexArea = 0;
			
			// iterate over pixels within bounding box
			for (int y = ymin; y < ymax; y++)
			{
				for (int x = xmin; x < xmax; x++)
				{
					if ((int) image.getf(x, y) == labels[i])
					{
						area++;
					}
					if (convexHull.contains(new Point2D.Double(x + 0.5, y + 0.5)))
					{
						convexArea++;
					}
				}
			}
			
			// calibrate measures
            area *= pixelArea;
            convexArea *= pixelArea;
            
            // save convexity measures for this label
            res[i] = new Convexity.Result(area, convexArea);
		}
		
		return res;
	}
	
    /**
     * Simple class for storing the results of convexity computations.
     */
	public class Result
	{
        /** The area of the region in the original image. */
        public double area;
        
        /** The area of the convex hull of the region. */
        public double convexArea;

        /**
         * The convexity of the region, computed as the ratio of area over
         * convex area.
         */
        public double convexity;
		
        /**
		 * Creates a new result for storing convexity, based on the area and the
		 * convex area measures for the region.
		 * 
		 * @param area
		 *            the area of the region
		 * @param convexArea
		 *            the convex area of the region
		 */
		public Result(double area, double convexArea)
		{
			this.area = area;
			this.convexArea = convexArea;
			this.convexity = area / convexArea;
		}
	}
}
