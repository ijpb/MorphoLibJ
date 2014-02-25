package inra.ijpb.plugins;

import java.awt.Color;
import java.awt.FlowLayout;
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
import ij.process.ImageProcessor;
import inra.ijpb.binary.ConnectedComponents;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.util.ColorMaps;
import inra.ijpb.util.ColorMaps.CommonLabelMaps;
import inra.ijpb.watershed.WatershedTransform3D;

public class MorphologicalSegmentation implements PlugIn {

	/** GUI window */
	private CustomWindow win;
	
	/** original input image */
	ImagePlus inputImage = null;
	/** image to be displayed in the GUI */
	ImagePlus displayImage = null;
	
	/** segmentation result image */
	ImagePlus resultImage = null;		
		
	/** parameters panel */
	JPanel paramsPanel = new JPanel();
	/** main panel */
	Panel all = new Panel();
	
	/** segmentation button */
	JButton segmentButton;
	/** toggle overlay button */
	JButton overlayButton;
	/** create result button */
	JButton resultButton;
	
	/** flag to display the overlay image */
	private boolean showColorOverlay;
	
	/** extended regional minima dynamic panel */
	JPanel dynamicPanel = new JPanel();
	/** extended regional minima dynamic label */
	JLabel dynamicLabel;
	/** extended regional minima dynamic text field */
	JTextField dynamicText;
	
	/** connectivity choice */
	JPanel connectivityPanel = new JPanel();
	/** connectivity label */
	JLabel connectivityLabel;
	/** connectivity list of options (6 and 26) */
	String[] connectivityOptions = new String[]{ "6", "26" };
	/** connectivity combo box */
	JComboBox<String> connectivityList;
	
	/** checkbox to choose the priority queue watershed method */
	JCheckBox queueBox;
	/** flag to use a priority queue in the watershed transform */
	private boolean usePriorityQueue = true;
	/** priority queue panel */
	JPanel queuePanel = new JPanel();;
	
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
			
			setTitle( "Morphological Segmentation" );
			
			// regional minima dynamic value
			dynamicLabel = new JLabel( "Dynamic" );
			dynamicLabel.setToolTipText( "Extended minima dynamic" );
			dynamicText = new JTextField( "10", 5 );
			dynamicPanel.setLayout( new FlowLayout( FlowLayout.LEFT, 5, 5 ) );
			dynamicPanel.add( dynamicLabel );
			dynamicPanel.add( dynamicText );
			dynamicPanel.setToolTipText( "Extended minima dynamic" );				
				
			// connectivity
			connectivityList = new JComboBox<String>( connectivityOptions );
			connectivityList.setToolTipText( "Voxel connectivity to use" );
			connectivityLabel = new JLabel( "Connectivity" );
			connectivityPanel.add( connectivityLabel );
			connectivityPanel.add( connectivityList );
			connectivityPanel.setToolTipText( "Voxel connectivity to use" );
			
			// use priority queue option
			queueBox = new JCheckBox( "Use priority queue", usePriorityQueue );
			queueBox.setToolTipText( "Check to use a priority queue in the watershed transform" );
			queuePanel.add( queueBox );
			
			
			// Segmentation button
			segmentButton = new JButton( "Segment" );
			segmentButton.setToolTipText( "Run the morphological segmentation" );
			segmentButton.addActionListener( listener );
			
			// Overlay button
			overlayButton = new JButton( "Toggle overlay" );
			overlayButton.setEnabled( false );
			overlayButton.setToolTipText( "Toggle overlay with segmentation result" );
			overlayButton.addActionListener( listener );
			
			showColorOverlay = false;
			
			// Result button
			resultButton = new JButton( "Create result" );
			resultButton.setEnabled( false );
			resultButton.setToolTipText( "Show segmentation result in new window" );
			resultButton.addActionListener( listener );

			// Parameters panel (left side of the GUI)
			paramsPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
			GridBagLayout paramsLayout = new GridBagLayout();
			GridBagConstraints paramsConstraints = new GridBagConstraints();
			paramsConstraints.anchor = GridBagConstraints.NORTHWEST;
			paramsConstraints.fill = GridBagConstraints.HORIZONTAL;
			paramsConstraints.gridwidth = 1;
			paramsConstraints.gridheight = 1;
			paramsConstraints.gridx = 0;
			paramsConstraints.gridy = 0;
			paramsConstraints.insets = new Insets(5, 5, 6, 6);
			paramsPanel.setLayout(paramsLayout);						
						
