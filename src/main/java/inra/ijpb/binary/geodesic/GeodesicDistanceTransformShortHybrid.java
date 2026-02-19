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
package inra.ijpb.binary.geodesic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMask2D.ShortOffset;

/**
 * Computation of geodesic distances based on a chamfer mask using short integer
 * array for storing result.
 * 
 * This implementation also works on label maps as input.
 * 
 * All computations are performed using integers, results are stored as shorts.
 * The maximum propagated distance is limited to Short.MAX_VALUE.
 * 
 * @see GeodesicDistanceTransformFloatHybrid
 * 
 * @author David Legland
 * 
 */
public class GeodesicDistanceTransformShortHybrid extends AlgoStub implements GeodesicDistanceTransform
{
	// ==================================================
	// Class variables
	
	/**
	 * The value used to initialize the distance map.
	 */
	public static final short MAX_DIST = Short.MAX_VALUE;
	
	/**
	 * The value associated to the background in the result image.
	 */
	public static final short BACKGROUND = 0;
	
	/**
	 * The chamfer mask used for propagating distances from the marker.
	 */
	ChamferMask2D mask;
	
	/**
	 * Flag for dividing final distance map by the value first weight. 
	 * This results in distance map values closer to Euclidean distance. 
	 */
	boolean normalizeMap = true;


	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new algorithm for propagating geodesic distances.
	 * 
	 * @param mask
	 *            the chamfer mask to use for propagating distances
	 */
	public GeodesicDistanceTransformShortHybrid(ChamferMask2D mask) 
	{
		this.mask = mask;
	}
	
	/**
	 * Creates a new algorithm for propagating geodesic distances.
	 * 
	 * @param mask
	 *            the chamfer mask to use for propagating distances
	 * @param normalizeMap
	 *            the flag for normalization
	 */
	public GeodesicDistanceTransformShortHybrid(ChamferMask2D mask, boolean normalizeMap) 
	{
		this.mask = mask;
		this.normalizeMap = normalizeMap;
	}

	
	// ==================================================
	// Methods 
	
	/**
	 * Computes the geodesic distance function for each pixel in mask label
	 * image, using the given binary marker image. Mask and marker should be
	 * ImageProcessor the same size and containing integer values.
	 * 
	 * The function returns a new ShortProcessor the same size as the input,
	 * with values greater or equal to zero.
	 *
	 * @param marker
	 *            the binary marker image
	 * @param labelImage
	 *            the label image used as mask
	 * @return the geodesic distance map from the marker image within each label
	 *         of the mask
	 * @see inra.ijpb.binary.geodesic.GeodesicDistanceTransform#geodesicDistanceMap(ij.process.ImageProcessor,
	 *      ij.process.ImageProcessor)
	 */
	@Override
	public ImageProcessor geodesicDistanceMap(ImageProcessor marker, ImageProcessor labelImage)
	{
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		
		// create new empty image, and fill it with black
		fireStatusChanged(this, "Initialization..."); 
		ShortProcessor distMap = initialize(marker, labelImage);

		// forward iteration
		fireStatusChanged(this, "Forward iteration");
		forwardIteration(distMap, labelImage);

		// backward iteration
		fireStatusChanged(this, "Backward iteration"); 
		PriorityQueue<Record> queue = backwardIteration(distMap, labelImage);

		// Iterate while pixels have been modified
        fireStatusChanged(this, "Process queue"); 
        processQueue(distMap, labelImage, queue);

		// Normalize values by the first weight
		if (this.normalizeMap) 
		{
			fireStatusChanged(this, "Normalize map"); 
			normalizeResult(distMap, labelImage);
		}
		
		// Compute max value within the mask
		fireStatusChanged(this, "Normalize display"); 
		float maxVal = 0;
		for (int i = 0; i < sizeX; i++)
		{
			for (int j = 0; j < sizeY; j++)
			{
				short val = (short) distMap.get(i, j);
				if (val != MAX_DIST)
				{
					maxVal = Math.max(maxVal, val);
				}
			}
		}
		// System.out.println("max value: " + Float.toString(maxVal));

		// update and return resulting Image processor
		distMap.setMinAndMax(0, maxVal);
		// Forces the display to non-inverted LUT
		if (distMap.isInvertedLut())
		{
			distMap.invertLut();
		}
		
		return distMap;
	}

