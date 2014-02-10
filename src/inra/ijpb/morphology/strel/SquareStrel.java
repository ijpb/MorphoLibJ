/**
 * 
 */
package inra.ijpb.morphology.strel;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A square structuring element, obtained by decomposition into horizontal
 * and vertical linear structuring elements with the same size.
 * 
 * @see OctagonStrel
 * @see LinearHorizontalStrel
 * @see LinearVerticalStrel
 * @author David Legland
 *
 */
public class SquareStrel extends AbstractSeparableStrel {

	// ==================================================
	// Static methods 
	
	public final static SquareStrel fromDiameter(int diam) {
		return new SquareStrel(diam);
	}
	
	public final static SquareStrel fromRadius(int radius) {
		return new SquareStrel(2 * radius + 1, radius);
	}
	
	// ==================================================
	// Class variables
	
	/**
	 * The size of each side of the square. 
	 */
	int size;
	
	/**
	 * The offset of the square, which is the same in all directions. 
	 */
	int offset;

	
	// ==================================================
	// Constructors 
	
	/**
	 * Creates a new square structuring element of a given size.
	 * @param size the length of each side of the square
	 */
	public SquareStrel(int size) {
		if (size < 1) {
			throw new RuntimeException("Requires a positive size");
		}
		this.size = size;
		
		this.offset = (int) Math.floor((this.size - 1) / 2);
	}
	
	/**
	 * Creates a new square structuring element of a given size and
	 * with a given offset. 
	 * @param size the length of each side of the square
	 * @param offset the position of the reference pixel in each direction
	 */
	public SquareStrel(int size, int offset) {
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
	 * @see ijt.morphology.SeparableStrel#separate()
	 */
	@Override
	public Collection<InPlaceStrel> decompose() {
		ArrayList<InPlaceStrel> strels = new ArrayList<InPlaceStrel>(2);
		strels.add(new LinearHorizontalStrel(this.size, this.offset));
		strels.add(new LinearVerticalStrel(this.size, this.offset));
		return strels;
	}

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#getMask()
	 */
	@Override
	public int[][] getMask() {
		int[][] mask = new int[this.size][this.size];
		for (int y = 0; y < this.size; y++) {
			for (int x = 0; x < this.size; x++) {
				mask[y][x] = 255;
			}
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
		int n = this.size * this.size;
		int[][] shifts = new int[n][2];
		int i = 0;
		
		for (int y = 0; y < this.size; y++) {
			for (int x = 0; x < this.size; x++) {
				shifts[i][0] = x - this.offset;
				shifts[i][1] = y - this.offset;
				i++;
			}
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

	/* (non-Javadoc)
	 * @see ijt.morphology.Strel#reverse()
	 */
	@Override
	public SquareStrel reverse() {
		return new SquareStrel(this.size, this.size - this.offset - 1);
	}

}
