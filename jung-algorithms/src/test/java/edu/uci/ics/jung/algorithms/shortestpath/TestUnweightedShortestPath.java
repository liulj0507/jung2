/*
 * Created on Aug 22, 2003
 *
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.Factory;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.uci.ics.jung.algorithms.Indexer;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * @author Scott White
 */
public class TestUnweightedShortestPath extends TestCase
{
    private Factory<String> vertexFactory =
    	new Factory<String>() {
    	int count = 0;
    	public String create() {
    		return "V"+count++;
    	}};
    	
     private Factory<Integer> edgeFactory =
        	new Factory<Integer>() {
        	int count = 0;
        	public Integer create() {
        		return count++;
        	}};
    BidiMap<String,Integer> id;

    protected void setUp() {
    }
	public static Test suite()
	{
		return new TestSuite(TestUnweightedShortestPath.class);
	}
	
	public void testUndirected() {
		UndirectedGraph<String,Integer> ug = 
			new UndirectedSparseGraph<String,Integer>();
		for(int i=0; i<5; i++) {
			ug.addVertex(vertexFactory.create());
		}
		id = Indexer.<String>create(ug.getVertices());

//		GraphUtils.addVertices(ug,5);
//		Indexer id = Indexer.getIndexer(ug);
		ug.addEdge(edgeFactory.create(), id.getKey(0), id.getKey(1));
		ug.addEdge(edgeFactory.create(), id.getKey(1), id.getKey(2));
		ug.addEdge(edgeFactory.create(), id.getKey(2), id.getKey(3));
		ug.addEdge(edgeFactory.create(), id.getKey(0), id.getKey(4));
		ug.addEdge(edgeFactory.create(), id.getKey(4), id.getKey(3));
		
		UnweightedShortestPath<String,Integer> usp = 
			new UnweightedShortestPath<String,Integer>(ug);
		Assert.assertEquals(usp.getDistance(id.getKey(0),id.getKey(3)).intValue(),2);
		Assert.assertEquals(((Number) usp.getDistanceMap(id.getKey(0)).get(id.getKey(3))).intValue(),2);
		Assert.assertNull(usp.getIncomingEdgeMap(id.getKey(0)).get(id.getKey(0)));
		Assert.assertNotNull(usp.getIncomingEdgeMap(id.getKey(0)).get(id.getKey(3)));
	}
	
	public void testDirected() {
			DirectedGraph<String,Integer> dg = 
				new DirectedSparseGraph<String,Integer>();
			for(int i=0; i<5; i++) {
				dg.addVertex(vertexFactory.create());
			}
			id = Indexer.<String>create(dg.getVertices());
			dg.addEdge(edgeFactory.create(), id.getKey(0), id.getKey(1));
			dg.addEdge(edgeFactory.create(), id.getKey(1), id.getKey(2));
			dg.addEdge(edgeFactory.create(), id.getKey(2), id.getKey(3));
			dg.addEdge(edgeFactory.create(), id.getKey(0), id.getKey(4));
			dg.addEdge(edgeFactory.create(), id.getKey(4), id.getKey(3));
			dg.addEdge(edgeFactory.create(), id.getKey(3), id.getKey(0));
		
			UnweightedShortestPath<String,Integer> usp = 
				new UnweightedShortestPath<String,Integer>(dg);
			Assert.assertEquals(usp.getDistance(id.getKey(0),id.getKey(3)).intValue(),2);
			Assert.assertEquals(((Number) usp.getDistanceMap(id.getKey(0)).get(id.getKey(3))).intValue(),2);
			Assert.assertNull(usp.getIncomingEdgeMap(id.getKey(0)).get(id.getKey(0)));
			Assert.assertNotNull(usp.getIncomingEdgeMap(id.getKey(0)).get(id.getKey(3)));

		}
}