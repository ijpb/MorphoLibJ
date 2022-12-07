/**
 * 
 */
package inra.ijpb.label.conncomp;

import java.util.Collection;
import java.util.TreeSet;

/**
 * A boundary between several regions.
 * 
 * The boundary is identified by an index (that can be used to populate a label
 * map), and contains a list of integers corresponding to the label of the
 * regions it is adjacent to. 
 * 
 * @author dlegland
 *
 */
public class Boundary
{
    /**
     * The (integer) label associated to this boundary. 
     */
    int label;
    
    /**
     * The list of integer labels corresponding to the regions this boundary is
     * adjacent to.
     */
    TreeSet<Integer> regionLabels;
    
    /**
     * Creates a new boundary by specifying its label, and the list of label of
     * the adjacent regions.
     * 
     * @param label
     *            the (integer) label associated to this boundary.
     * @param regionLabels
     *            the list of integer labels corresponding to the regions this
     *            boundary is adjacent to.
     */
    Boundary(int label, Collection<Integer> regionLabels)
    {
        this.label = label;
        
        this.regionLabels = new TreeSet<Integer>();
        this.regionLabels.addAll(regionLabels);
    }
    
    /**
     * @param regionLabels
     *            a list of integer labels
     * @return true if the specified labels are the same (not necessarily in the
     *         same order) than the labels of regions adjacent to this boundary.
     */
    public boolean hasSameRegions(Collection<Integer> regionLabels)
    {
        if (this.regionLabels.size() != regionLabels.size()) 
        {
            return false;
        }
        return this.regionLabels.containsAll(regionLabels);
    }
}
