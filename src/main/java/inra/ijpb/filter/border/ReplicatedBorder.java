/**
 * 
 */
package inra.ijpb.filter.border;

import ij.process.ImageProcessor;

/**
 * Assess pixel outside image bounds have same value as the closest pixel
 * on image border.
 *  
 * @author David Legland
 *
 */
public class ReplicatedBorder implements BorderManager {

	ImageProcessor image;
	
	public ReplicatedBorder(ImageProcessor image) {
		this.image = image;
	}
	
	/** 
	 * Forces both of x and y to be between 0 and the corresponding image size, 
	 * and returns the corresponding image value. 
	 * @see inra.ijpb.filter.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y) {
		if (x < 0) x = 0;
		if (y < 0) y = 0;
		x = Math.min(x, image.getWidth() - 1);
		y = Math.min(y, image.getHeight() - 1);
		return this.image.get(x, y);
	}

}
