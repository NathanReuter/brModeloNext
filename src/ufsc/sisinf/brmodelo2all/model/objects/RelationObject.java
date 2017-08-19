package ufsc.sisinf.brmodelo2all.model.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mxgraph.model.mxCell;

public class RelationObject extends ModelingObject {
	private static final long serialVersionUID = -4379194659559624895L;
	private List<ModelingObject> relatedObjects = new ArrayList();
	private boolean markedRelation = false;
	private final int NUMBER_OF_ATTRIBUTES = 0;

	public RelationObject(String name) {
		super(name);
	}

	public RelationObject(String name, Object parentObject) {
		super(name);
		setParentObject(parentObject);
	}

	public void addRelatedObject(ModelingObject object) {
		this.relatedObjects.add(object);
	}

	public void removeRelatedObject(ModelingObject object) {
		this.relatedObjects.remove(object);
	}

	public Iterator<ModelingObject> getRelatedObjectsIterator() {
		return this.relatedObjects.iterator();
	}

	public List<ModelingObject> getRelatedObjects() {
		return this.relatedObjects;
	}

	public void setMarkedRelation(boolean markedRelation) {
		this.markedRelation = markedRelation;
	}

	public boolean isMarkedRelation() {
		return this.markedRelation;
	}

	public int attributesCount() {
		return super.attributesCount() + 0;
	}

	public void getAttributes(int[] types, String[] names, String[] values, boolean[] enabled) {
		super.getAttributes(types, names, values, enabled);
	}

	public int windowHeight() {
		return super.windowHeight();
	}

	public void removeReferencesFromParents() {
		Object parent = getParentObject();
		if (parent != null) {
			if ((((mxCell) parent).getValue() instanceof EntityObject)) {
				EntityObject entityObject = (EntityObject) ((mxCell) parent).getValue();
				entityObject.removeChildObject(this);
				entityObject.setSelfRelated(false);
			} else if ((((mxCell) parent).getValue() instanceof AssociativeEntityObject)) {
				AssociativeEntityObject entityObject = (AssociativeEntityObject) ((mxCell) parent).getValue();
				entityObject.removeChildObject(this);
				entityObject.setSelfRelated(false);
			}
		}
	}

	public String getStyle() {
		return "relation";
	}

	public ConnectorObject getConnectorObject(mxCell relationCell, EntityObject entityObject) {
		ConnectorObject returnedObject = null;

		int count = relationCell.getEdgeCount();
		for (int i = 0; i < count; i++) {
			mxCell edge = (mxCell) relationCell.getEdgeAt(i);
			if (((edge.getValue() instanceof ConnectorObject)) && (((edge.getSource() == relationCell)
					&& ((((mxCell) edge.getTarget()).getValue() instanceof EntityObject))
					&& ((EntityObject) ((mxCell) edge.getTarget()).getValue() == entityObject))
					|| ((edge.getTarget() == relationCell)
							&& ((((mxCell) edge.getSource()).getValue() instanceof EntityObject))
							&& ((EntityObject) ((mxCell) edge.getSource()).getValue() == entityObject)))) {
				returnedObject = (ConnectorObject) edge.getValue();
			}
		}
		return returnedObject;
	}
}
