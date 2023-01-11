/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;
import ij.gui.Toolbar;
import ij.plugin.PlugIn;
import inra.ijpb.label.LabelImages;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.Strel3D;

/**
 * Plugin for the manual edition of label images (2D and 3D).
 * @author iarganda
 *
 */
public class LabelEdition implements PlugIn 
{			
	/** main GUI window */
	private CustomWindow win;
	
	/** original input image */
	ImagePlus inputImage = null;
	/** copy of the input image stack */
	ImageStack inputStackCopy = null;

	/** image to be displayed in the GUI */
	ImagePlus displayImage = null;
	
	/** flag to indicate 2D input image */
	boolean inputIs2D = false;
	
	/** executor service to launch threads for the plugin methods and events */
	final ExecutorService exec = Executors.newFixedThreadPool( 1 );

	// Button panel components
	/** button to merge selected labels */
	JButton mergeButton = null;
	/** button to dilate all labels */
	JButton dilateButton = null;
	/** button to erode all labels */
	JButton erodeButton = null;
	/** button to open all labels */
	JButton openButton = null;
	/** button to close all labels */
	JButton closeButton = null;
	/** button to remove selected label(s) */
	JButton removeSelectedButton = null;
	/** button to remove largest label */
	JButton removeLargestButton = null;
	/** button to remove all border labels */
	JButton removeBorderLabelsButton = null;
	/** button to apply label area opening */
	JButton sizeOpeningButton = null;
	/** button to reset image to initial form */
	JButton resetButton = null;
	/** button to finish plugin and exit */
	JButton doneButton = null;

	/** panel to include all buttons */
	JPanel buttonsPanel = new JPanel();
	
	/** main panel */
	Panel all = new Panel();
	
	/**
	 * Custom window to define the plugin GUI
	 */
	private class CustomWindow extends StackWindow
	{

		/**
		 * Generated serial version UID
		 */
		private static final long serialVersionUID = 7356632113911531536L;

		/**
		 * Listener for the GUI buttons
		 */
		private ActionListener listener = new ActionListener() {

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				// listen to the buttons on separate threads not to block
				// the event dispatch thread
				exec.submit(new Runnable() {

					public void run()
					{
						// Merge button
						if( e.getSource() == mergeButton )
						{
							setButtonsEnabled( false );
							mergeLabels( );
							setButtonsEnabled( true );
						}
						else if( e.getSource() == dilateButton )
						{
							setButtonsEnabled( false );
							dilateLabels( );
							setButtonsEnabled( true );
						}
						else if( e.getSource() == erodeButton )
						{
							setButtonsEnabled( false );
							erodeLabels( );
							setButtonsEnabled( true );
						}
						else if( e.getSource() == openButton )
						{
							setButtonsEnabled( false );
							openLabels( );
							setButtonsEnabled( true );
						}
						else if( e.getSource() == closeButton )
						{
							setButtonsEnabled( false );
							closeLabels( );
							setButtonsEnabled( true );
						}
						else if( e.getSource() == removeSelectedButton )
						{
							setButtonsEnabled( false );
							removeSelectedLabels();
							setButtonsEnabled( true );
						}
						else if( e.getSource() == removeLargestButton )
						{
							setButtonsEnabled( false );
							removeLargestLabel();
							setButtonsEnabled( true );
						}
						else if( e.getSource() == removeBorderLabelsButton )
						{
							setButtonsEnabled( false );
							removeBorderLabels();
							setButtonsEnabled( true );
						}
						else if( e.getSource() == sizeOpeningButton )
						{
							setButtonsEnabled( false );
							labelSizeOpening();
							setButtonsEnabled( true );
						}
						else if( e.getSource() == resetButton )
						{
							resetLabels( );
						}
						else if( e.getSource() == doneButton )
						{
							showResult();
							win.windowClosing( null );
						}
					}
				});
			}
		};

