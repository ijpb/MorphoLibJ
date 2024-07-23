/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
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
	
	/**
	 * Creates a new line-shape structuring element with the specified diameter
	 * (equal to the length of the line).
	 * 
	 * @param diam
	 *            the length of the line along the Y direction
	 * @return a new linear structuring element
	 */
	public final static LinearVerticalStrel fromDiameter(int diam) {
		return new LinearVerticalStrel(diam);
	}
	
	/**
	 * Creates a new line-shape structuring element with the specified radius
	 * (such that orthogonal diameter equals 2*radius+1).
	 * 
	 * @param radius
	 *            the radius of the line, such that line length equals 2*radius+1
	 * @return a new linear structuring element
	 */
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
		
		// create local histogram instance
		LocalExtremumBufferGray8 localMax = new LocalExtremumBufferGray8(size,
				LocalExtremum.Type.MAXIMUM);

		// Iterate on image columns
		for (int x = 0; x < width; x++) {
			fireProgressChanged(this, x, width);
			
			// reset local histogram
			localMax.fill(Strel.BACKGROUND);
			
			// init local histogram with neighbor values
			for (int y = 0; y < Math.min(shift, height); y++) {
				localMax.add(image.get(x, y));
			}
			
			// iterate along "middle" values
			for (int y = 0; y < height - shift; y++) {
				localMax.add(image.get(x, y + shift));
				image.set(x, y, (int) localMax.getMax());
			}
			
			// process pixels at the end of the line
			for (int y = Math.max(0, height - shift); y < height; y++) {
				localMax.add(Strel.BACKGROUND);
				image.set(x, y, (int) localMax.getMax());
			}
		}
		
		// clear the progress bar
		fireProgressChanged(this, width, width);
	}

	
	private void inPlaceDilationFloat(ImageProcessor image) {
		// get image size
		int width = image.getWidth(); 
		int height = image.getHeight();
	
		// shifts between reference position and last position
		int shift = this.size - this.offset - 1;
		
		// create local histogram instance
		LocalExtremumBufferDouble localMax = new LocalExtremumBufferDouble(size,
				LocalExtremum.Type.MAXIMUM);
		
		// Iterate on image columns
		for (int x = 0; x < width; x++) {
			fireProgressChanged(this, x, width);
			
			// reset local histogram
			localMax.fill(Float.NEGATIVE_INFINITY);
			
			// init local histogram with neighbor values
			for (int y = 0; y < Math.min(shift, height); y++) {
				localMax.add(image.getf(x, y));
			}
			
			// iterate along "middle" values
			for (int y = 0; y < height - shift; y++) {
				localMax.add(image.getf(x, y + shift));
				image.setf(x, y, (float) localMax.getMax());
			}
			
			// process pixels at the end of the line
			for (int y = Math.max(0, height - shift); y < height; y++) {
				localMax.add(Float.NEGATIVE_INFINITY);
				image.setf(x, y, (float) localMax.getMax());
			}
		}
		
		// clear the progress bar
		fireProgressChanged(this, width, width);
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
		
		// create local histogram instance
		LocalExtremumBufferGray8 localMin = new LocalExtremumBufferGray8(size,
				LocalExtremum.Type.MINIMUM);


		// Iterate on image columns
		for (int x = 0; x < width; x++) {
			fireProgressChanged(this, x, width);
			
			// reset local histogram
			localMin.fill(Strel.FOREGROUND);
			
			// init local histogram with neighbor values
			for (int y = 0; y < Math.min(shift, height); y++) {
				localMin.add(image.get(x, y));
			}
			
			// iterate along "middle" values
			for (int y = 0; y < height - shift; y++) {
				localMin.add(image.get(x, y + shift));
				image.set(x, y, localMin.getMax());
			}
			
			// process pixels at the end of the line
			for (int y = Math.max(0, height - shift); y < height; y++) {
				localMin.add(Strel.FOREGROUND);
				image.set(x, y, localMin.getMax());
			}
		}
		
		// clear the progress bar
		fireProgressChanged(this, width, width);
	}

	private void inPlaceErosionFloat(ImageProcessor image) {
		// get image size
		int width = image.getWidth(); 
		int height = image.getHeight();
	
		// shifts between reference position and last position
		int shift = this.size - this.offset - 1;
		
		// create local histogram instance
		LocalExtremumBufferDouble localMin = new LocalExtremumBufferDouble(size,
				LocalExtremum.Type.MINIMUM);

		
		// Iterate on image columns
		for (int x = 0; x < width; x++) {
			fireProgressChanged(this, x, width);
			
			// reset local histogram
			localMin.fill(Float.POSITIVE_INFINITY);
			
			// init local histogram with neighbor values
			for (int y = 0; y < Math.min(shift, height); y++) {
				localMin.add(image.getf(x, y));
			}
			
			// iterate along "middle" values
			for (int y = 0; y < height - shift; y++) {
				localMin.add(image.getf(x, y + shift));
				image.setf(x, y, (float) localMin.getMax());
			}
			
			// process pixels at the end of the line
			for (int y = Math.max(0, height - shift); y < height; y++) {
				localMin.add(Float.POSITIVE_INFINITY);
				image.setf(x, y, (float) localMin.getMax());
			}
		}
		
		// clear the progress bar
		fireProgressChanged(this, width, width);
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
		return new int[]{0, this.offset};
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
