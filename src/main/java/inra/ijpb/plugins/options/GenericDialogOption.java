/**
 * 
 */
package inra.ijpb.plugins.options;

import ij.gui.GenericDialog;

/**
 * An interface for facilitating the usage of GenericDialog instances.
 * 
 * Specializations of this interface can populate a dialog with one or several
 * widgets. When the <code>parseValue</code> method is called, the user input(s)
 * are automatically converted into an instance of the class defined by the type
 * <code>T</code>.
 * 
 * @author dlegland
 *
 */
public interface GenericDialogOption<T>
{
    /**
     * Updates the generic dialog by adding a widget for choosing the
     * appropriate option.
     * 
     * @param gd
     *            the GenericDialog to update
     * @param label
     *            the label associated to the widget
     */
    public void populateDialog(GenericDialog gd, String label);
    
    /**
     * Retrieve the value selected by the user on the GenericDialog.
     * 
     * @param gd
     *            the GenericDialgo used for selection option
     * @return the value of the option chosen by the user.
     */
    public T parseValue(GenericDialog gd);
}
