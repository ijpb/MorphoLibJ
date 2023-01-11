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
package inra.ijpb.measure;

import ij.measure.Calibration;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.BinaryConfigurationsHistogram2D;
import inra.ijpb.measure.region2d.IntrinsicVolumes2DUtils;

/**
 * Computation of intrinsic volumes(area, perimeter, Euler number) in planar
 * binary or label images.
 * 
 * For binary images, a binary image is expected, together with the spatial
 * calibration of the image. For label images, the list of region labels within
 * images should be specified as an array of integers.
 * 
 * @see IntrinsicVolumes3D
 * @see inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D
 * @see inra.ijpb.measure.region2d.IntrinsicVolumes2DUtils
 * 
 * @author dlegland
 *
 */
public class IntrinsicVolumes2D
{
    /**
     * Measures the area of the foreground region within a binary image.
     * 
     * @see #perimeter(ImageProcessor, Calibration, int)
     * @see #eulerNumber(ImageProcessor, int)
     *  
     * @see inra.ijpb.binary.BinaryImages#countForegroundPixels(ImageProcessor) 
     * 
     * @param image
     *            the input binary image containing the region to analyze
     * @param calib
     *            the spatial calibration of the image
     * @return the area of each region
     */
    public static final double area(ImageProcessor image, Calibration calib)
    {
        // area of individual pixel
        double pixelArea = calib.pixelWidth * calib.pixelHeight;

        // First count the number of pixels in each region
        int count = BinaryImages.countForegroundPixels(image);
        double area = count * pixelArea;
        return area;
    }

    /**
     * Measures the area of each region within a label image, taking into
     * account image resolution.
     * 
     * @see inra.ijpb.label.LabelImages#pixelCount(ImageProcessor, int[]) 
     * 
     * @param image
     *            the input image containing label of particles
     * @param labels
     *            the array of unique labels in image
     * @param calib
     *            the spatial calibration of the image
     * @return the area of each region
     */
    public static final double[] areas(ImageProcessor image, int[] labels, Calibration calib)
    {
        // area of individual pixel
        double pixelArea = calib.pixelWidth * calib.pixelHeight;

        // initialize result
        int nLabels = labels.length;
        double[] areas = new double[nLabels];

        // First count the number of pixels in each region
        int[] counts = LabelImages.pixelCount(image, labels);

        // convert pixel counts to areas
        for (int i = 0; i < areas.length; i++) 
        {
            areas[i] = counts[i] * pixelArea;
        }

        return areas;
    }

    /**
     * Computes the area density for the 2D binary microstructure. 
     * 
     * @param image
     *            the input binary image
     * @return the area density of the binary structure
     */
    public static final double areaDensity(ImageProcessor image)
    {
        // image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        double totalArea = sizeX * sizeY;
        
        // count the number of pixels with value greater than 0
        double count = BinaryImages.countForegroundPixels(image);

        // normalization by image size
        return count / totalArea;
    }


    /**
     * Measures the perimeter of the foreground region within a binary image. 
     * 
     * @see #area(ImageProcessor, Calibration)
     * @see #eulerNumber(ImageProcessor, int)
     * 
     * @param image
     *            the input image containing the binary region to analyze
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, either 2 or 4
     * @return the perimeter of the binary region within the image
     */
    public static final double perimeter(ImageProcessor image, Calibration calib, int nDirs)
    {
        // pre-compute LUT corresponding to resolution and number of directions
        double[] lut = IntrinsicVolumes2DUtils.perimeterLut(calib, nDirs);

        // histogram of configurations for each label
        int[] histo = new BinaryConfigurationsHistogram2D().process(image);
        
        // apply Binary configuration look-up table
        return BinaryConfigurationsHistogram2D.applyLut(histo, lut);
    }

    /**
     * Measures the perimeter of each region within a label image.
     * 
     * @see #areas(ImageProcessor, int[], Calibration)
     * @see #eulerNumbers(ImageProcessor, int[], int)
     * 
     * @param image
     *            the input image containing the labels of regions to analyze
     * @param labels
     *            the labels of the regions within the image
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, either 2 or 4
     * @return the perimeter of each region within the image
     */
    public static final double[] perimeters(ImageProcessor image, int[] labels, Calibration calib, int nDirs)
    {
        // pre-compute LUT corresponding to resolution and number of directions
        double[] lut = IntrinsicVolumes2DUtils.perimeterLut(calib, nDirs);

        // histogram of configurations for each label
        int[][] histos = new BinaryConfigurationsHistogram2D().process(image, labels);
        
        // apply Binary configuration look-up table
        return BinaryConfigurationsHistogram2D.applyLut(histos, lut);
    }

