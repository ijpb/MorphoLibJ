package inra.ijpb.plugins;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;
import ij.plugin.PlugIn;

public class MorphologicalSegmentation implements PlugIn {

	/** original input image */
	ImagePlus inputImage;
	/** image to be displayed in the GUI */
	ImagePlus displayImage;
	/** GUI window */
	private CustomWindow win;
	
	
	
	/**
	 * Custom window to define the plugin GUI
	 */
	private class CustomWindow extends StackWindow
	{
		/**
		 * serial version uid
		 */
		private static final long serialVersionUID = -6201855439028581892L;
		
		/** parameters panel */
		JPanel paramsPanel = new JPanel();
		/** main panel */
		Panel all = new Panel();
		
		/** segmentation button */
		JButton segmentButton;
		
		/**
		 * Construct the plugin window
		 * 
		 * @param imp input image
		 */
		CustomWindow( ImagePlus imp )
		{
			super(imp, new ImageCanvas(imp));
			
			final ImageCanvas canvas = (ImageCanvas) getCanvas();
			
			setTitle("Morphological Segmentation");
			
			segmentButton = new JButton( "Segment" );
			segmentButton.setToolTipText( "Run the morphological segmentation" );
			
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

			paramsPanel.add( segmentButton, paramsConstraints );
			
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
			all.add( canvas, allConstraints);
			
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
/*
			allConstraints.gridx++;
			allConstraints.anchor = GridBagConstraints.NORTHEAST;
			allConstraints.weightx = 0;
			allConstraints.weighty = 0;
			allConstraints.gridheight = 2;
			all.add(annotationsPanel, allConstraints);
*/
			GridBagLayout wingb = new GridBagLayout();
			GridBagConstraints winc = new GridBagConstraints();
			winc.anchor = GridBagConstraints.NORTHWEST;
			winc.fill = GridBagConstraints.BOTH;
			winc.weightx = 1;
			winc.weighty = 1;
			setLayout( wingb );
			add( all, winc );
			
			
		}
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
