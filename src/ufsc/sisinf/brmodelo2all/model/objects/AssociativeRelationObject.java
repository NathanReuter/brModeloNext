package ufsc.sisinf.brmodelo2all.model.objects;

import com.mxgraph.util.mxResources;

public class AssociativeRelationObject extends RelationObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6136299230865369725L;

	private int index = 0;

	public AssociativeRelationObject(String name) {
		super(name);
	}

	public int attributesCount() {
		return super.attributesCount();
	}

	public void setIndexForComponents(int index) {
		this.index = index;
	}

	public int getIndexForComponents() {
		return index;
	}

	public void getAttributes(int types[], String names[], String values[], boolean fieldsEnabled[]) {
		super.getAttributes(types, names, values, fieldsEnabled);
	}

	public String getNameLabel() {
		return mxResources.get("relationName");
	}

	public void setAttributes(String values[]) {
		super.setAttributes(values);
	}

	public int windowHeight() {
		return 220;
	}

	public String getToolTip() {
		String tip = "<center>Relacionamento</center>";

		tip += super.getToolTip();

		return tip;
	}

	public String getStyle() {
		return "associativeRelation";
	}
}
