/**
 * 
 */
package inra.ijpb.measure;

import ij.ImageStack;
import ij.measure.Calibration;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region3d.BinaryConfigurationsHistogram3D;
import inra.ijpb.measure.region3d.IntrinsicVolumesAnalyzer3D;

/**
 * Computation of intrinsic volumes (volume, surface area, mean breadth and
 * Euler number) for binary or label 3D images.
 * 
 * For binary images, a binary stack is expected, together with the spatial
 * calibration of the image. For label images, the list of region labels within
 * images should be specified as an array of integers.
 * 
 * @see IntrinsicVolumes2D
 * 
 * @author dlegland
 *
 */
public class IntrinsicVolumes3D
{
    // ==================================================
    // Static methods
    
    /**
     * Measures the volume of a single region within a 3D binary image.
     * 
     * @see inra.ijpb.binary.BinaryImages#countForegroundVoxels(ImageStack) 
     * 
     * @param image
     *            the binary image containing the region
     * @param calib
     *            the spatial calibration of the image
     * @return the volume of the region in the image
     */
    public static final double volume(ImageStack image, Calibration calib)
    {
        // pre-compute the volume of individual voxel
        double voxelVolume = calib.pixelWidth * calib.pixelHeight * calib.pixelDepth;
        
        // count non-zero voxels
        int voxelCount = BinaryImages.countForegroundVoxels(image);

        // convert voxel count to particle volume
        double volume = voxelCount * voxelVolume;
        return volume;
    }
        
    /**
     * Measures the volume of each region within a 3D label image.
     * 
     * @param labelImage
     *            image containing the label of each region
     * @param labels
     *            the set of labels for which volume has to be computed
     * @param calib
     *            the spatial calibration of the image
     * @return the volume of each region within the image
     */
    public static final double[] volumes(ImageStack labelImage, int[] labels, Calibration calib)
    {
        // create associative array to know index of each label
        int nLabels = labels.length;
        
        // pre-compute the volume of individual voxel
        double voxelVolume = calib.pixelWidth * calib.pixelHeight * calib.pixelDepth;
        
        // initialize result
        int[] voxelCounts = LabelImages.voxelCount(labelImage, labels);

        // convert voxel counts to particle volumes
        double[] volumes = new double[nLabels];
        for (int i = 0; i < nLabels; i++) 
        {
            volumes[i] = voxelCounts[i] * voxelVolume;
        }
        return volumes;
    }
    
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
        double voxelCount = BinaryImages.countForegroundVoxels(image);
    
