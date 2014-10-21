/**
 * 
 */
package inra.ijpb.morphology.extrema;

import ij.process.ImageProcessor;

/**
 * Interface for appying regional extrema (regional minima and maxima).
 * 
 * Example of use:
 * <pre><code>
 * ImageProcessor image = IJ.getImage().getProcessor();
 * RegionalExtremaAlgo algo = new RegionalExtremaByFlooding(); 
 * algo.setConnectivity(4);
 * algo.setExtremaType(ExtremaType.MAXIMA);
 * ImageProcessor result = algo.applyTo(image);
 * ImagePlus resPlus = new ImagePlus("Regional Extrema", result); 
 * resPlus.show(); 
 * </code></pre>
 *
 * @author David Legland
 *
 */
public abstract class RegionalExtremaAlgo {

	// ==============================================================
	// class variables
	
	/** Choose between minima or maxima */
	ExtremaType extremaType = ExtremaType.MINIMA;
	
	/** The value of connectivity, either 4 or 8 */
	int connectivity = 4;
	
	boolean progressVisible = true;
	
	// ==============================================================
	// Constructors
	
	/**
	 * Creates a new algorithm for computing regional extrema, that computes
	 * regional minima with connectivity 4.
	 */
	public RegionalExtremaAlgo() {
	}
	
	/**
	 * Creates a new algorithm for computing regional extrema, by choosing type of minima and connectivity.
	 * @param extremaType the type of extrema (minima or maxima)
	 * @param connectivity should be 4 or 8
	 */
	public RegionalExtremaAlgo(ExtremaType extremaType, int connectivity) {
		this.extremaType = extremaType;
		this.connectivity = connectivity;
	}

	
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
	 * Applies this regional extrema algorithm on the image given as argument,
	 * and returns the result as a binary image. 
	 */
	public abstract ImageProcessor applyTo(ImageProcessor inputImage); 
	
}
