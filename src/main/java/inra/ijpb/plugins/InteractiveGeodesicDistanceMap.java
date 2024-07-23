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
import java.awt.event.ActionEvent;
import java.awt.image.IndexColorModel;

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
import ij.process.ImageProcessor;
import ij.process.LUT;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMasks2D;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformFloat;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformShort;
import inra.ijpb.color.ColorMaps;
import inra.ijpb.util.IJUtils;

/**
 * Plugin for computing geodesic distance map from binary images using chamfer
 * weights. The difference with the regular GeodesicDistanceMapPlugin is that
 * the marker image is created on demand based on user-defined ROIs.
 *
 * @author Ignacio Arganda-Carreras
 *
 */
public class InteractiveGeodesicDistanceMap implements ExtendedPlugInFilter,
DialogListener
{
	private int flags = DOES_8G | KEEP_PREVIEW | FINAL_PROCESSING;
	PlugInFilterRunner pfr;
	int nPasses;
	boolean previewing = false;

	/** need to keep the instance of ImagePlus */
	private ImagePlus imagePlus;

	/** keep the original image, to restore it after the preview */
	private ImageProcessor baseImage;

	private NonBlockingGenericDialog gd;
	/** Keep instance of result image */
	private ImageProcessor result;

	private RoiListener listener;

	/** the different weights */
	private ChamferMasks2D chamferChoice = ChamferMasks2D.CHESSKNIGHT;
	/** flag to select float result */
	private static boolean resultAsFloat = true;
	/** flag to select to normalize the weights */
	private static boolean normalize = true;
	/**
	 * Called at the beginning of the process to know if the plugin can be run
	 * with current image, and at the end to finalize.
	 */
	public int setup( String arg, ImagePlus imp )
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
		if( !BinaryImages.isBinaryImage( imp ) )
		{
			IJ.error( "Interactive Geodesic Distance Map", "Input image is not"
					+ " binary (8-bit with only 0 or 255 values)" );
			return DONE;
		}

		// Store user data
		this.imagePlus = imp;
		this.baseImage = imp.getProcessor().duplicate();
		this.pfr = pfr;

		/** ROI listener to update plugin when new ROIs are added */
		this.listener = new RoiListener() {
			@Override
			public void roiModified( ImagePlus imp, int id ) {

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
		gd = new NonBlockingGenericDialog( "Interactive Geodesic "
						+ "Distance Map" );
		// Set Chessknight weights as default
		gd.addChoice( "Distances", ChamferMasks2D.getAllLabels(),
				chamferChoice.toString() );
		String[] outputTypes = new String[] { "32 bits", "16 bits" };
		gd.addChoice( "Output Type", outputTypes,
				outputTypes[ resultAsFloat ? 0:1 ] );
		gd.addCheckbox( "Normalize weights", normalize );
		gd.addPreviewCheckbox( pfr );
		gd.addDialogListener(this);
		previewing = true;
		gd.addHelp( "http://imagej.net/MorphoLibJ#Utilities_for_binary_images" );
		gd.showDialog();
		previewing = false;

		if (gd.wasCanceled())
			return DONE;

		// identify which weights should be used
		chamferChoice = ChamferMasks2D.fromLabel(gd.getNextChoice());
		resultAsFloat = gd.getNextChoiceIndex() == 0;
		normalize = gd.getNextBoolean();

		return flags;
	}

	/**
	 * Called when a dialog widget has been modified: recomputes option values
	 * from dialog content.
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent evt)
	{
		// set up current parameters
		chamferChoice = ChamferMasks2D.fromLabel(gd.getNextChoice());
		resultAsFloat = gd.getNextChoiceIndex() == 0;
		normalize = gd.getNextBoolean();
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

		// Execute core of the plugin
		Roi roi = imagePlus.getRoi();
		ChamferMask2D chamferMask = chamferChoice.getMask();
		result = process(image, roi, chamferMask, resultAsFloat, normalize);

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
			double valMax = result.getMax();
			// values need to be adjusted to the input image type
			for (int i = 0; i < image.getPixelCount(); i++)
			{
				image.set(i, (int) (255 * result.getf(i) / valMax));
			}
			// Copy LUT
			image.setLut( result.getLut() );
			image.resetMinAndMax();
			if (image.isInvertedLut())
				image.invertLut();
		}

		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime( "Interactive Geodesic Distance Map",
				t1 - t0, imagePlus );
	}

	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the white phase.
	 *
	 * @param mask
	 *            the binary mask image that will constrain the propagation
	 * @param roi
	 *            the roi to define the marker image
	 * @param chamferMask
	 *            the chamfer mask used for computing distances
	 * @param floatProcess
	 *            specifies whether algorithm should use floating-point or
	 *            integer computation
	 * @param normalize
	 *            specifies whether the resulting distance map should be
	 *            normalized
	 * @return geodesic distance map image
	 */
	public ImageProcessor process( ImageProcessor mask, Roi roi,
			ChamferMask2D chamferMask, boolean floatProcess, boolean normalize)
	{
		if( mask == null || imagePlus == null || baseImage == null)
		{
			IJ.showMessage( "Please run the plugin with an image open." );
			return null;
		}

		if( chamferMask == null )
		{
			IJ.showMessage( "Weights not specified" );
			return null;
		}

		if( roi == null )
		{
			IJ.showMessage( "Please define the markers using for example "
					+ "the point selection tool." );
			return null;
		}
		// Create marker image from ROI
		ByteProcessor marker = new ByteProcessor( mask.getWidth(),
				mask.getHeight() );
		marker.setColor( java.awt.Color.WHITE );
		marker.draw( roi );

		// Initialize calculator
		GeodesicDistanceTransform algo;
		if (floatProcess)
			algo = new GeodesicDistanceTransformFloat(chamferMask, normalize);
		else
			algo = new GeodesicDistanceTransformShort(chamferMask, normalize);

		DefaultAlgoListener.monitor( algo );

		// Compute distance on specified images
		ImageProcessor result = algo.geodesicDistanceMap( marker, mask );

		// setup display options
		double maxVal = result.getMax();
		result.setLut( createFireLUT( maxVal ) );

		// create result image
		return result;
	}
	
	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the white phase.
	 *
	 * @param mask
	 *            the binary mask image that will constrain the propagation
	 * @param roi
	 * 			  the roi to define the marker image
	 * @param weights
	 *            the set of chamfer weights for computing distances
	 * @param normalize
	 *            specifies whether the resulting distance map should be
	 *            normalized
	 * @return geodesic distance map image
	 */
	@Deprecated
	public ImageProcessor process( ImageProcessor mask, Roi roi,
			float[] weights, boolean normalize)
	{
		if( mask == null || imagePlus == null || baseImage == null)
		{
			IJ.showMessage( "Please run the plugin with an image open." );
			return null;
		}

		if( weights == null )
		{
			IJ.showMessage( "Weights not specified" );
			return null;
		}

		if( roi == null )
		{
			IJ.showMessage( "Please define the markers using for example "
					+ "the point selection tool." );
			return null;
		}
		// Create marker image from ROI
		ByteProcessor marker = new ByteProcessor( mask.getWidth(),
				mask.getHeight() );
		marker.setColor( java.awt.Color.WHITE );
		marker.draw( roi );

		// Initialize calculator
		GeodesicDistanceTransform algo;
		algo = new GeodesicDistanceTransformFloat( ChamferMask2D.fromWeights(weights), normalize);

		DefaultAlgoListener.monitor( algo );

		// Compute distance on specified images
		ImageProcessor result = algo.geodesicDistanceMap( marker, mask );

		// setup display options
		double maxVal = result.getMax();
		result.setLut( createFireLUT( maxVal ) );

		// create result image
		return result;
	}

	/**
	 * Computes the distance propagated from the boundary of the white
	 * particles, within the white phase.
	 *
	 * @param mask
	 *            the binary mask image that will constrain the propagation
	 * @param roi
	 * 			  the roi to define the marker image
	 * @param weights
	 *            the set of chamfer weights for computing distances
	 * @param normalize
	 *            specifies whether the resulting distance map should be
	 *            normalized
	 * @return geodesic distance map image
	 */
	@Deprecated
	public ImageProcessor process( ImageProcessor mask, Roi roi,
			short[] weights, boolean normalize)
	{
		if( mask == null || imagePlus == null || baseImage == null)
		{
			IJ.showMessage( "Please run the plugin with an image open." );
			return null;
		}

		if( weights == null )
		{
			IJ.showMessage( "Weights not specified" );
			return null;
		}

		if( roi == null )
		{
			IJ.showMessage( "Please define the markers using for example "
					+ "the point selection tool." );
			return null;
		}
		// Create marker image from ROI
		ByteProcessor marker = new ByteProcessor( mask.getWidth(),
				mask.getHeight() );
		marker.setColor( java.awt.Color.WHITE );
		marker.draw( roi );

		// Initialize calculator
		GeodesicDistanceTransform algo;
		algo = new GeodesicDistanceTransformShort( ChamferMask2D.fromWeights(weights), normalize);

		DefaultAlgoListener.monitor( algo );

		// Compute distance on specified images
		ImageProcessor result = algo.geodesicDistanceMap( marker, mask );

		// setup display options
		double maxVal = result.getMax();
		result.setLut( createFireLUT( maxVal ) );

		// create result image
		return result;
	}

	/**
	 * Create fire look-up table
	 * @param maxVal maximum intensity value
	 * @return fire look-up table
	 */
	private LUT createFireLUT(double maxVal)
	{
		byte[][] lut = ColorMaps.createFireLut(256);
		byte[] red = new byte[256];
		byte[] green = new byte[256];
		byte[] blue = new byte[256];
		for (int i = 0; i < 256; i++)
		{
			red[i] 		= lut[i][0];
			green[i] 	= lut[i][1];
			blue[i] 	= lut[i][2];
		}
		IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);
		return new LUT(cm, 0, maxVal);
	}

	private static String createResultImageName(ImagePlus baseImage)
	{
		return baseImage.getShortTitle() + "-geoddist";
	}
}
