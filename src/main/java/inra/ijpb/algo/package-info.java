/**
 * Several classes for managing progress of algorithms. 
 * 
 * Sample code usage:
 * <code><pre>
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
 * </pre></code>	
 */
package inra.ijpb.algo;
