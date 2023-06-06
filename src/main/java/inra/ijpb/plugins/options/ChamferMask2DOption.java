/**
 * 
 */
package inra.ijpb.plugins.options;

import ij.gui.GenericDialog;
import inra.ijpb.binary.distmap.ChamferMask2D;

/**
 * <p>
 * Example of use:
 * 
 * <pre><code>
 *    // setup option
 *    ChamferMask2DOption maskOption = new ChamferMask2DOption(ChamferMask2D.ChessKnight);
 *    
 *    // Create dialog with Chess Knight chamfer mask as default
 *    GenericDialog gd = new GenericDialog("Sample Dialog");
 *    maskOption.populateDialog(gd, "Chamfer Mask");
 *    
 *    // wait for user input
 *    gd.showDialog();
 *    if (gd.wasCanceled())
 *        return;
 *    
 *    // retrieve parameters
 *    ChamferMask2D chamferMask = maskOption.parseValue(gd);
 * </code></pre>
 * 
 * @author dlegland
 */
public class ChamferMask2DOption implements GenericDialogOption<ChamferMask2D>
{
    private enum Options
    {
        /** Use weight equal to 1 for all neighbors */
        CHESSBOARD("Chessboard (1,1)", ChamferMask2D.CHESSBOARD), 
        /**
         * Use weights 1 for orthogonal neighbors and 2 for diagonal neighbors,
         * and 3 for cube-diagonals.
         */
        CITY_BLOCK("City-Block (1,2)", ChamferMask2D.CITY_BLOCK),
        /**
         * Use floating-point weights 1.0 for orthogonal neighbors and sqrt(2) for
         * diagonal neighbors, and sqrt(3) for cube-diagonals.
         * 
         * Use 10, 14 and 17 for the 16-bits integer version.
         */
        QUASI_EUCLIDEAN("Quasi-Euclidean (1,1.41)", ChamferMask2D.QUASI_EUCLIDEAN), 
        /**
         * Use weights 3 for orthogonal neighbors and 4 for diagonal neighbors,
         * and 5 for cube-diagonals (best approximation for 3-by-3-by-3 masks).
         */
        BORGEFORS("Borgefors (3,4)", ChamferMask2D.BORGEFORS),
        /**
         * Use weights 5 for orthogonal neighbors and 7 for diagonal neighbors, and
         * 11 for chess-knight moves (best approximation for 5-by-5 masks).
         */
        CHESSKNIGHT("Chessknight (5,7,11)", ChamferMask2D.CHESSKNIGHT),
        /**
         * Chamfer mask in the 7-by-7 neighborhood defined using the four weights
         * 12, 17, 27, and 38.
         */
        VERWER("Verwer (12,17,27,38)", ChamferMask2D.VERWER);


        private final String label;
        private final ChamferMask2D mask;

        private Options(String label, ChamferMask2D mask)
        {
            this.label = label;
            this.mask = mask;
        }
    }
    
    ChamferMask2D defaultValue = ChamferMask2D.CHESSKNIGHT;
    
    public ChamferMask2DOption()
    {
        this(ChamferMask2D.CHESSKNIGHT);
    }
    
    public ChamferMask2DOption(ChamferMask2D defaultValue)
    {
        this.defaultValue = defaultValue;
    }
    
    @Override
    public void populateDialog(GenericDialog gd, String label)
    {
       populateDialog(gd, label, defaultValue);
    }

    public void populateDialog(GenericDialog gd, String label, ChamferMask2D defaultValue)
    {
        String[] optionLabels = computeOptionLabels();
        String defaultLabel = getOptionLabel(defaultValue);
        gd.addChoice(label, optionLabels, defaultLabel);
    }
    
    private String[] computeOptionLabels()
    {
        String[] optionLabels = new String[Options.values().length];
        int i = 0;
        for (Options option : Options.values())
        {
            optionLabels[i++] = option.label;
        }
        return optionLabels;
    }
    
    private String getOptionLabel(ChamferMask2D value)
    {
        for (Options option : Options.values())
        {
            if (option.mask == value)
            {
                return option.label;
            }
        }
        throw new IllegalArgumentException(
                "Unable to identify value in the list of available options");
    }
    
    @Override
    public ChamferMask2D parseValue(GenericDialog gd)
    {
        String label = gd.getNextChoice();
        for (Options option : Options.values())
        {
            if (option.label.equalsIgnoreCase(label))
                return option.mask;
        }
        throw new IllegalArgumentException(
                "Unable to parse ChamferMask2D option with label: " + label);
    }
}
