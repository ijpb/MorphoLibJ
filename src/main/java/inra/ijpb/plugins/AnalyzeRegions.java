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


import java.awt.geom.Point2D;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.geometry.Circle2D;
import inra.ijpb.geometry.Ellipse;
import inra.ijpb.geometry.PointPair2D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.Area;
import inra.ijpb.measure.region2d.CroftonPerimeter;
import inra.ijpb.measure.region2d.GeodesicDiameter;
import inra.ijpb.measure.region2d.InertiaEllipse;
import inra.ijpb.measure.region2d.LargestInscribedCircle;
import inra.ijpb.measure.region2d.MaxFeretDiameter;

public class AnalyzeRegions implements PlugInFilter 
{
    // ====================================================
    // Global Constants
    
	boolean computeArea = true;
	boolean computePerimeter = true;
	boolean computeInertiaEllipse = true;
	boolean computeEllipseElongation = true;
	boolean computeMaxFeretDiameter = true;
	boolean computeGeodesicDiameter = true;
	boolean computeTortuosity = true;
	boolean computeMaxInscribedDisc = true;
	boolean computeGeodesicElongation = true;
	

    // ====================================================
    // Class variables
    
   /**
     * When this options is set to true, information messages are displayed on
     * the console, and the number of counts for each direction is included in
     * results table. 
     */
    public boolean debug  = false;
    
	ImagePlus imagePlus;
	
    
    // ====================================================
    // Calling functions 
    
	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	@Override
	public int setup(String arg, ImagePlus imp)
	{
		if (imp == null)
		{
			IJ.noImage();
			return DONE;
		}

		this.imagePlus = imp;
		return DOES_ALL | NO_CHANGES;
	}

