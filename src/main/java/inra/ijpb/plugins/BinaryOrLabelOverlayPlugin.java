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
/**
 * 
 */
package inra.ijpb.plugins;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.LUT;
import inra.ijpb.color.CommonColors;
import inra.ijpb.data.image.ColorImages;
import inra.ijpb.data.image.ImageUtils;

/**
 * Display a label map or a binary image as overlay onto a grayscale image.
 * 
 * @author dlegland
 *
 */
public class BinaryOrLabelOverlayPlugin implements PlugIn
{
    /** 
     * The list of images currently open.
     * Determined when plugin is run, and used during frame creation.
     */
    String[] imageNames;

    /**
     * The run method used to start the plugin from the GUI.
     * 
     * @param arg
     *            the string argument of the plugin
     */
    @Override
    public void run(String arg)
    {
        // Check version compatibility
        if (IJ.getVersion().compareTo("1.48a") < 0)
        {
            IJ.error("Binary / Label Overlay", "ERROR: detected ImageJ version "
                    + IJ.getVersion()
                    + ".\nBinary / Label Overlay requires version 1.48a or superior, please update ImageJ!");
            return;
        }
        
        // get current image, and returns if no image is loaded
        ImagePlus refImage = WindowManager.getCurrentImage();
        if (refImage == null)
        {
            return;
        }
        
        // initialize image name list
        this.imageNames = WindowManager.getImageTitles();
                
        // create new OverlayData initialized with current image (assuming this is the
        // reference image)
        OverlayData data = new OverlayData(refImage);
        // update overlay image arbitrarily with first image 
        data.updateOverlayImage(refImage);
        
        // Build GUI
        SwingUtilities.invokeLater(() ->
        {
            new CustomWindow(data);
        });
    }
    
    
    // ====================================================
    // Inner class for gathering overlay data
    
    /**
     * Contains the data for computing overlay, as well as some methods for
     * performing computation.
     */
    class OverlayData
    {
        /** The original input image */
        ImagePlus referenceImage = null;

        /** The image to overlay */
        ImagePlus overlayImage = null;

        /**
         * The image to be displayed in the GUI, obtained as a combination of
         * reference and overlay images.
         */
        ImagePlus displayImage = null;

        /** flag to indicate 2D input image */
        boolean is2DInput = false;

        /**
         * Boolean flag that indicates whether image to overlay is binary or
         * label
         */
        boolean binaryOverlay = true;

        /** The color to use to display binary overlays */ 
        Color overlayColor = Color.RED;
        
        /** Opacity to display overlays, between 0 and 1.0. Default is 33%. */
        double overlayOpacity = 1.0 / 3.0;
        
        
        public OverlayData(ImagePlus refImage)
        {
            if (refImage == null)
            {
                IJ.log("ref image equals null");
            }
            updateReferenceImage(refImage);
        }
        
        /**
         * Combines the reference image and the overlay image (and the overlay
         * color if the overlay image is binary) to update the image to display.
         * 
         * @param refImage
         *            the new reference image
         */
        public void updateReferenceImage(ImagePlus refImage)
        {
            if (refImage == null)
            {
                IJ.log("Can not update reference image to null.");
                return;
            }
            this.referenceImage = refImage;
            
            // set the 2D flag
            this.is2DInput = refImage.getImageStackSize() == 1;
            
            // make a copy of the input image and use it for display
            this.displayImage = refImage.duplicate();
            this.displayImage.setTitle("Label Map Overlay");
            this.displayImage.setSlice(refImage.getCurrentSlice());

            // correct Fiji error when the slices are read as frames
            if (this.is2DInput == false && this.displayImage.isHyperStack() == false
                    && this.displayImage.getNSlices() == 1)
            {
                // correct stack by setting number of frames as slices
                this.displayImage.setDimensions(1, this.displayImage.getNFrames(), 1);
            }
            
            // 
            if (this.overlayImage != null && ImageUtils.isSameSize(referenceImage, overlayImage))
            {
                updateDisplayImage();
            }
        }
        
        /**
         * Combines the reference image and the overlay image (and the overlay
         * color if the overlay image is binary) to update the image to display.
         * 
         * @param ovrImage
         *            the new overlay image
         */
        public void updateOverlayImage(ImagePlus ovrImage)
        {
            this.overlayImage = ovrImage;
            updateDisplayImage();
        }
        
