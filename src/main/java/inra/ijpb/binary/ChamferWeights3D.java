/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
package inra.ijpb.binary;

import java.util.ArrayList;
import java.util.Collection;


/**
 * <p>
 * A pre-defined set of weights that can be used to compute 3D distance 
 * transform using chamfer approximations of euclidean metric.
 * </p>
 * 
 * <p>
 * Provides methods to access weight values either as float array or as short
 * array.
 * </p>
 * 
 * <p>
 * Example of use:
 * <pre><code>
 * float[] floatWeights = ChamferWeights3D.BORGEFORS.getFloatWeights();
 * boolean normalize = true;
 * DistanceTransform3D dt = new DistanceTransform3DFloat(floatWeights, normalize);
 * ImageStack result = dt.distanceMap(inputStack);
 * </code></pre>
 *
 * @deprecated replaced by inra.ijpb.binary.distmap.ChamferMask2D (since 1.5.0)
 * 
 * @see BinaryImages#distanceMap(ij.ImageStack)
 * @see inra.ijpb.binary.distmap.DistanceTransform3D
 */
@Deprecated
public enum ChamferWeights3D
{
	/** Use weight equal to 1 for all neighbors */
	CHESSBOARD("Chessboard (1,1,1)", new short[] { 1, 1, 1 }), 
	/**
	 * Use weights 1 for orthogonal neighbors and 2 for diagonal neighbors,
	 * and 3 for cube-diagonals.
	 */
	CITY_BLOCK("City-Block (1,2,3)", new short[] { 1, 2, 3 }), 
	/**
	 * Use weights 1 for orthogonal neighbors and sqrt(2) for diagonal
	 * neighbors, and sqrt(3) for cube-diagonals. 
	 * Use 10, 14 and 17 for short version.
	 */
	QUASI_EUCLIDEAN("Quasi-Euclidean (1,1.41,1.73)", 
			new short[] { 10, 14, 17 },
			new float[] { 1, (float) Math.sqrt(2), (float) Math.sqrt(3) }),	
	/**
	 * Use weights 3 for orthogonal neighbors and 4 for diagonal neighbors,
	 * and 5 for cube-diagonals (best approximation for 3-by-3-by-3 masks).
	 */
	BORGEFORS("Borgefors (3,4,5)", new short[] { 3, 4, 5 }),

	/**
	 * Use weights 3 for orthogonal neighbors and 4 for diagonal neighbors, and
	 * 5 for cube-diagonals, and 7 for (2,1,1) shifts. Good approximation using
	 * only four weights, and keeping low value of orthogonal weight.
	 */
	WEIGHTS_3_4_5_7("Svensson <3,4,5,7>", new short[] { 3, 4, 5, 7 });

	private final String label;
	private final short[] shortWeights;
	private final float[] floatWeights;

	private ChamferWeights3D(String label, short[] shortWeights)
	{
		this.label = label;
		this.shortWeights = shortWeights;
		this.floatWeights = new float[shortWeights.length];
		for (int i = 0; i < shortWeights.length; i++)
			this.floatWeights[i] = (float) shortWeights[i];
	}

	private ChamferWeights3D(String label, short[] shortWeights,
			float[] floatWeights)
	{
		this.label = label;
		this.shortWeights = shortWeights;
		this.floatWeights = floatWeights;
	}

	/**
	 * @return the weights as shorts
	 */
	public short[] getShortWeights()
	{
		return this.shortWeights;
	}

	/**
	 * @return the weights as floats
	 */
	public float[] getFloatWeights()
	{
		return this.floatWeights;
	}

	/**
	 * @return the label associated to this chamfer weight
	 */
	public String toString()
	{
		return this.label;
	}

	/**
	 * @return all the chamfer weights labels
	 */
	public static String[] getAllLabels()
	{
		int n = ChamferWeights3D.values().length;
		String[] result = new String[n];

		int i = 0;
		for (ChamferWeights3D weight : ChamferWeights3D.values())
			result[i++] = weight.label;

		return result;
	}

	/**
	 * Determines the operation type from its label.
	 * 
	 * @param label the name of a chamfer weight 
	 * @return the ChamferWeights3D enum corresponding to the given name
	 * 
	 * @throws IllegalArgumentException
	 *             if label name is not recognized.
	 */
	public static ChamferWeights3D fromLabel(String label)
	{
		if (label != null)
			label = label.toLowerCase();
		for (ChamferWeights3D weight : ChamferWeights3D.values())
		{
			String cmp = weight.label.toLowerCase();
			if (cmp.equals(label))
				return weight;
		}
		throw new IllegalArgumentException(
				"Unable to parse ChamferWeights3D with label: " + label);
	}
	
