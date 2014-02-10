package inra.ijpb.morphology.strel;

import java.util.SortedMap;
import java.util.TreeMap;

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
public class LocalBufferMaxFloat {
	
	SortedMap<Float, Integer> histo;
	
	/**
	 * Current max value
	 */
	float maxValue = Float.MIN_VALUE;

	/**
	 * Circular buffer of stored values
	 */
	float[] buffer;
	
	/**
	 * Current index in circular buffer
	 */
	int bufferIndex = 0;
	
	/**
	 * Main constructor.
	 */
	public LocalBufferMaxFloat(int n) {
		this(n, 0);
	}
	
	/**
	 * Initializes an histogram filled with the given value.
	 */
	public LocalBufferMaxFloat(int n, float value) {
		histo = new TreeMap<Float, Integer>();
		histo.put(value, n);
		
		this.buffer = new float[n];
		for (int i = 0; i < n; i++)
			this.buffer[i] = value;
	}

	/**
	 * Adds a value to the local histogram, and update bounds if needed. 
	 * Then removes the last stored value, and update bounds if needed.
	 * @param value the value to add
	 */
	public void add(float value) {

		// add the new value, and remove the oldest one
		addValue(value);
		removeValue(this.buffer[this.bufferIndex]);
		
		// update local circular buffer
		this.buffer[this.bufferIndex] = value;
		this.bufferIndex = (++this.bufferIndex) % this.buffer.length;
	}
	
	private void addValue(float value) {
		// update counts
		if (this.histo.containsKey(value)) {
			this.histo.put(value, this.histo.get(value) + 1);
		} else {
			this.histo.put(value, 1);
		}
		
		// update max value
		if (value > this.maxValue) {
			this.maxValue = value;
		}
	}
	
	private void removeValue(float value) {
		// Check bounds
		if (!this.histo.containsKey(value)) {
			throw new IllegalArgumentException("Can not remove a value not present in histogram: " + value);
		}
		
		int newCount = this.histo.get(value) - 1;
		if (newCount == 0) {
			histo.remove(value);
		} else {
			histo.put(value, newCount);
		}
		
		// update max value if needed
		if (value == this.maxValue) {
			updateMaxValue();
		}
	}
	
	private void updateMaxValue() {
		this.maxValue = histo.lastKey();
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
	public void fill(float value) {
		// get histogram size
		int n = this.buffer.length;

		histo.clear();
		histo.put(value, n);
		
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
	public float getMax() {
		return this.maxValue;
	}
}
