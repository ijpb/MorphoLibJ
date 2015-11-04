/**
 * 
 */
package inra.ijpb.label;

import static java.lang.Math.max;
import static java.lang.Math.min;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	 * Creates a label image with the appropriate class to store the required
	 * number of labels.
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
	 *            a collection of binary images (0: background, >0 pixel belongs
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
	 */
	public static final ImageStack labelBoundaries(ImageStack stack) 
	{
		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		
		for (int z = 0; z < sizeZ - 1; z++) 
		{
			for (int y = 0; y < sizeY - 1; y++)
			{
				for (int x = 0; x < sizeX - 1; x++)
				{
					double value = stack.getVoxel(x, y, z);
					if (stack.getVoxel(x+1, y, z) != value)
						result.setVoxel(x, y, z, 255);
					if (stack.getVoxel(x, y+1, z) != value)
						result.setVoxel(x, y, z, 255);
					if (stack.getVoxel(x, y, z+1) != value)
						result.setVoxel(x, y, z, 255);
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
	 * contains only particles with at least the specified number of pixels or voxels.
	 * @see #areaOpening(ImageProcessor, int)
	 * @see #volumeOpening(ImageStack, int)
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
		removeLabels(image, labels, 0);
	}

	private static final int[] findBorderLabels(ImageProcessor image) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
	
		for (int x = 0; x < sizeX; x++)
		{
			labelSet.add((int) image.get(x, 0));
			labelSet.add((int) image.get(x, sizeY - 1));
		}
	
		for (int y = 0; y < sizeY; y++) 
		{
			labelSet.add((int) image.get(0, y));
			labelSet.add((int) image.get(sizeX - 1, y));
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
		removeLabels(image, labels, 0);
	}

	private static final int[] findBorderLabels(ImageStack image) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
	
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				labelSet.add((int) image.getVoxel(x, y, 0));
				labelSet.add((int) image.getVoxel(x, y, sizeZ - 1));
			}
		}
		
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int x = 0; x < sizeX; x++)
{
				labelSet.add((int) image.getVoxel(x, 0, z));
				labelSet.add((int) image.getVoxel(x, sizeY - 1, z));
			}
		}
		
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
	 * @param imagePlus an instance if ImagePlus containing a binary image
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
	 * @param image a binary image
	 */
	public static final ImageProcessor keepLargestLabel(ImageProcessor image)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		ImageProcessor result = new ByteProcessor(sizeX, sizeY);
		
		// find the label of the largest particle
		int[] labels = findAllLabels(image);
		int[] areas = pixelCount(image, labels);
		int indMax = labels[indexOfMax(areas)];

		// convert label image to binary image
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int value = (int) image.getf(x, y); 
				if (value == indMax)
					result.set(x, y, 255);
				else
					result.set(x, y, 0);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a binary image that contains only the largest label.
	 * @param image a binary image
	 */
	public static final ImageStack keepLargestLabel(ImageStack image) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
				
		// find the label of the largest particle
		int[] labels = findAllLabels(image);
		int[] volumes = voxelCount(image, labels);		
		int indMax = labels[indexOfMax(volumes)];
		
		// convert label image to binary image
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int value = (int) image.getVoxel(x, y, z); 
					if (value == indMax)
						result.setVoxel(x, y, z, 255);
					else
						result.setVoxel(x, y, z, 0);
				}
			}
		}
		
		return result;
	}


	public static final void removeLargestLabel(ImagePlus imagePlus)
	{
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			removeLargestLabel(imagePlus.getProcessor());
		} else {
			removeLargestLabel(imagePlus.getStack());
		}
	}

	public static final void removeLargestLabel(ImageProcessor image) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// find the label of the largest particle
		int[] labels = findAllLabels(image);
		int[] areas = pixelCount(image, labels);
		int indMax = labels[indexOfMax(areas)];
		
		// remove voxels belonging to the largest label
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++) 
			{
				int value = (int) image.getf(x, y); 
				if (value == indMax)
					image.setf(x, y, 0);
			}
		}
	}

	public static final void removeLargestLabel(ImageStack image) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// find the label of the largest particle
		int[] labels = findAllLabels(image);
		int[] volumes = voxelCount(image, labels);
		int indMax = labels[indexOfMax(volumes)];
		
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++) 
				{
					int value = (int) image.getVoxel(x, y, z); 
					if (value == indMax)
						image.setVoxel(x, y, z, 0);
				}
			}
		}
	}

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
	 * @see inra.ijpb.measure.GeometricMeasures2D#area(ij.process.ImageProcessor, int[], double[])
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
	*/
	public final static int[] voxelCount(ImageStack labelImage, int[] labels) 
	{
        // create associative array to know index of each label
		HashMap<Integer, Integer> labelIndices = mapLabelIndices(labels);

        // initialize result
		int nLabels = labels.length;
		int[] counts = new int[nLabels];

		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		int sizeZ = labelImage.getSize();

		// iterate on image voxels
		IJ.showStatus("Count label voxels...");
        for (int z = 0; z < sizeZ; z++) 
        {
        	IJ.showProgress(z, sizeZ);
        	for (int y = 0; y < sizeY; y++)
        	{
        		for (int x = 0; x < sizeX; x++)
        		{
        			int label = (int) labelImage.getVoxel(x, y, z);
					// do not consider background
					if (label == 0)
						continue;
					int labelIndex = labelIndices.get(label);
					counts[labelIndex]++;
        		}
        	}
        }
        
		IJ.showStatus("");
        return counts;
	}
	
    /**
     * Returns the set of unique labels existing in the given image, excluding 
     * the value zero (used for background).
     */
    public final static int[] findAllLabels(ImagePlus image) 
    {
		return image.getStackSize() == 1 ? findAllLabels(image.getProcessor())
				: findAllLabels(image.getStack());
    }

    /**
     * Returns the set of unique labels existing in the given stack, excluding 
     * the value zero (used for background).
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
        	IJ.showProgress(z, sizeZ);
        	for (int y = 0; y < sizeY; y++)  
        	{
        		for (int x = 0; x < sizeX; x++)
        		{
        			labels.add((int) image.getVoxel(x, y, z));
        		}
        	}
        }
        IJ.showProgress(1);
        
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
        		IJ.showProgress(y, sizeY);
        		for (int x = 0; x < sizeX; x++) 
        			labels.add((int) image.getf(x, y));
        	}
        } 
        else
        {
        	// for integer-based images, simply use integer result
        	for (int y = 0; y < sizeY; y++) 
        	{
        		IJ.showProgress(y, sizeY);
        		for (int x = 0; x < sizeX; x++) 
        			labels.add(image.get(x, y));
        	}
        }
        IJ.showProgress(1);
        
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
	 * Replace all values specified in label array by the value 0. 
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
			removeLabels(image, labels, newLabel);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			removeLabels(image, labels, newLabel);
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
	 * Replace all values specified in label array by the value 0.
	 *  
	 * @param image a label planar image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public static final void removeLabels(ImageProcessor image, int[] labels, int newLabel)
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
					image.setf(x, y, newLabel);
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
	 * Replace all values specified in label array by the value 0.
	 *  
	 * @param image a label 3D image
	 * @param labels the list of labels to replace 
	 * @param newLabel the new value for labels 
	 */
	public static final void removeLabels(ImageStack image, int[] labels, int newLabel)
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
	 * @param imagePlus an ImagePlus containing a planar label image
	 * @param labels the list of values to keep 
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
	 * @param image a label planar image
	 * @param labels the list of values to keep 
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
	 * @param image a label 3D image
	 * @param labels the list of values to keep 
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
	 * Create associative array to retrieve the index corresponding each label.
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
}
