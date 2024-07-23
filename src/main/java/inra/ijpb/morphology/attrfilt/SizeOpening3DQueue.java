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

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.Neighborhood3D;
import inra.ijpb.data.Neighborhood3DC26;
import inra.ijpb.data.Neighborhood3DC6;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.FloodFill3D;
import inra.ijpb.morphology.MinimaAndMaxima3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Volume opening on 3D grayscale image using priority queue for updating each
 * 3D regional maxima.
 * 
 * @author dlegland
 *
 */
public class SizeOpening3DQueue extends AlgoStub implements SizeOpening3D
{
	/** Default connectivity is 6 */
	int conn = 6;
	
	/**
	 * Changes the connectivity used by this algorithm.
	 * 
	 * @param connectivity the connectivity to use, either 6 or 26
	 */
	public void setConnectivity(int connectivity)
	{
		if (conn != 6 && conn != 26)
		{
			throw new IllegalArgumentException("Connectivity must be either 6 or 26, not " + connectivity);
		}
		
		this.conn = connectivity;
	}

	/**
	 * Returns the current connectivity value for this algorithm.
	 * 
	 * @return the current connectivity value (either 6 or 26)
	 */
	public int getConnectivity()
	{
		return this.conn;
	}
	
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.attrfilt.SizeOpening3D#process(ij.process.ImageStack, int)
	 */
	@Override
	public ImageStack process(ImageStack image, int minVolume)
	{
		// extract image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		Neighborhood3D neigh;
		switch (this.conn)
		{
		case 6: 
			neigh = new Neighborhood3DC6();
			break;
		case 26: 
			neigh = new Neighborhood3DC26();
			break;
		default:
			throw new RuntimeException("Unknown connectivity: " + this.conn);
		}

		// find position of maxima within image
		fireStatusChanged( this, "Finding maxima positions..." );
		Collection<Cursor3D> maximaPositions = findMaximaPositions(image);
		
		// initialize result as copy of input image
		ImageStack result = image.duplicate();
		Image3D result2 = Images3D.createWrapper(result);

		fireStatusChanged( this, "Iterating over maxima..." );
		double iter = 0;
		final double maxIter = maximaPositions.size();
		// iterate over maxima and update result image by removing maxima with small area
		for (Cursor3D pos0 : maximaPositions)
		{
			iter++;
			fireProgressChanged( this, iter, maxIter );
			// accumulator for the positions of the current maxima, all gray levels
			ArrayList<Cursor3D> positions = new ArrayList<Cursor3D>();

			// initialize list of neighbors of current maxima region
			Queue<Cursor3D> queue = new PriorityQueue<Cursor3D>(new Position3DValueComparator(result));
			queue.add(pos0);

			// process voxels in the neighborhood
			int nPixels = 0;
			double currentLevel = result2.getValue(pos0.getX(), pos0.getY(), pos0.getZ());
			while(!queue.isEmpty())
			{
				// extract next neighbor around current regional maximum, in decreasing order of image value
				Cursor3D pos = queue.remove();

				// if neighbor corresponds to another regional maximum, stop iteration
				double neighborValue = result2.getValue(pos.getX(), pos.getY(), pos.getZ());
				if (neighborValue > currentLevel)
				{
					break;					
				}

				// add current neighbor to regional maximum neighborhood, and update level if necessary
				positions.add(pos);
				nPixels++;
				currentLevel = neighborValue;

				// check size condition
				if (nPixels >= minVolume)
				{
					break; 
				}
				
				// add neighbors to queue
				neigh.setCursor(pos);
				for (Cursor3D pos2 : neigh.getNeighbors())
				{
					int x2 = pos2.getX();
					int y2 = pos2.getY();
					int z2 = pos2.getZ();
					if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY && z2 >= 0 && z2 < sizeZ)
					{
						// Check if position was already processed
						if (!positions.contains(pos2) && !queue.contains(pos2))
						{
							queue.add(pos2);
						}
					}
				}
			}
			
			// Replace the value of all voxel in regional maximum neighborhood
			// by the value of the lowest level
			for (Cursor3D pos : positions)
			{
				result2.setValue(pos.getX(), pos.getY(), pos.getZ(), currentLevel);
			}
		}
		fireProgressChanged( this, 1, 1 );
		return result;
	}

	private Collection<Cursor3D> findMaximaPositions(ImageStack image)
	{
		// identify each maxima
		ImageStack maxima = MinimaAndMaxima3D.regionalMaxima(image, conn);
		Image3D maxima2 = Images3D.createWrapper(maxima);
		
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		Collection<Cursor3D> positions = new ArrayList<Cursor3D>();
		
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					// if current pixel is a regional maximum, keep the position,
					// and remove the regional maximum
					if (maxima2.getValue(x, y, z) > 0)
					{
						positions.add(new Cursor3D(x, y, z));
						FloodFill3D.floodFill(maxima, x, y, z, 0, this.conn);
					}
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
	class Position3DValueComparator implements Comparator<Cursor3D>
	{
		Image3D image;
		
		Position3DValueComparator(ImageStack image)
		{
			this.image = Images3D.createWrapper(image);
		}
		
		@Override
		public int compare(Cursor3D pos1, Cursor3D pos2)
		{
			int val1 = image.get(pos1.getX(), pos1.getY(), pos1.getZ());
			int val2 = image.get(pos2.getX(), pos2.getY(), pos2.getZ());
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
