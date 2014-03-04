package inra.ijpb.plugins;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.image.ColorModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.plugin.PlugIn;
import inra.ijpb.binary.ConnectedComponents;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.util.ColorMaps;
import inra.ijpb.util.ColorMaps.CommonLabelMaps;
import inra.ijpb.watershed.Watershed;

/**
 * Plugin to perform automatic segmentation of 2D and 3D images 
 * based on morphological operations, mainly extended minima
 * and watershed transforms.
 * 
 * References:
 * [1] Soille, P., Morphological Image Analysis: Principles and
 *     Applications, Springer-Verlag, 1999, pp. 170-171.
 * 
 * @author Ignacio Arganda-Carreras
 *
 */
public class MorphologicalSegmentation implements PlugIn {

	/** main GUI window */
	private CustomWindow win;
	
	/** original input image */
	ImagePlus inputImage = null;
	/** image to be displayed in the GUI */
	ImagePlus displayImage = null;
	
	/** segmentation result image */
	ImagePlus resultImage = null;		
		
	/** segmentation panel */
	JPanel segmentationPanel = new JPanel();
	/** display panel */
	JPanel displayPanel = new JPanel();
	
	/** parameters panel (segmentation + display options) */
	JPanel paramsPanel = new JPanel();
	
	/** main panel */
	Panel all = new Panel();
	
	/** segmentation button */
	JButton segmentButton;
	/** toggle overlay button */
	JButton overlayButton;
	JPanel overlayPanel = new JPanel();
	
	/** display segmentation result button */
	JButton resultButton;	
	/** result display options */
	String[] resultDisplayOption = new String[]{ "Catchment basins", "Overlayed dams", "Watershed lines" };
	/** result display combo box */
	JComboBox resultDisplayList;
	/** result display panel */
	JPanel resultDisplayPanel = new JPanel();
	
	/** flag to display the overlay image */
	private boolean showColorOverlay;
	
	/** extended regional minima dynamic panel */
	JPanel dynamicPanel = new JPanel();
	/** extended regional minima dynamic label */
	JLabel dynamicLabel;
	/** extended regional minima dynamic text field */
	JTextField dynamicText;
	
	/** advanced options panel */
	JPanel advancedOptionsPanel = new JPanel();
	/** checkbox to enable/disable the advanced options */
	JCheckBox advancedOptionsCheckBox;
	/** flag to select/deselect the advanced options */
	private boolean selectAdvancedOptions = false;
	
	/** checkbox to select the use of morphological gradient */
	JCheckBox gradientCheckBox;
	/** flag to apply morphological gradient to the input image */
	private boolean applyGradient = false;
	/** gradient panel */
	JPanel gradientPanel = new JPanel();
	
	/** connectivity choice */
	JPanel connectivityPanel = new JPanel();
	/** connectivity label */
	JLabel connectivityLabel;
	/** connectivity list of options (6 and 26) */
	String[] connectivityOptions = new String[]{ "6", "26" };
	/** connectivity combo box */
	JComboBox connectivityList;
	
	/** checkbox to choose the priority queue watershed method */
	JCheckBox queueCheckBox;
	/** flag to use a priority queue in the watershed transform */
	private boolean usePriorityQueue = true;
	/** priority queue panel */
	JPanel queuePanel = new JPanel();
	
	/** executor service to launch threads for the plugin methods and events */
	final ExecutorService exec = Executors.newFixedThreadPool(1);
	
	/**
	 * Custom window to define the plugin GUI
	 */
	private class CustomWindow extends StackWindow
	{
		/**
		 * serial version uid
		 */
		private static final long serialVersionUID = -6201855439028581892L;
						
