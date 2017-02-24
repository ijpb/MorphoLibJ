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
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import inra.ijpb.data.border.BorderManager;
import inra.ijpb.data.border.BorderManager3D;


/**
 * Pads the current image with the specified value or color. Works for 2D or 3D
 * images. In the latter case, it is possible to specify the number of slices to
 * add in front of and behind the stack.
 * 
 * @author David Legland
 *
 */
public class ExtendBordersPlugin implements PlugIn 
{
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) 
	{
		// Get current image, and show error msg if no one is open
		ImagePlus imagePlus = IJ.getImage();
		
		// Open a dialog to choose the different parameters
		GenericDialog gd = new GenericDialog("Add Border");
		gd.addNumericField("Left", 0, 0);
		gd.addNumericField("Right", 0, 0);
		gd.addNumericField("Top", 0, 0);
		gd.addNumericField("Bottom", 0, 0);
		if (imagePlus.getStackSize() > 1)
		{
			gd.addNumericField("Front", 0, 0);
			gd.addNumericField("Back", 0, 0);
		}
		gd.addChoice("Fill Value", BorderManager.Type.getAllLabels(),
				BorderManager.Type.REPLICATED.toString());

		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		// Extract the different border values
		int left 	= (int) gd.getNextNumber();
		int right 	= (int) gd.getNextNumber();
		int top 	= (int) gd.getNextNumber();
		int bottom 	= (int) gd.getNextNumber();
		int front= 0, back = 0;
		if (imagePlus.getStackSize() > 1)
		{
			 front 	= (int) gd.getNextNumber();
			 back 	= (int) gd.getNextNumber();
		}
		
		// parse border manager type
		String label = gd.getNextChoice();
		ImagePlus resPlus;
		if (imagePlus.getStackSize() == 1)
		{
			BorderManager.Type borderType = BorderManager.Type.fromLabel(label);

			// create border manager for input image
			ImageProcessor image = imagePlus.getProcessor();
			BorderManager border = borderType.createBorderManager(image);

			// Execute core of the plugin
			ImageProcessor res = process(image, left, right, top, bottom, border);
			resPlus = new ImagePlus(imagePlus.getShortTitle()+"-ext", res);
		}
		else
		{
			BorderManager3D.Type borderType = BorderManager3D.Type.fromLabel(label);

			// create border manager for input image
			ImageStack image = imagePlus.getStack();
			BorderManager3D border = borderType.createBorderManager(image);

			// Execute core of the plugin
			ImageStack res = process(image, left, right, top, bottom, front, back, border);
			resPlus = new ImagePlus(imagePlus.getShortTitle()+"-ext", res);			
		}
		
		// display result image
//		resPlus.copyAttributes(imagePlus);
		resPlus.show();
	}
	
	@Deprecated
	public ImagePlus exec(ImagePlus image, int left, int right, int top,
			int bottom, BorderManager border)
	{
		ImageProcessor proc = image.getProcessor();
		ImageProcessor result = process(proc, left, right, top, bottom, border);
		return new ImagePlus(image.getTitle(), result);
	}

	/**
	 * Adds the specified number of pixels around the input image, and returns
	 * the resulting image. 
	 * 
	 * @param image
	 *            the input image
	 * @param left
	 *            the number of pixels to add to the left of the image
	 * @param right
	 *            the number of pixels to add to the right of the image
	 * @param top
	 *            the number of pixels to add on top of the image
	 * @param bottom
	 *            the number of pixels to add at the bottom of the image
	 * @param border
	 *            an instance of BorderManager that specifies the value of
	 *            pixels to be added
	 * @return a new image with extended borders
	 */
	public static final ImageProcessor process(ImageProcessor image, 
			int left, int right, int top, int bottom, BorderManager border)
	{
		// get image dimensions
		int width = image.getWidth(); 
		int height = image.getHeight(); 
		
		// compute result dimensions
		int width2 = width + left + right;
		int height2 = height + top + bottom;
		ImageProcessor result = image.createProcessor(width2 , height2);
		
		// fill result image
		for (int x = 0; x < width2; x++)
		{
			for (int y = 0; y < height2; y++)
			{
				result.set(x, y, border.get(x - left, y - top));
			}
		}
		
		return result;
	}
	/**
	 * Adds the specified number of pixels around the input image, and returns
	 * the resulting image. 
	 * 
	 * @param image
	 *            the input image stack 
	 * @param left
	 *            the number of voxels to add to the left
	 * @param right
	 *            the number of voxels to add to the right
	 * @param top
	 *            the number of voxels to add on top of the stack
	 * @param bottom
	 *            the number of voxels to add at the bottom of the stack
	 * @param front
	 *            the number of slices to add in front of the stack
	 * @param back
	 *            the number of slices to add behind the stack
	 * @param border
	 *            an instance of BorderManager that specifies the value of
	 *            pixels to be added
	 * @return a new image with extended borders
	 */
	public static final ImageStack process(ImageStack image, 
			int left, int right, int top, int bottom, int front, int back, BorderManager3D border)
	{
		// get image dimensions
		int width = image.getWidth(); 
		int height = image.getHeight(); 
		int depth = image.getSize(); 
		
		// compute result dimensions
		int width2 = width + left + right;
		int height2 = height + top + bottom;
		int depth2 = depth + front + back;
		ImageStack result = ImageStack.create(width2 , height2, depth2, image.getBitDepth());
		
		// fill result image
		for (int z = 0; z < depth2; z++)
		{
			for (int y = 0; y < height2; y++)
			{
				for (int x = 0; x < width2; x++)
				{
					result.setVoxel(x, y, z, border.get(x - left, y - top, z - front));
				}
			}
		}
		return result;
	}
}
