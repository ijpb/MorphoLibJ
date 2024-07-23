/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
package inra.ijpb.measure.region2d;

import java.util.Map;
import java.util.TreeMap;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.RegionAnalyzer;

/**
 * Base implementation of RegionAnalyzer interface for planar binary/label
 * images.
 *
 * @param <T>
 *            the type of the data computed for each region. May be a class
 *            instance, or a single Numeric type.
 *
 * @see inra.ijpb.measure.region3d.RegionAnalyzer3D
 * 
 * @author dlegland
 *
 */
public abstract class RegionAnalyzer2D<T> extends AlgoStub implements RegionAnalyzer<T>
{
	/**
	 * Computes an instance of the generic type T for each region in input label image.
	 * 
	 * @param image
	 *            the input image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array of the type used to represent the analysis result of each region 
	 */
	public abstract T[] analyzeRegions(ImageProcessor image, int[] labels, Calibration calib);
	
	/**
	 * Identifies labels within image and computes an instance of the generic
	 * type T for each region in input label image.
	 * 
	 * @param image
	 *            a label image (8, 16 or 32 bits)
	 * @param calib
	 *            the spatial calibration of the image
	 * @return a map between the region label and the result of analysis for
	 *         each region
	 */
	public Map<Integer, T> analyzeRegions(ImageProcessor image, Calibration calib)
	{
		// extract particle labels
		fireStatusChanged(this, "Find Labels");
		int[] labels = LabelImages.findAllLabels(image);
		int nLabels = labels.length;
		
		// compute analysis result for each label
		fireStatusChanged(this, "Analyze regions");
		T[] results = analyzeRegions(image, labels, calib);

		// encapsulate into map
		fireStatusChanged(this, "Convert to map");
		Map<Integer, T> map = new TreeMap<Integer, T>();
		for (int i = 0; i < nLabels; i++)
		{
			map.put(labels[i], results[i]);
		}

        return map;
	}
	

	/**
	 * Default implementation of the analyzeRegions method, that calls the more
	 * specialized {@link #analyzeRegions(ImageProcessor, int[], Calibration)}
	 * method and transforms the result into a map.
	 * 
	 * @param labelPlus
	 *            the input image containing label of regions
	 * @return the mapping between region label and result of analysis for each
	 *         region
	 */
	public Map<Integer, T> analyzeRegions(ImagePlus labelPlus)
	{
		ImageProcessor labelImage = labelPlus.getProcessor();
		int[] labels = LabelImages.findAllLabels(labelImage);
		
		T[] results = analyzeRegions(labelImage, labels, labelPlus.getCalibration());
		
		// convert the arrays into a map of index-value pairs
		Map<Integer, T> map = new TreeMap<Integer, T>();
		for (int i = 0; i < labels.length; i++)
		{
			map.put(labels[i], results[i]);
		}
		
		return map;
	}
}
