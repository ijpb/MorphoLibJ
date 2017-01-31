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
package inra.ijpb.data.image;

import ij.ImageStack;
import inra.ijpb.data.Cursor3D;
import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 * Access the data of a 3D image containing gray16 values stored as short.
 * 
 * 
 * <p>
 * Example of use:
 *<pre>{@code
 *	ImageStack stack = IJ.getImage().getStack();
 *	Image3D image = new ShortStackWrapper(stack);
 *	int val = image.get(0, 0, 0);
 *}</pre>
 * 
 * @see ByteStackWrapper
 * @see FloatStackWrapper
 * 
 * @author David Legland
 *
 */
public class ShortStackWrapper implements Image3D
{
	short[][] slices;
	
	int sizeX;
	int sizeY;
	int sizeZ;
	
	public ShortStackWrapper(ImageStack stack) 
	{
		// Check type
		if (stack.getBitDepth() != 16) 
		{
			throw new IllegalArgumentException("Requires a 16-bits stack");
		}
		
		// store stack size
		this.sizeX = stack.getWidth();
		this.sizeY = stack.getHeight();
		this.sizeZ = stack.getSize();

		// Convert slices type
		this.slices = new short[sizeZ][];
		Object[] array = stack.getImageArray();
		for (int i = 0; i < sizeZ; i++)
		{
			slices[i] = (short[]) array[i];
		}
	}
	
	@Override
	public int getSize(int dim)
	{
		switch(dim)
		{
		case 0: return this.sizeX;
		case 1: return this.sizeY;
		case 2: return this.sizeZ;
		default:
			throw new IllegalArgumentException("Dimension must be comprised between 0 and 2, not " + dim);
		}
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.data.image.Image3D#get(int, int, int)
	 */
	@Override
	public int get(int x, int y, int z)
	{
		return slices[z][y * sizeX + x] & 0x00FFFF;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.data.image.Image3D#set(int, int, int, int)
	 */
	@Override
	public void set(int x, int y, int z, int value)
	{
		slices[z][y * sizeX + x] = (short) max(min(value, 65535), 0);
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.data.image.Image3D#getValue(int, int, int)
	 */
	@Override
	public double getValue(int x, int y, int z) 
	{
		return (double) (slices[z][y * sizeX + x] & 0x00FFFF);
	}

	@Override
	public double getValue(Cursor3D pos)
	{
		return getValue(pos.getX(), pos.getY(), pos.getZ());
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.data.image.Image3D#setValue(int, int, int, double)
	 */
	@Override
	public void setValue(int x, int y, int z, double value) 
	{
		slices[z][y * sizeX + x] = (short) max(min(value, 65535), 0);
	}

	@Override
	public void setValue(Cursor3D pos, double value)
	{
		setValue(pos.getX(), pos.getY(), pos.getZ(), value);
	}

}
