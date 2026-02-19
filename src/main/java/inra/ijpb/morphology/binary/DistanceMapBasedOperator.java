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
package inra.ijpb.morphology.binary;

import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.label.distmap.DistanceTransform2D;
import inra.ijpb.label.distmap.SaitoToriwakiDistanceTransform2DFloat;

/**
 * Implementation stub for operators based on a distance transform operator.
 * 
 * This class manages an inner reference to a DistanceTransform operator,
 * provides a default implementation of algorithm listener that propagates
 * events from distance transform to listener of this operator.
 * 
 * @author dlegland
 *
 */
public abstract class DistanceMapBasedOperator extends AlgoStub implements BinaryImageOperator, AlgoListener
{
    protected DistanceTransform2D distanceTransform;
    
    /**
     * Creates a new operator based on the specified Distance transform
     * operator.
     * 
     * @param distanceTransform
     *            the distanceTransform operator
     */
    protected DistanceMapBasedOperator(DistanceTransform2D distanceTransform)
    {
        setupDistanceTransform(distanceTransform);
    }
    
    /**
     * Creates a new operator based on a default Distance transform operator.
     */
    protected DistanceMapBasedOperator()
    {
        DistanceTransform2D algo = new SaitoToriwakiDistanceTransform2DFloat();
        setupDistanceTransform(algo);
    }
    
    private void setupDistanceTransform(DistanceTransform2D algo)
    {
        this.distanceTransform = algo;
        this.distanceTransform.addAlgoListener(this);
    }


    @Override
    public void algoProgressChanged(AlgoEvent evt) 
    {
        this.fireProgressChanged(evt);
    }

    @Override
    public void algoStatusChanged(AlgoEvent evt)
    {
        this.fireStatusChanged(evt);
    }
}
