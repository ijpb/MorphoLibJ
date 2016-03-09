/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.morphology.Strel3D;


/**
 * Implementation basis for structuring elements, that mainly manages a flag
 * for progress display.
 * 
 * @author David Legland
 *
 */
public abstract class AbstractStrel3D extends AlgoStub implements Strel3D
{
	// ===================================================================
	// Class variables
	
	/**
	 * Local flag indicating whether this algorithm should display progress or
	 * not. This can be used to toggle progress of nested strels operations.
	 */
	private boolean showProgress = true;

	
	// ===================================================================
	// Setter and getters
	
	public boolean showProgress()
	{
		return showProgress;
	}

	public void showProgress(boolean b)
	{
		this.showProgress = b;
	}


	// ===================================================================
	// Default implementation of some methods
	
	public ImageStack closing(ImageStack stack)
	{
		return this.reverse().erosion(this.dilation(stack));
	}

	public ImageStack opening(ImageStack stack)
	{
		return this.reverse().dilation(this.erosion(stack));
	}
	
	
	// ===================================================================
	// Management of progress status
	
	protected void fireProgressChanged(Object source, double step, double total)
	{
		if (showProgress)
			super.fireProgressChanged(source, step, total);
	}

	protected void fireProgressChanged(AlgoEvent evt)
	{
		if (showProgress)
			super.fireProgressChanged(evt);
	}

	protected void fireStatusChanged(Object source, String message)
	{
		if (showProgress)
			super.fireStatusChanged(source, message);
	}

	protected void fireStatusChanged(AlgoEvent evt)
	{
		if (showProgress)
			super.fireStatusChanged(evt);
	}
}
