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
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.text.TextPanel;
import ij.text.TextWindow;
import inra.ijpb.label.LabelImages;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Combines a label image and the values within a column of a results table to
 * create a new image with intensity corresponding to the vales in the table
 * column.
 * 
 * @author dlegland
 *
 */
public class LabelToValuePlugin implements PlugIn, DialogListener 
{
	ImagePlus labelImagePlus;
	
	ImagePlus resultPlus;
	
	ResultsTable table = null;
	
	
	GenericDialog gd;
	
	String selectedHeaderName = null;
	
	double minValue;
	double maxValue;

	// number of digits after decimal mark to display min/max values
	int nDigits = 3;
		
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) 
	{
		// Work on current image, and exit if no one is open
		this.labelImagePlus = IJ.getImage();
	
		// Check that a table window is open
		TextWindow[] textWindows = getTableWindows();
        if (textWindows.length == 0)
        {
            IJ.error("Requires at least one Table window");
            return;
        }
        
		// Create empty result image
		initResultImage();
		
		// Opens dialog to choose options
		createDialog();
		this.gd.showDialog();
		
		// parse dialog
		if (gd.wasCanceled())
			return;
		parseDialogOptions();
		
		ImagePlus resultPlus = computeResultImage();
		if( null == resultPlus )
			return;
		
		this.resultPlus.copyScale(this.labelImagePlus);

		String newName = this.labelImagePlus.getShortTitle() + "-" + selectedHeaderName;
		resultPlus.setTitle(newName);
		resultPlus.show();
		
		// set up display 
		if (labelImagePlus.getStackSize() > 1) 
		{
			resultPlus.setSlice(labelImagePlus.getCurrentSlice());
		}
	}

	private void initResultImage() 
	{
		if (this.labelImagePlus.getStackSize() == 1) 
		{
			ImageProcessor labelImage = this.labelImagePlus.getProcessor();
			int sizeX = labelImage.getWidth(); 
			int sizeY = labelImage.getHeight(); 
			
			ImageProcessor resultImage = new FloatProcessor(sizeX, sizeY);
			this.resultPlus = new ImagePlus("Result", resultImage);
		} 
		else 
		{
			ImageStack labelImage = this.labelImagePlus.getStack();
			int sizeX = labelImage.getWidth(); 
			int sizeY = labelImage.getHeight(); 
			int sizeZ = labelImage.getSize(); 
			
			ImageStack resultImage = ImageStack.create(sizeX, sizeY, sizeZ, 32); 
			this.resultPlus = new ImagePlus("Result", resultImage);
		}
		
		this.resultPlus.copyScale(this.labelImagePlus);
	}
	
	private GenericDialog createDialog()
	{
		// Get the list of windows containing tables
		TextWindow[] textWindows = getTableWindows();
		if (textWindows.length == 0)
		{
			IJ.error("Requires at least one Table window");
			return null;
		}
		String[] tableNames = new String[textWindows.length];
		for (int i = 0; i < textWindows.length; i++) {
			tableNames[i] = textWindows[i].getTitle();
//			IJ.log("Found table: " + tableNames[i]);
		}
		
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

		double[] extent = computeColumnExtent(table, defaultHeading);

		this.gd = new GenericDialog("Colorize Labels");
		gd.addChoice("Results Table:", tableNames, tableNames[0]);
		gd.addChoice("Column:", headings, defaultHeading);
		gd.addNumericField("Min Value", extent[0], this.nDigits, 10, null);
		gd.addNumericField("Max Value", extent[1], this.nDigits, 10, null);
		gd.addDialogListener(this);
		
		return gd;
	}
	
	/**
	 * Iterates on the list of TextWindows, and keeps only the ones containing a
	 * non-null ResultsTable
	 */
	private static final TextWindow[] getTableWindows() 
	{
		Frame[] frames = WindowManager.getNonImageWindows();
		
		ArrayList<TextWindow> windows = new ArrayList<TextWindow>(frames.length);
		
		for (Frame frame : frames) 
		{
			if (frame instanceof TextWindow) 
			{
				TextWindow tw = (TextWindow) frame;
				if (tw.getTextPanel().getResultsTable() != null) 
				{
					windows.add(tw);
				}
			}
		}
		
		return windows.toArray(new TextWindow[0]);
	}

	/**
	 * analyse dialog, and setup inner fields of the class.
	 */
	private void parseDialogOptions() 
	{
		String tableName = this.gd.getNextChoice();
		Frame tableFrame = WindowManager.getFrame(tableName);
		this.table = ((TextWindow) tableFrame).getTextPanel().getResultsTable();
		
		this.selectedHeaderName = this.gd.getNextChoice();
		
		this.minValue = this.gd.getNextNumber();
		this.maxValue = this.gd.getNextNumber();
	}
	
	private ImagePlus computeResultImage() 
	{
		// extract array of numerical values
		double[] values = getColumnValues(table, this.selectedHeaderName);

		// Different processing depending on image dimensionality
		try 
		{
			if (this.resultPlus.getStackSize() == 1) 
			{
				ImageProcessor labelImage = this.labelImagePlus.getProcessor();
				ImageProcessor resultImage = LabelImages.applyLut(labelImage, values);
				this.resultPlus.setProcessor(resultImage);
			}
			else 
			{
				ImageStack labelImage = this.labelImagePlus.getStack();
				ImageStack resultImage = LabelImages.applyLut(labelImage, values);
				this.resultPlus.setStack(resultImage);
			}
		}
		catch (RuntimeException ex) 
		{
			IJ.error("Label to value error", 
					"ERROR: label image values do not \n" + "correspond with table values!");
			return null;
		}

		this.resultPlus.setDisplayRange(this.minValue, this.maxValue);
		return this.resultPlus;
	}
	
	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt) 
	{
		if (gd.wasCanceled() || gd.wasOKed()) 
		{
			return true;
		}
		
		@SuppressWarnings("rawtypes")
		Vector choices = gd.getChoices();
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
			
			// Choose current heading
			String[] headings = this.table.getHeadings();
			String defaultHeading = headings[0];
			if (defaultHeading.equals("Label") && headings.length > 1) 
			{
				defaultHeading = headings[1];
			}
			
			Choice headingChoice = (Choice) choices.get(1);
			headingChoice.removeAll();
			for (String heading : headings) 
			{
				headingChoice.add(heading);
			}
			headingChoice.select(defaultHeading);

			changeColumnHeader(defaultHeading);
		}
		
		// Change of the column heading
		if (evt.getSource() == choices.get(1)) 
		{
			String headerName = ((Choice) evt.getSource()).getSelectedItem();
            changeColumnHeader(headerName);
		}
		
		return true;
	}
	
    private void changeColumnHeader(String headerName) 
    {
        double[] extent = computeColumnExtent(this.table, headerName);
        this.minValue = extent[0];
        this.maxValue = extent[1];
        
        updateMinMaxFields(extent[0], extent[1]);
    }
    
	/**
	 * Updates the text of the editable fields with the values of min and max of
	 * the current column.
	 * 
	 * @param minValue
	 *            the new minimum value of current column
	 * @param maxValue
	 *            the new maximum value of current column
	 */
	private void updateMinMaxFields( double minValue, double maxValue) 
	{
		@SuppressWarnings("rawtypes")
		Vector numericFields = this.gd.getNumericFields();
		TextField tf;
		
		tf = (TextField) numericFields.get(0);
		tf.setText(IJ.d2s(minValue, this.nDigits));
		tf = (TextField) numericFields.get(1);
		tf.setText(IJ.d2s(maxValue, this.nDigits));
	}

    /**
     * Computes min and max values within a table column. 
     * 
     * @return an array of double with two values.
     */
    private double[] computeColumnExtent(ResultsTable table, String heading) 
    {
        double[] values = getColumnValues(table, heading);
        
        double minVal = Double.MAX_VALUE;
        double maxVal = Double.MIN_VALUE;
        for (double v : values) 
        {
            minVal = Math.min(minVal, v);
            maxVal = Math.max(maxVal, v);
        }
        return new double[]{minVal, maxVal};
    }
    
    private double[] getColumnValues(ResultsTable table, String heading)
    {
        String[] allHeaders = table.getHeadings();

        // Check if column header corresponds to row label header
        boolean hasRowLabels = hasRowLabelColumn(table);
        if (hasRowLabels && heading.equals(allHeaders[0]))
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
        int index = table.getColumnIndex(heading);
        if (index == ResultsTable.COLUMN_NOT_FOUND)
        {
            throw new RuntimeException("Unable to find column index from header: " + heading);
        }
        return table.getColumnAsDoubles(index);
    }
    
    private static final boolean hasRowLabelColumn(ResultsTable table)
    {
        return table.getLastColumn() == (table.getHeadings().length-2);
    }
}
