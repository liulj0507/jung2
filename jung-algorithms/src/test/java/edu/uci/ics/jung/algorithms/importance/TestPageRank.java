/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package edu.uci.ics.jung.algorithms.importance;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.util.NumericalPrecision;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * @author Scott White
 * @author Tom Nelson - adapted to jung2
 */
public class TestPageRank extends TestCase {
	
	private Map<Integer,Number> edgeWeights;
	private DirectedGraph<Integer,Integer> graph;
	private Factory<Integer> edgeFactory;
	
    public static Test suite() {
        return new TestSuite(TestPageRank.class);
    }

    protected void setUp() {
    	edgeWeights = new HashMap<Integer,Number>();
    	edgeFactory = new Factory<Integer>() {
    		int i=0;
			public Integer create() {
				return i++;
			}};
    }

    private void addEdge(Graph G, Integer v1, Integer v2, double weight) {
    	Integer edge = edgeFactory.create();
    	graph.addEdge(edge, v1, v2);
    	edgeWeights.put(edge, weight);
    }

    public void testRanker() {
    	graph = new DirectedSparseGraph<Integer,Integer>();
    	for(int i=0; i<4; i++) {
    		graph.addVertex(i);
    	}
        addEdge(graph,0,1,1.0);
        addEdge(graph,1,2,1.0);
        addEdge(graph,2,3,0.5);
        addEdge(graph,3,1,1.0);
        addEdge(graph,2,1,0.5);

        PageRank<Integer,Integer> ranker = new PageRank<Integer,Integer>(graph,0,edgeWeights);
        ranker.setMaximumIterations(500);
        ranker.evaluate();

        Assert.assertTrue(NumericalPrecision.equal(((Ranking)ranker.getRankings().get(0)).rankScore,0.4,.001));
        Assert.assertTrue(NumericalPrecision.equal(((Ranking)ranker.getRankings().get(1)).rankScore,0.4,.001));
        Assert.assertTrue(NumericalPrecision.equal(((Ranking)ranker.getRankings().get(2)).rankScore,0.2,.001));
        Assert.assertTrue(NumericalPrecision.equal(((Ranking)ranker.getRankings().get(3)).rankScore,0,.001));
    }
}