		/**
		 * Custom window to create plugin GUI
		 * @param imp input label image (2d or 3d)
		 */
		public CustomWindow( ImagePlus imp ) 
		{
		
			super(imp, new ImageCanvas(imp));

			final ImageCanvas canvas = (ImageCanvas) getCanvas();

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			double screenWidth = screenSize.getWidth();
			double screenHeight = screenSize.getHeight();
			
			// Zoom in if image is too small
			while( 	ic.getWidth() < screenWidth/2 &&
				ic.getHeight() < screenHeight/2 &&
				ic.getMagnification() < 32.0 )			
			{
				final int canvasWidth = ic.getWidth();
				ic.zoomIn( 0, 0 );
				// check if canvas size changed (otherwise stop zooming)
				if( canvasWidth == ic.getWidth() )
				{
					ic.zoomOut(0, 0);
					break;
				}
		    }
			// Zoom out if canvas is too large
			while( ( ic.getWidth() > 0.75 * screenWidth ||
					ic.getHeight() > 0.75 * screenHeight ) &&
					ic.getMagnification() > 1/72.0 )
			{
				final int canvasWidth = ic.getWidth();
				ic.zoomOut( 0, 0 );
				// check if canvas size changed (otherwise stop zooming)
				if( canvasWidth == ic.getWidth() )
				{
					ic.zoomIn(0, 0);
					break;
				}
			}
			setTitle( "Label Edition" );

			mergeButton = new JButton( "Merge" );
			mergeButton.addActionListener( listener );
			mergeButton.setToolTipText( "Merge labels selected by point or "
					+ "freehand ROIs" );

			dilateButton = new JButton( "Dilate" );
			dilateButton.addActionListener( listener );
			dilateButton.setToolTipText( "Dilate all labels" );

			erodeButton = new JButton( "Erode" );
			erodeButton.addActionListener( listener );
			erodeButton.setToolTipText( "Erode all labels" );

			openButton = new JButton( "Open" );
			openButton.addActionListener( listener );
			openButton.setToolTipText( "Apply morphological opening to all "
					+ "labels" );

			closeButton = new JButton( "Close" );
			closeButton.addActionListener( listener );
			closeButton.setToolTipText( "Apply morphological closing to all "
					+ "labels" );

			removeSelectedButton = new JButton( "Remove selected" );
			removeSelectedButton.addActionListener( listener );
			removeSelectedButton.setToolTipText( "Remove selected label(s)" );

			removeLargestButton = new JButton( "Remove largest" );
			removeLargestButton.addActionListener( listener );
			removeLargestButton.setToolTipText( "Remove largest label" );

			removeBorderLabelsButton = new JButton( "Remove in border" );
			removeBorderLabelsButton.addActionListener( listener );
			removeBorderLabelsButton.setToolTipText( "Remove labels in the "
					+ "borders of the image" );

			sizeOpeningButton = new JButton( "Size opening" );
			sizeOpeningButton.addActionListener( listener );
			sizeOpeningButton.setToolTipText( "Remove labels under a certain"
					+ " size" );

			resetButton = new JButton( "Reset" );
			resetButton.addActionListener( listener );
			resetButton.setToolTipText( "Reset labels to initial state" );

			doneButton = new JButton( "Done" );
			doneButton.addActionListener( listener );
			doneButton.setToolTipText( "Close plugin and return result" );
			
			// Buttons panel (left side of the GUI)
			buttonsPanel.setBorder(
					BorderFactory.createTitledBorder( "Options" ) );
			GridBagLayout buttonsLayout = new GridBagLayout();
			GridBagConstraints buttonsConstraints = new GridBagConstraints();
			buttonsConstraints.anchor = GridBagConstraints.NORTHWEST;
			buttonsConstraints.fill = GridBagConstraints.HORIZONTAL;
			buttonsConstraints.gridwidth = 1;
			buttonsConstraints.gridheight = 1;
			buttonsConstraints.gridx = 0;
			buttonsConstraints.gridy = 0;
			buttonsConstraints.insets = new Insets( 5, 5, 6, 6 );
			buttonsPanel.setLayout( buttonsLayout );

			buttonsPanel.add( mergeButton, buttonsConstraints );
			buttonsConstraints.gridy++;
			buttonsPanel.add( dilateButton, buttonsConstraints );
			buttonsConstraints.gridy++;
			buttonsPanel.add( erodeButton, buttonsConstraints);
			buttonsConstraints.gridy++;
			buttonsPanel.add( openButton, buttonsConstraints );
			buttonsConstraints.gridy++;
			buttonsPanel.add( closeButton, buttonsConstraints);
			buttonsConstraints.gridy++;
			buttonsPanel.add( removeSelectedButton, buttonsConstraints);
			buttonsConstraints.gridy++;
			buttonsPanel.add( removeLargestButton, buttonsConstraints);
			buttonsConstraints.gridy++;
			buttonsPanel.add( removeBorderLabelsButton, buttonsConstraints);
			buttonsConstraints.gridy++;
			buttonsPanel.add( sizeOpeningButton, buttonsConstraints);
			buttonsConstraints.gridy++;
			buttonsPanel.add( resetButton, buttonsConstraints);
			buttonsConstraints.gridy++;
			buttonsPanel.add( doneButton, buttonsConstraints );
			buttonsConstraints.gridy++;
			
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints allConstraints = new GridBagConstraints();
			all.setLayout(layout);

			allConstraints.anchor = GridBagConstraints.NORTHWEST;
			allConstraints.fill = GridBagConstraints.BOTH;
			allConstraints.gridwidth = 1;
			allConstraints.gridheight = 2;
			allConstraints.gridx = 0;
			allConstraints.gridy = 0;
			allConstraints.weightx = 0;
			allConstraints.weighty = 0;

			all.add( buttonsPanel, allConstraints );
			
			allConstraints.gridx++;
			allConstraints.weightx = 1;
			allConstraints.weighty = 1;
			allConstraints.gridheight = 1;
			all.add( canvas, allConstraints );
			
			allConstraints.gridy++;
			allConstraints.weightx = 0;
			allConstraints.weighty = 0;
			// if the input image is 3d, put the
			// slice selectors in place
			if( null != super.sliceSelector )
			{
				sliceSelector.setValue( inputImage.getCurrentSlice() );
				displayImage.setSlice( inputImage.getCurrentSlice() );

				all.add( super.sliceSelector, allConstraints );

				if( null != super.zSelector )
					all.add( super.zSelector, allConstraints );
				if( null != super.tSelector )
					all.add( super.tSelector, allConstraints );
				if( null != super.cSelector )
					all.add( super.cSelector, allConstraints );

			}
			allConstraints.gridy--;

			GridBagLayout wingb = new GridBagLayout();
			GridBagConstraints winc = new GridBagConstraints();
			winc.anchor = GridBagConstraints.NORTHWEST;
			winc.fill = GridBagConstraints.BOTH;
			winc.weightx = 1;
			winc.weighty = 1;
			setLayout( wingb );
			add( all, winc );
			
			// Fix minimum size to the preferred size at this point
			pack();
			setMinimumSize( getPreferredSize() );
		}// end CustomWindow constructor
		
