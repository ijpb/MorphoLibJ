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
package inra.ijpb.binary.geodesic;

import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.algo.Algo;

/**
 * Interface for computing geodesic diameter of a set of binary or labeled
 * particles or regions. The data types for computation and algorithm
 * implementation are left to implementations.
 * 
 * <p>
 * Example of use:
 *
 * <pre>
 * {@code
 * 	ChamferWeights weights = ChamferWeights.CHESSKNIGHT;
 * 	GeodesicDiameter gd = new GeodesicDiameterFloat(weights);
 * 	ResultsTable table = gd.analyseImage(inputLabelImage);
 * 	table.show("Geodesic Diameter");
 * }
 * </pre>
 *
 * @deprecated since 1.3.5, use inra.ijpb.measure.region2d.GeodesicDiameter instead 
 * @see inra.ijpb.measure.region2d.GeodesicDiameter
 * 
 * @author dlegland
 */
@Deprecated
public interface GeodesicDiameter extends Algo
{
	/**
	 * computes geodesic diameter for each region of the input image.
	 * 
	 * @param labelImage
	 *            the label image containing regions.
	 * @return a ResultsTable containing the geodesic diameter of each region.
	 */
	public abstract ResultsTable analyzeImage(ImageProcessor labelImage);
}
