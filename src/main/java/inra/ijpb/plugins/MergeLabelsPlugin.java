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
/**
 * 
 */
package inra.ijpb.plugins;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.edit.MergeLabels;
import inra.ijpb.label.edit.ReplaceLabelValues;
import inra.ijpb.util.IJUtils;

/**
 * @author dlegland
 *
 */
public class MergeLabelsPlugin implements PlugIn
{
	// ====================================================
	// Enumeration of the different possibilities to manage gaps

	enum GapManagement
	{
		NO_GAP("No Gap"), ORTHOGONAL("Orthogonal"), DIAGONAL("Diagonal");

		String label;

		private GapManagement(String label)
		{
			this.label = label;
		}

		/**
		 * Returns a set of labels for the gap management strategies.
		 * 
		 * @return a list of labels
		 */
		public static String[] getAllLabels()
		{
			// array of all Strel types
			GapManagement[] values = GapManagement.values();
			int n = values.length;

			// keep all values but the last one ("Custom")
			String[] result = new String[n];
			for (int i = 0; i < n; i++)
				result[i] = values[i].label;

			return result;
		}

		/**
		 * Determines the gap management strategy from its label.
		 * 
		 * @param label
		 *            the name of the gap management strategy.
		 * @return a new instance of GapManagement
		 * @throws IllegalArgumentException
		 *             if label is not recognized.
		 */
		public static GapManagement fromLabel(String label)
		{
			if (label != null) label = label.toLowerCase();
			for (GapManagement type : GapManagement.values())
			{
				if (type.label.toLowerCase().equals(label)) return type;
			}
			throw new IllegalArgumentException("Unable to parse Strel.Shape with label: " + label);
		}

		/**
		 * @return the label associated to this gap management strategy.
		 */
		public String toString()
		{
			return this.label;
		}
	}

	// ====================================================
	// Calling functions

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg)
	{
		ImagePlus imagePlus = IJ.getImage();

		// create the dialog
		GenericDialog gd = new GenericDialog("Merge Labels");
		gd.addChoice("Gap Management", GapManagement.getAllLabels(), GapManagement.NO_GAP.toString());
		gd.showDialog();

		if (gd.wasCanceled()) return;

		int gapManagementIndex = gd.getNextChoiceIndex();

		float[] labels = convertArrayList(LabelImages.getSelectedLabels(imagePlus, imagePlus.getRoi()));


		long t0 = System.currentTimeMillis();
		switch (gapManagementIndex)
		{
			case 0:
			{
				ReplaceLabelValues algo = new ReplaceLabelValues();
				DefaultAlgoListener.monitor(algo);
				algo.process(imagePlus, labels, labels[0]);
				break;
			}
			case 1:
			{
				int conn = imagePlus.hasImageStack() ? 6 : 4;
				MergeLabels algo = new MergeLabels();
				DefaultAlgoListener.monitor(algo);
				algo.process(imagePlus, labels, labels[0], conn);
				break;
			}
			case 2:
			{
				int conn = imagePlus.hasImageStack() ? 26 : 6;
				MergeLabels algo = new MergeLabels();
				DefaultAlgoListener.monitor(algo);
				algo.process(imagePlus, labels, labels[0], conn);
				break;
			}
		}
		long t1 = System.currentTimeMillis();
		IJ.showProgress(1.0);
		IJUtils.showElapsedTime("Merge Labels", (t1 - t0), imagePlus); 
		
		imagePlus.updateAndDraw();
	}

	private static final float[] convertArrayList(ArrayList<Float> list)
	{
		// convert ArrayList to array
		float[] array = new float[list.size()];
		int i = 0;
		for (Float f : list)
			array[i++] = f != null ? f : Float.NaN;
		return array;
	}

}