	/**
	 * Computes the collection of weighted offsets corresponding to a scan of
	 * the voxels in a 3D image in the forward direction.
	 * 
	 * The number of offsets depends on the number of weights (3 or 4 weights
	 * are required):
	 * <ul>
	 * <li>the first weight corresponds to a step in the orthogonal directions
	 * (three offsets).</li>
	 * <li>the second weight corresponds to a step in a diagonal direction
	 * within a plane (six offsets).</li>
	 * <li>the third weight corresponds to a step in a cube-diagonal direction
	 * (four offsets).</li>
	 * <li>the (optional) fourth weight corresponds to a step in a permutation
	 * of the (2,1,1) vector (twelve offsets).</li>
	 * </ul>
	 * 
	 * 	 
	 * @see #getForwardOffsets(float[])
	 * @see #getBackwardOffsets(short[])
	 * 
	 * @param weights
	 *            an array of (short) weights, corresponding to orthogonal,
	 *            diagonal, and optionally more neighbors.
	 * @return a collection of offset encapsulating the shift in x, y and z
	 *         directions, and the associated weight.
	 */
	public static Collection<ShortOffset> getForwardOffsets(short[] weights)
	{
		int nWeights = weights.length;
		if (nWeights < 3 || nWeights > 4)
		{
			throw new RuntimeException("Can not compute offset when number of weights equal " + nWeights);
		}
	
		// create array of forward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();
	
		if (nWeights == 4)
		{
			// offsets in the z-2 plane
			offsets.add(new ShortOffset(-1, -1, -2, weights[3]));
			offsets.add(new ShortOffset(+1, -1, -2, weights[3]));
			offsets.add(new ShortOffset(-1, +1, -2, weights[3]));
			offsets.add(new ShortOffset(+1, +1, -2, weights[3]));
		}
	
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
	
		if (nWeights == 4)
		{
			offsets.add(new ShortOffset(-1, -2, -1, weights[3]));
			offsets.add(new ShortOffset(+1, -2, -1, weights[3]));
			offsets.add(new ShortOffset(-2, -1, -1, weights[3]));
			offsets.add(new ShortOffset(+2, -1, -1, weights[3]));
			offsets.add(new ShortOffset(-2, +1, -1, weights[3]));
			offsets.add(new ShortOffset(+2, +1, -1, weights[3]));
			offsets.add(new ShortOffset(-1, +2, -1, weights[3]));
			offsets.add(new ShortOffset(+1, +2, -1, weights[3]));
		}			
		// offsets in the current plane
		offsets.add(new ShortOffset(-1, -1, 0, weights[1]));
		offsets.add(new ShortOffset( 0, -1, 0, weights[0]));
		offsets.add(new ShortOffset(+1, -1, 0, weights[1]));
		offsets.add(new ShortOffset(-1,  0, 0, weights[0]));
	
		return offsets;
	}

