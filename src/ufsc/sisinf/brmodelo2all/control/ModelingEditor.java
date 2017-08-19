package ufsc.sisinf.brmodelo2all.control;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.model.Modeling;
import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;
import ufsc.sisinf.brmodelo2all.ui.AppMainWindow;
import ufsc.sisinf.brmodelo2all.ui.CommandActions.SaveAction;
import ufsc.sisinf.brmodelo2all.ui.KeyboardHandler;
import ufsc.sisinf.brmodelo2all.ui.PopupMenu;

/**
 *
 * @author Caca
 */
public class ModelingEditor extends JInternalFrame implements Serializable {

	protected final AppMainWindow mainWindow;
	protected final boolean conceptualModeling;
	protected final boolean relationalModeling;
	protected mxGraphComponent modelingComponent;
	protected ModelingManager modelingManager;
	protected mxUndoManager undoManager;
	protected File currentFile;
	protected boolean modified = false;
	protected mxRubberband rubberband;
	protected mxKeyboardHandler keyboardHandler;

	public void setModelingComponent(mxGraphComponent modelingComponent) {
		this.modelingComponent = modelingComponent;
	}

	public mxGraph getSelectedGraph() {
		return mainWindow.getCurrentEditor().getGraphComponent().getGraph();
	}

	public mxGraphComponent getModelingComponent() {
		return modelingComponent;
	}

	public ModelingManager getModelingManager() {
		return modelingManager;
	}

	protected mxIEventListener undoHandler = new mxIEventListener() {
		public void invoke(Object source, mxEventObject evt) {
			undoManager.undoableEditHappened((mxUndoableEdit) evt.getProperty("edit"));
		}
	};
	protected mxIEventListener changeTracker = new mxIEventListener() {
		public void invoke(Object source, mxEventObject evt) {
			setModified(true);
		}
	};

	public ModelingEditor(String name, final AppMainWindow mainWindow, boolean conceptualModeling,
			boolean relationalModeling) {
		super(name);
		this.mainWindow = mainWindow;
		this.conceptualModeling = conceptualModeling;
		this.relationalModeling = relationalModeling;

		// Stores a reference to the graph and creates the command history
		modelingComponent = new ModelingComponent(new Modeling(), mainWindow);
		final mxGraph graph = modelingComponent.getGraph();
		undoManager = createUndoManager();

		modelingComponent.setFoldingEnabled(false);
		// Do not change the scale and translation after files have been loaded
		graph.setResetViewOnRootChange(false);

		// Updates the modified flag if the graph model changes
		graph.getModel().addListener(mxEvent.CHANGE, changeTracker);

		// Adds the command history to the model and view
		graph.getModel().addListener(mxEvent.UNDO, undoHandler);
		graph.getView().addListener(mxEvent.UNDO, undoHandler);
		graph.setVertexLabelsMovable(true);

		// Keeps the selection in sync with the command history
		mxIEventListener undoHandler = new mxIEventListener() {
			public void invoke(Object source, mxEventObject evt) {
				List<mxUndoableChange> changes = ((mxUndoableEdit) evt.getProperty("edit")).getChanges();
				graph.setSelectionCells(graph.getSelectionCellsForChanges(changes));

			}
		};

		undoManager.addListener(mxEvent.UNDO, undoHandler);
		undoManager.addListener(mxEvent.REDO, undoHandler);

		modelingManager = new ModelingManager(mainWindow, modelingComponent);

		setClosable(true);
		setMaximizable(true);
		setIconifiable(true);
		setResizable(true);
		setBounds(20 * AppMainWindow.windowCount, AppMainWindow.windowCount, AppMainWindow.EDITOR_WIDTH,
				AppMainWindow.EDITOR_HEIGHT);
		setContentPane(modelingComponent);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setFrameIcon(null);
		setTitle(getTitle() + " " + ++AppMainWindow.windowCount);

		// Display some useful information about repaint events
		installRepaintListener();
		// Installs rubberband selection and handling for some special
		// keystrokes such as F2, Control-C, -V, X, A etc.
		installHandlers();
		installListeners();
	}

	public void initialize() {
		// Set this internal frame to be selected
		try {
			setSelected(true);
			setMaximum(true);

		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}

		show();
	}

	public void dispose() {
		boolean canDispose = true;

		if (isModified()) {
			switch (JOptionPane.showConfirmDialog(this, mxResources.get("saveChanges"))) {
			case JOptionPane.YES_OPTION:

				SaveAction sa = new SaveAction(false);

				sa.actionPerformed2(this, mainWindow);

				break;

			case JOptionPane.CANCEL_OPTION:
				canDispose = false;
			}
		}

		if (canDispose) {
			List<ModelingEditor> list = mainWindow.getEditors();
			list.remove(this);
			ModelingEditor newEditor = null;
			if (list.size() > 0) {
				newEditor = list.get(0); // take first from list
			}
			mainWindow.setCurrentEditor(newEditor);

			super.dispose();
		}
	}

