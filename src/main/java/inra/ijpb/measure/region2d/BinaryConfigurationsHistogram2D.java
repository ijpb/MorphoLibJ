/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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

import java.util.ArrayList;
import java.util.HashMap;

import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.label.LabelImages;

/**
 * Computes histogram of binary configurations composed of 2-by-2 pixels (planar
 * images).
 * 
 * Implements the Algo interface, so the progress can be tracked.
 * 
 * @author dlegland
 *
 */
public class BinaryConfigurationsHistogram2D extends AlgoStub
{
    // ==================================================
    // Static methods

	/**
	 * Applies look-up-table of values for each configuration, based on the
	 * array of count for each binary configuration.
	 * 
	 * @param histogram
	 *            the count of each type of 2-by-2 binary configurations, as an
	 *            array
	 * @param lut
	 *            the value to associate to each configuration
	 * @return the sum of the products of counts by the associated value
	 */
    public static final double applyLut(int[] histogram, double[] lut)
    {
        double sum = 0;
        for (int i = 0; i < histogram.length; i++)
        {
            sum += histogram[i] * lut[i];
        }
        return sum;
    }

	/**
	 * Applies look-up-table of values for each configuration, based on the
	 * array of count for each binary configuration.
	 * 
	 * @param histogram
	 *            the count of each type of 2-by-2 binary configurations, as an
	 *            array
	 * @param lut
	 *            the value to associate to each configuration
	 * @return the sum of the products of counts by the associated value
	 */
    public static final int applyLut(int[] histogram, int[] lut)
    {
        int sum = 0;
        for (int i = 0; i < histogram.length; i++)
        {
            sum += histogram[i] * lut[i];
        }
        return sum;
    }

	/**
	 * Applies look-up-table of values for each configuration for each label,
	 * based on the 16-array of count for each binary configuration.
	 * 
	 * @param histograms
	 *            the count of each type of 2-by-2 binary configuration of each
	 *            label, as a nLabels-by-16 array
	 * @param lut
	 *            the value to associate to each configuration
	 * @return the sum of the products of counts by the associated value for
	 *         each label
	 */
    public static final double[] applyLut(int[][] histograms, double[] lut)
    {
        double[] sums = new double[histograms.length];
        for (int iLabel = 0; iLabel < histograms.length; iLabel++)
        {
            sums[iLabel] = applyLut(histograms[iLabel], lut);
        }
        return sums;
    }

	/**
	 * Applies look-up-table of values for each configuration for each label,
	 * based on the 16-array of count for each binary configuration.
	 * 
	 * @param histograms
	 *            the count of each type of 2-by-2 binary configuration of each
	 *            label, as a nLabels-by-16 array
	 * @param lut
	 *            the value to associate to each configuration
	 * @return the sum of the products of counts by the associated value for
	 *         each label
	 */
    public static final int[] applyLut(int[][] histograms, int[] lut)
    {
        int[] sums = new int[histograms.length];
        for (int iLabel = 0; iLabel < histograms.length; iLabel++)
        {
            sums[iLabel] = applyLut(histograms[iLabel], lut);
        }
        return sums;
    }

    
    // ==================================================
    // Constructors

    /**
     * Default empty constructor.
     */
    public BinaryConfigurationsHistogram2D()
    {
    }


    // ==================================================
    // General methods

    /**
     * Computes the histogram of binary configurations for the region within the
     * input binary image.
     * 
     * Takes into account the border of the image: histogram considers all the
     * 2-by-2 configurations that contain at least one pixel of the image.
     * 
	 * @see #processInnerFrame(ImageProcessor)
	 * 
     * @param binaryImage
     *            the input image containing the region the analyze
     * @return an array of integers containing the 16-elements histogram of
     *         binary configurations
     */
    public int[] process(ImageProcessor binaryImage)
    {
        // initialize result
        int[] histogram = new int[16];

        // size of image
        int sizeX = binaryImage.getWidth();
        int sizeY = binaryImage.getHeight();

        // values of pixels within current 2-by-2 configuration
        // (first digit for y, second digit for x)
        boolean[] configValues = new boolean[4];
        
        // Iterate over all 2-by-2 configurations containing at least one pixel
        // within the image.
        // Current pixel is the lower-right pixel in configuration
        // (corresponding to b11).
        for (int y = 0; y < sizeY + 1; y++) 
        {
            this.fireProgressChanged(this, y, sizeY + 1);

            configValues[0] = false;
            configValues[2] = false;
            
            for (int x = 0; x < sizeX + 1; x++) 
            {
                // update pixel values of configuration
                configValues[1] = x < sizeX & y > 0 ? (int) binaryImage.getf(x, y - 1) > 0: false;
                configValues[3] = x < sizeX & y < sizeY ? (int) binaryImage.getf(x, y) > 0: false;

                // Compute index of local configuration
                int index = configIndex(configValues);

                // update histogram
                histogram[index]++;

                // update values of configuration for next iteration
                configValues[0] = configValues[1];
                configValues[2] = configValues[3];
            }
        }

        this.fireProgressChanged(this, 1, 1);
        return histogram;
    }

