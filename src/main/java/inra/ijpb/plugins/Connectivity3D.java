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
	C6("6", 6),
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

	public int getValue()
	{
		return this.value;
	}

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
