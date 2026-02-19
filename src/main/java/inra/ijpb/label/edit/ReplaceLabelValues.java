/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
/**
 * 
 */
package inra.ijpb.label.edit;

import java.util.TreeSet;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;

/**
 * Provides several methods for replacing label values within a label map
 * (stored as 8-, 16- or 32-bits).
 * 
 * <pre>{@code
    ImagePlus imagePlus = IJ.getImage();
	int[] labelArray = new int[]{2, 3, 8};
	int finalValue = 0;
	// create algo for replacing values in original image
	ReplaceLabelValues algo = new ReplaceLabelValues();
	DefaultAlgoListener.monitor(algo);
	algo.process(imagePlus, labelArray, finalValue);
 * }</pre>
 * 
 * @author dlegland
 */
public class ReplaceLabelValues extends AlgoStub
{
	/**
	 * Replace all values specified in label array by the value 0. 
	 * This method changes directly the values within the image.
	 * 
	 * @param imagePlus an ImagePlus containing a 3D label image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public void process(ImagePlus imagePlus, float[] labels, float newLabel) 
	{
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			process(image, labels, newLabel);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			process(image, labels, newLabel);
		}
	}

	/**
	 * Replace all values specified in label array by a new value.
	 *  
	 * @param image a label planar image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public void process(ImageProcessor image, int[] labels, int newLabel)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
		for (int i = 0; i < labels.length; i++)
		{
			labelSet.add(labels[i]);
		}
		
		for (int y = 0; y < sizeY; y++)
		{
			this.fireProgressChanged(this, y, sizeY);
			for (int x = 0; x < sizeX; x++)
			{
				int value = (int) image.getf(x, y);
				if (value == newLabel)
					continue;
				if (labelSet.contains(value)) 
					image.setf( x, y, newLabel );
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
	}

	/**
	 * Replace all values specified in label array by the value 0.
	 *  
	 * @param image a label planar image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public void process(ImageProcessor image, float[] labels, float newLabel)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		TreeSet<Float> labelSet = new TreeSet<Float>();
		for (int i = 0; i < labels.length; i++)
		{
			labelSet.add(labels[i]);
		}
		
		for (int y = 0; y < sizeY; y++)
		{
			this.fireProgressChanged(this, y, sizeY);
			for (int x = 0; x < sizeX; x++)
			{
				float value = image.getf(x, y); 
				if (value == newLabel)
					continue;
				if (labelSet.contains(value)) 
					image.setf(x, y, newLabel);
			}
		}
		
		this.fireProgressChanged(this, sizeY, sizeY);
	}
	

	/**
	 * Replace all values specified in label array by a new value.
	 *  
	 * @param image a label 3D image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public void process(ImageStack image, int[] labels, int newLabel)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
		for (int i = 0; i < labels.length; i++) 
		{
			labelSet.add(labels[i]);
		}
		
		for (int z = 0; z < sizeZ; z++) 
		{
			this.fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int value = (int) image.getVoxel(x, y, z); 
					if (value == newLabel)
						continue;
					if (labelSet.contains(value)) 
						image.setVoxel(x, y, z, newLabel);
				}
			}
		}

		this.fireProgressChanged(this, sizeZ, sizeZ);
	}

	/**
	 * Replace all values specified in label array by the specified value.
	 * 
	 * @param image
	 *            a 3D label image
	 * @param labels
	 *            the list of labels to replace
	 * @param newLabel
	 *            the new value for labels
	 */
	public void process(ImageStack image, float[] labels, float newLabel)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		TreeSet<Float> labelSet = new TreeSet<Float>();
		for (int i = 0; i < labels.length; i++) 
		{
			labelSet.add(labels[i]);
		}
		
		for (int z = 0; z < sizeZ; z++) 
		{
			this.fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++) 
				{
					float value = (float) image.getVoxel(x, y, z); 
					if (value == newLabel)
						continue;
					if (labelSet.contains(value)) 
						image.setVoxel(x, y, z, newLabel);
				}
			}
		}
		
		this.fireProgressChanged(this, sizeZ, sizeZ);
	}
}
