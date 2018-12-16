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


import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.geometry.Vector3D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region3d.InertiaEllipsoid;
import inra.ijpb.measure.region3d.InertiaEllipsoid.InertiaMoments3D;

public class InertiaEllipsoidPlugin implements PlugIn
{
    // ====================================================
    // Class variables
    
	ImagePlus imagePlus;
	
    
    // ====================================================
    // Calling functions 
   

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String args)
    {
        ImagePlus imagePlus = IJ.getImage();
    
		if (imagePlus.getStackSize() == 1) 
		{
			IJ.error("Requires a Stack");
			return;
		}

		// Extract required information
		ImageStack image = imagePlus.getStack();
		int[] labels = LabelImages.findAllLabels(imagePlus);
		Calibration calib= imagePlus.getCalibration();
		
		// Compute inertia moments
        ResultsTable table;
        ResultsTable vectorTable;
        try 
        {
        	InertiaEllipsoid algo = new InertiaEllipsoid();
        	DefaultAlgoListener.monitor(algo);
        	table = algo.computeTable(imagePlus);
            // show table results
            String title = imagePlus.getShortTitle() + "-ellipsoid";
            table.show(title);
        	
            InertiaMoments3D[] moments = algo.computeMoments(image, labels, calib);
        	vectorTable = createVectorTable(labels, moments);
            title = imagePlus.getShortTitle() + "-eigenVectors";
            vectorTable.show(title);
        } 
        catch (Exception ex) 
        {
        	String msg = ex.getMessage();
        	IJ.log(msg);
			IJ.error("Problem occured during Inertia Ellipsoid computation:\n" + msg);
        	ex.printStackTrace(System.err);
        	return;
        }

    }
    
    private ResultsTable createVectorTable(int[] labels, InertiaMoments3D[] moments)
    {
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		for (int i = 0; i < labels.length; i++)
		{
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(labels[i]));

			ArrayList<Vector3D> vectors = moments[i].eigenVectors();
			
			Vector3D v1 = vectors.get(0);
			table.addValue("EigenVector1.X", v1.getX());
			table.addValue("EigenVector1.Y", v1.getY());
			table.addValue("EigenVector1.Z", v1.getZ());
			
			Vector3D v2 = vectors.get(1);
			table.addValue("EigenVector2.X", v2.getX());
			table.addValue("EigenVector2.Y", v2.getY());
			table.addValue("EigenVector2.Z", v2.getZ());
			
			Vector3D v3 = vectors.get(2);
			table.addValue("EigenVector3.X", v3.getX());
			table.addValue("EigenVector3.Y", v3.getY());
			table.addValue("EigenVector3.Z", v3.getZ());

		}
		
    	return table;
    }
}
