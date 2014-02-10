/**
 * 
 */
package inra.ijpb.morphology;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.*;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Point;
import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Computes label image of connected components in a binary planar image or 3D
 * stack. The dialog provides an option to choose data type of output image.
 * 
 * @author David Legland
 * 
 */
public class LabelingPlugin implements PlugIn {

	private final static String[] conn2DLabels = {"4", "8"};
	private final static int[] conn2DValues = {4, 8};
	private final static String[] conn3DLabels = {"6", "26"};
	private final static int[] conn3DValues = {6, 26};
	
	private final static String[] resultBitDepthLabels = {"8 bits", "16 bits", "float"};
	private final static int[] resultBitDepthList = {8, 16, 32}; 

	@Override
	public void run(String arg) {
		ImagePlus imagePlus = IJ.getImage();
		
		GenericDialog gd = new GenericDialog("Labeling");
		int nSlices = imagePlus.getStackSize();
		String[] connLabels = nSlices == 1 ? conn2DLabels : conn3DLabels;
		gd.addChoice("Connectivity", connLabels, connLabels[0]);
		gd.addChoice("Type of result", resultBitDepthLabels, resultBitDepthLabels[1]);
		gd.showDialog();
		
		if (gd.wasCanceled()) 
			return;

		int connIndex = gd.getNextChoiceIndex();
		int bitDepth = resultBitDepthList[gd.getNextChoiceIndex()];

		ImagePlus resultPlus;
		if (nSlices == 1) {
			// Process planar image
			int conn = conn2DValues[connIndex];
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor labelImage = computeLabelsByte(image, conn);
			resultPlus = new ImagePlus("labels", labelImage);
			resultPlus.show();
			
		} else {
			// Process 3D image stack
			int conn = conn3DValues[connIndex];
			resultPlus = computeLabels3D(imagePlus, conn, bitDepth);
			resultPlus.show();
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}

	/**
	 * Same algorithm as computeLabels(ImageStack,int,int), but operates on an 
	 * ImagePlus, making it possible to set up min and max values of final
	 * image display.
	 * @param imagePlus contains the 3D binary image stack
	 * @param conn the connectivity, either 6 or 26
	 * @param bitDepth the number of bits used to create the result stack (8, 16 or 32)
	 * @return a 3D ImageStack containing the label of each connected component.
	 */
	public final static ImagePlus computeLabels3D(ImagePlus imagePlus, int conn, int bitDepth) {
		// get 3D image stack
		ImageStack image = imagePlus.getStack();
		
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		IJ.showStatus("Allocate Memory");
		ImageStack labels = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);

		int nLabels = 0;
		
		boolean isFloat = bitDepth == 32;
		
		IJ.showStatus("Compute Labels...");
		for (int z = 0; z < sizeZ; z++) {
			IJ.showProgress(z, sizeZ);
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// Do not process background voxels
					if (image.getVoxel(x, y, z) == 0)
						continue;
					
					// Do not process voxels already labeled
					if (labels.getVoxel(x, y, z) > 0)
						continue;

					// a new label is found: increment label index, and propagate 
					nLabels++;
					if (!isFloat)
						fill(image, x, y, z, labels, nLabels, conn);
					else
						fillFloat(image, x, y, z, labels, (float) nLabels, conn);
				}
			}
		}
		IJ.showProgress(1);
		
		ImagePlus labelPlus = new ImagePlus("Labels", labels);
		labelPlus.setDisplayRange(0, nLabels);
		return labelPlus;
	}

	public final static ImageStack computeLabels(ImageStack image, int conn, int bitDepth) {
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		IJ.showStatus("Allocate Memory");
		ImageStack labels = ImageStack.create(sizeX, sizeY, sizeZ, bitDepth);

		int nLabels = 0;
		
		boolean isInteger = bitDepth != 32;
		
		IJ.showStatus("Compute Labels...");
		for (int z = 0; z < sizeZ; z++) {
			IJ.showProgress(z, sizeZ);
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					// Do not process background voxels
					if (image.getVoxel(x, y, z) == 0)
						continue;
					
					// Do not process voxels already labeled
					if (labels.getVoxel(x, y, z) > 0)
						continue;

					// a new label is found: increment label index, and propagate 
					nLabels++;
					if (isInteger)
						fill(image, x, y, z, labels, nLabels, conn);
					else
						fillFloat(image, x, y, z, labels, (float) nLabels, conn);
				}
			}
		}
		IJ.showProgress(1);
		return labels;
	}

	
