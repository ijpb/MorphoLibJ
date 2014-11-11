/**
 * 
 */
package inra.ijpb.binary;

/**
 * A pre-defined set of weigths that can be used to compute distance maps.
 * Provides methosd to access weight values either as float array or as short
 * array.
 * 
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageStack, short[], boolean)
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ImageStack, float[], boolean)
 */
public enum ChamferWeights3D
{
	CHESSBOARD("Chessboard (1,1,1)", new short[] { 1, 1, 1 }), 
	CITY_BLOCK("City-Block (1,2,3)", new short[] { 1, 2, 3 }), 
	QUASI_EUCLIDEAN("Quasi-Euclidean (1,1.41,1.73)", 
			new short[] { 10, 14 }, 
			new float[] {1, (float) Math.sqrt(2), (float) Math.sqrt(3)}), 
	BORGEFORS("Borgefors (3,4,5)", new short[] { 3, 4, 5 });

	private final String label;
	private final short[] shortWeights;
	private final float[] floatWeights;

	private ChamferWeights3D(String label, short[] shortWeights)
	{
		this.label = label;
		this.shortWeights = shortWeights;
		this.floatWeights = new float[shortWeights.length];
		for (int i = 0; i < shortWeights.length; i++)
			this.floatWeights[i] = (float) shortWeights[i];
	}

	private ChamferWeights3D(String label, short[] shortWeights,
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
		int n = ChamferWeights3D.values().length;
		String[] result = new String[n];

		int i = 0;
		for (ChamferWeights3D weight : ChamferWeights3D.values())
			result[i++] = weight.label;

		return result;
	}

	/**
	 * Determines the operation type from its label.
	 * 
	 * @throws IllegalArgumentException
	 *             if label is not recognized.
	 */
	public static ChamferWeights3D fromLabel(String label)
	{
		if (label != null)
			label = label.toLowerCase();
		for (ChamferWeights3D weight : ChamferWeights3D.values())
		{
			String cmp = weight.label.toLowerCase();
			if (cmp.equals(label))
				return weight;
		}
		throw new IllegalArgumentException(
				"Unable to parse ChamferWeights3D with label: " + label);
	}

}
