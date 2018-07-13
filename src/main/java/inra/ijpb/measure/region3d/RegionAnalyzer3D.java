/**
 * 
 */
package inra.ijpb.measure.region3d;

import java.util.Map;
import java.util.TreeMap;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.label.LabelImages;
import inra.ijpb.measure.RegionAnalyzer;

/**
 * Base implementation of RegionAnalyzer interface for 3D binary/label
 * images.
 *
 * @param T
 *            the type of the data computed for each region. May be a class
 *            instance, or a single Numeric type.
 * @author dlegland
 *
 */
public abstract class RegionAnalyzer3D<T> extends AlgoStub implements RegionAnalyzer<T>
{
	/**
	 * Computes an instance of the generic type T for each region in input label image.
	 * 
	 * @param image
	 *            the input 3D image containing label of particles
	 * @param labels
	 *            the array of labels within the image
	 * @param calib
	 *            the spatial calibration of the image
	 * @return an array of the type used to represent the analysis result of each region 
	 */
	public abstract T[] analyzeRegions(ImageStack image, int[] labels, Calibration calib);
	
	/**
	 * Identifies labels within image and computes an instance of the generic
	 * type T for each region in input label image.
	 * 
	 * @param image
	 *            a 3D label image (8, 16 or 32 bits)
	 * @param calib
	 *            the spatial calibration of the image
	 * @return a map between the region label and the result of analysis for
	 *         each region
	 */
	public Map<Integer, T> analyzeRegions(ImageStack image, Calibration calib)
	{
		// extract particle labels
		fireStatusChanged(this, "Find Labels");
		int[] labels = LabelImages.findAllLabels(image);
		int nLabels = labels.length;
		
		// compute analysis result for each label
		fireStatusChanged(this, "Analyze regions");
		T[] results = analyzeRegions(image, labels, calib);

		// encapsulate into map
		fireStatusChanged(this, "Convert to map");
		Map<Integer, T> map = new TreeMap<Integer, T>();
		for (int i = 0; i < nLabels; i++)
		{
			map.put(labels[i], results[i]);
		}

        return map;
	}
	

	/**
	 * Default implementation of the analyzeRegions method, that calls the more
	 * specialized {@link #analyzeRegions(ImageProcessor, int[], Calibration)}
	 * method and transforms the result into a map.
	 * 
	 * @param labelPlus
	 *            the input image containing label of regions
	 * @return the mapping between region label and result of analysis for each
	 *         region
	 */
	public Map<Integer, T> analyzeRegions(ImagePlus labelPlus)
	{
		ImageStack labelImage = labelPlus.getStack();
		int[] labels = LabelImages.findAllLabels(labelImage);
		
		T[] results = analyzeRegions(labelImage, labels, labelPlus.getCalibration());
		
		// convert the arrays into a map of index-value pairs
		Map<Integer, T> map = new TreeMap<Integer, T>();
		for (int i = 0; i < labels.length; i++)
		{
			map.put(labels[i], results[i]);
		}
		
		return map;
	}


	/**
	 * Default implementation of computeTable method, using the two other
	 * methods {@link #analyzeRegions(ImagePlus)} and {@link #createTable(Map)}:
	 * 
	 * @param labelPlus
	 *            a label or binary image of region(s)
	 * @return an instance of ResultsTable containing results presented in a
	 *         tabular format.
	 */
	public ResultsTable computeTable(ImagePlus labelPlus)
	{
		return createTable(analyzeRegions(labelPlus));
	}
}
