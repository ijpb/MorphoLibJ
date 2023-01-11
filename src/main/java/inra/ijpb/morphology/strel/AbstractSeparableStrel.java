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
package inra.ijpb.morphology.strel;

import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;

import java.util.Collection;

/**
 * Implementation stub for separable Structuring elements.
 * @author David Legland
 *
 */
public abstract class AbstractSeparableStrel extends AbstractStrel 
implements SeparableStrel, AlgoListener 
{
	public ImageProcessor dilation(ImageProcessor image)
	{
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel> strels = this.decompose();
		int n = strels.size();
		
		
		// Dilation
		int i = 1;
		for (InPlaceStrel strel : strels)
		{
			fireStatusChanged(this, createStatusMessage("Dilation", i, n));
			runDilation(result, strel);
			i++;
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageProcessor erosion(ImageProcessor image) 
	{
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		// Extract structuring elements
		Collection<InPlaceStrel> strels = this.decompose();
		int n = strels.size();
		
		// Erosion
		int i = 1;
		for (InPlaceStrel strel : strels)
		{
			fireStatusChanged(this, createStatusMessage("Erosion", i, n));
			runErosion(result, strel);
			i++;
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
		return result;
	}

	public ImageProcessor closing(ImageProcessor image)
	{
        // Allocate memory for result (padding borders to avoid edge effects)
        ImageProcessor result = this.addBorder(image);
		
		// Extract structuring elements
		Collection<InPlaceStrel> strels = this.decompose();
		int n = strels.size();
		
		// Dilation
		int i = 1;
		for (InPlaceStrel strel : strels)
		{
			fireStatusChanged(this, createStatusMessage("Dilation", i, n));
			runDilation(result, strel);
			i++;
		}
		
		// Erosion (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel strel : strels)
		{
			fireStatusChanged(this, createStatusMessage("Erosion", i, n));
			runErosion(result, strel);
			i++;
		}
		
		// clear status bar
		fireStatusChanged(this, "");
		
        return cropBorder(result);
	}

	public ImageProcessor opening(ImageProcessor image) 
	{
		// Allocate memory for result (padding borders to avoid edge effects)
	    ImageProcessor result = this.addBorder(image);
        
		// Extract structuring elements
		Collection<InPlaceStrel> strels = this.decompose();
		int n = strels.size();
		
		// Erosion
		int i = 1;
		for (InPlaceStrel strel : strels)
		{
			fireStatusChanged(this, createStatusMessage("Erosion", i, n));
			runErosion(result, strel);
			i++;
		}
		
		// Dilation (with reversed strel)
		i = 1;
		strels = this.reverse().decompose();
		for (InPlaceStrel strel : strels) 
		{
			fireStatusChanged(this, createStatusMessage("Dilation", i, n));
			runDilation(result, strel);
			i++;
		}
		
		// clear status bar
		fireStatusChanged(this, "");

        return cropBorder(result);
	}
	
	private void runDilation(ImageProcessor image, InPlaceStrel strel)
	{
		strel.showProgress(this.showProgress());
		strel.addAlgoListener(this);
		strel.inPlaceDilation(image);
		strel.removeAlgoListener(this);
	}
	
	private void runErosion(ImageProcessor image, InPlaceStrel strel) 
	{
		strel.showProgress(this.showProgress());
		strel.addAlgoListener(this);
		strel.inPlaceErosion(image);
		strel.removeAlgoListener(this);
	}
	
	private String createStatusMessage(String opName, int i, int n)
	{
		String channel = this.getChannelName();
		if (channel == null)
			return opName + " " + i + "/" + n;
		else
			return opName + " " + channel + " " + i + "/" + n;
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
