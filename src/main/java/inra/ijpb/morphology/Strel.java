/**
 * 
 */
package inra.ijpb.morphology;

import ij.process.ImageProcessor;
import inra.ijpb.morphology.strel.Cross3x3Strel;
import inra.ijpb.morphology.strel.DiamondStrel;
import inra.ijpb.morphology.strel.DiskStrel;
import inra.ijpb.morphology.strel.LinearDiagDownStrel;
import inra.ijpb.morphology.strel.LinearDiagUpStrel;
import inra.ijpb.morphology.strel.LinearHorizontalStrel;
import inra.ijpb.morphology.strel.LinearVerticalStrel;
import inra.ijpb.morphology.strel.OctagonStrel;
import inra.ijpb.morphology.strel.SquareStrel;

/**
 * Interface for planar structuring elements.
 * 
 * @author David Legland
 *
 */
public interface Strel extends Strel3D {

	/**
	 * Default value for background pixels.
	 */
	public final static int BACKGROUND = Strel3D.BACKGROUND;

	/**
	 * Default value for foreground pixels.
	 */
	public final static int FOREGROUND = Strel3D.FOREGROUND;


	/**
	 * An enumeration of the different possible structuring element shapes. 
	 * Each item of the enumeration can create Strel instances of specific
	 * class and of given size.
	 */
	public enum Shape {
		/**
		 * Disk of a given radius
		 * @see DiskStrel 
		 */
		DISK("Disk"),
		
		/** 
		 * Square of a given side
		 * @see SquareStrel 
		 */
		SQUARE("Square"),
		
		/** 
		 * Diamond of a given diameter
		 * @see DiamondStrel
		 * @see Cross3x3Strel 
		 */
		DIAMOND("Diamond"),
		
		/** 
		 * Octagon of a given diameter
		 * @see OctagonStrel
		 */
		OCTAGON("Octagon"),
		
		/**
		 * Horizontal line of a given length 
		 * @see LinearHorizontalStrel
		 */
		LINE_HORIZ("Horizontal Line"),
		
		/** 
		 * Vertical line of a given length 
		 * @see LinearVerticalStrel
		 */
		LINE_VERT("Vertical Line"),
		
		/**
		 * Diagonal line of a given length 
		 * @see LinearDiagUpStrel
		 */
		LINE_DIAG_UP("Line 45 degrees"),
		
		/** 
		 * Diagonal line of a given length 
		 * @see LinearDiagDownStrel
		 */
		LINE_DIAG_DOWN("Line 135 degrees");
		
		private final String label;
		
		private Shape(String label) {
			this.label = label;
		}
		
		/**
		 * Returns the label associated to this shape.
		 */
		public String toString() {
			return this.label;
		}
		
		/**
		 * Creates a structuring element of the given type and with the
		 * specified radius. The final size is given by 2 * radius + 1, to
		 * take into account the central pixel.
		 * 
		 * @param radius the radius of the structuring element, in pixels
		 * @return a new structuring element
		 * 
		 */
		public Strel fromRadius(int radius) {
			if (this == DISK) 
				return DiskStrel.fromRadius(radius);
			return fromDiameter(2 * radius + 1);
		}
		
		/**
		 * Creates a structuring element of the given type and with the
		 * specified diameter.
		 * @param diam the orthogonal diameter of the structuring element (max of x and y sizes), in pixels
		 * @return a new structuring element
		 */
		public Strel fromDiameter(int diam) {
			if (this == DISK) 
				return DiskStrel.fromRadius((diam - 1) / 2);
			if (this == SQUARE) 
				return new SquareStrel(diam);
			if (this == DIAMOND) {
				if (diam == 3)
					return new Cross3x3Strel();
				return new DiamondStrel(diam);
			}
			if (this == OCTAGON) 
				return new OctagonStrel(diam);
			if (this == LINE_HORIZ) 
				return new LinearHorizontalStrel(diam);
			if (this == LINE_VERT) 
				return new LinearVerticalStrel(diam);
			if (this == LINE_DIAG_UP) 
				return new LinearDiagUpStrel(diam);
			if (this == LINE_DIAG_DOWN) 
				return new LinearDiagDownStrel(diam);
			
			throw new IllegalArgumentException("No default method for creating element of type " + this.label);
		}
		
