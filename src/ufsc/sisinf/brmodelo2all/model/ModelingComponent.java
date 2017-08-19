package ufsc.sisinf.brmodelo2all.model;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.view.mxInteractiveCanvas;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.app.BrModelo2All;
import ufsc.sisinf.brmodelo2all.control.ModelingHandler;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;
import ufsc.sisinf.brmodelo2all.ui.AppMainWindow;

public class ModelingComponent extends mxGraphComponent {

	private static final long serialVersionUID = -6833603133512882012L;

	protected final AppMainWindow mainWindow;

	/**
	 * 
	 * @param graph
	 */
	public ModelingComponent(mxGraph graph, final AppMainWindow mainWindow) {
		super(graph);

		this.mainWindow = mainWindow;

		// Sets switches typically used in an editor
		setPageVisible(false);
		setGridVisible(false);
		setToolTips(true);
		getConnectionHandler().setCreateTarget(true);
		// impedir que fará uma marca ao colocar o mouse sobre um vertex
		getConnectionHandler().getMarker().setEnabled(false);

		// Loads the defalt stylesheet from an external file
		mxCodec codec = new mxCodec();
		Document doc = mxUtils.loadDocument(
				BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/resources/default-style.xml").toString());
		codec.decode(doc.getDocumentElement(), graph.getStylesheet());

		// Sets the background to white
		getViewport().setOpaque(false);

		setBackground(Color.WHITE);
	}

	public AppMainWindow getMainWindow() {
		return mainWindow;
	}

	@Override
	protected mxGraphHandler createGraphHandler() {
		return new ModelingHandler(this);
	}

	/**
	 * Overrides drop behavior to set the cell style if the target is not a
	 * valid drop target and the cells are of the same type (eg. both vertices
	 * or both edges).
	 */
	public Object[] importCells(Object[] cells, double dx, double dy, Object target, Point location) {
		if (target == null && cells.length == 1 && location != null) {
			target = getCellAt(location.x, location.y);

			if (target instanceof mxICell && cells[0] instanceof mxICell) {
				mxICell targetCell = (mxICell) target;
				mxICell dropCell = (mxICell) cells[0];

				if (targetCell.isVertex() == dropCell.isVertex() || targetCell.isEdge() == dropCell.isEdge()) {
					mxIGraphModel model = graph.getModel();
					model.setStyle(target, model.getStyle(cells[0]));
					graph.setSelectionCell(null/* target */);

					return null;
				}
			}
		}

		return super.importCells(cells, dx, dy, target, location);
	}

	/**
	 * 
	 */
	public mxInteractiveCanvas createCanvas() {
		return new ufsc.sisinf.brmodelo2all.model.shapes.ShapesCanvas();
	}

	/**
	 * 
	 */
	public void startEditingAtCell(Object cell, EventObject evt) {
		if (cell == null) {

			cell = graph.getSelectionCell();

			if (cell != null && !graph.isCellEditable(cell)) {
				cell = null;
			}

		}

		if (cell != null) {
			mainWindow.properties(cell, this);

		}

	}

	@Override
	public Object[] selectRegion(Rectangle rect, MouseEvent e) {
		Object[] cells = getCells(rect);

		if (cells.length == 0)
			mainWindow.updateSelectionMenu(null);
		else if (cells.length == 1) {
			mainWindow.updateSelectionMenu((ModelingObject) ((mxCell) cells[0]).getValue());

		}

		else
			mainWindow.updateSelectionMenu(true);

		return super.selectRegion(rect, e);
	}

	/** 
	 * 
	 */
	protected void installDoubleClickHandler() {

	}

}