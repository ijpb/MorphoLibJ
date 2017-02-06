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
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.gui.Toolbar;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.data.image.ColorImages;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.label.LabelImages;
import inra.ijpb.util.ColorMaps;
import inra.ijpb.util.ColorMaps.CommonLabelMaps;
import inra.ijpb.watershed.Watershed;

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
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Plugin to perform automatic segmentation of 2D and 3D images
 * using marker-controlled watershed with manually selected seeds.
 *
 * References:
 * [1] Soille, P., Morphological Image Analysis: Principles and
 *     Applications, Springer-Verlag, 1999, pp. 170-171.
 *
 * @author Ignacio Arganda-Carreras
 *
 */
public class InteractiveMarkerControlledWatershedSegmentation implements PlugIn {

    /** main GUI window */
    private CustomWindow win;

    /** original input image */
    ImagePlus inputImage = null;

    ImageStack inputStackCopy = null;

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

    //	 Watershed segmentation panel design:
    //
    //	 __ Watershed Segmentation___
    //	| Tolerance: [10]            |
    //	| x - Advanced options       |
    //	|  ------------------------- |
    //	| | x - Use dams            ||
    //	| | Connectivity: [6/26]    ||
    //	|  ------------------------- |
    //  |          -----             |
    //  |         | Run |	     |
    //  |          -----             |
    //	|____________________________|

    /** watershed segmentation panel */
    JPanel segmentationPanel = new JPanel();

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
    JComboBox<String> connectivityList;

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

    /** text of option to display results as overlaid catchment basins */
    static String overlaidBasinsText = "Overlaid basins";
    /** text of option to display results as overlaid dams (watershed lines) */
    static String overlaidDamsText = "Overlaid dams";
    /** text of option to display results as catchment basins */
    static String catchmentBasinsText = "Catchment basins";
    /** text of option to display results as binary watershed lines */
    static String watershedLinesText = "Watershed lines";

    /** list of result display options (to show in the GUI canvas) */
    String[] resultDisplayOption = new String[]{ overlaidBasinsText,
	    overlaidDamsText, catchmentBasinsText, watershedLinesText };
    /** result display combo box */
    JComboBox<String> resultDisplayList = null;
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

    /** post-processing panel */
    JPanel postProcessPanel = new JPanel();
    /** button to merge selected labels */
    JButton mergeButton = null;
    /** button to shuffle LUT of display image */
    JButton shuffleColorsButton = null;

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
    public static enum ResultMode { OVERLAID_BASINS, OVERLAID_DAMS, BASINS, LINES };

    // Macro recording constants (corresponding to
    // the static method names to be called)
    /** name of the macro method to segment the
     * current image based on the current parameters */
    public static String RUN_SEGMENTATION = "segment";
    /** name of the macro method to toggle the current overlay */
    public static String SHOW_RESULT_OVERLAY = "toggleOverlay";
    /** name of the macro method to show current segmentation result in a new window */
    public static String CREATE_IMAGE = "createResultImage";

    /** name of the macro method to set the input image type */
    public static String SET_INPUT_TYPE = "setInputImageType";
    /** name of the macro method to set the flag to show the gradient image */
    public static String SHOW_GRADIENT = "setShowGradient";
    /** name of the macro method to set the output display format */
    public static String SET_DISPLAY = "setDisplayFormat";

    /** name of the macro method to set the gradient radius */
    public static String SET_RADIUS = "setGradientRadius";

