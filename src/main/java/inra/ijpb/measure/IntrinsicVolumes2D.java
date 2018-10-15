/**
 * 
 */
package inra.ijpb.measure;

import ij.measure.Calibration;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.BinaryConfigurationsHistogram2D;
import inra.ijpb.measure.region2d.IntrinsicVolumesAnalyzer2D;

/**
 * Computation of intrinsic volumes(area, perimeter, Euler number) in planar
 * binary or label images.
 * 
 * For binary images, a binary image is expected, together with the spatial
 * calibration of the image. For label images, the list of region labels within
 * images should be specified as an array of integers.
 * 
 * @see IntrinsicVolumes3D
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
        double[] lut = IntrinsicVolumesAnalyzer2D.perimeterLut(calib, nDirs);

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
        double[] lut = IntrinsicVolumesAnalyzer2D.perimeterLut(calib, nDirs);

        // histogram of configurations for each label
        int[][] histos = new BinaryConfigurationsHistogram2D().process(image, labels);
        
        // apply Binary configuration look-up table
        return BinaryConfigurationsHistogram2D.applyLut(histos, lut);
    }

    public static final int eulerNumber(ImageProcessor image, int conn)
    {
        // pre-compute LUT corresponding to connectivity
        int[] lut = IntrinsicVolumesAnalyzer2D.eulerNumberIntLut(conn);

        // histogram of configurations for each label
        int[] histo = new BinaryConfigurationsHistogram2D().process(image);
        
        // apply Binary configuration look-up table
        return BinaryConfigurationsHistogram2D.applyLut(histo, lut) / 4;
    }

    public static final int[] eulerNumbers(ImageProcessor image, int[] labels, int conn)
    {
        // pre-compute LUT corresponding to connectivity
        int[] lut = IntrinsicVolumesAnalyzer2D.eulerNumberIntLut(conn);

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

    
    // ==================================================
    // Constructors

    /**
     * Private constructor to prevent instantiation.
     */
    private IntrinsicVolumes2D() 
    {
    }
}
