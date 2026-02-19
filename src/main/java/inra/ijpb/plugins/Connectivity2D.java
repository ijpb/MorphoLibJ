/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
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
