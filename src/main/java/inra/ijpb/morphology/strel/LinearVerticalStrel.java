/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

/**
 * A vertical linear structuring element of a given length.
 * Provides methods for fast in place erosion and dilation.
 * 
 * @see LinearHorizontalStrel
 * @see LinearDiagUpStrel
 * @see LinearDiagDownStrel
 * @author David Legland
 *
 */
public class LinearVerticalStrel extends AbstractInPlaceStrel {

	// ==================================================
	// Static methods 
	
	public final static LinearVerticalStrel fromDiameter(int diam) {
		return new LinearVerticalStrel(diam);
	}
	
	public final static LinearVerticalStrel fromRadius(int radius) {
		return new LinearVerticalStrel(2 * radius + 1, radius);
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
	 * Creates a new vertical linear structuring element of a given size.
	 * @param size the number of pixels in this structuring element
	 */
	public LinearVerticalStrel(int size) {
		if (size < 1) {
			throw new RuntimeException("Requires a positive size");
		}
		this.size = size;
		
		this.offset = (int) Math.floor((this.size - 1) / 2);
	}
	
	/**
	 * Creates a new vertical linear structuring element of a given size and
	 * with a given offset. 
	 * @param size the number of pixels in this structuring element
	 * @param offset the position of the reference pixel (between 0 and size-1)
	 */
	public LinearVerticalStrel(int size, int offset) {
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
	
		// shifts between reference position and last position
		int shift = this.size - this.offset - 1;
		
		// local histogram
		LocalBufferMax localMax = new LocalBufferMax(size);
		
		// Iterate on image columns
		for (int x = 0; x < width; x++) {
			fireProgressChange(this, x, width);
			
			// reset local histogram
			localMax.fill(Strel.BACKGROUND);
			
			// init local histogram with neighbor values
			for (int y = 0; y < Math.min(shift, height); y++) {
				localMax.add(image.get(x, y));
			}
			
			// iterate along "middle" values
			for (int y = 0; y < height - shift; y++) {
				localMax.add(image.get(x, y + shift));
				image.set(x, y, localMax.getMax());
			}
			
			// process pixels at the end of the line
			for (int y = Math.max(0, height - shift); y < height; y++) {
				localMax.add(Strel.BACKGROUND);
				image.set(x, y, localMax.getMax());
			}
		}
		
		// clear the progress bar
		fireProgressChange(this, width, width);
	}

	
	private void inPlaceDilationFloat(ImageProcessor image) {
		// get image size
		int width = image.getWidth(); 
		int height = image.getHeight();
	
		// shifts between reference position and last position
		int shift = this.size - this.offset - 1;
		
		// local histogram
		LocalBufferMaxFloat localMax = new LocalBufferMaxFloat(size);
		
		// Iterate on image columns
		for (int x = 0; x < width; x++) {
			fireProgressChange(this, x, width);
			
			// reset local histogram
			localMax.fill(Float.MIN_VALUE);
			
			// init local histogram with neighbor values
			for (int y = 0; y < Math.min(shift, height); y++) {
				localMax.add(image.getf(x, y));
			}
			
			// iterate along "middle" values
			for (int y = 0; y < height - shift; y++) {
				localMax.add(image.getf(x, y + shift));
				image.setf(x, y, localMax.getMax());
			}
			
			// process pixels at the end of the line
			for (int y = Math.max(0, height - shift); y < height; y++) {
				localMax.add(Float.MIN_VALUE);
				image.setf(x, y, localMax.getMax());
			}
		}
		
		// clear the progress bar
		fireProgressChange(this, width, width);
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
	
		// shifts between reference position and last position
		int shift = this.size - this.offset - 1;
		
		// local histogram
		LocalBufferMin localMin = new LocalBufferMin(size);
		
		// Iterate on image columns
		for (int x = 0; x < width; x++) {
			fireProgressChange(this, x, width);
			
			// reset local histogram
			localMin.fill(Strel.FOREGROUND);
			
			// init local histogram with neighbor values
			for (int y = 0; y < Math.min(shift, height); y++) {
				localMin.add(image.get(x, y));
			}
			
			// iterate along "middle" values
			for (int y = 0; y < height - shift; y++) {
				localMin.add(image.get(x, y + shift));
				image.set(x, y, localMin.getMin());
			}
			
			// process pixels at the end of the line
			for (int y = Math.max(0, height - shift); y < height; y++) {
				localMin.add(Strel.FOREGROUND);
				image.set(x, y, localMin.getMin());
			}
		}
		
		// clear the progress bar
		fireProgressChange(this, width, width);
	}

	private void inPlaceErosionFloat(ImageProcessor image) {
		// get image size
		int width = image.getWidth(); 
		int height = image.getHeight();
	
		// shifts between reference position and last position
		int shift = this.size - this.offset - 1;
		
		// local histogram
		LocalBufferMinFloat localMin = new LocalBufferMinFloat(size);
		
		// Iterate on image columns
		for (int x = 0; x < width; x++) {
			fireProgressChange(this, x, width);
			
			// reset local histogram
			localMin.fill(Float.MAX_VALUE);
			
			// init local histogram with neighbor values
			for (int y = 0; y < Math.min(shift, height); y++) {
				localMin.add(image.getf(x, y));
			}
			
			// iterate along "middle" values
			for (int y = 0; y < height - shift; y++) {
				localMin.add(image.getf(x, y + shift));
				image.setf(x, y, localMin.getMin());
			}
			
			// process pixels at the end of the line
			for (int y = Math.max(0, height - shift); y < height; y++) {
				localMin.add(Float.MAX_VALUE);
				image.setf(x, y, localMin.getMin());
			}
		}
		
		// clear the progress bar
		fireProgressChange(this, width, width);
	}

	
	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getMask()
	 */
	@Override
	public int[][] getMask() {
		int[][] mask = new int[this.size][1];
		for (int i = 0; i < this.size; i++) {
			mask[i][0] = 255;
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
			shifts[i][0] = 0;
			shifts[i][1] = i - this.offset;
		}
		return shifts;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getSize()
	 */
	@Override
	public int[] getSize() {
		return new int[]{1, this.size};
	}

	/**
	 * Returns a linear vertical line with same size and offset equal to size-offset-1.
	 * @see inra.ijpb.morphology.Strel#reverse()
	 */
	@Override
	public LinearVerticalStrel reverse() {
		return new LinearVerticalStrel(this.size, this.size - this.offset - 1);
	}
}