	protected mxUndoManager createUndoManager() {
		return new mxUndoManager();
	}

	protected void installHandlers() {
		rubberband = new mxRubberband(modelingComponent);
		rubberband.setBorderColor(Color.BLACK);
		rubberband.setFillColor(new Color(0, 0, 0, 50));
		keyboardHandler = new KeyboardHandler(modelingComponent);
	}

	public void updateKeyboardHandler() {
		keyboardHandler = new KeyboardHandler(modelingComponent);
	}

	protected void installRepaintListener() {
		modelingComponent.getGraph().addListener(mxEvent.REPAINT, new mxIEventListener() {
			@Override
			public void invoke(Object source, mxEventObject evt) {
				String buffer = (modelingComponent.getTripleBuffer() != null) ? "" : " (unbuffered)";
				mxRectangle dirty = (mxRectangle) evt.getProperty("region");

				if (dirty == null) {
					mainWindow.status("Repaint all" + buffer, null);
				} else {
					mainWindow.status("Repaint: x=" + (int) (dirty.getX()) + " y=" + (int) (dirty.getY()) + " w="
							+ (int) (dirty.getWidth()) + " h=" + (int) (dirty.getHeight()) + buffer, null);
				}
			}
		});
	}

	protected void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() < 0) {
			modelingComponent.zoomIn();
		} else {
			modelingComponent.zoomOut();
		}

		mainWindow.status(
				mxResources.get("scale") + ": " + (int) (100 * modelingComponent.getGraph().getView().getScale()) + "%",
				null);
	}

	protected void showGraphPopupMenu(MouseEvent e) {
		Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), modelingComponent);
		PopupMenu menu = new PopupMenu(mainWindow);
		menu.show(modelingComponent, pt.x, pt.y);

		e.consume();
	}

	protected void mouseLocationChanged(MouseEvent e) {
		mainWindow.status(e.getX() + ", " + e.getY(), null);
	}

	protected void installListeners() {
		// Installs mouse wheel listener for zooming
		MouseWheelListener wheelTracker = new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getSource() instanceof mxGraphOutline || e.isControlDown()) {
					mouseWheelMoved(e);
				}
			}
		};

		// Handles mouse wheel events in the graph component
		modelingComponent.addMouseWheelListener(wheelTracker);

		// Installs the popup menu in the graph component
		modelingComponent.getGraphControl().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				modelingManager.insertObjectToModeling(e.getX(), e.getY());
			}

			public void mousePressed(MouseEvent e) {
				// Handles context menu on the Mac where the trigger is
				// on mousepressed
				mouseReleased(e);
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showGraphPopupMenu(e);
				}
				if (!e.isConsumed() && modelingComponent.isEditEvent(e)) {
					Object cell = modelingComponent.getCellAt(e.getX(), e.getY(), false);

					if (cell != null && modelingComponent.getGraph().isCellEditable(cell)) {
						if (((mxCell) cell).getValue() instanceof ModelingObject) {
							// EventObject k = null;

							((ModelingComponent) modelingComponent).startEditingAtCell(cell, e);

						}
					}
				}

			}
		});

		// Installs a mouse motion listener to display the mouse location
		modelingComponent.getGraphControl().addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				mouseLocationChanged(e);
			}

			public void mouseMoved(MouseEvent e) {
				mouseDragged(e);
			}
		});

		addInternalFrameListener(new InternalFrameListener() {
			@Override
			public void internalFrameOpened(InternalFrameEvent arg0) {
			}

			@Override
			public void internalFrameIconified(InternalFrameEvent arg0) {
			}

			@Override
			public void internalFrameDeiconified(InternalFrameEvent arg0) {
			}

			@Override
			public void internalFrameDeactivated(InternalFrameEvent arg0) {
			}

			@Override
			public void internalFrameClosing(InternalFrameEvent arg0) {
				arg0.getInternalFrame().dispose();
			}

			@Override
			public void internalFrameClosed(InternalFrameEvent arg0) {
			}

			@Override
			public void internalFrameActivated(InternalFrameEvent arg0) {
				mainWindow.setCurrentEditor((ModelingEditor) arg0.getInternalFrame());
			}
		});
	}

	public void setCurrentFile(File file) {
		File oldValue = currentFile;
		currentFile = file;

		firePropertyChange("currentFile", oldValue, file);

		if (oldValue != file) {
			mainWindow.updateTitle();
		}
	}

	public File getCurrentFile() {
		return currentFile;
	}

	/**
	 *
	 * @param modified
	 */
	public void setModified(boolean modified) {
		boolean oldValue = this.modified;
		this.modified = modified;

		firePropertyChange("modified", oldValue, modified);

		if (oldValue != modified) {
			mainWindow.updateTitle();
		}
	}

	/**
	 *
	 * @return whether or not the current graph has been modified
	 */
	public boolean isModified() {
		return modified;
	}

	public boolean isConceptualModeling() {
		return conceptualModeling;
	}

	public boolean isRelationalModeling() {
		return relationalModeling;
	}

	public mxGraphComponent getGraphComponent() {
		return modelingComponent;
	}

	public mxUndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * Creates an action that executes the specified layout.
	 *
	 * @param key
	 *            Key to be used for getting the label from mxResources and also
	 *            to create the layout instance for the commercial graph editor
	 *            example.
	 * @return an action that executes the specified layout
	 */
	public Action graphLayout(final String key, boolean animate) {
		final mxIGraphLayout layout = createLayout(key, animate);

		if (layout != null) {
			return new AbstractAction(mxResources.get(key)) {
				public void actionPerformed(ActionEvent e) {
					final mxGraph graph = modelingComponent.getGraph();
					Object cell = graph.getSelectionCell();

					if (cell == null || graph.getModel().getChildCount(cell) == 0) {
						cell = graph.getDefaultParent();
					}

					graph.getModel().beginUpdate();
					try {
						long t0 = System.currentTimeMillis();
						layout.execute(cell);
						mainWindow.status("Layout: " + (System.currentTimeMillis() - t0) + " ms", null);
					} finally {
						mxMorphing morph = new mxMorphing(modelingComponent, 20, 1.2, 20);

						morph.addListener(mxEvent.DONE, new mxIEventListener() {
							public void invoke(Object sender, mxEventObject evt) {
								graph.getModel().endUpdate();
							}
						});

						morph.startAnimation();
					}

				}
			};
		} else {
			return new AbstractAction(mxResources.get(key)) {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(modelingComponent, mxResources.get("noLayout"));
				}
			};
		}
	}

	/**
	 * Creates a layout instance for the given identifier.
	 */
	protected mxIGraphLayout createLayout(String ident, boolean animate) {
		mxIGraphLayout layout = null;

		if (ident != null) {
			mxGraph graph = modelingComponent.getGraph();

			if (ident.equals("verticalHierarchical")) {
				layout = new mxHierarchicalLayout(graph);
			} else if (ident.equals("horizontalHierarchical")) {
				layout = new mxHierarchicalLayout(graph, JLabel.WEST);
			} else if (ident.equals("verticalTree")) {
				layout = new mxCompactTreeLayout(graph, false);
			} else if (ident.equals("horizontalTree")) {
				layout = new mxCompactTreeLayout(graph, true);
			} else if (ident.equals("parallelEdges")) {
				layout = new mxParallelEdgeLayout(graph);
			} else if (ident.equals("placeEdgeLabels")) {
				layout = new mxEdgeLabelLayout(graph);
			} else if (ident.equals("organicLayout")) {
				layout = new mxOrganicLayout(graph);
			}
			if (ident.equals("verticalPartition")) {
				layout = new mxPartitionLayout(graph, false) {
					/**
					 * Overrides the empty implementation to return the size of
					 * the graph control.
					 */
					public mxRectangle getContainerSize() {
						return modelingComponent.getLayoutAreaSize();
					}
				};
			} else if (ident.equals("horizontalPartition")) {
				layout = new mxPartitionLayout(graph, true) {
					/**
					 * Overrides the empty implementation to return the size of
					 * the graph control.
					 */
					public mxRectangle getContainerSize() {
						return modelingComponent.getLayoutAreaSize();
					}
				};
			} else if (ident.equals("verticalStack")) {
				layout = new mxStackLayout(graph, false) {
					/**
					 * Overrides the empty implementation to return the size of
					 * the graph control.
					 */
					public mxRectangle getContainerSize() {
						return modelingComponent.getLayoutAreaSize();
					}
				};
			} else if (ident.equals("horizontalStack")) {
				layout = new mxStackLayout(graph, true) {
					/**
					 * Overrides the empty implementation to return the size of
					 * the graph control.
					 */
					public mxRectangle getContainerSize() {
						return modelingComponent.getLayoutAreaSize();
					}
				};
			} else if (ident.equals("circleLayout")) {
				layout = new mxCircleLayout(graph);
			}
		}

		return layout;
	}
}
