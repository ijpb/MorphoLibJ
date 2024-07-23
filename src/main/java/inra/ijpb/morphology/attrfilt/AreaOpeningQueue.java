/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
package inra.ijpb.morphology.attrfilt;

import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.morphology.FloodFill;
import inra.ijpb.morphology.MinimaAndMaxima;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Area opening using priority queue for updating each regional maxima.
 * 
 * @author dlegland
 *
 */
public class AreaOpeningQueue extends AlgoStub implements AreaOpening
{
	/** Default connectivity is 4 */
	int conn  = 4;
	
	// the pixel shifts used to identify neighbors
	private int[] dx = new int[]{0, -1, +1, 0};
	private int[] dy = new int[]{-1, 0, 0, +1};

	/**
	 * Changes the connectivity of this algorithm.
	 * 
	 * @param connectivity the connectivity to use, either 4 or 8
	 */
	public void setConnectivity(int connectivity)
	{
		switch(connectivity)
		{
		case 4:
			this.dx = new int[]{0, -1, +1, 0};
			this.dy = new int[]{-1, 0, 0, +1};
			break;
			
		case 8:
			this.dx = new int[]{-1, 0, +1, -1, +1, -1, 0, +1};
			this.dy = new int[]{-1, -1, -1, 0, 0, +1, +1, +1};
			break;
			
		default:
			throw new IllegalArgumentException("Connectivity must be either 4 or 8, not " + connectivity);
		}
		
		this.conn = connectivity;
	}

	/**
	 * Returns the current connectivity value for this algorithm.
	 * 
	 * @return the current connectivity value (either 4 or 8)
	 */
	public int getConnectivity()
	{
		return this.conn;
	}
	
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.attrfilt.AreaOpening#process(ij.process.ImageProcessor, int)
	 */
	@Override
	public ImageProcessor process(ImageProcessor image, int minArea)
	{
		// extract image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		fireStatusChanged( this, "Finding maxima positions..." );
		// find position of maxima within image
		Collection<Point> maximaPositions = findMaximaPositions(image);
		
		// initialize result as copy of input image
		ImageProcessor result = image.duplicate();

		fireStatusChanged( this, "Iterating over maxima..." );
		double iter = 0;
		final double maxIter = maximaPositions.size();
		// iterate over maxima and update result image by removing maxima
		// with small area
		for (Point pos0 : maximaPositions)
		{
			iter++;
			fireProgressChanged( this, iter, maxIter );
			// accumulator for the positions of the current minimum,
			// all gray levels
			ArrayList<Point> positions = new ArrayList<Point>();

			// initialize list of neighbors of current maxima region
			Queue<Point> queue = new PriorityQueue<Point>(new PositionValueComparator(result));
			queue.add(pos0);

			// process pixels in the neighborhood
			int nPixels = 0;
			int currentLevel = image.get(pos0.x, pos0.y);
			while(!queue.isEmpty())
			{
				// extract next neighbor around current regional maximum, in decreasing order of image value
				Point pos = queue.remove();

				// if neighbor corresponds to another regional maximum, stop iteration
				int neighborValue = result.get(pos.x, pos.y);
				if (neighborValue > currentLevel)
				{
					break;					
				}

				// add current neighbor to regional maximum neighborhood, and update level if necessary
				positions.add(pos);
				nPixels++;
				currentLevel = neighborValue;

				// check size condition
				if (nPixels >= minArea)
				{
					break; 
				}
				
				// add neighbors to queue
				for (int iNeigh = 0; iNeigh < this.dx.length; iNeigh++)
				{
					int x2 = pos.x + this.dx[iNeigh];
					int y2 = pos.y + this.dy[iNeigh];
					if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY)
					{
						Point pos2 = new Point(x2, y2);
						// Check if position was already processed
						if (!positions.contains(pos2) && !queue.contains(pos2))
						{
							queue.add(pos2);
						}
					}
				}
			}
			
			// Replace the value of all pixel in regional maximum neighborhood by the last visited level
			for (Point pos : positions)
			{
				result.set(pos.x, pos.y, currentLevel);
			}
		}
		fireProgressChanged( this, 1, 1 );
		return result;
	}

	private Collection<Point> findMaximaPositions(ImageProcessor image)
	{
		// identify each maxima
		ImageProcessor maxima = MinimaAndMaxima.regionalMaxima(image, conn);
		
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		Collection<Point> positions = new ArrayList<Point>();
		
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				// if current pixel is a regional maximum, keep the position,
				// and remove the regional maximum
				if (maxima.get(x, y) > 0)
				{
					positions.add(new Point(x, y));
					FloodFill.floodFill(maxima, x, y, 0, this.conn);
				}
			}
		}

		return positions;
	}

	
	/**
	 * Compares positions within an image, by considering largest values with
	 * higher priority than smallest ones.
	 * 
	 * @author dlegland
	 *
	 */
	private class PositionValueComparator implements Comparator<Point>
	{
		ImageProcessor image;
		PositionValueComparator(ImageProcessor image)
		{
			this.image = image;
		}
		
		@Override
		public int compare(Point pos1, Point pos2)
		{
			int val1 = image.get(pos1.x, pos1.y);
			int val2 = image.get(pos2.x, pos2.y);
			if (val1 > val2)
			{
				return -1;
			}
			if (val2 > val1)
			{
				return +1;
			}
			return 0;
		}
	}
}