		/**
		 * Overwrite windowClosing to display the result image after closing
		 * the GUI and shut down the executor service
		 */
		@Override
		public void windowClosing( WindowEvent e )
		{							
			super.windowClosing( e );

			// show original input image as well
			inputImage.changes = false;
			inputImage.getWindow().setVisible( true );

			// remove listeners
			mergeButton.removeActionListener( listener );
			dilateButton.removeActionListener( listener );
			erodeButton.removeActionListener( listener );
			doneButton.removeActionListener( listener );

			// shut down executor service
			exec.shutdownNow();
		}
		
		/**
		 * Merge labels of current image that have been selected by either point
		 * or freehand ROIs.
		 */
		void mergeLabels()
		{
			LabelImages.mergeLabels( displayImage, displayImage.getRoi(),
					true );
			displayImage.deleteRoi();
			displayImage.updateAndDraw();
		}

		/**
		 * Dilate labels using a square/cube of radius 1 as structuring element
		 */
		void dilateLabels()
		{
			if( inputIs2D )
			{
				displayImage.setProcessor( Morphology.dilation(
						displayImage.getProcessor(),
						Strel.Shape.SQUARE.fromRadius( 1 ) ) );
			}
			else
			{
				displayImage.setStack( Morphology.dilation(
						displayImage.getImageStack(),
						Strel3D.Shape.CUBE.fromRadius( 1 ) ) );
			}
			displayImage.updateAndDraw();
		}

		/**
		 * Erode labels using a square/cube of radius 1 as structuring element
		 */
		void erodeLabels()
		{
			if( inputIs2D )
			{
				displayImage.setProcessor( Morphology.erosion(
						displayImage.getProcessor(),
						Strel.Shape.SQUARE.fromRadius( 1 ) ) );
			}
			else
			{
				displayImage.setStack( Morphology.erosion(
						displayImage.getImageStack(),
						Strel3D.Shape.CUBE.fromRadius( 1 ) ) );
			}
			displayImage.updateAndDraw();
		}

		/**
		 * Open labels using a square/cube of radius 1 as structuring element
		 */
		void openLabels()
		{
			if( inputIs2D )
			{
				displayImage.setProcessor( Morphology.opening(
						displayImage.getProcessor(),
						Strel.Shape.SQUARE.fromRadius( 1 ) ) );
			}
			else
			{
				displayImage.setStack( Morphology.opening(
						displayImage.getImageStack(),
						Strel3D.Shape.CUBE.fromRadius( 1 ) ) );
			}
			displayImage.updateAndDraw();
		}

		/**
		 * Close labels using a square/cube of radius 1 as structuring element
		 */
		void closeLabels()
		{
			if( inputIs2D )
			{
				displayImage.setProcessor( Morphology.closing(
						displayImage.getProcessor(),
						Strel.Shape.SQUARE.fromRadius( 1 ) ) );
			}
			else
			{
				displayImage.setStack( Morphology.closing(
						displayImage.getImageStack(),
						Strel3D.Shape.CUBE.fromRadius( 1 ) ) );
			}
			displayImage.updateAndDraw();
		}

