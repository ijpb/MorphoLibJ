/**
 * 
 */
package inra.ijpb.morphology.directional;

import java.util.Arrays;

import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.data.border.BorderManager;
import inra.ijpb.data.border.MirroringBorder;
import inra.ijpb.morphology.Strel;

/**
 * @author David Legland
 *
 */
public class DirectionalFilter extends AlgoStub 
{
	// =======================================================================
	// Public enumerations
	
	/**
	 * Specifies how to combine the different orientations: min or max.
	 */
	public enum Type 
	{
		MIN("Min"),
		MAX("Max");
		
		private final String label;
		
		private Type(String label) 
		{
			this.label = label;
		}
		
//		public MorphologicalDirectionalFilter createFilter(OrientedStrelFactory factory, 
//				int nTheta,	Filters.Operation operation)
//		{
//			if (this == MIN)
//				return new MinDirectionalFilter(factory, nTheta, operation);
//			if (this == MAX)
//				return new MaxDirectionalFilter(factory, nTheta, operation);
//			
//			throw new RuntimeException(
//					"Unable to process the " + this + " operation");
//		}
		
		public String toString()
		{
			return this.label;
		}
		
		public static String[] getAllLabels()
		{
			int n = Type.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Type op : Type.values())
				result[i++] = op.label;
			
			return result;
		}
		
		/**
		 * Determines the operation type from its label.
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Type fromLabel(String opLabel)
		{
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Type op : Type.values())
			{
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException("Unable to parse Operation with label: " + opLabel);
		}
	};
	
	/**
	 * A pre-defined set of operations.
	 */
	public enum Operation
	{
		EROSION("Erosion"), 
		DILATION("Dilation"), 
		OPENING("Opening"), 
		CLOSING("Closing"), 
		MEAN("Mean"), 
		MEDIAN("Median");

		private final String label;

		private Operation(String label)
		{
			this.label = label;
		}

		public ImageProcessor apply(ImageProcessor image, Strel strel)
		{
			if (this == DILATION)
				return strel.dilation(image);
			if (this == EROSION)
				return strel.erosion(image);
			if (this == CLOSING)
				return strel.closing(image);
			if (this == OPENING)
				return strel.opening(image);
			if (this == MEAN)
				return mean(image, strel);
			if (this == MEDIAN)
				return median(image, strel);

			throw new RuntimeException("Unable to process the " + this
					+ " filter operation");
		}

		public String toString()
		{
			return this.label;
		}

		public static String[] getAllLabels()
		{
			int n = Operation.values().length;
			String[] result = new String[n];

			int i = 0;
			for (Operation op : Operation.values())
				result[i++] = op.label;

			return result;
		}

		/**
		 * Determines the operation type from its label.
		 * 
		 * @throws IllegalArgumentException
		 *             if label is not recognized.
		 */
		public static Operation fromLabel(String opLabel)
		{
			if (opLabel != null)
				opLabel = opLabel.toLowerCase();
			for (Operation op : Operation.values())
			{
				String cmp = op.label.toLowerCase();
				if (cmp.equals(opLabel))
					return op;
			}
			throw new IllegalArgumentException(
					"Unable to parse Operation with label: " + opLabel);
		}
	};
	
	
	// =======================================================================
	// class variables
	
	/**
	 * The type of combination of results on various orientations. 
	 */
	Type type = Type.MAX;
	
	/**
	 * The operation to apply on each orientation. Default is opening (the most
	 * commonly exemplified)
	 */
	Operation operation = Operation.OPENING;
	
	/**
	 * The factory of oriented structuring elements
	 */
	OrientedStrelFactory strelFactory;
	
	/**
	 * The number of distinct orientations, between 0 and 180.
	 * Examples:
	 * <ul>
	 * <li> nTheta = 2: considers 0 and 90� only </li>
	 * <li> nTheta = 4: considers 0, 45, 90 and 135 degrees</li>
	 * <li> nTheta = 180: considers one oriented line strel for each degree</li>
	 * </ul>
	 * 
	 * Default is 32.
	 */
	int nDirections;

	
	// =======================================================================
	// Constructors

