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
package inra.ijpb.measure;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.HashMap;

import ij.IJ;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.InertiaEllipse;
import inra.ijpb.measure.region2d.LargestInscribedCircle;

/**
 * Provides a set of static methods to compute geometric measures such as area,
 * perimeter, and their densities in planar binary or label images.
 * 
 * @deprecated use IntrinsicVolumes2D instead
 * 
 * @see IntrinsicVolumes2DOld
 * 
 * @author David Legland
 *
 */
@Deprecated
public class GeometricMeasures2D 
{
	// ====================================================
	// Class constants

	public static boolean debug = false;

	// ====================================================
	// Main processing functions

	/**
	 * Private constructor to prevent class instantiation.
	 */
	private GeometricMeasures2D()
	{
	}
	
	/**
	 * Computes several morphometric features for each region in the input label
	 * image.
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param resol
	 *            the spatial resolution
	 * @return a data table containing the area, the perimeter and the
	 *         "circularity" of each labeled particle
	 */
	@Deprecated
	public static final ResultsTable analyzeRegions(ImageProcessor labelImage,
			double[] resol) 
	{
		return analyzeRegions(labelImage, resol, 4);
	}

	/**
	 * Computes several morphometric features for each region in the input label
	 * image and specifying number of directions to use for measuring perimeter.
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param resol
	 *            the spatial resolution
	 * @param nDirs
	 *            the number of directions for perimeter measure
	 * @return a data table containing the area, the perimeter and the
	 *         "circularity" of each labeled particle
	 */
	@Deprecated
	public static final ResultsTable analyzeRegions(ImageProcessor labelImage,
			double[] resol, int nDirs)
	{
		// Check validity of parameters
		if (labelImage == null)
			return null;

		// identify the labels
		int[] labels = LabelImages.findAllLabels(labelImage);
		int nbLabels = labels.length;

		// compute area and perimeter (use 4 directions by default)
//		double[] areas = area(labelImage, labels, resol);
//		double[] perims = croftonPerimeter(labelImage, labels, resol, nDirs);
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		double[] areas = IntrinsicVolumes2DOld.areas(labelImage, labels, calib);
		double[] perims = IntrinsicVolumes2DOld.perimeters(labelImage, labels, calib, nDirs);

		// Create data table, and add shape parameters
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < nbLabels; i++)
		{
			int label = labels[i];

			table.incrementCounter();
			table.addLabel(Integer.toString(label));

			table.addValue("Area", areas[i]);
			table.addValue("Perimeter", perims[i]);

			// Also compute circularity (ensure value is between 0 and 1)
			double p = perims[i];
			double circu = Math.min(4 * Math.PI * areas[i] / (p * p), 1);
			table.addValue("Circularity", circu);
			table.addValue("Elong.", 1. / circu);
		}
		IJ.showStatus("");

