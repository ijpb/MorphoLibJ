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

import java.util.Set;
import java.util.TreeSet;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.morphology.strel.CubeStrel;
import inra.ijpb.morphology.strel.SquareStrel;
import inra.ijpb.util.IJUtils;

/**
 * Extract the neighbor regions within a label image from a region label, or a
 * list of region labels.
 * 
 * @see inra.ijpb.label.LabelImages#keepLabels(ImagePlus, int[])
 * 
 * @author dlegland
 *
 */
public class NeighborLabelsPlugin implements PlugIn 
{
    /** The input label image.*/
	ImagePlus labelImagePlus;
	
    /**
     * The resulting label image. It keeps the spatial calibration and display
     * settings.
     */
	ImagePlus resultPlus;
	
	GenericDialog gd = null;


    /** The list of labels to keep, as a coma-separated list of values. */
    String labelString = "";
    
    /** The list of labels to keep, as an array of int. */
    int[] labelList;
    
    /** The radius for detecting neighbors */
    int radius = 2;
    
    /**
     * boolean flag for keeping or not the initial labels in the result image.
     */
	boolean keepInitialLabels = false;
		
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
		
		// parse dialog
		if (gd.wasCanceled())
			return;
		parseDialogOptions();
		
		ImagePlus resultPlus = computeResultImage();
		if( null == resultPlus )
			return;
		
		this.resultPlus.copyScale(this.labelImagePlus);

