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
package inra.ijpb.morphology;

import static org.junit.Assert.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.strel.SquareStrel;

import org.junit.Test;

public class MorphologyTest {

	@Test
	public void testGetAllLabels() {
		String[] labels = Morphology.Operation.getAllLabels();
		assertNotNull(labels);
		assertTrue(labels.length > 0);
	}

	@Test
	public void testFromLabel() {
		Morphology.Operation op;
			
		op = Morphology.Operation.fromLabel("Closing");
		assertEquals(Morphology.Operation.CLOSING, op);

		op = Morphology.Operation.fromLabel("Gradient");
		assertEquals(Morphology.Operation.GRADIENT, op);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromLabel_Illegal() {
		Strel.Shape.fromLabel("Illegal Strel");
	}

	@Test
	public void testFromLabel_Illegal2() {
		boolean ok = false;
		try {
			Strel.Shape.fromLabel("Illegal Strel");
		} catch (IllegalArgumentException ex) {
			ok = true;
		}
		assertTrue(ok);
	}

	/**
	 * Tests if each operation given in the enumeration can be applied on 
	 * a basic test image.
	 */
	@Test
	public void testApplyOperation() {
		ImageProcessor image = new ByteProcessor(100, 100);
		// initialize image
		for (int y = 40; y < 60; y++) {
			for (int x = 40; x < 60; x++) {
				image.set(x, y, 255);
			}
		}

		Strel strel = SquareStrel.fromDiameter(5);
		
		for (Morphology.Operation op : Morphology.Operation.values()) {
			ImageProcessor result = op.apply(image, strel);
			assertNotNull(result);
		}
	}

	/**
	 * Tests the stability of closing by square when particle is a square.
	 */
	@Test
	public void testClosing_Square() {
		ImageProcessor image = new ByteProcessor(100, 100);
		// initialize image
		for (int y = 40; y < 60; y++) {
			for (int x = 40; x < 60; x++) {
				image.set(x, y, 255);
			}
		}

		Strel strel = SquareStrel.fromDiameter(5);
		ImageProcessor result = Morphology.closing(image, strel);

		for (int y = 40; y < 60; y++) {
			for (int x = 40; x < 60; x++) {
				assertEquals(image.get(x,y), result.get(x, y));
			}
		}
	}

	/**
	 * Tests closing can be run on an RGB image.
	 */
	@Test
	public void testClosing_Square_RGB() {
		String fileName = getClass().getResource("/files/peppers-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ColorProcessor image = (ColorProcessor) imagePlus.getProcessor();
		
		Strel strel = SquareStrel.fromDiameter(5);
		ColorProcessor result = (ColorProcessor) Morphology.closing(image, strel);
		assertNotNull(result);
		
		// Check that result is greater than or equal to the original image
		int width = image.getWidth();
		int height = image.getHeight();
		int[] rgb0 = new int[3];
		int[] rgb = new int[3];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.getPixel(x, y, rgb0);
				result.getPixel(x, y, rgb);
				for (int c = 0; c < 3; c++)
					assertTrue(rgb[c] >= rgb0[c]);
			}
		}
	}

