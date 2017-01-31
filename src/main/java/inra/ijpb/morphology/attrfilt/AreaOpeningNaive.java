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
package inra.ijpb.morphology.attrfilt;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.segment.Threshold;

/**
 * Computes area opening using naive algorithm. Iterate over the list of
 * thresholds, compute binary image, apply binary area opening, and concatenate
 * the results.
 * 
 * @author dlegland
 */
public class AreaOpeningNaive extends AlgoStub implements AreaOpening
{

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.attrfilt.AreaOpening#process(ij.process.ImageProcessor, int)
	 */
	@Override
	public ImageProcessor process(ImageProcessor image, int minArea)
	{
		fireStatusChanged(this, "Initialize");
		
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		ByteProcessor result = new ByteProcessor(sizeX, sizeY);
		
		fireStatusChanged(this, "Compute thesholds");
		for (int level = 1; level <= 255; level++)
		{
			fireStatusChanged(this, "Threshold: " + level);
			fireProgressChanged(this, level-1, 255);
			
			// threshold
			ImageProcessor binary = Threshold.threshold(image, level, 255);
			
			// keep only components with size larger than minArea
			binary = BinaryImages.areaOpening(binary, minArea);
			
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					if (binary.get(x, y) > 0)
					{
						result.set(x, y, level);
					}
				}
			}
		}
		
		fireStatusChanged(this, "");
		fireProgressChanged(this, 1, 1);
		
		return result;
	}

}
