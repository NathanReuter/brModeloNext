package ufsc.sisinf.brmodelo2all.control;

import java.awt.event.MouseEvent;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphHandler;

import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;

public class ModelingHandler extends mxGraphHandler {

	public ModelingHandler(mxGraphComponent graphComponent) {
		super(graphComponent);
		setRemoveCellsFromParent(false);
	}

	public void mousePressed(MouseEvent e) {

		if (cell != null && ((mxCell) cell).getValue() instanceof ModelingObject) {

			((ModelingComponent) graphComponent).getMainWindow()
					.updateSelectionMenu((ModelingObject) ((mxCell) cell).getValue());

		} else {
			((ModelingComponent) graphComponent).getMainWindow().updateSelectionMenu(null);
		}
		super.mousePressed(e);
	}

}
