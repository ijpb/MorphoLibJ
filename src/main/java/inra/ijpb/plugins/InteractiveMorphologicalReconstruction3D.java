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
import ij.WindowManager;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.Connectivity3D;
import inra.ijpb.morphology.Reconstruction3D;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for performing interactive geodesic reconstruction by dilation erosion
 * on grayscale images.
 *
 * In contrast with the non-interactive plugin, the active image is considered
 * the mask image, while the marker image is constructed by user-defined
 * point ROIs. The marker image is used to initialize the reconstruction, and
 * the mask image is used to constrain it.
 * The connectivity can also be specified.
 *
 * @author Ignacio Arganda-Carreras (ignacio.arganda@ehu.eus)
 */
public class InteractiveMorphologicalReconstruction3D implements PlugIn
{
	private static Connectivity3D connectivity = Connectivity3D.C6;
	private static Operation operation = Operation.BY_DILATION;

	private NonBlockingGenericDialog gd;

	/**
	 * A pre-defined set of operations for geodesic reconstruction.
	 */
	enum Operation {
		BY_DILATION("By Dilation"),
		BY_EROSION("By Erosion");

		private final String label;

		private Operation(String label) {
			this.label = label;
		}

		public ImageStack applyTo(
				ImageStack marker,
				ImageStack mask,
				int conn )
		{
			if ( this == BY_DILATION )
				return Reconstruction3D.reconstructByDilation(
						marker, mask, conn );
			if ( this == BY_EROSION )
				return Reconstruction3D.reconstructByErosion(
						marker, mask, conn );

			throw new RuntimeException(
					"Unable to process the " + this + " operation");
		}

		public String toString() {
			return this.label;
		}

		public static String[] getAllLabels(){
			int n = Operation.values().length;
			String[] result = new String[n];

			int i = 0;
			for (Operation op : Operation.values())
				result[i++] = op.label;

			return result;
		}

		/**
		 * Determines the operation type from its label.
		 *
		 * @param opLabel
		 *            the name of the operation
		 * @return the operation corresponding to the name
		 * @throws IllegalArgumentException
		 *             if operation name is not recognized.
		 */
		public static Operation fromLabel(String opLabel) {
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Operation op : Operation.values()) {
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
		}
	};


