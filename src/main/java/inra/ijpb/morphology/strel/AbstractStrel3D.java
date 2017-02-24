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
package inra.ijpb.morphology.strel;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.morphology.Strel3D;


/**
 * Implementation basis for 3D structuring elements
 * 
 * @author David Legland
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
