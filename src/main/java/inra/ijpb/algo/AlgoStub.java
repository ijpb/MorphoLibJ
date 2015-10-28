/**
 * 
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
	private ArrayList<AlgoListener> algoListeners = new ArrayList<AlgoListener>();

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
