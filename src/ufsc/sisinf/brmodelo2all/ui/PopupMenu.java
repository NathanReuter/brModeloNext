package ufsc.sisinf.brmodelo2all.ui;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.ui.CommandActions.HistoryAction;

public class PopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3132749140550242191L;

	public PopupMenu(AppMainWindow mainWindow)

	{

		boolean selected = !mainWindow.getCurrentEditor().getGraphComponent().getGraph().isSelectionEmpty();

		Object sel = mainWindow.getCurrentEditor().getGraphComponent().getGraph().getSelectionCell();

		mxGraph gra = mainWindow.getCurrentEditor().getGraphComponent().getGraph();

		add(mainWindow.bind(mxResources.get("undo"), new HistoryAction(true),
				"/ufsc/sisinf/brmodelo2all/ui/images/undo_small.png"));

		addSeparator();

		add(mainWindow.bind(mxResources.get("cut"), (Action) new mouseHandler(1),
				"/ufsc/sisinf/brmodelo2all/ui/images/cut_small.png")).setEnabled(selected);

		add(mainWindow.bind(mxResources.get("copy"), new CopyHandler(1),
				"/ufsc/sisinf/brmodelo2all/ui/images/copy_small.png")).setEnabled(selected);

		add(mainWindow.bind(mxResources.get("paste"), new PasteHandler(1),
				"/ufsc/sisinf/brmodelo2all/ui/images/paste_small.png")).setEnabled(true);

		addSeparator();

		add(mainWindow.bind(mxResources.get("delete"), new DeleteHandler(1),
				"/ufsc/sisinf/brmodelo2all/ui/images/delete_small.png")).setEnabled(selected);

		addSeparator();

		// Creates the format menu
		JMenu menu = (JMenu) add(new JMenu(mxResources.get("format")));
		MenuBar.populateFormatMenu(menu, mainWindow);
		menu.setEnabled(selected);

		addSeparator();

		add(mainWindow.bind("Selecionar Atributos", new Select(sel, gra), null/* icon */)).setEnabled(selected);

		// add(mainWindow.bind("Mover nome do atributo",new MoveName(sel,gra),
		// null/*icon*/)).setEnabled(selected);

		// add(mainWindow.bind(mxResources.get("promoteToAssociativeEntity"),
		// null/*action*/,
		// null/*icon*/));
		// addSeparator();

		// add(mainWindow.bind(mxResources.get("objectProperties"),
		// null/*action*/)).setEnabled(true);
	}

}