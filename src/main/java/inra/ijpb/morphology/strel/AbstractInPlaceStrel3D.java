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
package inra.ijpb.morphology.strel;

import ij.ImageStack;

/**
 * Implementation stub for in place 3D Structuring elements. Implements
 * operations methods by calling in-place versions.
 * 
 * @author David Legland
 *
 */
public abstract class AbstractInPlaceStrel3D extends AbstractStrel3D implements
		InPlaceStrel3D
{

	public ImageStack dilation(ImageStack stack)
	{
		ImageStack result = stack.duplicate();
		this.inPlaceDilation(result);
		return result;
	}

	public ImageStack erosion(ImageStack stack)
	{
		ImageStack result = stack.duplicate();
		this.inPlaceErosion(result);
		return result;
	}

	public ImageStack closing(ImageStack stack)
	{
		ImageStack result = stack.duplicate();
		this.inPlaceDilation(result);
		this.reverse().inPlaceErosion(result);
		return result;
	}

	public ImageStack opening(ImageStack stack)
	{
		ImageStack result = stack.duplicate();
		this.inPlaceErosion(result);
		this.reverse().inPlaceDilation(result);
		return result;
	}
}
