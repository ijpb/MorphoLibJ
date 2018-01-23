/**
 * 
 */
package inra.ijpb.label;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import inra.ijpb.label.distmap.LabelDistanceTransform;
import inra.ijpb.label.distmap.LabelDistanceTransform3x3Float;
import inra.ijpb.label.distmap.LabelDistanceTransform3x3Short;
import inra.ijpb.label.distmap.LabelDistanceTransform5x5Float;
import inra.ijpb.label.distmap.LabelDistanceTransform5x5Short;
import inra.ijpb.label.geodesic.LabelGeodesicDistanceTransform;
import inra.ijpb.label.geodesic.LabelGeodesicDistanceTransform5x5FloatScanning;
import inra.ijpb.label.geodesic.LabelGeodesicDistanceTransform5x5ShortScanning;

/**
 * Collection of static methods for computing distances or distance maps within
 * label images.
 * 
 * @author dlegland
 *
 */
@Deprecated
public class LabelDistances
{
	/**
	 * Private constructor to prevent class instantiation.
	 */
	protected LabelDistances()
	{
	}
	
	public static final ImagePlus distanceMap(ImagePlus imagePlus)
	{
		ImageProcessor result = distanceMap(imagePlus.getProcessor());

		String title = imagePlus.getShortTitle() + "-distMap";
		return new ImagePlus(title, result);
	}

	/**
	 * <p>
	 * Computes the distance map (or distance transform) from a label image. 
	 * Distance is computed for pixel within a label (with value &gt; 0), as the
	 * chamfer distance to the nearest pixel with a different value.
	 * </p>
	 * 
	 * <p>
	 * This method uses default 5x5 weights, and normalizes the resulting map.
	 * Result is given in a new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input label image
	 * @return a new ImageProcessor containing the distance map result
	 */
	public static final ImageProcessor distanceMap(ImageProcessor image) 
	{
		return distanceMap(image, new short[]{5, 7, 11}, true);
	}
	
	/**
	 * <p>
	 * Computes the distance map (or distance transform) from a label image, by specifying
	 * weights and normalization.
	 *  
	 * Distance is computed for pixel within a label (with value &gt; 0), as the
	 * chamfer distance to the nearest pixel with a different value.
	 * </p>
	 * 
	 * <p>
	 * This method uses default 5x5 weights, and normalizes the resulting map.
	 * Result is given in a new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input label image
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return a new ImageProcessor containing the distance map result
	 */
	public static final ShortProcessor distanceMap(ImageProcessor image,
			short[] weights, boolean normalize)
	{
		LabelDistanceTransform algo;
		switch (weights.length) {
		case 2:
			algo = new LabelDistanceTransform3x3Short(weights, normalize);
			break;
		case 3:
			algo = new LabelDistanceTransform5x5Short(weights, normalize);
			break;
		default:
			throw new IllegalArgumentException(
					"Requires weight array with 2 or 3 elements");
		}

		return (ShortProcessor) algo.distanceMap(image);
	}

