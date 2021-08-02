/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Connectivity2D;

/**
 * @author dlegland
 *
 */
public class GrayscaleAttributeFilteringTest
{
	/**
	 * Test method for {@link inra.ijpb.morphology.attrfilt.GrayscaleAttributeFiltering#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testProcess_Opening_Area()
	{
		ImageProcessor array = createImage();
		GrayscaleAttributeFiltering algo = new GrayscaleAttributeFiltering(AttributeFilterType.OPENING, Attribute2D.AREA, 7, Connectivity2D.C4);
		
		ImageProcessor result = algo.process(array);
		
		assertEquals(array.getWidth(), result.getWidth());
		assertEquals(array.getHeight(), result.getHeight());
		// should remove elongated regions
		assertEquals(0, result.get(1, 6));
		assertEquals(0, result.get(6, 1));
		// should keep square region
		assertEquals(15, result.get(4, 4));
		// should remove single pixel
		assertEquals(0, result.get(8, 8));
	}

	/**
	 * Test method for {@link inra.ijpb.morphology.attrfilt.GrayscaleAttributeFiltering#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testProcess_TopHat_Area()
	{
		ImageProcessor array = createImage();
		GrayscaleAttributeFiltering algo = new GrayscaleAttributeFiltering(AttributeFilterType.TOP_HAT, Attribute2D.AREA, 7, Connectivity2D.C4);
		
		ImageProcessor result = algo.process(array);
		
		assertEquals(array.getWidth(), result.getWidth());
		assertEquals(array.getHeight(), result.getHeight());
		// should remove elongated regions
		assertEquals(3, result.get(6, 1));
		assertEquals(4, result.get(1, 6));
		// should keep square region
		assertEquals(0, result.get(4, 4));
		// should remove single pixel
		assertEquals(20, result.get(8, 8));
	}

	/**
	 * Test method for {@link inra.ijpb.morphology.attrfilt.GrayscaleAttributeFiltering#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testProcess_Opening_Diagonal()
	{
		ImageProcessor array = createImage();
		GrayscaleAttributeFiltering algo = new GrayscaleAttributeFiltering(AttributeFilterType.OPENING, Attribute2D.BOX_DIAGONAL, 5, Connectivity2D.C4);
		
		ImageProcessor result = algo.process(array);
		
		assertEquals(array.getWidth(), result.getWidth());
		assertEquals(array.getHeight(), result.getHeight());
		// should keep elongated regions
		assertEquals(3, result.get(6, 1));
		assertEquals(4, result.get(1, 6));
		// should remove square region
		assertEquals(0, result.get(4, 4));
		// should remove single pixel
		assertEquals(0, result.get(8, 8));
	}
	
	/**
	 * Test method for {@link inra.ijpb.morphology.attrfilt.GrayscaleAttributeFiltering#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testProcess_Opening_Area_C8()
	{
		ImageProcessor array = createImage();
		GrayscaleAttributeFiltering algo = new GrayscaleAttributeFiltering(AttributeFilterType.OPENING, Attribute2D.AREA, 7, Connectivity2D.C8);
		
		ImageProcessor result = algo.process(array);
		
		assertEquals(array.getWidth(), result.getWidth());
		assertEquals(array.getHeight(), result.getHeight());
		// should keep elongated regions, as the total area is greater than 7
		assertEquals(3, result.get(1, 6));
		assertEquals(3, result.get(6, 1));
		// should keep square region
		assertEquals(15, result.get(4, 4));
		// should remove single pixel
		assertEquals(0, result.get(8, 8));
	}

	/**
	 * Creates an image with:
	 * <ul>
	 * <li>two elongated regions with a length of 6 pixels</li> 
	 * <li>one square regions with a side length of 4 pixels</li> 
	 * <li>one single-pixel region</li>
	 * </ul>
	 *  
	 * @return the resulting test image.
	 */
	private ImageProcessor createImage()
	{
		ImageProcessor array = new ByteProcessor(10, 10);
		for (int i = 2; i < 8; i++)
		{
			array.set(i, 1, 3);
			array.set(1, i, 4);
		}
		for (int y = 3; y < 7; y++)
		{
			for (int x = 3; x < 7; x++)
			{
				array.set(x, y, 15);
			}
		}
		array.set(8, 8, 20);
		
		return array;
	}
}
