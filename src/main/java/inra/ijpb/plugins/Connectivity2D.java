/**
 * 
 */
package inra.ijpb.plugins;

/**
 * An enumeration of connectivity options for 2D Mathematical Morphology
 * operators.
 * 
 * @author dlegland
 *
 */
public enum Connectivity2D 
{
	/** The 4 connectivity (only orthogonal neighbors)*/
	C4("4", 4),
	/** The 8 connectivity (orthogonal and diagonal neighbors) */
	C8("8", 8);

	private final String label;
	private final int value;

	private Connectivity2D(String label, int value)
	{
		this.label = label;
		this.value = value;
	}

	/**
	 * Returns the integer value associated to this connectivity.
	 * 
	 * @return the integer value associated to this connectivity.
	 */
	public int getValue()
	{
		return this.value;
	}

	/**
	 * Returns all the labels for this enumeration.
	 * 
	 * @return all the labels for this enumeration.
	 */
	public static String[] getAllLabels()
	{
		int n = Connectivity2D.values().length;
		String[] result = new String[n];

		int i = 0;
		for (Connectivity2D conn : Connectivity2D.values())
			result[i++] = conn.label;

		return result;
	}

	/**
	 * Determines the connectivity type from its label.
	 * 
	 * @param label
	 *            the name of the connectivity
	 * @return the connectivity associated to the label
	 * @throws IllegalArgumentException
	 *             if label is not recognized.
	 */
	public static Connectivity2D fromLabel(String label)
	{
		for (Connectivity2D conn : Connectivity2D.values())
		{
			if (conn.label.equalsIgnoreCase(label))
				return conn;
		}
		throw new IllegalArgumentException(
				"Unable to parse Connectivity2D with label: " + label);
	}

	public String toString()
	{
		return this.label;
	}
}
