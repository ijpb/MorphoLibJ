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
	public void addAlgoListener(AlgoListener listener);

	public void removeAlgoListener(AlgoListener listener);
}
