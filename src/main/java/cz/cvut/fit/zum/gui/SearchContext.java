package cz.cvut.fit.zum.gui;

import cz.cvut.fit.zum.VisInfo;
import cz.cvut.fit.zum.api.AbstractAlgorithm;
import cz.cvut.fit.zum.api.Algorithm;
import cz.cvut.fit.zum.api.InformedSearch;
import java.util.List;
import cz.cvut.fit.zum.api.Node;
import cz.cvut.fit.zum.api.UninformedSearch;
import cz.cvut.fit.zum.data.Edge;
import cz.cvut.fit.zum.data.StateSpace;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.SwingWorker;

/**
 *
 * @author Tomas Barton
 */
public class SearchContext extends SwingWorker<Void, HighlightTask> implements TaskContext {

    private Algorithm algorithm;
    private final List<Node> nodes;
    private final Node startNode;
    private final Node endNode;
    private SearchLayer layer;
    private int expandCalls;
    private int exploredNodes;
    private int targetCheck;
    private long delay;
    private boolean stop = false;
    private List<Node> path;
    private long startTime, endTime;
    private BufferedImage startPoint;
    private BufferedImage endPoint;
    private BufferedImage visited;
    private Color edgeColor = new Color(255, 0, 255);

    public SearchContext(AbstractAlgorithm algorithm, Node startNode, Node endNode, SearchLayer layer, long delay) {
        this.algorithm = algorithm;
        VisInfo visInfo = VisInfo.getInstance();
        this.nodes = visInfo.getNodes();
        this.layer = layer;
        this.startNode = startNode;
        this.endNode = endNode;
        this.expandCalls = 0;
        this.exploredNodes = 0;
        this.targetCheck = 0;
        this.delay = delay;

        startPoint = visInfo.createCircle(Color.RED);
        endPoint = visInfo.createCircle(Color.GREEN);
        visited = visInfo.createCircle(new Color(215, 153, 3));
    }

    public Node getStartNode() {
        return this.startNode;
    }

    public Node getTargetNode() {
        return this.endNode;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public final Node getNode(int id) {
        return nodes.get(id);
    }

    public boolean isFinal(int id) {
        return id == this.endNode.getId();
    }

    /**
     * Delayed painting
     *
     * @param start
     * @param end
     */
    public void highlightEdge(Node start, Node end) {
        publish(new HighlightEdge(layer, start, end, edgeColor));
    }

    public void expandCalled() {
        expandCalls++;
    }

    public int getExpandCalls() {
        return expandCalls;
    }

    public int getExploredNodes() {
        return exploredNodes;
    }

    public void incExplored(int exp) {
        exploredNodes += exp;

    }

    public void targetCheck(Node node) {
        targetCheck++;
        if (node.getId() != startNode.getId()) {
            publish(new HighlightPoint(layer, node, visited));
        }
    }

    public int getTargetCheck() {
        return targetCheck;
    }

    public long getDelay() {
        return delay;
    }

    public boolean isStop() {
        return stop;
    }

    @Override
    public void setFinish(boolean stop) {
        this.stop = stop;
        if (stop) {
            cancel(true);
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        startTime = System.currentTimeMillis();
        endTime = startTime;
        if (algorithm instanceof UninformedSearch) {
            path = ((UninformedSearch) algorithm).findPath(startNode);
        } else if (algorithm instanceof InformedSearch) {
            path = ((InformedSearch) algorithm).findPath(startNode, endNode);
        } else {
            throw new RuntimeException("Algorithm must implement either UninformedSearch or InformedSearch");
        }

        layer.searchFinished = true;
        stop = true;

        endTime = System.currentTimeMillis();
        layer.fireAlgEvent(AlgorithmEvents.FINISHED);

        return null;
    }

    @Override
    protected void process(List<HighlightTask> pairs) {
        while (!pairs.isEmpty()) {
            HighlightTask task = pairs.remove(0);
            task.process();
        }
        layer.repaint(); //after exploring some node we repaint the layer
        layer.updateStats(this);
    }

    @Override
    protected void done() {
        layer.highlightPoint(startNode, layer.startPoint);
        layer.highlightPoint(endNode, layer.endPoint);
        double dist = layer.highlightPath(path);
        double expanded = getExpandCalls();
        double cov = expanded / (double) StateSpace.nodesCount() * 100;
        long time = endTime - startTime;
        layer.stats.put("explored", (double) getExploredNodes());
        layer.stats.put("expanded", expanded);
        layer.stats.put("coverage", cov);
        layer.stats.put("distance", dist);
        layer.stats.put("time", (double) time);
        layer.fireStatsChanged(layer.stats);
        layer.repaint();

        System.out.println("Search finished, time = " + time + " ms");
    }

    public long getTime() {
        if (endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public List<Node> expand(Node from) {
        expandCalled();
        List<Edge> edges = from.getEdges();
        ArrayList<Node> result = new ArrayList<Node>();
        Node to;
        for (Edge e : edges) {
            int toId = e.getToId();
            to = nodes.get(toId);
            highlightEdge(from, to);
            result.add(to);
        }
        //if we have to stop
        if (isStop()) {
            throw new RuntimeException("Algorithm stopped by the user");
        }
        incExplored(result.size());
        long d = getDelay();
        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            System.out.println("Search interrupted.");
            //Exceptions.printStackTrace(e);
        }
        return result;
    }

    @Override
    public boolean isTarget(Node node) {
        targetCheck(node);
        return isFinal(node.getId());
    }

    @Override
    public void panelResize() {
       //@TODO implement
    }
}