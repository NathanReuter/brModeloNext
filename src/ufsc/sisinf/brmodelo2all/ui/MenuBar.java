package ufsc.sisinf.brmodelo2all.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.ui.CommandActions.AssociativeEntityPromotionAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.CloseAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.ColorAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.ConvertConceptualToLogicalAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.ConvertConceptualToNoSqlAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.ConvertLogicalToPhysicalAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.ConvertLogicalToPhysicalNoSQLAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.EntityPromotionAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.ExitAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.HistoryAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.NewConceptualModelingAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.NewNoSQLModelingAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.NewRelationalModelingAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.OpenAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.PrintAction;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.SaveAction;

public class MenuBar extends JMenuBar {

	/**
	 *
	 */
	private static final long serialVersionUID = 4060203894740766714L;

	public MenuBar(final AppMainWindow mainWindow) {
		JMenu menu = null;

		// Creates the file menu
		menu = add(new JMenu(mxResources.get("file")));
		populateFileMenu(menu, mainWindow);

		// Creates the edit menu
		menu = add(new JMenu(mxResources.get("edit")));
		populateEditMenu(menu, mainWindow);

		// Creates the conceptual menu
		menu = add(new JMenu(mxResources.get("conceptualModeling")));
		populateConceptualModelingMenu(menu, mainWindow);

		// Creates the logical menu
		menu = add(new JMenu(mxResources.get("logicalModeling")));
		populateLogicalModelingMenu(menu, mainWindow);

		// Creates the NoSQL physical convertion menu
		menu = add(new JMenu(mxResources.get("logicalModeling")));
		populateNoSQLConvertionMenu(menu, mainWindow);
		menu.setVisible(false);

		// Creates the selection menu
		menu = add(new JMenu(mxResources.get("selection")));
		menu.setVisible(false); // it depends on an selected object to be
								// visible
		populateSelectionMenu(menu, mainWindow);

		// Creates the window menu
		menu = add(new JMenu(mxResources.get("window")));
		populateWindowMenu(menu, mainWindow);

		// Creates the help menu
		menu = add(new JMenu(mxResources.get("help")));
		populateHelpMenu(menu, mainWindow);

		// Creates the view menu
		menu = add(new JMenu(mxResources.get("view")));
		populateViewMenu(menu, mainWindow);

	}

