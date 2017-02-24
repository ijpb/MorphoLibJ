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
package inra.ijpb.morphology.directional;

/**
 * Factory for oriented linear structuring elements.
 * 
 * @author David Legland
 *
 */
public class OrientedLineStrelFactory implements OrientedStrelFactory 
{
	/** 
	 * The Euclidean length of the discrete line 
	 */
	double length;

	/**
	 * Constructs a new oriented line factory, that will generate lines with
	 * approximately the given length.
	 * 
	 * @param length
	 *            the length of the lines to be generated
	 */
	public OrientedLineStrelFactory(double length)
	{
		this.length = length;
	}
	
	/**
	 * Creates a new instance of OrientedLineStrel with the length stored
	 * internally and the orientation given as argument.
	 * 
	 * @see inra.ijpb.morphology.directional.OrientedStrelFactory#createStrel(double)
	 */
	@Override
	public OrientedLineStrel createStrel(double theta) 
	{
		return new OrientedLineStrel(this.length, theta);
	}

}
