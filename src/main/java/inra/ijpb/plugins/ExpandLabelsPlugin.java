package inra.ijpb.plugins;

import java.util.HashMap;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.GeometricMeasures2D;
import inra.ijpb.measure.GeometricMeasures3D;

/**
 * Creates a new image larger than the original one, and copies each label identically
 * but shifted by a given dilation coefficient. This results in 2D or 3D images with 
 * fewer labels touching each other, making them easier to visualize.</p>  
 *
 * The idea is to transform a label image in the following way:
 * <pre><code>
 *                  1 1 1 0 0 0 0 0
 * 1 1 1 0 2 2      1 1 1 0 0 0 2 2
 * 1 1 1 0 2 2      0 0 0 0 0 0 2 2
 * 0 0 0 0 2 2  =>  0 0 0 0 0 0 2 2
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
        GenericDialog gd = new GenericDialog("Dilate labels");
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
        
        // copy disp^lay range
        double vmin = imagePlus.getDisplayRangeMin();
        double vmax = imagePlus.getDisplayRangeMax();
        resultPlus.setDisplayRange(vmin, vmax);

        // Display image
        resultPlus.show();
        
        // For 3D images, choose same slice as original
		if (imagePlus.getStackSize() > 1)
		{
			int newSlice = (int) Math.floor(imagePlus.getSlice() * (1.0 + expandRatio / 100.0));
			resultPlus.setSlice(newSlice);
		}
	}
	
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
		double[][] centroids = GeometricMeasures2D.centroids(image, labels);
		
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
				int label = (int) image.getf(x, y);
				if (label == 0)
					continue;

				int index = labelIndices.get(label);
				int x2 = x + shifts[index][0];
				int y2 = y + shifts[index][1];
				result.set(x2, y2, label);
			}
		}
		
		return result;
	}

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
		double[][] centroids = GeometricMeasures3D.centroids(image, labels);
		
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
        			int label = (int) image.getVoxel(x, y, z);
        			if (label == 0)
        				continue;

        			int index = labelIndices.get(label);
        			int x2 = x + shifts[index][0];
        			int y2 = y + shifts[index][1];
        			int z2 = z + shifts[index][2];
        			result.setVoxel(x2, y2, z2, label);
        		}
        	}
        }

		return result;
	}

}
