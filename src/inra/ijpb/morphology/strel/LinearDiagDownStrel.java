/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.IJ;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

/**
 * A diagonal linear structuring element of a given length, with direction
 * vector (+1,+1) in image coordinate system. 
 * Provides methods for fast in place erosion and dilation.
 * 
 * @see LinearHorizontalStrel
 * @see LinearVerticalStrel
 * @see LinearDiagUpStrel
 * 
 * @author David Legland
 *
 */
public class LinearDiagDownStrel extends AbstractInPlaceStrel {

	// ==================================================
	// Static methods 
	
	public final static LinearDiagDownStrel fromDiameter(int diam) {
		return new LinearDiagDownStrel(diam);
	}
	
	public final static LinearDiagDownStrel fromRadius(int radius) {
		return new LinearDiagDownStrel(2 * radius + 1, radius);
	}
	
	
	// ==================================================
	// Class variables
	
	/**
	 * Number of element in this structuring element. 
	 * Corresponds to the horizontal size.
	 */
	int size;
	
	/**
	 * Position of the origin within the segment.
	 * Corresponds to the number of elements before the reference element.
	 */
	int offset;
	
	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new diagonal linear structuring element of a given size.
	 * @param size the number of pixels in this structuring element
	 */
	public LinearDiagDownStrel(int size) {
		if (size < 1) {
			throw new RuntimeException("Requires a positive size");
		}
		this.size = size;
		
		this.offset = (int) Math.floor((this.size - 1) / 2);
	}
	
	/**
	 * Creates a new diagonal linear structuring element of a given size and
	 * with a given offset. 
	 * @param size the number of pixels in this structuring element
	 * @param offset the position of the reference pixel (between 0 and size-1)
	 */
	public LinearDiagDownStrel(int size, int offset) {
		if (size < 1) {
			throw new RuntimeException("Requires a positive size");
		}
		this.size = size;
		
		if (offset < 0) {
			throw new RuntimeException("Requires a non-negative offset");
		}
		if (offset >= size) {
			throw new RuntimeException("Offset can not be greater than size");
		}
		this.offset = offset;
	}
	
	
	// ==================================================
	// General methods 
	
	/* (non-Javadoc)
	 * @see ijt.morphology.InPlaceStrel#inPlaceDilation(ij.process.ImageProcessor)
	 */
	@Override
	public void inPlaceDilation(ImageProcessor image) {
		// If size is one, there is no need to compute
		if (size <= 1) { 
			return;
		}
		
		if (image instanceof ByteProcessor)
			inPlaceDilationGray8(image);
		else
			inPlaceDilationFloat(image);
	}
	
	private void inPlaceDilationGray8(ImageProcessor image) {
		// get image size
		int width = image.getWidth(); 
		int height = image.getHeight();
	
		// Consider all diagonal lines with direction vector (+1,+1) that intersect image.
		// Diagonal lines are identified by their intersection "d" with axis (-1,+1)
		// Need to identify bounds for d
		int dmin = -(width - 1);
		int dmax = height - 1;
	
		// local histogram
		LocalBufferMax localMax = new LocalBufferMax(size);
		
		// Iterate on diagonal lines
		for (int d = dmin; d < dmax; d++) {
			if (this.showProgress()) {
				IJ.showProgress(d, dmax - dmin);
			}
				
			// reset local histogram
			localMax.fill(Strel.BACKGROUND);
			
			int xmin = Math.max(0, -d);
			int xmax = Math.min(width, height - d);
			int ymin = Math.max(0, d);
			int ymax = Math.min(height, d - width);
			
			int tmin = Math.max(xmin, d - ymin);
			int tmax = Math.min(xmax, d - ymax);
			
			// position on the line
			int t = tmin;

			// init local histogram image values after current pos
			while (t < Math.min(tmin + this.offset, tmax)) {
				localMax.add(image.get(t, t + d));
				t++;
			}
			
			// process position that do not touch lower-right image boundary
			while (t < tmax) {
				localMax.add(image.get(t, t + d));
				int t2 = t - this.offset;
				image.set(t2, t2 + d, localMax.getMax());
				t++;
			}
			
			// process pixels at the end of the line 
			// and that do not touch the upper left image boundary
			while (t < tmax + this.offset) {
				localMax.add(Strel.BACKGROUND);
				int x = t - this.offset;
				int y = t + d - this.offset;
				if (x >= 0 && y >= 0 && x < width && y < height)
					image.set(x, y, localMax.getMax());
				t++;
			}
		}
		
		// clear the progress bar
		if (this.showProgress()) {
			IJ.showProgress(1);
		}
	}

