package ufsc.sisinf.brmodelo2all.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.model.objects.AttributeObject;

public class ComposeAction implements Action {

	mxGraph graph;

	public ComposeAction(AppMainWindow gra) {

		graph = gra.getCurrentEditor().getSelectedGraph();

	}

	public void actionPerformed(ActionEvent arg0) {

		if (graph != null) {

			if (!graph.isSelectionEmpty()) {

				Object selectedCell = graph.getSelectionCell();
				graph.getModel().beginUpdate();

				try {

					mxCell cell = (mxCell) selectedCell;

					AttributeObject attribute = (AttributeObject) cell.getValue();
					attribute.setComposed(true);

					mxGeometry geometry = cell.getGeometry();
					double newY = AdjustAttributePosition(geometry.getY() - 10);
					double newX = geometry.getCenterX();
					Object[] atributo = new Object[1];

					atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
							new AttributeObject(mxResources.get("attribute"), cell, false), newX, newY, 16, 16, //$NON-NLS-1$
							"attribute"); //$NON-NLS-1$

					attribute.addChildObject(atributo[0]);

					graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
							cell, atributo[0], "straight"); //$NON-NLS-1$

				} finally {
					graph.getModel().endUpdate();
				}

			}
		}

	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	public double AdjustAttributePosition(double cellY) {
		return cellY - 30;
	}

	@Override
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void putValue(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEnabled(boolean arg0) {
		// TODO Auto-generated method stub

	}

}
