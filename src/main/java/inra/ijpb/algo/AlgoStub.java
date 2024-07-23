/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2024 INRA.
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

import java.util.ArrayList;

/**
 * A minimal implementation of algorithm for managing progression listeners.
 * 
 * @author David Legland
 */
public class AlgoStub implements Algo
{
	// ===================================================================
	// Class variables
	
	private ArrayList<AlgoListener> algoListeners = new ArrayList<AlgoListener>();


	// ===================================================================
	// Management of listeners
	
	@Override
	public void addAlgoListener(AlgoListener listener) 
	{
		this.algoListeners.add(listener);
	}

	@Override
	public void removeAlgoListener(AlgoListener listener) 
	{
		this.algoListeners.remove(listener);
	}

	
	// ===================================================================
	// fire events
	
	protected void fireProgressChanged(Object source, double step, double total) 
	{
		if (!this.algoListeners.isEmpty()) 
		{
			AlgoEvent evt = new AlgoEvent(source, step, total);
			for (AlgoListener listener : this.algoListeners)
			{
				listener.algoProgressChanged(evt);
			}
		}
	}

	protected void fireProgressChanged(AlgoEvent evt) 
	{
		for (AlgoListener listener : this.algoListeners) 
		{
			listener.algoProgressChanged(evt);
		}
	}
	
	protected void fireStatusChanged(Object source, String message) 
	{
		if (!this.algoListeners.isEmpty()) 
		{
			AlgoEvent evt = new AlgoEvent(source, message);
			for (AlgoListener listener : this.algoListeners) 
			{
				listener.algoStatusChanged(evt);
			}
		}
	}

	protected void fireStatusChanged(AlgoEvent evt)
	{
		for (AlgoListener listener : this.algoListeners)
		{
			listener.algoStatusChanged(evt);
		}
	}
}
