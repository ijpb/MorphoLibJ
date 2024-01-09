/**
 * 
 */
package inra.ijpb.plugins.options;

import ij.gui.GenericDialog;

/**
 * 
 */
public class BooleanOption implements GenericDialogOption<Boolean>
{
    String title;
    boolean state;
    
    public BooleanOption(String title, boolean defaultState)
    {
        this.title = title;
        this.state = defaultState;
    }

    @Override
    public void populateDialog(GenericDialog gd, String label)
    {
        gd.addCheckbox(label, state);
    }

    @Override
    public Boolean parseValue(GenericDialog gd)
    {
        this.state = gd.getNextBoolean();
        return state;
    }
    
}
