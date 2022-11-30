/**
 * 
 */
package inra.ijpb.label.conncomp;

import java.util.Collection;
import java.util.TreeSet;

/**
 * A boundary between several regions.
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
