/**
 * 
 */
package inra.ijpb.morphology.directional;

import ij.process.ImageProcessor;
import inra.ijpb.data.border.BorderManager;
import inra.ijpb.data.border.MirroringBorder;
import inra.ijpb.morphology.Strel;

import java.util.Arrays;

/**
 * A set of generic filters (mean, median), implemented in a static way.
 * 
 * @author David Legland
 *
 */
public class Filters {

	// =======================================================================
	// Enumeration for operations
	
	/**
	 * A pre-defined set of operations.
	 */
	public enum Operation {
		EROSION("Erosion"),
		DILATION("Dilation"),
		OPENING("Opening"),
		CLOSING("Closing"), 
		MEAN("Mean"), 
		MEDIAN("Median");
		
		private final String label;
		
		private Operation(String label) {
			this.label = label;
		}
		
		public ImageProcessor apply(ImageProcessor image, Strel strel) {
			if (this == DILATION)
				return Filters.dilation(image, strel);
			if (this == EROSION)
				return Filters.erosion(image, strel);
			if (this == CLOSING)
				return closing(image, strel);
			if (this == OPENING)
				return opening(image, strel);
			if (this == MEAN)
				return mean(image, strel);
			if (this == MEDIAN)
				return median(image, strel);
			
			throw new RuntimeException(
					"Unable to process the " + this + " filter operation");
		}
		
		public String toString() {
			return this.label;
		}
		
		public static String[] getAllLabels(){
			int n = Operation.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Operation op : Operation.values())
				result[i++] = op.label;
			
			return result;
		}
		
		/**
		 * Determines the operation type from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Operation fromLabel(String opLabel) {
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Operation op : Operation.values()) {
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
		}
	};
	

	/**
	 * Computes the minimum value among the neighbors.
	 */
	public static ImageProcessor erosion(ImageProcessor image, Strel strel) {
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		BorderManager bm = new MirroringBorder(image);
		
		int[][] shifts = strel.getShifts();
		double res;
		
		// Iterate on image pixels
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				// reset accumulator
				res = Double.MAX_VALUE;
				
				// iterate on neighbors
				for (int i = 0; i < shifts.length; i++) {
					double value = bm.get(x + shifts[i][0], y + shifts[i][1]);
					res = Math.min(res, value);
				}
				
				// compute result
				result.setf(x, y, (float) res);
			}			
		}
		
		return result;
	}

	/**
	 * Computes the minimum value among the neighbors.
	 */
	public static ImageProcessor dilation(ImageProcessor image, Strel strel) {
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		BorderManager bm = new MirroringBorder(image);
		
		int[][] shifts = strel.getShifts();
		double res;
		
		// Iterate on image pixels
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				// reset accumulator
				res = Double.MIN_VALUE;
				
				// iterate on neighbors
				for (int i = 0; i < shifts.length; i++) {
					double value = bm.get(x + shifts[i][0], y + shifts[i][1]);
					res = Math.max(res, value);
				}
				
				// compute result
				result.setf(x, y, (float) res);
			}			
		}
		
		return result;
	}

	public static ImageProcessor closing(ImageProcessor image, Strel strel) {
		return erosion(dilation(image, strel), strel);
	}

	public static ImageProcessor opening(ImageProcessor image, Strel strel) {
		return dilation(erosion(image, strel), strel);
	}

	/**
	 * Computes the average value among the neighbors.
	 */
	public static ImageProcessor mean(ImageProcessor image, Strel strel) {
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		BorderManager bm = new MirroringBorder(image);
		
		int[][] shifts = strel.getShifts();
		double accum;
		
		// Iterate on image pixels
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				// reset accumulator
				accum = 0;
				
				// iterate on neighbors
				for (int i = 0; i < shifts.length; i++) {
					accum += bm.get(x + shifts[i][0], y + shifts[i][1]);
				}
				
				// compute result
				double res = accum / shifts.length;
				result.setf(x, y, (float) res);
			}			
		}
		
		return result;
	}


	/**
	 * Computes the median value among the neighbors.
	 */
	public static ImageProcessor median(ImageProcessor image, Strel strel) {
		// Allocate memory for result
		ImageProcessor result = image.duplicate();
		
		BorderManager bm = new MirroringBorder(image);
		
		int[][] shifts = strel.getShifts();
		int n = shifts.length;
		double[] buffer = new double[n];
		
		// Iterate on image pixels
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				// iterate on neighbors
				for (int i = 0; i < shifts.length; i++) {
					buffer[i] = bm.get(x + shifts[i][0], y + shifts[i][1]);
				}
				
				// compute result
				double res = median(buffer);
				result.setf(x, y, (float) res);
			}			
		}
		
		return result;
	}

	/**
	 * Sorts the array, and returns its median value.
	 */
	private static double median(double[] values) {
		Arrays.sort(values);
		return medianSorted(values);
	}

	/**
	 * Computes the median value in a sorted array.
	 */
	private static double medianSorted(double[] values) {
		// index of middle element
		int middle = values.length / 2;
		
		// Different choice depending on even-odd number
		if (values.length % 2 == 1) {
			// Odd number of elements: return the middle one.
			return values[middle];
		} else {
			// Even number: return average of middle two
			return (values[middle - 1] + values[middle]) / 2.0;
		}
	}
}
