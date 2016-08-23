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
		GLASBEY("Glasbey"),
		GOLDEN_ANGLE( "Golden angle" ),
		ICE("Ice"), 
		SPECTRUM("Spectrum"), 
		JET("Jet"), 
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
			} else if (this == GLASBEY) {
				lut = ColorMaps.createGlasbeyLut();
			}else if (this == GOLDEN_ANGLE) {
				lut = ColorMaps.createGoldenAngleLut( nValues );
			}else if (this == ICE) {
				lut = ColorMaps.createIceLut(nValues);
			} else if (this == SPECTRUM) {
				lut = ColorMaps.createSpectrumLut();
			} else if (this == JET) {
				lut = ColorMaps.createJetLut(nValues);
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
		 * 
		 * @param label
		 *            the name of the color map
		 * @return the enumeration item corresponding to the given label
		 * @throws IllegalArgumentException
		 *             if label is not recognized.
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
	 * Private constructor to prevent class instantiation.
	 */
	private ColorMaps()
	{
	}

	/**
	 * Creates a Java.awt.image.ColorModel from a color map given as an triplet
	 * of byte arrays.
	 * 
	 * @param cmap a colormap given as a triplet of byte arrays
	 * @return the corresponding color model
	 */
	public final static ColorModel createColorModel(byte[][] cmap) {
		int n = cmap.length;
		return new IndexColorModel(8, n, cmap[0], cmap[1], cmap[2]);
	}
	
	/**
	 * Creates a Java.awt.image.ColorModel from a color map given as an triplet
	 * of byte arrays, and a color for the background that will be associated 
	 * to label 0.
	 * 
	 * @param cmap a colormap given as a triplet of byte arrays
	 * @param bg the color associated to the background
	 * @return the corresponding color model
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
	
	/**
	 * Create lookup table with a  maximally distinct sets of colors (copied
	 * from Fiji's Glasbey LUT).
	 * Reference: 
	 * [1] Glasbey, Chris, Gerie van der Heijden, Vivian FK Toh, and Alision 
	 *     Gray. "Colour displays for categorical images." Color Research &amp; 
	 *     Application 32, no. 4 (2007): 304-309.
	 * 
	 * @return Glasbey lookup table
	 */
	public final static byte[][] createGlasbeyLut() {
		// initial values (copied from Fiji's Glasbey LUT)
		int[] r = { 255, 0, 255, 0, 0, 255, 0, 255, 0, 154, 0, 120, 31, 255, 
				177, 241, 254, 221, 32, 114, 118, 2, 200, 136, 255, 133, 161, 
				20, 0, 220, 147, 0, 0, 57, 238, 0, 171, 161, 164, 255, 71, 212, 
				251, 171, 117, 166, 0, 165, 98, 0, 0, 86, 159, 66, 255, 0, 252, 
				159, 167, 74, 0, 145, 207, 195, 253, 66, 106, 181, 132, 96, 255, 
				102, 254, 228, 17, 210, 91, 32, 180, 226, 0, 93, 166, 97, 98, 
				126, 0, 255, 7, 180, 148, 204, 55, 0, 150, 39, 206, 150, 180, 
				110, 147, 199, 115, 15, 172, 182, 216, 87, 216, 0, 243, 216, 1, 
				52, 255, 87, 198, 255, 123, 120, 162, 105, 198, 121, 0, 231, 217, 
				255, 209, 36, 87, 211, 203, 62, 0, 112, 209, 0, 105, 255, 233, 
				191, 69, 171, 14, 0, 118, 255, 94, 238, 159, 80, 189, 0, 88, 71, 
				1, 99, 2, 139, 171, 141, 85, 150, 0, 255, 222, 107, 30, 173, 
				255, 0, 138, 111, 225, 255, 229, 114, 111, 134, 99, 105, 200, 
				209, 198, 79, 174, 170, 199, 255, 146, 102, 111, 92, 172, 210, 
				199, 255, 250, 49, 254, 254, 68, 201, 199, 68, 147, 22, 8, 116, 
				104, 64, 164, 207, 118, 83, 0, 43, 160, 176, 29, 122, 214, 160, 
				106, 153, 192, 125, 149, 213, 22, 166, 109, 86, 255, 255, 255, 
				202, 67, 234, 191, 38, 85, 121, 254, 139, 141, 0, 63, 255, 17, 
				154, 149, 126, 58, 189 };
		int[] g = { 255, 0, 0, 255, 0, 0, 83, 211, 159, 77, 255, 63, 150, 172, 
				204, 8, 143, 0, 26, 0, 108, 173, 255, 108, 183, 133, 3, 249, 71, 
				94, 212, 76, 66, 167, 112, 0, 245, 146, 255, 206, 0, 173, 118, 
				188, 0, 0, 115, 93, 132, 121, 255, 53, 0, 45, 242, 93, 255, 191, 
				84, 39, 16, 78, 149, 187, 68, 78, 1, 131, 233, 217, 111, 75, 
				100, 3, 199, 129, 118, 59, 84, 8, 1, 132, 250, 123, 0, 190, 60, 
				253, 197, 167, 186, 187, 0, 40, 122, 136, 130, 164, 32, 86, 0, 
				48, 102, 187, 164, 117, 220, 141, 85, 196, 165, 255, 24, 66, 
				154, 95, 241, 95, 172, 100, 133, 255, 82, 26, 238, 207, 128, 
				211, 255, 0, 163, 231, 111, 24, 117, 176, 24, 30, 200, 203, 194, 
				129, 42, 76, 117, 30, 73, 169, 55, 230, 54, 0, 144, 109, 223, 
				80, 93, 48, 206, 83, 0, 42, 83, 255, 152, 138, 69, 109, 0, 76, 
				134, 35, 205, 202, 75, 176, 232, 16, 82, 137, 38, 38, 110, 164, 
				210, 103, 165, 45, 81, 89, 102, 134, 152, 255, 137, 34, 207, 
				185, 148, 34, 81, 141, 54, 162, 232, 152, 172, 75, 84, 45, 60, 
				41, 113, 0, 1, 0, 82, 92, 217, 26, 3, 58, 209, 100, 157, 219, 
				56, 255, 0, 162, 131, 249, 105, 188, 109, 3, 0, 0, 109, 170, 
				165, 44, 185, 182, 236, 165, 254, 60, 17, 221, 26, 66, 157, 
				130, 6, 117};
		int[] b = { 255, 255, 0, 0, 51, 182, 0, 0, 255, 66, 190, 193, 152, 253, 
				113, 92, 66, 255, 1, 85, 149, 36, 0, 0, 159, 103, 0, 255, 158, 
				147, 255, 255, 80, 106, 254, 100, 204, 255, 115, 113, 21, 197, 
				111, 0, 215, 154, 254, 174, 2, 168, 131, 0, 63, 66, 187, 67, 
				124, 186, 19, 108, 166, 109, 0, 255, 64, 32, 0, 84, 147, 0, 211, 
				63, 0, 127, 174, 139, 124, 106, 255, 210, 20, 68, 255, 201, 122, 
				58, 183, 0, 226, 57, 138, 160, 49, 1, 129, 38, 180, 196, 128, 
				180, 185, 61, 255, 253, 100, 250, 254, 113, 34, 103, 105, 182, 
				219, 54, 0, 1, 79, 133, 240, 49, 204, 220, 100, 64, 70, 69, 233, 
				209, 141, 3, 193, 201, 79, 0, 223, 88, 0, 107, 197, 255, 137, 
				46, 145, 194, 61, 25, 127, 200, 217, 138, 33, 148, 128, 126, 96, 
				103, 159, 60, 148, 37, 255, 135, 148, 0, 123, 203, 200, 230, 68, 
				138, 161, 60, 0, 157, 253, 77, 57, 255, 101, 48, 80, 32, 0, 255, 
				86, 77, 166, 101, 175, 172, 78, 184, 255, 159, 178, 98, 147, 30, 
				141, 78, 97, 100, 23, 84, 240, 0, 58, 28, 121, 0, 255, 38, 215, 
				155, 35, 88, 232, 87, 146, 229, 36, 159, 207, 105, 160, 113, 207, 
				89, 34, 223, 204, 69, 97, 78, 81, 248, 73, 35, 18, 173, 0, 51, 
				2, 158, 212, 89, 193, 43, 40, 246, 146, 84, 238, 72, 101, 101 };

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
	
	/**
	 * Make lookup table with esthetically pleasing colors based on the golden
	 * angle
	 * 
	 * @param nColors number of colors to generate
	 * @return lookup table with golden-angled-based colors 
	 */
	public final static byte[][] createGoldenAngleLut( int nColors ) 
	{
		// hue for assigning new color ([0.0-1.0])
		float hue = 0.5f;
		// saturation for assigning new color ([0.5-1.0])
		float saturation = 0.75f;
		
		// create colors recursively by adding golden angle ratio to hue and
		// saturation of previous color
		Color[] colors = new Color[nColors];
		for (int i = 0; i < nColors; i++)
		{
			// create current color
			colors[i] = Color.getHSBColor(hue, saturation, 1);

			// update hue and saturation for next color
			hue += 0.38197f; // golden angle
			if (hue > 1)
				hue -= 1;
			saturation += 0.38197f; // golden angle
			if (saturation > 1)
				saturation -= 1;
			saturation = 0.5f * saturation + 0.5f;
		}

		// create map
		byte[][] map = new byte[nColors][3];

		// fill up the color map by converting color array 
		for (int i = 0; i < nColors; i++)
		{
			Color color = colors[i];
			map[i][0] = (byte) color.getRed();
			map[i][1] = (byte) color.getGreen();
			map[i][2] = (byte) color.getBlue();
		}

		return map;
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

	public final static byte[][] createJetLut(int nColors) {
		byte[][] lut = createJetLut();
		if (nColors != lut.length) 
			lut = interpolateLut(lut, nColors);
		return lut;
	}

	public final static byte[][] createJetLut()	{
		// create map
		byte[][] map = new byte[256][3];
		
		// shade of dark blue to blue
		for (int i = 0; i < 32; i++) 
			map[i][2] = (byte) (127 + i * 4);
		for (int i = 32; i < 96; i++) { 
			map[i][1] = (byte) ((i - 32) * 4);
			map[i][2] = (byte) 255;
		}
		for (int i = 96; i < 160; i++) { 
			map[i][0] = (byte) ((i - 96) * 4);
			map[i][1] = (byte) 255;
			map[i][2] = (byte) (255 - (i - 96) * 4);
		}
		for (int i = 160; i < 224; i++) { 
			map[i][0] = (byte) 255;
			map[i][1] = (byte) (255 - (i - 160) * 4);
			map[i][2] = 0;
		}
		for (int i = 224; i < 256; i++) { 
			map[i][0] = (byte) (255 - (i - 224) * 4);
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
	 * 
	 * @return the created LUT
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
	 * 
	 * @return the created LUT
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
	 * 
	 * @param lut
	 *            the initial values of the look-up table
	 * @param seed
	 *            the value used to initialize the random number generator
	 * @return the randomly shuffled look-up table
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
	 * 
	 * @param array the values to analyze
	 * @return the array of indices corresponding to sorted values
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
