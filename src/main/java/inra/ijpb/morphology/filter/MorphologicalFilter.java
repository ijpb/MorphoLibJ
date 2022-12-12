/**
 * 
 */
package inra.ijpb.morphology.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.data.image.ColorImages;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;

/**
 * Base class for morphological filters, based on a structuring element.
 * 
 * Can process ScalarArray instances, or VectorArray instances, and return array
 * the same type and the same size as input arrays. In the case of VectorArray
 * instances the process is applied on each channel / component image of the
 * vector image).
 * 
 * @see Strel
 * 
 * @author dlegland
 */
public abstract class MorphologicalFilter extends AlgoStub implements AlgoListener
{
    // =======================================================================
    // Class members
    
    /**
     * The structuring element used by concrete implementations.
     */
    Strel3D strel;
    
    /**
     * The string suffix that is used to complete image name after filtering.
     */
    String suffix = "-filt";
    
    
    // =======================================================================
    // Constructors
    
    protected MorphologicalFilter(Strel3D strel)
    {
        this.strel = strel;
    }
    
    protected MorphologicalFilter(Strel3D strel, String suffix)
    {
        this.strel = strel;
        this.suffix = suffix;
    }
    
    
    // =======================================================================
    // Methods
    
    /**
     * Applies the morphological filter to the input ImagePlus, and returns a
     * new instance of ImagePlus. The settings of the input image (calibration,
     * colormap...) are propagated to the result image
     * 
     * @param imagePlus
     *            the image to process
     * @return the result of filtering operation
     */
    public ImagePlus process(ImagePlus imagePlus)
    {
        ImagePlus resultPlus;
        String newName = imagePlus.getShortTitle() + suffix;
        
        // Dispatch to appropriate function depending on dimension
        if (imagePlus.getStackSize() == 1) 
        {
            // check-up strel dimensionality
            if (!(strel instanceof Strel))
            {
                throw new RuntimeException("Processing 2D image requires a 2D strel");
            }
            
            // process planar image
            ImageProcessor image = imagePlus.getProcessor();
            ImageProcessor result = this.process(image);
            result.setColorModel(image.getColorModel());
            resultPlus = new ImagePlus(newName, result);
        } 
        else
        {
            // process image stack
            ImageStack image = imagePlus.getStack();
            ImageStack result = this.process(image);
            result.setColorModel(image.getColorModel());
            resultPlus = new ImagePlus(newName, result);
        }
        
        // keep settings from original image
        resultPlus.copyScale(imagePlus);
        resultPlus.setDisplayRange(imagePlus.getDisplayRangeMin(), imagePlus.getDisplayRangeMax());
        resultPlus.setLut(imagePlus.getProcessor().getLut());
        return resultPlus;      
    }
    
    /**
     * Apply filtering operation on the specified image, and returns the result
     * as a new instance of ImageProcessor.
     * 
     * @param image
     *            the image to process
     * @return the result of morphological filter.
     */
    public abstract ImageProcessor process(ImageProcessor image);
    
    /**
     * Apply filtering operation on the specified 3D image, and returns the
     * result as a new instance of ImageStack.
     * 
     * @param image
     *            the image to process
     * @return the result of morphological filter.
     */
    public abstract ImageStack process(ImageStack image);
    
    /**
     * Performs morphological dilation on each channel, and reconstitutes the
     * resulting color image.
     * 
     * @param image
     *            the input RGB image
     * @param strel
     *            the structuring element used for dilation
     * @return the result of the dilation
     */
    protected ColorProcessor processColor(ColorProcessor image)
    {
        // extract channels and allocate memory for result
        Map<String, ByteProcessor> channels = ColorImages.mapChannels(image);
        Collection<ImageProcessor> res = new ArrayList<ImageProcessor>(3);
        
        // Process each channel individually
        for (String name : new String[] { "red", "green", "blue" })
        {
            ((Strel) strel).setChannelName(name);
            res.add(this.process(channels.get(name)));
        }
        
        return ColorImages.mergeChannels(res);
    }
    
    /**
     * @return the structuring element associated to this operator. This can be
     *         also an instance of the Strel interface.
     */
    public Strel3D getStrel()
    {
        return this.strel;
    }
    
    
    // =======================================================================
    // Management of algorithms events
    
    @Override
    public void algoProgressChanged(AlgoEvent evt)
    {
        fireProgressChanged(evt);
    }

    @Override
    public void algoStatusChanged(AlgoEvent evt)
    {
        fireStatusChanged(evt);
    }
    
    
    // =======================================================================
    // Utility functions
    
    /**
     * Determines max possible value from bit depth.
     *  8 bits -> 255
     * 16 bits -> 65535
     * 32 bits -> Float.MAX_VALUE
     */
    protected static final double getMaxPossibleValue(ImageStack stack)
    {
        double maxVal = 255;
        int bitDepth = stack.getBitDepth(); 
        if (bitDepth == 16)
        {
            maxVal = 65535;
        }
        else if (bitDepth == 32)
        {
            maxVal = Float.MAX_VALUE;
        }
        return maxVal;
    }
    
    protected final static int clamp(int value, int min, int max) 
    {
        return Math.min(Math.max(value, min), max);
    }
}
