/**
 * 
 */
package inra.ijpb.label.edit;

import java.util.TreeSet;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;

/**
 * Extract the set of unique labels existing in the given image, excluding the
 * value zero (used for background).
 * 
 * @author dlegland
 */
public class FindAllLabels extends AlgoStub
{
    /**
     * Default empty constructor.
     */
    public FindAllLabels()
    {
    }
    
    /**
     * Returns the set of unique labels existing in the given image, excluding
     * the value zero (used for background).
     * 
     * @param image
     *            an instance of ImagePlus containing a label image
     * @return the list of unique labels present in image (without background)
     */
    public int[] process(ImagePlus image) 
    {
        return image.getStackSize() == 1 ? process(image.getProcessor()) : process(image.getStack());
    }

    /**
     * Returns the set of unique labels existing in the given image, excluding 
     * the value zero (used for background).
     * 
     * @param image
     *            a label image
     * @return the list of unique labels present in image (without background)
     */
    public int[] process(ImageProcessor image)
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // initialize tree for storing the collection of labels
        TreeSet<Integer> labels = new TreeSet<Integer> ();
        
        // iterate on image pixels
        if (image instanceof FloatProcessor) 
        {
            // For float processor, use explicit case to int from float value  
            for (int y = 0; y < sizeY; y++) 
            {
                this.fireProgressChanged(this, y, sizeY);
                for (int x = 0; x < sizeX; x++)
                {
                    labels.add((int) image.getf(x, y));
                }
            }
        } 
        else
        {
            // for integer-based images, simply use integer result
            for (int y = 0; y < sizeY; y++) 
            {
                this.fireProgressChanged(this, y, sizeY);
                for (int x = 0; x < sizeX; x++)
                {
                    labels.add(image.get(x, y));
                }
            }
        }
        
        this.fireProgressChanged(this, 1, 1);
        
        // remove 0 if it exists
        if (labels.contains(0))
            labels.remove(0);
        
        // convert to array of integers
        return convertToArray(labels);
    }

    /**
     * Returns the set of unique labels existing in the given stack, excluding 
     * the value zero (used for background).
     * 
     * @param image
     *            a 3D label image
     * @return the list of unique labels present in image (without background)
     */
    public int[] process(ImageStack image) 
    {
        // retrieve image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        // initialize tree for storing the collection of labels
        TreeSet<Integer> labels = new TreeSet<Integer> ();
        
        // create wrapper to make processing slightly faster
        Image3D image3d = Images3D.createWrapper(image);
        
        // iterate on image pixels
        for (int z = 0; z < sizeZ; z++) 
        {
            this.fireProgressChanged(this, z, sizeZ);
            for (int y = 0; y < sizeY; y++)  
            {
                for (int x = 0; x < sizeX; x++)
                {
                    labels.add(image3d.get(x, y, z));
                }
            }
        }
        
        this.fireProgressChanged(this, 1, 1);
        
        // remove 0 if it exists
        if (labels.contains(0))
            labels.remove(0);
        
        // convert to array of integers
        return convertToArray(labels);
    }
    
    /**
     * Converts a tree containing integers into a sorted array of ints.
     * 
     * @param integerTree
     *            the tree of Integers to convert
     * @return the corresponding array of ints.
     */
    private final static int[] convertToArray(TreeSet<Integer> integerTree)
    {
        int[] array = new int[integerTree.size()];
        int i = 0;
        for (int value : integerTree)
        {
            array[i++] = value;
        }
        return array;
    }
}
