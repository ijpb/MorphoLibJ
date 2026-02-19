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
/**
 * <p>Geodesic reconstruction by erosion and dilation.</p>
 * 
 * <p>This package provides interfaces for morphological reconstructions algorithms,
 * and several implementations.</p>
 *
 * <p>
 * For geodesic reconstruction on planar images, the class GeodesicReconstructionHybrid.
 * </p>
 * 
 * <p>
 * For geodesic reconstruction on 3D stacks, the class to use depends on the data type:
 * <ul>
 * <li>GeodesicReconstruction3DHybrid0Float for floating point images</li>
 * <li>GeodesicReconstruction3DHybrid0Gray8 for images with 255 gray levels</li>
 * <li>GeodesicReconstructionByDilation3DScanning and GeodesicReconstructionByErosion3DScanning for 16 bits images</li>
 * </ul> 
 */
package inra.ijpb.morphology.geodrec;


