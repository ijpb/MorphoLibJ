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

import java.awt.Font;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.util.IJUtils;
import inra.ijpb.watershed.ExtendedMinimaWatershed;

/**
 * This plugin computes watershed on the inverse of the distance map of a 3D
 * binary image. Imposition of extended minima is used to control the number of
 * local minima. This way, touching objects can be separated.
 *
 * @author Ignacio Arganda-Carreras (ignacio.arganda@ehu.eus)
 *
 */
public class DistanceTransformWatershed3D implements PlugIn
{
	/** the different weights */
	private ChamferWeights3D weights;
	private static boolean floatProcessing	= false;
	private static boolean normalize = true;

	private static int dynamic = 2;
	private static String weightLabel = ChamferWeights3D.BORGEFORS.toString();

	private static Conn3D connectivity = Conn3D.C6;

	/**
	 * A pre-defined set of connectivities
	 */
	enum Conn3D {
		C6("6", 6),
		C26("26", 26);

		private final String label;
		private final int value;

		private Conn3D(String label, int value) {
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
			int n = Conn3D.values().length;
			String[] result = new String[n];

			int i = 0;
			for (Conn3D op : Conn3D.values())
				result[i++] = op.label;

			return result;
		}

		/**
		 * Determines the operation type from its label.
		 * @param opLabel name of the operation type
		 * @return 3D connectivity type
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Conn3D fromLabel(String opLabel) {
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Conn3D op : Conn3D.values()) {
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Conn3D with label: " + opLabel);
		}
	};

	/**
	 * Plugin run method
	 */
	public void run(String arg) {
		
		ImagePlus image = WindowManager.getCurrentImage();
		if ( image == null ) 
		{
			IJ.error("Distance Transform Watershed 3D",
					"Need at least one image to work");
			return;
		}

		if( !BinaryImages.isBinaryImage( image ) )
		{
			IJ.error( "Distance Transform Watershed 3D", "Input image is not"
					+ " binary (8-bit with only 0 or 255 values)" );
		}

		// Create a new generic dialog with appropriate options
		GenericDialog gd = new GenericDialog( "Distance Transform Watershed 3D" );
		gd.setInsets( 0, 0, 0 );
		gd.addMessage( "Distance map options:",
				new Font( "SansSerif", Font.BOLD, 12 ) );
		gd.addChoice( "Distances", ChamferWeights3D.getAllLabels(), weightLabel );
		String[] outputTypes = new String[]{"32 bits", "16 bits"};
		gd.addChoice( "Output Type", outputTypes, outputTypes[ floatProcessing ? 0:1 ]);
		gd.setInsets( 0, 0, 0 );
		gd.addCheckbox( "Normalize weights", normalize );
		gd.setInsets( 20, 0, 0 );
		gd.addMessage( "Watershed options:",
				new Font( "SansSerif", Font.BOLD, 12 ) );
		gd.addNumericField( "Dynamic", dynamic, 2 );
		gd.addChoice( "Connectivity", Conn3D.getAllLabels(),
				connectivity.label );
		gd.addHelp( "http://imagej.net/MorphoLibJ#Utilities_for_binary_images" );
		gd.showDialog();

		// test cancel
		if ( gd.wasCanceled() )
			return;

		// set up current parameters
		weightLabel = gd.getNextChoice();
		floatProcessing = gd.getNextChoiceIndex() == 0;
		normalize = gd.getNextBoolean();
		dynamic = (int) gd.getNextNumber();
		connectivity = Conn3D.fromLabel( gd.getNextChoice() );

		// identify which weights should be used
		weights = ChamferWeights3D.fromLabel( weightLabel );

		long t0 = System.currentTimeMillis();

		final ImagePlus result;
		if (floatProcessing)
			result = processFloat( image, weights.getFloatWeights(), normalize );
		else
			result = processShort( image, weights.getShortWeights(), normalize );

		Images3D.optimizeDisplayRange( result );

		// Display the result image
		result.show();
		result.setSlice( image.getCurrentSlice() );

		// Display elapsed time
		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime( "Distance Transform Watershed 3D",
				t1 - t0, image );
	}

	private ImagePlus processFloat(
			ImagePlus image,
			float[] weights,
			boolean normalize )
	{
		final ImageStack dist =
				BinaryImages.distanceMap( image.getImageStack(), weights,
						normalize );
		// invert distance map
		Images3D.invert( dist );

		ImageStack result = ExtendedMinimaWatershed.extendedMinimaWatershed(
				dist, image.getImageStack(), dynamic, connectivity.value, false );
		ImagePlus ip = new ImagePlus( image.getShortTitle() + "dist-watershed",
				result );
		ip.setCalibration( image.getCalibration() );
		return ip;
	}

	private ImagePlus processShort(
			ImagePlus image,
			short[] weights,
			boolean normalize )
	{
		// Compute distance on specified image
		final ImageStack dist =
				BinaryImages.distanceMap( image.getImageStack(), weights,
						normalize );
		// invert distance map
		Images3D.invert( dist );

		ImageStack result = ExtendedMinimaWatershed.extendedMinimaWatershed(
				dist, image.getImageStack(), dynamic, connectivity.value, false );
		ImagePlus ip = new ImagePlus( image.getShortTitle() + "dist-watershed",
				result );
		ip.setCalibration( image.getCalibration() );
		return ip;
	}
}