        /**
         * Combines the reference image and the overlay image (and the overlay color
         * if the overlay image is binary) to update the image to display.
         */
        public void updateDisplayImage()
        {
            if (referenceImage == null)
            {
                IJ.log("ref image not initialized, do not update result.");
                return;
            }
            
            ImageRoi roi = null;
            if (overlayImage != null && ImageUtils.isSameSize(referenceImage, overlayImage))
            {
                int sliceIndex = displayImage.getCurrentSlice();
                ImageProcessor overlayProcessor = overlayImage.getImageStack().getProcessor(sliceIndex);
                overlayProcessor = makeOverlayProcessor(overlayProcessor, binaryOverlay, overlayColor);
                
                // convert image processor to ROI
                roi = createRoi(overlayProcessor, overlayOpacity);
            }

            displayImage.setOverlay(new Overlay(roi));
        }
        
        /**
         * Computes the result (RGB) image by combining the reference image with the
         * overlay image, and the other options.
         * 
         * @return a new RGB image (2D or3D).
         */
        public ImagePlus computeResultImage()
        {
            // switch to binary or label overlay depending on binaryOverlay option
            return binaryOverlay 
                    ? ColorImages.binaryOverlay(referenceImage, overlayImage, overlayColor, overlayOpacity)
                    : ColorImages.labelMapOverlay(referenceImage, overlayImage, overlayOpacity);
        }
    }
    
    
    // ====================================================
    // Utility methods used by the OverlayData class 
    
    private static final ImageProcessor makeOverlayProcessor(ImageProcessor array, boolean binaryOverlay, Color overlayColor)
    {
        if (binaryOverlay)
        {
            // assume overlay is binary
            ImageProcessor res = array.duplicate(); // to avoid side effect on original overlay image
            res.setLut(LUT.createLutFromColor(overlayColor));
            return res;
        }
        else
        {
            // duplicate label image, using new LUT as ColorModel
            ImageProcessor res = array.duplicate();
            res.setLut(makeBlackBackGround(array.getLut()));
            return res;
        }
    }
    
    /**
     * Enforces black color for the first element of the LUT.
     * 
     * @param lut
     *            the input LUT.
     * @return a new LUT with same size and color list, except for the first
     *         item which is black.
     */
    private static final LUT makeBlackBackGround(LUT lut)
    {
        byte[] red = new byte[256];
        byte[] green = new byte[256];
        byte[] blue = new byte[256];
        lut.getReds(red);
        lut.getGreens(green);
        lut.getBlues(blue);

        // forces background to 0
        red[0] = 0;
        green[0] = 0;
        blue[0] = 0;

        return new LUT(red, green, blue);
    }
    
    private static ImageRoi createRoi(ImageProcessor overlayProcessor, double opacity)
    {
        // convert image processor to ROI
        ImageRoi roi = new ImageRoi(0, 0, overlayProcessor);
        roi.setZeroTransparent(true);
        roi.setOpacity(opacity);
        return roi;
    }
    

    // ====================================================
    // Inner class for window
    
    /**
     * Custom window to display together plugin option and result image.
     */
    private class CustomWindow extends StackWindow
    {
        /**
         * Serial version UID
         */
        private static final long serialVersionUID = 1L;
        
        OverlayData data;
        
        /** Main panel */
        Panel mainPanel = new Panel();

        /** Parameters panel (segmentation + display options) */
        JPanel paramsPanel = new JPanel();
        
        // Widgets 
        
        JComboBox<String> referenceImageCombo;
        
        JComboBox<String> overlayImageCombo;
        
        JLabel overlayColorLabel;
        JComboBox<String> overlayColorNameCombo;
        
        JLabel overlayOpacityLabel;
        JPanel overlayOpacityPanel;
        JTextField overlayOpacityTextField;
        JSlider overlayOpacitySlider;
        
        JCheckBox binaryOverlayCheckbox;
        
        JButton resultButton = null;    
        
        /** Executor service to launch threads for the plugin methods and events */
        final ExecutorService threadPool = Executors.newFixedThreadPool(1);

        /**
         * Construct the plugin window.
         * 
         * @param imp
         *            input image
         */
        CustomWindow(OverlayData data)
        {
            super(data.displayImage, new ImageCanvas(data.displayImage));
            
            this.data = data;
            
            adjustCurrentZoom();
            
            setupWidgets();
            buildLayout();
            
            setTitle("Image Overlay");
            pack();
        }
        
