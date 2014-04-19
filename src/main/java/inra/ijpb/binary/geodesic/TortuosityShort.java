/**
 * 
 */
package inra.ijpb.binary.geodesic;

import ij.IJ;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/**
 * @author David Legland
 *
 */
public class TortuosityShort {

	short[] weights;
	GeodesicDistanceMapShort mapCalculator;

	/**
	 * Initializes the tortuosity map calculator with the given set of weights.
	 */
	public TortuosityShort(short[] weights) {
		this.weights = weights;
		
		mapCalculator = new GeodesicDistanceMapShort(weights, false);
		mapCalculator.setMaskLabel(0);
	}

	public double computeTortuosityTopDown(ImageProcessor image) {
		int width 	= image.getWidth();
		int height 	= image.getHeight();
		ByteProcessor marker = new ByteProcessor(width, height);
		ShortProcessor distMap;
		
		// one length for each pixel on top of image
		double[] geodLengths = new double[width];
		
		// iterate on pixel of first row
		for (int x = 0; x < width; x++) {
			// If current pixel is foreground, tortuosity is undefined (NaN)
			if (image.get(x, 0) > 0) {
				geodLengths[x] = Double.NaN;
				continue;
			}
			
			// propagate geodesic distance from current marker
			resetMarker(marker, x, 0);
			distMap = mapCalculator.geodesicDistanceMap(image, marker);
			
			// compute smallest distance on last line
			double distMin = minValueHorzLine(distMap, height-1);
			
			geodLengths[x] = distMin / this.weights[0];
		}
		
		
		double lengthMin = Double.MAX_VALUE;
		for (int x = 0; x < width; x++) {
			if (geodLengths[x] > Double.MAX_VALUE / 4)
				continue;
			if (Double.isNaN(geodLengths[x]))
				continue;
			lengthMin = Math.min(lengthMin, geodLengths[x]);
		}
		
		return lengthMin / (width - 1);
	}
	
	/**
	 * Computes geodesic length to the bottom border starting from the pixel
	 * located at (x0,0).
	 */
	public double computeGeodLengthTopDown(ImageProcessor image, int x0) {
		int width 	= image.getWidth();
		int height 	= image.getHeight();
		ByteProcessor marker = new ByteProcessor(width, height);
		ShortProcessor distMap;
		
		// one length for each pixel on top of image
		double geodLength;
		
		// If current pixel is foreground, tortuosity is undefined (NaN)
		if (image.get(x0, 0) > 0) {
			return Double.NaN;
		} 
			
		// propagate geodesic distance from current marker
		resetMarker(marker, x0, 0);
		distMap = mapCalculator.geodesicDistanceMap(image, marker);
		
		// compute smallest distance on last line
		double distMin = minValueHorzLine(distMap, height-1);
		
		geodLength = distMin / this.weights[0];
		return geodLength;
	}

	/**
	 * Computes average tortuosity in a tortuosity map, by keeping only one
	 * pixel over spacing in each direction.
	 */
	public ResultsTable tortuosity(ImageProcessor image, int spacing) {

		FloatProcessor tortMap;
		if (image instanceof FloatProcessor)
			tortMap = (FloatProcessor) image;
		else
			tortMap = tortuosityMap(image);
		
		return averageTortuosity(tortMap, spacing);
	}
	
	/**
	 * Computes average tortuosity in a tortuosity map, by keeping only one
	 * pixel over spacing in each direction.
	 */
	private ResultsTable averageTortuosity(FloatProcessor tortMap, int spacing) {
		// Size of image
		int width 	= tortMap.getWidth();
		int height 	= tortMap.getHeight();

		double tortSum = 0;
		int tortCount = 0;
		
		for (int y = 0; y < height; y += spacing) {
			IJ.showProgress(y, height);
			for (int x = 0; x < width; x += spacing) {
				float tort = tortMap.getf(x, y);
				if (Float.isInfinite(tort) || Float.isNaN(tort))
					continue;
				
				tortSum += tort;
				tortCount++;
			}
		}
			
		// Clear progress bar
		IJ.showProgress(1, 0);
		
		double avgTort = tortSum / tortCount;
		
		// Create data table for result
		ResultsTable table = new ResultsTable();
		
		// Populate the results table
		table.incrementCounter();
		table.addValue("Tortuosity", avgTort);
		table.addValue("Count", tortCount);
		
		return table;
	}
	
