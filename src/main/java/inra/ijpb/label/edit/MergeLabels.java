/**
 * 
 */
package inra.ijpb.label.edit;

import java.util.ArrayList;
import java.util.HashSet;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.label.LabelImages;

/**
 * Merge several regions identified by their label, filling the gap between
 * former regions. Labels are merged in place (i.e., the input image is
 * modified). The background pixels/voxels are merged only if they are neighbor
 * of at least two regions to be merged, and of no other region.
 */
public class MergeLabels extends AlgoStub
{
	/**
     * Merge several regions identified by their label, filling the gap between
     * former regions. Labels are merged in place (i.e., the input image is
     * modified). The background pixels are merged only if they are neighbor of
     * at least two regions to be merged, and of no other region.
	 * 
	 * @param imagePlus
	 *            an ImagePlus containing a 3D label image
	 * @param labels
	 *            the list of labels to replace
	 * @param newLabel
	 *            the new value for labels
	 * @param conn
	 *            the connectivity to check neighbors of background pixels
	 */
	public void process(ImagePlus imagePlus, float[] labels, float newLabel, int conn) 
	{
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			process(image, labels, newLabel, conn);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			process(image, labels, newLabel, conn);
		}
	}
	
	/**
	 * Merge several regions identified by their label, filling the gap between
	 * former regions. Labels are merged in place (i.e., the input image is
	 * modified). The background pixels are merged only if they are neighbor of
	 * at least two regions to be merged, and of no other region.
	 * 
	 * 
	 * @param image
	 *            a label planar image
	 * @param labels
	 *            the list of labels to replace
	 * @param newLabel
	 *            the new value for labels
	 * @param conn
	 *            the connectivity to check neighbors of background pixels
	 */
	public void process(ImageProcessor image, float[] labels, float newLabel, int conn)
	{
        // get image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        
        // convert array to tree (to accelerate search)
        HashSet<Float> labelSet = new HashSet<Float>();
        for (int i = 0; i < labels.length; i++)
        {
            labelSet.add(labels[i]);
        }
        
        // determines the shift to compute neighbor coordinates
        int[][] shifts;
        if (conn == 4)
        {
            shifts = new int[][] {{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        }
        else if (conn == 8)
        {
            shifts = new int[][] { 
                { -1, -1 }, { 0, -1 }, { 1, -1 },   // previous line
                { -1, 0 }, { 1, 0 },                // current line
                { -1, 1 }, { 0, 1 }, { 1, 1 } };    // next line
        }
        else
        {
            throw new IllegalArgumentException("Connectivity value should be either 4 or 8.");
        }
        
        // create structure to store the boundary pixels
        ArrayList<int[]> boundaryPixels = new ArrayList<int[]>();
        
        // process background pixels in-between two or more labels
		this.fireStatusChanged(this, "MergeLabels: find boundary pixels");
        for (int y = 0; y < sizeY; y++)
        {
    		this.fireProgressChanged(this, y, sizeY);
            for (int x = 0; x < sizeX; x++)
            {
                // focus on background pixels
                if (image.getf(x, y) != 0)
                    continue;

                // extract the neighbor labels
                ArrayList<Float> neighborLabels = new ArrayList<Float>(shifts.length);
                for (int[] shift : shifts)
                {
                    int x2 = x + shift[0];
                    if (x2 < 0 || x2 >= sizeX) continue;

                    int y2 = y + shift[1];
                    if (y2 < 0 || y2 >= sizeY) continue;

                    float value2 = image.getf(x2, y2);
                    if (value2 == 0) continue;

                    if (!neighborLabels.contains(value2))
                    {
                        neighborLabels.add(value2);
                    }
                }

                // count number of neighbors
                if (neighborLabels.size() > 1 && labelSet.containsAll(neighborLabels))
                    boundaryPixels.add(new int[] {x, y});
            }
        }
		this.fireProgressChanged(this, sizeY, sizeY);
        
        // replace label value of all regions
		this.fireStatusChanged(this, "MergeLabels: replace labels");
        LabelImages.replaceLabels(image, labels, newLabel);

		this.fireStatusChanged(this, "MergeLabels: fill boundary");
        for (int[] pos : boundaryPixels)
            image.setf(pos[0], pos[1], newLabel);          
	}
	

	/**
	 * Merge several regions identified by their label, filling the gap between
	 * former regions. Labels are merged in place (i.e., the input image is
	 * modified). The background voxels are merged only if they are neighbor of
	 * at least two regions to be merged, and of no other region.
	 * 
	 * @param image
	 *            a 3D label image
	 * @param labels
	 *            the list of labels to replace
	 * @param newLabel
	 *            the new value for labels
	 * @param conn
	 *            the connectivity to check neighbors of background pixels
	 */
	public void process(ImageStack image, float[] labels, float newLabel, int conn)
	{
        // get image size
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        int sizeZ = image.getSize();
        
        // convert array to tree (to accelerate search)
        HashSet<Float> labelSet = new HashSet<Float>();
        for (int i = 0; i < labels.length; i++)
        {
            labelSet.add(labels[i]);
        }
        
        // determines the shift to compute neighbor coordinates
        int[][] shifts;
        if (conn == 6)
        {
            shifts = new int[][] {{0, 0, -1}, {0, -1, 0}, {-1, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        }
        else if (conn == 26)
        {
            shifts = new int[][] {
                // previous slice (dz = -1)
                {-1, -1, -1}, {0, -1, -1}, {1, -1, -1},   {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},   {-1, 1, -1}, {0, 1, -1}, {1, 1, -1},   
                // current slice (dz = 0)
                {-1, -1, 0}, {0, -1, 0}, {1, -1, 0},   {-1, 0, 0}, {1, 0, 0},   {-1, 1, 0}, {0, 1, 0}, {1, 1, 0},   
                // next slice (dz = +1 )
                {-1, -1, 1}, {0, -1, 1}, {1, -1, 1},   {-1, 0, 1}, {0, 0, 1}, {1, 0, 1},   {-1, 1, 1}, {0, 1, 1}, {1, 1, 1}};
        }
        else
        {
            throw new IllegalArgumentException("Connectivity value should be either 6 or 26.");
        }
        
        // create structure to store the boundary voxels
        ArrayList<int[]> boundaryVoxels = new ArrayList<int[]>();
        
        // process background pixels in-between two or more labels
		this.fireStatusChanged(this, "MergeLabels: find boundary pixels");
        for (int z = 0; z < sizeZ; z++)
        {
        	this.fireProgressChanged(this, z, sizeZ);
			for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    // process only background voxels
                    if(image.getVoxel(x, y, z) != 0)
                        continue;

                    // extract the neighbor labels
                    ArrayList<Float> neighborLabels = new ArrayList<Float>(shifts.length);
                    for (int[] shift : shifts)
                    {
                        int x2 = x + shift[0];
                        if (x2 < 0 || x2 >= sizeX) continue;

                        int y2 = y + shift[1];
                        if (y2 < 0 || y2 >= sizeY) continue;

                        int z2 = z + shift[2];
                        if (z2 < 0 || z2 >= sizeZ) continue;

                        float value2 = (float) image.getVoxel(x2, y2, z2);
                        if (value2 == 0) continue;

                        if (!neighborLabels.contains(value2))
                        {
                            neighborLabels.add(value2);
                        }
                    }

                    // check if all neighbor labels are in the label set
                    if (neighborLabels.size() > 1 && labelSet.containsAll(neighborLabels))
                        boundaryVoxels.add(new int[] {x, y, z});
                }
            }
        }
		this.fireProgressChanged(this, sizeZ, sizeZ);
        
        // replace label value of all regions
		this.fireStatusChanged(this, "MergeLabels: replace labels");
        LabelImages.replaceLabels(image, labels, newLabel);
        
		this.fireStatusChanged(this, "MergeLabels: fill boundary");
		int np = boundaryVoxels.size();
        for (int p = 0; p < np; p++)
        {
        	this.fireProgressChanged(this, p, np);
        	int[] pos = boundaryVoxels.get(p);
        	image.setVoxel(pos[0], pos[1], pos[2], newLabel);
        }
		this.fireStatusChanged(this, "");
	}

}
