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
package inra.ijpb.measure;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ij.measure.ResultsTable;

public class ResultsBuilderTest {

	@Test
	public final void testAppendResults() 
	{
		ResultsTable rt1 = new ResultsTable();
		ResultsTable rt2 = new ResultsTable();
		ResultsTable rt3 = new ResultsTable();
		
		ResultsTable rt_final = new ResultsTable();
		
		
		// Populate Results
		for (int i=0; i<20; i++)
		{
			rt1.incrementCounter();
			rt2.incrementCounter();
			rt3.incrementCounter();
			rt_final.incrementCounter();
			
			rt1.setLabel("Row #"+(i+1), i);
			rt2.setLabel("Row #"+(i+1), i);
			rt3.setLabel("Row #"+(i+1), i);
			rt_final.setLabel("Row #"+(i+1), i);
			
			rt1.addValue("Metric 1", (i+1)* 10);
			rt2.addValue("Metric 2", (i+1)* 100);
			rt3.addValue("Metric 3", (i+1)* 1000);
			
			rt_final.addValue("Metric 1", (i+1)* 10);
			rt_final.addValue("Metric 2", (i+1)* 100);
			rt_final.addValue("Metric 3", (i+1)* 1000);
			
		}
		
		ResultsTable built_results = new ResultsBuilder().addResult(rt3).addResult(rt2).addResult(rt1).getResultsTable();
		
		// Check equal sizes
		assertEquals(built_results.size(), rt1.size());
		
		assertEquals(built_results.getLastColumn(), rt_final.getLastColumn());
				
		for (int i=0; i<20; i++)
		{
			assertEquals(built_results.getValue("Metric 1", i), rt1.getValue("Metric 1", i), 0.0);
			assertEquals(built_results.getValue("Metric 2", i), rt2.getValue("Metric 2", i), 0.0);
			assertEquals(built_results.getValue("Metric 3", i), rt3.getValue("Metric 3", i), 0.0);
		}
	}
}
