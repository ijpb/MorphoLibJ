package inra.ijpb.plugins;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.ConnectedComponents;
import inra.ijpb.data.image.ColorImages;
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

	/** gradient image stack */
	ImageStack gradientStack = null;

	/** image containing the final results of the watershed segmentation (basins with or without dams) */
	ImagePlus resultImage = null;		

	/** parameters panel (segmentation + display options) */
	JPanel paramsPanel = new JPanel();

	/** main panel */
	Panel all = new Panel();
	
	/** flag to indicate 2D input image */
	boolean inputIs2D = false;

	//	 input panel design:
	//	 __ Input Image ____________
	//	| o Border Image            |
	//	| o Object Image            |
	//	|---------------------------|
	//	|| Gradient type: [options]||
	//	|| Gradient size: [3]      ||
	//	|| x - show gradient       ||
	//	| --------------------------|
	//	|___________________________| 


	/** input image panel */
	JPanel inputImagePanel = new JPanel();
	/** input image options */
	ButtonGroup inputImageButtons;
	/** radio button to specify input image has borders already highlighted */
	JRadioButton borderButton;
	/** radio button to specify input image has highlighted objects and not borders */
	JRadioButton objectButton;
	/** text for border image type radio button */
	static String borderImageText = "Border Image";
	/** text for object image type radio button */
	static String objectImageText = "Object Image";
	/** panel to store the radio buttons with the image type options */
	JPanel radioPanel = new JPanel( new GridLayout(0, 1) );

	/** gradient options panel */
	JPanel gradientOptionsPanel = new JPanel();	
	/** gradient type panel */
	JPanel gradientTypePanel = new JPanel();
	/** gradient type label */
	JLabel gradientTypeLabel;
	/** gradient list of options */
	String[] gradientOptions = new String[]{ "Morphological" };
	/** gradient combo box */
	JComboBox gradientList;		
	/** gradient radius size panel */
	JPanel gradientSizePanel = new JPanel();
	/** gradient size label */
	JLabel gradientRadiusSizeLabel;
	/** gradient size text field */
	JTextField gradientRadiusSizeText;
	/** gradient radius */
	int gradientRadius = 1;
	/** flag to show the gradient result in the displayed canvas */
	boolean showGradient = false;

	/** checkbox to enable/disable the display of the gradient image */
	JCheckBox gradientCheckBox;
	/** gradient checkbox panel */
	JPanel showGradientPanel = new JPanel();
	/** flag to apply gradient to the input image */
	private boolean applyGradient = false;

	//	 Watershed segmentation panel design:
	//
	//	 __ Watershed Segmentation___
	//	| Tolerance: [10]            |	
	//	| x - Advanced options       |
	//	|  ------------------------- |
	//	| | x - Use dams            ||
	//	| | Connectivity: [6/26]    ||
	//	| | x - Use priority queue  ||
	//	|  ------------------------- |
	//  |          -----             |
	//  |         | Run |	         |
	//  |          -----             |	
	//	|____________________________| 

	/** watershed segmentation panel */
	JPanel segmentationPanel = new JPanel();

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

	/** dams panel */
	JPanel damsPanel = new JPanel();
	/** checkbox to enable/disable the calculation of watershed dams */
	JCheckBox damsCheckBox;
	/** flag to select/deselect the calculation of watershed dams */
	private boolean calculateDams = true;

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

	/** segmentation button (Run) */
	JButton segmentButton;

	//	Results panel design:
	//
	//	 __ Results__________________
	//	| Display: [Overlay basins]  |	
	//	| x - Overlay results        |
	//  |      --------------        |
	//  |     | Create Image |       |
	//  |      --------------        |	
	//	|____________________________| 

	/** main Results panel */
	JPanel displayPanel = new JPanel();

	/** label for the display combo box */
	JLabel displayLabel = null;

	/** text of option to display results as overlayed catchment basins */
	static String overlayedBasinsText = "Overlayed basins";
	/** text of option to display results as overlayed dams (watershed lines) */
	static String overlayedDamsText = "Overlayed dams";
	/** text of option to display results as catchment basins */
	static String catchmentBasinsText = "Catchment basins";
	/** text of option to display results as binary watershed lines */
	static String watershedLinesText = "Watershed lines";

	/** list of result display options (to show in the GUI canvas) */
	String[] resultDisplayOption = new String[]{ overlayedBasinsText, 
			overlayedDamsText, catchmentBasinsText, watershedLinesText };
	/** result display combo box */
	JComboBox resultDisplayList = null;
	/** panel to store the combo box with the display options */
	JPanel resultDisplayPanel = new JPanel();

	/** check box to toggle the result overlay */
	JCheckBox toggleOverlayCheckBox = null;
	/** panel to store the check box to toggle the result overlay */
	JPanel overlayPanel = new JPanel();

	/** button to display the results in a new window ("Create Image") */
	JButton resultButton = null;	

	/** flag to display the result overlay in the canvas */
	private boolean showColorOverlay = false;

	/** executor service to launch threads for the plugin methods and events */
	final ExecutorService exec = Executors.newFixedThreadPool(1);

	/** thread to run the segmentation */
	private Thread segmentationThread = null;

	/** text of the segmentation button when segmentation not running */
	private String segmentText = "Run";
	/** tip text of the segmentation button when segmentation not running */
	private String segmentTip = "Run the morphological segmentation";
	/** text of the segmentation button when segmentation running */
	private String stopText = "STOP";
	/** tip text of the segmentation button when segmentation running */
	private String stopTip = "Click to abort segmentation";

	/** enumeration of result modes */
	public static enum ResultMode { OVERLAYED_BASINS, OVERLAYED_DAMS, BASINS, LINES };

	// Macro recording constants (corresponding to  
	// the static method names to be called)
	/** name of the macro method to segment the 
	 * current image based on the current parameters */
	public static String SEGMENT = "segment";
	/** name of the macro method to toggle the current overlay */
	public static String TOGGLE_OVERLAY = "toggleOverlay";
	/** name of the macro method to show current segmentation result */
	public static String SHOW_RESULT = "showResult";

	/** opacity to display overlays */
	double opacity = 1.0/3.0;

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
			public void actionPerformed( final ActionEvent e ) 
			{

				final String command = e.getActionCommand();

				// listen to the buttons on separate threads not to block
				// the event dispatch thread
				exec.submit(new Runnable() {

					public void run()
					{
						// "Run" segmentation button		
						if( e.getSource() == segmentButton )
						{
							runSegmentation( command );						
						}						
						// "Show result overlay" check box
						else if( e.getSource() == toggleOverlayCheckBox )
						{
							toggleOverlay();
							// Macro recording
							String[] arg = new String[] {};
							record( TOGGLE_OVERLAY, arg );
						}
						// "Create Image" button
						else if( e.getSource() == resultButton )
						{
							createResultImage();						
						}
						// "Advanced options" check box
						else if( e.getSource() == advancedOptionsCheckBox )
						{
							selectAdvancedOptions = !selectAdvancedOptions;
							enableAdvancedOptions( selectAdvancedOptions );
						}
						// "Show gradient" check box
						else if ( e.getSource() == gradientCheckBox )
						{
							showGradient = !showGradient;
							updateDisplayImage();
							if( showColorOverlay )
								updateResultOverlay();
						}
						// "Display" result combo box
						else if ( e.getSource() == resultDisplayList )
						{
							if( showColorOverlay )
								updateResultOverlay();
						}
						// "Object Image" radio button 
						else if( command == objectImageText ||  command == borderImageText)
						{
							// apply gradient only when using and object image
							applyGradient = command == objectImageText;
							enableGradientOptions( applyGradient );
							// update display image (so gradient image is shown if needed)
							updateDisplayImage();
							if( showColorOverlay )
								updateResultOverlay();
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

			// === Input Image panel ===

			// input image options (border or object types)
			borderButton = new JRadioButton( borderImageText );
			borderButton.setSelected( !applyGradient );
			borderButton.setActionCommand( borderImageText );
			borderButton.addActionListener( listener );
			borderButton.setToolTipText( "input image has object borders already highlighted" );

			objectButton = new JRadioButton( objectImageText );
			objectButton.setActionCommand( objectImageText );
			objectButton.addActionListener( listener );
			objectButton.setToolTipText( "input image has highlighted objects but not borders" );

			inputImageButtons = new ButtonGroup();
			inputImageButtons.add( borderButton );
			inputImageButtons.add( objectButton );
			radioPanel.add( borderButton );
			radioPanel.add( objectButton );

			// gradient options panel (activated when selecting object image)
			gradientTypeLabel = new JLabel( "Gradient type " );			
			gradientTypeLabel.setToolTipText( "type of gradient filter to apply" );		
			gradientList = new JComboBox( gradientOptions );			
			gradientTypePanel.add( gradientTypeLabel );
			gradientTypePanel.add( gradientList );			
			gradientTypePanel.setToolTipText( "type of gradient filter to apply" );	

			gradientRadiusSizeLabel = new JLabel( "Gradient radius" );
			gradientRadiusSizeLabel.setToolTipText( "radius in pixels of the gradient filter");			
			gradientRadiusSizeText = new JTextField( String.valueOf( gradientRadius ), 5 );
			gradientRadiusSizeText.setToolTipText( "radius in pixels of the gradient filter");
			gradientSizePanel.add( gradientRadiusSizeLabel );
			gradientSizePanel.add( gradientRadiusSizeText );

			gradientCheckBox = new JCheckBox( "Show gradient", false );
			gradientCheckBox.addActionListener( listener );
			showGradientPanel.add( gradientCheckBox );

			GridBagLayout gradientOptionsLayout = new GridBagLayout();
			GridBagConstraints gradientOptionsConstraints = new GridBagConstraints();
			gradientOptionsConstraints.anchor = GridBagConstraints.WEST;
			gradientOptionsConstraints.gridwidth = 1;
			gradientOptionsConstraints.gridheight = 1;
			gradientOptionsConstraints.gridx = 0;
			gradientOptionsConstraints.gridy = 0;
			gradientOptionsPanel.setLayout( gradientOptionsLayout );

			gradientOptionsPanel.add( gradientTypePanel, gradientOptionsConstraints );
			gradientOptionsConstraints.gridy++;
			gradientOptionsPanel.add( gradientSizePanel, gradientOptionsConstraints );
			gradientOptionsConstraints.gridy++;			
			gradientOptionsPanel.add( showGradientPanel, gradientOptionsConstraints );
			gradientOptionsConstraints.gridy++;

			gradientOptionsPanel.setBorder( BorderFactory.createTitledBorder("") );

			enableGradientOptions( applyGradient );

			// add components to input image panel
			inputImagePanel.setBorder( BorderFactory.createTitledBorder( "Input Image" ) );
			GridBagLayout inputImageLayout = new GridBagLayout();
			GridBagConstraints inputImageConstraints = new GridBagConstraints();
			inputImageConstraints.anchor = GridBagConstraints.CENTER;
			inputImageConstraints.fill = GridBagConstraints.NONE;
			inputImageConstraints.gridwidth = 1;
			inputImageConstraints.gridheight = 1;
			inputImageConstraints.gridx = 0;
			inputImageConstraints.gridy = 0;
			inputImageConstraints.insets = new Insets(5, 5, 6, 6);
			inputImagePanel.setLayout( inputImageLayout );						

			inputImagePanel.add( radioPanel, inputImageConstraints );
			inputImageConstraints.gridy++;
			inputImageConstraints.anchor = GridBagConstraints.NORTHWEST;
			inputImageConstraints.fill = GridBagConstraints.HORIZONTAL;
			inputImagePanel.add( gradientOptionsPanel, inputImageConstraints );			
			inputImageConstraints.gridy++;


			// === Watershed Segmentation panel ===

			// regional minima dynamic value ("Tolerance")
			dynamicLabel = new JLabel( "Tolerance" );
			dynamicLabel.setToolTipText( "Tolerance in the search of local minima" );
			dynamicText = new JTextField( "10", 5 );
			dynamicPanel.add( dynamicLabel );
			dynamicPanel.add( dynamicText );
			dynamicPanel.setToolTipText( "Tolerance in the search of local minima" );							

			// advanced options (connectivity + priority queue choices)
			advancedOptionsCheckBox = new JCheckBox( "Advanced options", selectAdvancedOptions );
			advancedOptionsCheckBox.setToolTipText( "Enable advanced options" );
			advancedOptionsCheckBox.addActionListener( listener );

			// dams option
			damsCheckBox = new JCheckBox( "Calculate dams", calculateDams );
			damsCheckBox.setToolTipText( "Calculate watershed dams" );
			damsPanel.add( damsCheckBox );

			// connectivity
			if( inputIs2D )
				connectivityOptions = new String[]{ "4", "8" };
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
			advancedOptoinsConstraints.anchor = GridBagConstraints.WEST;
			advancedOptoinsConstraints.gridwidth = 1;
			advancedOptoinsConstraints.gridheight = 1;
			advancedOptoinsConstraints.gridx = 0;
			advancedOptoinsConstraints.gridy = 0;
			advancedOptionsPanel.setLayout( advancedOptionsLayout );

			advancedOptionsPanel.add( damsPanel, advancedOptoinsConstraints );
			advancedOptoinsConstraints.gridy++;			
			advancedOptionsPanel.add( connectivityPanel, advancedOptoinsConstraints );
			advancedOptoinsConstraints.gridy++;			
			advancedOptionsPanel.add( queuePanel, advancedOptoinsConstraints );

			advancedOptionsPanel.setBorder(BorderFactory.createTitledBorder(""));

			// Segmentation button
			segmentButton = new JButton( segmentText );
			segmentButton.setToolTipText( segmentTip );
			segmentButton.addActionListener( listener );

			// Segmentation panel
			segmentationPanel.setBorder( BorderFactory.createTitledBorder( "Watershed Segmentation" ) );
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
			segmentationConstraints.anchor = GridBagConstraints.CENTER;
			segmentationConstraints.fill = GridBagConstraints.NONE;
			segmentationPanel.add( segmentButton, segmentationConstraints );

			// === Results panel ===

			// Display result panel
			displayLabel = new JLabel( "Display" );
			displayLabel.setEnabled( false );
			resultDisplayList = new JComboBox( resultDisplayOption );
			resultDisplayList.setEnabled( false );
			resultDisplayList.setToolTipText( "Select how to display segmentation results" );
			resultDisplayList.addActionListener( listener );

			resultDisplayPanel.add( displayLabel );
			resultDisplayPanel.add( resultDisplayList );

			// Toggle overlay check box
			showColorOverlay = false;
			toggleOverlayCheckBox = new JCheckBox( "Show result overlay" );
			toggleOverlayCheckBox.setEnabled( showColorOverlay );
			toggleOverlayCheckBox.setToolTipText( "Toggle overlay with segmentation result" );
			toggleOverlayCheckBox.addActionListener( listener );
			overlayPanel.add( toggleOverlayCheckBox );

			// Create Image button
			resultButton = new JButton( "Create Image" );
			resultButton.setEnabled( false );
			resultButton.setToolTipText( "Show segmentation result in new window" );
			resultButton.addActionListener( listener );					

			// main Results panel
			displayPanel.setBorder( BorderFactory.createTitledBorder( "Results" ) );
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

			displayPanel.add( resultDisplayPanel, displayConstraints );
			displayConstraints.gridy++;
			displayPanel.add( overlayPanel, displayConstraints );
			displayConstraints.gridy++;
			displayConstraints.anchor = GridBagConstraints.CENTER;
			displayConstraints.fill = GridBagConstraints.NONE;
			displayPanel.add( resultButton, displayConstraints );


			// Parameter panel (left side of the GUI, it includes the 
			// three main panels: Input Image, Watershed Segmentation
			// and Results).
			GridBagLayout paramsLayout = new GridBagLayout();
			GridBagConstraints paramsConstraints = new GridBagConstraints();
			paramsConstraints.insets = new Insets( 5, 5, 6, 6 );
			paramsPanel.setLayout( paramsLayout );
			paramsConstraints.anchor = GridBagConstraints.NORTHWEST;
			paramsConstraints.fill = GridBagConstraints.HORIZONTAL;
			paramsConstraints.gridwidth = 1;
			paramsConstraints.gridheight = 1;
			paramsConstraints.gridx = 0;
			paramsConstraints.gridy = 0;
			paramsPanel.add( inputImagePanel, paramsConstraints);
			paramsConstraints.gridy++;
			paramsPanel.add( segmentationPanel, paramsConstraints);
			paramsConstraints.gridy++;
			paramsPanel.add( displayPanel, paramsConstraints);
			paramsConstraints.gridy++;


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
			
			// Fix minimum size to the preferred size at this point
			pack();
			setMinimumSize( getPreferredSize() );

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
		 * Overwrite windowClosing to display the input image after closing 
		 * the GUI and shut down the executor service
		 */
		@Override
		public void windowClosing( WindowEvent e ) 
		{							
			super.windowClosing( e );

			// display input image
			inputImage.getWindow().setVisible( true );

			inputImage.setSlice( displayImage.getCurrentSlice() );

			// shut down executor service
			exec.shutdownNow();
		}

		/**
		 * Set dynamic value in the GUI
		 * 
		 * @param dynamic dynamic value
		 */
		void setDynamic( int dynamic )
		{
			dynamicText.setText( Integer.toString(dynamic) );
		}

		/**
		 * Set flag and GUI checkbox to calculate watershed dams
		 * 
		 * @param b boolean flag
		 */
		void setCalculateDams( boolean b )
		{
			calculateDams = b;
			damsCheckBox.setSelected( b );
		}

		/**
		 * Set flag and GUI checkbox to apply morphological gradient
		 * 
		 * @param b boolean flag
		 */
		void setApplyGradient( boolean b )
		{
			applyGradient = b;
			gradientCheckBox.setSelected( b );
		}

		/**
		 * Set connectivity value in the GUI
		 * 
		 * @param connectivity 4-8 or 6-26 neighbor connectivity
		 */
		void setConnectivity( int connectivity )
		{			
			if( ( inputImage.getImageStackSize() > 1 && (connectivity == 6  || connectivity == 26 ) )
				|| ( inputImage.getImageStackSize() == 1 && (connectivity == 4  || connectivity == 8 ) ) )
				connectivityList.setSelectedItem( Integer.toString(connectivity) );									
		}

		/**
		 * Set flag and GUI checkbox to use priority queue
		 * 
		 * @param b boolean flag
		 */
		void setUsePriorityQueue( boolean b )
		{
			usePriorityQueue = b;
			queueCheckBox.setSelected( b );
		}

		/**
		 * Get segmentation command (text on the segment button)
		 * @return text on the segment button when segmentation is not running
		 */		
		String getSegmentText(){
			return segmentText;
		}

		/**
		 * Run morphological segmentation pipeline
		 */
		private void runSegmentation( String command ) 
		{
			// If the command is the text on the run segmentation button
			if ( command.equals( segmentText ) ) 
			{			
				// read connectivity
				int readConn = Integer.parseInt( (String) connectivityList.getSelectedItem() );

				// convert connectivity to 3D if needed (2D images are processed as 3D)
				if( inputIs2D )
					readConn = readConn == 4 ? 6 : 26;
				
				final int connectivity = readConn;
				
				// read dynamic
				final double dynamic;
				try{
					dynamic = Double.parseDouble( dynamicText.getText() );
				}
				catch( NullPointerException ex )
				{
					IJ.error( "Morphological Sementation", "ERROR: missing dynamic value" );
					return;
				}
				catch( NumberFormatException ex )
				{
					IJ.error( "Morphological Sementation", "ERROR: dynamic value must be a number" );
					return;
				}

				double max = 255;
				int bitDepth = inputImage.getBitDepth();
				if( bitDepth == 16 )
					max = 65535;
				else if( bitDepth == 32 )
					max = Float.MAX_VALUE;

				if( dynamic < 0 || dynamic > max )
				{
					IJ.error( "Morphological Sementation", "ERROR: the dynamic value must be a number between 0 and " + max );
					return;
				}

				// Set button text to "STOP"
				segmentButton.setText( stopText );
				segmentButton.setToolTipText( stopTip );
				segmentButton.setSize( segmentButton.getMinimumSize() );
				segmentButton.repaint();

				final Thread oldThread = segmentationThread;

				// Thread to run the segmentation
				Thread newThread = new Thread() {								 

					public void run()
					{
						// Wait for the old task to finish
						if (null != oldThread) 
						{
							try { 
								IJ.log("Waiting for old task to finish...");
								oldThread.join(); 
							} 
							catch (InterruptedException ie)	{ /*IJ.log("interrupted");*/ }
						}

						// read dams flag
						calculateDams = damsCheckBox.isSelected();

						// read priority queue flag
						usePriorityQueue = queueCheckBox.isSelected();

						// disable parameter panel
						setParamsEnabled( false );

						ImageStack image = inputImage.getImageStack();

						final long start = System.currentTimeMillis();

						if( applyGradient )
						{
							// read radius to use
							try{
								gradientRadius = Integer.parseInt( gradientRadiusSizeText.getText() );
							}
							catch( NullPointerException ex )
							{
								IJ.error( "Morphological Sementation", "ERROR: missing gradient radius value" );
								return;
							}
							catch( NumberFormatException ex )
							{
								IJ.error( "Morphological Sementation", "ERROR: radius value must be an integer number" );
								return;
							}

							final long t1 = System.currentTimeMillis();
							IJ.log( "Applying morphological gradient to input image..." );

							Strel3D strel = Strel3D.Shape.CUBE.fromRadius( gradientRadius );
							image = Morphology.gradient( image, strel );
							//(new ImagePlus("gradient", image) ).show();

							// store gradient image
							gradientStack = image;

							final long t2 = System.currentTimeMillis();
							IJ.log( "Morphological gradient took " + (t2-t1) + " ms.");
						}

						IJ.log( "Running extended minima with dynamic value " + (int)dynamic + "..." );
						final long step0 = System.currentTimeMillis();				

						// Run extended minima
						ImageStack regionalMinima = MinimaAndMaxima3D.extendedMinima( image, (int)dynamic, connectivity );

						if( null == regionalMinima )
						{
							IJ.log( "The segmentation was interrupted!" );
							IJ.showStatus( "The segmentation was interrupted!" );
							IJ.showProgress( 1.0 );
							return;
						}

						final long step1 = System.currentTimeMillis();		
						IJ.log( "Regional minima took " + (step1-step0) + " ms.");

						IJ.log( "Imposing regional minima on original image (connectivity = " + connectivity + ")..." );

						// Impose regional minima over the original image
						ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima( image, regionalMinima, connectivity );

						if( null == imposedMinima )
						{
							IJ.log( "The segmentation was interrupted!" );
							IJ.showStatus( "The segmentation was interrupted!" );
							IJ.showProgress( 1.0 );
							return;
						}

						final long step2 = System.currentTimeMillis();
						IJ.log( "Imposition took " + (step2-step1) + " ms." );

						IJ.log( "Labeling regional minima..." );

						// Label regional minima
						ImageStack labeledMinima = ConnectedComponents.computeLabels( regionalMinima, connectivity, 32 );
						if( null == labeledMinima )
						{
							IJ.log( "The segmentation was interrupted!" );
							IJ.showStatus( "The segmentation was interrupted!" );
							IJ.showProgress( 1.0 );
							return;
						}

						final long step3 = System.currentTimeMillis();
						IJ.log( "Connected components took " + (step3-step2) + " ms." );

						// Apply watershed		
						IJ.log("Running watershed...");

						ImageStack resultStack = Watershed.computeWatershed( imposedMinima, labeledMinima, 
								connectivity, usePriorityQueue, calculateDams );
						if( null == resultStack )
						{
							IJ.log( "The segmentation was interrupted!" );
							IJ.showStatus( "The segmentation was interrupted!" );
							IJ.showProgress( 1.0 );
							return;
						}

						resultImage = new ImagePlus( "watershed", resultStack );
						resultImage.setCalibration( inputImage.getCalibration() );

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
						updateDisplayImage();
						updateResultOverlay();
						showColorOverlay = true;
						toggleOverlayCheckBox.setSelected( true );

						// enable parameter panel
						setParamsEnabled( true );
						// set button back to initial text
						segmentButton.setText( segmentText );
						// set thread to null					
						segmentationThread = null;

						// Record
						String[] arg = new String[] {
								"dynamic=" + Integer.toString( (int) dynamic ),
								"calculateDams=" + calculateDams,
								"applyGradient=" + applyGradient,
								"connectivity=" + Integer.toString( connectivity ),
								"usePriorityQueue=" + usePriorityQueue };
						record( SEGMENT, arg );

					}
				};

				segmentationThread = newThread;
				newThread.start();

			}
			else if( command.equals( stopText ) ) 							  
			{
				if( null != segmentationThread )
					segmentationThread.interrupt();
				else
					IJ.log("Error: interrupting segmentation failed becaused the thread is null!");

				// set button back to initial text
				segmentButton.setText( segmentText );
				segmentButton.setToolTipText( segmentTip );
				// enable parameter panel
				setParamsEnabled( true );			
			}
		}

		/**
		 * Update the display image with the gradient or the input image voxels.
		 */
		void updateDisplayImage()
		{
			if( applyGradient && showGradient && null != gradientStack )			
				displayImage.setStack( gradientStack );
			else
				displayImage.setStack( inputImage.getImageStack() );	
			displayImage.updateAndDraw();
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
		void createResultImage()
		{
			if( null != resultImage )
			{

				final String displayOption = (String) resultDisplayList.getSelectedItem();

				String[] arg = null;

				ImagePlus watershedResult = null;

				// options: "Catchment basins", "Overlayed dams", "Watershed lines", "Overlayed basins"
				if( displayOption.equals( catchmentBasinsText ) )
				{			
					watershedResult = getResult( ResultMode.BASINS );									
					arg = new String[] { "mode=basins" };
				}
				else if( displayOption.equals( overlayedDamsText ) )
				{
					watershedResult = getResult( ResultMode.OVERLAYED_DAMS );
					arg = new String[] { "mode=overlayed_dams" };
				}
				else if( displayOption.equals( watershedLinesText ) )
				{
					watershedResult = getResult( ResultMode.LINES );
					arg = new String[] { "mode=lines" };
				}
				else if ( displayOption.equals( overlayedBasinsText ) )
				{
					watershedResult = getResult( ResultMode.OVERLAYED_BASINS );									
					arg = new String[] { "mode=overlayed_basins" };
				}

				if( null != watershedResult )
				{
					watershedResult.show();
					watershedResult.setSlice( displayImage.getSlice() );
				}

				// Macro recording	
				if( null != arg )
					record( SHOW_RESULT, arg );
			}
		}

		/**
		 * Get current segmentation results based on selected mode
		 * @param mode selected result mode ("Overlayed basins", "Overlayed dams", "Catchment basins", "Watershed lines") 
		 * @return result image
		 */
		ImagePlus getResult( ResultMode mode )
		{
			String title = inputImage.getTitle();
			String ext = "";
			int index = title.lastIndexOf( "." );
			if( index != -1 )
			{
				ext = title.substring( index );
				title = title.substring( 0, index );				
			}
			
			ImagePlus result = null;

			switch( mode ){
				case OVERLAYED_BASINS:
					result = displayImage.duplicate();
					result.setOverlay( null ); // remove existing overlay
					ImageStack is = new ImageStack( displayImage.getWidth(), displayImage.getHeight() );
	
					for( slice=1; slice<=result.getImageStackSize(); slice++ )
					{
						ImagePlus aux = new ImagePlus( "", result.getImageStack().getProcessor( slice ) );
						ImageRoi roi = new ImageRoi(0, 0, resultImage.getImageStack().getProcessor( slice ) );
						roi.setOpacity( opacity );
						aux.setOverlay( new Overlay( roi ) );
						aux = aux.flatten();
						is.addSlice( aux.getProcessor() );
					}
					result.setStack( is );
					if( applyGradient && showGradient )
						title += "-gradient";
					result.setTitle( title + "-overlayed-basins" + ext );
					break;
				case BASINS:
					result = resultImage.duplicate();
					result.setTitle( title + "-catchment-basins" + ext );				
					result.setSlice( displayImage.getSlice() );					
					break;
				case OVERLAYED_DAMS:
					result = getWatershedLines( resultImage );
					result = ColorImages.binaryOverlay( displayImage, result, Color.red ) ;
					if( applyGradient && showGradient )
						title += "-gradient";
					result.setTitle( title + "-overlayed-dams" + ext );				
					break;
				case LINES:
					result = getWatershedLines( resultImage );
					IJ.run( result, "Invert", "" );
					result.setTitle( title + "-watershed-lines" + ext );								
					break;
			}

			return result;
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
			displayImage.deleteRoi();
			int slice = displayImage.getCurrentSlice();
			
			final String displayOption = (String) resultDisplayList.getSelectedItem();							

			ImageRoi roi = null;
			
			if( displayOption.equals( catchmentBasinsText ) )
			{
				roi = new ImageRoi(0, 0, resultImage.getImageStack().getProcessor( slice ) );
				roi.setOpacity( 1.0 );
			}
			else if( displayOption.equals( overlayedDamsText ) )				
			{
				ImageProcessor lines = BinaryImages.binarize( resultImage.getImageStack().getProcessor( slice ) );
				lines.invert();
				ImageProcessor gray = displayImage.getImageStack().getProcessor( slice );
				roi = new ImageRoi(0, 0, ColorImages.binaryOverlay( gray, lines, Color.red ) ) ;
				roi.setOpacity( 1.0 );
			}
			else if( displayOption.equals( watershedLinesText ) )
			{
				roi = new ImageRoi(0, 0, BinaryImages.binarize( resultImage.getImageStack().getProcessor( slice ) ) );
				roi.setOpacity( 1.0 );
			}
			else if( displayOption.equals( overlayedBasinsText ) )	
			{
				roi = new ImageRoi(0, 0, resultImage.getImageStack().getProcessor( slice ) );
				roi.setOpacity( opacity );
			}
											
			displayImage.setOverlay( new Overlay( roi ) );
		}
	}


	/**
	 * Get the watershed lines out of the result catchment basins image
	 * @param labels labeled catchment basins image
	 * @return binary image with the watershed lines in white
	 */
	ImagePlus getWatershedLines( ImagePlus labels )
	{
		final ImagePlus lines = BinaryImages.binarize( labels );
		IJ.run( lines, "Invert", "" );
		return lines;
	}


	/**
	 * Enable/disable all components in the parameter panel
	 * 
	 * @param enabled boolean flag to enable/disable components
	 */
	void setParamsEnabled( boolean enabled )
	{
		this.dynamicText.setEnabled( enabled );
		this.dynamicLabel.setEnabled( enabled );		
		this.advancedOptionsCheckBox.setEnabled( enabled );
		this.toggleOverlayCheckBox.setEnabled( enabled );
		this.resultButton.setEnabled( enabled );
		this.resultDisplayList.setEnabled( enabled );
		displayLabel.setEnabled( enabled );
		if( selectAdvancedOptions )
			enableAdvancedOptions( enabled );
		if( applyGradient )
			enableGradientOptions( enabled );
	}

	/**
	 * Enable/disable advanced options components
	 * 
	 * @param enabled flag to enable/disable components
	 */
	void enableAdvancedOptions( boolean enabled )
	{
		damsCheckBox.setEnabled( enabled );
		connectivityLabel.setEnabled( enabled );
		connectivityList.setEnabled( enabled );
		queueCheckBox.setEnabled( enabled );
	}

	/**
	 * Enable/disable gradient options components
	 * 
	 * @param enabled flag to enable/disable components
	 */
	void enableGradientOptions( boolean enabled )
	{
		gradientList.setEnabled( enabled );
		gradientList.setEnabled( enabled );
		gradientRadiusSizeLabel.setEnabled( enabled );
		gradientRadiusSizeText.setEnabled( enabled );
		gradientCheckBox.setEnabled( enabled );
		gradientTypeLabel.setEnabled( enabled );
	}

	@Override
	public void run(String arg0) 
	{
		if ( IJ.getVersion().compareTo("1.48a") < 0 )
		{
			IJ.error( "Morphological Segmentation", "ERROR: detected ImageJ version " + IJ.getVersion()  
					+ ".\nMorphological Segmentation requires version 1.48a or superior, please update ImageJ!" );
			return;
		}

		// get current image
		if (null == WindowManager.getCurrentImage())
		{
			inputImage = IJ.openImage();
			if (null == inputImage) return; // user canceled open dialog
		}
		else
			inputImage = WindowManager.getCurrentImage();

		if( inputImage.getType() == ImagePlus.COLOR_256 || 
				inputImage.getType() == ImagePlus.COLOR_RGB )
		{
			IJ.error( "Morphological Segmentation", "This plugin only works on grayscale images.\nPlease convert it to 8, 16 or 32-bit." );
			return;
		}

		displayImage = inputImage.duplicate();
		displayImage.setTitle("Morphological Segmentation");
		displayImage.setSlice( inputImage.getSlice() );

		// hide input image (to avoid accidental closing)
		inputImage.getWindow().setVisible( false );

		// set the 2D flag
		inputIs2D = inputImage.getImageStackSize() == 1;
		
		// Build GUI
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						win = new CustomWindow( displayImage );
						win.pack();
					}
				});

	}

	/* **********************************************************
	 * Macro recording related methods
	 * *********************************************************/

	/**
	 * Macro-record a specific command. The command names match the static 
	 * methods that reproduce that part of the code.
	 * 
	 * @param command name of the command including package info
	 * @param args set of arguments for the command
	 */
	public static void record(String command, String... args) 
	{
		command = "call(\"inra.ijpb.plugins.MorphologicalSegmentation." + command;
		for(int i = 0; i < args.length; i++)
			command += "\", \"" + args[i];
		command += "\");\n";
		if(Recorder.record)
			Recorder.recordString(command);
	}

	/**
	 * Segment current image (GUI needs to be running)
	 * 
	 * @param dynamic string containing dynamic value (format: "dynamic=[integer value]")
	 * @param calculateDams string containing boolean flag to create dams (format: "calculateDams=[boolean])
	 * @param applyGradient string containing boolean flag to apply morphological gradient (format: "applyGradient=[boolean])
	 * @param connectivity string containing connectivity value (format: "connectivity=[4 or 8 / 6 or 26])
	 * @param usePriorityQueue string containing boolean flag to use priority queue (format: "usePriorityQueue=[boolean])
	 */
	public static void segment(
			String dynamic,
			String calculateDams,
			String applyGradient,
			String connectivity,
			String usePriorityQueue )
	{		
		final ImageWindow iw = WindowManager.getCurrentImage().getWindow();
		if( iw instanceof CustomWindow )
		{
			//IJ.log( "GUI detected" );			
			final CustomWindow win = (CustomWindow) iw;
			win.setDynamic( Integer.parseInt( dynamic.replace( "dynamic=", "" ) ) );
			win.setCalculateDams( calculateDams.contains( "true" ) );
			win.setApplyGradient( applyGradient.contains( "true" ) );
			win.setConnectivity( Integer.parseInt( connectivity.replace( "connectivity=", "" ) ) );
			win.setUsePriorityQueue( usePriorityQueue.contains( "true" ) );
			win.runSegmentation( win.getSegmentText() );			
		}
		else
			IJ.log( "Error: Morphological Segmentation GUI not detected." );
	}

	/**
	 * Toggle current result overlay image
	 */
	public static void toggleOverlay()
	{
		final ImageWindow iw = WindowManager.getCurrentImage().getWindow();
		if( iw instanceof CustomWindow )
		{
			final CustomWindow win = (CustomWindow) iw;
			win.toggleOverlay();
		}
	}

	/**
	 * Show current result in a new image
	 */
	public static void showResult( String modeText )
	{
		final ImageWindow iw = WindowManager.getCurrentImage().getWindow();
		if( iw instanceof CustomWindow )
		{
			final CustomWindow win = (CustomWindow) iw;
			String mode = modeText.replace( "mode=", "" );

			ImagePlus result = null;

			if( mode.equals( "basins") )
				result = win.getResult( ResultMode.BASINS );
			else if( mode.equals( "lines" ) )
				result = win.getResult( ResultMode.LINES );
			else if( mode.equals( "dams" ))
				result = win.getResult( ResultMode.OVERLAYED_DAMS );

			if( null != result )
			{
				result.show();
				result.setSlice( win.getImagePlus().getSlice() );
			}
		}
	}

}// end MorphologicalSegmentation class
