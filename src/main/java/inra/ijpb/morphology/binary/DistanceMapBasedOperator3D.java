/**
 * 
 */
package inra.ijpb.morphology.binary;

import inra.ijpb.algo.AlgoEvent;
import inra.ijpb.algo.AlgoListener;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.label.distmap.DistanceTransform3D;
import inra.ijpb.label.distmap.SaitoToriwakiDistanceTransform3DFloat;

/**
 * Implementation stub for operators based on a 3D distance transform operator.
 * 
 * This class manages an inner reference to a DistanceTransform3D operator,
 * provides a default implementation of algorithm listener that propagates
 * events from distance transform to listener of this operator.
 * 
 * @author dlegland
 *
 */
public abstract class DistanceMapBasedOperator3D extends AlgoStub implements BinaryImageOperator3D, AlgoListener
{
    protected DistanceTransform3D distanceTransform;
    
    /**
     * Creates a new operator based on the specified 3D distance transform
     * operator.
     * 
     * @param distanceTransform
     *            the distanceTransform operator
     */
    protected DistanceMapBasedOperator3D(DistanceTransform3D distanceTransform)
    {
        setupDistanceTransform(distanceTransform);
    }
    
    /**
     * Creates a new operator based on a default 3D distance transform operator.
     */
    protected DistanceMapBasedOperator3D()
    {
        DistanceTransform3D algo = new SaitoToriwakiDistanceTransform3DFloat();
        setupDistanceTransform(algo);
    }
    
    private void setupDistanceTransform(DistanceTransform3D algo)
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
