/**
 * 
 */
package inra.ijpb.measure;

import java.util.Map;

import ij.ImagePlus;
import ij.measure.ResultsTable;

/**
 * Common interface for classes devoted to the extraction of information from
 * each region of label or binary image.
 *
 * @param <T>
 *            the type of the data computed for each region. May be a class
 *            instance, or a single Numeric type.
 *            
 * @author dlegland
 *
 */
public interface RegionAnalyzer<T>
{
	/**
	 * Generic method to compute the result of an analysis on each region of a
	 * label or binary image.
	 * 
	 * @param labelPlus
	 *            a label or binary image of region(s)
	 * @return the mapping between each region label within the image and the
	 *         result of the analysis for the regions
	 */
	public Map<Integer, T> analyzeRegions(ImagePlus labelPlus);
	
	/**
	 * <p>Returns the result of the analysis in the form of a ResultsTable, to
	 * facilitate concatenation of results obtained from several instances of
	 * RegionAnalyzer.</p>
	 * 
	 * <p>
	 * This method can be quickly implemented by using the two other methods {@link #analyzeRegions(ImagePlus)} and {@link #createTable(Map)}:
	 * <pre>{@code
	 *  public ResultsTable computeTable(ImagePlus labelPlus)
	 *  {
	 *      return createTable(analyzeRegions(labelPlus));
	 *  }
	 * }
	 * </pre> 
	 * 
	 * 
	 * @param labelPlus
	 *            a label or binary image of region(s)
	 * @return an instance of ResultsTable containing results presented in a
	 *         tabular format.
	 */
	public ResultsTable computeTable(ImagePlus labelPlus);

	/**
	 * Utility method that converts the detailed results of the
	 * {@link #analyzeRegions(ImagePlus)} method into an instance of
	 * ResultsTable to facilitate display by ImageJ.
	 * 
	 * @param results
	 *            the mapping between each region label and the result of the
	 *            analysis
	 * @return an instance of ResultsTable containing results presented in a
	 *         tabular format.
	 */
	public ResultsTable createTable(Map<Integer, T> results);
}
