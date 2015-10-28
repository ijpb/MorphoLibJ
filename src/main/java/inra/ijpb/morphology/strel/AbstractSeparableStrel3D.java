/**
 * 
 */
package inra.ijpb.morphology.strel;

import ij.ImageStack;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;

import java.util.Collection;

/**
 * Implementation stub for separable 3D Structuring elements.
 * 
 * @author David Legland
 *
 */
public abstract class AbstractSeparableStrel3D extends AbstractStrel3D
		implements SeparableStrel3D, AlgoListener 
{
	public ImageStack dilation(ImageStack stack) 
	{
		// Allocate memory for result
		ImageStack result = stack.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel3D> strels = this.decompose();
		int n = strels.size();
		
		// Dilation
		int i = 1;
		for (InPlaceStrel3D strel : strels) 
		{
			fireStatusChanged(this, "Dilation " + (i++) + "/" + n);
			runDilation(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageStack erosion(ImageStack stack) 
	{
		// Allocate memory for result
		ImageStack result = stack.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel3D> strels = this.decompose();
		int n = strels.size();
		
		// Erosion
		int i = 1;
		for (InPlaceStrel3D strel : strels) 
		{
			fireStatusChanged(this, "Erosion " + (i++) + "/" + n);
			runErosion(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageStack closing(ImageStack stack) 
	{
		// Allocate memory for result
		ImageStack result = stack.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel3D> strels = this.decompose();
		int n = strels.size();
		
		// Dilation
		int i = 1;
		for (InPlaceStrel3D strel : strels) 
		{
			fireStatusChanged(this, "Dilation " + (i++) + "/" + n);
			runDilation(result, strel);
		}
		
		// Erosion (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel3D strel : strels) 
		{
			fireStatusChanged(this, "Erosion " + (i++) + "/" + n);
			runErosion(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageStack opening(ImageStack stack) 
	{
		// Allocate memory for result
		ImageStack result = stack.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel3D> strels = this.decompose();
		int n = strels.size();
		
		// Erosion
		int i = 1;
		for (InPlaceStrel3D strel : strels) 
		{
			fireStatusChanged(this, "Erosion " + (i++) + "/" + n);
			runErosion(result, strel);
		}
		
		// Dilation (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel3D strel : strels) 
		{
			fireStatusChanged(this, "Dilation " + (i++) + "/" + n);
			runDilation(result, strel);
		}
		
		// clear status bar
		fireStatusChanged(this, "");

		return result;
	}
	
	private void runDilation(ImageStack image, InPlaceStrel3D strel) 
	{
		strel.showProgress(this.showProgress());
		strel.addAlgoListener(this);
		strel.inPlaceDilation(image);
		strel.removeAlgoListener(this);
	}
	
	private void runErosion(ImageStack image, InPlaceStrel3D strel) 
	{
		strel.showProgress(this.showProgress());
		strel.addAlgoListener(this);
		strel.inPlaceErosion(image);
		strel.removeAlgoListener(this);
	}
	
	/**
	 * Propagates the event by changing the source.
	 */
	public void algoProgressChanged(AlgoEvent evt) 
	{
		this.fireProgressChanged(this, evt.getCurrentProgress(), evt.getTotalProgress());
	}

	/**
	 * Propagates the event by changing the source.
	 */
	public void algoStatusChanged(AlgoEvent evt) 
	{
		this.fireStatusChanged(this, evt.getStatus());
	}
}
