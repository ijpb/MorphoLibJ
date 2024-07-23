/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
/**
 * 
 */
package inra.ijpb.binary.distmap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A collection of weighted offsets for computing distance maps from 3D binary
 * images.
 * 
 * Each offset is defined by a (x,y,z) triplet and by a weight. The weights can
 * be defined as integers (making it possible to store result in
 * ShortProcessors) or as floating point values (could be more precise).
 * 
 * @see ChamferMask2D
 * @see ChamferDistanceTransform3D
 *
 * @author dlegland
 *
 */
public abstract class ChamferMask3D
{
	// ==================================================
	// Public constants
	
	/** Use weights equal to 1 for all neighbors. */
	public final static ChamferMask3D CHESSBOARD = new ChamferMask3DW3(new short[] {1, 1, 1});
	
	/**
	 * Use weights 1 for orthogonal neighbors, 2 for square-diagonal neighbors,
	 * and 3 for cube-diagonals.
	 */
	public final static ChamferMask3D CITY_BLOCK = new ChamferMask3DW3(new short[] {1, 2, 3});
	
	/**
	 * Use weights 3 for orthogonal neighbors, 4 for square-diagonal neighbors,
	 * and 5 for cube-diagonals (best approximation for 3-by-3-by-3 masks).
	 */
	public final static ChamferMask3D BORGEFORS = new ChamferMask3DW3(new short[] {3, 4, 5});
	
	/**
	 * Use weights 1 for orthogonal neighbors, sqrt(2) for square-diagonal
	 * neighbors, and sqrt(3) for cube-diagonals. 
	 * Use 10, 14 and 17 for short version.
	 */
	public final static ChamferMask3D QUASI_EUCLIDEAN = new ChamferMask3DW3Float(
			new short[] { 10, 14, 17 },
			new float[] { 1, (float) Math.sqrt(2), (float) Math.sqrt(3) });

	/**
	 * Use weights 3 for orthogonal neighbors, 4 for square-diagonal neighbors,
	 * 5 for cube-diagonals, and 7 for (2,1,1) shifts. Good approximation using
	 * only four weights, and keeping low value for orthogonal weight.
	 */
	public final static ChamferMask3D SVENSSON_3_4_5_7 = new ChamferMask3DW4(3, 4, 5, 7);
	
	
	// ==================================================
	// Static factories
	
	/**
	 * Creates a new Chamfer mask from a list of weights.
	 * 
	 * The size of the chamfer mask depends on the length of the array:
	 * <ul>
	 * <li>single element: chamfer mask is 3-by-3-by-3, the element value corresponds
	 * to the weight of orthogonal offsets</li>
	 * <li>two elements: chamfer mask is 3-by-3-by-3, the second element corresponds
	 * to the weight of square-diagonal offsets</li>
	 * <li>three elements: chamfer mask is 3-by-3-by-3, the third element corresponds
	 * to the weight of cube-diagonal offsets</li>
	 * <li>four elements: chamfer mask is 5-by-5-by-5, the fourth element corresponds
	 * to the weight of (1,1,2) offset and its permutations and symmetries </li>
	 * </ul>
	 * 
	 * @param weights
	 *            the list of weights used for building the chamfer mask
	 * @return the chamfer mask corresponding to the weights
	 */
	public static final ChamferMask3D fromWeights(int[] weights)
	{
		if (weights.length == 1)
		{
			int a = weights[0];
			return new ChamferMask3DW3(a, 2 * a, 3 * a);
		}
		else if (weights.length == 2)
		{
			int a = weights[0];
			int b = weights[1];
			return new ChamferMask3DW3(a, b, a + b);
		}
		else if (weights.length == 3)
		{
			int a = weights[0];
			int b = weights[1];
			int c = weights[2];
			return new ChamferMask3DW3(a, b, c);
		}
		else if (weights.length == 4)
		{
			int a = weights[0];
			int b = weights[1];
			int c = weights[2];
			int e = weights[3];
			return new ChamferMask3DW4(a, b, c, e);
		}
		else
		{
			throw new RuntimeException("Can not create chamfer mask with the given number of weights: " + weights.length);
		}
	}
	
