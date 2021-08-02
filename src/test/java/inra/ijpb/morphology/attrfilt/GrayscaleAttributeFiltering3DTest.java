/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;
import inra.ijpb.morphology.Connectivity3D;

/**
 * @author dlegland
 *
 */
public class GrayscaleAttributeFiltering3DTest
{
	/**
	 * Test method for {@link inra.ijpb.morphology.attrfilt.GrayscaleAttributeFiltering3D#process(ij.process.ImageStack)}.
	 */
	@Test
	public final void testProcess_Opening_Volume()
	{
		ImageStack array = createImage();
		GrayscaleAttributeFiltering3D algo = new GrayscaleAttributeFiltering3D(AttributeFilterType.OPENING, Attribute3D.VOLUME, 7, Connectivity3D.C6);
		
		ImageStack result = algo.process(array);
		
		assertEquals(array.getWidth(), result.getWidth());
		assertEquals(array.getHeight(), result.getHeight());
		assertEquals(array.getSize(), result.getSize());
		
		// should remove elongated regions
		assertEquals(0, (int) result.getVoxel(6, 1, 1));
		assertEquals(0, (int) result.getVoxel(1, 6, 1));
		assertEquals(0, (int) result.getVoxel(1, 1, 6));
		// should keep square region
		assertEquals(15, (int) result.getVoxel(4, 4, 4));
		// should remove single pixel
		assertEquals(0, (int) result.getVoxel(8, 8, 8));
	}

	/**
	 * Test method for {@link inra.ijpb.morphology.attrfilt.GrayscaleAttributeFiltering3D#process(ij.process.ImageStack)}.
	 */
	@Test
	public final void testProcess_TopHat_Volume()
	{
		ImageStack array = createImage();
		GrayscaleAttributeFiltering3D algo = new GrayscaleAttributeFiltering3D(AttributeFilterType.TOP_HAT, Attribute3D.VOLUME, 7, Connectivity3D.C6);
		
		ImageStack result = algo.process(array);
		
		assertEquals(array.getWidth(), result.getWidth());
		assertEquals(array.getHeight(), result.getHeight());
		assertEquals(array.getSize(), result.getSize());

		// should remove elongated regions
		assertEquals(3, (int) result.getVoxel(6, 1, 1));
		assertEquals(4, (int) result.getVoxel(1, 6, 1));
		assertEquals(5, (int) result.getVoxel(1, 1, 6));
		// should keep square region
		assertEquals(0, (int) result.getVoxel(4, 4, 4));
		// should remove single pixel
		assertEquals(20, (int) result.getVoxel(8, 8, 8));
	}
	
	/**
	 * Test method for {@link inra.ijpb.morphology.attrfilt.GrayscaleAttributeFiltering3D#process(ij.process.ImageStack)}.
	 */
	@Test
	public final void testProcess_Opening_Volume_C26()
	{
		ImageStack array = createImage();
		GrayscaleAttributeFiltering3D algo = new GrayscaleAttributeFiltering3D(AttributeFilterType.OPENING, Attribute3D.VOLUME, 7, Connectivity3D.C26);
		
		ImageStack result = algo.process(array);
		
		assertEquals(array.getWidth(), result.getWidth());
		assertEquals(array.getHeight(), result.getHeight());
		assertEquals(array.getSize(), result.getSize());

		// should keep elongated regions, as the total area is greater than 7
		assertEquals(3, (int) result.getVoxel(6, 1, 1));
		assertEquals(4, (int) result.getVoxel(1, 6, 1));
		assertEquals(4, (int) result.getVoxel(1, 1, 6));
		// should keep square region
		assertEquals(15, (int) result.getVoxel(4, 4, 4));
		// should remove single pixel
		assertEquals(0, (int) result.getVoxel(8, 8, 8));
	}

	/**
	 * Creates a 3D image with:
	 * <ul>
	 * <li>three elongated regions with a length of 6 voxels</li> 
	 * <li>one cubic region with a side length of 4 voxels</li> 
	 * <li>one single-voxel region</li>
	 * </ul>
	 *  
	 * @return the resulting test image.
	 */
	private ImageStack createImage()
	{
		ImageStack array = ImageStack.create(10, 10, 10, 8);
		for (int i = 2; i < 8; i++)
		{
			array.setVoxel(i, 1, 1, 3);
			array.setVoxel(1, i, 1, 4);
			array.setVoxel(1, 1, i, 5);
		}
		for (int z = 3; z < 7; z++)
		{
			for (int y = 3; y < 7; y++)
			{
				for (int x = 3; x < 7; x++)
				{
					array.setVoxel(x, y, z, 15);
				}
			}
		}
		array.setVoxel(8, 8, 8, 20);
		
		return array;
	}
}
