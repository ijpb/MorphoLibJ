/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
 * Implementation of Chamfer Weights for 3D images that manages three type
 * offset types, corresponding to orthogonal, square-diagonal and cube-diagonal
 * neighbors.
 * 
 * By default, computation is performed using integers.
 * 
 * @see ChamferMask3DW3Float
 * @see ChamferMask3DW4
 * 
 * @author dlegland
 */
public class ChamferMask3DW3 extends ChamferMask3D
{
	short a;
	short b;
	short c;
	
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
	public ChamferMask3DW3(int w0, int w1, int w2)
	{
		this.a = (short) w0;
		this.b = (short) w1;
		this.c = (short) w2;
	}

	/**
	 * Creates a new ChamferMask3D object by specifying the weights
	 * associated to orthogonal and diagonal neighbors.
	 * 
	 * @param weights the weights associated to the different types of offset
	 */	
	public ChamferMask3DW3(short[] weights)
	{
		if (weights.length != 3)
		{
			throw new RuntimeException("Number of weights must be 3, not " + weights.length);
		}
		this.a = weights[0];
		this.b = weights[1];
		this.c = weights[2];
	}

	@Override
	public Collection<ShortOffset> getForwardOffsets()
	{
		// create array of forward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();
	
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
	
		// offsets in the current plane
		offsets.add(new ShortOffset(-1, -1, 0, b));
		offsets.add(new ShortOffset( 0, -1, 0, a));
		offsets.add(new ShortOffset(+1, -1, 0, b));
		offsets.add(new ShortOffset(-1,  0, 0, a));
	
		return offsets;
	}

	@Override
	public Collection<ShortOffset> getBackwardOffsets()
	{
		// create array of backward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();

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

		// offsets in the current plane
		offsets.add(new ShortOffset(-1, +1, 0, b));
		offsets.add(new ShortOffset( 0, +1, 0, a));
		offsets.add(new ShortOffset(+1, +1, 0, b));
		offsets.add(new ShortOffset(+1,  0, 0, a));

		return offsets;
	}

	@Override
	public short getShortNormalizationWeight()
	{
		return a;
	}
}
