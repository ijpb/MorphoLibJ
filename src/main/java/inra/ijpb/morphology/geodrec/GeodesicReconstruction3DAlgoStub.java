/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
package inra.ijpb.morphology.geodrec;

import ij.IJ;
import inra.ijpb.algo.AlgoStub;

/**
 * <p>
 * Implementation basis for geodesic reconstruction algorithms for 3D images.
 * </p>
 * 
 * <p>
 * This class provides the management of the connectivity, several fields to
 * manage algorithm monitoring, and protected utility methods.
 * </p>
 * 
 * @author dlegland
 *
 */
public abstract class GeodesicReconstruction3DAlgoStub extends AlgoStub implements
		GeodesicReconstruction3DAlgo
{
	/**
	 * The connectivity of the algorithm, either 6 or 26.
	 */
	protected int connectivity = 6;
	
	/**
	 * Boolean flag for the display of debugging infos.
	 */
	public boolean verbose = false;
	
	/**
	 * Boolean flag for the display of algorithm state in ImageJ status bar
	 */
	public boolean showStatus = true;
	
	/**
	 * Boolean flag for the display of algorithm progress in ImageJ status bar
	 */
	public boolean showProgress = false; 

	
	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.geodrec.GeodesicReconstruction3DAlgo#getConnectivity()
	 */
	@Override
	public int getConnectivity()
	{
		return this.connectivity;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.geodrec.GeodesicReconstruction3DAlgo#setConnectivity(int)
	 */
	@Override
	public void setConnectivity(int conn)
	{
		this.connectivity = conn;
	}

	/**
	 * Displays the specified message in the status bar of the ImageJ frame, if
	 * the <code>showStatus</code> flag is true.
	 * 
	 * @param status
	 *            the message to display
	 */
	protected void showStatus(String status)
	{
		fireStatusChanged(this, status);

		if (this.showStatus) 
		{
			IJ.showStatus(status);
		}
	}

	/**
	 * Displays the current progression of the algorithm in the status bar of
	 * the ImageJ frame, if the <code>showProgress</code> flag is true.
	 * 
	 * @param current
	 *            the current progression
	 * @param max
	 *            the maximum possible value for progression
	 */            
	protected void showProgress(double current, double max)
	{
		fireProgressChanged(this, current, max);
		if (showProgress) 
		{
			IJ.showProgress(current / max);
		}
	}
	
	/**
	 * Displays the current progression of the algorithm in the status bar of
	 * the ImageJ frame, if the <code>showProgress</code> flag is true.
	 * 
	 * @param current
	 *            the current progression
	 * @param max
	 *            the maximum possible value for progression
	 * @param msg
	 *            an additional message that will be displayed in the console
	 */            
	protected void showProgress(double current, double max, String msg)
	{
		fireProgressChanged(this, current, max);
		if (showProgress) 
		{
			IJ.showProgress(current / max);
			if (msg != null && !msg.isEmpty())
			{
				trace(msg);
			}
		}
	}

	/**
	 * Display a trace message in the console, if the <code>verbose</code> flag
	 * is true.
	 * 
	 * @param traceMessage
	 *            the message to display
	 */
	protected void trace(String traceMessage)
	{
		// Display current status
		if (verbose) 
		{
			System.out.println(traceMessage);
		}
	}
}
