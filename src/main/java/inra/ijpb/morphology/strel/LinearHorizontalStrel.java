/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

/**
 * An horizontal linear structuring element of a given length.
 * Provides methods for fast in place erosion and dilation.
 * 
 * @see LinearVerticalStrel
 * @see LinearDiagUpStrel
 * @see LinearDiagDownStrel
 * @author David Legland
 *
 */
public class LinearHorizontalStrel extends AbstractInPlaceStrel  {

	// ==================================================
	// Static methods 
	
	public final static LinearHorizontalStrel fromDiameter(int diam) {
		return new LinearHorizontalStrel(diam);
	}
	
	public final static LinearHorizontalStrel fromRadius(int radius) {
		return new LinearHorizontalStrel(2 * radius + 1, radius);
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
	 * Creates a new horizontal linear structuring element of a given size.
	 * @param size the number of pixels in this structuring element
	 */
	public LinearHorizontalStrel(int size) {
		if (size < 1) {
			throw new RuntimeException("Requires a positive size");
		}
		this.size = size;
		
		this.offset = (int) Math.floor((this.size - 1) / 2);
	}
	
	/**
	 * Creates a new horizontal linear structuring element of a given size and
	 * with a given offset. 
	 * @param size the number of pixels in this structuring element
	 * @param offset the position of the reference pixel (between 0 and size-1)
	 */
	public LinearHorizontalStrel(int size, int offset) {
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
	 * @see inra.ijpb.morphology.InPlaceStrel#inPlaceDilation(ij.process.ImageProcessor)
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
//		LocalBufferMax localMax = new LocalBufferMax(size);
//		LocalBufferGray8 localMax = new LocalBufferGray8(size);
//		localMax.setMinMaxSign(+1);
		LocalExtremaBufferGray8 localMax = new LocalExtremaBufferGray8(size);
		localMax.setMinMaxSign(+1);
		
		// Iterate on image rows
		for (int y = 0; y < height; y++) {
			fireProgressChange(this, y, height);
			
			// init local histogram with background values
			localMax.fill(Strel.BACKGROUND);
			
			// add neighbor values
			for (int x = 0; x < Math.min(shift, width); x++) {
				localMax.add(image.get(x, y));
			}
			
			// iterate along "middle" values
			for (int x = 0; x < width - shift; x++) {
				localMax.add(image.get(x + shift, y));
				image.set(x, y, localMax.getMax());
			}
			
			// process pixels at the end of the line
			for (int x = Math.max(0, width - shift); x < width; x++) {
				localMax.add(Strel.BACKGROUND);
				image.set(x, y, localMax.getMax());
			}
		}
		
		// clear the progress bar
		fireProgressChange(this, height, height);
	}

	private void inPlaceDilationFloat(ImageProcessor image) {
		// get image size
		int width = image.getWidth(); 
		int height = image.getHeight();
			
		// shifts between reference position and last position
		int shift = this.size - this.offset - 1;
		
		// local histogram
//		LocalBufferMaxFloat localMax = new LocalBufferMaxFloat(size);
		LocalExtremaBufferDouble localMax = new LocalExtremaBufferDouble(size);
		localMax.setMinMaxSign(+1);
		
		// Iterate on image rows
		for (int y = 0; y < height; y++) {
			fireProgressChange(this, y, height);
			
			// init local histogram with background values
			localMax.fill(Float.MIN_VALUE);
			
			// add neighbor values
			for (int x = 0; x < Math.min(shift, width); x++) {
				localMax.add(image.getf(x, y));
			}
			
			// iterate along "middle" values
			for (int x = 0; x < width - shift; x++) {
				localMax.add(image.getf(x + shift, y));
				image.setf(x, y, (float) localMax.getMax());
			}
			
			// process pixels at the end of the line
			for (int x = Math.max(0, width - shift); x < width; x++) {
				localMax.add(Strel.BACKGROUND);
				image.setf(x, y, (float) localMax.getMax());
			}
		}
		
		// clear the progress bar
		fireProgressChange(this, height, height);
	}


	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.InPlaceStrel#inPlaceErosion(ij.process.ImageProcessor)
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
//		LocalBufferMin localMin = new LocalBufferMin(size);
//		LocalBufferGray8 localMin = new LocalBufferGray8(size);
		LocalExtremaBufferGray8 localMin = new LocalExtremaBufferGray8(size);
		localMin.setMinMaxSign(-1);
		
		// Iterate on image rows
		for (int y = 0; y < height; y++) {
			fireProgressChange(this, y, height);
			
			// reset local histogram
			localMin.fill(Strel.FOREGROUND);
			
			// init local histogram with neighbor values
			for (int x = 0; x < Math.min(shift, width); x++) {
				localMin.add(image.get(x, y));
			}
			
			// iterate along "middle" values
			for (int x = 0; x < width - shift; x++) {
				localMin.add(image.get(x + shift, y));
				image.set(x, y, localMin.getMax());
			}
			
			// process pixels at the end of the line
			for (int x = Math.max(0, width - shift); x < width; x++) {
				localMin.add(Strel.FOREGROUND);
				image.set(x, y, localMin.getMax());
			}
		}
		
		// clear the progress bar
		fireProgressChange(this, height, height);
	}

	private void inPlaceErosionFloat(ImageProcessor image) {
		// get image size
		int width = image.getWidth(); 
		int height = image.getHeight();
		
		// shifts between reference position and last position
		int shift = this.size - this.offset - 1;
		
		// local histogram
//		LocalBufferMinFloat localMin = new LocalBufferMinFloat(size);
		LocalExtremaBufferFloat localMin = new LocalExtremaBufferFloat(size);
		localMin.setMinMaxSign(-1);
		
		// Iterate on image rows
		for (int y = 0; y < height; y++) {
			fireProgressChange(this, y, height);
			
			// reset local histogram
			localMin.fill(Float.MAX_VALUE);
			
			// init local histogram with neighbor values
			for (int x = 0; x < Math.min(shift, width); x++) {
				localMin.add(image.getf(x, y));
			}
			
			// iterate along "middle" values
			for (int x = 0; x < width - shift; x++) {
				localMin.add(image.getf(x + shift, y));
				image.setf(x, y, localMin.getMax());
			}
			
			// process pixels at the end of the line
			for (int x = Math.max(0, width - shift); x < width; x++) {
				localMin.add(Float.MAX_VALUE);
				image.setf(x, y, localMin.getMax());
			}
		}
		
		// clear the progress bar
		fireProgressChange(this, height, height);
	}

	
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel#getMask()
	 */
	@Override
	public int[][] getMask() {
		int[][] mask = new int[1][this.size];
		for (int i = 0; i < this.size; i++) {
			mask[0][i] = 255;
		}
		
		return mask;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel#getOffset()
	 */
	@Override
	public int[] getOffset() {
		return new int[]{this.offset, this.offset};
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel#getShifts()
	 */
	@Override
	public int[][] getShifts() {
		int[][] shifts = new int[this.size][2];
		for (int i = 0; i < this.size; i++) {
			shifts[i][0] = i - this.offset;
			shifts[i][1] = 0;
		}
		return shifts;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel#getSize()
	 */
	@Override
	public int[] getSize() {
		return new int[]{this.size, 1};
	}

	/**
	 * Returns a linear horizontal line with same size and offset equal to size-offset-1.
	 * @see inra.ijpb.morphology.Strel#reverse()
	 */
	@Override
	public LinearHorizontalStrel reverse() {
		return new LinearHorizontalStrel(this.size, this.size - this.offset - 1);
	}

}
