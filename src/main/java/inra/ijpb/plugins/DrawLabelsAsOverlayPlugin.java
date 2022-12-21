/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.plugin.PlugIn;
import inra.ijpb.data.image.ImageUtils;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.region2d.Centroid;
import inra.ijpb.util.IJUtils;

/**
 * Draw the labels of the regions as overlay.
 * 
 * The labels are located on the centroid of the regions.
 * 
 * @author dlegland
 *
 */
public class DrawLabelsAsOverlayPlugin implements PlugIn
{
    // Static fields for keeping results between successive calls to plugin

    static int xOffsetSave = -5;
    static int yOffsetSave = -5;
    
    int xOffset;
    int yOffset;
    
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) 
	{
        // create the list of image names
        String[] imageNames = IJUtils.getOpenImageNames();
        if (imageNames.length == 0)
        {
            IJ.error("No image", "Need at least one image to work");
            return;
        }

        // name of current image
        String currentImageName = IJ.getImage().getTitle();

        // create the dialog
        GenericDialog gd = new GenericDialog("Draw Labels");
        gd.addChoice("Label Image:", imageNames, currentImageName);
        gd.addChoice("Image to overlay:", imageNames, currentImageName);
        gd.addNumericField("X-Offset", xOffsetSave, 0);
        gd.addNumericField("Y-Offset", yOffsetSave, 0);
        
        // wait for user input
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }
        
        // parse dialog options
        int labelImageIndex = gd.getNextChoiceIndex();
        ImagePlus labelImage = WindowManager.getImage(labelImageIndex + 1);
        int resultImageIndex = gd.getNextChoiceIndex();
        ImagePlus resultImage = WindowManager.getImage(resultImageIndex + 1);
        this.xOffset = (int) gd.getNextNumber();
        this.yOffset = (int) gd.getNextNumber();
        
        // save some options for next dialog call
        xOffsetSave = xOffset;
        yOffsetSave = yOffset;
        
        // check if image is a label image
        if (!LabelImages.isLabelImageType(labelImage))
        {
            IJ.showMessage("Input image should be a label image");
            return;
        }
        // check if both images have same size
        if (!ImageUtils.isSameSize(labelImage, resultImage))
        {
            IJ.showMessage("Both images must have same size");
            return;
        }
        
        int[] labels = LabelImages.findAllLabels(labelImage);
        double[][] centroids = Centroid.centroids(labelImage.getProcessor(), labels);
		addLabelsAsOverlay(resultImage, labels, centroids);
	}
	
	/**
	 * Draw the values onto the target image.
	 * 
     * @param target
     *            the target image.
     * @param labels
     *            the list of labels of the regions.
     * @param coords
     *            for each region, the coordinate used for drawing the label.
	 */
	public void addLabelsAsOverlay(ImagePlus target, int[] labels, double[][] coords)
	{
	    Overlay overlay = new Overlay();
	    
        for (int i = 0; i < labels.length; i++)
        {
            String str = Integer.toString(labels[i]);
            int xi = (int) (coords[i][0] + this.xOffset);
            int yi = (int) (coords[i][1] + this.yOffset);
            Roi roi = new TextRoi(xi, yi, str);
            overlay.add(roi);
        }

	    target.setOverlay(overlay);
	}
}
