/**
 * 
 */
package inra.ijpb.morphology.extrema;

import ij.process.ImageProcessor;

/**
 * @author David Legland
 *
 */
public abstract class RegionalExtremaAlgo {

	// ==============================================================
	// constants
	
	/**
	 * One of the two types of extrema
	 */
	public enum ExtremaType {
		MINIMA, 
		MAXIMA;
	}
	
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
	
	public abstract ImageProcessor applyTo(ImageProcessor inputImage); 
	
}
