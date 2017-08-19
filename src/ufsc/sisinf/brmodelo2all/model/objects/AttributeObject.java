package ufsc.sisinf.brmodelo2all.model.objects;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

public class AttributeObject extends ModelingObject {
	private static final long serialVersionUID = 5885776804640481160L;
	private boolean identifier = false;
	private boolean optional = false;
	private boolean composed = false;
	private boolean multiValued = false;
	private char minimumCardinality = '1';
	private char maximumCardinality = '1';
	private String type = "Texto(1)";
	private final int NUMBER_OF_ATTRIBUTES = 7;

	public AttributeObject(String name, Object parentObject, boolean identifier) {
		super(name);
		setParentObject(parentObject);
		setIdentifier(identifier);
	}

	public void setIdentifier(boolean identifier) {
		this.identifier = identifier;
	}

	public boolean isIdentifier() {
		return this.identifier;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isOptional() {
		return this.optional;
	}

	public void setComposed(boolean composed) {
		this.composed = composed;
	}

	public boolean isComposed() {
		return this.composed;
	}

	public void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}

	public boolean isMultiValued() {
		return this.multiValued;
	}

	public void setMinimumCardinality(char minimumCardinality) {
		this.minimumCardinality = minimumCardinality;
	}

	public char getMinimumCardinality() {
		return this.minimumCardinality;
	}

	public void setMaximumCardinality(char maximumCardinality) {
		this.maximumCardinality = maximumCardinality;
	}

	public char getMaximumCardinality() {
		return this.maximumCardinality;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public int attributesCount() {
		return super.attributesCount() + 7;
	}

	public void getAttributes(int[] types, String[] names, String[] values, boolean[] enabled) {
		super.getAttributes(types, names, values, enabled);

		int i = super.attributesCount();

		types[i] = 1;
		names[i] = mxResources.get("identifier");
		enabled[i] = true;
		values[(i++)] = (this.identifier ? "true" : "false");
		types[i] = 1;

		names[i] = mxResources.get("optional");
		enabled[i] = true;
		values[(i++)] = (this.optional ? "true" : "false");
		types[i] = 1;

		names[i] = mxResources.get("composed");
		enabled[i] = false;
		values[(i++)] = (this.composed ? "true" : "false");
		types[i] = 1;

		names[i] = mxResources.get("multiValued");
		enabled[i] = true;
		values[(i++)] = (this.multiValued ? "true" : "false");
		types[i] = 2;

		names[i] = mxResources.get("minimumCardinality");
		enabled[i] = (this.multiValued ? true : false);
		values[(i++)] = Character.toString(this.minimumCardinality);
		types[i] = 2;

		names[i] = mxResources.get("maximumCardinality");
		enabled[i] = (this.multiValued ? true : false);
		values[(i++)] = Character.toString(this.maximumCardinality);
		types[i] = 0;

		names[i] = mxResources.get("type");
		enabled[i] = true;
		values[i] = this.type;
	}

	public void setAttributes(String[] values) {
		super.setAttributes(values);

		setIdentifier(Boolean.parseBoolean(values[2].toString()));
		setOptional(Boolean.parseBoolean(values[3].toString()));
		setMultiValued(Boolean.parseBoolean(values[5].toString()));
		setMinimumCardinality(values[6].charAt(0));
		setMaximumCardinality(values[7].charAt(0));
		setType(values[8]);
	}

	public int windowHeight() {
		return 360;
	}

	public String getToolTip() {
		String tip = "Tipo: Atributo<br>";

		tip = tip + super.getToolTip();
		tip = tip + mxResources.get("identifier") + ": ";
		tip = tip + (this.identifier ? mxResources.get("yes") : mxResources.get("no"));
		tip = tip + "<br>";
		tip = tip + mxResources.get("composed") + ": ";
		tip = tip + (this.composed ? mxResources.get("yes") : mxResources.get("no"));
		tip = tip + "<br>";
		tip = tip + mxResources.get("multiValued") + ": ";
		tip = tip + (this.multiValued ? mxResources.get("yes") : mxResources.get("no"));
		tip = tip + "<br>";
		if (this.multiValued) {
			tip = tip + mxResources.get("optional") + ": ";
			tip = tip + (this.optional ? mxResources.get("yes") : mxResources.get("no"));
			tip = tip + "<br>";
			tip = tip + mxResources.get("minimumCardinality") + ": ";
			tip = tip + this.minimumCardinality;
			tip = tip + "<br>";
			tip = tip + mxResources.get("maximumCardinality") + ": ";
			tip = tip + this.maximumCardinality;
			tip = tip + "<br>";
		}
		tip = tip + mxResources.get("type") + ": ";
		tip = tip + this.type;
		tip = tip + "<br>";

		return tip;
	}

	public String[] getComboValues(String name) {
		if (mxResources.get("minimumCardinality") == name) {
			String[] values = { "0", "1" };
			return values;
		}
		if (mxResources.get("maximumCardinality") == name) {
			String[] values = { "n", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
					"16", "17", "18", "19", "20" };
			return values;
		}
		return null;
	}

	public void removeReferencesFromParents() {
		ModelingObject parentObject = (ModelingObject) ((mxCell) getParentObject()).getValue();
		if (parentObject != null) {
			parentObject.removeChildObject(this);
			if ((parentObject instanceof AttributeObject)) {
				AttributeObject parentAttribute = (AttributeObject) parentObject;
				if (parentAttribute.getChildObjects().size() == 0) {
					parentAttribute.setComposed(false);
				}
			}
		}
	}

	public String toString() {
		String result = getName();
		if ((this.multiValued) || (this.optional)) {
			result = result + "(" + this.minimumCardinality + "," + this.maximumCardinality + ")";
		}
		return result;
	}

	public String getStyle() {
		return this.identifier ? "identifierAttribute" : "attribute";
	}

	public void createHandlers(JComponent[] components) {
		int componentsNumber = components.length;
		final JComboBox maximumCardinalityCombo = (JComboBox) components[(componentsNumber - 2)];
		final JComboBox minimumCardinalityCombo = (JComboBox) components[(componentsNumber - 3)];
		final JCheckBox multiValuedCheck = (JCheckBox) components[(componentsNumber - 4)];
		final JCheckBox optionalCheck = (JCheckBox) components[(componentsNumber - 6)];

		ItemListener listener = new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if (event.getSource() == multiValuedCheck) {
					boolean selected = event.getStateChange() == 1;

					optionalCheck.setEnabled(selected);
					minimumCardinalityCombo.setEnabled(selected);
					maximumCardinalityCombo.setEnabled(selected);
					if (selected) {
						maximumCardinalityCombo.setSelectedItem("n");
					} else {
						optionalCheck.setSelected(false);
						minimumCardinalityCombo.setSelectedItem("1");
						maximumCardinalityCombo.setSelectedItem("1");
					}
				} else if (event.getSource() == optionalCheck) {
					if (event.getStateChange() == 1) {
						minimumCardinalityCombo.setSelectedItem("0");
					} else {
						minimumCardinalityCombo.setSelectedItem("1");
					}
				} else if ((event.getSource() == minimumCardinalityCombo) && (event.getStateChange() == 1)) {
					optionalCheck.setSelected(minimumCardinalityCombo.getSelectedItem() == "0");
				}
			}
		};
		multiValuedCheck.addItemListener(listener);
		optionalCheck.addItemListener(listener);
		minimumCardinalityCombo.addItemListener(listener);
	}

	public boolean usesComponentHandler(String componentName) {
		return (componentName == mxResources.get("multiValued")) || (componentName == mxResources.get("optional"));
	}
}
