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
/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.ImageStack;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.Strel3D;

/**
 * @author dlegland
 *
 */
public class ChamferStrel3D extends AbstractStrel3D
{
    // ==================================================
    // Class variables

    ChamferMask3D chamferMask;
    double radius;
    
    int[][][] mask;
    
    // ==================================================
    // Constructors 
    
    /**
     * Creates a new ChamferStrel3D from a chamfer mask (used for computing
     * distances) and a radius (used to computing the extent of propagation).
     * 
     * @param chamferMask
     *            the chamfer mask
     * @param radius
     *            the radius
     */
    public ChamferStrel3D(ChamferMask3D chamferMask, double radius)
    {
        this.chamferMask = chamferMask;
        this.radius = radius;
        
        computeMask();
    }
    
    private void computeMask()
    {
        int[] size = getSize();
        ImageStack marker = ImageStack.create(size[0], size[1], size[2], 8);

        Images3D.fill(marker, 255);
        int[] offset = getOffset();
        marker.setVoxel(offset[0], offset[1], offset[2], 0);
        
        ImageStack distMap = BinaryImages.distanceMap(marker, this.chamferMask, true, true);
        
        this.mask = new int[size[2]][size[1]][size[0]];
        for (int z = 0; z < size[2]; z++)
        {
            for (int y = 0; y < size[1]; y++)
            {
                for (int x = 0; x < size[0]; x++)
                {
                    if (distMap.getVoxel(x, y, z) < this.radius + 0.5)
                    {
                        this.mask[z][y][x] = 255;
                    }
                }
            }
        }
    }
    

    // ==================================================
    // Implementation of Strel3D methods 
    
    @Override
    public int[] getSize()
    {
        int k = (int) Math.ceil(this.radius - 0.5);
        return new int[] {2 * k + 1, 2* k + 1, 2* k + 1};
    }

    @Override
    public int[][][] getMask3D()
    {
        return this.mask;
    }

    @Override
    public int[] getOffset()
    {
        int k = (int) Math.ceil(this.radius - 0.5);
        return new int[] {k, k, k};
    }

    @Override
    public int[][] getShifts3D()
    {
        return convertMaskToShifts(getMask3D());
    }

    @Override
    public Strel3D reverse()
    {
        return this;
    }

 }
