package ufsc.sisinf.brmodelo2all.control;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

import ufsc.sisinf.brmodelo2all.app.BrModelo2All;
import ufsc.sisinf.brmodelo2all.model.Cardinality;
import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.AssociativeEntityObject;
import ufsc.sisinf.brmodelo2all.model.objects.AssociativeRelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.AttributeObject;
import ufsc.sisinf.brmodelo2all.model.objects.Collection;
import ufsc.sisinf.brmodelo2all.model.objects.ColumnObject;
import ufsc.sisinf.brmodelo2all.model.objects.ConnectorObject;
import ufsc.sisinf.brmodelo2all.model.objects.DisjunctionObject;
import ufsc.sisinf.brmodelo2all.model.objects.EntityObject;
import ufsc.sisinf.brmodelo2all.model.objects.InheritanceObject;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;
import ufsc.sisinf.brmodelo2all.model.objects.NoSqlAttributeObject;
import ufsc.sisinf.brmodelo2all.model.objects.RelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.TableObject;
import ufsc.sisinf.brmodelo2all.model.objects.WritingBoxObject;
import ufsc.sisinf.brmodelo2all.ui.AppMainWindow;
import ufsc.sisinf.brmodelo2all.ui.JanelaComposto;
import ufsc.sisinf.brmodelo2all.ui.JanelaEstrangeira;
import ufsc.sisinf.brmodelo2all.util.Messages;

public class ModelingManager {
	public static double BLOCK_HEIGHT = 40.0D;
	public static double BLOCK_WIDTH_PERCENTAGE = 0.3D;
	public static double NOSQL_ATTRIBUTE_HEIGHT = 20.0D;
	public static double NOSQL_ATTRIBUTE_WIDTH_PERCENTAGE = 0.4D;
	public static double DISJUNCTION_CELL_WIDTH = 20.0D;
	protected final AppMainWindow mainWindow;
	protected ModelingComponent modelingComponent;
	protected Object source = null;
	protected Object target = null;
	protected Object sourceCardinality = null;
	protected Object targetCardinality = null;
	protected boolean control = false;
	private List<mxPoint> value;
	private mxICell sourceCell = null;
	private mxICell targetCell = null;

	public Object getSourceCardinality() {
		return this.sourceCardinality;
	}

	public void setSourceCardinality(Object sourceCardinality) {
		this.sourceCardinality = sourceCardinality;
	}

	public Object getTargetCardinality() {
		return this.targetCardinality;
	}

	public void setTargetCardinality(Object targetCardinality) {
		this.targetCardinality = targetCardinality;
	}

	protected static enum Connection {
		ENTITY_TO_ENTITY, ENTITY_TO_RELATION, RELATION_TO_ENTITY, ENTITY_TO_INHERITANCE, TABLES, NONE, ERROR;
	}

	public ModelingManager(AppMainWindow mainWindow, mxGraphComponent modelingComponent) {
		this.mainWindow = mainWindow;
		this.modelingComponent = ((ModelingComponent) modelingComponent);
	}

	public void insertObject(Object object) {
		mxGraph graph = this.modelingComponent.getGraph();
		graph.addCell(object);
	}

	public void insertObjectToModeling(double x, double y) {
		String selectedEntry = this.mainWindow.getSelectedEntry();

		double scale = this.modelingComponent.getGraph().getView().getScale();
		double scaleX = x / scale;
		double scaleY = y / scale;
		if (selectedEntry != null) {
			boolean clearSelection = false;

			mxGraph graph = this.modelingComponent.getGraph();

			Object insertedCell = null;
			if (selectedEntry.equals(mxResources.get("entity"))) {
				insertedCell = graph.insertVertex(graph.getDefaultParent(), null,
						new EntityObject(mxResources.get("entity")), scaleX, scaleY, 120.0D, 50.0D, "entity");
			} else if (selectedEntry.equals(mxResources.get("relation"))) {
				insertedCell = graph.insertVertex(graph.getDefaultParent(), null,
						new RelationObject(mxResources.get("relation")), scaleX, scaleY, 120.0D, 50.0D, "relation");
			} else if (selectedEntry.equals(mxResources.get("selfRelation"))) {
				insertedCell = insertSelfRelatedObject(x, y);
			} else if (selectedEntry.equals(mxResources.get("inheritance"))) {
				insertedCell = insertInheritanceObject(x, y);
			} else if (selectedEntry.equals(mxResources.get("exclusiveInheritance"))) {
				insertedCell = insertExclusiveInheritanceObject(x, y);
			} else if (selectedEntry.equals(mxResources.get("nonExclusiveInheritance"))) {
				insertedCell = insertNonExclusiveInheritanceObject(x, y);
			} else if (selectedEntry.equals(mxResources.get("associativeEntity"))) {
				insertedCell = insertAssociativeEntityObject(scaleX, scaleY);
			} else if (selectedEntry.equals(mxResources.get("composedAttribute"))) {
				insertedCell = insertComposedAttributeObject(x, y);
			} else if ((selectedEntry.equals(mxResources.get("attribute"))) ||

					(selectedEntry.equals(mxResources.get("identifierAttribute")))) {
				insertedCell = insertAttributeObject((int) x, (int) y);
			} else if (selectedEntry.equals(mxResources.get("connector"))) {
				clearSelection = insertConnectorObject((int) x, (int) y);
			} else if (selectedEntry.equals(mxResources.get("type"))) {
				insertedCell = insertComposedAttributeObject(x, y);
			} else if (selectedEntry.equals("WritingBox")) {
				insertedCell = graph.insertVertex(graph.getDefaultParent(), null,
						new WritingBoxObject("Insira seu comentario"), scaleX, scaleY, 200.0D, 150.0D, "wb");
			} else if (selectedEntry.equals(mxResources.get("table"))) {
				insertedCell = graph.insertVertex(graph.getDefaultParent(), null,
						new TableObject(mxResources.get("table")), scaleX, scaleY, 150.0D, 30.0D, "table");
			} else if ((selectedEntry.equals(mxResources.get("primaryKey")))
					|| (selectedEntry.equals(mxResources.get("foreignKey")))
					|| (selectedEntry.equals(mxResources.get("field")))) {
				insertedCell = insertColumnObject(x, y, selectedEntry);
			} else if (selectedEntry.equals(mxResources.get("collection"))) {
				insertedCell = graph.insertVertex(graph.getDefaultParent(), null,
						new Collection(mxResources.get("collection"), false), scaleX, scaleY, 200.0D, 40.0D,
						"collection");
			} else if ((selectedEntry.equals(mxResources.get("noSqlAttribute"))) ||

					(selectedEntry.equals(mxResources.get("noSqlIdentifierAttribute")))) {
				insertedCell = insertNoSqlAttributeObject(x, y, selectedEntry, null);
			} else if (selectedEntry.equals(mxResources.get("noSqlReferenceAttribute"))) {
				clearSelection = insertNoSqlReferenceAttribute((int) x, (int) y);
			} else if (selectedEntry.equals(mxResources.get("block"))) {
				insertedCell = insertBlock(x, y, null);
			} else if (selectedEntry.equals(mxResources.get("disjointGeneralization"))) {
				clearSelection = true;
			}
			if (insertedCell != null) {
				this.mainWindow.clearModelingPalette();
				this.mainWindow.status(null, null);
				this.source = null;
				this.target = null;
				this.mainWindow.properties(insertedCell, this.modelingComponent);
			} else if (clearSelection) {
				this.mainWindow.clearModelingPalette();
				this.mainWindow.status(null, null);
				this.source = null;
				this.target = null;
			}
		}
	}

	public double resizeParentCells(mxGeometry geometry, mxGraph graph, double objectHeight) {
		mxRectangle newBounds = new mxRectangle(geometry.getRectangle());
		mxICell parentCell = this.sourceCell.getParent();
		newBounds.setHeight(newBounds.getHeight() + objectHeight + 5.0D);
		double newY = newBounds.getHeight() - (objectHeight + 20.0D);
		graph.resizeCell(this.sourceCell, newBounds);
		while (parentCell != graph.getDefaultParent()) {
			mxGeometry parentGeometry = parentCell.getGeometry();
			mxRectangle parentNewBounds = new mxRectangle(parentGeometry.getRectangle());
			parentNewBounds.setHeight(parentNewBounds.getHeight() + 20.0D);
			graph.resizeCell(parentCell, parentNewBounds);
			parentCell = parentCell.getParent();
		}
		return newY;
	}

