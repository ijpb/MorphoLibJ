/**
 * 
 */
package inra.ijpb.event;

import java.util.ArrayList;

/**
 * A minimal implementation of algorithm for managing progression listeners.
 * @author David Legland
 */
public class AlgoStub implements Algo {
	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
	private ArrayList<StatusListener> statusListeners = new ArrayList<StatusListener>();

	@Override
	public void addProgressListener(ProgressListener listener) {
		this.progressListeners.add(listener);
	}

	@Override
	public void removeProgressListener(ProgressListener listener) {
		this.progressListeners.remove(listener);
	}
	
	@Override
	public void addStatusListener(StatusListener listener) {
		this.statusListeners.add(listener);
	}

	@Override
	public void removeStatusListener(StatusListener listener) {
		this.statusListeners.remove(listener);
	}

	protected void fireProgressChange(Object source, double step, double total) {
		if (!this.progressListeners.isEmpty()) {
			ProgressEvent evt = new ProgressEvent(source, step, total);
			for (ProgressListener listener : this.progressListeners) {
				listener.progressChanged(evt);
			}
		}
	}

	protected void fireProgressChange(ProgressEvent evt) {
		for (ProgressListener listener : this.progressListeners) {
			listener.progressChanged(evt);
		}
	}
	
	protected void fireStatusChanged(Object source, String message) {
		if (!this.statusListeners.isEmpty()) {
			StatusEvent evt = new StatusEvent(source, message);
			for (StatusListener listener : this.statusListeners) {
				listener.statusChanged(evt);
			}
		}
	}

	protected void fireStatusChanged(StatusEvent evt) {
		for (StatusListener listener : this.statusListeners) {
			listener.statusChanged(evt);
		}
	}
}