	/**
	 * Creates new instance of directional filter using line structuring
	 * elements.
	 * 
	 * @param type
	 *            the type of combination (min or max)
	 * @param op
	 *            the operation to apply with each structuring element
	 * @param lineLength
	 *            the (approximated) length of each line structuring element (in
	 *            pixels)
	 * @param nTheta
	 *            the number of directions to consider
	 */
	public DirectionalFilter(Type type, Operation op, int lineLength, int nTheta)
	{
		this.type = type;
		this.operation = op;
		this.strelFactory = new OrientedLineStrelFactory(lineLength);
		this.nDirections = nTheta;
	}

	/**
	 * Creates new instance of directional filter using line structuring
	 * elements.
	 * 
	 * @param type
	 *            the type of combination (min or max)
	 * @param op
	 *            the operation to apply with each structuring element
	 * @param factory
	 *            the factory for creating structuring elements of various
	 *            orientations
	 * @param nTheta
	 *            the number of directions to consider
	 */
	public DirectionalFilter(Type type, Operation op, OrientedStrelFactory factory, int nTheta)
	{
		this.type = type;
		this.operation = op;
		this.strelFactory = factory;
		this.nDirections = nTheta;
	}
	
	
	// =======================================================================
	// Methods

	/**
	 * Apply directional filter with current settings to the specified image.
	 * 
	 * @param image
	 *            a grayscale image
	 * @return the result of directional filter
	 */
	public ImageProcessor applyTo(ImageProcessor image)
	{
		// determine the sign of min/max computation
		int sign = this.type == Type.MAX ? 1 : -1;
		
		// initialize result
		ImageProcessor result = image.duplicate();
		if (this.type == Type.MAX)
		{
			result.setValue(0);
		}
		else
		{
			result.setValue(Integer.MAX_VALUE);
		}
		result.fill();
		
		int sizeX = image.getWidth();
		int sizeY = image.getHeight();
		
		// Iterate over the set of directions
		for (int i = 0; i < nDirections; i++)
		{
			fireProgressChanged(this, i, nDirections);
			
			// Create the structuring element for current orientation
			double theta = ((double) i) * 180.0 / nDirections;
			Strel strel = this.strelFactory.createStrel(theta);

			// Apply oriented filter
			ImageProcessor oriented = this.operation.apply(image, strel);

			// combine current result with global result
			for (int y = 0; y < sizeY; y++)
			{
				for (int x = 0; x < sizeX; x++)
				{
					int value = oriented.get(x, y);
					if (value * sign > result.get(x, y) * sign)
					{
						result.set(x, y, value);
					}
				}
			}
		}
		
		fireProgressChanged(this, 1, 1);
		
		// return the min or max value computed over all orientations
		return result;
	}
	
	
	// =======================================================================
	// Utility Methods

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
	public static ImageProcessor median(ImageProcessor image, Strel strel)
	{
		// Allocate memory for result
		ImageProcessor result = image.duplicate();

		BorderManager bm = new MirroringBorder(image);

		int[][] shifts = strel.getShifts();
		int n = shifts.length;
		double[] buffer = new double[n];

		// Iterate on image pixels
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				// iterate on neighbors
				for (int i = 0; i < shifts.length; i++)
				{
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
	private static double median(double[] values)
	{
		Arrays.sort(values);
		return medianSorted(values);
	}

	/**
	 * Computes the median value in a sorted array.
	 */
	private static double medianSorted(double[] values)
	{
		// index of middle element
		int middle = values.length / 2;

		// Different choice depending on even-odd number
		if (values.length % 2 == 1)
		{
			// Odd number of elements: return the middle one.
			return values[middle];
		} else
		{
			// Even number: return average of middle two
			return (values[middle - 1] + values[middle]) / 2.0;
		}
	}
}
