/**
 * 
 */
package inra.ijpb.morphology;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Point;
import java.util.ArrayList;

import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * Implements various flood-filling algorithms, for planar images and 3D stacks.
 * Rewritten from ij.process.FloodFiller and adapted to 3D stacks. Also support
 * floating point images and stacks.
 * 
 * Check also "http://en.wikipedia.org/wiki/Flood_fill".
 * 
 * @see MinimaAndMaxima
 * @see MinimaAndMaxima3D
 * 
 * @author David Legland
 */
public class FloodFill {

	/**
	 * Replaces all the neighbor pixels of (x,y) that have the same values
	 * by the specified value.
	 */
	public final static void floodFillC4(ImageProcessor image, int x, int y, int value) {
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		// get old value
		int oldValue = image.getPixel(x, y);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) {
			// Extract current position
			Point p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			
			// process only pixel with the same value
			if (image.get(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && image.getPixel(x1-1, y) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < width - 1 && image.getPixel(x2+1, y) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(image, y, x1, x2, value);
					
			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					int val = image.get(i, y - 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines below the current one
			if (y < height - 1) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					int val = image.getPixel(i, y + 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
		}

	}

	/**
	 * Replaces all the pixels in the 4-neighborhood of (x,y) that have the 
	 * same values as the pixel in (x,y) by the specified floating point value.
	 * Should work the same way for all type of images.
	 */
	public final static void floodFillC4(ImageProcessor image, int x, int y, float value) {
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		// get old value
		float oldValue = image.getf(x, y);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));
		
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) {
			// Extract current position
			Point p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			
			// process only pixel of the same value
			if (image.getf(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && image.getf(x1-1, y) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < width - 1 && image.getf(x2+1, y) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(image, y, x1, x2, value);
			
			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					float val = image.getf(i, y - 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines below the current one
			if (y < height - 1) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					float val = image.getf(i, y + 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
		}
	}

	/**
	 * Replaces all the pixels in the 8-neighborhood of (x,y) that have the 
	 * same values by the specified value.
	 */
	public final static void floodFillC8(ImageProcessor image, int x, int y, int value) {
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		// get old value
		int oldValue = image.getPixel(x, y);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) {
			// Extract current position
			Point p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			
			// process only pixel with the same value
			if (image.get(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && image.getPixel(x1-1, y) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < width - 1 && image.getPixel(x2+1, y) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(image, y, x1, x2, value);
					
			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = Math.max(x1-1, 0); i <= Math.min(x2+1, width-1); i++) {
					int val = image.get(i, y - 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines below the current one
			if (y < height - 1) {
				inScanLine = false;
				for (int i = Math.max(x1-1, 0); i <= Math.min(x2+1, width-1); i++) {
					int val = image.getPixel(i, y + 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
		}

	}

	/**
	 * Replaces all the pixels in the 8-neighborhood of (x,y) that have the 
	 * same values as the pixel in (x,y) by the specified value.
	 * Should work for all integer based images: ByteProcessor, ShortProcessor 
	 * and ColorProcessor. 
	 */
	public final static void floodFillC8(ImageProcessor image, int x, int y, float value) {
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		// get old value
		float oldValue = image.getf(x, y);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) {
			// Extract current position
			Point p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			
			// process only pixel with the same value
			if (image.getf(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && image.getf(x1-1, y) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < width - 1 && image.getf(x2+1, y) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(image, y, x1, x2, value);
					
			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = Math.max(x1-1, 0); i <= Math.min(x2+1, width-1); i++) {
					float val = image.getf(i, y - 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines below the current one
			if (y < height - 1) {
				inScanLine = false;
				for (int i = Math.max(x1-1, 0); i <= Math.min(x2+1, width-1); i++) {
					float val = image.getf(i, y + 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
		}

	}

	/**
	 * Assign to all the neighbor pixels of (x,y,z) that have the same pixel value 
	 * in <code>image</code>, the specified new label value (<code>value</code>) 
	 * in <code>labelImage</code>, using the specified connectivity.
	 * 
	 * @param inputImage original image to read the pixel values from
	 * @param x x- coordinate of pixel of interest
	 * @param y y- coordinate of pixel of interest
	 * @param outputImage output label image (to fill) 
	 * @param value filling value
	 * @param conn connectivity to use (4-8)
	 */
	public final static void floodFill(ImageProcessor inputImage, int x, int y,
			ImageProcessor outputImage, int value, int conn) {
		
		// the shifts to look for new markers to start lines
		int dx1 = 0;
		int dx2 = 0;
		if (conn == 8) {
			dx1 = -1;
			dx2 = +1;
		}
		
		// get image size
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		
		// get old value
		int oldValue = inputImage.getPixel(x, y);
				
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));
		
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) {
			// Extract current position
			Point p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			
			// process only pixel of the same value
			if (inputImage.get(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			//TODO: find cleaner way of writing this
			while (x1 >= 0 && inputImage.getPixel(x1, y) == oldValue) 
				x1--;
			x1++;
			
			// find end of scan-line
			//TODO: find cleaner way of writing this
			while (x2 < width && inputImage.getPixel(x2, y) == oldValue) 
				x2++;                   
			x2--;
			
			// fill current scan-line
			fillLine(outputImage, y, x1, x2, value);
			
			
			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++) {
					int val = inputImage.get(i, y - 1);
					int lab = (int) outputImage.get(i, y - 1);
					if (!inScanLine && val == oldValue && lab != value) {
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines below the current one
			if (y < height - 1) {
				inScanLine = false;
//				for (int i = x1; i <= x2; i++) {
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++) {
					int val = inputImage.getPixel(i, y + 1);
					int lab = (int) outputImage.get(i, y + 1);
					if (!inScanLine && val == oldValue && lab != value) {
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
		}
	}

	private final static void fillLine(ImageProcessor ip, int y, int x1, int x2, int value) {
		if (x1 > x2) {
			int t = x1;
			x1 = x2;
			x2 = t;
		}
		for (int x = x1; x <= x2; x++)
			ip.set(x, y, value);
	}

	/**
	 * Assign to all the neighbor pixels of (x,y,z) that have the same pixel value 
	 * in <code>image</code>, the specified new label value (<code>value</code>) 
	 * in <code>labelImage</code>, using the specified connectivity.
	 * 
	 * @param inputImage original image to read the pixel values from
	 * @param x x- coordinate of pixel of interest
	 * @param y y- coordinate of pixel of interest
	 * @param outputImage output label image (to fill) 
	 * @param value filling value
	 * @param conn connectivity to use (4-8)
	 */
	public final static void floodFill(ImageProcessor inputImage, int x, int y,
			ImageProcessor outputImage, float value, int conn) {
		
		// the shifts to look for new markers to start lines
		int dx1 = 0;
		int dx2 = 0;
		if (conn == 8) {
			dx1 = -1;
			dx2 = +1;
		}
		
		// get image size
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		
		// get old value
		int oldValue = inputImage.getPixel(x, y);
		
		// initialize the stack with original pixel
		ArrayList<Point> stack = new ArrayList<Point>();
		stack.add(new Point(x, y));
		
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) {
			// Extract current position
			Point p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			
			// process only pixel of the same value
			if (inputImage.get(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 >= 0 && inputImage.get(x1, y) == oldValue && outputImage.getf(x1, y) == 0) 
				x1--;
			x1++;
			
			// find end of scan-line
			while (x2 < width && inputImage.get(x2, y) == oldValue && outputImage.getf(x1, y) == 0) 
				x2++;                   
			x2--;
			
			// fill current scan-line
			fillLine(outputImage, y, x1, x2, value);
			
			
			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++) {
					int val = inputImage.get(i, y - 1);
					float lab = outputImage.getf(i, y - 1);
					if (!inScanLine && val == oldValue && lab == 0) {
						stack.add(new Point(i, y - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines below the current one
			if (y < height - 1) {
				inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++) {
					int val = inputImage.getPixel(i, y + 1);
					float lab = outputImage.getf(i, y + 1);
					if (!inScanLine && val == oldValue && lab == 0) {
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
		}
	}

	/**
	 * Fill in the horizontal line define by y-coordinate and the two x 
	 * coordinate extremities (inclusive), with the specified integer value.
	 * the value x1 must be lower than or equal the value x2. 
	 */
	private final static void fillLine(ImageProcessor ip, int y, int x1, int x2, float value) {
		for (int x = x1; x <= x2; x++)
			ip.setf(x, y, value);
	}

	/**
	 * Replaces all the pixels in the 6-neighborhood of (x,y,z) that have the 
	 * same values as the pixel in (x,y,z) by the specified value.
	 * Should work for all integer based images: ByteProcessor, ShortProcessor 
	 * and ColorProcessor. 
	 */
	public final static void floodFillC6(ImageStack image, int x, int y, int z, int value) {
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// get old value
		int oldValue = (int) image.getVoxel(x, y, z);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x, y, z));
		
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) {
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			z = p.z;
			
			// process only pixel of the same value
			if (image.getVoxel(x, y, z) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && image.getVoxel(x1-1, y, z) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && image.getVoxel(x2+1, y, z) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(image, x1, x2, y, z, value);
			
			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					int val = (int) image.getVoxel(i, y - 1, z);
					if (!inScanLine && val == oldValue) {
						stack.add(new Cursor3D(i, y - 1, z));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines below the current one
			if (y < sizeY - 1) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					int val = (int) image.getVoxel(i, y + 1, z);
					if (!inScanLine && val == oldValue) {
						stack.add(new Cursor3D(i, y + 1, z));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}

			// find scan-lines in front of the current one
			if (z > 0) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					int val = (int) image.getVoxel(i, y, z - 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Cursor3D(i, y, z - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines behind the current one
			if (z < sizeZ - 1) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					int val = (int) image.getVoxel(i, y, z + 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Cursor3D(i, y, z + 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
		}

	}

	/**
	 * Replaces all the pixels in the 4-neighborhood of (x,y) that have the 
	 * same values as the pixel in (x,y) by the specified floating point value.
	 * Should work the same way for all type of images.
	 */
	public final static void floodFillC6(ImageStack image, int x, int y, int z, double value) {
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// get old value
		double oldValue = image.getVoxel(x, y, z);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x, y, z));
		
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) {
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			z = p.z;
			
			// process only pixel of the same value
			if (image.getVoxel(x, y, z) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && image.getVoxel(x1-1, y, z) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && image.getVoxel(x2+1, y, z) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(image, x1, x2, y, z, value);
			
			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					double val = image.getVoxel(i, y - 1, z);
					if (!inScanLine && val == oldValue) {
						stack.add(new Cursor3D(i, y - 1, z));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines below the current one
			if (y < sizeY - 1) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					double val = image.getVoxel(i, y + 1, z);
					if (!inScanLine && val == oldValue) {
						stack.add(new Cursor3D(i, y + 1, z));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines in front of the current one
			if (z > 0) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					double val = image.getVoxel(i, y, z - 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Cursor3D(i, y, z - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines behind the current one
			if (z < sizeZ - 1) {
				inScanLine = false;
				for (int i = x1; i <= x2; i++) {
					double val = image.getVoxel(i, y, z + 1);
					if (!inScanLine && val == oldValue) {
						stack.add(new Cursor3D(i, y, z + 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
		}
	}

	/**
	 * Replaces all the pixels in the 8-neighborhood of (x,y) that have the 
	 * same values as the pixel in (x,y) by the specified value.
	 * Should work for all integer based images: ByteProcessor, ShortProcessor 
	 * and ColorProcessor. 
	 */
	public final static void floodFillC26(ImageStack image, int x, int y, int z, int value) {
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// get old value
		int oldValue = (int) image.getVoxel(x, y, z);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x, y, z));
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) {
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			z = p.z;
			
			// process only pixel with the same value
			if (image.getVoxel(x, y, z) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && image.getVoxel(x1-1, y, z) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && image.getVoxel(x2+1, y, z) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(image, x1, x2, y, z, value);
			
			// check the eigth X-lines around the current one
			for (int z2 = max(z - 1, 0); z2 <= min(z + 1, sizeZ - 1); z2++) {
				for (int y2 = max(y - 1, 0); y2 <= min(y + 1, sizeY - 1); y2++) {
					// do not process the middle line
					if (y2 == z && y2 == y)
						continue;
					
					inScanLine = false;
					for (int i = max(x1-1, 0); i <= min(x2+1, sizeX-1); i++) {
						int val = (int) image.getVoxel(i, y2, z2);
						if (!inScanLine && val == oldValue) {
							stack.add(new Cursor3D(i, y2, z2));
							inScanLine = true;
						} else if (inScanLine && val != oldValue)
							inScanLine = false;
					}
					
				}
			} // end of iteration on neighbor lines

		} // end of iteration on position stack

	}

	/**
	 * Replaces all the pixels in the 26-neighborhood of (x,y,z) that have the 
	 * same values as the pixel in (x,y,z) by the specified value.
	 * This version is dedicated to floating-point stacks. 
	 */
	public final static void floodFillC26(ImageStack image, int x, int y, int z, double value) {
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// get old value
		double oldValue = image.getVoxel(x, y, z);
		
		// test if already the right value 
		if (oldValue == value) 
			return ;
		
		// initialize the stack with original pixel
		ArrayList<Cursor3D> stack = new ArrayList<Cursor3D>();
		stack.add(new Cursor3D(x, y, z));
		
		boolean inScanLine;
		
		// process all items in stack
		while (!stack.isEmpty()) {
			// Extract current position
			Cursor3D p = stack.remove(stack.size()-1);
			x = p.x;
			y = p.y;
			z = p.z;
			
			// process only pixel with the same value
			if (image.getVoxel(x, y, z) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 && image.getVoxel(x1-1, y, z) == oldValue)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 && image.getVoxel(x2+1, y, z) == oldValue)
				x2++;
			
			// fill current scan-line
			fillLine(image, x1, x2, y, z, value);
					
			// check the eigth X-lines around the current one
			for (int z2 = max(z - 1, 0); z2 <= min(z + 1, sizeZ - 1); z2++) {
				for (int y2 = max(y - 1, 0); y2 <= min(y + 1, sizeY - 1); y2++) {
					// do not process the middle line
					if (y2 == z && y2 == y)
						continue;
					
					inScanLine = false;
					for (int i = max(x1-1, 0); i <= min(x2+1, sizeX-1); i++) {
						double val = image.getVoxel(i, y2, z2);
						if (!inScanLine && val == oldValue) {
							stack.add(new Cursor3D(i, y2, z2));
							inScanLine = true;
						} else if (inScanLine && val != oldValue)
							inScanLine = false;
					}
					
				}
			} // end of iteration on neighbor lines

		} // end of iteration on position stack

	}

	/**
	 * Fill in the horizontal line define by y-coordinate and the two x 
	 * coordinate extremities (inclusive), with the specified integer value.
	 * the value x1 must be lower than or equal the value x2. 
	 */
	private final static void fillLine(ImageStack ip, int x1, int x2, int y, int z, int value) {
		for (int x = x1; x <= x2; x++)
			ip.setVoxel(x, y, z, value);
	}

	/**
	 * Fill in the horizontal line define by y-coordinate and the two x 
	 * coordinate extremities (inclusive), with the specified integer value.
	 * the value x1 must be lower than or equal the value x2. 
	 */
	private final static void fillLine(ImageStack ip, int x1, int x2, int y, int z, double value) {
		for (int x = x1; x <= x2; x++)
			ip.setVoxel(x, y, z, value);
	}

	/**
	 * Defines a position within a 3D stack.
	 * Need to be a static class to be called by static methods.
	 */
	private static class Cursor3D {
		int x;
		int y;
		int z;
		public Cursor3D(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}
