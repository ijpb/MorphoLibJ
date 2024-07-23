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

import ij.ImageStack;
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
public class LinearDepthStrel3D extends AbstractInPlaceStrel3D  {

	// ==================================================
	// Static methods 
	
	/**
	 * Creates a new line-shape structuring element with the specified diameter
	 * (equal to the length of the line).
	 * 
	 * @param diam
	 *            the length of the line along the Z direction
	 * @return a new linear structuring element
	 */
	public final static LinearDepthStrel3D fromDiameter(int diam) {
		return new LinearDepthStrel3D(diam);
	}
	
	/**
	 * Creates a new line-shape structuring element with the specified radius
	 * (such that orthogonal diameter equals 2*radius+1).
	 * 
	 * @param radius
	 *            the radius of the line, such that line length equals 2*radius+1
	 * @return a new linear structuring element
	 */
	public final static LinearDepthStrel3D fromRadius(int radius) {
		return new LinearDepthStrel3D(2 * radius + 1, radius);
	}
	
	// ==================================================
	// Class variables
	
	/**
	 * Number of element in this structuring element. 
	 * Corresponds to the size in z direction.
	 */
	int length;
	
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
	public LinearDepthStrel3D(int size) {
		if (size < 1) {
			throw new RuntimeException("Requires a positive size");
		}
		this.length = size;
		
		this.offset = (int) Math.floor((this.length - 1) / 2);
	}
	
