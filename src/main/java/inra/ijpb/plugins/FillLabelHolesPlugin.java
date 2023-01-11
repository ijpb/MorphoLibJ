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
/**
 * 
 */
package inra.ijpb.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.morphology.FloodFill;
import inra.ijpb.morphology.FloodFill3D;

/**
 * Fills the holes within the region of a label image.
 * 
 * @author dlegland
 */
public class FillLabelHolesPlugin implements PlugIn
{
    // Widget labels and corresponding values of output type option
    private final static String[] resultBitDepthLabels = {"8 bits", "16 bits", "32 bits (float)"};
    private final static int[] resultBitDepthList = {8, 16, 32};
    
    /**
     * The shifts to identify the 2D neighbors of a pixel, using the 4-connectivity.
     */
    private final static int[][] shifts2d = new int[][] { { 0, -1 }, { -1, 0 },
            { +1, 0 }, { 0, +1 } };
    
    /**
     * The shifts to identify the 3D neighbors of a voxel, using the 6-connectivity.
     */
    private final static int[][] shifts3d = new int[][] { 
        { 0, 0, -1 },
        { 0, -1, 0 }, { -1, 0, 0 }, { +1, 0, 0 }, { 0, +1, 0 },
        { 0, 0, +1 } };
        
    @Override
    public void run(String arg)
    {
        // retrieve current image
        ImagePlus imagePlus = IJ.getImage();
        
        boolean isPlanar = imagePlus.getStackSize() == 1;
        
        // Display dialog options
        GenericDialog gd = new GenericDialog("Fill Label Holes");
        String[] connLabels = isPlanar ? Connectivity2D.getAllLabels() : Connectivity3D.getAllLabels();
        gd.addChoice("Background Connectivity", connLabels, connLabels[0]);
        gd.addChoice("Labeling Bit-Depth", resultBitDepthLabels, resultBitDepthLabels[1]);
        
        // wait for user answer
        gd.showDialog();
        if (gd.wasCanceled()) 
            return;

        // parses dialog options
        int conn = parseConnectivityValue(isPlanar ? 2 : 3, gd.getNextChoice());
        int bitDepth = resultBitDepthList[gd.getNextChoiceIndex()];
        
        // dispatch processing according to image dimensionality
        if (imagePlus.getStackSize() == 1)
        {
            process2d(imagePlus.getProcessor(), conn, bitDepth);
        }
        else
        {
            process3d(imagePlus.getStack(), conn, bitDepth);
        }
        
        // refresh display
        imagePlus.updateAndDraw();
    }
    
    private static final int parseConnectivityValue(int nd, String string)
    {
        if (nd == 2) return Connectivity2D.fromLabel(string).getValue();
        if (nd == 3) return Connectivity3D.fromLabel(string).getValue();
        throw new RuntimeException("Requires dimensionality equal to 2 or 3, not " + nd);
    }
    
    private void process2d(ImageProcessor labelImage, int conn, int bitDepth)
    {
        // identified regions of the background
        ImageProcessor bgLabelMap = LabelImages.regionComponentsLabeling(labelImage, 0, conn, bitDepth);
        
        // for each background region, find labels of regions within original image
        Map<Integer, BackgroundRegion2D> map = mapNeighbors(bgLabelMap, labelImage);
        
        for (BackgroundRegion2D region : map.values())
        {
            // if the background region is surrounded by only one region, then
            // the corresponding pixels in the original label map can be
            // replaced by the value of the surrounding region
            if (region.values.size() == 1)
            {
                int value = region.values.get(0);
                FloodFill.floodFill(bgLabelMap, region.x0, region.y0, labelImage, value, conn);
            }
        }
    }
    
