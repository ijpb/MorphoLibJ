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
