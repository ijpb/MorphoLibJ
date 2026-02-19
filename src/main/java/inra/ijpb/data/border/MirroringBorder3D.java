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
package inra.ijpb.data.border;

import ij.ImageStack;

/**
 * Periodic border that considers image is mirrored indefinitely in all
 * directions.
 * 
 * @author David Legland
 *
 */
public class MirroringBorder3D implements BorderManager3D
{

	ImageStack image;

	/**
	 * Creates a new Mirroring Border Manager
	 * 
	 * @param image
	 *            the image to expand
	 */
	public MirroringBorder3D(ImageStack image)
	{
		this.image = image;
	}

	/**
	 * @see inra.ijpb.data.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y, int z)
	{
		int width = this.image.getWidth();
		int height = this.image.getHeight();
		int depth = this.image.getSize();
		x = x % (2 * width);
		y = y % (2 * height);
		z = z % (2 * depth);
		if (x < 0)
			x = -x - 1;
		if (y < 0)
			y = -y - 1;
		if (z < 0)
			z = -z - 1;
		if (x >= width)
			x = 2 * width - 1 - x;
		if (y >= height)
			y = 2 * height - 1 - y;
		if (z >= depth)
			z = 2 * depth - 1 - y;
		return (int) this.image.getVoxel(x, y, z);
	}

}
