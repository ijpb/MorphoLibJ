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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.label.filter.ChamferLabelDilation2DShort;
import inra.ijpb.label.filter.ChamferLabelDilation3DShort;
import inra.ijpb.label.filter.ChamferLabelErosion2DShort;
import inra.ijpb.label.filter.ChamferLabelErosion3DShort;
import inra.ijpb.util.IJUtils;

/**
 * Applies morphological filtering on a label image.
 * 
 * Supported operations are erosion, dilation, opening and closing.
 * 
 * Note that the algorithm is different that for grayscale images. Instead of
 * using a structuring element, the a chamfer mask is used, allowing to choose
 * between closest labels for dilation.
 */
public class LabelMapMorphologicalFilteringPlugin implements PlugIn
{
    /**
     * A pre-defined set of basis morphological operations, that can be easily 
     * used with a GenericDialog. 
     * Example:
     * <pre><code>
     * // Use a generic dialog to define an operator 
     * GenericDialog gd = new GenericDialog();
     * gd.addChoice("Operation", Operation.getAllLabels();
     * gd.showDialog();
     * Operation op = Operation.fromLabel(gd.getNextChoice());
     * // Apply the operation on the current image
     * ImageStack image = IJ.getImage().getStack();
     * ImageStack res = op.process(image, ChamferMask3D.BORGEFORS, 2.0);
     * </code></pre>
     */
    public enum Operation 
    {
        /** Morphological erosion (local minima)*/
        EROSION("Erosion"),
        /** Morphological dilation (local maxima)*/
        DILATION("Dilation"),
        /** Morphological opening (erosion followed by dilation)*/
        OPENING("Opening"),
        /** Morphological closing (dilation followed by erosion)*/
        CLOSING("Closing");
        
        private final String label;
        
        private Operation(String label) 
        {
            this.label = label;
        }
        
        /**
         * Applies the current operator to the input image.
         * 
         * @param image
         *            the image to process
         * @param mask
         *            the chamfer mask used for propagating distances
         * @param radius
         *            the radius used to threshold the distance map
         * @return the result of morphological operation applied to image
         */
        public ImageProcessor process(ImageProcessor image, ChamferMask2D mask, double radius) 
        {
            if (this == DILATION)
            {
                ChamferLabelDilation2DShort dilation = new ChamferLabelDilation2DShort(mask, radius);
                return dilation.process(image);
            }
            if (this == EROSION)
            {
                ChamferLabelErosion2DShort erosion = new ChamferLabelErosion2DShort(mask, radius);
                return erosion.process(image);
            }
            if (this == CLOSING)
            {
                ChamferLabelDilation2DShort dilation = new ChamferLabelDilation2DShort(mask, radius);
                ChamferLabelErosion2DShort erosion = new ChamferLabelErosion2DShort(mask, radius);
                return erosion.process(dilation.process(image));
            }
            if (this == OPENING)
            {
                ChamferLabelErosion2DShort erosion = new ChamferLabelErosion2DShort(mask, radius);
                ChamferLabelDilation2DShort dilation = new ChamferLabelDilation2DShort(mask, radius);
                return dilation.process(erosion.process(image));
            }
            
            throw new RuntimeException(
                    "Unable to process the " + this + " morphological operation");
        }
        
        /**
         * Applies the current operator to the input 3D image.
         * 
         * @param image
         *            the image to process
         * @param mask
         *            the chamfer mask used for propagating distances
         * @param radius
         *            the radius used to threshold the distance map
         * @return the result of morphological operation applied to image
         */
        public ImageStack process(ImageStack image, ChamferMask3D mask, double radius)
        {
            if (this == DILATION)
            {
                ChamferLabelDilation3DShort dilation = new ChamferLabelDilation3DShort(mask, radius);
                return dilation.process(image);
            }
            if (this == EROSION)
            {
                ChamferLabelErosion3DShort erosion = new ChamferLabelErosion3DShort(mask, radius);
                return erosion.process(image);
            }
            if (this == CLOSING)
            {
                ChamferLabelDilation3DShort dilation = new ChamferLabelDilation3DShort(mask, radius);
                ChamferLabelErosion3DShort erosion = new ChamferLabelErosion3DShort(mask, radius);
                return erosion.process(dilation.process(image));
            }
            if (this == OPENING)
            {
                ChamferLabelDilation3DShort dilation = new ChamferLabelDilation3DShort(mask, radius);
                ChamferLabelErosion3DShort erosion = new ChamferLabelErosion3DShort(mask, radius);
                return dilation.process(erosion.process(image));
            }
            
            throw new RuntimeException(
                    "Unable to process the " + this + " morphological operation");
        }
        
