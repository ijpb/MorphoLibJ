/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
 * <p>A collection of Structuring Element ("Strel") implementations for mathematical morphology.</p> 
 * 
 * Contains the implementation of several types of structuring elements. 
 * The packages tries to takes advantage of the separability property of most 
 * structuring elements.<p>
 * 
 * The package can be divided into:
 * <ul>
 * <li>Specialization Strel interfaces: {@link inra.ijpb.morphology.strel.SeparableStrel}, 
 * 	{@link inra.ijpb.morphology.strel.InPlaceStrel}</li>
 * <li>Abstract classes for facilitating implementations: {@link inra.ijpb.morphology.strel.AbstractStrel}, 
 * 	{@link inra.ijpb.morphology.strel.AbstractSeparableStrel}, 
 * 	{@link inra.ijpb.morphology.strel.AbstractInPlaceStrel}</li>
 * <li>Final Strel implementations: {@link inra.ijpb.morphology.strel.SquareStrel}, 
 * 	{@link inra.ijpb.morphology.strel.OctagonStrel}, {@link inra.ijpb.morphology.strel.DiamondStrel}, 
 * 	{@link inra.ijpb.morphology.strel.Cross3x3Strel}...</li>
 * <li>Utility classes that manage local extremum: {@link inra.ijpb.morphology.strel.LocalExtremum}, 
 * 	{@link inra.ijpb.morphology.strel.LocalExtremumBufferGray8},
 * {@link inra.ijpb.morphology.strel.LocalExtremumBufferDouble}</li> 
 * </ul>
 */
package inra.ijpb.morphology.strel;


