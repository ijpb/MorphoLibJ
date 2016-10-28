/**
 * 
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
		
		// Create empty result image
		initResultImage();
		
		// Opens dialog to choose options
		createDialog();
		this.gd.showDialog();
		
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
			resultPlus.setSlice(labelImagePlus.getSlice());
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
			IJ.log("Found table: " + tableNames[i]);
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
	 * Computes min and max values within a table column. 
	 * 
	 * @return an array of double with two values.
	 */
	private double[] computeColumnExtent(ResultsTable table, String heading) 
	{
		int index = table.getColumnIndex(heading);
		double[] values = table.getColumnAsDoubles(index);
		
		double minVal = Double.MAX_VALUE;
		double maxVal = Double.MIN_VALUE;
		for (double v : values) 
		{
			minVal = Math.min(minVal, v);
			maxVal = Math.max(maxVal, v);
		}
		return new double[]{minVal, maxVal};
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
		// Assumes 2D image for the moment...
		
		// extract array of numerical values
		int index = this.table.getColumnIndex(this.selectedHeaderName);
		double[] values = table.getColumnAsDoubles(index);

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
//		IJ.log("event captured");
		if (gd.wasCanceled() || gd.wasOKed()) 
		{
			IJ.log("already closed...");
			return true;
		}
		
		@SuppressWarnings("rawtypes")
		Vector choices = gd.getChoices();
		if (choices == null) 
		{
			IJ.log("empty choices array...");
			return false;
		}
		
		if (evt.getSource() == choices.get(0)) 
		{
			IJ.log("update table");
			// Change of the data table
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
		
		if (evt.getSource() == choices.get(1)) 
		{
			IJ.log("update column header");
			// Change of the column heading
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
}
