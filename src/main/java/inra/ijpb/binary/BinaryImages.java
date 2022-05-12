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
package inra.ijpb.binary;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.binary.conncomp.ConnectedComponentsLabeling;
import inra.ijpb.binary.conncomp.ConnectedComponentsLabeling3D;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import inra.ijpb.binary.distmap.ChamferDistanceTransform2DFloat;
import inra.ijpb.binary.distmap.ChamferDistanceTransform2DShort;
import inra.ijpb.binary.distmap.ChamferDistanceTransform3DFloat;
import inra.ijpb.binary.distmap.ChamferDistanceTransform3DShort;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.binary.distmap.ChamferMask3D;
import inra.ijpb.binary.distmap.DistanceTransform;
import inra.ijpb.binary.distmap.DistanceTransform3D;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransform;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformFloat;
import inra.ijpb.binary.geodesic.GeodesicDistanceTransformShort;
import inra.ijpb.binary.skeleton.ImageJSkeleton;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.ImageUtils;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.label.LabelImages;

/**
 * A collection of static methods for operating on binary images (2D/3D).
 * Some of the methods need the LabelImages class. 
 * 
 * @see inra.ijpb.label.LabelImages
 * 
 * @author David Legland
 *
 */
public class BinaryImages 
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	private BinaryImages()
	{
	}

	/**
	 * Check if input image is binary (8-bit with only 0 or 255 values)
	 * @param image input image
	 * @return true if image is binary
	 */
	public final static boolean isBinaryImage( ImagePlus image )
	{
		if( image.getType() != ImagePlus.GRAY8 &&
				image.getType() != ImagePlus.COLOR_256 )
			return false;
		for(int n=1; n<=image.getImageStackSize(); n++ )
		{
			final int[] hist =
					image.getImageStack().getProcessor( n ).getHistogram();
			for( int i=1; i<hist.length-1; i++ )
				if( hist[ i ] > 0 )
					return false;
		}
		return true;
	}

	/**
	 * Check if input image is binary (8-bit with only 0 or 255 values)
	 * @param image input image
	 * @return true if image is binary
	 */
	public final static boolean isBinaryImage( ImageProcessor image )
	{
		if( ! (image instanceof ByteProcessor) )
			return false;
		final int[] hist = image.getHistogram();
		for( int i=1; i<hist.length-1; i++ )
			if( hist[ i ] > 0 )
				return false;
		return true;
	}

	/**
	 * Counts the number of foreground pixels. The foreground pixels are the
	 * pixels with a value greater than zero.
	 * 
	 * @param image
	 *            a binary image with zero values for background
	 * @return the number of pixels with value greater than zero
	 */
	public static final int countForegroundPixels(ImageProcessor image)
	{
		int width = image.getWidth();
		int height = image.getHeight();

		int count = 0;
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++) 
			{
				if (image.getf(x, y) > 0) 
					count++;
			}
		}
		
		return count;
	}
	
	/**
	 * Counts the number of foreground voxels. The foreground voxels are the
	 * voxels with a value greater than zero.
	 * 
	 * @param image
	 *            a binary 3D image with zero values for background
	 * @return the number of voxels with value greater than zero
	 */
	public static final int countForegroundVoxels(ImageStack image)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		int depth = image.getSize();

		int count = 0;
		
		// iterate over 3D image, using Image3D wrapper
		Image3D image3d = Images3D.createWrapper(image);
		for (int z = 0; z < depth; z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++) 
				{
					if (image3d.get(x, y, z) > 0)
						count++;
				}
			}
		}		
		return count;
	}
	
	/**
	 * Computes the labels in the binary 2D or 3D image contained in the given
	 * ImagePlus, and computes the maximum label to set up the display range of
	 * the resulting ImagePlus.
	 * 
	 * @param imagePlus
	 *            contains the 3D binary image stack
	 * @param conn
	 *            the connectivity, either 4 or 8 for planar images, or 6 or 26
	 *            for 3D images
	 * @param bitDepth
	 *            the number of bits used to create the result image (8, 16 or
	 *            32)
	 * @return an ImagePlus containing the label of each connected component.
	 * @throws RuntimeException
	 *             if the number of labels reaches the maximum number that can
	 *             be represented with this bitDepth
	 * 
	 * @see inra.ijpb.binary.conncomp.ConnectedComponentsLabeling
	 * @see inra.ijpb.binary.conncomp.ConnectedComponentsLabeling3D
	 * @see inra.ijpb.morphology.FloodFill
	 */
	public final static ImagePlus componentsLabeling(ImagePlus imagePlus, 
			int conn, int bitDepth)
	{
		ImagePlus labelPlus;
	
		// Dispatch processing depending on input image dimensionality
		if (imagePlus.getStackSize() == 1)
		{
			ImageProcessor labels = componentsLabeling(imagePlus.getProcessor(),
					conn, bitDepth);
			labelPlus = new ImagePlus("Labels", labels);
			
		}
		else 
		{
			ImageStack labels = componentsLabeling(imagePlus.getStack(), conn,
					bitDepth);
			labelPlus = new ImagePlus("Labels", labels);
		}

		// setup display range to show largest label as white
		double nLabels = ImageUtils.findMaxValue(labelPlus);
		labelPlus.setDisplayRange(0, nLabels);
		return labelPlus;
	}
	
	/**
	 * Computes the labels of the connected components in the given planar
	 * binary image. The type of result is controlled by the bitDepth option.
	 * 
	 * Uses a Flood-fill type algorithm.
	 * 
	 * @param image
	 *            contains the binary image (any type is accepted)
	 * @param conn
	 *            the connectivity, either 4 or 8
	 * @param bitDepth
	 *            the number of bits used to create the result image (8, 16 or
	 *            32)
	 * @return a new instance of ImageProcessor containing the label of each
	 *         connected component.
	 * @throws RuntimeException
	 *             if the number of labels reaches the maximum number that can
	 *             be represented with this bitDepth
	 *             
	 * @see inra.ijpb.binary.conncomp.ConnectedComponentsLabeling     
	 */
	public final static ImageProcessor componentsLabeling(ImageProcessor image,
			int conn, int bitDepth) 
	{
		ConnectedComponentsLabeling algo = new FloodFillComponentsLabeling(conn, bitDepth);
		DefaultAlgoListener.monitor(algo);
		return algo.computeLabels(image);
	}

	/**
	 * Computes the labels of the connected components in the given 3D binary
	 * image. The type of result is controlled by the bitDepth option.
	 * 
	 * Uses a Flood-fill type algorithm.
	 * 
	 * @param image
	 *            contains the 3D binary image (any type is accepted)
	 * @param conn
	 *            the connectivity, either 6 or 26
	 * @param bitDepth
	 *            the number of bits used to create the result stack (8, 16 or
	 *            32)
	 * @return a new instance of ImageStack containing the label of each
	 *         connected component.
	 * @throws RuntimeException
	 *             if the number of labels reaches the maximum number that can
	 *             be represented with this bitDepth
	 *             
	 * @see inra.ijpb.binary.conncomp.ConnectedComponentsLabeling3D     
	 */
	public final static ImageStack componentsLabeling(ImageStack image,
			int conn, int bitDepth)
	{
		ConnectedComponentsLabeling3D algo = new FloodFillComponentsLabeling3D(conn, bitDepth);
		DefaultAlgoListener.monitor(algo);
		return algo.computeLabels(image);
	}

	/**
	 * Computes the distance map (or distance transform) from a binary image
	 * processor. Distance is computed for each foreground (white) pixel or
	 * voxel, as the chamfer distance to the nearest background (black) pixel or
	 * voxel.
	 * 
	 * @param imagePlus
	 *            an ImagePlus object containing a binary or label image 
	 * @return a new ImagePlus containing the distance map
	 * 
	 * @see inra.ijpb.binary.distmap.DistanceTransform
	 * @see inra.ijpb.binary.distmap.DistanceTransform3D
	 */
	public static final ImagePlus distanceMap(ImagePlus imagePlus)
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-distMap";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = distanceMap(image);
			resultPlus = new ImagePlus(newName, result);
		} 
		else
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = distanceMap(image);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.copyScale(imagePlus);
		return resultPlus;		
	}
	
	/**
	 * <p>
	 * Computes the distance map (or distance transform) from a binary image
	 * processor. Distance is computed for each foreground (white) pixel, as the
	 * chamfer distance to the nearest background (black) pixel.
	 * </p>
	 * 
	 * <p>
	 * This method uses default 5x5 weights, and normalizes the resulting map.
	 * Result is given in a new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input binary or label image
	 * @return a new ImageProcessor containing the distance map result
	 */
	public static final ImageProcessor distanceMap(ImageProcessor image) 
	{
		return distanceMap(image, new short[]{5, 7, 11}, true);
	}
	
	/**
	 * <p>
	 * Computes the distance map from a binary image processor, by specifying
	 * weights and normalization.
	 * </p>
	 * 
	 * <p>
	 * Distance is computed for each foreground (white) pixel, as the chamfer
	 * distance to the nearest background (black) pixel. Result is given in a
	 * new instance of FloatProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input binary or label image
	 * @param mask
	 *            the chamfer mask used to propagate distances
	 * @param floatingPoint
	 *            indicates if the computation should be performed using
	 *            floating point computation
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageProcessor distanceMap(ImageProcessor image,
			ChamferMask2D mask, boolean floatingPoint, boolean normalize) 
	{
		DistanceTransform algo; 
		if (floatingPoint)
		{
			algo = new ChamferDistanceTransform2DFloat(mask, normalize);	
		}
		else
		{
			algo = new ChamferDistanceTransform2DShort(mask, normalize);
		}
		return algo.distanceMap(image);
	}
	
	/**
	 * <p>
	 * Computes the distance map from a binary image processor, by specifying
	 * weights and normalization.
	 * </p>
	 * 
	 * <p>
	 * Distance is computed for each foreground (white) pixel, as the chamfer
	 * distance to the nearest background (black) pixel. Result is given as a
	 * new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input binary or label image
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ShortProcessor distanceMap(ImageProcessor image,
			short[] weights, boolean normalize)
	{
		ChamferMask2D mask = ChamferMask2D.fromWeights(weights);
		DistanceTransform algo = new ChamferDistanceTransform2DShort(mask, normalize);
		return (ShortProcessor) algo.distanceMap(image);
	}

	/**
	 * <p>
	 * Computes the distance map from a binary image processor, by specifying
	 * weights and normalization.
	 * </p>
	 * 
	 * <p>
	 * Distance is computed for each foreground (white) pixel, as the chamfer
	 * distance to the nearest background (black) pixel. Result is given in a
	 * new instance of FloatProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input binary or label image
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final FloatProcessor distanceMap(ImageProcessor image,
			float[] weights, boolean normalize) 
	{
		ChamferMask2D mask = ChamferMask2D.fromWeights(weights);
		DistanceTransform algo = new ChamferDistanceTransform2DFloat(mask, normalize);
		return (FloatProcessor) algo.distanceMap(image);
	}

	/**
	 * Computes the distance map from a binary 3D image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @param image
	 *            the input binary image
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image)
	{
		ChamferMask3D mask = ChamferMask3D.BORGEFORS;
		DistanceTransform3D	algo = new ChamferDistanceTransform3DFloat(mask);
		return algo.distanceMap(image);
	}
	
	/**
	 * Computes the distance map from a binary 3D image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @param image
	 *            the input binary image
	 * @param chamferMask
	 *            the chamfer mask used for propagating distances
	 * @param floatingPoint
	 * 		      indicates if the distane propagation should use floating point computation 
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image,
			ChamferMask3D chamferMask, boolean floatingPoint, boolean normalize)
	{
		DistanceTransform3D algo = floatingPoint
				? new ChamferDistanceTransform3DFloat(chamferMask, normalize)
				: new ChamferDistanceTransform3DShort(chamferMask, normalize);
		return algo.distanceMap(image);
	}
	
	/**
	 * Computes the distance map from a binary 3D image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @param image
	 *            the input binary image
	 * @param weights
	 *            an array of chamfer weights, with at least three values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image,
			short[] weights, boolean normalize)
	{
		ChamferMask3D mask = ChamferMask3D.fromWeights(weights);
		DistanceTransform3D	algo = new ChamferDistanceTransform3DShort(mask, normalize);
			
		return algo.distanceMap(image);
	}
	
	/**
	 * Computes the distance map from a binary 3D image. 
	 * Distance is computed for each foreground (white) pixel, as the 
	 * chamfer distance to the nearest background (black) pixel.
	 * 
	 * @param image
	 *            the input 3D binary image
	 * @param weights
	 *            an array of chamfer weights, with at least three values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the distance map obtained after applying the distance transform
	 */
	public static final ImageStack distanceMap(ImageStack image, 
			float[] weights, boolean normalize)
	{
		ChamferMask3D mask = ChamferMask3D.fromWeights(weights);
		DistanceTransform3D	algo = new ChamferDistanceTransform3DFloat(mask, normalize);
		return algo.distanceMap(image);
	}
	
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask. Returns the result
	 * in a new instance of ImagePlus.
	 * 
	 * @param markerPlus
	 *            the image containing the marker
	 * @param maskPlus
	 *            the image containing the marker
	 * @return the geodesic distance map in a new ImagePlus
	 */
	public static final ImagePlus geodesicDistanceMap(ImagePlus markerPlus,
			ImagePlus maskPlus)
	{
		ImagePlus result = null;
		String newName = markerPlus.getShortTitle() + "-distMap";
		
		if (markerPlus.getStackSize() == 1 && maskPlus.getStackSize() == 1)
		{
			ImageProcessor distMap = geodesicDistanceMap(markerPlus.getProcessor(), maskPlus.getProcessor());
			result = new ImagePlus(newName, distMap);
		}

		result.copyScale(markerPlus);
		return result;
	}
	
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask.
	 * Returns the result in a new instance of ShortProcessor.
	 * 
	 * @param marker
	 *            the binary image of marker
	 * @param mask
	 *            the binary image of mask
	 * @return the geodesic distance map in a new ImageProcessor
	 */
	public static final ImageProcessor geodesicDistanceMap(ImageProcessor marker,
			ImageProcessor mask) 
	{
		return geodesicDistanceMap(marker, mask, new short[]{5, 7, 11}, true);
	}
	
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask.
	 * Returns the result in a new instance of ShortProcessor.
	 * 
	 * @param marker
	 *            the binary image of marker
	 * @param mask
	 *            the binary image of mask
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the geodesic distance map in a new ImageProcessor
	 */
	public static final ImageProcessor geodesicDistanceMap(ImageProcessor marker,
			ImageProcessor mask, short[] weights, boolean normalize) 
	{
		ChamferMask2D chamferMask = ChamferMask2D.fromWeights(weights);
		GeodesicDistanceTransform algo = new GeodesicDistanceTransformShort(chamferMask, normalize);
		return algo.geodesicDistanceMap(marker, mask);
	}
	
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask. 
	 * Returns the result in a new instance of FloatProcessor.
	 * 
	 * @param marker
	 *            the binary image of marker
	 * @param mask
	 *            the binary image of mask
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return the geodesic distance map in a new ImageProcessor
	 */
	public static final ImageProcessor geodesicDistanceMap(ImageProcessor marker,
			ImageProcessor mask, float[] weights, boolean normalize) 
	{
		ChamferMask2D chamferMask = ChamferMask2D.fromWeights(weights);
		GeodesicDistanceTransform algo = new GeodesicDistanceTransformFloat(chamferMask, normalize);
		return algo.geodesicDistanceMap(marker, mask);
	}
	

	/**
	 * Applies size opening on a binary 2D or 3D image. The method creates a new
	 * binary image that contains only particles with at least the specified
	 * number of pixels.
	 * 
	 * @param imagePlus
	 *            the binary 2D or 3D image containing individual particles
	 * @param minElementCount
	 *            the minimum number of pixels or voxels required to keep a
	 *            particle
	 * @return a new binary image containing only the selected particles
	 * 
	 * @see inra.ijpb.label.LabelImages#sizeOpening(ImagePlus, int)
	 */
	public static final ImagePlus sizeOpening(ImagePlus imagePlus,
			int minElementCount) 
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-sizeOpening";
        
        if (imagePlus.getStackSize() == 1) 
        {
            ImageProcessor image = imagePlus.getProcessor();
            ImageProcessor result = areaOpening(image, minElementCount);
            resultPlus = new ImagePlus(newName, result);    		
        }
        else
        {
            ImageStack image = imagePlus.getStack();
            ImageStack result = LabelImages.volumeOpening(image, minElementCount);
        	result.setColorModel(image.getColorModel());
            resultPlus = new ImagePlus(newName, result);
        }
        
        // keep spatial calibration
		resultPlus.copyScale(imagePlus);
		return resultPlus;
	}

	/**
	 * Applies area opening on a binary image: creates a new binary image that
	 * contains only particles with at least the specified number of pixels.
	 * 
	 * @param image
	 *            the binary image containing individual particles
	 * @param nPixelMin
	 *            the minimum number of pixels required to keep a particle
	 * @return a new binary image containing only the selected particles
	 * 
	 * @see inra.ijpb.label.LabelImages#areaOpening(ImageProcessor, int)
	 */
	public static final ImageProcessor areaOpening(ImageProcessor image,
			int nPixelMin) 
	{
		// Labeling
		ConnectedComponentsLabeling algo = new FloodFillComponentsLabeling(4, 16);	
		ImageProcessor labelImage = algo.computeLabels(image);

		// keep only necessary labels and binarize
		return binarize(LabelImages.areaOpening(labelImage, nPixelMin));
	}
	
	
	/**
	 * Applies area opening on a binary image: creates a new binary image that
	 * contains only particle with at least the specified number of voxels.
	 * 
	 * @param image
	 *            the 3D binary image containing individual particles
	 * @param nVoxelMin
	 *            the minimum number of voxels required to keep a particle
	 * @return a new binary image containing only the selected particles
	 *
	 * @see inra.ijpb.label.LabelImages#volumeOpening(ImageStack, int)
	 */
	public static final ImageStack volumeOpening(ImageStack image, int nVoxelMin) 
	{
		// Labeling
		ImageStack labelImage = componentsLabeling(image, 6, 16);

		// keep only necessary labels and binarize
		return binarize(LabelImages.volumeOpening(labelImage, nVoxelMin));
	}
	
	
	/**
	 * Returns a binary image that contains only the largest region.
	 * 
	 * @param imagePlus
	 *            an instance of imagePlus that contains a binary image
	 * @return a new ImagePlus containing only the largest region from original
	 *         image
	 */
	public static final ImagePlus keepLargestRegion(ImagePlus imagePlus) 
	{
		ImagePlus resultPlus;
		String newName = imagePlus.getShortTitle() + "-largest";
		
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			// process planar image
			ImageProcessor image = imagePlus.getProcessor();
			ImageProcessor result = keepLargestRegion(image);
			resultPlus = new ImagePlus(newName, result);
		} 
		else 
		{
			// process image stack
			ImageStack image = imagePlus.getStack();
			ImageStack result = keepLargestRegion(image);
			resultPlus = new ImagePlus(newName, result);
		}
		
		resultPlus.copyScale(imagePlus);
		return resultPlus;
	}
	
	/**
	 * Returns a binary image that contains only the largest region.
	 * 
	 * @param image
	 *            a binary image containing several individual regions
	 * @return a new binary image containing only the largest region from
	 *         original image
	 */
	public static final ImageProcessor keepLargestRegion(ImageProcessor image) 
	{
		ImageProcessor labelImage = componentsLabeling(image, 4, 16);
		ImageProcessor result = binarize(LabelImages.keepLargestLabel(labelImage));
		result.setLut(image.getLut());
		return result;
	}
	
	/**
	 * Returns a binary image that contains only the largest label.
	 * 
	 * @param image
	 *            a binary 3D image containing several individual regions
	 * @return a new binary image containing only the largest region from
	 *         original image
	 */
	public static final ImageStack keepLargestRegion(ImageStack image) 
	{
		ImageStack labelImage = componentsLabeling(image, 6, 16);
		ImageStack result = binarize(LabelImages.keepLargestLabel(labelImage));
		result.setColorModel(image.getColorModel());
		return result;
	}


	/**
	 * Returns a binary image in which the largest region has been replaced by
	 * background. Works for both 2D and 3D images.
	 * 
	 * @param imagePlus
	 *            a binary image containing several individual particles
	 */
	public static final void removeLargestRegion(ImagePlus imagePlus) 
	{
		// Dispatch to appropriate function depending on dimension
		if (imagePlus.getStackSize() == 1) 
		{
			imagePlus.setProcessor(removeLargestRegion(imagePlus.getProcessor()));
		} 
		else 
		{
			imagePlus.setStack(removeLargestRegion(imagePlus.getStack()));
		}
		
	}

	/**
	 * Returns a binary image in which the largest region has been replaced by
	 * the background value.
	 * 
	 * @param image
	 *            a binary image containing several individual particles
	 * @return a new binary image containing all the regions from original image
	 *         but the largest one
	 */
	public static final ImageProcessor removeLargestRegion(ImageProcessor image) 
	{
		ImageProcessor labelImage = componentsLabeling(image, 4, 16);
		LabelImages.removeLargestLabel(labelImage);
		ImageProcessor result = binarize(labelImage);
		result.setLut(image.getLut());
		return result;

	}

	/**
	 * Returns a binary image in which the largest region has been replaced by
	 * the background value.
	 * 
	 * @param image
	 *            a binary 3D image containing several individual particles
	 * @return a new binary image containing all the regions from original image
	 *         but the largest one
	 */
	public static final ImageStack removeLargestRegion(ImageStack image) 
	{
		ImageStack labelImage = componentsLabeling(image, 6, 16);
		LabelImages.removeLargestLabel(labelImage);
		ImageStack result = binarize(labelImage);
		result.setColorModel(image.getColorModel());
		return result;
	}
	
	/**
	 * Converts a grayscale 2D or 3D image into a binary image by setting
	 * non-zero elements to 255.
	 * 
	 * @param imagePlus
	 *            an ImagePlus object containing a gray scale image
	 * @return a binary image containing 255 for all non-zero elements of
	 *         original image
	 */
	public static final ImagePlus binarize(ImagePlus imagePlus) 
	{
		// Dispatch to appropriate function depending on dimension
		ImagePlus resultPlus;
		String title = imagePlus.getShortTitle() + "-bin";
		if (imagePlus.getStackSize() == 1)
		{
			ImageProcessor result = binarize(imagePlus.getProcessor());
			resultPlus = new ImagePlus(title, result);
		}
		else 
		{
			ImageStack result = binarize(imagePlus.getStack());
			resultPlus = new ImagePlus(title, result);
		}
		return resultPlus;
	}

	/**
	 * Converts a grayscale 2D image into a binary 2D image by setting non-zero
	 * pixels to 255.
	 * 
	 * @param image
	 *            a gray scale image
	 * @return a binary image containing 255 for all non-zero elements of
	 *         original image
	 	 */
	public static final ImageProcessor binarize(ImageProcessor image) 
	{
		int width = image.getWidth();
		int height = image.getHeight();
		ImageProcessor result = new ByteProcessor(width, height);
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++) 
			{
				if (image.get(x, y) > 0) 
					result.set(x, y, 255);
			}
		}
		
		return result;
	}
	
	/**
	 * Converts a grayscale 3D image into a binary 3D image by setting non-zero
	 * voxels to 255.
	 * 
	 * @param image
	 *            a gray scale 3D image
	 * @return a binary image containing 255 for all non-zero elements of
	 *         original image
	 */
	public static final ImageStack binarize(ImageStack image)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if (image.getVoxel(x, y, z) > 0) 
						result.setVoxel(x, y, z, 255);
				}
			}
		}
		
		return result;
	}

	/**
	 * Converts a grayscale 2D or 3D image into a binary image by setting
	 * zero elements to 255, and non zero ones to 0.
	 * 
	 * @param imagePlus
	 *            an ImagePlus object containing a gray scale image
	 * @return a binary image containing 255 for all non-zero elements of
	 *         original image
	 */
	public static final ImagePlus binarizeBackground(ImagePlus imagePlus) 
	{
		// Dispatch to appropriate function depending on dimension
		ImagePlus resultPlus;
		String title = imagePlus.getShortTitle() + "-bg";
		if (imagePlus.getStackSize() == 1)
		{
			ImageProcessor result = binarizeBackground(imagePlus.getProcessor());
			resultPlus = new ImagePlus(title, result);
		}
		else 
		{
			ImageStack result = binarizeBackground(imagePlus.getStack());
			resultPlus = new ImagePlus(title, result);
		}
		return resultPlus;
	}
	
	/**
	 * Converts a grayscale 2D image into a binary 2D image by setting
	 * zero elements to 255, and non zero ones to 0.
	 * 
	 * @param image
	 *            a gray scale image
	 * @return a binary image containing 255 for all non-zero elements of
	 *         original image
	 	 */
	public static final ImageProcessor binarizeBackground(ImageProcessor image) 
	{
		int width = image.getWidth();
		int height = image.getHeight();
		ImageProcessor result = new ByteProcessor(width, height);
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++) 
			{
				if (image.get(x, y) == 0) 
					result.set(x, y, 255);
			}
		}
		
		return result;
	}
	
	/**
	 * Converts a grayscale 3D image into a binary 3D image by setting
	 * zero elements to 255, and non zero ones to 0.
	 * 
	 * @param image
	 *            a gray scale 3D image
	 * @return a binary image containing 255 for all non-zero elements of
	 *         original image
	 */
	public static final ImageStack binarizeBackground(ImageStack image)
	{
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		int sizeZ = image.getSize();
		
		ImageStack result = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		
		for (int z = 0; z < sizeZ; z++)
		{
			for (int y = 0; y < sizeY; y++) 
			{
				for (int x = 0; x < sizeX; x++) 
				{
					if (image.getVoxel(x, y, z) == 0) 
						result.setVoxel(x, y, z, 255);
				}
			}
		}
		
		return result;
	}
	
    /**
     * Computes the skeleton of a binary image, and returns another binary image.
     * 
     * Uses an adaptation of the algorithm from ImageJ.
     * 
     * @param image
     *            a binary image
     * @return a binary image containing 255 for skeleton elements
     */
    public static final ImageProcessor skeleton(ImageProcessor image) 
    {
        return new ImageJSkeleton().process(image);
    }
}