			paramsPanel.add( connectivityPanel, paramsConstraints );
			paramsConstraints.gridy++;
			paramsPanel.add( dynamicPanel, paramsConstraints );
			paramsConstraints.gridy++;
			paramsPanel.add( queuePanel, paramsConstraints );
			paramsConstraints.gridy++;
			paramsPanel.add( segmentButton, paramsConstraints );
			paramsConstraints.gridy++;
			paramsPanel.add( overlayButton, paramsConstraints );
			paramsConstraints.gridy++;
			paramsPanel.add( resultButton, paramsConstraints );
			paramsConstraints.gridy++;
			
			// main panel
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
				all.add( super.zSelector, allConstraints );
				all.add( super.sliceSelector, allConstraints );
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
									
									updateResultOverlay();
									displayImage.updateAndDraw();
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
								updateResultOverlay();
								displayImage.updateAndDraw();
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
									updateResultOverlay();									
									displayImage.updateAndDraw();
									
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
	}
	
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
			ImagePlus watershedResult = resultImage.duplicate();
			watershedResult.setTitle( "Watershed-" + this.inputImage.getTitle() );
			watershedResult.show();
		}
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
		
		// read priority queue flag
		this.usePriorityQueue = queueBox.isSelected();
		
		// disable parameter panel
		setParamsEnabled( false );
				
		IJ.log( "Running extended minima with dynamic value " + dynamic + "..." );
		final long start = System.currentTimeMillis();
		
		final ImageStack image = this.inputImage.getImageStack(); 
		
		// Run extended minima
		ImageStack regionalMinima = MinimaAndMaxima3D.extendedMinima( image, (int)dynamic, connectivity );
		
		final long step1 = System.currentTimeMillis();		
		IJ.log( "Regional minima took " + (step1-start) + " ms.");
		
		IJ.log( "Imposing regional minima on original image (connectivity = " + connectivity + ")..." );
						
		// Impose regional minima over the original image
		ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima( image, regionalMinima, connectivity );
		
		ImagePlus impMin = new ImagePlus( "imposed minima", imposedMinima );
		impMin.setCalibration( this.inputImage.getCalibration() );
		
		final long step2 = System.currentTimeMillis();
		IJ.log( "Imposition took " + (step2-step1) + " ms.");
						
		IJ.log( "Labeling regional minima..." );
		
		// Label regional minima
		ImageStack labeledMinima = ConnectedComponents.computeLabels( regionalMinima, connectivity, 32 );
		
		final long step3 = System.currentTimeMillis();
		IJ.log( "Connected components took " + (step3-step2) + " ms.");
		
		// Apply watershed
		
		IJ.log("Running watershed...");
		
		ImagePlus connectedMinima = new ImagePlus( "connected minima", labeledMinima );				
		WatershedTransform3D wt = new WatershedTransform3D( impMin, connectedMinima, null, connectivity );
		
		if ( usePriorityQueue )
			resultImage = wt.applyWithPriorityQueue();
		else 
			resultImage = wt.apply();
		
		final long end = System.currentTimeMillis();
		IJ.log( "Watershed 3d took " + (end-step3) + " ms.");
		IJ.log( "Whole plugin took " + (end-start) + " ms.");
		
		// Adjust min and max values to display
		double min = 0;
		double max = 0;
		for( int slice=1; slice<=resultImage.getImageStackSize(); slice++ )			
		{
			ImageProcessor ip = resultImage.getImageStack().getProcessor(slice);
			ip.resetMinAndMax();
			if( max < ip.getMax() )
				max = ip.getMax();
			if( min > ip.getMin() )
				min = ip.getMin();
		}
		
		resultImage.setDisplayRange( min, max );
		
		byte[][] colorMap = CommonLabelMaps.fromLabel( CommonLabelMaps.SPECTRUM.getLabel() ).computeLut(255, true);;
		ColorModel cm = ColorMaps.createColorModel(colorMap, Color.BLACK);
		resultImage.getProcessor().setColorModel(cm);
		resultImage.getImageStack().setColorModel(cm);
		resultImage.updateAndDraw();
		//resultImage.show();
		
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
	void setParamsEnabled( Boolean enabled )
	{
		this.dynamicText.setEnabled( enabled );
		this.connectivityList.setEnabled( enabled );
		this.segmentButton.setEnabled( enabled );
		this.overlayButton.setEnabled( enabled );
		this.resultButton.setEnabled( enabled );
		this.queueBox.setEnabled( enabled );
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
