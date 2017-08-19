package ufsc.sisinf.brmodelo2all.model.objects;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

public class InheritanceObject extends ModelingObject {
	private static final long serialVersionUID = -4514813052165457729L;
	private boolean exclusive = false;
	private boolean partial = false;
	private boolean converted = false;
	private final int NUMBER_OF_OBJECTS = 2;

	public InheritanceObject(String name, Object parentObject) {
		super(name);
		setParentObject(parentObject);
	}

	public String toString() {
		if (this.partial) {
			return "p";
		}
		return "";
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}

	public boolean isExclusive() {
		return this.exclusive;
	}

	public void setPartial(boolean partial) {
		this.partial = partial;
	}

	public boolean isPartial() {
		return this.partial;
	}

	public void setConverted(boolean converted) {
		this.converted = converted;
	}

	public boolean isConverted() {
		return this.converted;
	}

	public int attributesCount() {
		return super.attributesCount() + 2;
	}

	public void getAttributes(int[] types, String[] names, String[] values, boolean[] enabled) {
		super.getAttributes(types, names, values, enabled);

		int i = super.attributesCount();

		types[i] = 1;
		names[i] = mxResources.get("exclusive");
		enabled[i] = false;
		values[(i++)] = (this.exclusive ? "true" : "false");
		types[i] = 1;
		names[i] = mxResources.get("partial");
		enabled[i] = true;
		values[i] = (this.partial ? "true" : "false");
	}

	public void setAttributes(String[] values) {
		super.setAttributes(values);

		setExclusive(Boolean.parseBoolean(values[2].toString()));
		setPartial(Boolean.parseBoolean(values[3].toString()));
	}

	public int windowHeight() {
		return 220;
	}

	public String getToolTip() {
		String tip = "Tipo: Especializa��o <br>";

		tip = tip + super.getToolTip();
		tip = tip + mxResources.get("exclusive") + ": ";
		tip = tip + (this.exclusive ? mxResources.get("yes") : mxResources.get("no"));
		tip = tip + "<br>";
		tip = tip + mxResources.get("partial") + ": ";
		tip = tip + (this.partial ? mxResources.get("yes") : mxResources.get("no"));
		tip = tip + "<br>";

		return tip;
	}

	public void removeReferencesFromParents() {
		Object parent = getParentObject();
		if (parent != null) {
			EntityObject entityObject = (EntityObject) ((mxCell) parent).getValue();
			entityObject.removeChildObject(this);
			entityObject.setSpecialized(false);
		}
	}

	public String getStyle() {
		return this.partial ? "partialInheritance" : "inheritance";
	}
}
