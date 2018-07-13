/**
 * 
 */
package inra.ijpb.measure.region3d;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.HashMap;
import java.util.Map;

import ij.IJ;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.geometry.Box3D;
import inra.ijpb.label.LabelImages;

/**
 * Compute bounding box of each region within a label or binary image.
 * 
 * @author dlegland
 *
 */
public class BoundingBox3D extends RegionAnalyzer3D<Box3D>
{
	// ==================================================
	// Static methods

	/**
	 * Compute bounding box of each label in input stack and returns the result
	 * as an array of double for each label.
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels an array of unique labels in image
	 * @return a data table containing for each labeled particle the extent in
	 *         each dimension
	 */
	public final static double[][] boundingBox(ImageStack labelImage, int[] labels)
	{
        // create associative array to know index of each label
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

        // initialize result
		int nLabels = labels.length;
		double[][] boxes = new double[nLabels][6];
		for (int i = 0; i < nLabels; i++)
		{
			boxes[i][0] = Double.POSITIVE_INFINITY;
			boxes[i][1] = Double.NEGATIVE_INFINITY;
			boxes[i][2] = Double.POSITIVE_INFINITY;
			boxes[i][3] = Double.NEGATIVE_INFINITY;
			boxes[i][4] = Double.POSITIVE_INFINITY;
			boxes[i][5] = Double.NEGATIVE_INFINITY;
		}

		
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		int sizeZ = labelImage.getSize();

		// iterate on image voxels to update bounding boxes
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
					
					// do not processes labels not in the list
					if (!labelIndices.containsKey(label))
						continue;

					// update bounding box of current label
					int labelIndex = labelIndices.get(label);
					boxes[labelIndex][0] = min(boxes[labelIndex][0], x);
					boxes[labelIndex][1] = max(boxes[labelIndex][1], x + 1);
					boxes[labelIndex][2] = min(boxes[labelIndex][2], y);
					boxes[labelIndex][3] = max(boxes[labelIndex][3], y + 1);
					boxes[labelIndex][4] = min(boxes[labelIndex][4], z);
					boxes[labelIndex][5] = max(boxes[labelIndex][5], z + 1);
        		}
        	}
        }
        
		return boxes;
	}
	
	// ==================================================
	// Constructor

	/**
	 * Default constructor
	 */
	public BoundingBox3D()
	{
	}

	
	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Computes inertia ellipse of each region in input label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the calibration of the image
	 * @return an array of Box3D representing the calibrated coordinates of
	 *         the inertia ellipse of each region
	 */
	public Box3D[] analyzeRegions(ImageStack image, int[] labels, Calibration calib)
	{
		// size of image
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();

		// Extract spatial calibration
		double sx = 1, sy = 1, sz = 1;
		double ox = 0, oy = 0, oz = 0;
		if (calib != null)
		{
			sx = calib.pixelWidth;
			sy = calib.pixelHeight;
			sz = calib.pixelDepth;
			ox = calib.xOrigin;
			oy = calib.yOrigin;
			oz = calib.zOrigin;
		}
		
		// create associative array to know index of each label
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// allocate memory for result
		int nLabels = labels.length;
		double[] xmin = new double[nLabels];
		double[] xmax = new double[nLabels];
		double[] ymin = new double[nLabels];
		double[] ymax = new double[nLabels];
		double[] zmin = new double[nLabels];
		double[] zmax = new double[nLabels];
		
		// initialize to extreme values
		for (int i = 0; i < nLabels; i++)
		{
			xmin[i] = Double.POSITIVE_INFINITY;
			xmax[i] = Double.NEGATIVE_INFINITY;
			ymin[i] = Double.POSITIVE_INFINITY;
			ymax[i] = Double.NEGATIVE_INFINITY;
			zmin[i] = Double.POSITIVE_INFINITY;
			zmax[i] = Double.NEGATIVE_INFINITY;
		}

		// compute extreme coordinates of each region
    	fireStatusChanged(this, "Compute bounds");
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

    				xmin[index] = Math.min(xmin[index], x);
    				xmax[index] = Math.max(xmax[index], x + 1);
    				ymin[index] = Math.min(ymin[index], y);
    				ymax[index] = Math.max(ymax[index], y + 1);
    				zmin[index] = Math.min(zmin[index], z);
    				zmax[index] = Math.max(zmax[index], z + 1);
    			}
    		}
    	}
    	
		// create bounding box instances
		Box3D[] boxes = new Box3D[nLabels];
		for (int i = 0; i < nLabels; i++)
		{
			boxes[i] = new Box3D(
					xmin[i] * sx + ox, xmax[i] * sx + ox,
					ymin[i] * sy + oy, ymax[i] * sy + oy, 
					zmin[i] * sz + oz, zmax[i] * sz + oz);
		}
		return boxes;
	}

	/**
	 * Utility method that transforms the mapping between labels and inertia
	 * boxes instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and Inertia Box3Ds
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Box3D> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Box3D box = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// coordinates of centroid
			table.addValue("Box.XMin", box.getXMin());
			table.addValue("Box.XMax", box.getXMax());
			table.addValue("Box.YMin", box.getYMin());
			table.addValue("Box.YMax", box.getYMax());
			table.addValue("Box.ZMin", box.getZMin());
			table.addValue("Box.ZMax", box.getZMax());
		}
	
		return table;
	}
}
