/**
 * 
 */
package inra.ijpb.morphology.extrema;

import ij.process.ImageProcessor;

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
public abstract class RegionalExtremaAlgo {

	// ==============================================================
	// class variables
	
	int connectivity = 4;
	
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
	 * Applies this regional extrema algorithm on the image given as argument,
	 * and returns the result as a binary image. 
	 */
	public abstract ImageProcessor applyTo(ImageProcessor inputImage); 
	
}
