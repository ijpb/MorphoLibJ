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
import java.util.Map;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.PolygonRoi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import inra.ijpb.geometry.Polygon2D;
import inra.ijpb.label.LabelMapToPolygons;

/**
 * Convert to outer boundary of the binary region present within the current
 * image into a polygon, and add this polygon to the list of current ROIs.
 * 
 * @author dlegland
 *
 */
public class LabelMapToPolygonRois implements PlugIn
{
	@Override
	public void run(String arg)
	{
		// retrieve current image
		ImagePlus imagePlus = IJ.getImage();
		ImageProcessor image = imagePlus.getProcessor();

		// create the dialog, with operator options
		GenericDialog gd = new GenericDialog("Label Maps To Rois");
		gd.addChoice("Connectivity:", new String[] { "C4", "C8" }, "C4");
		gd.addChoice("Vertex_Location:", new String[] { "Corners", "Edge_Middles", "Pixel_Centers" }, "Corners");
		gd.addStringField("Name_Pattern", "r%03d");

		// wait for user input
		gd.showDialog();
		// If cancel was clicked, do nothing
		if (gd.wasCanceled()) return;

		// parse options
		int conn = gd.getNextChoiceIndex() == 0 ? 4 : 8;
		int locIndex = gd.getNextChoiceIndex();
		LabelMapToPolygons.VertexLocation loc = LabelMapToPolygons.VertexLocation.CORNER;
		if (locIndex == 1) loc = LabelMapToPolygons.VertexLocation.EDGE_CENTER;
		if (locIndex == 2) loc = LabelMapToPolygons.VertexLocation.PIXEL;
		String pattern = gd.getNextString();

		// compute boundaries
		LabelMapToPolygons tracker = new LabelMapToPolygons(conn, loc);
		Map<Integer, ArrayList<Polygon2D>> boundaries = tracker.process(image);

		// retrieve RoiManager
		RoiManager rm = RoiManager.getInstance();
		if (rm == null)
		{
			rm = new RoiManager();
		}

		// populate RoiManager with PolygonRoi
		for (int label : boundaries.keySet())
		{
			ArrayList<Polygon2D> polygons = boundaries.get(label);
			String name = String.format(pattern, label);

			if (polygons.size() == 1)
			{
				PolygonRoi roi = polygons.get(0).createRoi();
				roi.setName(name);
				rm.addRoi(roi);
			}
			else
			{
				int index = 0;
				for (Polygon2D poly : polygons)
				{
					PolygonRoi roi = poly.createRoi();
					roi.setName(name + "-" + (index++));
					rm.addRoi(roi);
				}
			}
		}
	}
}
