/**
 * 
 */
package inra.ijpb.algo;

/**
 * An interface for managing progression and status changes of algorithms.
 * 
 * @author David Legland
 *
 */
public interface Algo
{
	public void addProgressListener(ProgressListener listener);

	public void removeProgressListener(ProgressListener listener);
	
	public void addStatusListener(StatusListener listener);

	public void removeStatusListener(StatusListener listener);
}
