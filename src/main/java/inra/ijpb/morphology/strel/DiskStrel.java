/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.plugin.filter.RankFilters;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * Disk structuring element. This class is a wrapper for the ImageJ native
 * RankFilters() method, that uses disk neighborhood.
 * 
 * @author David Legland
 *
 */
public class DiskStrel extends AbstractInPlaceStrel implements InPlaceStrel {

	int radius;
	
	public final static DiskStrel fromRadius(int radius) {
		return new DiskStrel(radius);
	}
	
	private DiskStrel(int radius) {
		this.radius = radius;
	}
	
	/* (non-Javadoc)
	 * @see ijt.filter.morphology.Strel#getSize()
	 */
	@Override
	public int[] getSize() {
		int diam = 2 * radius + 1;
		return new int[]{diam, diam};
	}

	/* (non-Javadoc)
	 * @see ijt.filter.morphology.Strel#getMask()
	 */
	@Override
	public int[][] getMask() 
	{
		// Create an empty image with just a white pixel in the middle
		int size = 2 * radius + 1;
		ImageProcessor img = new ByteProcessor(size, size);
		img.set(radius, radius, 255);
		
		// apply dilation
		this.inPlaceDilation(img);
		
		// convert to int array
		int[][] mask = new int[size][size];
		for (int y = 0; y < size; y++) 
		{
			for (int x = 0; x < size; x++)
			{
				mask[y][x] = img.get(x, y);
			}
		}

		return mask;
	}

	/* (non-Javadoc)
	 * @see ijt.filter.morphology.Strel#getOffset()
	 */
	@Override
	public int[] getOffset() {
		return new int[]{radius, radius};
	}

	/* (non-Javadoc)
	 * @see ijt.filter.morphology.Strel#getShifts()
	 */
	@Override
	public int[][] getShifts() {
		int[][] mask = getMask();
		int size = 2 * radius + 1;
		
		int n = 0;
		for (int y = 0; y < size; y++)
		{
			for (int x = 0; x < size; x++)
			{
				if (mask[y][x] > 0)
					n++;
			}
		}
		
		int[][] offsets = new int[n][2];
		int i = 0;
		for (int y = 0; y < size; y++)
		{
			for (int x = 0; x < size; x++)
			{
				if (mask[y][x] > 0)
				{
					offsets[i][0] = x;
					offsets[i][1] = y;
					i++;
				}
			}
		}
		
		return offsets;
	}

	/* (non-Javadoc)
	 * @see ijt.filter.morphology.Strel#reverse()
	 */
	@Override
	public DiskStrel reverse() {
		return new DiskStrel(radius);
	}


	@Override
	public void inPlaceDilation(ImageProcessor image) {
		new RankFilters().rank(image, radius, RankFilters.MAX);
	}

	@Override
	public void inPlaceErosion(ImageProcessor image) {
		new RankFilters().rank(image, radius, RankFilters.MIN);
	}
}
