/**
 * 
 */
package inra.ijpb.util;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;


/**
 * Contains static methods for creating common color maps.
 * @author David Legland
 *
 */
public class ColorMaps {

	/**
	 * A collection of color maps useful for displaying label images.
	 */
	public enum CommonLabelMaps {
		GRAYS("Grays"), 
		FIRE("Fire"),
		ICE("Ice"), 
		SPECTRUM("Spectrum"), 
		RGB332("RGB 3-3-2"), 
		MAIN_COLORS("Main Colors"), 
		MIXED_COLORS("Mixed Colors"),
		REDGREEN("Red-Green"); 
		
		private final String label;

		CommonLabelMaps(String label) {
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}

		public String toString() {
			return label;
		}
		
		public byte[][] computeLut(int nValues, boolean shuffle) {
			byte[][] lut;
			// create a lut with as many colors as the number of labels
			if (this == GRAYS) {
				lut = ColorMaps.createGrayLut();
			} else if (this == FIRE) {
				lut = ColorMaps.createFireLut(nValues);
			} else if (this == ICE) {
				lut = ColorMaps.createIceLut(nValues);
			} else if (this == SPECTRUM) {
				lut = ColorMaps.createSpectrumLut();
			} else if (this == RGB332) {
				lut = ColorMaps.createRGB332Lut();
			} else if (this == MAIN_COLORS) {
				lut = ColorMaps.createMainColorsLut();
				if (lut.length != nValues)
					lut = ColorMaps.circularLut(lut, nValues);
				
			} else if (this == MIXED_COLORS) {
				lut = ColorMaps.createMixedColorsLut();
				if (lut.length != nValues)
					lut = ColorMaps.circularLut(lut, nValues);

			} else if (this == REDGREEN) {
				lut = ColorMaps.createRedGreenLut();
			} else {
				throw new RuntimeException("Could not create lut for name: " + label);
			}
			
			if (lut.length != nValues) {
				lut = ColorMaps.interpolateLut(lut, nValues);
			}
			
			if (shuffle) {
				lut = ColorMaps.shuffleLut(lut, 42);
			}

			return lut;
		}
		
		public static String[] getAllLabels(){
			int n = CommonLabelMaps.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (CommonLabelMaps map : CommonLabelMaps.values())
				result[i++] = map.label;
			
			return result;
		}
		
		/**
		 * Determines the color map from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static CommonLabelMaps fromLabel(String label) {
			if (label != null)
				label = label.toLowerCase();
			for (CommonLabelMaps map : CommonLabelMaps.values()) {
				String cmp = map.label.toLowerCase();
				if (cmp.equals(label))
					return map;
			}
			throw new IllegalArgumentException("Unable to parse CommonLabelMaps with label: " + label);
		}
		
	};

	/**
	 * Creates a Java.awt.image.ColorModel from a color map given as an triplet
	 * of byte arrays.
	 */
	public final static ColorModel createColorModel(byte[][] cmap) {
		int n = cmap.length;
		return new IndexColorModel(8, n, cmap[0], cmap[1], cmap[2]);
	}
	
	/**
	 * Creates a Java.awt.image.ColorModel from a color map given as an triplet
	 * of byte arrays, and a color for the background that will be associated 
	 * to label 0.
	 */
	public final static ColorModel createColorModel(byte[][] cmap, Color bg) {
		int n = cmap.length;
		byte[] r = new byte[n+1];
		byte[] g = new byte[n+1];
		byte[] b = new byte[n+1];
		
		r[0] = (byte) bg.getRed();
		g[0] = (byte) bg.getGreen();
		b[0] = (byte) bg.getBlue();

		for (int i = 0; i < n; i++) {
			r[i+1] = cmap[i][0];
			g[i+1] = cmap[i][1];
			b[i+1] = cmap[i][2];
		}
		return new IndexColorModel(8, n+1, r, g, b);
	}
	
	public final static byte[][] createFireLut(int nColors) {
		byte[][] lut = createFireLut();
		if (nColors != lut.length) 
			lut = interpolateLut(lut, nColors);
		return lut;
	}

	public final static byte[][] createFireLut() {
		// initial values
		int[] r = { 0, 0, 1, 25, 49, 73, 98, 122, 146, 162, 173, 184, 195, 207,
				217, 229, 240, 252, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255 };
		int[] g = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101,
				117, 133, 147, 161, 175, 190, 205, 219, 234, 248, 255, 255,
				255, 255 };
		int[] b = { 0, 61, 96, 130, 165, 192, 220, 227, 210, 181, 151, 122, 93,
				64, 35, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 98, 160, 223,
				255 };

		// create map
		byte[][] map = new byte[r.length][3];
		
