/**
 * <p>Computations of distance maps on binary images, using chamfer distances
 * (integer approximation of Euclidean distances).</p>
 * 
 * <p>Contains implementations for computation using shorts or float values, 
 * for 3x3 and 5x5 neighborhoods.</p>
 * 
 * <p>
 * Example of use:
 * <pre><code>
 *	short[] shortWeights = ChamferWeights.CHESSKNIGHT.getShortWeights();
 *	boolean normalize = true;
 *	DistanceTransform dt = new DistanceTransform5x5Short(shortWeights, normalize);
 *	ImageProcessor result = dt.distanceMap(inputImage);
 *	// or:
 *	ImagePlus resultPlus = BinaryImages.distanceMap(imagePlus, shortWeights, normalize);
 * </code></pre>
 * 
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageProcessor, short[], boolean)
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageProcessor, float[], boolean)
 * @see inra.ijpb.binary.distmap.DistanceTransform
 */
package inra.ijpb.binary.distmap;

