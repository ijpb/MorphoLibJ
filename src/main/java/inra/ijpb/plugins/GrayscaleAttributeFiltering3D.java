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
package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.AttributeFiltering;

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
	/**
	 * Morphological operations that can be done using this plugin.
	 */
	enum Operation
	{
		CLOSING("Closing"), 
		OPENING("Opening"), 
		TOP_HAT("Top Hat"), 
		BOTTOM_HAT("Bottom Hat");

		String label;

		Operation(String label)
		{
			this.label = label;
		}

		public static String[] getAllLabels()
		{
			int n = Operation.values().length;
			String[] result = new String[n];

			int i = 0;
			for (Operation op : Operation.values())
			{
				result[i++] = op.label;
			}
			return result;
		}

		/**
		 * Determines the operation type from its label.
		 *
		 * @param opLabel
		 *            the label of the operation
		 * @return the parsed Operation
		 * @throws IllegalArgumentException
		 *             if label is not recognized.
		 */
		public static Operation fromLabel(String opLabel)
		{
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Operation op : Operation.values())
			{
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Operation with "
					+ "label: " + opLabel);
		}
	};
	
	/**
	 * Attributes that can be used as filtering criterion in the plugin.
	 */
	enum Attribute
	{
		VOLUME("Volume");

		String label;

		Attribute(String label)
		{
			this.label = label;
		}

		public static String[] getAllLabels()
		{
			int n = Attribute.values().length;
			String[] result = new String[n];

			int i = 0;
			for (Attribute att : Attribute.values())
			{
				result[i++] = att.label;
			}
			return result;
		}

		/**
		 * Determines the Attribute type from its label.
		 *
		 * @param opLabel
		 *            the label of the Attribute
		 * @return the parsed Attribute
		 * @throws IllegalArgumentException
		 *             if label is not recognized.
		 */
		public static Attribute fromLabel(String attrLabel)
		{
			if (attrLabel != null)
				attrLabel = attrLabel.toLowerCase();
			for (Attribute op : Attribute.values())
			{
				String cmp = op.label.toLowerCase();
				if (cmp.equals(attrLabel))
					return op;
			}
			throw new IllegalArgumentException( "Unable to parse Attribute with "
					+ "label: " + attrLabel );
		}
	};

	static Operation operation = Operation.OPENING;
	static Attribute attribute = Attribute.VOLUME;
    static int nPixelMin = 100;
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
		gd.addChoice("Operation", Operation.getAllLabels(), operation.label);
		gd.addChoice("Attribute", Attribute.getAllLabels(), attribute.label);
        String label = "Min Voxel Number:";
        gd.addNumericField(label, nPixelMin, 0);
        gd.addChoice("Connectivity", Connectivity3D.getAllLabels(), connectivity.name());
        gd.showDialog();

        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        // read options
        operation = Operation.fromLabel( gd.getNextChoice() );
        attribute = Attribute.fromLabel( gd.getNextChoice() );
        nPixelMin = (int) gd.getNextNumber();
        connectivity = Connectivity3D.fromLabel(gd.getNextChoice());

        ImagePlus resultPlus;
        String newName = imagePlus.getShortTitle() + "-attrFilt";

        // Identify image to process (original, or inverted)
        ImagePlus image2 = imagePlus.duplicate();
        if( operation == Operation.CLOSING || operation == Operation.BOTTOM_HAT )        	
        {        	
        	IJ.run( image2, "Invert", "stack" );
        }

        // apply volume opening
        final ImageStack image = image2.getStack();
		final ImageStack result = AttributeFiltering.volumeOpening(image,
				nPixelMin, connectivity.getValue());
		resultPlus = new ImagePlus(newName, result);

        // For top-hat and bottom-hat, we consider the difference with the
        // original image
		if (operation == Operation.TOP_HAT || operation == Operation.BOTTOM_HAT)
        {
        	for( int x = 0; x < image.getWidth(); x++ )
        		for( int y = 0; y < image.getHeight(); y++ )
        			for( int z = 0; z < image.getSize(); z++ )
        	{
				double diff = Math.abs(result.getVoxel(x, y, z) - image.getVoxel(x, y, z));
				result.setVoxel(x, y, z, diff);
        	}
        }

        // For closing, invert back the result
		else if (operation == Operation.CLOSING)
        {
        	IJ.run(resultPlus, "Invert", "stack");
        }
        
        // show result
		resultPlus.copyScale(imagePlus);
		Images3D.optimizeDisplayRange(resultPlus);
		resultPlus.updateAndDraw();
		resultPlus.show();
		resultPlus.setSlice(imagePlus.getCurrentSlice());
	}
}
