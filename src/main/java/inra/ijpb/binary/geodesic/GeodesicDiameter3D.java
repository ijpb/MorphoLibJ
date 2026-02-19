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
package inra.ijpb.binary.geodesic;

import ij.ImageStack;
import ij.measure.ResultsTable;
import inra.ijpb.algo.Algo;

/**
 * Interface for computing geodesic diameter of a set of 3D binary or labeled
 * particles or regions. The data types for computation and algorithm
 * implementation are left to implementations.
 * 
 * <p>
 * Example of use:
 *<pre>{@code
 *	GeodesicDiameterFloat3D gd3d = new GeodesicDiameter3DFloat(ChamferWeights3D.BORGEFORS);
 *	ResultsTable table = gd3d.process(inputLabelImage);
 *	table.show("Geodesic Diameter 3D");
 *}</pre>
 *
 *
 * @deprecated since 1.3.5, use inra.ijpb.measure.region3d.GeodesicDiameter3D instead 
 * @see inra.ijpb.measure.region3d.GeodesicDiameter3D
 * 
 * @author dlegland
 */
public interface GeodesicDiameter3D extends Algo
{
	/**
	 * computes geodesic diameter for each region of the input image.
	 * 
	 * @param labelImage
	 *            the label image containing regions.
	 * @return a ResultsTable containing the geodesic diameter of each region.
	 */
	public abstract ResultsTable process(ImageStack labelImage);
}
