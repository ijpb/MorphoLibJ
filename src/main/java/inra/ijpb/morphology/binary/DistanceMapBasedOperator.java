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