    /** name of the macro method to merge selected labels */
    public static String MERGE_LABELS = "mergeLabels";
    /** name of the macro method to shuffle color labels */
    public static String SHUFFLE_COLORS = "shuffleColors";

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
			    record( SHOW_RESULT_OVERLAY, arg );
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
			// "Display" result combo box
			else if ( e.getSource() == resultDisplayList )
			{
			    if( showColorOverlay )
				updateResultOverlay();
			    // Macro recording
			    String[] arg = new String[] { (String) resultDisplayList.getSelectedItem() };
			    record( SET_DISPLAY, arg );
			}
			else if( e.getSource()  == mergeButton )
			{
			    setParamsEnabled( false );
			    mergeLabels();
			    setParamsEnabled( true );
			}
			else if( e.getSource()  == shuffleColorsButton )
			{
			    shuffleColors();
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

	    setTitle( "Interactive Marker-controlled Watershed Segmentation" );

	    // select point tool for manual label merging
	    Toolbar.getInstance().setTool( Toolbar.POINT );

	    // === Watershed Segmentation panel ===

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
	    connectivityList = new JComboBox<String>( connectivityOptions );
	    connectivityList.setToolTipText( "Voxel connectivity to use" );
	    connectivityLabel = new JLabel( "Connectivity" );
	    connectivityPanel.add( connectivityLabel );
	    connectivityPanel.add( connectivityList );
	    connectivityPanel.setToolTipText( "Voxel connectivity to use" );

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
	    resultDisplayList = new JComboBox<String>( resultDisplayOption );
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

	    // === Post-processing panel ===
	    mergeButton = new JButton( "Merge labels" );
	    mergeButton.setEnabled( false );
	    mergeButton.setToolTipText( "Merge labels selected by freehand or "
		    + "point ROI" );
	    mergeButton.addActionListener( listener );

	    shuffleColorsButton = new JButton( "Shuffle colors" );
	    shuffleColorsButton.setEnabled( false );
	    shuffleColorsButton.setToolTipText( "Shuffle color labels" );
	    shuffleColorsButton.addActionListener( listener );

	    GridBagLayout mergingLayout = new GridBagLayout();
	    postProcessPanel.setLayout( mergingLayout );

	    GridBagConstraints ppConstraints = new GridBagConstraints();
	    ppConstraints.anchor = GridBagConstraints.NORTHWEST;
	    ppConstraints.fill = GridBagConstraints.HORIZONTAL;
	    ppConstraints.gridwidth = 1;
	    ppConstraints.gridheight = 1;
	    ppConstraints.gridx = 0;
	    ppConstraints.gridy = 0;
	    ppConstraints.insets = new Insets( 5, 5, 6, 6 );

	    postProcessPanel.add( mergeButton, ppConstraints );
	    ppConstraints.gridy++;
	    postProcessPanel.add( shuffleColorsButton, ppConstraints );
	    postProcessPanel.setBorder( BorderFactory.createTitledBorder(
		    "Post-processing" ) );


	    // Parameter panel (left side of the GUI, it includes the
	    // three main panels: Watershed Segmentation, Results and
	    // Post-processing).
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
	    paramsPanel.add( segmentationPanel, paramsConstraints);
	    paramsConstraints.gridy++;
	    paramsPanel.add( displayPanel, paramsConstraints);
	    paramsConstraints.gridy++;
	    paramsPanel.add( postProcessPanel, paramsConstraints);
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
	}// end CustomWindow constructor

