package ufsc.sisinf.brmodelo2all.model.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

public class EntityObject extends ModelingObject implements Comparable<EntityObject> {
	private static final long serialVersionUID = -7141376806822311393L;
	private boolean selfRelated = false;
	private boolean specialized = false;
	private boolean markedEntity = false;
	private boolean convertedEntity = false;
	private int functionalClosureCount = 1;
	private int numberOfRelationsConnected = 0;
	private final int NUMBER_OF_ATTRIBUTES = 2;

	public EntityObject(String name) {
		super(name);
	}

	public void setSelfRelated(boolean selfRelated) {
		this.selfRelated = selfRelated;
	}

	public boolean isSelfRelated() {
		return this.selfRelated;
	}

	public void setSpecialized(boolean specialized) {
		this.specialized = specialized;
	}

	public boolean isSpecialized() {
		return this.specialized;
	}

	public void setMarkedEntity(boolean markedEntity) {
		this.markedEntity = markedEntity;
	}

	public boolean isMarkedEntity() {
		return this.markedEntity;
	}

	public void setConvertedEntity(boolean convertedEntity) {
		this.convertedEntity = convertedEntity;
	}

	public boolean isConvertedEntity() {
		return this.convertedEntity;
	}

	public void addFunctionalClosureCount() {
		this.functionalClosureCount += 1;
	}

	public int getFunctionalClosureCount() {
		return this.functionalClosureCount;
	}

	public void addNumberOfRelationsConnected() {
		this.numberOfRelationsConnected += 1;
	}

	public int getNumberOfRelationsConnected() {
		return this.numberOfRelationsConnected;
	}

	public int attributesCount() {
		return super.attributesCount() + 2;
	}

	public void getAttributes(int[] types, String[] names, String[] values, boolean[] fieldsEnabled) {
		super.getAttributes(types, names, values, fieldsEnabled);

		int i = super.attributesCount();

		types[i] = 1;
		names[i] = mxResources.get("selfRelated");
		fieldsEnabled[i] = false;
		values[(i++)] = (this.selfRelated ? "true" : "false");
		types[i] = 1;
		names[i] = mxResources.get("specialized");
		fieldsEnabled[i] = false;
		values[i] = (this.specialized ? "true" : "false");
	}

	public void setAttributes(String[] values) {
		super.setAttributes(values);

		setSelfRelated(Boolean.parseBoolean(values[2].toString()));
		setSpecialized(Boolean.parseBoolean(values[3].toString()));
	}

	public int windowHeight() {
		return 220;
	}

	public String getToolTip() {
		String tip = "Tipo: Entidade <br>";

		tip = tip + super.getToolTip();
		tip = tip + mxResources.get("selfRelated") + ": ";
		tip = tip + (this.selfRelated ? mxResources.get("yes") : mxResources.get("no"));
		tip = tip + "<br>";
		tip = tip + mxResources.get("specialized") + ": ";
		tip = tip + (this.specialized ? mxResources.get("yes") : mxResources.get("no"));
		tip = tip + "<br>";

		return tip;
	}

	public void removeReferencesFromParents() {
		mxCell parent = (mxCell) getParentObject();
		if ((parent != null) && ((parent.getValue() instanceof InheritanceObject))) {
			InheritanceObject inheritance = (InheritanceObject) parent.getValue();
			if (inheritance.getChildObjects().size() == 2) {
				inheritance.setExclusive(false);
			}
			inheritance.removeChildObject(this);

			setParentObject(null);
		}
	}

	public String getStyle() {
		return "entity";
	}

	public List<Object> getPrimaryKeys() {
		List<Object> primaryKeys = new ArrayList();

		Iterator<Object> iterator = getChildObjects().iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if ((((mxCell) object).getValue() instanceof AttributeObject)) {
				AttributeObject attribute = (AttributeObject) ((mxCell) object).getValue();
				if (attribute.isIdentifier()) {
					primaryKeys.add(object);
				}
			}
		}
		return primaryKeys;
	}

	public int compareTo(EntityObject otherEntityObject) {
		if (this.functionalClosureCount < otherEntityObject.getFunctionalClosureCount()) {
			return 1;
		}
		if (this.functionalClosureCount > otherEntityObject.getFunctionalClosureCount()) {
			return -1;
		}
		return 0;
	}
}