package inra.ijpb.label;

import static org.junit.Assert.*;

import java.util.Set;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import inra.ijpb.label.RegionAdjacencyGraph.LabelPair;

import org.junit.Test;

public class RegionAdjacencyGraphTest {

	@Test
	public void testRegionAdjacencyGraphImageProcessor_FiveRegions() 
	{
		byte[] data = new byte[]{
				1, 1, 1, 0, 2, 2, 2, 
				1, 1, 0, 5, 0, 2, 2, 
				1, 0, 5, 5, 5, 0, 2, 
				0, 5, 5, 5, 5, 5, 0,
				3, 0, 5, 5, 5, 0, 4, 
				3, 3, 0, 5, 0, 4, 4, 
				3, 3, 3, 0, 4, 4, 4
		};
		ImageProcessor image = new ByteProcessor(7, 7, data);
		
		Set<RegionAdjacencyGraph.LabelPair> adjacencies =
				RegionAdjacencyGraph.computeAdjacencies(image);
		assertEquals(8, adjacencies.size());
		
		assertTrue(adjacencies.contains(new LabelPair(1, 2)));
		assertTrue(adjacencies.contains(new LabelPair(1, 3)));
		assertFalse(adjacencies.contains(new LabelPair(1, 4)));
		assertTrue(adjacencies.contains(new LabelPair(1, 5)));
		assertFalse(adjacencies.contains(new LabelPair(2, 3)));
		assertTrue(adjacencies.contains(new LabelPair(2, 4)));
		assertTrue(adjacencies.contains(new LabelPair(2, 5)));
		assertTrue(adjacencies.contains(new LabelPair(3, 4)));
		assertTrue(adjacencies.contains(new LabelPair(3, 5)));
		assertTrue(adjacencies.contains(new LabelPair(4, 5)));
	}

}