        // Normalizes voxel count by image volume.
        double voxelNumber = image.getWidth() * image.getWidth() * image.getSize();
        return voxelCount / voxelNumber;
    }

    /**
     * Measures the surface area of a single region within a 3D binary image.
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
     * @return the surface area of each region within the image
     */
    public static final double surfaceArea(ImageStack image, Calibration calib, int nDirs)
    {
        // pre-compute LUT corresponding to resolution and number of directions
        double[] lut = IntrinsicVolumesAnalyzer3D.surfaceAreaLut(calib, nDirs);

        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum up for each label
        int[] histo = new BinaryConfigurationsHistogram3D().process(image);
        return BinaryConfigurationsHistogram3D.applyLut(histo, lut);
    }
    

    /**
     * Measures the surface area of each region within a label image.
     * 
     * Uses discretization of the Crofton formula, that consists in computing
     * numbers of intersections with lines of various directions.
     * 
     * @param image
     *            image containing the label of each particle
     * @param labels
     *            the set of labels in the image
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, either 3 or 13
     * @return the surface area of each region within the image
     */
    public static final double[] surfaceAreas(ImageStack image, int[] labels, 
            Calibration calib, int nDirs)
    {
        // pre-compute LUT corresponding to resolution and number of directions
        double[] lut = IntrinsicVolumesAnalyzer3D.surfaceAreaLut(calib, nDirs);

        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum up for each label
        int[][] histos = new BinaryConfigurationsHistogram3D().process(image, labels);
        return BinaryConfigurationsHistogram3D.applyLut(histos, lut);
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
        double[] lut = IntrinsicVolumesAnalyzer3D.surfaceAreaLut(calib, nDirs);
    
        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum up
        int[] histo = new BinaryConfigurationsHistogram3D().processInnerFrame(image);
        double surf = BinaryConfigurationsHistogram3D.applyLut(histo, lut);
        
        // normalize by volume of sampling window
        double vol = samplingVolume(image, calib);
        return surf / vol;
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
     * @param volume
     *            the volume of a region
     * @param surface
     *            the surface area of a region
     * @return the sphericity index
     * 
     * @see #surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)
     * @see #volumes(ij.ImageStack, int[], ij.measure.Calibration)
     */
    public final static double sphericity(double volume, double surface) 
    {
        // normalization constant such that sphere has sphericity equal to 1 
        double c = 36 * Math.PI;

        // Compute sphericity
        return c * volume * volume / (surface * surface * surface);
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
     * @param volumes
     *            the volume of each particle
     * @param surfaces
     *            the surface area of each particle
     * @return the sphericity index of each particle
     * 
     * @see #surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)
     * @see #volumes(ij.ImageStack, int[], ij.measure.Calibration)
     */
    public final static double[] sphericity(double[] volumes, double[] surfaces) 
    {
        int n = volumes.length;
        if (surfaces.length != n) 
        {
            throw new IllegalArgumentException("Volume and surface arrays must have the same length");
        }
        
        // Compute sphericity of each label
        double[] sphericities = new double[n];
        for (int i = 0; i < n; i++) 
        {
            sphericities[i] = sphericity(volumes[i], surfaces[i]);
        }
        
        return sphericities;
    }
    
    /**
     * Measures the mean breadth of a single region within a 3D binary image.
     * 
     * The mean breadth is proportional to the integral of mean curvature: mb =
     * 2*pi*IMC.
     * 
     * @param image
     *            image containing the label of each particle
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, either 3 or 13
     * @param conn2d
     * 		      the connectivity to use on planar sections with square tiles (either 4 or 8)            
     * @return the mean breadth of the binary region within the image
     */
    public static final double meanBreadth(ImageStack image, Calibration calib, int nDirs, int conn2d)
    {
        // pre-compute LUT corresponding to resolution and number of directions
        double[] lut = IntrinsicVolumesAnalyzer3D.meanBreadthLut(calib, nDirs, conn2d);

        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum up
        int[] histo = new BinaryConfigurationsHistogram3D().process(image);
        return BinaryConfigurationsHistogram3D.applyLut(histo, lut);
    }

    /**
     * Measures the mean breadth of each region within a label image. The mean
     * breadth is proportional to the integral of mean curvature: mb = 2*pi*IMC.
     * 
     * Uses discretization of the Crofton formula, that consists in computing
     * euler number of intersection with planes of various orientations.
     * 
     * @param image
     *            image containing the label of each region
     * @param labels
     *            the set of labels in the image
     * @param calib
     *            the spatial calibration of the image
     * @param nDirs
     *            the number of directions to consider, either 3 or 13
     * @param conn2d
     * 		      the connectivity to use on planar sections with square tiles (either 4 or 8)            
     * @return the mean breadth of each region within the image
     */
    public static final double[] meanBreadths(ImageStack image, int[] labels, 
            Calibration calib, int nDirs, int conn2d)
    {
        // pre-compute LUT corresponding to resolution and number of directions
        double[] lut = IntrinsicVolumesAnalyzer3D.meanBreadthLut(calib, nDirs, conn2d);

        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum up for each label
        int[][] histos = new BinaryConfigurationsHistogram3D().process(image, labels);
        return BinaryConfigurationsHistogram3D.applyLut(histos, lut);
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
     * @param conn2d
     *            the connectivity to use on planar sections with square tiles (either 4 or 8)            
     * @return the surface area density of the binary phase within the image
     */
    public static final double meanBreadthDensity(ImageStack image, Calibration calib, int nDirs, int conn2d)
    {
        // pre-compute LUT corresponding to resolution and number of directions
        double[] lut = IntrinsicVolumesAnalyzer3D.meanBreadthLut(calib, nDirs, conn2d);
    
        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum u
        int[] histo = new BinaryConfigurationsHistogram3D().processInnerFrame(image);
        double meanBreadth = BinaryConfigurationsHistogram3D.applyLut(histo, lut);
        
        // normalize by volume of sampling window
        double vol = samplingVolume(image, calib);
        return meanBreadth / vol;
    }

    /**
     * Measures the Euler number of the region within the binary image, using
     * the specified connectivity.
     * 
     * @param image
     *            the input 3D binary image
     * @param conn
     *            the connectivity to use (either 6 or 26)
     * @return the Euler number of the region within the binary image
     */
    public static final double eulerNumber(ImageStack image, int conn)
    {
        // pre-compute LUT corresponding to the chosen connectivity
        double[] lut = IntrinsicVolumesAnalyzer3D.eulerNumberLut(conn);

        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum up
        int[] histo = new BinaryConfigurationsHistogram3D().process(image);
        return BinaryConfigurationsHistogram3D.applyLut(histo, lut);
    }
    
    /**
     * Measures the Euler number of each region given in the "labels" argument,
     * using the specified connectivity.
     * 
     * @param image
     *            the input 3D label image (with labels having integer values)
     * @param labels
     *            the set of unique labels in image
     * @param conn
     *            the connectivity to use (either 6 or 26)
     * @return the Euler number of each region within the image
     */
    public final static double[] eulerNumbers(ImageStack image, int[] labels,
            int conn)
    {    
        // pre-compute LUT corresponding to the chosen connectivity
        double[] lut = IntrinsicVolumesAnalyzer3D.eulerNumberLut(conn);

        // Compute index of each 2x2x2 binary voxel configuration, associate LUT
        // contribution, and sum up for each label
        int[][] histos = new BinaryConfigurationsHistogram3D().process(image, labels);
        return BinaryConfigurationsHistogram3D.applyLut(histos, lut);
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
        double[] lut = IntrinsicVolumesAnalyzer3D.eulerNumberLut(conn);

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
    // Constructors

    /**
     * Private constructor to prevent instantiation.
     */
    private IntrinsicVolumes3D() 
    {
    }
}
