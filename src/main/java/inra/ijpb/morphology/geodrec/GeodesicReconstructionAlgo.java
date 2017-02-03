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

import ij.process.ImageProcessor;

/**
 * <p>
 * Defines the interface for geodesic reconstructions algorithms applied to
 * planar images.
 * </p>
 * 
 * <p>
 * There are currently four implementation of geodesic reconstruction for planar
 * images:
 * <ul>
 * <li>GeodesicReconstructionByDilation: implements reconstruction by dilation,
 * using scanning algorithm</li>
 * <li>GeodesicReconstructionByErosion: implements reconstruction by erosion,
 * using scanning algorithm</li>
 * <li>GeodesicReconstructionScanning: implements reconstruction by dilation or
 * erosion, using scanning algorithm.</li>
 * <li>GeodesicReconstructionHybrid: implements reconstruction by dilation or
 * erosion, using a classical forward pass, a backward pass that initialize a
 * processing queue, and processes each pixel in the queue until it is empty.</li>
 * </ul>
 * 
 * The most versatile one is the "Hybrid" version.
 * 
 * @author David Legland
 */
public interface GeodesicReconstructionAlgo 
{
	/**
	 * Applies the geodesic reconstruction algorithm to the input marker and
	 * mask images.
	 * @param marker image used to initialize the reconstruction
	 * @param mask image used to constrain the reconstruction
	 * @return the geodesic reconstruction of marker image constrained by mask image
	 */
	public ImageProcessor applyTo(ImageProcessor marker, ImageProcessor mask);
	
	/**
	 * Returns the chosen connectivity of the algorithm, either 4 or 8. 
	 * 
	 * @return the current connectivity for this algorithm
	 */
	public int getConnectivity();

	/**
	 * Changes the connectivity of the algorithm to either 4 or 8.
	 * 
	 * @param conn the connectivity to use, either 4 or 8
	 */
	public void setConnectivity(int conn);
}
