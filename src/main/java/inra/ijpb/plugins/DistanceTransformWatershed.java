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
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.ChamferWeights;
import inra.ijpb.watershed.ExtendedMinimaWatershed;

import java.awt.AWTEvent;
import java.awt.Font;

/**
 * This plugin computes watershed on the inverse of the distance map of a
 * binary image. Imposition of extended minima is used to control the number of
 * local minima. This way, touching objects can be interactively separated.
 *
 * @author Ignacio Arganda-Carreras (ignacio.arganda@ehu.eus)
 *
 */
public class DistanceTransformWatershed implements ExtendedPlugInFilter,
DialogListener
{

	/** Apparently, it's better to store flags in plugin */
	private int flags = DOES_8G | KEEP_PREVIEW | FINAL_PROCESSING;
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;

	/** need to keep the instance of ImagePlus */
	private ImagePlus imagePlus;

	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;

	/** the different weights */
	private ChamferWeights weights;
	private static boolean floatProcessing	= true;
	private static boolean normalize = true;

	private static int dynamic = 1;
	private static String connLabel = Conn2D.C4.label;
	private static String weightLabel = ChamferWeights.BORGEFORS.toString();

	private int connectivity = 4;

	/**
	 * A pre-defined set of connectivities
	 */
	enum Conn2D {
		C4("4", 4),
		C8("8", 8);

		private final String label;
		private final int value;

		private Conn2D(String label, int value) {
			this.label = label;
			this.value = value;
		}

		public String toString() {
			return this.label;
		}

		public int getValue() {
			return this.value;
		}

		public static String[] getAllLabels(){
			int n = Conn2D.values().length;
			String[] result = new String[n];

			int i = 0;
			for (Conn2D op : Conn2D.values())
				result[i++] = op.label;

			return result;
		}

		/**
		 * Determines the operation type from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Conn2D fromLabel(String opLabel) {
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Conn2D op : Conn2D.values()) {
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Conn2D with label: " + opLabel);
		}
	};

	/** Keep instance of result image */
	private ImageProcessor result;

	/**
	 * Called at the beginning of the process to know if the plugin can be run
	 * with current image, and at the end to finalize.
	 */
	public int setup(String arg, ImagePlus imp)
	{
		// Special case of plugin called to finalize the process
		if ( arg.equals("final") ) {
			// replace the preview image by the original image
			imagePlus.setProcessor(baseImage);
			imagePlus.draw();

			// Create a new ImagePlus with the filter result
			String newName = createResultImageName(imagePlus);
			ImagePlus resPlus = new ImagePlus(newName, result);
			resPlus.copyScale(imagePlus);
			resPlus.show();
			return DONE;
		}

		return flags;
	}

	public int showDialog(
			ImagePlus imp,
			String command,
			PlugInFilterRunner pfr )
	{
		if( !BinaryImages.isBinaryImage( imp ) )
		{
			IJ.error( "Distance Transform Watershed", "Input image is not"
					+ " binary (8-bit with only 0 or 255 values)" );
			return DONE;
		}
		// Store user data
		this.imagePlus = imp;
		this.baseImage = imp.getProcessor().duplicate();
		this.pfr = pfr;

		// Create a new generic dialog with appropriate options
		GenericDialog gd = new GenericDialog( "Distance Transform Watershed" );
		gd.setInsets( 0, 0, 0 );
		gd.addMessage( "Distance map options:",
				new Font( "SansSerif", Font.BOLD, 12 ) );
		gd.addChoice( "Distances", ChamferWeights.getAllLabels(), weightLabel );
		String[] outputTypes = new String[]{"32 bits", "16 bits"};
		gd.addChoice( "Output Type", outputTypes, outputTypes[ floatProcessing ? 0:1 ]);
		gd.setInsets( 0, 0, 0 );
		gd.addCheckbox( "Normalize weights", normalize );
		gd.setInsets( 20, 0, 0 );
		gd.addMessage( "Watershed options:",
				new Font( "SansSerif", Font.BOLD, 12 ) );
		gd.addNumericField( "Dynamic", dynamic, 2 );
		gd.addChoice( "Connectivity", Conn2D.getAllLabels(), connLabel );
		gd.setInsets( 20, 0, 0 );
		gd.addPreviewCheckbox( pfr );
		gd.addDialogListener(this);
		previewing = true;
		gd.addHelp( "http://imagej.net/MorphoLibJ#Utilities_for_binary_images" );
		gd.showDialog();
		previewing = false;

		// test cancel
		if (gd.wasCanceled())
			return DONE;

		// set up current parameters
		weightLabel = gd.getNextChoice();
		floatProcessing = gd.getNextChoiceIndex() == 0;
		normalize = gd.getNextBoolean();
		dynamic = (int) gd.getNextNumber();
		connLabel = gd.getNextChoice();
		connectivity = Conn2D.fromLabel( connLabel ).getValue();

		// identify which weights should be used
		weights = ChamferWeights.fromLabel( weightLabel );

		return flags;
	}

	/**
	 * Called when a dialog widget has been modified: recomputes option values
	 * from dialog content.
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt)
	{
		synchronized (this){
			// set up current parameters
			weightLabel = gd.getNextChoice();
			floatProcessing = gd.getNextChoiceIndex() == 0;
			normalize = gd.getNextBoolean();
			dynamic = (int) gd.getNextNumber();
			connLabel = gd.getNextChoice();
			connectivity = Conn2D.fromLabel( connLabel ).getValue();

			// identify which weights should be used
			weights = ChamferWeights.fromLabel( weightLabel );
		}
		return true;
	}

	public void setNPasses (int nPasses)
	{
		this.nPasses = nPasses;
	}

	/**
	 * Apply the current filter settings to process the given image.
	 */
	public void run(ImageProcessor image)
	{
		synchronized (this){
			if (floatProcessing)
				result = processFloat(image, weights.getFloatWeights(), normalize );
			else
				result = processShort(image, weights.getShortWeights(), normalize);
		}
		if (previewing)
		{
			// Fill up the values of original image with values of the result
			double valMax = result.getMax();
			for (int i = 0; i < image.getPixelCount(); i++)
			{
				image.set(i, (int) (255 * result.getf(i) / valMax));
			}
			image.resetMinAndMax();
			if (image.isInvertedLut())
				image.invertLut();
		}
	}

	private ImageProcessor processFloat(
			ImageProcessor image,
			float[] weights,
			boolean normalize )
	{
		final ImageProcessor dist =
				BinaryImages.distanceMap( image, weights, normalize );
		dist.invert();

		return ExtendedMinimaWatershed.extendedMinimaWatershed(
				dist, image, dynamic, connectivity, false );
	}

	private ImageProcessor processShort(
			ImageProcessor image,
			short[] weights,
			boolean normalize )
	{
		// Compute distance on specified image
		final ImageProcessor dist =
				BinaryImages.distanceMap( image, weights, normalize );
		dist.invert();

		return ExtendedMinimaWatershed.extendedMinimaWatershed(
				dist, image, dynamic, connectivity, false );
	}

	private static String createResultImageName(ImagePlus baseImage) {
		return baseImage.getShortTitle() + "-dist-watershed";
	}
}
