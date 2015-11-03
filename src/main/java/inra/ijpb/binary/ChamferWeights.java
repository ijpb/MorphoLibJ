/**
 * 
 */
package inra.ijpb.binary;

/**
 * <p>
 * A pre-defined set of weights that can be used to compute distance transform
 * using chamfer approximations of Euclidean metric.
 * </p>
 * 
 * <p>
 * Provides methods to access weight values either as float array or as short
 * array.
 * </p>
 * 
 * <p>
 * Example of use:
 * <pre><code>
 * short[] floatWeights = ChamferWeights.BORGEFORS.getShortWeights();
 * boolean normalize = true;
 * DistanceTransform dt = new DistanceTransform3x3Float(floatWeights, normalize);
 * ImageProcessor result = dt.distanceMap(inputImage);
 * // or:
 * ImagePlus resultPlus = BinaryImages.distanceMap(imagePlus, floatWeights, normalize);
 * </code></pre>
 *  
 * 
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageProcessor, short[], boolean)
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageProcessor, float[], boolean)
 * @see inra.ijpb.binary.distmap.DistanceTransform
 */
public enum ChamferWeights
{
	/** Use weight equal to 1 for all neighbors */
	CHESSBOARD("Chessboard (1,1)", new short[] { 1, 1 }), 
	/** Use weights 1 for orthogonal neighbors and 2 for diagonal neighbors */
	CITY_BLOCK("City-Block (1,2)", new short[] { 1, 2 }), 
	/**
	 * Use weights 1 for orthogonal neighbors and sqrt(2) for diagonal
	 * neighbors. Use 10 and 14 for short version.
	 */
	QUASI_EUCLIDEAN("Quasi-Euclidean (1,1.41)", 
			new short[] { 10, 14 }, 
			new float[] {1, (float) Math.sqrt(2) }), 
	/** Use weights 1 for orthogonal neighbors and 2 for diagonal neighbors */
	BORGEFORS("Borgefors (3,4)", new short[] { 3, 4 }), 
	/**
	 * Use weights 3 for orthogonal neighbors and 4 for diagonal neighbors (best
	 * approximation of Euclidean distance for 3-by-3 masks)
	 */
	WEIGHTS_23("Weights (2,3)", new short[] { 2, 3 }), 
	/** Use weights 5 for orthogonal neighbors and 7 for diagonal neighbors */
	WEIGHTS_57("Weights (5,7)", new short[] { 5, 7 }), 
	/**
	 * Use weights 5 for orthogonal neighbors and 7 for diagonal neighbors, and
	 * 11 for chess-knight moves (best approximation for 5-by-5 masks).
	 */
	CHESSKNIGHT("Chessknight (5,7,11)", new short[] { 5, 7, 11 });

	private final String label;
	private final short[] shortWeights;
	private final float[] floatWeights;

	private ChamferWeights(String label, short[] shortWeights)
	{
		this.label = label;
		this.shortWeights = shortWeights;
		this.floatWeights = new float[shortWeights.length];
		for (int i = 0; i < shortWeights.length; i++)
			this.floatWeights[i] = (float) shortWeights[i];
	}

	private ChamferWeights(String label, short[] shortWeights,
			float[] floatWeights)
	{
		this.label = label;
		this.shortWeights = shortWeights;
		this.floatWeights = floatWeights;
	}

	public short[] getShortWeights()
	{
		return this.shortWeights;
	}

	public float[] getFloatWeights()
	{
		return this.floatWeights;
	}

	public String toString()
	{
		return this.label;
	}

	public static String[] getAllLabels()
	{
		int n = ChamferWeights.values().length;
		String[] result = new String[n];

		int i = 0;
		for (ChamferWeights weight : ChamferWeights.values())
			result[i++] = weight.label;

		return result;
	}

	/**
	 * Determines the operation type from its label.
	 * 
	 * @throws IllegalArgumentException
	 *             if label is not recognized.
	 */
	public static ChamferWeights fromLabel(String label)
	{
		if (label != null)
			label = label.toLowerCase();
		for (ChamferWeights weight : ChamferWeights.values())
		{
			String cmp = weight.label.toLowerCase();
			if (cmp.equals(label))
				return weight;
		}
		throw new IllegalArgumentException(
				"Unable to parse ChamferWeights with label: " + label);
	}

}