	/**
	 * <p>
	 * Computes the distance map (or distance transform) from a label image, by specifying
	 * weights and normalization.
	 *  
	 * Distance is computed for pixel within a label (with value &gt; 0), as the
	 * chamfer distance to the nearest pixel with a different value.
	 * </p>
	 * 
	 * <p>
	 * This method uses default 5x5 weights, and normalizes the resulting map.
	 * Result is given in a new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param image
	 *            the input label image
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return a new ImageProcessor containing the distance map result
	 */
	public static final FloatProcessor distanceMap(ImageProcessor image,
			float[] weights, boolean normalize) 
	{
		LabelDistanceTransform algo;
		switch (weights.length) 
		{
		case 2:
			algo = new LabelDistanceTransform3x3Float(weights, normalize);
			break;
		case 3:
			algo = new LabelDistanceTransform5x5Float(weights, normalize);
			break;
		default:
			throw new IllegalArgumentException(
					"Requires weight array with 2 or 3 elements");
		}
		
		return (FloatProcessor) algo.distanceMap(image);
	}

	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a label image of mask.
	 * Returns the result in a new instance of ShortProcessor.
	 *  
	 * Distance is computed for pixel within a label (with value &gt; 0), as the
	 * chamfer distance to the nearest pixel with a different value.
	 * 
	 * <p>
	 * This method uses default 5x5 weights, and normalizes the resulting map.
	 * Result is given in a new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param marker
	 *            the input binary image of markers
	 * @param mask
	 *            the input label image of mask(s)
	 * @return a new ImageProcessor containing the distance map result
	 */
	public static final ImagePlus geodesicDistanceMap(ImagePlus marker, ImagePlus mask)
	{
		return geodesicDistanceMap(marker, mask, new short[]{5, 7, 11}, true);
	}

	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a label image of mask.
	 * Returns the result in a new instance of ShortProcessor.
	 *  
	 * Distance is computed for pixel within a label (with value &gt; 0), as the
	 * chamfer distance to the nearest pixel with a different value.
	 * 
	 * <p>
	 * This method uses default 5x5 weights, and normalizes the resulting map.
	 * Result is given in a new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param marker
	 *            the input binary image of markers
	 * @param mask
	 *            the input label image of mask(s)
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return a new ImageProcessor containing the distance map result
	 */
	public static final ImagePlus geodesicDistanceMap(ImagePlus marker, ImagePlus mask,
			short[] weights, boolean normalize)
	{
		LabelGeodesicDistanceTransform algo;
		algo = new LabelGeodesicDistanceTransform5x5ShortScanning(weights, normalize);

		ImageProcessor result = algo.geodesicDistanceMap(marker.getProcessor(), mask.getProcessor());
		
		String title = marker.getShortTitle() + "-geoddist";
		return new ImagePlus(title, result);
	}


	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a label image of mask.
	 * 
	 * Uses default Chess-knight distance weights (5,7,11), coded as an array of
	 * short, and normalizes the resulting map.
	 * 
	 * @param marker
	 *            the input binary image of markers
	 * @param mask
	 *            the input label image of mask(s)
	 * @return a new ImageProcessor containing the distance map result
	 */
	public static final ImageProcessor geodesicDistanceMap(ImageProcessor marker, ImageProcessor mask)
	{
		return geodesicDistanceMap(marker, mask, new short[]{5, 7, 11}, true);
	}
	
	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a label image of mask.
	 * Returns the result in a new instance of ShortProcessor.
	 *  
	 * Distance is computed for pixel within a label (with value &gt; 0), as the
	 * chamfer distance to the nearest pixel with a different value.
	 * 
	 * <p>
	 * This method uses default 5x5 weights, and normalizes the resulting map.
	 * Result is given in a new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param marker
	 *            the input binary image of markers
	 * @param mask
	 *            the input label image of mask(s)
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return a new ImageProcessor containing the distance map result
	 */
	public static final ShortProcessor geodesicDistanceMap(ImageProcessor marker, ImageProcessor mask,
			short[] weights, boolean normalize)
	{
		LabelGeodesicDistanceTransform algo;
		algo = new LabelGeodesicDistanceTransform5x5ShortScanning(weights, normalize);

		return (ShortProcessor) algo.geodesicDistanceMap(marker, mask);
	}

	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a label image of mask.
	 * Returns the result in a new instance of FloatProcessor.
	 *  
	 * Distance is computed for pixel within a label (with value &gt; 0), as the
	 * chamfer distance to the nearest pixel with a different value.
	 * 
	 * <p>
	 * This method uses default 5x5 weights, and normalizes the resulting map.
	 * Result is given in a new instance of ShortProcessor.
	 * </p>
	 * 
	 * @param marker
	 *            the input binary image of markers
	 * @param mask
	 *            the input label image of mask(s)
	 * @param weights
	 *            an array of chamfer weights, with at least two values
	 * @param normalize
	 *            indicates whether the resulting distance map should be
	 *            normalized (divide distances by the first chamfer weight)
	 * @return a new ImageProcessor containing the distance map result
	 */
	public static final FloatProcessor geodesicDistanceMap(ImageProcessor marker, ImageProcessor mask,
			float[] weights, boolean normalize) 
	{
		LabelGeodesicDistanceTransform algo;
		algo = new LabelGeodesicDistanceTransform5x5FloatScanning(weights, normalize);

		return (FloatProcessor) algo.geodesicDistanceMap(marker, mask);
	}

}
