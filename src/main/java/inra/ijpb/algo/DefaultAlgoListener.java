/**
 * 
 */
package inra.ijpb.algo;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.strel.SquareStrel;

/**
 * Utility class that catches algorithm events and displays them either on ImageJ
 * main Frame.<p>
 *  
 * Example of use:
 * <pre><code>
 * // init image and process 
 * ImageProcessor image = ...
 * Strel strel = SquareStrel.fromDiameter(15);
 * 
 * // Add monitoring of the process  
 * DefaultAlgoListener.monitor(strel);
 * 
 * // run process. The IJ frame will display progress
 * strel.dilation(image);
 * </code></pre>
 * @author David Legland
 *
 */
public class DefaultAlgoListener implements AlgoListener
{
	/**
	 * Static method that creates a new instance of DefaultAlgoListener, and 
	 * add it to the given algorithm.
	 * 
	 * 
	 * <pre><code>
	 * // read demo image
	 * String imageURL = "http://imagej.nih.gov/ij/images/NileBend.jpg";
	 * ImageProcessor image = IJ.openImage(imageURL).getProcessor();
	 * // init processor class
	 * Strel strel = SquareStrel.fromDiameter(15);
	 * // Add monitoring of the process  
	 * DefaultAlgoListener.monitor(strel);
	 * // run process. The IJ frame will display progress
	 * strel.dilation(image);
	 * </code></pre>
	 * 
	 * @param algo the algorithm to monitor
	 */
	public static final void monitor(Algo algo)
	{
		DefaultAlgoListener dal = new DefaultAlgoListener();
		algo.addAlgoListener(dal);
	}
	
	/* (non-Javadoc)
	 * @see inra.ijpb.event.AlgoListener#algoProgressChanged(inra.ijpb.event.AlgoEvent)
	 */
	@Override
	public void algoProgressChanged(AlgoEvent evt)
	{
		IJ.showProgress(evt.getProgressRatio());
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.event.AlgoListener#algoStatusChanged(inra.ijpb.event.AlgoEvent)
	 */
	@Override
	public void algoStatusChanged(AlgoEvent evt) 
	{
		IJ.showStatus(evt.getStatus());
	}

	/**
	 * Sample program demonstrating the use of DefaultAlgoListener.
	 */
	public static final void main(String[] args) 
	{
		new ImageJ();
		
		ImagePlus imagePlus = IJ.openImage("http://imagej.nih.gov/ij/images/NileBend.jpg");
		 
		imagePlus.show("Input");
		
		ImageProcessor image = imagePlus.getProcessor();
		
		Strel strel = SquareStrel.fromDiameter(21);
		DefaultAlgoListener.monitor(strel);
		
		ImageProcessor result = Morphology.dilation(image, strel);
		ImagePlus resultPlus = new ImagePlus("Result", result);
		resultPlus.show("Result");

		System.out.println("done.");
	}
}
