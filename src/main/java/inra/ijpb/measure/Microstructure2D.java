/**
 * 
 */
package inra.ijpb.measure;

import ij.measure.Calibration;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.measure.region2d.BinaryConfigurationsHistogram2D;
import inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D;

/**
 * Characterizes binary microstructures by computing densities of 2D geometrical
 * measures: area density, perimeter density, 2D euler number density.
 * 
 * @see Microstructure3D
 *
 * @author dlegland
 *
 */
public class Microstructure2D
{
    /**
     * Computes the area density for each particle in the label image, taking into
     * account image resolution.
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
        double[] lut = IntrinsicVolumesAnalyzer2D.perimeterLut(calib, nDirs);

        // histogram of configurations for each label
        int[] histo = new BinaryConfigurationsHistogram2D().processInnerFrame(image);
        
        // apply Binary configuration look-up table
        double perim = BinaryConfigurationsHistogram2D.applyLut(histo, lut);
        return perim / samplingArea(image, calib);
    }

    public static final double eulerNumberDensity(ImageProcessor image, Calibration calib, int conn)
    {
        // create associative array to know index of each label
        double[] lut = IntrinsicVolumesAnalyzer2D.eulerNumberLut(conn);

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
    // Constructor

    /**
     * Private constructor to prevent instantiation.
     */
    private Microstructure2D()
    {
    }
}
