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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.data.image.ImageUtils;

/**
 * Simple plugin to replace a value by another one, for 2D/3D images, for 
 * gray8, gray16 and float images.
 * 
 * @author David Legland
 *
 */
public class ReplaceValuePlugin implements PlugIn 
{
	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();
		
		GenericDialog gd = new GenericDialog("Replace Value");
		gd.addNumericField("Initial Value", 1, 0);
		gd.addNumericField("Final Value", 0, 0);
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		double initialValue = gd.getNextNumber();
		double finalValue = gd.getNextNumber();

		ImageUtils.replaceValue(imagePlus, initialValue, finalValue);
		imagePlus.updateAndDraw();
	}
}
