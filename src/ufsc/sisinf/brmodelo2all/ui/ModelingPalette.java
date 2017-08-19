package ufsc.sisinf.brmodelo2all.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;

import ufsc.sisinf.brmodelo2all.app.BrModelo2All;

public class ModelingPalette extends JToolBar {

	private static final long serialVersionUID = 7771113885935187066L;
	protected mxEventSource eventSource = new mxEventSource(this);
	protected List<mxGraphTransferable> transferables = new ArrayList<mxGraphTransferable>();
	protected JButton selectedEntry = null;
	protected JButton pointer = null;
	protected final AppMainWindow mainWindow;

	@SuppressWarnings("serial")
	public ModelingPalette(final AppMainWindow mainWindow) {
		this.mainWindow = mainWindow;

		setPointerButton();

		// Clears the current selection when the background is clicked
		addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
				clearSelection();
			}

			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}
		});

		// Shows a nice icon for drag and drop but doesn't import anything
		setTransferHandler(new TransferHandler() {
			public boolean canImport(JComponent comp, DataFlavor[] flavors) {
				return true;
			}
		});
	}

	public void setPointerButton() {
		ImageIcon icon = new ImageIcon(
				BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/conceitual/pointer.png"));
		pointer = new JButton(icon);
		pointer.setPreferredSize(new Dimension(28, 28));
		pointer.setVerticalTextPosition(JButton.BOTTOM);
		pointer.setHorizontalTextPosition(JButton.CENTER);
		pointer.setIconTextGap(0);
		pointer.setSelected(true);
		pointer.setToolTipText("Ponteiro");

		pointer.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
				clearSelection();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});

		add(pointer);
		addSeparator();
	}

	public JButton getSelectedEntry() {
		return selectedEntry;
	}

	public void clearSelection() {
		if (selectedEntry != null) {
			selectedEntry.setSelected(false);
			pointer.setSelected(true);
		}
		setSelectionEntry(null, null);
	}

	public void setSelectionEntry(JButton entry, mxGraphTransferable t) {
		JButton previous = selectedEntry;
		if (mainWindow.getCurrentEditor() == null) { // || previous == entry) {
			selectedEntry = null;
		} else {
			selectedEntry = entry;
		}

		eventSource.fireEvent(
				new mxEventObject(mxEvent.SELECT, "entry", selectedEntry, "transferable", t, "previous", previous));
	}

	public void setPreferredWidth(int width) {
		int cols = Math.max(1, width / 55);
		setPreferredSize(
				new Dimension(width, (getComponentCount() * 55 / cols)/*
																		 * + 30
																		 */));
		revalidate();
	}

	/**
	 *
	 * @param name
	 * @param icon
	 * @param style
	 * @param width
	 * @param height
	 * @param value
	 */
	public void addEdgeTemplate(final String name, ImageIcon icon, String style, int width, int height, Object value) {
		mxGeometry geometry = new mxGeometry(0, 0, width, height);
		geometry.setTerminalPoint(new mxPoint(0, height), true);
		geometry.setTerminalPoint(new mxPoint(width, 0), false);
		geometry.setRelative(true);

		mxCell cell = new mxCell(value, geometry, style);
		cell.setEdge(true);

		addTemplate(name, icon, cell);
	}

	/**
	 *
	 * @param name
	 * @param icon
	 * @param style
	 * @param width
	 * @param height
	 * @param value
	 */
	public void addTemplate(final String name, ImageIcon icon, String style, int width, int height, Object value) {
		mxCell cell = new mxCell(value, new mxGeometry(0, 0, width, height), style);
		cell.setVertex(true);
		addTemplate(name, icon, cell);
	}

	/**
	 *
	 * @param name
	 * @param icon
	 * @param cell
	 */
	public void addTemplate(final String name, ImageIcon icon, mxCell cell) {
		mxRectangle bounds = (mxGeometry) cell.getGeometry().clone();
		final mxGraphTransferable t = new mxGraphTransferable(new Object[] { cell }, bounds);
		transferables.add(t);

		final JButton entry = new JButton(icon);
		entry.setPreferredSize(new Dimension(28, 28));
		entry.setVerticalTextPosition(JButton.BOTTOM);
		entry.setHorizontalTextPosition(JButton.CENTER);
		entry.setIconTextGap(0);

		entry.setToolTipText(name);

		entry.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
				if (selectedEntry != null) {
					selectedEntry.setSelected(false);
				}

				mainWindow.status(null, null);
				setSelectionEntry(entry, t);

				if (name.equals(mxResources.get("disjointGeneralization"))) {
					mainWindow.getCurrentEditor().getModelingManager().getDisjunctionGeometry(null);
				} else {
					entry.setSelected(true);
					pointer.setSelected(false);
				}
			}

			public void mouseClicked(MouseEvent e) {
				setStatusBarMessage(null);
			}

			public void mouseEntered(MouseEvent e) {
				setStatusBarMessage(entry.getToolTipText());
			}

			public void mouseExited(MouseEvent e) {
				setStatusBarMessage(null);
			}

			public void mouseReleased(MouseEvent e) {
			}
		});

		add(entry);
	}

	public mxGraphTransferable getTransferable(int index) {
		return transferables.get(index);
	}

	/**
	 * @param eventName
	 * @param mxIEventListener
	 * @see com.mxgraph.util.mxEventSource#addListener(java.lang.String,
	 *      com.mxgraph.util.mxEventSource.mxIEventListener)
	 */
	public void addListener(String eventName, mxEventSource.mxIEventListener mxIEventListener) {
		eventSource.addListener(eventName, mxIEventListener);
	}

	/**
	 * @return whether or not event are enabled for this palette
	 * @see com.mxgraph.util.mxEventSource#isEventsEnabled()
	 */
	public boolean isEventsEnabled() {
		return eventSource.isEventsEnabled();
	}

	/**
	 * @param listener
	 * @see com.mxgraph.util.mxEventSource#removeListener(com.mxgraph.util.mxEventSource.mxIEventListener)
	 */
	public void removeListener(mxIEventListener listener) {
		eventSource.removeListener(listener);
	}

	/**
	 * @param eventName
	 * @param listener
	 * @see com.mxgraph.util.mxEventSource#removeListener(java.lang.String,
	 *      com.mxgraph.util.mxEventSource.mxIEventListener)
	 */
	public void removeListener(mxIEventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}

	/**
	 * @param eventsEnabled
	 * @see com.mxgraph.util.mxEventSource#setEventsEnabled(boolean)
	 */
	public void setEventsEnabled(boolean eventsEnabled) {
		eventSource.setEventsEnabled(eventsEnabled);
	}

	public void setStatusBarMessage(String message) {

		if (message == null) {
			mainWindow.status(null, null);
			return;
		}

		String status = "";

		if (mainWindow.getSelectedEntry() == null) {
			if (message == mxResources.get("entity"))
				status = mxResources.get("insertsEntity");
			else if (message == mxResources.get("relation"))
				status = mxResources.get("insertsRelation");
			else if (message == mxResources.get("selfRelation"))
				status = mxResources.get("insertsSelfRelation");
			else if (message == mxResources.get("inheritance"))
				status = mxResources.get("insertsInheritance");
			else if (message == mxResources.get("exclusiveInheritance"))
				status = mxResources.get("insertsExclusiveInheritance");
			else if (message == mxResources.get("nonExclusiveInheritance"))
				status = mxResources.get("insertsNonExclusiveInheritance");
			else if (message == mxResources.get("associativeEntity"))
				status = mxResources.get("insertsAssociativeEntity");
			else if (message == mxResources.get("attribute"))
				status = mxResources.get("insertsAttribute");
			else if (message == mxResources.get("identifierAttribute"))
				status = mxResources.get("insertsIdentifierAttribute");
			else if (message == mxResources.get("composedAttribute"))
				status = mxResources.get("insertsComposedAttribute");
			else if (message == mxResources.get("connector"))
				status = mxResources.get("insertsConnector");
			else if (message == mxResources.get("table"))
				status = mxResources.get("insertsTable");
			else if (message == mxResources.get("field"))
				status = mxResources.get("insertsField");
			else if (message == mxResources.get("primaryKey"))
				status = mxResources.get("insertsPrimaryKey");
			else if (message == mxResources.get("foreignKey"))
				status = mxResources.get("insertsForeignKey");
			else if (message == mxResources.get("collection"))
				status = mxResources.get("insertsCollection");
			else if (message == mxResources.get("noSqlAttribute"))
				status = mxResources.get("insertsNoSqlAttribute");
			else if (message == mxResources.get("noSqlIdentifierAttribute"))
				status = mxResources.get("insertsNoSqlIdentifierAttribute");
			else if (message == mxResources.get("noSqlReferenceAttribute"))
				status = mxResources.get("insertsNoSqlReferenceAttribute");
			else if (message == mxResources.get("block"))
				status = mxResources.get("insertsBlock");

		}
		mainWindow.status(null, status);
		mainWindow.statusBar.setFont(new Font(mainWindow.statusBar.getFont().getFamily(), Font.BOLD, 11));
	}
}