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
    private final static int[][] shifts2d = new int[][] {
        {0, -1}, {-1, 0}, {+1, 0}, {0, +1}
    };
    
    private final static int[][] shifts3d = new int[][] {
        {0, 0, -1}, {0, -1, 0}, {-1, 0, 0}, {+1, 0, 0}, {0, +1, 0}, {0, 0, +1}
    };
    
    @Override
    public void run(String arg)
    {
        ImagePlus imagePlus = IJ.getImage();
        
        if (imagePlus.getStackSize() == 1)
        {
            process2d(imagePlus.getProcessor());
        }
        else
        {
            process3d(imagePlus.getStack());
        }
        
        imagePlus.repaintWindow();
    }
    
    private void process2d(ImageProcessor labelImage)
    {
        // identified regions of the background
        ImageProcessor bgLabelMap = LabelImages.regionComponentsLabeling(labelImage, 0, 4, 16);
        
        // for each background region, find labels of regions within original image
        Map<Integer, BackgroundRegion2D> map = mapNeighbors(bgLabelMap, labelImage);
        
        for (int bgLabel : map.keySet())
        {
            IJ.log("Process BG region with label " + bgLabel);
            BackgroundRegion2D region = map.get(bgLabel);
            
            // if the background region is surrounded by only one region, then it can be replaced by this value
            IJ.log("  number of neighbors: " + region.values.size());
            if (region.values.size() == 1)
            {
                IJ.log("  replace with value: " + region.values.get(0));
                int value = region.values.get(0);
                FloodFill.floodFill(bgLabelMap, region.x0, region.y0, labelImage, value, 4);
            }
        }
    }
    
    private Map<Integer, BackgroundRegion2D> mapNeighbors(ImageProcessor keyLabelMap, ImageProcessor valueLabelMap)
    {
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
    
    private void process3d(ImageStack labelImage)
    {
        // identified regions of the background
        ImageStack bgLabelMap = LabelImages.regionComponentsLabeling(labelImage, 0, 6, 16);
        
        // for each background region, find labels of regions within original image
        Map<Integer, BackgroundRegion3D> map = mapNeighbors(bgLabelMap, labelImage);
        
        for (int bgLabel : map.keySet())
        {
            IJ.log("Process BG region with label " + bgLabel);
            BackgroundRegion3D region = map.get(bgLabel);
            
            // if the background region is surrounded by only one region, then it can be replaced by this value
            IJ.log("  number of neighbors: " + region.values.size());
            if (region.values.size() == 1)
            {
                IJ.log("  replace with value: " + region.values.get(0));
                int value = region.values.get(0);
                FloodFill3D.floodFill(bgLabelMap, region.x0, region.y0, region.z0, labelImage, value, 6);
            }
        }
    }
    
    private Map<Integer, BackgroundRegion3D> mapNeighbors(ImageStack keyLabelMap, ImageStack valueLabelMap)
    {
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
