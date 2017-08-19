package ufsc.sisinf.brmodelo2all.model.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

import ufsc.sisinf.brmodelo2all.util.AppConstants;

public class ModelingObject implements Serializable {

	private static final long serialVersionUID = -7570012929655893724L;
	private List<Object> childObjects = new ArrayList<Object>();
	private Object parentObject = null;
	private List<RelationObject> relations = new ArrayList<RelationObject>();
	private String name;

	// observacoes
	private String notes;
	private final int NUMBER_OF_ATTRIBUTES = 2;

	public ModelingObject(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
	}

	public void setChildObjects(List<Object> childObjects) {
		this.childObjects = childObjects;
	}

	public List<Object> getChildObjects() {
		return childObjects;
	}

	public void addChildObject(Object modelingObject) {
		childObjects.add(modelingObject);
	}

	public void addAllChildObject(List<Object> modelingObject) {
		childObjects.addAll(modelingObject);
	}

	public void removeChildObject(Object modelingObject) {
		Object objectToRemove = null;
		if (modelingObject instanceof ModelingObject) {
			Iterator<Object> iterator = childObjects.iterator();
			while (iterator.hasNext() && objectToRemove == null) {
				Object object = iterator.next();
				if (((ModelingObject) ((mxCell) object).getValue()) == (ModelingObject) modelingObject) {
					objectToRemove = object;
				}
			}
		} else {
			objectToRemove = modelingObject;
		}
		childObjects.remove(objectToRemove);
	}

	public void addRelation(RelationObject relation) {
		relations.add(relation);
	}

	public void removeRelation(RelationObject relation) {
		relations.remove(relation);
	}

	public Iterator<RelationObject> getRelationsIterator() {
		return relations.iterator();
	}

	public void setParentObject(Object parentObject) {
		this.parentObject = parentObject;
	}

	public Object getParentObject() {
		return parentObject;
	}

	public int attributesCount() {
		return NUMBER_OF_ATTRIBUTES;
	}

	public int getIndexForComponents() {
		return 0;
	}

	public void getAttributes(int types[], String names[], String values[], boolean fieldsEnabled[]) {
		int i = getIndexForComponents();

		types[i] = AppConstants.TEXT_FIELD;
		names[i] = getNameLabel();
		values[i] = name;
		fieldsEnabled[i++] = true;
		types[i] = AppConstants.TEXT_FIELD;
		names[i] = mxResources.get("notes");
		values[i] = notes;
		fieldsEnabled[i] = true;
	}

	public String getNameLabel() {
		return mxResources.get("name");
	}

	public void setAttributes(String values[]) {
		int i = getIndexForComponents();

		setName(values[i++]);
		setNotes(values[i]);
	}

	public int windowHeight() {
		return 180;
	}

	public String[] getComboValues(String name) {
		return null;
	}

	public String getToolTip() {
		return mxResources.get("name") + ": " + name + "<br>";
	}

	public String getStyle() {
		return null;
	}

	public void createHandlers(JComponent[] components) {
	}

	public void removeReferencesFromParents() {
	}

}
