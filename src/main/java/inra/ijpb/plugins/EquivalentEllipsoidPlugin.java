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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.geometry.Ellipsoid;
import inra.ijpb.label.edit.FindAllLabels;
import inra.ijpb.measure.region3d.EquivalentEllipsoid;
import inra.ijpb.measure.region3d.EquivalentEllipsoid.Moments3D;
import inra.ijpb.util.IJUtils;

/**
 * Computes equivalent ellipsoid for each region with a binary or label 3D
 * image. The spatial calibration is taken into account.
 * 
 * @author dlegland
 *
 */
public class EquivalentEllipsoidPlugin implements PlugIn
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
		Calibration calib= imagePlus.getCalibration();
		long t0 = System.nanoTime();
		
		// identifies labels within image
		IJ.showStatus("Find Labels");
        FindAllLabels findLabels = new FindAllLabels();
        DefaultAlgoListener.monitor(findLabels);
        int[] labels = findLabels.process(image);

		// Compute inertia moments
        ResultsTable table;
        ResultsTable vectorTable;
        try 
        {
            // create algo instance
        	EquivalentEllipsoid algo = new EquivalentEllipsoid();
        	DefaultAlgoListener.monitor(algo);
        	
        	// compute results
            IJ.showStatus("Compute 3D moments");
            Moments3D[] moments = algo.computeMoments(image, labels, calib);
            IJ.showStatus("Convert moments to ellipsoids");
        	Ellipsoid[] ellipsoids = algo.momentsToEllipsoids(moments);
            
            // show results as ImageJ Table
            IJ.showStatus("Create table");
            table = algo.createTable(labels, ellipsoids);
            String title = imagePlus.getShortTitle() + "-ellipsoids";
            table.show(title);
        	
            // also create a table for moments
        	vectorTable = algo.createTable(labels, moments);
            title = imagePlus.getShortTitle() + "-eigenVectors";
            vectorTable.show(title);
        } 
        catch (Exception ex) 
        {
        	String msg = ex.getMessage();
        	IJ.log(msg);
			IJ.error("Problem occured during Equivalent Ellipsoid computation:\n" + msg);
        	ex.printStackTrace(System.err);
        	return;
        }
        
        long t1 = System.nanoTime();
        double dt = (t1 - t0) / 1_000_000;
        IJUtils.showElapsedTime("Equivalent Ellipsoids", dt, imagePlus);
    }
}
