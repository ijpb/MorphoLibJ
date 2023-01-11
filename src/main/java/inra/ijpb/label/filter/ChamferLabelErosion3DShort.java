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
/**
 * 
 */
package inra.ijpb.label.filter;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.morphology.strel.ChamferStrel3D;

/**
 * Implementation of morphological erosion for 3D label images / label maps.
 * 
 * Can be applied to label maps encoded with 8 or 16 bits integers, or 32 bit
 * floats.
 * 
 * 
 * @author dlegland
 *
 */
public class ChamferLabelErosion3DShort extends AlgoStub
{
    /**
     * The chamfer mask used to propagate distances.
     */
    ChamferMask3D mask;
    
    /**
     * The radius of dilation of labels. In practice, the distance is propagated
     * up to radius + 0.5.
     */
    double radius;
    
    /**
     * For erosion, we can work only with the structuring element.
     */
    Strel3D strel;
    
    /**
     * Creates a new operator for erosion of label images based on Chamfer
     * masks. The principle is to compute for each non-zero voxel (label), the
     * distance to the nearest background voxel, and to apply a threshold on
     * this distance map.
     * 
     * @param mask
     *            the Chamfer mask use to propagate distances
     * @param radius
     *            the radius used to compute erosion from distance map.
     */
    public ChamferLabelErosion3DShort(ChamferMask3D mask, double radius)
    {
        this.mask = mask;
        this.radius = radius;
        
        this.strel = new ChamferStrel3D(mask, radius);
    }
    
    /**
     * Apply morphological erosion of the labels within the specified input
     * image.
     * 
     * @param image
     *            the label map to erode
     * @return the result of erosion.
     */
    public ImageStack process(ImageStack image)
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        // allocate memory for output
        ImageStack res = ImageStack.create(sizeX, sizeY, sizeZ, image.getBitDepth());
        
        // pre-compute strel shifts
        int[][] shifts = strel.getShifts3D();
        
        // iterate over pixels within output image
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    int label = (int) image.getVoxel(x, y, z);
                    
                    // no need to process background pixels
                    if (label == 0)
                    {
                        continue;
                    }
                    
                    // iterate over neighbors defined by strel
                    for (int[] shift : shifts)
                    {
                        // position of neighbor
                        int x2 = x + shift[0];
                        int y2 = y + shift[1];
                        int z2 = z + shift[2];
                        
                        // do not process pixels outside of image bounds
                        if (x2 < 0 || x2 >= sizeX) continue;
                        if (y2 < 0 || y2 >= sizeY) continue;
                        if (z2 < 0 || z2 >= sizeZ) continue;
                        
                        // check if neighbor is background
                        int label2 = (int) image.getVoxel(x2, y2, z2);
                        if (label2 == 0)
                        {
                            label = 0;
                            break;
                        }
                    }
                    
                    res.setVoxel(x, y, z, label);
                }
            }
        }
        return res;
    }
}
