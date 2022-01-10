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
public abstract class RegionalExtremaAlgo
{
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
	public RegionalExtremaAlgo() 
	{
	}
	
	/**
	 * Creates a new algorithm for computing regional extrema, by choosing type
	 * of minima and connectivity.
	 * 
	 * @param extremaType
	 *            the type of extrema (minima or maxima)
	 * @param connectivity
	 *            should be 4 or 8
	 */
	public RegionalExtremaAlgo(ExtremaType extremaType, int connectivity)
	{
		this.extremaType = extremaType;
		this.connectivity = connectivity;
	}

	
	// ==============================================================
	// getter and setters
	
	/**
	 * Returns the connectivity value.
	 * 
	 * @return the connectivity value.
	 */
	public int getConnectivity()
	{
		return this.connectivity;
	}
	
	/**
	 * Sets the connectivity value.
	 * 
	 * @param conn the connectivity value (either 4 or 8).
	 */
	public void setConnectivity(int conn)
	{
		this.connectivity = conn;
	}
	
	/**
	 * Returns the type of extrema.
	 * 
	 * @return the type of extrema.
	 */
	public ExtremaType getExtremaType() 
	{
		return extremaType;
	}

	/**
	 * Sets the type of extrema.
	 * 
	 * @param extremaType the type of extrema.
	 */
	public void setExtremaType(ExtremaType extremaType)
	{
		this.extremaType = extremaType;
	}

	/**
	 * Returns the value of progress visibility option.
	 * 
	 * @return the value of progress visibility option.
	 */
	public boolean isProgressVisible()
	{
		return progressVisible;
	}

	/**
	 * Sets the value of progress visibility option.
	 * 
	 * @param progressVisible the new value of progress visibility option.
	 */
	public void setProgressVisible(boolean progressVisible) 
	{
		this.progressVisible = progressVisible;
	}

	
	// ==============================================================
	// interface for running the algorithm
	
	/**
	 * Applies this regional extrema algorithm on the image given as argument,
	 * and returns the result as a binary image.
	 * 
	 * @param inputImage the image to process
	 * @return the result of regional extrema detection
	 */
	public abstract ImageProcessor applyTo(ImageProcessor inputImage); 
	
}
