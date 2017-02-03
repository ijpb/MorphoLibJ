/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
package inra.ijpb.plugins;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inra.ijpb.data.border.ConstantBorder;

import org.junit.Test;

/**
 * @author dlegland
 *
 */
public class ExtendBordersPluginTest
{
	/**
	 * Test method for {@link inra.ijpb.plugins.ExtendBordersPlugin#process(ij.process.ImageProcessor, int, int, int, int, inra.ijpb.data.border.BorderManager)}.
	 */
	@Test
	public void testProcess2D()
	{
		ImagePlus inputPlus = IJ.openImage(getClass().getResource( "/files/grains.tif" ).getFile() );
		ImageProcessor input = inputPlus.getProcessor();
		
		ImageProcessor result = ExtendBordersPlugin.process(input, 5, 10, 15, 20, new ConstantBorder(input, 0));
		
		int exp = input.getWidth() + 15;
		assertEquals(exp, result.getWidth());
		exp = input.getHeight() + 35;
		assertEquals(exp, result.getHeight());
	}

}
