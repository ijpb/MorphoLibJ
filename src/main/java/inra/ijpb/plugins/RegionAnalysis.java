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
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.GeodesicDiameter;
import inra.ijpb.measure.GeometricMeasures2D;
import inra.ijpb.measure.MaxFeretDiameter;

public class RegionAnalysis implements PlugInFilter 
{
    // ====================================================
    // Global Constants
    
//    /**
//     * List of available numbers of directions
//     */
//    public final static String[] dirNumberLabels = {
//            "2 directions", 
//            "4 directions" 
//    }; 
//    
//    /**
//     *  Array of weights, in the same order than the array of names.
//     */
//    public final static int[] dirNumbers = {
//        2, 4
//    };
    
	boolean computeArea = true;
	boolean computePerimeter = true;
	boolean computeInertiaEllipse = true;
	boolean computeMaxFeretDiameter = true;
	boolean computeGeodesicDiameter = true;
	boolean computeMaxInscribedDisc = true;


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
        gd.addCheckbox("Max. Feret Diameter", true);
        gd.addCheckbox("Geodesic Diameter", true);
        gd.addCheckbox("Max. Inscribed Disc", true);
//        gd.addMessage("");
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        // Extract features to extract from image
        computeArea             = gd.getNextBoolean();
        computePerimeter        = gd.getNextBoolean();
        computeInertiaEllipse   = gd.getNextBoolean();
        computeMaxFeretDiameter = gd.getNextBoolean();
        computeGeodesicDiameter = gd.getNextBoolean();
        computeMaxInscribedDisc = gd.getNextBoolean();
        
        
        // Execute the plugin
        ResultsTable table = process(imagePlus);
        
        // show result
		String tableName = imagePlus.getShortTitle() + "-Morphometry"; 
		table.show(tableName);
    }
    
    public ResultsTable process(ImagePlus imagePlus)
    {
        // Extract spatial calibration
        Calibration calib = imagePlus.getCalibration();
        return process(imagePlus.getProcessor(), calib);
    }
    
    public ResultsTable process(ImageProcessor image, Calibration calib)
    {
		// extract particle labels
		int[] labels = LabelImages.findAllLabels(image);
		int nLabels = labels.length;

		// create array for resolution
		double[] resol = new double[] {calib.pixelWidth, calib.pixelHeight};
		
		// create results table with appropriate labels
    	ResultsTable table = new ResultsTable();
    	for (int i = 0; i < nLabels; i++)
    	{
    		table.incrementCounter();
    		table.addLabel("" + labels[i]);
    	}
    	
    	if (computeArea)
    	{
    		double[] areaList = GeometricMeasures2D.area(image, labels, resol);
    		addColumn(table, "Area", areaList);
    	}

    	if (computePerimeter)
    	{
    		double[] perimList = GeometricMeasures2D.croftonPerimeter(image, labels, resol, 4);
    		addColumn(table, "Perimeter", perimList);
    	}

    	if (computeInertiaEllipse)
    	{
    		ResultsTable ellipseTable = GeometricMeasures2D.inertiaEllipse(image);
    		addAllColumns(table, ellipseTable);
    	}

    	if (computeMaxFeretDiameter)
    	{
    		MaxFeretDiameter.PointPair[] pairs = new MaxFeretDiameter().process(image, labels, calib);
    		for (int i = 0; i < nLabels; i++)
        	{
        		table.setValue("MaxFeretDiam", i, pairs[i].diameter());
        		table.setValue("MaxFeretDiamAngle", i, Math.toDegrees(pairs[i].angle()));
        	}
    	}

    	if (computeGeodesicDiameter)
    	{
    		GeodesicDiameter.Result[] results = new GeodesicDiameter().process(image, labels);
    		for (int i = 0; i < nLabels; i++)
        	{
    			GeodesicDiameter.Result result = results[i].recalibrate(calib);
        		table.setValue("GeodesicDiam", i, result.diameter);
        	}
    	}

    	if (computeMaxInscribedDisc)
    	{
    		ResultsTable inscribedCircleTable = GeometricMeasures2D.maximumInscribedCircle(image, resol);
    		addAllColumns(table, inscribedCircleTable);
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
