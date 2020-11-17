package inra.ijpb.measure;

import ij.measure.ResultsTable;

import java.util.*;
import java.util.stream.IntStream;

public class ResultsChildAndParentMerger
{
	private final ResultsTable childTable;
	private final String childName;
	private final String childTableParentLabelColumn;
	private final ResultsTable parentTable;
	private final HashMap< Integer, Map< String, List< Double > > > parentToFeatureToMeasurements;
	private final HashMap< Integer, Integer > parentToRowIndex;

	public enum AggregationMode
	{
		Mean,
		Sum,
		Max,
		Min
	}

	public ResultsChildAndParentMerger( ResultsTable childTable, String childName, String parentLabelColumn, ResultsTable parentTable )
	{
		this.childTable = childTable;
		this.childName = childName;
		this.childTableParentLabelColumn = parentLabelColumn;
		this.parentTable = parentTable;

		parentToRowIndex = initParentToRowIndex();
		parentToFeatureToMeasurements = initFeatureMap();
		populateFeatureMap( parentToFeatureToMeasurements );
	}

	public ResultsTable appendToParentTable( AggregationMode mode )
	{
		parentToFeatureToMeasurements.keySet().stream().forEach( parent ->
		{
			parentToFeatureToMeasurements.get( parent ).keySet().stream().forEach( measurement ->
			{
				DoubleSummaryStatistics statistics = parentToFeatureToMeasurements.get( parent ).get( measurement ).stream().mapToDouble( x -> x ).summaryStatistics();
				Integer row = parentToRowIndex.get( parent );

				if ( measurement.equals( "Label" ) )
				{
					final String column = childName + "_" + "Count";
					parentTable.setValue( column, row, statistics.getCount() );
				}
				else
				{
					final String column = "" + childName + "_" + mode + "_" + measurement;

					switch ( mode )
					{
						case Mean:
							parentTable.setValue( column, row, statistics.getAverage() );
						case Sum:
							parentTable.setValue( column, row, statistics.getSum() );
						case Max:
							parentTable.setValue( column, row, statistics.getMax() );
						case Min:
							parentTable.setValue( column, row, statistics.getMin() );
					}
				}
			} );
		} );

		return parentTable;
	}

	private HashMap< Integer, Map< String, List< Double > > > initFeatureMap()
	{
		HashMap< Integer, Map< String, List< Double > > > parentToFeatureToMeasurements = new HashMap<>();
		IntStream.range( 0, parentTable.size() ).forEach( row ->
		{
			final HashMap< String, List< Double > > featureToMeasurements = new HashMap<>();
			parentToFeatureToMeasurements.put( Integer.parseInt( parentTable.getLabel( row ) ), featureToMeasurements );
			Arrays.stream( childTable.getHeadings() ).forEach( column -> featureToMeasurements.put( column, new ArrayList<>() ) );
		} );

		return parentToFeatureToMeasurements;
	}

	private void populateFeatureMap( HashMap< Integer, Map< String, List< Double > > > parentToFeatureToMeasurements )
	{
		Arrays.stream( childTable.getHeadings() ).forEach( column -> {
			IntStream.range( 0, childTable.size() ).forEach( rowIndex ->
			{
				final int parentIndex = ( int ) childTable.getValue( childTableParentLabelColumn, rowIndex );

				if ( parentIndex != 0 )
				{
					if ( column.equals( "Label" ) )
					{
						parentToFeatureToMeasurements.get( parentIndex ).get( column ).add( 1.0D );
					}
					else
					{
						final double measurement = childTable.getValue( column, rowIndex );
						parentToFeatureToMeasurements.get( parentIndex ).get( column ).add( measurement );
					}
				}
				else
				{
					// child object that does not reside within any parent object
				}
			} );
		} );
	}

	private HashMap< Integer, Integer > initParentToRowIndex()
	{
		HashMap< Integer, Integer > parentLabelToRowIndex = new HashMap<>();
		IntStream.range( 0, parentTable.size() ).forEach( row ->
		{
			parentLabelToRowIndex.put( Integer.parseInt( parentTable.getLabel( row ) ), row );
		});

		return parentLabelToRowIndex;
	}
}