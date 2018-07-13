/**
 * 
 */
package inra.ijpb.measure.region3d;

import java.util.Map;

import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.geometry.Point3D;
import inra.ijpb.geometry.Sphere;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.LabelValues;

/**
 * Computes the largest inscribed ball for each region of a label or binary
 * image.
 * 
 * @author dlegland
 *
 */
public class LargestInscribedBall extends RegionAnalyzer3D<Sphere>
{
	// ==================================================
	// Static methods
	
	
	// ==================================================
	// Constructors
	
	public LargestInscribedBall()
	{
	}
	

	// ==================================================
	// Implementation of RegionAnalyzer interface

	/**
	 * Utility method that transforms the mapping between labels and inscribed
	 * ball instances into a ResultsTable that can be displayed with ImageJ.
	 * 
	 * @param map
	 *            the mapping between labels and inscribed ball
	 * @return a ResultsTable that can be displayed with ImageJ.
	 */
	public ResultsTable createTable(Map<Integer, Sphere> map)
	{
		// Initialize a new result table
		ResultsTable table = new ResultsTable();
	
		// Convert all results that were computed during execution of the
		// "computeGeodesicDistanceMap()" method into rows of the results table
		for (int label : map.keySet())
		{
			// current diameter
			Sphere ball = map.get(label);
			
			// add an entry to the resulting data table
			table.incrementCounter();
			table.addLabel(Integer.toString(label));
			
			// coordinates of ball center
			table.addValue("InscrBall.Center.X", ball.center().getX());
			table.addValue("InscrBall.Center.Y", ball.center().getY());
			table.addValue("InscrBall.Center.Z", ball.center().getZ());
			
			// ball radius
			table.addValue("InscrBall.Radius", ball.radius());
		}
	
		return table;
	}
	
	/**
	 * Computes largest inscribed ball of each particle. Particles must be
	 * disjoint.
	 * 
	 * @param labelImage
	 *            the 3D input image containing label of particles
	 * @param labels
	 *            the set of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array of Sphere representing the inscribed balls of each
	 *         region, in calibrated coordinates
	 */
	public Sphere[] analyzeRegions(ImageStack labelImage, int[] labels, Calibration calib)
    {
    	// compute max label within image
    	int nLabels = labels.length;
    	
		// first distance propagation to find an arbitrary center
    	fireStatusChanged(this, "Compute distance map");
		ImageStack distanceMap = LabelImages.distanceMap(labelImage);
		
		// Extract position of maxima
		fireStatusChanged(this, "Find inscribed balls center");
		Cursor3D[] posCenter;
		posCenter = LabelValues.findPositionOfMaxValues(distanceMap, labelImage, labels);
		float[] radii = getValues(distanceMap, posCenter);

		// Create result data table
		Sphere[] balls = new Sphere[nLabels];
		for (int i = 0; i < nLabels; i++) 
		{
			double xc = posCenter[i].getX() * calib.pixelWidth + calib.xOrigin;
			double yc = posCenter[i].getY() * calib.pixelHeight + calib.yOrigin;
			double zc = posCenter[i].getZ() * calib.pixelDepth + calib.zOrigin;
			Point3D center = new Point3D(xc, yc, zc);
			balls[i] = new Sphere(center, radii[i] * calib.pixelWidth);
		}

		return balls;
    }
	
	/**
	 * Get values in input image for each specified position.
	 */
	private final static float[] getValues(ImageStack image, 
			Cursor3D[] positions) 
	{
		float[] values = new float[positions.length];
		
		// iterate on positions
		for (int i = 0; i < positions.length; i++) 
		{
			values[i] = (float) image.getVoxel((int) positions[i].getX(),
					(int) positions[i].getY(), (int) positions[i].getZ());
		}
				
		return values;
	}
}
