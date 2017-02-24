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
package inra.ijpb.morphology.geodrec;

import inra.ijpb.algo.AlgoStub;

/**
 * Implementation basis for geodesic reconstruction algorithms for planar images.
 * 
 * @author dlegland
 *
 */
public abstract class GeodesicReconstructionAlgoStub extends AlgoStub implements
		GeodesicReconstructionAlgo
{
	/**
	 * The connectivity of the algorithm, either 4 or 8.
	 */
	protected int connectivity = 4;
	
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
	 * @see inra.ijpb.morphology.geodrec.GeodesicReconstructionAlgo#getConnectivity()
	 */
	@Override
	public int getConnectivity()
	{
		return this.connectivity;
	}

	/* (non-Javadoc)
	 * @see inra.ijpb.morphology.geodrec.GeodesicReconstructionAlgo#setConnectivity(int)
	 */
	@Override
	public void setConnectivity(int conn)
	{
		this.connectivity = conn;
	}

}
