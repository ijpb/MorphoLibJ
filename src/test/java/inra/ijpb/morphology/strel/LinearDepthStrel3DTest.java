/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
/**
 * 
 */
package inra.ijpb.morphology.strel;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * @author dlegland
 *
 */
public class LinearDepthStrel3DTest {

	/**
	 * Test method for {@link inra.ijpb.morphology.strel.LinearDepthStrel3D#inPlaceDilation(ij.ImageStack)}.
	 */
	@Test
	public void testDilation() 
	{
		ImageStack image = createIsolatedVoxelImage();
		
		LinearDepthStrel3D strel = LinearDepthStrel3D.fromDiameter(7);
		ImageStack result = strel.dilation(image);
		
		assertEquals(255, result.getVoxel(5, 5, 5), .01);
		assertEquals(255, result.getVoxel(5, 5, 2), .01);
		assertEquals(255, result.getVoxel(5, 5, 8), .01);
		assertEquals(  0, result.getVoxel(5, 5, 1), .01);
		assertEquals(  0, result.getVoxel(5, 5, 9), .01);
	}

	private static final ImageStack createIsolatedVoxelImage()
	{
		ImageStack image = ImageStack.create(10, 10, 10, 8);
		image.setVoxel(5, 5, 5, 255);
		return image;
	}
}
