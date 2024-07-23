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
/**
 * Several classes for managing progress of algorithms. 
 * 
 * Sample code usage:
 * <pre><code>
 * 	// create ImageJ instance and open an image
 * 	new ImageJ();
 * 	ImagePlus imagePlus = IJ.openImage("http://imagej.nih.gov/ij/images/NileBend.jpg");
 * 	imagePlus.show("Input");
 * 	
 * 	// Create an operator (here a structuring element operation)
 * 	Strel strel = SquareStrel.fromDiameter(21);
 * 	
 * 	// uses default listener. This will display progress in ImageJ progress bar
 * 	// and display status in ImageJ status bar
 * 	DefaultAlgoListener.monitor(strel);
 * 	
 * 	// run the operator on input image
 * 	ImageProcessor image = imagePlus.getProcessor();
 * 	ImageProcessor result = Morphology.dilation(image, strel);
 * 	ImagePlus resultPlus = new ImagePlus("Result", result);
 * 	resultPlus.show("Result");
 * </code></pre>	
 */
package inra.ijpb.algo;
