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
package inra.ijpb.data.image;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.color.BinaryOverlay;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A set of static methods for working with color images.
 * 
 * @author David Legland
 *
 */
public class ColorImages
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private ColorImages()
	{
	}

	/**
	 * Prints the content of the input color image on the console. This can be
	 * used for debugging (small) images.
	 * 
	 * @param image
	 *            the color image to display on the console
	 */
	public static final void print(ColorProcessor image) 
	{
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int intCode = image.get(x, y);
				int r = (intCode & 0xFF0000) >> 16;
				int g = (intCode & 0xFF00) >> 8;
				int b = (intCode & 0xFF);
				System.out.print(String.format("(%3d,%3d,%3d) ", r, g, b));
			}
			System.out.println("");
		}
	}

	/**
	 * Splits the channels of the color image into three new instances of
	 * ByteProcessor.
	 * 
	 * @param image
	 *            the original image, assumed to be a ColorProcessor
	 * @return a collection containing the red, green and blue channels
	 */
	public static final Collection<ByteProcessor> splitChannels(ImageProcessor image) 
	{
		if (!(image instanceof ColorProcessor)) 
		{
			throw new IllegalArgumentException("Requires an instance of ColorProcessor");
		}
		
		// size of input image
		int width = image.getWidth();
		int height = image.getHeight();
		int size = width * height;

		// Extract red, green and blue components
		byte[] redArray = new byte[size];
		byte[] greenArray = new byte[size];
		byte[] blueArray = new byte[size];
		((ColorProcessor) image).getRGB(redArray, greenArray, blueArray);

		// create image processor for each channel
		ByteProcessor red = new ByteProcessor(width, height, redArray, null);
		ByteProcessor green = new ByteProcessor(width, height, greenArray, null);
		ByteProcessor blue  = new ByteProcessor(width, height, blueArray, null);

		// concatenate channels into a new collection
		ArrayList<ByteProcessor> result = new ArrayList<ByteProcessor>(3);
		result.add(red);
		result.add(green);
		result.add(blue);
		
		return result;
	}

	/**
	 * Splits the channels of the color image into three new instances of
	 * ByteProcessor.
	 * 
	 * @param image
	 *            the original image, assumed to be a ColorProcessor
	 * @return a collection containing the red, green and blue channels
	 */
	public static final Collection<ImageStack> splitChannels(ImageStack image) 
	{
		if (!(image.getProcessor(1) instanceof ColorProcessor)) 
		{
			throw new IllegalArgumentException("Requires a Stack containing instances of ColorProcessor");
		}
		
		// size of input image
		int width = image.getWidth();
		int height = image.getHeight();
		int depth = image.getSize();

		// create byte stacks
		ImageStack redStack = ImageStack.create(width, height, depth, 8);
		ImageStack greenStack = ImageStack.create(width, height, depth, 8);
		ImageStack blueStack = ImageStack.create(width, height, depth, 8);
		
		for (int z = 1; z <= depth; z++)
		{
			// extract the current RGB slice
			ColorProcessor rgb = (ColorProcessor) image.getProcessor(z);

			// extract the current slice of each channel
			ByteProcessor red 	= (ByteProcessor) redStack.getProcessor(z);
			ByteProcessor green = (ByteProcessor) greenStack.getProcessor(z);
			ByteProcessor blue 	= (ByteProcessor) blueStack.getProcessor(z);
			
			// convert int codes to color components
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int intCode = rgb.get(x, y);
					red.set(x, y, (intCode >> 16) & 0x00FF);
					green.set(x, y, (intCode >> 8) & 0x00FF);
					blue.set(x, y, intCode & 0x00FF);
				}
			}

			// set slices (should not be necessary, but more secure)
			redStack.setProcessor(red, z);
			greenStack.setProcessor(green, z);
			blueStack.setProcessor(blue, z);
		}
		
		// concatenate channels into a new collection of ImageStack instances
		ArrayList<ImageStack> result = new ArrayList<ImageStack>(3);
		result.add(redStack);
		result.add(greenStack);
		result.add(blueStack);
		
		return result;
	}

	/**
	 * Splits the channels of the color image and returns the new ByteImages
	 * into a Map, using channel names as key. 
	 *  
	 * Example:
	 * <pre><code>
	 * ColorProcessor colorImage = ...
	 * HashMap&lt;String, ByteProcessor&gt; channels = mapChannels(colorImage);
	 * ByteProcessor blue = channels.get("blue");
	 * </code></pre>
	 * 
	 * @param image the original image, assumed to be a ColorProcessor
	 * @return a hashmap indexing the three channels by their names
	 */
	public static final HashMap<String, ByteProcessor> mapChannels(ImageProcessor image) 
	{
		if (!(image instanceof ColorProcessor)) 
		{
			throw new IllegalArgumentException("Requires an instance of ColorProcessor");
		}
		
		// size of input image
		int width = image.getWidth();
		int height = image.getHeight();
		int size = width * height;

		// Extract red, green and blue components
		byte[] redArray = new byte[size];
		byte[] greenArray = new byte[size];
		byte[] blueArray = new byte[size];
		((ColorProcessor) image).getRGB(redArray, greenArray, blueArray);

		// create image processor for each channel
		ByteProcessor red = new ByteProcessor(width, height, redArray, null);
		ByteProcessor green = new ByteProcessor(width, height, greenArray, null);
		ByteProcessor blue  = new ByteProcessor(width, height, blueArray, null);

		// concatenate channels into a new collection
		HashMap<String, ByteProcessor> map = new HashMap<String, ByteProcessor>(3);
		map.put("red", red);
		map.put("green", green);
		map.put("blue", blue);
		
		return map;
	}

	/**
	 * Splits the channels of the 3D color image into three new instances of
	 * ImageStack containing ByteProcessors.
	 * 
	 * @param image
	 *            the original image, assumed to be a ColorProcessor
	 * @return a collection containing the red, green and blue channels
	 */
	public static final HashMap<String, ImageStack> mapChannels(ImageStack image) 
	{
		if (!(image.getProcessor(1) instanceof ColorProcessor)) 
		{
			throw new IllegalArgumentException("Requires a Stack containing instances of ColorProcessor");
		}
		
		// size of input image
		int width = image.getWidth();
		int height = image.getHeight();
		int depth = image.getSize();

		// create byte stacks
		ImageStack redStack = ImageStack.create(width, height, depth, 8);
		ImageStack greenStack = ImageStack.create(width, height, depth, 8);
		ImageStack blueStack = ImageStack.create(width, height, depth, 8);
		
		for (int z = 1; z <= depth; z++)
		{
			// extract the current RGB slice
			ColorProcessor rgb = (ColorProcessor) image.getProcessor(z);

			// extract the current slice of each channel
			ByteProcessor red 	= (ByteProcessor) redStack.getProcessor(z);
			ByteProcessor green = (ByteProcessor) greenStack.getProcessor(z);
			ByteProcessor blue 	= (ByteProcessor) blueStack.getProcessor(z);
			
			// convert int codes to color components
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int intCode = rgb.get(x, y);
					red.set(x, y, (intCode >> 16) & 0x00FF);
					green.set(x, y, (intCode >> 8) & 0x00FF);
					blue.set(x, y, intCode & 0x00FF);
				}
			}

			// set slices (should not be necessary, but more secure)
			redStack.setProcessor(red, z);
			greenStack.setProcessor(green, z);
			blueStack.setProcessor(blue, z);
		}
		
		// concatenate channels into a new collection
		HashMap<String, ImageStack> map = new HashMap<String, ImageStack>(3);
		map.put("red", redStack);
		map.put("green", greenStack);
		map.put("blue", blueStack);
		
		return map;
	}

	/**
	 * Creates a new ColorProcessor from a collection of three channels.
	 * 
	 * @param channels
	 *            a collection containing the red, green and blue channels
	 * @return the color image corresponding to the concatenation of the three
	 *         channels
	 * @throws IllegalArgumentException
	 *             if the collection contains less than three channels
	 */
	public static final ColorProcessor mergeChannels(Collection<ImageProcessor> channels) 
	{
		// check validity of input
		if (channels.size() < 3)
			throw new IllegalArgumentException("Requires at least three channels in the collection");
		
		// extract each individual channel
		Iterator<ImageProcessor> iterator = channels.iterator();
		ImageProcessor red = iterator.next();
		ImageProcessor green = iterator.next();
		ImageProcessor blue = iterator.next();

		// call helper function
		return mergeChannels(red, green, blue);
	}

	/**
	 * Creates a new ColorProcessor from the red, green and blue channels. Each
	 * channel must be an instance of ByteProcessor.
	 * 
	 * @param red
	 *            the image for the red channel (must be a ByteProcessor)
	 * @param green
	 *            the image for the green channel (must be a ByteProcessor)
	 * @param blue
	 *            the image for the blue channel (must be a ByteProcessor)
	 * @return the color image corresponding to the concatenation of the three
	 *         channels
	 * @throws IllegalArgumentException
	 *             if one of the channel is not an instance of ByteProcessor
	 */
	public static final ColorProcessor mergeChannels(ImageProcessor red, 
			ImageProcessor green, ImageProcessor blue)
	{
		// check validity of input
		if (!(red instanceof ByteProcessor))
			throw new IllegalArgumentException("Input channels must be instances of ByteProcessor");
		if (!(green instanceof ByteProcessor))
			throw new IllegalArgumentException("Input channels must be instances of ByteProcessor");
		if (!(blue instanceof ByteProcessor))
			throw new IllegalArgumentException("Input channels must be instances of ByteProcessor");
		
		// Extract byte array of each channel
		byte[] redArray 	= (byte[]) red.getPixels();
		byte[] greenArray 	= (byte[]) green.getPixels();
		byte[] blueArray 	= (byte[]) blue.getPixels();
		
		// get image size
		int width = red.getWidth();
		int height = red.getHeight();

		// create result color image
		ColorProcessor result = new ColorProcessor(width, height);
		result.setRGB(redArray, greenArray, blueArray);
		
		return result;	
	}
	
	/**
	 * Creates a new color ImageStack from the red, green and blue ImageStack
	 * instances. Each channel must contains instances of ByteProcessor.
	 * 
	 * @param red
	 *            the image for the red channel (must contain ByteProcessor instances)
	 * @param green
	 *            the image for the green channel (must contain ByteProcessor instances)
	 * @param blue
	 *            the image for the blue channel (must contain ByteProcessor instances)
	 * @return the color image corresponding to the concatenation of the three
	 *         channels
	 * @throws IllegalArgumentException
	 *             if one of the ImageStack does not contain instances of ByteProcessor
	 */
	public static final ImageStack mergeChannels(ImageStack red, 
			ImageStack green, ImageStack blue)
	{
		// check validity of input
		if (!(red.getProcessor(1) instanceof ByteProcessor))
			throw new IllegalArgumentException("Input channels must be instances of ByteProcessor");
		if (!(green.getProcessor(1) instanceof ByteProcessor))
			throw new IllegalArgumentException("Input channels must be instances of ByteProcessor");
		if (!(blue.getProcessor(1) instanceof ByteProcessor))
			throw new IllegalArgumentException("Input channels must be instances of ByteProcessor");
		
		int width = red.getWidth();
		int height = red.getHeight();
		int depth = red.getSize();
		ImageStack result = ImageStack.create(width, height, depth, 24);
		
		for (int z = 1; z <= depth; z++)
		{
			// extract current slices
			ByteProcessor redSlice = (ByteProcessor) red.getProcessor(z);
			ByteProcessor greenSlice = (ByteProcessor) green.getProcessor(z);
			ByteProcessor blueSlice = (ByteProcessor) blue.getProcessor(z);
			ColorProcessor rgbSlice =  (ColorProcessor) result.getProcessor(z);

			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int r = redSlice.get(x, y);
					int g = greenSlice.get(x, y);
					int b = blueSlice.get(x, y);
					rgbSlice.set(x, y, rgbCode(r, g, b));
				}
			}
			
			result.setProcessor(rgbSlice, z);
		}
		
		return result;	
	}
	
	/**
	 * Applies an overlay of a binary image mask onto a grayscale or color
	 * image, using the specified color. Both images must have the same size.
	 * 
	 * @param imagePlus
	 *            the original 2D or 3D image used as background
	 * @param maskPlus
	 *            the binary 2D or 3D mask image
	 * @param color
	 *            the color used to display overlay
	 * @return a new ImagePlus instance containing a 2D or 3D color image
	 */
    public final static ImagePlus binaryOverlay(ImagePlus imagePlus, ImagePlus maskPlus, Color color)
    {
	    BinaryOverlay algo = new BinaryOverlay();
	    DefaultAlgoListener.monitor(algo);
	    return algo.process(imagePlus, maskPlus, color);
	}
	
	/**
	 * Applies an overlay of a binary image mask onto a grayscale or color
	 * image, using the specified color. Both images must have the same size.
	 * 
	 * @param refImage
	 *            the original image used as background
	 * @param mask
	 *            the binary mask image
	 * @param color
	 *            the color used to display overlay
	 * @return a new ImageProcessor instance containing a 2D color image
	 */
    public final static ImageProcessor binaryOverlay(ImageProcessor refImage, ImageProcessor mask, Color color)
    {
        BinaryOverlay algo = new BinaryOverlay();
        DefaultAlgoListener.monitor(algo);
        return algo.process(refImage, mask, color);
	}

	/**
	 * Applies an overlay of a binary image mask onto a grayscale or color
	 * image, using the specified color. Both images must have the same size.
	 * 
	 * @param refImage
	 *            the original 3D image used as background
	 * @param mask
	 *            the binary 3D mask image
	 * @param color
	 *            the color used to display overlay
	 * @return a new ImageStack containing a 3D color image
	 */
    public final static ImageStack binaryOverlay(ImageStack refImage, ImageStack mask, Color color)
    {
        BinaryOverlay algo = new BinaryOverlay();
        DefaultAlgoListener.monitor(algo);
        return algo.process(refImage, mask, color);
	}
	
    /**
     * Applies an overlay of a binary image mask onto a 2D or 3D  grayscale image, using
     * the specified color and opacity. Both images must have the same size.
     * 
     * @param refImage
     *            the original 2D or 3D image used as background
     * @param binaryMask
     *            the binary mask image
     * @param overlayColor
     *            the color used to display overlay
     * @param overlayOpacity
     *            the overlay opacity, between 0 and 1.
     * @return a new ImageProcessor instance containing a 2D color image
     */
    public static final ImagePlus binaryOverlay(ImagePlus refImage, ImagePlus binaryMask, Color overlayColor, double overlayOpacity)
    {
        if (refImage.getBitDepth() != 8)
        {
            throw new RuntimeException("Reference image must be 8-bits grayscale");
        }
        
        if (refImage.getStackSize() == 1)
        {
            ByteProcessor proc = (ByteProcessor) refImage.getProcessor();
            ImageProcessor ovr = binaryOverlay(proc, binaryMask.getProcessor(), overlayColor, overlayOpacity);
            return new ImagePlus(refImage.getShortTitle() + "-ovr", ovr);
        }
        else
        {
            ImageStack ovr = binaryOverlay(refImage.getImageStack(), binaryMask.getImageStack(), overlayColor, overlayOpacity);
            return new ImagePlus(refImage.getShortTitle() + "-ovr", ovr);
        }
    }
    
	/**
     * Applies an overlay of a binary image mask onto a grayscale image, using
     * the specified color and opacity. Both images must have the same size.
     * 
     * @param refImage
     *            the original 2D image used as background
     * @param binaryMask
     *            the binary mask image
     * @param overlayColor
     *            the color used to display overlay
     * @param overlayOpacity
     *            the overlay opacity, between 0 and 1.
     * @return a new ImageProcessor instance containing a 2D color image
     */
    public static final ColorProcessor binaryOverlay(ByteProcessor refImage, ImageProcessor binaryMask, Color overlayColor, double overlayOpacity)
    {
        // check input validity
        if (!ImageUtils.isSameSize(refImage, binaryMask))
        {
            throw new RuntimeException("Both input images must have same size");
        }
        if (refImage.getBitDepth() != 8)
        {
            throw new RuntimeException("Reference image must be 8-bits grayscale");
        }
        
        // retrieve image size
        final int sizeX = refImage.getWidth();
        final int sizeY = refImage.getHeight();

        // pre-compute opacity weights for gray and overlay
        final double op0 = overlayOpacity;
        final double op1 = 1.0 - op0;

        int rOvr = overlayColor.getRed();
        int gOvr = overlayColor.getGreen();
        int bOvr = overlayColor.getBlue();

        // the values for result pixel
        int r, g, b;

        ColorProcessor result = new ColorProcessor(sizeX, sizeY);
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                int gray = refImage.get(x, y); // assume 8-bit image
                boolean ovr = binaryMask.get(x, y) > 0;
                r = ovr ? (int) (gray * op1 + rOvr * op0) : gray;
                g = ovr ? (int) (gray * op1 + gOvr * op0) : gray;
                b = ovr ? (int) (gray * op1 + bOvr * op0) : gray;
                result.set(x, y, rgbCode(r, g, b)); 
            }
        }
        return result;
    }
    
    /**
     * Computes the overlay of a binary image over a grayscale image, using an
     * opacity for the binary layer. The result is returned as a Color Stack.
     * 
     * @param refImage
     *            the grayscale image used as reference.
     * @param binaryMask
     *            the binary mask, with non zero values where a overlay must
     *            occur
     * @param overlayColor
     *            the color used for painting overlay
     * @param overlayOpacity
     *            the opacity used for painting overlay, between 0 (totally
     *            transparent) and 1 (totally opaque)
     * @return the result of overlay, as a Color stack
     */
    public static final ImageStack binaryOverlay(ImageStack refImage, ImageStack binaryMask, Color overlayColor, double overlayOpacity)
    {
        // check input validity
        if (!ImageUtils.isSameSize(refImage, binaryMask))
        {
            throw new RuntimeException("Both input images must have same size");
        }
        if (refImage.getBitDepth() != 8)
        {
            throw new RuntimeException("Reference image must be 8-bits grayscale");
        }
        
        // allocate result
        int nSlices = refImage.size();
        ImageStack resStack = ImageStack.create(refImage.getWidth(), refImage.getHeight(), nSlices, 24);
        
        // iterate processing over slices
        for (int z = 1; z <= nSlices; z++)
        {
            IJ.showProgress(z, nSlices);
            ByteProcessor slice = (ByteProcessor) refImage.getProcessor(z);
            ImageProcessor overlaySlice = binaryMask.getProcessor(z);
            ImageProcessor result = binaryOverlay(slice, overlaySlice, overlayColor, overlayOpacity);
            resStack.setProcessor(result, z);
        }
        
        return resStack;
    }

    /**
     * Applies an overlay of a label map onto a 2D/3D grayscale image, using
     * the specified opacity. Both images must have the same size.
     * 
     * 
     * @param refImage
     *            the original 2D or 3D image used as background
     * @param labelMap
     *            the label map used as overlay
     * @param overlayOpacity
     *            the overlay opacity, between 0 and 1.
     * @return a new ImageProcessor instance containing a 2D color image
     */
    public static final ImagePlus labelMapOverlay(ImagePlus refImage, ImagePlus labelMap, double overlayOpacity)
    {
        if (refImage.getBitDepth() != 8)
        {
            throw new RuntimeException("Reference image must be 8-bits grayscale");
        }
        
        if (refImage.getStackSize() == 1)
        {
            ByteProcessor proc = (ByteProcessor) refImage.getProcessor();
            ImageProcessor ovr = labelMapOverlay(proc, labelMap.getProcessor(), overlayOpacity);
            return new ImagePlus(refImage.getShortTitle() + "-ovr", ovr);
        }
        else
        {
            ImageStack ovr = labelMapOverlay(refImage.getImageStack(), labelMap.getImageStack(), overlayOpacity);
            return new ImagePlus(refImage.getShortTitle() + "-ovr", ovr);
        }
    }
    
    /**
     * Applies an overlay of a label map onto a grayscale image, using
     * the specified opacity. Both images must have the same size.
     * 
     * 
     * @param refImage
     *            the original 2D image used as background
     * @param labelMap
     *            the label map used as overlay
     * @param overlayOpacity
     *            the overlay opacity, between 0 and 1.
     * @return a new ImageProcessor instance containing a 2D color image
     */
    public static final ImageProcessor labelMapOverlay(ImageProcessor refImage, ImageProcessor labelMap, double overlayOpacity)
    {
        // check input validity
        if (!ImageUtils.isSameSize(refImage, labelMap))
        {
            throw new RuntimeException("Both input images must have same size");
        }
        if (refImage.getBitDepth() != 8)
        {
            throw new RuntimeException("Reference image must be 8-bits grayscale");
        }
        
        // retrieve image size
        final int sizeX = refImage.getWidth();
        final int sizeY = refImage.getHeight();
        LUT lut = labelMap.getLut();

        // pre-compute opacity weights for gray and overlay
        final double op0 = overlayOpacity;
        final double op1 = 1.0 - op0;

        // the values for result pixel
        int r, g, b;
        int rgbCode;

        ColorProcessor result = new ColorProcessor(sizeX, sizeY);
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                int gray = refImage.get(x, y); // assume 8-bit image

                int label = (int) labelMap.getf(x, y);
                if (label == 0)
                {
                    // create RGB from gray value
                    r = gray;
                    g = gray;
                    b = gray;
                    rgbCode = grayToRGB(gray);
                }
                else
                {
                    // combine gray value with overlay color from LUT
                    r = (int) (gray * op1 + lut.getRed(label) * op0);
                    g = (int) (gray * op1 + lut.getGreen(label) * op0);
                    b = (int) (gray * op1 + lut.getBlue(label) * op0);
                    rgbCode = rgbCode(r, g, b);
                }
                result.set(x, y, rgbCode); 
            }
        }
        return result;
    }
    
    /**
     * Applies an overlay of a label map onto a 3D grayscale image, using
     * the specified opacity. Both images must have the same size.
     * 
     * 
     * @param refImage
     *            the original 3D image used as background
     * @param labelMap
     *            the label map used as overlay
     * @param overlayOpacity
     *            the overlay opacity, between 0 and 1.
     * @return a new ImageStack instance containing a 3D color image
     */
    public static final ImageStack labelMapOverlay(ImageStack refImage, ImageStack labelMap, double overlayOpacity)
    {
        // check input validity
        if (!ImageUtils.isSameSize(refImage, labelMap))
        {
            throw new RuntimeException("Both input images must have same size");
        }
        if (refImage.getBitDepth() != 8)
        {
            throw new RuntimeException("Reference image must be 8-bits grayscale");
        }
        
        // allocate (empty) result
        int nSlices = refImage.size();
        ImageStack resStack = new ImageStack(refImage.getWidth(), refImage.getHeight(), nSlices);
        
        // iterate processing over slices
        for (int z = 1; z <= nSlices; z++)
        {
            IJ.showProgress(z, nSlices);
            ByteProcessor slice = (ByteProcessor) refImage.getProcessor(z);
            ImageProcessor overlaySlice = labelMap.getProcessor(z);
            ImageProcessor result = labelMapOverlay(slice, overlaySlice, overlayOpacity);
            resStack.setProcessor(result, z);
        }
        
        return resStack;
    }
    
    private static final int rgbCode(int r, int g, int b)
    {
        return r << 16 | g << 8 | b;
    }

    private static final int grayToRGB(int gray)
    {
        return gray << 16 | gray << 8 | gray;
    }

}
