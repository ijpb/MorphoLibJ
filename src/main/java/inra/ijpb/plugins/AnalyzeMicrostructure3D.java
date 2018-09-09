/**
 * 
 */
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.measure.Microstructure3D;
import inra.ijpb.measure.region3d.BinaryConfigurationsHistogram3D;
import inra.ijpb.measure.region3d.IntrinsicVolumes3D;

/**
 * @author dlegland
 *
 */
public class AnalyzeMicrostructure3D implements PlugIn
{
    // ====================================================
    // Global Constants
    
    /**
     * The list of connectivity names.
     */
    private final static String[] connectivityNames = {
        "C6", "C26"
    };
    
    private final static int[] connectivityValues = new int[]{6, 26};
    
    /**
     * List of available numbers of directions
     */
    private final static String[] dirNumberLabels = {
            "Crofton  (3 dirs.)", 
            "Crofton (13 dirs.)" 
    }; 
    
    /**
     *  Array of weights, in the same order than the array of names.
     */
    private final static int[] dirNumbers = {
        3, 13
    };
    
    // ====================================================
    // Class variables
    
    boolean computeVolume       = true;
    boolean computeSurface      = true;
    boolean computeMeanBreadth  = true;
    boolean computeEulerNumber  = true;

    int surfaceAreaDirs = 13;
    int meanBreadthDirs = 13;
    int connectivity = 6;
    

    /* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    @Override
    public void run(String arg)
    {
        ImagePlus imagePlus = IJ.getImage();
        
        if (imagePlus.getStackSize() == 1) 
        {
            IJ.error("Requires a Stack");
            return;
        }
        
        // create the dialog, with operator options
        GenericDialog gd = new GenericDialog("Microstructure Analysis 3D");
        gd.addCheckbox("Volume", true);
        gd.addCheckbox("Surface Area", true);
        gd.addCheckbox("Mean_Breadth", true);
        gd.addCheckbox("Euler Number", true);
        gd.addMessage("");
        gd.addChoice("Surface area method:", dirNumberLabels, dirNumberLabels[1]);
        gd.addChoice("Mean breadth method:", dirNumberLabels, dirNumberLabels[1]);
        gd.addChoice("Euler Connectivity:", connectivityNames, connectivityNames[1]);
        gd.showDialog();
        
        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        // Extract features to extract from image
        computeVolume       = gd.getNextBoolean();
        computeSurface      = gd.getNextBoolean();
        computeMeanBreadth  = gd.getNextBoolean();
        computeEulerNumber  = gd.getNextBoolean();
        
        // extract analysis options
        surfaceAreaDirs = dirNumbers[gd.getNextChoiceIndex()];
        meanBreadthDirs = dirNumbers[gd.getNextChoiceIndex()];
        connectivity = connectivityValues[gd.getNextChoiceIndex()];
        
        // Execute the plugin
        ResultsTable table = process(imagePlus);
        
        // create string for indexing results
        String tableName = imagePlus.getShortTitle() + "-microstructure"; 
    
        // show result
        table.show(tableName);
    }
    /**
     * Computes features from an ImagePlus object. 
     * Spatial resolution is read from image Calibration.
     * 
     * @param imagePlus the label image to analyze
     * @return the results in a new ResultsTable
     */
    public ResultsTable process(ImagePlus imagePlus)
    {
        // Check validity of parameters
        if (imagePlus==null) 
            return null;

        // Extract Image Stack and its calibration
        ImageStack image = imagePlus.getStack();
        Calibration calib = imagePlus.getCalibration();
        
        return process(image, calib);
    }
    
    /**
     * Computes features from an ImageStack object, specifying the calibration.
     * 
     * @param image
     *            the 3D label image to analyze
     * @param calib
     *            the spatial calibration of the image
     * @return the results in a new ResultsTable
     */
    public ResultsTable process(ImageStack image, Calibration calib)
    {
        // Extract spatial calibration
        double[] resol = new double[]{1, 1, 1};
        if (calib != null && calib.scaled()) 
        {
            resol[0] = calib.pixelWidth;
            resol[1] = calib.pixelHeight;
            resol[2] = calib.pixelDepth;
        }

        // Compute histogram of binary 2-by-2-by-2 configurations (array length: 256)
        BinaryConfigurationsHistogram3D algo = new BinaryConfigurationsHistogram3D();
        DefaultAlgoListener.monitor(algo);
        int[] histogram = algo.processInnerFrame(image);
      
        // pre-compute LUT corresponding to resolution and number of directions
        double vol = Microstructure3D.samplingVolume(image, calib);
        
        // Convert to ResultsTable object
        IJ.showStatus("Create Table");
        ResultsTable table = new ResultsTable();
        
        table.incrementCounter();
        // geometrical quantities
        if (computeVolume)
        {
            double[] volumeLut = IntrinsicVolumes3D.volumeLut(calib);
            double volume = BinaryConfigurationsHistogram3D.applyLut(histogram, volumeLut);
            table.addValue("VolumeDensity", volume / vol);
        }
        if (computeSurface)
        {
            double[] surfaceAreaLut = IntrinsicVolumes3D.surfaceAreaLut(calib, surfaceAreaDirs);
            double surfaceArea = BinaryConfigurationsHistogram3D.applyLut(histogram, surfaceAreaLut);
            table.addValue("SurfaceAreaDensity", surfaceArea / vol);
        }
        if (computeMeanBreadth)
        {
            double[] meanBreadthLut = IntrinsicVolumes3D.meanBreadthLut(calib, meanBreadthDirs, 8); // TODO: add option
            double meanBreadth = BinaryConfigurationsHistogram3D.applyLut(histogram, meanBreadthLut);
            table.addValue("MeanBreadthDensity", meanBreadth / vol);
        }
        if (computeEulerNumber)
        {
            double[] eulerNumberLut = IntrinsicVolumes3D.eulerNumberLut(connectivity);
            double eulerNumber = BinaryConfigurationsHistogram3D.applyLut(histogram, eulerNumberLut);
            table.addValue("EulerNumberDensity", eulerNumber / vol);
        }

        return table;
     }
}
