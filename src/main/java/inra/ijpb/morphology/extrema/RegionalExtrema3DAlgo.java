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
package inra.ijpb.morphology.extrema;

import ij.ImageStack;
import inra.ijpb.algo.AlgoStub;

/**
 * Interface for computing regional extrema (regional minima and regional maxima).
 * 
 * Example of use:
 * <pre><code>
 * ImageStack image = IJ.getImage().getStack();
 * RegionalExtrema3DAlgo algo = new RegionalExtrema3DFlooding(); 
 * algo.setConnectivity(6);
 * algo.setExtremaType(ExtremaType.MAXIMA);
 * ImageStack result = algo.applyTo(image);
 * ImagePlus resPlus = new ImagePlus("Regional Extrema", result); 
 * resPlus.show(); 
 * </code></pre>
 *
 * @see RegionalExtrema3DByFlooding
 * 
 * @author David Legland
 *
 */
public abstract class RegionalExtrema3DAlgo extends AlgoStub
{
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
	public RegionalExtrema3DAlgo() 
	{
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
	public RegionalExtrema3DAlgo(ExtremaType extremaType, int connectivity)
	{
		this.extremaType = extremaType;
		this.connectivity = connectivity;
	}

	// ==============================================================
	// getter and setters
	
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
	 * @param conn the connectivity value (either 6 or 26).
	 */
	public void setConnectivity(int conn) 
	{
		this.connectivity = conn;
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
	 * Applies this regional extrema algorithm on the 3D image given as argument,
	 * and returns the result as a binary image stack. 
	 * 
	 * @param inputImage the 3D image to process
	 * @return the result of regional extrema detection
	 */
	public abstract ImageStack applyTo(ImageStack inputImage); 
	
	/**
	 * Applies this regional extrema algorithm on the 3D image given as argument
	 * and using the given mask, and returns the result as a binary image stack. 
	 * 
	 * @param inputImage the 3D image to process
	 * @param maskImage a binary mask
	 * @return the result of regional extrema detection
	 */
	public abstract ImageStack applyTo(ImageStack inputImage, ImageStack maskImage);
	
}
