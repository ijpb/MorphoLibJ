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
import inra.ijpb.geometry.OrientedBox2D;
import inra.ijpb.geometry.PointPair2D;
import inra.ijpb.label.LabelImages;
//import inra.ijpb.measure.IntrinsicVolumes2D;
import inra.ijpb.measure.region2d.Convexity;
import inra.ijpb.measure.region2d.GeodesicDiameter;
import inra.ijpb.measure.region2d.InertiaEllipse;
import inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D;
import inra.ijpb.measure.region2d.LargestInscribedCircle;
import inra.ijpb.measure.region2d.MaxFeretDiameter;
import inra.ijpb.measure.region2d.OrientedBoundingBox2D;

public class AnalyzeRegions implements PlugInFilter 
{
    // ====================================================
    // Global Constants
    
	boolean computeArea = true;
	boolean computePerimeter = true;
	boolean computeCircularity = true;
	boolean computeEulerNumber = true;
	boolean computeInertiaEllipse = true;
	boolean computeEllipseElongation = true;
	boolean computeConvexity = true;
	boolean computeMaxFeretDiameter = true;
	boolean computeOrientedBox = true;
	boolean computeOrientedBoxElongation = true;
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
        gd.addCheckbox("Circularity", true);
        gd.addCheckbox("Euler Number", true);
        gd.addCheckbox("Inertia Ellipse", true);
        gd.addCheckbox("Ellipse Elong.", true);
        gd.addCheckbox("Convexity", true);
        gd.addCheckbox("Max. Feret Diameter", true);
        gd.addCheckbox("Oriented Box", true);
        gd.addCheckbox("Oriented Box Elong.", true);
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
        computeCircularity        = gd.getNextBoolean();
        computeEulerNumber        = gd.getNextBoolean();
        computeInertiaEllipse     = gd.getNextBoolean();
        computeEllipseElongation  = gd.getNextBoolean();
        computeConvexity          = gd.getNextBoolean();
        computeMaxFeretDiameter   = gd.getNextBoolean();
        computeOrientedBox   	  = gd.getNextBoolean();
        computeOrientedBoxElongation = gd.getNextBoolean();
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
//    	double[] areaList = null;
//    	double[] perimList = null;
//    	double[] eulerNumberList = null;
    	IntrinsicVolumesAnalyzer2D.Result[] intrinsicVolumes = null; 
    	Ellipse[] ellipses = null;
    	Convexity.Result[] convexities = null;
    	PointPair2D[] maxFeretDiams = null;
    	OrientedBox2D[] orientedBoxes = null;
    	GeodesicDiameter.Result[] geodDiams = null;
    	Circle2D[] inscrDiscs = null;
    	

//    	// Compute parameters depending on the selected options
//    	
//    	if (computeArea || computeCircularity)
//    	{
//    		IJ.showStatus("Compute area");
//    		areaList = IntrinsicVolumes2D.areas(image, labels, calib);
//    		addColumnToTable(table, "Area", areaList);
//    	}
//
//    	if (computePerimeter || computeCircularity)
//    	{
//    		IJ.showStatus("Compute perimeter");
//    		perimList = IntrinsicVolumes2D.perimeters(image, labels, calib, 4);
//    		addColumnToTable(table, "Perimeter", perimList);
//    	}
//
//    	if (computeEulerNumber)
//    	{
//    		IJ.showStatus("Compute Euler number");
//    		eulerNumberList = IntrinsicVolumes2D.eulerNumbers(image, labels, 4);
//    	}

    	// compute intrinsic volumes
    	if (computeArea || computePerimeter || computeEulerNumber || computeCircularity)
    	{
    	    IJ.showStatus("Intrinsic Volumes");
    	    
    	    // Create ans setup computation class
            IntrinsicVolumesAnalyzer2D algo = new IntrinsicVolumesAnalyzer2D();
            algo.setDirectionNumber(4);
            algo.setConnectivity(4);
            DefaultAlgoListener.monitor(algo);
            
            // run analysis
            intrinsicVolumes = algo.analyzeRegions(image, labels, calib); 
    	}
    	
    	if (computeInertiaEllipse || computeEllipseElongation)
    	{
    		IJ.showStatus("Inertia Ellipse");
    		InertiaEllipse algo = new InertiaEllipse();
    		DefaultAlgoListener.monitor(algo);
    		ellipses = algo.analyzeRegions(image, labels, calib);
    	}

