package inra.ijpb.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.AttributeFiltering;

/**
 * Removes connected components whose size is inferior to the given number of
 * elements, in a 2D or 3D grayscale image.
 *
 * @see AreaOpeningPlugin
 *
 * @author David Legland
 *
 */
public class GrayscaleAttributeFiltering3D implements PlugIn
{
	enum Operation
	{
		CLOSING( "Closing" ),
		OPENING( "Opening" ),
		TOP_HAT( "Top Hat" ),
		BOTTOM_HAT( "Bottom Hat" );

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
				result[i++] = op.label;

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
		String title = "Attribute Filtering 3D";
        GenericDialog gd = new GenericDialog( title );
        gd.addChoice( "Operation", Operation.getAllLabels(),
        		Operation.OPENING.toString());
        String label = "Min Voxel Number:";
        gd.addNumericField(label, 100, 0);
        gd.showDialog();

        // If cancel was clicked, do nothing
        if (gd.wasCanceled())
            return;

        // read options
        Operation operation = Operation.fromLabel( gd.getNextChoice() );
        int nPixelMin = (int) gd.getNextNumber();

        ImagePlus resultPlus;
        String newName = imagePlus.getShortTitle() + "-attrFilt";

        // Identify image to process (original, or inverted)
        ImagePlus image2 = imagePlus.duplicate();
        if( operation == Operation.CLOSING ||
        		operation == Operation.BOTTOM_HAT )        	
        {        	
        	IJ.run( image2, "Invert", "stack" );
        }

        // apply volume opening
        final ImageStack image = image2.getStack();
        final ImageStack result =
        		AttributeFiltering.volumeOpening( image, nPixelMin );
        resultPlus = new ImagePlus( newName, result );

        // For top-hat and bottom-hat, we consider the difference with the
        // original image
        if( operation == Operation.TOP_HAT ||
        	operation == Operation.BOTTOM_HAT )
        {
        	for( int x = 0; x < image.getWidth(); x++ )
        		for( int y = 0; y < image.getHeight(); y++ )
        			for( int z = 0; z < image.getSize(); z++ )
        	{
        		double diff = Math.abs( result.getVoxel( x, y, z ) -
        							   image.getVoxel( x, y, z ) );
        		result.setVoxel( x, y, z, diff );
        	}
        }

        // For closing, invert back the result
        else if( operation == Operation.CLOSING )
        	IJ.run( resultPlus, "Invert", "stack" );

        // show result
        resultPlus.copyScale( imagePlus );
		Images3D.optimizeDisplayRange( resultPlus );
		resultPlus.setSlice( imagePlus.getSlice() );
		resultPlus.updateAndDraw();
        resultPlus.show();
	}// end run method
} // end class
