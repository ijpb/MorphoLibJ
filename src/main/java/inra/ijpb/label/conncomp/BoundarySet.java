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
    
    /**
     * Creates a new (empty) BoundarySet.
     */
    public BoundarySet()
    {
        this.boundaries = new TreeMap<Integer, Boundary>();
    }
    
    /**
     * @return the number of boundaries within the set.
     */
    public int size()
    {
        return this.boundaries.size();
    }
    
    /**
     * Retrieve a Boundary from its label.
     * 
     * @param label
     *            the label of the boundary
     * @return the boundary instance associated to the specified label
     */
    public Boundary getBoundary(int label)
    {
        return this.boundaries.get(label);
    }
    
    /**
     * Try to find a Boundary that has same adjacent regions as the ones
     * specified in the argument. If no such Boundary can be found, creates a
     * new one, adds it to the set, and returns the new Boundary.
     * 
     * @param neighborLabels
     *            the (integer) labels of the regions adjacent to the boundary
     * @return the (existing or new) Boundary instance with the specified labels
     *         as adjacent regions
     */
    public Boundary findOrCreateBoundary(Collection<Integer> neighborLabels)
    {
        // try to find existing boundary
        Boundary boundary = findBoundary(neighborLabels);
        
        // if boundary does not exist, create a new one
        if (boundary == null)
        {
            createBoundary(neighborLabels);
        }
        
        return boundary;
    }
    
    /**
     * Try to find a Boundary that has same adjacent regions as the ones
     * specified in the argument. If no such Boundary can be found, return null.
     * 
     * @param neighborLabels
     *            the (integer) labels of the regions adjacent to the boundary
     * @return the existing Boundary instance with the specified labels as
     *         adjacent regions, or null if no such boundary exist.
     */
    public Boundary findBoundary(Collection<Integer> neighborLabels)
    {
        // try to find existing boundary
        for (Boundary boundary : this.boundaries.values())
        {
            if (boundary.hasSameRegions(neighborLabels))
            {
                return boundary;
            }
        }
        
        // if boundary does not exist, create a new one
        return null;
    }
    
    /**
     * Creates a new Boundary from the list of adjacent regions, add it to the
     * set, and returns the new instance of Boundary.
     * 
     * @param neighborLabels
     *            the list of labels of the (usually two) regions adjacent to
     *            the boundary.
     * @return the new boundary.
     */
    public Boundary createBoundary(Collection<Integer> neighborLabels)
    {
        // if boundary does not exist, create a new one
        int boundaryIndex = this.boundaries.size() + 1;
        Boundary boundary = new Boundary(boundaryIndex, neighborLabels);
        
        // update current boundary list
        this.boundaries.put(boundaryIndex, boundary);
        
        // return new boundary
        return boundary;
    }
}