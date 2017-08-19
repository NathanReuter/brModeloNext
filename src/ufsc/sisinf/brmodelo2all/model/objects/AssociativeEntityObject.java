package ufsc.sisinf.brmodelo2all.model.objects;

import com.mxgraph.util.mxResources;

import ufsc.sisinf.brmodelo2all.util.AppConstants;

public class AssociativeEntityObject extends ModelingObject {

	private static final long serialVersionUID = -3208501055540606907L;
	private boolean selfRelated = false;
	private boolean relationAlreadyResolved = false;
	private final int NUMBER_OF_ATTRIBUTES = 1;
	private final AssociativeRelationObject relationObject;

	public AssociativeRelationObject getRelationObject() {
		return relationObject;
	}

	public AssociativeEntityObject(String name, final AssociativeRelationObject relationObject) {
		super(name);
		this.relationObject = relationObject;
	}

	public void setSelfRelated(boolean selfRelated) {
		this.selfRelated = selfRelated;
	}

	public boolean isSelfRelated() {
		return selfRelated;
	}

	public int attributesCount() {
		return super.attributesCount() + relationObject.attributesCount() + NUMBER_OF_ATTRIBUTES;
	}

	public void getAttributes(int types[], String names[], String values[], boolean fieldsEnabled[]) {
		super.getAttributes(types, names, values, fieldsEnabled);

		int i = super.attributesCount();

		types[i] = AppConstants.CHECK_BOX;
		names[i] = mxResources.get("selfRelated");
		fieldsEnabled[i] = false;
		values[i++] = selfRelated ? "true" : "false";

		relationObject.setIndexForComponents(super.attributesCount() + NUMBER_OF_ATTRIBUTES);
		relationObject.getAttributes(types, names, values, fieldsEnabled);
	}

	public void setAttributes(String values[]) {
		super.setAttributes(values);

		setSelfRelated(Boolean.parseBoolean(values[2].toString()));

		relationObject.setIndexForComponents(super.attributesCount() + NUMBER_OF_ATTRIBUTES);
		relationObject.setAttributes(values);
	}

	public int windowHeight() {
		return 300;
	}

	public String getToolTip() {
		String tip = "Tipo: Entidade associativa<br>";

		tip += super.getToolTip();
		tip += mxResources.get("selfRelated") + ": ";
		tip += selfRelated ? mxResources.get("yes") : mxResources.get("no");
		tip += relationObject.getToolTip();

		return tip;
	}

	public String getStyle() {
		return "associativeEntity";
	}
}
