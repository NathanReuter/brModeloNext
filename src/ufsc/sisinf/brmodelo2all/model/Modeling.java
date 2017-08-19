package ufsc.sisinf.brmodelo2all.model;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.model.objects.AssociativeRelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.EntityObject;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;

public class Modeling extends mxGraph {

	public static final NumberFormat numberFormat = NumberFormat.getInstance();
	/**
	 * Holds the edge to be used as a template for inserting new edges.
	 */
	protected Object edgeTemplate;

	/**
	 * Custom graph that defines the alternate edge style to be used when the
	 * middle control point of edges is double clicked (flipped).
	 */
	public Modeling() {
		setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
		setCellsDisconnectable(false);
		setDropEnabled(false);
	}

	/**
	 * Sets the edge template to be used to inserting edges.
	 */
	public void setEdgeTemplate(Object template) {
		edgeTemplate = template;
	}

	/**
	 * Prints out some useful information about the cell in the tooltip.
	 */
	@Override
	public String getToolTipForCell(Object cell) {
		String tip = null;

		if (cell instanceof mxICell) {
			Object object = ((mxICell) cell).getValue();
			if (object instanceof AssociativeRelationObject) {
				object = ((mxCell) cell).getParent().getValue();
				cell = ((mxCell) cell).getParent();
			}

			if (object instanceof ModelingObject) {
				mxGeometry geo = getModel().getGeometry(cell);
				tip = "<html>";
				ModelingObject objectModeling = (ModelingObject) object;
				tip += "<b>" + mxResources.get("objectProperties") + "</b><br>";
				tip += objectModeling.getToolTip();

				if (getModel().isVertex(cell)) {
					if (geo != null) {
						tip += mxResources.get("position") + "(x,y): (";
						tip += numberFormat.format(geo.getX()) + "," + numberFormat.format(geo.getY()) + ")<br>";
						tip += mxResources.get("width") + ": " + numberFormat.format(geo.getWidth()) + "<br>";
						tip += mxResources.get("height") + ": " + numberFormat.format(geo.getHeight());
					}
				}

				tip += "</html>";
			}
		}

		return tip;
	}

	/**
	 * Overrides the method to use the currently selected edge template for new
	 * edges.
	 *
	 * @param graph
	 * @param parent
	 * @param id
	 * @param value
	 * @param source
	 * @param target
	 * @param style
	 * @return
	 */
	@Override
	public Object createEdge(Object parent, String id, Object value, Object source, Object target, String style) {

		return super.createEdge(parent, id, value, source, target, style);
	}

	// @Override
	// public void setSelectionCell(Object objectCell) {
	// mxCell cell = (mxCell) objectCell;
	// if (cell.getStyle().equals("straight")) {
	// super.setSelectionCell(null);
	// } else if (!cell.getStyle().equals("associativeRelation")) {
	// super.setSelectionCell(cell);
	// } else {
	// super.setSelectionCell(cell.getParent());
	// }
	// }
	//
	// @Override
	// public boolean isCellSelectable(Object objectCell) {
	// mxCell cell = (mxCell) objectCell;
	// return !cell.getStyle().equals("straight");
	// }

	@Override
	public Object[] removeCells(Object[] cells, boolean includeEdges) {

		if (cells == null) {
			cells = getDeletableCells(getSelectionCells());
		}

		// Remover tableRelation e conectores
		List<Object> allRelatedCells = new ArrayList<Object>();

		for (Object object : cells) {
			mxCell cell = (mxCell) object;
			if (cell.getStyle().equals("tableRelation")) {
				allRelatedCells.add(cell);
				for (int i = 0; i < cell.getEdgeCount(); i++) {
					allRelatedCells.add((mxCell) cell.getEdgeAt(i));
				}
			}
		}

		removeReferencesFromParents(cells);
		addAllRelatedCells(cells, allRelatedCells);

		return super.removeCells(allRelatedCells.toArray(), includeEdges);
	}

	public void removeReferencesFromParents(Object[] cells) {
		for (int i = 0; i < cells.length; i++) {
			if (((mxCell) cells[i]).getValue() instanceof ModelingObject) {
				ModelingObject modelingObject = (ModelingObject) ((mxCell) cells[i]).getValue();
				if (modelingObject != null) {
					modelingObject.removeReferencesFromParents();
				}
			}
		}
	}

	public void addAllRelatedCells(Object[] cells, List<Object> allRelatedCells) {
		for (int i = 0; i < cells.length; i++) {
			mxCell cell = (mxCell) cells[i];
			if (cell.getValue() instanceof ModelingObject) {
				ModelingObject modelingObject = (ModelingObject) cell.getValue();

				if (!(cell.getValue() instanceof EntityObject) || ((cell.getValue() instanceof EntityObject)
						&& ((EntityObject) cell.getValue()).getParentObject() == null)) {
					addAllRelatedCells(modelingObject.getChildObjects().toArray(), allRelatedCells);
					allRelatedCells.add(cells[i]);
				}
			}
		}
	}
}