	/**
	 * Apply the current filter settings to process the given image.
	 */
	public void run( String arg )
	{
		ImagePlus image = WindowManager.getCurrentImage();
		if ( image == null || image.getImageStackSize() < 2 )
		{
			IJ.error( "Interactive Geodesic Reconstruction 3D",
					"Need at least one 3D image to work" );
			return;
		}

		// select point tool for manual introduction of markers
		Toolbar.getInstance().setTool( Toolbar.POINT );

		// create the dialog
		gd = new NonBlockingGenericDialog( "Interactive Geodesic "
				+ "Reconstruction 3D" );

		gd.addChoice("Type of Reconstruction",
				Operation.getAllLabels(),
				operation.label);
		gd.addChoice("Connectivity",
				Connectivity3D.getAllLabels(),
				connectivity.name());
		gd.addHelp( "http://imagej.net/MorphoLibJ" );
		gd.showDialog();

		if (gd.wasCanceled())
			return;

		// set up current parameters
		operation = Operation.fromLabel( gd.getNextChoice() );
		connectivity = Connectivity3D.fromLabel( gd.getNextChoice() );

		long t0 = System.currentTimeMillis();

		// Compute geodesic reconstruction
		final ImagePlus result = process( image, image.getRoi() );

		if( null == result )
			return;

		if (result.getBitDepth() != 24)
		{
			Images3D.optimizeDisplayRange( result );
		}
		
		// Display the result image
		result.setSlice( image.getCurrentSlice() );
		result.show();

		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime( operation.toString(), t1 - t0, image );
	}
	/**
	 * Apply geodesic reconstruction to mask image based on current operation
	 * and ROI. The regions of interest in different slices can be set either
	 * using the multi point selection tool or the ROI manager. When using the
	 * ROI manager, only the currently selected ROIs will be used to create
	 * the maker image.
	 * @param mask mask image
	 * @param roi region of interest to create marker image
	 * @return morphologically reconstructed image
	 */
	ImagePlus process( ImagePlus mask, Roi roi )
	{
		if( mask == null )
		{
			IJ.showMessage( "Please run the plugin with an image open." );
			return null;
		}
		if( roi == null )
		{
			IJ.showMessage( "Please define the markers using "
					+ "the (multi) point selection tool or the ROI manager." );
			return null;
		}

		// create marker image from ROIs with the same bit depth as the
		// mask image (otherwise the reconstruction based on the current
		// methods won't work).
		final int bitDepth = mask.getBitDepth();

		final double maxValue;
		if( bitDepth == 8 )
			maxValue = 255;
		else if ( bitDepth == 16 )
			maxValue = 65535;
		else if ( bitDepth == 24 )
			maxValue = 0xFFFFFF;
		else
			maxValue = Float.MAX_VALUE;

		ImageStack markerStack =
				new ImageStack( mask.getWidth(), mask.getHeight() );
		ImageProcessor[] markerSlice = new ImageProcessor[ mask.getImageStackSize() ];
		for( int n=0; n<mask.getImageStackSize(); n++ )
		{
			if( bitDepth == 8 )
				markerSlice[ n ] = new ByteProcessor( mask.getWidth(), mask.getHeight() );
			else if( bitDepth == 16 )
				markerSlice[ n ] = new ShortProcessor( mask.getWidth(), mask.getHeight() );			
			else if( bitDepth == 24 )
				markerSlice[ n ] = new ColorProcessor( mask.getWidth(), mask.getHeight() );			
			else
				markerSlice[ n ] = new FloatProcessor( mask.getWidth(), mask.getHeight() );			

			markerSlice[ n ].setValue( maxValue );
			markerSlice[ n ].setColor( maxValue );
		}
		// if input ROI is a point or multi-point ROI
		if( roi instanceof PointRoi )
		{
			int[] xpoints = roi.getPolygon().xpoints;
			int[] ypoints = roi.getPolygon().ypoints;

			for( int i=0; i<xpoints.length; i++ )
			{
				markerSlice[ ((PointRoi) roi).getPointPosition( i ) -1 ].draw(
						new PointRoi( xpoints[i], ypoints[i] ) );
			}
		}
		// if not and ROI manager is open, read ROIs from ROI manager
		else if ( null != RoiManager.getInstance() )
		{
			RoiManager manager = RoiManager.getInstance();
			int[] selected = manager.getSelectedIndexes();
			if( selected.length > 0 )
			{
				for( int i=0; i<selected.length; i++ )
				{
					final Roi selectedRoi = manager.getRoi( i );
					int slice =
							manager.getSliceNumber( manager.getName( i ) );
					if( selectedRoi.isArea() )
						markerSlice[ slice-1 ].fill( selectedRoi );
					else
						markerSlice[ slice-1 ].draw( selectedRoi );
				}
			}
			else
			{
				IJ.error( "Please select the ROIs you want to use"
						+ " as markers in the ROI manager." );
				return null;
			}
		}
		// otherwise paint ROI on the slice currently selected
		// on the mask image
		else
		{
			int slice = mask.getCurrentSlice();

			if( roi.isArea() )
				markerSlice[ slice-1 ].fill( roi );
			else
				markerSlice[ slice-1 ].draw( roi );
		}

		// add slices to stack
		for( int n=0; n<mask.getImageStackSize(); n++ )
		{
			markerStack.addSlice( markerSlice[n] );
		}

		// invert marker image if reconstructing by erosion
		if( operation == Operation.BY_EROSION )
			Images3D.invert(markerStack);

		// Compute morphological reconstruction
		ImageStack result =
				operation.applyTo( markerStack, mask.getImageStack(),
						connectivity.getValue() );
		
		// Keep same color model
		result.setColorModel( mask.getImageStack().getColorModel() );
		
		// create resulting image
		String newName = mask.getShortTitle() + "-rec";
		ImagePlus resultPlus = new ImagePlus( newName, result );
		resultPlus.copyScale( mask );

		resultPlus.setSlice( mask.getCurrentSlice() );
		resultPlus.show();

		return resultPlus;
	}
}
