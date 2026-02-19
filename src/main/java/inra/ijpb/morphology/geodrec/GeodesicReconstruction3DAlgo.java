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
package inra.ijpb.morphology.geodrec;

import ij.ImageStack;
import inra.ijpb.algo.Algo;

/**
 * Defines the interface for geodesic reconstruction algorithms applied to
 * 3D stack images.
 * 
 * @author David Legland
 */
public interface GeodesicReconstruction3DAlgo extends Algo 
{
	/**
	 * Applies the geodesic reconstruction algorithm to the input marker and
	 * mask images.
	 * 
	 * @param marker image used to initialize the reconstruction
	 * @param mask image used to constrain the reconstruction
	 * @return the geodesic reconstruction of marker image constrained by mask image
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask);
	
	/**
	 * Applies the geodesic reconstruction algorithm to the input marker and
	 * mask images, restricted by a binary mask.
	 * 
	 * @param marker image used to initialize the reconstruction
	 * @param mask image used to constrain the reconstruction
	 * @param binaryMask binary mask to restrict the region of application
	 * @return the geodesic reconstruction of marker image constrained by mask image
	 */
	public ImageStack applyTo(ImageStack marker, ImageStack mask, ImageStack binaryMask );
	
	/**
	 * Returns the chosen connectivity of the algorithm, either 6 or 26.
	 * 
	 * @return the current connectivity for this algorithm
	 */
	public int getConnectivity();

	/**
	 * Changes the connectivity of the algorithm to either 6 or 26. 
	 * 
	 * @param conn the 3D connectivity to use, either 6 or 26 
	 */
	public void setConnectivity(int conn);
}
