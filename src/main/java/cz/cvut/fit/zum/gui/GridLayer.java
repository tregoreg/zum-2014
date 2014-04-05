package cz.cvut.fit.zum.gui;

import cz.cvut.fit.zum.VisInfo;
import cz.cvut.fit.zum.api.Node;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import cz.cvut.fit.zum.data.Edge;
import cz.cvut.fit.zum.data.NodeImpl;
import java.awt.Color;

/**
 *
 * @author Tomas Barton
 */
public class GridLayer extends BufferedPanel {

    private static final long serialVersionUID = -2300535687309104278L;
    private VisInfo visInfo;
    private BufferedImage buffCircle;
    private Shape line;

    public GridLayer(Dimension minSize) {
        setSize(minSize);
        this.visInfo = VisInfo.getInstance();
        initComponents();
    }

    private void initComponents() {
        setOpaque(false);
        setDoubleBuffered(false);
        setMinimumSize(this.getSize());
        //cache point in graph
        buffCircle = visInfo.createCircle(visInfo.getNodeColor());
    }

    @Override
    protected void drawComponent(Graphics2D g) {
        if (visInfo.hasNodes()) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g.setColor(visInfo.getNodeColor());

            AffineTransform at = visInfo.getTranformation();
            Point2D from;
            for (Node node : visInfo.getNodes()) {
                from = node.getPoint();

                //filling shapes seems to be very expensive operation
                //this is probably the fastest way how to draw
                at.setToIdentity();
                at.translate(from.getX() - visInfo.getCircleDiam(), from.getY() - visInfo.getCircleDiam());
                g.drawImage(buffCircle, at, null);

                //System.out.println("point [" + x + ", " + y + "]");
                for (Edge edge : node.getEdges()) {
                    if (edge.getFromId() < edge.getToId()) {
                        NodeImpl nodeTo = (NodeImpl) visInfo.getNode(edge.getToId());
                        line = new Line2D.Double(from, nodeTo.getPoint());
                        g.draw(line);
                    }
                }
            }
        }
        //g.dispose();
    }

    protected void higlightEdge(final Node start, final Node end, Color color) {
        graphics.setColor(color);
        line = new Line2D.Double(start.getPoint(), end.getPoint());
        graphics.draw(line);
    }
}
