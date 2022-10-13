/**
 * 
 */
package inra.ijpb.util;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.measure.ResultsTable;

/**
 * @author dlegland
 *
 */
public class TablesTest
{
    
    /**
     * Test method for {@link inra.ijpb.util.Tables#concatenateV(ij.measure.ResultsTable[])}.
     */
    @Test
    public final void testConcatenateV()
    {
        ResultsTable tab1 = new ResultsTable(5);
        tab1.setValue("Vol", 0, 10);
        tab1.setValue("Surf.", 0, 20);
        tab1.setValue("Nb", 0, 3);
        
        ResultsTable tab2 = new ResultsTable(4);
        tab2.setValue("Vol", 0, 30);
        tab2.setValue("Surf.", 0, 50);
        tab2.setValue("Nb", 0, 4);
        
        ResultsTable res = Tables.concatenateV(tab1, tab2);
        
        assertEquals(9, res.getCounter());
        assertEquals(3, res.getHeadings().length);
        
        assertEquals(10, res.getValueAsDouble(0, 0), 0.01);
        assertEquals(30, res.getValueAsDouble(0, 5), 0.01);
        assertEquals(4, res.getValueAsDouble(2, 5), 0.01);
    }
    
}
