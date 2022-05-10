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
package inra.ijpb.util;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Locale;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ImageProcessor;
import ij.text.TextWindow;

/**
 * A collection of utility methods for interacting with ImageJ.
 * 
 * @author David Legland
 *
 */
public class IJUtils 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private IJUtils()
	{
	}

	/**
	 * Display elapsed time, converted into seconds, and computes number of
	 * processed elements per second. Also returns the created message.
	 * 
	 * <p>
	 * Example of use:
	 * <pre><code>
	 * // initialize processing 
	 * ImageStack image = IJ.getImage().getStack();
	 * Strel3D strel = CubeStrel.fromDiameter(5);
	 * 
	 * // initialize timing 
	 * long t0 = System.currentTimeMillis();
	 * 
	 * // start processing 
	 * ImageStack res = Morphology.dilation(image, strel);
	 * ImagePlus resPlus = new ImagePlus("Dilation Result", res);
	 * resPlus.show();
	 * 
	 * // Display elapsed time
	 * long t1 = System.currentTimeMillis();
	 * IJUtils.showElapsedTime("dilation", t1 - t0, resPlus);
	 * </code></pre>
	 *
	 * @param opName
	 *            the name of the operation (algorithm, plugin...)
	 * @param timeInMillis
	 *            the elapsed time, in milliseconds
	 * @param refImage
	 *            the image on which process was applied
	 * @return the String corresponding to the message displayed in status bar
	 */
	public final static String showElapsedTime(String opName, double timeInMillis, ImagePlus refImage) 
	{
		double nElements = ((double) refImage.getWidth())  * refImage.getHeight();
		String elementName;
		if (refImage.getImageStackSize() == 1) 
		{
			elementName = "pixels";
		}
		else 
		{
			nElements *= refImage.getStackSize();
			elementName = "voxels";
		}
		
		double timeInSecs = timeInMillis / 1000.0;
		int elementsPerSecond = (int) (nElements / timeInSecs);
				
		String pattern = "%s: %.3f seconds, %d %s/second";
		String status = String.format(Locale.ENGLISH, pattern, opName, timeInSecs, elementsPerSecond, elementName);
		
		IJ.showStatus(status);
		return status;
	}
	
	/**
     * Return an array of String containing the name of all the currently open
     * images. If no image is open, returns an array with zero element.
     * 
     * @return an array of string containing the name of open images.
     */
    public static final String[] getOpenImageNames()
    {
        // retrieve list of image ID's
        int[] indices = WindowManager.getIDList();
        if (indices == null)
        {
            return new String[0];
        }

        // convert to a list of image names
        String[] imageNames = new String[indices.length];
        for (int i = 0; i < indices.length; i++)
        {
            imageNames[i] = WindowManager.getImage(indices[i]).getTitle();
        }

        // return image names
        return imageNames;
    }
	
    /**
     * Iterates on the list of TextWindows, and keeps only the ones containing a
     * non-null ResultsTable
     * 
     * @return an array containing the list of open TextWindows
     */
    public static final TextWindow[] getTableWindows() 
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
     * Extracts a list of integer labels from a string representation.
     * 
     * @param string
     *            the String containing labels, separated by commas or spaces.
     * @return the list of integer labels identified within the string
     */
    public static final int[] parseLabelList(String string) 
    {
        String[] tokens = string.split("[, ]+");
        int n = tokens.length;
        
        int[] labels = new int[n];
        for (int i = 0; i < n; i++)
        {
            labels[i] = Integer.parseInt(tokens[i]);
        }
        return labels;
    }
    
    /**
     * Prints the content of the given ImageProcessor on the console. This can be used
     * for debugging (small) images.
     * 
     * @param image the image to display on the console
     * 
     * @deprecated use {@link inra.ijpb.data.image.ImageUtils#print(ImageProcessor)} instead
     */
    @Deprecated
    public static final void printImage(ImageProcessor image) 
    {
        for (int y = 0; y < image.getHeight(); y++)
        {
            for (int x = 0; x < image.getWidth(); x++)
            {
                System.out.print(String.format("%3d ", (int) image.getf(x, y)));
            }
            System.out.println("");
        }
    }
    

}
