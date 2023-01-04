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
package inra.ijpb.binary.conncomp;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.morphology.FloodFill3D;

/**
 * Computes the labels of the connected components in a 3D binary image. The
 * type of result is controlled by the bitDepth option.
 * 
 * Uses a Flood-fill type algorithm. The image voxels are iterated, and each
 * time a foreground voxel not yet associated with a label is encountered, its
 * connected component is associated with a new label.
 * 
 * Example of use:
 * <pre>{@code
    int conn = 6;
    int bitDepth = 16;
    FloodFillComponentsLabeling3D algo = new FloodFillComponentsLabeling3D(conn, bitDepth);
    DefaultAlgoListener.monitor(algo);
    ImageStack labels = algo.computeLabels(image);
    // or:
    FloodFillComponentsLabeling3D.Result res = algo.computeResult(image);
    ImageStack labels = res.labelMap; 
 * }</pre> 
 * 
 * @see FloodFillComponentsLabeling
 * @see inra.ijpb.morphology.FloodFill3D
 * 
 * @author dlegland
 */
public class FloodFillComponentsLabeling3D extends AlgoStub implements
		ConnectedComponentsLabeling3D
{
	/** 
	 * The connectivity of the components, either 6 (default) or 26.
	 */
	int connectivity = 6;
	
	/**
	 * The number of bits for representing the result label image. Can be 8, 16
	 * (default), or 32.
	 */
	int bitDepth = 16;
	
	/**
	 * Constructor with default connectivity 6 and default output bitdepth equal to 16.  
	 */
	public FloodFillComponentsLabeling3D()
	{
	}
	
	/**
	 * Constructor specifying the connectivity and using default output bitdepth equal to 16.  
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (6 or 26)
	 */
	public FloodFillComponentsLabeling3D(int connectivity)
	{
		this.connectivity = connectivity;
	}
	
	/**
	 * Constructor specifying the connectivity and the bitdepth of result label
	 * image
	 * 
	 * @param connectivity
	 *            the connectivity of connected components (6 or 26)
	 * @param bitDepth
	 *            the bit depth of the result (8, 16, or 32)
	 */
	public FloodFillComponentsLabeling3D(int connectivity, int bitDepth)
	{
	    this.connectivity = connectivity;
	    this.bitDepth = bitDepth;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.binary.conncomp.ConnectedComponentsLabeling3D#computeLabels(ij.ImageStack)
	 */
	@Override
	public ImageStack computeLabels(ImageStack image)
	{
		if ( Thread.currentThread().isInterrupted() )					
			return null;
		
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
    public Result computeResult(ImageStack image)
    {
        if ( Thread.currentThread().isInterrupted() )                   
            return null;
        
        // get image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();

        // initialize result image
        fireStatusChanged(this, "Allocate memory...");
        Result res = new Result(ImageStack.create(sizeX, sizeY, sizeZ, bitDepth));

        // identify the maximum label index
        int maxLabel = FloodFillComponentsLabeling.largestPossibleLabel(this.bitDepth);

        fireStatusChanged(this, "Compute Labels...");
        
        // Iterate over image voxels. 
        // Each time a white voxel not yet associated
        // with a label is encountered, uses flood-fill to associate its
        // connected component to a new label
        for (int z = 0; z < sizeZ; z++) 
        {
            fireProgressChanged(this, z, sizeZ);
            for (int y = 0; y < sizeY; y++) 
            {
                for (int x = 0; x < sizeX; x++) 
                {
                    // Do not process background voxels
                    if (image.getVoxel(x, y, z) == 0)
                        continue;

                    // Do not process voxels already labeled
                    if (res.labelMap.getVoxel(x, y, z) > 0)
                        continue;

                    // a new label is found: check current label number  
                    if (res.nLabels == maxLabel)
                    {
                        throw new RuntimeException("Max number of label reached (" + maxLabel + ")");
                    }
                    
                    // increment label index, and propagate
                    res.nLabels++;
                    fireStatusChanged(this, "Process label " + res.nLabels);
                    FloodFill3D.floodFillFloat(image, x, y, z, res.labelMap, res.nLabels, this.connectivity);
                }
            }
        }
        
        fireStatusChanged(this, "");
        fireProgressChanged(this, 1, 1);
        return res;
    }
	
    /**
     * Data class that stores result of connected component labeling.
     */
    public class Result
    {
        /**
         * The image stack containing labels of connected components, or 0 for background.
         */
        public ImageStack labelMap;
        
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
        public Result(ImageStack labelMap)
        {
            this.labelMap = labelMap;
        }
    }
}
