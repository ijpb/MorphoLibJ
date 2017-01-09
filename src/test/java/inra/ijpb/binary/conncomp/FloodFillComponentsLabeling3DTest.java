package inra.ijpb.binary.conncomp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.ImageStack;

public class FloodFillComponentsLabeling3DTest
{
	/**
	 * Default settings are 4 connectivity, 16 bits image.
	 */
	@Test
	public void testFloodFillComponentsLabeling_Default()
	{
		ImageStack image = createNineCubesImage();
		
		FloodFillComponentsLabeling3D algo = new FloodFillComponentsLabeling3D();
		ImageStack result = algo.computeLabels(image);
		
		assertEquals(16, result.getBitDepth());
		assertEquals(9, result.getVoxel(7, 7, 7), .1);
	}

	/**
	 * Using 6 connectivity should result in nine connected components.
	 */
	@Test
	public void testFloodFillComponentsLabeling_C6_Byte()
	{
		ImageStack image = createNineCubesImage();
		
		FloodFillComponentsLabeling3D algo = new FloodFillComponentsLabeling3D(6, 8);
		ImageStack result = algo.computeLabels(image);
		
		assertEquals(8, result.getBitDepth());
		assertEquals(9, result.getVoxel(7, 7, 7), .1);
	}


	/**
	 * Using 26 connectivity should result in one connected component.
	 */
	@Test
	public void testFloodFillComponentsLabeling_C26_Short()
	{
		ImageStack image = createNineCubesImage();
		
		FloodFillComponentsLabeling3D algo = new FloodFillComponentsLabeling3D(26, 16);
		ImageStack result = algo.computeLabels(image);
		
		assertEquals(16, result.getBitDepth());
		assertEquals(1, result.getVoxel(7, 7, 7), .1);
	}
	

	/**
	 * Create a 10-by-10-by-10 byte stack containing nine squares touching by
	 * corners.
	 * 
	 * Expected number of connected components is nine for 6 (and 18)
	 * connectivity, and one for 26 connectivity.
	 * 
	 * @return an image containing nine cubes touching by corners
	 */
	private final static ImageStack createNineCubesImage()
	{
		ImageStack image = ImageStack.create(10,  10,  10, 8);
		for (int z = 0; z < 2; z++)
		{
			for (int y = 0; y < 2; y++)
			{
				for (int x = 0; x < 2; x++)
				{
					image.setVoxel(x + 2, y + 2, z + 2, 255);
					image.setVoxel(x + 2, y + 6, z + 2, 255);
					image.setVoxel(x + 6, y + 2, z + 2, 255);
					image.setVoxel(x + 6, y + 6, z + 2, 255);
					image.setVoxel(x + 4, y + 4, z + 4, 255);
					image.setVoxel(x + 2, y + 2, z + 6, 255);
					image.setVoxel(x + 2, y + 6, z + 6, 255);
					image.setVoxel(x + 6, y + 2, z + 6, 255);
					image.setVoxel(x + 6, y + 6, z + 6, 255);
				}
			}
		}
		return image;
	}
}
