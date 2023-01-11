/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2023 INRA.
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
