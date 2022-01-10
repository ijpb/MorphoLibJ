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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.label.select.LabelSizeFiltering;
import inra.ijpb.label.select.RelationalOperator;

/**
 * Filter labels within a label image using a size criterion.
 * 
 * @author David Legland
 *
 */
public class LabelSizeFilteringPlugin implements PlugIn
{
    /**
     * A pre-defined set of Labels for populating plugin.
     */
    public enum Operation
    {
    	/** The operator for "greater than" */
        GT("Greater_Than", RelationalOperator.GT),
    	/** The operator for "lower than" */
        LT("Lower_Than", RelationalOperator.LT),
    	/** The operator for "greater than or equal" */
        GE("Greater_Than_Or_Equal", RelationalOperator.GE),
    	/** The operator for "greater than or equal" */
        LE("Lower_Than_Or_Equal", RelationalOperator.LE),
    	/** The operator for "equal" */
        EQ("Equal", RelationalOperator.EQ),
        /** The operator for "not equal" */
        NE("Not_Equal", RelationalOperator.NE);
        
        /** The label to display in plugin. */
        private final String label;
        
        /** Encapsulates the operator */
        private final RelationalOperator operator;
        
        private Operation(String label, RelationalOperator operator) 
        {
            this.label = label;
            this.operator = operator;
        }
        
        /**
		 * Applies the operator wrapped by this enumeration item to the input
		 * image.
		 * 
		 * @param image
		 *            the image to process
		 * @param sizeLimit
		 *            the scalar value used for filtering
		 * @return the result of the binary operator applied on image
		 */
        public ImagePlus applyTo(ImagePlus image, int sizeLimit) 
        {
            LabelSizeFiltering algo = new LabelSizeFiltering(this.operator, sizeLimit);
            return algo.process(image);
        }
        
        public String toString() 
        {
            return this.label;
        }
        
        /**
    	 * Returns all the labels for this enumeration.
    	 * 
    	 * @return all the labels for this enumeration.
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
         *            the name of the operation
         * @return the operation corresponding to the name
         * @throws IllegalArgumentException
         *             if operation name is not recognized.
         */
        public static Operation fromLabel(String opLabel)
        {
            if (opLabel != null)
                opLabel = opLabel.toLowerCase();
            for (Operation op : Operation.values()) {
                String cmp = op.label.toLowerCase();
                if (cmp.equals(opLabel))
                    return op;
            }
            throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
        }
    };

	@Override
	public void run(String args) 
	{
		ImagePlus imagePlus = IJ.getImage();
        
		// determine image dimensionality
		boolean isPlanar = imagePlus.getStackSize() == 1; 
        
		// create the dialog, with operator options
		String title = "Label Size Filtering";
        GenericDialog gd = new GenericDialog(title);
        gd.addChoice("Operation", 
                Operation.getAllLabels(),
                Operation.GT.label);
        String label = isPlanar ? "Size Limit (pixels):" : "Size Limit (voxels)";
        gd.addNumericField(label, 100, 0);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;
        
        Operation op = Operation.fromLabel(gd.getNextChoice());
        int sizeLimit = (int) gd.getNextNumber();
        
        // Apply size filtering on label image
        ImagePlus resultPlus = op.applyTo(imagePlus, sizeLimit);

        // copy settings
        resultPlus.copyScale(imagePlus);
        resultPlus.setDisplayRange(imagePlus.getDisplayRangeMin(), imagePlus.getDisplayRangeMax());
        resultPlus.setLut( imagePlus.getProcessor().getLut() );

        // Display image, and choose same slice as original
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) 
		{
            resultPlus.setZ(imagePlus.getZ());
            resultPlus.setSlice(imagePlus.getCurrentSlice());
		}
	}
}