	/**
	 * Computes the collection of weighted offsets corresponding to a scan of
	 * the voxels in a 3D image in the forward direction.
	 * 
	 * The number of offsets depends on the number of weights (3 or 4 weights
	 * are required):
	 * <ul>
	 * <li>the first weight corresponds to a step in the orthogonal directions
	 * (three offsets).</li>
	 * <li>the second weight corresponds to a step in a diagonal direction
	 * within a plane (six offsets).</li>
	 * <li>the third weight corresponds to a step in a cube-diagonal direction
	 * (four offsets).</li>
	 * <li>the (optional) fourth weight corresponds to a step in a permutation
	 * of the (2,1,1) vector (twelve offsets).</li>
	 * </ul>
	 * 
	 * @see #getForwardOffsets(short[])
	 * @see #getBackwardOffsets(float[])
	 * 
	 * @param weights
	 *            an array of (short) weights, corresponding to orthogonal,
	 *            diagonal, and optionally more neighbors.
	 * @return a collection of offset encapsulating the shift in x, y and z
	 *         directions, and the associated weight.
	 */
	public static Collection<FloatOffset> getForwardOffsets(float[] weights)
	{
		int nWeights = weights.length;
		if (nWeights < 3 || nWeights > 4)
		{
			throw new RuntimeException("Can not compute offset when number of weights equal " + nWeights);
		}

		// create array of forward shifts
		ArrayList<FloatOffset> offsets = new ArrayList<FloatOffset>();

		if (nWeights == 4)
		{
			// offsets in the z-2 plane
			offsets.add(new FloatOffset(-1, -1, -2, weights[3]));
			offsets.add(new FloatOffset(+1, -1, -2, weights[3]));
			offsets.add(new FloatOffset(-1, +1, -2, weights[3]));
			offsets.add(new FloatOffset(+1, +1, -2, weights[3]));
		}

		// offsets in the z-1 plane
		offsets.add(new FloatOffset(-1, -1, -1, weights[2]));
		offsets.add(new FloatOffset( 0, -1, -1, weights[1]));
		offsets.add(new FloatOffset(+1, -1, -1, weights[2]));
		offsets.add(new FloatOffset(-1,  0, -1, weights[1]));
		offsets.add(new FloatOffset( 0,  0, -1, weights[0]));
		offsets.add(new FloatOffset(+1,  0, -1, weights[1]));
		offsets.add(new FloatOffset(-1, +1, -1, weights[2]));
		offsets.add(new FloatOffset( 0, +1, -1, weights[1]));
		offsets.add(new FloatOffset(+1, +1, -1, weights[2]));

		if (nWeights == 4)
		{
			offsets.add(new FloatOffset(-1, -2, -1, weights[3]));
			offsets.add(new FloatOffset(+1, -2, -1, weights[3]));
			offsets.add(new FloatOffset(-2, -1, -1, weights[3]));
			offsets.add(new FloatOffset(+2, -1, -1, weights[3]));
			offsets.add(new FloatOffset(-2, +1, -1, weights[3]));
			offsets.add(new FloatOffset(+2, +1, -1, weights[3]));
			offsets.add(new FloatOffset(-1, +2, -1, weights[3]));
			offsets.add(new FloatOffset(+1, +2, -1, weights[3]));
		}

		// offsets in the current plane
		offsets.add(new FloatOffset(-1, -1, 0, weights[1]));
		offsets.add(new FloatOffset( 0, -1, 0, weights[0]));
		offsets.add(new FloatOffset(+1, -1, 0, weights[1]));
		offsets.add(new FloatOffset(-1,  0, 0, weights[0]));

		return offsets;
	}

	/**
	 * Computes the collection of weighted offsets corresponding to a scan of
	 * the voxels in a 3D image in the backward direction.
	 * 
	 * The number of offsets depends on the number of weights (3 or 4 weights
	 * are required):
	 * <ul>
	 * <li>the first weight corresponds to a step in the orthogonal directions
	 * (three offsets).</li>
	 * <li>the second weight corresponds to a step in a diagonal direction
	 * within a plane (six offsets).</li>
	 * <li>the third weight corresponds to a step in a cube-diagonal direction
	 * (four offsets).</li>
	 * <li>the (optional) fourth weight corresponds to a step in a permutation
	 * of the (2,1,1) vector (twelve offsets).</li>
	 * </ul>
	 * 
	 * @see #getForwardOffsets(short[])
	 * 
	 * @param weights
	 *            an array of (short) weights, corresponding to orthogonal,
	 *            diagonal, and optionally more neighbors.
	 * @return a collection of offset encapsulating the shift in x, y and z
	 *         directions, and the associated weight.
	 */
	public static Collection<ShortOffset> getBackwardOffsets(short[] weights)
	{
		int nWeights = weights.length;
		if (nWeights < 3 || nWeights > 4)
		{
			throw new RuntimeException("Can not compute offset when number of weights equal " + nWeights);
		}

		// create array of backward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();

		if (nWeights == 4)
		{
			// offsets in the z+2 plane
			offsets.add(new ShortOffset(-1, -1, +2, weights[3]));
			offsets.add(new ShortOffset(+1, -1, +2, weights[3]));
			offsets.add(new ShortOffset(-1, +1, +2, weights[3]));
			offsets.add(new ShortOffset(+1, +1, +2, weights[3]));
		}

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

		if (nWeights == 4)
		{
			offsets.add(new ShortOffset(-1, -2, +1, weights[3]));
			offsets.add(new ShortOffset(+1, -2, +1, weights[3]));
			offsets.add(new ShortOffset(-2, -1, +1, weights[3]));
			offsets.add(new ShortOffset(+2, -1, +1, weights[3]));
			offsets.add(new ShortOffset(-2, +1, +1, weights[3]));
			offsets.add(new ShortOffset(+2, +1, +1, weights[3]));
			offsets.add(new ShortOffset(-1, +2, +1, weights[3]));
			offsets.add(new ShortOffset(+1, +2, +1, weights[3]));
		}

		// offsets in the current plane
		offsets.add(new ShortOffset(-1, +1, 0, weights[1]));
		offsets.add(new ShortOffset( 0, +1, 0, weights[0]));
		offsets.add(new ShortOffset(+1, +1, 0, weights[1]));
		offsets.add(new ShortOffset(+1,  0, 0, weights[0]));

		return offsets;
	}

