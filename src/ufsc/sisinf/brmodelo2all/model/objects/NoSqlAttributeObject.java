package ufsc.sisinf.brmodelo2all.model.objects;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxResources;

public class NoSqlAttributeObject extends ModelingObject {
	private boolean identifierAttribute = false;
	private boolean referenceAttribute = false;
	private boolean optional = false;
	private boolean multiValued = false;
	private char minimumCardinality = '1';
	private char maximumCardinality = '1';
	private String type = "string";
	private int position = 0;
	private final int NUMBER_OF_ATTRIBUTES = 6;
	private NoSqlAttributeObject referenceObject = null;

	public NoSqlAttributeObject(String name, Object collection) {
		super(name);
		setParentObject(collection);
		this.position = (((Collection) ((mxCell) collection).getValue()).getChildObjects().size() + 1);
	}

	public NoSqlAttributeObject(String name, Object collection, boolean identifierAttribute,
			boolean referenceAttribute) {
		super(name);
		setParentObject(collection);
		this.identifierAttribute = identifierAttribute;
		this.referenceAttribute = referenceAttribute;
		this.position = (((Collection) ((mxCell) collection).getValue()).getChildObjects().size() + 1);
	}

	public boolean isIdentifierAttribute() {
		return this.identifierAttribute;
	}

	public void setIdentifierAttribute(boolean identifierAttribute) {
		this.identifierAttribute = identifierAttribute;
	}

	public boolean isReferenceAttribute() {
		return this.referenceAttribute;
	}

	public boolean isOptional() {
		return this.optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isMultiValued() {
		return this.multiValued;
	}

	public void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}

	public char getMaximumCardinality() {
		return this.maximumCardinality;
	}

	public void setMaximumCardinality(char maximumCardinality) {
		this.maximumCardinality = maximumCardinality;
	}

	public void setMinimumCardinality(char minimumCardinality) {
		this.minimumCardinality = minimumCardinality;
	}

	public char getMinimumCardinality() {
		return this.minimumCardinality;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Collection getParent() {
		return (Collection) getParentObject();
	}

	public String toString() {
		String result = getName();
		if (this.identifierAttribute) {
			result = "ID_" + result;
		}
		return result;
	}

	public String getStyle() {
		return "align=left";
	}

	public int attributesCount() {
		return super.attributesCount() + 6;
	}

	public void getAttributes(int[] types, String[] names, String[] values, boolean[] enabled) {
		super.getAttributes(types, names, values, enabled);

		int i = super.attributesCount();

		types[i] = 1;
		names[i] = mxResources.get("identifierAttribute");
		enabled[i] = true;
		values[(i++)] = (this.identifierAttribute ? "true" : "false");

		types[i] = 1;
		names[i] = mxResources.get("optional");
		enabled[i] = (this.identifierAttribute ? false : true);
		values[(i++)] = (this.optional ? "true" : "false");

		types[i] = 1;
		names[i] = mxResources.get("multiValued");
		enabled[i] = true;
		values[(i++)] = (this.multiValued ? "true" : "false");

		types[i] = 2;
		names[i] = mxResources.get("minimumCardinality");
		enabled[i] = false;
		values[(i++)] = Character.toString(this.minimumCardinality);

		types[i] = 2;
		names[i] = mxResources.get("maximumCardinality");
		enabled[i] = false;
		values[(i++)] = Character.toString(this.maximumCardinality);

		types[i] = 0;
		names[i] = mxResources.get("type");
		enabled[i] = true;
		values[i] = this.type;
	}

	public void setAttributes(String[] values) {
		super.setAttributes(values);

		setIdentifierAttribute(Boolean.parseBoolean(values[2].toString()));
		setOptional(Boolean.parseBoolean(values[3].toString()));
		setMultiValued(Boolean.parseBoolean(values[4].toString()));
		setMinimumCardinality(values[5].charAt(0));
		setMaximumCardinality(values[6].charAt(0));
		setType(values[7]);
	}

	public String getToolTip() {
		String tip = "Tipo: Atributo NoSQL<br>";

		tip = tip + super.getToolTip();
		tip = tip + mxResources.get("multiValued") + ": ";
		tip = tip + (this.multiValued ? mxResources.get("yes") : mxResources.get("no"));
		tip = tip + "<br>";
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

	public int windowHeight() {
		return 335;
	}

	public void createHandlers(JComponent[] components) {
		int componentsNumber = components.length;
		final JComboBox maximumCardinalityCombo = (JComboBox) components[(componentsNumber - 2)];
		final JComboBox minimumCardinalityCombo = (JComboBox) components[(componentsNumber - 3)];
		final JCheckBox multiValuedCheck = (JCheckBox) components[(componentsNumber - 4)];
		final JCheckBox optionalCheck = (JCheckBox) components[(componentsNumber - 5)];

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

	
	/**
	 * Set the NoSQLAttributeObject Identifier that is referenced.
	 * 
	 * @param noSQLAttribute
	 */
	public void setReferencedObject(NoSqlAttributeObject noSQLAttribute) {
		this.referenceObject = noSQLAttribute;
	}

	public NoSqlAttributeObject getReferencedObject() {
		return referenceObject;
	}

}
