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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImageJ;

public class InteractiveMarkerControlledWatershedTest {
	/**
	 * Main method to test and debug the Interactive
	 * Marker-controlled Watershed plugin GUI
	 *
	 * @param args
	 */
	public static void main( final String[] args )
	{
		ImageJ.main( args );

		IJ.open(
				InteractiveMarkerControlledWatershed.class.getResource(
						"/files/grains.tif" ).getFile() );
		IJ.run("Find Edges");

		new InteractiveMarkerControlledWatershed().run( null );
	}
}
