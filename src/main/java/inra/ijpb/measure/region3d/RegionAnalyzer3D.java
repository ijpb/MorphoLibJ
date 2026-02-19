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
/**
 * 
 */
package inra.ijpb.measure.region3d;

import java.util.Map;
import java.util.TreeMap;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.edit.FindAllLabels;
import inra.ijpb.measure.RegionAnalyzer;

/**
 * Base implementation of RegionAnalyzer interface for 3D binary/label
 * images.
 *
 * @param <T>
 *            the type of the data computed for each region. May be a class
 *            instance, or a single Numeric type.
 *
 * @see inra.ijpb.measure.region2d.RegionAnalyzer2D
 * 
 * @author dlegland
 *
 */
public abstract class RegionAnalyzer3D<T> extends AlgoStub implements RegionAnalyzer<T>, AlgoListener
{
	/**
     * Utility method that convert an array of result into a map using labels as
     * keys.
     * 
     * @param <T2>
     *            the type of data measured for each label
     * @param labels
     *            the array of labels to use as keys
     * @param data
     *            the array of objects to map
     * @return a map between each entry of label array and data array
     */
	public static final <T2> Map<Integer, T2> createMap(int[] labels, T2[] data)
	{
		// check input sizes
		int nLabels = labels.length;
		if (data.length != nLabels)
		{
			throw new IllegalArgumentException("Require same number of elements for label array and data array");
		}
		
		// iterate over labels
		Map<Integer, T2> map = new TreeMap<Integer, T2>();
		for (int i = 0; i < nLabels; i++)
		{
			map.put(labels[i], data[i]);
		}
        return map;
	}

	/**
	 * Computes an instance of the generic type T for each region in input label image.
	 * 
	 * @param image
	 *            the input 3D image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array of the type used to represent the analysis result of each region 
	 */
	public abstract T[] analyzeRegions(ImageStack image, int[] labels, Calibration calib);
	
	/**
	 * Identifies labels within image and computes an instance of the generic
	 * type T for each region in input label image.
	 * 
	 * @param image
	 *            a 3D label image (8, 16 or 32 bits)
	 * @param calib
	 *            the spatial calibration of the image
	 * @return a map between the region label and the result of analysis for
	 *         each region
	 */
	public Map<Integer, T> analyzeRegions(ImageStack image, Calibration calib)
	{
		// extract particle labels
		fireStatusChanged(this, "Find Labels");
		int[] labels = LabelImages.findAllLabels(image);
		
		// compute analysis result for each label
		fireStatusChanged(this, "Analyze regions");
		T[] results = analyzeRegions(image, labels, calib);

		// encapsulate into map
		fireStatusChanged(this, "Convert to map");
		Map<Integer, T> map = createMap(labels, results);

		// cleanup monitoring
		fireStatusChanged(this, "");
        return map;
	}
	
	/**
	 * Default implementation of the analyzeRegions method, that calls the more
	 * specialized {@link #analyzeRegions(ImageStack, int[], ij.measure.Calibration)}
	 * method and transforms the result into a map.
	 * 
	 * @param labelPlus
	 *            the input image containing label of regions
	 * @return the mapping between region label and result of analysis for each
	 *         region
	 */
	public Map<Integer, T> analyzeRegions(ImagePlus labelPlus)
	{
		// extract particle labels
		fireStatusChanged(this, "Find Labels");
		FindAllLabels findLabels = new FindAllLabels();
		findLabels.addAlgoListener(this);
		int[] labels = findLabels.process(labelPlus);
		
		// compute analysis result for each label
		fireStatusChanged(this, "Analyze regions");
		T[] results = analyzeRegions(labelPlus.getImageStack(), labels, labelPlus.getCalibration());
		
		// encapsulate into map
		fireStatusChanged(this, "Convert to map");
		Map<Integer, T> map = createMap(labels, results);

		// cleanup monitoring
		fireStatusChanged(this, "");
        return map;
	}
	
    @Override
    public void algoProgressChanged(AlgoEvent evt)
    {
        this.fireProgressChanged(evt);
    }


    @Override
    public void algoStatusChanged(AlgoEvent evt)
    {
        this.fireProgressChanged(evt);
    }
}
