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
 * ImageStack image = IJ.getImage().getStack();
 * RegionalExtrema3DAlgo algo = new RegionalExtrema3DFlooding(); 
 * algo.setConnectivity(6);
 * algo.setExtremaType(ExtremaType.MAXIMA);
 * ImageStack result = algo.applyTo(image);
 * ImagePlus resPlus = new ImagePlus("Regional Extrema", result); 
 * resPlus.show(); 
 * </pre></code>
 *
 * @author David Legland
 *
 */
public abstract class RegionalExtrema3DAlgo {

	// ==============================================================
	// class variables
	
	/** Choose between minima or maxima */
	ExtremaType extremaType = ExtremaType.MINIMA;
	
	/** The value of connectivity, either 4 or 8 */
	int connectivity = 6;
	
	boolean progressVisible = true;
	
	
	// ==============================================================
	// Constructors
	
	/**
	 * Creates a new algorithm for computing regional extrema, that computes
	 * regional minima with connectivity 6.
	 */
	public RegionalExtrema3DAlgo() {
	}
	
	/**
	 * Creates a new algorithm for computing regional extrema, by choosing type
	 * of minima and connectivity.
	 * 
	 * @param extremaType
	 *            the type of extrema (minima or maxima)
	 * @param connectivity
	 *            should be 6 or 26
	 */
	public RegionalExtrema3DAlgo(ExtremaType extremaType, int connectivity) {
		this.extremaType = extremaType;
		this.connectivity = connectivity;
	}

	// ==============================================================
	// getter and setters
	
	public ExtremaType getExtremaType() {
		return extremaType;
	}

	public void setExtremaType(ExtremaType extremaType) {
		this.extremaType = extremaType;
	}

	public int getConnectivity() {
		return this.connectivity;
	}
	
	public void setConnectivity(int conn) {
		this.connectivity = conn;
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
