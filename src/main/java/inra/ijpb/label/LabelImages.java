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
package inra.ijpb.label;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.FreehandRoi;
import ij.gui.PlotWindow;
import ij.gui.PointRoi;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * Utility methods for label images (stored as 8-, 16- or 32-bits).
 * 
 * @author David Legland
 *
 */
public class LabelImages 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	protected LabelImages()
	{
	}

	/**
	 * Checks if the input image may be a label image, and returns false if the
	 * type is not valid for label images.
	 * 
	 * @param imagePlus
	 *            the input image to check
	 * @return true if the type of the input image is valid for label images
	 */
	public static final boolean isLabelImageType(ImagePlus imagePlus)
	{
		int type = imagePlus.getType();
		return type == ImagePlus.GRAY8 || type == ImagePlus.COLOR_256
				|| type == ImagePlus.GRAY16 || type == ImagePlus.GRAY32;
	}

	/**
	 * Creates a label image with the appropriate class to store the required
	 * number of labels.
	 * 
	 * @param width
	 *            the width of the new label image
	 * @param height
	 *            the height of the new label image
	 * @param nLabels
	 *            expected number of labels in new image
	 * @return a new ImageProcessor with type adapted to store the expected
	 *         number of labels
	 */
	public static final ImageProcessor createLabelImage(int width, int height,
			int nLabels)	
	{
		if (nLabels < 256) 
		{
			return new ByteProcessor(width, height);
		} 
		else if (nLabels < 256 * 256) 
		{
			return new ShortProcessor(width, height);
		} 
		else if (nLabels < (0x01 << 23)) 
		{
			return new FloatProcessor(width, height);
		} 
		else 
		{
			IJ.error("Too many classes");
			return null;
		}
	}
	
	/**
	 * Creates a new image stack with the appropriate class to store the required
	 * number of labels.
	 * @param width
	 *            the width of the new label image
	 * @param height
	 *            the height of the new label image
	 * @param depth
	 *            the depth (number of slices) of the new label image
	 * @param nLabels
	 *            expected number of labels in new image
	 * @return a new ImageStack with type adapted to store the expected
	 *         number of labels
	 */
	public static final ImageStack createLabelStack(int width, int height,
			int depth, int nLabels)
	{
		if (nLabels < 256) 
		{
			return ImageStack.create(width, height, depth, 8);
		} 
		else if (nLabels < 256 * 256) 
		{
			return ImageStack.create(width, height, depth, 16);
		} 
		else if (nLabels < (0x01 << 23)) 
		{
			return ImageStack.create(width, height, depth, 32);
		} 
		else 
		{
			IJ.error("Too many classes");
			return null;
		}
	}
	
	/**
	 * <p>
	 * Creates a new label image from a set of binary images. The label values
	 * range between 1 and the number of images.
	 * </p>
	 * 
	 * Example:
	 * <pre><code>
	 * ImageProcessor binary1 = new ByteProcessor(10, 10);
	 * binary1.set(2, 2, 255);
	 * binary1.set(3, 5, 255);
	 * ImageProcessor binary2 = new ByteProcessor(10, 10);
	 * binary2.set(4, 4, 255);
	 * binary2.set(3, 5, 255); // overlap of binary images
	 * ImageProcessor binary3 = new ByteProcessor(10, 10);
	 * binary3.set(6, 6, 255);
	 * ImageProcessor labels = LabelImages.createLabelImage(binary1, 
	 * 		binary2, binary3);
	 * int background = labels.get(1, 1); // returns 0
	 * int label1 = labels.get(2, 2); // returns 1
	 * int label2 = labels.get(4, 4); // returns 2
	 * int label3 = labels.get(6, 6); // returns 3
	 * int label1overlap2 = labels.get(3, 5); // returns 2
	 * </code></pre>
	 * 
	 * @param images
	 *            a collection of binary images (0: background, &gt;0 pixel belongs
	 *            to current label)
	 * @return a new image with label values
	 */
	public static final ByteProcessor createLabelImage(ImageProcessor... images)
	{
		ImageProcessor refImage = images[0];
		int width = refImage.getWidth();
		int height = refImage.getHeight();
		
		ByteProcessor result = new ByteProcessor(width, height);
		
		int label = 0;
		for (ImageProcessor image : images) 
		{
			label++;
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					if (image.get(x, y) > 0) 
					{
						result.set(x, y, label);
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Creates a binary 3D image that contains 255 for voxels that are
	 * boundaries between two labels.
	 *
	 * @param image
	 *            a 3D image containing label regions
	 * @return a new 3D binary image with white voxels at label boundaries
	 */
	public static final ImageStack labelBoundaries(ImageStack image) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		
		for (int z = 0; z < sizeZ - 1; z++) 
		{
			for (int y = 0; y < sizeY - 1; y++)
			{
				for (int x = 0; x < sizeX - 1; x++)
				{
					double value = image.getVoxel(x, y, z);
					if (image.getVoxel(x+1, y, z) != value)
						result.setVoxel(x, y, z, 255);
					if (image.getVoxel(x, y+1, z) != value)
						result.setVoxel(x, y, z, 255);
					if (image.getVoxel(x, y, z+1) != value)
						result.setVoxel(x, y, z, 255);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Creates a binary 2D image that contains 255 for pixels that are
	 * boundaries between two labels.
	 *
	 * @param image
	 *            a 2D image containing label regions
	 * @return a new 2D binary image with white pixels at label boundaries
	 */
	public static final ImageProcessor labelBoundaries( ImageProcessor image )
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		ByteProcessor result = new ByteProcessor( sizeX, sizeY );

			for (int y = 0; y < sizeY - 1; y++)
			{
				for (int x = 0; x < sizeX - 1; x++)
				{
					double value = image.getf( x, y );
					if (image.getf( x+1, y ) != value )
						result.setf( x, y, 255 );
					if (image.getf( x, y+1 ) != value )
						result.setf( x, y, 255 );
					if (image.getf( x, y ) != value )
						result.setf( x, y, 255 );
				}
			}
		return result;
	}

	/**
	 * Returns a binary image that contains only the selected particle or
	 * region, by automatically cropping the image and eventually adding some
	 * borders.
	 * 
	 * @param imagePlus an image containing label of particles
	 * @param label the label of the particle to select
	 * @param border the number of pixels to add to each side of the particle
	 * @return a smaller binary image containing only the selected particle
	 */
	public static final ImagePlus cropLabel(ImagePlus imagePlus, int label,	int border) 
	{
        String newName = imagePlus.getShortTitle() + "-crop"; 
        ImagePlus croppedPlus;
        
        // Compute the cropped image
        if (imagePlus.getStackSize() == 1) 
        {
            ImageProcessor image = imagePlus.getProcessor();
            ImageProcessor cropped = LabelImages.cropLabel(image, label, border);
            croppedPlus = new ImagePlus(newName, cropped);
        }
        else
        {
            ImageStack image = imagePlus.getStack();
            ImageStack cropped = LabelImages.cropLabel(image, label, border);
            croppedPlus = new ImagePlus(newName, cropped);
        }

        return croppedPlus;
	}

	/**
	 * Returns a binary image that contains only the selected particle or
	 * region, by automatically cropping the image and eventually adding some
	 * borders.
	 * 
	 * @param image a, image containing label of particles
	 * @param label the label of the particle to select
	 * @param border the number of pixels to add to each side of the particle
	 * @return a smaller binary image containing only the selected particle
	 */
	public static final ImageProcessor cropLabel(ImageProcessor image, int label, int border) 
	{
		// image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		// Initialize label bounds
		int xmin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		int ymin = Integer.MAX_VALUE;
		int ymax = Integer.MIN_VALUE;
		
		// update bounds by iterating on voxels 
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				// process only specified label
				int val = image.get(x, y);
				if (val != label)
				{
					continue;
				}

				// update bounds of current label
				xmin = min(xmin, x);
				xmax = max(xmax, x);
				ymin = min(ymin, y);
				ymax = max(ymax, y);
			}
		}

		// Compute size of result, taking into account border
		int sizeX2 = (xmax - xmin + 1 + 2 * border);
		int sizeY2 = (ymax - ymin + 1 + 2 * border);

		// allocate memory for result image
		ImageProcessor result = new ByteProcessor(sizeX2, sizeY2);
		
		// fill result with binary label
		for (int y = ymin, y2 = border; y <= ymax; y++, y2++) 
		{
			for (int x = xmin, x2 = border; x <= xmax; x++, x2++) 
			{
				if ((image.get(x, y)) == label)
				{
					result.set(x2, y2, 255);
				}
			}
		}

		return result;
	}
	

	/**
	 * Returns a binary image that contains only the selected particle or
	 * region, by automatically cropping the image and eventually adding some
	 * borders.
	 * 
	 * @param image a 3D image containing label of particles
	 * @param label the label of the particle to select
	 * @param border the number of voxels to add to each side of the particle
	 * @return a smaller binary image containing only the selected particle
	 */
	public static final ImageStack cropLabel(ImageStack image, int label, int border) 
	{
		// image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// Initialize label bounds
		int xmin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		int ymin = Integer.MAX_VALUE;
		int ymax = Integer.MIN_VALUE;
		int zmin = Integer.MAX_VALUE;
		int zmax = Integer.MIN_VALUE;
		
		// update bounds by iterating on voxels 
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					// process only specified label
					int val = (int) image.getVoxel(x, y, z);
					if (val != label)
					{
						continue;
					}
					
					// update bounds of current label
					xmin = min(xmin, x);
					xmax = max(xmax, x);
					ymin = min(ymin, y);
					ymax = max(ymax, y);
					zmin = min(zmin, z);
					zmax = max(zmax, z);
				}
			}
		}
		
		// Compute size of result, taking into account border
		int sizeX2 = (xmax - xmin + 1 + 2 * border);
		int sizeY2 = (ymax - ymin + 1 + 2 * border);
		int sizeZ2 = (zmax - zmin + 1 + 2 * border);

		// allocate memory for result image
		ImageStack result = ImageStack.create(sizeX2, sizeY2, sizeZ2, 8);
		
		// fill result with binary label
		for (int z = zmin, z2 = border; z <= zmax; z++, z2++) 
		{
			for (int y = ymin, y2 = border; y <= ymax; y++, y2++) 
			{
				for (int x = xmin, x2 = border; x <= xmax; x++, x2++) 
				{
					if (((int) image.getVoxel(x, y, z)) == label)
					{
						result.setVoxel(x2, y2, z2, 255);
					}
				}
			}
		}
		
		return result;

	}
	
	/**
	 * Applies size opening on a label image: creates a new label image that
	 * contains only particles with at least the specified number of pixels or
	 * voxels.
	 * 
	 * @see #areaOpening(ImageProcessor, int)
	 * @see #volumeOpening(ImageStack, int)
	 * 
	 * @param labelImage
	 *            an image of label regions
	 * @param minElementCount
	 *            the minimal number of pixels or voxels of regions
	 * @return a new image containing only regions with enough elements
	 */
	public static final ImagePlus sizeOpening(ImagePlus labelImage, int minElementCount) 
	{
		boolean isPlanar = labelImage.getStackSize() == 1;
		
		ImagePlus resultPlus;
		String newName = labelImage.getShortTitle() + "-sizeOpening";
        
        if (isPlanar) 
        {
            ImageProcessor image = labelImage.getProcessor();
            ImageProcessor result = LabelImages.areaOpening(image, minElementCount);
            if (!(result instanceof ColorProcessor))
    			result.setLut(image.getLut());
            resultPlus = new ImagePlus(newName, result);    		
        }
        else
        {
            ImageStack image = labelImage.getStack();
            ImageStack result = LabelImages.volumeOpening(image, minElementCount);
        	result.setColorModel(image.getColorModel());
            resultPlus = new ImagePlus(newName, result);
        }
        
        // update display range
    	double min = labelImage.getDisplayRangeMin();
    	double max = labelImage.getDisplayRangeMax();
    	resultPlus.setDisplayRange(min, max);
        
        // keep spatial calibration
		resultPlus.copyScale(labelImage);
		return resultPlus;
	}
	
	/**
	 * Applies area opening on a label image: creates a new label image that
	 * contains only particles with at least the specified number of pixels.
	 * 
	 * @param labelImage
	 *            an image of label regions
	 * @param nPixelMin
	 *            the minimal number of pixels of regions
	 * @return a new image containing only regions with enough pixels
	 */
	public static final ImageProcessor areaOpening(ImageProcessor labelImage, int nPixelMin) 
	{
		// compute area of each label
		int[] labels = findAllLabels(labelImage);
		int[] areas = pixelCount(labelImage, labels);
		
		// find labels with sufficient area
		ArrayList<Integer> labelsToKeep = new ArrayList<Integer>(labels.length);
		for (int i = 0; i < labels.length; i++) 
		{
			if (areas[i] >= nPixelMin) 
			{
				labelsToKeep.add(labels[i]);
			}
		}
		
		// Convert array list into int array
		int[] labels2 = new int[labelsToKeep.size()];
		for (int i = 0; i < labelsToKeep.size(); i++) 
		{
			labels2[i] = labelsToKeep.get(i);
		}
		
		// keep only necessary labels
		ImageProcessor result = keepLabels(labelImage, labels2);

		if (!(result instanceof ColorProcessor))
			result.setLut(labelImage.getLut());
		return result;
	}
	
	/**
	 * Applies area opening on a 3D label image: creates a new label image that
	 * contains only particle with at least the specified number of voxels.
	 * Keep original labels unchanged.
	 * 
	 * @param labelImage
	 *            an image of label regions
	 * @param nVoxelMin
	 *            the minimal number of voxels of regions
	 * @return a new image containing only regions with enough voxels
	 */
	public static final ImageStack volumeOpening(ImageStack labelImage, int nVoxelMin) 
	{
		// compute area of each label
		int[] labels = LabelImages.findAllLabels(labelImage);
		int[] vols = LabelImages.voxelCount(labelImage, labels);
		
		// find labels with sufficient area
		ArrayList<Integer> labelsToKeep = new ArrayList<Integer>(labels.length);
		for (int i = 0; i < labels.length; i++)
		{
			if (vols[i] >= nVoxelMin)
			{
				labelsToKeep.add(labels[i]);
			}
		}
		
		// Convert array list into int array
		int[] labels2 = new int[labelsToKeep.size()];
		for (int i = 0; i < labelsToKeep.size(); i++)
		{
			labels2[i] = labelsToKeep.get(i);
		}
		
		// keep only necessary labels
		ImageStack result = keepLabels(labelImage, labels2); 

		// update display info
		result.setColorModel(labelImage.getColorModel());
    	
		return result;
	}
	
	/**
	 * Creates a new Color image from a label image, a LUT, and a
	 * color for background.
	 * 
	 * @param imagePlus a 2D or 3D image containing labels and 0 for background
	 * @param lut the array of color components for each label 
	 * @param bgColor the background color
	 * @return a new Color image
	 */
	public static final ImagePlus labelToRgb(ImagePlus imagePlus, byte[][] lut, Color bgColor)
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-rgb";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1)
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = labelToRgb(image, lut, bgColor);
			resultPlus = new ImagePlus(newName, result);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = labelToRgb(image, lut, bgColor);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.copyScale(imagePlus);
		return resultPlus;
	}
	
	/**
	 * Creates a new Color image from a label planar image, a LUT, and a 
	 * color for background.
	 * 
	 * @param image an ImageProcessor with label values and 0 for background
	 * @param lut the array of color components for each label 
	 * @param bgColor the background color
	 * @return a new instance of ColorProcessor
	 */
	public static final ColorProcessor labelToRgb(ImageProcessor image, byte[][] lut, Color bgColor) 
	{
		int width = image.getWidth();
		int height = image.getHeight();
		
		int bgColorCode = bgColor.getRGB();
		
		ColorProcessor result = new ColorProcessor(width, height);
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				int index = (int) image.getf(x, y);
				if (index == 0) 
				{
					result.set(x, y, bgColorCode);
				} 
				else 
				{
					byte[] rgb = lut[index - 1];
					int color = (int) ((rgb[0] & 0xFF) << 16
							| (rgb[1] & 0xFF) << 8 | (rgb[2] & 0xFF));
					result.set(x, y, color);
				}
			}
		}
		
		return result;
	}

	/**
	 * Creates a new Color image stack from a label image stack, a LUT, and a
	 * color for background.
	 * 
	 * @param image an ImageStack with label values and 0 for background
	 * @param lut the array of color components for each label 
	 * @param bgColor the background color
	 * @return a new instance of ImageStack containing color processors
	 */
	public static final ImageStack labelToRgb(ImageStack image, byte[][] lut, Color bgColor)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 24);
		
		int bgColorCode = bgColor.getRGB();
		
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int index = (int) image.getVoxel(x, y, z);
					if (index == 0) 
					{
						result.setVoxel(x, y, z, bgColorCode);
					} 
					else 
					{
						byte[] rgb = lut[index - 1];
						int color = (int) ((rgb[0] & 0xFF) << 16
								| (rgb[1] & 0xFF) << 8 | (rgb[2] & 0xFF));
						result.setVoxel(x, y, z, color);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Removes all regions that touch the borders of the image.
	 * @param imagePlus a label image
	 */
	public static final void removeBorderLabels(ImagePlus imagePlus) 
	{
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			removeBorderLabels(imagePlus.getProcessor());
		} else {
			removeBorderLabels(imagePlus.getStack());
		}
	}

	/**
	 * Removes all regions that touch the borders of the image.
	 * @param image a label image
	 */
	public static final void removeBorderLabels(ImageProcessor image) 
	{
		int[] labels = findBorderLabels(image);
		replaceLabels(image, labels, 0);
	}

	private static final int[] findBorderLabels(ImageProcessor image) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
	
		// find labels in top and bottom borders
		for (int x = 0; x < sizeX; x++)
		{
			labelSet.add((int) image.getf(x, 0));
			labelSet.add((int) image.getf(x, sizeY - 1));
		}
	
		// find labels in left and right borders
		for (int y = 0; y < sizeY; y++) 
		{
			labelSet.add((int) image.getf(0, y));
			labelSet.add((int) image.getf(sizeX - 1, y));
		}
	
		// remove label for the background
		labelSet.remove(0);
		
		// convert to an array of int
		int[] labels = new int[labelSet.size()];
		int i = 0 ;
		Iterator<Integer> iter = labelSet.iterator();
		while(iter.hasNext()) 
		{
			labels[i++] = (int) iter.next();
		}
		
		return labels;
	}

	/**
	 * Removes all regions that touch the borders of the image.
	 * @param image a label image
	 */
	public static final void removeBorderLabels(ImageStack image) 
	{
		int[] labels = findBorderLabels(image);
		replaceLabels(image, labels, 0);
	}

	private static final int[] findBorderLabels(ImageStack image) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
	
		// find labels in front (z=0) and back (z=sizeZ-1) slices
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				labelSet.add((int) image.getVoxel(x, y, 0));
				labelSet.add((int) image.getVoxel(x, y, sizeZ - 1));
			}
		}
		
		// find labels in top  (y=0) and bottom (y=sizeY-1) borders
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int x = 0; x < sizeX; x++)
			{
				labelSet.add((int) image.getVoxel(x, 0, z));
				labelSet.add((int) image.getVoxel(x, sizeY - 1, z));
			}
		}
		
		// find labels in left (x=0) and right (x=sizeX-1) borders
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++) 
			{
				labelSet.add((int) image.getVoxel(0, y, z));
				labelSet.add((int) image.getVoxel(sizeX - 1, y, z));
			}
		}
	
		// remove label for the background
		labelSet.remove(0);
		
		// convert to an array of int
		int[] labels = new int[labelSet.size()];
		int i = 0 ;
		Iterator<Integer> iter = labelSet.iterator();
		while(iter.hasNext())
		{
			labels[i++] = (int) iter.next();
		}
		
		return labels;
	}

	/**
	 * Returns a binary image that contains only the largest label.
	 * 
	 * @param imagePlus an instance of ImagePlus containing a label image
	 * @return a new ImagePlus containing only the largest label
	 */
	public static final ImagePlus keepLargestLabel(ImagePlus imagePlus)
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-largest";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = keepLargestLabel(image);
			resultPlus = new ImagePlus(newName, result);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = keepLargestLabel(image);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.copyScale(imagePlus);
		return resultPlus;
	}
	
	/**
	 * Returns a binary image that contains only the largest label.
	 * 
	 * @param image
	 *            a label image
	 * @return a new image containing only the largest label
	 * @throws RuntimeException if the image is empty
	 */
	public static final ImageProcessor keepLargestLabel(ImageProcessor image)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		ImageProcessor result = new ByteProcessor(sizeX, sizeY);
		
		// identify labels of input image
		int[] labels = findAllLabels(image);
		if (labels.length == 0)
		{
			throw new RuntimeException("Can not select a label in an empty image");
		}

		// find the label of the largest particle
		int[] areas = pixelCount(image, labels);
		int largestLabel = labels[indexOfMax(areas)];

		// convert label image to binary image
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int label = (int) image.getf(x, y); 
				if (label == largestLabel)
					result.set(x, y, 255);
				else
					result.set(x, y, 0);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a binary image that contains only the largest label.
	 * 
	 * @param image
	 *            a label image
	 * @return a new image containing only the largest label
	 * @throws RuntimeException if the image is empty
	 */
	public static final ImageStack keepLargestLabel(ImageStack image) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		
		// identify labels of input image
		int[] labels = findAllLabels(image);
		if (labels.length == 0)
		{
			throw new RuntimeException("Can not select a label in an empty image");
		}
		
		// find the label of the largest particle
		int[] volumes = voxelCount(image, labels);		
		int largestLabel = labels[indexOfMax(volumes)];
		
		// convert label image to binary image
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int label = (int) image.getVoxel(x, y, z); 
					if (label == largestLabel)
						result.setVoxel(x, y, z, 255);
					else
						result.setVoxel(x, y, z, 0);
				}
			}
		}
		
		return result;
	}

	/**
	 * Removes the regions corresponding to the largest label from a label image.
	 * 
	 * @param imagePlus
	 *            a label image
	 */
	public static final void removeLargestLabel(ImagePlus imagePlus)
	{
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
			removeLargestLabel(imagePlus.getProcessor());
		else
			removeLargestLabel(imagePlus.getStack());
	}

	/**
	 * Removes the regions corresponding to the largest label from a label
	 * image.
	 * 
	 * @param image
	 *            a label image
	 */
	public static final void removeLargestLabel(ImageProcessor image) 
	{
		// determine image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// identify labels of input image
		int[] labels = findAllLabels(image);
		if (labels.length == 0)
		{
			// if no label is found, there is nothing to remove...
			return;
		}
		
		// find the label of the largest particle
		int[] areas = pixelCount(image, labels);
		int largestLabel = labels[indexOfMax(areas)];
		
		// remove pixels belonging to the largest label
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int label = (int) image.getf(x, y); 
				if (label == largestLabel)
					image.setf(x, y, 0);
			}
		}
	}

	/**
	 * Removes the regions corresponding to the largest label from a label
	 * image.
	 * 
	 * @param image
	 *            a 3D label image
	 */
	public static final void removeLargestLabel(ImageStack image) 
	{
		// determine image size
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// identify labels of input image
		int[] labels = findAllLabels(image);
		if (labels.length == 0)
		{
			// if no label is found, there is nothing to remove...
			return;
		}

		// find the label of the largest particle
		int[] volumes = voxelCount(image, labels);
		int largestLabel = labels[indexOfMax(volumes)];
		
		// remove voxels belonging to the largest label
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int label = (int) image.getVoxel(x, y, z); 
					if (label == largestLabel)
						image.setVoxel(x, y, z, 0);
				}
			}
		}
	}

	/**
	 * Returns the index of the maximum value within the integer array, or -1 if
	 * the array has no element.
	 * 
	 * @param values
	 *            the array of values
	 * @return the index of the maximum value
	 */
	private static final int indexOfMax(int[] values)
	{
		int indMax = -1;
		int volumeMax = Integer.MIN_VALUE;
		for (int i = 0; i < values.length; i++) 
		{
			if (values[i] > volumeMax) 
			{
				volumeMax = values[i];
				indMax = i;
			}
		}
		return indMax;
	}
	
    /**
	 * Computes the number of pixels composing each particle in the label image.
	 * 
	 * @see inra.ijpb.measure.GeometricMeasures2D#area(ij.process.ImageProcessor,
	 *      int[], double[])
	 * 
	 * @param image
	 *            a label image
	 * @param labels the array of label indices to process
	 * @return an array the same size as labels, containing the number of pixels
	 *         of each region
	 */
    public static final int[] pixelCount(ImageProcessor image, int[] labels) 
    {
    	// image size
	    int width 	= image.getWidth();
	    int height 	= image.getHeight();
	
        // create associative array to identify the index of each label
	    HashMap<Integer, Integer> labelIndices = mapLabelIndices(labels);

        // initialize result
		int nLabels = labels.length;
		int[] counts = new int[nLabels];
	
		// count all pixels belonging to the particle
	    for (int y = 0; y < height; y++) 
	    {
	        for (int x = 0; x < width; x++) 
	        {
	        	int label = (int) image.getf(x, y);
	        	if (label == 0)
					continue;
				int labelIndex = labelIndices.get(label);
				counts[labelIndex]++;
	        }
	    }	
	    
	    return counts;
	}

	/**
	 * Counts the number of voxels that compose each labeled particle in 3D 
	 * image.
	 * @see inra.ijpb.measure.GeometricMeasures3D#volume(ij.ImageStack, int[], double[])
	 * 
	 * @param image
	 *            a label image
	 * @param labels the array of label indices to process
	 * @return an array the same size as labels, containing the number of voxels
	 *         of each region
	*/
	public final static int[] voxelCount(ImageStack image, int[] labels) 
	{
        // create associative array to know index of each label
		HashMap<Integer, Integer> labelIndices = mapLabelIndices(labels);

        // initialize result
		int nLabels = labels.length;
		int[] counts = new int[nLabels];

		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// iterate on image voxels
		for (int z = 0; z < sizeZ; z++) 
        {
        	IJ.showProgress(z, sizeZ);
        	for (int y = 0; y < sizeY; y++)
        	{
        		for (int x = 0; x < sizeX; x++)
        		{
        			int label = (int) image.getVoxel(x, y, z);
					// do not consider background
					if (label == 0)
						continue;
					int labelIndex = labelIndices.get(label);
					counts[labelIndex]++;
        		}
        	}
        }
        
		return counts;
	}
	
	public final static int findLargestLabel(ImagePlus imagePlus)
	{
		int max = 0;
		for (int i = 1; i <= imagePlus.getImageStackSize(); i++)
		{
			ImageProcessor slice = imagePlus.getStack().getProcessor(i);
			for (int j = 0; j < slice.getPixelCount(); j++)
			{
				max = Math.max(max, (int) slice.getf(j));
			}
		}
		return max;
	}
	
    /**
	 * Returns the set of unique labels existing in the given image, excluding
	 * the value zero (used for background).
	 * 
	 * @param image
	 *            an instance of ImagePlus containing a label image
	 * @return the list of unique labels present in image (without background)
	 */
    public final static int[] findAllLabels(ImagePlus image) 
    {
		return image.getStackSize() == 1 ? findAllLabels(image.getProcessor())
				: findAllLabels(image.getStack());
    }

    /**
     * Returns the set of unique labels existing in the given stack, excluding 
     * the value zero (used for background).
	 * 
	 * @param image
	 *            a 3D label image
	 * @return the list of unique labels present in image (without background)
     */
    public final static int[] findAllLabels(ImageStack image) 
    {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        TreeSet<Integer> labels = new TreeSet<Integer> ();
        
        // iterate on image pixels
        for (int z = 0; z < sizeZ; z++) 
        {
        	for (int y = 0; y < sizeY; y++)  
        	{
        		for (int x = 0; x < sizeX; x++)
        		{
        			labels.add((int) image.getVoxel(x, y, z));
        		}
        	}
        }
        
        // remove 0 if it exists
        if (labels.contains(0))
            labels.remove(0);
        
        // convert to array of integers
        int[] array = new int[labels.size()];
        Iterator<Integer> iterator = labels.iterator();
        for (int i = 0; i < labels.size(); i++) 
            array[i] = iterator.next();
        
        return array;
    }

    /**
     * Returns the set of unique labels existing in the given image, excluding 
     * the value zero (used for background).
	 * 
	 * @param image
	 *            a label image
	 * @return the list of unique labels present in image (without background)
     */
    public final static int[] findAllLabels(ImageProcessor image)
    {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        TreeSet<Integer> labels = new TreeSet<Integer> ();
        
        // iterate on image pixels
        if (image instanceof FloatProcessor) 
        {
        	// For float processor, use explicit case to int from float value  
        	for (int y = 0; y < sizeY; y++) 
        	{
        		for (int x = 0; x < sizeX; x++) 
        			labels.add((int) image.getf(x, y));
        	}
        } 
        else
        {
        	// for integer-based images, simply use integer result
        	for (int y = 0; y < sizeY; y++) 
        	{
        		for (int x = 0; x < sizeX; x++) 
        			labels.add(image.get(x, y));
        	}
        }
        
        // remove 0 if it exists
        if (labels.contains(0))
            labels.remove(0);
        
        // convert to array of integers
        int[] array = new int[labels.size()];
        Iterator<Integer> iterator = labels.iterator();
        for (int i = 0; i < labels.size(); i++) 
            array[i] = iterator.next();
        
        return array;
    }

	/**
	 * Replace all values specified in label array by a new value. 
	 * This method changes directly the values within the image.
	 * 
	 * @param imagePlus an ImagePlus containing a 3D label image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public static final void replaceLabels(ImagePlus imagePlus, int[] labels, int newLabel) 
	{
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			replaceLabels(image, labels, newLabel);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			replaceLabels(image, labels, newLabel);
		}
	}

	/**
	 * Replace all values specified in label array by the value 0. 
	 * This method changes directly the values within the image.
	 * 
	 * @param imagePlus an ImagePlus containing a 3D label image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public static final void replaceLabels(ImagePlus imagePlus, float[] labels, float newLabel) 
	{
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			replaceLabels(image, labels, newLabel);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			replaceLabels(image, labels, newLabel);
		}
	}

	/**
	 * Replace all values specified in label array by a new value.
	 *  
	 * @param image a label planar image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public static final void replaceLabels(ImageProcessor image, int[] labels, int newLabel)
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
			for (int x = 0; x < sizeX; x++)
			{
				int value = (int) image.getf(x, y);
				if (value == newLabel)
					continue;
				if (labelSet.contains(value)) 
					image.setf( x, y, newLabel );
			}
		}
	}

	/**
	 * Replace all values specified in label array by the value 0.
	 *  
	 * @param image a label planar image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public static final void replaceLabels(ImageProcessor image, float[] labels, float newLabel)
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
			for (int x = 0; x < sizeX; x++)
			{
				float value = image.getf(x, y); 
				if (value == newLabel)
					continue;
				if (labelSet.contains(value)) 
					image.setf(x, y, newLabel);
			}
		}
	}

	/**
	 * Replace all values specified in label array by a new value.
	 *  
	 * @param image a label 3D image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public static final void replaceLabels(ImageStack image, int[] labels, int newLabel)
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
	}

	/**
	 * Replace all values specified in label array by the value 0.
	 *  
	 * @param image a label 3D image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public static final void replaceLabels(ImageStack image, float[] labels, float newLabel)
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
	public static final ImagePlus keepLabels(ImagePlus imagePlus, int[] labels) 
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-keepLabels";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = keepLabels(image, labels);
			resultPlus = new ImagePlus(newName, result);
		}
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = keepLabels(image, labels);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.copyScale(imagePlus);
		return resultPlus;
	}

	/**
	 * Creates a new image containing only the specified labels.
	 * 
	 * @param image
	 *            a planar label image
	 * @param labels
	 *            the list of values to keep
	 * @return a new label image containing only the specified labels
	 */
	public static final ImageProcessor keepLabels(ImageProcessor image, int[] labels) 
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
				if (value == 0)
					continue;
				if (labelSet.contains(value)) 
					result.setf(x, y, value);
			}
		}
		
		return result;
	}

	/**
	 * Creates a new image containing only the specified labels.
	 * 
	 * @param image
	 *            a 3D label image
	 * @param labels
	 *            the list of values to keep
	 * @return a new 3D label image containing only the specified labels
	 */
	public static final ImageStack keepLabels(ImageStack image, int[] labels) 
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
	
	/**
	 * Applies the given Look-up table to the input label image.
	 * 
	 * @param labelImage
	 *            a label image
	 * @param values
	 *            a set of values associated to each unique label
	 * @return a new FloatProcessor containing for each pixel, either the value
	 *         associated to the corresponding pixel, or 0 if the pixel is
	 *         background
	 */
	public static final FloatProcessor applyLut(ImageProcessor labelImage, double[] values) 
	{
		int width = labelImage.getWidth(); 
		int height = labelImage.getHeight(); 
		
		FloatProcessor resultImage = new FloatProcessor(width, height);
		
        // extract particle labels
        int[] labels = LabelImages.findAllLabels(labelImage);
        
        // create associative array to know index of each label
        HashMap<Integer, Integer> labelIndices = mapLabelIndices(labels);

		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				if (label == 0)
				{
					resultImage.setf(x, y, Float.NaN);
					continue;
				}
				
				int index = labelIndices.get(label);
				
				if (index >= values.length) {
					throw new RuntimeException("Try to access index " + index + 
							" in array with " + values.length + " values");
				}
				
				double value = values[index];
				resultImage.setf(x, y, (float) value);
			}
		}
		
		return resultImage;
	}
	
	/**
	 * Applies the given Look-up table to the input label image.
	 * 
	 * @param labelImage
	 *            a 3D label image
	 * @param values
	 *            a set of values associated to each unique label
	 * @return a new 3D image containing for each pixel, either the value
	 *         associated to the corresponding pixel, or 0 if the pixel is
	 *         background
	 */
	public static final ImageStack applyLut(ImageStack labelImage, double[] values) 
	{
		int sizeX = labelImage.getWidth(); 
		int sizeY = labelImage.getHeight(); 
		int sizeZ = labelImage.getSize(); 
		
		ImageStack resultImage = ImageStack.create(sizeX, sizeY, sizeZ, 32);
		
        // extract particle labels
        int[] labels = LabelImages.findAllLabels(labelImage);
        
        // create associative array to know index of each label
        HashMap<Integer, Integer> labelIndices = mapLabelIndices(labels);

        // Iterate over voxels to change their color
        for (int z = 0; z < sizeZ; z++) 
        {
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++)
				{
					int label = (int) labelImage.getVoxel(x, y, z);
					if (label == 0)
					{
						resultImage.setVoxel(x, y, z, Double.NaN);
						continue;
					}

					int index = labelIndices.get(label);
					
					if (index >= values.length) 
					{
						throw new RuntimeException("Try to access index " + index + 
								" in array with " + values.length + " values");
					}
					
					double value = values[index];
					resultImage.setVoxel(x, y, z, value);
				}
			}
		}
        
        return resultImage;
	}

	/**
	 * Creates an associative array to retrieve the index corresponding to each label.
	 * 
	 * <p>
	 * The resulting map verifies the relation:
	 * <pre>
	 * {@code
	 * int[] labels = ...
	 * Map<Integer,Integer> labelIndices = LabelImages.mapLabelIndices(labels);
	 * int index = ...
	 * assert(index == labelIndices.get(labels[index]));
	 * }
	 * </pre>
	 * 
	 * @param labels
	 *            an array of labels
	 * @return a HashMap instance with each label as key, and the index of the
	 *         label in array as value.
	 */
	public static final HashMap<Integer, Integer> mapLabelIndices(int[] labels)
	{
		int nLabels = labels.length;
        HashMap<Integer, Integer> labelIndices = new HashMap<Integer, Integer>();
        for (int i = 0; i < nLabels; i++) 
        {
        	labelIndices.put(labels[i], i);
        }

        return labelIndices;
	}
	
	/**
	 * Merge labels selected by freehand or point tool. Labels are merged
	 * in place (i.e., the input image is modified). Zero-value label is never
	 * merged.
	 *
	 * @param labelImage  label image to modify
	 * @param roi  selection indicating the labels to merge
	 * @param verbose  option to write in the log window the labels merged
	 */
	public static final void mergeLabels(
			final ImagePlus labelImage,
			final Roi roi,
			final boolean verbose )
	{
		if( roi == null )
		{
			IJ.showMessage( "Please select some labels to merge using the "
					+ "freehand or point selection tool." );
			return;
		}

		final ArrayList<Float> list = getSelectedLabels( labelImage, roi );

		// if more than one value is selected, merge
		if( list.size() > 1 )
		{
			float finalValue = list.remove( 0 );
			float[] labelArray = new float[ list.size() ];
			int i = 0;

			for ( Float f : list )
				labelArray[i++] = f != null ? f : Float.NaN;

			String sLabels = new String( ""+ (long) labelArray[ 0 ] );
			for( int j=1; j < labelArray.length; j++ )
				sLabels += ", " + (long) labelArray[ j ];
			if( verbose )
				IJ.log( "Merging label(s) " + sLabels + " to label "
							+ (long) finalValue );
			LabelImages.replaceLabels( labelImage,
					labelArray, finalValue );
		}
		else
			IJ.error( "Please select two or more different"
					+ " labels to merge" );
	}// end method mergeLabels

	/**
	 * Remove labels selected by freehand or point ROIs (in place).
	 *
	 * @param labelImage  input label image
	 * @param roi  FreehandRoi or PointRoi with selected labels
	 * @param verbose  flag to print deleted labels in log window
	 */
	public static void removeLabels(
			final ImagePlus labelImage,
			final Roi roi,
			final boolean verbose )
	{
		if( roi == null )
		{
			IJ.showMessage( "Please select at least one label to be removed"
					+ " using the freehand or point selection tools." );
			return;
		}

		final ArrayList<Float> list = getSelectedLabels( labelImage, roi );

		if( list.size() > 0 )
		{
			// move list values into an array
			float[] labelArray = new float[ list.size() ];
			int i = 0;
			for ( Float f : list )
				labelArray[ i++ ] = f != null ? f : Float.NaN;

			String sLabels = new String( ""+ (long) labelArray[ 0 ] );
			for( int j=1; j < labelArray.length; j++ )
				sLabels += ", " + (long) labelArray[ j ];
			if( verbose )
				IJ.log( "Removing label(s) " + sLabels + "..." );

			LabelImages.replaceLabels( labelImage,
					labelArray, 0 );
		}
		else
			IJ.error( "Please select at least one label to remove." );
	}

	/**
	 * Get list of selected labels in label image. Labels are selected by
	 * either a freehand ROI or point ROIs. Zero-value label is skipped.
	 *
	 * @param labelImage  label image
	 * @param roi  FreehandRoi or PointRoi with selected labels
	 * @return list of selected labels
	 */
	public static ArrayList<Float> getSelectedLabels(
			final ImagePlus labelImage,
			final Roi roi )
	{
		final ArrayList<Float> list = new ArrayList<Float>();

		// if the user makes point selections
		if( roi instanceof PointRoi )
		{
			int[] xpoints = roi.getPolygon().xpoints;
			int[] ypoints = roi.getPolygon().ypoints;

			// read label values at those positions
			if( labelImage.getImageStackSize() > 1 )
			{
				final ImageStack labelStack = labelImage.getImageStack();
				for ( int i = 0; i<xpoints.length; i ++ )
				{
					float value = (float) labelStack.getVoxel(
							xpoints[ i ],
							ypoints[ i ],
							((PointRoi) roi).getPointPosition( i )-1 );
					if( Float.compare( 0f, value ) != 0 &&
							list.contains( value ) == false )
						list.add( (float) value );
				}
			}
			else
			{
				final ImageProcessor ip = labelImage.getProcessor();
				for ( int i = 0; i<xpoints.length; i ++ )
				{
					float value = ip.getf( xpoints[ i ], ypoints[ i ]);
					if( Float.compare( 0f, value ) != 0 &&
							list.contains( value ) == false )
						list.add( (float) value );
				}
			}
		}
		else if( roi instanceof FreehandRoi )
		{
			// read values from ROI using a profile plot
			// save interpolation option
			boolean interpolateOption = PlotWindow.interpolate;
			// do not interpolate pixel values
			PlotWindow.interpolate = false;
			// get label values from line roi (different from 0)
			float[] values = ( new ProfilePlot( labelImage ) )
					.getPlot().getYValues();
			PlotWindow.interpolate = interpolateOption;

			for( int i=0; i<values.length; i++ )
			{
				if( Float.compare( 0f, values[ i ] ) != 0 &&
						list.contains( values[ i ]) == false )
					list.add( values[ i ]);
			}
		}
		return list;
	}
	
	/**
	 * For each label, finds the position of the point belonging to label region
	 * defined by <code>labelImage</code> and with maximal value in intensity
	 * image <code>valueImage</code>.
	 * 
	 * @param valueImage
	 *            the intensity image containing values to compare
	 * @param labelImage
	 *            the intensity image containing label of each pixel
	 * @param labels
	 *            the list of labels in the label image
	 * @return the position of maximum value in intensity image for each label
	 */
	public static final Point[] findPositionOfMaxValues(ImageProcessor valueImage, 
			ImageProcessor labelImage, int[] labels)
	{
		// Create associative map between each label and its index
		Map<Integer,Integer> labelIndices = mapLabelIndices(labels);
		
		// Init Position and value of maximum for each label
		int nbLabel = labels.length;
		Point[] posMax 	= new Point[nbLabel];
		float[] maxValues = new float[nbLabel];
		for (int i = 0; i < nbLabel; i++) 
		{
			maxValues[i] = Float.NEGATIVE_INFINITY;
			posMax[i] = new Point(-1, -1);
		}
		
		// iterate on image pixels
		int width 	= labelImage.getWidth();
		int height 	= labelImage.getHeight();
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				
				// do not process pixels that do not belong to any particle
				if (label == 0)
					continue;

				int index = labelIndices.get(label);
				
				// update values and positions
				float value = valueImage.getf(x, y);
				if (value > maxValues[index]) 
				{
					posMax[index].setLocation(x, y);
					maxValues[index] = value;
				}
			}
		}
				
		return posMax;
	}

	/**
	 * For each label, finds the position of the point belonging to label region
	 * defined by <code>labelImage</code> and with minimal value in intensity
	 * image <code>valueImage</code>.
	 * 
	 * @param valueImage
	 *            the intensity image containing values to compare
	 * @param labelImage
	 *            the intensity image containing label of each pixel
	 * @param labels
	 *            the list of labels in the label image
	 * @return the position of minimum value in intensity image for each label
	 */
	public static final Point[] findPositionOfMinValues(ImageProcessor valueImage, 
			ImageProcessor labelImage, int[] labels)
	{
		// Create associative map between each label and its index
		Map<Integer,Integer> labelIndices = mapLabelIndices(labels);
		
		// Init Position and value of minimum for each label
		int nbLabel = labels.length;
		Point[] posMax 	= new Point[nbLabel];
		float[] maxValues = new float[nbLabel];
		for (int i = 0; i < nbLabel; i++) 
		{
			maxValues[i] = Float.POSITIVE_INFINITY;
			posMax[i] = new Point(-1, -1);
		}
		
		// iterate on image pixels
		int width 	= labelImage.getWidth();
		int height 	= labelImage.getHeight();
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				int label = (int) labelImage.getf(x, y);
				
				// do not process pixels that do not belong to any particle
				if (label == 0)
					continue;

				int index = labelIndices.get(label);
				
				// update values and positions
				float value = valueImage.getf(x, y);
				if (value < maxValues[index]) 
				{
					posMax[index].setLocation(x, y);
					maxValues[index] = value;
				}
			}
		}
				
		return posMax;
	}

}// end class LabelImages
