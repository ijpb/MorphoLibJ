/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
package inra.ijpb.morphology;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.filter.BlackTopHat;
import inra.ijpb.morphology.filter.Closing;
import inra.ijpb.morphology.filter.Dilation;
import inra.ijpb.morphology.filter.Erosion;
import inra.ijpb.morphology.filter.ExternalGradient;
import inra.ijpb.morphology.filter.Gradient;
import inra.ijpb.morphology.filter.InternalGradient;
import inra.ijpb.morphology.filter.Laplacian;
import inra.ijpb.morphology.filter.Opening;
import inra.ijpb.morphology.filter.WhiteTopHat;

/**
 * <p>
 * Collection of static methods for morphological filters,
 * as well as an enumeration of available methods.
 * </p>
 * 
 * <p>
 * Example of use:
 * <pre><code>
 * ImageProcessor image = IJ.getImage().getProcessor();
 * Strel se = SquareStrel.fromDiameter(5);
 * ImageProcessor grad = Morphology.gradient(image, se);
 * ImagePlus res = new ImagePlus("Gradient", grad);
 * res.show(); 
 * </code></pre>
 * 
 * <p>
 * Example of use with 3D image (stack):
 * <pre><code>
 * ImageStack image = IJ.getImage().getStack();
 * Strel3D se = CubeStrel.fromDiameter(3);
 * ImageStack grad = Morphology.gradient(image, se);
 * ImagePlus res = new ImagePlus("Gradient3D", grad);
 * res.show(); 
 * </code></pre>
 * <p>
 * Or directly with an instance of ImagePlus:
 * <pre><code>
 * ImagePlus image = IJ.getImage();
 * Strel se = SquareStrel.fromDiameter(5);
 * ImagePlus res = Morphology.gradient(image, se);
 * res.show(); 
 * </code></pre>
 * 
 * @author David Legland
 */
public class Morphology 
{
	// =======================================================================
	// Enumeration for operations
	
	/**
	 * A pre-defined set of basis morphological operations, that can be easily 
	 * used with a GenericDialog. 
	 * Example:
	 * <pre><code>
	 * // Use a generic dialog to define an operator 
	 * GenericDialog gd = new GenericDialog();
	 * gd.addChoice("Operation", Operation.getAllLabels();
	 * gd.showDialog();
	 * Operation op = Operation.fromLabel(gd.getNextChoice());
	 * // Apply the operation on the current image
	 * ImageProcessor image = IJ.getImage().getProcessor();
	 * op.apply(image, SquareStrel.fromRadius(2));
	 * </code></pre>
	 */
	public enum Operation 
	{
		/** Morphological erosion (local minima)*/
		EROSION("Erosion"),
		/** Morphological dilation (local maxima)*/
		DILATION("Dilation"),
		/** Morphological opening (erosion followed by dilation)*/
		OPENING("Opening"),
		/** Morphological closing (dilation followed by erosion)*/
		CLOSING("Closing"), 
		/** White Top-Hat */
		TOPHAT("White Top Hat"),
		/** Black Top-Hat */
		BOTTOMHAT("Black Top Hat"),
		/** Morphological gradient (difference of dilation with erosion) */
		GRADIENT("Gradient"), 
		/** Morphological laplacian (difference of external gradient with internal gradient) */
		LAPLACIAN("Laplacian"), 
		/** Morphological internal gradient (difference of dilation with original image) */
		INTERNAL_GRADIENT("Internal Gradient"), 
		/** Morphological internal gradient (difference of original image with erosion) */
		EXTERNAL_GRADIENT("External Gradient");
		
		private final String label;
		
		private Operation(String label) 
		{
			this.label = label;
		}
		
		/**
		 * Applies the current operator to the input image.
		 * 
		 * @param image
		 *            the image to process
		 * @param strel
		 *            the structuring element to use
		 * @return the result of morphological operation applied to image
		 */
		public ImageProcessor apply(ImageProcessor image, Strel strel) 
		{
			if (this == DILATION)
				return dilation(image, strel);
			if (this == EROSION)
				return erosion(image, strel);
			if (this == CLOSING)
				return closing(image, strel);
			if (this == OPENING)
				return opening(image, strel);
			if (this == TOPHAT)
				return whiteTopHat(image, strel);
			if (this == BOTTOMHAT)
				return blackTopHat(image, strel);
			if (this == GRADIENT)
				return gradient(image, strel);
			if (this == LAPLACIAN)
				return laplacian(image, strel);
			if (this == INTERNAL_GRADIENT)
				return internalGradient(image, strel);
			if (this == EXTERNAL_GRADIENT)
				return externalGradient(image, strel);
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}
		