	private void inPlaceDilationFloat(ImageProcessor image) {
		// get image size
		int width = image.getWidth(); 
		int height = image.getHeight();
	
		// Consider all diagonal lines with direction vector (+1,+1) that intersect image.
		// Diagonal lines are identified by their intersection "d" with axis (-1,+1)
		// Need to identify bounds for d
		int dmin = -(width - 1);
		int dmax = height - 1;
	
		// local histogram
		LocalBufferMaxFloat localMax = new LocalBufferMaxFloat(size);
		
		// Iterate on diagonal lines
		for (int d = dmin; d < dmax; d++) {
			if (this.showProgress()) {
				IJ.showProgress(d, dmax - dmin);
			}
				
			// reset local histogram
			localMax.fill(Float.MIN_VALUE);
			
			int xmin = Math.max(0, -d);
			int xmax = Math.min(width, height - d);
			int ymin = Math.max(0, d);
			int ymax = Math.min(height, d - width);
			
			int tmin = Math.max(xmin, d - ymin);
			int tmax = Math.min(xmax, d - ymax);
			
			// position on the line
			int t = tmin;

			// init local histogram image values after current pos
			while (t < Math.min(tmin + this.offset, tmax)) {
				localMax.add(image.getf(t, t + d));
				t++;
			}
			
			// process position that do not touch lower-right image boundary
			while (t < tmax) {
				localMax.add(image.getf(t, t + d));
				int t2 = t - this.offset;
				image.setf(t2, t2 + d, localMax.getMax());
				t++;
			}
			
			// process pixels at the end of the line 
			// and that do not touch the upper left image boundary
			while (t < tmax + this.offset) {
				localMax.add(Float.MIN_VALUE);
				int x = t - this.offset;
				int y = t + d - this.offset;
				if (x >= 0 && y >= 0 && x < width && y < height)
					image.setf(x, y, localMax.getMax());
				t++;
			}
		}
		
		// clear the progress bar
		if (this.showProgress()) {
			IJ.showProgress(1);
		}
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.InPlaceStrel#inPlaceErosion(ij.process.ImageProcessor)
	 */
	@Override
	public void inPlaceErosion(ImageProcessor image) {
		// If size is one, there is no need to compute
		if (size <= 1) { 
			return;
		}
		
		if (image instanceof ByteProcessor)
			inPlaceErosionGray8(image);
		else
			inPlaceErosionFloat(image);
	}
	
	private void inPlaceErosionGray8(ImageProcessor image) {
		// get image size
		int width = image.getWidth(); 
		int height = image.getHeight();
	
		// Consider all diagonal lines with direction vector (+1,+1) that intersect image.
		// Diagonal lines are identified by their intersection "d" with axis (-1,+1)
		// Need to identify bounds for d
		int dmin = -(width - 1);
		int dmax = height - 1;
		
		// compute shifts
		int dt0 = this.offset;
		
		// local histogram
		LocalBufferMin localMin = new LocalBufferMin(size);
		
		// Iterate on diagonal lines
		for (int d = dmin; d < dmax; d++) {
			if (this.showProgress()) {
				IJ.showProgress(d, dmax - dmin);
			}
			
			// reset local histogram
			localMin.fill(Strel.FOREGROUND);
			
			int xmin = Math.max(0, -d);
			int xmax = Math.min(width, height - d);
			int ymin = Math.max(0, d);
			int ymax = Math.min(height, d - width);
			
			int tmin = Math.max(xmin, d - ymin);
			int tmax = Math.min(xmax, d - ymax);
			
			// position on the line
			int t = tmin;
			
			// init local histogram image values after current pos
			while (t < Math.min(tmin + dt0, tmax)) {
				localMin.add(image.get(t, t + d));
				t++;
			}
			
			// process position that do not touch lower-right image boundary
			while (t < tmax) {
				localMin.add(image.get(t, t + d));
				image.set(t - dt0, t + d - dt0, localMin.getMin());
				t++;
			}
			
			// process pixels at the end of the line 
			// and that do not touch the upper left image boundary
			while (t < tmax + dt0) {
				localMin.add(Strel.FOREGROUND);
				int x = t - dt0;
				int y = t + d - dt0;
				if (x >= 0 && y >= 0 && x < width && y < height)
					image.set(x, y, localMin.getMin());
				t++;
			}
		}
		
		// clear the progress bar
		if (this.showProgress()) {
			IJ.showProgress(1);
		}
	}

	private void inPlaceErosionFloat(ImageProcessor image) {
		// get image size
		int width = image.getWidth(); 
		int height = image.getHeight();
	
		// Consider all diagonal lines with direction vector (+1,+1) that intersect image.
		// Diagonal lines are identified by their intersection "d" with axis (-1,+1)
		// Need to identify bounds for d
		int dmin = -(width - 1);
		int dmax = height - 1;
		
		// compute shifts
		int dt0 = this.offset;
		
		// local histogram
		LocalBufferMinFloat localMin = new LocalBufferMinFloat(size);
		
		// Iterate on diagonal lines
		for (int d = dmin; d < dmax; d++) {
			if (this.showProgress()) {
				IJ.showProgress(d, dmax - dmin);
			}
			
			// reset local histogram
			localMin.fill(Float.MAX_VALUE);
			
			int xmin = Math.max(0, -d);
			int xmax = Math.min(width, height - d);
			int ymin = Math.max(0, d);
			int ymax = Math.min(height, d - width);
			
			int tmin = Math.max(xmin, d - ymin);
			int tmax = Math.min(xmax, d - ymax);
			
			// position on the line
			int t = tmin;
			
			// init local histogram image values after current pos
			while (t < Math.min(tmin + dt0, tmax)) {
				localMin.add(image.getf(t, t + d));
				t++;
			}
			
			// process position that do not touch lower-right image boundary
			while (t < tmax) {
				localMin.add(image.getf(t, t + d));
				image.setf(t - dt0, t + d - dt0, localMin.getMin());
				t++;
			}
			
			// process pixels at the end of the line 
			// and that do not touch the upper left image boundary
			while (t < tmax + dt0) {
				localMin.add(Float.MAX_VALUE);
				int x = t - dt0;
				int y = t + d - dt0;
				if (x >= 0 && y >= 0 && x < width && y < height)
					image.setf(x, y, localMin.getMin());
				t++;
			}
		}
		
		// clear the progress bar
		if (this.showProgress()) {
			IJ.showProgress(1);
		}
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getMask()
	 */
	@Override
	public int[][] getMask() {
		int[][] mask = new int[this.size][this.size];
		for (int i = 0; i < this.size; i++) {
			mask[i][i] = 255;
		}
		
		return mask;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getOffset()
	 */
	@Override
	public int[] getOffset() {
		return new int[]{this.offset, this.offset};
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getShifts()
	 */
	@Override
	public int[][] getShifts() {
		int[][] shifts = new int[this.size][2];
		for (int i = 0; i < this.size; i++) {
			shifts[i][0] = i - this.offset;
			shifts[i][1] = i - this.offset;
		}
		return shifts;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getSize()
	 */
	@Override
	public int[] getSize() {
		return new int[]{this.size, this.size};
	}

	/**
	 * Returns a linear diagonal line with same size and offset equal to size-offset.
	 * @see inra.ijpb.morphology.Strel#reverse()
	 */
	@Override
	public LinearDiagDownStrel reverse() {
		return new LinearDiagDownStrel(this.size, this.size - this.offset - 1);
	}

}
