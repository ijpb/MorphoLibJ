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
 *	short[] shortWeights = ChamferWeights.CHESSKNIGHT.getShortWeights();
 *	boolean normalize = true;
 *	DistanceTransform dt = new DistanceTransform5x5Short(shortWeights, normalize);
 *	ImageProcessor result = dt.distanceMap(inputImage);
 *	// or:
 *	ImagePlus resultPlus = BinaryImages.distanceMap(imagePlus, shortWeights, normalize);
 * </code></pre>
 * 
 * @deprecated replaced by inra.ijpb.binary.distmap.ChamferMask2D (since 1.5.0)
 * 
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ij.process.ImageProcessor, short[], boolean)
 * @see inra.ijpb.binary.BinaryImages#distanceMap(ij.process.ImageProcessor, float[], boolean)
 * @see inra.ijpb.binary.distmap.DistanceTransform
 */
@Deprecated
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
	/**
	 * Use weights 3 for orthogonal neighbors and 4 for diagonal neighbors (best
	 * approximation of Euclidean distance for 3-by-3 masks)
	 */
	BORGEFORS("Borgefors (3,4)", new short[] { 3, 4 }),
	/** Use weights 2 for orthogonal neighbors and 3 for diagonal neighbors */
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

	/**
	 * @return the weights as shorts
	 */
	public short[] getShortWeights()
	{
		return this.shortWeights;
	}

	/**
	 * @return the weights as floats
	 */
	public float[] getFloatWeights()
	{
		return this.floatWeights;
	}

	/**
	 * @return the label associated to this chamfer weight
	 */
	public String toString()
	{
		return this.label;
	}

	/**
	 * @return all the chamfer weights labels
	 */
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
	 * @param label the name of a chamfer weight 
	 * @return the ChamferWeights enum corresponding to the given name
	 * 
	 * @throws IllegalArgumentException
	 *             if label name is not recognized.
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
