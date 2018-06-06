package inra.ijpb.measure;

import ij.measure.ResultsTable;

/**
 * Builder-type class to hold results
 * Basically it will keep appending results to a larger results table
 * Built in a way where we can chain them together
 * @author oburri
 *
 */

public class ResultsBuilder {
	/** Results table with all the accumulated results */
	private ResultsTable allResults;
	
	/**
	 * Basic constructor. It creates a default empty table.
	 */
	public ResultsBuilder() { this.allResults = new ResultsTable();	}
	/**
	 * Constructs a ResultsBuilder initializing the default table to the input one.
	 * @param rt initial results table
	 */
	public ResultsBuilder(ResultsTable rt) { this.allResults = rt; }
	
	/**
	 * Add a results table to the already existing table.
	 * @param rt table to add
	 * @return current results builder
	 */
	public ResultsBuilder addResult (ResultsTable rt) {
		// Keep the label and everything in the same order as before, but just append whatever columns do not exist yet
		if(allResults.size() == rt.size() ) {
			for(int c=0; c<=rt.getLastColumn(); c++) {
				String colName = rt.getColumnHeading(c);
				if( !allResults.columnExists(colName)) {
					for(int i=0; i<rt.getCounter(); i++) {
						allResults.setValue(colName, i, rt.getValue(colName, i)); // Currently only supports numbered results...
					}
				}
			}
		} else { // Overwrite
			this.allResults = rt;
		}
		
		return this;
	}
	
	/**
	 * Get the current results table.
	 * @return current results table
	 */
	public ResultsTable getResultsTable() {
		return this.allResults;
	}
}
