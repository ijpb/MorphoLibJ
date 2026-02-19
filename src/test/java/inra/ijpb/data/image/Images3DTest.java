/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
package inra.ijpb.data.image;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;

/**
 * @author dlegland
 *
 */
public class Images3DTest
{

	/**
	 * Test method for {@link inra.ijpb.data.image.Images3D#fill(ij.ImageStack, double)}.
	 */
	@Test
	public final void testFill()
	{
		ImageStack image = ImageStack.create(10, 10, 10, 8);
		Images3D.fill(image, 120.0);
		
		assertEquals(120, (int) image.getVoxel(0, 0, 0));
		assertEquals(120, (int) image.getVoxel(5, 5, 5));
		assertEquals(120, (int) image.getVoxel(9, 9, 9));
	}
	
	/**
	 * Test method for {@link inra.ijpb.data.image.Images3D#invert(ij.ImageStack)}.
	 */
	@Test
	public final void testInvert()
	{
		ImageStack image = ImageStack.create(10, 10, 10, 8);
		image.setVoxel(5, 5, 5, 100);
		image.setVoxel(6, 6, 6, 255);
		assertEquals(0, (int) image.getVoxel(0, 0, 0));
		
		Images3D.invert(image);
		
		assertEquals(255, (int) image.getVoxel(0, 0, 0));
		assertEquals(255, (int) image.getVoxel(9, 9, 9));
		assertEquals(155, (int) image.getVoxel(5, 5, 5));
		assertEquals(  0, (int) image.getVoxel(6, 6, 6));
	}

}
