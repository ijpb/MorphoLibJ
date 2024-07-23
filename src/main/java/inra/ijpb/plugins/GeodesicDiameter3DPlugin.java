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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.distmap.ChamferMasks3D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region3d.GeodesicDiameter3D;
import inra.ijpb.util.IJUtils;


/**
 * Plugin for computing geodesic distances of 3D particles from label images
 * using chamfer weights.
 * 
 * @author dlegland
 *
 */
public class GeodesicDiameter3DPlugin implements PlugIn
{
	// ====================================================
	// Calling functions 
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
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
		GenericDialog gd = new GenericDialog("Geodesic Diameter 3D");
		gd.addChoice("Label Image (3D):", imageNames, selectedImageName);
		// Set Chessknight weights as default
		gd.addChoice("Distances", ChamferMasks3D.getAllLabels(), 
				ChamferMasks3D.SVENSSON_3_4_5_7.toString());
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// set up current parameters
		int labelImageIndex = gd.getNextChoiceIndex();
		ImagePlus labelPlus = WindowManager.getImage(labelImageIndex+1);
		ChamferMask3D chamferMask = ChamferMasks3D.fromLabel(gd.getNextChoice()).getMask();
		
		// check if image is a label image
		if (!LabelImages.isLabelImageType(labelPlus))
		{
			IJ.showMessage("Input image should be a label image");
			return;
		}
		
		// Compute geodesic diameters, using floating-point calculations
		long start = System.nanoTime();
		GeodesicDiameter3D algo = new GeodesicDiameter3D(chamferMask);
		DefaultAlgoListener.monitor(algo);
		ResultsTable table = algo.computeTable(labelPlus);
		long finalTime = System.nanoTime();
		
		// Final time, displayed in milliseconds
		float elapsedTime = (finalTime - start) / 1000000.0f;

		// display the result table
		String tableName = labelPlus.getShortTitle() + "-GeodDiameters"; 
		table.show(tableName);
	
		IJUtils.showElapsedTime("Geodesic Diameter 3D", (long) elapsedTime, labelPlus);
		
		
		// extract column corresponding to geodesic diameter
		int gdIndex = table.getColumnIndex("GeodesicDiameter");
		double[] geodDiamArray = table.getColumnAsDoubles(gdIndex);
		
		// Check validity of resulting geodesic diameters
		for (double geodDiam : geodDiamArray)
		{
			if (Float.isInfinite((float) geodDiam))
			{
				IJ.showMessage("Geodesic Diameter Warning", "Some geodesic diameters are infinite,\n"
						+ "meaning that some particles are not connected.\n" + "Maybe labeling was not performed?");
				break;
			}
		}
	}
}