	private ShortProcessor initialize(ImageProcessor marker, ImageProcessor labelImage)
	{
		// size of image
		int sizeX = marker.getWidth();
		int sizeY = marker.getHeight();
		
		ShortProcessor distMap = new ShortProcessor(sizeX, sizeY);
		distMap.setValue(0);
		distMap.fill();

		// initialize empty image with either 0 (foreground) or Inf (background)
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				if (label == 0)
				{
					distMap.set(x, y, BACKGROUND);
				}
				else
				{
					distMap.set(x, y, marker.get(x, y) == 0 ? MAX_DIST : 0);
				}
			}
		}

		return distMap;
	}
	
	private void forwardIteration(ShortProcessor distMap, ImageProcessor labelImage) 
	{
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		Collection<ShortOffset> offsets = mask.getForwardOffsets();
		
		// Iterate over pixels
		for (int y = 0; y < sizeY; y++)
		{
			this.fireProgressChanged(this, y, sizeY);
			for (int x = 0; x < sizeX; x++)
			{
				// get current label
				int label = (int) labelImage.getf(x, y);
				
				// do not process background pixels
				if (label == 0)
					continue;
				
				// current distance value
				int currentDist = distMap.get(x, y);
				int newDist = currentDist;
				
				// iterate over neighbors
				for (ShortOffset offset : offsets)
				{
					// compute neighbor coordinates
					int x2 = x + offset.dx;
					int y2 = y + offset.dy;
					
					// check bounds
					if (x2 < 0 || x2 >= sizeX)
						continue;
					if (y2 < 0 || y2 >= sizeY)
						continue;
					
					if (((int) labelImage.getf(x2, y2)) == label)
					{
						// Increment distance
						newDist = Math.min(newDist, distMap.get(x2, y2) + offset.weight);
					}
				}
				
				if (newDist < currentDist) 
				{
					distMap.set(x, y, newDist);
				}
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
	}

	private PriorityQueue<Record> backwardIteration(ShortProcessor distMap, ImageProcessor labelImage)
	{
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		Collection<ShortOffset> offsets = mask.getBackwardOffsets();
		
		// initialize queue
        PriorityQueue<Record> queue = new PriorityQueue<>();
		ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>(offsets.size());
		
		// Iterate over pixels
		for (int y = sizeY-1; y >= 0; y--)
		{
			this.fireProgressChanged(this, sizeY-1-y, sizeY);
			for (int x = sizeX-1; x >= 0; x--)
			{
				// get current label
				int label = (int) labelImage.getf(x, y);
				
				// do not process background pixels
				if (label == 0)
					continue;
				
				// current distance value
				int currentDist = distMap.get(x, y);
				int newDist = currentDist;
				neighbors.clear();
				
				// iterate over neighbors
				for (ShortOffset offset : offsets)
				{
					// compute neighbor coordinates
					int x2 = x + offset.dx;
					int y2 = y + offset.dy;
					
					// check bounds
					if (x2 < 0 || x2 >= sizeX)
						continue;
					if (y2 < 0 || y2 >= sizeY)
						continue;
					
					if (((int) labelImage.getf(x2, y2)) == label)
					{
						// Increment distance
					    int dist2 = distMap.get(x2, y2);
						newDist = Math.min(newDist, dist2 + offset.weight);
						// update list of neighbors
						neighbors.add(new Neighbor(x2, y2, dist2, offset.weight));
					}
				}
				
				if (newDist < currentDist) 
				{
					distMap.set(x, y, newDist);
					
					// eventually add lower-right neighbors to queue
					for (Neighbor neighbor : neighbors)
					{
                        // compute new possible distance
                        int dist2 = newDist + neighbor.offsetWeight;
                        
                        // update queue
                        if (neighbor.value > dist2)
                        {
                            queue.offer(new Record(neighbor.x, neighbor.y, dist2));
                        }
					}
				}
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
		return queue;
	}
	
    private void processQueue(ShortProcessor distMap, ImageProcessor labelImage, PriorityQueue<Record> queue)
    {
        // size of image
        int sizeX = labelImage.getWidth();
        int sizeY = labelImage.getHeight();
        Collection<ShortOffset> offsets = mask.getOffsets();
        ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>(offsets.size());
        
//        System.out.println("  queue size: " + queue.size());
        while (!queue.isEmpty()) 
        {
            // get current position
            Record p = queue.poll();
            int x = p.x;
            int y = p.y;
            
            // retrieve properties of current pixel
            int label = (int) labelImage.getf(x, y);
            int currentDist = distMap.get(x, y);
            // check if current position was already updated
            if (currentDist <= p.value)
            {
                continue;
            }
            int newDist = currentDist;
            neighbors.clear();
            
            // iterate over (all) neighbors within chamfer mask
            for (ShortOffset offset : offsets)
            {
                // compute neighbor coordinates
                int x2 = x + offset.dx;
                int y2 = y + offset.dy;
                
                // check bounds
                if (x2 < 0 || x2 >= sizeX)
                    continue;
                if (y2 < 0 || y2 >= sizeY)
                    continue;
                
                if (((int) labelImage.getf(x2, y2)) == label)
                {
                    // Increment distance
                    int dist2 = distMap.get(x2, y2);
                    newDist = Math.min(newDist, dist2 + offset.weight);
                    // update list of neighbors
                    neighbors.add(new Neighbor(x2, y2, dist2, offset.weight));
                }
            }
            
            // Check if we need to update current pixel
            if (newDist < currentDist) 
            {
                distMap.set(x, y, newDist);
                
                // check if some neighbors must be added into queue
                for (Neighbor neighbor : neighbors)
                {
                    int dist2 = newDist + neighbor.offsetWeight;
                    
                    // update queue
                    if (neighbor.value > dist2)
                    {
                        queue.offer(new Record(neighbor.x, neighbor.y, dist2));
                    }
                }
            }
        }
    }
    
	private void normalizeResult(ShortProcessor distMap, ImageProcessor labelImage)
	{
		// size of image
		int sizeX = distMap.getWidth();
		int sizeY = distMap.getHeight();

        // retrieve the minimum weight
        double w0 = this.mask.getNormalizationWeight();
		
        // iterate over pixels within map to normalize values
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				if (((int) labelImage.getf(x, y)) == 0)
				{
					continue;
				}
				
				short val = (short) distMap.get(x, y);
				if (val != MAX_DIST)
				{
					distMap.set(x, y, (int) Math.round(val / w0));
				}
			}
		}
	}
	
	private class Neighbor
	{
	    int x;
	    int y;
	    
        int value;
        int offsetWeight;
	    
	    public Neighbor(int x, int y, int value, int offsetWeight)
	    {
	        this.x = x;
	        this.y = y;
	        this.value = value;
	        this.offsetWeight = offsetWeight;
	    }
	}
	
	/**
     * Records a position and its current distance value. The Record are
     * compared according to the inner value, and the comparator is not
     * consistent with equal (two instances may be different and compare to
     * zero).
     */
    private class Record implements Comparable<Record>
    {
        int x;
        int y;
        
        int value;
        
        public Record(int x, int y, int value)
        {
            this.x = x;
            this.y = y;
            this.value = value;
        }

        @Override
        public int compareTo(Record that)
        {
            return this.value - that.value;
        }
    }
}
