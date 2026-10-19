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
package inra.ijpb.plugins;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.distmap.ChamferMasks2D;
import inra.ijpb.binary.distmap.ChamferMasks3D;

/**
 * Tests the static distance transform watershed methods (2D and 3D).
 */
public class DistanceTransformWatershedTest
{
	/** Two overlapping discs */
	private static ByteProcessor createTwoDiscs()
	{
		ByteProcessor ip = new ByteProcessor( 60, 30 );
		int[] cx = { 20, 38 };
		for ( int y = 0; y < 30; y++ )
			for ( int x = 0; x < 60; x++ )
				for ( int c : cx )
					if ( ( x - c ) * ( x - c ) + ( y - 15 ) * ( y - 15 ) <= 144 )
						ip.set( x, y, 255 );
		return ip;
	}

	/** Two overlapping balls */
	private static ImageStack createTwoBalls()
	{
		ImageStack stack = ImageStack.create( 50, 30, 30, 8 );
		int[] cx = { 15, 33 };
		for ( int z = 0; z < 30; z++ )
		{
			ImageProcessor ip = stack.getProcessor( z + 1 );
			for ( int y = 0; y < 30; y++ )
				for ( int x = 0; x < 50; x++ )
					for ( int c : cx )
						if ( ( x - c ) * ( x - c ) + ( y - 15 ) * ( y - 15 )
								+ ( z - 15 ) * ( z - 15 ) <= 121 )
							ip.set( x, y, 255 );
		}
		return stack;
	}

	private static int countLabels2D( ImageProcessor ip )
	{
		Set<Integer> labels = new HashSet<Integer>();
		for ( int i = 0; i < ip.getPixelCount(); i++ )
			if ( ip.get( i ) != 0 )
				labels.add( ip.get( i ) );
		return labels.size();
	}

	private static int countLabels3D( ImageStack stack )
	{
		Set<Integer> labels = new HashSet<Integer>();
		for ( int z = 1; z <= stack.getSize(); z++ )
		{
			ImageProcessor ip = stack.getProcessor( z );
			for ( int i = 0; i < ip.getPixelCount(); i++ )
				if ( ip.get( i ) != 0 )
					labels.add( ip.get( i ) );
		}
		return labels.size();
	}

	@Test
	public void testDistanceTransformWatershed2D()
	{
		ByteProcessor image = createTwoDiscs();
		ChamferMask2D mask = ChamferMasks2D.CHESSKNIGHT.getMask();

		ImageProcessor flt = DistanceTransformWatershed
				.distanceTransformWatershedFloat( image, mask, true, 1, 4 );
		assertEquals( 32, flt.getBitDepth() );
		assertEquals( 2, countLabels2D( flt ) );

		ImageProcessor sht = DistanceTransformWatershed
				.distanceTransformWatershedShort( image, mask, true, 1, 4 );
		assertEquals( 16, sht.getBitDepth() );
		assertEquals( 2, countLabels2D( sht ) );

		// background preserved
		assertEquals( 0, flt.get( 0, 0 ) );
		assertEquals( 0, sht.get( 0, 0 ) );
	}

	@Test
	public void testDistanceTransformWatershed2DImagePlus()
	{
		ImagePlus imp = new ImagePlus( "discs", createTwoDiscs() );
		ImagePlus res = DistanceTransformWatershed.distanceTransformWatershed(
				imp, ChamferMasks2D.CHESSKNIGHT.getMask(), false, true, 1, 8 );
		assertEquals( 2, countLabels2D( res.getProcessor() ) );
		assertEquals( 16, res.getBitDepth() );
	}

	@Test
	public void testDistanceTransformWatershed3D()
	{
		ImageStack image = createTwoBalls();
		ChamferMask3D mask = ChamferMasks3D.BORGEFORS.getMask();

		ImageStack flt = DistanceTransformWatershed3D
				.distanceTransformWatershedFloat( image, mask, true, 2, 6 );
		assertEquals( 32, flt.getBitDepth() );
		assertEquals( 2, countLabels3D( flt ) );

		ImageStack sht = DistanceTransformWatershed3D
				.distanceTransformWatershedShort( image, mask, true, 2, 6 );
		assertEquals( 16, sht.getBitDepth() );
		assertEquals( 2, countLabels3D( sht ) );

		assertEquals( 0, sht.getVoxel( 0, 0, 0 ), 0 );
	}

	@Test
	public void testDistanceTransformWatershed3DImagePlus()
	{
		ImagePlus imp = new ImagePlus( "balls", createTwoBalls() );
		ImagePlus res = DistanceTransformWatershed3D.distanceTransformWatershed(
				imp, ChamferMasks3D.BORGEFORS.getMask(), false, true, 2, 26 );
		assertEquals( 16, res.getBitDepth() );
		assertEquals( 2, countLabels3D( res.getImageStack() ) );
	}
}
