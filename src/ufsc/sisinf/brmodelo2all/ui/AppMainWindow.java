package ufsc.sisinf.brmodelo2all.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxResources;

import ufsc.sisinf.brmodelo2all.app.BrModelo2All;
import ufsc.sisinf.brmodelo2all.control.ModelingEditor;
import ufsc.sisinf.brmodelo2all.model.Modeling;
import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.AssociativeRelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.ColumnObject;
import ufsc.sisinf.brmodelo2all.model.objects.ConnectorObject;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;
import ufsc.sisinf.brmodelo2all.model.objects.WritingBoxObject;

public class AppMainWindow extends JPanel {

	private static final long serialVersionUID = -6561623072112577140L;

	/**
	 * Adds required resources for i18n
	 */
	static {
		try {

			mxResources.add("ufsc/sisinf/brmodelo2all/ui/resources/editor");
		} catch (Exception e) {
		}
	}
	/**
	 * Holds the shared number formatter.
	 *
	 * @see NumberFormat#getInstance()
	 */
	public static final NumberFormat numberFormat = NumberFormat.getInstance();
	public static int windowCount = 0;
	public static final int EDITOR_WIDTH = 600;
	public static final int EDITOR_HEIGHT = 600;
	public static final int EDITOR_MAX_WIDTH = 600;
	public static final int EDITOR_MAX_HEIGHT = 600;
	public static final int OBJECTS_PALETTE_WIDTH = 65;
	public static final int DIVIDER_SIZE = 3;
	public static final int CONCEPTUAL_MENU = 2;
	public static final int LOGICAL_MENU = 3;
	public static final int SELECTION_MENU = 4;
	public static final int NOSQL_MENU = 4;
	public static final int ENTITY_PROMOTION_MENU = 2;
	public static final int ASSOCIATIVE_ENTITY_PROMOTION_MENU = 3;
	public static final int EXCLUSIVE_CONVERTION_MENU = 4;
	public static final int OPTIONAL_CONVERTION_MENU = 5;
	public static final int ATTRIBUTE_MENU = 7;
	protected String appTitle;
	protected JPanel toolBarPanel;
	protected ToolBar toolBar;
	protected String selectedEntry = null;
	protected ModelingPalette conceptualObjects = null;
	protected ModelingPalette relationalObjects = null;
	protected ModelingPalette noSQLObjects = null;
	protected static JDesktopPane desktop;
	protected static ModelingEditor currentEditor;
	protected List<ModelingEditor> editors;
	protected JPanel statusBar;
	protected JLabel mouseLocation;
	protected JLabel description;
	protected String lastDiretory = null;

	public AppMainWindow() {
		this("brModeloNext");

		createModelingPalette();
		installToolBar();
	}

	public AppMainWindow(String appTitle) {
		// Stores and updates the frame title
		this.appTitle = appTitle;

		currentEditor = new ModelingEditor(mxResources.get("newConceptual"), this, true/* conceptualModeling */, false);
		editors = new ArrayList<ModelingEditor>();
		editors.add(currentEditor);

		desktop = new JDesktopPane();
		desktop.setBackground(Color.GRAY);
		desktop.add(currentEditor, -1);

		currentEditor.initialize();

		// Creates the status bar
		statusBar = createStatusBar();

		// Puts everything together
		setLayout(new BorderLayout());
		add(desktop, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);

		updateTitle();
	}

