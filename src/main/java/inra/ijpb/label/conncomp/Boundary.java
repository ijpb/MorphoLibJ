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
    int label;
    TreeSet<Integer> regionLabels;
    
    Boundary(int label, Collection<Integer> regionLabels)
    {
        this.label = label;
        
        this.regionLabels = new TreeSet<Integer>();
        this.regionLabels.addAll(regionLabels);
    }
    
    public boolean hasSameRegions(Collection<Integer> regionLabels)
    {
        if (this.regionLabels.size() != regionLabels.size()) 
        {
            return false;
        }
        return this.regionLabels.containsAll(regionLabels);
    }
}
