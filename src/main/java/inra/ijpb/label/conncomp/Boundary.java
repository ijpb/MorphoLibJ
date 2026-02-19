/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
/**
 * 
 */
package inra.ijpb.label.conncomp;

import java.util.Collection;
import java.util.Iterator;
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
    
    @Override
    public String toString()
    {
        return String.format("Boundary(label=%d, regions={%s})", this.label, createRegionLabelsString(this.regionLabels));   
    }
    
    private static final String createRegionLabelsString(TreeSet<Integer> labels)
    {
        StringBuilder sb = new StringBuilder();
        Iterator<Integer> iter = labels.iterator();
        if (iter.hasNext())
        {
            sb.append(Integer.toString(iter.next()));
        }
        while (iter.hasNext())
        {
            sb.append(", " + Integer.toString(iter.next()));
        }
        return sb.toString();
    }
}