	public void populateFileMenu(JMenu menu, final AppMainWindow mainWindow) {
		menu.add(mainWindow.bind(mxResources.get("newConceptual"), new NewConceptualModelingAction(),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/new_small.png"));
		menu.add(mainWindow.bind(mxResources.get("newRelational"), new NewRelationalModelingAction(),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/new_small.png"));
		menu.add(mainWindow.bind(mxResources.get("newNoSQL"), new NewNoSQLModelingAction(),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/new_small.png"));
		menu.add(mainWindow.bind(mxResources.get("openFile"), new OpenAction(),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/open_small.png"));
		menu.add(mainWindow.bind(mxResources.get("close"), new CloseAction()));

		menu.addSeparator();

		menu.add(mainWindow.bind(mxResources.get("save"), new SaveAction(false),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/save_small.png"));
		menu.add(mainWindow.bind(mxResources.get("saveAs"), new SaveAction(true),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/save_small.png"));

		menu.addSeparator();

		menu.add(mainWindow.bind(mxResources.get("print"), new PrintAction(),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/print_small.png"));

		menu.add(mainWindow.bind("Exportar Imagem", new ExportAction(mainWindow), null));

		menu.addSeparator();

		menu.add(mainWindow.bind(mxResources.get("exit"), new ExitAction()));
	}

	public void populateEditMenu(JMenu menu, final AppMainWindow mainWindow) {
		menu.add(mainWindow.bind(mxResources.get("undo"), new HistoryAction(true),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/undo_small.png"));
		menu.add(mainWindow.bind(mxResources.get("redo"), new HistoryAction(false),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/redo_small.png"));

		menu.addSeparator();

		menu.add(mainWindow.bind(mxResources.get("cut"), new mouseHandler(1),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/cut_small.png"));
		menu.add(mainWindow.bind(mxResources.get("copy"), new CopyHandler(1),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/copy_small.png"));
		menu.add(mainWindow.bind(mxResources.get("paste"), new PasteHandler(1),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/paste_small.png"));

		menu.addSeparator();

		menu.add(mainWindow.bind(mxResources.get("delete"), new DeleteHandler(1),
				"/ufsc/sisinf/brmodelo2all/ui/images/menu/delete_small.png"));

		menu.addSeparator();

		mainWindow.getCurrentEditor().getGraphics();
		mxGraph gra = mainWindow.getCurrentEditor().getGraphComponent().getGraph();
		menu.add(mainWindow.bind(mxResources.get("selectAll"), new TrataAll(gra)));
		menu.add(mainWindow.bind(mxResources.get("selectNone"), new TrataNome(gra)));

	}

	private void populateViewMenu(JMenu menu, AppMainWindow mainWindow) {
		menu.add(new CommandActions.ToggleGridItem(mainWindow, mxResources.get("grid")));

		menu.addSeparator();

		JMenu submenu = null;

		submenu = (JMenu) menu.add(new JMenu(mxResources.get("zoom")));
		submenu.add(mainWindow.bind("400%", new CommandActions.ScaleAction(4)));
		submenu.add(mainWindow.bind("200%", new CommandActions.ScaleAction(2)));
		submenu.add(mainWindow.bind("150%", new CommandActions.ScaleAction(1.5)));
		submenu.add(mainWindow.bind("100%", new CommandActions.ScaleAction(1)));
		submenu.add(mainWindow.bind("75%", new CommandActions.ScaleAction(0.75)));
		submenu.add(mainWindow.bind("50%", new CommandActions.ScaleAction(0.5)));
	}

	/**
	 * Adds menu items to the given format menu. This is factored out because
	 * the format menu appears in the menubar and also in the popupmenu.
	 */
	public static void populateFormatMenu(JMenu menu, AppMainWindow mainWindow) {
		menu.add(mainWindow.bind(mxResources.get("fillcolor"),
				new ColorAction(mxResources.get("fillcolor"), mxConstants.STYLE_FILLCOLOR),
				"/ufsc/sisinf/brmodelo2all/ui/images/fillcolor.gif"));
		menu.add(mainWindow.bind(mxResources.get("fontcolor"),
				new ColorAction(mxResources.get("fontcolor"), mxConstants.STYLE_FONTCOLOR),
				"/ufsc/sisinf/brmodelo2all/ui/images/fontcolor.gif"));

	}

	public void populateConceptualModelingMenu(JMenu menu, final AppMainWindow mainWindow) {
		menu.add(mainWindow.bind("Convers\u00E3o para modelagem lógica relacional",
				new ConvertConceptualToLogicalAction()));
		menu.add(mainWindow.bind("Convers\u00E3o para modelagem NoSQL", new ConvertConceptualToNoSqlAction()));
	}

	public void populateLogicalModelingMenu(JMenu menu, final AppMainWindow mainWindow) {
		JMenu submenu = (JMenu) menu.add(new JMenu(mxResources.get("insertsObject")));
		menu.addSeparator();

		menu.add(mainWindow.bind("Convers\u00E3o para modelagem f\u00EDsica", new ConvertLogicalToPhysicalAction()));

	}

	private void populateNoSQLConvertionMenu(JMenu menu, AppMainWindow mainWindow) {
		menu.add(mainWindow.bind(mxResources.get("nosql"), new ConvertLogicalToPhysicalNoSQLAction()));
	}

	public void populateSelectionMenu(JMenu menu, final AppMainWindow mainWindow) {
		// Creates the format menu
		JMenu submenu = (JMenu) menu.add(new JMenu(mxResources.get("format")));
		populateFormatMenu(submenu, mainWindow);

		menu.addSeparator();

		menu.add(mainWindow.bind(mxResources.get("promoteToEntity"), new EntityPromotionAction(), null/* icon */))
				.setVisible(false);
		menu.add(mainWindow.bind(mxResources.get("promoteToAssociativeEntity"), new AssociativeEntityPromotionAction(),
				null/* icon */)).setVisible(false);
		menu.add(mainWindow.bind(mxResources.get("convertToExclusive"), null/* action */, null/* icon */))
				.setVisible(false);
		menu.add(mainWindow.bind(mxResources.get("convertToOptional"), null/* action */, null/* icon */))
				.setVisible(false);
		menu.add(mainWindow.bind(mxResources.get("convertToOptional"), null/* action */, null/* icon */))
				.setVisible(false);
		menu.add(mainWindow.bind("Adicionar atributo", new ComposeAction(mainWindow), null/* icon */))
				.setVisible(false);

	}

	public void populateWindowMenu(JMenu menu, final AppMainWindow mainWindow) {
		UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();

		for (int i = 0; i < lafs.length; i++) {
			final String clazz = lafs[i].getClassName();
			menu.add(new AbstractAction(lafs[i].getName()) {
				/**
				 *
				 */
				private static final long serialVersionUID = -1959608552341208477L;

				public void actionPerformed(ActionEvent e) {
					mainWindow.setLookAndFeel(clazz);
				}
			});
		}
	}

	public void populateHelpMenu(JMenu menu, final AppMainWindow mainWindow) {
		JMenuItem item = menu.add(new JMenuItem(mxResources.get("about")));
		item.addActionListener(new ActionListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				mainWindow.about();
			}
		});
	}
}