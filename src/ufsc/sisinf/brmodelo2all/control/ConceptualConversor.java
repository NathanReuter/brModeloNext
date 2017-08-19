package ufsc.sisinf.brmodelo2all.control;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
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
import ufsc.sisinf.brmodelo2all.model.objects.AttributeObject;
import ufsc.sisinf.brmodelo2all.model.objects.ColumnObject;
import ufsc.sisinf.brmodelo2all.model.objects.ConnectorObject;
import ufsc.sisinf.brmodelo2all.model.objects.EntityObject;
import ufsc.sisinf.brmodelo2all.model.objects.InheritanceObject;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;
import ufsc.sisinf.brmodelo2all.model.objects.RelationObject;
import ufsc.sisinf.brmodelo2all.model.objects.TableObject;
import ufsc.sisinf.brmodelo2all.ui.AppMainWindow;
import ufsc.sisinf.brmodelo2all.ui.ConversionIteratorWindow;

public class ConceptualConversor {

	private final ModelingComponent conceptualModelingComponent;
	private final ModelingEditor logicalModelingEditor;
	private final AppMainWindow mainWindow;
	/**
	 * @var atributos Listas
	 */
	private List<TableObject> tablesCreated = new ArrayList<TableObject>();
	private List<mxCell> cellsCreated = new ArrayList<mxCell>();
	private HashMap<String, mxCell> hashDeletados = new HashMap<>();
	private List<mxCell> entityCells = new ArrayList<mxCell>();
	/**
	 * @var atributos booleanos de controle
	 */
	private boolean adoptSugestions = false;
	private boolean canceledByUser = false;
	private boolean tablesForAllEntities = false;
	private boolean noTablesForAllEntities = false;

	/**
	 * Constructor
	 *
	 * @param conceptualModelingComponent
	 * @param logicalModelingEditor
	 * @param mainWindow
	 */
	public ConceptualConversor(ModelingComponent conceptualModelingComponent, ModelingEditor logicalModelingEditor,
			final AppMainWindow mainWindow) {
		this.conceptualModelingComponent = conceptualModelingComponent;
		this.logicalModelingEditor = logicalModelingEditor;
		this.mainWindow = mainWindow;

	}

	/*
	 * CONVERT CONCEITUAL -> LOGICO
	 */
	public void convertModeling() {

		List<mxCell> inheritanceObjects = new ArrayList<mxCell>();
		List<mxCell> relationObjects = new ArrayList<mxCell>();
		List<mxCell> associativeEntityObjects = new ArrayList<mxCell>();

		/*
		 * Run through all entities at the modeling and transform each one in a
		 * table for the logical modeling check if the entity is weak to deal
		 * with its conversion the way it is supposed to be dealed
		 */

		mxRectangle rect = conceptualModelingComponent.getGraph().getGraphBounds();

		int x = (int) rect.getX();
		int y = (int) rect.getY();

		Rectangle ret = new Rectangle(x + 60000, y + 60000);

		Object[] cells = conceptualModelingComponent.getCells(ret);
		for (Object cell : cells) {
			if (cell instanceof mxCell) {

				mxCell objectCell = (mxCell) cell;
				mxGeometry geometry = objectCell.getGeometry();
				if (objectCell.getValue() instanceof EntityObject) {
					entityCells.add(objectCell);
					EntityObject entity = (EntityObject) objectCell.getValue();
					convertEntity(entity, geometry);
					weakEntityConversion(objectCell, geometry);

				} else if (objectCell.getValue() instanceof InheritanceObject) {
					inheritanceObjects.add(objectCell);
				} else if (objectCell.getValue() instanceof AssociativeEntityObject) {
					associativeEntityObjects.add(objectCell);
				} else if (objectCell.getValue() instanceof RelationObject) {
					relationObjects.add(objectCell);
				}
			}
		}

		if (canceledByUser) {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
					"Conversï¿½o cancelada pelo usuï¿½rio", "Aviso", 0); //$NON-NLS-1$
			return;
		}

