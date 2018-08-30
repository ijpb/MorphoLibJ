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
 * images) or 2-by-2-by-2 voxels (3D images).
 * 
 * @author dlegland
 *
 */
public class BinaryConfigurationsHistogram2D extends AlgoStub
{
    // ==================================================
    // Static methods

    public static final double applyLut(int[] histogram, double[] lut)
    {
        double sum = 0;
        for (int i = 0; i < histogram.length; i++)
        {
            sum += histogram[i] * lut[i];
        }
        return sum;
    }

    public static final double[] applyLut(int[][] histograms, double[] lut)
    {
        double[] sums = new double[histograms.length];
        for (int iLabel = 0; iLabel < histograms.length; iLabel++)
        {
            sums[iLabel] = applyLut(histograms[iLabel], lut);
        }
        return sums;
    }

    
    // ==================================================
    // Constructors

    /**
     * Empty constructor.
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

        return histogram;
    }

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
