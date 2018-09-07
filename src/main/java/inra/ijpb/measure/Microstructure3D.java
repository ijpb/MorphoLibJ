/**
 * 
 */
package inra.ijpb.measure;

import ij.ImageStack;
import ij.measure.Calibration;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.measure.region3d.BinaryConfigurationsHistogram3D;
import inra.ijpb.measure.region3d.IntrinsicVolumes3D;

/**
 * @author dlegland
 *
 */
public class Microstructure3D
{
    /**
     * Measures the volume density of a single region within a 3D binary image.
     * 
     * @param image
     *            the binary image containing the region
     * @return the volume density of the region within the image
     */
    public static final double volumeDensity(ImageStack image)
    {
        // count non-zero voxels
        int voxelCount = BinaryImages.countForegroundVoxels(image);

        // Normalizes voxel count by image volume.
        int voxelNumber = image.getWidth() * image.getWidth() * image.getSize();
        return voxelCount / voxelNumber;
    }

    /**
     * Measures the surface area density of a single region within a 3D binary image.
     * 
     * Uses discretization of the Crofton formula, that consists in computing
     * numbers of intersections with lines of various directions.
     * 
     * @param image
     *            image containing the label of each particle
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, either 3 or 13
     * @return the surface area density of the binary phase within the image
     */
    public static final double surfaceAreaDensity(ImageStack image, Calibration calib, int nDirs)
    {
        // pre-compute LUT corresponding to resolution and number of directions
        double[] lut = IntrinsicVolumes3D.surfaceAreaLut(calib, nDirs);

        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum up
        int[] histo = new BinaryConfigurationsHistogram3D().processInnerFrame(image);
        double surf = BinaryConfigurationsHistogram3D.applyLut(histo, lut);
        
        // normalize by volume of sampling window
        double vol = samplingVolume(image, calib);
        return surf / vol;
    }
    
    /**
     * Measures the mean breadth density of a single region within a 3D binary image.
     * 
     * Uses discretization of the Crofton formula, that consists in computing
     * euler number of intersection with planes of various orientations.
     * 
     * @param image
     *            image containing a binary region
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, either 3 or 13
     * @return the surface area density of the binary phase within the image
     */
    public static final double meanBreadthDensity(ImageStack image, Calibration calib, int nDirs)
    {
        // pre-compute LUT corresponding to resolution and number of directions
        double[] lut = IntrinsicVolumes3D.meanBreadthLut(calib, nDirs);

        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum u
        int[] histo = new BinaryConfigurationsHistogram3D().processInnerFrame(image);
        double meanBreadth = BinaryConfigurationsHistogram3D.applyLut(histo, lut);
        
        // normalize by volume of sampling window
        double vol = samplingVolume(image, calib);
        return meanBreadth / vol;
    }

    /**
     * Measures the Euler number density of the foreground region within a
     * binary image, using the specified connectivity.
     * 
     * @param image
     *            the input 3D binary image
     * @param calib
     *            the spatial calibration of the image
     * @param conn
     *            the connectivity to use (either 6 or 26)
     * @return the Euler number density within the binary image
     */
    public static final double eulerNumberDensity(ImageStack image, Calibration calib, int conn)
    {
        // pre-compute LUT corresponding to resolution and number of directions
        double[] lut = IntrinsicVolumes3D.eulerNumberLut(conn);

        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum up
        int[] histo = new BinaryConfigurationsHistogram3D().processInnerFrame(image);
        double euler = BinaryConfigurationsHistogram3D.applyLut(histo, lut);
        
        // normalize by the volume of the sampling window
        double vol = samplingVolume(image, calib);
        return euler / vol;
    }
    
    public static final double samplingVolume(ImageStack image, Calibration calib)
    {
        // size of image
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        return (sizeX - 1) * calib.pixelWidth * (sizeY - 1) * calib.pixelHeight * (sizeZ - 1) * calib.pixelDepth;   
    }
    

    // ==================================================
    // Constructor

    /**
     * Private constructor to prevent instantiation.
     */
    private Microstructure3D()
    {
    }
}
