/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
 * An Octagonal structuring element, obtained by decomposition into horizontal,
 * vertical, and diagonal linear structuring elements.
 * 
 * @see SquareStrel
 * @author David Legland
 *
 */
public class OctagonStrel extends AbstractSeparableStrel {

	// ==================================================
	// Static methods 
	
	/**
	 * Creates an octagon-shape structuring element with specified diameter.
	 * 
	 * @param diam
	 *            the orthogonal diameter of the octagon
	 * @return a new octagon-shape structuring element
	 */
	public final static OctagonStrel fromDiameter(int diam) {
		return new OctagonStrel(diam);
	}
	
	/**
	 * Creates an octagon-shape structuring element with specified radius.
	 * 
	 * @param radius
	 *            the radius of the octagon
	 * @return a new octagon-shape structuring element
	 */
	public final static OctagonStrel fromRadius(int radius) {
		return new OctagonStrel(2 * radius + 1, radius);
	}
	
	// ==================================================
	// Class variables
	
	/**
	 * The orthogonal diameter of the octagon. Computed from the square and
	 * diagonal sizes.
	 * 
	 * totalSize = squareSize + 2 * (diagSize - 1)
	 */
	int size;

	/**
	 * The offset of the octagon. Computed from the square and diagonal offsets.
	 */
	int offset;

	/**
	 * Size of the square sides, also the length of orthogonal linear
	 * structuring elements.
	 */
	int squareSize;

	/**
	 * Size of the diagonal sides, also the length of diagonal linear
	 * structuring elements.
	 */
	int diagSize;

	int squareOffset;
	int diagOffset;
	
	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new octagonal structuring element of a given orthogonal
	 * diameter.
	 * 
	 * @param size
	 *            the orthogonal diameter of the octagon
	 */
	public OctagonStrel(int size) {
		this(size, (int) Math.floor((size - 1) / 2));
	}

	/**
	 * Creates a new octagonal structuring element of a given orthogonal
	 * diameter and with a given offset.
	 * 
	 * @param size
	 *            the orthogonal diameter of the octagon
	 * @param offset
	 *            the position of the reference pixel in each direction
	 * @throws IllegalArgumentException
	 *             if size is negative or zero
	 * @throws IllegalArgumentException
	 *             if offset is negative or greater than size
	 */
	public OctagonStrel(int size, int offset) {
		if (size < 1) {
			throw new IllegalArgumentException("Requires a positive size");
		}
		this.size = size;
		
		if (offset < 0) {
			throw new IllegalArgumentException("Requires a non-negative offset");
		}
		if (offset >= size) {
			throw new IllegalArgumentException("Offset can not be greater than size");
		}
		this.offset = offset;
		
		// init side lengths
		this.diagSize = (int) Math.round((this.size + 2) / (2 + Math.sqrt(2)));
		this.squareSize = this.size - 2 * (this.diagSize - 1);

		// Init offsets
		this.squareOffset = (int) Math.floor((this.squareSize - 1) / 2);
		this.diagOffset = (int) Math.floor((this.diagSize - 1) / 2);
	}

	/**
	 * Creates the octagonal structuring element by specifying both square and
	 * diagonal sizes and offsets.
	 * 
	 * @param squareSize
	 *            the length of the sides in orthogonal directions
	 * @param diagSize
	 *            the length of the sides in diagonal directions
	 * @param squareOffset
	 *            the offset in orthogonal directions
	 * @param diagOffset
	 *            the offset in diagonal directions
	 * @throws IllegalArgumentException
	 *             if a size is negative or zero
	 * @throws IllegalArgumentException
	 *             if an offset is negative or greater than size
	 */
	protected OctagonStrel(int squareSize, int diagSize, int squareOffset,
			int diagOffset) {
		if (squareSize < 1) {
			throw new IllegalArgumentException("Requires a positive square size");
		}
		this.squareSize = squareSize;
		
		if (diagSize < 1) {
			throw new IllegalArgumentException("Requires a positive diagonal size");
		}
		this.diagSize = diagSize;
		
		if (squareOffset < 0) {
			throw new IllegalArgumentException("Requires a non-negative square offset");
		}
		this.squareOffset = squareOffset;
		
		if (diagOffset < 0) {
			throw new RuntimeException("Requires a non-negative diagonal offset");
		}
		this.diagOffset = diagOffset;
		
		this.size = this.squareSize + 2 * (this.diagSize - 1);
		this.offset = this.squareOffset + this.diagSize - 1;
	}

	
	// ==================================================
	// General methods 
	
	/**
	 * Returns a decomposition into four structuring elements, corresponding to
	 * horizontal, vertical, and diagonal linear structuring elements.
	 * 
	 * @see SeparableStrel#decompose()
	 * @see LinearHorizontalStrel
	 * @see LinearVerticalStrel
	 * @see LinearDiagUpStrel
	 * @see LinearDiagDownStrel
	 */
	@Override
	public Collection<InPlaceStrel> decompose() {
		// We need to use a different offset for horizontal lines, because 
		// the sum of offsets for diagonals shifts the reference point by one
		// pixel to the right
		int horizOffset = this.squareOffset;
		if (this.diagSize % 2 == 0)
			horizOffset = this.squareSize - 1 - this.squareOffset;
		
		// Allocate memory for linear strels
		ArrayList<InPlaceStrel> strels = new ArrayList<InPlaceStrel>(4);
		
		// create elementary strels in each of the four directions
		strels.add(new LinearHorizontalStrel(this.squareSize, horizOffset));
		strels.add(new LinearVerticalStrel(this.squareSize, this.squareOffset));
		strels.add(new LinearDiagUpStrel(this.diagSize, this.diagOffset));
		strels.add(new LinearDiagDownStrel(this.diagSize, this.diagOffset));
		return strels;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getMask()
	 */
	@Override
	public int[][] getMask() {
		// Create array
		int[][] mask = new int[this.size][this.size];
		
		// process the top part: 
		// a number of pixels grows from "squareSize", adding 2 at each row
        for (int y = 0; y < this.diagSize - 1; y++)
        {
            for (int x = this.diagSize-1-y; x < this.diagSize-1+this.squareSize+y; x++)
            {
                mask[y][x] = 255;
            }
        }
        
        // process the middle part: full rows
        for (int y = this.diagSize-1; y < this.diagSize + this.squareSize - 1; y++)
        {
            for (int x = 0; x < this.size; x++)
            {
                mask[y][x] = 255;
            }
        }
        
        // process the bottom part: 
        for (int i = 0; i < this.diagSize - 1; i++)
        {
            for (int x = i+1; x < this.size-1-i; x++)
            {
                mask[i+diagSize+squareSize-1][x] = 255;
            }
        }
		
		return mask;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getShifts()
	 */
	@Override
	public int[][] getShifts() {
		return convertMaskToShifts(getMask());
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getSize()
	 */
	@Override
	public int[] getSize() {
		return new int[]{this.size, this.size};
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getOffset()
	 */
	@Override
	public int[] getOffset() {
		return new int[]{this.offset, this.offset};
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#reverse()
	 */
	@Override
	public OctagonStrel reverse() {
		return new OctagonStrel(this.squareSize, this.diagSize, 
				this.squareSize - this.squareOffset - 1, 
				this.diagSize - this.diagOffset - 1);
	}

}
