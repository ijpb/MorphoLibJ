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
package inra.ijpb.data.border;

import ij.process.ImageProcessor;

/**
 * Assess pixel outside image bounds have same value as the closest pixel
 * on image border.
 *  
 * @author David Legland
 *
 */
public class ReplicatedBorder implements BorderManager
{
	ImageProcessor image;

	/**
	 * Creates a new Replicating Border Manager
	 * 
	 * @param image
	 *            the image to expand
	 */
	public ReplicatedBorder(ImageProcessor image)
	{
		this.image = image;
	}

	/**
	 * Forces both of x and y to be between 0 and the corresponding image size,
	 * and returns the corresponding image value.
	 * 
	 * @see inra.ijpb.data.border.BorderManager#get(int, int)
	 */
	@Override
	public int get(int x, int y)
	{
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		x = Math.min(x, image.getWidth() - 1);
		y = Math.min(y, image.getHeight() - 1);
		return this.image.get(x, y);
	}

	/**
	 * Forces both of x and y to be between 0 and the corresponding image size,
	 * and returns the corresponding image value.
	 * 
	 * @see inra.ijpb.data.border.BorderManager#getf(int, int)
	 */
	@Override
	public float getf(int x, int y)
	{
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		x = Math.min(x, image.getWidth() - 1);
		y = Math.min(y, image.getHeight() - 1);
		return this.image.getf(x, y);
	}
}
