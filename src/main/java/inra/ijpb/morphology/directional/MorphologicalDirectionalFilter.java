/**
 * 
 */
package inra.ijpb.morphology.directional;

import ij.process.ImageProcessor;

/**
 * @author David Legland
 *
 */
public interface MorphologicalDirectionalFilter 
{
	/**
	 * A pre-defined set of operations.
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
		
		public MorphologicalDirectionalFilter createFilter(OrientedStrelFactory factory, 
				int nTheta,	Filters.Operation operation)
		{
			if (this == MIN)
				return new MinDirectionalFilter(factory, nTheta, operation);
			if (this == MAX)
				return new MaxDirectionalFilter(factory, nTheta, operation);
			
			throw new RuntimeException(
					"Unable to process the " + this + " operation");
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
	
	public ImageProcessor applyTo(ImageProcessor image);
}
