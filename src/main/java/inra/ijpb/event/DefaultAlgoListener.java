/**
 * 
 */
package inra.ijpb.event;

import ij.IJ;

/**
 * <p>Utility class that catches algorithm events and displays them either on ImageJ
 * main Frame.</p>
 *  
 * <p>
 * Example of use:
 * <code><pre>
 * // init image and process 
 * ImageProcessor image = ...
 * Strel strel = SquareStrel.fromDiameter(15);
 * 
 * // Add monitoring of the process  
 * DefaultAlgoListener.monitor(strel);
 * 
 * // run process. The IJ frame will display progress
 * strel.dilation(image);
 * </pre></code>
 * </p>
 * @author David Legland
 *
 */
public class DefaultAlgoListener implements ProgressListener, StatusListener
{

	public static final void monitor(AlgoStub algo)
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

}
