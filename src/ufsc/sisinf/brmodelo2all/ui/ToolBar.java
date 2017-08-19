package ufsc.sisinf.brmodelo2all.ui;

import javax.swing.BorderFactory;
import javax.swing.JToolBar;

import ufsc.sisinf.brmodelo2all.ui.CommandActions.HistoryAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.NewConceptualModelingAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.NewNoSQLModelingAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.NewRelationalModelingAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.OpenAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.PrintAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.SaveAction;

//import com.mxgraph.swing.mxGraphComponent;

public class ToolBar extends JToolBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8015443128436394471L;
	// menu indexes
	public static final int NEW_CONCEPTUAL_BUTTON = 0;
	public static final int NEW_LOGICAL_BUTTON = 1;
	public static final int OPEN_BUTTON = 2;
	public static final int SAVE_BUTTON = 3;
	public static final int PRINT_BUTTON = 5;
	public static final int CUT_BUTTON = 7;
	public static final int COPY_BUTTON = 8;
	public static final int PASTE_BUTTON = 9;
	public static final int DELETE_BUTTON = 11;
	public static final int UNDO_BUTTON = 13;
	public static final int REDO_BUTTON = 14;

	/**
	 * 
	 */
	public ToolBar(final AppMainWindow editor, int orientation) {

		super(orientation);

		setFloatable(false);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), getBorder()));

		add(editor.bind("NewConceptual", new NewConceptualModelingAction(),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/new_conceitual.png"));
		add(editor.bind("NewRelational", new NewRelationalModelingAction(),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/new_logico.png"));
		add(editor.bind("NewNoSQL", new NewNoSQLModelingAction(), null));
		add(editor.bind("Open", new OpenAction(), "/ufsc/sisinf/brmodelo2all/ui/images/menu/open.png"));
		add(editor.bind("Save", new SaveAction(false), "/ufsc/sisinf/brmodelo2all/ui/images/menu/save.png"));

		addSeparator();

		add(editor.bind("Undo", new HistoryAction(true), "/ufsc/sisinf/brmodelo2all/ui/images/menu/undo.png"));
		add(editor.bind("Redo", new HistoryAction(false), "/ufsc/sisinf/brmodelo2all/ui/images/menu/redo.png"));

		addSeparator();

		add(editor.bind("Print", new PrintAction(), "/ufsc/sisinf/brmodelo2all/ui/images/menu/print.png"));

	}
}
