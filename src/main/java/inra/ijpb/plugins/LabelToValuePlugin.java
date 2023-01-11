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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import ij.text.TextPanel;
import ij.text.TextWindow;
import inra.ijpb.label.LabelImages;
import inra.ijpb.util.IJUtils;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.TextField;
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
	
	/** The table containing the column to use */
	ResultsTable table = null;
	
	/** The name of the column (header) containing values associated to labels. */
	String columnName = null;
	
	/** Min and max values used to scale the display */
	double minValue;
	double maxValue;

	/**
     * The instance of GenericDialog using the options. When table or column is
     * changed, other widgets need to be updated.
     */
    GenericDialog gd = null;
    
	/** number of digits after decimal mark to display min/max values */
	int nDigits = 3;
		
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) 
	{
	    // in case of macro, we expect to run the static method "process(...)". 
	    if (IJ.isMacro())
	    {
	        return;
	    }
	    
		// Work on current image, and exit if no one is open
	    /**
	     * The label image, obtained as the current image when the plugin is run.
	     */
	    ImagePlus labelImagePlus;
		labelImagePlus = IJ.getImage();
	
		// Check that a table window is open
		TextWindow[] textWindows = IJUtils.getTableWindows();
        if (textWindows.length == 0)
        {
            IJ.error("Requires at least one Table window");
            return;
        }
        
		// Opens dialog to choose options
        this.gd = createDialog(textWindows);
		this.gd.showDialog();
		
		// parse dialog
		if (gd.wasCanceled())
			return;
		parseDialogOptions();
		
		// compute result image
		ImagePlus resultPlus;
        try 
        {
            resultPlus = process(labelImagePlus, table, this.columnName, this.minValue, this.maxValue);
        }
        catch (RuntimeException ex) 
        {
            IJ.error("Label to value error", 
                    "ERROR: label image values do not \n" + "correspond with table values!");
            return;
        }
		if (null == resultPlus)
		{
			return;
		}
		
		resultPlus.copyScale(labelImagePlus);

		String newName = labelImagePlus.getShortTitle() + "-" + columnName;
		resultPlus.setTitle(newName);
		resultPlus.show();
		
		// set up display 
		if (labelImagePlus.getStackSize() > 1) 
		{
			resultPlus.setSlice(labelImagePlus.getCurrentSlice());
		}
		
		// Write instructions for running the plugin from a macro
		String[] recordArgs = new String[] {
	            "Table=" + table.getTitle(), 
                "Column=" + columnName, 
                "Min=" + Double.toString(minValue), 
                "Max=" + Double.toString(maxValue), 
		};
		record("process", recordArgs);
	}
	
	private GenericDialog createDialog(TextWindow[] textWindows)
	{
	    String[] tableNames = retrieveTableNames(textWindows);
		
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

		this.gd = new GenericDialog("Assign Measure to Label");
		gd.addChoice("Table Name:", tableNames, tableNames[0]);
		gd.addChoice("Column:", headings, defaultHeading);
		gd.addNumericField("Min Value", extent[0], this.nDigits, 10, null);
		gd.addNumericField("Max Value", extent[1], this.nDigits, 10, null);
		gd.addDialogListener(this);
		
		return gd;
	}
	
	private static final String[] retrieveTableNames(TextWindow[] textWindows)
	{
        String[] tableNames = new String[textWindows.length];
        for (int i = 0; i < textWindows.length; i++)
        {
            tableNames[i] = textWindows[i].getTitle();
        }
        return tableNames;
	}
	
	/**
	 * Analyze current dialog, and setup inner fields of the class.
	 */
	private void parseDialogOptions() 
	{
		String tableName = this.gd.getNextChoice();
		Frame tableFrame = WindowManager.getFrame(tableName);
		this.table = ((TextWindow) tableFrame).getTextPanel().getResultsTable();
		
		this.columnName = this.gd.getNextChoice();
		
		this.minValue = this.gd.getNextNumber();
		this.maxValue = this.gd.getNextNumber();
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
    
    private static final double[] getColumnValues(ResultsTable table, String heading)
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
    
    
    /* **********************************************************
     * Processing methods
     * *********************************************************/

    /**
     * Combines a label image and the values within a column of a ResultsTable
     * to generate a parametric map of values that associates to each pixel /
     * voxel the value of the row associated to the label it belongs to.
     * Background pixels are associated to the value Float.NaN.
     * 
     * @param labelImage
     *            the image containing the regions labels
     * @param table
     *            the ResultsTable containing the values to map
     * @param columnName
     *            the name of the column containing the values to map (one row
     *            per label)
     * 
     * @return a new 32-bit float image containing the values associated to each
     *         label
     */
    public static final ImagePlus process(ImagePlus labelImage, ResultsTable table, String columnName)
    {
        // extract array of numerical values
        double[] values = getColumnValues(table, columnName);
        
        // Create result image
        ImagePlus resultPlus = LabelImages.applyLut(labelImage, values);
        resultPlus.copyScale(labelImage);
        
        return resultPlus;
    }
    
    /**
     * Combines a label image and the values within a column of a ResultsTable
     * to generate a parametric map of values that associates to each pixel /
     * voxel the value of the row associated to the label it belongs to.
     * Background pixels are associated to the value Float.NaN.
     * 
     * @param labelImage
     *            the image containing the regions labels
     * @param table
     *            the ResultsTable containing the values to map
     * @param columnName
     *            the name of the column containing the values to map (one row
     *            per label)
     * @param minValue
     *            the value that will be displayed in black
     * @param maxValue
     *            the value that will be displayed in white
     * 
     * @return a new 32-bit float image containing the values associated to each
     *         label
     */
    public static final ImagePlus process(ImagePlus labelImage, ResultsTable table, String columnName, double minValue, double maxValue)
    {
        ImagePlus resultPlus = process(labelImage, table, columnName);
        resultPlus.setDisplayRange(minValue, maxValue);

        return resultPlus;
    }
    
    
    /* **********************************************************
     * Macro recording related methods
     * *********************************************************/

    /**
     * Macro-record a specific command. The command names match the static 
     * methods that reproduce that part of the code.
     * 
     * @param command name of the command including package info
     * @param args set of arguments for the command
     */
    public static final void record(String command, String... args) 
    {
        if (!Recorder.record)
        {
            return;
        }

        // build the command string
        command = "call(\"inra.ijpb.plugins.LabelToValuePlugin." + command;
        for(int i = 0; i < args.length; i++)
        {
            command += "\", \"" + args[i];
        }
        command += "\");\n";
        
        // record the command
        Recorder.recordString(command);
    }
    
    /**
     * Process the current image as a label image using the provided string
     * arguments.
     * 
     * This method is intended to be called from macro, using the command line
     * generated by the "record" method.
     *
     * @see #record(String, String...)
     * @see #process(ImagePlus, ResultsTable, String, double, double)
     * 
     * @param tableNameArg
     *            the name of the result table to use
     * @param columnNameArg
     *            the name of the column (within the table) containing the
     *            values
     * @param minValueArg
     *            the minimal value to display as black in result image
     * @param maxValueArg
     *            the maximal value to display as white in result image
     */
    public static final void process(
            String tableNameArg,
            String columnNameArg,
            String minValueArg,
            String maxValueArg)
    {
        // first retrieve the image containing labels
        ImagePlus labelImagePlus = IJ.getImage();
        
        // convert options
        String tableName = tableNameArg.replace( "Table=", "");
        String columnName = columnNameArg.replace( "Column=", "");
        double minValue = Double.parseDouble(minValueArg.replace("Min=", ""));
        double maxValue = Double.parseDouble(maxValueArg.replace("Max=", ""));

        // retrieve Results table
        Frame tableFrame = WindowManager.getFrame(tableName);
        ResultsTable table = ((TextWindow) tableFrame).getTextPanel().getResultsTable();
        
        // check column name is valid
        if (table.getColumnIndex(columnName) == ResultsTable.COLUMN_NOT_FOUND)
        {
            String pattern = "Could not find column \"%s\" from table \"%s\"";
            throw new RuntimeException(String.format(pattern, columnName, tableName));
        }
        
        // Create result image
        ImagePlus resultPlus = process(labelImagePlus, table, columnName, minValue, maxValue);

        String newName = labelImagePlus.getShortTitle() + "-" + columnName;
        resultPlus.setTitle(newName);
        resultPlus.show();
        
        // set up display 
        if (labelImagePlus.getStackSize() > 1) 
        {
            resultPlus.setSlice(labelImagePlus.getCurrentSlice());
        }
    }
}