        public String toString() 
        {
            return this.label;
        }
        
        /**
         * Returns the list of labels for this enumeration.
         * 
         * @return the list of labels for this enumeration.
         */
        public static String[] getAllLabels()
        {
            int n = Operation.values().length;
            String[] result = new String[n];
            
            int i = 0;
            for (Operation op : Operation.values())
                result[i++] = op.label;
            
            return result;
        }
        
        /**
         * Determines the operation type from its label.
         * 
         * @param opLabel
         *            the label of the operation
         * @return the parsed Operation
         * @throws IllegalArgumentException
         *             if label is not recognized.
         */
        public static Operation fromLabel(String opLabel)
        {
            if (opLabel != null)
                opLabel = opLabel.toLowerCase();
            for (Operation op : Operation.values()) 
            {
                String cmp = op.label.toLowerCase();
                if (cmp.equals(opLabel))
                    return op;
            }
            throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
        }
    };
    
    /**
     * The filtering operation to apply. 
     */
    Operation op = Operation.DILATION;
    
    /**
     * The radius defining the circular neighborhood around each pixel or voxel. Default is 2.0.
     */
    double radius = 2.0;

	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();
		
		// open a dialog to choose options
        GenericDialog gd = new GenericDialog("Label Morphological Filter");
        gd.addChoice("Operation", Operation.getAllLabels(), this.op.toString());
        gd.addNumericField("Radius", this.radius, 1);
        
        // If cancel was clicked, do nothing
        gd.showDialog();
        if (gd.wasCanceled())
            return;

        // parse user options
        Operation op = Operation.fromLabel(gd.getNextChoice());
        double radius = gd.getNextNumber();
        this.op = op;
        this.radius = radius;

        String newName = imagePlus.getShortTitle() + "-" + op.toString();
		ImagePlus resultPlus;

		// apply operator on current image
        long t0 = System.currentTimeMillis();
		if (imagePlus.getStackSize() == 1)
		{
			// Process 2D image
		    ImageProcessor image = imagePlus.getProcessor();
		    
		    ChamferMask2D mask = ChamferMask2D.CHESSKNIGHT;	    
		    ImageProcessor result = op.process(image, mask, radius);
            
			result.setMinAndMax(image.getMin(), image.getMax());
            result.setColorModel(image.getColorModel());
			resultPlus = new ImagePlus(newName, result);
		} 
		else 
		{
			// Process 3D image
			ImageStack image = imagePlus.getStack();
			ChamferMask3D mask = ChamferMask3D.SVENSSON_3_4_5_7;
						
            ImageStack result = op.process(image, mask, radius);
            
			result.setColorModel(image.getColorModel());
			resultPlus = new ImagePlus(newName, result);
			
	        // update display range
	    	double min = imagePlus.getDisplayRangeMin();
	    	double max = imagePlus.getDisplayRangeMax();
	    	resultPlus.setDisplayRange(min, max);
		}
		long elapsedTime = System.currentTimeMillis() - t0;
		
    	// display result
		resultPlus.show();
		resultPlus.copyScale(imagePlus);
		
		if (imagePlus.getStackSize() > 1) 
		{
			resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
		
		// display elapsed time
		IJUtils.showElapsedTime("Dilate Labels", elapsedTime, imagePlus);
	}
}