//	public final static ImageStack computeLabelsByte(ImageStack image) {
//		return computeLabelsByte(image, 6);
//	}
//
//	public final static ImageStack computeLabelsByte(ImageStack image, int conn) {
//		// get image size
//		int sizeX = image.getWidth();
//		int sizeY = image.getHeight();
//		int sizeZ = image.getSize();
//		
//		ImageStack labels = ImageStack.create(sizeX, sizeY, sizeZ, 8);
//
//		int nLabels = 0;
//		
//		for (int z = 0; z < sizeZ; z++) {
//			for (int y = 0; y < sizeY; y++) {
//				for (int x = 0; x < sizeX; x++) {
//					// Do not process background voxels
//					if (image.getVoxel(x, y, z) == 0)
//						continue;
//					
//					// Do not process voxels already labeled
//					if (labels.getVoxel(x, y, z) > 0)
//						continue;
//
//					// a new label is found: increment label index, and propagate 
//					nLabels++;
////					System.out.println(String.format(
////							"Found new label %d at position (%d,%d,%d)",
////							nLabels, x, y, z));
//					fill(image, x, y, z, labels, nLabels, conn);
//				}
//			}
//		}
//		
////		labels.setMinAndMax(0, nLabels);
//		return labels;
//	}

	
	/**
	 * Replaces all the neighbor pixels of (x,y) that have the same values
	 * by the specified value, using the specified connectivity.
	 */
	private final static void fill(ImageStack image, int x, int y, int z,
			ImageStack labelImage, int value, int conn) {
		// the shifts to look for new markers to start lines
		int dx1 = 0;
		int dx2 = 0;
		if (conn == 8) {
			dx1 = -1;
			dx2 = +1;
		}
		
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
			if (((int) image.getVoxel(x, y, z)) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 
					&& ((int) image.getVoxel(x1-1, y, z)) == oldValue
					&& ((int) labelImage.getVoxel(x1-1, y, z)) == 0)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 
					&& ((int) image.getVoxel(x2+1, y, z)) == oldValue 
					&& ((int) labelImage.getVoxel(x2+1, y, z)) == 0)
				x2++;
		
			// fill current scan-line
			fillLine(labelImage, x1, x2, y, z, value);
			
			// search bounds on x axis for neighbor lines
			int x1l = max(x1 + dx1, 0);
			int x2l = min(x2 + dx2, sizeX - 1);

			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++) {
					int val = (int) image.getVoxel(i, y - 1, z);
					int label = (int) labelImage.getVoxel(i, y - 1, z);
					if (!inScanLine && val == oldValue && label == 0) {
						stack.add(new Cursor3D(i, y - 1, z));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines below the current one
			if (y < sizeY - 1) {
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++) {
					int val = (int) image.getVoxel(i, y + 1, z);
					int label = (int) labelImage.getVoxel(i, y + 1, z);
					if (!inScanLine && val == oldValue && label == 0) {
						stack.add(new Cursor3D(i, y + 1, z));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines in front of the current one
			if (z > 0) {
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++) {
					int val = (int) image.getVoxel(i, y, z - 1);
					int label = (int) labelImage.getVoxel(i, y, z - 1);
					if (!inScanLine && val == oldValue && label == 0) {
						stack.add(new Cursor3D(i, y, z - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines behind the current one
			if (z < sizeZ - 1) {
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++) {
					int val = (int) image.getVoxel(i, y, z + 1);
					int label = (int) labelImage.getVoxel(i, y, z + 1);

					if (!inScanLine && val == oldValue && label == 0) {
						stack.add(new Cursor3D(i, y, z + 1));
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
	private final static void fillLine(ImageStack image, int x1, int x2, int y,
			int z, int value) {
		for (int x = x1; x <= x2; x++)
			image.setVoxel(x, y, z, value);
	}

	/**
	 * Replaces all the neighbor pixels of (x,y) that have the same values
	 * by the specified value.
	 */
	private final static void fillFloat(ImageStack image, int x, int y, int z,
			ImageStack labelImage, float value, int conn) {
		// the shifts to look for new markers to start lines
		int dx1 = 0;
		int dx2 = 0;
		if (conn == 8) {
			dx1 = -1;
			dx2 = +1;
		}
		
		// get image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// get old value
		float oldValue = (float) image.getVoxel(x, y, z);
		
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
			if (((float) image.getVoxel(x, y, z)) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 > 0 
					&& ((float) image.getVoxel(x1-1, y, z)) == oldValue
					&& ((float) labelImage.getVoxel(x1-1, y, z)) == 0)
				x1--;
			
			// find end of scan-line
			while (x2 < sizeX - 1 
					&& ((float) image.getVoxel(x2+1, y, z)) == oldValue 
					&& ((float) labelImage.getVoxel(x2+1, y, z)) == 0)
				x2++;
		
			// fill current scan-line
			fillLineFloat(labelImage, x1, x2, y, z, value);
			
			// search bounds on x axis for neighbor lines
			int x1l = max(x1 + dx1, 0);
			int x2l = min(x2 + dx2, sizeX - 1);

			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++) {
					float val = (float) image.getVoxel(i, y - 1, z);
					float label = (float) labelImage.getVoxel(i, y - 1, z);
					if (!inScanLine && val == oldValue && label == 0) {
						stack.add(new Cursor3D(i, y - 1, z));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines below the current one
			if (y < sizeY - 1) {
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++) {
					float val = (float) image.getVoxel(i, y + 1, z);
					float label = (float) labelImage.getVoxel(i, y + 1, z);
					if (!inScanLine && val == oldValue && label == 0) {
						stack.add(new Cursor3D(i, y + 1, z));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines in front of the current one
			if (z > 0) {
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++) {
					float val = (float) image.getVoxel(i, y, z - 1);
					float label = (float) labelImage.getVoxel(i, y, z - 1);
					if (!inScanLine && val == oldValue && label == 0) {
						stack.add(new Cursor3D(i, y, z - 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
			
			// find scan-lines behind the current one
			if (z < sizeZ - 1) {
				inScanLine = false;
				for (int i = x1l; i <= x2l; i++) {
					float val = (float) image.getVoxel(i, y, z + 1);
					float label = (float) labelImage.getVoxel(i, y, z + 1);

					if (!inScanLine && val == oldValue && label == 0) {
						stack.add(new Cursor3D(i, y, z + 1));
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
	private final static void fillLineFloat(ImageStack image, int x1, int x2, int y,
			int z, float value) {
		for (int x = x1; x <= x2; x++)
			image.setVoxel(x, y, z, value);
	}

	public final static ByteProcessor computeLabelsByte(ImageProcessor image) {
		return computeLabelsByte(image, 4);
	}

	public final static ImageProcessor computeLabels(ImageProcessor image, int conn, int bitDepth) {
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		ImageProcessor labels;
		switch (bitDepth) {
		case 8: labels = new ByteProcessor(width, height); break; 
		case 16: labels = new ShortProcessor(width, height); break; 
		case 32: labels = new FloatProcessor(width, height); break;
		default: throw new IllegalArgumentException("Bit Depth should be 8, 16 or 32.");
		}
		
		// the label counter
		int nLabels = 0;
		
		boolean isInteger = bitDepth != 32;
		
		// iterate on image pixels to fin new regions
		for (int y = 0; y < height; y++) {
			IJ.showProgress(y, height);
			for (int x = 0; x < width; x++) {
				if (image.get(x, y) == 0)
					continue;
				if (labels.get(x, y) > 0)
					continue;
				
				nLabels++;
				if (isInteger)
					fill(image, x, y, labels, nLabels, conn);
				else
					fillFloat(image, x, y, labels, (float) nLabels, conn);
			}
		}
		IJ.showProgress(1);
		
		labels.setMinAndMax(0, nLabels);
		return labels;
	}

	public final static ByteProcessor computeLabelsByte(ImageProcessor image, int conn) {
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		
		ByteProcessor labels = new ByteProcessor(width, height);

		int nLabels = 0;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.get(x, y) == 0)
					continue;
				if (labels.get(x, y) > 0)
					continue;
				
				nLabels++;
				fill(image, x, y, labels, nLabels, conn);
			}
		}
		
		labels.setMinAndMax(0, nLabels);
		return labels;
	}


	/**
	 * Replaces all the neighbor pixels of (x,y) that have the same values
	 * by the specified value, using the specified connectivity.
	 */
	private final static void fill(ImageProcessor image, int x, int y,
			ImageProcessor labelImage, int value, int conn) {
		
		// the shifts to look for new markers to start lines
		int dx1 = 0;
		int dx2 = 0;
		if (conn == 8) {
			dx1 = -1;
			dx2 = +1;
		}
		
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
			
			// process only pixel of the same value
			if (image.get(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 >= 0 && image.getPixel(x1, y) == oldValue && labelImage.getPixel(x1, y) == 0) 
				x1--;
			x1++;
			
			// find end of scan-line
			while (x2 < width && image.getPixel(x2, y) == oldValue && labelImage.getPixel(x1, y) == 0) 
				x2++;                   
			x2--;
			
			// fill current scan-line
			fillLine(labelImage, y, x1, x2, value);
			
			
			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++) {
					int val = image.get(i, y - 1);
					int lab = (int) labelImage.get(i, y - 1);
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
//				for (int i = x1; i <= x2; i++) {
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++) {
					int val = image.getPixel(i, y + 1);
					int lab = (int) labelImage.get(i, y + 1);
					if (!inScanLine && val == oldValue && lab == 0) {
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
	 * Replaces all the neighbor pixels of (x,y) that have the same values
	 * by the specified value, using the specified connectivity.
	 */
	private final static void fillFloat(ImageProcessor image, int x, int y,
			ImageProcessor labelImage, float value, int conn) {
		
		// the shifts to look for new markers to start lines
		int dx1 = 0;
		int dx2 = 0;
		if (conn == 8) {
			dx1 = -1;
			dx2 = +1;
		}
		
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
			
			// process only pixel of the same value
			if (image.get(x, y) != oldValue) 
				continue;
			
			// x extremities of scan-line
			int x1 = x; 
			int x2 = x;
			
			// find start of scan-line
			while (x1 >= 0 && image.get(x1, y) == oldValue && labelImage.getf(x1, y) == 0) 
				x1--;
			x1++;
			
			// find end of scan-line
			while (x2 < width && image.get(x2, y) == oldValue && labelImage.getf(x1, y) == 0) 
				x2++;                   
			x2--;
			
			// fill current scan-line
			fillLineFloat(labelImage, y, x1, x2, value);
			
			
			// find scan-lines above the current one
			if (y > 0) {
				inScanLine = false;
				for (int i = max(x1 + dx1, 0); i <= min(x2 + dx2, width - 1); i++) {
					int val = image.get(i, y - 1);
					float lab = labelImage.getf(i, y - 1);
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
					int val = image.getPixel(i, y + 1);
					float lab = labelImage.getf(i, y + 1);
					if (!inScanLine && val == oldValue && lab == 0) {
						stack.add(new Point(i, y + 1));
						inScanLine = true;
					} else if (inScanLine && val != oldValue)
						inScanLine = false;
				}
			}
		}
	}

	private final static void fillLineFloat(ImageProcessor ip, int y, int x1, int x2, float value) {
		if (x1 > x2) {
			int t = x1;
			x1 = x2;
			x2 = t;
		}
		for (int x = x1; x <= x2; x++)
			ip.setf(x, y, value);
	}

	/**
	 * Defines a position within a 3D stack.
	 * Need to be a static class to be called by static methods.
	 */
	private final static class Cursor3D {
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
