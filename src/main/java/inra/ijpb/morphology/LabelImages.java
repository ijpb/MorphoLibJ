/**
 * 
 */
package inra.ijpb.morphology;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.measure.GeometricMeasures2D;
import inra.ijpb.measure.GeometricMeasures3D;

import java.awt.Color;
import java.util.Iterator;
import java.util.TreeSet;
import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 * Utility methods for label images (stored as 8-, 16- or 32-bits).
 * 
 * @author David Legland
 *
 */
public class LabelImages {
	
	/**
	 * Creates a binary 3D image that contains 255 for voxels that are 
	 * boundaries between two labels.
	 */
	public static final ImageStack labelBoundaries(ImageStack stack) {
		int sizeX = stack.getWidth();
		int sizeY = stack.getHeight();
		int sizeZ = stack.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		
		for (int z = 0; z < sizeZ - 1; z++) {
			for (int y = 0; y < sizeY - 1; y++) {
				for (int x = 0; x < sizeX - 1; x++) {
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
	
	public static final ImageStack cropLabel(ImageStack image, int label, int border) 
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// Determine label bounds
		int xmin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		int ymin = Integer.MAX_VALUE;
		int ymax = Integer.MIN_VALUE;
		int zmin = Integer.MAX_VALUE;
		int zmax = Integer.MIN_VALUE;
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int val = (int) image.getVoxel(x, y, z);
					if (val != label)
					{
						continue;
					}
					
					xmin = min(xmin, x);
					xmax = max(xmax, x);
					ymin = min(ymin, y);
					ymax = max(ymax, y);
					zmin = min(zmin, z);
					zmax = max(zmax, z);
				}
			}
		}
		
		// Compute siez of result
		int sizeX2 = (xmax - xmin + 1 + 2 * border);
		int sizeY2 = (ymax - ymin + 1 + 2 * border);
		int sizeZ2 = (zmax - zmin + 1 + 2 * border);

		// allocate memory for result image
		ImageStack result = ImageStack.create(sizeX2, sizeY2, sizeZ2, 8);
		
		// fill result with binary label
		for (int z = zmin, z2 = border; z <= zmax; z++, z2++) {
			for (int y = ymin, y2 = border; y <= ymax; y++, y2++) {
				for (int x = xmin, x2 = border; x <= xmax; x++, x2++) {
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
	 * Creates a new Color image from a label image, a LUT, and a
	 * color for background.
	 * 
	 * @param imagePlus a 2D or 3D image containing labels and 0 for background
	 * @param lut the array of color components for each label 
	 * @param bgColor the background color
	 * @return a new Color image
	 */
	public static final ImagePlus labelToRgb(ImagePlus imagePlus, byte[][] lut, Color bgColor) {
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-rgb";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) {
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = labelToRgb(image, lut, bgColor);
			resultPlus = new ImagePlus(newName, result);
		} else {
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
	public static final ColorProcessor labelToRgb(ImageProcessor image, byte[][] lut, Color bgColor) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		int bgColorCode = bgColor.getRGB();
		
		ColorProcessor result = new ColorProcessor(width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int index = image.get(x, y);
				if (index == 0) {
					result.set(x, y, bgColorCode);
				} else {
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
	public static final ImageStack labelToRgb(ImageStack image, byte[][] lut, Color bgColor) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 24);
		
		int bgColorCode = bgColor.getRGB();
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int index = (int) image.getVoxel(x, y, z);
					if (index == 0) {
						result.setVoxel(x, y, z, bgColorCode);
					} else {
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
	 * Replace all values specified in label array by the value 0. 
	 * This method changes directly the values within the image.
	 * 
	 * @param imagePlus an ImagePlus containing a 3D label image
	 * @param labels the list of values to remove 
	 */
	public static final void removeLabels(ImagePlus imagePlus, int[] labels) {
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) {
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			removeLabels(image, labels);
		} else {
			// process image stack
			ImageStack image = imagePlus.getStack();
			removeLabels(image, labels);
		}
	}
	
	/**
	 * Replace all values specified in label array by the value 0. 
	 * @param image a label planar image
	 * @param labels the list of values to remove 
	 */
	public static final void removeLabels(ImageProcessor image, int[] labels) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
		for (int i = 0; i < labels.length; i++) {
			labelSet.add(labels[i]);
		}
		
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				int value = image.get(x, y); 
				if (value == 0)
					continue;
				if (labelSet.contains(value)) 
					image.set(x, y, 0);
			}
		}
	}


	/**
	 * Replace all values specified in label array by the value 0. 
	 * @param image a label 3D image
	 * @param labels the list of values to remove 
	 */
	public static final void removeLabels(ImageStack image, int[] labels) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
		for (int i = 0; i < labels.length; i++) {
			labelSet.add(labels[i]);
		}
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int value = (int) image.getVoxel(x, y, z); 
					if (value == 0)
						continue;
					if (labelSet.contains(value)) 
						image.setVoxel(x, y, z, 0);
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
	public static final ImagePlus keepLabels(ImagePlus imagePlus, int[] labels) {
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-keepLabels";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) {
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = keepLabels(image, labels);
			resultPlus = new ImagePlus(newName, result);
		} else {
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
	public static final ImageProcessor keepLabels(ImageProcessor image, int[] labels) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		ImageProcessor result = image.createProcessor(sizeX,  sizeY);
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
		for (int i = 0; i < labels.length; i++) {
			labelSet.add(labels[i]);
		}
		
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				int value = image.get(x, y); 
				if (value == 0)
					continue;
				if (labelSet.contains(value)) 
					result.set(x, y, value);
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
	public static final ImageStack keepLabels(ImageStack image, int[] labels) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, image.getBitDepth());
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
		for (int i = 0; i < labels.length; i++) {
			labelSet.add(labels[i]);
		}
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
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
	 * Returns a binary image that contains only the largest label.
	 * 
	 * @param imagePlus an instance if ImagePlus containing a binary image
	 */
	public static final ImagePlus keepLargestLabel(ImagePlus imagePlus) {
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-largest";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) {
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = keepLargestLabel(image);
			resultPlus = new ImagePlus(newName, result);
		} else {
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
	public static final ImageProcessor keepLargestLabel(ImageProcessor image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		ImageProcessor result = new ByteProcessor(sizeX, sizeY);
		
		// find the label of the largest particle
		int[] labels = findAllLabels(image);
		double[] areas = GeometricMeasures2D.area(image, labels, new double[]{1, 1});
		int indMax = labels[indexOfMax(areas)];

		// convert label image to binary image
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				int value = image.get(x, y); 
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
	public static final ImageStack keepLargestLabel(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
				
		// find the label of the largest particle
		int[] labels = findAllLabels(image);
		double[] volumes = GeometricMeasures3D.volume(image, labels, new double[]{1, 1, 1});		
		int indMax = indexOfMax(volumes);
		
		// convert label image to binary image
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int value = (int) image.getVoxel(x, y, z); 
					if (value == indMax)
						result.setVoxel(x, y, z, 255);
					else
						result.setVoxel(x, y,  z, 0);
				}
			}
		}
		
		return result;
	}


	public static final void removeLargestLabel(ImagePlus imagePlus) {
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) {
			removeLargestLabel(imagePlus.getProcessor());
		} else {
			removeLargestLabel(imagePlus.getStack());
		}
	}

	public static final void removeLargestLabel(ImageProcessor image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();

		// find the label of the largest particle
		int[] labels = findAllLabels(image);
		double[] areas = GeometricMeasures2D.area(image, labels, new double[]{1, 1});
		int indMax = labels[indexOfMax(areas)];
		
		// remove voxels belonging to the largest label
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				int value = image.get(x, y); 
				if (value == indMax)
					image.set(x, y, 0);
			}
		}
	}

	public static final void removeLargestLabel(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		// find the label of the largest particle
		int[] labels = findAllLabels(image);
		double[] volumes = GeometricMeasures3D.volume(image, labels, new double[]{1, 1, 1});
		int indMax = labels[indexOfMax(volumes)];
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				for (int x = 0; x < sizeX; x++) {
					int value = (int) image.getVoxel(x, y, z); 
					if (value == indMax)
						image.setVoxel(x, y, z, 0);
				}
			}
		}
	}

	private static final int indexOfMax(double[] values) {
		int indMax = -1;
		double volumeMax = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > volumeMax) {
				volumeMax = values[i];
				indMax = i;
			}
		}
		return indMax;
	}
	
    public final static int[] findAllLabels(ImagePlus image) {
		return image.getStackSize() == 1 ? findAllLabels(image.getProcessor())
				: findAllLabels(image.getStack());
    }

    public final static int[] findAllLabels(ImageStack image) {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        TreeSet<Integer> labels = new TreeSet<Integer> ();
        
        // iterate on image pixels
        for (int z = 0; z < sizeZ; z++) {
        	IJ.showProgress(z, sizeZ);
        	for (int y = 0; y < sizeY; y++) 
        		for (int x = 0; x < sizeX; x++) 
        			labels.add((int) image.getVoxel(x, y, z));
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

    public final static int[] findAllLabels(ImageProcessor image) {
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        TreeSet<Integer> labels = new TreeSet<Integer> ();
        
        // iterate on image pixels
        for (int y = 0; y < sizeY; y++)  {
        	IJ.showProgress(y, sizeY);
        	for (int x = 0; x < sizeX; x++) 
        		labels.add(image.get(x, y));
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
}