        /**
         * Ensure that the current frame has a size compatible for the display
         * of the current image, zooming in or out to make image more visible.
         */
        private void adjustCurrentZoom()
        {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double screenWidth = screenSize.getWidth();
            double screenHeight = screenSize.getHeight();
            
            // Zoom in if image is too small
            while ((ic.getWidth() < screenWidth * 0.25 || ic.getHeight() < screenHeight * 0.25)
                    && ic.getMagnification() < 16.0)
            {
                final int canvasWidth = ic.getWidth();
                ic.zoomIn(0, 0);
                // check if canvas size changed (otherwise stop zooming)
                if (canvasWidth == ic.getWidth())
                {
                    ic.zoomOut(0, 0);
                    break;
                }
            }
            
            // Zoom out if canvas is too large
            while ((ic.getWidth() > screenWidth * 0.75 || ic.getHeight() > screenHeight * 0.75)
                    && ic.getMagnification() > 1 / 32.0)
            {
                final int canvasWidth = ic.getWidth();
                ic.zoomOut(0, 0);
                // check if canvas size changed (otherwise stop zooming)
                if (canvasWidth == ic.getWidth())
                {
                    ic.zoomIn(0, 0);
                    break;
                }
            }
        }
        
        private void setupWidgets()
        {        
            // Combo box for choosing reference image
            referenceImageCombo = new JComboBox<>(imageNames);
            referenceImageCombo.setSelectedItem(data.referenceImage.getTitle());
            referenceImageCombo.setToolTipText("The reference / background image");
            referenceImageCombo.addItemListener(evt -> 
            {
                // work only when items are selected (not when they are unselected)
                if ((evt.getStateChange() & ItemEvent.SELECTED) == 0)
                {
                    return;
                }
                IJ.log("change reference image choice");
                
                threadPool.submit(() ->
                {
                    // retrieve reference image 
                    ImagePlus refImage = retrieveImage(referenceImageCombo.getSelectedIndex());
                    data.updateReferenceImage(refImage);
                    CustomWindow.this.setImage(data.displayImage);
                    adjustCurrentZoom();
                    CustomWindow.this.updateImage(refImage);
                });
            });
            
            // Combo box for choosing overlay image
            overlayImageCombo = new JComboBox<>(imageNames);
            overlayImageCombo.setToolTipText("The binary or label image to overlay");
            overlayImageCombo.addItemListener(evt -> 
            {
                // work only when items are selected (not when they are unselected)
                if ((evt.getStateChange() & ItemEvent.SELECTED) == 0)
                {
                    return;
                }
                
                threadPool.submit(() ->
                {
                    // retrieve overlay image 
                    ImagePlus ovrImage = retrieveImage(overlayImageCombo.getSelectedIndex());
                    data.updateOverlayImage(ovrImage);
                });
            });
            
            
            // label for drop down list
            overlayColorLabel = new JLabel("Overlay Color:");
            overlayColorNameCombo = new JComboBox<String>(CommonColors.getAllLabels());
            overlayColorNameCombo.setSelectedItem(CommonColors.RED.getLabel());
            overlayColorNameCombo.addItemListener(evt ->
            {
                // work only when items are selected (not when they are unselected)
                if ((evt.getStateChange() & ItemEvent.SELECTED) == 0)
                {
                    return;
                }
                
                threadPool.submit(() -> 
                {
                    // retrieve overlay color name 
                    String colorName = (String) overlayColorNameCombo.getSelectedItem();
                    
                    // parse color and update display
                    data.overlayColor = CommonColors.fromLabel(colorName).getColor();
                    data.updateDisplayImage();
                });
            });
            
            
            // Create widgets for opacity
            overlayOpacityLabel = new JLabel("Opacity");
            overlayOpacityLabel.setToolTipText("Overlay opacity, between 0 and 100");
            String opacityText = String.format(Locale.ENGLISH, "%4.1f", data.overlayOpacity * 100);
            overlayOpacityTextField = new JTextField(opacityText, 5);
            overlayOpacityTextField.setToolTipText("Opacity of overlay over original image");
            overlayOpacityTextField.addActionListener(evt ->
            {
                String text = overlayOpacityTextField.getText();
                double value;
                try 
                {
                    value = Double.parseDouble(text);
                }
                catch (Exception ex)
                {
                    return;
                }
                
                data.overlayOpacity = Math.min(Math.max(value, 0.0), 100.0) / 100.0;
                updateOverlayOpacity(data.overlayOpacity);
                data.updateDisplayImage();
            });
            
            int initialOpacity = (int) (data.overlayOpacity * 100.0);
            overlayOpacitySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, initialOpacity);
            overlayOpacitySlider.setToolTipText("Overlay opacity, between 0 and 100");
            overlayOpacitySlider.addChangeListener(evt ->
            {
                double value = overlayOpacitySlider.getValue();
                data.overlayOpacity = value / 100.0;
                updateOverlayOpacity(data.overlayOpacity);
                data.updateDisplayImage();
            });
            
