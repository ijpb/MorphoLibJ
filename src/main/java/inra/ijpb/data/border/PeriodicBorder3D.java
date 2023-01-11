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
package inra.ijpb.data.border;

import ij.ImageStack;

/**
 * Periodic border that considers image is repeated indefinitely in all
 * directions.
 * 
 * @author David Legland
 *
 */
public class PeriodicBorder3D implements BorderManager3D
{

	ImageStack image;

	/**
	 * Creates a new Periodic Border Manager
	 * 
	 * @param image
	 *            the image to expand
	 */
	public PeriodicBorder3D(ImageStack image)
	{
		this.image = image;
	}

	/**
	 * @see inra.ijpb.data.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y, int z)
	{
		x = x % image.getWidth();
		y = y % image.getHeight();
		z = z % image.getSize();
		if (x < 0)
			x += image.getWidth();
		if (y < 0)
			y += image.getHeight();
		if (z < 0)
			z += image.getSize();
		return (int) this.image.getVoxel(x, y, z);
	}

}
