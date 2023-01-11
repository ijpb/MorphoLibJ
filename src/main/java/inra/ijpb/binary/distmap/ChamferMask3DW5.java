/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
 * Implementation of Chamfer Weights for 3D images that manages five types of
 * offsets.
 * 
 * Offsets correspond to:
 * <ul>
 * <li>orthogonal neighbors</li>
 * <li>square-diagonal neighbors</li>
 * <li>cube-diagonal neighbors</li>
 * <li>shift by a permutation of (+/-1, +/-1, +/-2)</li>
 * <li>shift by a permutation of (+/-1, +/-1, +/-2)</li>
 * </ul>
 * 
 * @see ChamferMask3DW3
 * @see ChamferMask3DW3Float
 * 
 * @author dlegland
 *
 */
public class ChamferMask3DW5 extends ChamferMask3D
{
	short a;
	short b;
	short c;
	short d;
	short e;
	
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
	 * @param d
	 *            the weight associated to permutations of (2, 1, 0)
	 * @param e
	 *            the weight associated to permutations of (2, 1, 1)
	 */
	public ChamferMask3DW5(int a, int b, int c, int d, int e)
	{
		this.a = (short) a;
		this.b = (short) b;
		this.c = (short) c;
		this.d = (short) d;
		this.e = (short) e;
	}

	/**
	 * Creates a new ChamferMask3D object by specifying the weights
	 * associated to orthogonal and diagonal neighbors.
	 * 
	 * @param weights the weights associated to the different types of offset
	 */	
	public ChamferMask3DW5(short[] weights)
	{
		if (weights.length != 5)
		{
			throw new RuntimeException("Number of weights must be 5, not " + weights.length);
		}
		this.a = weights[0];
		this.b = weights[1];
		this.c = weights[2];
		this.d = weights[3];
		this.e = weights[4];
	}

	@Override
	public Collection<ShortOffset> getForwardOffsets()
	{
		// create array of forward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();
	
		
		
		// offsets in the z-2 plane
		offsets.add(new ShortOffset( 0, -1, -2, d));
		offsets.add(new ShortOffset(-1,  0, -2, d));
		offsets.add(new ShortOffset(+1,  0, -2, d));
		offsets.add(new ShortOffset( 0, +1, -2, d));
		offsets.add(new ShortOffset(-1, -1, -2, e));
		offsets.add(new ShortOffset(+1, -1, -2, e));
		offsets.add(new ShortOffset(-1, +1, -2, e));
		offsets.add(new ShortOffset(+1, +1, -2, e));

		// "type d" offsets in z-1 plane
		offsets.add(new ShortOffset( 0, -2, -1, d));
		offsets.add(new ShortOffset(-2,  0, -1, d));
		offsets.add(new ShortOffset(+2,  0, -1, d));
		offsets.add(new ShortOffset( 0, +2, -1, d));

		// offsets in the z-1 plane
		offsets.add(new ShortOffset(-1, -1, -1, c));
		offsets.add(new ShortOffset( 0, -1, -1, b));
		offsets.add(new ShortOffset(+1, -1, -1, c));
		offsets.add(new ShortOffset(-1,  0, -1, b));
		offsets.add(new ShortOffset( 0,  0, -1, a));
		offsets.add(new ShortOffset(+1,  0, -1, b));
		offsets.add(new ShortOffset(-1, +1, -1, c));
		offsets.add(new ShortOffset( 0, +1, -1, b));
		offsets.add(new ShortOffset(+1, +1, -1, c));
		
		// "type e" offsets in z-1 plane
		offsets.add(new ShortOffset(-1, -2, -1, e));
		offsets.add(new ShortOffset(+1, -2, -1, e));
		offsets.add(new ShortOffset(-2, -1, -1, e));
		offsets.add(new ShortOffset(+2, -1, -1, e));
		offsets.add(new ShortOffset(-2, +1, -1, e));
		offsets.add(new ShortOffset(+2, +1, -1, e));
		offsets.add(new ShortOffset(-1, +2, -1, e));
		offsets.add(new ShortOffset(+1, +2, -1, e));

	
		// "type d" offsets in current plane
		offsets.add(new ShortOffset(-1, -2,  0, d));
		offsets.add(new ShortOffset(+1, -2,  0, d));
		offsets.add(new ShortOffset(-2, -1,  0, d));
		offsets.add(new ShortOffset(+2, -1,  0, d));

		// offsets in the current plane
		offsets.add(new ShortOffset(-1, -1,  0, b));
		offsets.add(new ShortOffset( 0, -1,  0, a));
		offsets.add(new ShortOffset(+1, -1,  0, b));
		offsets.add(new ShortOffset(-1,  0,  0, a));
	
		return offsets;
	}

	@Override
	public Collection<ShortOffset> getBackwardOffsets()
	{
		// create array of backward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();

		// offsets in the z+2 plane
		offsets.add(new ShortOffset( 0, -1, +2, d));
		offsets.add(new ShortOffset(-1,  0, +2, d));
		offsets.add(new ShortOffset(+1,  0, +2, d));
		offsets.add(new ShortOffset( 0, +1, +2, d));
		offsets.add(new ShortOffset(-1, -1, +2, e));
		offsets.add(new ShortOffset(+1, -1, +2, e));
		offsets.add(new ShortOffset(-1, +1, +2, e));
		offsets.add(new ShortOffset(+1, +1, +2, e));

		// "type d" offsets in z-1 plane
		offsets.add(new ShortOffset( 0, -2, +1, d));
		offsets.add(new ShortOffset(-2,  0, +1, d));
		offsets.add(new ShortOffset(+2,  0, +1, d));
		offsets.add(new ShortOffset( 0, +2, +1, d));

		// offsets in the z+1 plane
		offsets.add(new ShortOffset(-1, -1, +1, c));
		offsets.add(new ShortOffset( 0, -1, +1, b));
		offsets.add(new ShortOffset(+1, -1, +1, c));
		offsets.add(new ShortOffset(-1,  0, +1, b));
		offsets.add(new ShortOffset( 0,  0, +1, a));
		offsets.add(new ShortOffset(+1,  0, +1, b));
		offsets.add(new ShortOffset(-1, +1, +1, c));
		offsets.add(new ShortOffset( 0, +1, +1, b));
		offsets.add(new ShortOffset(+1, +1, +1, c));
		
		// "type e" offsets in z+1 plane
		offsets.add(new ShortOffset(-1, -2, +1, e));
		offsets.add(new ShortOffset(+1, -2, +1, e));
		offsets.add(new ShortOffset(-2, -1, +1, e));
		offsets.add(new ShortOffset(+2, -1, +1, e));
		offsets.add(new ShortOffset(-2, +1, +1, e));
		offsets.add(new ShortOffset(+2, +1, +1, e));
		offsets.add(new ShortOffset(-1, +2, +1, e));
		offsets.add(new ShortOffset(+1, +2, +1, e));

		// "type d" offsets in current plane
		offsets.add(new ShortOffset(-1, +2,  0, d));
		offsets.add(new ShortOffset(+1, +2,  0, d));
		offsets.add(new ShortOffset(-2, +1,  0, d));
		offsets.add(new ShortOffset(+2, +1,  0, d));

		// offsets in the current plane
		offsets.add(new ShortOffset(-1, +1,  0, b));
		offsets.add(new ShortOffset( 0, +1,  0, a));
		offsets.add(new ShortOffset(+1, +1,  0, b));
		offsets.add(new ShortOffset(+1,  0,  0, a));

		return offsets;
	}

	@Override
	public short getShortNormalizationWeight()
	{
		return a;
	}
}
