/**
 * 
 */
package inra.ijpb.binary.distmap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of Chamfer Weights that manages three type offset types,
 * corresponding to orthogonal, square-diagonal and cube-diagonal neighbors.
 * 
 * This implementation manages two series of weights, one for integer
 * computation, the other one for floating-point computation.
 * 
 * @see ChamferWeights3DW4
 * 
 * @author dlegland
 */
public class ChamferWeights3DW3Float extends ChamferWeights3D
{
	short[] shortWeights;
	float[] floatWeights;

	public ChamferWeights3DW3Float(short[] shortWeights, float[] floatWeights)
	{
		if (shortWeights.length != 3)
		{
			throw new RuntimeException("Number of short weights must be 3, not " + shortWeights.length);
		}
		if (floatWeights.length != 3)
		{
			throw new RuntimeException("Number of float weights must be 3, not " + floatWeights.length);
		}

		this.shortWeights = shortWeights;
		this.floatWeights = floatWeights;
	}

	@Override
	public Collection<ShortOffset> getForwardOffsets()
	{
		// create array of forward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();
	
		// offsets in the z-1 plane
		offsets.add(new ShortOffset(-1, -1, -1, shortWeights[2]));
		offsets.add(new ShortOffset( 0, -1, -1, shortWeights[1]));
		offsets.add(new ShortOffset(+1, -1, -1, shortWeights[2]));
		offsets.add(new ShortOffset(-1,  0, -1, shortWeights[1]));
		offsets.add(new ShortOffset( 0,  0, -1, shortWeights[0]));
		offsets.add(new ShortOffset(+1,  0, -1, shortWeights[1]));
		offsets.add(new ShortOffset(-1, +1, -1, shortWeights[2]));
		offsets.add(new ShortOffset( 0, +1, -1, shortWeights[1]));
		offsets.add(new ShortOffset(+1, +1, -1, shortWeights[2]));
	
		// offsets in the current plane
		offsets.add(new ShortOffset(-1, -1, 0, shortWeights[1]));
		offsets.add(new ShortOffset( 0, -1, 0, shortWeights[0]));
		offsets.add(new ShortOffset(+1, -1, 0, shortWeights[1]));
		offsets.add(new ShortOffset(-1,  0, 0, shortWeights[0]));
	
		return offsets;
	}

	@Override
	public Collection<ShortOffset> getBackwardOffsets()
	{
		// create array of backward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();

		// offsets in the z+1 plane
		offsets.add(new ShortOffset(-1, -1, +1, shortWeights[2]));
		offsets.add(new ShortOffset( 0, -1, +1, shortWeights[1]));
		offsets.add(new ShortOffset(+1, -1, +1, shortWeights[2]));
		offsets.add(new ShortOffset(-1,  0, +1, shortWeights[1]));
		offsets.add(new ShortOffset( 0,  0, +1, shortWeights[0]));
		offsets.add(new ShortOffset(+1,  0, +1, shortWeights[1]));
		offsets.add(new ShortOffset(-1, +1, +1, shortWeights[2]));
		offsets.add(new ShortOffset( 0, +1, +1, shortWeights[1]));
		offsets.add(new ShortOffset(+1, +1, +1, shortWeights[2]));

		// offsets in the current plane
		offsets.add(new ShortOffset(-1, +1, 0, shortWeights[1]));
		offsets.add(new ShortOffset( 0, +1, 0, shortWeights[0]));
		offsets.add(new ShortOffset(+1, +1, 0, shortWeights[1]));
		offsets.add(new ShortOffset(+1,  0, 0, shortWeights[0]));

		return offsets;
	}

	@Override
	public Collection<FloatOffset> getForwardFloatOffsets()
	{
		// create array of forward shifts
		ArrayList<FloatOffset> offsets = new ArrayList<FloatOffset>();
	
		// offsets in the z-1 plane
		offsets.add(new FloatOffset(-1, -1, -1, floatWeights[2]));
		offsets.add(new FloatOffset( 0, -1, -1, floatWeights[1]));
		offsets.add(new FloatOffset(+1, -1, -1, floatWeights[2]));
		offsets.add(new FloatOffset(-1,  0, -1, floatWeights[1]));
		offsets.add(new FloatOffset( 0,  0, -1, floatWeights[0]));
		offsets.add(new FloatOffset(+1,  0, -1, floatWeights[1]));
		offsets.add(new FloatOffset(-1, +1, -1, floatWeights[2]));
		offsets.add(new FloatOffset( 0, +1, -1, floatWeights[1]));
		offsets.add(new FloatOffset(+1, +1, -1, floatWeights[2]));
	
		// offsets in the current plane
		offsets.add(new FloatOffset(-1, -1, 0, floatWeights[1]));
		offsets.add(new FloatOffset( 0, -1, 0, floatWeights[0]));
		offsets.add(new FloatOffset(+1, -1, 0, floatWeights[1]));
		offsets.add(new FloatOffset(-1,  0, 0, floatWeights[0]));
	
		return offsets;
	}

	@Override
	public Collection<FloatOffset> getBackwardFloatOffsets()
	{
		// create array of backward shifts
		ArrayList<FloatOffset> offsets = new ArrayList<FloatOffset>();

		// offsets in the z+1 plane
		offsets.add(new FloatOffset(-1, -1, +1, floatWeights[2]));
		offsets.add(new FloatOffset( 0, -1, +1, floatWeights[1]));
		offsets.add(new FloatOffset(+1, -1, +1, floatWeights[2]));
		offsets.add(new FloatOffset(-1,  0, +1, floatWeights[1]));
		offsets.add(new FloatOffset( 0,  0, +1, floatWeights[0]));
		offsets.add(new FloatOffset(+1,  0, +1, floatWeights[1]));
		offsets.add(new FloatOffset(-1, +1, +1, floatWeights[2]));
		offsets.add(new FloatOffset( 0, +1, +1, floatWeights[1]));
		offsets.add(new FloatOffset(+1, +1, +1, floatWeights[2]));

		// offsets in the current plane
		offsets.add(new FloatOffset(-1, +1, 0, floatWeights[1]));
		offsets.add(new FloatOffset( 0, +1, 0, floatWeights[0]));
		offsets.add(new FloatOffset(+1, +1, 0, floatWeights[1]));
		offsets.add(new FloatOffset(+1,  0, 0, floatWeights[0]));

		return offsets;
	}
}
