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
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region3d.LargestInscribedBall;

public class MaxInscribedSpherePlugin implements PlugIn 
{
    // ====================================================
    // Global Constants

    // ====================================================
    // Class variables
    
    // ====================================================
    // Calling functions 
    
	@Override
	public void run(String arg0) 
	{
		// Open a dialog to choose:
		// - a label image
		// - a set of weights
		int[] indices = WindowManager.getIDList();
		if (indices==null)
		{
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// create the list of image names
		String[] imageNames = new String[indices.length];
		for (int i=0; i<indices.length; i++)
		{
			imageNames[i] = WindowManager.getImage(indices[i]).getTitle();
		}
		
		// name of selected image
		String selectedImageName = IJ.getImage().getTitle();

		// create the dialog
		GenericDialog gd = new GenericDialog("Max. Inscribed Sphere");
		gd.addChoice("Label Image:", imageNames, selectedImageName);
		// Set Borgefors weights as default
//		gd.addChoice("Distances", ChamferWeights3D.getAllLabels(), 
//				ChamferWeights3D.BORGEFORS.toString());
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelImage = WindowManager.getImage(labelImageIndex+1);
//		ChamferWeights3D weights = ChamferWeights3D.fromLabel(gd.getNextChoice());
		
		// check if image is a 3D label image
		if (labelImage.getStackSize() <= 1) 
		{
            IJ.showMessage("Input image should be a 3D label image");
            return;
		}
		
		// Check if image may be a label image
		if (!LabelImages.isLabelImageType(labelImage))
		{
            IJ.showMessage("Input image should be a 3D label image");
            return;
        }
        
		// Execute the plugin
		LargestInscribedBall algo = new LargestInscribedBall();
		DefaultAlgoListener.monitor(algo);
		ResultsTable table = algo.computeTable(labelImage);
        
        // Display plugin result
		String tableName = labelImage.getShortTitle() + "-MaxInscribedSphere"; 
		table.show(tableName);
	}
}
