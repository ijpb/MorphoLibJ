/**
 * 
 */
package inra.ijpb.measure;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;

/**
 * @author dlegland
 *
 */
public class IntensityMeasuresTest
{
    
    /**
     * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMean()}.
     */
    @Test
    public final void testGetMean()
    {
        String fileName = getClass().getResource("/files/grains.tif").getFile();
        ImagePlus grayImagePlus = IJ.openImage(fileName);
        assertNotNull(grayImagePlus);
        
        fileName = getClass().getResource("/files/grains-med-WTH-lbl.tif").getFile();
        ImagePlus labelImagePlus = IJ.openImage(fileName);
        assertNotNull(labelImagePlus);
        
        IntensityMeasures algo = new IntensityMeasures(grayImagePlus, labelImagePlus);
        
        ResultsTable table = algo.getMean();
        assertTrue(table.getCounter() > 80);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMean()}.
     */
    @Test
    public final void testGetMean_MissingLabels()
    {
        String fileName = getClass().getResource("/files/grains.tif").getFile();
        ImagePlus grayImagePlus = IJ.openImage(fileName);
        assertNotNull(grayImagePlus);
        
        fileName = getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile();
        ImagePlus labelImagePlus = IJ.openImage(fileName);
        assertNotNull(labelImagePlus);
        
        IntensityMeasures algo = new IntensityMeasures(grayImagePlus, labelImagePlus);
        
        ResultsTable table = algo.getMean();
        assertTrue(table.getCounter() > 40);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMedian()}.
     */
    @Test
    public final void testGetMedian()
    {
        String fileName = getClass().getResource("/files/grains.tif").getFile();
        ImagePlus grayImagePlus = IJ.openImage(fileName);
        assertNotNull(grayImagePlus);
        
        fileName = getClass().getResource("/files/grains-med-WTH-lbl.tif").getFile();
        ImagePlus labelImagePlus = IJ.openImage(fileName);
        assertNotNull(labelImagePlus);
        
        IntensityMeasures algo = new IntensityMeasures(grayImagePlus, labelImagePlus);
        
        ResultsTable table = algo.getMedian();
        assertTrue(table.getCounter() > 80);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMedian()}.
     */
    @Test
    public final void testGetMedian3D()
    {
        ImagePlus labelImagePlus = createImage3DEightLabels();
        ImagePlus grayImagePlus = new ImagePlus("values", ImageStack.create(10, 10, 10, 32));
        
        IntensityMeasures algo = new IntensityMeasures(grayImagePlus, labelImagePlus);
        
        ResultsTable table = algo.getMedian();
        assertTrue(table.getCounter() == 8);
    }
    

    /**
     * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMedian()}.
     */
    @Test
    public final void testGetMedian_MissingLabels()
    {
        String fileName = getClass().getResource("/files/grains.tif").getFile();
        ImagePlus grayImagePlus = IJ.openImage(fileName);
        assertNotNull(grayImagePlus);
        
        fileName = getClass().getResource("/files/grains-WTH-areaOpen-lbl2.tif").getFile();
        ImagePlus labelImagePlus = IJ.openImage(fileName);
        assertNotNull(labelImagePlus);
        
        IntensityMeasures algo = new IntensityMeasures(grayImagePlus, labelImagePlus);
        
        ResultsTable table = algo.getMedian();
        assertTrue(table.getCounter() > 40);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMedian()}.
     */
    @Test
    public final void testGetMedian3D_MissingLabels()
    {
        ImagePlus labelImagePlus = createImage3DEightLabels2();
        ImagePlus grayImagePlus = new ImagePlus("values", ImageStack.create(10, 10, 10, 32));
        
        IntensityMeasures algo = new IntensityMeasures(grayImagePlus, labelImagePlus);
        
        ResultsTable table = algo.getMedian();
        assertTrue(table.getCounter() == 8);
    }
    
    /**
     * Test method for {@link inra.ijpb.measure.IntensityMeasures#getMedian()}.
     */
    @Test
    public final void testGetNeigborsMedian3D_Binary()
    {
        // use the same image for label and gray levels
        ImagePlus labelImagePlus = createImage3DCube();
        ImagePlus grayImagePlus = createImage3DCube();
       
        IntensityMeasures algo = new IntensityMeasures(grayImagePlus, labelImagePlus);
        
        ResultsTable table = algo.getNeighborsMedian();
        assertTrue(table.getCounter() == 1);
    }
    
    private static final ImagePlus createImage3DCube()
    {
        ImageStack image = ImageStack.create(10, 10, 10, 8);
        for (int z = 2; z < 8; z++)
        {
            for (int y = 2; y < 8; y++)
            {
                for (int x = 2; x < 8; x++)
                {
                    image.setVoxel(x, y, z, 255);
                }
            }
        }
        
        return new ImagePlus("labels", image);
    }
    
    /**
     * @return a 10x10x10 black image containing a 8x8x8 cube with value 255 in
     *         the middle of the image.
     */
    private static final ImagePlus createImage3DEightLabels()
    {
        ImageStack image = ImageStack.create(10, 10, 10, 8);
        for (int z = 0; z < 2; z++)
        {
            for (int y = 0; y < 2; y++)
            {
                for (int x = 0; x < 2; x++)
                {
                    image.setVoxel(x+2, y+2, z+2, 1);
                    image.setVoxel(x+6, y+2, z+2, 2);
                    image.setVoxel(x+2, y+6, z+2, 3);
                    image.setVoxel(x+6, y+6, z+2, 4);
                    image.setVoxel(x+2, y+2, z+6, 5);
                    image.setVoxel(x+6, y+2, z+6, 6);
                    image.setVoxel(x+2, y+6, z+6, 7);
                    image.setVoxel(x+6, y+6, z+6, 8);
                }
            }
        }
        
        return new ImagePlus("labels", image);
    }
    
    private static final ImagePlus createImage3DEightLabels2()
    {
        ImageStack image = ImageStack.create(10, 10, 10, 8);
        for (int z = 0; z < 2; z++)
        {
            for (int y = 0; y < 2; y++)
            {
                for (int x = 0; x < 2; x++)
                {
                    image.setVoxel(x+2, y+2, z+2, 2);
                    image.setVoxel(x+6, y+2, z+2, 4);
                    image.setVoxel(x+2, y+6, z+2, 6);
                    image.setVoxel(x+6, y+6, z+2, 8);
                    image.setVoxel(x+2, y+2, z+6, 10);
                    image.setVoxel(x+6, y+2, z+6, 12);
                    image.setVoxel(x+2, y+6, z+6, 14);
                    image.setVoxel(x+6, y+6, z+6, 16);
                }
            }
        }
        
        return new ImagePlus("labels", image);
    }
}
