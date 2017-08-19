package ufsc.sisinf.brmodelo2all.control;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;

import ufsc.sisinf.brmodelo2all.model.Cardinality;
import ufsc.sisinf.brmodelo2all.model.Modeling;
import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.AssociativeEntityObject;
import ufsc.sisinf.brmodelo2all.model.objects.AssociativeRelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.AttributeObject;
import ufsc.sisinf.brmodelo2all.model.objects.Collection;
import ufsc.sisinf.brmodelo2all.model.objects.ConnectorObject;
import ufsc.sisinf.brmodelo2all.model.objects.EntityObject;
import ufsc.sisinf.brmodelo2all.model.objects.InheritanceObject;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;
import ufsc.sisinf.brmodelo2all.model.objects.NoSqlAttributeObject;
import ufsc.sisinf.brmodelo2all.model.objects.RelationObject;
import ufsc.sisinf.brmodelo2all.ui.AppMainWindow;

public class ConceptualToNoSql {
	private final ModelingComponent conceptualModelingComponent;
	private final ModelingEditor noSqlModelingEditor;
	private final AppMainWindow mainWindow;
	private List<Collection> collectionsCreated = new ArrayList<Collection>();
	private List<mxCell> cellsCreated = new ArrayList<mxCell>();
	private List<mxCell> entityCells = new ArrayList<mxCell>();
	private List<EntityObject> entityObjects = new ArrayList<EntityObject>();
	private List<mxCell> inheritanceObjects = new ArrayList<mxCell>();
	private List<mxCell> associativeEntityObjects = new ArrayList<mxCell>();
	private boolean associativeEntityFlag = false;
	private List<mxCell> orderedListOfEntities;
	private List<mxCell> relationObjects = new ArrayList<mxCell>();

	public ConceptualToNoSql(ModelingComponent conceptualModelingComponent, ModelingEditor noSqlModelingEditor,
			AppMainWindow mainWindow) {
		this.conceptualModelingComponent = conceptualModelingComponent;
		this.noSqlModelingEditor = noSqlModelingEditor;
		this.mainWindow = mainWindow;
	}

