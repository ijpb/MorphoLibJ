/**
 * 
 */
package inra.ijpb.event;

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
public class DefaultAlgoListener implements ProgressListener, StatusListener
{

	public static final void monitor(Algo algo)
	{
		DefaultAlgoListener dal = new DefaultAlgoListener();
		algo.addProgressListener(dal);
		algo.addStatusListener(dal);
	}
	
	/* (non-Javadoc)
	 * @see inra.ijpb.event.StatusListener#statusChanged(inra.ijpb.event.StatusEvent)
	 */
	@Override
	public void statusChanged(StatusEvent evt)
	{
		IJ.showStatus(evt.getMessage());
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.event.ProgressListener#progressChanged(inra.ijpb.event.ProgressEvent)
	 */
	@Override
	public void progressChanged(ProgressEvent evt)
	{
		IJ.showProgress(evt.getProgressRatio());
	}

	/**
	 * Sample program demonstrating the use of DefaultAlgoListener.
	 */
	public static final void main(String[] args) 
	{
		new ImageJ();
		
//		ImagePlus imagePlus = IJ.openImage("./src/test/resources/files/2432a_corr.png");
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
