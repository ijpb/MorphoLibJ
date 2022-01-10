/**
 * 
 */
package inra.ijpb.plugins;

/**
 * An enumeration of connectivity options for 3D Mathematical Morphology
 * operators.
 * 
 * @author dlegland
 *
 */
public enum Connectivity3D 
{
	/** The 6-connectivity, that considers orthogonal neighbors in three dimensions*/
	C6("6", 6),
	/** The 26-connectivity, that considers all neighbors in three dimensions*/
	C26("26", 26);

	private final String label;
	private final int value;

	private Connectivity3D(String label, int value)
	{
		this.label = label;
		this.value = value;
	}

	public String toString()
	{
		return this.label;
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
		int n = Connectivity3D.values().length;
		String[] result = new String[n];

		int i = 0;
		for (Connectivity3D conn : Connectivity3D.values())
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
	public static Connectivity3D fromLabel(String label)
	{
		for (Connectivity3D conn : Connectivity3D.values())
		{
			if (conn.label.equalsIgnoreCase(label))
				return conn;
		}
		throw new IllegalArgumentException(
				"Unable to parse Connectivity3D with label: " + label);
	}
}
