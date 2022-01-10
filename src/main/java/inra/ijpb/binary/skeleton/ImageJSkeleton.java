/**
 * 
 */
package inra.ijpb.binary.skeleton;

import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;

/**
 * @author dlegland
 *
 */

/**
 * Adaptation of the skeletonization code from ImageJ.
 * 
 * @author dlegland
 *
 */
public class ImageJSkeleton extends AlgoStub
{
    /**
     * The look-up-table for converting between index of 3-by-3 configuration
     * (256 indices, without central pixel) and a deletion flag for central
     * vertex.
     * 
     * 1 -> delete in first pass 
     * 2 -> delete in second pass 
     * 3 -> delete in either pass
     */
    private static int[] table1  =             
        {
                // 0->3  4        8        12       16       20       24       28
                0,0,0,0, 0,0,1,3, 0,0,3,1, 1,0,1,3, 0,0,0,0, 0,0,0,0, 0,0,2,0, 3,0,3,3,
                0,0,0,0, 0,0,0,0, 3,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 2,0,0,0, 3,0,2,2,
                0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0,
                2,0,0,0, 0,0,0,0, 2,0,0,0, 2,0,0,0, 3,0,0,0, 0,0,0,0, 3,0,0,0, 3,0,2,0,
                0,0,3,1, 0,0,1,3, 0,0,0,0, 0,0,0,1, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,1,
                3,1,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 2,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0,
                2,3,1,3, 0,0,1,3, 0,0,0,0, 0,0,0,1, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0,
                2,3,0,1, 0,0,0,1, 0,0,0,0, 0,0,0,0, 3,3,0,1, 0,0,0,0, 2,2,0,0, 2,0,0,0
        };

    /**
     * Another table for removing additional (spurious ?) pixels.
     */
    private static int[] table2  =
        {
                // 0->3  4        8        12       16       20       24       28
                0,0,0,1, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 2,0,2,2, 0,0,0,0,
                0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,2,0, 2,0,0,0, 0,0,0,0, 0,0,2,0, 0,0,0,0,
                0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0,
                0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0,
                0,1,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,2,0, 0,0,0,0,
                0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 2,0,0,0, 0,0,0,0,
                0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0,
                0,0,1,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0
        };

    /**
     * Creates a new skeletonizer.
     */
    public ImageJSkeleton()
    {
    }
    
    /**
	 * Computes the skeleton of the region within the binary image and returns
	 * the result in a new ImageProcessor.
	 * 
	 * @param image
	 *            the binary image to process
	 * @return the result of skeleton computation
	 */
    public ImageProcessor process(ImageProcessor image)
    {
       
        ImageProcessor result = image.duplicate();
        
        // note: original IJ algo clears pixels on the boundary
        
        int removedPixels;
        do
        {
            removedPixels = thin(result, 1, table1);
            removedPixels += thin(result, 2, table1);
        } while (removedPixels > 0);
        
        // use a second table to remove "stuck" pixels
        do
        {
            removedPixels = thin(result, 1, table2);
            removedPixels += thin(result, 2, table2);
        } while (removedPixels > 0);

        return result;
        
    }
    
    /**
     * Applies a two-passes thinning operation on the input image, using the
     * pass number and the specified look-up table.
     * 
     * @param image
     *            the binary image to process
     * @param pass
     *            the number of the pass (1 or 2)
     * @param table
     *            the look-up-table indicating for each configuration whether it
     *            should be removed or not
     * @return the number of removed pixels.
     */
    private int thin(ImageProcessor image, int pass, int[] table)
    {
        // get image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // to keep information about original pixels
        ImageProcessor copy = image.duplicate();
        
        // count the number of removed pixels
        int removedPixels = 0;
        
        // Original IJ algorithm does not process image borders
        for (int  y = 0; y < sizeY; y++)
        {
            for (int  x = 0; x < sizeX; x++)
            {
                // label value of current pixel
                int label = (int) copy.getf(x, y);
                
                // do not process background pixels
                if (label == 0)
                {
                    continue;
                }

                // determine index of current 3-by-3 configuration
                int index = 0;
                // Process neighbor pixels on previous line
                if (y > 0)
                {
                    if (x > 0)
                    {
                        if ((int) copy.getf(x-1, y-1) == label) index |=  1;
                    }
                    if ((int) copy.getf(x, y-1) == label) index |=  2;
                    if (x < sizeX - 1)
                    {
                        if ((int) copy.getf(x+1, y-1) == label) index |=  4;
                    }
                }
                // Process neighbor pixels on current line
                if (x > 0)
                {
                    if ((int) copy.getf(x-1, y) == label) index |= 128;
                }
                if (x < sizeX - 1)
                {
                    if ((int) copy.getf(x+1, y) == label) index |= 8;
                }
                // Process neighbor pixels on next line
                if (y < sizeY-1)
                {
                    if (x > 0)
                    {
                        if ((int) copy.getf(x-1, y+1) == label) index |= 64;
                    }
                    if ((int) copy.getf(x, y+1) == label) index |= 32;
                    if (x < sizeX - 1)
                    {
                        if ((int) copy.getf(x+1, y+1) == label) index |= 16;
                    }
                }
                
                int code = table[index];
                if ((code & pass) > 0)
                {
                    image.set(x, y, 0);
                    removedPixels++;
                }
            }
        }
        
        return removedPixels;
    }
}