	/**
	 * Overwrite windowClosing to display the input image after closing
	 * the GUI and shut down the executor service
	 */
	@Override
	public void windowClosing( WindowEvent e )
	{
	    super.windowClosing( e );

	    if( null != inputImage )
	    {
		if( null != displayImage )
		    inputImage.setSlice( displayImage.getCurrentSlice() );

		// display input image
		inputImage.getWindow().setVisible( true );
	    }

	    // remove listeners
	    advancedOptionsCheckBox.removeActionListener( listener );
	    segmentButton.removeActionListener( listener );
	    resultDisplayList.removeActionListener( listener );
	    toggleOverlayCheckBox.removeActionListener( listener );
	    resultButton.removeActionListener( listener );

	    if( null != displayImage )
	    {
		//displayImage.close();
		displayImage = null;
	    }
	    // shut down executor service
	    exec.shutdownNow();
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
	 * Get segmentation command (text on the segment button)
	 * @return text on the segment button when segmentation is not running
	 */
	String getSegmentText(){
	    return segmentText;
	}

	/**
	 * Run marker-controlled watershed segmentation pipeline
	 */
	private void runSegmentation( String command )
	{
	    // If the command is the text on the run segmentation button
	    if ( command.equals( segmentText ) )
	    {
		// read connectivity
		final int readConn =
			Integer.parseInt( (String)
				connectivityList.getSelectedItem() );

		// convert connectivity to 3D if needed (2D images are processed as 3D)
		final int connectivity;
		if( inputIs2D )
		    connectivity = readConn == 4 ? 6 : 26;
		else
		    connectivity = readConn;

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

			// disable parameter panel
			setParamsEnabled( false );

			// get original image info
			final Roi roi = displayImage.getRoi();
			if( roi == null )
			{
			    IJ.showMessage( "Please define the markers using for example "
				    + "the point selection tool." );
			    IJ.showProgress( 1.0 );
			    // set button back to initial text
			    segmentButton.setText( segmentText );
			    segmentButton.setToolTipText( segmentTip );
			    return;
			}

			final long start = System.currentTimeMillis();

			// create marker image from ROIs
			final double maxValue = Float.MAX_VALUE;

			ImageStack markerStack =
				new ImageStack( inputImage.getWidth(), inputImage.getHeight() );
			ImageProcessor[] markerSlice =
				new ImageProcessor[ inputImage.getImageStackSize() ];
			for( int n=0; n<inputImage.getImageStackSize(); n++ )
			{
			    markerSlice[ n ] =
				    new FloatProcessor( inputImage.getWidth(), inputImage.getHeight() );

			    markerSlice[ n ].setValue( maxValue );
			    markerSlice[ n ].setColor( maxValue );
			}
			if( inputIs2D )
			{
			    // paint ROI
			    if( roi.isArea() )
				markerSlice[0].fill( roi );
			    else
				markerSlice[0].draw( roi );
			}
			else
			{
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
				    IJ.showProgress( 1.0 );
				    // set button back to initial text
				    segmentButton.setText( segmentText );
				    segmentButton.setToolTipText( segmentTip );
				    return;
				}
			    }
			    // otherwise paint ROI on the slice currently selected
			    // on the mask image
			    else
			    {
				int slice = inputImage.getSlice();

				if( roi.isArea() )
				    markerSlice[ slice-1 ].fill( roi );
				else
				    markerSlice[ slice-1 ].draw( roi );
			    }
			}
			// add slices to stack
			for( int n=0; n<inputImage.getImageStackSize(); n++ )
			{
			    markerStack.addSlice( markerSlice[n] );
			}

			markerStack = BinaryImages.componentsLabeling(
				markerStack, connectivity, 32 );
			ImagePlus marker = new ImagePlus( "marker", markerStack );

			try{
			    resultImage = Watershed.computeWatershed( inputImage, marker, null,
				    connectivity, calculateDams, false );
			}
			catch( Exception ex )
			{
			    ex.printStackTrace();
			    IJ.log( "Error while runing watershed: " + ex.getMessage() );
			}
			catch( OutOfMemoryError err )
			{
			    err.printStackTrace();
			    IJ.log( "Error: the plugin run out of memory. Please use a smaller input image." );
			}
			if( null == resultImage )
			{
			    IJ.log( "The segmentation was interrupted!" );
			    IJ.showStatus( "The segmentation was interrupted!" );
			    IJ.showProgress( 1.0 );
			    // set button back to initial text
			    segmentButton.setText( segmentText );
			    segmentButton.setToolTipText( segmentTip );
			    return;
			}

			resultImage.setTitle("Waterhsed");
			resultImage.setCalibration( inputImage.getCalibration() );

			final long end = System.currentTimeMillis();
			IJ.log( "Whole plugin took " + (end-start) + " ms.");

			// Adjust min and max values to display
			Images3D.optimizeDisplayRange( resultImage );

			byte[][] colorMap = CommonLabelMaps.fromLabel( CommonLabelMaps.GOLDEN_ANGLE.getLabel() ).computeLut( 255, false );
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
			segmentButton.setToolTipText( segmentTip );
			// set thread to null
			segmentationThread = null;

			//			// Record
			//			String[] arg = new String[] {
			//				"tolerance=" + Integer.toString( (int) dynamic ),
			//				"calculateDams=" + calculateDams,
			//				"connectivity=" + Integer.toString( connectivity ) };
			//			record( RUN_SEGMENTATION, arg );
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
	 * Update the display image with the input image voxels.
	 */
	void updateDisplayImage()
	{
	    int slice = displayImage.getCurrentSlice();
	    displayImage.setStack( inputStackCopy );
	    displayImage.setSlice( slice );
	    displayImage.updateAndDraw();
	}

	/**
	 * Toggle overlay with segmentation results (if any)
	 */
	void toggleOverlay()
	{
	    showColorOverlay = !showColorOverlay;

	    toggleOverlayCheckBox.setSelected( showColorOverlay );

	    if ( showColorOverlay )
		updateResultOverlay();
	    else
		displayImage.setOverlay( null );
	    displayImage.updateAndDraw();
	}

	/**
	 * Show segmentation result (if exists) in a new window
	 */
	void createResultImage()
	{
	    if( null != resultImage )
	    {

		final String displayOption = (String) resultDisplayList.getSelectedItem();

		ImagePlus watershedResult = null;

		// options: "Catchment basins", "Overlaid dams", "Watershed lines", "Overlaid basins"
		if( displayOption.equals( catchmentBasinsText ) )
		    watershedResult = getResult( ResultMode.BASINS );
		else if( displayOption.equals( overlaidDamsText ) )
		    watershedResult = getResult( ResultMode.OVERLAID_DAMS );
		else if( displayOption.equals( watershedLinesText ) )
		    watershedResult = getResult( ResultMode.LINES );
		else if ( displayOption.equals( overlaidBasinsText ) )
		    watershedResult = getResult( ResultMode.OVERLAID_BASINS );

		if( null != watershedResult )
		{
		    watershedResult.show();
		    watershedResult.setSlice( displayImage.getSlice() );
		}

		// Macro recording
		record( CREATE_IMAGE );
	    }
	}
	/**
	 * Merge labels selected by free-hand or point ROIs
	 */
	void mergeLabels()
	{
	    if( resultImage == null )
	    {
		IJ.error( "You need to run the segmentation before merging "
			+ "labels!" );
		return;
	    }
	    final Roi roi = displayImage.getRoi();
	    if( roi == null )
	    {
		IJ.showMessage( "Please select some labels to merge using any "
			+ "of the selection tools" );
		return;
	    }

	    // merge labels under the ROI
	    LabelImages.mergeLabels( resultImage, roi, true );

	    if ( showColorOverlay )
		updateResultOverlay();

	    // macro recording
	    record( MERGE_LABELS );
	}

	/**
	 * Shuffle colors of current result image
	 */
	void shuffleColors()
	{
	    if( null != resultImage )
	    {
		byte[][] colorMap = CommonLabelMaps.fromLabel(
			CommonLabelMaps.GOLDEN_ANGLE.getLabel() )
			.computeLut( 255, false );
		// shuffle color map
		long seed = (long) (new Random()).nextInt();
		colorMap = ColorMaps.shuffleLut( colorMap, seed );

		ColorModel cm = ColorMaps.createColorModel(
			colorMap, Color.BLACK );
		resultImage.getProcessor().setColorModel( cm );
		resultImage.getImageStack().setColorModel( cm );
		resultImage.updateAndDraw();
		if ( showColorOverlay )
		    updateResultOverlay();
	    }
	    // macro recording
	    record( SHUFFLE_COLORS );
	}


	/**
	 * Get current segmentation results based on selected mode
	 * @param mode selected result mode ("Overlaid basins", "Overlaid dams", "Catchment basins", "Watershed lines") 
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

	    // if the overlay is not shown
	    if( showColorOverlay == false )
	    {
		result = displayImage.duplicate();
		result.setTitle( title + ext );
		return result;
	    }


	    switch( mode ){
	    case OVERLAID_BASINS:
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
		result.setTitle( title + "-overlaid-basins" + ext );
		break;
	    case BASINS:
		result = resultImage.duplicate();
		result.setTitle( title + "-catchment-basins" + ext );
		break;
	    case OVERLAID_DAMS:
		result = getWatershedLines( resultImage );
		result = ColorImages.binaryOverlay( displayImage, result, Color.red );
		result.setTitle( title + "-overlaid-dams" + ext );
		break;
	    case LINES:
		result = getWatershedLines( resultImage );
		IJ.run( result, "Invert", "stack" );
		result.setTitle( title + "-watershed-lines" + ext );
		break;
	    }

	    return result;
	}

	/**
	 * Set the result display option in the GUI
	 * @param option output format
	 */
	void setResultDisplayOption( String option )
	{
	    if( Arrays.asList( resultDisplayOption ).contains( option ) )
		resultDisplayList.setSelectedItem( option );
	}

	/**
	 * Get the selected display option
	 * @return currently selected display option
	 */
	String getResultDisplayOption()
	{
	    return (String) resultDisplayList.getSelectedItem();
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

		final String displayOption = (String) resultDisplayList.getSelectedItem();

		ImageRoi roi = null;

		if( displayOption.equals( catchmentBasinsText ) )
		{
		    roi = new ImageRoi(0, 0, resultImage.getImageStack().getProcessor( slice ) );
		    roi.setOpacity( 1.0 );
		}
		else if( displayOption.equals( overlaidDamsText ) )
		{
		    ImageProcessor lines = BinaryImages.binarize( resultImage.getImageStack().getProcessor( slice ) );
		    lines.invert();
		    lines.setLut( LUT.createLutFromColor( Color.red ) );
		    roi = new ImageRoi( 0, 0, lines );
		    roi.setZeroTransparent( true );
		    roi.setOpacity( 1.0 );
		}
		else if( displayOption.equals( watershedLinesText ) )
		{
		    roi = new ImageRoi(0, 0, BinaryImages.binarize( resultImage.getImageStack().getProcessor( slice ) ) );
		    roi.setOpacity( 1.0 );
		}
		else if( displayOption.equals( overlaidBasinsText ) )
		{
		    roi = new ImageRoi(0, 0, resultImage.getImageStack().getProcessor( slice ) );
		    roi.setOpacity( opacity );
		}

		displayImage.setOverlay( new Overlay( roi ) );
	    }
	}

