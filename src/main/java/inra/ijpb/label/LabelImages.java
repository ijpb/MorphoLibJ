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

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PlotWindow;
import ij.gui.PointRoi;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.ResultsTable;
import ij.plugin.Selection;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatPolygon;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.data.Cursor2D;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.data.image.ImageUtils;
import inra.ijpb.label.conncomp.FloodFillRegionComponentsLabeling;
import inra.ijpb.label.conncomp.FloodFillRegionComponentsLabeling3D;
import inra.ijpb.label.distmap.ChamferDistanceTransform3DFloat;
import inra.ijpb.label.distmap.ChamferDistanceTransform3DShort;
import inra.ijpb.label.distmap.DistanceTransform3D;
import inra.ijpb.label.distmap.LabelDilation2DShort;
import inra.ijpb.label.distmap.LabelDilation3DShort;
import inra.ijpb.label.edit.FindAllLabels;
import inra.ijpb.label.edit.ReplaceLabelValues;

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
	 * Returns the binary image with value equals to
	 * {@code true} only when the corresponding value in the input image equals {@code label}.
	 * 
	 * @param image
	 *            the input label map
	 * @param label
	 *            the label of the region to binarize. Using a value equal to
	 *            zero binarizes the background.
	 * @return a binary image of the selected label.
	 */
	public static final ImageProcessor binarize(ImageProcessor image, int label)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		ByteProcessor result = new ByteProcessor( sizeX, sizeY );
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				// process only specified label
				int val = (int) image.getf(x, y);
				if (val == label)
				{
					result.set(x, y, 255);
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
     * Computes the number of pixels (or voxels) composing each regions in the
     * 2D or 3D label image.
     * 
     * 
     * @param image
     *            a label image (2D or 3D)
     * @param labels
     *            the array of label indices to process
     * @return an array the same size as labels, containing the number of pixels
     *         / voxels within each region
     */
	public static final int[] pixelCount(ImagePlus image, int[] labels)
	{
        return image.getStackSize() == 1
                ? pixelCount(image.getProcessor(), labels)
                : voxelCount(image.getStack(), labels);
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
	
	/**
	 * Find largest label (by number of pixels/voxels) in input image
	 * @param imagePlus input image
	 * @return value of the largest label in the input label image
	 */
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
	 * #see inra.ijpb.label.edit.FindAllLabels
	 * 
	 * @param image
	 *            an instance of ImagePlus containing a label image
	 * @return the list of unique labels present in image (without background)
	 */
    public final static int[] findAllLabels(ImagePlus image) 
    {
        return new FindAllLabels().process(image);
    }

    /**
     * Returns the set of unique labels existing in the given stack, excluding 
     * the value zero (used for background).
	 * 
     * #see inra.ijpb.label.edit.FindAllLabels
     * 
	 * @param image
	 *            a 3D label image
	 * @return the list of unique labels present in image (without background)
     */
    public final static int[] findAllLabels(ImageStack image) 
    {
        return new FindAllLabels().process(image);
    }

    /**
     * Returns the set of unique labels existing in the given image, excluding 
     * the value zero (used for background).
	 * 
     * #see inra.ijpb.label.edit.FindAllLabels
     * 
	 * @param image
	 *            a label image
	 * @return the list of unique labels present in image (without background)
     */
    public final static int[] findAllLabels(ImageProcessor image)
    {
        return new FindAllLabels().process(image);
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
		new ReplaceLabelValues().process(image, labels, newLabel);
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
		new ReplaceLabelValues().process(image, labels, newLabel);
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
		new ReplaceLabelValues().process(image, labels, newLabel);
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
	public static final void replaceLabels(ImageStack image, float[] labels, float newLabel)
	{
		new ReplaceLabelValues().process(image, labels, newLabel);
	}

 
    /**
	 * Ensures that the labels in the given label image range from 1 to Lmax.
	 * 
	 * @param imagePlus
	 *            the instance of ImagePlus containing the label image
	 */
	public static final void remapLabels(ImagePlus imagePlus)
	{
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			remapLabels(image);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			remapLabels(image);
		}
		
	}

	/**
	 * Ensures that the labels in the given label image range from 1 to Lmax.
	 *  
	 * @param image the label image
	 */
	public static final void remapLabels(ImageProcessor image)
	{
		int[] labels = findAllLabels(image);
		HashMap<Integer, Integer> map = mapLabelIndices(labels);
		
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int label = (int) image.getf(x, y);
				if (label != 0)
				{
					image.setf(x, y, map.get(label) + 1);
				}
			}
		}
	}
	
	/**
	 * Ensures that the labels in the given label image range from 1 to Lmax.
	 *  
	 * @param image the 3D label image
	 */
	public static final void remapLabels(ImageStack image)
	{
		int[] labels = findAllLabels(image);
		HashMap<Integer, Integer> map = mapLabelIndices(labels);
		
		for (int z = 0; z < image.getSize(); z++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				for (int x = 0; x < image.getWidth(); x++)
				{
					int label = (int) image.getVoxel(x, y, z);
					if (label != 0)
					{
						image.setVoxel(x, y, z, map.get(label) + 1);
					}
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
			if (!(result instanceof ColorProcessor))
			    result.setLut(image.getLut());
			resultPlus = new ImagePlus(newName, result);
		}
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = keepLabels(image, labels);
			result.setColorModel(image.getColorModel());
            resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.copyScale(imagePlus);
		resultPlus.setDisplayRange(imagePlus.getDisplayRangeMin(), imagePlus.getDisplayRangeMax());
        
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
     *            a label image (2D or 3D)
     * @param values
     *            a set of values associated to each unique label
     * @return a new ImagePlus containing for each pixel or voxel, either the value
     *         associated to the corresponding label, or 0 if the pixel is
     *         background
     */
	public static final ImagePlus applyLut(ImagePlus labelImage, double[] values)
	{
        ImagePlus resultPlus;
        String newName = labelImage.getShortTitle() + "-applyLut";
        
        // Dispatch to appropriate function depending on dimension
        if (labelImage.getStackSize() == 1) 
        {
            // process planar image
            ImageProcessor image = labelImage.getProcessor();
            ImageProcessor result = applyLut(image, values);
            resultPlus = new ImagePlus(newName, result);
        }
        else 
        {
            // process image stack
            ImageStack image = labelImage.getStack();
            ImageStack result = applyLut(image, values);
            resultPlus = new ImagePlus(newName, result);
        }
        
        resultPlus.copyScale(labelImage);
        
        return resultPlus;
	}
	
	/**
	 * Applies the given Look-up table to the input 2D label image.
	 * 
	 * @param labelImage
	 *            a label image
	 * @param values
	 *            a set of values associated to each unique label
	 * @return a new FloatProcessor containing for each pixel, either the value
	 *         associated to the corresponding label, or 0 if the pixel is
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
	 * Applies the given Look-up table to the input 3D label image.
	 * 
	 * @param labelImage
	 *            a 3D label image
	 * @param values
	 *            a set of values associated to each unique label
	 * @return a new 3D image containing for each pixel, either the value
	 *         associated to the corresponding label, or 0 if the voxel is
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
     * Computes the labels in the binary 2D or 3D image contained in the given
     * ImagePlus, and computes the maximum label to set up the display range of
     * the resulting ImagePlus.
     * 
     * @param imagePlus
     *            contains the 3D binary image stack
     * @param regionLabel
     *            the label of the region to process, that can be the background
     *            (value 0)
     * @param conn
     *            the connectivity, either 4 or 8 for planar images, or 6 or 26
     *            for 3D images
     * @param bitDepth
     *            the number of bits used to create the result image (8, 16 or
     *            32)
     * @return an ImagePlus containing the label of each connected component.
     * @throws RuntimeException
     *             if the number of labels reaches the maximum number that can
     *             be represented with this bitDepth
     * 
     * @see inra.ijpb.label.conncomp.FloodFillRegionComponentsLabeling
     * @see inra.ijpb.label.conncomp.FloodFillRegionComponentsLabeling3D
     * @see inra.ijpb.morphology.FloodFill
     */
    public final static ImagePlus regionComponentsLabeling(ImagePlus imagePlus, 
            int regionLabel, int conn, int bitDepth)
    {
        ImagePlus labelPlus;
    
        // Dispatch processing depending on input image dimensionality
        if (imagePlus.getStackSize() == 1)
        {
            ImageProcessor labels = regionComponentsLabeling(imagePlus.getProcessor(),
                    regionLabel, conn, bitDepth);
            labelPlus = new ImagePlus("Labels", labels);
        }
        else 
        {
            ImageStack labels = regionComponentsLabeling(imagePlus.getStack(),
                    regionLabel, conn, bitDepth);
            labelPlus = new ImagePlus("Labels", labels);
        }

        // setup display range to show largest label as white
        double nLabels = ImageUtils.findMaxValue(labelPlus);
        labelPlus.setDisplayRange(0, nLabels);
        return labelPlus;
    }
    
    /**
     * Computes the labels of the connected components in the given planar
     * binary image. The type of result is controlled by the bitDepth option.
     * 
     * Uses a Flood-fill type algorithm.
     * 
     * @param image
     *            contains the binary image (any type is accepted)
     * @param regionLabel
     *            the label of the region to process, that can be the background
     *            (value 0)
     * @param conn
     *            the connectivity, either 4 or 8
     * @param bitDepth
     *            the number of bits used to create the result image (8, 16 or
     *            32)
     * @return a new instance of ImageProcessor containing the label of each
     *         connected component.
     * @throws RuntimeException
     *             if the number of labels reaches the maximum number that can
     *             be represented with this bitDepth
     *             
     * @see inra.ijpb.label.conncomp.FloodFillRegionComponentsLabeling     
     * @see inra.ijpb.binary.conncomp.ConnectedComponentsLabeling
     */
    public final static ImageProcessor regionComponentsLabeling(ImageProcessor image,
            int regionLabel, int conn, int bitDepth) 
    {
        FloodFillRegionComponentsLabeling algo = new FloodFillRegionComponentsLabeling(conn, bitDepth);
        DefaultAlgoListener.monitor(algo);
        return algo.computeLabels(image, regionLabel);
    }

    /**
     * Computes the labels of the connected components in the given 3D binary
     * image. The type of result is controlled by the bitDepth option.
     * 
     * Uses a Flood-fill type algorithm.
     * 
     * @param image
     *            contains the 3D binary image (any type is accepted)
     * @param regionLabel
     *            the label of the region to process, that can be the background
     *            (value 0)
     * @param conn
     *            the connectivity, either 6 or 26
     * @param bitDepth
     *            the number of bits used to create the result stack (8, 16 or
     *            32)
     * @return a new instance of ImageStack containing the label of each
     *         connected component.
     * @throws RuntimeException
     *             if the number of labels reaches the maximum number that can
     *             be represented with this bitDepth
     *             
     * @see inra.ijpb.label.conncomp.FloodFillRegionComponentsLabeling3D     
     * @see inra.ijpb.binary.conncomp.ConnectedComponentsLabeling3D     
     */
    public final static ImageStack regionComponentsLabeling(ImageStack image,
            int regionLabel, int conn, int bitDepth)
    {
        FloodFillRegionComponentsLabeling3D algo = new FloodFillRegionComponentsLabeling3D(conn, bitDepth);
        DefaultAlgoListener.monitor(algo);
        return algo.computeLabels(image, regionLabel);
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
     * Merge labels selected by freehand or point tool. Labels are merged
     * in place (i.e., the input image is modified). Zero-value label is 
     * merged only if its neighbors contains two different labels.
     *
     * @param labelImage  label image to modify
     * @param roi  selection indicating the labels to merge
     * @param conn the connectivity to check for neighbors of background labels
     * @param verbose  option to write in the log window the labels merged
     */
    public static final void mergeLabelsWithGap(
            final ImagePlus labelImage,
            final Roi roi,
            final int conn, 
            final boolean verbose )
    {
        if( roi == null )
        {
            IJ.showMessage( "Please select some labels to merge using the "
                    + "freehand or point selection tool." );
            return;
        }

        // get labels selected by ROI
        final ArrayList<Float> list = getSelectedLabels(labelImage, roi);
        if (list.size() <= 1)
        {
            IJ.error("Please select two or more different labels to merge");
        }
        
        // the label value to convert to
        float finalValue = list.get( 0 );
        
        // convert ArrayList to array
        float[] labelArray = new float[list.size()];
        int i = 0;
        for (Float f : list)
            labelArray[i++] = f != null ? f : Float.NaN;

        // create log message
        String sLabels = new String("" + (long) labelArray[0]);
        for (int j = 1; j < labelArray.length; j++)
            sLabels += ", " + (long) labelArray[j];
        if (verbose)
            IJ.log("Merging label(s) " + sLabels + " to label "
                    + (long) finalValue + " filling gap with conn " + conn);
        
        LabelImages.mergeLabelsWithGap(labelImage, labelArray, finalValue, conn);
    } // end method mergeLabelsWithDams

    /**
     * Merge several regions identified by their label, filling the gap between
     * former regions. Labels are merged in place (i.e., the input image is
     * modified). The background pixels or voxels are merged only if they are
     * neighbor of at least two regions to be merged, and of no other region.
     * 
     * @param imagePlus
     *            an ImagePlus containing a 3D label image
     * @param labels
     *            the list of labels to replace
     * @param newLabel
     *            the new value for labels
     * @param conn
     *            the connectivity to check neighbors of background pixels
     */
    public static final void mergeLabelsWithGap(ImagePlus imagePlus, float[] labels, float newLabel, int conn) 
    {
        // Dispatch to appropriate function depending on dimension
        if (imagePlus.getStackSize() == 1) 
        {
            // process planar image
            ImageProcessor image = imagePlus.getProcessor();
            mergeLabelsWithGap(image, labels, newLabel, conn);
        } 
        else 
        {
            // process image stack
            ImageStack image = imagePlus.getStack();
            mergeLabelsWithGap(image, labels, newLabel, conn);
        }
    }

    /**
     * Merge several regions identified by their label, filling the gap between
     * former regions. Labels are merged in place (i.e., the input image is
     * modified). The background pixels are merged only if they are neighbor of
     * at least two regions to be merged, and of no other region.
     * 
     * @param image
     *            a label planar image
     * @param labels
     *            the list of labels to replace
     * @param newLabel
     *            the new value for labels
     * @param conn
     *            the connectivity to check neighbors of background pixels
     */
    public static final void mergeLabelsWithGap(ImageProcessor image, float[] labels, float newLabel, int conn)
    {
        // get image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // convert array to tree (to accelerate search)
        TreeSet<Float> labelSet = new TreeSet<Float>();
        for (int i = 0; i < labels.length; i++)
        {
            labelSet.add(labels[i]);
        }
        
        // determines the shift to compute neighbor coordinates
        int[][] shifts;
        if (conn == 4)
        {
            shifts = new int[][] {{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        }
        else if (conn == 8)
        {
            shifts = new int[][] { 
                { -1, -1 }, { 0, -1 }, { 1, -1 },   // previous line
                { -1, 0 }, { 1, 0 },                // current line
                { -1, 1 }, { 0, 1 }, { 1, 1 } };    // next line
        }
        else
        {
            throw new IllegalArgumentException("Connectivity value should be either 4 or 8.");
        }
        
        // create structure to store the boundary pixels
        ArrayList<Cursor2D> boundaryPixels = new ArrayList<Cursor2D>();
        
        // process background pixels in-between two or more labels
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                // focus on background pixels
                if (image.getf(x, y) != 0)
                    continue;

                // extract the neighbor labels
                ArrayList<Float> neighborLabels = new ArrayList<Float>(shifts.length);
                for (int[] shift : shifts)
                {
                    int x2 = x + shift[0];
                    if (x2 < 0 || x2 >= sizeX) continue;

                    int y2 = y + shift[1];
                    if (y2 < 0 || y2 >= sizeY) continue;

                    float value2 = image.getf(x2, y2);
                    if (value2 == 0) continue;

                    if (!neighborLabels.contains(value2))
                    {
                        neighborLabels.add(value2);
                    }
                }

                // count number of neighbors
                if (neighborLabels.size() > 1 && labelSet.containsAll(neighborLabels))
                    boundaryPixels.add(new Cursor2D(x, y));
            }
        }
        
        // replace label value of all regions
        replaceLabels(image, labels, newLabel);

        for (Cursor2D c : boundaryPixels)
            image.setf(c.getX(), c.getY(), newLabel);          
    }

    /**
     * Merge several regions identified by their label, filling the gap between
     * former regions. Labels are merged in place (i.e., the input image is
     * modified). The background voxels are merged only if they are neighbor of
     * at least two regions to be merged, and of no other region.
     *  
     * @param image a 3D label image
     * @param labels the list of labels to replace 
     * @param newLabel the new value for labels 
     * @param conn the connectivity to check neighbors of background pixels
     */
    public static final void mergeLabelsWithGap(ImageStack image, float[] labels, float newLabel, int conn)
    {
        // get image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        // convert array to tree (to accelerate search)
        TreeSet<Float> labelSet = new TreeSet<Float>();
        for (int i = 0; i < labels.length; i++)
        {
            labelSet.add(labels[i]);
        }
        
        // determines the shift to compute neighbor coordinates
        int[][] shifts;
        if (conn == 6)
        {
            shifts = new int[][] {{0, 0, -1}, {0, -1, 0}, {-1, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        }
        else if (conn == 26)
        {
            shifts = new int[][] {
                // previous slice (dz = -1)
                {-1, -1, -1}, {0, -1, -1}, {1, -1, -1},   {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},   {-1, 1, -1}, {0, 1, -1}, {1, 1, -1},   
                // current slice (dz = 0)
                {-1, -1, 0}, {0, -1, 0}, {1, -1, 0},   {-1, 0, 0}, {1, 0, 0},   {-1, 1, 0}, {0, 1, 0}, {1, 1, 0},   
                // next slice (dz = +1 )
                {-1, -1, 1}, {0, -1, 1}, {1, -1, 1},   {-1, 0, 1}, {0, 0, 1}, {1, 0, 1},   {-1, 1, 1}, {0, 1, 1}, {1, 1, 1}};
        }
        else
        {
            throw new IllegalArgumentException("Connectivity value should be either 6 or 26.");
        }
        
        // create structure to store the boundary voxels
        ArrayList<Cursor3D> boundaryVoxels = new ArrayList<Cursor3D>();
        
        // process background pixels in-between two or more labels
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    // process only background voxels
                    if(image.getVoxel(x, y, z) != 0)
                        continue;

                    // extract the neighbor labels
                    ArrayList<Float> neighborLabels = new ArrayList<Float>(shifts.length);
                    for (int[] shift : shifts)
                    {
                        int x2 = x + shift[0];
                        if (x2 < 0 || x2 >= sizeX) continue;

                        int y2 = y + shift[1];
                        if (y2 < 0 || y2 >= sizeY) continue;

                        int z2 = z + shift[2];
                        if (z2 < 0 || z2 >= sizeZ) continue;

                        float value2 = (float) image.getVoxel(x2, y2, z2);
                        if (value2 == 0) continue;

                        if (!neighborLabels.contains(value2))
                        {
                            neighborLabels.add(value2);
                        }
                    }

                    // check if all neighbor labels are in the label set
                    if (neighborLabels.size() > 1 && labelSet.containsAll(neighborLabels))
                        boundaryVoxels.add(new Cursor3D(x, y, z));
                }
            }
        }
        
        // replace label value of all regions
        replaceLabels(image, labels, newLabel);
        
        for (Cursor3D c : boundaryVoxels)
          image.setVoxel(c.getX(), c.getY(), c.getZ(), newLabel);          
    }

    /**
	 * Remove labels selected by freehand or point ROIs (in place).
	 *
	 * @param labelImage  input label image
	 * @param roi  FreehandRoi or PointRoi with selected labels
	 * @param verbose  flag to print deleted labels in log window
	 */
	public static final void removeLabels(
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
	 * Computes the 3D distance map from an image of labels.
	 * 
	 * Distance is computed for each label voxel, as the chamfer distance to the
	 * nearest voxel with a different value.
	 * 
	 * @param image
	 *            the input 3D label image
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image)
	{
		DistanceTransform3D algo = new ChamferDistanceTransform3DFloat(ChamferMask3D.BORGEFORS);
		return algo.distanceMap(image);
	}
	
	/**
	 * <p>
	 * Computes the 3D distance map from an image of labels, by specifying
	 * the chamfer mask and the normalization.
	 * </p>
	 * 
	 * <p>
	 * Distance is computed for each foreground (white) pixel, as the chamfer
	 * distance to the nearest background (black) pixel. Result is given in a
	 * new instance of FloatProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input binary or label image
	 * @param mask
	 *            the chamfer mask used to propagate distances
	 * @param floatingPoint
	 *            indicates if the computation should be performed using
	 *            floating point computation
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image,
			ChamferMask3D mask, boolean floatingPoint, boolean normalize) 
	{
		DistanceTransform3D algo = floatingPoint 
				? new ChamferDistanceTransform3DFloat(mask, normalize)
				: new ChamferDistanceTransform3DShort(mask, normalize);
		return algo.distanceMap(image);
	}
	
	/**
	 * Computes the distance map from a 3D image of labels
	 * 
	 * Distance is computed for each label voxel, as the chamfer distance to the
	 * nearest voxel with a different value.
	 * 
	 * @param image
	 *            the input 3D image of labels
	 * @param weights
	 *            an array of chamfer weights, with at least three values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image,
			short[] weights, boolean normalize)
	{
		ChamferMask3D mask = ChamferMask3D.fromWeights(weights);
		DistanceTransform3D	algo = new ChamferDistanceTransform3DShort(mask, normalize);
			
		return algo.distanceMap(image);
	}
	
	/**
	 * Computes the distance map from a 3D image of labels
	 * 
	 * Distance is computed for each label voxel, as the chamfer distance to the
	 * nearest voxel with a different value.
	 * 
	 * @param image
	 *            the input 3D image of labels
	 * @param weights
	 *            an array of chamfer weights, with at least three values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image, 
			float[] weights, boolean normalize)
	{
		ChamferMask3D mask = ChamferMask3D.fromWeights(weights);
		DistanceTransform3D	algo = new ChamferDistanceTransform3DFloat(mask, normalize);
		return algo.distanceMap(image);
	}
	
	/**
	 * Applies a constrained dilation to each region in the 3D label map:
	 * <ul>
	 * <li>The dilation of each region is constrained by the other regions;</li>
	 * <li>The dilation extent is limited by the specified distance (in voxel
	 * unit)</li>
	 * </ul>
	 * 
	 * @param imagePlus
	 *            the image of regions to process
	 * @param distMax
	 *            the maximum distance used for dilation
	 * @return a new label map of regions, where regions are dilated only over
	 *         the background.
	 */
	public static final ImagePlus dilateLabels(ImagePlus imagePlus, double distMax)
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-dilated";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = dilateLabels(image, distMax);
			resultPlus = new ImagePlus(newName, result);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = dilateLabels(image, distMax);
			resultPlus = new ImagePlus(newName, result);
		}
		
        // update display range
    	double min = imagePlus.getDisplayRangeMin();
    	double max = imagePlus.getDisplayRangeMax();
    	resultPlus.setDisplayRange(min, max);
    	
        // propagate spatial calibration
    	resultPlus.copyScale(imagePlus);
		return resultPlus;
	}
	
	/**
	 * Applies a constrained dilation to each region in the label map:
	 * <ul>
	 * <li>The dilation of each region is constrained by the other regions;</li>
	 * <li>The dilation extent is limited by the specified distance (in pixel
	 * unit)</li>
	 * </ul>
	 * 
	 * @param labelMap
	 *            the image of regions to process
	 * @param distMax
	 *            the maximum distance used for dilation
	 * @return a new label map of regions, where regions are dilated only over
	 *         the background.
	 */
	public static final ImageProcessor dilateLabels(ImageProcessor labelMap, double distMax)
	{
		LabelDilation2DShort algo = new LabelDilation2DShort(ChamferMask2D.CHESSKNIGHT);
		ImageProcessor result = algo.process(labelMap, distMax);
		result.setLut(labelMap.getLut());
		return result;
	}
	
	/**
	 * Applies a constrained dilation to each region in the 3D label map:
	 * <ul>
	 * <li>The dilation of each region is constrained by the other regions;</li>
	 * <li>The dilation extent is limited by the specified distance (in voxel
	 * unit)</li>
	 * </ul>
	 * 
	 * @param labelMap
	 *            the image of regions to process
	 * @param distMax
	 *            the maximum distance used for dilation
	 * @return a new label map of regions, where regions are dilated only over
	 *         the background.
	 */
	public static final ImageStack dilateLabels(ImageStack labelMap, double distMax)
	{
		LabelDilation3DShort algo = new LabelDilation3DShort(ChamferMask3D.SVENSSON_3_4_5_7);
		ImageStack result = algo.process(labelMap, distMax);
		result.setColorModel(labelMap.getColorModel());
		return result;
	}
	
	/**
	 * Get list of selected labels in label image. Labels are selected by
	 * any selection tool in the current slice of the input label image.
	 * Zero-value label is skipped.
	 *
	 * @param labelImage  label image
	 * @param roi  FreehandRoi or PointRoi with selected labels
	 * @return list of selected labels
	 */
	public static final ArrayList<Float> getSelectedLabels(
			final ImagePlus labelImage,
			final Roi roi )
	{
		final ArrayList<Float> list = new ArrayList<Float>();
		labelImage.setRoi( roi );
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
		// 1 pixel-thick lines
		else if( roi.isLine() && roi.getStrokeWidth() <= 1 )
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
		else if( roi.getType() == Roi.FREELINE  )// freehand thicker lines
		{
			final int width = Math.round( roi.getStrokeWidth() );
			FloatPolygon p = roi.getFloatPolygon();
			int n = p.npoints;
			final ImageProcessor ip = labelImage.getProcessor();
			double x1, y1;
			double x2 = p.xpoints[0]-(p.xpoints[1]-p.xpoints[0]);
			double y2 = p.ypoints[0]-(p.ypoints[1]-p.ypoints[0]);
			for( int i=0; i<n; i++ )
			{
				x1 = x2;
				y1 = y2;
				x2 = p.xpoints[i];
				y2 = p.ypoints[i];

				double dx = x2-x1;
				double dy = y1-y2;
				double length = (float)Math.sqrt(dx*dx+dy*dy);
				dx /= length;
				dy /= length;
				double x = x2-dy*width/2.0;
				double y = y2-dx*width/2.0;

				int n2 = width;

				do {
					float value = ip.getf( (int) (x+0.5), (int) (y+0.5) );
					if( Float.compare( 0f, value ) != 0 &&
							list.contains( value ) == false )
						list.add( (float) value );
					x += dy;
					y += dx;
				} while (--n2>0);
			}
		}
		// for regular rectangles
		else if ( roi.getType() == Roi.RECTANGLE && roi.getCornerDiameter() == 0 )
		{
			final Rectangle rect = roi.getBounds();

			final int x0 = rect.x;
			final int y0 = rect.y;

			final int lastX = x0 + rect.width;
			final int lastY = y0 + rect.height;
			final ImageProcessor ip = labelImage.getProcessor();
			for( int x = x0; x < lastX; x++ )
				for( int y = y0; y < lastY; y++ )
				{
					float value = ip.getf( x, y );
					if( Float.compare( 0f, value ) != 0 &&
							list.contains( value ) == false )
						list.add( (float) value );
				}
		}
		else // for the rest of ROIs we get ALL points inside the ROI
		{
			final ShapeRoi shapeRoi = roi.isLine() ? new ShapeRoi( Selection.lineToArea( roi ) )
					: new ShapeRoi( roi );
			final Rectangle rect = shapeRoi.getBounds();

			int lastX = rect.x + rect.width ;
			if( lastX >= labelImage.getWidth() )
				lastX = labelImage.getWidth() - 1;
			int lastY = rect.y + rect.height;
			if( lastY >= labelImage.getHeight() )
				lastY = labelImage.getHeight() - 1;
			int firstX = Math.max( rect.x, 0 );
			int firstY = Math.max( rect.y, 0 );

			final ImageProcessor ip = labelImage.getProcessor();
			// create equivalent binary image to speed up the checking
			// of each pixel belonging to the shape
			ByteProcessor bp = new ByteProcessor( rect.width, rect.height );
			bp.setValue( 255 );
			shapeRoi.setLocation( 0 , 0 );
			bp.fill( shapeRoi );

			for( int x = firstX, rectX = 0; x < lastX; x++, rectX++ )
				for( int y = firstY, rectY = 0; y < lastY; y++, rectY++ )
					if( bp.getf(rectX, rectY) > 0 )
					{
						float value = ip.getf( x, y );
						if( Float.compare( 0f, value ) != 0 &&
								list.contains( value ) == false )
							list.add( (float) value );
					}
		}
		return list;
	}
	/**
	 * Get the total overlap between two label images (source and target).
	 * <p>
	 * Total Overlap (for all regions) $TO = \frac{ \sum_r{|S_r \cap T_r|} }{ \sum_r{|T_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return total overlap value or -1 if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getTotalOverlap(
			ImageProcessor sourceImage,
			ImageProcessor targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() )
			return -1;
		double intersection = 0;
		double numPixTarget = 0;
		// calculate the pixel to pixel intersection
	    for( int i = 0; i < sourceImage.getWidth(); i++ )
	    	for( int j = 0; j < sourceImage.getHeight(); j++ )
	    	{
	    		if( sourceImage.getf( i, j ) > 0 ) // skip label 0 (background)
	    		{
	    			if( sourceImage.getf( i, j ) == targetImage.getf( i, j ) )
	    				intersection ++;
	    		}
	    		if( targetImage.getf( i, j ) > 0 )
	    			numPixTarget ++;
	    	}
	    // return the total overlap
	    return intersection / numPixTarget;
	}
	/**
	 * Get the target overlap between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * Target Overlap (for individual label regions r) $TO_r = \frac{ |S_r \cap T_r| }{ |T_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return target overlap per label or null if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getTargetOverlapPerLabel(
			ImageProcessor sourceImage,
			ImageProcessor targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() )
			return null;

		int[] sourceLabels = findAllLabels( sourceImage );
		double[] intersection = new double[ sourceLabels.length ];
		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> sourceLabelIndices = mapLabelIndices( sourceLabels );

	    int[] targetLabels = findAllLabels( targetImage );
		int[] numPixTarget = pixelCount( targetImage, targetLabels );

		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> targetLabelIndices = mapLabelIndices( targetLabels );

		// calculate the pixel to pixel intersection
	    for( int i = 0; i < sourceImage.getWidth(); i++ )
	    	for( int j = 0; j < sourceImage.getHeight(); j++ )
	    		if( sourceImage.getf( i, j ) > 0 ) // skip label 0 (background)
	    		{
	    			if( sourceImage.getf( i, j ) == targetImage.getf( i, j ) )
	    				intersection[ sourceLabelIndices.get( (int) sourceImage.getf( i, j ) ) ] ++;
	    		}
	    // return the target overlap
	    for( int i = 0; i < intersection.length; i ++ )
	    	intersection[ i ] = targetLabelIndices.get( sourceLabels[ i ] ) != null ?
	    			intersection[ i ] / numPixTarget[ targetLabelIndices.get( sourceLabels[ i ] ) ] : 0;
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < sourceLabels.length; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( sourceLabels[i] ));
			table.addValue("TargetOverlap", intersection[i]);
		}
	    return table;
	}
	/**
	 * Get the total overlap between two label images (source and target).
	 * <p>
	 * Total Overlap (for all regions) $TO = \frac{ \sum_r{|S_r \cap T_r|} }{ \sum_r{|T_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return total overlap value or -1 if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getTotalOverlap(
			ImageStack sourceImage,
			ImageStack targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() )
			return -1;
		double intersection = 0;
		double numPixTarget = 0;
		// calculate the pixel to pixel intersection
		for( int k = 0; k < sourceImage.getSize(); k ++ )
		{
			final ImageProcessor ls = sourceImage.getProcessor( k+1 );
			final ImageProcessor lt = targetImage.getProcessor( k+1 );
			for( int i = 0; i < sourceImage.getWidth(); i++ )
				for( int j = 0; j < sourceImage.getHeight(); j++ )
				{
					if( ls.getf( i, j ) > 0 ) // skip label 0 (background)
		    		{
		    			if( ls.getf( i, j ) == lt.getf( i, j ) )
		    				intersection ++;
		    		}
					if( lt.getf( i, j ) > 0 )
						numPixTarget ++;
				}
		}
	    // return the total overlap
	    return intersection / numPixTarget;
	}
	/**
	 * Get the total overlap between two label images (source and target).
	 * <p>
	 * Total Overlap (for all regions) $TO = \frac{ \sum_r{|S_r \cap T_r|} }{ \sum_r{|T_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return total overlap value or -1 if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getTotalOverlap(
			ImagePlus sourceImage,
			ImagePlus targetImage )
	{
		return getTotalOverlap( sourceImage.getImageStack(), targetImage.getImageStack() );
	}

	/**
	 * Get the target overlap between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * Target Overlap (for individual label regions r) $TO_r = \frac{ |S_r \cap T_r| }{ |T_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return target overlap per label or null if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getTargetOverlapPerLabel(
			ImageStack sourceImage,
			ImageStack targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() )
			return null;

		int[] sourceLabels = findAllLabels( sourceImage );
		double[] intersection = new double[ sourceLabels.length ];
		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> sourceLabelIndices = mapLabelIndices( sourceLabels );

	    int[] targetLabels = findAllLabels( targetImage );
		int[] numPixTarget = voxelCount( targetImage, targetLabels );

		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> targetLabelIndices = mapLabelIndices( targetLabels );
	    // calculate the pixel to pixel intersection
	 	for( int k = 0; k < sourceImage.getSize(); k ++ )
	 	{
	 		final ImageProcessor ls = sourceImage.getProcessor( k+1 );
			final ImageProcessor lt = targetImage.getProcessor( k+1 );
	 		for( int i = 0; i < sourceImage.getWidth(); i++ )
	 			for( int j = 0; j < sourceImage.getHeight(); j++ )
	 				if( ls.getf( i, j ) > 0 ) // skip label 0 (background)
	 				{
	 					if( ls.getf( i, j ) == lt.getf( i, j ) )
	 						intersection[ sourceLabelIndices.get( (int) ls.getf( i, j ) ) ] ++;
	 				}
	 	}
	    // return the target overlap
	    for( int i = 0; i < intersection.length; i ++ )
	    	intersection[ i ] = targetLabelIndices.get( sourceLabels[ i ] ) != null ?
	    			intersection[ i ] / numPixTarget[ targetLabelIndices.get( sourceLabels[ i ] ) ] : 0;
		// create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < sourceLabels.length; i++) {
			table.incrementCounter();
			table.addLabel(Integer.toString( sourceLabels[i] ));
			table.addValue("TargetOverlap", intersection[i]);
		}
	    return table;
	}
	/**
	 * Get the target overlap between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * Target Overlap (for individual label regions r) $TO_r = \frac{ |S_r \cap T_r| }{ |T_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return target overlap per label or null if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getTargetOverlapPerLabel(
			ImagePlus sourceImage,
			ImagePlus targetImage )
	{
		return getTargetOverlapPerLabel( sourceImage.getImageStack(), targetImage.getImageStack() );
	}
	/**
	 * Get the Jaccard index (intersection over union overlap) between
	 * two label images.
	 * @param labelImage1 first label image
	 * @param labelImage2 second label image
	 * @return Jaccard index value or -1 if error
	 * @see <a href="https://en.wikipedia.org/wiki/Jaccard_index">https://en.wikipedia.org/wiki/Jaccard_index</a>
	 */
	public static final double getJaccardIndex(
			ImageProcessor labelImage1,
			ImageProcessor labelImage2 )
	{
		if( labelImage1.getWidth() != labelImage2.getWidth() ||
				labelImage1.getHeight() != labelImage2.getHeight() )
			return -1;
		double intersection = 0;
		double numPix1 = 0;
		double numPix2 = 0;
		// calculate the pixel to pixel intersection
	    for( int i = 0; i < labelImage1.getWidth(); i++ )
	    	for( int j = 0; j < labelImage1.getHeight(); j++ )
	    	{
	    		if( labelImage1.getf( i, j ) > 0 ) // skip label 0 (background)
	    		{
	    			numPix1 ++;
	    			if( labelImage1.getf( i, j ) == labelImage2.getf( i, j ) )
	    				intersection ++;
	    		}
	    		if( labelImage2.getf( i, j ) > 0 )
	    			numPix2 ++;
	    	}
	    // return the intersection over the union
	    return intersection / ( numPix1 + numPix2 - intersection );
	}
	/**
	 * Get the Jaccard index per label (intersection over union overlap) between
	 * two label images.
	 * @param labelImage1 reference label image
	 * @param labelImage2 label image to compare with
	 * @return Jaccard index per label in the reference image or null if error
	 * @see <a href="https://en.wikipedia.org/wiki/Jaccard_index">https://en.wikipedia.org/wiki/Jaccard_index</a>
	 */
	public static final ResultsTable getJaccardIndexPerLabel(
			ImageProcessor labelImage1,
			ImageProcessor labelImage2 )
	{
		if( labelImage1.getWidth() != labelImage2.getWidth() ||
				labelImage1.getHeight() != labelImage2.getHeight() )
			return null;

		int[] labels1 = findAllLabels( labelImage1 );
		int[] numPix1 = pixelCount( labelImage1, labels1 );
		double[] intersection = new double[ labels1.length ];
		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> labelIndices1 = mapLabelIndices( labels1 );

	    int[] labels2 = findAllLabels( labelImage2 );
		int[] numPix2 = pixelCount( labelImage2, labels2 );

		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> labelIndices2 = mapLabelIndices( labels2 );

		// calculate the pixel to pixel intersection
	    for( int i = 0; i < labelImage1.getWidth(); i++ )
	    	for( int j = 0; j < labelImage1.getHeight(); j++ )
	    		if( labelImage1.getf( i, j ) > 0 ) // skip label 0 (background)
	    		{
	    			if( labelImage1.getf( i, j ) == labelImage2.getf( i, j ) )
	    				intersection[ labelIndices1.get( (int) labelImage1.getf( i, j ) ) ] ++;
	    		}
	    // return the intersection over the union
	    for( int i = 0; i < intersection.length; i ++ )
	    {
	    	int num2 = labelIndices2.get( labels1[ i ] ) != null ? numPix2[ labelIndices2.get( labels1[ i ] ) ] : 0;
	    	intersection[ i ] /= ( numPix1[ i ] + num2 - intersection[ i ] );
	    }
	    // create data table
 		ResultsTable table = new ResultsTable();
 		for (int i = 0; i < labels1.length; i++) {
 			table.incrementCounter();
 			table.addLabel(Integer.toString( labels1[i] ));
 			table.addValue("JaccardIndex", intersection[i]);
 		}
 	    return table;
	}
	/**
	 * Get the Jaccard index (intersection over union overlap) between
	 * two label images.
	 * @param labelImage1 first label image
	 * @param labelImage2 second label image
	 * @return Jaccard index value or -1 if error
	 * @see <a href="https://en.wikipedia.org/wiki/Jaccard_index">https://en.wikipedia.org/wiki/Jaccard_index</a>
	 */
	public static final double getJaccardIndex(
			ImageStack labelImage1,
			ImageStack labelImage2 )
	{
		if( labelImage1.getWidth() != labelImage2.getWidth() ||
				labelImage1.getHeight() != labelImage2.getHeight() ||
				labelImage1.getSize() != labelImage2.getSize() )
			return -1;
		double intersection = 0;
		double numPix1 = 0;
		double numPix2 = 0;
		// calculate the pixel to pixel intersection
		for( int k = 0; k < labelImage1.getSize(); k ++ )
		{
			final ImageProcessor l1 = labelImage1.getProcessor( k+1 );
			final ImageProcessor l2 = labelImage2.getProcessor( k+1 );
			for( int i = 0; i < labelImage1.getWidth(); i++ )
				for( int j = 0; j < labelImage1.getHeight(); j++ )
				{
					if( l1.getf( i, j ) > 0 ) // skip label 0 (background)
		    		{
		    			numPix1 ++;
		    			if( l1.getf( i, j ) == l2.getf( i, j ) )
		    				intersection ++;
		    		}
					if( l2.getf( i, j ) > 0 )
						numPix2 ++;
				}
		}
	    // return the intersection over the union
	    return intersection / ( numPix1 + numPix2 - intersection );
	}
	/**
	 * Get the Jaccard index per label (intersection over union overlap) between
	 * two label images.
	 * @param labelImage1 reference label image
	 * @param labelImage2 label image to compare with
	 * @return Jaccard index per label in the reference image or null if error
	 * @see <a href="https://en.wikipedia.org/wiki/Jaccard_index">https://en.wikipedia.org/wiki/Jaccard_index</a>
	 */
	public static final ResultsTable getJaccardIndexPerLabel(
			ImageStack labelImage1,
			ImageStack labelImage2 )
	{
		if( labelImage1.getWidth() != labelImage2.getWidth() ||
				labelImage1.getHeight() != labelImage2.getHeight() )
			return null;

		int[] labels1 = findAllLabels( labelImage1 );
		int[] numPix1 = voxelCount( labelImage1, labels1 );
		double[] intersection = new double[ labels1.length ];
		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> labelIndices1 = mapLabelIndices( labels1 );

	    int[] labels2 = findAllLabels( labelImage2 );
		int[] numPix2 = voxelCount( labelImage2, labels2 );

		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> labelIndices2 = mapLabelIndices( labels2 );

		// calculate the voxel to voxel intersection
	    for( int k = 0; k < labelImage1.getSize(); k ++ )
		{
			final ImageProcessor l1 = labelImage1.getProcessor( k+1 );
			final ImageProcessor l2 = labelImage2.getProcessor( k+1 );
			for( int i = 0; i < labelImage1.getWidth(); i++ )
				for( int j = 0; j < labelImage1.getHeight(); j++ )
					if( l1.getf( i, j ) > 0 ) // skip label 0 (background)
		    		{
						if( l1.getf( i, j ) == l2.getf( i, j ) )
							intersection[ labelIndices1.get( (int) l1.getf( i, j ) ) ] ++;
		    		}
		}
	    // return the intersection over the union
	    for( int i = 0; i < intersection.length; i ++ )
	    {
	    	int num2 = labelIndices2.get( labels1[ i ] ) != null ? numPix2[ labelIndices2.get( labels1[ i ] ) ] : 0;
	    	intersection[ i ] /= ( numPix1[ i ] + num2 - intersection[ i ] );
	    }
	    // create data table
  		ResultsTable table = new ResultsTable();
  		for (int i = 0; i < labels1.length; i++) {
  			table.incrementCounter();
  			table.addLabel(Integer.toString( labels1[i] ));
  			table.addValue("JaccardIndex", intersection[i]);
  		}
  	    return table;
	}
	/**
	 * Get the Jaccard index (intersection over union overlap) between
	 * two label images.
	 * @param labelImage1 first label image
	 * @param labelImage2 second label image
	 * @return Jaccard index value or -1 if error
	 * @see <a href="https://en.wikipedia.org/wiki/Jaccard_index">https://en.wikipedia.org/wiki/Jaccard_index</a>
	 */
	public static final double getJaccardIndex(
			ImagePlus labelImage1,
			ImagePlus labelImage2 )
	{
		return getJaccardIndex( labelImage1.getImageStack(), labelImage2.getImageStack() );
	}
	/**
	 * Get the Jaccard index per label (intersection over union overlap) between
	 * two label images.
	 * @param labelImage1 reference label image
	 * @param labelImage2 label image to compare with
	 * @return Jaccard index per label in the reference image or null if error
	 * @see <a href="https://en.wikipedia.org/wiki/Jaccard_index">https://en.wikipedia.org/wiki/Jaccard_index</a>
	 */
	public static final ResultsTable getJaccardIndexPerLabel(
			ImagePlus labelImage1,
			ImagePlus labelImage2 )
	{
		return getJaccardIndexPerLabel( labelImage1.getImageStack(), labelImage2.getImageStack() );
	}
	/**
	 * Get the Dice coefficient between two label images.
	 * @param labelImage1 first label image
	 * @param labelImage2 second label image
	 * @return Dice coefficient value or -1 if error
	 * @see <a href="https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient">https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient</a>
	 */
	public static final double getDiceCoefficient(
			ImageProcessor labelImage1,
			ImageProcessor labelImage2 )
	{
		if( labelImage1.getWidth() != labelImage2.getWidth() ||
				labelImage1.getHeight() != labelImage2.getHeight() )
			return -1;
		double intersection = 0;
		double numPix1 = 0;
		double numPix2 = 0;
		// calculate the pixel to pixel intersection
	    for( int i = 0; i < labelImage1.getWidth(); i++ )
	    	for( int j = 0; j < labelImage1.getHeight(); j++ )
	    	{
	    		if( labelImage1.getf( i, j ) > 0 ) // skip label 0 (background)
	    		{
	    			numPix1 ++;
	    			if( labelImage1.getf( i, j ) == labelImage2.getf( i, j ) )
	    				intersection ++;
	    		}
	    		if( labelImage2.getf( i, j ) > 0 )
	    			numPix2 ++;
	    	}
	    // return the Dice coefficient
	    return 2.0 * intersection / (numPix1 + numPix2);
	}
	/**
	 * Get the Dice coefficient per label (intersection over union overlap) between
	 * two label images.
	 * @param labelImage1 reference label image
	 * @param labelImage2 label image to compare with
	 * @return Dice coefficient per label in the reference image or null if error
	 * @see <a href="https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient">https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient</a>
	 */
	public static final ResultsTable getDiceCoefficientPerLabel(
			ImageProcessor labelImage1,
			ImageProcessor labelImage2 )
	{
		if( labelImage1.getWidth() != labelImage2.getWidth() ||
				labelImage1.getHeight() != labelImage2.getHeight() )
			return null;

		int[] labels1 = findAllLabels( labelImage1 );
		int[] numPix1 = pixelCount( labelImage1, labels1 );
		double[] intersection = new double[ labels1.length ];
		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> labelIndices1 = mapLabelIndices( labels1 );

	    int[] labels2 = findAllLabels( labelImage2 );
		int[] numPix2 = pixelCount( labelImage2, labels2 );

		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> labelIndices2 = mapLabelIndices( labels2 );

		// calculate the pixel to pixel intersection
	    for( int i = 0; i < labelImage1.getWidth(); i++ )
	    	for( int j = 0; j < labelImage1.getHeight(); j++ )
	    		if( labelImage1.getf( i, j ) > 0 ) // skip label 0 (background)
	    		{
	    			if( labelImage1.getf( i, j ) == labelImage2.getf( i, j ) )
	    				intersection[ labelIndices1.get( (int) labelImage1.getf( i, j ) ) ] ++;
	    		}
	    // return the Dice coefficient
	    for( int i = 0; i < intersection.length; i ++ )
	    {
	    	int num2 = labelIndices2.get( labels1[ i ] ) != null ? numPix2[ labelIndices2.get( labels1[ i ] ) ] : 0;
	    	intersection[ i ] = 2.0 * intersection[ i ]  / ( numPix1[ i ] + num2 );
	    }
	    // create data table
  		ResultsTable table = new ResultsTable();
  		for (int i = 0; i < labels1.length; i++) {
  			table.incrementCounter();
  			table.addLabel(Integer.toString( labels1[i] ));
  			table.addValue("DiceCoefficient", intersection[i]);
  		}
  	    return table;
	}
	/**
	 * Get the Dice coefficient between two label images.
	 * @param labelImage1 first label image
	 * @param labelImage2 second label image
	 * @return Dice coefficient value or -1 if error
	 * @see <a href="https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient">https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient</a>
	 */
	public static final double getDiceCoefficient(
			ImageStack labelImage1,
			ImageStack labelImage2 )
	{
		if( labelImage1.getWidth() != labelImage2.getWidth() ||
				labelImage1.getHeight() != labelImage2.getHeight() )
			return -1;
		double intersection = 0;
		double numPix1 = 0;
		double numPix2 = 0;
		// calculate the pixel to pixel intersection
		for( int k = 0; k < labelImage1.getSize(); k ++ )
		{
			final ImageProcessor l1 = labelImage1.getProcessor( k+1 );
			final ImageProcessor l2 = labelImage2.getProcessor( k+1 );
			for( int i = 0; i < labelImage1.getWidth(); i++ )
				for( int j = 0; j < labelImage1.getHeight(); j++ )
				{
					if( l1.getf( i, j ) > 0 ) // skip label 0 (background)
		    		{
		    			numPix1 ++;
		    			if( l1.getf( i, j ) == l2.getf( i, j ) )
		    				intersection ++;
		    		}
					if( l2.getf( i, j ) > 0 )
						numPix2 ++;
				}
		}
	    // return the Dice coefficient
	    return 2.0 * intersection / ( numPix1 + numPix2 );
	}
	/**
	 * Get the Dice coefficient per label (intersection over union overlap) between
	 * two label images.
	 * @param labelImage1 reference label image
	 * @param labelImage2 label image to compare with
	 * @return Dice coefficient per label in the reference image or null if error
	 * @see <a href="https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient">https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient</a>
	 */
	public static final ResultsTable getDiceCoefficientPerLabel(
			ImageStack labelImage1,
			ImageStack labelImage2 )
	{
		if( labelImage1.getWidth() != labelImage2.getWidth() ||
				labelImage1.getHeight() != labelImage2.getHeight() )
			return null;

		int[] labels1 = findAllLabels( labelImage1 );
		int[] numPix1 = voxelCount( labelImage1, labels1 );
		double[] intersection = new double[ labels1.length ];
		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> labelIndices1 = mapLabelIndices( labels1 );

	    int[] labels2 = findAllLabels( labelImage2 );
		int[] numPix2 = voxelCount( labelImage2, labels2 );

		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> labelIndices2 = mapLabelIndices( labels2 );

		// calculate the voxel to voxel intersection
	    for( int k = 0; k < labelImage1.getSize(); k ++ )
		{
			final ImageProcessor l1 = labelImage1.getProcessor( k+1 );
			final ImageProcessor l2 = labelImage2.getProcessor( k+1 );
			for( int i = 0; i < labelImage1.getWidth(); i++ )
				for( int j = 0; j < labelImage1.getHeight(); j++ )
					if( l1.getf( i, j ) > 0 ) // skip label 0 (background)
		    		{
						if( l1.getf( i, j ) == l2.getf( i, j ) )
							intersection[ labelIndices1.get( (int) l1.getf( i, j ) ) ] ++;
		    		}
		}
	    // return the Dice coefficient
	    for( int i = 0; i < intersection.length; i ++ )
	    {
	    	int num2 = labelIndices2.get( labels1[ i ] ) != null ? numPix2[ labelIndices2.get( labels1[ i ] ) ] : 0;
	    	intersection[ i ] = 2.0 * intersection[ i ]  / ( numPix1[ i ] + num2 );
	    }
	    // create data table
  		ResultsTable table = new ResultsTable();
  		for (int i = 0; i < labels1.length; i++) {
  			table.incrementCounter();
  			table.addLabel(Integer.toString( labels1[i] ));
  			table.addValue("DiceCoefficient", intersection[i]);
  		}
  	    return table;
	}
	/**
	 * Get the Dice coefficient between two label images.
	 * @param labelImage1 first label image
	 * @param labelImage2 second label image
	 * @return Dice coefficient value or -1 if error
	 * @see <a href="https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient">https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient</a>
	 */
	public static final double getDiceCoefficient(
			ImagePlus labelImage1,
			ImagePlus labelImage2 )
	{
		return getDiceCoefficient( labelImage1.getImageStack(), labelImage2.getImageStack() );
	}
	/**
	 * Get the Dice coefficient per label (intersection over union overlap) between
	 * two label images.
	 * @param labelImage1 reference label image
	 * @param labelImage2 label image to compare with
	 * @return Dice coefficient per label in the reference image or null if error
	 * @see <a href="https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient">https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient</a>
	 */
	public static final ResultsTable getDiceCoefficientPerLabel(
			ImagePlus labelImage1,
			ImagePlus labelImage2 )
	{
		return getDiceCoefficientPerLabel( labelImage1.getImageStack(), labelImage2.getImageStack() );
	}
	/**
	 * Get the total volume similarity between two label images (source and target).
	 * <p>
	 * Volume Similarity (for all regions) $VS = 2\frac{ \sum_r{|S_r| - |T_r|} }{ \sum_r{|S_r| + |T_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return total volume similarity value
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getVolumeSimilarity(
			ImageProcessor sourceImage,
			ImageProcessor targetImage )
	{
		double numPixSource = 0;
		double numPixTarget = 0;
		int[] sourceLabels = findAllLabels( sourceImage );
		int[] sourcePixPerLabel = pixelCount( sourceImage, sourceLabels );

	    int[] targetLabels = findAllLabels( targetImage );
		int[] targetPixPerLabel = pixelCount( targetImage, targetLabels );

		for( int i = 0; i < sourceLabels.length; i ++ )
			numPixSource += sourcePixPerLabel[ i ];
		for( int i = 0; i < targetLabels.length; i ++ )
			numPixTarget += targetPixPerLabel[ i ];

	    // return the total volume similarity
	    return 2.0 * ( numPixSource - numPixTarget )  / ( numPixSource + numPixTarget );
	}
	/**
	 * Get the volume similarity between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * Volume Similarity (for each label region r) $VS_r = 2\frac{ |S_r| - |T_r| }{ |S_r| + |T_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return volume similarity per label
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getVolumeSimilarityPerLabel(
			ImageProcessor sourceImage,
			ImageProcessor targetImage )
	{
		int[] sourceLabels = findAllLabels( sourceImage );
		int[] numPixSource = pixelCount( sourceImage, sourceLabels );
		double[] volumeSim = new double[ sourceLabels.length ];

	    int[] targetLabels = findAllLabels( targetImage );
		int[] numPixTarget = pixelCount( targetImage, targetLabels );

		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> targetLabelIndices = mapLabelIndices( targetLabels );

	    // calculate the volume similarity of the source labels
	    for( int i = 0; i < sourceLabels.length; i ++ )
	    	volumeSim[ i ] = targetLabelIndices.get( sourceLabels[ i ] ) != null ?
	    			2.0 * ( numPixSource[ i ] - numPixTarget[ targetLabelIndices.get( sourceLabels[ i ] ) ] )
	    			/ ( numPixSource[ i ] + numPixTarget[ targetLabelIndices.get( sourceLabels[ i ] ) ] )
	    			: 0;
		// create data table
  		ResultsTable table = new ResultsTable();
  		for (int i = 0; i < sourceLabels.length; i++) {
  			table.incrementCounter();
  			table.addLabel(Integer.toString( sourceLabels[ i ] ));
  			table.addValue("VolumeSimilarity", volumeSim[ i ] );
  		}
  	    return table;
	}
	/**
	 * Get the total volume similarity between two label images (source and target).
	 * <p>
	 * Volume Similarity (for all regions) $VS = 2\frac{ \sum_r{|S_r| - |T_r|} }{ \sum_r{|S_r| + |T_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return total volume similarity value or NaN if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getVolumeSimilarity(
			ImageStack sourceImage,
			ImageStack targetImage )
	{
		double numPixSource = 0;
		double numPixTarget = 0;
		int[] sourceLabels = findAllLabels( sourceImage );
		int[] sourcePixPerLabel = voxelCount( sourceImage, sourceLabels );

	    int[] targetLabels = findAllLabels( targetImage );
		int[] targetPixPerLabel = voxelCount( targetImage, targetLabels );

		for( int i = 0; i < sourceLabels.length; i ++ )
			numPixSource += sourcePixPerLabel[ i ];
		for( int i = 0; i < targetLabels.length; i ++ )
			numPixTarget += targetPixPerLabel[ i ];

	    // return the total volume similarity
	    return 2.0 * ( numPixSource - numPixTarget )  / ( numPixSource + numPixTarget );
	}
	/**
	 * Get the volume similarity between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * Volume Similarity (for each label region r) $VS_r = 2\frac{ |S_r| - |T_r| }{ |S_r| + |T_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return volume similarity per label
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getVolumeSimilarityPerLabel(
			ImageStack sourceImage,
			ImageStack targetImage )
	{
		int[] sourceLabels = findAllLabels( sourceImage );
		int[] numPixSource = voxelCount( sourceImage, sourceLabels );
		double[] volumeSim = new double[ sourceLabels.length ];

	    int[] targetLabels = findAllLabels( targetImage );
		int[] numPixTarget = voxelCount( targetImage, targetLabels );

		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> targetLabelIndices = mapLabelIndices( targetLabels );

	    // calculate the volume similarity of the source labels
	    for( int i = 0; i < sourceLabels.length; i ++ )
	    	volumeSim[ i ] = targetLabelIndices.get( sourceLabels[ i ] ) != null ?
	    			2.0 * ( numPixSource[ i ] - numPixTarget[ targetLabelIndices.get( sourceLabels[ i ] ) ] )
	    			/ ( numPixSource[ i ] + numPixTarget[ targetLabelIndices.get( sourceLabels[ i ] ) ] )
	    			: 0;
		// create data table
  		ResultsTable table = new ResultsTable();
  		for (int i = 0; i < sourceLabels.length; i++) {
  			table.incrementCounter();
  			table.addLabel(Integer.toString( sourceLabels[ i ] ));
  			table.addValue("VolumeSimilarity", volumeSim[ i ] );
  		}
  	    return table;
	}
	/**
	 * Get the total volume similarity between two label images (source and target).
	 * <p>
	 * Volume Similarity (for all regions) $VS = 2\frac{ \sum_r{|S_r| - |T_r|} }{ \sum_r{|S_r| + |T_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return total volume similarity value
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getVolumeSimilarity(
			ImagePlus sourceImage,
			ImagePlus targetImage )
	{
		return getVolumeSimilarity( sourceImage.getImageStack(), targetImage.getImageStack() );
	}
	/**
	 * Get the volume similarity between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * Volume Similarity (for each label region r) $VS_r = 2\frac{ |S_r| - |T_r| }{ |S_r| + |T_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return volume similarity per label
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getVolumeSimilarityPerLabel(
			ImagePlus sourceImage,
			ImagePlus targetImage )
	{
		return getVolumeSimilarityPerLabel( sourceImage.getImageStack(), targetImage.getImageStack() );
	}
	/**
	 * Get the total false negative error between two label images (source and target).
	 * <p>
	 * False Negative Error (for all regions) $FN = \frac{ \sum_r{|T_r \setminus S_r|} }{ \sum_r{|T_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false negative error or -1 if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getFalseNegativeError(
			ImageProcessor sourceImage,
			ImageProcessor targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() )
			return -1;
		return 1.0 - LabelImages.getTotalOverlap( sourceImage, targetImage );
	}
	/**
	 * Get the false negative error between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * False Negative Error (for each individual labeled region r) $FN_r = \frac{ |T_r \setminus S_r| }{ |T_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false negative error per label or null if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getFalseNegativeErrorPerLabel(
			ImageProcessor sourceImage,
			ImageProcessor targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() )
			return null;

		ResultsTable toTable = LabelImages.getTargetOverlapPerLabel( sourceImage, targetImage );
		// create data table
  		ResultsTable fneTable = new ResultsTable();
  		for (int i = 0; i < toTable.getCounter(); i++) {
  			fneTable.incrementCounter();
  			fneTable.addLabel( toTable.getLabel( i ) );
  			fneTable.addValue( "FalseNegativeError", 1.0 - toTable.getValue("TargetOverlap", i) );
  		}
  	    return fneTable;
	}
	/**
	 * Get the total false negative error between two label images (source and target).
	 * <p>
	 * False Negative Error (for all regions) $FN = \frac{ \sum_r{|T_r \setminus S_r|} }{ \sum_r{|T_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false negative error or -1 if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getFalseNegativeError(
			ImageStack sourceImage,
			ImageStack targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() ||
				sourceImage.getSize() != targetImage.getSize() )
			return -1;
		return 1.0 - LabelImages.getTotalOverlap( sourceImage, targetImage );
	}
	/**
	 * Get the false negative error between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * False Negative Error (for each individual labeled region r) $FN_r = \frac{ |T_r \setminus S_r| }{ |T_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false negative error per label or null if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getFalseNegativeErrorPerLabel(
			ImageStack sourceImage,
			ImageStack targetImage )
	{
		ResultsTable toTable = LabelImages.getTargetOverlapPerLabel( sourceImage, targetImage );
		// create data table
  		ResultsTable fneTable = new ResultsTable();
  		for (int i = 0; i < toTable.getCounter(); i++) {
  			fneTable.incrementCounter();
  			fneTable.addLabel( toTable.getLabel( i ) );
  			fneTable.addValue( "FalseNegativeError", 1.0 - toTable.getValue("TargetOverlap", i) );
  		}
  	    return fneTable;
	}

	/**
	 * Get the total false negative error between two label images (source and target).
	 * <p>
	 * False Negative Error (for all regions) $FN = \frac{ \sum_r{|T_r \setminus S_r|} }{ \sum_r{|T_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false negative error or -1 if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getFalseNegativeError(
			ImagePlus sourceImage,
			ImagePlus targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() ||
				sourceImage.getNSlices() != targetImage.getNSlices() )
			return -1;
		return 1.0 - LabelImages.getTotalOverlap( sourceImage, targetImage );
	}
	/**
	 * Get the false negative error between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * False Negative Error (for each individual labeled region r) $FN_r = \frac{ |T_r \setminus S_r| }{ |T_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false negative error per label or null if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getFalseNegativeErrorPerLabel(
			ImagePlus sourceImage,
			ImagePlus targetImage )
	{
		return LabelImages.getFalseNegativeErrorPerLabel( sourceImage.getImageStack() , targetImage.getImageStack() );
	}
	/**
	 * Get the total false positive error between two label images (source and target).
	 * <p>
	 * False Positive Error (for all regions) $FP = \frac{ \sum_r{|S_r \setminus T_r|} }{ \sum_r{|S_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false positive error or -1 if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getFalsePositiveError(
			ImageProcessor sourceImage,
			ImageProcessor targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() )
			return -1;
		double setDiff = 0;
		double numPixSource = 0;
		// calculate the set difference between source and target
	    for( int i = 0; i < sourceImage.getWidth(); i++ )
	    	for( int j = 0; j < sourceImage.getHeight(); j++ )
	    	{
	    		if( sourceImage.getf( i, j ) > 0 ) // skip label 0 (background)
	    		{
	    			numPixSource ++;
	    			if( sourceImage.getf( i, j ) != targetImage.getf( i, j ) )
	    				setDiff ++;
	    		}
	    	}
	    // return the total overlap
	    return setDiff / numPixSource;
	}
	/**
	 * Get the false positive error between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * False Positive Error (for each individual labeled region r) $FN_r = \frac{ |S_r \setminus T_r| }{ |S_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false positive error per label or null if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getFalsePositiveErrorPerLabel(
			ImageProcessor sourceImage,
			ImageProcessor targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() )
			return null;

		int[] sourceLabels = findAllLabels( sourceImage );
		double[] setDiff = new double[ sourceLabels.length ];
		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> sourceLabelIndices = mapLabelIndices( sourceLabels );
	    int[] numPixSource = pixelCount( sourceImage, sourceLabels );

		// calculate the set difference between source and target
	    for( int i = 0; i < sourceImage.getWidth(); i++ )
	    	for( int j = 0; j < sourceImage.getHeight(); j++ )
	    		if( sourceImage.getf( i, j ) > 0 ) // skip label 0 (background)
	    		{
	    			if( sourceImage.getf( i, j ) != targetImage.getf( i, j ) )
	    				setDiff[ sourceLabelIndices.get( (int) sourceImage.getf( i, j ) ) ] ++;
	    		}
	    // return the false positive error
	    for( int i = 0; i < setDiff.length; i ++ )
	    	setDiff[ i ] /= numPixSource[ i ];

	    // create data table
  		ResultsTable table = new ResultsTable();
  		for (int i = 0; i < sourceLabels.length; i++) {
  			table.incrementCounter();
  			table.addLabel(Integer.toString( sourceLabels[ i ] ));
  			table.addValue( "FalsePositiveError", setDiff[ i ] );
  		}
  	    return table;
	}
	/**
	 * Get the total false positive error between two label images (source and target).
	 * <p>
	 * False Positive Error (for all regions) $FP = \frac{ \sum_r{|S_r \setminus T_r|} }{ \sum_r{|S_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false positive error or -1 if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getFalsePositiveError(
			ImageStack sourceImage,
			ImageStack targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() ||
				sourceImage.getSize() != targetImage.getSize() )
			return -1;
		double setDiff = 0;
		double numPixSource = 0;
		// calculate the set difference between source and target
		for( int k = 0; k < sourceImage.getSize(); k++ )
		{
			final ImageProcessor ls = sourceImage.getProcessor( k+1 );
			final ImageProcessor lt = targetImage.getProcessor( k+1 );
		    for( int i = 0; i < sourceImage.getWidth(); i++ )
		    	for( int j = 0; j < sourceImage.getHeight(); j++ )
		    	{
		    		if( ls.getf( i, j ) > 0 ) // skip label 0 (background)
		    		{
		    			numPixSource ++;
		    			if( ls.getf( i, j ) != lt.getf( i, j ) )
		    				setDiff ++;
		    		}
		    	}
		}
	    // return the total overlap
	    return setDiff / numPixSource;
	}
	/**
	 * Get the false positive error between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * False Positive Error (for each individual labeled region r) $FN_r = \frac{ |S_r \setminus T_r| }{ |S_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false positive error per label or null if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getFalsePositiveErrorPerLabel(
			ImageStack sourceImage,
			ImageStack targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() ||
				sourceImage.getSize() != targetImage.getSize() )
			return null;
		int[] sourceLabels = findAllLabels( sourceImage );
		double[] setDiff = new double[ sourceLabels.length ];
		// create associative array to identify the index of each label
	    HashMap<Integer, Integer> sourceLabelIndices = mapLabelIndices( sourceLabels );
	    int[] numPixSource = voxelCount( sourceImage, sourceLabels );

	    // calculate the set difference between source and target
	    for( int k = 0; k < sourceImage.getSize(); k++ )
	 	{
	 		final ImageProcessor ls = sourceImage.getProcessor( k+1 );
	 		final ImageProcessor lt = targetImage.getProcessor( k+1 );
	 		for( int i = 0; i < sourceImage.getWidth(); i++ )
	 			for( int j = 0; j < sourceImage.getHeight(); j++ )
	 				if( ls.getf( i, j ) > 0 ) // skip label 0 (background)
	 				{
	 					if( ls.getf( i, j ) != lt.getf( i, j ) )
	 						setDiff[ sourceLabelIndices.get( (int) ls.getf( i, j ) ) ] ++;
	 				}
	 	}
	
	    // return the false positive error
	    for( int i = 0; i < setDiff.length; i ++ )
	    	setDiff[ i ] /= numPixSource[ i ];

	    // create data table
  		ResultsTable table = new ResultsTable();
  		for (int i = 0; i < sourceLabels.length; i++) {
  			table.incrementCounter();
  			table.addLabel(Integer.toString( sourceLabels[ i ] ));
  			table.addValue( "FalsePositiveError", setDiff[ i ] );
  		}
  	    return table;
	}
	/**
	 * Get the total false positive error between two label images (source and target).
	 * <p>
	 * False Positive Error (for all regions) $FP = \frac{ \sum_r{|S_r \setminus T_r|} }{ \sum_r{|S_r|} }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false positive error or -1 if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final double getFalsePositiveError(
			ImagePlus sourceImage,
			ImagePlus targetImage )
	{
		if( sourceImage.getWidth() != targetImage.getWidth() ||
				sourceImage.getHeight() != targetImage.getHeight() ||
				sourceImage.getNSlices() != targetImage.getNSlices() )
			return -1;
		return LabelImages.getFalsePositiveError( sourceImage.getImageStack(), targetImage.getImageStack() );
	}
	/**
	 * Get the false positive error between two label images (source and target)
	 * per each individual labeled region.
	 * <p>
	 * False Positive Error (for each individual labeled region r) $FN_r = \frac{ |S_r \setminus T_r| }{ |S_r| }$.
	 * @param sourceImage source label image
	 * @param targetImage target label image
	 * @return false positive error per label or null if error
	 * @see <a href="http://www.insight-journal.org/browse/publication/707">http://www.insight-journal.org/browse/publication/707</a>
	 */
	public static final ResultsTable getFalsePositiveErrorPerLabel(
			ImagePlus sourceImage,
			ImagePlus targetImage )
	{
		return LabelImages.getFalsePositiveErrorPerLabel( sourceImage.getImageStack(), targetImage.getImageStack() );
	}
	/**
	 * For each label, finds the position of the point belonging to label region
	 * defined by <code>labelImage</code> and with maximal value in intensity
	 * image <code>valueImage</code>.
	 * 
	 * @deprecated use LavelValues.findPositionOfMaxValues instead
	 * 
	 * @param valueImage
	 *            the intensity image containing values to compare
	 * @param labelImage
	 *            the intensity image containing label of each pixel
	 * @param labels
	 *            the list of labels in the label image
	 * @return the position of maximum value in intensity image for each label
	 */
	@Deprecated
	public static final Point[] findPositionOfMaxValues(ImageProcessor valueImage, 
			ImageProcessor labelImage, int[] labels)
	{
		return LabelValues.findPositionOfMaxValues(valueImage, labelImage, labels);
	}

	/**
	 * For each label, finds the position of the point belonging to label region
	 * defined by <code>labelImage</code> and with minimal value in intensity
	 * image <code>valueImage</code>.
	 * 
	 * @deprecated use LavelValues.findPositionOfMinValues instead
	 * 
	 * @param valueImage
	 *            the intensity image containing values to compare
	 * @param labelImage
	 *            the intensity image containing label of each pixel
	 * @param labels
	 *            the list of labels in the label image
	 * @return the position of minimum value in intensity image for each label
	 */
	@Deprecated
	public static final Point[] findPositionOfMinValues(ImageProcessor valueImage, 
			ImageProcessor labelImage, int[] labels)
	{
		return LabelValues.findPositionOfMinValues(valueImage, labelImage, labels);
	}

}// end class LabelImages