		// cast elements
		for (int i = 0; i < r.length; i++) {
			map[i][0] = (byte) r[i];
			map[i][1] = (byte) g[i];
			map[i][2] = (byte) b[i];
		}
		
		return  map;
	}
	
	public final static byte[][] createGrayLut() {
		// create map
		byte[][] map = new byte[256][3];
		
		// cast elements
		for (int i = 0; i < 256; i++) {
			map[i][0] = (byte) i;
			map[i][1] = (byte) i;
			map[i][2] = (byte) i;
		}
		
		return map;
	}
	
	public final static byte[][] createIceLut(int nColors) {
		byte[][] lut = createIceLut();
		if (nColors != lut.length) 
			lut = interpolateLut(lut, nColors);
		return lut;
	}

	public final static byte[][] createIceLut() {
		// initial values
		int[] r = { 0, 0, 0, 0, 0, 0, 19, 29, 50, 48, 79, 112, 134, 158, 186,
				201, 217, 229, 242, 250, 250, 250, 250, 251, 250, 250, 250,
				250, 251, 251, 243, 230 };
		int[] g = { 156, 165, 176, 184, 190, 196, 193, 184, 171, 162, 146, 125,
				107, 93, 81, 87, 92, 97, 95, 93, 93, 90, 85, 69, 64, 54, 47,
				35, 19, 0, 4, 0 };
		int[] b = { 140, 147, 158, 166, 170, 176, 209, 220, 234, 225, 236, 246,
				250, 251, 250, 250, 245, 230, 230, 222, 202, 180, 163, 142,
				123, 114, 106, 94, 84, 64, 26, 27 };
		
		// create map
		byte[][] map = new byte[r.length][3];
		
		// cast elements
		for (int i = 0; i < r.length; i++) {
			map[i][0] = (byte) r[i];
			map[i][1] = (byte) g[i];
			map[i][2] = (byte) b[i];
		}
		
		return  map;
	}

	public final static byte[][] createSpectrumLut() {
		// create map
		byte[][] map = new byte[256][3];

		// cast elements
		for (int i = 0; i < 256; i++) {
			Color c = Color.getHSBColor(i / 255f, 1f, 1f);
			map[i][0] = (byte) c.getRed();
			map[i][1] = (byte) c.getGreen();
			map[i][2] = (byte) c.getBlue();
		}

		return  map;
	}

	public final static byte[][] createRGB332Lut() {
		// create map
		byte[][] map = new byte[256][3];

		// cast elements
		for (int i = 0; i < 256; i++) {
			map[i][0] = (byte) (i & 0xe0);
			map[i][1] = (byte) ((i<<3) & 0xe0);
			map[i][2] = (byte) ((i<<6) & 0xc0);
		}

		return  map;
	}

	public final static byte[][] createRedGreenLut() {
		// create map
		byte[][] map = new byte[256][3];

		for (int i=0; i<128; i++) {
			map[i][0] =  (byte)(i*2);
			map[i][1] =  (byte) 0;
			map[i][2] =  (byte) 0;
		}
		for (int i=128; i<256; i++) {
			map[i][0] = (byte) 0;
			map[i][1] = (byte)(i*2);
			map[i][2] = (byte) 0;
		}
		return map;
	}

	/**
	 * Returns a LUT with only the 6 main colors.
	 */
	public final static byte[][] createMainColorsLut() {
		return new byte[][]{
				{(byte) 255, (byte) 0, (byte) 0}, 
				{(byte) 0, (byte) 255, (byte) 0},
				{(byte) 0, (byte) 0, (byte) 255},
				{(byte) 0, (byte) 255, (byte) 255},
				{(byte) 255, (byte) 0, (byte) 255}, 
				{(byte) 255, (byte) 255, (byte) 0}
		};
	}

	/**
	 * Returns a LUT with only the main colors and their mixture.
	 */
	public final static byte[][] createMixedColorsLut() {
		return new byte[][]{
				{(byte) 255, (byte) 0, (byte) 0}, 
				{(byte) 0, (byte) 255, (byte) 0},
				{(byte) 0, (byte) 0, (byte) 255},
				
				{(byte) 0, (byte) 255, (byte) 255},
				{(byte) 255, (byte) 0, (byte) 255}, 
				{(byte) 255, (byte) 255, (byte) 0},
				
				{(byte) 127, (byte) 0, (byte) 0}, 
				{(byte) 0, (byte) 127, (byte) 0},
				{(byte) 0, (byte) 0, (byte) 127},
				
				{(byte) 127, (byte) 127, (byte) 0}, 
				{(byte) 0, (byte) 127, (byte) 127},
				{(byte) 127, (byte) 0, (byte) 127},
				
				{(byte) 127, (byte) 127, (byte) 255}, 
				{(byte) 255, (byte) 127, (byte) 127},
				{(byte) 127, (byte) 255, (byte) 127},
				
				{(byte) 127, (byte) 0, (byte) 255}, 
				{(byte) 127, (byte) 255, (byte) 0}, 
				{(byte) 127, (byte) 255, (byte) 255}, 
				
				{(byte) 0, (byte) 127, (byte) 255}, 
				{(byte) 255, (byte) 127, (byte) 0}, 
				{(byte) 255, (byte) 127, (byte) 255}, 

				{(byte) 0, (byte) 255, (byte) 127}, 
				{(byte) 255, (byte) 0, (byte) 127}, 
				{(byte) 255, (byte) 255, (byte) 127}, 
		};
	}

