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
package inra.ijpb.label.conncomp;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.morphology.FloodFill;

/**
 * Computes the labels of the different connected components of a region within
 * a label image. The type of result is controlled by the bitDepth option.
 * 
 * Uses a Flood-fill type algorithm. The image pixels are iterated, and each
 * time a foreground pixel not yet associated with a label is encountered, its
 * connected component is associated with a new label.
 *
 * @see inra.ijpb.binary.conncomp.FloodFillComponentsLabeling
 * @see inra.ijpb.morphology.FloodFill
 * 
 * @author dlegland
 */
public class FloodFillRegionComponentsLabeling extends AlgoStub
{
	/** 
	 * The connectivity of the components, either 4 (default) or 8.
	 */
	int connectivity = 4;

	/**
	 * The number of bits for representing the result label image. Can be 8, 16
	 * (default), or 32.
	 */
	int bitDepth = 16;
	
	/**
	 * Constructor with default connectivity 4 and default output bitdepth equal to 16.  
	 */
	public FloodFillRegionComponentsLabeling()
	{
	}
	
	/**
	 * Constructor specifying the connectivity and using default output bitdepth equal to 16.  
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (4 or 8)
	 */
	public FloodFillRegionComponentsLabeling(int connectivity)
	{
		this.connectivity = connectivity;
	}
	
	/**
	 * Constructor specifying the connectivity and the bitdepth of result label
	 * image
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (4 or 8)
	 * @param bitDepth
	 *            the bit depth of the result (8, 16, or 32)
	 */
	public FloodFillRegionComponentsLabeling(int connectivity, int bitDepth)
	{
		this.connectivity = connectivity;
		this.bitDepth = bitDepth;
	}
	
    /**
     * Computes labels corresponding to connected components of a region within
     * a label map.
     * 
     * @param image
     *            the label map containing index of each region.
     * @param regionLabel
     *            the label of the region whose connected components have to be computed.
     * @return a new label map with the labels of the connected components of
     *         the specified region.
     */
	public ImageProcessor computeLabels(ImageProcessor image, int regionLabel)
	{
		// get image size
		int width = image.getWidth();
		int height = image.getHeight();
		int maxLabel;

		// Depending on bitDepth, create result image, and choose max label 
		// number
		ImageProcessor labelMap;
		switch (this.bitDepth) {
		case 8: 
			labelMap = new ByteProcessor(width, height);
			maxLabel = 255;
			break; 
		case 16: 
			labelMap = new ShortProcessor(width, height);
			maxLabel = 65535;
			break;
		case 32:
			labelMap = new FloatProcessor(width, height);
			maxLabel = 0x01 << 23 - 1;
			break;
		default:
			throw new IllegalArgumentException(
					"Bit Depth should be 8, 16 or 32.");
		}

		// the label counter
		int nLabels = 0;

		// iterate on image pixels to find new regions
		for (int y = 0; y < height; y++) 
		{
            if (Thread.currentThread().isInterrupted())
            {
                return null;
            }
			this.fireProgressChanged(this, y, height);
			
			for (int x = 0; x < width; x++) 
			{
			    // process only pixels from the selected region
				if (image.get(x, y) != regionLabel)
					continue;
				// do not process pixels that were already processed
				if (labelMap.get(x, y) > 0)
					continue;

				// a new label is found: check current label number  
				if (nLabels == maxLabel)
				{
					throw new RuntimeException("Max number of label reached (" + maxLabel + ")");
				}
				
				// increment label index, and propagate
				nLabels++;
				FloodFill.floodFillFloat(image, x, y, labelMap, nLabels, this.connectivity);
			}
		}
		this.fireProgressChanged(this, 1, 1);

		labelMap.setMinAndMax(0, nLabels);
		return labelMap;
	}

}