	/**
	 * Computes the collection of weighted offsets corresponding to a scan of
	 * the voxels in a 3D image in the backward direction.
	 * 
	 * The number of offsets depends on the number of weights (3 or 4 weights
	 * are required):
	 * <ul>
	 * <li>the first weight corresponds to a step in the orthogonal directions
	 * (three offsets).</li>
	 * <li>the second weight corresponds to a step in a diagonal direction
	 * within a plane (six offsets).</li>
	 * <li>the third weight corresponds to a step in a cube-diagonal direction
	 * (four offsets).</li>
	 * <li>the (optional) fourth weight corresponds to a step in a permutation
	 * of the (2,1,1) vector (twelve offsets).</li>
	 * </ul>
	 * 
	 * @see #getForwardOffsets(short[])
	 * 
	 * @param weights
	 *            an array of (short) weights, corresponding to orthogonal,
	 *            diagonal, and optionally more neighbors.
	 * @return a collection of offset encapsulating the shift in x, y and z
	 *         directions, and the associated weight.
	 */
	public static Collection<FloatOffset> getBackwardOffsets(float[] weights)
	{
		int nWeights = weights.length;
		if (nWeights < 3 || nWeights > 4)
		{
			throw new RuntimeException("Can not compute offset when number of weights equal " + nWeights);
		}
		
		// create array of backward shifts
		ArrayList<FloatOffset> offsets = new ArrayList<FloatOffset>();

		if (nWeights == 4)
		{
			// offsets in the z+2 plane
			offsets.add(new FloatOffset(-1, -1, +2, weights[3]));
			offsets.add(new FloatOffset(+1, -1, +2, weights[3]));
			offsets.add(new FloatOffset(-1, +1, +2, weights[3]));
			offsets.add(new FloatOffset(+1, +1, +2, weights[3]));
		}

		// offsets in the z+1 plane
		offsets.add(new FloatOffset(-1, -1, +1, weights[2]));
		offsets.add(new FloatOffset( 0, -1, +1, weights[1]));
		offsets.add(new FloatOffset(+1, -1, +1, weights[2]));
		offsets.add(new FloatOffset(-1,  0, +1, weights[1]));
		offsets.add(new FloatOffset( 0,  0, +1, weights[0]));
		offsets.add(new FloatOffset(+1,  0, +1, weights[1]));
		offsets.add(new FloatOffset(-1, +1, +1, weights[2]));
		offsets.add(new FloatOffset( 0, +1, +1, weights[1]));
		offsets.add(new FloatOffset(+1, +1, +1, weights[2]));

		if (nWeights == 4)
		{
			offsets.add(new FloatOffset(-1, -2, +1, weights[3]));
			offsets.add(new FloatOffset(+1, -2, +1, weights[3]));
			offsets.add(new FloatOffset(-2, -1, +1, weights[3]));
			offsets.add(new FloatOffset(+2, -1, +1, weights[3]));
			offsets.add(new FloatOffset(-2, +1, +1, weights[3]));
			offsets.add(new FloatOffset(+2, +1, +1, weights[3]));
			offsets.add(new FloatOffset(-1, +2, +1, weights[3]));
			offsets.add(new FloatOffset(+1, +2, +1, weights[3]));
		}

		// offsets in the current plane
		offsets.add(new FloatOffset(-1, +1, 0, weights[1]));
		offsets.add(new FloatOffset( 0, +1, 0, weights[0]));
		offsets.add(new FloatOffset(+1, +1, 0, weights[1]));
		offsets.add(new FloatOffset(+1,  0, 0, weights[0]));

		return offsets;
	}

	/**
	 * The class for storing Chamfer offsets using integer weights.
	 */
	@Deprecated
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
		 * Creates a new offset for integer computations.
		 * 
		 * @param dx
		 *            the offset along the X-axis
		 * @param dy
		 *            the offset along the Y-axis
		 * @param dz
		 *            the offset along the Z-axis
		 * @param weight
		 *            the weight associated to this offset
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
	 * The class for storing Chamfer offsets using floating-point weights.
	 */
	@Deprecated
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
		 * Creates a new offset for floating point computations.
		 * 
		 * @param dx
		 *            the offset along the X-axis
		 * @param dy
		 *            the offset along the Y-axis
		 * @param dz
		 *            the offset along the Z-axis
		 * @param weight
		 *            the weight associated to this offset
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
