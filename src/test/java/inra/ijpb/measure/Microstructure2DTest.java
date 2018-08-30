/**
 * 
 */
package inra.ijpb.measure;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author dlegland
 *
 */
public class Microstructure2DTest
{
    /**
     * Test method for {@link inra.ijpb.measure.Microstructure2D#areaDensity(ij.process.ImageProcessor)}.
     */
    @Test
    public final void testAreaDensity_OhserMecklich()
    {
        ImageProcessor image = createOhserMuecklichImage();
        
        double density = Microstructure2D.areaDensity(image);
        
        assertEquals(0.3008, density, .001);
    }

   /**
     * Test method for {@link inra.ijpb.measure.Microstructure2D#eulerNumberDensity(ij.process.ImageProcessor, int)}.
     */
    @Test
    public final void testPerimeterDensity_OhserMecklich_D2()
    {
        ImageProcessor image = createOhserMuecklichImage();
        Calibration calib = new Calibration();
        
        double density = Microstructure2D.perimeterDensity(image, calib, 2);
        
        assertEquals(0.5, density, .05);
    }

    /**
     * Test method for {@link inra.ijpb.measure.Microstructure2D#eulerNumberDensity(ij.process.ImageProcessor, int)}.
     */
    @Test
    public final void testPerimeterDensity_OhserMecklich_D4()
    {
        ImageProcessor image = createOhserMuecklichImage();
        Calibration calib = new Calibration();
        
        double density = Microstructure2D.perimeterDensity(image, calib, 4);
        
        assertEquals(0.5, density, .05);
    }

    /**
     * Test method for {@link inra.ijpb.measure.Microstructure2D#eulerNumberDensity(ij.process.ImageProcessor, int)}.
     */
    @Test
    public final void testEulerNumberDensity_OhserMecklich_C4()
    {
        ImageProcessor image = createOhserMuecklichImage();
        Calibration calib = new Calibration();
        
        double density = Microstructure2D.eulerNumberDensity(image, calib, 4);
        
        assertEquals(0.0444, density, .001);
    }

    /**
     * Test method for {@link inra.ijpb.measure.Microstructure2D#eulerNumberDensity(ij.process.ImageProcessor, int)}.
     */
    @Test
    public final void testEulerNumberDensity_OhserMecklich_C8()
    {
        ImageProcessor image = createOhserMuecklichImage();
        Calibration calib = new Calibration();
        
        double density = Microstructure2D.eulerNumberDensity(image, calib, 8);
        
        assertEquals(0.0267, density, .001);
    }


    /**
     * Generate the sample image provided as example in the Book "Statistical
     * Analysis of microstructures in material sciences", from J. Ohser and F.
     * Muecklich.
     * 
     * @return a sample image
     */
    public final ImageProcessor createOhserMuecklichImage()
    {
        int[][] data = new int[][] {
            {0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0}, 
            {0, 1, 0, 0,  1, 1, 1, 1,  0, 0, 0, 0,  0, 0, 0, 1}, 
            {0, 1, 1, 0,  0, 1, 1, 1,  0, 0, 0, 0,  1, 1, 0, 0}, 
            {0, 1, 1, 1,  0, 1, 1, 1,  0, 1, 1, 0,  0, 0, 1, 0}, 
            {0, 0, 0, 0,  0, 1, 1, 1,  0, 0, 0, 1,  0, 0, 1, 0}, 
            {0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 1, 0}, 
            {0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 1, 1, 0}, 
            {0, 0, 0, 0,  0, 0, 1, 1,  1, 1, 0, 0,  1, 0, 1, 0}, 
            {0, 0, 0, 0,  0, 0, 1, 0,  1, 0, 0, 1,  1, 0, 0, 0}, 
            {0, 0, 0, 0,  1, 0, 1, 0,  1, 0, 0, 1,  1, 0, 1, 0}, 
            {0, 1, 0, 0,  1, 0, 1, 1,  1, 0, 1, 0,  1, 0, 1, 0}, 
            {0, 1, 0, 1,  1, 0, 0, 0,  0, 0, 1, 0,  0, 0, 1, 0}, 
            {0, 1, 1, 1,  0, 0, 0, 0,  0, 0, 1, 1,  1, 1, 1, 0}, 
            {0, 0, 1, 1,  0, 0, 0, 0,  0, 1, 1, 1,  1, 1, 0, 0}, 
            {0, 0, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0,  0, 0, 0, 0}, 
            {0, 0, 0, 0,  0, 1, 1, 0,  0, 0, 0, 0,  0, 0, 0, 0}, 
        };

        int sizeX = 16;
        int sizeY = 16;
        ByteProcessor image = new ByteProcessor(sizeX, sizeY);
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeY; x++)
            {
                image.set(x, y, data[y][x] > 0 ? 255 : 0);
            }
        }
        
        return image;
    }

}
