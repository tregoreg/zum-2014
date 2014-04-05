<<<<<<< HEAD
package cz.cvut.fit.zum.gui;

import cz.cvut.fit.zum.AlgorithmFactory;
import cz.cvut.fit.zum.EvolutionFactory;
import cz.cvut.fit.zum.VisInfo;
import cz.cvut.fit.zum.api.AbstractAlgorithm;
import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.VertexCoverTask;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import javax.swing.JLayeredPane;

/**
 *
 * @author Tomas Barton
 */
public class MapPanel extends JLayeredPane {

    private static final long serialVersionUID = 7363939254912224717L;
    private MapLayer mapLayer;
    private GridLayer gridLayer;
    private SearchLayer searchLayer;
    private Dimension size = new Dimension(0, 0);
    private VisInfo visInfo;

    public MapPanel() {
        visInfo = VisInfo.getInstance();
        initializeComponents();
    }

    private void initializeComponents() {
        Dimension minSize = new Dimension(800, 800);
        visInfo.computePositions(minSize);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0D;
        c.weighty = 1.0D;
        //layer with map texture
        mapLayer = new MapLayer(minSize);
        add(mapLayer, c, 10); //back layer
        //layer with nodes and edges
        gridLayer = new GridLayer(minSize);
        add(gridLayer, c, 0);
        searchLayer = new SearchLayer(minSize, gridLayer);
        add(searchLayer, c, 0);
        setPreferredSize(minSize);
        Rectangle bounds = getBounds();
        Dimension dim = new Dimension(bounds.width, bounds.height);
        mapLayer.setPreferredSize(dim);
        gridLayer.setPreferredSize(dim);
    }

    public void updateSize(Dimension dim) {
        int min = Math.min(dim.width, dim.height - 115);
        size.width = min;
        size.height = min;
        //when size changes, we need to rescale points
        visInfo.computePositions(size);
        mapLayer.rescale(size);
        gridLayer.rescale(size);
        searchLayer.rescale(size);
        setPreferredSize(size);
    }

    protected void updateLayer() {
        searchLayer.updateLayer();
        gridLayer.updateLayer();
        mapLayer.updateLayer();
    }

    public void algorithmChanged(String algName) {
        AbstractAlgorithm alg = AlgorithmFactory.getDefault().getProvider(algName);
        if (alg != null) {
            searchLayer.algorithmChanged(alg);
        }
    }

    public void vertexCoverAlgorithmChanged(String algName, boolean run) {
        AbstractEvolution alg = EvolutionFactory.getInstance().getProvider(algName);
        if (alg != null) {
            searchLayer.vertexAlgorithmChanged(alg, run);
        }
    }

    public void resetMap() {
        searchLayer.clearSelection();
        searchLayer.updateLayer();
        gridLayer.updateLayer();
        mapLayer.updateLayer();
    }

    public void stopSearch() {
        searchLayer.stopSearch();
    }

    public void addStatsListener(AlgorithmListener listener) {
        searchLayer.addStatsListener(listener);
    }

    public void addEvolutionListener(EvolutionListener listener) {
        searchLayer.addEvolutionListener(listener);
    }

    void test1Search() {
        searchLayer.search(visInfo.getNode(1118), visInfo.getNode(636));
    }

    void test2Search() {
        searchLayer.search(visInfo.getNode(5), visInfo.getNode(1863));
    }

    void test3Search() {
        searchLayer.search(visInfo.getNode(163), visInfo.getNode(315));
    }

    void test4Search() {
        searchLayer.search(visInfo.getNode(287), visInfo.getNode(338));
    }

    public void setDelay(long delay) {
        searchLayer.setDelay(delay);
    }

  /*  public void startVertexCover() {
        searchLayer.vertexCover();
    }*/

    protected VertexCoverTask getVertexCoverTask(){
        return searchLayer.getVertexCoverTask();
    }
}
=======
package cz.cvut.fit.zum.gui;

import cz.cvut.fit.zum.AlgorithmFactory;
import cz.cvut.fit.zum.EvolutionFactory;
import cz.cvut.fit.zum.VisInfo;
import cz.cvut.fit.zum.api.AbstractAlgorithm;
import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.VertexCoverTask;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import javax.swing.JLayeredPane;

/**
 *
 * @author Tomas Barton
 */
public class MapPanel extends JLayeredPane {

    private static final long serialVersionUID = 7363939254912224717L;
    private MapLayer mapLayer;
    private GridLayer gridLayer;
    private SearchLayer searchLayer;
    private Dimension size = new Dimension(0, 0);
    private VisInfo visInfo;

    public MapPanel() {
        visInfo = VisInfo.getInstance();
        initializeComponents();
    }

    private void initializeComponents() {
        Dimension minSize = new Dimension(800, 800);
        visInfo.computePositions(minSize);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0D;
        c.weighty = 1.0D;
        //layer with map texture
        mapLayer = new MapLayer(minSize);
        add(mapLayer, c, 10); //back layer
        //layer with nodes and edges
        gridLayer = new GridLayer(minSize);
        add(gridLayer, c, 0);
        searchLayer = new SearchLayer(minSize, gridLayer);
        add(searchLayer, c, 0);
        setPreferredSize(minSize);
        Rectangle bounds = getBounds();
        Dimension dim = new Dimension(bounds.width, bounds.height);
        mapLayer.setPreferredSize(dim);
        gridLayer.setPreferredSize(dim);
    }

    public void updateSize(Dimension dim) {
        int min = Math.min(dim.width, dim.height - 115);
        size.width = min;
        size.height = min;
        //when size changes, we need to rescale points
        visInfo.computePositions(size);
        mapLayer.rescale(size);
        gridLayer.rescale(size);
        searchLayer.rescale(size);
        setPreferredSize(size);
    }

    protected void updateLayer() {
        searchLayer.updateLayer();
        gridLayer.updateLayer();
        mapLayer.updateLayer();
    }

    public void algorithmChanged(String algName) {
        AbstractAlgorithm alg = AlgorithmFactory.getDefault().getProvider(algName);
        if (alg != null) {
            searchLayer.algorithmChanged(alg);
        }
    }

    public void vertexCoverAlgorithmChanged(String algName, boolean run) {
        AbstractEvolution alg = EvolutionFactory.getInstance().getProvider(algName);
        if (alg != null) {
            searchLayer.vertexAlgorithmChanged(alg, run);
        }
    }

    public void resetMap() {
        searchLayer.clearSelection();
        searchLayer.updateLayer();
        gridLayer.updateLayer();
        mapLayer.updateLayer();
    }

    public void stopSearch() {
        searchLayer.stopSearch();
    }

    public void addStatsListener(AlgorithmListener listener) {
        searchLayer.addStatsListener(listener);
    }

    public void addEvolutionListener(EvolutionListener listener) {
        searchLayer.addEvolutionListener(listener);
    }

    void test1Search() {
        searchLayer.search(visInfo.getNode(1118), visInfo.getNode(636));
    }

    void test2Search() {
        searchLayer.search(visInfo.getNode(5), visInfo.getNode(1863));
    }

    void test3Search() {
        searchLayer.search(visInfo.getNode(163), visInfo.getNode(315));
    }

    void test4Search() {
        searchLayer.search(visInfo.getNode(287), visInfo.getNode(338));
    }

    public void setDelay(long delay) {
        searchLayer.setDelay(delay);
    }

  /*  public void startVertexCover() {
        searchLayer.vertexCover();
    }*/

    protected VertexCoverTask getVertexCoverTask(){
        return searchLayer.getVertexCoverTask();
    }
}
>>>>>>> f74fa964b97379918a7c8cc7844b342a2cedab05
