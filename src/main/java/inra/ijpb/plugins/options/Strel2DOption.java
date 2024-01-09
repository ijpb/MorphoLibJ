/**
 * 
 */
package inra.ijpb.plugins.options;

import ij.gui.GenericDialog;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.strel.Cross3x3Strel;
import inra.ijpb.morphology.strel.DiamondStrel;
import inra.ijpb.morphology.strel.DiskStrel;
import inra.ijpb.morphology.strel.LinearDiagDownStrel;
import inra.ijpb.morphology.strel.LinearDiagUpStrel;
import inra.ijpb.morphology.strel.LinearHorizontalStrel;
import inra.ijpb.morphology.strel.LinearVerticalStrel;
import inra.ijpb.morphology.strel.OctagonStrel;
import inra.ijpb.morphology.strel.SquareStrel;

/**
 * 
 */
public class Strel2DOption implements GenericDialogOption<Strel>
{
    /**
     * An enumeration of the different possible structuring element shapes. 
     * Each item of the enumeration can create Strel instances of specific
     * class and of given size.
     */
    public enum Shape
    {
        /**
         * Disk of a given radius
         * @see DiskStrel 
         */
        DISK("Disk"),
        
        /** 
         * Square of a given side
         * @see SquareStrel 
         */
        SQUARE("Square"),
        
        /** 
         * Diamond of a given diameter
         * @see DiamondStrel
         * @see Cross3x3Strel 
         */
        DIAMOND("Diamond"),
        
        /** 
         * Octagon of a given diameter
         * @see OctagonStrel
         */
        OCTAGON("Octagon"),
        
        /**
         * Horizontal line of a given length 
         * @see LinearHorizontalStrel
         */
        LINE_HORIZ("Horizontal Line"),
        
        /** 
         * Vertical line of a given length 
         * @see LinearVerticalStrel
         */
        LINE_VERT("Vertical Line"),
        
        /**
         * Diagonal line of a given length 
         * @see LinearDiagUpStrel
         */
        LINE_DIAG_UP("Line 45 degrees"),
        
        /** 
         * Diagonal line of a given length 
         * @see LinearDiagDownStrel
         */
        LINE_DIAG_DOWN("Line 135 degrees");
        
        private final String label;
        
        private Shape(String label) 
        {
            this.label = label;
        }
        
        /**
         * @return the label associated to this shape.
         */
        public String toString()
        {
            return this.label;
        }
        
        /**
         * Creates a structuring element of the given type and with the
         * specified radius. The final size is given by 2 * radius + 1, to
         * take into account the central pixel.
         * 
         * @param radius the radius of the structuring element, in pixels
         * @return a new structuring element
         * 
         */
        public Strel fromRadius(int radius)
        {
            if (this == DISK) 
                return DiskStrel.fromRadius(radius);
            return fromDiameter(2 * radius + 1);
        }
        
        /**
         * Creates a structuring element of the given type and with the
         * specified diameter.
         * @param diam the orthogonal diameter of the structuring element (max of x and y sizes), in pixels
         * @return a new structuring element
         */
        public Strel fromDiameter(int diam) 
        {
            if (this == DISK) 
                return DiskStrel.fromDiameter(diam);
            if (this == SQUARE) 
                return new SquareStrel(diam);
            if (this == DIAMOND) {
                if (diam == 3)
                    return new Cross3x3Strel();
                return new DiamondStrel(diam);
            }
            if (this == OCTAGON) 
                return new OctagonStrel(diam);
            if (this == LINE_HORIZ) 
                return new LinearHorizontalStrel(diam);
            if (this == LINE_VERT) 
                return new LinearVerticalStrel(diam);
            if (this == LINE_DIAG_UP) 
                return new LinearDiagUpStrel(diam);
            if (this == LINE_DIAG_DOWN) 
                return new LinearDiagDownStrel(diam);
            
            throw new IllegalArgumentException("No default method for creating element of type " + this.label);
        }
        
        /**
         * Returns a set of labels for most of classical structuring elements.
         * 
         * @return a list of labels
         */
        public static String[] getAllLabels()
        {
            // array of all Strel types
            Shape[] values = Shape.values();
            int n = values.length;
            
            // keep all values but the last one ("Custom")
            String[] result = new String[n];
            for (int i = 0; i < n; i++)
                result[i] = values[i].label;
            
            return result;
        }
        
        /**
         * Determines the strel shape from its label.
         * 
         * @param label
         *            the shape name of the structuring element
         * @return a new Shape instance that can be used to create structuring
         *         elements
         * @throws IllegalArgumentException
         *             if label is not recognized.
         */
        public static Shape fromLabel(String label)
        {
            if (label != null)
                label = label.toLowerCase();
            for (Shape type : Shape.values()) 
            {
                if (type.label.toLowerCase().equals(label))
                    return type;
            }
            throw new IllegalArgumentException("Unable to parse Strel.Shape with label: " + label);
        }
    }
    
    
    Shape shape = Shape.DISK;
    int radius = 2;


    @Override
    public void populateDialog(GenericDialog gd, String label)
    {
        gd.addChoice("Shape", Strel.Shape.getAllLabels(), 
                this.shape.toString());
        gd.addNumericField("Radius (in pixels)", this.radius, 0);
    }

    @Override
    public Strel parseValue(GenericDialog gd)
    {
        this.shape  = Shape.fromLabel(gd.getNextChoice());
        this.radius = (int) gd.getNextNumber();
        
        // Create structuring element of the given size
        Strel strel = shape.fromRadius(radius);
        return strel;
    }
    
}