		/**
		 * Listener for the GUI buttons
		 */
		private ActionListener listener = new ActionListener() {

			@Override
			public void actionPerformed( final ActionEvent e ) {
				//final String command = e.getActionCommand();
				
				// listen to the buttons on separate threads not to block
				// the event dispatch thread
				exec.submit(new Runnable() {
													
					public void run()
					{
						if( e.getSource() == segmentButton )
						{
							runSegmentation();						
						}						
						else if( e.getSource() == overlayButton )
						{
							toggleOverlay();						
						}
						else if( e.getSource() == resultButton )
						{
							showResult();						
						}
						else if( e.getSource() == advancedOptionsCheckBox )
						{
							selectAdvancedOptions = !selectAdvancedOptions;
							enableAdvancedOptions( selectAdvancedOptions );
						}
					}

					
				});
			}
			
		};
		
		
		/**
		 * Construct the plugin window
		 * 
		 * @param imp input image
		 */
		CustomWindow( ImagePlus imp )
		{
			super(imp, new ImageCanvas(imp));
			
			final ImageCanvas canvas = (ImageCanvas) getCanvas();
			
			// Zoom in if image is too small
			while(ic.getWidth() < 512 && ic.getHeight() < 512)
				IJ.run( imp, "In","" );
			
			setTitle( "Morphological Segmentation" );
			
			// regional minima dynamic value
			dynamicLabel = new JLabel( "Dynamic" );
			dynamicLabel.setToolTipText( "Extended minima dynamic" );
			dynamicText = new JTextField( "10", 5 );
			dynamicPanel.add( dynamicLabel );
			dynamicPanel.add( dynamicText );
			dynamicPanel.setToolTipText( "Extended minima dynamic" );				
				
			// advanced options (connectivity + priority queue choices)
			advancedOptionsCheckBox = new JCheckBox( "Advanced options", selectAdvancedOptions );
			advancedOptionsCheckBox.setToolTipText( "Enable advanced options" );
			advancedOptionsCheckBox.addActionListener( listener );
			
			// gradient
			gradientCheckBox = new JCheckBox( "Apply morphological gradient", applyGradient );
			gradientCheckBox.setToolTipText( "Select to apply morphological gradient to input image");
			gradientPanel.add( gradientCheckBox );
			
			// connectivity
			connectivityList = new JComboBox( connectivityOptions );
			connectivityList.setToolTipText( "Voxel connectivity to use" );
			connectivityLabel = new JLabel( "Connectivity" );
			connectivityPanel.add( connectivityLabel );
			connectivityPanel.add( connectivityList );
			connectivityPanel.setToolTipText( "Voxel connectivity to use" );
			
			// use priority queue option
			queueCheckBox = new JCheckBox( "Use priority queue", usePriorityQueue );
			queueCheckBox.setToolTipText( "Check to use a priority queue in the watershed transform" );
			queuePanel.add( queueCheckBox );
			
			enableAdvancedOptions( selectAdvancedOptions );
			
			// add components to advanced options panel
			GridBagLayout advancedOptionsLayout = new GridBagLayout();
			GridBagConstraints advancedOptoinsConstraints = new GridBagConstraints();
			advancedOptoinsConstraints.anchor = GridBagConstraints.NORTHEAST;
			advancedOptoinsConstraints.fill = GridBagConstraints.HORIZONTAL;
			advancedOptoinsConstraints.gridwidth = 1;
			advancedOptoinsConstraints.gridheight = 1;
			advancedOptoinsConstraints.gridx = 0;
			advancedOptoinsConstraints.gridy = 0;
			advancedOptionsPanel.setLayout( advancedOptionsLayout );
			
			advancedOptionsPanel.add( gradientPanel, advancedOptoinsConstraints );
			advancedOptoinsConstraints.gridy++;
			advancedOptionsPanel.add( connectivityPanel, advancedOptoinsConstraints );
			advancedOptoinsConstraints.gridy++;			
			advancedOptionsPanel.add( queuePanel, advancedOptoinsConstraints );
			
			advancedOptionsPanel.setBorder(BorderFactory.createTitledBorder(""));
			
			
			// Segmentation button
			segmentButton = new JButton( "Segment" );
			segmentButton.setToolTipText( "Run the morphological segmentation" );
			segmentButton.addActionListener( listener );
			
			// Overlay button
			overlayButton = new JButton( "Toggle overlay" );
			overlayButton.setEnabled( false );
			overlayButton.setToolTipText( "Toggle overlay with segmentation result" );
			overlayButton.addActionListener( listener );
			overlayPanel.add( overlayButton );
			
			showColorOverlay = false;
			
			// Result pannel
			resultDisplayList = new JComboBox( resultDisplayOption );
			resultDisplayList.setEnabled( false );
			resultDisplayList.setToolTipText( "Select how to display segmentation results" );
			resultButton = new JButton( "Show" );
			resultButton.setEnabled( false );
			resultButton.setToolTipText( "Show segmentation result in new window" );
			resultButton.addActionListener( listener );
			resultDisplayPanel.add( resultDisplayList );
			resultDisplayPanel.add( resultButton );
			

			// Segmentation panel
			segmentationPanel.setBorder( BorderFactory.createTitledBorder( "Segmentation" ) );
			GridBagLayout segmentationLayout = new GridBagLayout();
			GridBagConstraints segmentationConstraints = new GridBagConstraints();
			segmentationConstraints.anchor = GridBagConstraints.NORTHWEST;
			segmentationConstraints.fill = GridBagConstraints.HORIZONTAL;
			segmentationConstraints.gridwidth = 1;
			segmentationConstraints.gridheight = 1;
			segmentationConstraints.gridx = 0;
			segmentationConstraints.gridy = 0;
			segmentationConstraints.insets = new Insets(5, 5, 6, 6);
			segmentationPanel.setLayout( segmentationLayout );						
						
			segmentationPanel.add( dynamicPanel, segmentationConstraints );
			segmentationConstraints.gridy++;
			segmentationPanel.add( advancedOptionsCheckBox, segmentationConstraints );
			segmentationConstraints.gridy++;
			segmentationPanel.add( advancedOptionsPanel, segmentationConstraints );			
			segmentationConstraints.gridy++;
			segmentationPanel.add( segmentButton, segmentationConstraints );
			segmentationConstraints.gridy++;
			
			// Display panel
			displayPanel.setBorder( BorderFactory.createTitledBorder( "Display" ) );
			GridBagLayout displayLayout = new GridBagLayout();
			GridBagConstraints displayConstraints = new GridBagConstraints();
			displayConstraints.anchor = GridBagConstraints.NORTHWEST;
			displayConstraints.fill = GridBagConstraints.HORIZONTAL;
			displayConstraints.gridwidth = 1;
			displayConstraints.gridheight = 1;
			displayConstraints.gridx = 0;
			displayConstraints.gridy = 0;
			displayConstraints.insets = new Insets(5, 5, 6, 6);
			displayPanel.setLayout( displayLayout );					
			
			displayPanel.add( overlayPanel, displayConstraints );
			displayConstraints.gridy++;
			displayPanel.add( resultDisplayPanel, displayConstraints );
			displayConstraints.gridy++;
			
			// Parameter panel (left side of the GUI, including training and options)
			GridBagLayout paramsLayout = new GridBagLayout();
			GridBagConstraints paramsConstraints = new GridBagConstraints();
			paramsPanel.setLayout( paramsLayout );
			paramsConstraints.anchor = GridBagConstraints.NORTHWEST;
			paramsConstraints.fill = GridBagConstraints.HORIZONTAL;
			paramsConstraints.gridwidth = 1;
			paramsConstraints.gridheight = 1;
			paramsConstraints.gridx = 0;
			paramsConstraints.gridy = 0;
			paramsPanel.add( segmentationPanel, paramsConstraints);
			paramsConstraints.gridy++;
			paramsPanel.add( displayPanel, paramsConstraints);
			paramsConstraints.gridy++;
			paramsConstraints.insets = new Insets( 5, 5, 6, 6 );
			
			// main panel (including parameters panel and canvas)
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints allConstraints = new GridBagConstraints();
			all.setLayout(layout);

			// put parameter panel in place
			allConstraints.anchor = GridBagConstraints.NORTHWEST;
			allConstraints.fill = GridBagConstraints.BOTH;
			allConstraints.gridwidth = 1;
			allConstraints.gridheight = 1;
			allConstraints.gridx = 0;
			allConstraints.gridy = 0;
			allConstraints.gridheight = 2;
			allConstraints.weightx = 0;
			allConstraints.weighty = 0;
			
			all.add( paramsPanel, allConstraints );
									
			// put canvas in place
			allConstraints.gridx++;
			allConstraints.weightx = 1;
			allConstraints.weighty = 1;
			allConstraints.gridheight = 1;
			all.add( canvas, allConstraints );
			
			allConstraints.gridy++;
			allConstraints.weightx = 1;
			allConstraints.weighty = 1;
			
			// if the input image is 3d, put the
			// slice selectors in place
			if( null != super.sliceSelector )
			{
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
			
			// add especial listener if the input image is a stack
			if(null != sliceSelector)
			{
				// add adjustment listener to the scroll bar
				sliceSelector.addAdjustmentListener(new AdjustmentListener() 
				{

					public void adjustmentValueChanged(final AdjustmentEvent e) {
						exec.submit(new Runnable() {
							public void run() {							
								if(e.getSource() == sliceSelector)
								{
									//IJ.log("moving scroll");
									displayImage.killRoi();
									if( showColorOverlay )
									{
										updateResultOverlay();
										displayImage.updateAndDraw();
									}
								}

							}							
						});
					}
				});

				// mouse wheel listener to update the rois while scrolling
				addMouseWheelListener(new MouseWheelListener() {

					@Override
					public void mouseWheelMoved(final MouseWheelEvent e) {

						exec.submit(new Runnable() {
							public void run() 
							{
								//IJ.log("moving scroll");
								displayImage.killRoi();
								if( showColorOverlay )
								{
									updateResultOverlay();
									displayImage.updateAndDraw();
								}
							}
						});

					}
				});

				// key listener to repaint the display image and the traces
				// when using the keys to scroll the stack
				KeyListener keyListener = new KeyListener() {

					@Override
					public void keyTyped(KeyEvent e) {}

					@Override
					public void keyReleased(final KeyEvent e) {
						exec.submit(new Runnable() {
							public void run() 
							{
								if(e.getKeyCode() == KeyEvent.VK_LEFT ||
										e.getKeyCode() == KeyEvent.VK_RIGHT ||
										e.getKeyCode() == KeyEvent.VK_LESS ||
										e.getKeyCode() == KeyEvent.VK_GREATER ||
										e.getKeyCode() == KeyEvent.VK_COMMA ||
										e.getKeyCode() == KeyEvent.VK_PERIOD)
								{
									//IJ.log("moving scroll");
									displayImage.killRoi();
									if( showColorOverlay )
									{
										updateResultOverlay();
										displayImage.updateAndDraw();
									}
								}
							}
						});

					}

					@Override
					public void keyPressed(KeyEvent e) {}
				};
				// add key listener to the window and the canvas
				addKeyListener(keyListener);
				canvas.addKeyListener(keyListener);

			}		
		}
		
		/**
		 * Overwrite windowClosing to display the input image after closing GUI
		 */
		public void windowClosing( WindowEvent e ) 
		{		
			inputImage.getWindow().setVisible( true );			
			super.windowClosing(e);		
		}
		
		
	}// end class CustomWindow
	