		String newName = this.labelImagePlus.getShortTitle() + "-NeighborLabels";
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
		this.gd = new GenericDialog("Extract Neighbor Label");
		gd.addStringField("Labels", "0");
		gd.addMessage("Add labels seperated by comma.\nEx: [1, 2, 6, 9]");
        gd.addNumericField("Radius", 2, 0, 10, null);
		gd.addCheckbox("Keep Initial Labels", false);
        return gd;
	}
	
	/**
	 * Parses dialog, and setup inner fields of the class.
	 */
	private void parseDialogOptions() 
	{
	    this.labelString = this.gd.getNextString();
	    this.labelList = IJUtils.parseLabelList(this.labelString);
	    
        this.radius = (int) this.gd.getNextNumber();
        
        this.keepInitialLabels = this.gd.getNextBoolean();
	}
	
	private ImagePlus computeResultImage() 
	{
        int[] labels = IJUtils.parseLabelList(labelString);

        // Different processing depending on image dimensionality
        Set<Integer> labelSet;
        if (this.resultPlus.getStackSize() == 1)
        {
            // planar image
            ImageProcessor labelImage = this.labelImagePlus.getProcessor();
            
            // create binary mask
            ImageProcessor mask = selectedLabelsToMask(labelImage, labels);
            
            // dilation of mask
            Strel se = SquareStrel.fromRadius(this.radius);
            mask = se.dilation(mask);
            
            // intersect with original label image
            labelSet = findLabelsWithinMask(labelImage, mask);
        }
        else 
        {
            ImageStack labelImage = this.labelImagePlus.getStack();
            
            // create binary mask
            ImageStack mask = selectedLabelsToMask(labelImage, labels);
            
            // dilation of mask
            Strel3D se = CubeStrel.fromRadius(this.radius);
            mask = se.dilation(mask);
            
            // intersect with original label image
            labelSet  = findLabelsWithinMask(labelImage, mask);
        }
            
        // remove background label
        if (labelSet.contains(0))
        {
            labelSet.remove(0);
        }
        
        // optionally remove the initial labels
        if (!this.keepInitialLabels)
        {
            for (int label : labels)
            {
                labelSet.remove(label);
            }
        }
        
        // convert list to array
        int[] labelsToKeep = new int[labelSet.size()];
        int ind = 0;
        for (int label : labelSet)
        {
            labelsToKeep[ind++] = label;
        }
        
        this.resultPlus = LabelImages.keepLabels(this.labelImagePlus, labelsToKeep); 

		return this.resultPlus;
	}
	
	
	/**
     * Creates a new image containing only the specified labels.
     * 
     * @param imagePlus
     *            an ImagePlus containing a planar label image
     * @param labels
     *            the list of values to keep
     * @return a new instance of ImagePlus containing only the specified labels
     */
	public static final ImagePlus selectedLabelsToMask(ImagePlus imagePlus, int[] labels) 
    {
        ImagePlus resultPlus;
        String newName = imagePlus.getShortTitle() + "-keepLabels";
        
        // Dispatch to appropriate function depending on dimension
        if (imagePlus.getStackSize() == 1) 
        {
            // process planar image
            ImageProcessor image = imagePlus.getProcessor();
            ImageProcessor result = selectedLabelsToMask(image, labels);
            resultPlus = new ImagePlus(newName, result);
        }
        else 
        {
            // process image stack
            ImageStack image = imagePlus.getStack();
            ImageStack result = selectedLabelsToMask(image, labels);
            resultPlus = new ImagePlus(newName, result);
        }
        
        resultPlus.copyScale(imagePlus);
        return resultPlus;
    }

    /**
     * Creates a new image containing only the specified labels.
     * 
     * @param image
     *            a planar image of labels
     * @param labels
     *            the list of values to keep
     * @return a new binary image containing 255 only for pixels with values
     *         belonging to the list of labels.
     */
    private static final ImageProcessor selectedLabelsToMask(ImageProcessor image, int[] labels) 
    {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        ImageProcessor result = image.createProcessor(sizeX,  sizeY);
        
        TreeSet<Integer> labelSet = new TreeSet<Integer>();
        for (int i = 0; i < labels.length; i++) 
        {
            labelSet.add(labels[i]);
        }
        
        for (int y = 0; y < sizeY; y++) 
        {
            for (int x = 0; x < sizeX; x++)
            {
                int value = (int) image.getf(x, y); 
                if (labelSet.contains(value)) 
                    result.setf(x, y, 255);
            }
        }
        
        return result;
    }

    /**
     * Creates a new image containing only the specified labels.
     * 
     * @param image
     *            a 3D image of labels
     * @param labels
     *            the list of values to keep
     * @return a new 3D binary image containing 255 only for pixels with values
     *         belonging to the list of labels.
     */
    private static final ImageStack selectedLabelsToMask(ImageStack image, int[] labels)
    {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, image.getBitDepth());
        
        TreeSet<Integer> labelSet = new TreeSet<Integer>();
        for (int i = 0; i < labels.length; i++)
        {
            labelSet.add(labels[i]);
        }
        
        for (int z = 0; z < sizeZ; z++) 
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    int value = (int) image.getVoxel(x, y, z); 
                    if (value == 0)
                        continue;
                    if (labelSet.contains(value)) 
                        result.setVoxel(x, y, z, value);
                }
            }
        }
        
        return result;
    }
    
    private static final Set<Integer> findLabelsWithinMask(ImageProcessor image, ImageProcessor mask)
    {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();

        TreeSet<Integer> labelSet = new TreeSet<Integer>();
        for (int y = 0; y < sizeY; y++) 
        {
            for (int x = 0; x < sizeX; x++)
            {
                if (mask.get(x, y) > 0)
                {
                    int label = (int) image.getf(x, y);
                    labelSet.add(label);
                }
            }
        }
        
        return labelSet;
    }
    
    private static final Set<Integer> findLabelsWithinMask(ImageStack image, ImageStack mask)
    {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();

        TreeSet<Integer> labelSet = new TreeSet<Integer>();
        for (int z = 0; z < sizeZ; z++) 
        {
            for (int y = 0; y < sizeY; y++) 
            {
                for (int x = 0; x < sizeX; x++)
                {
                    if (mask.getVoxel(x, y, z) > 0)
                    {
                        int label = (int) image.getVoxel(x, y, z);
                        labelSet.add(label);
                    }
                }
            }
        }
        
        return labelSet;
    }
}
