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

import java.util.HashMap;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.Centroid;
import inra.ijpb.measure.region3d.Centroid3D;

/**
 * Creates a new image larger than the original one, and copies each label identically
 * but shifted by a given dilation coefficient. This results in 2D or 3D images with 
 * fewer labels touching each other, making them easier to visualize.  
 *
 * <p>The idea is to transform a label image in the following way:
 * <pre><code>
 *                  1 1 1 0 0 0 0 0
 * 1 1 1 0 2 2      1 1 1 0 0 0 2 2
 * 1 1 1 0 2 2      0 0 0 0 0 0 2 2
 * 0 0 0 0 2 2  =&gt;  0 0 0 0 0 0 2 2
 * 3 3 3 0 2 2      0 0 0 0 0 0 2 2     
 * 3 3 3 0 2 2      3 3 3 0 0 0 2 2
 *                  3 3 3 0 0 0 0 0
 *</code></pre>
 * 
 * @author dlegland
 *
 */
public class ExpandLabelsPlugin implements PlugIn 
{
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();
		
        // create the dialog, with operator options
		boolean isPlanar = imagePlus.getStackSize() == 1; 
        GenericDialog gd = new GenericDialog("Expand labels");
        gd.addNumericField("Dilation Coeff. (%)", 20, 0);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        int expandRatio = (int) gd.getNextNumber();
        
        String newName = imagePlus.getShortTitle() + "-expandLbl";
        
        // Apply size opening using IJPB library
        ImagePlus resultPlus;
        if (isPlanar)
        {
        	ImageProcessor image = imagePlus.getProcessor();
            ImageProcessor result = expandLabels(image, expandRatio);
            resultPlus = new ImagePlus(newName, result);
        }
        else
        {
        	ImageStack image = imagePlus.getStack();
        	ImageStack result = expandLabels(image, expandRatio);
        	result.setColorModel(image.getColorModel());
            resultPlus = new ImagePlus(newName, result);
        }

        // copy spatial calibration
        resultPlus.copyScale(imagePlus);
        
        // copy display range
        double vmin = imagePlus.getDisplayRangeMin();
        double vmax = imagePlus.getDisplayRangeMax();
        resultPlus.setDisplayRange(vmin, vmax);

        // Display image
        resultPlus.show();
        
        // For 3D images, choose same slice as original
		if (imagePlus.getStackSize() > 1)
		{
			int newSlice = (int) Math.floor(imagePlus.getCurrentSlice() * (1.0 + expandRatio / 100.0));
			resultPlus.setSlice(newSlice);
		}
	}
	/**
	 * Expand labels by a given factor
	 * @param image  input label image
	 * @param ratio  percentage of expansion (values between 0 and 100)
	 * @return expanded image
	 */
	public static final ImageProcessor expandLabels(ImageProcessor image,
			float ratio) 
	{
		// size of input image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		// size of result image
		int sizeX2 = (int) Math.round(sizeX * (1.0 + ratio / 100.0));
		int sizeY2 = (int) Math.round(sizeY * (1.0 + ratio / 100.0));
		
		// allocate memory for result
		ImageProcessor result = image.createProcessor(sizeX2, sizeY2);
	
		// compute centroids of labels
		int[] labels = LabelImages.findAllLabels(image);
		double[][] centroids = Centroid.centroids(image, labels);
		
		// compute shift associated to each label
		int nLabels = labels.length;
		int[][] shifts = new int[nLabels][2];
		for (int i = 0; i < nLabels; i++)
		{
			shifts[i][0] = (int) Math.floor(centroids[i][0] * ratio / 100.0);
			shifts[i][1] = (int) Math.floor(centroids[i][1] * ratio / 100.0);
		}
		
        // create associative array to know index of each label
		HashMap<Integer, Integer> labelIndices = new HashMap<Integer, Integer>();
        for (int i = 0; i < nLabels; i++) 
        {
        	labelIndices.put(labels[i], i);
        }

		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				float label = image.getf(x, y);
				if ( Float.compare( label, 0f ) == 0 )
					continue;

				int index = labelIndices.get((int)label);
				int x2 = x + shifts[index][0];
				int y2 = y + shifts[index][1];
				result.setf( x2, y2, label );
			}
		}
		
		return result;
	}

	/**
	 * Expand labels by a given factor
	 * @param image  input label image
	 * @param ratio  percentage of expansion (values between 0 and 100)
	 * @return expanded image
	 */
	public static final ImageStack expandLabels(ImageStack image,
			float ratio) 
	{
		// size of input image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// size of result image
		int sizeX2 = (int) Math.round(sizeX * (1. + ratio / 100));
		int sizeY2 = (int) Math.round(sizeY * (1. + ratio / 100));
		int sizeZ2 = (int) Math.round(sizeZ * (1. + ratio / 100));
		
		// allocate memory for result
		int bitDepth = image.getBitDepth();
		ImageStack result = ImageStack.create(sizeX2, sizeY2, sizeZ2, bitDepth);
	
		// compute centroids of labels
		int[] labels = LabelImages.findAllLabels(image);
		double[][] centroids = Centroid3D.centroids(image, labels);
		
		// compute shift associated to each label
		int nLabels = labels.length;
		int[][] shifts = new int[nLabels][3];
		for (int i = 0; i < nLabels; i++)
		{
			shifts[i][0] = (int) Math.floor(centroids[i][0] * ratio / 100);
			shifts[i][1] = (int) Math.floor(centroids[i][1] * ratio / 100);
			shifts[i][2] = (int) Math.floor(centroids[i][2] * ratio / 100);
		}
		
        // create associative array to know index of each label
		HashMap<Integer, Integer> labelIndices = new HashMap<Integer, Integer>();
        for (int i = 0; i < nLabels; i++) 
        {
        	labelIndices.put(labels[i], i);
        }

        for (int z = 0; z < sizeZ; z++)
        {
        	for (int y = 0; y < sizeY; y++)
        	{
        		for (int x = 0; x < sizeX; x++)
        		{
        			double label = image.getVoxel( x, y, z );
        			if ( Double.compare( label,  0 ) == 0 )
        				continue;

        			int index = labelIndices.get( (int) label );
        			int x2 = x + shifts[index][0];
        			int y2 = y + shifts[index][1];
        			int z2 = z + shifts[index][2];
        			result.setVoxel( x2, y2, z2, label );
        		}
        	}
        }

		return result;
	}

}
