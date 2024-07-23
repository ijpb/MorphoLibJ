/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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
package inra.ijpb.color;

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
	/** The white color (255,255,255). */
	WHITE("White", 		Color.WHITE), 
	/** The black color (0,0,0).*/
	BLACK("Black", 		Color.BLACK), 
	/** The red color */
	RED("Red", 			Color.RED), 
	/** The green color */
	GREEN("Green", 		Color.GREEN), 
	/** The blue color */
	BLUE("Blue", 		Color.BLUE), 
	/** The cyan (light blue) color */
	CYAN("Cyan", 		Color.CYAN), 
	/** The magenta color */
	MAGENTA("Magenta", 	Color.MAGENTA), 
	/** The yellow color */
	YELLOW("Yellow", 	Color.YELLOW), 
	/** The White color */
	GRAY("Gray", 		Color.GRAY), 
	/** The dark gray color */
	DARK_GRAY("Dark Gray", 	 Color.DARK_GRAY), 
	/** The light gray color */
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
	
	/**
	 * @return the array of labels for the colors within this enumeration
	 */
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
