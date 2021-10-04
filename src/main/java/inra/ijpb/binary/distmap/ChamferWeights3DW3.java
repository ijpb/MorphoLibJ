/**
 * 
 */
package inra.ijpb.binary.distmap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of Chamfer Weights that manages three type offset types,
 * corresponding to orthogonal, square-diagonal and cube-diagonal neighbors.
 * By default, computation is performed using integers.
 * 
 * @see ChamferWeights3DW4
 * 
 * @author dlegland
 */
public class ChamferWeights3DW3 extends ChamferWeights3D
{
	short[] weights;
	
	/**
	 * Creates a new ChamferWeights3D object by specifying the weights
	 * associated to orthogonal, square-diagonal and cube-diagonal neighbors,
	 * respectively.
	 * 
	 * @param w0
	 *            the weight associated to orthogonal neighbors
	 * @param w1
	 *            the weight associated to square-diagonal neighbors
	 * @param w2
	 *            the weight associated to cube-diagonal neighbors
	 */
	public ChamferWeights3DW3(int w0, int w1, int w2)
	{
		this.weights = new short[] {(short) w0, (short) w1, (short) w2};
	}

	public ChamferWeights3DW3(short[] weights)
	{
		if (weights.length != 3)
		{
			throw new RuntimeException("Number of weights must be 3, not " + weights.length);
		}
		this.weights = weights;
	}

	@Override
	public Collection<ShortOffset> getForwardOffsets()
	{
		// create array of forward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();
	
		// offsets in the z-1 plane
		offsets.add(new ShortOffset(-1, -1, -1, weights[2]));
		offsets.add(new ShortOffset( 0, -1, -1, weights[1]));
		offsets.add(new ShortOffset(+1, -1, -1, weights[2]));
		offsets.add(new ShortOffset(-1,  0, -1, weights[1]));
		offsets.add(new ShortOffset( 0,  0, -1, weights[0]));
		offsets.add(new ShortOffset(+1,  0, -1, weights[1]));
		offsets.add(new ShortOffset(-1, +1, -1, weights[2]));
		offsets.add(new ShortOffset( 0, +1, -1, weights[1]));
		offsets.add(new ShortOffset(+1, +1, -1, weights[2]));
	
		// offsets in the current plane
		offsets.add(new ShortOffset(-1, -1, 0, weights[1]));
		offsets.add(new ShortOffset( 0, -1, 0, weights[0]));
		offsets.add(new ShortOffset(+1, -1, 0, weights[1]));
		offsets.add(new ShortOffset(-1,  0, 0, weights[0]));
	
		return offsets;
	}

	@Override
	public Collection<ShortOffset> getBackwardOffsets()
	{
		// create array of backward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();

		// offsets in the z+1 plane
		offsets.add(new ShortOffset(-1, -1, +1, weights[2]));
		offsets.add(new ShortOffset( 0, -1, +1, weights[1]));
		offsets.add(new ShortOffset(+1, -1, +1, weights[2]));
		offsets.add(new ShortOffset(-1,  0, +1, weights[1]));
		offsets.add(new ShortOffset( 0,  0, +1, weights[0]));
		offsets.add(new ShortOffset(+1,  0, +1, weights[1]));
		offsets.add(new ShortOffset(-1, +1, +1, weights[2]));
		offsets.add(new ShortOffset( 0, +1, +1, weights[1]));
		offsets.add(new ShortOffset(+1, +1, +1, weights[2]));

		// offsets in the current plane
		offsets.add(new ShortOffset(-1, +1, 0, weights[1]));
		offsets.add(new ShortOffset( 0, +1, 0, weights[0]));
		offsets.add(new ShortOffset(+1, +1, 0, weights[1]));
		offsets.add(new ShortOffset(+1,  0, 0, weights[0]));

		return offsets;
	}
}
