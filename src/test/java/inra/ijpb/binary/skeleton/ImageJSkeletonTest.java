/**
 * 
 */
package inra.ijpb.binary.skeleton;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class ImageJSkeletonTest
{
    @Test
    public void test_process_ThinLines()
    {
        // Create a synthetic image containing 4 bars
        // bar 1: x =  1 ->  3,  y = 1 -> 8
        // bar 2: x =  5 -> 12,  y = 1 -> 3
        // bar 3: x =  5 -> 12,  y = 6 -> 8
        // bar 4: x = 15 -> 17,  y = 1 -> 8
        ImageProcessor image = new ByteProcessor(20, 10);
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                image.set(j+1,  i+1, 255);
                image.set(i+5,  j+1, 255);
                image.set(i+5,  j+6, 255);
                image.set(j+15, i+1, 255);
            }
        }
        // IJUtils.printImage(image);
        
        ImageJSkeleton algo = new ImageJSkeleton();
        ImageProcessor result = algo.process(image);

//        System.out.println("Result:");
//        IJUtils.printImage(result);

        assertEquals(255, result.get( 2, 6));
        assertEquals(255, result.get( 8, 2));
        assertEquals(255, result.get( 8, 7));
        assertEquals(255, result.get(16, 6));
    }

    @Test
    public void test_process_ThinLines_TouchBorders()
    {
        // Create a synthetic image containing 4 bars
        // bar 1: x =  0 ->  2,  y = 0 -> 9
        // bar 2: x =  5 -> 14,  y = 0 -> 2
        // bar 3: x =  5 -> 14,  y = 7 -> 9
        // bar 4: x = 17 -> 19,  y = 0 -> 9
        ImageProcessor image = new ByteProcessor(20, 10);
        for (int i = 0; i < 10; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                image.set(j,  i, 255);
                image.set(i+5,  j, 255);
                image.set(i+5,  j+7, 255);
                image.set(j+17, i, 255);
            }
        }
//        System.out.println("Image:");
//        IJUtils.printImage(image);
        
        ImageJSkeleton algo = new ImageJSkeleton();
        ImageProcessor result = algo.process(image);
//        System.out.println("Result:");
//        IJUtils.printImage(result);
        
        assertEquals(255, result.get( 1, 6));
        assertEquals(255, result.get( 8, 1));
        assertEquals(255, result.get(10, 8));
        assertEquals(255, result.get(18, 6));
    }

    @Test
    public void test_process_ThinLines_TouchingLabels()
    {
        // Create a synthetic image containing 5 bars
        // bar 1: x =  0 ->  2,  y = 0 -> 9, label 2
        // bar 2: x =  3 -> 11,  y = 0 -> 2, label 3
        // bar 3: x =  3 -> 11,  y = 3 -> 5, label 4
        // bar 4: x =  3 -> 11,  y = 6 -> 8, label 5
        // bar 5: x = 12 -> 14,  y = 0 -> 9, label 7
        ImageProcessor image = new ByteProcessor(15, 9);
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                image.set(j, i, 2);
                image.set(i+3, j, 3);
                image.set(i+3, j+3, 4);
                image.set(i+3, j+6, 5);
                image.set(j+12, i, 7);
            }
        }
//        System.out.println("Image:");
//        IJUtils.printImage(image);
        
        ImageJSkeleton algo = new ImageJSkeleton();
        ImageProcessor result = algo.process(image);
//        System.out.println("Result:");
//        IJUtils.printImage(result);

        // region 1, label = 2
        assertEquals(0, result.get( 0, 6));
        assertEquals(2, result.get( 1, 6));
        assertEquals(0, result.get( 2, 6));
        
        // region 2, label = 2
        assertEquals(0, result.get( 8, 0));
        assertEquals(3, result.get( 8, 1));
        assertEquals(0, result.get( 8, 2));
        
        // region 3, label = 4
        assertEquals(0, result.get( 8, 3));
        assertEquals(4, result.get( 8, 4));
        assertEquals(0, result.get( 8, 5));

        // region 4, label = 5
        assertEquals(0, result.get( 8, 6));
        assertEquals(5, result.get( 8, 7));
        assertEquals(0, result.get( 8, 8));

        // region 5, label = 7
        assertEquals(0, result.get(12, 6));
        assertEquals(7, result.get(13, 6));
        assertEquals(0, result.get(14, 6));
    }
}