	public FloatProcessor tortuosityMap(ImageProcessor image) {
		// Size of image
		int width 	= image.getWidth();
		int height 	= image.getHeight();
		
		// Initialize result image with NAN or 0 depending on initial image
		FloatProcessor res = new FloatProcessor(width, height);
		res.setValue(0);
		res.fill();
		
		// Create empty marker images 
		ByteProcessor marker1 = new ByteProcessor(width, height);
		ByteProcessor marker2 = new ByteProcessor(width, height);
		ShortProcessor distMap1, distMap2;
		
		// initialize markers in first and last rows
		marker1.setValue(0); marker1.fill();
		marker2.setValue(0); marker2.fill();
		for (int x = 0; x < width; x++) {
			marker1.set(x, 0, 255);
			marker2.set(x, height-1, 255);
		}

		// tortuosity in the vertical direction
		distMap1 = mapCalculator.geodesicDistanceMap(image, marker1);
		distMap2 = mapCalculator.geodesicDistanceMap(image, marker2);
		FloatProcessor tort1 = calcTortuosity(distMap1, distMap2, height);
		
		// initialize markers in first and last columns
		marker1.setValue(0); marker1.fill();
		marker2.setValue(0); marker2.fill();
		for (int y = 0; y < height; y++) {
			marker1.set(0, y, 255);
			marker2.set(width-1, y, 255);
		}

		// tortuosity in the horizontal direction
		distMap1 = mapCalculator.geodesicDistanceMap(image, marker1);
		distMap2 = mapCalculator.geodesicDistanceMap(image, marker2);
		FloatProcessor tort2 = calcTortuosity(distMap1, distMap2, width);
		
		// Normalization step:
		// * Set foreground pixel of original image to NaN
		// * compute either average of horz and vertical tortuosity, or keep
		//	   only the finite one
		// * normalize valid distance such that they are expressed in pixels 
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (image.get(x, y) > 0) {
					res.setf(x, y, Float.NaN);
				} else {
					float val1 = tort1.getf(x, y);
					float val2 = tort2.getf(x, y);
					if (!Float.isInfinite(val1) && !Float.isInfinite(val2))
						res.setf(x, y, (float) (val1 + val2) / 2 );
					else {
						if (!Float.isInfinite(val1))
							res.setf(x, y, (float) val1);
						else
							res.setf(x, y, (float) val2);
					}
				}
			}
		}

		return res;
	}

	/**
	 * Computes a tortuosity map by computing the sum of two geodesic distances.
	 * If the value given by one of the two maps is maximal, the corresponding
	 * result value is set to infinite. Finite result values are normalized by
	 * the given length (corresponding to the image size in the direction of 
	 * interest).
	 */
	private FloatProcessor calcTortuosity(ShortProcessor distMap1,
			ShortProcessor distMap2, int length) {
		// Size of images
		int width 	= distMap1.getWidth();
		int height 	= distMap1.getHeight();
		FloatProcessor res = new FloatProcessor(width, height);
		
		// iterate over image pixels
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				short val1 = (short) distMap1.get(x, y);
				short val2 = (short) distMap2.get(x, y);
		
				if (val1 == Short.MAX_VALUE || val2 == Short.MAX_VALUE) {
					// if one border can not be reached, result is infinite
					res.setf(x, y, Float.POSITIVE_INFINITY);
				} else {
					// sum of the distances to each border, normalized by 
					// chamfer weight
					double lg = ((double) val1 + (double) val2) / weights[0];
					
					// normalize by image size
					double tort = lg / (length - 1);
					res.setf(x, y, (float) tort);
				} 
			}
		}
		
		return res;
	}

	public FloatProcessor tortuosityMapOld(ImageProcessor image) {
		// Size of image
		int width 	= image.getWidth();
		int height 	= image.getHeight();
		
		FloatProcessor res = new FloatProcessor(width, height);
		
		for (int y = 0; y < height; y++) {
			IJ.showProgress(y, height);
			for (int x = 0; x < width; x++) {
				if (image.get(x, y) > 0) {
					res.setf(x, y, Float.NaN);
				} else {
					// compute distance to each face
					double[] dists = geodesicLengthToFaces(image, x, y);
					
					// geodesic lengths in x and y directions
					double lx = (dists[0] + dists[1]) / 2;
					double ly = (dists[2] + dists[3]) / 2;
					
					// compute tortuosity from lengths
					double tx = lx / (width - 1);
					double ty = ly / (height - 1);

					// average tortuosity
					double tort = (tx + ty) / 2;
					
					if (Double.isInfinite(tort))
						res.setf(x, y, Float.POSITIVE_INFINITY);
					else
						res.setf(x, y, (float) tort);
				}
			}
		}
			
		// Clear progress bar
		IJ.showProgress(1, 0);

		return res;
	}
	
	public double[] geodesicLengthToFaces(ImageProcessor image, int x0, int y0) {
		// Size of image
		int width 	= image.getWidth();
		int height 	= image.getHeight();
		
		// pointer to images
		ByteProcessor marker = new ByteProcessor(width, height);
		ShortProcessor distMap;
		
		// If current pixel is foreground, tortuosity is undefined (NaN)
		if (image.get(x0, y0) > 0) {
			return new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN};
		} 
			
		// propagate geodesic distance from current marker
		resetMarker(marker, x0, y0);
		distMap = mapCalculator.geodesicDistanceMap(image, marker);
		
		// Compute minimal distances on border lines
		double distX0 = minValueVertLine(distMap, 0);
		double distX1 = minValueVertLine(distMap, width - 1);
		double distY0 = minValueHorzLine(distMap, 0);
		double distY1 = minValueHorzLine(distMap, height - 1);
		
		// put distances into an array
		double[] res = new double[]{distX0, distX1, distY0, distY1};
		return res;
	}

	/**
	 * Computes the minimal value of the distance map on the horizontal line with
	 * position given by y.
	 */
	private double minValueHorzLine(ShortProcessor distMap, int y) {
		// the minimal distance value
		int minDist = Short.MAX_VALUE;
		
		// a flag indicating whether value is valid
		boolean reached = false;
		
		// iterate over all pixels of the horizontal line
		for (int x = 0; x < distMap.getWidth(); x++) {
			int val = distMap.get(x, y); 
			if (val != Short.MAX_VALUE) {
				reached = true;
				minDist = Math.min(minDist, val);
			}
		}
		
		// If value is not valid, return infinity
		if (reached)
			return minDist;
		else
			return Double.POSITIVE_INFINITY;
	}

	/**
	 * Computes the minimal value of the distance map on the vertical line with
	 * position given by x.
	 */
	private double minValueVertLine(ShortProcessor distMap, int x) {
		// the minimal distance value
		int minDist = Short.MAX_VALUE;
		
		// a flag indicating whether value is valid
		boolean reached = false;
		
		// iterate over all pixels of the vertical line
		for (int y = 0; y < distMap.getHeight(); y++) {
			int val = distMap.get(x, y); 
			if (val != Short.MAX_VALUE) {
				reached = true;
				minDist = Math.min(minDist, val);
			}
		}
		
		// If value is not valid, return infinity
		if (reached)
			return minDist;
		else
			return Double.POSITIVE_INFINITY;
	}

	/**
	 * Clears all the mask, exepct the pixel located at (x0, y0).
	 */
	private void resetMarker(ByteProcessor marker, int x0, int y0) {
		marker.setValue(0);
		marker.fill();
		marker.set(x0, y0, 255);
	}
}