	/* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(ImageProcessor ip) {
        
        // check if image is a label image
		// Check if image may be a label image
		if (!LabelImages.isLabelImageType(imagePlus))
		{
           IJ.showMessage("Input image should be a label image");
            return;
        }

        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Particles Analysis 3D");
        gd.addCheckbox("Area", true);
        gd.addCheckbox("Perimeter", true);
        gd.addCheckbox("Inertia Ellipse", true);
        gd.addCheckbox("Ellipse Elong.", true);
        gd.addCheckbox("Max. Feret Diameter", true);
        gd.addCheckbox("Geodesic Diameter", true);
        gd.addCheckbox("Tortuosity", true);
        gd.addCheckbox("Max. Inscribed Disc", true);
        gd.addCheckbox("Geodesic Elong.", true);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        // Extract features to extract from image
        computeArea               = gd.getNextBoolean();
        computePerimeter          = gd.getNextBoolean();
        computeInertiaEllipse     = gd.getNextBoolean();
        computeEllipseElongation  = gd.getNextBoolean();
        computeMaxFeretDiameter   = gd.getNextBoolean();
        computeGeodesicDiameter   = gd.getNextBoolean();
        computeTortuosity         = gd.getNextBoolean();
        computeMaxInscribedDisc   = gd.getNextBoolean();
        computeGeodesicElongation = gd.getNextBoolean();
        
        
        // Execute the plugin
        ResultsTable table = process(imagePlus);
        
        // show result
		String tableName = imagePlus.getShortTitle() + "-Morphometry"; 
		table.show(tableName);
    }
    
    public ResultsTable process(ImagePlus imagePlus)
    {
    	ImageProcessor image = imagePlus.getProcessor();
        // Extract spatial calibration
        Calibration calib = imagePlus.getCalibration();

		int[] labels = LabelImages.findAllLabels(image);
		int nLabels = labels.length;

		// create results table with appropriate labels
    	ResultsTable table = new ResultsTable();
    	for (int i = 0; i < nLabels; i++)
    	{
    		table.incrementCounter();
    		table.addLabel("" + labels[i]);
    	}
    	
    	// Parameters to be computed
    	PointPair2D[] maxFeretDiams = null;
    	GeodesicDiameter.Result[] geodDiams = null;
    	Circle2D[] inscrDiscs = null;
    	

    	// Compute parameters depending on the selected options
    	
    	if (computeArea)
    	{
    		Area algo = new Area();
    		DefaultAlgoListener.monitor(algo);

    		double[] areaList = algo.analyzeRegions(image, labels, calib);
    		addColumn(table, "Area", areaList);
    	}

    	if (computePerimeter)
    	{
    		CroftonPerimeter algo = new CroftonPerimeter();
    		DefaultAlgoListener.monitor(algo);
    		
    		double[] perimList = algo.analyzeRegions(image, labels, calib);
    		addColumn(table, "Perimeter", perimList);
    	}

    	if (computeInertiaEllipse)
    	{
    		InertiaEllipse algo = new InertiaEllipse();
    		DefaultAlgoListener.monitor(algo);
    		Map<Integer, Ellipse> ellipses = algo.analyzeRegions(imagePlus);
    		
    		addAllColumns(table, algo.createTable(ellipses));
    		
    		if (computeEllipseElongation)
    		{
    			double[] elong = new double[nLabels];
    			for (int i = 0; i < nLabels; i++)
    			{
    				Ellipse elli = ellipses.get(labels[i]);
    				elong[i] = elli.radius1() / elli.radius2();
    			}
    			addColumn(table, "Ellipse.Elong", elong);
    		}
    	}

    	if (computeMaxFeretDiameter || computeTortuosity)
    	{
    		MaxFeretDiameter algo = new MaxFeretDiameter(); 
    		DefaultAlgoListener.monitor(algo);
    		maxFeretDiams = algo.analyzeRegions(image, labels, calib);
    		
    	}

    	if (computeGeodesicDiameter || computeTortuosity)
    	{
    		GeodesicDiameter algo = new GeodesicDiameter();
    		DefaultAlgoListener.monitor(algo);
    		geodDiams = algo.analyzeRegions(image, labels, calib);
    		
    	}

    	if (computeMaxInscribedDisc || computeGeodesicElongation)
    	{
    		LargestInscribedCircle algo = new LargestInscribedCircle();
    		DefaultAlgoListener.monitor(algo);
    		inscrDiscs = algo.analyzeRegions(image, labels, calib);
    	}

    	
    	// Fill results table

    	if (computeMaxFeretDiameter)
		{
			for (int i = 0; i < nLabels; i++)
			{
				table.setValue("MaxFeretDiam", i, maxFeretDiams[i].diameter());
				table.setValue("MaxFeretDiamAngle", i, Math.toDegrees(maxFeretDiams[i].angle()));
			}
		}
		
		if (computeGeodesicDiameter)
		{
			for (int i = 0; i < nLabels; i++)
			{
				GeodesicDiameter.Result result = geodDiams[i];
				table.setValue("GeodesicDiameter", i, result.diameter);
			}
		}

		if (computeTortuosity)
    	{
    		double[] tortuosity = new double[nLabels];
			for (int i = 0; i < nLabels; i++)
			{
				tortuosity[i] = geodDiams[i].diameter / maxFeretDiams[i].diameter();
			}
			addColumn(table, "Tortuosity", tortuosity);
    	}
    	
    	if (computeMaxInscribedDisc)
    	{
    		for (int i = 0; i < nLabels; i++)
			{
    			Point2D center = inscrDiscs[i].getCenter();
				table.setValue("InscrDisc.Center.X", i, center.getX());
				table.setValue("InscrDisc.Center.Y", i, center.getY());
				table.setValue("InscrDisc.Radius", i, inscrDiscs[i].getRadius());
			}
    	}

    	if (computeGeodesicElongation)
    	{
    		double[] elong = new double[nLabels];
			for (int i = 0; i < nLabels; i++)
			{
				elong[i] = geodDiams[i].diameter / (inscrDiscs[i].getRadius() * 2);
			}
			addColumn(table, "GeodesicElongation", elong);
    	}
    	    	
    	return table;
    }
    
    private void addColumn(ResultsTable table, String colName, double[] values)
    {
    	for (int i = 0; i < values.length; i++)
    	{
    		table.setValue(colName, i, values[i]);
    	}
    }

    private void addAllColumns(ResultsTable table, ResultsTable otherTable)
    {
    	for (int c = 0; c <= otherTable.getLastColumn(); c++)
    	{
    		String colName = otherTable.getColumnHeading(c);
        	for (int i = 0; i < otherTable.getCounter(); i++)
        	{
        		table.setValue(colName, i, otherTable.getValue(colName, i));
        	}
    	}
    }
}