    	if (computeConvexity)
    	{
    		IJ.showStatus("Compute convexity");
    		Convexity algo = new Convexity();
    		DefaultAlgoListener.monitor(algo);
    		convexities = algo.analyzeRegions(image, labels, calib);
    	}

    	if (computeMaxFeretDiameter || computeTortuosity)
    	{
    		IJ.showStatus("Max Feret Diameter");
    		maxFeretDiams = MaxFeretDiameter.maxFeretDiameters(image, labels, calib);
    	}

    	if (computeOrientedBox)
    	{
    		IJ.showStatus("Oriented Bounding Box");
    		OrientedBoundingBox2D algo = new OrientedBoundingBox2D();
    		DefaultAlgoListener.monitor(algo);
    		orientedBoxes = algo.analyzeRegions(image, labels, calib);
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
    	
        if (computeArea)
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("Area", i, intrinsicVolumes[i].area);
            }
        }
        
        if (computePerimeter)
        {
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("Perimeter", i, intrinsicVolumes[i].perimeter);
            }
        }
        
    	if (computeCircularity)
    	{
    		double[] circularities = computeCircularities(intrinsicVolumes);
    		addColumnToTable(table, "Circularity", circularities);
    	}

    	if (computeEulerNumber)
    	{
            for (int i = 0; i < nLabels; i++)
            {
                table.setValue("EulerNumber", i, intrinsicVolumes[i].eulerNumber);
            }
    	}

    	if (computeInertiaEllipse)
    	{
			for (int i = 0; i < nLabels; i++)
			{
				Ellipse elli = ellipses[i];
				Point2D center = elli.center();
				table.setValue("Ellipse.Center.X", i, center.getX());
				table.setValue("Ellipse.Center.Y", i, center.getY());
				table.setValue("Ellipse.Radius1", i, elli.radius1());
				table.setValue("Ellipse.Radius2", i, elli.radius2());
				table.setValue("Ellipse.Orientation", i, elli.orientation());
			}
    	}

		if (computeEllipseElongation)
		{
			double[] elong = new double[nLabels];
			for (int i = 0; i < nLabels; i++)
			{
				Ellipse elli = ellipses[i];
				elong[i] = elli.radius1() / elli.radius2();
			}
			addColumnToTable(table, "Ellipse.Elong", elong);
		}

		if (computeConvexity)
		{
			for (int i = 0; i < nLabels; i++)
			{
				table.setValue("ConvexArea", i, convexities[i].convexArea);
				table.setValue("Convexity", i, convexities[i].convexity);
			}
		}

		if (computeMaxFeretDiameter)
		{
			for (int i = 0; i < nLabels; i++)
			{
				table.setValue("MaxFeretDiam", i, maxFeretDiams[i].diameter());
				table.setValue("MaxFeretDiamAngle", i, Math.toDegrees(maxFeretDiams[i].angle()));
			}
		}
		
    	if (computeOrientedBox)
    	{
			for (int i = 0; i < nLabels; i++)
			{
				OrientedBox2D obox = orientedBoxes[i];
				Point2D center = obox.center();
				table.setValue("OBox.Center.X", i, center.getX());
				table.setValue("OBox.Center.Y", i, center.getY());
				table.setValue("OBox.Length", i, obox.length());
				table.setValue("OBox.Width", i, obox.width());
				table.setValue("OBox.Orientation", i, obox.orientation());
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
			addColumnToTable(table, "Tortuosity", tortuosity);
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
			addColumnToTable(table, "GeodesicElongation", elong);
    	}
    	    	
    	return table;
    }

//    private double[] computeCircularities(double[] areas, double[] perims)
//    {
//        int n = areas.length;
//        double[] circularities = new double[n];
//        for (int i = 0; i < n; i++)
//        {
//            circularities[i] = 4 * Math.PI * areas[i] / (perims[i] * perims[i]);
//        }
//        return circularities;
//    }
    
    private double[] computeCircularities(IntrinsicVolumesAnalyzer2D.Result[] morphos)
    {
        int n = morphos.length;
        double[] circularities = new double[n];
        for (int i = 0; i < n; i++)
        {
            double area = morphos[i].area;
            double perim = morphos[i].perimeter;
            circularities[i] = 4 * Math.PI * area / (perim * perim);
        }
        return circularities;
    }
    
    private static final void addColumnToTable(ResultsTable table, String colName, double[] values)
    {
    	for (int i = 0; i < values.length; i++)
    	{
    		table.setValue(colName, i, values[i]);
    	}
    }
}
