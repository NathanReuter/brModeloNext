package ufsc.sisinf.brmodelo2all.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class MoveName implements Action {

	Object sel;

	mxGraph gra;

	public MoveName(Object s, mxGraph g) {

		this.sel = s;

		this.gra = g;
	}

	public void actionPerformed(ActionEvent arg0) {

		mxCell cell = ((mxCell) sel);
		cell.setStyle("attributeRight");
		gra.refresh();

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