	/**
	 * Update the overlay in the display image based on 
	 * the current result and slice
	 */
	void updateResultOverlay() 
	{
		if( null != resultImage )
		{
			int slice = displayImage.getCurrentSlice();
			ImageRoi roi = new ImageRoi(0, 0, resultImage.getImageStack().getProcessor( slice ) );
			roi.setOpacity( 1.0/3.0 );
			displayImage.setOverlay( new Overlay( roi ) );
		}
	}
	
	/**
	 * Toggle overlay with segmentation results (if any)
	 */
	void toggleOverlay()
	{
		showColorOverlay = !showColorOverlay;

		if ( showColorOverlay )		
			updateResultOverlay();
		else
			displayImage.setOverlay( null );
		displayImage.updateAndDraw();
	}
	
	/**
	 * Show segmentation result in a new window (it exists)
	 */
	void showResult()
	{
		if( null != this.resultImage )
		{
			final String displayOption = (String) resultDisplayList.getSelectedItem();
			
			// options: "Catchment basins", "Overlayed dams", "Watershed lines"
			if( displayOption.equals( "Catchment basins" ) )
			{			
				ImagePlus watershedResult = resultImage.duplicate();
				watershedResult.setTitle( "Catchment-basins-" + this.inputImage.getTitle() );
				watershedResult.setSlice( this.displayImage.getSlice() );
				watershedResult.show();
			}
			else if( displayOption.equals( "Overlayed dams" ) )
			{
				final ImagePlus lines = getWatershedLines( resultImage );
				final ImagePlus overlayed = BinaryOverlayPlugin.binaryOverlay( inputImage, lines, Color.red ) ;
				overlayed.setTitle( "Overlayed-dams" + this.inputImage.getTitle() );
				overlayed.setSlice( this.displayImage.getSlice() );
				overlayed.show();
			}
			else if( displayOption.equals( "Watershed lines" ) )
			{
				final ImagePlus lines = getWatershedLines( resultImage );
				lines.setTitle( "Watershed-lines-" + this.inputImage.getTitle() );
				lines.setSlice( this.displayImage.getSlice() );
				lines.show();
			}
		}
	}
	
