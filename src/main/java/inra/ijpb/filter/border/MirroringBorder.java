/**
 * 
 */
package inra.ijpb.filter.border;

import ij.process.ImageProcessor;

/**
 * Periodic border that considers image is mirrored indefinitely in all
 * directions.
 * @author David Legland
 *
 */
public class MirroringBorder implements BorderManager {

	ImageProcessor image;
	
	public MirroringBorder(ImageProcessor image) {
		this.image = image;
	}
	
	/** 
	 * @see inra.ijpb.filter.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y) {
		int width = this.image.getWidth();
		int height = this.image.getHeight();
		x = x % (2 * width);
		y = y % (2 * height);
		if (x < 0)
			x = -x -1;
		if (y < 0)
			y = -y -1;
		if (x >= width)
			x = 2 * width - 1 - x;
		if (y >= height)
			y = 2 * height - 1 - y;
		return this.image.get(x, y);
	}

}
