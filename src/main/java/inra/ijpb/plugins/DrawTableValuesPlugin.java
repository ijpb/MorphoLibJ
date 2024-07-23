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

import java.awt.AWTEvent;
import java.awt.Choice;
import java.awt.Frame;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.text.TextPanel;
import ij.text.TextWindow;
import inra.ijpb.util.IJUtils;

/**
 * Draw the values of a results table column into current image, using specified
 * columns for x and y positions.
 * 
 * @author dlegland
 *
 */
public class DrawTableValuesPlugin implements PlugIn, DialogListener 
{
    // Static fields for keeping results between successive calls to plugin

    static String selectedHeaderNameSave = null;
    static boolean calibratedPositionSave = false;
    static String xPosHeaderNameSave = null;
    static String yPosHeaderNameSave = null;
    static int xOffsetSave = -10;
    static int yOffsetSave = -10;
    static String valueHeaderNameSave = null;
    static String patternSave = "%5.2f";
    
    /**
     * The image to overlay, initialized as the current image when plugin is run
     */
	ImagePlus imagePlus;
	
	ResultsTable table = null;
		
	GenericDialog gd = null;
	
    String selectedHeaderName = selectedHeaderNameSave;
    boolean calibratedPosition = calibratedPositionSave;
    String xPosHeaderName = xPosHeaderNameSave;
    String yPosHeaderName = yPosHeaderNameSave;
    int xOffset = xOffsetSave;
    int yOffset = yOffsetSave;
    String valueHeaderName = valueHeaderNameSave;
    String pattern = patternSave;
    

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) 
	{
		// Work on current image, and exit if no one is open
		this.imagePlus = IJ.getImage();
	
		// Check that a table window is open
        if (IJUtils.getTableWindows().length == 0)
        {
            IJ.error("Requires at least one Table window");
            return;
        }
  		
		// Opens dialog to choose options
		createDialog();
		this.gd.showDialog();
		
		// parse dialog
		if (gd.wasCanceled())
			return;
		
		parseDialogOptions();
		saveDialogOptions();
		
		drawValues(this.imagePlus);
	}
	
	private GenericDialog createDialog()
	{
		// Get the list of windows containing tables
		TextWindow[] textWindows = IJUtils.getTableWindows();
		String[] tableNames = IJUtils.getWindowNames(textWindows);
		
		// Choose current table
		TextPanel tp = textWindows[0].getTextPanel();
		ResultsTable table = tp.getResultsTable();
		this.table = table;
		
		// Choose current heading
		String[] headings = table.getHeadings();
		String defaultHeading = headings[0];
		if (defaultHeading.equals("Label") && headings.length > 1)
		{
			defaultHeading = headings[1];
		}

		this.gd = new GenericDialog("Draw Text from Column");
		gd.addChoice("Results Table:", tableNames, tableNames[0]);
        gd.addCheckbox("Calibrated Position:", this.calibratedPosition);
        gd.addChoice("X-Position:", headings, defaultHeading);
        gd.addChoice("Y-Position:", headings, defaultHeading);
        gd.addNumericField("X-Offset:", this.xOffset, 0, 5, "pixels");
        gd.addNumericField("Y-Offset:", this.yOffset, 0, 5, "pixels");
        gd.addChoice("Values:", headings, defaultHeading);
        gd.addStringField("Pattern:", this.pattern, 10);
        
        @SuppressWarnings("unchecked")
        Vector<Choice> choices = gd.getChoices();
        replaceStrings(choices.get(1), headings, chooseDefaultHeading(headings, xPosHeaderName));
        replaceStrings(choices.get(2), headings, chooseDefaultHeading(headings, yPosHeaderName));
        replaceStrings(choices.get(3), headings, chooseDefaultHeading(headings, valueHeaderName));

        gd.addDialogListener(this);
		
		return gd;
	}

	/**
	 * Parses dialog options, and setup inner fields of the class.
	 */
	private void parseDialogOptions() 
	{
	    // select the result table from its name
		String tableName = this.gd.getNextChoice();
		Frame tableFrame = WindowManager.getFrame(tableName);
		this.table = ((TextWindow) tableFrame).getTextPanel().getResultsTable();

        this.calibratedPosition = this.gd.getNextBoolean();
        this.xPosHeaderName = this.gd.getNextChoice();
        this.yPosHeaderName = this.gd.getNextChoice();
        this.xOffset = (int) this.gd.getNextNumber();
        this.yOffset = (int) this.gd.getNextNumber();
        this.valueHeaderName = this.gd.getNextChoice();
        this.pattern = this.gd.getNextString();
	}
	
	private void saveDialogOptions()
	{
	    calibratedPositionSave = this.calibratedPosition;
        xPosHeaderNameSave = this.xPosHeaderName;
        yPosHeaderNameSave = this.yPosHeaderName;
        xOffsetSave = xOffset;
        yOffsetSave = yOffset;
        valueHeaderNameSave = valueHeaderName;
        patternSave = pattern;
	}
	
	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt) 
	{
		if (gd.wasCanceled() || gd.wasOKed()) 
		{
			return true;
		}
		
		@SuppressWarnings({ "unchecked" })
        Vector<Choice> choices = gd.getChoices();
		if (choices == null) 
		{
			IJ.log("empty choices array...");
			return false;
		}
		
		// Change of the data table
        if (evt.getSource() == choices.get(0)) 
		{
			String tableName = ((Choice) evt.getSource()).getSelectedItem();
			Frame tableFrame = WindowManager.getFrame(tableName);
			this.table = ((TextWindow) tableFrame).getTextPanel().getResultsTable();
			
			// Choose current headings
			String[] headings = this.table.getHeadings();		
			replaceStrings(choices.get(1), headings, chooseDefaultHeading(headings, xPosHeaderName));
            replaceStrings(choices.get(2), headings, chooseDefaultHeading(headings, yPosHeaderName));
            replaceStrings(choices.get(3), headings, chooseDefaultHeading(headings, valueHeaderName));
		}
		
		return true;
	}
	
	private String chooseDefaultHeading(String[] headings, String proposedHeading)
	{
	    if (containsString(headings, proposedHeading))
	    {
	        return proposedHeading;  
	    }
	    else
        {
            if (headings[0].equals("Label") && headings.length > 1)
            {
                return headings[1];
            }
            else
            {
                return headings[0];
            }
        }
    }
	
	private boolean containsString(String[] strings, String string)
	{
	    if (string == null)
	        return false;
	    for (String str : strings)
	    {
	        if (string.equals(str))
	            return true;
	    }
	    return false;
	}
	
	private void replaceStrings(Choice choice, String[] strings, String defaultString)
	{
	    choice.removeAll();
        for (String str : strings) 
        {
            choice.add(str);
        }
        choice.select(defaultString);
	}
	
	/**
	 * Draw the values onto the target image.
	 * 
	 * @param target
	 *            the target image.
	 */
	public void drawValues(ImagePlus target)
	{
	    // coordinates of labels
        double[] xPos = getColumnValues(this.table, this.xPosHeaderName);
        double[] yPos = getColumnValues(this.table, this.yPosHeaderName);
        
        // convert from (optionally calibrated) coordinates to pixel coordinates
        if (this.calibratedPosition)
        {
            Calibration calib = target.getCalibration();
            for (int i = 0; i < xPos.length; i++)
            {
                xPos[i] = (xPos[i] - calib.xOrigin) / calib.pixelWidth;
                yPos[i] = (yPos[i] - calib.yOrigin) / calib.pixelHeight;
            }
        }
            
        // retrieve values to display
        double[] values = getColumnValues(this.table, this.valueHeaderName);
        
        // update overlay by creating one ROI for each label 
        Overlay overlay = new Overlay();
        for (int i = 0; i < xPos.length; i++)
        {
            String text = String.format(this.pattern,  values[i]);
            Roi roi = new TextRoi(
                    xPos[i] + this.xOffset,
                    yPos[i] + this.yOffset,
                    text);
            overlay.add(roi);
        }

	    target.setOverlay(overlay);
	}
	    

    private double[] getColumnValues(ResultsTable table, String colName)
    {
        String[] allHeaders = table.getHeadings();

        // Check if column header corresponds to row label header
        boolean hasRowLabels = hasRowLabelColumn(table);
        if (hasRowLabels && colName.equals(allHeaders[0]))
        {
            // need to parse row label column
            int nr = table.size();
            double[] values = new double[nr];
            for (int r = 0; r < nr; r++)
            {
                String label = table.getLabel(r);
                values[r] = Double.parseDouble(label);
            }
            return values;
        }

        // determine index of column
        int index = table.getColumnIndex(colName);
        if (index == ResultsTable.COLUMN_NOT_FOUND)
        {
            throw new RuntimeException("Unable to find column index from header: " + colName);
        }
        return table.getColumnAsDoubles(index);
    }
    
    private static final boolean hasRowLabelColumn(ResultsTable table)
    {
        return table.getLastColumn() == (table.getHeadings().length-2);
    }
}
