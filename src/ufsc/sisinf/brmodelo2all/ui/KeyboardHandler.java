package ufsc.sisinf.brmodelo2all.ui;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.util.mxGraphActions;

/**
 * @author Administrator
 *
 */
public class KeyboardHandler extends mxKeyboardHandler {

	/**
	 *
	 * @param graphComponent
	 */
	public KeyboardHandler(mxGraphComponent graphComponent) {
		super(graphComponent);
	}

	/**
	 * Return JTree's input map.
	 */
	protected InputMap getInputMap(int condition) {
		InputMap map = super.getInputMap(condition);

		if (condition == JComponent.WHEN_FOCUSED && map != null) {
			map.put(KeyStroke.getKeyStroke("control S"), "save");
			map.put(KeyStroke.getKeyStroke("control shift S"), "saveAs");
			map.put(KeyStroke.getKeyStroke("control N"), "new");
			map.put(KeyStroke.getKeyStroke("control O"), "open");

			map.put(KeyStroke.getKeyStroke("control Z"), "undo");
			map.put(KeyStroke.getKeyStroke("control Y"), "redo");

			map.put(KeyStroke.getKeyStroke("control shift V"), "selectVertices");
			map.put(KeyStroke.getKeyStroke("control shift E"), "selectEdges");
		}

		return map;
	}

	/**
	 * Return the mapping between JTree's input map and JGraph's actions.
	 */
	protected ActionMap createActionMap() {
		ActionMap map = super.createActionMap();

		map.put("save", new CommandActions.SaveAction(false));
		map.put("saveAs", new CommandActions.SaveAction(true));
		map.put("newConceptual", new CommandActions.NewConceptualModelingAction());
		map.put("newRelational", new CommandActions.NewRelationalModelingAction());
		map.put("newNoSQL", new CommandActions.NewNoSQLModelingAction());
		map.put("open", new CommandActions.OpenAction());
		map.put("close", new CommandActions.CloseAction());
		map.put("undo", new CommandActions.HistoryAction(true));
		map.put("redo", new CommandActions.HistoryAction(false));
		map.put("selectVertices", mxGraphActions.getSelectVerticesAction());
		map.put("selectEdges", mxGraphActions.getSelectEdgesAction());

		return map;
	}

}