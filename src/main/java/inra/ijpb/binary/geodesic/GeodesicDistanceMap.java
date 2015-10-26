/**
 * 
 */
package inra.ijpb.binary.geodesic;

import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Interface for computing Geodesic distance maps (also known as geodesic
 * distance transform) from binary images.
 *
 * @author David Legland
 */
public interface GeodesicDistanceMap
{
	/**
	 * A pre-defined set of weigths that can be used to compute distance maps.
	 * 
	 * @deprecated use inra.ijpb.binary.ChamferWeights instead
	 */
	@Deprecated
	public enum Weights {
		CHESSBOARD("Chessboard (1,1)", new short[]{1,1}),
		CITY_BLOCK("City-Block (1,2)", new short[]{1, 2}),
		QUASI_EUCLIDEAN("Quasi-Euclidean (1,1.41)", 
				new short[]{10, 14}, 
				new float[]{1, (float)Math.sqrt(2)}),
		BORGEFORS("Borgefors (3,4)", new short[]{3, 4}),
		WEIGHTS_23("Weights (2,3)", new short[]{2, 3}),
		WEIGHTS_57("Weights (5,7)", new short[]{5, 7}),	
		CHESSKNIGHT("Chessknight (5,7,11)", new short[]{5, 7, 11});

		private final String label;
		private final short[] shortWeights;
		private final float[] floatWeights;
		
		private Weights(String label, short[] shortWeights) {
			this.label = label;
			this.shortWeights = shortWeights;
			this.floatWeights = new float[shortWeights.length];
			for (int i = 0; i < shortWeights.length; i++)
				this.floatWeights[i] = (float) shortWeights[i];
		}

		private Weights(String label, short[] shortWeights, float[] floatWeights) {
			this.label = label;
			this.shortWeights = shortWeights;
			this.floatWeights = floatWeights;
		}
		
		public short[] getShortWeights() {
			return this.shortWeights;
		}
		
		public float[] getFloatWeights() {
			return this.floatWeights;
		}
		
		public String toString() {
			return this.label;
		}
		
		public static String[] getAllLabels(){
			int n = Weights.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Weights weight : Weights.values())
				result[i++] = weight.label;
			
			return result;
		}
		
		/**
		 * Determines the operation type from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Weights fromLabel(String label) {
			if (label != null)
				label = label.toLowerCase();
			for (Weights weight : Weights.values()) {
				String cmp = weight.label.toLowerCase();
				if (cmp.equals(label))
					return weight;
			}
			throw new IllegalArgumentException("Unable to parse Weights with label: " + label);
		}

	}

	/**
	 * @deprecated only the method using ImageProcessing should be called
	 */
	@Deprecated
	public ImagePlus geodesicDistanceMap(ImagePlus mask, ImagePlus marker,
			String newName);

	/**
	 * Computes the geodesic distance transform (or geodesic distance map) of a
	 * binary image of marker, constrained to a binary mask.
	 */
	public ImageProcessor geodesicDistanceMap(ImageProcessor marker,
			ImageProcessor mask);
}