	/**
	 * Tests closing can be run on an RGB image.
	 */
	@Test
	public void testOpening_Square_RGB() {
		String fileName = getClass().getResource("/files/peppers-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ColorProcessor image = (ColorProcessor) imagePlus.getProcessor();
		
		Strel strel = SquareStrel.fromDiameter(5);
		ColorProcessor result = (ColorProcessor) Morphology.opening(image, strel);
		assertNotNull(result);
		
		// Check that result is lower than or equal to the original image
		int width = image.getWidth();
		int height = image.getHeight();
		int[] rgb0 = new int[3];
		int[] rgb = new int[3];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.getPixel(x, y, rgb0);
				result.getPixel(x, y, rgb);
				for (int c = 0; c < 3; c++)
					assertTrue(rgb[c] <= rgb0[c]);
			}
		}
	}

	/**
	 * Tests that most morphological operations can be run on an RGB image.
	 */
	@Test
	public void testVariousOperations_Square_RGB() {
		String fileName = getClass().getResource("/files/peppers-crop.png").getFile();
		ImagePlus imagePlus = IJ.openImage(fileName);
		assertNotNull(imagePlus);
		ColorProcessor image = (ColorProcessor) imagePlus.getProcessor();
		
		Strel strel = SquareStrel.fromDiameter(5);

		assertNotNull(Morphology.erosion(image, strel));
		assertNotNull(Morphology.dilation(image, strel));

		assertNotNull(Morphology.closing(image, strel));
		assertNotNull(Morphology.opening(image, strel));

		assertNotNull(Morphology.gradient(image, strel));
		assertNotNull(Morphology.internalGradient(image, strel));
		assertNotNull(Morphology.externalGradient(image, strel));
		assertNotNull(Morphology.laplacian(image, strel));
		
		assertNotNull(Morphology.blackTopHat(image, strel));
		assertNotNull(Morphology.whiteTopHat(image, strel));
	}

	/**
     * Tests that most morphological operations can be run on a 2D ImagePlus.
     */
    @Test
    public void testVariousOperations_ImagePlus2D_SquareDiam5() {
        String fileName = getClass().getResource("/files/grains.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);
        
        Strel strel = SquareStrel.fromDiameter(5);

        assertNotNull(Morphology.erosion(imagePlus, strel));
        assertNotNull(Morphology.dilation(imagePlus, strel));

        assertNotNull(Morphology.closing(imagePlus, strel));
        assertNotNull(Morphology.opening(imagePlus, strel));

        assertNotNull(Morphology.gradient(imagePlus, strel));
        assertNotNull(Morphology.internalGradient(imagePlus, strel));
        assertNotNull(Morphology.externalGradient(imagePlus, strel));
        assertNotNull(Morphology.laplacian(imagePlus, strel));
        
        assertNotNull(Morphology.blackTopHat(imagePlus, strel));
        assertNotNull(Morphology.whiteTopHat(imagePlus, strel));
    }
    
    /**
     * Tests that most morphological operations can be run on a 2D ImagePlus.
     */
    @Test
    public void testVariousOperations_FloatProcessor_SquareDiam5() {
        String fileName = getClass().getResource("/files/grains.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);
        
        ImageProcessor image = imagePlus.getProcessor().convertToFloat();
        
        Strel strel = SquareStrel.fromDiameter(5);

        assertNotNull(Morphology.erosion(image, strel));
        assertNotNull(Morphology.dilation(image, strel));

        assertNotNull(Morphology.closing(image, strel));
        assertNotNull(Morphology.opening(image, strel));

        assertNotNull(Morphology.gradient(image, strel));
        assertNotNull(Morphology.internalGradient(image, strel));
        assertNotNull(Morphology.externalGradient(image, strel));
        assertNotNull(Morphology.laplacian(image, strel));
        
        assertNotNull(Morphology.blackTopHat(image, strel));
        assertNotNull(Morphology.whiteTopHat(image, strel));
    }
    
    /**
     * Tests that most morphological operations can be run on a 2D ImagePlus.
     */
    @Test
    public void testVariousOperations_ImageStack_CubeDiam3() {
        ImageStack image = ImageStack.create(20, 20, 20, 8);
        for (int z = 5; z < 15; z++)
        {
            for (int y = 5; y < 15; y++)
            {
                for (int x = 5; x < 15; x++)
                {
                    image.setVoxel(x, y, z, 255);
                }
            }
        }
        
        Strel strel = SquareStrel.fromDiameter(5);

        assertNotNull(Morphology.erosion(image, strel));
        assertNotNull(Morphology.dilation(image, strel));

        assertNotNull(Morphology.closing(image, strel));
        assertNotNull(Morphology.opening(image, strel));

        assertNotNull(Morphology.gradient(image, strel));
        assertNotNull(Morphology.internalGradient(image, strel));
        assertNotNull(Morphology.externalGradient(image, strel));
        assertNotNull(Morphology.laplacian(image, strel));
        
        assertNotNull(Morphology.blackTopHat(image, strel));
        assertNotNull(Morphology.whiteTopHat(image, strel));
    }
    
    /**
     * Tests that most morphological operations can be run on a 2D ImagePlus.
     */
    @Test
    public void testVariousOperations_ImagePlus3D_CubeDiam3() {
        ImageStack image = ImageStack.create(20, 20, 20, 8);
        for (int z = 5; z < 15; z++)
        {
            for (int y = 5; y < 15; y++)
            {
                for (int x = 5; x < 15; x++)
                {
                    image.setVoxel(x, y, z, 255);
                }
            }
        }
        ImagePlus imagePlus = new ImagePlus("image", image);
        
        Strel strel = SquareStrel.fromDiameter(5);

        assertNotNull(Morphology.erosion(imagePlus, strel));
        assertNotNull(Morphology.dilation(imagePlus, strel));

        assertNotNull(Morphology.closing(imagePlus, strel));
        assertNotNull(Morphology.opening(imagePlus, strel));

        assertNotNull(Morphology.gradient(imagePlus, strel));
        assertNotNull(Morphology.internalGradient(imagePlus, strel));
        assertNotNull(Morphology.externalGradient(imagePlus, strel));
        assertNotNull(Morphology.laplacian(imagePlus, strel));
        
        assertNotNull(Morphology.blackTopHat(imagePlus, strel));
        assertNotNull(Morphology.whiteTopHat(imagePlus, strel));
    }
}
