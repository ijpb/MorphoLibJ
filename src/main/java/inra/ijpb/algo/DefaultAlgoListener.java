/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
package inra.ijpb.algo;

import ij.IJ;

/**
 * Utility class that catches algorithm events and displays them on ImageJ
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
}
