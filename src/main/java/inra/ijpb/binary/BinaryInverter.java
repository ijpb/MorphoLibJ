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
package inra.ijpb.binary;

import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;

/**
 * Utility class that inverts a binary image.
 * 
 * @author dlegland
 *
 */
public class BinaryInverter extends AlgoStub
{
	/**
	 * Inverts the specified binary stack by converting each value within the
	 * stack.
	 * 
	 * @param image
	 *            the image to invert.
	 */
	public void processInPlace(ImageStack image)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int nSlices = image.getSize();
		
		for (int z = 0; z < nSlices; z++)
		{
			this.fireProgressChanged(this, z, nSlices);
			
			ImageProcessor ip = image.getProcessor(z + 1);
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					ip.set(x, y, ip.get( x, y ) > 0 ? 0 : 255);
				}
			}
		}

		this.fireProgressChanged(this, 1, 1);
	}

}
