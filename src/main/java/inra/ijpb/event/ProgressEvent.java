/**
 * 
 */
package inra.ijpb.event;

/**
 * An event class for propagating information about the progression of an 
 * algorithm.
 * @author David Legland
 *
 */
public class ProgressEvent {
	private Object source;
	private double step;
	private double total;
	
	public ProgressEvent(Object source, double step, double total) {
		this.source = source;
		this.step = step;
		this.total = total;
	}
	
	/**
	 * @return the source
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * @return the step
	 */
	public double getStep() {
		return step;
	}

	/**
	 * @return the total
	 */
	public double getTotal() {
		return total;
	}

	public double getProgressRatio() {
		return this.step / this.total;
	}
}
