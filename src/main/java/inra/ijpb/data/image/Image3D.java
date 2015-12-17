/**
 * 
 */
package inra.ijpb.data.image;

/**
 * Interface for accessing the values of a 3D stack. Implementations should
 * provide efficient access to the inner data, without checking coordinate
 * bounds. Data can be accessed either as integer or as double. 
 * 
 * <p>
 * Example of use:
 *<pre>{@code
 *	ImageStack stack = IJ.getImage().getStack();
 *	Image3D image = new ByteStackWrapper(stack);
 *	int val = image.get(0, 0, 0);
 *}</pre>
 * 
 * @author David Legland
 * 
 */
public interface Image3D 
{
	/**
	 * Returns the value at the specified coordinates as an integer.
	 * 
	 * @param x
	 *            the x-coordinate of the voxel (0-indexed)
	 * @param y
	 *            the y-coordinate of the voxel (0-indexed)
	 * @param z
	 *            the z-coordinate of the voxel (0-indexed)
	 * @return the value at the specified position
	 */
	public int get(int x, int y, int z);

	/**
	 * Changes the value at the specified coordinates, using an integer to
	 * specify the new value.
	 * 
	 * @param x
	 *            the x-coordinate of the voxel (0-indexed)
	 * @param y
	 *            the y-coordinate of the voxel (0-indexed)
	 * @param z
	 *            the z-coordinate of the voxel (0-indexed)
	 * @param value
	 *            the new value at the specified position
	 */
	public void set(int x, int y, int z, int value);

	/**
	 * Returns the value at the specified coordinates as a double.
	 * 
	 * @param x
	 *            the x-coordinate of the voxel (0-indexed)
	 * @param y
	 *            the y-coordinate of the voxel (0-indexed)
	 * @param z
	 *            the z-coordinate of the voxel (0-indexed)
	 * @return the value at the specified position
	 */
	public double getValue(int x, int y, int z);
	
	/**
	 * Changes the value at the specified coordinates, using a double to
	 * specify the new value.
	 * 
	 * @param x
	 *            the x-coordinate of the voxel (0-indexed)
	 * @param y
	 *            the y-coordinate of the voxel (0-indexed)
	 * @param z
	 *            the z-coordinate of the voxel (0-indexed)
	 * @param value
	 *            the new value at the specified position
	 */
	public void setValue(int x, int y, int z, double value);
}
