/**
 * 
 */
package inra.ijpb.label.conncomp;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * A list of boundaries, indexed by an integer label.
 * 
 * Provides utility method to generate new boundaries when required.
 * 
 * @author dlegland
 */
public class BoundarySet
{
    /**
     * The map between the label of a boundary and the Boundary instance
     * that stores indices of adjacent regions.
     */
    Map<Integer, Boundary> boundaries;
    
    public BoundarySet()
    {
        this.boundaries = new TreeMap<Integer, Boundary>();
    }
    
    public Boundary getBoundary(int index)
    {
        return this.boundaries.get(index);
    }
    
    public Boundary findOrCreateBoundary(Collection<Integer> neighborLabels)
    {
        // try to find boundary index if it exists
        int boundaryIndex = -1;
        for (Boundary boundary : this.boundaries.values())
        {
            if (boundary.hasSameRegions(neighborLabels))
            {
                return boundary;
            }
        }
        
        // if boundary does not exist, create a new one
        boundaryIndex = this.boundaries.size() + 1;
        Boundary boundary = new Boundary(boundaryIndex, neighborLabels);
        
        // update current boundary list
        this.boundaries.put(boundaryIndex, boundary);
        
        // return new boundary
        return boundary;
    }
}
