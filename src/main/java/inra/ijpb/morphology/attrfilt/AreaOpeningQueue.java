/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.TreeMap;

import ij.IJ;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.morphology.MinimaAndMaxima;

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
	
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.attrfilt.AreaOpening#process(ij.process.ImageProcessor, int)
	 */
	@Override
	public ImageProcessor process(ImageProcessor image, int minArea)
	{
		// identify each maxima
		ImageProcessor maxima = MinimaAndMaxima.regionalMaxima(image, conn);
		ImageProcessor labelImage = BinaryImages.componentsLabeling(maxima, conn, 16);
		
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		// count the number of maxima
		int nMaxima = 0;
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				nMaxima = Math.max(nMaxima, labelImage.get(x, y));
			}
		}
		
		// initialize array of maxima position
		ArrayList<Point> maximaPositionArray = new ArrayList<Point>(nMaxima);
		for (int i = 0; i < nMaxima; i++)
		{
			maximaPositionArray.add(new Point());
		}
		
		// for each maxima, find a position
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				int label = labelImage.get(x, y);
				if (label == 0)
				{
					continue;
				}
				
				Point pos = new Point(x, y);
				maximaPositionArray.set(label - 1, pos);
			}
		}
		
		int[] dx = new int[]{0, -1, +1, 0};
		int[] dy = new int[]{-1, 0, 0, +1};
		
		ImageProcessor result = image.duplicate();
		
		// iterate over maxima
		for (int iMaxima = 0; iMaxima < nMaxima; iMaxima++)
		{
			// get current maxima
			Point pos0 = maximaPositionArray.get(iMaxima);
			int initialLevel = result.get(pos0.x, pos0.y);
			IJ.log("Process maxima at (" + pos0.x + "," + pos0.y + ")=" + initialLevel);
			
			// all the positions of the current maxima, all levels
			ArrayList<Point> positions = new ArrayList<Point>();

			// create the data structure to store neighbors, ordered by value, then
			// containing all positions of a given value connected to current
			// maxima
			TreeMap<Integer, Queue<Point>> mapQueue = new TreeMap<Integer, Queue<Point>>();

			// initialize neighbor list
			Queue<Point> queue = new ArrayDeque<Point>();
			queue.add(pos0);
			mapQueue.put(initialLevel, queue);

			int nPixels = 0;
			int currentLevel;
			while(true)
			{
				// process current gray level
				currentLevel = mapQueue.lastKey();
//				IJ.log("  Process level: " + currentLevel);
				queue = mapQueue.get(currentLevel);
				
				// iterate over neighbors with same gray level
				while(!queue.isEmpty())
				{
					Point pos = queue.remove();
					positions.add(pos);
					nPixels++;
					
					
					// add neighbors to queue
					for (int iNeigh = 0; iNeigh < dx.length; iNeigh++)
					{
						int x2 = pos.x + dx[iNeigh];
						int y2 = pos.y + dy[iNeigh];
						if (x2 >= 0 && x2 < sizeX && y2 >= 0 && y2 < sizeY)
						{
							Point pos2 = new Point(x2, y2);
							if (positions.contains(pos2))
							{
								continue;
							}
							
							int val = result.get(x2, y2);
							Queue<Point> queue2 = null;
							if (mapQueue.containsKey(val))
							{
								queue2 = mapQueue.get(val);
							}
							else
							{
								queue2 = new ArrayDeque<Point>();
								mapQueue.put(val, queue2);
							}
							queue2.add(pos2);
						}
					}
				}
				
				mapQueue.remove(currentLevel);
				
				if (nPixels >= minArea)
				{
//					IJ.log("  max number of pixels reached");
					break; 
				}
				
				if (mapQueue.lastKey() > currentLevel)
				{
//					IJ.log("  touches another maximum");
					break;					
				}
			}
			
			for (Point pos2 : positions)
			{
				result.set(pos2.x, pos2.y, currentLevel);
			}
		}
		
		return result;
	}

//	private Collection<Point> findComponentPositions(ImageProcessor image, Point pos0)
//	{
//		ArrayList<Point> positions = new ArrayList<Point>();
//		Queue<Point> queue = new ArrayDeque<Point>();
//
//		queue.add(pos0);
//		while (!queue.isEmpty())
//		{
//			Point pos = queue.remove();
//			positions.add(pos);
//			
//		}
//		
//		return positions;
//	}
	

}
