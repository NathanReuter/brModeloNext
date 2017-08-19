package ufsc.sisinf.brmodelo2all.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.model.objects.AssociativeEntityObject;
import ufsc.sisinf.brmodelo2all.model.objects.AssociativeRelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.EntityObject;
import ufsc.sisinf.brmodelo2all.model.objects.RelationObject;

@SuppressWarnings("serial")
public class Select extends AbstractAction {

	Object o;
	mxGraph g;

	public Select(Object oo, mxGraph gra) {

		this.g = gra;
		this.o = oo;

	}

	public void actionPerformed(ActionEvent e) {

		Object cell = o;

		if (cell != null && ((mxCell) cell).getValue() instanceof EntityObject
				|| ((mxCell) cell).getValue() instanceof RelationObject
				|| ((mxCell) cell).getValue() instanceof AssociativeEntityObject
				|| ((mxCell) cell).getValue() instanceof AssociativeRelationObject) {
			ArrayList<Object> celulas = new ArrayList<Object>();

			if (((mxCell) cell).getEdgeCount() > 0) {

				celulas.add(cell);

				for (int c = 0; c < ((mxCell) cell).getEdgeCount(); c++) {

					if (((mxCell) cell).getEdgeAt(c) != null
							&& ((mxCell) cell).getEdgeAt(c).getTerminal(false).getStyle().equals("attribute")
							|| ((mxCell) cell).getEdgeAt(c).getTerminal(false).getStyle()
									.equals("identifierAttribute")) {

						celulas.add(((mxCell) cell).getEdgeAt(c).getTerminal(false));

						for (int z = 0; z < ((mxCell) cell).getEdgeAt(c).getTerminal(false).getEdgeCount(); z++) {

							if (((mxCell) cell).getEdgeAt(c).getTerminal(false).getEdgeAt(z).getTerminal(false)
									.getStyle().equals("attribute")
									|| ((mxCell) cell).getEdgeAt(c).getTerminal(false).getEdgeAt(z).getTerminal(false)
											.getStyle().equals("attribute;labelPosition=left;align=right")
									|| ((mxCell) cell).getEdgeAt(c).getTerminal(false).getEdgeAt(z).getTerminal(false)
											.getStyle().equals("attribute;labelPosition=right;align=left")) {

								celulas.add(((mxCell) cell).getEdgeAt(c).getTerminal(false).getEdgeAt(z)
										.getTerminal(false));

							}

						}

					}

				}

				Object[] evento = celulas.toArray(new Object[celulas.size()]);

				g.setSelectionCells(evento);

			}
		}

	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener arg0) {
		// TODO Auto-generated method stub

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
