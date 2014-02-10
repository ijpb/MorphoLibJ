package inra.ijpb.morphology.strel;

/**
 * <p>
 * Computes the maximum in a local buffer around current point.
 * </p>
 * <p>
 * This implementation considers a circular buffer (when a value is added, 
 * it replaces the first value that was inserted) and a local histogram.
 * The local histogram makes it possible to quickly find the maximum.
 * The circular buffer makes it possible to keep the local histogram 
 * up-to-date.
 * </p>
 * <p>
 * Works only for Grayscale images coded between 0 and 255.
 * </p>
 * 
 * @see LocalBufferMin
 * @see LocalBufferedHistogram
 * @author David Legland
 *
 */
public class LocalBufferMax {
	
	/**
	 * The count of values for each gray level
	 */
	int[] counts = new int[256];
	
	/**
	 * Current max value
	 */
	int maxValue = Integer.MIN_VALUE;

	/**
	 * Circular buffer of stored values
	 */
	int[] buffer;
	
	/**
	 * Current index in circular buffer
	 */
	int bufferIndex = 0;
	
	/**
	 * Main constructor.
	 */
	public LocalBufferMax(int n) {
		this.counts[0] = n;
		
		this.buffer = new int[n];
		for (int i = 0; i < n; i++)
			this.buffer[i] = 0;
	}
	
	/**
	 * Initializes an histogram filled with the given value.
	 */
	public LocalBufferMax(int n, int value) {
		this.counts[n] = value;
		
		this.buffer = new int[n];
		for (int i = 0; i < n; i++)
			this.buffer[i] = value;
	}

	/**
	 * Adds a value to the local histogram, and update bounds if needed. 
	 * Then removes the last stored value, and update bounds if needed.
	 * @param value the value to add
	 */
	public void add(int value) {

		// add the new value, and remove the oldest one
		addValue(value);
		removeValue(this.buffer[this.bufferIndex]);
		
		// update local circular buffer
		this.buffer[this.bufferIndex] = value;
		this.bufferIndex = (++this.bufferIndex) % this.buffer.length;
	}
	
	private void addValue(int value) {
		// update counts
		this.counts[value]++;
		
		// update max value
		if (value > this.maxValue) {
			this.maxValue = value;
		}
	}
	
	private void removeValue(int value) {
		// Check bounds
		if (this.counts[value] <= 0) {
			throw new IllegalArgumentException("Can not remove a value not present in histogram: " + value);
		}
		
		// Update counts
		this.counts[value]--;
		
		// update max value if needed
		if (value == this.maxValue) {
			updateMaxValue();
		}
	}
	
	private void updateMaxValue() {
		// find the max value
		for (int i = 255; i>=0; i--) {
			if (counts[i] > 0) {
				this.maxValue = i;
				return;
			}
		}
		throw new RuntimeException("Can not find maximum value in an empty histogram");
	}
	
	/**
	 * Reset inner counts with zero values, and max and max values.
	 */
	public void clear() {
		this.fill(0);
	}
	
	/**
	 * Resets histogram by considering it is filled with the given value. 
	 * Update max and max accordingly.
	 */
	public void fill(int value) {
		// get histogram size
		int n = this.buffer.length;

		// reset the histogram count
		for (int i = 0; i < 256; i++) {
			this.counts[i] = 0;
		}		
		this.counts[value] = n;
		
		// Clear the circular buffer
		for (int i = 0; i < n; i++)
			buffer[i] = value;

		// update max and max values
		this.maxValue = value;
	}

	/**
	 * Returns the maximum value stored in this local histogram
	 * @return the maximum value in neighborhood
	 */
	public int getMax() {
		return this.maxValue;
	}
}
