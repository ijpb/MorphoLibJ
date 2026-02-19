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
 * 
 */
package inra.ijpb.data.image;

/**
 * A simple functional interface declaration to be used for populating 3D
 * images.
 * 
 * In general, it should not be necessary to reference the interface. Instead,
 * one can use lambda syntax. Example:
 * 
 * <pre>{@code
 * ImageStack image = ImageStack.create(7, 5, 3, 8);
 * ImageUtils.fill(image, (x, y, z) -> (x + y * 10.0 + z * 100.0));
 * }</pre>
 * 
 * 
 * @see inra.ijpb.data.image.ImageUtils#fill(ij.ImageStack, TriFunction)
 * 
 * @author dlegland
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R>
{
    /**
     * Applies the function to the triplet of input arguments.
     * 
     * @param t
     *            the first input argument
     * @param u
     *            the second input argument
     * @param v
     *            the third input argument
     * @return the result of the function evaluation at (t, u, v)
     */
    public R apply(T t, U u, V v);
}
