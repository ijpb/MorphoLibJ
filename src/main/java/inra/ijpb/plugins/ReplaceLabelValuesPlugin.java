/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.label.edit.ReplaceLabelValues;

/**
 * Replaces the value of selected label(s) by a given value, 
 * for 2D/3D images, for gray8, gray16 and float images. 
 * Can specify multiple labels.
 * 
 * @author David Legland
 *
 */
public class ReplaceLabelValuesPlugin implements PlugIn 
{
	@Override
	public void run(String arg0) 
	{
		ImagePlus imagePlus = IJ.getImage();

		GenericDialog gd = new GenericDialog("Remove/Replace Label(s)");
		gd.addStringField("Label(s)", "1", 12);
		gd.addMessage("Separate label values by \",\"");
		gd.addNumericField("Final Value", 0, 0);
		gd.addMessage("Replacing by value 0\n will remove labels");
		gd.showDialog();

		if (gd.wasCanceled())
			return;

		String labelListString = gd.getNextString();
		float[] labelArray = parseLabels(labelListString);
		float finalValue = (float) gd.getNextNumber();

		// create algo for replacing values in original image
		ReplaceLabelValues algo = new ReplaceLabelValues();
		DefaultAlgoListener.monitor(algo);
		algo.process(imagePlus, labelArray, finalValue);

		// update display
		imagePlus.updateAndDraw();
	}

	private static final float[] parseLabels(String string) 
	{
		String[] tokens = string.split("[, ]+");
		int n = tokens.length;

		float[] labels = new float[n];
		for (int i = 0; i < n; i++)
		{
			labels[i] = (float) Double.parseDouble(tokens[i]);
		}
		return labels;
	}
}
