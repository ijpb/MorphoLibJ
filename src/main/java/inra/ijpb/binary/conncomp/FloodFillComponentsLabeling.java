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
package inra.ijpb.binary.conncomp;

import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.data.image.ImageUtils;
import inra.ijpb.morphology.FloodFill;

/**
 * Computes the labels of the connected components in a binary image. The type
 * of result is controlled by the bitDepth option.
 * 
 * Uses a Flood-fill type algorithm. The image pixels are iterated, and each
 * time a foreground pixel not yet associated with a label is encountered, its
 * connected component is associated with a new label.
 *
 * Example of use:
 * <pre>{@code
    int conn = 4;
    int bitDepth = 16;
    ConnectedComponentsLabeling algo = new FloodFillComponentsLabeling(conn, bitDepth);
    DefaultAlgoListener.monitor(algo);
    ImageProcessor labels = algo.computeLabels(image);
    // or:
    FloodFillComponentsLabeling.Result res = algo.computeResult(image);
    ImageProcessor labels = res.labelMap; 
 * }</pre> 
 * 
 * @see FloodFillComponentsLabeling3D
 * @see inra.ijpb.morphology.FloodFill
 * 
 * @author dlegland
 *
 */
public class FloodFillComponentsLabeling extends AlgoStub implements
		ConnectedComponentsLabeling
{
    /**
     * Returns the largest possible label that can be obtained given the
     * specified bit-depth.
     * 
     * The value of the largest possible label is usually obtained by
     * <code>2^bitdepth-1</code>.
     * 
     * @param bitDepth
     *            the bit-depth of the ImageProcessor used for storing labels.
     *            Must be either 8, 16 or 32.
     * @return the largest possible label that can be obtained with the given
     *         bit-depth.
     */
    public final static int largestPossibleLabel(int bitDepth)
    {
        // identify the maximum label index
        switch (bitDepth)
        {
        case 8: return 255;
        case 16: return 65535;
        case 32: return 0x01 << 23;
        default:throw new IllegalArgumentException(
                "Bit Depth should be 8, 16 or 32.");
        }
    }
    
    
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
	public FloodFillComponentsLabeling()
	{
	}
	
	/**
	 * Constructor specifying the connectivity and using default output bitdepth equal to 16.  
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (4 or 8)
	 */
	public FloodFillComponentsLabeling(int connectivity)
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
	public FloodFillComponentsLabeling(int connectivity, int bitDepth)
	{
		this.connectivity = connectivity;
		this.bitDepth = bitDepth;
	}
	
	/* (non-Javadoc)
	 * @see inra.ijpb.binary.conncomp.ConnectedComponentsLabeling#computeLabels(ij.process.ImageProcessor)
	 */
	@Override
	public ImageProcessor computeLabels(ImageProcessor image)
	{
		return computeResult(image).labelMap;
	}
	
	/**
     * Computes connected components labeling on the input binary image, and
     * returns the results encapsulated into a <code>Result</code> class
     * together with the largest label index.
     * 
     * @param image
     *            the input binary image
     * @return an instance of the Result class that can be used to retrieve the
     *         label map.
     */
	public Result computeResult(ImageProcessor image)
	{
        // get image size
        int width = image.getWidth();
        int height = image.getHeight();
        int maxLabel = largestPossibleLabel(this.bitDepth);

        // Depending on bitDepth, create result image, and determine max label number
        Result res = new Result(ImageUtils.createImageProcessor(width, height, this.bitDepth));
        
        // iterate on image pixels to find new regions
        for (int y = 0; y < height; y++) 
        {
            this.fireProgressChanged(this, y, height);
            for (int x = 0; x < width; x++) 
            {
                if (image.get(x, y) == 0)
                    continue;
                if (res.labelMap.get(x, y) > 0)
                    continue;

                // a new label is found: check current label number  
                if (res.nLabels == maxLabel)
                {
                    throw new RuntimeException("Max number of label reached (" + maxLabel + ")");
                }
                
                // increment label index, and propagate
                res.nLabels++;
                FloodFill.floodFillFloat(image, x, y, res.labelMap, res.nLabels, this.connectivity);
            }
        }
        this.fireProgressChanged(this, 1, 1);

        res.labelMap.setMinAndMax(0, res.nLabels);
        return res;
	}
	
    /**
     * Data class that stores result of connected component labeling.
     */
	public class Result
	{
	    /**
	     * The image processor containing labels of connected components, or 0 for background.
	     */
	    public ImageProcessor labelMap;
	    
        /**
         * The number of labels within the label map.
         */
	    public int nLabels = 0;
	    
	    /**
         * Creates a new Result class from an (empty) labelMap.
         * 
         * @param labelMap
         *            the labelMap that will be initialized during processing.
         */
	    Result(ImageProcessor labelMap)
	    {
	        this.labelMap = labelMap;
	    }
	}

}