            // setup widgets for overlay panel
            binaryOverlayCheckbox = new JCheckBox("Binary Overlay", data.binaryOverlay);
            binaryOverlayCheckbox.addActionListener(evt ->
            {
                boolean state = binaryOverlayCheckbox.isSelected();
                data.binaryOverlay = state;
                overlayColorLabel.setEnabled(state);
                overlayColorNameCombo.setEnabled(state);
                data.updateDisplayImage();
            });
            
            // create button for generating result image
            resultButton = new JButton("Create Image");
            resultButton.setToolTipText("Show Overlay result in new window");
            resultButton.addActionListener(evt -> threadPool.submit(() -> data.computeResultImage().show()));
        }
        
        private void buildLayout()
        {
            final ImageCanvas canvas = (ImageCanvas) getCanvas();
            
            // Create layout for input images panel
            JPanel inputImagesPanel = new JPanel();
            inputImagesPanel.setBorder(BorderFactory.createTitledBorder("Input Images"));
            GridBagLayout inputImagesPanelLayout = new GridBagLayout();
            GridBagConstraints inputImagesPanelConstraints = newConstraints();
            inputImagesPanel.setLayout(inputImagesPanelLayout);
            
            inputImagesPanel.add(new JLabel("Reference Image:"), inputImagesPanelConstraints);
            inputImagesPanelConstraints.gridy++;
            inputImagesPanel.add(referenceImageCombo, inputImagesPanelConstraints);
            inputImagesPanelConstraints.gridy++;
            inputImagesPanel.add(new JLabel("Overlay Image:"), inputImagesPanelConstraints);
            inputImagesPanelConstraints.gridy++;
            inputImagesPanel.add(overlayImageCombo, inputImagesPanelConstraints);
            
            // Create layout for overlay panel
            JPanel overlayPanel = new JPanel();
            overlayPanel.setBorder(BorderFactory.createTitledBorder("Overlay"));
            GridBagLayout overlayPanelLayout = new GridBagLayout();
            GridBagConstraints overlayPanelConstraints = newConstraints();
            overlayPanel.setLayout(overlayPanelLayout);
            
            overlayPanel.add(binaryOverlayCheckbox, overlayPanelConstraints);
            overlayPanelConstraints.gridy++;

            JPanel overlayColorPanel = new JPanel();
            overlayColorPanel.add(overlayColorLabel);
            overlayColorPanel.add(overlayColorNameCombo);
            overlayPanel.add(overlayColorPanel, overlayPanelConstraints);
            overlayPanelConstraints.gridy++;

            overlayOpacityPanel = new JPanel();
            overlayOpacityPanel.add(overlayOpacityLabel);
            overlayOpacityPanel.add(overlayOpacityTextField);
            overlayPanel.add(overlayOpacityPanel, overlayPanelConstraints);
            overlayPanelConstraints.gridy++;
            
            overlayPanel.add(overlayOpacitySlider, overlayPanelConstraints);
            overlayPanelConstraints.gridy++;

            
            // Create layout for results panel
            JPanel resultsPanel = new JPanel();
            resultsPanel.setBorder(BorderFactory.createTitledBorder("Result"));
            GridBagLayout resultsPanelLayout = new GridBagLayout();
            GridBagConstraints resultsPanelConstraints = newConstraints();
            resultsPanel.setLayout(resultsPanelLayout);
            
            resultsPanelConstraints.anchor = GridBagConstraints.CENTER;
            resultsPanelConstraints.fill = GridBagConstraints.BOTH;
            resultsPanel.add(resultButton, resultsPanelConstraints );

            
            // Parameter panel, left side of the GUI.
            // It includes three sub-panels: 
            // * Input Image, 
            // * Overlay,
            // * Result).
            GridBagLayout paramsLayout = new GridBagLayout();
            GridBagConstraints paramsConstraints = newConstraints();
            paramsConstraints.anchor = GridBagConstraints.CENTER;
            paramsConstraints.fill = GridBagConstraints.BOTH;
            paramsPanel.setLayout(paramsLayout);
            paramsPanel.add(inputImagesPanel, paramsConstraints);
            paramsConstraints.gridy++;
            paramsPanel.add(overlayPanel, paramsConstraints);
            paramsConstraints.gridy++;
            paramsPanel.add(resultsPanel, paramsConstraints);
            paramsConstraints.gridy++;


            // main panel (including parameters panel and canvas)
            GridBagLayout layout = new GridBagLayout();
            mainPanel.setLayout(layout);

            // put parameter panel in place
            GridBagConstraints allConstraints = newConstraints();
            allConstraints.anchor = GridBagConstraints.NORTH;
            allConstraints.fill = GridBagConstraints.BOTH;
            allConstraints.insets = new Insets(0, 0, 0, 0);
            mainPanel.add(paramsPanel, allConstraints);
            
            // put canvas in place
            allConstraints.gridx++;
            allConstraints.weightx = 1;
            allConstraints.weighty = 1;
            mainPanel.add(canvas, allConstraints);
            
            allConstraints.gridy++;
            allConstraints.weightx = 0;
            allConstraints.weighty = 0;

            // if the input image is 3d, put the
            // slice selectors in place
            if (super.sliceSelector != null)
            {
                sliceSelector.setValue(data.referenceImage.getCurrentSlice());
                data.displayImage.setSlice(data.referenceImage.getCurrentSlice());
                
                mainPanel.add(super.sliceSelector, allConstraints);
                
                if (null != super.zSelector)
                    mainPanel.add(super.zSelector, allConstraints);
                if (null != super.tSelector)
                    mainPanel.add(super.tSelector, allConstraints);
                if (null != super.cSelector)
                    mainPanel.add(super.cSelector, allConstraints);
            }
            allConstraints.gridy--;

            // setup the layout for the window (?)
            GridBagLayout wingb = new GridBagLayout();
            GridBagConstraints winc = new GridBagConstraints();
            winc.anchor = GridBagConstraints.NORTHWEST;
            winc.fill = GridBagConstraints.BOTH;
            winc.weightx = 1;
            winc.weighty = 1;
            setLayout(wingb);
            add(mainPanel, winc);
            
            // Fix minimum size to the preferred size at this point
            pack();
            setMinimumSize(getPreferredSize());
            
            // add special listener if the input image is a stack
            if (sliceSelector != null)
            {
                // add adjustment listener to the scroll bar
                sliceSelector.addAdjustmentListener(evt -> threadPool.submit(() -> data.updateDisplayImage()));
                
                // mouse wheel listener to update the rois while scrolling
                addMouseWheelListener(evt -> threadPool.submit(() -> data.updateDisplayImage()));
                
                // add key listener to the window and the canvas
                KeyListener keyListener = new UpdateDisplayKeyListener();
                this.addKeyListener(keyListener);
                canvas.addKeyListener(keyListener);
            }
        }
        