	public void convertModeling() {
		mxRectangle rect = this.conceptualModelingComponent.getGraph().getGraphBounds();
		int x = (int) rect.getX();
		int y = (int) rect.getY();

		Rectangle ret = new Rectangle(x + 60000, y + 60000);

		Object[] cells = this.conceptualModelingComponent.getCells(ret);
		Object[] arrayOfObject1;
		int j = (arrayOfObject1 = cells).length;
		for (int i = 0; i < j; i++) {
			Object cell = arrayOfObject1[i];
			if ((cell instanceof mxCell)) {
				mxCell objectCell = (mxCell) cell;
				if ((objectCell.getValue() instanceof EntityObject)) {
					((EntityObject) objectCell.getValue()).setMarkedEntity(false);
					((EntityObject) objectCell.getValue()).setConvertedEntity(false);
					this.entityCells.add(objectCell);
					this.entityObjects.add((EntityObject) objectCell.getValue());
				} else if ((objectCell.getValue() instanceof InheritanceObject)) {
					((InheritanceObject) objectCell.getValue()).setConverted(false);
					this.inheritanceObjects.add(objectCell);
				} else if ((objectCell.getValue() instanceof AssociativeEntityObject)) {
					this.associativeEntityObjects.add(objectCell);
				} else if ((objectCell.getValue() instanceof RelationObject)) {
					((RelationObject) objectCell.getValue()).setMarkedRelation(false);
					relationObjects.add(objectCell);
				}
			}
		}
		List<mxCell> orderedInheritanceParents = orderParentInheritanceList(this.inheritanceObjects);
		if ((this.inheritanceObjects.size() > 0) && (orderedInheritanceParents.size() == 0)) {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(this.mainWindow),
					"Referencia circular na especializa������o/generaliza������o de entidades! Clique em OK para finalizar a convers���o.",
					"Aviso", 0);
			return;
		}
		Object iterator = orderedInheritanceParents.iterator();
		while (((Iterator<?>) iterator).hasNext()) {
			orderInheritanceList((mxCell) ((Iterator<?>) iterator).next());
		}
		convertRelation(this.entityObjects, relationObjects);
	}

	public Object convertEntity(EntityObject entity, mxGeometry geometry, Object collection, Object superclassEntity) {
		String collectionName = entity.getName();
		double x = geometry.getX();
		double y = geometry.getY();

		Modeling noSqlModeling = (Modeling) this.noSqlModelingEditor.modelingComponent.getGraph();
		if ((collection == null) && (superclassEntity == null)) {
			collection = noSqlModeling.insertVertex(noSqlModeling.getDefaultParent(), null,
					new Collection(collectionName, false), x, y, 200.0D, 40.0D, "verticalAlign=top");
		} else if ((superclassEntity != null) && (collection == null)) {
			collection = this.noSqlModelingEditor.modelingManager.insertBlock(0.0D, 0.0D, (mxICell) superclassEntity);
		}
		this.collectionsCreated.add((Collection) ((mxCell) collection).getValue());
		this.cellsCreated.add((mxCell) collection);

		int count = entity.getChildObjects().size();
		for (int i = 0; i < count; i++) {
			mxCell cell = (mxCell) entity.getChildObjects().get(i);
			if ((cell.getValue() instanceof AttributeObject)) {
				convertAttribute((AttributeObject) cell.getValue(), ((mxCell) collection).getGeometry(),
						(mxCell) collection, null);
			}
		}
		return collection;
	}

	public Object convertRelationIntoCollection(mxICell relationCell, Object superclassCollection) {
		List<Object> attributes = getRelationAttributes((mxCell) relationCell);
		Object newCollection = this.noSqlModelingEditor.modelingManager.insertBlock(0.0D, 0.0D,
				(mxICell) superclassCollection);

		this.collectionsCreated.add((Collection) ((mxCell) newCollection).getValue());
		this.cellsCreated.add((mxCell) newCollection);
		for (int i = 0; i < attributes.size(); i++) {
			AttributeObject attribute = (AttributeObject) attributes.get(i);
			convertAttribute(attribute, ((mxCell) newCollection).getGeometry(), (mxCell) newCollection, null);
		}
		return newCollection;
	}

	public void convertAttribute(AttributeObject attribute, mxGeometry collectionGeometry, mxICell collection,
			AttributeObject composedAttribute) {

		List<AttributeObject> attributes = new ArrayList<AttributeObject>();
		double x = collectionGeometry.getCenterX();
		double y = collectionGeometry.getCenterY();

		if (((attribute.isMultiValued()) || (attribute.isComposed())) && ((!attribute.isMultiValued())
				|| (attribute.getMaximumCardinality() != '1') || (attribute.isComposed()))) {
			getAttributeChilds(attribute, attributes);
		} else {
			attributes.add(attribute);
		}
		if (!attribute.isComposed()) {
			String style = attribute.isIdentifier() ? mxResources.get("noSqlIdentifierAttribute")
					: mxResources.get("noSqlAttribute");
			Object noSqlAttributeRest = this.noSqlModelingEditor.modelingManager.insertNoSqlAttributeObject(x, y, style,
					collection);
			NoSqlAttributeObject noSqlAttributeRestAtt = (NoSqlAttributeObject) ((mxICell) noSqlAttributeRest)
					.getValue();
			noSqlAttributeRestAtt.setType(attribute.getType());
			noSqlAttributeRestAtt.setOptional(attribute.isOptional());
			noSqlAttributeRestAtt.setMultiValued(attribute.isMultiValued());
			noSqlAttributeRestAtt.setMinimumCardinality(attribute.isOptional() ? '0' : '1');
			char maximumCardinality = attribute.isMultiValued() ? 'n' : '1';
			noSqlAttributeRestAtt.setMaximumCardinality(maximumCardinality);
			if (attribute.isIdentifier()) {
				noSqlAttributeRestAtt.setName(attribute.getName());
			} else {
				noSqlAttributeRestAtt.setName(attribute.getName());
			}
		} else {
			List<AttributeObject> childAttributes = new ArrayList<AttributeObject>();
			getAttributeChilds(attribute, childAttributes);
			Object block = this.noSqlModelingEditor.modelingManager.insertBlock(x, y, collection);
			this.collectionsCreated.add((Collection) ((mxCell) block).getValue());
			this.cellsCreated.add((mxCell) block);
			Collection blockAtt = (Collection) ((mxCell) block).getValue();
			blockAtt.setName(attribute.getName());
			blockAtt.setOptional(attribute.isOptional());
			blockAtt.setMultiValued(attribute.isMultiValued());
			char minimumCardinality = attribute.isOptional() ? '0' : '1';
			char maximumCardinality = attribute.isMultiValued() ? 'n' : '1';
			blockAtt.setMinimumCardinality(minimumCardinality);
			blockAtt.setMaximumCardinality(maximumCardinality);
			Iterator<AttributeObject> iterator = childAttributes.iterator();
			this.noSqlModelingEditor.modelingComponent.getGraph().refresh();
			while (iterator.hasNext()) {
				AttributeObject childAttribute = (AttributeObject) iterator.next();
				convertAttribute(childAttribute, ((mxICell) block).getGeometry(), (mxICell) block, attribute);
			}
		}
		this.noSqlModelingEditor.modelingComponent.getGraph().refresh();
	}

	public Object convertRelationIntoCollection(mxICell relationCell, Object superclassEntity,
			List<AttributeObject> relationAttributes) {
		return null;
	}

	public void getAttributeChilds(AttributeObject attribute, List<AttributeObject> attributes) {
		Iterator<Object> iterator = attribute.getChildObjects().iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			AttributeObject attributeObject = (AttributeObject) ((mxCell) object).getValue();
			if (attributeObject.getChildObjects().size() > 0) {
				getAttributeChilds(attributeObject, attributes);
			} else {
				attributes.add(attributeObject);
			}
		}
	}

	public void convertGeneralization(mxCell inheritanceCell) {
		boolean generalizationCountTest = generalizationCountTest(inheritanceCell);
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		if (inheritanceObject.isConverted()) {
			return;
		}
		if ((!hasConvertedEntities(inheritanceCell)) && (!subclassesConnectedToRelation(inheritanceCell))
				&& (generalizationCountTest) && (!hasReferencedEntities(inheritanceCell))) {
			superclassModeledGeneralization(inheritanceCell);
		} else if ((!inheritanceObject.isPartial()) && (inheritanceObject.isExclusive())
				&& (!hasConvertedEntities(inheritanceCell)) && (!superclassConnectedToRelation(inheritanceCell))) {
			subclassModeledGeneralization(inheritanceCell);
		} else {
			inheritanceModeledGeneralization(inheritanceCell);
		}
		mxCell parentEntityCell = (mxCell) inheritanceObject.getParentObject();
		EntityObject parentEntity = (EntityObject) parentEntityCell.getValue();
		setInheritancesConverted(parentEntity.getName());
	}

	public void superclassModeledGeneralization(mxCell inheritanceCell) {
		List<mxCell> subentities = new ArrayList<mxCell>();
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		mxCell parentEntityCell = (mxCell) inheritanceObject.getParentObject();
		EntityObject parentEntity = (EntityObject) parentEntityCell.getValue();
		boolean notExclusive = false;

		Object collection = convertEntity(parentEntity, parentEntityCell.getGeometry(), null, null);
		if (!((InheritanceObject) inheritanceCell.getValue()).isExclusive()) {
			subentities = getNonExclusiveGeneralizationSubentities(parentEntity.getName());
			notExclusive = true;
		}
		int count = notExclusive ? subentities.size() : inheritanceObject.getChildObjects().size();
		for (int i = 0; i < count; i++) {
			mxICell cell;
			if (notExclusive) {
				cell = (mxICell) subentities.get(i);
			} else {
				cell = (mxICell) inheritanceObject.getChildObjects().get(i);
			}
			if (!((EntityObject) cell.getValue()).isConvertedEntity()) {
				EntityObject entity = (EntityObject) cell.getValue();
				int attributesCount = entity.getChildObjects().size();
				for (int j = 0; j < attributesCount; j++) {
					mxCell newCell = (mxCell) entity.getChildObjects().get(j);
					if ((newCell.getValue() instanceof AttributeObject)) {
						AttributeObject attribute = (AttributeObject) newCell.getValue();
						attribute.setOptional(true);
						convertAttribute(attribute, ((mxCell) collection).getGeometry(), (mxCell) collection, null);
					}
				}
			} else {
				EntityObject entityAlreadyConverted = (EntityObject) cell.getValue();
				Collection superCollection = (Collection) ((mxICell) collection).getValue();
				insertCollectionToCollection(findCollection(entityAlreadyConverted.getName()), superCollection);
			}
			((EntityObject) cell.getValue()).setMarkedEntity(true);
			((EntityObject) cell.getValue()).setConvertedEntity(true);
		}
		Object noSqlAttributeRest = this.noSqlModelingEditor.modelingManager.insertNoSqlAttributeObject(0.0D, 0.0D,
				"noSqlAttribute", (mxICell) collection);
		NoSqlAttributeObject noSqlAttributeRestAtt = (NoSqlAttributeObject) ((mxICell) noSqlAttributeRest).getValue();
		noSqlAttributeRestAtt.setType("");
		noSqlAttributeRestAtt.setOptional(inheritanceObject.isPartial());
		noSqlAttributeRestAtt.setMinimumCardinality(!inheritanceObject.isPartial() ? '0' : '1');
		noSqlAttributeRestAtt.setName("tipo");
		if (!inheritanceObject.isExclusive()) {
			noSqlAttributeRestAtt.setMultiValued(true);
			noSqlAttributeRestAtt.setOptional(false);
			noSqlAttributeRestAtt.setMaximumCardinality('n');
		} else if (inheritanceObject.isExclusive()) {
			noSqlAttributeRestAtt.setMultiValued(false);
			noSqlAttributeRestAtt.setOptional(true);
			noSqlAttributeRestAtt.setMaximumCardinality('1');
		}
		parentEntity.setConvertedEntity(true);
		this.noSqlModelingEditor.modelingComponent.getGraph().refresh();
	}

	public void subclassModeledGeneralization(mxCell inheritanceCell) {
		List<mxCell> subentities = new ArrayList<mxCell>();
		boolean notExclusive = false;
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		List<Object> superclassAttributes = new ArrayList<Object>();
		mxICell entityCell = (mxICell) inheritanceObject.getParentObject();
		EntityObject superclassEntity = (EntityObject) entityCell.getValue();
		int attributesCount = superclassEntity.getChildObjects().size();
		for (int i = 0; i < attributesCount; i++) {
			mxCell cell = (mxCell) superclassEntity.getChildObjects().get(i);
			if ((cell.getValue() instanceof AttributeObject)) {
				superclassAttributes.add(cell.getValue());
			}
		}
		if (!((InheritanceObject) inheritanceCell.getValue()).isExclusive()) {
			subentities = getNonExclusiveGeneralizationSubentities(superclassEntity.getName());
			notExclusive = true;
		}
		int count = notExclusive ? subentities.size() : inheritanceObject.getChildObjects().size();
		for (int i = 0; i < count; i++) {
			mxICell cell;
			if (notExclusive) {
				cell = (mxICell) subentities.get(i);
			} else {
				cell = (mxICell) inheritanceObject.getChildObjects().get(i);
			}
			EntityObject entity = (EntityObject) cell.getValue();
			entity.setConvertedEntity(true);
			Object collectionObject = convertEntity(entity, cell.getGeometry(), null, null);
			for (int j = 0; j < superclassAttributes.size(); j++) {
				AttributeObject attribute = (AttributeObject) superclassAttributes.get(j);
				attribute.setOptional(false);
				convertAttribute(attribute, ((mxICell) collectionObject).getGeometry(), (mxICell) collectionObject,
						null);
			}
		}
	}

	public void inheritanceModeledGeneralization(mxCell inheritanceCell) {
		List<mxCell> subentities = new ArrayList<mxCell>();
		boolean notExclusive = false;
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		mxCell parentEntityCell = (mxCell) inheritanceObject.getParentObject();
		EntityObject parentEntity = (EntityObject) parentEntityCell.getValue();
		List<mxICell> entities = new ArrayList<mxICell>();
		boolean disjunction = false;

		Object object = convertEntity(parentEntity, parentEntityCell.getGeometry(), null, null);
		Collection superCollection = (Collection) ((mxICell) object).getValue();
		if ((!hasConvertedEntities(inheritanceCell)) && (inheritanceObject.isExclusive())) {
			disjunction = true;
		}
		if (!((InheritanceObject) inheritanceCell.getValue()).isExclusive()) {
			subentities = getNonExclusiveGeneralizationSubentities(parentEntity.getName());
			notExclusive = true;
		}
		int count = notExclusive ? subentities.size() : inheritanceObject.getChildObjects().size();
		for (int i = 0; i < count; i++) {
			mxICell cell;
			if (notExclusive) {
				cell = (mxICell) subentities.get(i);
			} else {
				cell = (mxICell) inheritanceObject.getChildObjects().get(i);
			}
			EntityObject entity = (EntityObject) cell.getValue();
			if (!entity.isConvertedEntity()) {
				mxICell entityCell = findEntityCell(entity.getName());
				Object subEntity = convertEntity(entity, entityCell.getGeometry(), null, null);
				Collection subEntityAtt = (Collection) ((mxICell) subEntity).getValue();
				subEntityAtt.setMultiValued(false);
				subEntityAtt.setMaximumCardinality('1');
				if ((inheritanceObject.isExclusive()) && (!inheritanceObject.isPartial())
						&& (hasConvertedEntities(inheritanceCell))) {
					subEntityAtt.setOptional(false);
					subEntityAtt.setMinimumCardinality('1');
				} else {
					subEntityAtt.setOptional(true);
					subEntityAtt.setMinimumCardinality('0');
				}
				subEntityAtt.setName(entity.getName());
				insertCollectionToCollection(subEntityAtt, superCollection);
				if (disjunction) {
					EntityObject entityObject = (EntityObject) ((mxICell) inheritanceObject.getChildObjects().get(i))
							.getValue();
					entities.add(findCollectionCell(entityObject.getName()));
				}
			} else {
				mxICell sourceCell = findCollectionCell(parentEntity.getName());
				mxICell targetCell = findCollectionCell(entity.getName());
				this.noSqlModelingEditor.modelingManager.insertNoSqlReferenceAttribute(0, 0, sourceCell, targetCell);
				superCollection.setReferencedCollection(true);
			}
			entity.setMarkedEntity(true);
			entity.setConvertedEntity(true);
		}
		if (disjunction) {
			this.noSqlModelingEditor.modelingManager.getDisjunctionGeometry(entities);
		}
		parentEntity.setConvertedEntity(true);
		this.noSqlModelingEditor.modelingComponent.getGraph().refresh();
	}

	public void convertRelation(List<EntityObject> entityObjects, List<mxCell> relationCells) {
		List<mxCell> entityList = determinePath(entityObjects);
		do {
			mxCell entityOfContinuation;
			if (entityList.size() > 0) {
				entityOfContinuation = (mxCell) entityList.get(0);
				entityList.remove(0);
			} else {
				mxCell unmarkedRelationCell = getUnmarkedRelation(relationCells);
				entityOfContinuation = (mxCell) getEntitiesConnectedToRelation(unmarkedRelationCell, null).get(0);
			}
			while (getUnmarkedRelation(entityOfContinuation) != null) {
				mxCell unmarkedRelation = getUnmarkedRelation(entityOfContinuation);
				mxCell e2Cell = (mxCell) getEntitiesConnectedToRelation(unmarkedRelation, entityOfContinuation).get(0);
				if (unmarkedRelation != null) {
					if ((getRelationMaximumCardinality(unmarkedRelation).equals("(1:1)"))
							&& (getEntity2Cardinality(unmarkedRelation, entityOfContinuation).equals("(1,1)"))
							&& (getEntitiesConnectedToRelation(unmarkedRelation, entityOfContinuation).get(0) != null)
							&& (!hasConvertedEntitiesInRelations(unmarkedRelation))) {
						singleEntityModeledRelation(unmarkedRelation);
						EntityObject e2 = (EntityObject) ((mxICell) getEntitiesConnectedToRelation(unmarkedRelation,
								entityOfContinuation).get(0)).getValue();
						e2.setConvertedEntity(true);
						e2.setMarkedEntity(true);
						entityOfContinuation = e2Cell;
					} else if ((getEntitiesConnectedToRelation(unmarkedRelation, null).size() == 2)
							&& (getEntity2Cardinality(unmarkedRelation, entityOfContinuation).equals("(1,1)"))
							&& (!hasConvertedEntitiesInRelations(unmarkedRelation))
							&& (((EntityObject) entityOfContinuation.getValue())
									.getName() != ((EntityObject) e2Cell.getValue()).getName())) {
						Collection findCollectionObject = findCollection(((EntityObject) e2Cell.getValue()).getName());
						if (!(findCollectionObject == null)) {
							if (!findCollectionObject.isReferencedCollection()) {
								generalizationModeledRelation(unmarkedRelation);
								EntityObject e2 = (EntityObject) ((mxICell) getEntitiesConnectedToRelation(
										unmarkedRelation, entityOfContinuation).get(0)).getValue();
								e2.setConvertedEntity(true);
								e2.setMarkedEntity(true);
								entityOfContinuation = e2Cell;
							}
						} else {
							generalizationModeledRelation(unmarkedRelation);
							EntityObject e2 = (EntityObject) ((mxICell) getEntitiesConnectedToRelation(unmarkedRelation,
									entityOfContinuation).get(0)).getValue();
							e2.setConvertedEntity(true);
							e2.setMarkedEntity(true);
							entityOfContinuation = e2Cell;
						}
					} else {
						referenceModeledRelation(unmarkedRelation);
					}
				}
				RelationObject relation = (RelationObject) unmarkedRelation.getValue();
				relation.setMarkedRelation(true);
			}
		} while (getUnmarkedRelation(relationCells) != null);
	}

	public List<mxCell> determinePath(List<EntityObject> entityObjects) {
		List<mxCell> removedEntities = new ArrayList<mxCell>();
		List<EntityObject> removedEntitiesObjects = new ArrayList<EntityObject>();
		List<mxCell> orderedListOfEntities = new ArrayList<mxCell>();
		for (int i = 0; i < this.entityCells.size(); i++) {
			calculateFunctionalClosure((mxCell) this.entityCells.get(i));
		}
		Collections.sort(entityObjects);
		for (int i = 0; i < entityObjects.size(); i++) {
			if (((EntityObject) entityObjects.get(i)).getFunctionalClosureCount() == 1) {
				removedEntities.add((mxCell) findEntityCell(((EntityObject) entityObjects.get(i)).getName()));
			} else {
				orderedListOfEntities.add((mxCell) findEntityCell(((EntityObject) entityObjects.get(i)).getName()));
			}
		}
		for (int i = 0; i < entityObjects.size(); i++) {
			if (((EntityObject) entityObjects.get(i)).getFunctionalClosureCount() == 1) {
				entityObjects.remove(i);
			}
		}
		calculateConnectionsToRelations(removedEntities);
		for (int i = 0; i < removedEntities.size(); i++) {
			removedEntitiesObjects.add((EntityObject) ((mxCell) removedEntities.get(i)).getValue());
		}
		for (int i = 0; i < removedEntitiesObjects.size(); i++) {
			orderedListOfEntities
					.add((mxCell) findEntityCell(((EntityObject) removedEntitiesObjects.get(i)).getName()));
		}
		return orderedListOfEntities;
	}

	public void priorityListOfInheritance(mxCell parent) {
		List<Object> childs = ((InheritanceObject) parent.getValue()).getChildObjects();
		for (Object object : childs) {
			for (Object object2 : ((EntityObject) ((mxICell) object).getValue()).getChildObjects()) {
				if (((mxICell) object2).getValue() instanceof InheritanceObject) {
					priorityListOfInheritance((mxCell) object2);
				}
			}
		}
		if (!orderedListOfEntities.contains(parent))
			orderedListOfEntities.add(parent);
	}

	public void singleEntityModeledRelation(mxCell relationCell) {
		RelationObject relation = (RelationObject) relationCell.getValue();
		List<EntityObject> entities = orderEntitiesForRelationsRules(relationCell);
		EntityObject entity1 = (EntityObject) entities.get(0);
		EntityObject entity2 = (EntityObject) entities.get(1);
		ConnectorObject connector = relation.getConnectorObject(relationCell, entity2);
		Cardinality cardinality = connector.getCardinality();
		char minimumCardinality = Cardinality.getText(cardinality).charAt(1);
		char maximumCardinality = Cardinality.getText(cardinality).charAt(3);
		mxICell entity1Cell = findEntityCell(entity1.getName());
		if ((entity1.isConvertedEntity()) || (entity2.isConvertedEntity())) {
			return;
		}
		if (entity1Cell != null) {
			Object collectionObject = convertEntity(entity1, entity1Cell.getGeometry(), null, null);
			List<Object> relationAttributes = getRelationAttributes(relationCell);
			for (int i = 0; i < entity2.getChildObjects().size(); i++) {
				mxCell cell = (mxCell) entity2.getChildObjects().get(i);
				if ((cell.getValue() instanceof AttributeObject)) {
					editCardinality(minimumCardinality, maximumCardinality, (AttributeObject) cell.getValue());
				}
			}
			// Conferir esse collectionObject...
			convertEntity(entity2, entity1Cell.getGeometry(), collectionObject, null);
			if (relationAttributes.size() > 0) {
				for (int i = 0; i < relationAttributes.size(); i++) {
					editCardinality(minimumCardinality, maximumCardinality,
							(AttributeObject) relationAttributes.get(i));
					convertAttribute((AttributeObject) relationAttributes.get(i), entity1Cell.getGeometry(),
							(mxICell) collectionObject, null);
				}
			}
		} else {
			System.out.println("ERRO");
		}
	}

	public void generalizationModeledRelation(mxCell relationCell) {
		RelationObject relation = (RelationObject) relationCell.getValue();
		List<EntityObject> entities = orderEntitiesForRelationsRules(relationCell);
		EntityObject entity1 = (EntityObject) entities.get(0);
		EntityObject entity2 = (EntityObject) entities.get(1);
		mxICell entity1Cell = findEntityCell(entity1.getName());
		ConnectorObject connector = relation.getConnectorObject(relationCell, entity2);
		Cardinality cardinality = connector.getCardinality();
		char minimumCardinality = Cardinality.getText(cardinality).charAt(1);
		char maximumCardinality = Cardinality.getText(cardinality).charAt(3);
		if ((entity1.isConvertedEntity()) || (entity2.isConvertedEntity())) {
			return;
		}
		if (entity1Cell != null) {
			Object collectionObject = convertEntity(entity1, entity1Cell.getGeometry(), null, null);
			List<Object> relationAttributes = getRelationAttributes(relationCell);

			Object collection2 = convertEntity(entity2, entity1Cell.getGeometry(), null, collectionObject);
			if (relationAttributes.size() > 0) {
				for (int i = 0; i < relationAttributes.size(); i++) {
					convertAttribute((AttributeObject) relationAttributes.get(i), entity1Cell.getGeometry(),
							(mxICell) collection2, null);
				}
			}
			Collection collection2Att = (Collection) ((mxICell) collection2).getValue();
			collection2Att.setName(entity2.getName());
			collection2Att.setMinimumCardinality(minimumCardinality);
			collection2Att.setMaximumCardinality(maximumCardinality);
			if (minimumCardinality == '0') {
				collection2Att.setOptional(true);
			} else {
				collection2Att.setOptional(false);
			}
			if (maximumCardinality == '1') {
				collection2Att.setMultiValued(false);
			} else {
				collection2Att.setMultiValued(true);
			}
		} else {
			System.out.println("ERRO");
		}
		this.noSqlModelingEditor.modelingComponent.getGraph().refresh();
	}

	public void referenceModeledRelation(mxCell relationCell) {
		List<mxICell> subEntities = getEntitiesConnectedToRelation(relationCell, null);

		Object relationCollection = null;
		if ((subEntities.size() == 2) && (!relationHasAttributes(relationCell))
				&& (relationHasNoMultivaluedEntity(relationCell) != null)) {
			mxICell entity = relationHasNoMultivaluedEntity(relationCell);

			EntityObject entityObject = (EntityObject) entity.getValue();
			mxICell collection1Cell;
			if (!entityObject.isConvertedEntity()) {
				collection1Cell = (mxICell) convertEntity(entityObject, entity.getGeometry(), null, null);
				entityObject.setConvertedEntity(true);
			} else {
				collection1Cell = findCollectionCell(entityObject.getName());
			}
			mxICell collection2Cell;
			EntityObject entityObject2;
			if (subEntities.get(0) != entity) {
				entityObject2 = (EntityObject) ((mxICell) subEntities.get(0)).getValue();
				if (entityObject2.isConvertedEntity()) {
					collection2Cell = findCollectionCell(entityObject.getName());
				} else {
					collection2Cell = (mxICell) convertEntity(entityObject2,
							((mxICell) subEntities.get(0)).getGeometry(), null, null);
				}
			} else {
				entityObject2 = (EntityObject) ((mxICell) subEntities.get(1)).getValue();
				if (entityObject2.isConvertedEntity()) {
					collection2Cell = findCollectionCell(entityObject.getName());
				} else {
					collection2Cell = (mxICell) convertEntity(entityObject2,
							((mxICell) subEntities.get(1)).getGeometry(), null, null);
				}
			}
			entityObject2.setConvertedEntity(true);
			this.noSqlModelingEditor.modelingManager.insertNoSqlReferenceAttribute(0, 0, collection2Cell,
					collection1Cell);
			Collection referencedCollection = (Collection) collection2Cell.getValue();
			referencedCollection.setReferencedCollection(true);
		} else {
			/*
			 * Caso não seja uma especialização
			 */
			for (int i = 0; i < subEntities.size(); i++) {
				EntityObject entity = (EntityObject) ((mxICell) subEntities.get(i)).getValue();
				Object collection;
				if (!entity.isConvertedEntity()) {
					collection = convertEntity((EntityObject) ((mxICell) subEntities.get(i)).getValue(),
							((mxICell) subEntities.get(i)).getGeometry(), null, null);
				} else {
					collection = findCollectionCell(entity.getName());
				}
				if (i == 0) {
					relationCollection = convertRelationIntoCollection(relationCell, collection);
					Collection relationCollectionRestAtt = (Collection) ((mxICell) relationCollection).getValue();
					relationCollectionRestAtt.setName(((RelationObject) relationCell.getValue()).getName());
					this.noSqlModelingEditor.modelingComponent.getGraph().refresh();
				} else {
					this.noSqlModelingEditor.modelingManager.insertNoSqlReferenceAttribute(0, 0, (mxCell) collection,
							(mxCell) relationCollection);
					((Collection) ((mxICell) collection).getValue()).setReferencedCollection(true);
					this.noSqlModelingEditor.modelingManager.insertNoSqlAttributeObject(0, 0,
							((mxICell) collection).getValue().toString(), (mxCell) collection);
					mxICell identifierAttribute = getMxICellFrom(((mxICell) collection).getValue().toString(),
							(mxCell) collection);
					if (!identifierAttribute.equals(null))
						((NoSqlAttributeObject) identifierAttribute.getValue()).setIdentifierAttribute(true);
					mxICell refAttribute = getMxICellFrom(
							((mxICell) collection).getValue().toString().toLowerCase() + "_REF",
							(mxCell) relationCollection);
					if (!refAttribute.equals(null))
						((NoSqlAttributeObject) refAttribute.getValue())
								.setReferencedObject((NoSqlAttributeObject) identifierAttribute.getValue());
				}
			}
		}
	}

	public mxICell relationHasNoMultivaluedEntity(mxCell relationCell) {
		RelationObject relation = (RelationObject) relationCell.getValue();
		for (int i = 0; i < relation.getRelatedObjects().size(); i++) {
			if ((relation.getRelatedObjects().get(i) instanceof EntityObject)) {
				EntityObject entity = (EntityObject) relation.getRelatedObjects().get(i);
				mxICell entityCell = findEntityCell(entity.getName());
				ConnectorObject connector = relation.getConnectorObject(relationCell, entity);
				Cardinality cardinality = connector.getCardinality();
				if ((Cardinality.getText(cardinality).equals("(1,1)"))
						|| (Cardinality.getText(cardinality).equals("(0,1)"))) {
					return entityCell;
				}
			}
		}
		return null;
	}

	public List<mxCell> orderParentInheritanceList(List<mxCell> inheritanceObjects) {
		List<mxCell> orderedInheritanceObjects = new ArrayList<mxCell>();
		Iterator<mxCell> iterator = inheritanceObjects.iterator();
		while (iterator.hasNext()) {
			mxCell inheritanceCell = (mxCell) iterator.next();
			InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
			EntityObject entityObject = (EntityObject) ((mxCell) inheritanceObject.getParentObject()).getValue();
			mxCell cell = (mxCell) entityObject.getParentObject();
			if ((cell == null) || (!(cell.getValue() instanceof InheritanceObject))) {
				orderedInheritanceObjects.add(inheritanceCell);
			}
		}
		return orderedInheritanceObjects;
	}

	public void orderInheritanceList(mxCell parent) {
		InheritanceObject inheritanceParent = (InheritanceObject) parent.getValue();
		List<Object> filhos = inheritanceParent.getChildObjects();
		for (Object filho : filhos) {
			mxCell cell = (mxCell) filho;
			EntityObject filhoEntity = (EntityObject) cell.getValue();
			if (filhoEntity.getChildObjects().size() > 0) {
				for (Object i : filhoEntity.getChildObjects()) {
					mxCell icell = (mxCell) i;
					if ((icell.getValue() instanceof InheritanceObject)) {
						orderInheritanceList((mxCell) i);
					}
				}
			}
		}
		convertGeneralization(parent);
	}

	public List<EntityObject> orderEntitiesForRelationsRules(mxCell relationCell) {
		List<EntityObject> orderedEntities = new ArrayList<EntityObject>();
		RelationObject relation = (RelationObject) relationCell.getValue();
		EntityObject entity1 = (EntityObject) relation.getRelatedObjects().get(0);
		EntityObject entity2 = (EntityObject) relation.getRelatedObjects().get(1);
		ConnectorObject connector = relation.getConnectorObject(relationCell, entity1);
		ConnectorObject connector2 = relation.getConnectorObject(relationCell, entity2);
		Cardinality cardinality = connector.getCardinality();
		Cardinality cardinality2 = connector2.getCardinality();
		if (Cardinality.getText(cardinality).equals("(1,1)")) {
			orderedEntities.add(entity1);
			orderedEntities.add(entity2);
			return orderedEntities;
		}
		if (Cardinality.getText(cardinality2).equals("(1,1)")) {
			orderedEntities.add(entity2);
			orderedEntities.add(entity1);
			return orderedEntities;
		}
		return null;
	}

	public List<mxCell> getNonExclusiveGeneralizationSubentities(String parentName) {
		List<mxCell> subentities = new ArrayList<mxCell>();
		for (int i = 0; i < this.inheritanceObjects.size(); i++) {
			InheritanceObject inheritanceObject = (InheritanceObject) ((mxCell) this.inheritanceObjects.get(i))
					.getValue();
			mxCell parentEntityCell = (mxCell) inheritanceObject.getParentObject();
			EntityObject parentEntity = (EntityObject) parentEntityCell.getValue();
			if (parentEntity.getName().equals(parentName)) {
				subentities.add((mxCell) inheritanceObject.getChildObjects().get(0));
			}
		}
		return subentities;
	}

	public void setInheritancesConverted(String parentName) {
		for (int i = 0; i < this.inheritanceObjects.size(); i++) {
			InheritanceObject inheritanceObject = (InheritanceObject) ((mxCell) this.inheritanceObjects.get(i))
					.getValue();
			mxCell parentEntityCell = (mxCell) inheritanceObject.getParentObject();
			EntityObject parentEntity = (EntityObject) parentEntityCell.getValue();
			if (parentEntity.getName().equals(parentName)) {
				inheritanceObject.setConverted(true);
			}
		}
	}

	public boolean hasReferencedEntities(mxICell inheritanceCell) {
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		List<Object> inheritanceChildsList = inheritanceObject.getChildObjects();
		for (int i = 0; i < inheritanceChildsList.size(); i++) {
			mxICell cell = (mxICell) inheritanceChildsList.get(i);
			if ((cell.getValue() instanceof EntityObject)) {
				EntityObject entity = (EntityObject) cell.getValue();
				Collection collection = findCollection(entity.getName());
				if ((collection != null) && (collection.isReferencedCollection())) {
					return true;
				}
			}
		}
		return false;
	}

	public mxICell insertCollectionToCollection(Collection childCollection, Collection superCollection) {
		ModelingManager modelingManager = this.noSqlModelingEditor.modelingManager;
		mxICell block = (mxICell) modelingManager.insertBlock(0.0D, 0.0D,
				findCollectionCell(superCollection.getName()));
		Collection blockAtt = (Collection) block.getValue();
		blockAtt.setName(childCollection.getName());
		for (int i = 0; i < childCollection.getChildObjects().size(); i++) {
			mxICell cell = (mxICell) childCollection.getChildObjects().get(i);
			Object object = cell.getValue();
			if ((object instanceof NoSqlAttributeObject)) {
				NoSqlAttributeObject oldAttribute = (NoSqlAttributeObject) object;
				mxICell newAttribute = (mxICell) modelingManager.insertNoSqlAttributeObject(0.0D, 0.0D,
						mxResources.get("noSqlAttribute"), block);
				NoSqlAttributeObject newAttributeAtt = (NoSqlAttributeObject) newAttribute.getValue();
				newAttributeAtt.setType(oldAttribute.getType());
				newAttributeAtt.setOptional(oldAttribute.isOptional());
				newAttributeAtt.setMinimumCardinality(oldAttribute.getMinimumCardinality());
				newAttributeAtt.setName(oldAttribute.getName());
				newAttributeAtt.setMultiValued(oldAttribute.isMultiValued());
				newAttributeAtt.setMaximumCardinality(oldAttribute.getMaximumCardinality());
			} else if ((object instanceof Collection)) {
				insertCollectionToCollection((Collection) object, blockAtt);
			}
		}
		this.collectionsCreated.add((Collection) ((mxCell) block).getValue());
		this.cellsCreated.add((mxCell) block);

		removeCell(findCollectionCell(childCollection.getName()), block);
		return block;
	}

	public String getEntity2Cardinality(mxCell relationCell, mxCell entity1) {
		RelationObject relation = (RelationObject) relationCell.getValue();
		Cardinality cardinality = relation.getConnectorObject(relationCell, (EntityObject) entity1.getValue())
				.getCardinality();
		return Cardinality.getText(cardinality);
	}

	public String getRelationMaximumCardinality(mxCell relationCell) {
		List<mxICell> entities = new ArrayList<mxICell>();
		RelationObject relation = (RelationObject) relationCell.getValue();
		for (int i = 0; i < relation.getRelatedObjects().size(); i++) {
			ModelingObject object = (ModelingObject) relation.getRelatedObjects().get(i);
			if ((relation.getRelatedObjects().get(i) instanceof EntityObject)) {
				entities.add(findEntityCell(object.getName()));
			}
		}
		Cardinality cardinality1 = relation
				.getConnectorObject(relationCell, (EntityObject) ((mxICell) entities.get(0)).getValue())
				.getCardinality();
		Cardinality cardinality2 = relation
				.getConnectorObject(relationCell, (EntityObject) ((mxICell) entities.get(1)).getValue())
				.getCardinality();
		String[] chars = Cardinality.getText(cardinality1).split("");
		String maximumCardinality1 = chars[3];
		chars = Cardinality.getText(cardinality2).split("");
		String maximumCardinality2 = chars[3];
		if (maximumCardinality1.equals(maximumCardinality2)) {
			return "(" + maximumCardinality1 + ":" + maximumCardinality1 + ")";
		}
		return "(1:n)";
	}

	public mxCell getUnmarkedRelation(List<mxCell> relationCells) {
		for (int i = 0; i < relationCells.size(); i++) {
			RelationObject relation = (RelationObject) ((mxCell) relationCells.get(i)).getValue();
			if (!relation.isMarkedRelation()) {
				return (mxCell) relationCells.get(i);
			}
		}
		return null;
	}

	public mxCell getUnmarkedRelation(mxCell entityCell) {
		if (entityCell == null) {
			return null;
		}
		List<mxICell> relationsConnectedToEntity = getRelationsConnectedToEntity(entityCell);
		for (int i = 0; i < relationsConnectedToEntity.size(); i++) {
			RelationObject relation = (RelationObject) ((mxICell) relationsConnectedToEntity.get(i)).getValue();
			if (!relation.isMarkedRelation()) {
				if ((relation instanceof AssociativeRelationObject)) {
					this.associativeEntityFlag = true;
				} else {
					this.associativeEntityFlag = false;
				}
				return (mxCell) relationsConnectedToEntity.get(i);
			}
		}
		return null;
	}

	public mxICell findEntityCell(String name) {
		for (int i = 0; i < this.entityCells.size(); i++) {
			EntityObject entity = (EntityObject) ((mxCell) this.entityCells.get(i)).getValue();
			if (entity.getName().equals(name)) {
				return (mxICell) this.entityCells.get(i);
			}
		}
		return null;
	}

	public List<Object> getRelationAttributes(mxCell relationParent) {
		List<Object> relationAttributes = new ArrayList<Object>();
		RelationObject relation = (RelationObject) relationParent.getValue();
		for (Object object : relation.getChildObjects()) {
			if ((((mxCell) object).getValue() instanceof AttributeObject)) {
				relationAttributes.add(((mxCell) object).getValue());
			}
		}
		return relationAttributes;
	}

	public List<Object> getAssociativeEntityAttributes(mxCell associativeEntity) {
		List<Object> associativeEntityAttributes = new ArrayList<Object>();
		AssociativeEntityObject associativeEntityObject = (AssociativeEntityObject) associativeEntity.getValue();
		for (Object object : associativeEntityObject.getChildObjects()) {
			if ((((mxCell) object).getValue() instanceof AttributeObject)) {
				associativeEntityAttributes.add(((mxCell) object).getValue());
			}
		}
		return associativeEntityAttributes;
	}

	public boolean hasConvertedEntities(mxCell inheritanceCell) {
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		for (int i = 0; i < inheritanceObject.getChildObjects().size(); i++) {
			EntityObject entity = (EntityObject) ((mxICell) inheritanceObject.getChildObjects().get(i)).getValue();
			if (entity.isConvertedEntity()) {
				return true;
			}
		}
		return false;
	}

	public boolean hasConvertedEntitiesInRelations(mxCell relationCell) {
		List<mxICell> entities = getEntitiesConnectedToRelation(relationCell, null);
		for (int i = 0; i < entities.size(); i++) {
			EntityObject entityObject = (EntityObject) ((mxICell) entities.get(i)).getValue();
			if (entityObject.isConvertedEntity()) {
				return true;
			}
		}
		return false;
	}

	public boolean subclassesConnectedToRelation(mxICell inheritanceCell) {
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		List<Object> inheritanceChildsList = inheritanceObject.getChildObjects();
		for (int i = 0; i < inheritanceChildsList.size(); i++) {
			mxICell entityCell = (mxICell) inheritanceChildsList.get(i);
			if ((entityCell.getValue() instanceof EntityObject)) {
				int edgeCount = entityCell.getEdgeCount();
				for (int j = 0; j < edgeCount; j++) {
					mxICell source = ((mxCell) entityCell.getEdgeAt(j)).getSource();
					mxICell target = ((mxCell) entityCell.getEdgeAt(j)).getTarget();
					if (((target.getValue() instanceof RelationObject))
							|| ((source.getValue() instanceof RelationObject))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean superclassConnectedToRelation(mxICell inheritanceCell) {
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		Object parentObject = inheritanceObject.getParentObject();
		mxICell entityCell = (mxICell) parentObject;
		if ((entityCell.getValue() instanceof EntityObject)) {
			int edgeCount = entityCell.getEdgeCount();
			for (int j = 0; j < edgeCount; j++) {
				mxICell source = ((mxCell) entityCell.getEdgeAt(j)).getSource();
				mxICell target = ((mxCell) entityCell.getEdgeAt(j)).getTarget();
				if (((target.getValue() instanceof RelationObject))
						|| ((source.getValue() instanceof RelationObject))) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean generalizationCountTest(mxICell inheritanceCell) {
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		List<Object> inheritanceChildsList = inheritanceObject.getChildObjects();
		for (int i = 0; i < inheritanceChildsList.size(); i++) {
			mxICell cell = (mxICell) inheritanceChildsList.get(i);
			if ((cell.getValue() instanceof EntityObject)) {
				EntityObject entityObject = (EntityObject) cell.getValue();
				int generalizationCount = generalizationCount(findEntityCell(entityObject.getName()));
				if (generalizationCount > 1) {
					return false;
				}
			}
		}
		return true;
	}

	public int generalizationCount(mxICell entityCell) {
		int count = 0;

		int edgeCount = entityCell.getEdgeCount();
		for (int j = 0; j < edgeCount; j++) {
			mxICell source = ((mxCell) entityCell.getEdgeAt(j)).getSource();
			mxICell target = ((mxCell) entityCell.getEdgeAt(j)).getTarget();
			if (((target.getValue() instanceof InheritanceObject))
					|| ((source.getValue() instanceof InheritanceObject))) {
				count++;
			}
		}
		return count;
	}

	public boolean everyRelationIsMarked(List<mxCell> relationCells) {
		for (int i = 0; i < relationCells.size(); i++) {
			if ((((mxCell) relationCells.get(i)).getValue() instanceof AssociativeEntityObject)) {
				if (!((AssociativeEntityObject) ((mxCell) relationCells.get(i)).getValue()).getRelationObject()
						.isMarkedRelation()) {
				}
				return false;
			}
			if (!((RelationObject) ((mxCell) relationCells.get(i)).getValue()).isMarkedRelation()) {
				return false;
			}
		}
		return true;
	}

	public void calculateFunctionalClosure(mxCell entityCell) {
		List<mxICell> entitiesConnectedToRelation = new ArrayList<mxICell>();
		for (int i = 0; i < entityCell.getEdgeCount(); i++) {
			mxICell source = ((mxCell) entityCell.getEdgeAt(i)).getSource();
			mxICell target = ((mxCell) entityCell.getEdgeAt(i)).getTarget();
			mxCell cell = null;
			if ((target.getValue() instanceof RelationObject)) {
				cell = (mxCell) target;
			} else if ((source.getValue() instanceof RelationObject)) {
				cell = (mxCell) source;
			}
			if (cell != null) {
				entitiesConnectedToRelation = getEntitiesConnectedToRelation(cell, entityCell);
				RelationObject relation = (RelationObject) cell.getValue();
				for (int j = 0; j < entitiesConnectedToRelation.size(); j++) {
					EntityObject newEntity = (EntityObject) ((mxICell) entitiesConnectedToRelation.get(j)).getValue();
					ConnectorObject connector = relation.getConnectorObject(cell, newEntity);
					Cardinality cardinality = connector.getCardinality();
					if (Cardinality.getText(cardinality).equals("(1,1)")) {
						((EntityObject) ((mxICell) entitiesConnectedToRelation.get(j)).getValue())
								.addFunctionalClosureCount();
						// calculateFunctionalClosure((mxCell)
						// entitiesConnectedToRelation.get(j)); Infinite Loop
					}
				}
			}
		}
	}

	public List<mxICell> getEntitiesConnectedToRelation(mxCell relationCell, mxCell entityCell) {
		List<mxICell> entitiesConnectedToRelation = new ArrayList<mxICell>();
		for (int i = 0; i < relationCell.getEdgeCount(); i++) {
			if ((((mxCell) relationCell.getEdgeAt(i)).getSource().getValue() instanceof EntityObject)) {
				mxICell newEntity = ((mxCell) relationCell.getEdgeAt(i)).getSource();
				if (newEntity != entityCell) {
					entitiesConnectedToRelation.add(newEntity);
				}
			} else if ((((mxCell) relationCell.getEdgeAt(i)).getTarget().getValue() instanceof EntityObject)) {
				mxICell newEntity = ((mxCell) relationCell.getEdgeAt(i)).getTarget();
				if (newEntity != entityCell) {
					entitiesConnectedToRelation.add(newEntity);
				}
			}
		}
		return entitiesConnectedToRelation;
	}

	public List<mxICell> getRelationsConnectedToEntity(mxCell entityCell) {
		List<mxICell> relationsConnectedToEntity = new ArrayList<mxICell>();
		for (int i = 0; i < entityCell.getEdgeCount(); i++) {
			if ((((mxCell) entityCell.getEdgeAt(i)).getSource().getValue() instanceof RelationObject)) {
				relationsConnectedToEntity.add(((mxCell) entityCell.getEdgeAt(i)).getSource());
			} else if ((((mxCell) entityCell.getEdgeAt(i)).getTarget().getValue() instanceof RelationObject)) {
				relationsConnectedToEntity.add(((mxCell) entityCell.getEdgeAt(i)).getTarget());
			} else if ((((mxCell) entityCell.getEdgeAt(i)).getSource().getValue() instanceof AssociativeEntityObject)) {
				AssociativeEntityObject associativeEntity = (AssociativeEntityObject) ((mxCell) entityCell.getEdgeAt(i))
						.getSource().getValue();
				mxICell relation = findEntityCell(associativeEntity.getRelationObject().getName());
				relationsConnectedToEntity.add(relation);
			} else if ((((mxCell) entityCell.getEdgeAt(i)).getTarget().getValue() instanceof AssociativeEntityObject)) {
				AssociativeEntityObject associativeEntity = (AssociativeEntityObject) ((mxCell) entityCell.getEdgeAt(i))
						.getTarget().getValue();
				mxICell relation = findEntityCell(associativeEntity.getRelationObject().getName());
				relationsConnectedToEntity.add(relation);
			}
		}
		return relationsConnectedToEntity;
	}

	public void calculateConnectionsToRelations(List<mxCell> removedEntitiesCells) {
		for (int i = 0; i < removedEntitiesCells.size(); i++) {
			int edgeCount = ((mxCell) removedEntitiesCells.get(i)).getEdgeCount();
			for (int j = 0; j < edgeCount; j++) {
				mxICell source = ((mxCell) ((mxCell) removedEntitiesCells.get(i)).getEdgeAt(j)).getSource();
				mxICell target = ((mxCell) ((mxCell) removedEntitiesCells.get(i)).getEdgeAt(j)).getTarget();
				if (((source.getValue() instanceof RelationObject))
						|| ((target.getValue() instanceof RelationObject))) {
					((EntityObject) ((mxCell) removedEntitiesCells.get(i)).getValue()).addNumberOfRelationsConnected();
				}
			}
		}
	}

	public boolean relationHasAttributes(mxICell relationCell) {
		int edgeCount = relationCell.getEdgeCount();
		for (int i = 0; i < edgeCount; i++) {
			Object source = ((mxCell) relationCell.getEdgeAt(i)).getSource().getValue();
			Object target = ((mxCell) relationCell.getEdgeAt(i)).getTarget().getValue();
			if (((source instanceof AttributeObject)) || ((target instanceof AttributeObject))) {
				return true;
			}
		}
		return false;
	}

	public Collection findCollection(String name) {
		for (int i = 0; i < this.collectionsCreated.size(); i++) {
			if (((Collection) this.collectionsCreated.get(i)).getName().equals(name)) {
				return (Collection) this.collectionsCreated.get(i);
			}
		}
		return null;
	}

	public mxICell findCollectionCell(String name) {
		for (int i = 0; i < this.cellsCreated.size(); i++) {
			if (((((mxCell) this.cellsCreated.get(i)).getValue() instanceof Collection))
					&& (((Collection) ((mxCell) this.cellsCreated.get(i)).getValue()).getName().equals(name))) {
				return (mxICell) this.cellsCreated.get(i);
			}
		}
		return null;
	}

	private void removeCell(mxICell cell, mxICell newCell) {
		Collection collection = (Collection) cell.getValue();

		this.cellsCreated.remove(cell);
		this.collectionsCreated.remove(collection);

		this.noSqlModelingEditor.modelingComponent.getGraph().getModel().remove(cell);
		this.noSqlModelingEditor.modelingComponent.getGraph().refresh();
	}

	public void editCardinality(char minimumCardinality, char maximumCardinality, AttributeObject attribute) {
		if (minimumCardinality == '0') {
			attribute.setOptional(true);
		} else if (minimumCardinality == '1') {
			attribute.setOptional(false);
		}
		if (maximumCardinality == '1') {
			attribute.setMultiValued(false);
		} else if (maximumCardinality == 'n') {
			attribute.setMultiValued(true);
		}
		attribute.setIdentifier(false);
	}

	/**
	 * Get a named mxICell from a mxCell.
	 * 
	 * @param nameOfThemxICell
	 *            name of the mxICell that is searched.
	 * @param collection
	 *            mxCell of the collection that is going to be searched.
	 * @return the mxICell if that exist, otherwise null.
	 */
	private mxICell getMxICellFrom(String nameOfThemxICell, mxCell collection) {
		for (int i = 0; i < collection.getChildCount(); i++)
			if (collection.getChildAt(i).getValue().toString().equals(nameOfThemxICell))
				return collection.getChildAt(i);
		return null;
	}

}
