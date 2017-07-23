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

import static org.junit.Assert.assertEquals;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.watershed.Watershed;
import java.text.* ;
import java.util.* ;

import org.junit.Test;

public class TestWatershed2D {

	long xorshift_state = 1;

	/**
	 * Random number generator to generate random mask.
	 */
	long xorshift()
	{
		long x = xorshift_state;
		x ^= (x << 21);
		x ^= (x >>> 35);
		x ^= (x << 4);
		xorshift_state = x;
		return x;
	}
	
	/**
	 * Test the morphological segmentation pipeline over the same image stored as 8, 16 and 32-bit.
	 */
	@Test
	public void testWatershed()
	{
		ImagePlus input = IJ.openImage( TestWatershed2D.class.getResource( "/files/grains.tif" ).getFile() );

		final ImagePlus copy = input.duplicate();
		
		int[] connectivityValues = new int[]{ 4, 8 };

		for( int connectivity : connectivityValues )
		{
			input = copy;

			ImageProcessor inputIP = input.getChannelProcessor();

			ImageProcessor maskIP = new ByteProcessor( inputIP.getWidth(), inputIP.getHeight() );
			for (int y = 0; y < input.getHeight(); y++) {
				for (int x = 0; x < input.getWidth(); x++)
					maskIP.setf(x, y, xorshift() % 2);
			}

			ImageProcessor resultIP = Watershed.computeWatershed( inputIP, maskIP, connectivity );
						
			ImagePlus result = new ImagePlus("result", resultIP);

			ImagePlus reference = IJ.openImage( TestWatershed2D.class.getResource(
				"/files/grains_watershed_" + connectivity + ".tif" ).getFile() );
			
			assertEquals( "Different results for resulting images (connectivity = " + connectivity + ")",
				0, diffImagePlus( result, reference ) );
		}
	}

	private int diffImagePlus(final ImagePlus a, final ImagePlus b) {
		final int[] dimsA = a.getDimensions(), dimsB = b.getDimensions();
		if (dimsA.length != dimsB.length) return dimsA.length - dimsB.length;
		for (int i = 0; i < dimsA.length; i++) {
			if (dimsA[i] != dimsB[i]) return dimsA[i] - dimsB[i];
		}
		int count = 0;
		final ImageStack stackA = a.getStack(), stackB = b.getStack();
		for (int slice = 1; slice <= stackA.getSize(); slice++) {
			count += diff( stackA.getProcessor( slice ), stackB.getProcessor( slice ) );
		}
		return count;
	}

	private int diff(final ImageProcessor a, final ImageProcessor b) {
		int count = 0;
		final int width = a.getWidth(), height = a.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (a.getf(x, y) != b.getf(x, y)) count++;
			}
		}
		return count;
	}
		
	/**
	 * Main method to test and debug the watershed
	 *  
	 * @param args
	 */
	public static void main( final String[] args )
	{
		ImageJ.main( args );
		
		IJ.open( TestWatershed2D.class.getResource( "/files/grains.tif" ).getFile() );
		
		new Watershed3DPlugin().run( null );
	}

}
