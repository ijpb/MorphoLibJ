/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.morphology.MinimaAndMaxima;

import java.awt.Point;
import java.util.ArrayList;
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
		// identify each maxima
		ImageProcessor maxima = MinimaAndMaxima.regionalMaxima(image, conn);
		
		// TODO: should be possible to replace computation of labels by
		// extraction of initial position for each regional maxima
		ImageProcessor labelImage = BinaryImages.componentsLabeling(maxima, conn, 32);
		
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		// count the number of maxima
		int nMaxima = 0;
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				nMaxima = Math.max(nMaxima, (int) labelImage.getf(x, y));
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
				int label = (int) labelImage.getf(x, y);
				if (label == 0)
				{
					continue;
				}
				
				Point pos = new Point(x, y);
				maximaPositionArray.set(label - 1, pos);
			}
		}
				
		ImageProcessor result = image.duplicate();
		
		// iterate over maxima
		for (int iMaxima = 0; iMaxima < nMaxima; iMaxima++)
		{
			// get current maxima
			Point pos0 = maximaPositionArray.get(iMaxima);
			
			// all the positions of the current maxima, all levels
			ArrayList<Point> positions = new ArrayList<Point>();

			// initialize neighbor list
			Queue<Point> queue = new PriorityQueue<Point>(new PositionValueComparator(result));
			queue.add(pos0);

			int nPixels = 0;
			int currentLevel = image.get(pos0.x, pos0.y);
			while(!queue.isEmpty())
			{
				// extract next neighbor around current regional maxim, in decreasing order of image value
				Point pos = queue.remove();

				// if neighbor corresponds to another maxima, stop iteration
				int neighborValue = result.get(pos.x, pos.y);
				if (neighborValue > currentLevel)
				{
					break;					
				}

				// add current neighbor to maxim neighborhood, and update level if necessary
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
						if (positions.contains(pos2))
						{
							continue;
						}

						queue.add(pos2);
					}
				}
			}
			
			// Replace the value of all pixel in maxim neighborhood by the last visited level
			for (Point pos : positions)
			{
				result.set(pos.x, pos.y, currentLevel);
			}
		}
		
		return result;
	}

	/**
	 * Compares positions within an image, by considering largest values with
	 * higher priority than smallest ones.
	 * 
	 * @author dlegland
	 *
	 */
	class PositionValueComparator implements Comparator<Point>
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