	/**
	 * Get the watershed lines out of the result catchment basins image
	 * @param labels labeled catchment basins image
	 * @return binary image with the watershed lines
	 */
	ImagePlus getWatershedLines( ImagePlus labels )
	{
		final ImagePlus lines = labels.duplicate();
		IJ.setThreshold( lines, 0, 0 );
		IJ.run(lines, "Convert to Mask", "method=Default background=Light" );
		return lines;
	}
	
	/**
	 * Run morphological segmentation pipeline
	 */
	private void runSegmentation() 
	{
		// read dynamic
		double dynamic;
		
		int connectivity = Integer.parseInt( (String) connectivityList.getSelectedItem() );
		
		try{
			dynamic = Double.parseDouble( this.dynamicText.getText() );
		}
		catch( NullPointerException ex )
		{
			IJ.error( "Error", "Missing dynamic value" );
			return;
		}
		catch( NumberFormatException ex )
		{
			IJ.error( "Error", "Dynamic value must be a number" );
			return;
		}
		
		// read gradient flag
		this.applyGradient = gradientCheckBox.isSelected();
		
		// read priority queue flag
		this.usePriorityQueue = queueCheckBox.isSelected();
		
		// disable parameter panel
		setParamsEnabled( false );
		
		ImageStack image = this.inputImage.getImageStack();
		
		final long start = System.currentTimeMillis();
		
		if( applyGradient )
		{
			final long t1 = System.currentTimeMillis();
			IJ.log( "Applying morphological gradient to input image..." );
			
			Strel3D strel = Strel3D.Shape.CUBE.fromRadius( 1 );
			image = Morphology.gradient( image, strel );
			//(new ImagePlus("gradient", image) ).show();
			
			final long t2 = System.currentTimeMillis();
			IJ.log( "Morphological gradient took " + (t2-t1) + " ms.");
		}
				
		IJ.log( "Running extended minima with dynamic value " + (int)dynamic + "..." );
		final long step0 = System.currentTimeMillis();				
		
		// Run extended minima
		ImageStack regionalMinima = MinimaAndMaxima3D.extendedMinima( image, (int)dynamic, connectivity );
		
		final long step1 = System.currentTimeMillis();		
		IJ.log( "Regional minima took " + (step1-step0) + " ms.");
		
		IJ.log( "Imposing regional minima on original image (connectivity = " + connectivity + ")..." );
						
		// Impose regional minima over the original image
		ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima( image, regionalMinima, connectivity );
		
		final long step2 = System.currentTimeMillis();
		IJ.log( "Imposition took " + (step2-step1) + " ms." );
						
		IJ.log( "Labeling regional minima..." );
		
		// Label regional minima
		ImageStack labeledMinima = ConnectedComponents.computeLabels( regionalMinima, connectivity, 32 );
		
		final long step3 = System.currentTimeMillis();
		IJ.log( "Connected components took " + (step3-step2) + " ms." );
		
		// Apply watershed		
		IJ.log("Running watershed...");
		
		ImageStack resultStack = Watershed.computeWatershed( imposedMinima, labeledMinima, connectivity, usePriorityQueue, true );
		resultImage = new ImagePlus( "watershed", resultStack );
		resultImage.setCalibration( this.inputImage.getCalibration() );
		
		final long end = System.currentTimeMillis();
		IJ.log( "Watershed 3d took " + (end-step3) + " ms.");
		IJ.log( "Whole plugin took " + (end-start) + " ms.");
		
		// Adjust min and max values to display
		Images3D.optimizeDisplayRange( resultImage );
		
		byte[][] colorMap = CommonLabelMaps.fromLabel( CommonLabelMaps.SPECTRUM.getLabel() ).computeLut(255, true);;
		ColorModel cm = ColorMaps.createColorModel(colorMap, Color.BLACK);
		resultImage.getProcessor().setColorModel(cm);
		resultImage.getImageStack().setColorModel(cm);
		resultImage.updateAndDraw();
		
		// display result overlaying the input image
		updateResultOverlay();
		showColorOverlay = true;
		
		// enable parameter panel
		setParamsEnabled( true );				
	}
	
