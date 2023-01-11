/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
package inra.ijpb.morphology.geodrec;

import static org.junit.Assert.assertNotNull;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.strel.CubeStrel;

public class GeodesicReconstructionByDilation3DScanningTiming
{
    public final void compareTimingC6()
    {
        String fileName = getClass().getResource("/files/bat-cochlea_sub25.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);
        ImageStack mask = imagePlus.getStack();

        // Ensure regularity of the mask
        mask = Morphology.opening(mask, CubeStrel.fromRadius(1));

        // create marker image
        int width = mask.getWidth();
        int height = mask.getHeight();
        int depth = mask.getSize();
        int bitDepth = mask.getBitDepth();
        ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
        marker.setVoxel(20, 80, 50, 255);

        long t0, t1;
        double dt;

        GeodesicReconstructionByDilation3DScanning algo1 = new GeodesicReconstructionByDilation3DScanning();
        algo1.setConnectivity(6);
        algo1.verbose = false;

        t0 = System.currentTimeMillis();
        algo1.applyTo(marker, mask);
        t1 = System.currentTimeMillis();

        dt = (t1 - t0) / 1000.0;
        System.out.println("Algo scanning C6 int: " + dt + " s");

        GeodesicReconstructionByDilation3DScanningGray8 algo2 = new GeodesicReconstructionByDilation3DScanningGray8();
        algo2.setConnectivity(6);
        algo2.verbose = false;

        t0 = System.currentTimeMillis();
        algo2.applyTo(marker, mask);
        t1 = System.currentTimeMillis();

        dt = (t1 - t0) / 1000.0;
        System.out.println("Algo scanning C6 gray8: " + dt + " s");

        marker = marker.convertToFloat();
        mask = mask.convertToFloat();

        t0 = System.currentTimeMillis();
        algo1.applyTo(marker, mask);
        t1 = System.currentTimeMillis();

        dt = (t1 - t0) / 1000.0;
        System.out.println("Algo scanning C6 double: " + dt + " s");
    }

    public final void compareTimingC26()
    {
        String fileName = getClass().getResource("/files/bat-cochlea_sub25.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        assertNotNull(imagePlus);
        ImageStack mask = imagePlus.getStack();

        // Ensure regularity of the mask
        mask = Morphology.opening(mask, CubeStrel.fromRadius(1));

        // create marker image
        int width = mask.getWidth();
        int height = mask.getHeight();
        int depth = mask.getSize();
        int bitDepth = mask.getBitDepth();
        ImageStack marker = ImageStack.create(width, height, depth, bitDepth);
        marker.setVoxel(20, 80, 50, 255);

        GeodesicReconstructionByDilation3DScanning algo1 = new GeodesicReconstructionByDilation3DScanning();
        algo1.setConnectivity(26);
        algo1.verbose = false;

        long t0 = System.currentTimeMillis();
        algo1.applyTo(marker, mask);
        long t1 = System.currentTimeMillis();

        double dt = (t1 - t0) / 1000.0;
        System.out.println("Algo scanning C26 int: " + dt + " s");

        GeodesicReconstruction3DAlgo algo2 = new GeodesicReconstructionByDilation3DScanningGray8();
        algo2.setConnectivity(26);
        // algo2.verbose = false;

        t0 = System.currentTimeMillis();
        algo2.applyTo(marker, mask);
        t1 = System.currentTimeMillis();

        dt = (t1 - t0) / 1000.0;
        System.out.println("Algo scanning C26 gray8: " + dt + " s");

        marker = marker.convertToFloat();
        mask = mask.convertToFloat();

        t0 = System.currentTimeMillis();
        algo1.applyTo(marker, mask);
        t1 = System.currentTimeMillis();

        dt = (t1 - t0) / 1000.0;
        System.out.println("Algo scanning C26 double: " + dt + " s");
    }
    
    public static final void main(String... args)
    {
        GeodesicReconstructionByDilation3DScanningTiming algo = new GeodesicReconstructionByDilation3DScanningTiming();
        
        algo.compareTimingC6();
        algo.compareTimingC26();
    }
}
