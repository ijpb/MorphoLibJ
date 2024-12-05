/**
 * 
 */
package inra.ijpb.util;

import ij.measure.ResultsTable;

/**
 * A collection of utility methods for managing ResultsTable.
 */
public class Tables
{
	/**
	 * Appends the columns of all the table to the first table, and returns the
	 * concatenated table. All tables must have the same number of rows.
	 * 
	 * @param refTable
	 *            the table used for concatenation
	 * @param tables
	 *            the tables to concatenate (will not be modified)
	 * @return the reference to refTable
	 */
	public static final ResultsTable appendColumns(ResultsTable refTable, ResultsTable... tables)
	{
		// iterate over tables to concatenate
		for (ResultsTable table : tables)
		{
			if (table.size() != refTable.size())
			{
				throw new IllegalArgumentException("All tables must have same number of rows");
			}
			
			// iterate over columns of current table
			for (int c = 0; c <= table.getLastColumn(); c++)
			{
				String colName = table.getColumnHeading(c);
				
				// do not process columns that already exist
				if (refTable.columnExists(colName))
				{
					continue;
				}
				
				for (int r = 0; r < table.getCounter(); r++)
				{
					refTable.setValue(colName, r, table.getValue(colName, r));
				}
			}
		}
		
		return refTable;
	}

	/**
	 * Private constructor to prevent instantiation. 
	 */
	private Tables()
	{
	}
}