		/**
		 * Remove labels selected by point ROIs
		 */
		void removeSelectedLabels()
		{
			LabelImages.removeLabels( displayImage, displayImage.getRoi(),
					true );
			displayImage.deleteRoi();
			displayImage.updateAndDraw();
		}
		/**
		 * Remove largest label
		 */
		void removeLargestLabel()
		{
			LabelImages.removeLargestLabel( displayImage );
			displayImage.updateAndDraw();
		}
		/**
		 * Remove all labels in the border of the image
		 */
		void removeBorderLabels()
		{
			LabelImages.removeBorderLabels( displayImage );
			displayImage.updateAndDraw();
		}
		/**
		 * Remove labels under a certain size
		 */
		void labelSizeOpening()
		{
			String title = inputIs2D ? "Area Opening" : "Volume Opening";
			GenericDialog gd = new GenericDialog( title );
	        String label = inputIs2D ? "Min Pixel Number:" :
	        	"Min Voxel Number:";
	        gd.addNumericField( label, 100, 0 );
	        gd.showDialog();

	        // If cancel was clicked, do nothing
	        if ( gd.wasCanceled() )
	            return;

	        int nPixelMin = (int) gd.getNextNumber();

	        // Apply size opening
	        ImagePlus res = LabelImages.sizeOpening( displayImage, nPixelMin );
	        displayImage.setStack( res.getImageStack() );
	        displayImage.updateAndDraw();
		}

		/**
		 * Reset labels to original form
		 */
		void resetLabels()
		{
			displayImage.setStack( inputImage.getImageStack().duplicate() );
			displayImage.updateAndDraw();
		}
		/**
		 * Show edited result in a new window
		 */
		void showResult()
		{
			if( null != displayImage )
			{
				final ImagePlus result = displayImage.duplicate();
				result.setTitle( inputImage.getShortTitle() + "-edited" );
				result.setSlice( displayImage.getCurrentSlice() );
				result.setCalibration( inputImage.getCalibration() );
				result.show();
			}
		}
		/**
		 * Enable/disable all buttons
		 *
		 * @param enable flag to enable/disable buttons
		 */
		void setButtonsEnabled( boolean enable )
		{
			mergeButton.setEnabled( enable );
			erodeButton.setEnabled( enable );
			dilateButton.setEnabled( enable );
			openButton.setEnabled( enable );
			closeButton.setEnabled( enable );
			removeSelectedButton.setEnabled( enable );
			removeLargestButton.setEnabled( enable );
			removeBorderLabelsButton.setEnabled( enable );
			sizeOpeningButton.setEnabled( enable );
			resetButton.setEnabled( enable );
			doneButton.setEnabled( enable );
		}

	}// end CustomWindow class
	
	/**
	 * Plug-in run method
	 * 
	 * @param arg plug-in arguments
	 */
	public void run(String arg) 
	{
		if ( IJ.getVersion().compareTo("1.48a") < 0 )
		{
			IJ.error( "Label Edition", "ERROR: detected ImageJ version "
					+ IJ.getVersion() + ".\nLabel Edition requires"
					+ " version 1.48a or superior, please update ImageJ!" );
			return;
		}

		// get current image
		if ( null == WindowManager.getCurrentImage() )
		{
			inputImage = IJ.openImage();
			if ( null == inputImage )
				return; // user canceled open dialog
		}
		else
			inputImage = WindowManager.getCurrentImage();

		// Check if input image is a label image
		if( LabelImages.isLabelImageType( inputImage ) == false )
		{
			IJ.error( "Label Edition", "This plugin only works on"
				+ " label images.\nPlease convert it to 8, 16 or 32-bit." );
			return;
		}
		
		// select point tool for manual label merging
		Toolbar.getInstance().setTool( Toolbar.POINT );

		inputStackCopy = inputImage.getImageStack().duplicate();
		displayImage = new ImagePlus( inputImage.getTitle(), 
				inputStackCopy );
		displayImage.setTitle( "Label Edition" );
		displayImage.setSlice( inputImage.getCurrentSlice() );
		displayImage.setCalibration( inputImage.getCalibration() );

		// hide input image (to avoid accidental closing)
		inputImage.getWindow().setVisible( false );
		
		// set the 2D flag
		inputIs2D = inputImage.getImageStackSize() == 1;

		// correct Fiji error when the slices are read as frames
		if ( inputIs2D == false && 
				displayImage.isHyperStack() == false && 
				displayImage.getNSlices() == 1 )
		{
			// correct stack by setting number of frames as slices
			displayImage.setDimensions( 1, displayImage.getNFrames(), 1 );
		}

		// Build GUI
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						win = new CustomWindow( displayImage );
						win.pack();
					}
				});
	}	
}// end LabelEdition class
