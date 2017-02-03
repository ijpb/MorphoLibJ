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
 * <p>Mathematical Morphology filters and reconstructions.</p>
 * 
 * <p>This package contains several classes for computing various morphological 
 * filtering operations (such as dilation, erosion, opening...) 
 * as well as plugins based on geodesic reconstruction of images.</p>
 * 
 * <p><strong>Morphological filtering</strong> involves structuring elements of various shapes. 
 * Most of the processing is done by the structuring elements themselves.
 * The interface {@link inra.ijpb.morphology.Strel} defines the general contract 
 * for structuring element, and the class {@link inra.ijpb.morphology.Morphology}
 * contains static methods corresponding to each classical operation.</p>
 * 
 * <p>The class {@link inra.ijpb.morphology.GeodesicReconstruction} performs 
 * <strong>morphological geodesic reconstruction</strong> of a grayscale marker
 * image within a grayscale mask image. 
 * This class is used by the two plugins 
 * {@link inra.ijpb.plugins.FillHolesPlugin}
 * and {@link inra.ijpb.plugins.KillBordersPlugin}. 
 * More specialized algorithms are provided in the 
 * <code><a href="{@docRoot}/inra/ijpb/morphology/geodrec/package-summary.html">geodrec</a></code> package</p>
 * 
 * <p>Another common task is the detection of regional minima or maxima. 
 * A more powerful approach is to use extended minima or maxima, by specifying
 * a parameter of dynamic that allows to focus on relevant extrema. Static 
 * methods are provided in the {@link inra.ijpb.morphology.MinimaAndMaxima} and
 * {@link inra.ijpb.morphology.MinimaAndMaxima3D} classes. 
 * </p>
 */
package inra.ijpb.morphology;


