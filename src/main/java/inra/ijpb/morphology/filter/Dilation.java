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
package inra.ijpb.morphology.filter;

import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;

/**
 * Performs morphological dilation on 2D/3D images.
 * 
 * Dilation is obtained by extracting the maximum value among pixels/voxels
 * within the neighborhood given by the structuring element.
 * 
 * @see Erosion
 * @see Opening
 * @see Closing
 * @see Strel#dilation(ImageProcessor)
 */
public class Dilation extends MorphologicalFilter
{
    /**
     * Creates a new Morphological Dilation operator with the specified
     * structuring element.
     * 
     * @param strel
     *            the structuring element used for the operation, that can also
     *            be an instance of Strel.
     */
    public Dilation(Strel3D strel)
    {
        super(strel, "-dilation");
    }
    
    @Override
    public ImageProcessor process(ImageProcessor image)
    {
        // check-up strel dimensionality
        if (!(strel instanceof Strel))
        {
            throw new RuntimeException("Processing 2D image requires a 2D strel");
        }
        
        // check case of color images
        if (image instanceof ColorProcessor)
        {
            return this.processColor((ColorProcessor) image);
        }

        ImageProcessor result = ((Strel) strel).dilation(image);
        result.setColorModel(image.getColorModel());
        return result;
    }
    
    @Override
    public ImageStack process(ImageStack image)
    {
        return strel.dilation(image);
    }
}
