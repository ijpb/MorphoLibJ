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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel3D;


public class TestInteractiveMarkerControlledWatershedSegmentation {

	/**
	 * Main method to test and debug the Interactive Marker-controlled
	 * Watershed Segmentation GUI
	 *
	 * @param args
	 */
	public static void main( final String[] args )
	{
		ImageJ.main( args );

		ImagePlus img = IJ.openImage(
				TestInteractiveMarkerControlledWatershedSegmentation.class.getResource(
						"/files/bat-cochlea-volume.tif" ).getFile() );
		Strel3D strel = Strel3D.Shape.CUBE.fromRadius( 1 );
		(new ImagePlus( "gradient",
				Morphology.gradient( img.getStack(), strel ) ) ).show();

		new InteractiveMarkerControlledWatershedSegmentation().run( null );
	}

}
