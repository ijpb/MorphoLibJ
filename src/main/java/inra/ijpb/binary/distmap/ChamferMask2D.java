/**
 * 
 */
package inra.ijpb.binary.distmap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Chamfer masks defines a series of weights associated to neighbors of current
 * pixels.
 *
 * The interface declares methods to access forward and backward offsets, used
 * in classical two-passes algorithms.
 * 
 * The weights may be defined either as integers or as floating point values.
 * 
 * @author dlegland
 */
public abstract class ChamferMask2D
{
	// ==================================================
	// Public constants
	
	/** Use weight equal to 1 for all neighbors. */
	public final static ChamferMask2D CHESSBOARD = new ChamferMask2DW2(1, 1);
	
	/**
	 * Use weights 1 for orthogonal neighbors and 2 for diagonal neighbors,
	 * and 3 for cube-diagonals.
	 */
	public final static ChamferMask2D CITY_BLOCK = new ChamferMask2DW2(1, 2);
	
	/**
	 * Use weights 3 for orthogonal neighbors and 4 for diagonal neighbors,
	 * and 5 for cube-diagonals (best approximation for 3-by-3-by-3 masks).
	 */
	public final static ChamferMask2D QUASI_EUCLIDEAN = new ChamferMask2DW2Float( 
			new short[] { 10, 14 }, 
			new float[] {1, (float) Math.sqrt(2) });

	/**
	 * Use weights 3 for orthogonal neighbors and 4 for diagonal neighbors,
	 * and 5 for cube-diagonals (best approximation for 3-by-3-by-3 masks).
	 */
	public final static ChamferMask2D BORGEFORS = new ChamferMask2DW2(3, 4);
	
	/**
	 * Use weights 5 for orthogonal neighbors, 7 for diagonal neighbors, and 11
	 * for chess-knight moves (recommended approximation for 5-by-5 masks).
	 */
	public final static ChamferMask2D CHESSKNIGHT = new ChamferMask2DW3(5, 7, 11);
	
	
	// ==================================================
	// Static factories
	
	public static final ChamferMask2D fromWeights(int[] weights)
	{
		if (weights.length == 1)
		{
			int a = weights[0];
			return new ChamferMask2DW2(a, 2 * a);
		}
		else if (weights.length == 2)
		{
			int a = weights[0];
			int b = weights[1];
			return new ChamferMask2DW2(a, b);
		}
		else if (weights.length == 3)
		{
			int a = weights[0];
			int b = weights[1];
			int c = weights[2];
			return new ChamferMask2DW3(a, b, c);
		}
		else
		{
			throw new RuntimeException("Can not create chamfer mask with the given number of weights: " + weights.length);
		}
	}
	
	public static final ChamferMask2D fromWeights(short[] weights)
	{
		if (weights.length == 1)
		{
			int a = weights[0];
			return new ChamferMask2DW2(a, 2 * a);
		}
		else if (weights.length == 2)
		{
			int a = weights[0];
			int b = weights[1];
			return new ChamferMask2DW2(a, b);
		}
		else if (weights.length == 3)
		{
			int a = weights[0];
			int b = weights[1];
			int c = weights[2];
			return new ChamferMask2DW3(a, b, c);
		}
		else
		{
			throw new RuntimeException("Can not create chamfer mask with the given number of weights: " + weights.length);
		}
	}
	
	public static final ChamferMask2D fromWeights(float[] weights)
	{
		// compute integer version of floating point weights
		// (multiply by 10 to reduce rounding effect)
		short[] intWeights = new short[weights.length];
		for (int i = 0; i < weights.length; i++)
		{
			intWeights[i] = (short) Math.round(weights[i] * 10.0);
		}
		
		if (weights.length == 2)
		{
			return new ChamferMask2DW2Float(intWeights, weights);
		}
		else
		{
			throw new RuntimeException("Can not create chamfer mask with the given number of weights: " + weights.length);
		}
	}
	

	// ==================================================
	// Global methods
	
	/**
	 * @return the whole collection of offsets defined by this ChamferWeights2D.
	 */
	public Collection<ShortOffset> getOffsets()
	{
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();
		offsets.addAll(getForwardOffsets());
		offsets.addAll(getBackwardOffsets());
		return offsets;
	}
	
	/**
	 * @return the whole collection of offsets using floating-point weights
	 *         defined by this ChamferWeights2D.
	 */
	public Collection<FloatOffset> getFloatOffsets()
	{
		ArrayList<FloatOffset> offsets = new ArrayList<FloatOffset>();
		offsets.addAll(getForwardFloatOffsets());
		offsets.addAll(getBackwardFloatOffsets());
		return offsets;
	}
	

	// ==================================================
	// Declaration of abstract methods
	
	/**
	 * @return the set of offsets defined by this ChamferWeights2D for forward
	 *         iteration using integer weights.
	 */
	public abstract Collection<ShortOffset> getForwardOffsets();

	/**
	 * @return the set of offsets defined by this ChamferWeights2D for backward
	 *         iteration using integer weights.
	 */
	public abstract Collection<ShortOffset> getBackwardOffsets();
	
	/**
	 * @return the set of offsets defined by this ChamferWeights2D for forward
	 *         iteration using floating-point weights.
	 */
	public Collection<FloatOffset> getForwardFloatOffsets()
	{
		return convertToFloat(getForwardOffsets());
	}
	
	/**
	 * @return the set of offsets defined by this ChamferWeights2D for backward
	 *         iteration using floating-point weights.
	 */
	public Collection<FloatOffset> getBackwardFloatOffsets()
	{
		return convertToFloat(getBackwardOffsets());
	}
	
	private static final Collection<FloatOffset> convertToFloat(Collection<ShortOffset> shortOffsets)
	{
		ArrayList<FloatOffset> offsets = new ArrayList<FloatOffset>(shortOffsets.size());
		
		for (ShortOffset offset : shortOffsets)
		{
			offsets.add(new FloatOffset(offset.dx, offset.dy, offset.weight));
		}
		return offsets;
	}


	// ==================================================
	// Inner classes declaration
	
	/**
	 * The shift to a neighbor of a reference pixel, as a pair (dx,dy),
	 * and the associated weights given as a short.
	 */
	public static class ShortOffset
	{
		public final int dx;
		public final int dy;
		public final short weight;

		public ShortOffset(int dx, int dy, short weight)
		{
			this.dx = dx;
			this.dy = dy;
			this.weight = weight;
		}
	}

	/**
	 * The shift to a neighbor of a reference pixel, as a pair (dx,dy),
	 * and the associated weights given as a float.
	 */
	public static class FloatOffset
	{
		public final int dx;
		public final int dy;
		public final float weight;

		public FloatOffset(int dx, int dy, float weight)
		{
			this.dx = dx;
			this.dy = dy;
			this.weight = weight;
		}
	}

}
