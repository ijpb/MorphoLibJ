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
 * Implementation of Chamfer Weights that manages two types of offsets,
 * corresponding to orthogonal, and diagonal neighbors.
 * 
 * This implementation manages two series of weights, one for integer
 * computation, the other one for floating-point computation.
 * 
 * @see ChamferMask3DW3
 * @see ChamferMask3DW4
 * 
 * @author dlegland
 */
public class ChamferMask2DW2Float extends ChamferMask2D
{
	/**
	 * The offset weights used for integer computations.
	 */
	short[] shortWeights;

	/**
	 * The offset weights used for floating-point computations.
	 */
	float[] floatWeights;

	/**
	 * Creates a new chamfer mask
	 * 
	 * @param shortWeights
	 *            the offset weights used for integer computations
	 * @param floatWeights
	 *            the offset weights used for floating-point computations
	 */
	public ChamferMask2DW2Float(short[] shortWeights, float[] floatWeights)
	{
		if (shortWeights.length != 2)
		{
			throw new RuntimeException("Number of short weights must be 2, not " + shortWeights.length);
		}
		if (floatWeights.length != 2)
		{
			throw new RuntimeException("Number of float weights must be 2, not " + floatWeights.length);
		}

		this.shortWeights = shortWeights;
		this.floatWeights = floatWeights;
	}

	@Override
	public Collection<ShortOffset> getForwardOffsets()
	{
		// create array of forward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();
	
		offsets.add(new ShortOffset(-1, -1, shortWeights[1]));
		offsets.add(new ShortOffset( 0, -1, shortWeights[0]));
		offsets.add(new ShortOffset(+1, -1, shortWeights[1]));
		offsets.add(new ShortOffset(-1,  0, shortWeights[0]));
	
		return offsets;
	}

	@Override
	public Collection<ShortOffset> getBackwardOffsets()
	{
		// create array of backward shifts
		ArrayList<ShortOffset> offsets = new ArrayList<ShortOffset>();

		// offsets in the current plane
		offsets.add(new ShortOffset(-1, +1, shortWeights[1]));
		offsets.add(new ShortOffset( 0, +1, shortWeights[0]));
		offsets.add(new ShortOffset(+1, +1, shortWeights[1]));
		offsets.add(new ShortOffset(+1,  0, shortWeights[0]));

		return offsets;
	}

	@Override
	public Collection<FloatOffset> getForwardFloatOffsets()
	{
		// create array of forward shifts
		ArrayList<FloatOffset> offsets = new ArrayList<FloatOffset>();
	
		// offsets in the current plane
		offsets.add(new FloatOffset(-1, -1, floatWeights[1]));
		offsets.add(new FloatOffset( 0, -1, floatWeights[0]));
		offsets.add(new FloatOffset(+1, -1, floatWeights[1]));
		offsets.add(new FloatOffset(-1,  0, floatWeights[0]));
	
		return offsets;
	}

	@Override
	public Collection<FloatOffset> getBackwardFloatOffsets()
	{
		// create array of backward shifts
		ArrayList<FloatOffset> offsets = new ArrayList<FloatOffset>();

		// offsets in the current plane
		offsets.add(new FloatOffset(-1, +1, floatWeights[1]));
		offsets.add(new FloatOffset( 0, +1, floatWeights[0]));
		offsets.add(new FloatOffset(+1, +1, floatWeights[1]));
		offsets.add(new FloatOffset(+1,  0, floatWeights[0]));

		return offsets;
	}

	@Override
	public double getNormalizationWeight()
	{
		return floatWeights[0];
	}

	@Override
	public short getShortNormalizationWeight()
	{
		return shortWeights[0];
	}
}
