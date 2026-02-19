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
package inra.ijpb.measure;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.HashMap;

import ij.IJ;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.data.Cursor3D;
import inra.ijpb.geometry.Ellipsoid;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region3d.BoundingBox3D;
import inra.ijpb.measure.region3d.InertiaEllipsoid;
import inra.ijpb.measure.region3d.LargestInscribedBall;

/**
 * Provides a set of static methods to compute geometric measures in 3D binary
 * or label images.
 * 
 * Ideally, each parameter should provide two methods to measure it:
 * <ul>
 * <li>a method with two (or more) input parameters: the label image and the
 * resolution, and which returns a ResultsTable</li>
 * <li>a method with three (or more) input parameters: the label image, the list
 * of labels to consider, and the resolution, and which returns an array of
 * double, with as many elements as the number of labels</li>
 * </ul>
 * 
 * <p>
 * Example of code:
 * 
 * <pre>
 * {@code
 *  ImageStack labelImage = ...
 *  int[] labels = LabelImages.findAllLabels(image);
 *  double[] resol = new double[]{1, 1, 1};
 *  double[][] ellipsoids = GeometricMeasures3D.inertiaEllipsoid(labelImage,
 *  	labels, resol);
 *  double[][] elongations = GeometricMeasures3D.computeEllipsoidElongations(ellipsoids);
 * }
 * </pre>
 * 
 * @deprecated use IntrinsicVolumes3D instead
 * 
 * @author David Legland
 *
 */
