/**
 * 
 */
package inra.ijpb.data.image;

import ij.ImageStack;

/**
 * Access the data of a 3D image containing intensity values stored as float.
 * 
 * @author David Legland
 *
 */
public class FloatStackWrapper implements Image3D {

	float[][] slices;
	
	int sizeX;
	int sizeY;
	int sizeZ;
	
	public FloatStackWrapper(ImageStack stack) {
		// Check type
		if (stack.getBitDepth() != 32) {
			throw new IllegalArgumentException("Requires a 32-bits stack");
		}
		
		// store stack size
		this.sizeX = stack.getWidth();
		this.sizeY = stack.getHeight();
		this.sizeZ = stack.getSize();

		// Convert slices type
		this.slices = new float[sizeZ][];
		Object[] array = stack.getImageArray();
		for (int i = 0; i < sizeZ; i++) {
			slices[i] = (float[]) array[i];
		}
	}
	
	/* (non-Javadoc)
	 * @see ijt.data.StackAccessor#get(int, int, int)
	 */
	@Override
	public int get(int x, int y, int z) {
		return (int) slices[z][y * sizeX + x];
	}

	/* (non-Javadoc)
	 * @see ijt.data.StackAccessor#set(int, int, int, int)
	 */
	@Override
	public void set(int x, int y, int z, int value) {
		slices[z][y * sizeX + x] = (float) value;
	}

	/* (non-Javadoc)
	 * @see ijt.data.StackAccessor#getValue(int, int, int)
	 */
	@Override
	public double getValue(int x, int y, int z) {
		return slices[z][y * sizeX + x];
	}

	/* (non-Javadoc)
	 * @see ijt.data.StackAccessor#setValue(int, int, int, double)
	 */
	@Override
	public void setValue(int x, int y, int z, double value) {
		slices[z][y * sizeX + x] = (float) value;
	}

}
