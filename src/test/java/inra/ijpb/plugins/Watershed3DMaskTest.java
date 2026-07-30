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
 * #L%
 */
package inra.ijpb.plugins;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import inra.ijpb.watershed.Watershed;

public class Watershed3DMaskTest {

    @Test
    public void testWatershedRespectsMask3D() {
        final int w = 5, h = 5, d = 3;

        ImageStack inputStack = new ImageStack(w, h);
        ImageStack maskStack = new ImageStack(w, h);

        for (int z = 0; z < d; z++) {
            FloatProcessor fp = new FloatProcessor(w, h);
            ByteProcessor mp = new ByteProcessor(w, h);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    // simple gradient
                    fp.setf(x, y, (float) (z * 10 + x + y));
                    // mask center 3x3 is 1, border is 0
                    if (x >= 1 && x <= 3 && y >= 1 && y <= 3)
                        mp.setf(x, y, 1);
                    else
                        mp.setf(x, y, 0);
                }
            }
            inputStack.addSlice(fp);
            maskStack.addSlice(mp);
        }

        ImagePlus input = new ImagePlus("input", inputStack);
        ImagePlus mask = new ImagePlus("mask", maskStack);

        ImagePlus ws = Watershed.computeWatershed(input, mask, 6);
        // verify no labels outside mask
        ImageStack out = ws.getImageStack();
        for (int z = 0; z < d; z++) {
            ImageProcessor outIp = out.getProcessor(z+1);
            ImageProcessor maskIp = maskStack.getProcessor(z+1);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (maskIp.getf(x, y) == 0) {
                        assertEquals("Label outside mask at ("+x+","+y+","+z+")", 0f, outIp.getf(x, y), 0f);
                    }
                }
            }
        }
    }
}
