/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 * Created on Mar 5, 2004
 */
package edu.uci.ics.graph.predicates;

import edu.uci.ics.graph.Graph;



/**
 * @author Tom Nelson
 */
public interface GraphPredicate<V,E> {

    boolean evaluateEdge(Graph<V,E> graph, E edge);
    
    boolean evaluateVertex(Graph<V,E> graph, V vertex);

}