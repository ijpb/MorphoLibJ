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
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.Connectivity3D;
import inra.ijpb.morphology.attrfilt.Attribute3D;
import inra.ijpb.morphology.attrfilt.AttributeFilterType;

/**
 * Plugin to perform between attribute opening, closing, and black or white
 * top-hat on a 3D grayscale image. The size criterion is the number of voxels. 
 *
 * @see AreaOpeningPlugin
 *
 * @author David Legland, Ignacio Arganda-Carreras
 *
 */
public class GrayscaleAttributeFiltering3D implements PlugIn
{
	static AttributeFilterType operation = AttributeFilterType.OPENING;
	static Attribute3D attribute = Attribute3D.VOLUME;
    static int nVoxelMin = 100;
    static Connectivity3D connectivity = Connectivity3D.C6;

	/**
	 * Plugin run method
	 */
	@Override
	public void run(String arg0)
	{
		ImagePlus imagePlus = IJ.getImage();

		if( imagePlus.getImageStackSize() < 2 )
		{
			IJ.error( "Gray Scale Attribute Filtering 3D",
					"Input image must be 3D" );
			return;
		}

        // create the dialog, with operator options
		String title = "Gray Scale Attribute Filtering 3D";
        GenericDialog gd = new GenericDialog( title );
		gd.addChoice("Operation", AttributeFilterType.getAllLabels(), operation.getLabel());
		gd.addChoice("Attribute", Attribute3D.getAllLabels(), attribute.getLabel());
        String label = "Min Voxel Number:";
        gd.addNumericField(label, nVoxelMin, 0);
        gd.addChoice("Connectivity", Connectivity3D.getAllLabels(), connectivity.name());
        gd.showDialog();

        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        // read options
        operation = AttributeFilterType.fromLabel( gd.getNextChoice() );
        attribute = Attribute3D.fromLabel( gd.getNextChoice() );
        nVoxelMin = (int) gd.getNextNumber();
        connectivity = Connectivity3D.fromLabel(gd.getNextChoice());

		// create and configure the operator
		inra.ijpb.morphology.attrfilt.GrayscaleAttributeFiltering3D op = new inra.ijpb.morphology.attrfilt.GrayscaleAttributeFiltering3D();
		op.setFilterType(operation);
		op.setAttribute(attribute);
		op.setMinimumValue(nVoxelMin);
		op.setConnectivity(connectivity);
		
		// add algorithm monitoring
		DefaultAlgoListener.monitor(op);
		
		// run the operator
		final ImageStack image = imagePlus.getStack();
		ImageStack result = op.process(image);

		// create and configure resulting ImagePlus 
        String newName = imagePlus.getShortTitle() + "-attrFilt";
        ImagePlus resultPlus = new ImagePlus(newName, result);
		resultPlus.copyScale(imagePlus);
		Images3D.optimizeDisplayRange(resultPlus);
		resultPlus.updateAndDraw();

		// show result
		resultPlus.show();
		resultPlus.setSlice(imagePlus.getCurrentSlice());
	}
}