@Deprecated
public class GeometricMeasures3D 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private GeometricMeasures3D()
	{
	}
	
	/**
	 * Computes bounding box of each label in input stack and returns the result
	 * as a ResultsTable.
	 * 
	 * @deprecated use BoundingBox3D instead
	 * 
	 * @param labelImage
	 *            a 3D image containing label of particles or regions
	 * @return a new ResultsTable containing for each label, the extent of the
	 *         corresponding region
	 */
	@Deprecated
	public final static ResultsTable boundingBox(ImageStack labelImage) 
	{
		Calibration calib = new Calibration();
		BoundingBox3D algo = new BoundingBox3D();
		return algo.createTable(algo.analyzeRegions(labelImage, calib));
	}
	
	/**
	 * Computes bounding box of each label in input stack and returns the result
	 * as an array of double for each label.
	 * 
	 * @param labelImage
	 *            a 3D image containing label of particles or regions
	 * @param labels the set of labels present in image
	 * @return a new array of doubles  containing for each label, the extent of the
	 *         corresponding region
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
		IJ.showStatus("Compute Bounding boxes");
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
					boxes[labelIndex][1] = max(boxes[labelIndex][1], x);
					boxes[labelIndex][2] = min(boxes[labelIndex][2], y);
					boxes[labelIndex][3] = max(boxes[labelIndex][3], y);
					boxes[labelIndex][4] = min(boxes[labelIndex][4], z);
					boxes[labelIndex][5] = max(boxes[labelIndex][5], z);
        		}
        	}
        }
        
		IJ.showStatus("");
        return boxes;

	}
	
	/**
	 * Measures the volume of each particle in a 3D label image.
	 * 
	 * @deprecated used IntrinsicVolumes3D instead
	 * 
	 * @param labelImage image containing the label of each particle
	 * @param resol image resolution, as a double array with 3 elements
	 * @return the volume of each particle in the image
	 */
	@Deprecated
	public final static ResultsTable volume(ImageStack labelImage, double[] resol) 
	{
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		calib.pixelDepth = resol[2];
		
		IJ.showStatus("Compute volume...");
		int[] labels = LabelImages.findAllLabels(labelImage);
		int nbLabels = labels.length;

		double[] volumes = IntrinsicVolumes3D.volumes(labelImage, labels, calib);

		// Create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < nbLabels; i++) 
		{
			table.incrementCounter();
			table.addLabel(Integer.toString(labels[i]));
			table.addValue("Volume", volumes[i]);
		}

		IJ.showStatus("");
		return table;
	}
	
	/**
	 * Measures the volume of each particle in the 3D label image.
	 *
	 * @deprecated use IntrinsicVolumes3D instead
	 * 
	 * @param labelImage image containing the label of each particle
	 * @param labels the set of labels for which volume has to be computed
	 * @param resol image resolution, as a double array with 3 elements
	 * @return the volume of each particle in the image
	 */
	@Deprecated
	public final static double[] volume(ImageStack labelImage, int[] labels, double[] resol)
	{
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		calib.pixelDepth = resol[2];
		
		return IntrinsicVolumes3D.volumes(labelImage, labels, calib);
	}
	
	/**
	 * Helper function that computes the sphericity index of 3D particles, based
	 * on the value of volume and surface area.
	 * 
	 * The sphericity is computed using the following formula: <code>
	 * sphericity = 36 * PI * V^2 / S^3
	 * </code>
	 * 
	 * A perfect ball would have a sphericity index close to 1, a very complex
	 * particle will present a lower sphericity index.
	 * 
	 * @deprecated use IntrinsicVolumes3D instead
	 * 
	 * @param volumes
	 *            the volume of each particle
	 * @param surfaces
	 *            the surface area of each particle
	 * @return the sphericity index of each particle
	 * 
	 * @see #surfaceArea(ImageStack, double[], int)
	 * @see #volume(ImageStack, int[], double[])
	 */
	@Deprecated
	public final static double[] computeSphericity(double[] volumes, double[] surfaces) 
	{
		return IntrinsicVolumes3D.sphericity(volumes, surfaces);
	}
	
	/**
	 * Computes the surface area of each label in the 3D image, using the
	 * specified resolution, and the given number of directions.
	 * 
	 * Current algorithms pre-computes a LUT, then iterate on 2-by-2-by-2
	 * configurations of voxels, and identifies the labels whose surface area
	 * measure need to be updated.
	 * 
	 * For 3 directions, the surfaceAreaD3 function is an alternative that does
	 * not uses LUT.
	 * 
	 * @param labelImage
	 *            image containing the label of each particle
	 * @param resol
	 *            image resolution, as a double array with 3 elements
	 * @param nDirs
	 *            the number of directions to consider, either 3 or 13
	 * @return the surface area of each particle in the image
	 */
	public final static ResultsTable surfaceArea(ImageStack labelImage, 
			double[] resol, int nDirs)
	{
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		calib.pixelDepth = resol[2];
		
		IJ.showStatus("Count labels...");
		int[] labels = LabelImages.findAllLabels(labelImage);
		int nbLabels = labels.length;

		// Compute surface area of ach label
		double[] surfaces = IntrinsicVolumes3D.surfaceAreas(labelImage, labels, calib, nDirs);

		// Create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < nbLabels; i++)
		{
			table.incrementCounter();
			table.addLabel(Integer.toString(labels[i]));
			table.addValue("SurfaceArea", surfaces[i]);
		}

		IJ.showStatus("");
		return table;
	}

	/**
	 * Computes surface area for each label given in the "labels" argument.
	 * 
	 * @deprecated use IntrinsicVolumes3D instead
	 * 
	 * @param image
	 *            image containing the label of each particle
	 * @param labels
	 *            the set of labels in the image
	 * @param resol
	 *            image resolution, as a double array with 3 elements
	 * @param nDirs
	 *            the number of directions to consider, either 3 or 13
	 * @return the surface area of each particle in the image
	 */
	@Deprecated
	public final static double[] surfaceAreaCrofton(ImageStack image, int[] labels, 
			double[] resol, int nDirs)
	{
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		calib.pixelDepth = resol[2];
		
		return IntrinsicVolumes3D.surfaceAreas(image, labels, calib, nDirs);
	}
	
	/**
	 * Computes surface area for a single label in the image, using
	 * discretization of the Crofton formula. This can be useful for binary
	 * images by using label 255.
	 * 
	 * @deprecated use IntrinsicVolumes3D instead
	 * 
	 * @param image
	 *            the input 3D label image (with labels having integer values)
	 * @param label
	 *            the value of the label to measure
	 * @param resol
	 *            the resolution of the image, in each direction
	 * @param nDirs
	 *            the number of directions to consider for computing surface (3
	 *            or 13)
	 * @return the surface area measured for the given label
	 */
	@Deprecated
	public final static double surfaceAreaCrofton(ImageStack image, int label, 
			double[] resol, int nDirs) 
	{
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		calib.pixelDepth = resol[2];
		
		image = LabelImages.cropLabel(image, label, 0);
		return IntrinsicVolumes3D.surfaceArea(image, calib, nDirs);
	}
	
	/**
	 * Computes surface area of a binary image using 3 directions.
	 * 
	 * @deprecated use IntrinsicVolumes3D instead
	 * 
	 * @param image
	 *            the input 3D label image (with labels having integer values)
	 * @param resol
	 *            the resolution of the image, in each direction
	 * @return the surface area measured for the binary image
	 */
	@Deprecated
	public final static double surfaceAreaCroftonD3(ImageStack image, double[] resol) 
	{
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		calib.pixelDepth = resol[2];
		return IntrinsicVolumes3D.surfaceArea(image, calib, 3);
	}

	/**
	 * Computes Euler number for each label given in the "labels" argument,
	 * using the specified connectivity.
	 *
	 * @deprecated use
	 *             {@link IntrinsicVolumes3D#eulerNumbers(ImageStack, int[], int)}
	 *             instead
	 * 
	 * @see IntrinsicVolumes3D#eulerNumbers(ImageStack, int[], int)
	 * 
	 * @param image
	 *            the input 3D label image (with labels having integer values)
	 * @param labels
	 *            the set of unique labels in image
	 * @param conn
	 *            the connectivity to use (either 6 or 26)
	 * @return the Euler-Poincare characteristic of each region
	 */
	@Deprecated
	public static final double[] eulerNumber(ImageStack image, int[] labels,
			int conn)
	{    
		return IntrinsicVolumes3D.eulerNumbers(image, labels, conn);
	}

	/**
	 * Computes centroid of each label in input stack and returns the result
	 * as an array of double for each label.
	 * 
	 * @deprecated use {@link inra.ijpb.measure.region3d.Centroid3D#centroids(ImageStack, int[])} instead
	 * 
	 * @param labelImage an instance of ImageStack containing region labels
	 * @param labels the set of indices contained in the image
	 * @return the centroid of each region, as an array of double[3]
	 */
	@Deprecated
	public final static double[][] centroids(ImageStack labelImage,
			int[] labels) 
	{
		// create associative array to know index of each label
		int nLabels = labels.length;
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// allocate memory for result
		int[] counts = new int[nLabels];
		double[][] centroids = new double[nLabels][3];

		// compute centroid of each region
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		int sizeZ = labelImage.getSize();
		for (int z = 0; z < sizeZ; z++) 
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++)
				{
					int label = (int) labelImage.getVoxel(x, y, z);
					if (label == 0)
						continue;

					// do not process labels that are not in the input list 
					if (!labelIndices.containsKey(label))
						continue;
					
					// increment centroid and count for current label
					int index = labelIndices.get(label);
					centroids[index][0] += x;
					centroids[index][1] += y;
					centroids[index][2] += z;
					counts[index]++;
				}
			}
		}

		// normalize by number of pixels in each region
		for (int i = 0; i < nLabels; i++)
		{
			centroids[i][0] /= counts[i];
			centroids[i][1] /= counts[i];
			centroids[i][2] /= counts[i];
		}

		return centroids;
	}

	/**
     * Computes inertia ellipsoid of each 3D region in input 3D label image.
     * 
	 * @deprecated use {@link inra.ijpb.measure.region3d.InertiaEllipsoid} instead
	 * 
	 * @param image an instance of ImageStack containing region labels
	 * @return the parameters of the inertia ellipsoid for each region
	 *
     * @throws RuntimeException if jama package is not found.
     */
	@Deprecated
    public final static ResultsTable inertiaEllipsoid(ImageStack image)
    {
    	return inertiaEllipsoid(image, new double[]{1, 1, 1});
    }

    	
	/**
	 * <p>
	 * Computes inertia ellipsoid of each 3D region in input 3D label image.
	 * </p>
	 * 
	 * <p>
	 * The result is given as a ResultsTable with as many rows as the number of
	 * labels, and 9 columns. Columns correspond to the centroid coordinates (3
	 * values), the radius of the ellipsoid (3 values), and the orientation,
	 * given as azimut, elevation, and roll angles, in degrees (3 values).
	 * 
	 * @deprecated use {@link inra.ijpb.measure.region3d.InertiaEllipsoid} instead
	 * 
	 * @param image
	 *            an instance of ImageStack containing region labels
	 * @param resol
	 *            the resolution of the image, in each direction
	 * @return the parameters of the inertia ellipsoid for each region
	 * 
	 * @throws RuntimeException
	 *             if jama package is not found.
	 */
    @Deprecated
    public final static ResultsTable inertiaEllipsoid(ImageStack image, double[] resol)
    {
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		calib.pixelDepth = resol[2];

		InertiaEllipsoid algo = new InertiaEllipsoid();
        return algo.createTable(algo.analyzeRegions(image, calib));
    }

    /**
	 * <p>
	 * Computes inertia ellipsoid from input 3D label image for each specified
	 * region label.
	 * </p>
	 * 
	 * <p>
	 * The result is given as an array of double with as many rows as the number
	 * of labels, and 9 columns. Columns correspond to the centroid coordinates
	 * (3 values), the radius of the ellipsoid (3 values), and the orientation,
	 * given as azimut, elevation, and roll angles, in degrees (3 values).
	 * </p>
	 * 
	 * <pre><code>
	 * ImageStack labelImage = ...
	 * int[] labels = LabelImages.findAllLabels(image);
	 * double[] resol = new double[]{1, 1, 1};
	 * double[][] ellipsoids = GeometricMeasures3D.inertiaEllipsoid(labelImage,
	 * 		labels, resol);
	 * double[][] elongations = GeometricMeasures3D.computeEllipsoidElongations(ellipsoids);
	 * </code></pre>
	 *
	 * @deprecated use {@link inra.ijpb.measure.region3d.InertiaEllipsoid} instead
	 * 
	 * @param image
	 *            input image containing label of each particle
	 * @param labels
	 *            the list of labels for which we want to compute inertia
	 *            ellipsoid
	 * @param resol
	 *            the spatial resolution, as an array of length 3.
	 * @return an array with as many rows as the number of labels, and 9 columns
	 * @throws RuntimeException
	 *             if jama package is not found.
	 *             
	 */
    @Deprecated
	public static final double[][] inertiaEllipsoid(ImageStack image,
			int[] labels, double[] resol)
	{
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		calib.pixelDepth = resol[2];

		Ellipsoid[] ellipsoids = InertiaEllipsoid.inertiaEllipsoids(image, labels, calib);
		
		int nLabels = labels.length;
		double[][] results = new double[nLabels][9];
		for (int i = 0; i < nLabels; i++)
		{
			Ellipsoid elli = ellipsoids[i];
			results[i][0] = elli.center().getX();
			results[i][1] = elli.center().getY();
			results[i][2] = elli.center().getZ();
			results[i][3] = elli.radius1();
			results[i][4] = elli.radius2();
			results[i][5] = elli.radius3();
			results[i][6] = elli.phi();
			results[i][7] = elli.theta();
			results[i][8] = elli.psi();
		}
		return results;
    }
    
	/**
	 * Computes the three elongation factors for an array of ellipsoids.
	 * 
	 * <pre><code>
	 * ImageStack labelImage = ...
	 * int[] labels = LabelImages.findAllLabels(image);
	 * double[] resol = new double[]{1, 1, 1};
	 * double[][] ellipsoids = GeometricMeasures3D.inertiaEllipsoid(labelImage,
	 * 		labels, resol);
	 * double[][] elongations = GeometricMeasures3D.computeEllipsoidElongations(ellipsoids);
	 * </code></pre>
	 * 
	 * @deprecated use {@link Ellipsoid#elongations(Ellipsoid[])} instead
	 * 
	 * @param ellipsoids
	 *            an array of ellipsoids, with radius data given in columns 3,
	 *            4, and 5
	 * @return an array of elongation factors. When radii are ordered such that
	 *         R1 &gt; R2 &gt; R3, the three elongation factors are defined by
	 *         ratio of R1 by R2, ratio of R1 by R3, and ratio of R2 by R3.
	 *         
	 * @see #inertiaEllipsoid(ImageStack, double[])
	 * @see #inertiaEllipsoid(ImageStack, int[], double[])
	 */
    @Deprecated
	public static final double[][] computeEllipsoidElongations(double[][] ellipsoids)
    {
		int nLabels = ellipsoids.length;
    	double[][] res = new double[nLabels][3];
    	
    	for (int i = 0; i < nLabels; i++)
    	{
    		double ra = ellipsoids[i][3];
    		double rb = ellipsoids[i][4];
    		double rc = ellipsoids[i][5];
    		
    		res[i][0] = ra / rb;
    		res[i][1] = ra / rc;
    		res[i][2] = rb / rc;
    	}
    	
    	return res;
    }
    
    /**
	 * Radius of maximum inscribed sphere of each particle within a label image.
	 * 
	 * @deprecated use LargestInscribedBall instead
	 * 
	 * @param labelImage
	 *            input image containing label of each particle
	 * @param resol
	 *            the spatial resolution, as an array of length 3.
	 * @return a ResultsTable with as many rows as the number of labels, and 4
	 *         columns (xi, yi, zi, radius)
	 */
	@Deprecated
    public final static ResultsTable maximumInscribedSphere(ImageStack labelImage, 
    		double[] resol)
    {
    	Calibration calib = new Calibration();
    	calib.pixelWidth = resol[0];
    	calib.pixelHeight = resol[1];
    	calib.pixelDepth = resol[2];
    	LargestInscribedBall algo = new LargestInscribedBall();
    	
    	return algo.createTable(algo.analyzeRegions(labelImage, calib));
    }

	/**
	 * Radius of maximum inscribed sphere of each particle within a label image.
	 * 
	 * @param labelImage
	 *            input image containing label of each particle
	 * @param labels
	 *            the list of labels for which we want to compute inertia
	 *            ellipsoid
	 * @param resol
	 *            the spatial resolution, as an array of length 3.
	 * @return an array with as many rows as the number of labels, and 4 columns
	 *         (xi, yi, zi, radius)
	 */
    public final static double[][] maximumInscribedSphere(ImageStack labelImage, 
    		int[] labels, double[] resol)
    {
    	// compute max label within image
    	int nbLabels = labels.length;
    	
    	// Initialize mask as binarisation of labels
		ImageStack mask = BinaryImages.binarize(labelImage);

		// first distance propagation to find an arbitrary center
		ImageStack distanceMap = BinaryImages.distanceMap(mask);
		
		// Extract position of maxima
		Cursor3D[] posCenter;
		posCenter = findPositionOfMaxValues(distanceMap, labelImage, labels);
		float[] radii = getValues(distanceMap, posCenter);

		// Create result data table
		double[][] res = new double[nbLabels][4];
		for (int i = 0; i < nbLabels; i++) 
		{
			res[i][0] = posCenter[i].getX() * resol[0];
			res[i][1] = posCenter[i].getY() * resol[1];
			res[i][2] = posCenter[i].getZ() * resol[2];
			res[i][3] = radii[i] * resol[0];
		}

		return res;
    }

	/**
	 * Finds one position of maximum value within each label.
	 * 
	 * @param image
	 *            the input image containing the value (for example a distance 
	 *            map)
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels
	 *            the set of labels contained in the label image
	 *            
	 */
	private final static Cursor3D[] findPositionOfMaxValues(ImageStack image,
			ImageStack labelImage, int[] labels)
	{
		int width 	= labelImage.getWidth();
		int height 	= labelImage.getHeight();
		int depth 	= labelImage.getSize(); 
		
		// Compute value of greatest label
		int nbLabel = labels.length;
		int maxLabel = 0;
		for (int i = 0; i < nbLabel; i++)
		{
			maxLabel = Math.max(maxLabel, labels[i]);
		}
		
		// init index of each label
		// to make correspondence between label value and label index
		int[] labelIndex = new int[maxLabel+1];
		for (int i = 0; i < nbLabel; i++)
		{
			labelIndex[labels[i]] = i;
		}
		
		// Init Position and value of maximum for each label
		Cursor3D[] posMax 	= new Cursor3D[nbLabel];
		double[] maxValues = new double[nbLabel];
		for (int i = 0; i < nbLabel; i++) 
		{
			maxValues[i] = -1;
			posMax[i] = new Cursor3D(-1, -1, -1);
		}
		
		// store current value
		double value;
		int index;
		
		// iterate on image pixels
		for (int z = 0; z < depth; z++) 
		{
			for (int y = 0; y < height; y++) 
			{
				for (int x = 0; x < width; x++) 
				{
					int label = (int) labelImage.getVoxel(x, y, z);

					// do not process pixels that do not belong to particle
					if (label==0)
						continue;

					index = labelIndex[label];

					// update values and positions
					value = image.getVoxel(x, y, z);
					if (value > maxValues[index])
					{
						posMax[index].set(x, y, z);
						maxValues[index] = value;
					}
				}
			}
		}

		return posMax;
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
