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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;

/**
 * @author dlegland
 *
 */
public class MergeLabelsPlugin implements PlugIn
{
    // ====================================================
    // Enumeration of the different possibilities to manage gaps
    
    enum GapManagement
    {
        NO_GAP("No Gap"), 
        ORTHOGONAL("Orthogonal"), 
        DIAGONAL("Diagonal");
        
        String label;
        
        private GapManagement(String label)
        {
            this.label = label;
        }
        
        /**
         * Returns a set of labels for the gap management strategies.
         * 
         * @return a list of labels
         */
        public static String[] getAllLabels()
        {
            // array of all Strel types
            GapManagement[] values = GapManagement.values();
            int n = values.length;
            
            // keep all values but the last one ("Custom")
            String[] result = new String[n];
            for (int i = 0; i < n; i++)
                result[i] = values[i].label;
            
            return result;
        }
        
        /**
         * Determines the gap management strategy from its label.
         * 
         * @param label
         *            the name of the gap management strategy.
         * @return a new instance of GapManagement
         * @throws IllegalArgumentException
         *             if label is not recognized.
         */
        public static GapManagement fromLabel(String label)
        {
            if (label != null)
                label = label.toLowerCase();
            for (GapManagement type : GapManagement.values()) 
            {
                if (type.label.toLowerCase().equals(label))
                    return type;
            }
            throw new IllegalArgumentException("Unable to parse Strel.Shape with label: " + label);
        }

        /**
         * @return the label associated to this gap management strategy.
         */
        public String toString()
        {
            return this.label;
        }
    }
    
    // ====================================================
    // Calling functions 
    
    /* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    @Override
    public void run(String arg)
    {
        ImagePlus imagePlus = IJ.getImage();
        
        // create the dialog
        GenericDialog gd = new GenericDialog("Merge Labels");
        gd.addChoice("Gap Management", GapManagement.getAllLabels(), GapManagement.NO_GAP.toString());
        gd.showDialog();
        
        if (gd.wasCanceled())
            return;
        
        int gapManagementIndex = gd.getNextChoiceIndex();

        switch (gapManagementIndex)
        {
        case 0:
        {
            LabelImages.mergeLabels(imagePlus, imagePlus.getRoi(), true);
            break;
        }
        case 1:
        {
            int conn = imagePlus.isStack() ? 6 : 4;
            LabelImages.mergeLabelsWithGap(imagePlus, imagePlus.getRoi(), conn, true);
            break;
        }
        case 2:
        {
            int conn = imagePlus.isStack() ? 26 : 6;
            LabelImages.mergeLabelsWithGap(imagePlus, imagePlus.getRoi(), conn, true);
            break;
        }
        }
        
        imagePlus.updateAndDraw();
    }
    
}
