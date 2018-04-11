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
	
	private ResultsTable allResults;
	
	public ResultsBuilder() { this.allResults = new ResultsTable();	}
	public ResultsBuilder(ResultsTable rt) { this.allResults = rt; }
	
	public ResultsBuilder addResult (ResultsTable rt) {
		// Keep the label and everything in the same order as before, but just append whatever columns do not exist yet
		if(allResults.size() == rt.size() ) {
			for(int c=0; c<=rt.getLastColumn(); c++) {
				String colName = rt.getColumnHeading(c);
				if( !allResults.columnExists(colName)) {
					for(int i=0; i<rt.getCounter(); i++) {
						allResults.setValue(colName, i, rt.getValue(colName, i)); // Currently only supports numbered results...
						allResults.updateResults();
					}
				}
			}
		} else { // Overwrite
			this.allResults = rt;
		}
		
		return this;
	}
	
	public ResultsTable getResultsTable() {
		return this.allResults;
	}
}