    /**
     * Computes perimeter density of binary image.
     * 
     * @param image
     *            the input binary image
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, either 2 or 4
     * @return the perimeter density of the binary image
     */
    public static final double perimeterDensity(ImageProcessor image,
            Calibration calib, int nDirs)
    {
        // create associative array to know index of each label
        double[] lut = IntrinsicVolumes2DUtils.perimeterLut(calib, nDirs);

        // histogram of configurations for each label
        int[] histo = new BinaryConfigurationsHistogram2D().processInnerFrame(image);
        
        // apply Binary configuration look-up table
        double perim = BinaryConfigurationsHistogram2D.applyLut(histo, lut);
        return perim / samplingArea(image, calib);
    }

    /**
     * Measures the Euler number of the region within the binary image, using
     * the specified connectivity.
     * 
     * @param image
     *            the input 2D binary image
     * @param conn
     *            the connectivity to use (either 4 or 8)
     * @return the Euler number of the region within the binary image
     */
    public static final int eulerNumber(ImageProcessor image, int conn)
    {
        // pre-compute LUT corresponding to connectivity
        int[] lut = IntrinsicVolumes2DUtils.eulerNumberIntLut(conn);

        // histogram of configurations for each label
        int[] histo = new BinaryConfigurationsHistogram2D().process(image);
        
        // apply Binary configuration look-up table
        return BinaryConfigurationsHistogram2D.applyLut(histo, lut) / 4;
    }

    /**
     * Measures the Euler number of each region given in the "labels" argument,
     * using the specified connectivity.
     * 
     * @param image
     *            the input label image (with labels having integer values)
     * @param labels
     *            the set of unique labels in image
     * @param conn
     *            the connectivity to use (either 4 or 8)
     * @return the Euler number of each region within the image
     */
    public static final int[] eulerNumbers(ImageProcessor image, int[] labels, int conn)
    {
        // pre-compute LUT corresponding to connectivity
        int[] lut = IntrinsicVolumes2DUtils.eulerNumberIntLut(conn);

        // histogram of configurations for each label
        int[][] histos = new BinaryConfigurationsHistogram2D().process(image, labels);
        
        // apply Binary configuration look-up table
        int[] euler = BinaryConfigurationsHistogram2D.applyLut(histos, lut);
        for (int i = 0; i < euler.length; i++)
        {
            euler[i] /= 4;
        }
        
        return euler;
    }

    /**
     * Measures the Euler number density of the foreground region within a
     * binary image, using the specified connectivity.
     * 
     * @param image
     *            the input 2D binary image
     * @param calib
     *            the spatial calibration of the image
     * @param conn
     *            the connectivity to use (either 4 or 8)
     * @return the Euler number density within the binary image
     */
    public static final double eulerNumberDensity(ImageProcessor image, Calibration calib, int conn)
    {
        // create associative array to know index of each label
        double[] lut = IntrinsicVolumes2DUtils.eulerNumberLut(conn);

        // histogram of configurations for each label
        int[] histo = new BinaryConfigurationsHistogram2D().processInnerFrame(image);
        
        // apply Binary configuration look-up table
        double euler = BinaryConfigurationsHistogram2D.applyLut(histo, lut);
        return euler / samplingArea(image, calib);
    }

    /**
     * Computes the area of the sampling window, for estimation of densities.
     * 
     * @param image
     *            the image to analyze
     * @param calib
     *            the spatial calibration
     * @return the calibrated area of the sampling window
     */
    private static final double samplingArea(ImageProcessor image, Calibration calib)
    {
        // size of image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        return (sizeX - 1) * calib.pixelWidth * (sizeY - 1) * calib.pixelHeight;
    }
    

    // ==================================================
    // Constructors

    /**
     * Private constructor to prevent instantiation.
     */
    private IntrinsicVolumes2D() 
    {
    }
}
