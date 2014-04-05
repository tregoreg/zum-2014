<<<<<<< HEAD
package cz.cvut.fit.zum.gui;

import cz.cvut.fit.zum.VisInfo;
import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.data.Edge;
import cz.cvut.fit.zum.data.StateSpace;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author Tomas Barton
 */
public class VertexContext extends SwingWorker<Void, HighlightTask> implements TaskContext {

    private AbstractEvolution evolution;
    private long startTime, endTime;
    private SearchLayer layer;
    private BufferedImage notCovered;
    private BufferedImage coveredPoint;
    private BufferedImage reachablePoint;
    private Color edge = Color.BLUE;
    private Color nodeColor = new Color(0.0f, 0.0f, 0.0f);  // semi-transparent
    private HashSet<Node> cover;
    private HashSet<Node> reachable;
    private HashSet<Node> unreachable;
    private HashSet<Edge> coveredEdges;
    private HashSet<Edge> uncoveredEdges;
    private double bestFitness;
    private int generation = 0;
    protected HashMap<String, Double> stats = new HashMap<String, Double>();

    public VertexContext(SearchLayer layer, AbstractEvolution evolution) {
        this.evolution = evolution;
        this.layer = layer;
        initSpace();
        VisInfo visInfo = VisInfo.getInstance();
        //uncovered nodes
        //visInfo.setNodeColor(nodeColor);        
        notCovered = visInfo.createCircle(nodeColor);
        coveredPoint = visInfo.createCircle(Color.GREEN);
        reachablePoint = visInfo.createCircle(Color.BLUE);
        unreachable.addAll(StateSpace.getNodes());
        uncoveredEdges.addAll(StateSpace.getEdges());
    }

    public final void initSpace() {
        cover = new HashSet<Node>(StateSpace.nodesCount());
        reachable = new HashSet<Node>(StateSpace.nodesCount() / 2);
        unreachable = new HashSet<Node>(StateSpace.nodesCount());
        coveredEdges = new HashSet<Edge>(StateSpace.edgesCount());
        uncoveredEdges = new HashSet<Edge>(StateSpace.edgesCount());
    }

    @Override
    protected Void doInBackground() throws Exception {
        panelResize();
        startTime = System.currentTimeMillis();
        evolution.run();

        endTime = System.currentTimeMillis();
        return null;
    }

    @Override
    protected void process(List<HighlightTask> pairs) {
        while (!pairs.isEmpty()) {
            HighlightTask task = pairs.remove(0);
            task.process();
        }
        layer.repaint(); //after exploring some node we repaint the layer
        //  layer.updateStats(this);
    }

    @Override
    protected void done() {
        long time = endTime - startTime;
        System.out.println("Evolution time = " + time + " ms");
        updateStats();
        layer.enableSearchButton();
    }

