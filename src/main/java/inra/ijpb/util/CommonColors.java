/**
 * 
 */
package inra.ijpb.util;

import java.awt.Color;

/**
 * <p>
 * A collection of common colors used for creating overlays, choosing background
 * color...
 * </p>
 * 
 * Example:
 * <pre><code>
 * // Use a generic dialog to choose a color 
 * GenericDialog gd = new GenericDialog();
 * gd.addChoice("Color", CommonColors.getAllLabels());
 * gd.showDialog();
 * // extract the color from the name
 * String colorName = gd.getNextChoice();
 * Color color = CommonColors.fromLabel(colorName).getColor();
 * </code></pre>
 * @author dlegland
 *
 */
public enum CommonColors
{
	WHITE("White", 		Color.WHITE), 
	BLACK("Black", 		Color.BLACK), 
	RED("Red", 			Color.RED), 
	GREEN("Green", 		Color.GREEN), 
	BLUE("Blue", 		Color.BLUE), 
	CYAN("Cyan", 		Color.CYAN), 
	MAGENTA("Magenta", 	Color.MAGENTA), 
	YELLOW("Yellow", 	Color.YELLOW), 
	GRAY("Gray", 		Color.GRAY), 
	DARK_GRAY("Dark Gray", 	 Color.DARK_GRAY), 
	LIGHT_GRAY("Light Gray", Color.LIGHT_GRAY);

	/** The name of the color, as displayed in a widget */
	private final String label;
	
	/** The java color corresponding to this color */
	private final Color color;

	/**
	 * Constructor of the common color enumeration item.
	 * @param label the name of the item, for string representation
	 * @param color the java color associated to the enumeration item
	 */
	CommonColors(String label, Color color) 
	{
		this.label = label;
		this.color = color;
	}

	/**
	 * @return the label associated to this enumeration item
	 */
	public String getLabel() 
	{
		return label;
	}

	/**
	 * @return the java color corresponding to this enumeration item
	 */
	public Color getColor() 
	{
		return color;
	}
	
	/**
	 * @return a string representation of this enumeration item
	 */
	public String toString() 
	{
		return label;
	}
	
	public static String[] getAllLabels()
	{
		int n = CommonColors.values().length;
		String[] result = new String[n];
		
		int i = 0;
		for (CommonColors color : CommonColors.values())
			result[i++] = color.label;
		
		return result;
	}
	
	/**
	 * Determines the operation type from its label.
	 * 
	 * @param label
	 *            the name of the color
	 * @return the Colors enumeration corresponding to the name
	 * @throws IllegalArgumentException
	 *             if color name is not recognized.
	 */
	public static CommonColors fromLabel(String label) 
	{
		if (label != null)
			label = label.toLowerCase();
		for (CommonColors color : CommonColors.values()) 
		{
			String cmp = color.label.toLowerCase();
			if (cmp.equals(label))
				return color;
		}
		throw new IllegalArgumentException("Unable to parse Color with label: " + label);
	}

}
