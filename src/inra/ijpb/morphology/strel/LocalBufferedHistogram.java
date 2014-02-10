package inra.ijpb.morphology.strel;

/**
 * Local histogram in a linear neighborhood of a pixel. 
 * Works only for Grayscale images coded between 0 and 255.
 * 
 * @author David Legland
 *
 */
public class LocalBufferedHistogram {
	
	/**
	 * The count of values for each gray level
	 */
	int[] counts = new int[256];
	
	/**
	 * Current min value
	 */
	int minValue = Integer.MAX_VALUE;

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
	public LocalBufferedHistogram(int n) {
		this.counts[0] = n;
		
		this.buffer = new int[n];
		for (int i = 0; i < n; i++)
			this.buffer[i] = 0;
	}
	
	/**
	 * Initializes an histogram filled with the given value.
	 */
	public LocalBufferedHistogram(int n, int value) {
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
		// update counts
		this.counts[value]++;
		
		// update min and max values
		if (value > this.maxValue) {
			this.maxValue = value;
		} else if (value < this.minValue) {
			this.minValue = value;
		}

		// remove previous value
		remove(this.buffer[this.bufferIndex]);
		
		// update local circular buffer
		this.buffer[this.bufferIndex] = value;
		this.bufferIndex = (++this.bufferIndex) % this.buffer.length;
	}
	
	private void remove(int value) {
		// Check bounds
		if (this.counts[value] <= 0) {
			throw new IllegalArgumentException("Can not remove a value not present in histogram: " + value);
		}
		
		// Update counts
		this.counts[value]--;
		
		// update min and max values if needed
		if (value == this.maxValue) {
			updateMaxValue();
		} else if (value == this.minValue) {
			updateMinValue();
		}
	}
	
	private void updateMinValue() {
		// find the min value
		for (int i = 0; i< 256; i++) {
			if (counts[i] > 0) {
				this.minValue = i;
				return;
			}
		}
		throw new RuntimeException("Can not find minimum value in an empty histogram");
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
	 * Reset inner counts with zero values, and min and max values.
	 */
	public void clear() {
		this.fill(0);
	}
	
	/**
	 * Resets histogram by considering it is filled with the given value. 
	 * Update min and max accordingly.
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

		// update min and max values
		this.minValue = value;
		this.maxValue = value;
	}

	/**
	 * Returns the minimum value stored in this local histogram
	 * @return the minimum value in neighborhood
	 */
	public int getMin() {
		return this.minValue;
	}

	/**
	 * Returns the maximum value stored in this local histogram
	 * @return the maximum value in neighborhood
	 */
	public int getMax() {
		return this.maxValue;
	}
}
