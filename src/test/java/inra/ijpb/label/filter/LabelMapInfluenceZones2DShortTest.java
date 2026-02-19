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
package inra.ijpb.label.filter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * 
 */
public class LabelMapInfluenceZones2DShortTest
{

	/**
	 * Test method for {@link inra.ijpb.label.filter.LabelMapInfluenceZones2DShort#process(ij.process.ImageProcessor)}.
	 */
	@Test
	public final void testProcess_fourRegions()
	{
		ImageProcessor array = new ByteProcessor(16, 16);
		int[] labels = new int[] {3, 4, 7, 9};
		array.set(7, 7, labels[0]);
		array.set(8, 7, labels[1]);
		array.set(7, 8, labels[2]);
		array.set(8, 8, labels[3]);
		
		ImageProcessor res = new LabelMapInfluenceZones2DShort().process(array);
		
		assertEquals(labels[0], res.get( 0,  0));
		assertEquals(labels[1], res.get(15,  0));
		assertEquals(labels[2], res.get( 0, 15));
		assertEquals(labels[3], res.get(15, 15));
	}

}
