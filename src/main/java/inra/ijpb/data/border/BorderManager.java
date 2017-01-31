/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
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
package inra.ijpb.data.border;

import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/**
 * Manages borders of an image, by providing methods for accessing values also
 * for position out of image bounds.
 * 
 * <pre><code>
 * ImageProcessor image = ...
 * BorderManager bm = new ReplicatedBorder(image);
 * int value = bm.get(-5, -10);
 * </code></pre>
 * @author David Legland
 *
 */
public interface BorderManager {

	/**
	 * A set of pre-defined border managers stored in an enumeration.
	 * Each type is associated with a label, for better user-interface
	 * integration. The set of all labels can be obtained via static method
	 * <code>getAllLabels()</code>, and can be used as input of list dialogs.
	 * To get the type corresponding to a given label, use the twin method
	 * <code>fromLabel(String)</code>.
	 * 
	 * <pre><code>
	 * // init initial values 
	 * ImageProcessor image = ...
	 * String borderManagerName = "Periodic";
	 * 
	 * // create border manager from name and image
	 * BorderManager.Type bmType = BorderManager.Type.fromLabel(borderManagerName);
	 * BorderManager bm = bmType.createBorderManager(image);
	 * int value = bm.get(-5, -10);
	 * </code></pre>
	 * @author David Legland
	 *
	 */
	public enum Type {
		REPLICATED("Replicate"), 
		PERIODIC("Periodic"), 
		MIRRORED("Mirrored"), 
		BLACK("Black"), 
		WHITE("White"), 
		GRAY("Gray");
		
		private Type(String label) {
			this.label = label;
		}
		
		String label;
		
		public String toString() {
			return this.label;
		}
		
		public String getLabel() {
			return this.label;
		}
		
		public BorderManager createBorderManager(ImageProcessor image) {
			switch((Type) this) {
			case REPLICATED:
				return new ReplicatedBorder(image);
			case PERIODIC:
				return new PeriodicBorder(image);
			case MIRRORED:
				return new MirroringBorder(image);
			case BLACK:
				return new ConstantBorder(image, 0);
			case WHITE:
				return new ConstantBorder(image, 0xFFFFFF);
			case GRAY:
				if (image instanceof ColorProcessor)
					return new ConstantBorder(image, 0x7F7F7F);
				if (image instanceof ShortProcessor)
					return new ConstantBorder(image, 0x007FFF);
				return new ConstantBorder(image, 127);
			default:
				throw new RuntimeException("Unknown border manager for type "  + this);
			}
		}
	
		public static String[] getAllLabels(){
			int n = Type.values().length;
			String[] result = new String[n];
			
			int i = 0;
			for (Type value : Type.values())
				result[i++] = value.label;
			
			return result;
		}
		
		/**
		 * Determines the operation type from its label.
		 * 
		 * @param label
		 *            the name of the border manager
		 * @return the enumeration item corresponding to the name
		 * @throws IllegalArgumentException
		 *             if label is not recognized.
		 */
		public static Type fromLabel(String label) {
			if (label != null)
				label = label.toLowerCase();
			for (Type value : Type.values()) {
				String cmp = value.label.toLowerCase();
				if (cmp.equals(label))
					return value;
			}
			throw new IllegalArgumentException("Unable to parse Value with label: " + label);
		}
	}

	/**
	 * Returns the value corresponding to (x,y) position. Position can be
	 * outside original image bounds.
	 *   
	 * @param x column index of the position
	 * @param y row index of the position
	 * @return border corrected value
	 */
	public int get(int x, int y);
	
	/**
	 * Returns the floating-point value corresponding to (x,y) position.
	 * Position can be outside original image bounds.
	 * 
	 * @param x
	 *            column index of the position
	 * @param y
	 *            row index of the position
	 * @return border corrected value
	 */
	public float getf(int x, int y); 
}
