/**
 * 
 */
package inra.ijpb.measure;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import inra.ijpb.geometry.Point3D;

/**
 * @author dlegland
 *
 */
public class IntrinsicVolumes3DTest
{
    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#volumes(ij.ImageStack, int[], ij.measure.Calibration)}.
     */
    @Test
    public final void testVolumes()
    {
        ImageStack image = createBallImage();
        int[] labels = new int[] {255};
        Calibration calib = new Calibration();
        
        double[] volumes = IntrinsicVolumes3D.volumes(image, labels, calib);
        
        double exp = 33510.0;
        assertEquals(1, volumes.length);
        assertEquals(exp, volumes[0], 15.0);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#volumeDensity(ij.ImageStack)}.
     */
    @Test
    public void testVolumeDensity()
    {
        String fileName = getClass().getResource("/files/microstructure3D_10x10x10.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);

        // basic check up
        assertNotNull(imagePlus);
        assertTrue(imagePlus.getStackSize() > 0);

        // get image stack and calibration
        ImageStack image = imagePlus.getStack();

        double volDensity = IntrinsicVolumes3D.volumeDensity(image);
        assertEquals(0.363, volDensity, .001);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceArea(ij.ImageStack, ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceArea_SmallCube_D3()
    {
        ImageStack image = ImageStack.create(4, 4, 4, 8);
        image.setVoxel(1, 1, 1, 255);
        image.setVoxel(2, 1, 1, 255);
        image.setVoxel(1, 2, 1, 255);
        image.setVoxel(2, 2, 1, 255);
        image.setVoxel(1, 1, 2, 255);
        image.setVoxel(2, 1, 2, 255);
        image.setVoxel(1, 2, 2, 255);
        image.setVoxel(2, 2, 2, 255);
        Calibration calib = new Calibration();
        
        double surface = IntrinsicVolumes3D.surfaceArea(image, calib, 3);
        
        double exp = 16.0;
        assertEquals(exp, surface, 16.0*0.2);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceArea(ij.ImageStack, ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceArea_SmallCubeTouchingBorder_D3()
    {
        ImageStack image = ImageStack.create(2, 2, 2, 8);
        image.setVoxel(0, 0, 0, 255);
        image.setVoxel(1, 0, 0, 255);
        image.setVoxel(0, 1, 0, 255);
        image.setVoxel(1, 1, 0, 255);
        image.setVoxel(0, 0, 1, 255);
        image.setVoxel(1, 0, 1, 255);
        image.setVoxel(0, 1, 1, 255);
        image.setVoxel(1, 1, 1, 255);
        Calibration calib = new Calibration();
        
        double surface = IntrinsicVolumes3D.surfaceArea(image, calib, 3);
        
        double exp = 16.0;
        assertEquals(exp, surface, 16.0*0.2);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_SingleBall_D3()
    {
        ImageStack image = createBallImage();
        int[] labels = new int[] {255};
        Calibration calib = new Calibration();
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 3);
        
        double exp = 5026.0;
        assertEquals(1, surfaces.length);
        assertEquals(exp, surfaces[0], 2.);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_SingleBall_D3_HalfResolX()
    {
        ImageStack image = createBallImage_HalfResolX();
        int[] labels = new int[] {255};
        Calibration calib = new Calibration();
        calib.pixelWidth = 2.0;
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 3);
        
        double exp = 5026.0;
        assertEquals(1, surfaces.length);
        assertEquals(exp, surfaces[0], exp * 0.02);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_SingleBall_D3_HalfResolY()
    {
        ImageStack image = createBallImage_HalfResolY();
        int[] labels = new int[] {255};
        Calibration calib = new Calibration();
        calib.pixelHeight = 2.0;
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 3);
        
        double exp = 5026.0;
        assertEquals(1, surfaces.length);
        assertEquals(exp, surfaces[0], exp * 0.02);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_SingleBall_D3_HalfResolZ()
    {
        ImageStack image = createBallImage_HalfResolZ();
        int[] labels = new int[] {255};
        Calibration calib = new Calibration();
        calib.pixelDepth = 2.0;
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 3);
        
        double exp = 5026.0;
        assertEquals(1, surfaces.length);
        assertEquals(exp, surfaces[0], exp * 0.02);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_SingleBall_D3_X3_Y4_Z5()
    {
    	double radius = 40;
    	
    	// create image
    	ImageStack image = ImageStack.create(100/3, 100/4, 100/5, 8);
        int[] labels = new int[] {255};
        Calibration calib = new Calibration();
        calib.pixelWidth  = 3.0;
        calib.pixelHeight = 4.0;
        calib.pixelDepth  = 5.0;
        calib.xOrigin = 0;
        calib.yOrigin = 0;
        calib.zOrigin = 0;
        
        Phantoms3D.fillBall(image, calib, new Point3D(50.0, 50.0, 50.0), radius, 255);

        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 3);
        
        double exp = 4 * Math.PI * radius * radius; // 5026.0;
        assertEquals(1, surfaces.length);
        assertEquals(exp, surfaces[0], exp * 0.02);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_SingleBall_D13()
    {
        ImageStack image = createBallImage();
        int[] labels = new int[] {255};
        Calibration calib = new Calibration();
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 13);
        
        double exp = 5026.0;
        assertEquals(1, surfaces.length);
        assertEquals(exp, surfaces[0], 2.);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_SingleBall_D13_HalfResolX()
    {
        ImageStack image = createBallImage_HalfResolX();
        int[] labels = new int[] {255};
        Calibration calib = new Calibration();
        calib.pixelWidth = 2.0;
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 13);
        
        double exp = 5026.0;
        assertEquals(1, surfaces.length);
        assertEquals(exp, surfaces[0], exp * 0.02);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_SingleBall_D13_HalfResolY()
    {
        ImageStack image = createBallImage_HalfResolY();
        int[] labels = new int[] {255};
        Calibration calib = new Calibration();
        calib.pixelHeight = 2.0;
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 13);
        
        double exp = 5026.0;
        assertEquals(1, surfaces.length);
        assertEquals(exp, surfaces[0], exp * 0.02);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_SingleBall_D13_HalfResolZ()
    {
        ImageStack image = createBallImage_HalfResolZ();
        int[] labels = new int[] {255};
        Calibration calib = new Calibration();
        calib.pixelDepth = 2.0;
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 13);
        
        double exp = 5026.0;
        assertEquals(1, surfaces.length);
        assertEquals(exp, surfaces[0], exp * 0.02);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_ManyBalls_D13()
    {
        ImageStack image = createManyBallsImage();
        int[] labels = new int[27];
        for(int i = 0; i < 27; i++)
        {
            labels[i] = i + 1;
        }
        Calibration calib = new Calibration();
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 13);
        
        double exp = 2000.0;
        assertEquals(27, surfaces.length);
        for (int i = 0; i < 27; i++)
        {
            assertEquals(exp, surfaces[i], 2.);
        }
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_TouchingLabels_D3()
    {
        ImageStack image = ImageStack.create(9, 9, 9, 8);
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                for (int k = 0; k < 3; k++)
                {
                    image.setVoxel(    i,     j,     k,  1);
                    image.setVoxel(i + 3,     j,     k,  2);
                    image.setVoxel(    i, j + 3,     k,  3);
                    image.setVoxel(i + 3, j + 3,     k,  4);
                    image.setVoxel(    i,     j, k + 3,  5);
                    image.setVoxel(i + 3,     j, k + 3,  6);
                    image.setVoxel(    i, j + 3, k + 3,  7);
                    image.setVoxel(i + 3, j + 3, k + 3,  8);
                }
            }
        }
        int[] labels = new int[] {1, 2, 3, 4, 5, 6, 7, 8};
        Calibration calib = new Calibration();
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 3);
        double exp = 36.0;
        assertEquals(8, surfaces.length);
        for (int i = 0; i < 8; i++)
        {
            assertEquals(exp, surfaces[i], exp * 0.2);
        }
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreas(ij.ImageStack, int[], ij.measure.Calibration, int)}.
     */
    @Test
    public final void testSurfaceAreas_TouchingLabels_D13()
    {
        ImageStack image = ImageStack.create(9, 9, 9, 8);
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                for (int k = 0; k < 3; k++)
                {
                    image.setVoxel(    i,     j,     k,  1);
                    image.setVoxel(i + 3,     j,     k,  2);
                    image.setVoxel(    i, j + 3,     k,  3);
                    image.setVoxel(i + 3, j + 3,     k,  4);
                    image.setVoxel(    i,     j, k + 3,  5);
                    image.setVoxel(i + 3,     j, k + 3,  6);
                    image.setVoxel(    i, j + 3, k + 3,  7);
                    image.setVoxel(i + 3, j + 3, k + 3,  8);
                }
            }
        }
        int[] labels = new int[] {1, 2, 3, 4, 5, 6, 7, 8};
        Calibration calib = new Calibration();
        
        double[] surfaces = IntrinsicVolumes3D.surfaceAreas(image, labels, calib, 13);
        double exp = 41.07;
        assertEquals(8, surfaces.length);
        for (int i = 0; i < 8; i++)
        {
            assertEquals(exp, surfaces[i], exp * 0.05);
        }
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#surfaceAreaDensity(ij.ImageStack, ij.measure.Calibration, int)}.
     */
    @Test
    public void testSurfaceAreaDensity()
    {
        String fileName = getClass().getResource("/files/microstructure3D_10x10x10.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        
        // basic check up
        assertNotNull(imagePlus);
        assertTrue(imagePlus.getStackSize() > 0);
    
        // get image stack and calibration
        ImageStack image = imagePlus.getStack();
        Calibration calib = imagePlus.getCalibration();
    
        // compare with a value computed with Matlab (2018.09.11)
        double surfaceDensity = IntrinsicVolumes3D.surfaceAreaDensity(image, calib, 13);
        assertEquals(0.56, surfaceDensity, .001);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#meanBreadth(ij.ImageStack, ij.measure.Calibration, int)}.
     */
    @Test
    public final void testMeanBreadth_SmallCube_D3()
    {
        ImageStack image = ImageStack.create(4, 4, 4, 8);
        image.setVoxel(1, 1, 1, 255);
        image.setVoxel(2, 1, 1, 255);
        image.setVoxel(1, 2, 1, 255);
        image.setVoxel(2, 2, 1, 255);
        image.setVoxel(1, 1, 2, 255);
        image.setVoxel(2, 1, 2, 255);
        image.setVoxel(1, 2, 2, 255);
        image.setVoxel(2, 2, 2, 255);
        Calibration calib = new Calibration();
        
        double meanBreadth = IntrinsicVolumes3D.meanBreadth(image, calib, 3, 8);
        
        double exp = 2.0;
        assertEquals(exp, meanBreadth, exp * 0.2);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#meanBreadth(ij.ImageStack, ij.measure.Calibration, int)}.
     */
    @Test
    public final void testMeanBreadth_SmallCube_D13()
    {
        ImageStack image = ImageStack.create(4, 4, 4, 8);
        image.setVoxel(1, 1, 1, 255);
        image.setVoxel(2, 1, 1, 255);
        image.setVoxel(1, 2, 1, 255);
        image.setVoxel(2, 2, 1, 255);
        image.setVoxel(1, 1, 2, 255);
        image.setVoxel(2, 1, 2, 255);
        image.setVoxel(1, 2, 2, 255);
        image.setVoxel(2, 2, 2, 255);
        Calibration calib = new Calibration();
        
        double meanBreadth = IntrinsicVolumes3D.meanBreadth(image, calib, 13, 8);
        
        double exp = 2.141;
        assertEquals(exp, meanBreadth, exp * 0.2);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#meanBreadth(ij.ImageStack, ij.measure.Calibration, int)}.
     */
    @Test
    public final void testMeanBreadth_SingleBall_D3()
    {
        ImageStack image = createBallImage();
        Calibration calib = new Calibration();
        
        double meanBreadth = IntrinsicVolumes3D.meanBreadth(image, calib, 3, 8);
        
        double exp = 40.0;
        assertEquals(exp, meanBreadth, exp * 0.2);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#meanBreadth(ij.ImageStack, ij.measure.Calibration, int)}.
     */
    @Test
    public final void testMeanBreadth_SingleBall_D13()
    {
        ImageStack image = createBallImage();
        Calibration calib = new Calibration();
        
        double meanBreadth = IntrinsicVolumes3D.meanBreadth(image, calib, 13, 8);
        
        double exp = 40.0;
        assertEquals(exp, meanBreadth, exp * 0.2);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#meanBreadthDensity(ij.ImageStack, ij.measure.Calibration, int, int)}.
     */
    @Test
    public void testMeanBreadthDensity()
    {
        String fileName = getClass().getResource("/files/microstructure3D_10x10x10.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        
        // basic check up
        assertNotNull(imagePlus);
        assertTrue(imagePlus.getStackSize() > 0);
    
        // get image stack and calibration
        ImageStack image = imagePlus.getStack();
        Calibration calib = imagePlus.getCalibration();
    
        // compare with a value computed with Matlab (2018.09.11)
        double meanBreadthDensity = IntrinsicVolumes3D.meanBreadthDensity(image, calib, 13, 8);
        assertEquals(-0.014, meanBreadthDensity, .001);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#eulerNumber(ij.ImageStack, int)}.
     */
    @Test
    public final void testEulerNumber_ball_C6()
    {
        ImageStack image = createBallImage();
    
        double euler = IntrinsicVolumes3D.eulerNumber(image, 6);
        
        assertEquals(1, euler, 0.1);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#eulerNumber(ij.ImageStack, int)}.
     */
    @Test
    public final void testEulerNumber_ball_C26()
    {
        ImageStack image = createBallImage();
    
        double euler = IntrinsicVolumes3D.eulerNumber(image, 26);
        
        assertEquals(1, euler, 0.1);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#eulerNumbers(ij.ImageStack, int[], int)}.
     */
    @Test
    public final void testEulerNumbers_C6()
    {
        ImageStack image = createEulerImage();
        int[] labels = {1, 2, 3, 4};

        double[] euler = IntrinsicVolumes3D.eulerNumbers(image, labels, 6);
        
        assertEquals(1, euler[0], 0.1);
        assertEquals(8, euler[1], 0.1);
        assertEquals(0, euler[2], 0.1);
        assertEquals(2, euler[3], 0.1);
    }

    /**
     * Test method for {@link inra.ijpb.measure.IntrinsicVolumes3D#eulerNumbers(ij.ImageStack, int[], int)}.
     */
    @Test
    public final void testEulerNumbers_C26()
    {
        ImageStack image = createEulerImage();
        int[] labels = {1, 2, 3, 4};

        double[] euler = IntrinsicVolumes3D.eulerNumbers(image, labels, 26);
        
        assertEquals(1, euler[0], 0.1);
        assertEquals(8, euler[1], 0.1);
        assertEquals(0, euler[2], 0.1);
        assertEquals(2, euler[3], 0.1);
    }

    /**
     * Generate a ball of radius 20 in a discrete image of size 50x50x50, with a
     * center not exactly in the center of the central voxel.
     * 
     * Expected surface area is around 5026.
     * Expected mean breadth is equal to the diameter, i.e. 40.
     */
	private final static ImageStack createBallImage()
	{
		// ball features
        double xc = 25.12;
        double yc = 25.23;
        double zc = 25.34;
        double radius = 20;
        double r2 = radius * radius;
        
        // image size
        int size1 = 50;
        int size2 = 50;
        int size3 = 50;
        
        ImageStack result = ImageStack.create(size1, size2, size3, 8);
        
        for (int z = 0; z < size3; z++) {
            double z2 = z - zc; 
            for (int y = 0; y < size2; y++) {
                double y2 = y - yc; 
                for (int x = 0; x < size1; x++) {
                    double x2 = x - xc;
                    double ri = x2 * x2 + y2 * y2 + z2 * z2; 
                    if (ri <= r2) {
                        result.setVoxel(x, y, z, 255);
                    }
                }
            }
        }
        
        return result;
    }
    
	private final static ImageStack createBallImage_HalfResolX()
	{
		ImageStack baseImage = createBallImage();
		int size1 = baseImage.getWidth() / 2;
		int size2 = baseImage.getHeight();
		int size3 = baseImage.getSize();
		
        ImageStack result = ImageStack.create(size1, size2, size3, 8);
        
		for (int z = 0; z < size3; z++)
		{
			for (int y = 0; y < size2; y++)
			{
				for (int x = 0; x < size1; x++)
				{
                    result.setVoxel(x, y, z, baseImage.getVoxel(2 * x, y, z));
                }
            }
        }
		
		return result;
	}
		
	private final static ImageStack createBallImage_HalfResolY()
	{
		ImageStack baseImage = createBallImage();
		int size1 = baseImage.getWidth();
		int size2 = baseImage.getHeight() / 2;
		int size3 = baseImage.getSize();
		
        ImageStack result = ImageStack.create(size1, size2, size3, 8);
        
		for (int z = 0; z < size3; z++)
		{
			for (int y = 0; y < size2; y++)
			{
				for (int x = 0; x < size1; x++)
				{
                    result.setVoxel(x, y, z, baseImage.getVoxel(x, 2 * y, z));
                }
            }
        }
		
		return result;
	}
		
	private final static ImageStack createBallImage_HalfResolZ()
	{
		ImageStack baseImage = createBallImage();
		int size1 = baseImage.getWidth();
		int size2 = baseImage.getHeight();
		int size3 = baseImage.getSize() / 2;
		
        ImageStack result = ImageStack.create(size1, size2, size3, 8);
        
		for (int z = 0; z < size3; z++)
		{
			for (int y = 0; y < size2; y++)
			{
				for (int x = 0; x < size1; x++)
				{
                    result.setVoxel(x, y, z, baseImage.getVoxel(x, y, 2 * z));
                }
            }
        }
		
		return result;
	}
		
    /**
     * Generate an image containing 27 balls with same radius.
     * Radius of the ball is 12.61, resulting in a surface area of around 2000.
     */
    private final static ImageStack createManyBallsImage() {
        // ball features
        double xc = 20.12;
        double yc = 20.23;
        double zc = 20.34;
        double radius = 12.62;
        double r2 = radius * radius;
        
        // image size
        int size1 = 100;
        int size2 = 100;
        int size3 = 100;
        
        ImageStack result = ImageStack.create(size1, size2, size3, 8);
        
        for (int z = 5; z < 35; z++) {
            double z2 = z - zc; 
            for (int y = 5; y < 35; y++) {
                double y2 = y - yc; 
                for (int x = 5; x < 35; x++) {
                    double x2 = x - xc;
                    double ri = x2 * x2 + y2 * y2 + z2 * z2; 
                    if (ri <= r2) {
                        result.setVoxel(x, y, z, 1);
                        result.setVoxel(x + 30, y, z, 2);
                        result.setVoxel(x + 60, y, z, 3);
                        result.setVoxel(x, y + 30, z, 4);
                        result.setVoxel(x + 30, y + 30, z, 5);
                        result.setVoxel(x + 60, y + 30, z, 6);
                        result.setVoxel(x, y + 60, z, 7);
                        result.setVoxel(x + 30, y + 60, z, 8);
                        result.setVoxel(x + 60, y + 60, z, 9);
                        
                        result.setVoxel(x, y, z + 30, 10);
                        result.setVoxel(x + 30, y, z + 30, 11);
                        result.setVoxel(x + 60, y, z + 30, 12);
                        result.setVoxel(x, y + 30, z + 30, 13);
                        result.setVoxel(x + 30, y + 30, z + 30, 14);
                        result.setVoxel(x + 60, y + 30, z + 30, 15);
                        result.setVoxel(x, y + 60, z + 30, 16);
                        result.setVoxel(x + 30, y + 60, z + 30, 17);
                        result.setVoxel(x + 60, y + 60, z + 30, 18);

                        result.setVoxel(x, y, z + 60, 19);
                        result.setVoxel(x + 30, y, z + 60, 20);
                        result.setVoxel(x + 60, y, z + 60, 21);
                        result.setVoxel(x, y + 30, z + 60, 22);
                        result.setVoxel(x + 30, y + 30, z + 60, 23);
                        result.setVoxel(x + 60, y + 30, z + 60, 24);
                        result.setVoxel(x, y + 60, z + 60, 25);
                        result.setVoxel(x + 30, y + 60, z + 60, 26);
                        result.setVoxel(x + 60, y + 60, z + 60, 27);

                    }
                }
            }
        }
        
        return result;
    }   
    
    /**
     * Generate the 3D test image with 7 labels for measuring Euler Number.
     * 
     * Labels:
     * 1: a single blob, with some thickness (Euler = 1)
     * 2: a set of single points (Euler = 8)
     * 3: a single loop, one voxel thickness (Euler = 0)
     * 4: a hollow sphere (Euler = 2)
     */
    private final static ImageStack createEulerImage() 
    {
        ImageStack labelImage = ImageStack.create(10, 10, 10, 8);
        
        // Label 1 -> a single compact blob
        for(int z = 1; z < 4; z++)
        {
            for(int y = 1; y < 4; y++)
            {
                for(int x = 1; x < 4; x++)
                {
                    labelImage.setVoxel(x, y, z, 1);
                }
            }
        }
        
        // Label 2 -> eight indivdual voxels
        labelImage.setVoxel(5, 1, 1, 2);
        labelImage.setVoxel(7, 1, 1, 2);
        labelImage.setVoxel(5, 3, 1, 2);
        labelImage.setVoxel(7, 3, 1, 2);
        labelImage.setVoxel(5, 1, 3, 2);
        labelImage.setVoxel(7, 1, 3, 2);
        labelImage.setVoxel(5, 3, 3, 2);
        labelImage.setVoxel(7, 3, 3, 2);
    
        // Label 3 -> a single loop
        for (int x = 1; x < 4; x++)
        {
            labelImage.setVoxel(x, 5, 1, 3);
            labelImage.setVoxel(x, 7, 1, 3);
            labelImage.setVoxel(x, 5, 3, 3);
            labelImage.setVoxel(x, 7, 3, 3);
        }
        labelImage.setVoxel(3, 6, 1, 3);
        labelImage.setVoxel(3, 6, 3, 3);
        labelImage.setVoxel(1, 5, 2, 3);
        labelImage.setVoxel(1, 7, 2, 3);
    
        // Label 4 -> hollow cube
        for(int z = 1; z < 4; z++)
        {
            for(int y = 5; y < 8; y++)
            {
                for(int x = 5; x < 8; x++)
                {
                    labelImage.setVoxel(x, y, z, 4);
                }
            }
        }
        labelImage.setVoxel(6, 6, 2, 0);
        
        return labelImage;
    }
}
