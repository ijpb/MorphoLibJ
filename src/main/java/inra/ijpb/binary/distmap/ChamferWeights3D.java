/**
 * 
 */
package inra.ijpb.binary.distmap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A collection of weights offsets for computing  distance maps within 3D binary images.
 * 
 * Each offset is defined by a (x,y,z) triplet and by a weight. 
 * The weights can be defined as integers (making it possible to store result in ShortProcessors) 
 * or as floating point values (could be more precise).
 * 
 * @author dlegland
 *
 */
public abstract class ChamferWeights3D
{
	// ==================================================
	// Public constants
	
	/** Use weight equal to 1 for all neighbors. */
	public final static ChamferWeights3D CHESSBOARD = new ChamferWeights3DW3(new short[] {1, 1, 1});
	
	/**
	 * Use weights 1 for orthogonal neighbors and 2 for diagonal neighbors,
	 * and 3 for cube-diagonals.
	 */
	public final static ChamferWeights3D CITY_BLOCK = new ChamferWeights3DW3(new short[] {1, 2, 3});
	
	/**
	 * Use weights 3 for orthogonal neighbors and 4 for diagonal neighbors,
	 * and 5 for cube-diagonals (best approximation for 3-by-3-by-3 masks).
	 */
	public final static ChamferWeights3D BORGEFORS = new ChamferWeights3DW3(new short[] {3, 4, 5});
	
	
	// ==================================================
	// Global methods
	
	/**
	 * @return the whole collection of offsets defined by this ChamferWeights3D.
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
	 *         defined by this ChamferWeights3D.
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
	 * @return the set of offsets defined by this ChamferWeights3D for forward
	 *         iteration using integer weights.
	 */
	public abstract Collection<ShortOffset> getForwardOffsets();

	/**
	 * @return the set of offsets defined by this ChamferWeights3D for backward
	 *         iteration using integer weights.
	 */
	public abstract Collection<ShortOffset> getBackwardOffsets();
	
	/**
	 * @return the set of offsets defined by this ChamferWeights3D for forward
	 *         iteration using floating-point weights.
	 */
	public Collection<FloatOffset> getForwardFloatOffsets()
	{
		return convertToFloat(getForwardOffsets());
	}
	
	/**
	 * @return the set of offsets defined by this ChamferWeights3D for backward
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
			offsets.add(new FloatOffset(offset.dx, offset.dy, offset.dz, offset.weight));
		}
		return offsets;
		
	}

	// ==================================================
	// Inner classes declaration
	
	public static class ShortOffset
	{
		public final int dx;
		public final int dy;
		public final int dz;
		public final short weight;

		public ShortOffset(int dx, int dy, int dz, short weight)
		{
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.weight = weight;
		}
	}

	public static class FloatOffset
	{
		public final int dx;
		public final int dy;
		public final int dz;
		public final float weight;

		public FloatOffset(int dx, int dy, int dz, float weight)
		{
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.weight = weight;
		}
	}
}