		/**
		 * Returns a set of labels for most of classical structuring elements.
		 * @return a list of labels
		 */
		public static String[] getAllLabels(){
			// array of all Strel types
			Shape[] values = Shape.values();
			int n = values.length;
			
			// keep all values but the last one ("Custom")
			String[] result = new String[n];
			for (int i = 0; i < n; i++)
				result[i] = values[i].label;
			
			return result;
		}
		
		/**
		 * Determines the strel shape from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Shape fromLabel(String label) {
			if (label != null)
				label = label.toLowerCase();
			for (Shape type : Shape.values()) {
				if (type.label.toLowerCase().equals(label))
					return type;
			}
			throw new IllegalArgumentException("Unable to parse Strel.Shape with label: " + label);
		}
	}
	
	/**
	 * Returns the size of the structuring element, as an array of size in each
	 * direction. The first index corresponds to the number of pixels in the x
	 * direction.
	 * 
	 * @return the size of the structuring element
	 */
	public int[] getSize();

	/**
	 * Returns the structuring element as a mask. Each value is either 0 or 255.
	 * The first index corresponds to the y position, and the second index to
	 * the x direction.
	 * 
	 * @return the mask of the structuring element
	 */
	public int[][] getMask();

	/**
	 * Returns the offset in the mask. The first value corresponds to the shift
	 * in the x direction.
	 * 
	 * @return the offset in the mask
	 */
	public int[] getOffset();

	/**
	 * Returns the structuring element as a set of shifts. The size of the
	 * result is N-by-2, where N is the number of elements of the structuring
	 * element. The first value corresponds to the shift in the x direction.
	 * 
	 * @return a set of shifts
	 */
	public int[][] getShifts();

	/**
	 * Returns a reversed (i.e. symmetric wrt the origin) version of this
	 * structuring element. Implementations can return more specialized type
	 * depending on the implemented interfaces.
	 * 
	 * @return the reversed structuring element
	 */
	public Strel reverse();

	/**
	 * Performs a morphological dilation of the input image with this
	 * structuring element, and returns the result in a new ImageProcessor.
	 * 
	 * @param image
	 *            the input image
	 * @return the result of dilation with this structuring element
	 * @see #erosion(ij.process.ImageProcessor)
	 * @see #closing(ij.process.ImageProcessor)
	 * @see #opening(ij.process.ImageProcessor)
	 */
	public ImageProcessor dilation(ImageProcessor image);

	/**
	 * Performs an morphological erosion of the input image with this
	 * structuring element, and returns the result in a new ImageProcessor.
	 * 
	 * @param image
	 *            the input image
	 * @return the result of erosion with this structuring element
	 * @see #dilation(ij.process.ImageProcessor)
	 * @see #closing(ij.process.ImageProcessor)
	 * @see #opening(ij.process.ImageProcessor)
	 */
	public ImageProcessor erosion(ImageProcessor image);
	
	/**
	 * Performs a morphological closing of the input image with this structuring
	 * element, and returns the result in a new ImageProcessor.
	 *  
	 * The closing is equivalent in performing a dilation followed by an
	 * erosion with the reversed structuring element.
	 * 
	 * @param image
	 *            the input image
	 * @return the result of closing with this structuring element
	 * @see #dilation(ij.process.ImageProcessor)
	 * @see #erosion(ij.process.ImageProcessor)
	 * @see #opening(ij.process.ImageProcessor)
	 * @see #reverse()
	 */
	public ImageProcessor closing(ImageProcessor image);

	/**
	 * Performs a morphological opening of the input image with this structuring
	 * element, and returns the result in a new ImageProcessor.
	 * 
	 * The opening is equivalent in performing an erosion followed by a
	 * dilation with the reversed structuring element.
	 * 
	 * @param image
	 *            the input image
	 * @return the result of opening with this structuring element
	 * @see #dilation(ij.process.ImageProcessor)
	 * @see #erosion(ij.process.ImageProcessor)
	 * @see #closing(ij.process.ImageProcessor)
	 * @see #reverse()
	 */
	public ImageProcessor opening(ImageProcessor image);
	
}
