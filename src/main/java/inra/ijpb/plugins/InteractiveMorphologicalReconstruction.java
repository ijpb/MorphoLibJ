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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.event.ActionEvent;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.gui.Toolbar;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.morphology.Reconstruction;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for performing interactive morphological reconstruction by dilation or
 * by erosion on grayscale images.
 *
 * In contrast with the non-interactive plugin, the active image is considered
 * the mask image, while the marker image is constructed by user-defined point
 * ROIs. The marker image is used to initialize the reconstruction, and the mask
 * image is used to constrain it. The connectivity can also be specified.
 *
 * @author Ignacio Arganda-Carreras (ignacio.arganda@ehu.eus)
 */
public class InteractiveMorphologicalReconstruction implements ExtendedPlugInFilter,
DialogListener
{
	/** Apparently, it's better to store flags in plugin */
	private int flags = DOES_8G | DOES_8C | DOES_16 | DOES_32 | DOES_RGB | KEEP_PREVIEW
			| FINAL_PROCESSING;
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;

	/** need to keep the instance of ImagePlus */
	private ImagePlus imagePlus;

	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;

	private static Connectivity2D connectivity = Connectivity2D.C4;
	private static Operation operation = Operation.BY_DILATION;

	private NonBlockingGenericDialog gd;

	/**
	 * A pre-defined set of operations for geodesic reconstruction.
	 */
	enum Operation {
		/** reconstruction by dilation */
		BY_DILATION("By Dilation"),
		/** reconstruction by erosion */
		BY_EROSION("By Erosion");

		private final String label;

		private Operation(String label) {
			this.label = label;
		}

		public ImageProcessor applyTo(
				ImageProcessor marker,
				ImageProcessor mask,
				int conn )
		{
			if ( this == BY_DILATION )
				return Reconstruction.reconstructByDilation(marker, mask, conn);
			if ( this == BY_EROSION )
				return Reconstruction.reconstructByErosion(marker, mask, conn);

			throw new RuntimeException(
					"Unable to process the " + this + " operation");
		}

		public String toString() {
			return this.label;
		}

		/**
		 * Returns all the labels for this enumeration.
		 * 
		 * @return all the labels for this enumeration.
		 */
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

	/** Keep instance of result image */
	private ImageProcessor result;

	private RoiListener listener;

	/**
	 * Called at the beginning of the process to know if the plugin can be run
	 * with current image, and at the end to finalize.
	 */
	public int setup(String arg, ImagePlus imp)
	{
		if( null == imp )
			return DONE;
		// Special case of plugin called to finalize the process
		if ( arg.equals("final") ) {
			// replace the preview image by the original image
			imagePlus.setProcessor( baseImage );
			imagePlus.draw();

			if( null != result )
			{
				// Create a new ImagePlus with the filter result
				String newName = createResultImageName( imagePlus );
				ImagePlus resPlus = new ImagePlus( newName, result );
				resPlus.copyScale( imagePlus );
				resPlus.show();
			}
			Roi.removeRoiListener( listener );
			return DONE;
		}

		return flags;
	}

	public int showDialog(
			ImagePlus imp,
			String command,
			final PlugInFilterRunner pfr )
	{
		// Store user data
		this.imagePlus = imp;
		this.baseImage = imp.getProcessor().duplicate();
		this.pfr = pfr;

		/** ROI listener to update plugin when new ROIs are added */
		this.listener = new RoiListener() {
			@Override
			public void roiModified(ImagePlus imp, int id) {

				if( imp == imagePlus )
				{
					// set preview to false to restart plugin filter runner
					gd.getPreviewCheckbox().setState( false );

					pfr.dialogItemChanged( gd,
							new ActionEvent( gd.getPreviewCheckbox(),
									ActionEvent.ACTION_PERFORMED, "Preview" ) );
				}
			}
		};
		Roi.addRoiListener( listener );

		// select point tool for manual introduction of markers
		Toolbar.getInstance().setTool( Toolbar.POINT );

		// create the dialog
		gd = new NonBlockingGenericDialog( "Interactive Morphological "
				+ "Reconstruction");

		gd.addChoice("Type of Reconstruction",
				Operation.getAllLabels(),
				operation.label );
		gd.addChoice("Connectivity",
				Connectivity2D.getAllLabels(),
				Connectivity2D.C4.name() );
		gd.addPreviewCheckbox( pfr );
		gd.addDialogListener(this);
		previewing = true;
		gd.addHelp( "http://imagej.net/MorphoLibJ" );
		gd.showDialog();
		previewing = false;

		if (gd.wasCanceled())
			return DONE;

		// set up current parameters
		operation = Operation.fromLabel(gd.getNextChoice());
		connectivity = Connectivity2D.fromLabel(gd.getNextChoice());

		return flags;
	}

	/**
	 * Called when a dialog widget has been modified: recomputes option values
	 * from dialog content.
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt)
	{
		// set up current parameters
		operation = Operation.fromLabel(gd.getNextChoice());
		connectivity = Connectivity2D.fromLabel(gd.getNextChoice());

		return true;
	}

	public void setNPasses (int nPasses)
	{
		this.nPasses = nPasses;
	}

	/**
	 * Apply the current filter settings to process the given image.
	 */
	public void run( ImageProcessor image )
	{
		if( null == image )
			return;
		long t0 = System.currentTimeMillis();

		// Compute geodesic reconstruction
		result = process( image, imagePlus.getRoi() );

		if ( null == result )
		{
			// force preview to be unchecked when processing fails
			gd.getPreviewCheckbox().setState( false );
			pfr.dialogItemChanged( gd,
					new ActionEvent( gd.getPreviewCheckbox(),
							ActionEvent.ACTION_PERFORMED, "Preview" ) );
			return;
		}
		if( previewing )
		{
			// Fill up the values of original image with values of the result
			image.setPixels( result.getPixels() );

			image.resetMinAndMax();

			if (image.isInvertedLut())
				image.invertLut();
		}

		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(operation.toString(), t1 - t0, imagePlus);
	}
	/**
	 * Apply geodesic reconstruction to mask image based on current operation
	 * and ROI
	 * @param mask mask image
	 * @param roi region of interest to create marker image
	 * @return reconstructed image
	 */
	ImageProcessor process( ImageProcessor mask, Roi roi )
	{
		if( mask == null || imagePlus == null || baseImage == null)
		{
			IJ.showMessage( "Please run the plugin with an image open." );
			return null;
		}
		if( roi == null )
		{
			IJ.showMessage( "Please define the markers using for example "
					+ "the point selection tool." );
			return null;
		}
		
		// Create marker image from ROI
		ImageProcessor marker;
		if( mask instanceof ByteProcessor )
		{
			marker = new ByteProcessor( mask.getWidth(), mask.getHeight() );
			marker.setValue( 255 );
		}
		else if( mask instanceof ShortProcessor )
		{
			marker = new ShortProcessor( mask.getWidth(), mask.getHeight() );
			marker.setValue( 65535 );
		}
		else if( mask instanceof ColorProcessor )
		{
			marker = new ColorProcessor( mask.getWidth(), mask.getHeight() );
			marker.setColor(Color.WHITE);
		}
		else
		{
			marker = new FloatProcessor( mask.getWidth(), mask.getHeight() );
			marker.setValue( Float.MAX_VALUE );
		}

		// paint ROI
		if( roi.isArea() )
			marker.fill( roi );
		else
			marker.draw( roi );

		// invert marker image if reconstructing by erosion
		if( operation == Operation.BY_EROSION )
			marker.invert();

		// Compute geodesic reconstruction
		return operation.applyTo( marker, mask, connectivity.getValue() );
	}

	private static String createResultImageName( ImagePlus baseImage ) {
		return baseImage.getShortTitle() + "-rec";
	}
}