    public long getTime() {
        if (endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public List<Node> expand(Node node) {
        List<Edge> edges = node.getEdges();
        ArrayList<Node> result = new ArrayList<Node>();
        Node node2;
        for (Edge e : edges) {
            int toId = e.getToId();
            node2 = StateSpace.getNode(toId);
            result.add(node2);
        }
        return result;
    }

    /**
     *
     * @param current
     * @param covered is included in cover
     */
    public void markNode(Node current, boolean covered) {
        if (covered) {
            publish(new HighlightPoint(layer, current, coveredPoint));
            cover.add(current);
            reachable.remove(current); //remove if present
            unreachable.remove(current);
            for (Node to : current.expand()) {
                //highlight edge

                //both edge's nodes are covered
                if (cover.contains(to)) {
                    publish(new HighlightEdge(layer, current, to, Color.YELLOW));
                } else {
                    publish(new HighlightEdge(layer, current, to, Color.GREEN));
                    //point
                    publish(new HighlightPoint(layer, to, reachablePoint));
                    reachable.add(to);
                    unreachable.remove(to);
                }                
            }
            
            for(Edge e : current.getEdges()) {
                coveredEdges.add(e);
                uncoveredEdges.remove(e); // remove if present
            }

        } else {
            cover.remove(current);

            List<Node> nn = current.expand();
            boolean coveredByNeighbour = false;
            for (Node to : nn) {
                //from is not in cover
                if (cover.contains(to)) {
                    coveredByNeighbour = true;
                    publish(new HighlightEdge(layer, current, to, Color.GREEN));
                } else {
                    publish(new HighlightEdge(layer, current, to, Color.RED));

                    //check all neighours which has been possibly covered by this node
                    boolean isCovered = false;
                    for (Node nNeighbour : to.expand()) {
                        if (cover.contains(nNeighbour)) {
                            isCovered = true;
                            //publish(new HighlightEdge(layer, nNeighbour, to, Color.GREEN));
                        }
                    }

                    if (!isCovered) {
                        reachable.remove(to);
                        unreachable.add(to);
                        publish(new HighlightPoint(layer, to, notCovered));
                    }
                }
            }


            if (coveredByNeighbour) {
                publish(new HighlightPoint(layer, current, reachablePoint));
                unreachable.remove(current);
                reachable.add(current);
            } else {
                publish(new HighlightPoint(layer, current, notCovered));
                reachable.remove(current);
                unreachable.add(current);
            }
            
            for(Edge e : current.getEdges()) {             
                if(!cover.contains(StateSpace.getNode(e.getFromId())) &&
                   !cover.contains(StateSpace.getNode(e.getToId()))) {
                    
                    coveredEdges.remove(e);
                    uncoveredEdges.add(e);
                }
            }            
        }
        updateStats();
    }
    
    
    protected void updateStats() {
        double covSize = cover.size();
        double cov = covSize / (double) StateSpace.nodesCount() * 100;
        stats.put("cover", covSize);
        stats.put("fitness", bestFitness);
        stats.put("coverage", cov);
        stats.put("reached", (double) reachable.size());
        stats.put("coveredEdges", (double)coveredEdges.size());
        stats.put("uncoveredEdges", (double)uncoveredEdges.size());
        stats.put("unreachable", (double) unreachable.size());
        double sec = getTime() / 1000.0;
        stats.put("time", sec);
        stats.put("generation", (double) generation);

        layer.fireEvolutionChanged(stats);
    }

    public void setBestFitness(double bestFitness) {
        this.bestFitness = bestFitness;
    }

    public AbstractEvolution getEvolution() {
        return evolution;
    }

    @Override
    public boolean isTarget(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFinish(boolean interrupt) {
        endTime = System.currentTimeMillis();
        updateStats();
        cancel(interrupt);
    }

    @Override
    public void panelResize() {
        for (Node current : reachable) {
            for (Node to : current.expand()) {
                //from is not in cover
                if (reachable.contains(to)) {
                    publish(new HighlightEdge(layer, current, to, Color.RED));
                }
            }
        }

        for (Node current : unreachable) {
            for (Node to : current.expand()) {
                //from is not in cover
                if (!cover.contains(to)) {
                    publish(new HighlightEdge(layer, current, to, Color.RED));
                }
            }
            publish(new HighlightPoint(layer, current, notCovered));
        }

        for (Node current : cover) {
            for (Node to : current.expand()) {
                //highlight edge

                //both edge's nodes are covered
                if (cover.contains(to)) {
                    publish(new HighlightEdge(layer, current, to, Color.YELLOW));
                } else {
                    publish(new HighlightEdge(layer, current, to, Color.GREEN));
                    //point
                    publish(new HighlightPoint(layer, to, reachablePoint));
                }
            }
            publish(new HighlightPoint(layer, current, coveredPoint));
        }
    }

    /**
     * Updates generations counter
     *
     * @param num
     */
    public void updateGeneration(int num) {
        generation = num;
        updateStats();
    }
}
=======
package cz.cvut.fit.zum.gui;

import cz.cvut.fit.zum.VisInfo;
import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.data.Edge;
import cz.cvut.fit.zum.data.StateSpace;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author Tomas Barton
 */
public class VertexContext extends SwingWorker<Void, HighlightTask> implements TaskContext {

    private AbstractEvolution evolution;
    private long startTime, endTime;
    private SearchLayer layer;
    private BufferedImage notCovered;
    private BufferedImage coveredPoint;
    private BufferedImage reachablePoint;
    private Color edge = Color.BLUE;
    private Color nodeColor = new Color(0.0f, 0.0f, 0.0f);  // semi-transparent
    private HashSet<Node> cover;
    private HashSet<Node> reachable;
    private HashSet<Node> unreachable;
    private HashSet<Edge> coveredEdges;
    private HashSet<Edge> uncoveredEdges;
    private double bestFitness;
    private int generation = 0;
    protected HashMap<String, Double> stats = new HashMap<String, Double>();

    public VertexContext(SearchLayer layer, AbstractEvolution evolution) {
        this.evolution = evolution;
        this.layer = layer;
        initSpace();
        VisInfo visInfo = VisInfo.getInstance();
        //uncovered nodes
        //visInfo.setNodeColor(nodeColor);        
        notCovered = visInfo.createCircle(nodeColor);
        coveredPoint = visInfo.createCircle(Color.GREEN);
        reachablePoint = visInfo.createCircle(Color.BLUE);
        unreachable.addAll(StateSpace.getNodes());
        uncoveredEdges.addAll(StateSpace.getEdges());
    }

    public final void initSpace() {
        cover = new HashSet<Node>(StateSpace.nodesCount());
        reachable = new HashSet<Node>(StateSpace.nodesCount() / 2);
        unreachable = new HashSet<Node>(StateSpace.nodesCount());
        coveredEdges = new HashSet<Edge>(StateSpace.edgesCount());
        uncoveredEdges = new HashSet<Edge>(StateSpace.edgesCount());
    }

    @Override
    protected Void doInBackground() throws Exception {
        panelResize();
        startTime = System.currentTimeMillis();
        evolution.run();

        endTime = System.currentTimeMillis();
        return null;
    }

    @Override
    protected void process(List<HighlightTask> pairs) {
        while (!pairs.isEmpty()) {
            HighlightTask task = pairs.remove(0);
            task.process();
        }
        layer.repaint(); //after exploring some node we repaint the layer
        //  layer.updateStats(this);
    }

    @Override
    protected void done() {
        long time = endTime - startTime;
        System.out.println("Evolution time = " + time + " ms");
        updateStats();
        layer.enableSearchButton();
    }

    public long getTime() {
        if (endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public List<Node> expand(Node node) {
        List<Edge> edges = node.getEdges();
        ArrayList<Node> result = new ArrayList<Node>();
        Node node2;
        for (Edge e : edges) {
            int toId = e.getToId();
            node2 = StateSpace.getNode(toId);
            result.add(node2);
        }
        return result;
    }

    /**
     *
     * @param current
     * @param covered is included in cover
     */
    public void markNode(Node current, boolean covered) {
        if (covered) {
            publish(new HighlightPoint(layer, current, coveredPoint));
            cover.add(current);
            reachable.remove(current); //remove if present
            unreachable.remove(current);
            for (Node to : current.expand()) {
                //highlight edge

                //both edge's nodes are covered
                if (cover.contains(to)) {
                    publish(new HighlightEdge(layer, current, to, Color.YELLOW));
                } else {
                    publish(new HighlightEdge(layer, current, to, Color.GREEN));
                    //point
                    publish(new HighlightPoint(layer, to, reachablePoint));
                    reachable.add(to);
                    unreachable.remove(to);
                }                
            }
            
            for(Edge e : current.getEdges()) {
                coveredEdges.add(e);
                uncoveredEdges.remove(e); // remove if present
            }

        } else {
            cover.remove(current);

            List<Node> nn = current.expand();
            boolean coveredByNeighbour = false;
            for (Node to : nn) {
                //from is not in cover
                if (cover.contains(to)) {
                    coveredByNeighbour = true;
                    publish(new HighlightEdge(layer, current, to, Color.GREEN));
                } else {
                    publish(new HighlightEdge(layer, current, to, Color.RED));

                    //check all neighours which has been possibly covered by this node
                    boolean isCovered = false;
                    for (Node nNeighbour : to.expand()) {
                        if (cover.contains(nNeighbour)) {
                            isCovered = true;
                            //publish(new HighlightEdge(layer, nNeighbour, to, Color.GREEN));
                        }
                    }

                    if (!isCovered) {
                        reachable.remove(to);
                        unreachable.add(to);
                        publish(new HighlightPoint(layer, to, notCovered));
                    }
                }
            }


            if (coveredByNeighbour) {
                publish(new HighlightPoint(layer, current, reachablePoint));
                unreachable.remove(current);
                reachable.add(current);
            } else {
                publish(new HighlightPoint(layer, current, notCovered));
                reachable.remove(current);
                unreachable.add(current);
            }
            
            for(Edge e : current.getEdges()) {             
                if(!cover.contains(StateSpace.getNode(e.getFromId())) &&
                   !cover.contains(StateSpace.getNode(e.getToId()))) {
                    
                    coveredEdges.remove(e);
                    uncoveredEdges.add(e);
                }
            }            
        }
        updateStats();
    }
    
    
    protected void updateStats() {
        double covSize = cover.size();
        double cov = covSize / (double) StateSpace.nodesCount() * 100;
        stats.put("cover", covSize);
        stats.put("fitness", bestFitness);
        stats.put("coverage", cov);
        stats.put("reached", (double) reachable.size());
        stats.put("coveredEdges", (double)coveredEdges.size());
        stats.put("uncoveredEdges", (double)uncoveredEdges.size());
        stats.put("unreachable", (double) unreachable.size());
        double sec = getTime() / 1000.0;
        stats.put("time", sec);
        stats.put("generation", (double) generation);

        layer.fireEvolutionChanged(stats);
    }

    public void setBestFitness(double bestFitness) {
        this.bestFitness = bestFitness;
    }

    public AbstractEvolution getEvolution() {
        return evolution;
    }

    @Override
    public boolean isTarget(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFinish(boolean interrupt) {
        endTime = System.currentTimeMillis();
        updateStats();
        cancel(interrupt);
    }

    @Override
    public void panelResize() {
        for (Node current : reachable) {
            for (Node to : current.expand()) {
                //from is not in cover
                if (reachable.contains(to)) {
                    publish(new HighlightEdge(layer, current, to, Color.RED));
                }
            }
        }

        for (Node current : unreachable) {
            for (Node to : current.expand()) {
                //from is not in cover
                if (!cover.contains(to)) {
                    publish(new HighlightEdge(layer, current, to, Color.RED));
                }
            }
            publish(new HighlightPoint(layer, current, notCovered));
        }

        for (Node current : cover) {
            for (Node to : current.expand()) {
                //highlight edge

                //both edge's nodes are covered
                if (cover.contains(to)) {
                    publish(new HighlightEdge(layer, current, to, Color.YELLOW));
                } else {
                    publish(new HighlightEdge(layer, current, to, Color.GREEN));
                    //point
                    publish(new HighlightPoint(layer, to, reachablePoint));
                }
            }
            publish(new HighlightPoint(layer, current, coveredPoint));
        }
    }

    /**
     * Updates generations counter
     *
     * @param num
     */
    public void updateGeneration(int num) {
        generation = num;
        updateStats();
    }
}
>>>>>>> f74fa964b97379918a7c8cc7844b342a2cedab05