	/**
	 * Creates a new horizontal linear structuring element of a given size and
	 * with a given offset. 
	 * @param size the number of pixels in this structuring element
	 * @param offset the position of the reference pixel (between 0 and size-1)
	 */
	public LinearDepthStrel3D(int size, int offset) {
		if (size < 1) {
			throw new RuntimeException("Requires a positive size");
		}
		this.length = size;
		
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
	 * @see inra.ijpb.morphology.InPlaceStrel#inPlaceDilation(ij.process.ImageStack)
	 */
	@Override
	public void inPlaceDilation(ImageStack stack) {
		// If size is one, there is no need to compute
		if (length <= 1) { 
			return;
		}
		
		if (stack.getBitDepth() == 8)
			inPlaceDilationGray8(stack);
		else
			inPlaceDilationFloat(stack);
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.InPlaceStrel#inPlaceDilation(ij.process.ImageStack)
	 */
	private void inPlaceDilationGray8(ImageStack stack) {
		// get image size
		int width 	= stack.getWidth(); 
		int height 	= stack.getHeight();
		int depth 	= stack.getSize();
			
		// shifts between reference position and last position
		int shift = this.length - this.offset - 1;
		
		// create local histogram instance
		LocalExtremumBufferGray8 localMax = new LocalExtremumBufferGray8(
				this.length, LocalExtremum.Type.MAXIMUM);		
		
		// Iterate on image z-columns
		for (int y = 0; y < height; y++) {
			fireProgressChanged(this, y, height);
			for (int x = 0; x < width; x++) {

				// init local histogram with background values
				localMax.fill(Strel.BACKGROUND);

				// add neighbor values
				for (int z = 0; z < Math.min(shift, depth); z++) {
					localMax.add((int) stack.getVoxel(x, y, z));
				}

				// iterate along "middle" values
				for (int z = 0; z < depth - shift; z++) {
					localMax.add((int) stack.getVoxel(x, y, z + shift));
					stack.setVoxel(x, y, z, localMax.getMax());
				}

				// process pixels at the end of the line
				for (int z = Math.max(0, depth - shift); z < depth; z++) {
					localMax.add(Strel.BACKGROUND);
					stack.setVoxel(x, y, z, localMax.getMax());
				}
			}
		}

		// clear the progress bar
		fireProgressChanged(this, height, height);		
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.InPlaceStrel#inPlaceDilation(ij.process.ImageStack)
	 */
	private void inPlaceDilationFloat(ImageStack stack) {
		// get image size
		int width 	= stack.getWidth(); 
		int height 	= stack.getHeight();
		int depth 	= stack.getSize();
			
		// shifts between reference position and last position
		int shift = this.length - this.offset - 1;
		
		// create local histogram instance
		LocalExtremumBufferDouble localMax = new LocalExtremumBufferDouble(
				this.length, LocalExtremum.Type.MAXIMUM);		
		
		// Iterate on image z-columns
		for (int y = 0; y < height; y++) {
			fireProgressChanged(this, y, height);
			for (int x = 0; x < width; x++) {

				// init local histogram with background values
				localMax.fill(Float.NEGATIVE_INFINITY);

				// add neighbor values
				for (int z = 0; z < Math.min(shift, depth); z++) {
					localMax.add((float) stack.getVoxel(x, y, z));
				}

				// iterate along "middle" values
				for (int z = 0; z < depth - shift; z++) {
					localMax.add((float) stack.getVoxel(x, y, z + shift));
					stack.setVoxel(x, y, z, localMax.getMax());
				}

				// process pixels at the end of the line
				for (int z = Math.max(0, depth - shift); z < depth; z++) {
					localMax.add(Float.NEGATIVE_INFINITY);
					stack.setVoxel(x, y, z, localMax.getMax());
				}
			}
		}

		// clear the progress bar
		fireProgressChanged(this, height, height);		
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.InPlaceStrel#inPlaceErosion(ij.process.ImageStack)
	 */
	@Override
	public void inPlaceErosion(ImageStack stack) {
		// If size is one, there is no need to compute
		if (length <= 1) { 
			return;
		}

		if (stack.getBitDepth() == 8)
			inPlaceErosionGray8(stack);
		else
			inPlaceErosionFloat(stack);
		
	}
	
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.InPlaceStrel#inPlaceErosion(ij.process.ImageStack)
	 */
	private void inPlaceErosionGray8(ImageStack stack) {
		// get image size
		int width 	= stack.getWidth(); 
		int height 	= stack.getHeight();
		int depth 	= stack.getSize();
		
		// shifts between reference position and last position
		int shift = this.length - this.offset - 1;
		
		// create local histogram instance
		LocalExtremumBufferDouble localMin = new LocalExtremumBufferDouble(this.length,
				LocalExtremum.Type.MINIMUM);
		
		
		// Iterate on image z-columns
		for (int y = 0; y < height; y++) {
			fireProgressChanged(this, y, height);
			for (int x = 0; x < width; x++) {

				// init local histogram with background values
				localMin.fill(Strel.FOREGROUND);

				// add neighbor values
				for (int z = 0; z < Math.min(shift, depth); z++) {
					localMin.add((int) stack.getVoxel(x, y, z));
				}

				// iterate along "middle" values
				for (int z = 0; z < depth - shift; z++) {
					localMin.add((int) stack.getVoxel(x, y, z + shift));
					stack.setVoxel(x, y, z, localMin.getMax());
				}

				// process pixels at the end of the line
				for (int z = Math.max(0, depth - shift); z < depth; z++) {
					localMin.add(Strel.FOREGROUND);
					stack.setVoxel(x, y, z, localMin.getMax());
				}
			}
		}
		
		// clear the progress bar
		fireProgressChanged(this, height, height);		
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.InPlaceStrel#inPlaceErosion(ij.process.ImageStack)
	 */
	private void inPlaceErosionFloat(ImageStack stack) {
		// get image size
		int width 	= stack.getWidth(); 
		int height 	= stack.getHeight();
		int depth 	= stack.getSize();
		
		// shifts between reference position and last position
		int shift = this.length - this.offset - 1;
		
		// create local histogram instance
		LocalExtremumBufferDouble localMin = new LocalExtremumBufferDouble(
				this.length, LocalExtremum.Type.MINIMUM);
		
		// Iterate on image z-columns
		for (int y = 0; y < height; y++) {
			fireProgressChanged(this, y, height);
			for (int x = 0; x < width; x++) {

				// init local histogram with background values
				localMin.fill(Float.POSITIVE_INFINITY);

				// add neighbor values
				for (int z = 0; z < Math.min(shift, depth); z++) {
					localMin.add((float) stack.getVoxel(x, y, z));
				}

				// iterate along "middle" values
				for (int z = 0; z < depth - shift; z++) {
					localMin.add((float) stack.getVoxel(x, y, z + shift));
					stack.setVoxel(x, y, z, localMin.getMax());
				}

				// process pixels at the end of the line
				for (int z = Math.max(0, depth - shift); z < depth; z++) {
					localMin.add(Float.POSITIVE_INFINITY);
					stack.setVoxel(x, y, z, localMin.getMax());
				}
			}
		}
		
		// clear the progress bar
		fireProgressChanged(this, height, height);		
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel#getMask()
	 */
	@Override
	public int[][][] getMask3D() {
		int[][][] mask = new int[this.length][1][1];
		for (int i = 0; i < this.length; i++) {
			mask[i][0][0] = 255;
		}
		
		return mask;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel#getOffset()
	 */
	@Override
	public int[] getOffset() {
		return new int[]{0, 0, this.offset};
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel#getShifts()
	 */
	@Override
	public int[][] getShifts3D() {
		int[][] shifts = new int[this.length][3];
		for (int i = 0; i < this.length; i++) {
			shifts[i][0] = 0;
			shifts[i][1] = 0;
			shifts[i][2] = i - this.offset;
		}
		return shifts;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.Strel#getSize()
	 */
	@Override
	public int[] getSize() {
		return new int[]{1, 1, this.length};
	}

	/**
	 * Returns a linear horizontal line with same size and offset equal to size-offset-1.
	 * @see inra.ijpb.morphology.Strel#reverse()
	 */
	@Override
	public LinearDepthStrel3D reverse() {
		return new LinearDepthStrel3D(this.length, this.length - this.offset - 1);
	}

}
