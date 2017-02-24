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
/**
 * <p>Computations of distance maps on binary images, using chamfer distances
 * (integer approximation of Euclidean distances).</p>
 * 
 * <p>Contains implementations for computation using shorts or float values, 
 * for 3x3 and 5x5 neighborhoods.</p>
 * 
 * <p>
 * Example of use:
 * <pre><code>
 *	short[] shortWeights = ChamferWeights.CHESSKNIGHT.getShortWeights();
 *	boolean normalize = true;
 *	DistanceTransform dt = new DistanceTransform5x5Short(shortWeights, normalize);
 *	ImageProcessor result = dt.distanceMap(inputImage);
 *	// or:
 *	ImagePlus resultPlus = BinaryImages.distanceMap(imagePlus, shortWeights, normalize);
 * </code></pre>
 * 
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageProcessor, short[], boolean)
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageProcessor, float[], boolean)
 * @see inra.ijpb.binary.distmap.DistanceTransform
 */
package inra.ijpb.binary.distmap;