		/**
		 * Applies the current operator to the input 3D image.
		 * 
		 * @param image
		 *            the image to process
		 * @param strel
		 *            the structuring element to use
		 * @return the result of morphological operation applied to image
		 */
		public ImageStack apply(ImageStack image, Strel3D strel)
		{
			if (this == DILATION)
				return dilation(image, strel);
			if (this == EROSION)
				return erosion(image, strel);
			if (this == CLOSING)
				return closing(image, strel);
			if (this == OPENING)
				return opening(image, strel);
			if (this == TOPHAT)
				return whiteTopHat(image, strel);
			if (this == BOTTOMHAT)
				return blackTopHat(image, strel);
			if (this == GRADIENT)
				return gradient(image, strel);
			if (this == LAPLACIAN)
				return laplacian(image, strel);
			if (this == INTERNAL_GRADIENT)
				return internalGradient(image, strel);
			if (this == EXTERNAL_GRADIENT)
				return externalGradient(image, strel);
			
			throw new RuntimeException(
					"Unable to process the " + this + " morphological operation");
		}
		
		public String toString() 
		{
			return this.label;
		}
		
		/**
		 * Returns the list of labels for this enumeration.
		 * 
		 * @return the list of labels for this enumeration.
		 */
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
			throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
		}
	};
	
	/**
	 * Makes the default constructor private to avoid creation of instances.
	 */
	private Morphology() 
	{
	}

	
	// =======================================================================
	// Main morphological operations
	
    /**
     * Performs morphological dilation on the input image.
     * 
     * Dilation is obtained by extracting the maximum value among pixels/voxels
     * in the neighborhood given by the structuring element.
     * 
     * This methods is called the equivalent static method for ImageProcessor or
     * ImageStack, and creates a new ImagePlus instance with the result.
     * 
     * @param imagePlus
     *            the input image to process
     * @param strel
     *            the structuring element used for dilation
     * @return the result of the dilation
     * 
     * @see #dilation(ImageProcessor, Strel)
     * @see #dilation(ImageStack, Strel3D)
     * @see #erosion(ImagePlus, Strel3D)
     */
	public static ImagePlus dilation(ImagePlus imagePlus, Strel3D strel)
    {
        return new Dilation(strel).process(imagePlus);
    }
    
    
    /**
	 * Performs morphological dilation on the input image.
	 * 
	 * Dilation is obtained by extracting the maximum value among pixels in the
	 * neighborhood given by the structuring element.
	 * 
	 * This methods is mainly a wrapper to the dilation method of the strel
	 * object.
	 * 
	 * @param image
	 *            the input image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for dilation
	 * @return the result of the dilation
	 * 
	 * @see #erosion(ImageProcessor, Strel)
	 * @see Strel#dilation(ImageProcessor)
	 */
	public static ImageProcessor dilation(ImageProcessor image, Strel strel)
	{
	    return new Dilation(strel).process(image);
	}

	/**
	 * Performs morphological dilation on the input 3D image.
	 * 
	 * Dilation is obtained by extracting the maximum value among voxels in the
	 * neighborhood given by the 3D structuring element.
	 * 
	 * @param image
	 *            the input 3D image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for dilation
	 * @return the result of the dilation
	 */
	public static ImageStack dilation(ImageStack image, Strel3D strel)
	{
	    return new Dilation(strel).process(image);
	}
	
    /**
     * Performs morphological erosion on the input image.
     * 
     * Erosion is obtained by extracting the minimum value among pixels/voxels
     * in the neighborhood given by the structuring element.
     * 
     * This methods is called the equivalent static method for ImageProcessor or
     * ImageStack, and creates a new ImagePlus instance with the result.
     * 
     * @param imagePlus
     *            the input image to process
     * @param strel
     *            the structuring element used for erosion
     * @return the result of the erosion
     * 
     * @see #erosion(ImageProcessor, Strel)
     * @see #erosion(ImageStack, Strel3D)
     * @see #dilation(ImagePlus, Strel3D)
     */
    public static ImagePlus erosion(ImagePlus imagePlus, Strel3D strel)
    {
        return new Erosion(strel).process(imagePlus);
    }
    
	/**
	 * Performs morphological erosion on the input image. Erosion is obtained by
	 * extracting the minimum value among pixels in the neighborhood given by
	 * the structuring element.
	 * 
	 * This methods is mainly a wrapper to the erosion method of the strel
	 * object.
	 * 
	 * @see #dilation(ImageProcessor, Strel)
	 * @see Strel#erosion(ImageProcessor)
	 * 
	 * @param image
	 *            the input image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for erosion
	 * @return the result of the erosion
	 */
	public static ImageProcessor erosion(ImageProcessor image, Strel strel)
	{
        return new Erosion(strel).process(image);
	}
	
	/**
	 * Performs morphological erosion on the input 3D image.
	 * 
	 * Erosion is obtained by extracting the minimum value among voxels in the
	 * neighborhood given by the 3D structuring element.
	 * 
	 * @param image
	 *            the input image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for erosion
	 * @return the result of the erosion
	 */
	public static ImageStack erosion(ImageStack image, Strel3D strel) 
	{
		return new Erosion(strel).process(image);
	}

    /**
     * Performs morphological opening on the input image.
     * 
     * The opening is obtained by performing an erosion followed by a dilation
     * with the reversed structuring element.
     * 
     * This methods is called the equivalent static method for ImageProcessor or
     * ImageStack, and creates a new ImagePlus instance with the result.
     * 
     * @param imagePlus
     *            the input image to process
     * @param strel
     *            the structuring element used for erosion
     * @return the result of the erosion
     * 
     * @see #opening(ImageProcessor, Strel)
     * @see #opening(ImageStack, Strel3D)
     * @see #closing(ImagePlus, Strel3D)
     */
    public static ImagePlus opening(ImagePlus imagePlus, Strel3D strel)
    {
        return new Opening(strel).process(imagePlus);
    }
    
	/**
	 * Performs morphological opening on the input image.
	 * 
	 * The opening is obtained by performing an erosion followed by a dilation
	 * with the reversed structuring element.
	 * 
	 * This methods is mainly a wrapper to the opening method of the strel object.
	 * 
	 * @see #closing(ImageProcessor, Strel)
	 * @see Strel#opening(ImageProcessor)
	 * 
	 * @param image
	 *            the input image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for opening
	 * @return the result of the morphological opening
	 */
	public static ImageProcessor opening(ImageProcessor image, Strel strel)
	{
        return new Opening(strel).process(image);
	}

	/**
	 * Performs morphological opening on the input 3D image.
	 * 
	 * The 3D opening is obtained by performing a 3D erosion followed by a 3D
	 * dilation with the reversed structuring element.
	 * 
	 * @see #closing(ImageStack, Strel3D)
	 * @see Strel#opening(ImageStack)
	 * 
	 * @param image
	 *            the input 3D image to process
	 * @param strel
	 *            the structuring element used for opening
	 * @return the result of the 3D morphological opening
	 */
	public static ImageStack opening(ImageStack image, Strel3D strel) 
	{
        return new Opening(strel).process(image);
	}

    /**
     * Performs morphological closing on the input image.
     * 
     * The opening is obtained by performing a dilation followed by an erosion
     * with the reversed structuring element.
     * 
     * This methods is called the equivalent static method for ImageProcessor or
     * ImageStack, and creates a new ImagePlus instance with the result.
     * 
     * @param imagePlus
     *            the input image to process
     * @param strel
     *            the structuring element used for closing
     * @return the result of the closing
     * 
     * @see #closing(ImageProcessor, Strel)
     * @see #closing(ImageStack, Strel3D)
     * @see #opening(ImagePlus, Strel3D)
     */
    public static ImagePlus closing(ImagePlus imagePlus, Strel3D strel)
    {
        return new Closing(strel).process(imagePlus);
    }
    
	/**
	 * Performs closing on the input image.
	 * The closing is obtained by performing a dilation followed by an erosion
	 * with the reversed structuring element.
	 *  
	 * This methods is mainly a wrapper to the opening method of the strel object.
	 * @see #opening(ImageProcessor, Strel)
	 * @see Strel#closing(ImageProcessor)
	 * 
	 * @param image
	 *            the input image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for closing
	 * @return the result of the morphological closing
	 */
	public static ImageProcessor closing(ImageProcessor image, Strel strel) 
	{
        return new Closing(strel).process(image);
	}

	/**
	 * Performs morphological closing on the input 3D image.
	 * 
	 * The 3D closing is obtained by performing a 3D dilation followed by a 3D
	 * erosion with the reversed structuring element.
	 * 
	 * @see #opening(ImageStack, Strel3D)
	 * @see Strel#opening(ImageStack)
	 * 
	 * @param image
	 *            the input 3D image to process
	 * @param strel
	 *            the structuring element used for closing
	 * @return the result of the 3D morphological closing
	 */
	public static ImageStack closing(ImageStack image, Strel3D strel) 
	{
        return new Closing(strel).process(image);
	}

    /**
     * Computes white top hat of the original image.
     * The white top hat is obtained by subtracting the result of an opening 
     * from the original image.
     *  
     * The white top hat enhances light structures smaller than the structuring element.
     * 
     * This methods is called the equivalent static method for ImageProcessor or
     * ImageStack, and creates a new ImagePlus instance with the result.
     * 
     * @param imagePlus
     *            the input image to process
     * @param strel
     *            the structuring element used for closing
     * @return the result of the closing
     * 
     * @see #whiteTopHat(ImageProcessor, Strel)
     * @see #whiteTopHat(ImageStack, Strel3D)
     * @see #blackTopHat(ImagePlus, Strel3D)
     */
    public static ImagePlus whiteTopHat(ImagePlus imagePlus, Strel3D strel)
    {
        return new WhiteTopHat(strel).process(imagePlus);
    }

	/**
	 * Computes white top hat of the original image.
	 * The white top hat is obtained by subtracting the result of an opening 
	 * from the original image.
	 *  
	 * The white top hat enhances light structures smaller than the structuring element.
	 * 
	 * @see #blackTopHat(ImageProcessor, Strel)
	 * @see #opening(ImageProcessor, Strel)
	 * 
	 * @param image
	 *            the input image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for white top-hat
	 * @return the result of the white top-hat
	 */
	public static ImageProcessor whiteTopHat(ImageProcessor image, Strel strel) 
	{
        return new WhiteTopHat(strel).process(image);
	}
	
	/**
	 * Computes 3D white top hat of the original image.
	 * 
	 * The white top hat is obtained by subtracting the result of an opening 
	 * from the original image.
	 *  
	 * The white top hat enhances light structures smaller than the structuring element.
	 * 
	 * @see #blackTopHat(ImageStack, Strel3D)
	 * @see #opening(ImageStack, Strel3D)
	 * 
	 * @param image
	 *            the input 3D image to process 
	 * @param strel
	 *            the structuring element used for white top-hat
	 * @return the result of the 3D white top-hat
	 */
	public static ImageStack whiteTopHat(ImageStack image, Strel3D strel)
	{
        return new WhiteTopHat(strel).process(image);
	}

    /**
     * Computes black top hat (or "bottom hat") of the original image.
     * The black top hat is obtained by subtracting the original image from
     * the result of a closing.
     *  
     * The black top hat enhances dark structures smaller than the structuring element.
     * 
     * This methods is called the equivalent static method for ImageProcessor or
     * ImageStack, and creates a new ImagePlus instance with the result.
     * 
     * @param imagePlus
     *            the input image to process
     * @param strel
     *            the structuring element used for closing
     * @return the result of the closing
     * 
     * @see #blackTopHat(ImageProcessor, Strel)
     * @see #blackTopHat(ImageStack, Strel3D)
     * @see #whiteTopHat(ImagePlus, Strel3D)
     */
    public static ImagePlus blackTopHat(ImagePlus imagePlus, Strel3D strel)
    {
        return new BlackTopHat(strel).process(imagePlus);
    }
    
	/**
	 * Computes black top hat (or "bottom hat") of the original image.
	 * The black top hat is obtained by subtracting the original image from
	 * the result of a closing.
	 *  
	 * The black top hat enhances dark structures smaller than the structuring element.
	 * 
	 * @see #whiteTopHat(ImageProcessor, Strel)
	 * @see #closing(ImageProcessor, Strel)
	 * 
	 * @param image
	 *            the input image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for black top-hat
	 * @return the result of the black top-hat
	 */
	public static ImageProcessor blackTopHat(ImageProcessor image, Strel strel)
	{
        return new BlackTopHat(strel).process(image);
	}
	
	/**
	 * Computes black top hat (or "bottom hat") of the original image.
	 * The black top hat is obtained by subtracting the original image from
	 * the result of a closing.
	 *  
	 * The black top hat enhances dark structures smaller than the structuring element.
	 * 
	 * @see #whiteTopHat(ImageStack, Strel3D)
	 * @see #closing(ImageStack, Strel3D)
	 * 
	 * @param image
	 *            the input 3D image to process
	 * @param strel
	 *            the structuring element used for black top-hat
	 * @return the result of the 3D black top-hat
	 */
	public static ImageStack blackTopHat(ImageStack image, Strel3D strel)
	{
        return new BlackTopHat(strel).process(image);
	}

    /**
     * Computes the morphological gradient of the input image.
     * The morphological gradient is obtained by from the difference of image 
     * dilation and image erosion computed with the same structuring element. 
     * 
     * This methods is called the equivalent static method for ImageProcessor or
     * ImageStack, and creates a new ImagePlus instance with the result.
     * 
     * @param imagePlus
     *            the input image to process
     * @param strel
     *            the structuring element used for closing
     * @return the result of the closing
     * 
     * @see #gradient(ImageProcessor, Strel)
     * @see #gradient(ImageStack, Strel3D)
     * @see #internalGradient(ImagePlus, Strel3D)
     * @see #externalGradient(ImagePlus, Strel3D)
     */
    public static ImagePlus gradient(ImagePlus imagePlus, Strel3D strel)
    {
        return new Gradient(strel).process(imagePlus);
    }
    	
	/**
	 * Computes the morphological gradient of the input image.
	 * The morphological gradient is obtained by from the difference of image 
	 * dilation and image erosion computed with the same structuring element. 
	 * 
	 * @see #erosion(ImageProcessor, Strel)
	 * @see #dilation(ImageProcessor, Strel)
	 * 
	 * @param image
	 *            the input image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for morphological gradient
	 * @return the result of the morphological gradient
	 */
	public static ImageProcessor gradient(ImageProcessor image, Strel strel)
	{
        return new Gradient(strel).process(image);
	}

	/**
	 * Computes the morphological gradient of the input 3D image.
	 * The morphological gradient is obtained by from the difference of image 
	 * dilation and image erosion computed with the same structuring element. 
	 * 
	 * @see #erosion(ImageStack, Strel3D)
	 * @see #dilation(ImageStack, Strel3D)
	 * 
	 * @param image
	 *            the input 3D image to process
	 * @param strel
	 *            the structuring element used for morphological gradient
	 * @return the result of the 3D morphological gradient
	 */
	public static ImageStack gradient(ImageStack image, Strel3D strel)
	{
        return new Gradient(strel).process(image);
	}


	/**
     * Computes the morphological Laplacian of the 3D input image. The
     * morphological gradient is obtained from the difference of the external
     * gradient with the internal gradient, both computed with the same
     * structuring element.
     * 
     * Homogeneous regions appear as gray.
     * 
     * This methods is called the equivalent static method for ImageProcessor or
     * ImageStack, and creates a new ImagePlus instance with the result.
     * 
     * @param imagePlus
     *            the input image to process
     * @param strel
     *            the structuring element used for closing
     * @return the result of the closing
     * 
     * @see #laplacian(ImageProcessor, Strel)
     * @see #laplacian(ImageStack, Strel3D)
     * @see #internalGradient(ImagePlus, Strel3D)
     * @see #externalGradient(ImagePlus, Strel3D)
     */
    public static ImagePlus laplacian(ImagePlus imagePlus, Strel3D strel)
    {
        return new Laplacian(strel).process(imagePlus);
    }


    /**
     * Computes the morphological Laplacian of the input image. The
     * morphological Laplacian is obtained from the difference of the external
     * gradient with the internal gradient, both computed with the same
     * structuring element.
     * 
     * Homogeneous regions appear as gray.
     * 
     * @see #erosion(ImageProcessor, Strel)
     * @see #dilation(ImageProcessor, Strel)
     * 
     * @param image
     *            the input image to process (grayscale or RGB)
     * @param strel
     *            the structuring element used for morphological laplacian
     * @return the result of the morphological laplacian
     */
	public static ImageProcessor laplacian(ImageProcessor image, Strel strel) 
	{
        return new Laplacian(strel).process(image);
	}

    /**
	 * Computes the morphological Laplacian of the 3D input image. The
	 * morphological gradient is obtained from the difference of the external
	 * gradient with the internal gradient, both computed with the same
	 * structuring element.
	 * 
	 * Homogeneous regions appear as gray.
	 * 
	 * @see #externalGradient(ImageStack, Strel3D)
	 * @see #internalGradient(ImageStack, Strel3D)
	 * 
	 * @param image
	 *            the input 3D image to process 
	 * @param strel
	 *            the structuring element used for morphological laplacian
	 * @return the result of the 3D morphological laplacian
	 */
	public static ImageStack laplacian(ImageStack image, Strel3D strel)
	{
        return new Laplacian(strel).process(image);
	}

    /**
     * Computes the morphological internal gradient of the input image.
     * The morphological internal gradient is obtained by from the difference 
     * of original image with the result of an erosion.
     * 
     * This methods is called the equivalent static method for ImageProcessor or
     * ImageStack, and creates a new ImagePlus instance with the result.
     * 
     * @param imagePlus
     *            the input image to process
     * @param strel
     *            the structuring element used for closing
     * @return the result of the closing
     * 
     * @see #internalGradient(ImageProcessor, Strel)
     * @see #internalGradient(ImageStack, Strel3D)
     * @see #gradient(ImagePlus, Strel3D)
     * @see #externalGradient(ImagePlus, Strel3D)
     */
    public static ImagePlus internalGradient(ImagePlus imagePlus, Strel3D strel)
    {
        return new InternalGradient(strel).process(imagePlus);
    }
        
	/** 
	 * Computes the morphological internal gradient of the input image.
	 * The morphological internal gradient is obtained by from the difference 
	 * of original image with the result of an erosion.
	 * 
	 * @see #erosion(ImageProcessor, Strel)
	 * @see #gradient(ImageProcessor, Strel)
	 * @see #externalGradient(ImageProcessor, Strel)
	 * 
	 * @param image
	 *            the input image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for morphological internal gradient
	 * @return the result of the morphological internal gradient
	 */
	public static ImageProcessor internalGradient(ImageProcessor image, Strel strel) 
	{
        return new InternalGradient(strel).process(image);
	}

	/** 
	 * Computes the morphological internal gradient of the 3D input image.
	 * The morphological internal gradient is obtained by from the difference 
	 * of original image with the result of an erosion.
	 * 
	 * @see #erosion(ImageStack, Strel3D)
	 * @see #gradient(ImageStack, Strel3D)
	 * @see #externalGradient(ImageStack, Strel3D)
	 * 
	 * @param image
	 *            the input image to process
	 * @param strel
	 *            the structuring element used for morphological internal gradient
	 * @return the result of the 3D morphological internal gradient
	 */
	public static ImageStack internalGradient(ImageStack image, Strel3D strel)
	{
        return new InternalGradient(strel).process(image);
	}

    /**
     * Computes the morphological external gradient of the input image.
     * The morphological external gradient is obtained by from the difference 
     * of the result of a dilation and of the original image .
     * 
     * This methods is called the equivalent static method for ImageProcessor or
     * ImageStack, and creates a new ImagePlus instance with the result.
     * 
     * @param imagePlus
     *            the input image to process
     * @param strel
     *            the structuring element used for closing
     * @return the result of the closing
     * 
     * @see #externalGradient(ImageProcessor, Strel)
     * @see #externalGradient(ImageStack, Strel3D)
     * @see #gradient(ImagePlus, Strel3D)
     * @see #internalGradient(ImagePlus, Strel3D)
     */
    public static ImagePlus externalGradient(ImagePlus imagePlus, Strel3D strel)
    {
        return new ExternalGradient(strel).process(imagePlus);
    }
        
	/** 
	 * Computes the morphological external gradient of the input image.
	 * The morphological external gradient is obtained by from the difference 
	 * of the result of a dilation and of the original image .
	 * 
	 * @see #dilation(ImageProcessor, Strel)
	 * 
	 * @param image
	 *            the input image to process (grayscale or RGB)
	 * @param strel
	 *            the structuring element used for morphological external gradient
	 * @return the result of the morphological external gradient
	 */
	public static ImageProcessor externalGradient(ImageProcessor image, Strel strel) 
	{
        return new ExternalGradient(strel).process(image);
	}

	/** 
	 * Computes the morphological external gradient of the input 3D image.
	 * The morphological external gradient is obtained by from the difference 
	 * of the result of a dilation and of the original image .
	 * 
	 * @see #dilation(ImageStack, Strel3D)
	 * @see #internalGradient(ImageStack, Strel3D)
	 * 
	 * @param image
	 *            the input image to process 
	 * @param strel
	 *            the structuring element used for morphological external gradient
	 * @return the result of the 3D morphological external gradient
	 */
	public static ImageStack externalGradient(ImageStack image, Strel3D strel) 
	{
        return new ExternalGradient(strel).process(image);
	}
}
