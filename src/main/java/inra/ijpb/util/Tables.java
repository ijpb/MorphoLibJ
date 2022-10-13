/**
 * 
 */
package inra.ijpb.util;

import ij.measure.ResultsTable;

/**
 * A collection of static methods for manipulating tables.
 * 
 * @author dlegland
 */
public class Tables
{
    /**
     * Concatenate all the tables vertically. All the tables are epected to have
     * the same column labels.
     * 
     * @param tables
     *            the tables to concatenate
     * @return the result of the concatenation of the tables in the vertical
     *         direction
     */
    public static final ResultsTable concatenateV(ResultsTable... tables)
    {
        // check empty input
        if (tables.length == 0)
        {
            throw new RuntimeException("Requires at least one table");
        }
        
        // initialize result table with first table
        ResultsTable firstTable = tables[0];
        int nRows = firstTable.getCounter();
        ResultsTable res = new ResultsTable(nRows);
        for (int iRow = 0; iRow < nRows; iRow++)
        {
            String label = firstTable.getLabel(iRow);
            if (label != null)
            {
                res.setLabel(label, iRow);
            }
            
            for (String colName : firstTable.getHeadings())
            {
                res.setValue(colName, iRow, firstTable.getValue(colName, iRow));
            }
        }
        
        // process remaining tables
        for (int iTable = 1; iTable < tables.length; iTable++)
        {
            ResultsTable table = tables[iTable];    
            
            nRows = table.getCounter();
            for (int iRow = 0; iRow < nRows; iRow++)
            {
                res.incrementCounter();
                String label = table.getLabel(iRow);
                if (label != null)
                {
                    res.setLabel(label, res.getCounter());
                }

                for (String colName : table.getHeadings())
                {
                    res.addValue(colName, table.getValue(colName, iRow));
                }
            }
        }
        
        return res;
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private Tables()
    {
    }
}
