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
package inra.ijpb.data.image;

import ij.ImageStack;
import inra.ijpb.data.Cursor3D;

/**
 * Access the data of a 3D image containing gray8 values stored as bytes.
 * 
 * 
 * <p>
 * Example of use:
 *<pre>{@code
 *	ImageStack stack = IJ.getImage().getStack();
 *	Image3D image = new ByteStackWrapper(stack);
 *	int val = image.get(0, 0, 0);
 *}</pre>
 * 
 * @see ShortStackWrapper
 * @see FloatStackWrapper
 * 
 * @author David Legland
 *
 */
public class ByteStackWrapper implements Image3D 
{
	byte[][] slices;
	
	int sizeX;
	int sizeY;
	int sizeZ;
	
	/**
	 * Creates a new wrapper to the ImageStack.
	 * 
	 * @param stack the stack to wrap (bit depth must equal 8)
	 */
	public ByteStackWrapper(ImageStack stack) 
	{
		// Check type
		if (stack.getBitDepth() != 8) 
		{
			throw new IllegalArgumentException("Requires a 8-bits stack");
		}
		
		// store stack size
		this.sizeX = stack.getWidth();
		this.sizeY = stack.getHeight();
		this.sizeZ = stack.getSize();

		// Convert slices type
		this.slices = new byte[sizeZ][];
		Object[] array = stack.getImageArray();
		for (int i = 0; i < sizeZ; i++) 
		{
			slices[i] = (byte[]) array[i];
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
		return slices[z][y * sizeX + x] & 0x00FF;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.data.image.Image3D#set(int, int, int, int)
	 */
	@Override
	public void set(int x, int y, int z, int value)
	{
		if (value > 255)
			value = 255;
		else if (value < 0)
			value = 0;
		slices[z][y * sizeX + x] = (byte) value;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.data.image.Image3D#getValue(int, int, int)
	 */
	@Override
	public double getValue(int x, int y, int z) 
	{
		return (double) (slices[z][y * sizeX + x] & 0x00FF);
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
		if (value > 255)
			value = 255;
		else if (value < 0)
			value = 0;
		slices[z][y * sizeX + x] = (byte) (value + .5);
	}

	@Override
	public void setValue(Cursor3D pos, double value)
	{
		setValue(pos.getX(), pos.getY(), pos.getZ(), value);
	}
}