	/**
	 * Creates a new Chamfer mask from a list of weights.
	 * 
	 * The size of the chamfer mask depends on the length of the array:
	 * <ul>
	 * <li>single element: chamfer mask is 3-by-3-by-3, the element value corresponds
	 * to the weight of orthogonal offsets</li>
	 * <li>two elements: chamfer mask is 3-by-3-by-3, the second element corresponds
	 * to the weight of square-diagonal offsets</li>
	 * <li>three elements: chamfer mask is 3-by-3-by-3, the third element corresponds
	 * to the weight of cube-diagonal offsets</li>
	 * <li>four elements: chamfer mask is 5-by-5-by-5, the fourth element corresponds
	 * to the weight of (1,1,2) offset and its permutations and symmetries </li>
	 * </ul>
	 * 
	 * @param weights
	 *            the list of weights used for building the chamfer mask
	 * @return the chamfer mask corresponding to the weights
	 */
	public static final ChamferMask3D fromWeights(short[] weights)
	{
		if (weights.length == 1)
		{
			int a = weights[0];
			return new ChamferMask3DW3(a, 2 * a, 3 * a);
		}
		else if (weights.length == 2)
		{
			int a = weights[0];
			int b = weights[1];
			return new ChamferMask3DW3(a, b, a + b);
		}
		else if (weights.length == 3)
		{
			int a = weights[0];
			int b = weights[1];
			int c = weights[2];
			return new ChamferMask3DW3(a, b, c);
		}
		else if (weights.length == 4)
		{
			int a = weights[0];
			int b = weights[1];
			int c = weights[2];
			int e = weights[3];
			return new ChamferMask3DW4(a, b, c, e);
		}
		else
		{
			throw new RuntimeException("Can not create chamfer mask with the given number of weights: " + weights.length);
		}
	}

	/**
	 * Creates a new Chamfer mask from a list of weights.
	 * 
	 * The size of the chamfer mask depends on the length of the array:
	 * <ul>
	 * <li>single element: chamfer mask is 3-by-3-by-3, the element value corresponds
	 * to the weight of orthogonal offsets</li>
	 * <li>two elements: chamfer mask is 3-by-3-by-3, the second element corresponds
	 * to the weight of square-diagonal offsets</li>
	 * <li>three elements: chamfer mask is 3-by-3-by-3, the third element corresponds
	 * to the weight of cube-diagonal offsets</li>
	 * <li>four elements: chamfer mask is 5-by-5-by-5, the fourth element corresponds
	 * to the weight of (1,1,2) offset and its permutations and symmetries </li>
	 * </ul>
	 * 
	 * @param weights
	 *            the list of weights used for building the chamfer mask
	 * @return the chamfer mask corresponding to the weights
	 */
	public static final ChamferMask3D fromWeights(float[] weights)
	{
		// compute integer version of floating point weights
		// (multiply by 10 to reduce rounding effect)
		short[] intWeights = new short[weights.length];
		for (int i = 0; i < weights.length; i++)
		{
			intWeights[i] = (short) Math.round(weights[i] + 10.0);
		}
		
		if (weights.length == 3)
		{
			return new ChamferMask3DW3Float(intWeights, weights);
		}
		else
		{
			throw new RuntimeException("Can not create chamfer mask with the given number of weights: " + weights.length);
		}
	}
	
	
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

	/**
	 * @return the weight associated to orthogonal offsets, that can be used to
	 *         normalize the distance map.
	 */
	public double getNormalizationWeight()
	{
		return getShortNormalizationWeight();
	}

	/**
	 * @return the weight associated to orthogonal offsets, that can be used to
	 *         normalize the distance map.
	 */
	public abstract short getShortNormalizationWeight();

	
	// ==================================================
	// Inner classes declaration
	
	/**
	 * The shift to a neighbor of a reference voxel, as a triplet (dx,dy,dz),
	 * and the associated weights given as a short.
	 */
	public static class ShortOffset
	{
		/** the offset along the X-axis */
		public final int dx;
		/** the offset along the Y-axis */
		public final int dy;
		/** the offset along the Z-axis */
		public final int dz;
		/** the weight associated to this offset */
		public final short weight;

		/**
		 * Creates a new Offset using a 16-bits integer weight.
		 * 
		 * @param dx
		 *            the offset along the X-axis
		 * @param dy
		 *            the offset along the Y-axis
		 * @param dz
		 *            the offset along the Z-axis
		 * @param weight
		 *            the weight of the offset
		 */
		public ShortOffset(int dx, int dy, int dz, short weight)
		{
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.weight = weight;
		}
	}

	/**
	 * The shift to a neighbor of a reference voxel, as a triplet (dx,dy,dz),
	 * and the associated weights given as a float.
	 */
	public static class FloatOffset
	{
		/** the offset along the X-axis */
		public final int dx;
		/** the offset along the Y-axis */
		public final int dy;
		/** the offset along the Z-axis */
		public final int dz;
		/** the weight associated to this offset */
		public final float weight;

		/**
		 * Creates a new Offset using a floating point weight.
		 * 
		 * @param dx
		 *            the offset along the X-axis
		 * @param dy
		 *            the offset along the Y-axis
		 * @param dz
		 *            the offset along the Z-axis
		 * @param weight
		 *            the weight of the offset
		 */
		public FloatOffset(int dx, int dy, int dz, float weight)
		{
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.weight = weight;
		}
	}
}
