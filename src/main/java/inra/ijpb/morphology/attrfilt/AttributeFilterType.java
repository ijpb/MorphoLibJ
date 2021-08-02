/**
 * 
 */
package inra.ijpb.morphology.attrfilt;

/**
 * @author dlegland
 *
 */
public enum AttributeFilterType
{
	CLOSING("Closing"), 
	OPENING("Opening"),
	TOP_HAT("Top Hat"),
	BOTTOM_HAT("Bottom Hat");
	
	String label;
	
	AttributeFilterType(String label)
	{
		this.label = label;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public static String[] getAllLabels()
	{
		int n = AttributeFilterType.values().length;
		String[] result = new String[n];
		
		int i = 0;
		for (AttributeFilterType op : AttributeFilterType.values())
			result[i++] = op.label;
		
		return result;
	}
	
	/**
	 * Determines the AttributeFilterType type from its label.
	 * 
	 * @param opLabel
	 *            the label of the AttributeFilterType
	 * @return the parsed AttributeFilterType
	 * @throws IllegalArgumentException
	 *             if label is not recognized.
	 */
	public static AttributeFilterType fromLabel(String opLabel)
	{
		if (opLabel != null)
			opLabel = opLabel.toLowerCase();
		for (AttributeFilterType op : AttributeFilterType.values()) 
		{
			String cmp = op.label.toLowerCase();
			if (cmp.equals(opLabel))
				return op;
		}
		throw new IllegalArgumentException("Unable to parse AttributeFilterType with label: " + opLabel);
	}
}
