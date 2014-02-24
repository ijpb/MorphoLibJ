/**
 * 
 */
package inra.ijpb.morphology.extrema;

import ij.ImageStack;

/**
 * Interface for appying regional extrema (regional mininma and regional maxima).
 * 
 * Example of use:
 * <code><pre>
 * ImageProcessor image = IJ.getImage().getProcessor();
 * RegionalExtremaAlgo algo = new RegionalExtremaByFlooding(); 
 * algo.setConnectivity(4);
 * algo.setExtremaType(ExtremaType.MAXIMA);
 * ImageProcessor result = algo.applyTo(image);
 * ImagePlus resPlus = new ImagePlus("Regional Extrema", result); 
 * resPlus.show(); 
 * </pre></code>
 *
 * @author David Legland
 *
 */
public abstract class RegionalExtremaAlgo3D {

	// ==============================================================
	// class variables
	
	int connectivity = 6;
	
	ExtremaType extremaType = ExtremaType.MINIMA;
	
	boolean progressVisible = true;
	
	// ==============================================================
	// getter and setters
	
	public int getConnectivity() {
		return this.connectivity;
	}
	
	public void setConnectivity(int conn) {
		this.connectivity = conn;
	}
	
	public ExtremaType getExtremaType() {
		return extremaType;
	}

	public void setExtremaType(ExtremaType extremaType) {
		this.extremaType = extremaType;
	}

	public boolean isProgressVisible() {
		return progressVisible;
	}

	public void setProgressVisible(boolean progressVisible) {
		this.progressVisible = progressVisible;
	}

	
	// ==============================================================
	// interface for running the algorithm
	
	/**
	 * Applies this regional extrema algorithm on the 3D image given as argument,
	 * and returns the result as a binary image stack. 
	 */
	public abstract ImageStack applyTo(ImageStack inputImage); 
	
	/**
	 * Applies this regional extrema algorithm on the 3D image given as argument
	 * and using the given mask, and returns the result as a binary image stack. 
	 */
	public abstract ImageStack applyTo(ImageStack inputImage, ImageStack maskImage);
	
}