	/**
	 * Applies a look-up-table for each of the 2x2 pixel configurations
	 * with all pixels within the input binary image, and returns the
	 * sum of contributions for each label.
	 * 
	 * This method is used for computing densities of Euler number, perimeter
	 * and area from binary images.
	 * 
	 * @see #process(ImageProcessor)
	 * 
	 * @param binaryImage
	 *            the input 2D binary image
	 * @return an array of 16 integers containing the number of each binary
	 *         configurations
	 */
    public int[] processInnerFrame(ImageProcessor binaryImage)
    {
        // initialize result
        int[] histogram = new int[16];

        // size of image
        int sizeX = binaryImage.getWidth();
        int sizeY = binaryImage.getHeight();

        // values of pixels within current 2-by-2 configuration
        // (first digit for y, second digit for x)
        boolean[] configValues = new boolean[4];

        // iterate on image pixel configurations
        for (int y = 1; y < sizeY; y++) 
        {
            this.fireProgressChanged(this, y, sizeY);
            
            configValues[0] = binaryImage.getf(0, y - 1) > 0;
            configValues[2] = binaryImage.getf(0,     y) > 0;

            for (int x = 1; x < sizeX; x++) 
            {
                // update pixel values of configuration
                configValues[1] = binaryImage.getf(x, y - 1) > 0;
                configValues[3] = binaryImage.getf(x,     y) > 0;

                // Compute index of local configuration
                int index = configIndex(configValues);

                // update histogram
                histogram[index]++;

                // update values of configuration for next iteration
                configValues[0] = configValues[1];
                configValues[2] = configValues[3];
            }
        }

        this.fireProgressChanged(this, 1, 1);
        return histogram;
    }

    private static final int configIndex(boolean[] configValues)
    {
        // Compute index of local configuration
        int index = 0;
        index += configValues[0] ? 1 : 0;
        index += configValues[1] ? 2 : 0;
        index += configValues[2] ? 4 : 0;
        index += configValues[3] ? 8 : 0;
        return index;
    }

    /**
     * Computes the histogram of binary configurations for each region of the
     * input label image.
     * 
     * Takes into account the border of the image: histograms consider all the
     * 2-by-2 configurations that contain at least one pixel of the image.
     * 
     * @param labelImage
     *            the input image containing region labels
     * @param labels
     *            the list of region labels
     * @return an array of integer containing for each region, the 16-elements
     *         histogram of binary configurations
     */
    public int[][] process(ImageProcessor labelImage, int[] labels)
    {
        // create associative array to know index of each label
        int nLabels = labels.length;
        HashMap<Integer, Integer> labelIndices = LabelImages.mapLabelIndices(labels);

        // initialize result
        int[][] histograms = new int[nLabels][16];

        // size of image
        int sizeX = labelImage.getWidth();
        int sizeY = labelImage.getHeight();

        // for each configuration of 2x2 pixels, we identify the labels
        ArrayList<Integer> localLabels = new ArrayList<Integer>(4);

        // values of pixels within current 2-by-2 configuration
        // (first digit for y, second digit for x)
        int[] configValues = new int[4];
        
        // Iterate over all 2-by-2 configurations containing at least one pixel
        // within the image.
        // Current pixel is the lower-right pixel in configuration
        // (corresponding to b11).
        for (int y = 0; y < sizeY + 1; y++) 
        {
            this.fireProgressChanged(this, y, sizeY + 1);

            configValues[0] = 0;
            configValues[2] = 0;
            
            for (int x = 0; x < sizeX + 1; x++) 
            {
                // update pixel values of configuration
                configValues[1] = x < sizeX & y > 0 ? (int) labelImage.getf(x, y - 1) : 0;
                configValues[3] = x < sizeX & y < sizeY ? (int) labelImage.getf(x, y) : 0;

                // identify labels in current config
                localLabels.clear();
                for (int label : configValues)
                {
                    if (label == 0)
                        continue;
                    // keep only one instance of each label
                    if (!localLabels.contains(label))
                        localLabels.add(label);
                }

                // For each label, compute binary confi
                for (int label : localLabels) 
                {
                    // Compute index of local configuration
                    int index = configIndex(configValues, label);

                    // retrieve label index from label value
                    int labelIndex = labelIndices.get(label);

                    // update histogram of current label
                    histograms[labelIndex][index]++;
                }

                // update values of configuration for next iteration
                configValues[0] = configValues[1];
                configValues[2] = configValues[3];
            }
        }

        this.fireProgressChanged(this, 1, 1);
        return histograms;
    }

    private static final int configIndex(int[] configValues, int label)
    {
        // Compute index of local configuration
        int index = 0;
        index += configValues[0] == label ? 1 : 0;
        index += configValues[1] == label ? 2 : 0;
        index += configValues[2] == label ? 4 : 0;
        index += configValues[3] == label ? 8 : 0;
        return index;
    }

}
