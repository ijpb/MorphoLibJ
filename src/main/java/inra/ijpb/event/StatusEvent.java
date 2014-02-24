/**
 * 
 */
package inra.ijpb.event;

/**
 * An event class for propagating information messages about an algorithm.
 * @author David Legland
 */
public class StatusEvent {
	private Object source;
	private String message;
	
	public StatusEvent(Object source, String message) {
		this.source = source;
		this.message = message;
	}
	
	/**
	 * @return the source
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
}
