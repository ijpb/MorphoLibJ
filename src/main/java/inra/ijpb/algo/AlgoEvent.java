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
package inra.ijpb.algo;

/**
 * An event class for storing information about the status and progression of
 * an algorithm.
 * 
 * @author David Legland
 *
 */
public class AlgoEvent 
{
	/**
	 * The algorithm object that threw this event
	 */
	protected Object source;
	
	protected String status;
	
	protected double step;
	protected double total;
	
	/**
	 * Creates a new AlgoEvent.
	 * 
	 * @param source
	 *            the object generating the event
	 * @param status
	 *            the status message
	 * @param step
	 *            the progression step
	 * @param total
	 *            the total amount of progression step
	 */
	public AlgoEvent(Object source, String status, double step, double total) 
	{
		this.source = source;
		this.status = status;
		this.step = step;
		this.total = total;
	}
	
	/**
	 * Creates a new AlgoEvent.
	 * 
	 * @param source
	 *            the object generating the event
	 * @param status
	 *            the status message
	 */
	public AlgoEvent(Object source, String status) 
	{
		this.source = source;
		this.status = status;
		this.step = 0;
		this.total = 0;
	}
	
	/**
	 * Creates a new AlgoEvent.
	 * 
	 * @param source
	 *            the object generating the event
	 * @param step
	 *            the progression step
	 * @param total
	 *            the total amount of progression step
	 */
	public AlgoEvent(Object source, double step, double total) 
	{
		this.source = source;
		this.status = "";
		this.step = step;
		this.total = total;
	}
	
	/**
	 * @return the source object
	 */
	public Object getSource() 
	{
		return source;
	}

	/**
	 * @return the current status of the algorithm
	 */
	public String getStatus() 
	{
		return status;
	}
	
	/**
	 * @return the current progression of the algorithm
	 */
	public double getCurrentProgress() 
	{
		return step;
	}

	/**
	 * @return the total progression of the algorithm
	 */
	public double getTotalProgress() 
	{
		return total;
	}

	/**
	 * @return the progress ratio, as the ratio of current progression over total progression.
	 */
	public double getProgressRatio() 
	{
		return this.step / this.total;
	}
}