	public final static byte[][] interpolateLut(byte[][] baseLut, int nColors) {
		
		int n0 = baseLut.length;
		// allocate memory for new lut
		byte[][] lut = new byte[nColors][3];
		
		// linear interpolation of each color of new lut
		for (int i = 0; i < nColors; i++) {
			// compute color index in original lut
			float i0 = ((float) i) * n0 / nColors;
			int i1 = (int) Math.floor(i0);
			
			// the two surrounding colors
			byte[] col1 = baseLut[i1];
			byte[] col2 = baseLut[Math.min(i1 + 1, n0 - 1)];

			// the ratio between the two surrounding colors
			float f = i0 - i1;
			
			// linear interpolation of surrounding colors with cast
			lut[i][0] = (byte) ((1. - f) * (col1[0] & 0xFF) + f * (col2[0] & 0xFF));
			lut[i][1] = (byte) ((1. - f) * (col1[1] & 0xFF) + f * (col2[1] & 0xFF));
			lut[i][2] = (byte) ((1. - f) * (col1[2] & 0xFF) + f * (col2[2] & 0xFF));
		}
		
		return lut;
	}

	public final static byte[][] circularLut(byte[][] baseLut, int nColors) {
		int n0 = baseLut.length;
		// allocate memory for new lut
		byte[][] lut = new byte[nColors][3];
		
		// linear interpolation of each color of new lut
		for (int i = 0; i < nColors; i++) {
			lut[i] = baseLut[i % n0];
		}
		return lut;
	}

	public final static byte[][] shuffleLut(byte[][] lut) {
		// initialize an array of random values
		int n = lut.length;
		double[] values = new double[n];
		Random random = new Random();
		for (int i = 0; i < n; i++)
			values[i] = random.nextDouble();
		
		// Sort the values, and keep ordering indices
		int[] indices = sort(values);
		
		// Create the resulting lut
		byte[][] result = new byte[n][];
		for (int i = 0; i < n; i++)
			result[i] = lut[indices[i]];
		return result;
	}
	
	/**
	 * Shuffles the LUT, using the specified seed for initializing the random 
	 * generator.
	 */
	public final static byte[][] shuffleLut(byte[][] lut, long seed) {
		// initialize an array of random values
		int n = lut.length;
		double[] values = new double[n];
		Random random = new Random(seed);
		for (int i = 0; i < n; i++)
			values[i] = random.nextDouble();
		
		// Sort the values, and keep ordering indices
		int[] indices = sort(values);
		
		// Create the resulting lut
		byte[][] result = new byte[n][];
		for (int i = 0; i < n; i++)
			result[i] = lut[indices[i]];
		return result;
	}
	
	/**
	 * Sorts the input array in increasing order, and returns the indices of 
	 * elements.
	 */
	private final static int[] sort(double[] array) {
		DoubleArrayIndexComparator comparator = new DoubleArrayIndexComparator(array);
		Integer[] indices = comparator.createIndexArray();
		Arrays.sort(indices, comparator);

		// convert Integer classes to int
		int[] sortedIndices = new int[indices.length];
		for (int i = 0; i < indices.length; i++)
			sortedIndices[i] = indices[i];

		return sortedIndices;
	}
	
	/**
	 * Utility class used for sorting double values.
	 */
	private static class DoubleArrayIndexComparator implements Comparator<Integer>
	{
	    private final double[] array;

		public DoubleArrayIndexComparator(double[] array) {
			this.array = array;
		}

		public Integer[] createIndexArray() {
			Integer[] indexes = new Integer[array.length];
			for (int i = 0; i < array.length; i++) {
				indexes[i] = i; // Autoboxing
			}
			return indexes;
		}

		@Override
		public int compare(Integer index1, Integer index2) {
			return Double.compare(array[index1], array[index2]);
		}
	}
}
