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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Structuring element representing a diamond of a given diameter.
 *  
 * The diamond is decomposed into linear diagonal and 3x3 cross structuring 
 * elements.
 *
 * @see LinearDiagDownStrel
 * @see LinearDiagUpStrel
 * @see ShiftedCross3x3Strel
 * @author David Legland
 */
public class DiamondStrel extends AbstractSeparableStrel {

	// ==================================================
	// Static methods 
	
	/**
	 * Creates a new diamond-shape structuring element with the specified
	 * diameter.
	 * 
	 * @param diam
	 *            the diameter of the diamond
	 * @return a new DiamondStrel instance
	 */
	public final static DiamondStrel fromDiameter(int diam) {
		return new DiamondStrel(diam);
	}
	
	/**
	 * Creates a new diamond-shape structuring element with the specified radius
	 * (such that diameter equals 2 * radius + 1).
	 * 
	 * @param radius
	 *            the radius of the diamond
	 * @return a new DiamondStrel instance
	 */
	public final static DiamondStrel fromRadius(int radius) {
		return new DiamondStrel(2 * radius + 1, radius);
	}
	
	// ==================================================
	// Class variables
	
	/**
	 * The size of the diamond, given as orthogonal diameter. 
	 */
	int size;
	
	/**
	 * The offset of the diamond, which is the same in all directions. 
	 */
	int offset;

	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new diamond structuring element of given diameter.
	 * Diameter must be odd.
	 * 
	 * @param size the diameter of the diamond, that must be odd
	 * @throws IllegalArgumentException if size is negative or zero
	 * @throws IllegalArgumentException if size is even
	 */
	public DiamondStrel(int size) {
		this(size, (size - 1) / 2);
	}

	/**
	 * Creates a new diamond structuring element with a given diameter and offset.
	 * Diameter must be odd and positive. Offset must be comprised between 0 and size-1.
	 * 
	 * @param size the diameter of the diamond, that must be odd
	 * @param offset the position of the reference point
	 * @throws IllegalArgumentException if size is negative or zero
	 * @throws IllegalArgumentException if size is even
	 * @throws IllegalArgumentException if offset is negative or greater than size
	 */
	public DiamondStrel(int size, int offset) {
		if (size < 1) {
			throw new IllegalArgumentException("Requires a positive size");
		}
		if (size % 2 != 1) {
			throw new IllegalArgumentException("Diamond size must be odd");
		}
		this.size = size;
		
		if (offset < 0) {
			throw new IllegalArgumentException("Requires a non-negative offset");
		}
		if (offset >= size) {
			throw new IllegalArgumentException("Offset can not be greater than size");
		}
		this.offset = offset;
	}
	
	
	// ==================================================
	// General methods 
	
	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getSize()
	 */
	@Override
	public int[] getSize() {
		return new int[]{this.size, this.size};
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getMask()
	 */
	@Override
	public int[][] getMask() {
		int[][] mask = new int[this.size][this.size];
		
		// Fill everything with 255
		for(int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				mask[i][j] = 255;
			}
		}

		// Put zeros at the corners
		int radius = (this.size - 1) / 2;
		for(int i = 0; i < radius; i++) {
			for (int j = 0; j < radius - i; j++) {
				mask[i][j] = 0;
				mask[i][this.size - 1 - j] = 0;
				mask[this.size - 1 - i][j] = 0;
				mask[this.size - 1 - i][this.size - 1 - j] = 0;
			}
		}
		
		// return the mask
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
		// Put zeros at the corners
		int radius = (this.size - 1) / 2;
		
		// allocate memory for shifts
		int nShifts = radius * (radius - 1) / 2 + 2 * this.size - 1;
		int[][] shifts = new int[nShifts][2];

		// Compute the shifts in each row of the mask
		int iShift = 0;
		for(int i = 0; i < this.size; i++) {
			int i2 = Math.min(i, this.size - 1 - i);
			int j1 = radius - i2;
			int j2 = radius + i2;
			
			for (int j = j1; j <= j2; j++) {
				shifts[iShift][0] = j - this.offset;
				shifts[iShift][1] = i - this.offset;
			}
			iShift++;
		}

		// return vector array
		return shifts;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.SeparableStrel#reverse()
	 */
	@Override
	public SeparableStrel reverse() {
		return new DiamondStrel(this.size, this.size - 1 - this.offset);
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.SeparableStrel#decompose()
	 */
	@Override
	public Collection<InPlaceStrel> decompose() {
		// allocate memory
		ArrayList<InPlaceStrel> strels = new ArrayList<InPlaceStrel>(3);

		// add each elementary strel
		int linSize = (this.size - 1) / 2;
		strels.add(ShiftedCross3x3Strel.RIGHT);
		strels.add(new LinearDiagUpStrel(linSize));
		strels.add(new LinearDiagDownStrel(linSize));
		
		return strels;
	}

}