	public Object addObject(mxICell sourceCell, String objectType, double objectHeight, double widthPercentage) {
		Object object = null;
		mxGraph graph = this.modelingComponent.getGraph();
		mxGeometry geometry = sourceCell.getGeometry();
		double width = geometry.getWidth() - geometry.getWidth() * widthPercentage;
		double newY = resizeParentCells(geometry, graph, objectHeight);
		if (objectType.equals("block")) {
			object = graph.insertVertex(sourceCell, null, new Collection(mxResources.get(objectType), true), 10.0D,
					newY, width, objectHeight, "verticalAlign=top");
		} else if ((objectType.equals("noSqlAttribute")) || (objectType.equals("noSqlIdentifierAttribute"))) {
			object = graph.insertVertex(sourceCell, null,
					new NoSqlAttributeObject(mxResources.get(objectType), sourceCell,
							objectType.equals("noSqlIdentifierAttribute"),
							objectType.equals("noSqlReferenceAttribute")),
					10.0D, newY, width, objectHeight, objectType);
			graph.setCellStyles(mxConstants.STYLE_ALIGN, "left", new Object[] { object });
		} else {
			object = graph.insertVertex(sourceCell, null, new NoSqlAttributeObject(objectType, sourceCell, false, true),
					10.0D, newY, width, objectHeight, objectType);
			graph.setCellStyles(mxConstants.STYLE_ALIGN, "left", new Object[] { object });
		}
		sourceCell.insert((mxICell) object);
		graph.setVertexLabelsMovable(false);
		((Collection) sourceCell.getValue()).addChildObject(object);
		if ((objectType.equals("noSqlAttribute")) || (objectType.equals("noSqlIdentifierAttribute"))) {
			((Collection) sourceCell.getValue()).addAttribute((NoSqlAttributeObject) ((mxICell) object).getValue());
		}
		return object;
	}

