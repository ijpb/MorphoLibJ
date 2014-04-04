/**
 * 
 */
package inra.ijpb.plugins;

import java.util.Iterator;
import java.util.TreeSet;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import inra.ijpb.morphology.LabelImages;

/**
 * @author David Legland
 *
 */
public class RemoveBorderLabelsPlugin implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) {
		ImagePlus imagePlus = IJ.getImage();
		
		ImagePlus resultPlus = imagePlus.duplicate();
		ImageStack stack = resultPlus.getStack();

		int[] labels = findBorderLabels(stack);
		LabelImages.removeLabels(stack, labels);
		
		// Display with same settings as original image
		resultPlus.show();
		if (imagePlus.getStackSize() > 1) {
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}

	public static final int[] findBorderLabels(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();

		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				labelSet.add((int) image.getVoxel(x, y, 0));
				labelSet.add((int) image.getVoxel(x, y, sizeZ - 1));
			}
		}
		
		for (int z = 0; z < sizeZ; z++) {
			for (int x = 0; x < sizeX; x++) {
				labelSet.add((int) image.getVoxel(x, 0, z));
				labelSet.add((int) image.getVoxel(x, sizeY - 1, z));
			}
		}
		
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				labelSet.add((int) image.getVoxel(0, y, z));
				labelSet.add((int) image.getVoxel(sizeX - 1, y, z));
			}
		}
	
		// remove label for the background
		labelSet.remove(0);
		
		// convert to an arrat of int
		int[] labels = new int[labelSet.size()];
		int i = 0 ;
		Iterator<Integer> iter = labelSet.iterator();
		while(iter.hasNext()) {
			labels[i++] = (int) iter.next();
		}
		
		return labels;
	}
}
