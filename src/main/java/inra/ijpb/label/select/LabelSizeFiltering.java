/**
 * 
 */
package inra.ijpb.label.select;

import java.util.ArrayList;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.algo.AlgoStub;
import inra.ijpb.label.LabelImages;

/**
 * Filter labels using a size criterion.
 * 
 * @author dlegland
 *
 */
public class LabelSizeFiltering extends AlgoStub
{
//    public enum Operator
//    {
//        LT,
//        GT,
//        LE,
//        GE,
//        EQ,
//        NE
//    }
    
    RelationalOperator operator;
    
    int sizeLimit;
    
    /**
     * Default constructor, specifying the operator and the size limit.
     */
    public LabelSizeFiltering(RelationalOperator operator, int sizeLimit)
    {
        this.operator = operator;
        this.sizeLimit = sizeLimit;
    }

    public ImagePlus process(ImagePlus imagePlus)
    {
        // initializations
        String newName = imagePlus.getShortTitle() + "-sizeFilt";
        ImagePlus resultPlus = null;
        
        if (imagePlus.getStackSize() == 1) 
        {
            ImageProcessor result = process(imagePlus.getProcessor());
            resultPlus = new ImagePlus(newName, result);
        } 
        else
        {
            ImageStack result = process(imagePlus.getStack());
            resultPlus = new ImagePlus(newName, result);
        }
        
        return resultPlus;
    }
    
    public ImageProcessor process(ImageProcessor labelImage)
    {
        // compute area of each label
        int[] labels = LabelImages.findAllLabels(labelImage);
        int[] areas = LabelImages.pixelCount(labelImage, labels);
        
        // find labels with sufficient area
        ArrayList<Integer> labelsToKeep = new ArrayList<Integer>(labels.length);
        for (int i = 0; i < labels.length; i++) 
        {
            if (operator.evaluate(areas[i], sizeLimit))
            {
                labelsToKeep.add(labels[i]);
            }
        }

        // Convert array list into int array
        int[] labels2 = new int[labelsToKeep.size()];
        for (int i = 0; i < labelsToKeep.size(); i++) 
        {
            labels2[i] = labelsToKeep.get(i);
        }
        
        // keep only necessary labels
        ImageProcessor result = LabelImages.keepLabels(labelImage, labels2);

        if (!(result instanceof ColorProcessor))
            result.setLut(labelImage.getLut());
        return result;
    }

    public ImageStack process(ImageStack image)
    {
        return image;
    }
}