		return table;
	}

	/**
	 * Compute bounding box of each label in input stack and returns the result
	 * as a ResultsTable.
	 * 
	 * @deprecated use BoundingBox class instead
	 * 
	 * @see inra.ijpb.measure.region2d.BoundingBox#analyzeRegions(ImageProcessor, int[], Calibration)
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @return a data table containing for each labeled particle the extent in
	 *         each dimension
	 */
	@Deprecated
	public final static ResultsTable boundingBox(ImageProcessor labelImage) 
	{
		int[] labels = LabelImages.findAllLabels(labelImage);
		int nbLabels = labels.length;

		double[][] boxes = boundingBox(labelImage, labels);

		// Create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < nbLabels; i++)
		{
			table.incrementCounter();
			table.addLabel(Integer.toString(labels[i]));
			table.addValue("XMin", boxes[i][0]);
			table.addValue("XMax", boxes[i][1]);
			table.addValue("YMin", boxes[i][2]);
			table.addValue("YMax", boxes[i][3]);
		}

		return table;
	}
	
	/**
	 * Compute bounding box of each label in input stack and returns the result
	 * as an array of double for each label.
	 * 
	 * @deprecated use BoundingBox class instead
	 * 
	 * @see inra.ijpb.measure.region2d.BoundingBox#boundingBoxes(ImageProcessor, int[], Calibration)
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels an array of unique labels in image
	 * @return a data table containing for each labeled particle the extent in
	 *         each dimension
	 */
	@Deprecated
	public final static double[][] boundingBox(ImageProcessor labelImage, int[] labels)
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
		}

		
		// size of image
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();

		// iterate on image voxels to update bounding boxes
		IJ.showStatus("Compute Bounding boxes");
		for (int y = 0; y < sizeY; y++)
		{
			for (int x = 0; x < sizeX; x++)
			{
				int label = labelImage.get(x, y);
				// do not consider background
				if (label == 0)
					continue;
				int labelIndex = labelIndices.get(label);

				// update bounding box of current label
				boxes[labelIndex][0] = min(boxes[labelIndex][0], x);
				boxes[labelIndex][1] = max(boxes[labelIndex][1], x);
				boxes[labelIndex][2] = min(boxes[labelIndex][2], y);
				boxes[labelIndex][3] = max(boxes[labelIndex][3], y);
			}
		}
        
		IJ.showStatus("");
        return boxes;

	}
	
	/**
	 * Computes the area for each particle in the label image, taking into
	 * account image resolution.
	 * 
	 * @see inra.ijpb.measure.IntrinsicVolumes2DOld#areas(ImageProcessor, int[], Calibration)
	 * 
	 * @deprecated use {@link inra.ijpb.measure.IntrinsicVolumes2DOld#areas(ImageProcessor, int[], Calibration)} instead
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of unique labels in image
	 * @param resol
	 *            the size of a pixel in each direction
	 * @return the area of each region
	 */
	@Deprecated
	public static final double[] area(ImageProcessor image, int[] labels,
			double[] resol) {
		// pre-compute the area of individual voxel
		if (resol == null || resol.length != 2) {
			throw new IllegalArgumentException(
					"Resolution must be a double array of length 2");
		}

		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];

		return IntrinsicVolumes2DOld.areas(image, labels, calib);
	}

	/**
	 * Counts the number of pixel that composes the particle with given label.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param label
	 *            the label of the region to process
	 * @return the number of pixels of the specified region
	 */
	@Deprecated
	public static final int particleArea(ImageProcessor image, int label) 
	{
		int[] counts = LabelImages.pixelCount(image, new int[] {label});
		return counts[0];
	}

	/**
	 * Computes perimeter of each label using Crofton method.
	 * 
	 * @deprecated use analyzeRegions instead
	 * @see #analyzeRegions(ij.process.ImageProcessor, double[])
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param nDirs
	 *            the number of directions to process, either 2 or 4
	 * @param resol
	 *            the spatial resolution
	 * @return a data table containing the perimeter of each labeled particle
	 */
	@Deprecated
	public static final ResultsTable croftonPerimeter(
			ImageProcessor labelImage, double[] resol, int nDirs)
	{
		// Check validity of parameters
		if (labelImage == null)
			return null;

		int[] labels = LabelImages.findAllLabels(labelImage);
		int nbLabels = labels.length;

		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		double[] areas = IntrinsicVolumes2DOld.areas(labelImage, labels, calib);
		double[] perims = IntrinsicVolumes2DOld.perimeters(labelImage, labels, calib, nDirs);

		// Create data table
		ResultsTable table = new ResultsTable();
		for (int i = 0; i < nbLabels; i++) 
		{
			int label = labels[i];

			table.incrementCounter();
			table.addLabel(Integer.toString(label));

			table.addValue("Area", areas[i]);
			table.addValue("Perimeter", perims[i]);

			// Also compute circularity (ensure value is between 0 and 1)
			double p = perims[i];
			double circu = Math.min(4 * Math.PI * areas[i] / (p * p), 1);
			table.addValue("Circularity", circu);
			table.addValue("Elong.", 1. / circu);
		}
		IJ.showStatus("");

		return table;
	}

	/**
	 * Compute surface area for each label given in the "labels" argument.
	 * 
	 * Consists in calling the
	 * {@link inra.ijpb.measure.IntrinsicVolumes2DOld} class.
	 * 
	 * @deprecated replaced by {@link inra.ijpb.measure.IntrinsicVolumes2DOld}
	 *  
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of unique labels in image
	 * @param resol
	 *            the spatial resolution
	 * @param nDirs
	 *            the number of directions to process, either 2 or 4
	 * @return an array containing for each label, an estimate of the region
	 *         perimeter
	 */
	@Deprecated
	public final static double[] croftonPerimeter(ImageProcessor image,
			int[] labels, double[] resol, int nDirs)
	{
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		
		return IntrinsicVolumes2DOld.perimeters(image, labels, calib, nDirs);
	}


	/**
	 * Computes perimeter of each label using Crofton method with 2 directions.
	 * 
	 * Iterates over labels, then iterates over lines in horizontal and vertical
	 * directions. Slow...
	 * 
	 * Consists in calling the
	 * {@link inra.ijpb.measure.IntrinsicVolumes2DOld} class.
	 * 
	 * @deprecated replaced by {@link inra.ijpb.measure.IntrinsicVolumes2DOld}
	 *  
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of unique labels in image
	 * @param resol
	 *            the spatial resolution
	 * @return an array containing for each label, an estimate of the region
	 *         perimeter
	 */
	@Deprecated
	public static final double[] croftonPerimeterD2(ImageProcessor labelImage,
			int[] labels, double[] resol)
	{
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		
		return IntrinsicVolumes2DOld.perimeters(labelImage, labels, calib, 2);
	}

	/**
	 * Computes perimeter of each label using Crofton method with 4 directions
	 * (orthogonal and diagonal).
	 * 
	 * Iterates over labels, then iterates over lines in the four main directions. Slow...
	 * 
	 * Consists in calling the
	 * {@link inra.ijpb.measure.IntrinsicVolumes2DOld} class.
	 * 
	 * @deprecated replaced by {@link inra.ijpb.measure.IntrinsicVolumes2DOld}
	 *  
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of unique labels in image
	 * @param resol
	 *            the spatial resolution
	 * @return an array containing for each label, an estimate of the region perimeter
	 */
	@Deprecated
	public static final double[] croftonPerimeterD4(ImageProcessor labelImage,
			int[] labels, double[] resol)
	{
		Calibration calib = new Calibration();
		calib.pixelWidth = resol[0];
		calib.pixelHeight = resol[1];
		
		return IntrinsicVolumes2DOld.perimeters(labelImage, labels, calib, 4);
	}

	/**
	 * Computes porosity and perimeter density of binary image.
	 * 
	 * @param image
	 *            the input binary image
	 * @param resol
	 *            the spatial resolution
	 * @param nDirs the number of directions to consider, either 2 or 4 
	 * @return a results table containing for each label, an estimate of the region perimeter
	 */
	@Deprecated
	public static final ResultsTable perimeterDensity(ImageProcessor image,
			double[] resol, int nDirs) 
	{
		if (nDirs == 2)
			return perimeterDensity_D2(image, resol);
		else
			return perimeterDensity_D4(image, resol);
	}

	/**
	 * Computes perimeter density of the binary image using Crofton method with
	 * 2 directions (horizontal and vertical).
	 * 
	 * @param image
	 *            the input binary image
	 * @param resol
	 *            the spatial resolution
	 * @return an array containing for each label, an estimate of the region perimeter
	 */
	@Deprecated
	private static final ResultsTable perimeterDensity_D2(ImageProcessor image,
			double[] resol) 
	{

		// Create data table
		ResultsTable table = new ResultsTable();
		double perim;

		double d1 = resol[0];
		double d2 = resol[1];

		// area of a pixel (used for computing line densities)
		double pixelArea = d1 * d2;

		// compute weights associated to each direction
		double[] weights = computeDirectionWeightsD4(resol);

		double area = particleArea(image, 255) * pixelArea;

		// Count number of transitions in each direction
		int n1 = countTransitionsD00(image, 255, false);
		int n2 = countTransitionsD90(image, 255, false);

		// Compute weighted diameters and multiplies by associated
		// direction weights
		double wd1 = n1 / d1 * weights[0];
		double wd2 = n2 / d2 * weights[1];

		// Compute perimeter
		perim = (wd1 + wd2) * pixelArea * Math.PI / 2;

		// Add new row in table
		table.incrementCounter();

		// area
		table.addValue("Area", area);

		// Compute porosity by dividing by observed area
		int pixelCount = image.getWidth() * image.getHeight();
		double refArea = pixelCount * pixelArea;
		table.addValue("A. Density", area / refArea);

		if (debug) 
		{
			// Display individual counts
			table.addValue("N1", n1);
			table.addValue("N2", n2);
		}

		// Display perimeter value
		table.addValue("Perimeter", perim);

		// compute perimeter density
		double refArea2 = (image.getWidth() - 1) * resol[0]
				* (image.getHeight() - 1) * resol[1];
		double perimDensity = perim / refArea2;
		table.addValue("P. Density", perimDensity);

		return table;
	}

	/**
	 * Computes perimeter density of the binary image using Crofton method with
	 * 4 directions (orthogonal and diagonal).
	 * 
	 * @param image
	 *            the input binary image
	 * @param resol
	 *            the spatial resolution
	 * @return an array containing for each label, an estimate of the region perimeter
	 */
	private static final ResultsTable perimeterDensity_D4(ImageProcessor image,
			double[] resol) 
	{
		// Create data table
		ResultsTable table = new ResultsTable();
		double perim;

		double d1 = resol[0];
		double d2 = resol[1];
		double d12 = Math.hypot(d1, d2);

		// area of a pixel (used for computing line densities)
		double pixelArea = d1 * d2;

		// compute weights associated to each direction
		double[] weights = computeDirectionWeightsD4(resol);

		double area = particleArea(image, 255) * pixelArea;

		// Count number of transitions in each direction
		int n1 = countTransitionsD00(image, 255, false);
		int n2 = countTransitionsD90(image, 255, false);
		int n3 = countTransitionsD45(image, 255, false);
		int n4 = countTransitionsD135(image, 255, false);

		// Compute weighted diameters and multiplies by associated
		// direction weights
		double wd1 = (n1 / d1) * weights[0];
		double wd2 = (n2 / d2) * weights[1];
		double wd3 = (n3 / d12) * weights[2];
		double wd4 = (n4 / d12) * weights[3];

		// Compute perimeter
		perim = (wd1 + wd2 + wd3 + wd4) * pixelArea * Math.PI / 2;

		// Add new row in table
		table.incrementCounter();

		// area
		table.addValue("Area", area);

		// Compute porosity by dividing by observed area
		int pixelCount = image.getWidth() * image.getHeight();
		double refArea = pixelCount * pixelArea;
		table.addValue("A. Density", area / refArea);

		if (debug) 
		{
			// Display individual counts
			table.addValue("N1", n1);
			table.addValue("N2", n2);
			table.addValue("N3", n3);
			table.addValue("N4", n4);
		}

		// Display perimeter value
		table.addValue("Perimeter", perim);

		// compute perimeter density
		double refArea2 = (image.getWidth() - 1) * (image.getHeight() - 1)
				* pixelArea;
		double perimDensity = perim / refArea2;
		table.addValue("P. Density", perimDensity);

		return table;
	}

	/**
	 * Counts the number of transitions in the horizontal direction, by counting
	 * +1 when the structure touches image edges.
	 */
	private static final int countTransitionsD00(ImageProcessor image,
			int label, boolean countBorder) 
	{
		int width = image.getWidth();
		int height = image.getHeight();

		int count = 0;
		int previous = 0;
		int current;

		// iterate on image pixels
		for (int y = 0; y < height; y++)
		{
			// Count border of image
			previous = (int) image.getf(0, y);
			if (countBorder && previous == label)
				count++;

			// count middle of image
			for (int x = 0; x < width; x++) {
				current = (int) image.getf(x, y);
				if (previous == label ^ current == label) // Exclusive or
					count++;
				previous = current;
			}

			// Count border of image
			if (countBorder && previous == label)
				count++;
		}

		return count;
	}

	/**
	 * Counts the number of transitions in the horizontal direction, by counting
	 * 1 when structure touches image edges.
	 */
	private static final int countTransitionsD90(ImageProcessor image,
			int label, boolean countBorder) 
	{
		int width = image.getWidth();
		int height = image.getHeight();

		int count = 0;
		int previous = 0;
		int current;

		// iterate on image pixels
		for (int x = 0; x < width; x++) 
		{
			// Count border of image
			previous = (int) image.getf(x, 0);
			if (countBorder && previous == label)
				count++;

			// count middle of image
			for (int y = 0; y < height; y++) 
			{
				current = (int) image.getf(x, y);
				if (previous == label ^ current == label) // Exclusive or
					count++;
				previous = current;
			}

			if (countBorder && previous == label)
				count++;
		}

		return count;
	}

	/**
	 * Counts the number of transitions in the upper diagonal direction, by
	 * counting 1 when structure touches image edges.
	 */
	private static final int countTransitionsD45(ImageProcessor image,
			int label, boolean countBorder) 
	{
		int width = image.getWidth();
		int height = image.getHeight();
		int nDiags = width + height - 1;

		int count = 0;
		int previous = 0;
		int current;

		// iterate on image pixels
		for (int i = 0; i < nDiags; i++)
		{
			int x0 = Math.max(width - i - 1, 0);
			int x1 = Math.min(nDiags - i, width);
			int y0 = Math.max(i + 1 - width, 0);
			int y1 = Math.min(i + 1, height);

			// Count first line border
			previous = (int) image.getf(x0, y0);
			if (countBorder && previous == label)
				count++;

			// number of pixels on the diagonal line
			int lineLength = x1 - x0;
			int lineLength2 = y1 - y0;
			assert lineLength == lineLength2 : "Bounds must be equal (upper diagonal "
					+ i + ")";

			// count middle of line
			for (int j = 1; j < lineLength; j++) 
			{
				current = (int) image.getf(x0 + j, y0 + j);
				if (previous == label ^ current == label) // Exclusive or
					count++;
				previous = current;
			}

			// Count last line border
			if (countBorder && previous == label)
				count++;
		}

		return count;
	}

	/**
	 * Counts the number of transitions in the lower diagonal direction, by
	 * counting 1 when structure touches image edges.
	 */
	private static final int countTransitionsD135(ImageProcessor image,
			int label, boolean countBorder)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		int nDiags = width + height - 1;

		int count = 0;
		int previous = 0;
		int current;

		// iterate on image pixels
		for (int i = 0; i < nDiags; i++)
		{
			int x0 = Math.max(i + 1 - height, 0);
			int x1 = Math.min(i, width - 1);
			int y0 = Math.min(i, height - 1);
			int y1 = Math.max(i + 1 - width, 0);

			// Count first line border
			previous = (int) image.getf(x0, y0);
			if (countBorder && previous == label)
				count++;

			// number of pixels on the diagonal line
			int lineLength = x1 - x0;
			int lineLength2 = y0 - y1;
			assert lineLength == lineLength2 : "Bounds must be equal (lower diagonal "
					+ i + ")";

			// count middle of line
			for (int j = 1; j <= lineLength; j++)
			{
				current = (int) image.getf(x0 + j, y0 - j);
				if (previous == label ^ current == label) // Exclusive or
					count++;
				previous = current;
			}

			// Count last line border
			if (countBorder && previous == label)
				count++;
		}

		return count;
	}

	/**
	 * Computes a set of weights for the four main directions (orthogonal plus
	 * diagonal) in discrete image. The sum of the weights equals 1.
	 * 
	 * @param resol
	 *            an array with the resolution in x and y directions
	 * @return the set of normalized weights
	 */
	private static final double[] computeDirectionWeightsD4(double[] resol) 
	{
		// resolution in each direction
		double d1 = resol[0];
		double d2 = resol[1];

		// angle of the diagonal
		double theta = Math.atan2(d2, d1);

		// angular sector for direction 1 ([1 0])
		double alpha1 = theta / Math.PI;

		// angular sector for direction 2 ([0 1])
		double alpha2 = (Math.PI / 2.0 - theta) / Math.PI;

		// angular sector for directions 3 and 4 ([1 1] and [-1 1])
		double alpha34 = .25;

		// concatenate the different weights
		return new double[] { alpha1, alpha2, alpha34, alpha34 };
	}

	/**
	 * Compute centroid of each label in input stack and returns the result as
	 * an array of double for each label.
	 * 
	 * @deprecated replaced by Centroid.centroids
	 * 
	 * @see inra.ijpb.measure.region2d.Centroid#centroids(ImageProcessor, int[])
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of unique labels in image the number of directions
	 *            to process, either 2 or 4
	 * @return an array containing for each label, the coordinates of the
	 *         centroid, in pixel coordinates
	 */
	@Deprecated
	public final static double[][] centroids(ImageProcessor labelImage,
			int[] labels) 
	{
		// create associative array to know index of each label
		int nLabels = labels.length;
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

		// allocate memory for result
		int[] counts = new int[nLabels];
		double[][] centroids = new double[nLabels][2];

		// compute centroid of each region
		int sizeX = labelImage.getWidth();
		int sizeY = labelImage.getHeight();
		for (int y = 0; y < sizeY; y++) 
		{
			for (int x = 0; x < sizeX; x++)
			{
				int label = (int) labelImage.getf(x, y);
				if (label == 0)
					continue;

				// do not process labels that are not in the input list 
				if (!labelIndices.containsKey(label))
					continue;
				
				int index = labelIndices.get(label);
				centroids[index][0] += x;
				centroids[index][1] += y;
				counts[index]++;
			}
		}

		// normalize by number of pixels in each region
		for (int i = 0; i < nLabels; i++)
		{
			centroids[i][0] /= counts[i];
			centroids[i][1] /= counts[i];
		}

		return centroids;
	}
	
	/**
	 * Computes inertia ellipse of each region in input label image.
	 * 
	 * @deprecated use inra.ijpb.measure.region2d.InertiaEllipse instead
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @return an ResultsTable containing for each label, the parameters of the
	 *         inertia ellipsoid, in pixel coordinates
	 */
	@Deprecated
	public final static ResultsTable inertiaEllipse(ImageProcessor image)
	{
		// Check validity of parameters
		if (image == null)
			return null;

		Calibration calib = new Calibration();
		InertiaEllipse op = new InertiaEllipse();
		return op.createTable(op.analyzeRegions(image, calib));
	}

	/**
	 * Computes radius and center of maximum inscribed disk of each particle. 
	 * Particles must be disjoint.
	 *
	 * @deprecated use class inra.ijpb.measure.region2d.largestInscribedCircle instead
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @return a ResultsTable with as many rows as the number of unique labels
	 *         in label image, and columns "Label", "xi", "yi" and "Radius".
	 */
    @Deprecated
    public final static ResultsTable maximumInscribedCircle(ImageProcessor labelImage)
    {
    	return maximumInscribedCircle(labelImage, new double[]{1, 1});
    }
    
	/**
	 * Radius of maximum inscribed disk of each particle. Particles must be
	 * disjoint.
	 * 
	 * @deprecated use class inra.ijpb.measure.region2d.largestInscribedCircle instead
	 * 
	 * @param labelImage
	 *            the input image containing label of particles
	 * @param resol an array containing the size of the pixel in each direction
	 * @return a ResultsTable with as many rows as the number of unique labels
	 *         in label image, and columns "Label", "xi", "yi" and "Radius".
	 */
    @Deprecated
    public final static ResultsTable maximumInscribedCircle(ImageProcessor labelImage, 
    		double[] resol)
    {
    	Calibration calib = new Calibration();
    	calib.pixelWidth = resol[0];
    	calib.pixelHeight = resol[1];
    	
    	LargestInscribedCircle op = new LargestInscribedCircle();
		return op.createTable(op.analyzeRegions(labelImage, calib));
    }
}
