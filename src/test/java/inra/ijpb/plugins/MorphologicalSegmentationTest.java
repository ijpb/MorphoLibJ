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
package inra.ijpb.plugins;

import static org.junit.Assert.assertEquals;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.watershed.Watershed;

import org.junit.Test;
/**
 * Class to test the Morphological Segmentation plugin
 * @author iarganda
 *
 */
public class MorphologicalSegmentationTest {
	
	
	/**
	 * Test the morphological segmentation pipeline over the same image stored as 8, 16 and 32-bit.
	 */
	@Test
	public void testSegmentationDifferentImageTypes()
	{
		ImagePlus input = IJ.openImage( MorphologicalSegmentationTest.class.getResource( "/files/grains.tif" ).getFile() );
		int dynamic = 10;
		int connectivity = 6;
		int gradientRadius = 1;
		
		final ImagePlus copy = input.duplicate();
		
		boolean[] values = new boolean[]{ true, false };

		for( boolean calculateDams : values )
		{
			input = copy;
			
			ImagePlus result8bit = segmentImage( input, dynamic, connectivity, gradientRadius, calculateDams );
			//IJ.run( input, "16-bit", "" );
			ImageConverter ic = new ImageConverter( input );
			ic.convertToGray16();
			ImagePlus result16bit = segmentImage( input, dynamic, connectivity, gradientRadius, calculateDams );		
			assertEquals( "Different results for 8 and 16 bit images (dams = " + calculateDams + ")", 0, diffImagePlus( result8bit, result16bit ) );
			//IJ.run( input.duplicate(), "32-bit", "" );
			ic = new ImageConverter( input.duplicate() );
			ic.convertToGray32();
			ImagePlus result32bit = segmentImage( input, dynamic, connectivity, gradientRadius, calculateDams );
			assertEquals( "Different results for 8 and 32 bit images (dams = " + calculateDams + ")", 0, diffImagePlus( result8bit, result32bit ) );
		}
	}
	
	ImagePlus segmentImage( 
			ImagePlus input, 
			int dynamic, 
			int connectivity,
			int gradientRadius,
			boolean calculateDams )
	{
		Strel3D strel = Strel3D.Shape.CUBE.fromRadius( gradientRadius );
		ImageStack image = Morphology.gradient( input.getImageStack(), strel );
		ImageStack regionalMinima = MinimaAndMaxima3D.extendedMinima( image, dynamic, connectivity );
		ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima( image, regionalMinima, connectivity );
		ImageStack labeledMinima = BinaryImages.componentsLabeling( regionalMinima, connectivity, 32 );
		ImageStack resultStack = Watershed.computeWatershed( imposedMinima, labeledMinima, 
				connectivity, calculateDams );
		ImagePlus resultImage = new ImagePlus( "watershed", resultStack );
		resultImage.setCalibration( input.getCalibration() );
		return resultImage;
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
	 * Main method to test and debug the Morphological
	 * Segmentation GUI
	 *  
	 * @param args
	 */
	public static void main( final String[] args )
	{
		ImageJ.main( args );
		
		IJ.open( MorphologicalSegmentationTest.class.getResource( "/files/grains.tif" ).getFile() );
		
		new MorphologicalSegmentation().run( null );
	}

}
