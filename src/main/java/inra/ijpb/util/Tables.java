/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2026 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
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
	 * Adds a numeric column given by an array of double values to the specified
	 * table. If the column already exists within the table, its values are
	 * updated.
	 * 
	 * @param table
	 *            The instance of ResultsTable to update
	 * @param colName
	 *            the name of the column.
	 * @param values
	 *            the values contained within the column.
	 */
	public static final void addColumnToTable(ResultsTable table, String colName, double[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			table.setValue(colName, i, values[i]);
		}
	}

	/**
	 * Adds a numeric column given by an array of integer values to the specified
	 * table. If the column already exists within the table, its values are
	 * updated.
	 * 
	 * @param table
	 *            The instance of ResultsTable to update
	 * @param colName
	 *            the name of the column.
	 * @param values
	 *            the values contained within the column.
	 */
	public static final void addColumnToTable(ResultsTable table, String colName, int[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			table.setValue(colName, i, values[i]);
		}
	}

	/**
	 * Private constructor to prevent instantiation. 
	 */
	private Tables()
	{
	}
}