        /**
         * A local key listener attached to image Canvas that catches some key
         * events and optionally updates the display of preview image.
         */
        private class UpdateDisplayKeyListener extends KeyAdapter
        {
            @Override
            public void keyReleased(final KeyEvent evt)
            {
                if (isUpdateDisplayKeyCode(evt.getKeyCode()))
                {
                    threadPool.submit(() -> data.updateDisplayImage());
                }
            }
            
            private boolean isUpdateDisplayKeyCode(int keyCode)
            {
                if (keyCode == KeyEvent.VK_LEFT) return true;
                if (keyCode == KeyEvent.VK_RIGHT) return true;
                if (keyCode == KeyEvent.VK_LESS) return true;
                if (keyCode == KeyEvent.VK_GREATER) return true;
                if (keyCode == KeyEvent.VK_COMMA) return true;
                if (keyCode == KeyEvent.VK_PERIOD) return true;
                return false;
            }
        }

        
        /**
         * Creates a new set of constraints for GridBagLayout using default settings.
         * 
         * @return a new instance of GridBagConstraints
         */
        private GridBagConstraints newConstraints()
        {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.insets = new Insets(5, 5, 6, 6);
            return constraints;
        }
        
        public void updateOverlayOpacity(double newValue)
        {
            int intOpacity = (int) (newValue * 100);
            overlayOpacityTextField.setText(Double.toString(intOpacity));
            overlayOpacitySlider.setValue(intOpacity);
        }

        /**
         * Override windowClosing to display the input image after closing 
         * the GUI and shut down the executor service
         */
        @Override
        public void windowClosing(WindowEvent e)
        {
            super.windowClosing(e);
            this.data = null;
            
            // shut down executor service
            threadPool.shutdownNow();
        }
    }

    private ImagePlus retrieveImage(int index)
    {
        if (index < 0) 
        {
            return null;
        }
        String imageName = this.imageNames[index];
        return WindowManager.getImage(imageName);
    }
}