	/**
	 * Enable/disable all components in the parameter panel
	 * 
	 * @param enabled boolean flag to enable/disable components
	 */
	void setParamsEnabled( boolean enabled )
	{
		this.dynamicText.setEnabled( enabled );
		this.advancedOptionsCheckBox.setEnabled( enabled );
		this.segmentButton.setEnabled( enabled );
		this.overlayButton.setEnabled( enabled );
		this.resultButton.setEnabled( enabled );
		this.resultDisplayList.setEnabled( enabled );
		if( selectAdvancedOptions )
			enableAdvancedOptions( enabled );
	}
	
	/**
	 * Enable/disable advanced options components
	 * 
	 * @param enabled flag to enable/disable components
	 */
	void enableAdvancedOptions( boolean enabled )
	{
		this.connectivityLabel.setEnabled( enabled );
		this.connectivityList.setEnabled( enabled );
		this.queueCheckBox.setEnabled( enabled );
		this.gradientCheckBox.setEnabled( enabled );
	}
	
	@Override
	public void run(String arg0) 
	{
		// get current image
		if (null == WindowManager.getCurrentImage())
		{
			inputImage = IJ.openImage();
			if (null == inputImage) return; // user canceled open dialog
		}
		else
			inputImage = WindowManager.getCurrentImage();
		
		displayImage = inputImage.duplicate();
		displayImage.setTitle("Morphological Segmentation");
		displayImage.setSlice( inputImage.getSlice() );
		
		// hide input image (to avoid accidental closing)
		inputImage.getWindow().setVisible( false );
		
		// Build GUI
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						win = new CustomWindow( displayImage );
						win.pack();
					}
				});
		
	}

}