		/*
		 * after converting all entities, deal with inheritances and relations
		 * convertion
		 */
		List<mxCell> orderedInheritanceParents = orderParentInheritanceList(inheritanceObjects);
		if (inheritanceObjects.size() > 0 && orderedInheritanceParents.size() == 0) {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
					"Referencia circular na especializaï¿½ï¿½o/generalizaï¿½ï¿½o de entidades! Clique em OK para finalizar a conversï¿½o.", //$NON-NLS-1$
					"Aviso", 0);
			return;
		}

		Iterator<mxCell> iterator = orderedInheritanceParents.iterator();
		while (iterator.hasNext() && !canceledByUser) {
			/* Convert Inheritances */
			orderInheritanceList(iterator.next());

		}

		if (canceledByUser) {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
					"Conversï¿½o cancelada pelo usuï¿½rio", "Aviso", 0); //$NON-NLS-1$
			return;
		}

		/* AssociativeRelations Objects */
		iterator = associativeEntityObjects.iterator();
		while (iterator.hasNext() && !canceledByUser) {
			convertAssociativeRelations(iterator.next());
		}

		/* Relations Objects */
		iterator = relationObjects.iterator();
		while (iterator.hasNext() && !canceledByUser) {
			convertRelations(iterator.next());
		}

		if (canceledByUser) {
			JOptionPane.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
					"Conversï¿½o cancelada pelo usuï¿½rio", "Aviso", 0); //$NON-NLS-1$
		}
	}

	public void convertRelations(mxCell relationCell) {
		RelationObject relationObject = (RelationObject) relationCell.getValue();

		if (relationObject.getRelatedObjects().size() < 2) {
			JOptionPane
					.showMessageDialog((JFrame) SwingUtilities.windowForComponent(mainWindow),
							"O relacionamento \"" + relationObject.getName()
									+ "\" nï¿½o foi bem formado e serï¿½ desconsiderado nesta conversï¿½o!",
							"Aviso", 0);
			return;
		}

		List<ModelingObject> relatedObjects = relationObject.getRelatedObjects();
		List<Cardinality> cardinalidades = new ArrayList<>();
		List<EntityObject> entidades = new ArrayList<>();

		boolean normal = true;
		if (relatedObjects.size() == 3) {
			convertTernario(relationCell);
		} else if (relatedObjects.size() == 2) {
			for (ModelingObject modelingObject : relatedObjects) {
				if (modelingObject instanceof EntityObject) {
					EntityObject entity = (EntityObject) modelingObject;
					if (entity.isSelfRelated()) {
						normal = false;
						convertSelfRelatedEntity(relationCell);
						break;
					} else {
						ConnectorObject connector = relationObject.getConnectorObject(relationCell, entity);
						Cardinality cardinality = connector.getCardinality();
						if (cardinality == null) {

						}
						cardinalidades.add(cardinality);
						entidades.add(entity);
					}
				}
			}
			if (normal) {
				// Chamar a funcao correta de conversao
				EntityObject entityTarget = convertByRelationType(entidades, relationCell, cardinalidades);
			}
		} else {
			JOptionPane.showMessageDialog(null,
					"problema na formataï¿½ï¿½o do relacionamento " + relationObject.getName());
		}
	}

	public void convertEntity(EntityObject entity, mxGeometry geometry) {
		String tableName = entity.getName();
		double x = geometry.getX();
		double y = geometry.getY();

		Modeling logicalModeling = (Modeling) logicalModelingEditor.modelingComponent.getGraph();

		Object table = logicalModeling.insertVertex(logicalModeling.getDefaultParent(), null,
				new TableObject(tableName), x, y, 150, 30, // $NON-NLS-1$
				"table"); //$NON-NLS-1$

		tablesCreated.add((TableObject) ((mxCell) table).getValue());
		cellsCreated.add(((mxCell) table));

		// check for attributes for this entity and convert them as fields for
		// the created table
		int count = entity.getChildObjects().size();
		for (int i = 0; i < count && !canceledByUser; i++) {
			mxCell cell = (mxCell) entity.getChildObjects().get(i);
			if (cell.getValue() instanceof AttributeObject) {
				convertAttribute((AttributeObject) cell.getValue(), ((mxCell) table).getGeometry(), (mxCell) table);
			}

		}
	}

	public void convertAttribute(AttributeObject attribute, mxGeometry tableGeometry, mxCell table) {
		mxCell parentCell = ((mxCell) attribute.getParentObject());
		EntityObject parentEntity = (EntityObject) parentCell.getValue();
		String parentName = parentEntity.getName();
		int selectedAlternative = 0;
		List<AttributeObject> attributes = new ArrayList<AttributeObject>();
		double x = tableGeometry.getCenterX();
		double y = tableGeometry.getCenterY();

		// aqui!
		mxICell sourceC = (mxICell) table;
		mxGeometry source = tableGeometry;

		double srcX = source.getX();
		double srcY = source.getY();

		if ((attribute.isMultiValued() || attribute.isComposed()) && !(attribute.isMultiValued()
				&& attribute.getMaximumCardinality() == '1' && !attribute.isComposed())) {
			getAttributeChilds(attribute, attributes);
		} else {
			attributes.add(attribute);
		}
		// cardinalidade true = 1-n ---- false 0-n
		boolean isRequired = true;
		if (attribute.isOptional()) {
			isRequired = false;
		}

		if (attribute.isMultiValued() && attribute.getMaximumCardinality() != '1') {
			if (attribute.getMaximumCardinality() == 'n') {

				Modeling logicalModeling = (Modeling) logicalModelingEditor.modelingComponent.getGraph();

				mxCell tableCell = (mxCell) logicalModeling.insertVertex(logicalModeling.getDefaultParent(), null,
						new TableObject(attribute.getName()),
						tableGeometry.getCenterX() + (tableGeometry.getWidth() * 2), tableGeometry.getY(), 150, 30,
						"table"); //$NON-NLS-1$

				tablesCreated.add((TableObject) ((mxCell) tableCell).getValue());
				cellsCreated.add(((mxCell) tableCell));
				mxGeometry newTableGeometry = tableCell.getGeometry();
				x = newTableGeometry.getCenterX();
				y = newTableGeometry.getCenterY();

				mxICell targetC = ((mxICell) tableCell);
				mxGeometry target = targetC.getGeometry();
				double tgtX = target.getX();
				double tgtY = target.getY();

				double xFinal = srcX > tgtX ? tgtX + ((srcX - tgtX) / 2) : srcX + ((tgtX - srcX) / 2);
				double yFinal = srcY > tgtY ? tgtY + ((srcY - tgtY) / 2) : srcY + ((tgtY - srcY) / 2);
				if (xFinal == srcX && xFinal == tgtX && yFinal == srcY && yFinal == tgtY) {
					y += source.getHeight() + (source.getHeight() * 1.5);
				}

				Object column = logicalModelingEditor.modelingManager.insertColumnObject(x, y,
						mxResources.get("primaryKey"));
				ColumnObject columnObject = (ColumnObject) ((mxCell) column).getValue();
				columnObject.setPrimaryKey(true);
				columnObject.setName(attribute.getName());
				columnObject.setType(attribute.getType());

				Object[] objects = new Object[5];

				objects[2] = logicalModeling.insertVertex(logicalModeling.getDefaultParent(), null, "", xFinal, yFinal, //$NON-NLS-1$
						20, 20, "tableRelation"); //$NON-NLS-1$
				ConnectorObject firstConnector = new ConnectorObject(Cardinality.getValue("(1,1)")); //$NON-NLS-1$
				objects[1] = logicalModeling.insertEdge(logicalModeling.getDefaultParent(), null, firstConnector,
						objects[2], sourceC, "entityRelationConnector"); //$NON-NLS-1$
				ConnectorObject secondConnector;
				if (isRequired) {
					secondConnector = new ConnectorObject(Cardinality.getValue("(1,n)")); //$NON-NLS-1$
				} else {
					secondConnector = new ConnectorObject(Cardinality.getValue("(0,n)")); //$NON-NLS-1$
				}
				objects[3] = logicalModeling.insertEdge(logicalModeling.getDefaultParent(), null, secondConnector,
						objects[2], targetC, "entityRelationConnector"); //$NON-NLS-1$

				String[] messages = new String[2];
				String[] alternatives = new String[2];

				Messages.fillAttributeInteraction(attribute, messages, alternatives, 1);

				selectedAlternative = askToUser(messages, alternatives, selectedAlternative);
				if (selectedAlternative == 0) {
					Object columnFk = logicalModelingEditor.modelingManager.insertColumnObject(x, y,
							mxResources.get("foreignKey"));
					ColumnObject columnFkAtt = (ColumnObject) ((mxCell) columnFk).getValue();
					columnFkAtt.setForeignKey(true);
					columnFkAtt.setName("fk_" + parentName);
					columnFkAtt.setType("int");
				} else if (selectedAlternative == 1) {

					Object columnFk = logicalModelingEditor.modelingManager.insertColumnObject(x, y,
							mxResources.get("bothKeys"));
					ColumnObject columnFkAtt = (ColumnObject) ((mxCell) columnFk).getValue();
					columnFkAtt.setForeignKey(true);
					columnFkAtt.setPrimaryKey(true);
					columnFkAtt.setName("fk_" + parentName);
					columnFkAtt.setType("int");

				}

				int quant = tableCell.getEdgeCount();

				for (int i = 0; i < quant; i++) {
					mxCell cell = (mxCell) tableCell.getEdgeAt(i);

				}
			}
			if (attribute.getMaximumCardinality() != 'n') {

				String[] messages = new String[2];
				String[] alternatives = new String[2];
				Messages.fillAttributeInteraction(attribute, messages, alternatives, 2);

				selectedAlternative = askToUser(messages, alternatives, selectedAlternative);

				if (selectedAlternative == 0) {

					Modeling logicalModeling = (Modeling) logicalModelingEditor.modelingComponent.getGraph();

					mxCell tableCell = (mxCell) logicalModeling.insertVertex(logicalModeling.getDefaultParent(), null,
							new TableObject(attribute.getName()),
							tableGeometry.getCenterX() + (tableGeometry.getWidth() * 2), tableGeometry.getY(), 150, 30,
							"table"); //$NON-NLS-1$

					tablesCreated.add((TableObject) ((mxCell) tableCell).getValue());
					cellsCreated.add(((mxCell) tableCell));
					mxGeometry newTableGeometry = tableCell.getGeometry();
					x = newTableGeometry.getCenterX();
					y = newTableGeometry.getCenterY();

					mxICell targetC = ((mxICell) tableCell);
					mxGeometry target = targetC.getGeometry();
					double tgtX = target.getX();
					double tgtY = target.getY();

					double xFinal = srcX > tgtX ? tgtX + ((srcX - tgtX) / 2) : srcX + ((tgtX - srcX) / 2);
					double yFinal = srcY > tgtY ? tgtY + ((srcY - tgtY) / 2) : srcY + ((tgtY - srcY) / 2);
					if (xFinal == srcX && xFinal == tgtX && yFinal == srcY && yFinal == tgtY) {
						y += source.getHeight() + (source.getHeight() * 1.5);
					}

					Object column = logicalModelingEditor.modelingManager.insertColumnObject(x, y,
							mxResources.get("primaryKey"));
					ColumnObject columnObject = (ColumnObject) ((mxCell) column).getValue();
					columnObject.setPrimaryKey(true);
					columnObject.setName(attribute.getName());
					columnObject.setType(attribute.getType());
					Object[] objects = new Object[5];

					objects[2] = logicalModeling.insertVertex(logicalModeling.getDefaultParent(), null, "", xFinal, //$NON-NLS-1$
							yFinal, 20, 20, "tableRelation"); //$NON-NLS-1$
					ConnectorObject firstConnector = new ConnectorObject(Cardinality.getValue("(1,1)")); //$NON-NLS-1$
					objects[1] = logicalModeling.insertEdge(logicalModeling.getDefaultParent(), null, firstConnector,
							sourceC, objects[2], "entityRelationConnector"); //$NON-NLS-1$
					ConnectorObject secondConnector;
					if (isRequired) {
						secondConnector = new ConnectorObject(Cardinality.getValue("(1,n)")); //$NON-NLS-1$
					} else {
						secondConnector = new ConnectorObject(Cardinality.getValue("(0,n)")); //$NON-NLS-1$
					}
					objects[3] = logicalModeling.insertEdge(logicalModeling.getDefaultParent(), null, secondConnector,
							objects[2], targetC, "entityRelationConnector"); //$NON-NLS-1$
					String[] messagesX = new String[2];
					String[] alternativesX = new String[2];
					Messages.fillAttributeInteraction(attribute, messagesX, alternativesX, 1);

					selectedAlternative = askToUser(messagesX, alternativesX, selectedAlternative);
					if (selectedAlternative == 0) {
						Object columnFk = logicalModelingEditor.modelingManager.insertColumnObject(x, y,
								mxResources.get("foreignKey"));
						ColumnObject columnFkAtt = (ColumnObject) ((mxCell) columnFk).getValue();
						columnFkAtt.setForeignKey(true);
						columnFkAtt.setName("fk_" + parentName);
						columnFkAtt.setType("int");
					} else if (selectedAlternative == 1) {

						Object columnFk = logicalModelingEditor.modelingManager.insertColumnObject(x, y,
								mxResources.get("bothKeys"));
						ColumnObject columnFkAtt = (ColumnObject) ((mxCell) columnFk).getValue();
						columnFkAtt.setForeignKey(true);
						columnFkAtt.setPrimaryKey(true);
						columnFkAtt.setName("fk_" + parentName);
						columnFkAtt.setType("int");
					}
				} else if (selectedAlternative == 1) {
					char maxCard = attribute.getMaximumCardinality();

					for (int i = 1; i <= Integer.parseInt(Character.toString(maxCard)); i++) {

						Object columnAtt = logicalModelingEditor.modelingManager.insertColumnObject(x, y, "column");
						ColumnObject columnAtribute = (ColumnObject) ((mxCell) columnAtt).getValue();
						columnAtribute.setName(attribute.getName() + i);
						columnAtribute.setType(attribute.getType());
						columnAtribute.setOptional(attribute.isOptional());

					}

				}
			}

		} else {
			Iterator<AttributeObject> iterator = attributes.iterator();
			while (iterator.hasNext()) {
				AttributeObject attributeObject = iterator.next();
				String style = attributeObject.isIdentifier() ? mxResources.get("primaryKey") : "column";
				Object columnRest = logicalModelingEditor.modelingManager.insertColumnObject(x, y, style);
				ColumnObject columnrestAtt = (ColumnObject) ((mxCell) columnRest).getValue();
				columnrestAtt.setName(attributeObject.getName());
				columnrestAtt.setType(attributeObject.getType());
				columnrestAtt.setOptional(attribute.isOptional());
			}
		}

		logicalModelingEditor.modelingComponent.getGraph().refresh();
	}

	/*
	 * CONVERT RELATION N:M
	 *
	 * Converte as relaï¿½ï¿½es entre as entidades de acordo com a cardinalidade
	 */
	public EntityObject convertSelfRelatedEntity(mxCell relationObject) {
		RelationObject relation = (RelationObject) relationObject.getValue();
		EntityObject entidade = (EntityObject) relation.getRelatedObjects().get(0);
		List<Object> primaryKeys = entidade.getPrimaryKeys();
		mxCell tabela = procuraNovaTabela(entidade.getName());
		String[] messages = new String[2];
		String[] alternatives = new String[2];
		Messages.fillSelfRelatedInteraction(entidade, relation, messages, alternatives);
		int anwser = askToUser(messages, alternatives, 0);

		switch (anwser) {
		case 0:
			addForeignKeys(tabela, primaryKeys);
			break;
		case 1:
			Modeling logicalModeling = (Modeling) logicalModelingEditor.modelingComponent.getGraph();
			mxGeometry geometry = relationObject.getGeometry();

			mxCell tableCell = (mxCell) logicalModeling.insertVertex(logicalModeling.getDefaultParent(), null,
					new TableObject(relation.getName()), geometry.getX(), geometry.getY(), 150, 30, "table");

			tablesCreated.add((TableObject) ((mxCell) tableCell).getValue());
			cellsCreated.add(((mxCell) tableCell));
			addForeignKeys(tableCell, primaryKeys);
			addBothKeys(tableCell, primaryKeys);

			connectCell(tableCell, tabela, Cardinality.ZERO_N, Cardinality.ZERO_N);
			break;
		}
		return entidade;
	}

	private EntityObject convertRelationOneN(List<EntityObject> entidades, List<Cardinality> cardinalidades,
			mxCell relationParent, Cardinality cardinality) {
		// pegar posicao da tabela com cardinalidade 1,1
		int posicao = -1;
		for (int i = 0; i < cardinalidades.size(); i++) {
			if (cardinalidades.get(i) == cardinality) {
				posicao = i;
				break;
			}
		}

		// pegar chave primaria da tabela
		mxCell cellSource = procuraNovaTabela(entidades.get(posicao).getName());
		EntityObject entitySource = entidades.get(posicao);
		List<Object> primaryKeys;
		if (cellSource.getValue() instanceof TableObject) {
			primaryKeys = ((TableObject) cellSource.getValue()).getPrimaryKeys();
		} else {
			primaryKeys = entitySource.getPrimaryKeys();
		}

		mxCell cellTarget;
		EntityObject entityTarget;
		if (entitySource == entidades.get(0)) {
			cellTarget = procuraNovaTabela(entidades.get(1).getName());
			entityTarget = entidades.get(1);
		} else {
			cellTarget = procuraNovaTabela(entidades.get(0).getName());
			entityTarget = entidades.get(0);
		}
		// pegar atributos da relacao
		List<Object> relationsAttributes = getRelationsAttributes(relationParent);

		// jogar attributos para cellTarget
		addAtributtes(cellTarget, relationsAttributes);
		addForeignKeys(cellTarget, primaryKeys);
		Cardinality cSource = cardinalidades.get(posicao);
		Cardinality cTarget = posicao == 0 ? cardinalidades.get(1) : cardinalidades.get(0);

		connectCell(cellSource, cellTarget, cSource, cTarget);

		return entityTarget;
	}

	private EntityObject convertRelationOneOptionalN(List<EntityObject> entidades, List<Cardinality> cardinalidades,
			mxCell relationParent) {
		String[] messages = new String[2];
		String[] alternatives = new String[2];

		Messages.fillRelationsInteraction(relationParent, messages, alternatives);
		int answer = askToUser(messages, alternatives, 0);
		EntityObject target = null;

		switch (answer) {
		case 0:
			target = convertRelationNM(entidades, relationParent, false, cardinalidades);
			break;
		case 1:
			target = convertRelationOneN(entidades, cardinalidades, relationParent, Cardinality.ZERO_ONE);
			break;
		default:
			canceledByUser = true;
			break;
		}

		return target;
	}

	private EntityObject convertRelationOneAll(List<EntityObject> entidades, mxCell relationParent,
			List<Cardinality> cardinalidades) {
		mxCell celulaMenor, celulaMaior;
		EntityObject entityTarget = null;

		if (cardinalidades.get(0) == Cardinality.ZERO_ONE) {
			celulaMenor = procuraNovaTabela(entidades.get(0).getName());
			celulaMaior = procuraNovaTabela(entidades.get(1).getName());
			entityTarget = entidades.get(1);
		} else {
			celulaMaior = procuraNovaTabela(entidades.get(0).getName());
			celulaMenor = procuraNovaTabela(entidades.get(1).getName());
			entityTarget = entidades.get(0);
		}

		List<Object> atributosColuna = new ArrayList<>();
		TableObject tabelaMenor = (TableObject) celulaMenor.getValue();
		List<Object> childObjects = tabelaMenor.getChildObjects();

		for (Object object : childObjects) {
			if (((mxCell) object).getValue() instanceof ColumnObject) {
				// ColumnObject coluna = (ColumnObject) object;
				atributosColuna.add(object);
			}
		}
		List<Object> relationsAttributes = getRelationsAttributes(relationParent);

		addColunas(atributosColuna, celulaMaior);
		addAtributtes(celulaMaior, relationsAttributes);
		replicaConectores(celulaMenor, celulaMaior);
		removeCell(celulaMenor, celulaMaior);

		return entityTarget;
	}

	private EntityObject convertRelationNM(List<EntityObject> entidades, mxCell relationParent, boolean both,
			List<Cardinality> cardinalidades) {
		// procurar no hashMap pelo nome da entidade, se encontrar
		List<Object> primaryKeys = new ArrayList<>();
		EntityObject entityA = entidades.get(0);
		EntityObject entityB = entidades.get(1);

		// Table 1
		TableObject tabela = procuraEntidade(entidades.get(0));
		if (tabela != null) {
			primaryKeys.addAll(tabela.getPrimaryKeys());
		} else {
			primaryKeys.addAll(entityA.getPrimaryKeys());
		}

		// Table 2
		TableObject tabela2 = procuraEntidade(entidades.get(1));
		if (tabela2 != null) {
			primaryKeys.addAll(tabela2.getPrimaryKeys());
		} else {
			primaryKeys.addAll(entityB.getPrimaryKeys());
		}

		List<Object> bothKeys = new ArrayList<>();
		List<Object> foreignKeys = new ArrayList<>();

		if (!both) {
			EntityObject entidadeMaior = (cardinalidades.get(0) == Cardinality.ONE_N
					|| cardinalidades.get(0) == Cardinality.ZERO_N) ? entityA : entityB;
			bothKeys = entidadeMaior.getPrimaryKeys();
			foreignKeys = entidadeMaior.equals(entityA) ? entityB.getPrimaryKeys() : entityA.getPrimaryKeys();
		}

		// pegar atrributos relacao
		List<Object> relationsAttributes = getRelationsAttributes(relationParent);

		// criar nova tabela
		Modeling logicalModeling = (Modeling) logicalModelingEditor.modelingComponent.getGraph();
		RelationObject relation = (RelationObject) relationParent.getValue();
		mxGeometry geometry = relationParent.getGeometry();

		mxCell tableCell = (mxCell) logicalModeling.insertVertex(logicalModeling.getDefaultParent(), null,
				new TableObject(relation.getName()), geometry.getX(), geometry.getY(), 150, 30, "table");

		tablesCreated.add((TableObject) ((mxCell) tableCell).getValue());
		cellsCreated.add(((mxCell) tableCell));

		// adicionar attributos a tabela criada
		if (both) {
			addBothKeys(tableCell, primaryKeys);
		} else {
			addForeignKeys(tableCell, foreignKeys);
			addBothKeys(tableCell, bothKeys);
		}
		addAtributtes(tableCell, relationsAttributes);
		connectCell(tableCell, procuraNovaTabela(entityA.getName()), cardinalidades.get(0), Cardinality.ONE_ONE);
		connectCell(tableCell, procuraNovaTabela(entityB.getName()), cardinalidades.get(1), Cardinality.ONE_ONE);

		// criar entidade target
		return criarEntidade(relationParent, entityA, entityB);
	}

	private EntityObject convertRelationOneOptional(List<EntityObject> entidades, mxCell relationParent,
			List<Cardinality> cardinalidades) {
		String[] messages = new String[2];
		String[] alternatives = new String[2];

		Messages.fillRelationsInteractionOneOptional(relationParent, messages, alternatives);
		int answer = askToUser(messages, alternatives, 0);

		switch (answer) {
		case 0:
			return convertRelationOneAll(entidades, relationParent, cardinalidades);
		case 1:
			return convertRelationOneN(entidades, cardinalidades, relationParent, Cardinality.ONE_ONE);
		default:
			canceledByUser = true;
			break;
		}
		return null;
	}

	private EntityObject convertRelationOptionalAll(List<EntityObject> entidades, mxCell relationParent,
			List<Cardinality> cardinalidades) {
		String[] messages = new String[2];
		String[] alternatives = new String[2];

		Messages.fillRelationsInteractionOptionalAll(relationParent, messages, alternatives);
		int answer = askToUser(messages, alternatives, 0);

		switch (answer) {
		case 0:
			return convertRelationNM(entidades, relationParent, false, cardinalidades);
		case 1:
			return convertRelationOptionalAll(entidades, relationParent);
		default:
			canceledByUser = true;
			break;
		}
		return null;
	}

	private EntityObject convertRelationOptionalAll(List<EntityObject> entidades, mxCell relationParent) {
		EntityObject entityA = entidades.get(0);
		EntityObject entityB = entidades.get(1);

		mxCell cellA = procuraNovaTabela(entityA.getName());
		mxCell cellB = procuraNovaTabela(entityB.getName());

		// adicionar chaves pk de cada tabela como fk na outra
		addForeignKeys(cellA, entityB.getPrimaryKeys());
		addForeignKeys(cellB, entityA.getPrimaryKeys());

		/*
		 * TODO: Escolher pra qual tabela colocar atributos da relacao
		 */
		List<Object> relationsAttributes = getRelationsAttributes(relationParent);
		mxCell cellTarget = null;
		if (!relationsAttributes.isEmpty()) {
			String[] messages = new String[2];
			String[] alternatives = new String[2];

			Messages.fillRelationsInteractionOptionalAttributtes(relationParent, entityA, entityB, messages,
					alternatives);
			cellTarget = (askToUser(messages, alternatives, 0) == 0) ? cellA : cellB;
			addAtributtes(cellTarget, relationsAttributes);
		}
		connectCell(cellA, cellB, Cardinality.ZERO_ONE, Cardinality.ZERO_ONE);

		return cellTarget == cellA ? entityA : entityB;
	}

	/*
	 * CONVERT INHERITANCES
	 *
	 * convertAllTablesInheritances - Converte cada entidade para uma tabela
	 * convertOneTableInheritances - Converte para uma unica tabela a entidade
	 * pai e as heranÃ§as convertSpecializedTableInheritances - Converte as
	 * entidades para as tabelas especializadas, que possuem os atributos da
	 * entidade pai
	 */
	public void convertInheritances(mxCell inheritanceCell) {
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		if (inheritanceObject.getChildObjects().size() < 1) {
			return;
		}
		String[] messages = new String[2];
		String[] alternatives = new String[!inheritanceObject.isPartial() ? 4 : 3];

		Messages.fillInheritanceInteraction(inheritanceObject, messages, alternatives);

		int answer = askToUser(messages, alternatives, inheritanceObject.isPartial() ? 0 : 1);

		switch (answer) {
		case 0:
			convertAllTablesInheritances(inheritanceObject);
			break;
		case 1:
			convertOneTableInheritances(inheritanceObject);
			break;
		case 2:
			convertSpecializedTableInheritances(inheritanceObject);
			break;
		default:
			canceledByUser = true;
			break;
		}
	}

	private void convertAllTablesInheritances(InheritanceObject inheritanceObject) {
		mxCell cellParent = (mxCell) inheritanceObject.getParentObject();
		EntityObject pai = (EntityObject) cellParent.getValue();
		List<Object> primaryKeys = pai.getPrimaryKeys();

		for (Object child : inheritanceObject.getChildObjects()) {
			mxCell cell = (mxCell) child;
			if (cell.getValue() instanceof EntityObject) {
				addBothKeys(cell, primaryKeys);
			}

		}
		for (Object filho : inheritanceObject.getChildObjects()) {
			mxCell celula = (mxCell) filho;
			if (celula.getValue() instanceof EntityObject) {
				connectCell(cellParent, celula, Cardinality.ONE_ONE, Cardinality.ZERO_N);
			}

		}
	}

	private void convertOneTableInheritances(InheritanceObject inheritanceObject) {
		// pegar todos atributos
		List<Object> attributes = new ArrayList<Object>();
		int count = inheritanceObject.getChildObjects().size();
		mxCell novaTabela = (mxCell) inheritanceObject.getParentObject();

		for (int i = 0; i < count; i++) {
			mxCell cell = (mxCell) inheritanceObject.getChildObjects().get(i);
			cell = procuraNovaTabela(cell);
			if (cell.getValue() instanceof TableObject) {
				TableObject tableObject = (TableObject) cell.getValue();
				attributes.addAll(tableObject.getChildObjects());
				replicaConectores(cell, novaTabela);
				removeCell(cell, procuraNovaTabela(novaTabela));
			}
		}
		ColumnObject columnTipo = new ColumnObject("tipo", procuraNovaTabela(novaTabela));
		columnTipo.setType("Int(1)");
		attributes.add(new mxCell(columnTipo));

		// add columns
		if (novaTabela.getValue() instanceof EntityObject) {
			addColunas(attributes, novaTabela);
		}
	}

	private void convertSpecializedTableInheritances(InheritanceObject inheritanceObject) {
		// Get parent attributes
		mxCell cellParent = (mxCell) inheritanceObject.getParentObject();
		EntityObject parent = (EntityObject) cellParent.getValue();

		List<Object> attributes = new ArrayList<Object>();
		attributes.addAll(parent.getChildObjects());

		// Set Attributes parent for childs
		int count = inheritanceObject.getChildObjects().size();
		for (int i = 0; i < count; i++) {
			mxCell cell = (mxCell) inheritanceObject.getChildObjects().get(i);
			if (cell.getValue() instanceof EntityObject) {
				addAtributtes(cell, attributes);
				replicaConectores(cellParent, cell);
			}
		}

		if (count > 0) {
			mxCell cell = (mxCell) inheritanceObject.getChildObjects().get(0);
			removeCell(cellParent, procuraNovaTabela(cell));
		}
	}

	/*
	 * CONVERT ENTIDADES ASSOCIATIVAS
	 */
	private void convertAssociativeRelations(mxCell relationCell) {

		AssociativeEntityObject associativeRelationObject = (AssociativeEntityObject) relationCell.getValue();

		RelationObject relationObject = (RelationObject) associativeRelationObject.getRelationObject();

		List<ModelingObject> relatedObjects = relationObject.getRelatedObjects();
		List<Cardinality> cardinalidades = new ArrayList<>();
		List<EntityObject> entidades = new ArrayList<>();

		// e6 relationObject.addRelatedObject((ModelingObject)
		// relationCell.getEdgeAt(0).getTerminal(false).getEdgeAt(0).getTerminal(true).getEdgeAt(0).getTerminal(true).getValue());
		relationCell.setValue(relationObject);

		// cardinalidades

		for (ModelingObject modelingObject : relatedObjects) {
			if (modelingObject instanceof EntityObject) {
				EntityObject entity = (EntityObject) modelingObject;
				ConnectorObject connector = getAssociativeConnectorObject(relationCell, entity);
				if (connector != null) {
					Cardinality cardinality = connector.getCardinality();
					entidades.add(entity);
					cardinalidades.add(cardinality);
				}
			}
		}

		// Fazer conexao, criar tabela para cada entidade
		EntityObject entityTarget = convertByRelationType(entidades, relationCell, cardinalidades);

	}

	/*
	 * CONVERT TERNARIO
	 */
	private void convertTernario(mxCell relationCell) {
		RelationObject relation = (RelationObject) relationCell.getValue();
		List<ModelingObject> relatedObjects = relation.getRelatedObjects();
		List<Cardinality> cardinalidades = new ArrayList<>();
		List<EntityObject> entidades = new ArrayList<>();

		for (ModelingObject objeto : relatedObjects) {
			if (objeto instanceof EntityObject) {
				EntityObject entidade = (EntityObject) objeto;
				ConnectorObject connector = relation.getConnectorObject(relationCell, entidade);
				Cardinality cardinality = connector.getCardinality();
				cardinalidades.add(cardinality);
				entidades.add(entidade);
			}
		}

		mxCell tableCell = criaTabela(relationCell);
		int tipo = getTernarioType(cardinalidades);

		// todo
		Cardinality source = null;
		switch (tipo) {
		case 0:// N,N,N
			convertTernNAll(entidades, relationCell, tableCell);
			source = Cardinality.ZERO_N;
			break;
		case 1:// 1,N,N
			convertTernOneDN(entidades, relationCell, tableCell, cardinalidades);
			source = Cardinality.ONE_N;
			break;
		case 2:// 1,1,N
			convertTernDOneN(entidades, relationCell, tableCell, cardinalidades);
			source = Cardinality.ONE_N;
			break;
		case 3:// 1,1,1
			convertTernOneAll(entidades, relationCell, tableCell);
			source = Cardinality.ZERO_ONE;
			break;
		}

		connectCell(tableCell, procuraNovaTabela(entidades.get(0).getName()), source, cardinalidades.get(0));
		connectCell(tableCell, procuraNovaTabela(entidades.get(1).getName()), source, cardinalidades.get(1));
		connectCell(tableCell, procuraNovaTabela(entidades.get(2).getName()), source, cardinalidades.get(2));
	}

	private void convertTernNAll(List<EntityObject> entidades, mxCell relationObject, mxCell tableCell) {
		List<Object> primaryKeys = new ArrayList<>();
		for (EntityObject entityObject : entidades) {
			primaryKeys.addAll(entityObject.getPrimaryKeys());
		}
		addBothKeys(tableCell, primaryKeys);
		List<Object> relationsAttributes = getRelationsAttributes(relationObject);
		addAtributtes(tableCell, relationsAttributes);
	}

	private void convertTernOneDN(List<EntityObject> entidades, mxCell relationCell, mxCell tableCell,
			List<Cardinality> cardinalidades) {
		List<Object> bothKeys = new ArrayList<>();
		List<Object> foreignKeys = new ArrayList<>();
		List<Object> relationsAttributes = getRelationsAttributes(relationCell);

		String fim = "1)";
		for (int i = 0; i < cardinalidades.size(); i++) {
			if (Cardinality.getText(cardinalidades.get(i)).endsWith(fim)) {
				foreignKeys.addAll(entidades.get(i).getPrimaryKeys());
			} else {
				bothKeys.addAll(entidades.get(i).getPrimaryKeys());
			}
		}

		addAtributtes(tableCell, relationsAttributes);
		addForeignKeys(tableCell, foreignKeys);
		addBothKeys(tableCell, bothKeys);
	}

	private void convertTernDOneN(List<EntityObject> entidades, mxCell relationCell, mxCell tableCell,
			List<Cardinality> cardinalidades) {
		RelationObject relation = (RelationObject) relationCell.getValue();
		List<Object> bothKeys = new ArrayList<>();
		List<Object> relationsAttributes = getRelationsAttributes(relationCell);
		List<Object> foreignKeys = new ArrayList<>();
		List<EntityObject> entidadesPossiveis = new ArrayList<>();

		String fim = "n)";
		for (int i = 0; i < cardinalidades.size(); i++) {
			if (Cardinality.getText(cardinalidades.get(i)).endsWith(fim)) {
				bothKeys.addAll(entidades.get(i).getPrimaryKeys());
			} else {
				entidadesPossiveis.add(entidades.get(i));
			}
		}
		String[] messages = new String[2];
		String[] alternatives = new String[2];
		Messages.fillTernDOneN(entidadesPossiveis, relation, messages, alternatives);
		int resposta = askToUser(messages, alternatives, 0);

		if (resposta == 0) {
			bothKeys.addAll(entidadesPossiveis.get(0).getPrimaryKeys());
			foreignKeys.addAll(entidadesPossiveis.get(1).getPrimaryKeys());
		} else {
			bothKeys.addAll(entidadesPossiveis.get(1).getPrimaryKeys());
			foreignKeys.addAll(entidadesPossiveis.get(0).getPrimaryKeys());
		}
		addAtributtes(tableCell, relationsAttributes);
		addForeignKeys(tableCell, foreignKeys);
		addBothKeys(tableCell, bothKeys);
	}

	private void convertTernOneAll(List<EntityObject> entidades, mxCell relationCell, mxCell tableCell) {
		RelationObject relation = (RelationObject) relationCell.getValue();
		List<Object> bothKeys = new ArrayList<>();
		List<Object> relationsAttributes = getRelationsAttributes(relationCell);
		List<Object> foreignKeys = new ArrayList<>();

		String[] messages = new String[2];
		String[] alternatives = new String[3];
		Messages.fillTernOneAll(entidades, relation, messages, alternatives);
		int resposta = askToUser(messages, alternatives, 0);
		if (resposta == 0) {
			bothKeys.addAll(entidades.get(0).getPrimaryKeys());
			bothKeys.addAll(entidades.get(1).getPrimaryKeys());
			foreignKeys.addAll(entidades.get(2).getPrimaryKeys());
		} else if (resposta == 1) {
			bothKeys.addAll(entidades.get(0).getPrimaryKeys());
			bothKeys.addAll(entidades.get(2).getPrimaryKeys());
			foreignKeys.addAll(entidades.get(1).getPrimaryKeys());
		} else {
			bothKeys.addAll(entidades.get(1).getPrimaryKeys());
			bothKeys.addAll(entidades.get(2).getPrimaryKeys());
			foreignKeys.addAll(entidades.get(0).getPrimaryKeys());
		}
		addAtributtes(tableCell, relationsAttributes);
		addForeignKeys(tableCell, foreignKeys);
		addBothKeys(tableCell, bothKeys);
	}

	/*
	 * GETTERS AND SETTERS
	 */
	public ConnectorObject getAssociativeConnectorObject(mxCell relationCell, EntityObject entity) {
		int childCount = relationCell.getChildCount();
		for (int i = 0; i < childCount; i++) {
			mxCell child = (mxCell) relationCell.getChildAt(i);
			int childCountEdges = child.getEdgeCount();
			for (int j = 0; j < childCountEdges; j++) {
				mxCell childAt = (mxCell) child.getEdgeAt(j);
				if (entity == childAt.getSource().getValue()) {
					return (ConnectorObject) childAt.getValue();
				}
			}
		}
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

	public void getInheritanceChildren(mxCell inheritanceCell, List<mxCell> orderedInheritanceObjects) {
		InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
		Iterator<Object> iterator = inheritanceObject.getChildObjects().iterator();

		while (iterator.hasNext()) {
			mxCell entityCell = (mxCell) iterator.next();
			EntityObject entityObject = (EntityObject) entityCell.getValue();
			Iterator<Object> iterator2 = entityObject.getChildObjects().iterator();

			while (iterator2.hasNext()) {
				mxCell cell = (mxCell) iterator2.next();
				if (cell.getValue() instanceof InheritanceObject) {
					if (orderedInheritanceObjects.indexOf(cell) > -1) {
						orderedInheritanceObjects.remove(cell);
					}

					orderedInheritanceObjects.add(cell);
					getInheritanceChildren(cell, orderedInheritanceObjects);
				}
			}
		}
	}

	private List<Object> getRelationsAttributes(mxCell relationParent) {
		List<Object> atributosRelacao = new ArrayList<>();
		RelationObject relacao = (RelationObject) relationParent.getValue();
		for (Object object : relacao.getChildObjects()) {
			if (((mxCell) object).getValue() instanceof AttributeObject) {
				atributosRelacao.add(object);
			}
		}
		return atributosRelacao;
	}

	private int getRelationType(List<Cardinality> cardinalidades) {

		if (cardinalidades.size() == 2) {
			Cardinality cardinalityA = cardinalidades.get(0);
			Cardinality cardinalityB = cardinalidades.get(1);

			if (cardinalityA == Cardinality.ONE_ONE && cardinalityB == Cardinality.ONE_ONE) {
				return 0;
			} else {
				if ((cardinalityA == Cardinality.ONE_ONE && cardinalityB == Cardinality.ZERO_ONE)
						|| (cardinalityA == Cardinality.ZERO_ONE && cardinalityB == Cardinality.ONE_ONE)) {
					return 1;
				} else {
					if (cardinalityA == Cardinality.ZERO_ONE && cardinalityB == Cardinality.ZERO_ONE) {
						return 2;
					} else {
						if (((cardinalityA == Cardinality.ONE_N && cardinalityB == Cardinality.ONE_ONE)
								|| (cardinalityA == Cardinality.ONE_ONE && cardinalityB == Cardinality.ONE_N))
								|| ((cardinalityA == Cardinality.ZERO_N && cardinalityB == Cardinality.ONE_ONE)
										|| (cardinalityA == Cardinality.ONE_ONE
												&& cardinalityB == Cardinality.ZERO_N))) {
							return 3;
						} else {
							if (((cardinalityA == Cardinality.ONE_N && cardinalityB == Cardinality.ZERO_ONE)
									|| (cardinalityA == Cardinality.ZERO_ONE && cardinalityB == Cardinality.ONE_N))
									|| ((cardinalityA == Cardinality.ZERO_N && cardinalityB == Cardinality.ZERO_ONE)
											|| (cardinalityA == Cardinality.ZERO_ONE
													&& cardinalityB == Cardinality.ZERO_N))) {
								return 4;
							} else {
								if ((cardinalityA == Cardinality.ONE_N || cardinalityA == Cardinality.ZERO_N)
										&& (cardinalityB == Cardinality.ONE_N || cardinalityB == Cardinality.ZERO_N)) {
									return 5;
								}
							}
						}
					}
				}
			}
		}

		return -1;
	}

	private int getTernarioType(List<Cardinality> cardinalidades) {
		String fim = "n)";
		List<Integer> maxCard = new ArrayList<>();
		for (int i = 0; i < cardinalidades.size(); i++) {
			if (Cardinality.getText(cardinalidades.get(i)).endsWith(fim)) {
				maxCard.add(0);
			} else {
				maxCard.add(1);
			}
		}
		return maxCard.get(0) + maxCard.get(1) + maxCard.get(2);
	}

	/*
	 * FUNCOES AUXILIARES
	 */
	public void addAtributtes(mxCell cell, List<Object> attributes) {
		mxGeometry geometry = cell.getGeometry();

		double x = geometry.getX();
		double y = geometry.getY();

		for (Object attribute : attributes) {
			mxCell att = (mxCell) attribute;
			if (att.getValue() instanceof AttributeObject) {
				AttributeObject attributeObject = (AttributeObject) att.getValue();
				String style = attributeObject.isIdentifier() ? mxResources.get("primaryKey") : "column";
				Object columnRest = logicalModelingEditor.modelingManager.insertColumnObject(geometry.getCenterX(),
						geometry.getCenterY(), style);
				ColumnObject columnrestAtt = (ColumnObject) ((mxCell) columnRest).getValue();
				if (attributeObject.isIdentifier()) {
					columnrestAtt.setPrimaryKey(true);
				}
				columnrestAtt.setName(attributeObject.getName());
				columnrestAtt.setType(attributeObject.getType());

			}

		}
		logicalModelingEditor.modelingComponent.getGraph().refresh();
	}

	public void addBothKeys(mxCell cell, List<Object> attributes) {
		mxGeometry geometry = cell.getGeometry();

		double x = geometry.getX();
		double y = geometry.getY();

		for (Object attribute : attributes) {
			mxCell att = (mxCell) attribute;
			if (att.getValue() instanceof AttributeObject) {
				AttributeObject attributeObject = (AttributeObject) att.getValue();
				String style = "bothKeys";
				Object columnRest = logicalModelingEditor.modelingManager.insertColumnObject(geometry.getCenterX(),
						geometry.getCenterY(), mxResources.get(style));
				ColumnObject columnrestAtt = (ColumnObject) ((mxCell) columnRest).getValue();
				columnrestAtt.setPrimaryKey(true);
				columnrestAtt.setForeignKey(true);
				columnrestAtt.setName(attributeObject.getName());
				columnrestAtt.setType(attributeObject.getType());

			}
			if (att.getValue() instanceof ColumnObject) {
				ColumnObject coluna = (ColumnObject) att.getValue();
				String style = "bothKeys";
				Object columnRest = logicalModelingEditor.modelingManager.insertColumnObject(geometry.getCenterX(),
						geometry.getCenterY(), mxResources.get(style));
				ColumnObject columnrestAtt = (ColumnObject) ((mxCell) columnRest).getValue();
				columnrestAtt.setName(coluna.getName());
			}
		}
		logicalModelingEditor.modelingComponent.getGraph().refresh();
	}

	public void addForeignKeys(mxCell cell, List<Object> attributes) {
		mxGeometry geometry = cell.getGeometry();

		double x = geometry.getX();
		double y = geometry.getY();

		for (Object attribute : attributes) {
			mxCell att = (mxCell) attribute;
			if (att.getValue() instanceof AttributeObject) {
				AttributeObject attributeObject = (AttributeObject) att.getValue();
				String style = "foreignKey";
				Object columnRest = logicalModelingEditor.modelingManager.insertColumnObject(geometry.getCenterX(),
						geometry.getCenterY(), mxResources.get(style));
				ColumnObject columnrestAtt = (ColumnObject) ((mxCell) columnRest).getValue();
				columnrestAtt.setPrimaryKey(false);
				columnrestAtt.setForeignKey(true);
				columnrestAtt.setName(attributeObject.getName());
				columnrestAtt.setType(attributeObject.getType());
			}
			if (att.getValue() instanceof ColumnObject) {
				ColumnObject coluna = (ColumnObject) att.getValue();
				TableObject tabela = (TableObject) cell.getValue();
				String style = "foreignKey";
				Object columnRest = logicalModelingEditor.modelingManager.insertColumnObject(geometry.getCenterX(),
						geometry.getCenterY(), mxResources.get(style));
				ColumnObject columnrestAtt = (ColumnObject) ((mxCell) columnRest).getValue();
				columnrestAtt.setPrimaryKey(false);
				columnrestAtt.setForeignKey(true);
				columnrestAtt.setName(coluna.getName());
				columnrestAtt.setType(coluna.getType());
			}
		}
		logicalModelingEditor.modelingComponent.getGraph().refresh();
	}

	public int askToUser(String[] messages, String[] alternatives, int suggestionIndex) {
		JFrame frame = (JFrame) SwingUtilities.windowForComponent(mainWindow);

		ConversionIteratorWindow conversionIterator = new ConversionIteratorWindow(frame, messages, alternatives,
				suggestionIndex);
		conversionIterator.setModal(true);

		// Centers inside the application frame
		int x = frame.getX() + (frame.getWidth() - conversionIterator.getWidth()) / 2;
		int y = frame.getY() + (frame.getHeight() - conversionIterator.getHeight()) / 2;
		conversionIterator.setLocation(x, y);

		// Shows the modal dialog and waits

		conversionIterator.setSize(conversionIterator.getWidth() + 250, conversionIterator.getHeight());
		conversionIterator.setTitle("Mapeamento Conceitual -> Lï¿½gico");
		conversionIterator.setVisible(true);
		return conversionIterator.getResult();
	}

	private void addColunas(List<Object> atributosColuna, mxCell celulaA) {

		mxGeometry geometry = celulaA.getGeometry();

		double x = geometry.getX();
		double y = geometry.getY();

		for (Object attribute : atributosColuna) {
			mxCell att = (mxCell) attribute;
			if (att.getValue() instanceof ColumnObject) {
				ColumnObject attributeObject = (ColumnObject) att.getValue();
				String style = attributeObject.getStyle();
				// String style = attributeObject.isPrimaryKey() ?
				// mxResources.get("primaryKey") : "column";
				Object columnRest = logicalModelingEditor.modelingManager.insertColumnObject(x, y, style);
				ColumnObject columnrestAtt = (ColumnObject) ((mxCell) columnRest).getValue();
				columnrestAtt.setName(attributeObject.getName());
				columnrestAtt.setType(attributeObject.getType());
				columnrestAtt.setPrimaryKey(attributeObject.isPrimaryKey());
				columnrestAtt.setForeignKey(attributeObject.isForeignKey());

			}

		}

	}

	private mxCell criaTabela(mxCell relationObject) {
		RelationObject relation = (RelationObject) relationObject.getValue();
		Modeling logicalModeling = (Modeling) logicalModelingEditor.modelingComponent.getGraph();
		mxGeometry geometry = relationObject.getGeometry();

		mxCell tableCell = (mxCell) logicalModeling.insertVertex(logicalModeling.getDefaultParent(), null,
				new TableObject(relation.getName()), geometry.getX(), geometry.getY(), 150, 30, "table");

		tablesCreated.add((TableObject) ((mxCell) tableCell).getValue());
		cellsCreated.add(((mxCell) tableCell));
		return tableCell;
	}

	private void connectCell(mxCell source, mxCell target, Cardinality cSource, Cardinality cTarget) {

		Modeling logicalModeling = (Modeling) logicalModelingEditor.modelingComponent.getGraph();

		if (!(source.getValue() instanceof TableObject)) {
			source = procuraNovaTabela(source);
		}

		if (!(target.getValue() instanceof TableObject)) {
			target = procuraNovaTabela(target);
		}

		mxGeometry sourceGeometry = source.getGeometry();
		mxGeometry targetGeometry = target.getGeometry();

		mxICell sourceC = (mxICell) source;
		mxICell targetC = (mxICell) target;

		int srcX = (int) sourceGeometry.getCenterX();
		int srcY = (int) sourceGeometry.getCenterY();
		int tgtX = (int) targetGeometry.getCenterX();
		int tgtY = (int) targetGeometry.getCenterY();
		double xFinal = srcX > tgtX ? tgtX + ((srcX - tgtX) / 2) : srcX + ((tgtX - srcX) / 2);
		double yFinal = srcY > tgtY ? tgtY + ((srcY - tgtY) / 2) : srcY + ((tgtY - srcY) / 2);

		// Diminuir pela metade do tamnho do conector do logico
		xFinal += -10;
		yFinal += -10;

		if (xFinal == srcX && xFinal == tgtX && yFinal == srcY && yFinal == tgtY) {
			srcY += sourceGeometry.getHeight() + (sourceGeometry.getHeight() * 1.5);
		}

		Object[] objects = new Object[5];

		ConnectorObject firstConnector = new ConnectorObject(cSource);
		ConnectorObject secondConnector = new ConnectorObject(cTarget);

		objects[2] = logicalModeling.insertVertex(logicalModeling.getDefaultParent(), null, "", xFinal, yFinal, 20, 20,
				"tableRelation");
		objects[1] = logicalModeling.insertEdge(logicalModeling.getDefaultParent(), null, firstConnector, objects[2],
				sourceC, "entityRelationConnector");
		objects[3] = logicalModeling.insertEdge(logicalModeling.getDefaultParent(), null, secondConnector, objects[2],
				targetC, "entityRelationConnector");
	}

	private mxCell procuraNovaTabela(mxCell cell) {
		if (cell.getValue() instanceof EntityObject) {
			EntityObject entidade = (EntityObject) cell.getValue();
			return procuraNovaTabela(entidade.getName());
		}
		return null;
	}

	private mxCell procuraNovaTabela(String nome) {
		mxCell cellNova = hashDeletados.get(nome);
		if (cellNova != null) {
			return cellNova;
		}

		for (mxCell celula : cellsCreated) {
			if (celula.getValue() instanceof TableObject) {
				TableObject tabela = (TableObject) celula.getValue();
				if (tabela.getName().equals(nome)) {
					return celula;
				}
			}
		}
		return null;
	}

	private TableObject procuraEntidade(EntityObject entidade) {
		// tableObject
		mxCell tableCell = hashDeletados.get(entidade.getName());

		if (tableCell != null) {
			TableObject tabela = (TableObject) tableCell.getValue();
			return tabela;
		}
		return null;
	}

	private void removeCell(mxCell cell, mxCell newCell) {
		if (!(cell.getValue() instanceof TableObject)) {
			cell = procuraNovaTabela(cell);
		}
		TableObject tabela = (TableObject) cell.getValue();
		hashDeletados.put(tabela.getName(), newCell);
		logicalModelingEditor.modelingComponent.getGraph().getModel().remove(cell);
		logicalModelingEditor.modelingComponent.getGraph().refresh();
	}

	private void replicaConectores(mxCell deletado, mxCell receptor) {
		Modeling logicalModeling = (Modeling) logicalModelingEditor.modelingComponent.getGraph();
		if (!(deletado.getValue() instanceof TableObject)) {
			deletado = procuraNovaTabela(deletado);
		}
		if (!(receptor.getValue() instanceof TableObject)) {
			receptor = procuraNovaTabela(receptor);
		}

		int countEdges = deletado.getEdgeCount();
		for (int i = 0; i < countEdges; i++) {
			mxICell source = ((mxCell) deletado.getEdgeAt(i)).getSource();
			if (!(source.getValue() instanceof RelationObject)) {
				ConnectorObject firstConnector = new ConnectorObject(Cardinality.getValue("(1,1)"));
				logicalModeling.insertEdge(logicalModeling.getDefaultParent(), null, firstConnector, source,
						(mxICell) receptor, "entityRelationConnector");
				deletado.removeEdge(deletado.getEdgeAt(i), true);
			}
		}
	}

	public void weakEntityConversion(mxCell entityCell, mxGeometry geometry) {
		double x = geometry.getX();
		double y = geometry.getY();

		int count = entityCell.getEdgeCount();
		for (int i = 0; i < count; i++) {
			mxCell edge = (mxCell) entityCell.getEdgeAt(i);
			if (edge.getValue() instanceof ConnectorObject) {
				ConnectorObject connector = (ConnectorObject) edge.getValue();
				if (connector.isWeakEntity()) {
					RelationObject relationObject = null;
					if (edge.getSource() == entityCell) {
						relationObject = (RelationObject) ((mxCell) edge.getTarget()).getValue();
					} else {
						relationObject = (RelationObject) ((mxCell) edge.getSource()).getValue();
					}

					if (relationObject.getRelatedObjects().size() == 2) {
						ModelingObject otherEntity = relationObject.getRelatedObjects()
								.get(0) == (ModelingObject) entityCell.getValue()
										? relationObject.getRelatedObjects().get(1)
										: relationObject.getRelatedObjects().get(0);

						Iterator<Object> iterator = otherEntity.getChildObjects().iterator();
						while (iterator.hasNext()) {
							Object object = iterator.next();
							if (((mxCell) object).getValue() instanceof AttributeObject) {
								AttributeObject attributeObject = (AttributeObject) ((mxCell) object).getValue();

								if (attributeObject.isIdentifier()) {
									Object column = logicalModelingEditor.modelingManager.insertColumnObject(x + 75,
											y + 15, "primaryKey");
									ColumnObject columnObject = (ColumnObject) ((mxCell) column).getValue();
									columnObject.setName(attributeObject.getName());
									columnObject.setType(attributeObject.getType());

									columnObject.setForeignKey(true);

								}
							}
						}
					}
				}
			}
		}
	}

	/*
	 * ORDERS
	 */
	/**
	 * Retorna as cabeÃ§as (Entidades) de cada heranÃ§a
	 *
	 * @param inheritanceObjects
	 * @return List<mxcell>
	 */
	public List<mxCell> orderParentInheritanceList(List<mxCell> inheritanceObjects) {
		List<mxCell> orderedInheritanceObjects = new ArrayList<mxCell>();
		Iterator<mxCell> iterator = inheritanceObjects.iterator();
		while (iterator.hasNext()) {
			mxCell inheritanceCell = iterator.next();
			InheritanceObject inheritanceObject = (InheritanceObject) inheritanceCell.getValue();
			EntityObject entityObject = (EntityObject) ((mxCell) inheritanceObject.getParentObject()).getValue();
			mxCell cell = (mxCell) entityObject.getParentObject();

			// se entidade nao possui pai adiciona a lista
			// orderedInheritanceObjects
			if (cell == null || !(cell.getValue() instanceof InheritanceObject)) {
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
					if (icell.getValue() instanceof InheritanceObject) {
						orderInheritanceList((mxCell) i);
					}
				}
			}
		}
		convertInheritances(parent);
	}

	public void orderChildrenInheritanceList(List<mxCell> inheritanceObjects, List<mxCell> orderedInheritanceObjects) {
		Iterator<mxCell> iterator = orderedInheritanceObjects.iterator();
		while (iterator.hasNext()) {
			mxCell inheritanceCell = iterator.next();
			getInheritanceChildren(inheritanceCell, orderedInheritanceObjects);
		}
	}

	private EntityObject criarEntidade(mxCell relationParent, EntityObject entityA, EntityObject entityB) {
		RelationObject relation = (RelationObject) relationParent.getValue();
		EntityObject entityObject = new EntityObject(relation.getName());
		entityObject.addAllChildObject(entityA.getPrimaryKeys());
		entityObject.addAllChildObject(entityB.getPrimaryKeys());

		return entityObject;
	}

	private EntityObject convertByRelationType(List<EntityObject> entidades, mxCell relationCell,
			List<Cardinality> cardinalidades) {
		int tipoRelacao = getRelationType(cardinalidades);

		switch (tipoRelacao) {
		case 0:
			return convertRelationOneAll(entidades, relationCell, cardinalidades);
		case 1:
			return convertRelationOneOptional(entidades, relationCell, cardinalidades);
		case 2:
			return convertRelationOptionalAll(entidades, relationCell, cardinalidades);
		case 3:
			return convertRelationOneN(entidades, cardinalidades, relationCell, Cardinality.ONE_ONE);
		case 4:
			return convertRelationOneOptionalN(entidades, cardinalidades, relationCell);
		case 5:
			return convertRelationNM(entidades, relationCell, true, cardinalidades);
		}
		return null;
	}
}