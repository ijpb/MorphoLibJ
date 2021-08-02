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
package inra.ijpb.morphology.attrfilt;

import ij.ImageStack;
import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.Connectivity3D;

/**
 * Plugin to perform between attribute opening, closing, and black or white
 * top-hat on a 3D grayscale image. The size criterion is the number of voxels. 
 *
 * @see GrayscaleAttributeFiltering
 * @see SizeOpening3D
 *
 * @author dlegland
 *
 */
public class GrayscaleAttributeFiltering3D extends AlgoStub implements AlgoListener
{
	// ==============================================================
	// Class variables
	
	AttributeFilterType filterType = AttributeFilterType.OPENING;
	Attribute3D attribute = Attribute3D.VOLUME; 
	int minimumValue = 100;
	
	/** The value of connectivity, either C6 or C26. */
	Connectivity3D connectivity = Connectivity3D.C6;
	
	
	// ==============================================================
	// Constructors
	
	/**
	 * Constructor with default values.
	 */
	public GrayscaleAttributeFiltering3D()
	{
	}

	/**
	 * Constructor with specified values
	 */
	public GrayscaleAttributeFiltering3D(AttributeFilterType type, Attribute3D attribute, int minValue, Connectivity3D connectivity)
	{
		this.filterType = type;
		this.attribute = attribute;
		this.minimumValue = minValue;
		this.connectivity = connectivity;
	}

	// ==============================================================
	// Getters and Setters

	public AttributeFilterType getFilterType()
	{
		return filterType;
	}

	public void setFilterType(AttributeFilterType filterType)
	{
		this.filterType = filterType;
	}

	public Attribute3D getAttribute()
	{
		return attribute;
	}

	public void setAttribute(Attribute3D attribute)
	{
		this.attribute = attribute;
	}

	public int getMinimumValue()
	{
		return minimumValue;
	}

	public void setMinimumValue(int minimumValue)
	{
		this.minimumValue = minimumValue;
	}

	public Connectivity3D getConnectivity()
	{
		return connectivity;
	}

	public void setConnectivity(Connectivity3D connectivity)
	{
		this.connectivity = connectivity;
	}

	
	// ==============================================================
	// Processing methods

	/**
	 * Apply 3D grayscale attribute filtering to the input stack, using the
	 * inner settings.
	 * 
	 * @param image
	 *            the (grayscale) image to process
	 * @return the result of the attribute filtering.
	 */
	public ImageStack process(ImageStack image)
	{
        // Identify image to process (original, or inverted)
		ImageStack image2 = image.duplicate();
        if( filterType == AttributeFilterType.CLOSING || filterType == AttributeFilterType.BOTTOM_HAT )        	
        {       
        	Images3D.invert(image2);
        }
        
        // apply volume opening
		SizeOpening3DQueue algo = new SizeOpening3DQueue();
		algo.setConnectivity(connectivity.getValue());
		DefaultAlgoListener.monitor(algo);
		final ImageStack result = algo.process(image, minimumValue);
		
        // For top-hat and bottom-hat, we consider the difference with the
        // original image
		if (filterType == AttributeFilterType.TOP_HAT || filterType == AttributeFilterType.BOTTOM_HAT)
        {
			for (int x = 0; x < image.getWidth(); x++)
				for (int y = 0; y < image.getHeight(); y++)
					for (int z = 0; z < image.getSize(); z++)
        	{
				double diff = Math.abs(result.getVoxel(x, y, z) - image.getVoxel(x, y, z));
				result.setVoxel(x, y, z, diff);
        	}
        }

        // For closing, invert back the result
		else if (filterType == AttributeFilterType.CLOSING)
        {
			Images3D.invert(result);
        }
		
		return result;
	}


	// ==============================================================
	// Management of algorithms

	@Override
	public void algoProgressChanged(AlgoEvent evt)
	{
		this.fireProgressChanged(evt);
	}

	@Override
	public void algoStatusChanged(AlgoEvent evt)
	{
		this.fireStatusChanged(evt);
	}

}
