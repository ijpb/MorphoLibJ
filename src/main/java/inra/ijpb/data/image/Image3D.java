/**
 * 
 */
package inra.ijpb.data.image;

/**
 * @author David Legland
 *
 */
public interface Image3D {

	public int get(int x, int y, int z);
	public void set(int x, int y, int z, int value);

	public double getValue(int x, int y, int z);
	public void setValue(int x, int y, int z, double value);
}