	public void createModelingPalette() {
		conceptualObjects = insertPalette();
		relationalObjects = insertPalette();
		noSQLObjects = insertPalette();

		// Sets the edge template to be used for creating new edges if an edge
		// is clicked in the shape palette
		conceptualObjects.addListener(mxEvent.SELECT, new mxIEventListener() {
			public void invoke(Object sender, mxEventObject evt) {
				Object tmp = evt.getProperty("transferable");

				if (tmp instanceof mxGraphTransferable) {
					mxGraphTransferable t = (mxGraphTransferable) tmp;
					Object cell = t.getCells()[0];
					mxCell c = (mxCell) cell;
					selectedEntry = c.getValue().toString();
					if (currentEditor.getGraphComponent().getGraph().getModel().isEdge(cell)) {
						((Modeling) currentEditor.getGraphComponent().getGraph()).setEdgeTemplate(cell);
					}

				} else {
					selectedEntry = null;
				}
			}
		});

		relationalObjects.addListener(mxEvent.SELECT, new mxIEventListener() {
			public void invoke(Object sender, mxEventObject evt) {
				Object tmp = evt.getProperty("transferable");

				if (tmp instanceof mxGraphTransferable) {
					mxGraphTransferable t = (mxGraphTransferable) tmp;
					Object cell = t.getCells()[0];
					mxCell c = (mxCell) cell;
					selectedEntry = c.getValue().toString();
					if (currentEditor.getGraphComponent().getGraph().getModel().isEdge(cell)) {
						((Modeling) currentEditor.getGraphComponent().getGraph()).setEdgeTemplate(cell);
					}

				} else {
					selectedEntry = null;
				}
			}
		});

		noSQLObjects.addListener(mxEvent.SELECT, new mxIEventListener() {
			public void invoke(Object sender, mxEventObject evt) {
				Object tmp = evt.getProperty("transferable");

				if (tmp instanceof mxGraphTransferable) {
					mxGraphTransferable t = (mxGraphTransferable) tmp;
					Object cell = t.getCells()[0];
					mxCell c = (mxCell) cell;
					selectedEntry = c.getValue().toString();
					if (currentEditor.getGraphComponent().getGraph().getModel().isEdge(cell)) {
						((Modeling) currentEditor.getGraphComponent().getGraph()).setEdgeTemplate(cell);
					}

				} else {
					selectedEntry = null;
				}
			}
		});

		// Adds some template cells for dropping into the graph
		String text = mxResources.get("pointer");

		/*
		 * 
		 * 
		 * ------------------CONCEPTUAL MENU------------------
		 */
		text = mxResources.get("entity");
		conceptualObjects.addTemplate(text,
				new ImageIcon(
						BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/entidade.png")),
				"entity", 300, 300, text);
		text = mxResources.get("relation");
		conceptualObjects.addTemplate(text,
				new ImageIcon(
						BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/relacao.png")),
				"relationship", 400, 180, text);
		text = mxResources.get("selfRelation");
		conceptualObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/auto_relacionamento.png")),
				"relationship", 400, 180, text);
		text = mxResources.get("associativeEntity");
		conceptualObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/entidade_associativa.png")),
				"associativeEntity", 100, 100, text);

		conceptualObjects.addSeparator();

		text = mxResources.get("inheritance");
		conceptualObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/especializacao.png")),
				"inheritance", 100, 100, text);
		text = mxResources.get("exclusiveInheritance");
		conceptualObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/especializacao_exclusiva.png")),
				"inheritance", 100, 100, text);
		text = mxResources.get("nonExclusiveInheritance");
		conceptualObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/especializacao_naoexclusiva.png")),
				"inheritance", 100, 100, text);

		conceptualObjects.addSeparator();

		text = mxResources.get("attribute");
		conceptualObjects.addTemplate(text,
				new ImageIcon(
						BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/atributo.png")),
				"attribute", 50, 20, text);
		text = mxResources.get("identifierAttribute");
		conceptualObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/atributo_identificador.png")),
				"identifierAttribute", 100, 100, text);
		text = mxResources.get("composedAttribute");
		conceptualObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/atributo_composto.png")),
				"attribute", 100, 100, text);

		text = mxResources.get("connector");
		conceptualObjects.addEdgeTemplate(text,
				new ImageIcon(
						BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/conector.png")),
				"connector", 100, 100, text);

		text = "WritingBox";

		conceptualObjects.addEdgeTemplate(text,
				new ImageIcon(BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/a.png")),
				"WritingBox", 100, 100, text);

		conceptualObjects.addSeparator();

		/*
		 * 
		 * 
		 * ------------------RELATIONAL MENU------------------
		 */
		text = mxResources.get("table");
		relationalObjects.addEdgeTemplate(text,
				new ImageIcon(BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/logico/tabela.png")),
				"table", 100, 100, text);

		relationalObjects.addSeparator();

		text = mxResources.get("field");
		relationalObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/logico/campo.png")),
				"column", 100, 100, text);
		text = mxResources.get("primaryKey");
		relationalObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/logico/chave_primaria.png")),
				"primaryKey", 100, 100, text);
		text = mxResources.get("foreignKey");
		relationalObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/logico/chave_estrangeira.png")),
				"foreignKey", 100, 100, text);
		text = mxResources.get("connector");
		relationalObjects.addEdgeTemplate(text,
				new ImageIcon(
						BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/conector.png")),
				"connector", 100, 100, text);
		text = "WritingBox";

		relationalObjects.addEdgeTemplate(text,
				new ImageIcon(BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/a.png")),
				"WritingBox", 100, 100, text);

		/*
		 * 
		 * 
		 * ------------------NoSQL MENU------------------
		 */

		text = mxResources.get("collection");
		noSQLObjects.addEdgeTemplate(text,
				new ImageIcon(BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/logico/tabela.png")),
				"collection", 100, 100, text);

		text = mxResources.get("block");
		noSQLObjects.addEdgeTemplate(text,
				new ImageIcon(
						BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/entidade.png")),
				"block", 100, 100, text);

		noSQLObjects.addSeparator();

		text = mxResources.get("noSqlAttribute");
		noSQLObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/logico/campo.png")),
				"verticalAlign=top", 100, 100, text);
		text = mxResources.get("noSqlIdentifierAttribute");
		noSQLObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/logico/chave_primaria.png")),
				"noSqlIdentifierAttribute", 100, 100, text);
		text = mxResources.get("noSqlReferenceAttribute");
		noSQLObjects.addTemplate(text,
				new ImageIcon(BrModelo2All.class
						.getResource("/ufsc/sisinf/brmodelo2all/ui/images/logico/chave_estrangeira.png")),
				"noSqlReferenceAttribute", 100, 100, text);

		text = mxResources.get("disjointGeneralization");
		noSQLObjects.addTemplate(text,
				new ImageIcon(
						BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/nosql/bracket_icon.png")),
				"disjointGeneralization", 100, 100, text);

		text = "WritingBox";
		noSQLObjects.addEdgeTemplate(text,
				new ImageIcon(BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/a.png")),
				"WritingBox", 100, 100, text);

		updateControls();
	}

	public void clearModelingPalette() {
		conceptualObjects.clearSelection();
		relationalObjects.clearSelection();
		noSQLObjects.clearSelection();
	}

	public void updateControls() {
		boolean conceptual, relational, noSQL = false;

		// Trocar entre ToolBar conceitual e logico
		if (currentEditor != null) {
			conceptual = currentEditor.isConceptualModeling();
			relational = currentEditor.isRelationalModeling();
			noSQL = (!conceptual && !relational);
			ModelingPalette toolBarAdd;

			// Trocar entre lÃ³gico e conceitual
			if (conceptual)
				toolBarAdd = conceptualObjects;
			else if (relational)
				toolBarAdd = relationalObjects;
			else
				toolBarAdd = noSQLObjects;
			add(toolBarAdd, BorderLayout.WEST);

		} else {
			conceptual = relational = noSQL = false;
		}

		// Set visible
		conceptualObjects.setVisible(conceptual);
		relationalObjects.setVisible(relational);
		noSQLObjects.setVisible(noSQL);

		// Update MenuBar
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		if (frame != null) {
			JMenuBar menuBar = frame.getJMenuBar();
			JMenu conceptualMenu = menuBar.getMenu(CONCEPTUAL_MENU);
			JMenu logicalMenu = menuBar.getMenu(LOGICAL_MENU);
			JMenu noSQLMenu = menuBar.getMenu(NOSQL_MENU);
			conceptualMenu.setVisible(conceptual);
			logicalMenu.setVisible(relational);
			noSQLMenu.setVisible(noSQL);

		}
	}

	protected void installToolBar() {
		toolBarPanel = new JPanel(new BorderLayout());
		toolBar = new ToolBar(this, JToolBar.HORIZONTAL);

		toolBarPanel.add(toolBar, BorderLayout.WEST);

		add(toolBarPanel, BorderLayout.NORTH);
	}

	protected JPanel createStatusBar() {
		JPanel statusBar = new JPanel(new BorderLayout());
		mouseLocation = new JLabel("");
		mouseLocation.setPreferredSize(new Dimension(70, 20));
		mouseLocation.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		statusBar.add(mouseLocation, BorderLayout.WEST);

		description = new JLabel(mxResources.get("ready"));
		description.setLayout(new FlowLayout());
		description.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		description.setFont(new Font(description.getFont().getFamily(), Font.BOLD, 11));
		statusBar.add(description, BorderLayout.CENTER);

		return statusBar;
	}

	public ModelingPalette insertPalette() {
		final ModelingPalette palette = new ModelingPalette(this);
		palette.setOrientation(javax.swing.SwingConstants.VERTICAL);
		palette.setFloatable(false);

		return palette;
	}

	public JDesktopPane getDesktop() {
		return desktop;
	}

	public String getSelectedEntry() {
		ModelingPalette currentPalette;
		if (currentEditor.isConceptualModeling())
			currentPalette = conceptualObjects;
		else if (currentEditor.isRelationalModeling())
			currentPalette = relationalObjects;
		else
			currentPalette = noSQLObjects;

		String selected = currentPalette.getSelectedEntry() != null ? currentPalette.getSelectedEntry().getToolTipText()
				: null;
		return selected;
	}

	public void setSelectedEntry(String entry) {
		this.selectedEntry = entry;
	}

	public ModelingEditor getCurrentEditor() {
		return currentEditor;
	}

	public void setCurrentEditor(ModelingEditor editor) {
		ModelingEditor previousEditor = this.currentEditor;

		if (previousEditor != editor) {
			if (previousEditor != null) {
				try {
					this.currentEditor.setMaximum(false);
					this.currentEditor.setSelected(false);
				} catch (PropertyVetoException e) {
				}
			}

			this.currentEditor = editor;

			updateControls();
			updateTitle();
		}
	}

	public List<ModelingEditor> getEditors() {
		return editors;
	}

	public void status(String location, String msg) {
		if (location != null) {
			mouseLocation.setText(location);
		}

		String descMsg;
		if (getSelectedEntry() != null) {
			descMsg = getEntryDescription();
			description.setOpaque(true);
			description.setForeground(Color.RED);
		} else {
			if (msg != null) {
				descMsg = msg;
				description.setOpaque(true);
				description.setForeground(Color.BLACK);
			} else {
				descMsg = "";
			}

		}

		description.setText(descMsg.toUpperCase());

	}

	public String getEntryDescription() {
		String description = "";

		if (selectedEntry == mxResources.get("entity"))
			description = "Clique na modelagem para inserir uma entidade";
		else if (selectedEntry == mxResources.get("relation"))
			description = "Clique na modelagem para inserir um relacionamento";
		else if (selectedEntry == mxResources.get("selfRelation"))
			description = "Selecione uma entidade/entidade associativa";
		else if (selectedEntry == mxResources.get("associativeEntity"))
			description = "Clique na modelagem para inserir uma entidade associativa";
		else if (selectedEntry == mxResources.get("attribute")
				|| selectedEntry == mxResources.get("identifierAttribute")
				|| selectedEntry == mxResources.get("composedAttribute"))
			description = "Selecione uma entidade/relacionamento/entidade associativa";
		else if (selectedEntry == mxResources.get("inheritance")
				|| selectedEntry == mxResources.get("exclusiveInheritance")
				|| selectedEntry == mxResources.get("nonExclusiveInheritance"))
			description = "Selecione uma entidade";
		else if (selectedEntry == mxResources.get("connector"))
			description = "Selecione dois objetos para conectar";
		else if (selectedEntry == mxResources.get("table"))
			description = "Clique na modelagem para inserir uma tabela";
		else if (selectedEntry == mxResources.get("field"))
			description = "Selecione uma tabela para inserir o campo";
		else if (selectedEntry == mxResources.get("primaryKey"))
			description = "Selecione uma tabela para inserir o campo com chave primï¿½ria";
		else if (selectedEntry == mxResources.get("foreignKey"))
			description = "Selecione uma tabela para inserir o campo com chave estrangeira";
		else if (selectedEntry == mxResources.get("separator"))
			description = "Selecione uma tabela para inserir o separador de campos.";
		else if (selectedEntry.equals("WritingBox"))
			description = "Clique para colocar um comentário.";
		else if (selectedEntry == mxResources.get("collection"))
			description = "Clique na modelagem para inserir uma coleção.";
		else if (selectedEntry == mxResources.get("block"))
			description = "Clique em uma coleção para adicionar um bloco a ela.";
		else if (selectedEntry == mxResources.get("noSqlAttribute"))
			description = "Clique em uma coleção ou bloco para inserir um atributo.";
		else if (selectedEntry == mxResources.get("noSqlIdentifierAttribute"))
			description = "Clique em uma coleção ou bloco para inserir um atributo identificador.";
		else if (selectedEntry == mxResources.get("noSqlReferenceAttribute"))
			description = "Clique na coleção ou bloco que possua o atributo identificador e depois clique na"
					+ " coleção ou bloco que receberá esse atributo referenciado.";

		return description;
	}

	public String getLastDiretory() {
		return lastDiretory == null ? System.getProperty("user.dir") : lastDiretory;
	}

	public void setLastDiretory(String lastDiretory) {
		this.lastDiretory = lastDiretory;
	}

	public void updateTitle() {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null) {
			String title = null;
			if (currentEditor != null) {
				if (currentEditor.getCurrentFile() != null) {

					title = currentEditor.getCurrentFile().getAbsolutePath();
					if (currentEditor.isModified()) {
						title += "*";
					}

				} else {
					if (currentEditor.isConceptualModeling())
						title = mxResources.get("newConceptual");
					else if (currentEditor.isRelationalModeling())
						title = mxResources.get("newRelational");
					else
						title = mxResources.get("newNoSQL");

					title += " " + windowCount;

					if (currentEditor.isModified()) {
						title += "*";
					}

				}

				currentEditor.setTitle(title);
			}

			String newTitle = title != null ? appTitle + " - " + title : appTitle;
			frame.setTitle(newTitle);
		}
	}

	public void openSqlEditor(SqlEditor sqlEditor) {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		sqlEditor.setModal(true);

		// Centers inside the application frame
		int x = frame.getX() + (frame.getWidth() - sqlEditor.getWidth()) / 2;
		int y = frame.getY() + (frame.getHeight() - sqlEditor.getHeight()) / 2;
		sqlEditor.setLocation(x, y);

		// Shows the modal dialog and waits
		sqlEditor.setVisible(true);
	}

	// Same as the openSqlEditor, the only difference is the sintaxe used.
	public void openNoSqlEditor(NoSqlEditor sqlEditor) {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		// Centers inside the application frame
		int x = frame.getX() + (frame.getWidth() - sqlEditor.getWidth()) / 2;
		int y = frame.getY() + (frame.getHeight() - sqlEditor.getHeight()) / 2;
		sqlEditor.setLocation(x, y);

		// Shows the modal dialog and waits
		sqlEditor.setVisible(true);
	}

	public void about() {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null) {
			AboutWindow about = new AboutWindow(frame);
			about.setModal(true);

			// Centers inside the application frame
			int x = frame.getX() + (frame.getWidth() - about.getWidth()) / 2;
			int y = frame.getY() + (frame.getHeight() - about.getHeight()) / 2;
			about.setLocation(x, y);

			// Shows the modal dialog and waits
			about.setVisible(true);
		}
	}

	public void properties(Object cell, ModelingComponent modelingComponent) {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		boolean c = false;

		if (frame != null) {
			PropertiesWindow properties = new PropertiesWindow(frame, cell, modelingComponent);
			properties.setModal(true);

			// make some adjustments for positioning the window
			mxCell cellObject = (mxCell) cell;

			if (cellObject.getValue() instanceof ColumnObject
					|| cellObject.getValue() instanceof AssociativeRelationObject) {
				cellObject = (mxCell) cellObject.getParent();
			}

			mxGeometry cellGeometry = cellObject.getGeometry();

			double usedWidth = cellGeometry.getX() + cellGeometry.getWidth() + PropertiesWindow.WINDOW_WIDTH;
			double usedHeight = cellGeometry.getY() + cellGeometry.getHeight() + properties.getWindowHeight();

			int x = (int) (usedWidth > modelingComponent.getWidth()
					? cellGeometry.getX() - PropertiesWindow.WINDOW_WIDTH
					: cellGeometry.getX() + cellGeometry.getWidth()) + 20;

			int y = (int) (usedHeight > modelingComponent.getHeight()
					? cellGeometry.getY() + cellGeometry.getHeight() - properties.getWindowHeight()
					: cellGeometry.getY());

			x += currentEditor.getX();
			if (x < 0) {
				x = 10;
			}
			y += currentEditor.getY() + (toolBarPanel.getHeight() * 2 - 5);
			if (y < 0) {
				y = 10;
			}

			if (cellObject.getValue() instanceof ConnectorObject) {

			}

			// Ajust scale of the properties
			double scale = modelingComponent.getGraph().getView().getScale();
			x = (int) (x * scale);
			y = (int) (y * scale);

			// Centers inside the application frame
			properties.setLocation(x, y);

			if (cellObject.getValue() instanceof WritingBoxObject) {

				CommentWindow pp = new CommentWindow(frame, cell, modelingComponent);
				pp.setModal(true);
				pp.setLocation(x, y);
				pp.setVisible(true);
				currentEditor.getGraphComponent().getGraph().updateCellSize(cellObject);

				c = true;

			}

			// Shows the modal dialog and waits
			if (c == false) {
				properties.setVisible(true);
			}

		}
	}

	public void exit() {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null) {
			frame.dispose();
		}
	}

	public void setLookAndFeel(String clazz) {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

		if (frame != null) {
			try {
				UIManager.setLookAndFeel(clazz);
				SwingUtilities.updateComponentTreeUI(frame);

				// Needs to assign the key bindings again
				Iterator<ModelingEditor> iterator = editors.iterator();
				while (iterator.hasNext()) {
					ModelingEditor editor = iterator.next();
					editor.updateKeyboardHandler();
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public JFrame createFrame(JMenuBar menuBar) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar(menuBar);
		menuBar.getMenu(LOGICAL_MENU).setVisible(false);

		// Updates the frame title
		updateTitle();

		URL url = this.getClass().getResource("/ufsc/sisinf/brmodelo2all/ui/images/icon.png");
		Image imagemTitulo = Toolkit.getDefaultToolkit().getImage(url);
		frame.setIconImage(imagemTitulo);

		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setBounds(0, 0, 800, 600);

		return frame;
	}

	public void updateSelectionMenu(ModelingObject object) {
		// JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		// if (frame != null) {
		// JMenuBar menuBar = frame.getJMenuBar();
		// JMenu selectionMenu = menuBar.getMenu(SELECTION_MENU);
		// selectionMenu.setVisible(object != null);
		// selectionMenu.getItem(ASSOCIATIVE_ENTITY_PROMOTION_MENU).setVisible(object
		// instanceof RelationObject);

		// boolean exclusiveConvertion = false;
		// boolean inheritanceObject = object instanceof InheritanceObject;
		// if (inheritanceObject) {
		// exclusiveConvertion = ((InheritanceObject) object).isExclusive();
		// }

		// selectionMenu.getItem(ATTRIBUTE_MENU).setVisible(object instanceof
		// AttributeObject);

		// selectionMenu.getItem(EXCLUSIVE_CONVERTION_MENU).setVisible(inheritanceObject
		// && exclusiveConvertion);
		// selectionMenu.getItem(OPTIONAL_CONVERTION_MENU).setVisible(inheritanceObject
		// && !exclusiveConvertion);
		// }
	}

	public void updateSelectionMenu(boolean visible) {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);
		if (frame != null) {
			JMenuBar menuBar = frame.getJMenuBar();
			JMenu selectionMenu = menuBar.getMenu(SELECTION_MENU);
			selectionMenu.setVisible(visible);
			selectionMenu.getItem(ENTITY_PROMOTION_MENU).setVisible(false);
			selectionMenu.getItem(ASSOCIATIVE_ENTITY_PROMOTION_MENU).setVisible(false);
			selectionMenu.getItem(EXCLUSIVE_CONVERTION_MENU).setVisible(false);
			selectionMenu.getItem(OPTIONAL_CONVERTION_MENU).setVisible(false);
		}

	}

	/**
	 *
	 * @param name
	 * @param action
	 * @return a new Action bound to the specified string name
	 */
	public Action bind(String name, final Action action) {
		return bind(name, action, null);
	}

	/**
	 *
	 * @param name
	 * @param action
	 * @param iconUrl
	 * @return a new Action bound to the specified string name and icon
	 */
	@SuppressWarnings("serial")
	public Action bind(String name, final Action action, String iconUrl) {
		return new AbstractAction(name,
				(iconUrl != null) ? new ImageIcon(BrModelo2All.class.getResource(iconUrl)) : null) {
			public void actionPerformed(ActionEvent e) {
				action.actionPerformed(new ActionEvent(getDesktop(), e.getID(), e.getActionCommand()));
			}
		};
	}

	protected void installListeners() {

	}

}