	public Object addDisjunction(mxICell sourceCell, double disjunctionX, double disjunctionY, double disjunctionHeight,
			int numberOfCells, Object[] selectedObjects) {
		Object object = null;
		mxGraph graph = this.modelingComponent.getGraph();
		mxGraphics2DCanvas.PRESERVE_IMAGE_ASPECT = false;

		object = graph.insertVertex(sourceCell, null, new DisjunctionObject("", sourceCell), 10.0D, disjunctionY - 4.0D,
				DISJUNCTION_CELL_WIDTH, disjunctionHeight, "shape=image;image="
						+ BrModelo2All.class.getResource("/ufsc/sisinf/brmodelo2all/ui/images/nosql/bracket_icon.png"));

		for (Object selectedObject : selectedObjects) {
			((DisjunctionObject) ((mxICell) object).getValue()).addToChildList(selectedObject);
		}
		((Collection) sourceCell.getValue()).addChildObject(object);

		new mxRectangle(10.0D, disjunctionY - 4.0D, DISJUNCTION_CELL_WIDTH, disjunctionHeight);
		graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, "none", new Object[] { object });
		graph.setCellStyles(mxConstants.STYLE_OVERFLOW, "hidden", new Object[] { object });
		return object;
	}

	public Object insertBlock(double x, double y, mxICell cell) {
		Object block = null;
		Object object;
		if (cell != null) {
			object = cell;
		} else {
			object = this.modelingComponent.getCellAt((int) x, (int) y);
		}
		if ((object instanceof mxICell)) {
			this.sourceCell = ((mxICell) object);
			mxICell parentCell = this.sourceCell.getParent();
			if (this.sourceCell.getStyle().equals("verticalAlign=top")) {
				block = addObject(this.sourceCell, "block", BLOCK_HEIGHT, BLOCK_WIDTH_PERCENTAGE);
			} else {
				if ((!this.sourceCell.getStyle().equals("noSqlAttribute"))
						&& (!this.sourceCell.getStyle().equals("noSqlIdentifierAttribute"))) {
					if (this.sourceCell.getStyle().equals("noSqlReferenceAttribute")) {
						if (!this.sourceCell.getParent().getStyle().equals("verticalAlign=top")) {
						}
					}
				} else {
					this.sourceCell = this.sourceCell.getParent();
					block = addObject(this.sourceCell, "block", BLOCK_HEIGHT, BLOCK_WIDTH_PERCENTAGE);
				}
				JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
						Messages.getString("ModelingManager.warning.needs.notACollection"),
						Messages.getString("ModelingManager.warning.window.advice"), 0);
			}
		} else {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
					Messages.getString("ModelingManager.warning.needs.selectACollectionForBlockInsertion"),
					Messages.getString("ModelingManager.warning.window.advice"), 0);
		}
		label258: return block;
	}

	public Object insertNoSqlAttributeObject(double x, double y, String attributeType, mxICell cell) {
		Object noSqlAttributeObject = null;
		Object object;
		if (cell != null) {
			object = cell;
		} else {
			object = this.modelingComponent.getCellAt((int) x, (int) y);
		}
		String style;
		if (attributeType.equals(mxResources.get("noSqlAttribute"))) {
			style = "noSqlAttribute";
		} else {
			if (attributeType.equals(mxResources.get("noSqlIdentifierAttribute"))) {
				style = "noSqlIdentifierAttribute";
			} else {
				style = attributeType;
			}
		}
		if ((object instanceof mxICell)) {
			this.sourceCell = ((mxICell) object);
			if (this.sourceCell.getStyle().equals("verticalAlign=top")) {
				noSqlAttributeObject = addObject(this.sourceCell, style, NOSQL_ATTRIBUTE_HEIGHT,
						NOSQL_ATTRIBUTE_WIDTH_PERCENTAGE);
			} else {
				if (!this.sourceCell.getStyle().equals("noSqlAttribute")) {
					if (this.sourceCell.getStyle().equals("noSqlIdentifierAttribute")) {
						if (!this.sourceCell.getParent().getStyle().equals("verticalAlign=top")) {
						}
					}
				} else {
					this.sourceCell = this.sourceCell.getParent();
					noSqlAttributeObject = addObject(this.sourceCell, style, NOSQL_ATTRIBUTE_HEIGHT,
							NOSQL_ATTRIBUTE_WIDTH_PERCENTAGE);

				}
				JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
						Messages.getString("ModelingManager.warning.needs.notACollectionOrBlock"),
						Messages.getString("ModelingManager.warning.window.advice"), 0);
			}
		} else {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
					Messages.getString("ModelingManager.warning.needs.selectACollectionOrBlock"),
					Messages.getString("ModelingManager.warning.window.advice"), 0);
		}
		label274: return noSqlAttributeObject;
	}

	/**
	 * @author Fabio Volkamnn Coelho
	 * 
	 *         This method is specific for creating ref on the NoSQL pallete.
	 *
	 * @param x
	 *            position of the x-axis on the modeling graph
	 * @param y
	 *            position of the y-axis on the modeling graph
	 * @return boolean telling to clear the selection from the choosen objects
	 */
	/*
	 * Source is the block with the identifier attribute and the target is the
	 * where the reference is gonna go. First click is stored in the source, and
	 * the second click is stored in the target. While the target is null wait.
	 * Some tmp are created to understand better the code. the refAttribute
	 * store the identifier for later use on convertion.
	 */
	public boolean insertNoSqlReferenceAttribute(int x, int y) {
		boolean clearSelection = false;

		if (source == null)
			source = modelingComponent.getCellAt(x, y);
		else
			target = modelingComponent.getCellAt(x, y);

		if (target == null) {
			return clearSelection;
		}
		Collection sourceBlock = null;
		clearSelection = true;

		if (((mxICell) source).getValue() instanceof Collection)
			sourceBlock = (Collection) ((mxICell) source).getValue();
		else {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
					Messages.getString("ModelingManager.warning.needs.mustHaveIdentifierAttribute"),
					Messages.getString("ModelingManager.warning.window.advice"), 0);
			return clearSelection;
		}

		Object refObject = null;
		for (Object blockChild : sourceBlock.getChildObjects()) {
			if (((mxICell) blockChild).getValue() instanceof NoSqlAttributeObject) {
				NoSqlAttributeObject identifierAttribute = (NoSqlAttributeObject) ((mxICell) blockChild).getValue();
				if (identifierAttribute.isIdentifierAttribute()) {
					refObject = insertNoSqlAttributeObject(x, y, sourceBlock.toString() + "_REF", (mxICell) target);
					NoSqlAttributeObject refAttribute = (NoSqlAttributeObject) ((mxICell) refObject).getValue();
					refAttribute.setReferencedObject(identifierAttribute);
				}
			}
		}

		if (refObject == null) {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
					Messages.getString("ModelingManager.warning.needs.mustHaveIdentifierAttribute"),
					Messages.getString("ModelingManager.warning.window.advice"), 0);
			return clearSelection;
		}
		return clearSelection;
	}

	public boolean insertNoSqlReferenceAttribute(int x, int y, mxICell existingSource, mxICell existingTarget) {
		boolean clearSelection = false;
		if (this.source == null) {
			if (existingSource != null) {
				this.source = existingSource;
			} else {
				this.source = this.modelingComponent.getCellAt(x, y);
			}
			if (((this.source instanceof mxICell)) && ((((mxICell) this.source).getValue() instanceof Collection))
					&& (((mxICell) this.source).isVertex())) {
				this.sourceCell = ((mxICell) this.source);
			} else if (((this.source instanceof mxICell))
					&& ((((mxICell) this.source).getValue() instanceof NoSqlAttributeObject))) {
				mxICell cell = (mxICell) this.source;
				this.sourceCell = cell.getParent();
			} else {
				this.source = null;

				JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
						Messages.getString("ModelingManager.warning.needs.mustHaveIdentifierAttribute"),
						Messages.getString("ModelingManager.warning.window.advice"), 0);
			}
		}
		if (this.target == null) {
			if (existingTarget != null) {
				this.target = existingTarget;
			} else {
				this.target = this.modelingComponent.getCellAt(x, y);
			}
			if (((this.target instanceof mxICell)) && ((((mxICell) this.target).getValue() instanceof Collection))
					&& (((mxICell) this.target).isVertex()) && (this.source != this.target)) {
				this.targetCell = ((mxICell) this.target);
				referenceEntities();
				clearSelection = true;
				return clearSelection;
			}
			if (((this.target instanceof mxICell))
					&& ((((mxICell) this.target).getValue() instanceof NoSqlAttributeObject))
					&& (((mxICell) this.target).isVertex())) {
				mxICell cell = (mxICell) this.target;
				this.targetCell = cell.getParent();
				referenceEntities();
				clearSelection = true;
				return clearSelection;
			}
			clearSelection = true;
			this.source = null;
			this.sourceCell = null;
			this.target = null;
			this.targetCell = null;

			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
					Messages.getString("ModelingManager.warning.needs.mustReceiveReferenceAttribute"),
					Messages.getString("ModelingManager.warning.window.advice"), 0);
		}

		return clearSelection;
	}

	public void referenceEntities() {
		double x = this.targetCell.getGeometry().getX();
		double y = this.targetCell.getGeometry().getY();
		Collection sourceEntity = (Collection) this.sourceCell.getValue();
		// Creating the ref.
		insertNoSqlAttributeObject(x, y, sourceEntity.getName().toLowerCase() + "_REF", this.targetCell);

		this.source = null;
		this.sourceCell = null;
		this.target = null;
		this.targetCell = null;
	}

	public Object getDisjunctionGeometry(List<mxICell> disjointCells) {
		mxGraph graph = modelingComponent.getGraph();
		Object[] selectedObjects;
		if (disjointCells == null)
			selectedObjects = graph.getSelectionCells();
		else {
			selectedObjects = new Object[disjointCells.size()];
			for (int i = 0; i < disjointCells.size(); i++)
				selectedObjects[i] = disjointCells.get(i);
		}

		mxICell[] selectedCells = new mxICell[selectedObjects.length];
		int lastObject = selectedObjects.length - 1;
		double disjunctionX, disjunctionY, disjunctionHeight;
		disjunctionX = disjunctionY = disjunctionHeight = 0;

		for (int i = 0; i < selectedObjects.length; i++) {
			if (selectedObjects[i] instanceof mxICell && ((mxICell) selectedObjects[i]).getValue() instanceof Collection
					&& ((Collection) ((mxICell) selectedObjects[i]).getValue()).isBlock())
				selectedCells[i] = (mxICell) selectedObjects[i];
			if (selectedCells[i] == null) {
				JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
						Messages.getString("ModelingManager.warning.needs.selectOnlyBlocks"),
						Messages.getString("ModelingManager.warning.window.advice"), 0);
				return null;
			}
		}

		/*
		 * BUBBLE SORT *TODO* It sorts the array by the Y values of every cell.
		 * We need the first cell of the array to be the cell with the lower Y
		 * value, and the last one the cell with the higher Y value so it can
		 * calculate the height of the disjunction icon. Bubble sort is not the
		 * best way to do it, but since it's working I'll only try to optimize
		 * after the rest is done.
		 * 
		 * UPDATE: 5 months after the comment. Still haven't optimized it and
		 * probably never will. Just in case the program starts run slowly, this
		 * is more likely the reason why.
		 * 
		 */
		boolean sorted = false;

		while (!sorted) {
			sorted = true;
			for (int i = 0; i < selectedObjects.length - 1; i++) {
				if (selectedCells[i].getGeometry().getY() > selectedCells[i + 1].getGeometry().getY()) {
					mxICell aux = selectedCells[i];
					selectedCells[i] = selectedCells[i + 1];
					selectedCells[i + 1] = aux;

					sorted = false;
				}
			}
		}

		if (selectedCells.length < 2) {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
					Messages.getString("ModelingManager.warning.needs.selectAtLeastTwoBlocks"),
					Messages.getString("ModelingManager.warning.window.advice"), 0);
		} else {
			disjunctionX = selectedCells[0].getGeometry().getX();
			disjunctionY = selectedCells[0].getGeometry().getY();
			disjunctionHeight = (selectedCells[lastObject].getGeometry().getY() - disjunctionY)
					+ selectedCells[lastObject].getGeometry().getHeight();
			moveDisjointCells(selectedObjects);
			Object object = addDisjunction(selectedCells[0].getParent(), disjunctionX, disjunctionY, disjunctionHeight,
					selectedCells.length, selectedObjects);
		}
		return null;
	}

	public void moveDisjointCells(Object[] selectedObjects) {
		mxGraph graph = this.modelingComponent.getGraph();
		graph.moveCells(selectedObjects, DISJUNCTION_CELL_WIDTH, 0.0D);
	}

	private void removeCell(mxICell cell, mxICell newCell) {
		this.modelingComponent.getGraph().getModel().remove(cell);
		this.modelingComponent.getGraph().refresh();
	}

	public Object insertColumnObject(double x, double y, String columnType) {
		Object columnObject = null;
		Object object = this.modelingComponent.getCellAt((int) x, (int) y);
		String style = "column";
		if (columnType.equals(mxResources.get("primaryKey"))) {
			style = "primaryKey";
		} else if (columnType.equals(mxResources.get("foreignKey"))) {
			style = "foreignKey";
		} else if (columnType.equals(mxResources.get("bothKeys"))) {
			style = "bothKeys";
		}
		if ((object instanceof mxICell)) {
			mxGraph graph = this.modelingComponent.getGraph();
			this.sourceCell = ((mxICell) object);
			if (this.sourceCell.getStyle().equals("table")) {
				mxGeometry geometry = this.sourceCell.getGeometry();
				double width = geometry.getWidth();
				double height = 20.0D;
				double newY = this.sourceCell.getChildCount() * 20 + 25;
				mxRectangle newBounds = new mxRectangle(geometry.getRectangle());
				newBounds.setHeight(newBounds.getHeight() + 25.0D);
				graph.resizeCell(this.sourceCell, newBounds);
				columnObject = graph.insertVertex(this.sourceCell, null,
						new ColumnObject(columnType, this.sourceCell, style.equals("primaryKey"),
								style.equals("foreignKey"), style.equals("bothKeys")),
						0.0D, newY, width, height, style);
				this.sourceCell.insert((mxICell) columnObject);
				((TableObject) this.sourceCell.getValue()).addChildObject(columnObject);
			} else if (((this.sourceCell.getStyle().equals("column"))
					|| (this.sourceCell.getStyle().equals("primaryKey"))
					|| (this.sourceCell.getStyle().equals("foreignKey"))
					|| (this.sourceCell.getStyle().equals("bothKeys")))
					&& (this.sourceCell.getParent().getStyle().equals("table"))) {
				mxICell tableCell = this.sourceCell.getParent();
				mxGeometry geometry = tableCell.getGeometry();
				double width = geometry.getWidth();
				double height = 20.0D;
				double newY = tableCell.getChildCount() * 20 + 25;
				mxRectangle newBounds = new mxRectangle(geometry.getRectangle());
				newBounds.setHeight(newBounds.getHeight() + 25.0D);

				columnObject = graph.insertVertex(tableCell,
						null, new ColumnObject(columnType, tableCell, style.equals("primaryKey"),
								style.equals("foreignKey"), style.equals("bothKeys")),
						0.0D, newY, width, height, style);
				tableCell.insert((mxICell) columnObject);
				((TableObject) tableCell.getValue()).addChildObject(columnObject);

				graph.constrainChild(columnObject);
			} else {
				JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
						Messages.getString("N?o ? uma tabela"),
						Messages.getString("ModelingManager.warning.window.advice"), 0);
			}
		} else {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
					Messages.getString("Selecione uma tabela"),
					Messages.getString("ModelingManager.warning.window.advice"), 0);
		}
		return columnObject;
	}

	public Object insertColunaDireta(Object o, String columnType) {
		Object columnObject = null;
		Object object = o;
		String style = "column";

		style = "foreignKey";
		if ((object instanceof mxICell)) {
			mxGraph graph = this.modelingComponent.getGraph();
			this.sourceCell = ((mxICell) object);
			if (this.sourceCell.getStyle().equals("table")) {
				mxGeometry geometry = this.sourceCell.getGeometry();
				double width = geometry.getWidth();
				double height = 20.0D;
				double newY = this.sourceCell.getChildCount() * 20 + 25;
				mxRectangle newBounds = new mxRectangle(geometry.getRectangle());
				newBounds.setHeight(newBounds.getHeight() + 25.0D);
				graph.resizeCell(this.sourceCell, newBounds);
				columnObject = graph.insertVertex(this.sourceCell, null,
						new ColumnObject(columnType, this.sourceCell, style.equals("primaryKey"),
								style.equals("foreignKey"), style.equals("bothKeys")),
						0.0D, newY, width, height, style);
				this.sourceCell.insert((mxICell) columnObject);
				((TableObject) this.sourceCell.getValue()).addChildObject(columnObject);
			} else if (((this.sourceCell.getStyle().equals("column"))
					|| (this.sourceCell.getStyle().equals("primaryKey"))
					|| (this.sourceCell.getStyle().equals("foreignKey"))
					|| (this.sourceCell.getStyle().equals("bothKeys")))
					&& (this.sourceCell.getParent().getStyle().equals("table"))) {
				mxICell tableCell = this.sourceCell.getParent();
				mxGeometry geometry = tableCell.getGeometry();
				double width = geometry.getWidth();
				double height = 20.0D;
				double newY = tableCell.getChildCount() * 20 + 25;
				mxRectangle newBounds = new mxRectangle(geometry.getRectangle());
				newBounds.setHeight(newBounds.getHeight() + 25.0D);

				columnObject = graph.insertVertex(tableCell,
						null, new ColumnObject(columnType, tableCell, style.equals("primaryKey"),
								style.equals("foreignKey"), style.equals("bothKeys")),
						0.0D, newY, width, height, style);
				tableCell.insert((mxICell) columnObject);
				((TableObject) tableCell.getValue()).addChildObject(columnObject);

				graph.constrainChild(columnObject);
			} else {
				JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
						Messages.getString("ModelingManager.warning.notAnTable"),
						Messages.getString("ModelingManager.warning.window.advice"), 0);
			}
		} else {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
					Messages.getString("ModelingManager.warning.needs.selectTableToInsertField"),
					Messages.getString("ModelingManager.warning.window.advice"), 0);
		}
		return columnObject;
	}

	public Object insertColuna(Object o, String columnType) {
		Object columnObject = null;
		Object object = o;
		String style = "column";
		if ((object instanceof mxICell)) {
			mxGraph graph = this.modelingComponent.getGraph();
			this.sourceCell = ((mxICell) object);
			if (this.sourceCell.getStyle().equals("table")) {
				mxGeometry geometry = this.sourceCell.getGeometry();
				double width = geometry.getWidth();
				double height = 20.0D;
				double newY = this.sourceCell.getChildCount() * 20 + 25;
				mxRectangle newBounds = new mxRectangle(geometry.getRectangle());
				newBounds.setHeight(newBounds.getHeight() + 25.0D);
				graph.resizeCell(this.sourceCell, newBounds);
				columnObject = graph.insertVertex(this.sourceCell, null,
						new ColumnObject(columnType, this.sourceCell, style.equals("primaryKey"),
								style.equals("foreignKey"), style.equals("bothKeys")),
						0.0D, newY, width, height, style);
				this.sourceCell.insert((mxICell) columnObject);
				((TableObject) this.sourceCell.getValue()).addChildObject(columnObject);
			} else if (((this.sourceCell.getStyle().equals("column"))
					|| (this.sourceCell.getStyle().equals("primaryKey"))
					|| (this.sourceCell.getStyle().equals("foreignKey"))
					|| (this.sourceCell.getStyle().equals("bothKeys")))
					&& (this.sourceCell.getParent().getStyle().equals("table"))) {
				mxICell tableCell = this.sourceCell.getParent();
				mxGeometry geometry = tableCell.getGeometry();
				double width = geometry.getWidth();
				double height = 20.0D;
				double newY = tableCell.getChildCount() * 20 + 25;
				mxRectangle newBounds = new mxRectangle(geometry.getRectangle());
				newBounds.setHeight(newBounds.getHeight() + 25.0D);

				columnObject = graph.insertVertex(tableCell,
						null, new ColumnObject(columnType, tableCell, style.equals("primaryKey"),
								style.equals("foreignKey"), style.equals("bothKeys")),
						0.0D, newY, width, height, style);
				tableCell.insert((mxICell) columnObject);
				((TableObject) tableCell.getValue()).addChildObject(columnObject);

				graph.constrainChild(columnObject);
			} else {
				JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
						Messages.getString("ModelingManager.warning.notAnTable"),
						Messages.getString("ModelingManager.warning.window.advice"), 0);
			}
		} else {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
					Messages.getString("ModelingManager.warning.needs.selectTableToInsertField"),
					Messages.getString("ModelingManager.warning.window.advice"), 0);
		}
		return columnObject;
	}

	public Object insertInheritanceObject(double x, double y) {
		Object inheritanceObject = null;
		Object object = modelingComponent.getCellAt((int) x, (int) y);

		if (object instanceof mxICell) {
			mxGraph graph = modelingComponent.getGraph();
			sourceCell = (mxICell) object;

			if (sourceCell.getStyle().equals("entity")) { //$NON-NLS-1$
				graph.getModel().beginUpdate();
				try {
					mxGeometry geometry = sourceCell.getGeometry();
					double newX = geometry.getX() + (geometry.getWidth() / 2) - 15;
					double newY = geometry.getY() + (geometry.getHeight() * 1.5);

					EntityObject entity = (EntityObject) sourceCell.getValue();

					inheritanceObject = graph.insertVertex(graph.getDefaultParent(), null,
							new InheritanceObject(mxResources.get("inheritance"), sourceCell), newX, //$NON-NLS-1$
							newY, 30, 30, "inheritance"); //$NON-NLS-1$
					graph.insertEdge(object, "straight", null, object, //$NON-NLS-1$ ctang
							inheritanceObject, "straight"); //$NON-NLS-1$

					entity.setSpecialized(true);
					entity.addChildObject(inheritanceObject);
				} finally {
					graph.getModel().endUpdate();
				}
			} else {
				JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);
				JOptionPane.showMessageDialog(frame, Messages.getString("ModelingManager.warning.notAnEntity"), //$NON-NLS-1$
						Messages.getString("ModelingManager.warning.window.advice"), 0); //$NON-NLS-1$
			}
		} else {
			JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);
			JOptionPane.showMessageDialog(frame,
					Messages.getString("ModelingManager.warning.needs.selectEntityToSpecialize"), "Aviso", 0); //$NON-NLS-1$
		}

		return inheritanceObject;
	}

	public Object insertExclusiveInheritanceObject(double x, double y) {
		Object inheritanceObject = insertInheritanceObject(x, y);
		if (inheritanceObject != null) {
			mxGraph graph = modelingComponent.getGraph();
			graph.getModel().beginUpdate();
			try {
				sourceCell = (mxICell) inheritanceObject;
				mxGeometry geometry = sourceCell.getGeometry();
				double centerX = geometry.getCenterX();
				double newY = geometry.getY() + (geometry.getHeight() * 1.5);
				double newX = centerX - 150;

				InheritanceObject inheritance = (InheritanceObject) ((mxCell) inheritanceObject).getValue();
				inheritance.setExclusive(true);
				EntityObject entity1 = new EntityObject(mxResources.get("entity")); //$NON-NLS-1$
				entity1.setParentObject(inheritanceObject);
				Object newObject = graph.insertVertex(graph.getDefaultParent(), null, entity1, newX, newY, 120, 50,
						"entity"); //$NON-NLS-1$
				graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
						newObject, inheritanceObject, "straight"); //$NON-NLS-1$
				inheritance.addChildObject(newObject);

				newX = centerX + 30;
				EntityObject entity2 = new EntityObject(mxResources.get("entity")); //$NON-NLS-1$
				entity2.setParentObject(inheritanceObject);
				newObject = graph.insertVertex(graph.getDefaultParent(), null, entity2, newX, newY, 120, 50, "entity"); //$NON-NLS-1$
				graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
						newObject, inheritanceObject, "straight"); //$NON-NLS-1$
				inheritance.addChildObject(newObject);

			} finally {
				graph.getModel().endUpdate();
			}
		}

		return inheritanceObject;
	}

	public Object insertNonExclusiveInheritanceObject(double x, double y) {
		Object inheritanceObject = insertInheritanceObject(x, y);
		if (inheritanceObject != null) {
			mxGraph graph = modelingComponent.getGraph();
			graph.getModel().beginUpdate();
			try {
				sourceCell = (mxICell) inheritanceObject;
				mxGeometry geometry = sourceCell.getGeometry();
				double centerX = geometry.getCenterX();
				double newY = geometry.getY() + (geometry.getHeight() * 1.5);
				double newX = centerX - 60;
				InheritanceObject inheritance = (InheritanceObject) sourceCell.getValue();
				EntityObject entity = new EntityObject(mxResources.get("entity")); //$NON-NLS-1$
				entity.setParentObject(inheritanceObject);
				Object newObject = graph.insertVertex(graph.getDefaultParent(), null, entity, newX, newY, 120, 50,
						"entity"); //$NON-NLS-1$
				graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
						newObject, inheritanceObject, "straight"); //$NON-NLS-1$
				inheritance.addChildObject(newObject);
			} finally {
				graph.getModel().endUpdate();
			}
		}

		return inheritanceObject;
	}

	public Object insertAssociativeEntityObject(double x, double y) {
		Object associativeEntityObject = null;
		mxGraph graph = modelingComponent.getGraph();
		graph.getModel().beginUpdate();
		try {
			AssociativeRelationObject relationObject = new AssociativeRelationObject(mxResources.get("relation")); //$NON-NLS-1$
			AssociativeEntityObject entityObject = new AssociativeEntityObject(mxResources.get("associativeEntity"), //$NON-NLS-1$
					relationObject);
			associativeEntityObject = graph.insertVertex(graph.getDefaultParent(), null, entityObject, x, y, 150, 80,
					"associativeEntity"); //$NON-NLS-1$

			entityObject.addChildObject(graph.insertVertex(associativeEntityObject, null, relationObject, 15, 20, 120,
					50, "associativeRelation")); //$NON-NLS-1$
		} finally {
			graph.getModel().endUpdate();
		}

		return associativeEntityObject;
	}

	public Object insertComposedAttributeObject(double x, double y) {

		Object object = modelingComponent.getCellAt((int) x, (int) y);

		if (object != null) {

			JanelaComposto jc = new JanelaComposto();
			jc.setVisible(true);

			String quantos = jc.get();

			jc.dispose();

			if (!quantos.equals("")) {

				if (quantos.equals("1")) {

					Object attributeObject = insertAttributeObject((int) x, (int) y);

					mxGraph graph = modelingComponent.getGraph();
					graph.getModel().beginUpdate();
					try {
						mxICell attributeCell = (mxICell) attributeObject;
						AttributeObject attribute = (AttributeObject) attributeCell.getValue();
						attribute.setComposed(true);

						mxGeometry geometry = attributeCell.getGeometry();
						double newY = AdjustAttributePosition(geometry.getY() - 10);
						double newX = geometry.getCenterX();
						Object[] atributo = new Object[1];

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

					} finally {
						graph.getModel().endUpdate();
					}

					return attributeObject;

				}

				if (quantos.equals("2")) {

					Object attributeObject = insertAttributeObject((int) x, (int) y);

					mxGraph graph = modelingComponent.getGraph();
					graph.getModel().beginUpdate();
					try {
						mxICell attributeCell = (mxICell) attributeObject;
						AttributeObject attribute = (AttributeObject) attributeCell.getValue();
						attribute.setComposed(true);

						mxGeometry geometry = attributeCell.getGeometry();
						double newY = AdjustAttributePosition(geometry.getY());
						double newX = geometry.getCenterX() - 40;
						Object[] atributo = new Object[1];

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_LEFT, atributo);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_RIGHT, atributo);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

						newX = geometry.getCenterX() + 40;

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_RIGHT, atributo);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_LEFT, atributo);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

					} finally {
						graph.getModel().endUpdate();
					}

					return attributeObject;

				}

				if (quantos.equals("3")) {

					Object attributeObject = insertAttributeObject((int) x, (int) y);

					mxGraph graph = modelingComponent.getGraph();
					graph.getModel().beginUpdate();
					try {
						mxICell attributeCell = (mxICell) attributeObject;
						AttributeObject attribute = (AttributeObject) attributeCell.getValue();
						attribute.setComposed(true);

						mxGeometry geometry = attributeCell.getGeometry();
						double newY = AdjustAttributePosition(geometry.getY());
						double newX = geometry.getCenterX() - 40;
						Object[] atributo = new Object[1];

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_LEFT, atributo);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_RIGHT, atributo);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

						newX = geometry.getCenterX() + 40;

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_RIGHT, atributo);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_LEFT, atributo);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

						newX = geometry.getCenterX();

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

					} finally {
						graph.getModel().endUpdate();
					}

					return attributeObject;

				}

				if (quantos.equals("4")) {

					Object attributeObject = insertAttributeObject((int) x, (int) y);

					mxGraph graph = modelingComponent.getGraph();
					graph.getModel().beginUpdate();
					try {
						mxICell attributeCell = (mxICell) attributeObject;
						AttributeObject attribute = (AttributeObject) attributeCell.getValue();
						attribute.setComposed(true);

						mxGeometry geometry = attributeCell.getGeometry();
						double newY = AdjustAttributePosition(geometry.getY());
						double newX = geometry.getCenterX() - 80;
						Object[] atributo = new Object[1];

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_LEFT, atributo);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_RIGHT, atributo);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

						newX = geometry.getCenterX() + 80;

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_RIGHT, atributo);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_LEFT, atributo);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

						newX = geometry.getCenterX() - 40;

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

						newX = geometry.getCenterX() + 40;

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

					} finally {
						graph.getModel().endUpdate();
					}

					return attributeObject;

				}

				if (quantos.equals("5")) {

					Object attributeObject = insertAttributeObject((int) x, (int) y);

					mxGraph graph = modelingComponent.getGraph();
					graph.getModel().beginUpdate();
					try {
						mxICell attributeCell = (mxICell) attributeObject;
						AttributeObject attribute = (AttributeObject) attributeCell.getValue();
						attribute.setComposed(true);

						mxGeometry geometry = attributeCell.getGeometry();
						double newY = AdjustAttributePosition(geometry.getY());
						double newX = geometry.getCenterX() - 80;
						Object[] atributo = new Object[1];

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_LEFT, atributo);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_RIGHT, atributo);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

						newX = geometry.getCenterX() + 80;

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.setCellStyles(mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_RIGHT, atributo);
						graph.setCellStyles(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_LEFT, atributo);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

						newX = geometry.getCenterX() - 40;

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

						newX = geometry.getCenterX() + 40;

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

						newX = geometry.getCenterX();

						atributo[0] = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(mxResources.get("attribute"), attributeObject, false), newX, newY, //$NON-NLS-1$
								16, 16, "attribute");

						attribute.addChildObject(atributo[0]);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								attributeObject, atributo[0], "straight"); //$NON-NLS-1$

					} finally {
						graph.getModel().endUpdate();
					}

					return attributeObject;

				}

			}

		} else {

			JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);
			JOptionPane.showMessageDialog(frame, "Selecione um objeto!", //$NON-NLS-1$
					Messages.getString("ModelingManager.warning.window.advice"), 0); //$NON-NLS-1$

		}
		return null;

	}//

	public Object insertAttributeObject(int x, int y) {
		Object attributeObject = null;
		Object object = modelingComponent.getCellAt(x, y);

		if (object instanceof mxICell) {
			mxGraph graph = modelingComponent.getGraph();
			sourceCell = (mxICell) object;
			double scale = graph.getView().getScale();

			if (sourceCell.isVertex()) {
				if (!(sourceCell.getValue() instanceof InheritanceObject)
						&& !(sourceCell.getValue() instanceof WritingBoxObject)) {

					graph.getModel().beginUpdate();
					try {
						String selectedEntry = mainWindow.getSelectedEntry();

						double originalY;
						if (sourceCell.getParent().getValue() instanceof ModelingObject) {
							originalY = sourceCell.getParent().getGeometry().getY();
						} else {
							originalY = sourceCell.getGeometry().getY();
						}

						if (sourceCell.getValue() instanceof AttributeObject) {
							AttributeObject attribute = (AttributeObject) sourceCell.getValue();
							attribute.setComposed(true);
						}

						double scaleX = x / scale;
						double newY = AdjustAttributePosition(originalY);

						boolean identifier = selectedEntry.equals(mxResources.get("identifierAttribute")) ? true //$NON-NLS-1$
								: false;
						String style = identifier ? "identifierAttribute" : "attribute"; //$NON-NLS-1$ //$NON-NLS-2$

						attributeObject = graph.insertVertex(graph.getDefaultParent(), null,
								new AttributeObject(selectedEntry, sourceCell, identifier), scaleX, newY, 16, 16,
								style);

						graph.insertEdge(graph.getDefaultParent(), "straight", null, //$NON-NLS-1$
								object, attributeObject, "attributeConnector"); //$NON-NLS-1$

						((ModelingObject) sourceCell.getValue()).addChildObject(attributeObject);
					} finally {
						graph.getModel().endUpdate();
					}
				} else {
					JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);
					JOptionPane.showMessageDialog(frame,
							Messages.getString("N�o � poss�vel colocar um atributo neste objeto"), "Aviso", 0); //$NON-NLS-1$
				}
			}
		} else {
			JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);
			JOptionPane.showMessageDialog(frame,
					Messages.getString("ModelingManager.warnig.needs.selectAnEntityRelationshipAssociativeAttribute"), //$NON-NLS-1$
					"Aviso", 0);
		}

		return attributeObject;
	}

	public Object insertSelfRelatedObject(double x, double y) {
		Object returnObject = null;
		Object object = modelingComponent.getCellAt((int) x, (int) y);

		if (object instanceof mxICell) {
			mxGraph graph = modelingComponent.getGraph();
			sourceCell = (mxICell) object;

			if (sourceCell.getStyle().equals("associativeRelation")) {
				sourceCell = sourceCell.getParent();
			}

			if (sourceCell.getStyle().equals("entity") || sourceCell.getStyle().equals("associativeEntity")) {
				graph.getModel().beginUpdate();
				Object[] objects = new Object[5];
				try {
					mxGeometry geometry = sourceCell.getGeometry();
					double newX = geometry.getX() + (geometry.getWidth() * 2);
					double newY = geometry.getY();

					objects[0] = sourceCell;
					objects[4] = sourceCell;
					RelationObject relationObject = new RelationObject(mxResources.get("relation"), sourceCell); //$NON-NLS-1$

					objects[2] = graph.insertVertex(graph.getDefaultParent(), null, relationObject, newX, newY, 120, 50,
							"relation"); //$NON-NLS-1$

					objects[1] = graph.insertEdge(graph.getDefaultParent(), null,
							new ConnectorObject(Cardinality.getValue("(0,n)")), //$NON-NLS-1$
							sourceCell, objects[2], "entityRelationConnector"); //$NON-NLS-1$

					objects[3] = graph.insertEdge(graph.getDefaultParent(), null,
							new ConnectorObject(Cardinality.getValue("(0,n)")), //$NON-NLS-1$
							objects[2], sourceCell, "entityRelationConnector"); //$NON-NLS-1$

					mxParallelEdgeLayout layout = new mxParallelEdgeLayout(graph);
					layout.execute(graph.getDefaultParent());

					if (sourceCell.getValue() instanceof EntityObject) {
						EntityObject entity = (EntityObject) sourceCell.getValue();
						entity.setSelfRelated(true);
						entity.addChildObject(objects[2]);
						relationObject.addRelatedObject(entity);
						relationObject.addRelatedObject(entity);
						entity.addRelation(relationObject);

					} else if (sourceCell.getValue() instanceof AssociativeEntityObject) {
						AssociativeEntityObject entity = (AssociativeEntityObject) sourceCell.getValue();
						entity.setSelfRelated(true);
						entity.addChildObject(objects[2]);
						relationObject.addRelatedObject(entity);
						relationObject.addRelatedObject(entity);
						entity.addRelation(relationObject);
					}

					returnObject = objects[2];
				} finally {
					graph.getModel().endUpdate();
				}

				AdjustEdgeLabelPosition(objects);
			} else {

				JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);
				JOptionPane.showMessageDialog(frame,
						Messages.getString("ModelingManager.warning.needs.selectAnEntityAssociative"), "Aviso", 0); //$NON-NLS-1$
			}
		} else {
			JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);
			JOptionPane.showMessageDialog(frame,
					Messages.getString("ModelingManager.warning.needs.selectAnEntityAssociative"), "Aviso", 0); //$NON-NLS-1$
		}

		return returnObject;
	}

	public boolean insertConnectorObject(int x, int y) {
		boolean clearSelection = false;

		if (source == null) {
			source = modelingComponent.getCellAt((int) x, (int) y);

			if (source instanceof mxICell) {
				sourceCell = (mxICell) source;

				if (!sourceCell.isVertex() || sourceCell.getValue() instanceof AttributeObject) {
					source = null;
					JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
							"Este n�o � um objeto valido para realizar uma conex�o", "Aviso", 0); //$NON-NLS-1$
				}

			} else {
				JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
						"Necess�rio selecionar um objeto", "Aviso", 0); //$NON-NLS-1$
			}

		}

		else if (target == null) {
			target = modelingComponent.getCellAt((int) x, (int) y);

			if (target == source && !((mxCell) target).getStyle().equals("table")) {

				insertSelfRelatedObject(x, y);
				clearSelection = true;
				return clearSelection;

			}

			if (target instanceof mxICell && target != source) {
				mxICell targetCell = (mxICell) target;

				if (!targetCell.isVertex() || targetCell.getValue() instanceof AttributeObject) {
					target = null;
					JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
							"Este n�o � um objeto v�lido para realizar uma conex�o", "Aviso", 0); //$NON-NLS-1$
				} else {
					insertConnectorBetweenObjects();
					clearSelection = true;
				}
			} else {
				JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
						"E necess�rio selecionar um objeto", "Aviso", 0); //$NON-NLS-1$
			}
		}

		return clearSelection;
	}

	public double AdjustAttributePosition(double cellY) {
		return cellY - 30;
	}

	public Connection getConnection(String sourceStyle, String targetStyle) {
		Connection connection = Connection.NONE;

		if ((sourceStyle.equals("entity") || sourceStyle.equals("associativeEntity")) && //$NON-NLS-1$ //$NON-NLS-2$
				(targetStyle.equals("entity") || targetStyle.equals("associativeEntity"))) //$NON-NLS-1$ //$NON-NLS-2$
		{
			connection = Connection.ENTITY_TO_ENTITY;

		} else if ((sourceStyle.equals("entity") || sourceStyle.equals("associativeEntity")) && //$NON-NLS-1$ //$NON-NLS-2$
				(targetStyle.equals("relation") || targetStyle.equals("associativeRelation"))) //$NON-NLS-1$ //$NON-NLS-2$
		{
			connection = Connection.ENTITY_TO_RELATION;

		} else if ((sourceStyle.equals("relation") || sourceStyle.equals("associativeRelation")) && //$NON-NLS-1$ //$NON-NLS-2$
				(targetStyle.equals("entity") || targetStyle.equals("associativeEntity"))) //$NON-NLS-1$ //$NON-NLS-2$
		{
			connection = Connection.RELATION_TO_ENTITY;

		} else if ((sourceStyle.equals("table") /* || sourceStyle.equals("column") */) && //$NON-NLS-1$ //$NON-NLS-2$
				(targetStyle.equals("table") /* || targetStyle.equals("column") */)) //$NON-NLS-1$ //$NON-NLS-2$
		{
			connection = Connection.TABLES;

		} else if ((sourceStyle.equals("inheritance") && targetStyle.equals("entity")) || //$NON-NLS-1$ //$NON-NLS-2$
				(sourceStyle.equals("entity") && targetStyle.equals("inheritance"))) //$NON-NLS-1$ //$NON-NLS-2$
		{
			connection = Connection.ENTITY_TO_INHERITANCE;

		} else if ((sourceStyle.equals("relation") && targetStyle.equals("relation"))
				|| (sourceStyle.equals("inheritance") && targetStyle.equals("inheritance"))
				|| (sourceStyle.equals("relation") && targetStyle.equals("inheritance"))
				|| (sourceStyle.equals("inheritance") && targetStyle.equals("relation"))
				|| (sourceStyle.equals("field") && targetStyle.equals("field"))) {
			connection = Connection.ERROR;
		}

		return connection;
	}

	public void insertConnectorBetweenObjects() {
		String srcCardinality = "(0,n)";
		String tgtCardinality = "(0,n)";

		if (sourceCardinality != null && sourceCardinality instanceof ConnectorObject) {
			srcCardinality = Cardinality.getText(((ConnectorObject) sourceCardinality).getCardinality());
		}

		if (targetCardinality != null && targetCardinality instanceof ConnectorObject) {
			tgtCardinality = Cardinality.getText(((ConnectorObject) targetCardinality).getCardinality());
		}

		mxGraph graph = modelingComponent.getGraph();
		sourceCell = (mxICell) source;
		mxICell targetCell = (mxICell) target;
		Object[] objects = null;

		Connection connection = getConnection(sourceCell.getStyle(), targetCell.getStyle());

		switch (connection) {
		case ENTITY_TO_ENTITY:
			mxGeometry sourceGeometry = sourceCell.getGeometry();
			if (source == target) {
				Object object = modelingComponent.getCellAt((int) sourceGeometry.getCenterX(),
						(int) sourceGeometry.getCenterY());

				if (object instanceof mxICell) {
					if (sourceCell.getStyle().equals("associativeRelation")) {
						sourceCell = sourceCell.getParent();
					}

					if (sourceCell.getStyle().equals("entity") || sourceCell.getStyle().equals("associativeEntity")) { //$NON-NLS-1$ //$NON-NLS-2$
						graph.getModel().beginUpdate();
						objects = new Object[5];
						try {
							mxGeometry geometry = sourceCell.getGeometry();
							double newX = geometry.getX() + (geometry.getWidth() * 2);
							double newY = geometry.getY();

							objects[0] = sourceCell;
							objects[4] = sourceCell;
							RelationObject relationObject = new RelationObject(mxResources.get("relation"), sourceCell); //$NON-NLS-1$
							objects[2] = graph.insertVertex(graph.getDefaultParent(), null, relationObject, newX, newY,
									120, 50, "relation"); //$NON-NLS-1$
							objects[1] = graph.insertEdge(graph.getDefaultParent(), null,
									new ConnectorObject(Cardinality.getValue(srcCardinality)), // $NON-NLS-1$
									sourceCell, objects[2], "entityRelationConnector"); //$NON-NLS-1$
							objects[3] = graph.insertEdge(graph.getDefaultParent(), null,
									new ConnectorObject(Cardinality.getValue(tgtCardinality)), // $NON-NLS-1$
									objects[2], sourceCell, "entityRelationConnector"); //$NON-NLS-1$

							if (sourceCell.getValue() instanceof EntityObject) {
								EntityObject entity = (EntityObject) sourceCell.getValue();
								entity.setSelfRelated(true);
								entity.addChildObject(objects[2]);
								relationObject.addRelatedObject(entity);
								relationObject.addRelatedObject(entity);
								entity.addRelation(relationObject);

							} else if (sourceCell.getValue() instanceof AssociativeEntityObject) {
								AssociativeEntityObject entity = (AssociativeEntityObject) sourceCell.getValue();
								entity.setSelfRelated(true);
								entity.addChildObject(objects[2]);
								relationObject.addRelatedObject(entity);
								relationObject.addRelatedObject(entity);
								entity.addRelation(relationObject);
							}

						} finally {
							graph.getModel().endUpdate();
						}

						AdjustEdgeLabelPosition(objects);
					} else {
						JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);
						JOptionPane.showMessageDialog(frame,
								Messages.getString("ModelingManager.warning.needs.selectAnEntityAssociative"), "Aviso", //$NON-NLS-1$
								0);
					}
				} else {
					JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);
					JOptionPane.showMessageDialog(frame,
							Messages.getString("ModelingManager.warning.needs.selectAnEntityAssociative"), "Aviso", 0); //$NON-NLS-1$
				}

			} else {
				mxGeometry targetGeometry = targetCell.getGeometry();
				double srcX = sourceGeometry.getX();
				double srcY = sourceGeometry.getY();
				double trgtX = targetGeometry.getX();
				double trgtY = targetGeometry.getY();
				double x = srcX > trgtX ? trgtX + ((srcX - trgtX) / 2) : srcX + ((trgtX - srcX) / 2);
				double y = srcY > trgtY ? trgtY + ((srcY - trgtY) / 2) : srcY + ((trgtY - srcY) / 2);

				if (x == srcX && x == trgtX && y == srcY && y == trgtY) {
					y += sourceGeometry.getHeight() + (sourceGeometry.getHeight() * 1.5);
				}

				objects = new Object[5];
				objects[0] = source;
				objects[4] = target;
				RelationObject relationObject = new RelationObject(mxResources.get("relation")); //$NON-NLS-1$

				relationObject.addRelatedObject((ModelingObject) ((mxCell) source).getValue());
				relationObject.addRelatedObject((ModelingObject) ((mxCell) target).getValue());

				((ModelingObject) ((mxCell) source).getValue()).addRelation(relationObject);
				((ModelingObject) ((mxCell) target).getValue()).addRelation(relationObject);

				objects[2] = graph.insertVertex(graph.getDefaultParent(), null, relationObject, x, y, 120, 50,
						"relation"); //$NON-NLS-1$
				ConnectorObject firstConnector = new ConnectorObject(Cardinality.getValue(srcCardinality)); // $NON-NLS-1$
				objects[1] = graph.insertEdge(graph.getDefaultParent(), null, firstConnector, source, objects[2],
						"entityRelationConnector"); //$NON-NLS-1$

				ConnectorObject secondConnector = new ConnectorObject(Cardinality.getValue(tgtCardinality)); // $NON-NLS-1$
				objects[3] = graph.insertEdge(graph.getDefaultParent(), null, secondConnector, objects[2], target,
						"entityRelationConnector"); //$NON-NLS-1$

				// mxPoint p= new mxPoint(30,30);
				// value = null;
				// value.add(p);
				// ((mxCell)objects[1]).getGeometry().setPoints(value);

			}
			break;

		case ENTITY_TO_RELATION:
			objects = new Object[3];
			objects[0] = source;
			objects[2] = target;
			if (((mxCell) source).getValue() instanceof RelationObject) {
				RelationObject relationObject = (RelationObject) ((mxCell) source).getValue();
				ModelingObject relatedObject = (ModelingObject) ((mxCell) target).getValue();
				relationObject.addRelatedObject(relatedObject);
				relatedObject.addRelation(relationObject);

			} else {
				RelationObject relationObject = (RelationObject) ((mxCell) target).getValue();
				ModelingObject relatedObject = (ModelingObject) ((mxCell) source).getValue();
				relationObject.addRelatedObject(relatedObject);
				relatedObject.addRelation(relationObject);

			}

			ConnectorObject connector = new ConnectorObject(Cardinality.getValue(srcCardinality)); // $NON-NLS-1$
			objects[1] = graph.insertEdge(graph.getDefaultParent(), null, connector, source, target,
					"entityRelationConnector"); //$NON-NLS-1$
			break;

		case RELATION_TO_ENTITY:
			objects = new Object[3];
			objects[0] = target;
			objects[2] = source;
			if (((mxCell) source).getValue() instanceof RelationObject) {
				RelationObject relationObject = (RelationObject) ((mxCell) source).getValue();
				ModelingObject relatedObject = (ModelingObject) ((mxCell) target).getValue();
				relationObject.addRelatedObject(relatedObject);
				relatedObject.addRelation(relationObject);

			} else {
				RelationObject relationObject = (RelationObject) ((mxCell) target).getValue();
				ModelingObject relatedObject = (ModelingObject) ((mxCell) source).getValue();
				relationObject.addRelatedObject(relatedObject);
				relatedObject.addRelation(relationObject);

			}

			ConnectorObject connector2 = new ConnectorObject(Cardinality.getValue(tgtCardinality)); // $NON-NLS-1$
			objects[1] = graph.insertEdge(graph.getDefaultParent(), null, connector2, target, source,
					"entityRelationConnector"); //$NON-NLS-1$
			break;

		case ENTITY_TO_INHERITANCE:
			InheritanceObject inheritance = null;
			EntityObject entity = null;
			Object child = null;
			if (sourceCell.getValue() instanceof InheritanceObject) {
				inheritance = (InheritanceObject) sourceCell.getValue();
				entity = (EntityObject) targetCell.getValue();
				child = targetCell;
			} else {
				inheritance = (InheritanceObject) targetCell.getValue();
				entity = (EntityObject) sourceCell.getValue();
				child = sourceCell;
			}

			inheritance.addChildObject(child);
			entity.setParentObject(child == sourceCell ? targetCell : sourceCell);
			if (inheritance.getChildObjects().size() == 2) {
				inheritance.setExclusive(true);
			}
			graph.insertEdge(graph.getDefaultParent(), "straight", null, source, target, "straight"); //$NON-NLS-1$ //$NON-NLS-2$
			break;

		case TABLES:

			sourceGeometry = sourceCell.getGeometry();
			mxGeometry targetGeometry = targetCell.getGeometry();
			double srcX = sourceGeometry.getX();
			double srcY = sourceGeometry.getY();
			double trgtX = targetGeometry.getX();
			double trgtY = targetGeometry.getY();
			double x = srcX > trgtX ? trgtX + ((srcX - trgtX) / 2) : srcX + ((trgtX - srcX) / 2);
			double y = srcY > trgtY ? trgtY + ((srcY - trgtY) / 2) : srcY + ((trgtY - srcY) / 2);

			if (x == srcX && x == trgtX && y == srcY && y == trgtY) {
				y += sourceGeometry.getHeight() + (sourceGeometry.getHeight() * 1.5);
			}

			objects = new Object[5];
			objects[0] = sourceCell.getStyle().equals("table") ? source : sourceCell.getParent(); //$NON-NLS-1$
			objects[4] = targetCell.getStyle().equals("table") ? target : targetCell.getParent(); //$NON-NLS-1$
			objects[2] = graph.insertVertex(graph.getDefaultParent(), null, "", x, y, 20, 20, "tableRelation"); //$NON-NLS-1$ //$NON-NLS-2$
			ConnectorObject firstConnector = new ConnectorObject(Cardinality.getValue(srcCardinality)); // $NON-NLS-1$
			objects[1] = graph.insertEdge(graph.getDefaultParent(), null, firstConnector, source, objects[2],
					"entityRelationConnector"); //$NON-NLS-1$
			ConnectorObject secondConnector = new ConnectorObject(Cardinality.getValue(tgtCardinality)); // $NON-NLS-1$
			objects[3] = graph.insertEdge(graph.getDefaultParent(), null, secondConnector, objects[2], target,
					"entityRelationConnector"); //$NON-NLS-1$
			JanelaEstrangeira je = new JanelaEstrangeira(objects[0], objects[4], this);
			je.setVisible(true);
			break;

		case ERROR:
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
					"A conex�o entre estes objetos n�o pode existir", "Aviso", 0); //$NON-NLS-1$
		}

		if (!(connection == Connection.NONE || connection == Connection.ENTITY_TO_INHERITANCE
				|| connection == Connection.ERROR)) {
			AdjustEdgeLabelPosition(objects);
		}
	}

	public Object insertConnectorTables(mxCell sourceCell, mxCell targetCell) {
		Connection connection = getConnection(sourceCell.getStyle(), targetCell.getStyle());
		mxGraph graph = modelingComponent.getGraph();
		Object[] objects = null;
		String srcCardinality = "(0,n)";
		String tgtCardinality = "(0,n)";

		// if (sourceCardinality != null && sourceCardinality instanceof
		// ConnectorObject) {
		// srcCardinality = Cardinality.getText(((ConnectorObject)
		// sourceCardinality).getCardinality());
		// }
		//
		// if (targetCardinality != null && targetCardinality instanceof
		// ConnectorObject) {
		// tgtCardinality = Cardinality.getText(((ConnectorObject)
		// targetCardinality).getCardinality());
		// }

		mxGeometry sourceGeometry = sourceCell.getGeometry();
		mxGeometry targetGeometry = targetCell.getGeometry();
		double srcX = sourceGeometry.getX();
		double srcY = sourceGeometry.getY();
		double trgtX = targetGeometry.getX();
		double trgtY = targetGeometry.getY();
		double x = srcX > trgtX ? trgtX + ((srcX - trgtX) / 2) : srcX + ((trgtX - srcX) / 2);
		double y = srcY > trgtY ? trgtY + ((srcY - trgtY) / 2) : srcY + ((trgtY - srcY) / 2);

		if (x == srcX && x == trgtX && y == srcY && y == trgtY) {
			y += sourceGeometry.getHeight() + (sourceGeometry.getHeight() * 1.5);
		}

		objects = new Object[5];
		objects[0] = sourceCell.getStyle().equals("table") ? source : sourceCell.getParent(); //$NON-NLS-1$
		objects[4] = targetCell.getStyle().equals("table") ? target : targetCell.getParent(); //$NON-NLS-1$
		objects[2] = graph.insertVertex(graph.getDefaultParent(), null, "", x, y, 20, 20, "tableRelation"); //$NON-NLS-1$ //$NON-NLS-2$

		if (!(connection == Connection.NONE || connection == Connection.ENTITY_TO_INHERITANCE
				|| connection == Connection.ERROR)) {
			AdjustEdgeLabelPosition(objects);
		}

		return objects[2];
	}

	public void AdjustEdgeLabelPosition(Object[] objects) {
		// mxGraph graph = modelingComponent.getGraph();
		// mxIGraphModel mxIGraphModelodel = graph.getModel();
		//
		// int numConectors = objects.length > 3 ? 2 : 1;
		// int indexConector = 1;
		// int indexSource = 0;
		// while (numConectors > 0) {
		// mxCellState state = graph.getView().getState(objects[indexConector]);
		// List<mxPoint> points = NearestPointsFromSource(
		// objects[indexSource], state);
		// mxPoint firstPoint = points.get(0);
		// mxPoint secondPoint = points.get(1);
		// double newX = firstPoint.getX()
		// + ((secondPoint.getX() - firstPoint.getX()) / 2);
		// double newY = firstPoint.getY()
		// + ((secondPoint.getY() - firstPoint.getY()) / 2);
		// mxGeometry geometry = model.getGeometry(state.getCell());
		//
		// if (geometry != null) {
		// geometry = (mxGeometry) geometry.clone();
		// insertedObject
		// // Resets the relative location stored inside the geometry
		// mxPoint pt = graph.getView().getRelativePoint(state, newX, newY);
		// geometry.setX(pt.getX());
		// geometry.setY(pt.getY());
		//
		// // Resets the offset inside the geometry to find the offset
		// // from the resulting point
		// double scale = graph.getView().getScale();
		// geometry.setOffset(new mxPoint(0, 0));
		// pt = graph.getView().getPoint(state, geometry);
		// geometry.setOffset(new mxPoint(Math.round((newX - pt.getX())
		// / scale), Math.round((newY - pt.getY()) / scale)));
		//
		// model.setGeometry(state.getCell(), geometry);
		// }
		// numConectors--;
		// indexConector += 2;
		// indexSource += 4;
		// }
	}

	public List<mxPoint> NearestPointsFromSource(Object object, mxCellState state) {
		List<mxPoint> points = new ArrayList<mxPoint>();
		sourceCell = (mxICell) object;

		double srcX = sourceCell.getGeometry().getX();
		double firstPoint = state.getAbsolutePoints().get(0).getX();
		double lastPoint = state.getAbsolutePoints().get(3).getX();

		if (Math.abs(firstPoint - srcX) < Math.abs(lastPoint - srcX)) {
			points.add(state.getAbsolutePoints().get(0));
			points.add(state.getAbsolutePoints().get(1));
		} else {
			points.add(state.getAbsolutePoints().get(2));
			points.add(state.getAbsolutePoints().get(3));
		}

		return points;
	}
}
