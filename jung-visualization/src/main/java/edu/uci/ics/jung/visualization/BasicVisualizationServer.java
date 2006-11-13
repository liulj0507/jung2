/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package edu.uci.ics.jung.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.Layout;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.transform.LayoutTransformer;
import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.Transformer;
import edu.uci.ics.jung.visualization.transform.ViewTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;

/**
 * A class that maintains many of the details necessary for creating 
 * visualizations of graphs.
 * 
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 * @author Danyel Fisher
 */
@SuppressWarnings("serial")
public class BasicVisualizationServer<V, E> extends JPanel 
                implements Transformer, LayoutTransformer, ViewTransformer, 
                ChangeListener, ChangeEventSupport, VisualizationServer<V, E>{

    protected ChangeEventSupport changeSupport =
        new DefaultChangeEventSupport(this);
    
    protected Map<V,Point2D> locationMap = new HashMap<V,Point2D>();

    /**
     * holds the state of this View
     */
    protected VisualizationModel<V,E> model;

	/**
	 * handles the actual drawing of graph elements
	 */
	protected Renderer<V,E> renderer = new BasicRenderer<V,E>();
	
	/**
	 * rendering hints used in drawing. Anti-aliasing is on
	 * by default
	 */
	protected Map renderingHints = new HashMap();
		
	/**
	 * pluggable support for picking graph elements by
	 * finding them based on their coordinates. Typically
	 * used in mouse events.
	 */
	protected GraphElementAccessor<V, E> pickSupport;
	
	/**
	 * holds the state of which vertices of the graph are
	 * currently 'picked'
	 */
	protected PickedState<V> pickedVertexState;
	
	/**
	 * holds the state of which edges of the graph are
	 * currently 'picked'
	 */
    protected PickedState<E> pickedEdgeState;
    
    /**
     * a listener used to cause pick events to result in
     * repaints, even if they come from another view
     */
    protected ItemListener pickEventListener;
	
	/**
	 * an offscreen image to render the graph
	 * Used if doubleBuffered is set to true
	 */
	protected BufferedImage offscreen;
	
	/**
	 * graphics context for the offscreen image
	 * Used if doubleBuffered is set to true
	 */
	protected Graphics2D offscreenG2d;
	
	/**
	 * user-settable choice to use the offscreen image
	 * or not. 'false' by default
	 */
	protected boolean doubleBuffered;
	
    /**
     * Provides support for mutating the AffineTransform that
     * is supplied to the rendering Graphics2D
     */
    protected MutableTransformer viewTransformer = 
        new MutableAffineTransformer(new AffineTransform());
    
    protected MutableTransformer layoutTransformer =
        new MutableAffineTransformer(new AffineTransform());
    
	/**
	 * a collection of user-implementable functions to render under
	 * the topology (before the graph is rendered)
	 */
	protected List<Paintable> preRenderers = new ArrayList<Paintable>();
	
	/**
	 * a collection of user-implementable functions to render over the
	 * topology (after the graph is rendered)
	 */
	protected List<Paintable> postRenderers = new ArrayList<Paintable>();
	
    protected RenderContext<V,E> renderContext = new PluggableRenderContext<V,E>();
    
    /**
     * Create an instance with passed parameters.
     * 
     * @param layout		The Layout to apply, with its associated Graph
     * @param renderer		The Renderer to draw it with
     */
	public BasicVisualizationServer(Layout<V,E> layout) {
	    this(new DefaultVisualizationModel<V,E>(layout));
	}
	
    /**
     * Create an instance with passed parameters.
     * 
     * @param layout		The Layout to apply, with its associated Graph
     * @param renderer		The Renderer to draw it with
     * @param preferredSize the preferred size of this View
     */
	public BasicVisualizationServer(Layout<V,E> layout, Dimension preferredSize) {
	    this(new DefaultVisualizationModel<V,E>(layout, preferredSize), preferredSize);
	}
	
	/**
	 * Create an instance with passed parameters.
	 * 
	 * @param model
	 * @param renderer
	 */
	public BasicVisualizationServer(VisualizationModel<V,E> model) {
	    this(model, new Dimension(600,600));
	}
	/**
	 * Create an instance with passed parameters.
	 * 
	 * @param model
	 * @param renderer
	 * @param preferredSize initial preferred size of the view
	 */
	@SuppressWarnings("unchecked")
    public BasicVisualizationServer(VisualizationModel<V,E> model,
	        Dimension preferredSize) {
	    this.model = model;
//        renderContext.setScreenDevice(this);
	    model.addChangeListener(this);
	    setDoubleBuffered(false);
		this.addComponentListener(new VisualizationListener(this));

		setPickSupport(new ShapePickSupport<V,E>(this));
		setPickedVertexState(new MultiPickedState<V>());
		setPickedEdgeState(new MultiPickedState<E>());
        
        renderContext.setEdgeDrawPaintFunction(new PickableEdgePaintTransformer<V,E>(getPickedEdgeState(), Color.black, Color.cyan));
        renderContext.setVertexFillPaintFunction(new PickableVertexPaintTransformer<V>(getPickedVertexState(), 
                Color.red, Color.yellow));
        renderContext.setViewTransformer(viewTransformer);

//		setRenderer(renderer);
		
		setPreferredSize(preferredSize);
		renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        scaleToLayout(model.getGraphLayout().getCurrentSize());
        this.layoutTransformer.addChangeListener(this);
        this.viewTransformer.addChangeListener(this);
	}
	
	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setDoubleBuffered(boolean)
     */
	public void setDoubleBuffered(boolean doubleBuffered) {
	    this.doubleBuffered = doubleBuffered;
	}
	
	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#isDoubleBuffered()
     */
	public boolean isDoubleBuffered() {
	    return doubleBuffered;
	}
	
	/**
	 * Ensure that, if doubleBuffering is enabled, the offscreen
	 * image buffer exists and is the correct size.
	 * @param d
	 */
	protected void checkOffscreenImage(Dimension d) {
	    if(doubleBuffered) {
	        if(offscreen == null || offscreen.getWidth() != d.width || offscreen.getHeight() != d.height) {
	            offscreen = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
	            offscreenG2d = offscreen.createGraphics();
	        }
	    }
	}
	
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getModel()
     */
    public VisualizationModel<V,E> getModel() {
        return model;
    }
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setModel(edu.uci.ics.jung.visualization.VisualizationModel)
     */
    public void setModel(VisualizationModel<V,E> model) {
        this.model = model;
    }
	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#stateChanged(javax.swing.event.ChangeEvent)
     */
	public void stateChanged(ChangeEvent e) {
	    repaint();
	    fireStateChanged();
	}

	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setRenderer(edu.uci.ics.jung.visualization.Renderer)
     */
	public void setRenderer(Renderer<V,E> r) {
	    this.renderer = r;
	    repaint();
	}
	
	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getRenderer()
     */
	public Renderer<V,E> getRenderer() {
	    return renderer;
	}

	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setGraphLayout(edu.uci.ics.jung.visualization.layout.Layout)
     */
    public void setGraphLayout(Layout<V,E> layout) {
        setGraphLayout(layout, true);
    }
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setGraphLayout(edu.uci.ics.jung.visualization.layout.Layout, boolean)
     */
	public void setGraphLayout(Layout<V,E> layout, boolean scaleToLayout) {

	    Dimension viewSize = getSize();
	    if(viewSize.width <= 0 || viewSize.height <= 0) {
	        viewSize = getPreferredSize();
	    }
	    model.setGraphLayout(layout, viewSize);
        if(scaleToLayout) scaleToLayout(layout.getCurrentSize());
	}
    
    protected void scaleToLayout(Dimension layoutSize) {
        Dimension viewSize = getSize();
        if(viewSize.width == 0 || viewSize.height == 0) {
            viewSize = getPreferredSize();
        }
        float scalex = (float)viewSize.width/layoutSize.width;
        float scaley = (float)viewSize.height/layoutSize.height;
        float scale = 1;
        if(scalex - 1 < scaley - 1) {
        		scale = scalex;
        } else {
        		scale = scaley;
        }
        // set scale to show the entire graph layout
        viewTransformer.setScale(scale, scale, new Point2D.Float());
    }
	
	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getGraphLayout()
     */
	public Layout<V,E> getGraphLayout() {
	        return model.getGraphLayout();
	}
	
	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setVisible(boolean)
     */
	public void setVisible(boolean aFlag) {
		super.setVisible(aFlag);
		model.getGraphLayout().resize(this.getSize());
	}

	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#isVisRunnerRunning()
     */
	public boolean isVisRunnerRunning() {
	    return model.isVisRunnerRunning();
	}

	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#inverseTransform(java.awt.geom.Point2D)
     */
	public Point2D inverseTransform(Point2D p) {
	    return layoutTransformer.inverseTransform(inverseViewTransform(p));
	}
	
	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#inverseViewTransform(java.awt.geom.Point2D)
     */
	public Point2D inverseViewTransform(Point2D p) {
	    return viewTransformer.inverseTransform(p);
	}

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#inverseLayoutTransform(java.awt.geom.Point2D)
     */
    public Point2D inverseLayoutTransform(Point2D p) {
        return layoutTransformer.inverseTransform(p);
    }

	/* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#transform(java.awt.geom.Point2D)
     */
	public Point2D transform(Point2D p) {
	    // transform with vv transform
	    return viewTransformer.transform(layoutTransform(p));
	}
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#viewTransform(java.awt.geom.Point2D)
     */
    public Point2D viewTransform(Point2D p) {
        return viewTransformer.transform(p);
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#layoutTransform(java.awt.geom.Point2D)
     */
    public Point2D layoutTransform(Point2D p) {
        return layoutTransformer.transform(p);
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setViewTransformer(edu.uci.ics.jung.visualization.transform.MutableTransformer)
     */
    public void setViewTransformer(MutableTransformer transformer) {
        this.viewTransformer.removeChangeListener(this);
        this.viewTransformer = transformer;
        this.viewTransformer.addChangeListener(this);
        renderContext.setViewTransformer(transformer);
    }

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setLayoutTransformer(edu.uci.ics.jung.visualization.transform.MutableTransformer)
     */
    public void setLayoutTransformer(MutableTransformer transformer) {
        this.layoutTransformer.removeChangeListener(this);
        this.layoutTransformer = transformer;
        this.layoutTransformer.addChangeListener(this);
    }

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getViewTransformer()
     */
    public MutableTransformer getViewTransformer() {
        return viewTransformer;
    }

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getLayoutTransformer()
     */
    public MutableTransformer getLayoutTransformer() {
        return layoutTransformer;
    }

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getRenderingHints()
     */
    public Map getRenderingHints() {
        return renderingHints;
    }
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setRenderingHints(java.util.Map)
     */
    public void setRenderingHints(Map renderingHints) {
        this.renderingHints = renderingHints;
    }
    
	protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

	    checkOffscreenImage(getSize());
		model.start();

		Graphics2D g2d = (Graphics2D)g;
		if(doubleBuffered) {
			renderGraph(offscreenG2d);
		    g2d.drawImage(offscreen, null, 0, 0);
		} else {
		    renderGraph(g2d);
		}
	}
	
	protected void renderGraph(Graphics2D g2d) {
	    if(renderContext.getGraphicsContext() == null) {
	        renderContext.setGraphicsContext(new GraphicsDecorator(g2d));
        } else {
        renderContext.getGraphicsContext().setDelegate(g2d);
        }
        renderContext.setScreenDevice(this);
	    Layout<V,E> layout = model.getGraphLayout();

		g2d.setRenderingHints(renderingHints);
		
		// the size of the VisualizationViewer
		Dimension d = getSize();
		
		// clear the offscreen image
		g2d.setColor(getBackground());
		g2d.fillRect(0,0,d.width,d.height);

		AffineTransform oldXform = g2d.getTransform();
        AffineTransform newXform = new AffineTransform(oldXform);
        newXform.concatenate(viewTransformer.getTransform());
		
        g2d.setTransform(newXform);

		// if there are  preRenderers set, paint them
		for(Paintable paintable : preRenderers) {

		    if(paintable.useTransform()) {
		        paintable.paint(g2d);
		    } else {
		        g2d.setTransform(oldXform);
		        paintable.paint(g2d);
                g2d.setTransform(newXform);
		    }
		}
		
        locationMap.clear();
        
		// paint all the edges
        try {
        	for(E e : layout.getGraph().getEdges()) {

		    V v1 = layout.getGraph().getEndpoints(e).getFirst();
		    V v2 = layout.getGraph().getEndpoints(e).getSecond();
            
            Point2D p = (Point2D) locationMap.get(v1);
            if(p == null) {
                
                p = layout.getLocation(v1);
                p = layoutTransformer.transform(p);
                locationMap.put(v1, p);
            }
		    Point2D q = (Point2D) locationMap.get(v2);
            if(q == null) {
                q = layout.getLocation(v2);
                q = layoutTransformer.transform(q);
                locationMap.put(v2, q);
            }

		    if(p != null && q != null) {

		        renderer.renderEdge(
		                renderContext,
		                layout.getGraph(),
		                e,
		                (int) p.getX(),
		                (int) p.getY(),
		                (int) q.getX(),
		                (int) q.getY());
		        renderer.renderEdgeLabel(
		                renderContext,
		                layout.getGraph(),
		                e,
		                (int) p.getX(),
		                (int) p.getY(),
		                (int) q.getX(),
		                (int) q.getY());
		    }
		}
        } catch(ConcurrentModificationException cme) {
            repaint();
        }
		
		// paint all the vertices
        try {
        	for(V v : layout.getGraph().getVertices()) {

		    Point2D p = (Point2D) locationMap.get(v);
            if(p == null) {
                p = layout.getLocation(v);
                p = layoutTransformer.transform(p);
                locationMap.put(v, p);
            }
		    if(p != null) {
		    	renderer.renderVertex(
		                renderContext,
                        layout.getGraph(),
		                v,
		                (int) p.getX(),
		                (int) p.getY());
		    	renderer.renderVertexLabel(
		                renderContext,
                        layout.getGraph(),
		                v,
		                (int) p.getX(),
		                (int) p.getY());
		    }
		}
        } catch(ConcurrentModificationException cme) {
            repaint();
        }
		
		// if there are postRenderers set, do it
		for(Paintable paintable : postRenderers) {

		    if(paintable.useTransform()) {
		        paintable.paint(g2d);
		    } else {
		        g2d.setTransform(oldXform);
		        paintable.paint(g2d);
                g2d.setTransform(newXform);
		    }
		}
		g2d.setTransform(oldXform);
	}

	/**
	 * VisualizationListener reacts to changes in the size of the
	 * VisualizationViewer. When the size changes, it ensures
	 * that the offscreen image is sized properly. 
	 * If the layout is locked to this view size, then the layout
	 * is also resized to be the same as the view size.
	 *
	 *
	 */
	protected class VisualizationListener extends ComponentAdapter {
		protected BasicVisualizationServer<V,E> vv;
		public VisualizationListener(BasicVisualizationServer<V,E> vv) {
			this.vv = vv;
		}

		/**
		 * create a new offscreen image for the graph
		 * whenever the window is resied
		 */
		public void componentResized(ComponentEvent e) {
		    Dimension d = vv.getSize();
		    if(d.width <= 0 || d.height <= 0) return;
		    checkOffscreenImage(d);
		    repaint();
		}
	}

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#addPreRenderPaintable(edu.uci.ics.jung.visualization.BasicPaintable)
     */
    public void addPreRenderPaintable(Paintable paintable) {
        if(preRenderers == null) {
            preRenderers = new ArrayList<Paintable>();
        }
        preRenderers.add(paintable);
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#removePreRenderPaintable(edu.uci.ics.jung.visualization.BasicPaintable)
     */
    public void removePreRenderPaintable(Paintable paintable) {
        if(preRenderers != null) {
            preRenderers.remove(paintable);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#addPostRenderPaintable(edu.uci.ics.jung.visualization.BasicVisualizationServer.Paintable)
     */
    public void addPostRenderPaintable(Paintable paintable) {
        if(postRenderers == null) {
            postRenderers = new ArrayList<Paintable>();
        }
        postRenderers.add(paintable);
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#removePostRenderPaintable(edu.uci.ics.jung.visualization.BasicVisualizationServer.Paintable)
     */
   public void removePostRenderPaintable(Paintable paintable) {
        if(postRenderers != null) {
            postRenderers.remove(paintable);
        }
    }

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#addChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#removeChangeListener(javax.swing.event.ChangeListener)
     */
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getChangeListeners()
     */
    public ChangeListener[] getChangeListeners() {
        return changeSupport.getChangeListeners();
    }

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#fireStateChanged()
     */
    public void fireStateChanged() {
        changeSupport.fireStateChanged();
    }   
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getPickedVertexState()
     */
    public PickedState<V> getPickedVertexState() {
        return pickedVertexState;
    }
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getPickedEdgeState()
     */
    public PickedState<E> getPickedEdgeState() {
        return pickedEdgeState;
    }
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setPickedVertexState(edu.uci.ics.jung.visualization.picking.PickedState)
     */
    public void setPickedVertexState(PickedState<V> pickedVertexState) {
        if(pickEventListener != null && this.pickedVertexState != null) {
            this.pickedVertexState.removeItemListener(pickEventListener);
        }
        this.pickedVertexState = pickedVertexState;
        this.renderContext.setPickedVertexState(pickedVertexState);
        if(pickEventListener == null) {
            pickEventListener = new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    repaint();
                }
            };
        }
        pickedVertexState.addItemListener(pickEventListener);
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setPickedEdgeState(edu.uci.ics.jung.visualization.picking.PickedState)
     */
    public void setPickedEdgeState(PickedState<E> pickedEdgeState) {
        if(pickEventListener != null && this.pickedEdgeState != null) {
            this.pickedEdgeState.removeItemListener(pickEventListener);
        }
        this.pickedEdgeState = pickedEdgeState;
        this.renderContext.setPickedEdgeState(pickedEdgeState);
        if(pickEventListener == null) {
            pickEventListener = new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    repaint();
                }
            };
        }
        pickedEdgeState.addItemListener(pickEventListener);
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getPickSupport()
     */
    public GraphElementAccessor<V,E> getPickSupport() {
        return pickSupport;
    }
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setPickSupport(edu.uci.ics.jung.visualization.GraphElementAccessor)
     */
    public void setPickSupport(GraphElementAccessor<V,E> pickSupport) {
        this.pickSupport = pickSupport;
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getCenter()
     */
    public Point2D getCenter() {
        Dimension d = getSize();
        return new Point2D.Float(d.width/2, d.height/2);
    }

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#getRenderContext()
     */
    public RenderContext<V,E> getRenderContext() {
        return renderContext;
    }

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.VisualizationServer#setRenderContext(edu.uci.ics.jung.visualization.RenderContext)
     */
    public void setRenderContext(RenderContext<V,E> renderContext) {
        this.renderContext = renderContext;
        renderContext.setViewTransformer(getViewTransformer());
    }
}