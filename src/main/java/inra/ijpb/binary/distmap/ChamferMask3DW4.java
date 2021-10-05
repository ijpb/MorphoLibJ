/**
 * 
 */
package inra.ijpb.binary.distmap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of Chamfer Weights for 3D images that manages four types of
 * offsets.
 * 
 * Offsets correspond to:
 * <ul>
 * <li>orthogonal neighbors</li>
 * <li>square-diagonal neighbors</li>
 * <li>cube-diagonal neighbors</li>
 * <li>shift by a permutation of (+/-1, +/-1, +/-2)</li>
 * </ul>
 * 
 * @see ChamferMask3DW3
 * @see ChamferMask3DW3Float
 * 
 * @author dlegland
 *
 */
public class ChamferMask3DW4 extends ChamferMask3D
{
	short[] weights;
	
	/**
	 * Creates a new ChamferWeights3D object by specifying the four weights
	 * associated to the four different kind of offsets. 
	 * Notations are from Svensson and Borgefors.
	 * 
	 * @param a
	 *            the weight associated to orthogonal neighbors
	 * @param b
	 *            the weight associated to square-diagonal neighbors
	 * @param c
	 *            the weight associated to cube-diagonal neighbors
	 * @param e
	 *            the weight associated to cube-diagonal neighbors
	 */
	public ChamferMask3DW4(int a, int b, int c, int e)
	{
		this.weights = new short[] {(short) a, (short) b, (short) c, (short) e};
	}

	public ChamferMask3DW4(short[] weights)
	{
		if (weights.length != 4)
		{
			throw new RuntimeException("Number of weights must be 4, not " + weights.length);
		}
		this.weights = weights;
	}

	@Override
	public Collection<ShortOffset> getForwardOffsets()
	{
		// create array of forward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();
	
		// offsets in the z-2 plane
		offsets.add(new ShortOffset(-1, -1, -2, weights[3]));
		offsets.add(new ShortOffset(+1, -1, -2, weights[3]));
		offsets.add(new ShortOffset(-1, +1, -2, weights[3]));
		offsets.add(new ShortOffset(+1, +1, -2, weights[3]));

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
		
		// "type e" offsets in z-1 plane
		offsets.add(new ShortOffset(-1, -2, -1, weights[3]));
		offsets.add(new ShortOffset(+1, -2, -1, weights[3]));
		offsets.add(new ShortOffset(-2, -1, -1, weights[3]));
		offsets.add(new ShortOffset(+2, -1, -1, weights[3]));
		offsets.add(new ShortOffset(-2, +1, -1, weights[3]));
		offsets.add(new ShortOffset(+2, +1, -1, weights[3]));
		offsets.add(new ShortOffset(-1, +2, -1, weights[3]));
		offsets.add(new ShortOffset(+1, +2, -1, weights[3]));

	
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

		// offsets in the z+2 plane
		offsets.add(new ShortOffset(-1, -1, +2, weights[3]));
		offsets.add(new ShortOffset(+1, -1, +2, weights[3]));
		offsets.add(new ShortOffset(-1, +1, +2, weights[3]));
		offsets.add(new ShortOffset(+1, +1, +2, weights[3]));

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
		
		// "type e" offsets in z+1 plane
		offsets.add(new ShortOffset(-1, -2, +1, weights[3]));
		offsets.add(new ShortOffset(+1, -2, +1, weights[3]));
		offsets.add(new ShortOffset(-2, -1, +1, weights[3]));
		offsets.add(new ShortOffset(+2, -1, +1, weights[3]));
		offsets.add(new ShortOffset(-2, +1, +1, weights[3]));
		offsets.add(new ShortOffset(+2, +1, +1, weights[3]));
		offsets.add(new ShortOffset(-1, +2, +1, weights[3]));
		offsets.add(new ShortOffset(+1, +2, +1, weights[3]));

		// offsets in the current plane
		offsets.add(new ShortOffset(-1, +1, 0, weights[1]));
		offsets.add(new ShortOffset( 0, +1, 0, weights[0]));
		offsets.add(new ShortOffset(+1, +1, 0, weights[1]));
		offsets.add(new ShortOffset(+1,  0, 0, weights[0]));

		return offsets;
	}
}
