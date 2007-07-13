/*
 * Created on Jul 12, 2007
 *
 * Copyright (c) 2007, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.algorithms.scoring;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;

/**
 * 
 * @author Joshua O'Madadhain
 */
public class PageRank<V,E> extends PageRankWithPriors<V,E>
{

    /**
     * @param graph
     * @param vertex_priors
     * @param output_map
     * @param alpha
     */
    public PageRank(Graph<V,E> graph, Transformer<?, ? extends Number> edge_weight, double alpha)
    {
        super(graph, ScoringUtils.getUniformPrior(graph), alpha);
    }

    /**
     * @param graph
     * @param vertex_priors
     * @param alpha
     */
    public PageRank(Graph<V,E> graph, double alpha)
    {
        super(graph, ScoringUtils.getUniformPrior(graph), alpha);
    }

}