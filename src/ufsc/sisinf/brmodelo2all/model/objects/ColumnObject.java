package ufsc.sisinf.brmodelo2all.model.objects;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

import ufsc.sisinf.brmodelo2all.util.AppConstants;

public class ColumnObject extends ModelingObject {

	private static final long serialVersionUID = -8644657476132504248L;
	private int position = 0;
	private boolean primaryKey = false;
	private boolean foreignKey = false;
	private String type = "Texto(1)";
	private String originTable = "";
	private String originField = "";
	private String onUpdate = "";
	private String onDelete = "";
	private String complement = "";
	private boolean optional = false;

	public boolean getOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	private final int NUMBER_OF_ATTRIBUTES = 10;

	public ColumnObject(String name, Object table) {
		super(name);
		setParentObject(table);
		this.position = ((TableObject) ((mxCell) table).getValue()).getChildObjects().size() + 1;
	}

	public ColumnObject(String name, Object table, boolean primaryKey, boolean foreignKey, boolean bothKeys) {
		super(name);
		setParentObject(table);
		if (bothKeys) {
			this.primaryKey = bothKeys;
			this.foreignKey = bothKeys;
		} else {
			this.primaryKey = primaryKey;
			this.foreignKey = foreignKey;
		}
		this.position = ((TableObject) ((mxCell) table).getValue()).getChildObjects().size() + 1;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public void incrementPosition() {
		position++;
	}

	public void decrementPosition() {
		position--;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setForeignKey(boolean foreignKey) {
		this.foreignKey = foreignKey;
	}

	public boolean isForeignKey() {
		return foreignKey;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setOriginTable(String originTable) {
		this.originTable = originTable;
	}

	public String getOriginTable() {
		return originTable;
	}

	public void setOriginField(String originField) {
		this.originField = originField;
	}

	public String getOriginField() {
		return originField;
	}

	public void setOnUpdate(String onUpdate) {
		this.onUpdate = onUpdate;
	}

	public String getOnUpdate() {
		return onUpdate;
	}

	public void setOnDelete(String onDelete) {
		this.onDelete = onDelete;
	}

	public String getOnDelete() {
		return onDelete;
	}

	public void setComplement(String complement) {
		this.complement = complement;
	}

	public String getComplement() {
		return complement;
	}

	public int attributesCount() {
		return super.attributesCount() + NUMBER_OF_ATTRIBUTES;
	}

	public void getAttributes(int types[], String names[], String values[], boolean enabled[]) {
		super.getAttributes(types, names, values, enabled);

		int i = super.attributesCount();

		types[i] = AppConstants.TEXT_FIELD;
		names[i] = mxResources.get("index");
		enabled[i] = false;
		values[i++] = Integer.toString(position);
		types[i] = AppConstants.CHECK_BOX;
		names[i] = mxResources.get("primaryKey");
		enabled[i] = true;
		values[i++] = primaryKey ? "true" : "false";
		types[i] = AppConstants.CHECK_BOX;
		names[i] = mxResources.get("foreignKey");
		enabled[i] = true;
		values[i++] = foreignKey ? "true" : "false";
		types[i] = AppConstants.TEXT_FIELD;
		names[i] = mxResources.get("type");
		enabled[i] = true;
		values[i++] = type;
		types[i] = AppConstants.TEXT_FIELD;
		names[i] = mxResources.get("originTable");
		enabled[i] = false;
		values[i++] = originTable;
		types[i] = AppConstants.TEXT_FIELD;
		names[i] = mxResources.get("originField");
		enabled[i] = false;
		values[i++] = originField;
		types[i] = AppConstants.TEXT_FIELD;
		names[i] = mxResources.get("onUpdate");
		enabled[i] = false;
		values[i++] = onUpdate;
		types[i] = AppConstants.TEXT_FIELD;
		names[i] = mxResources.get("onDelete");
		enabled[i] = false;
		values[i++] = onDelete;
		types[i] = AppConstants.TEXT_FIELD;
		names[i] = mxResources.get("complement");
		enabled[i] = false;
		values[i] = complement;
	}

	public int windowHeight() {
		return super.windowHeight() + 330;
	}

	public void setAttributes(String values[]) {
		super.setAttributes(values);

		setPosition(Integer.parseInt(values[2]));
		setPrimaryKey((Boolean.parseBoolean(values[3].toString())));
		setForeignKey((Boolean.parseBoolean(values[4].toString())));
		setType(values[5]);
		setOriginTable(values[6]);
		setOriginField(values[7]);
		setOnUpdate(values[8]);
		setOnDelete(values[9]);
		setComplement(values[10]);
	}

	public String getToolTip() {
		String tip = "Tipo: Campo<br>";

		tip += super.getToolTip();
		tip += mxResources.get("index") + ": ";
		tip += position;
		tip += "<br>";
		tip += mxResources.get("primaryKey") + ": ";
		tip += primaryKey ? mxResources.get("yes") : mxResources.get("no");
		tip += "<br>";
		tip += mxResources.get("foreignKey") + ": ";
		tip += foreignKey ? mxResources.get("yes") : mxResources.get("no");
		tip += "<br>";
		tip += mxResources.get("type") + ": ";
		tip += type;
		tip += "<br>";
		tip += mxResources.get("originTable") + ": ";
		tip += originTable;
		tip += "<br>";
		tip += mxResources.get("originField") + ": ";
		tip += originField;
		tip += "<br>";
		tip += mxResources.get("onUpdate") + ": ";
		tip += onUpdate;
		tip += "<br>";
		tip += mxResources.get("onDelete") + ": ";
		tip += onDelete;
		tip += "<br>";
		tip += mxResources.get("complement") + ": ";
		tip += complement;
		tip += "<br>";

		return tip;
	}

	public String getStyle() {
		if (primaryKey && foreignKey) {
			return "bothKeys";
		} else if (primaryKey) {
			return "primaryKey";
		} else if (foreignKey) {
			return "foreignKey";
		} else {
			return "column";
		}
	}

	public void removeReferencesFromParents() {
		ModelingObject parentObject = (ModelingObject) ((mxCell) getParentObject()).getValue();
		if (parentObject != null) {
			// adjust column positions for the parent table
			if (parentObject.getChildObjects().size() > position) {
				for (int i = position; i < parentObject.getChildObjects().size(); i++) {
					((ColumnObject) ((mxCell) parentObject.getChildObjects().get(i)).getValue()).decrementPosition();
				}
			}

			parentObject.removeChildObject(this);
		}
	}
}