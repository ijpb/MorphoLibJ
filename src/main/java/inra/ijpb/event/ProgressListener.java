/**
 * 
 */
package inra.ijpb.event;

/**
 * Interface for managing progression of an algorithm.
 *  
 * @author David Legland
 *
 */
public interface ProgressListener {
	public void progressChanged(ProgressEvent evt);
}
