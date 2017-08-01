/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
package inra.ijpb.morphology.directional;

import java.util.Arrays;

import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.data.border.BorderManager;
import inra.ijpb.data.border.MirroringBorder;
import inra.ijpb.morphology.Strel;

/**
 * <p>
 * Directional filtering of planar images.
 * </p>
 * 
 * <p>
 * Oriented structuring elements are considered, for example linear structuring
 * elements. A collection of orientation is chosen, and the result of a
 * filtering operation is performed for each orientation of the structuring
 * element. The results are combined to create the resulting image.
 * </p>
 * 
 * @author David Legland
 *
 */
public class DirectionalFilter extends AlgoStub 
{
	// =======================================================================
	// Public enumerations
	
	/**
	 * Specifies how to combine the different orientations: min or max.
	 * 
	 * Use MIN to enhance dark thin of the results, and MAX to enhance bright
	 * thin structures.
	 */
	public enum Type 
	{
		/** Keep the minimum value over all possible orientations */
		MIN("Min"),
		/** Keep the maximum value over all possible orientations */
		MAX("Max");
		
		private final String label;
		
		private Type(String label) 
		{
			this.label = label;
		}
		
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
		 * @param typeLabel label name
		 * @return operation type
		 * @throws IllegalArgumentException if label is not recognized.
		 */
		public static Type fromLabel(String typeLabel)
		{
			if (typeLabel != null)
				typeLabel = typeLabel.toLowerCase();
			for (Type type : Type.values())
			{
				String cmp = type.label.toLowerCase();
				if (cmp.equals(typeLabel))
					return type;
			}
			throw new IllegalArgumentException("Unable to parse Operation with label: " + typeLabel);
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
		 * @param opLabel operation label
		 * @return operation
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
	 * <li> nTheta = 2: considers 0 and 90 degrees only</li>
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
	 * Creates new instance of directional filter for arbitrary structuring
	 * elements, by specifying the factory.
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
	public ImageProcessor process(ImageProcessor image)
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
		
		fireStatusChanged(this, "Directional Filter...");

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
					float value = oriented.getf(x, y);
					if (value * sign > result.getf(x, y) * sign)
					{
						result.setf(x, y, value);
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
	 * @param image input image
	 * @param strel structuring element
	 * @return result image
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
					accum += bm.getf(x + shifts[i][0], y + shifts[i][1]);
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
	 * @param image input image
	 * @param strel structuring element
	 * @return result image
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
					buffer[i] = bm.getf(x + shifts[i][0], y + shifts[i][1]);
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
	 * @param values array of values
	 * @return median value
	 */
	private static double median(double[] values)
	{
		Arrays.sort(values);
		return medianSorted(values);
	}

	/**
	 * Computes the median value in a sorted array.
	 * @param values sorted array of values
	 * @return median value
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
		}
		else
		{
			// Even number: return average of middle two
			return (values[middle - 1] + values[middle]) / 2.0;
		}
	}
}
