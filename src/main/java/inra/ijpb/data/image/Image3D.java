/**
 * 
 */
package inra.ijpb.data.image;

/**
 * Interface for accessing the values of a 3D stack. Implementations should
 * provide efficient access to the inner data, without checking coordinate
 * bounds. Data can be accessed either as integer or as double. 
 * 
 * @author David Legland
 * 
 */
public interface Image3D {

	public int get(int x, int y, int z);
	public void set(int x, int y, int z, int value);

	public double getValue(int x, int y, int z);
	public void setValue(int x, int y, int z, double value);
}
