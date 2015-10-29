/**
 * 
 */
package inra.ijpb.data.border;

import ij.process.ImageProcessor;

/**
 * Periodic border that considers image is repeated indefinitely in all
 * directions.
 * @author David Legland
 *
 */
public class PeriodicBorder implements BorderManager {

	ImageProcessor image;
	
	public PeriodicBorder(ImageProcessor image) {
		this.image = image;
	}
	
	/** 
	 * @see inra.ijpb.data.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y) {
		x = x % image.getWidth();
		y = y % image.getHeight();
		if (x < 0)
			x += image.getWidth();
		if (y < 0)
			y += image.getHeight();
		return this.image.get(x, y);
	}

}