    /**
     * Associates to each 2D region of the first input, the list of labels that
     * correspond to the neighbors of the 2D region within the second input.
     * 
     * @param keyLabelMap
     *            the label map of the region to process
     * @param valueLabelMap
     *            the label map of the regions used to identify neighbors
     * @return the bipartite map of region labels
     */
    private Map<Integer, BackgroundRegion2D> mapNeighbors(
            ImageProcessor keyLabelMap, ImageProcessor valueLabelMap)
    {
        // retrieve image size
        int sizeX = keyLabelMap.getWidth();
        int sizeY = keyLabelMap.getHeight();
        
        // initialize map
        Map<Integer, BackgroundRegion2D> map = new HashMap<Integer, BackgroundRegion2D>();
        
        // iterate over pixels within key image
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                // retrieve current region
                int keyLabel = (int) keyLabelMap.getf(x, y);
                if (keyLabel == 0)
                {
                    continue;
                }
                
                // retrieve list of neighbor labels in original image
                BackgroundRegion2D bgRegion = map.get(keyLabel);
                if (bgRegion == null)
                {
                    bgRegion = new BackgroundRegion2D(x, y);
                }
                
                // iterate over neighbors
                for (int[] shift : shifts2d)
                {
                    int x2 = x + shift[0];
                    int y2 = y + shift[1];
                    if (x2 < 0 || x2 >= sizeX) continue;
                    if (y2 < 0 || y2 >= sizeY) continue;
                    
                    int value = (int) valueLabelMap.getf(x2, y2);
                    if (value != 0 && !bgRegion.values.contains(value))
                    {
                        bgRegion.values.add(value);
                    }
                }
                
                // update map
                map.put(keyLabel, bgRegion);
            }
        }
        
        return map;
    }
    
    private void process3d(ImageStack labelImage, int conn, int bitDepth)
    {
        // identified regions of the background
        ImageStack bgLabelMap = LabelImages.regionComponentsLabeling(labelImage, 0, conn, bitDepth);
        
        // for each background region, find labels of regions within original image
        Map<Integer, BackgroundRegion3D> map = mapNeighbors(bgLabelMap, labelImage);
        
        // iterate over background regions
        for (BackgroundRegion3D region : map.values())
        {
            // if the background region is surrounded by only one region, then
            // the corresponding voxels in the original label map can be
            // replaced by the value of the surrounding region
            if (region.values.size() == 1)
            {
                int value = region.values.get(0);
                FloodFill3D.floodFill(bgLabelMap, region.x0, region.y0, region.z0, labelImage, value, conn);
            }
        }
    }
    
    /**
     * Associates to each 3D region of the first input, the list of labels that
     * correspond to the neighbors of the 3D region within the second input.
     * 
     * @param keyLabelMap
     *            the label map of the region to process
     * @param valueLabelMap
     *            the label map of the regions used to identify neighbors
     * @return the bipartite map of region labels
     */
    private Map<Integer, BackgroundRegion3D> mapNeighbors(
            ImageStack keyLabelMap, ImageStack valueLabelMap)
    {
        // retrieve image size
        int sizeX = keyLabelMap.getWidth();
        int sizeY = keyLabelMap.getHeight();
        int sizeZ = keyLabelMap.getSize();
        
        // initialize map
        Map<Integer, BackgroundRegion3D> map = new HashMap<Integer, BackgroundRegion3D>();
        
        // iterate over pixels within key image
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    // retrieve current region
                    int keyLabel = (int) keyLabelMap.getVoxel(x, y, z);
                    if (keyLabel == 0)
                    {
                        continue;
                    }
                    
                    // retrieve list of neighbor labels in original image
                    BackgroundRegion3D bgRegion = map.get(keyLabel);
                    if (bgRegion == null)
                    {
                        bgRegion = new BackgroundRegion3D(x, y, z);
                    }
                    
                    // iterate over neighbors
                    for (int[] shift : shifts3d)
                    {
                        int x2 = x + shift[0];
                        int y2 = y + shift[1];
                        int z2 = z + shift[2];
                        if (x2 < 0 || x2 >= sizeX) continue;
                        if (y2 < 0 || y2 >= sizeY) continue;
                        if (z2 < 0 || z2 >= sizeZ) continue;
                        
                        int value = (int) valueLabelMap.getVoxel(x2, y2, z2);
                        if (value != 0 && !bgRegion.values.contains(value))
                        {
                            bgRegion.values.add(value);
                        }
                    }
                    
                    // update map
                    map.put(keyLabel, bgRegion);
                }
            }
        }
        
        return map;
    }
    
    
    class BackgroundRegion2D
    {
        ArrayList<Integer> values = new ArrayList<Integer>(4);
        int x0;
        int y0;
        
        public BackgroundRegion2D(int x0, int y0)
        {
            this.x0 = x0;
            this.y0 = y0;
        }
    }
    
    class BackgroundRegion3D
    {
        ArrayList<Integer> values = new ArrayList<Integer>(4);
        int x0;
        int y0;
        int z0;
        
        public BackgroundRegion3D(int x0, int y0, int z0)
        {
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
        }
    }
}
