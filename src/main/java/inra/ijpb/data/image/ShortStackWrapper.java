/**
 * 
 */
package inra.ijpb.data.image;

import ij.ImageStack;

import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 * @author David Legland
 *
 */
public class ShortStackWrapper implements Image3D {

	short[][] slices;
	
	int sizeX;
	int sizeY;
	int sizeZ;
	
	public ShortStackWrapper(ImageStack stack) {
		// Check type
		if (stack.getBitDepth() != 16) {
			throw new IllegalArgumentException("Requires a 16-bits stack");
		}
		
		// store stack size
		this.sizeX = stack.getWidth();
		this.sizeY = stack.getHeight();
		this.sizeZ = stack.getSize();

		// Convert slices type
		this.slices = new short[sizeZ][];
		Object[] array = stack.getImageArray();
		for (int i = 0; i < sizeZ; i++) {
			slices[i] = (short[]) array[i];
		}
	}
	
	/* (non-Javadoc)
	 * @see ijt.data.StackAccessor#get(int, int, int)
	 */
	@Override
	public int get(int x, int y, int z) {
		return slices[z][y * sizeX + x] & 0x00FFFF;
	}

	/* (non-Javadoc)
	 * @see ijt.data.StackAccessor#set(int, int, int, int)
	 */
	@Override
	public void set(int x, int y, int z, int value) {
		slices[z][y * sizeX + x] = (short) max(min(value, 65535), 0);
	}

	/* (non-Javadoc)
	 * @see ijt.data.StackAccessor#getValue(int, int, int)
	 */
	@Override
	public double getValue(int x, int y, int z) {
		return (double) (slices[z][y * sizeX + x] & 0x00FFFF);
	}

	/* (non-Javadoc)
	 * @see ijt.data.StackAccessor#setValue(int, int, int, double)
	 */
	@Override
	public void setValue(int x, int y, int z, double value) {
		slices[z][y * sizeX + x] = (short) max(min(value, 65535), 0);
	}

}
