/**
 * 
 */
package inra.ijpb.algo;

/**
 * Interface for managing progression and status changes of an algorithm.
 *  
 * 
 * @author David Legland
 *
 */
public interface AlgoListener
{
	public void algoProgressChanged(AlgoEvent evt);

	public void algoStatusChanged(AlgoEvent evt);
}
