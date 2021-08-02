/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

/**
 * An enumeration of region attributes for 2D attribute filtering.
 * 
 * @author dlegland
 *
 */
public enum Attribute2D
{
	AREA("Area"), 
	BOX_DIAGONAL("Box Diagonal");
	
	String label;
	
	Attribute2D(String label)
	{
		this.label = label;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public static String[] getAllLabels()
	{
		int n = Attribute2D.values().length;
		String[] result = new String[n];
		
		int i = 0;
		for (Attribute2D att : Attribute2D.values())
			result[i++] = att.label;
		
		return result;
	}
	
	/**
	 * Determines the Attribute2D type from its label.
	 * 
	 * @param attrLabel
	 *            the label of the Attribute2D
	 * @return the parsed Attribute2D
	 * @throws IllegalArgumentException
	 *             if label is not recognized.
	 */
	public static Attribute2D fromLabel(String attrLabel)
	{
		if (attrLabel != null)
			attrLabel = attrLabel.toLowerCase();
		for (Attribute2D op : Attribute2D.values()) 
		{
			String cmp = op.label.toLowerCase();
			if (cmp.equals(attrLabel))
				return op;
		}
		throw new IllegalArgumentException("Unable to parse Attribute2D with label: " + attrLabel);
	}
}
