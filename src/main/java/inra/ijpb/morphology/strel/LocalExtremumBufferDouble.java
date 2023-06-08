/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
package inra.ijpb.morphology.strel;

/**
 * <p>
 * Computes the maximum in a local buffer around current point.
 * </p>
 * <p>
 * This implementation considers a circular buffer (when a value is added, it
 * replaces the first value that was inserted) that makes it possible to update
 * extremum if needed.
 * </p>
 * 
 * @author David Legland
 * 
 */
public class LocalExtremumBufferDouble implements LocalExtremum 
{
    // ==================================================
    // Class variables
    
	/**
	 * Circular buffer of stored values.
	 */
	double[] buffer;
	
	/**
	 * Current index in circular buffer.
	 */
	int bufferIndex = 0;
	
    /**
     * Use a sign flag for managing both min and max.
     * sign = +1 -> compute max values
     * sign = -1 -> compute min values
     */
    int sign;
    
    /**
     * Current max value.
     */
    double maxValue = Double.NEGATIVE_INFINITY;

    boolean updateNeeded = false;
    
    
    // ==================================================
    // Constructor and initialization
    
	/**
	 * Main constructor.
	 *
	 * @param n
	 *            the size of the buffer
	 */
	public LocalExtremumBufferDouble(int n) 
	{
		this.buffer = new double[n];
		for (int i = 0; i < n; i++)
			this.buffer[i] = 0;
	}
	
	/**
	 * Constructor from size and type of extremum (minimum or maximum).
	 *
	 * @param n
	 *            the size of the buffer
	 * @param type
	 *            the type of extremum (maximum or minimum)
	 */
	public LocalExtremumBufferDouble(int n, LocalExtremum.Type type)
	{
		this(n);
		switch (type)
		{
	        case MINIMUM: this.sign = -1; break;
	        case MAXIMUM: this.sign = +1; break;
		}
	}
	
	/**
	 * Initializes an histogram filled with the given value.
	 *
	 * @param n
	 *            the size of the buffer
	 * @param value
	 *            the initial value of all elements in buffer
	 */
	public LocalExtremumBufferDouble(int n, double value) 
	{
		this.buffer = new double[n];
		for (int i = 0; i < n; i++)
			this.buffer[i] = value;
		this.maxValue = value;
	}

	/**
	 * Changes the sign used for distinguishing minimum and maximum.
	 * 
     * @deprecated should specify Extremum type at construction instead
     * 
	 * @param sign
	 *            +1 for maximum, -1 for minimum
	 */
	@Deprecated
	public void setMinMaxSign(int sign) 
	{
		this.sign = sign;
	}
	
	
    // ==================================================
    // General methods
    
	/**
	 * Adds a value to the local histogram, and update bounds if needed. 
	 * Then removes the last stored value, and update bounds if needed.
	 * 
	 * @param value the value to add
	 */
	public void add(double value) 
	{
		// add the new value, and remove the oldest one
		addValue(value);
		removeValue(this.buffer[this.bufferIndex]);
		
		// update local circular buffer
		this.buffer[this.bufferIndex] = value;
		this.bufferIndex = (++this.bufferIndex) % this.buffer.length;
	}
	
	/**
	 * Updates local extremum with the specified value.
	 * 
	 * @param value
	 *            the value to add
	 */
	private void addValue(double value) 
	{
		// update max value
		if (value * sign > this.maxValue * sign) 
		{
			this.maxValue = value;
            updateNeeded = false;
		}
	}
	
	/**
	 * Updates local extremum with the specified value.
	 * 
	 * @param value
	 *            the value to remove
	 */
	private void removeValue(double value) 
	{
		// update max value if needed
		if (value == this.maxValue) 
		{
			updateNeeded = true;
		}
	}
	
	private void recomputeMaxValue() 
	{
		if (sign == 1)
		{
			// find the maximum value in the buffer
			this.maxValue = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < buffer.length; i++) 
			{
				this.maxValue = Math.max(this.maxValue, this.buffer[i]);
			}
		}
		else
		{
			// find the maximum value in the buffer
			this.maxValue = Double.POSITIVE_INFINITY;
			for (int i = 0; i < buffer.length; i++) 
			{
				this.maxValue = Math.min(this.maxValue, this.buffer[i]);
			}
		}
		
		updateNeeded = false;
	}
	
    /**
     * Resets inner counts with default maximum values (+INFINITY for MAX,
     * -INFINITY for MIN)
     */
	public void clear() 
	{
		if (this.sign == 1)
			this.fill(Double.NEGATIVE_INFINITY);
		else
			this.fill(Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Resets histogram by considering it is filled with the given value. 
	 * Update max and max accordingly.
	 * 
	 * @param value
	 *            the new value of all elements in buffer
	 */
	public void fill(double value) 
	{
		// get buffer size
		int n = this.buffer.length;

		// Clear the circular buffer
		for (int i = 0; i < n; i++)
			buffer[i] = value;

		// update max and max values
		this.maxValue = value;
        updateNeeded = false;
	}

	/**
	 * Returns the maximum value stored in this local histogram
	 * @return the maximum value in neighborhood
	 */
	public double getMax() 
	{
		if (updateNeeded) 
		{
			recomputeMaxValue();
		}
		
		return this.maxValue;
	}
}