	/**
	 * Accessor to know if the result overlay needs to be displayed
	 * @return
	 */
	boolean isShowResultOverlaySelected()
	{
	    return showColorOverlay;
	}
    }// end class CustomWindow


    /**
     * Get the watershed lines out of the result catchment basins image
     * @param labels labeled catchment basins image
     * @return binary image with the watershed lines in white
     */
    ImagePlus getWatershedLines( ImagePlus labels )
    {
	final ImagePlus lines = BinaryImages.binarize( labels );
	IJ.run( lines, "Invert", "stack" );
	return lines;
    }


    /**
     * Enable/disable all components in the parameter panel
     *
     * @param enabled boolean flag to enable/disable components
     */
    void setParamsEnabled( boolean enabled )
    {
	this.advancedOptionsCheckBox.setEnabled( enabled );
	this.toggleOverlayCheckBox.setEnabled( enabled );
	this.resultButton.setEnabled( enabled );
	this.resultDisplayList.setEnabled( enabled );
	displayLabel.setEnabled( enabled );
	if( selectAdvancedOptions )
	    enableAdvancedOptions( enabled );
	this.mergeButton.setEnabled( enabled );
	this.shuffleColorsButton.setEnabled( enabled );
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
    }

    @Override
    public void run(String arg0)
    {
	if ( IJ.getVersion().compareTo("1.48a") < 0 )
	{
	    IJ.error( "Interactive Marker-controlled Watershed Segmentation", "ERROR: detected ImageJ version " + IJ.getVersion()  
	    + ".\nInteractive Marker-controlled Watershed Segmentation requires version 1.48a or superior, please update ImageJ!" );
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
	    IJ.error( "Interactive Marker-controlled Watershed Segmentation",
		    "This plugin only works on grayscale images.\nPlease convert it to 8, 16 or 32-bit." );
	    return;
	}

	inputStackCopy = inputImage.getImageStack().duplicate();
	displayImage = new ImagePlus( inputImage.getTitle(),
		inputStackCopy );
	displayImage.setTitle("Morphological Segmentation");
	displayImage.setSlice( inputImage.getSlice() );

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
     * @param connectivity string containing connectivity value (format: "connectivity=[4 or 8 / 6 or 26])
     * @param usePriorityQueue string containing boolean flag to use priority queue (format: "usePriorityQueue=[boolean])
     *
     * @deprecated the priority queue method is now the only one
     */
    public static void segment(
	    String dynamic,
	    String calculateDams,
	    String connectivity,
	    String usePriorityQueue )
    {
	final ImageWindow iw = WindowManager.getCurrentImage().getWindow();
	if( iw instanceof CustomWindow )
	{
	    //IJ.log( "GUI detected" );
	    final CustomWindow win = (CustomWindow) iw;
	    win.setCalculateDams( calculateDams.contains( "true" ) );
	    win.setConnectivity( Integer.parseInt( connectivity.replace( "connectivity=", "" ) ) );
	    win.runSegmentation( win.getSegmentText() );
	}
	else
	    IJ.log( "Error: Morphological Segmentation GUI not detected." );
    }
    /**
     * Segment current image (GUI needs to be running)
     *
     * @param dynamic string containing dynamic value (format: "dynamic=[integer value]")
     * @param calculateDams string containing boolean flag to create dams (format: "calculateDams=[boolean])
     * @param connectivity string containing connectivity value (format: "connectivity=[4 or 8 / 6 or 26])
     */
    public static void segment(
	    String dynamic,
	    String calculateDams,
	    String connectivity )
    {
	final ImageWindow iw = WindowManager.getCurrentImage().getWindow();
	if( iw instanceof CustomWindow )
	{
	    //IJ.log( "GUI detected" );
	    final CustomWindow win = (CustomWindow) iw;
	    win.setCalculateDams( calculateDams.contains( "true" ) );
	    win.setConnectivity( Integer.parseInt( connectivity.replace( "connectivity=", "" ) ) );
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
     * Merge labels that are selected either by a freehand or a point ROI.
     * Label of value 0 (watershed line) is skipped.
     */
    public static void mergeLabels()
    {
	final ImageWindow iw = WindowManager.getCurrentImage().getWindow();
	if( iw instanceof CustomWindow )
	{
	    final CustomWindow win = (CustomWindow) iw;
	    win.mergeLabels();
	}
    }
    /**
     * Shuffle LUT of current display image.
     */
    public static void shuffleColors()
    {
	final ImageWindow iw = WindowManager.getCurrentImage().getWindow();
	if( iw instanceof CustomWindow )
	{
	    final CustomWindow win = (CustomWindow) iw;
	    win.shuffleColors();
	}
    }

    /**
     * Show current result in a new image
     */
    public static void createResultImage()
    {
	final ImageWindow iw = WindowManager.getCurrentImage().getWindow();
	if( iw instanceof CustomWindow )
	{
	    final CustomWindow win = (CustomWindow) iw;
	    String mode = win.getResultDisplayOption();

	    ImagePlus result = null;

	    if( mode.equals( InteractiveMarkerControlledWatershedSegmentation.catchmentBasinsText) )
		result = win.getResult( ResultMode.BASINS );
	    else if( mode.equals( InteractiveMarkerControlledWatershedSegmentation.overlaidBasinsText ) )
		result = win.getResult( ResultMode.OVERLAID_BASINS );
	    else if( mode.equals( InteractiveMarkerControlledWatershedSegmentation.watershedLinesText ) )
		result = win.getResult( ResultMode.LINES );
	    else if( mode.equals( InteractiveMarkerControlledWatershedSegmentation.overlaidDamsText ))
		result = win.getResult( ResultMode.OVERLAID_DAMS );

	    if( null != result )
	    {
		result.show();
		result.setSlice( win.getImagePlus().getSlice() );
	    }
	}
    }

    /**
     * Set the display format in the GUI
     * @param format output mode ("Overlaid basins", "Overlaid dams", "Catchment basins", "Watershed lines")
     */
    public static void setDisplayFormat( String format )
    {
	final ImageWindow iw = WindowManager.getCurrentImage().getWindow();
	if( iw instanceof CustomWindow )
	{
	    final CustomWindow win = (CustomWindow) iw;
	    win.setResultDisplayOption( format );
	    if( win.isShowResultOverlaySelected() )
		win.updateResultOverlay();
	}
    }
}// end class
