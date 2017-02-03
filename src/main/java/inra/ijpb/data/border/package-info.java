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
 * <p>Management of pixels outside image bounds.</p> 
 * 
 * <p>
 * Contains several classes for accessing values outside image bounds. 
 * Such options may be useful when filtering images, to avoid border effects.
 * </p>
 * <p> 
 * The global behavior is defined by the {@link inra.ijpb.data.border.BorderManager} interface. 
 * Implementations manage replication, mirroring, constant borders... 
 */
package inra.ijpb.data.border;

