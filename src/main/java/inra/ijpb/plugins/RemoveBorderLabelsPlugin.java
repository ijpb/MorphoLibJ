/**
 * 
 */
package inra.ijpb.plugins;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.morphology.LabelImages;

/**
 * Removes all labels that touch the selected border, and replace them with value 0.
 * 
 * The plugins first opens a dialog that allows to choose the borders to 
 * consider. For 3D images, front and back borders can also be chosen. 
 * Then the labels that touch at least one of the selected borders are replaced
 * by the value zero. The result is shown in a new ImagePlus.  
 * 
 * @see inra.ijpb.morphology.LabelImages
 * 
 * @author David Legland
 *
 */
public class RemoveBorderLabelsPlugin implements PlugIn {


	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0)
	{
		ImagePlus imagePlus = IJ.getImage();
		boolean isStack = imagePlus.getStackSize() > 1;
		
		// opens a dialog to choose which borders to remove
		GenericDialog gd = new GenericDialog("Remove Border Labels");
		gd.addCheckbox("Left", true);
		gd.addCheckbox("Right", true);
		gd.addCheckbox("Top", true);
		gd.addCheckbox("Bottom", true);
		if (isStack) 
		{
			gd.addCheckbox("Front", true);
			gd.addCheckbox("Back", true);
		}
		
		gd.showDialog();
		if (gd.wasCanceled())
		{
			return;
		}
		
		boolean removeLeft = gd.getNextBoolean();
		boolean removeRight = gd.getNextBoolean();
		boolean removeTop = gd.getNextBoolean();
		boolean removeBottom = gd.getNextBoolean();
		boolean removeFront = false, removeBack = false;
		if (isStack)
		{
			removeFront = gd.getNextBoolean();
			removeBack = gd.getNextBoolean();
		}
		
		IJ.showStatus("Identifies border labels");
		
		// identifies the set of labels that need to be removed
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
		ImageStack image = imagePlus.getStack();
		if (removeLeft)
			labelSet.addAll(findBorderLabelsLeft(image));
		if (removeRight)
			labelSet.addAll(findBorderLabelsRight(image));
		if (removeTop)
			labelSet.addAll(findBorderLabelsTop(image));
		if (removeBottom)
			labelSet.addAll(findBorderLabelsBottom(image));
		if (removeFront)
			labelSet.addAll(findBorderLabelsFront(image));
		if (removeBack)
			labelSet.addAll(findBorderLabelsBack(image));
		
		
		// convert label set to an array of int
		int[] labels = new int[labelSet.size()];
		int i = 0 ;
		Iterator<Integer> iter = labelSet.iterator();
		while(iter.hasNext()) {
			labels[i++] = (int) iter.next();
		}

		// create result image
		IJ.showStatus("Duplicates image");
		ImagePlus resultPlus = imagePlus.duplicate();

		// remove labels direclty within result image
		IJ.showStatus("Remove border labels");
		LabelImages.removeLabels(resultPlus, labels);
		IJ.showStatus("");
		
		resultPlus.setTitle(imagePlus.getShortTitle() + "-killBorders");
		
		// Display with same settings as original image
		resultPlus.show();
		if (isStack) 
		{
			resultPlus.setZ(imagePlus.getZ());
			resultPlus.setSlice(imagePlus.getSlice());
		}
	}
	
	private static final Collection<Integer> findBorderLabelsLeft(ImageStack image) {
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
	
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				labelSet.add((int) image.getVoxel(0, y, z));
			}
		}
	
		// remove label for the background
		labelSet.remove(0);
				
		return labelSet;
	}

	private static final Collection<Integer> findBorderLabelsRight(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
	
		for (int z = 0; z < sizeZ; z++) {
			for (int y = 0; y < sizeY; y++) {
				labelSet.add((int) image.getVoxel(sizeX - 1, y, z));
			}
		}
	
		// remove label for the background
		labelSet.remove(0);
				
		return labelSet;
	}

	private static final Collection<Integer> findBorderLabelsTop(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeZ = image.getSize();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
	
		for (int z = 0; z < sizeZ; z++) {
			for (int x = 0; x < sizeX; x++) {
				labelSet.add((int) image.getVoxel(x, 0, z));
			}
		}
	
		// remove label for the background
		labelSet.remove(0);
				
		return labelSet;
	}

	private static final Collection<Integer> findBorderLabelsBottom(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
	
		for (int z = 0; z < sizeZ; z++) {
			for (int x = 0; x < sizeX; x++) {
				labelSet.add((int) image.getVoxel(x, sizeY - 1, z));
			}
		}
	
		// remove label for the background
		labelSet.remove(0);
				
		return labelSet;
	}

	private static final Collection<Integer> findBorderLabelsFront(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
	
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				labelSet.add((int) image.getVoxel(x, y, 0));
			}
		}
		
		// remove label for the background
		labelSet.remove(0);
				
		return labelSet;
	}

	private static final Collection<Integer> findBorderLabelsBack(ImageStack image) {
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		TreeSet<Integer> labelSet = new TreeSet<Integer>();
	
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				labelSet.add((int) image.getVoxel(x, y, sizeZ - 1));
			}
		}
		
		// remove label for the background
		labelSet.remove(0);
				
		return labelSet;
	}
}
