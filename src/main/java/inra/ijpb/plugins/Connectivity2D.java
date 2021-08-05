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
	C4("4", 4),
	C8("8", 8);

	private final String label;
	private final int value;

	private Connectivity2D(String label, int value)
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
}
