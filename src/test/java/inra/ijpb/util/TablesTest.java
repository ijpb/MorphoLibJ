/**
 * 
 */
package inra.ijpb.util;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.measure.ResultsTable;

/**
 * 
 */
public class TablesTest
{
	/**
	 * Test method for {@link inra.ijpb.util.Tables#appendColumns(ij.measure.ResultsTable, ij.measure.ResultsTable[])}.
	 */
	@Test
	public final void testAppendColumns()
	{
		// initializes two tables, with two columns each, and a common column
		double[] ti = new double[] {0.1, 0.2, 0.3, 0.4, 0.5};
		ResultsTable table1 = new ResultsTable();
		ResultsTable table2 = new ResultsTable();
		for (int i = 0; i < ti.length; i++)
		{
			table1.incrementCounter();
			table1.addValue("t", ti[i]);
			table1.addValue("cos_t", Math.cos(ti[i]));
			table2.incrementCounter();
			table2.addValue("t", ti[i]);
			table2.addValue("sin_t", Math.sin(ti[i]));
		}
		
		ResultsTable res = Tables.appendColumns(table1, table2);
		
		assertEquals(5, res.getCounter());
		assertEquals(2, res.getLastColumn());
		assertTrue(res.columnExists("t"));
		assertTrue(res.columnExists("cos_t"));
		assertTrue(res.columnExists("sin_t"));
	}

	/**
	 * Test method for {@link inra.ijpb.util.Tables#appendColumns(ij.measure.ResultsTable, ij.measure.ResultsTable[])}.
	 */
	@Test (expected = IllegalArgumentException.class)
	public final void testAppendColumns_differentRowCounts()
	{
		// initializes two tables, with two columns each, and a common column
		double[] ti = new double[] {0.1, 0.2, 0.3, 0.4, 0.5};
		ResultsTable table1 = new ResultsTable();
		ResultsTable table2 = new ResultsTable();
		for (int i = 0; i < ti.length; i++)
		{
			table1.incrementCounter();
			table1.addValue("t", ti[i]);
			table1.addValue("cos_t", Math.cos(ti[i]));
			table2.incrementCounter();
			table2.addValue("t", ti[i]);
			table2.addValue("sin_t", Math.sin(ti[i]));
		}
		// adds an extra value to table 2
		table2.incrementCounter();
		table2.addValue("t", 0.6);
		table2.addValue("sin_t", Math.sin(0.6));
		
		Tables.appendColumns(table1, table2);
	}
}
