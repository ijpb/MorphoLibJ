/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

/**
 * An enumeration of region attributes for 3D attribute filtering.
 * 
 * @author dlegland
 *
 */
public enum Attribute3D
{
	VOLUME("VOLUME"); 
//	BOX_DIAGONAL("Box Diagonal");
	
	String label;
	
	Attribute3D(String label)
	{
		this.label = label;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public static String[] getAllLabels()
	{
		int n = Attribute3D.values().length;
		String[] result = new String[n];
		
		int i = 0;
		for (Attribute3D att : Attribute3D.values())
			result[i++] = att.label;
		
		return result;
	}
	
	/**
	 * Determines the Attribute3D type from its label.
	 * 
	 * @param attrLabel
	 *            the label of the Attribute3D
	 * @return the parsed Attribute3D
	 * @throws IllegalArgumentException
	 *             if label is not recognized.
	 */
	public static Attribute3D fromLabel(String attrLabel)
	{
		if (attrLabel != null)
			attrLabel = attrLabel.toLowerCase();
		for (Attribute3D op : Attribute3D.values()) 
		{
			String cmp = op.label.toLowerCase();
			if (cmp.equals(attrLabel))
				return op;
		}
		throw new IllegalArgumentException("Unable to parse Attribute3D with label: " + attrLabel);
	}
}
