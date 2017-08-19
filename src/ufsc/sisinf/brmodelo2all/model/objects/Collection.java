package ufsc.sisinf.brmodelo2all.model.objects;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.mxgraph.util.mxResources;

public class Collection extends ModelingObject {
	private final int NUMBER_OF_ATTRIBUTES = 4;
	private boolean optional = false;
	private boolean multiValued = false;
	private boolean referencedBlock = false;
	private char minimumCardinality = '1';
	private char maximumCardinality = '1';
	private ArrayList<NoSqlAttributeObject> identifierAttributes = new ArrayList();
	private ArrayList<NoSqlAttributeObject> attributes = new ArrayList();
	private boolean block;
	private boolean disjunction = false;

	public Collection(String name, boolean subEntity) {
		super(name);
		this.block = subEntity;
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

	public void setReferencedCollection(boolean referencedCollection) {
		this.referencedBlock = referencedCollection;
	}

	public boolean isReferencedCollection() {
		return this.referencedBlock;
	}

	public char getMinimumCardinality() {
		return this.minimumCardinality;
	}

	public void setMinimumCardinality(char minimumCardinality) {
		this.minimumCardinality = minimumCardinality;
	}

	public char getMaximumCardinality() {
		return this.maximumCardinality;
	}

	public void setMaximumCardinality(char maximumCardinality) {
		this.maximumCardinality = maximumCardinality;
	}

	public String getStyle() {
		return "verticalAlign=top";
	}

	public void addAttribute(NoSqlAttributeObject attribute) {
		if (!attribute.isIdentifierAttribute()) {
			this.attributes.add(attribute);
		} else if (attribute.isIdentifierAttribute()) {
			this.identifierAttributes.add(attribute);
		}
	}

	public boolean isBlock() {
		return this.block;
	}

	public ArrayList<NoSqlAttributeObject> getIdentifierAttributes() {
		return this.identifierAttributes;
	}

	public int attributesCount() {
		if (this.block) {
			return super.attributesCount() + 4;
		}
		return super.attributesCount();
	}

	public void setAttributes(String[] values) {
		if (this.block) {
			super.setAttributes(values);

			setOptional(Boolean.parseBoolean(values[2].toString()));
			setMultiValued(Boolean.parseBoolean(values[3].toString()));
			setMinimumCardinality(values[4].charAt(0));
			setMaximumCardinality(values[5].charAt(0));
		} else {
			super.setAttributes(values);
		}
	}

	public void getAttributes(int[] types, String[] names, String[] values, boolean[] enabled) {
		if (this.block) {
			super.getAttributes(types, names, values, enabled);

			int i = super.attributesCount();

			types[i] = 1;
			names[i] = mxResources.get("optional");
			enabled[i] = true;
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
			values[i] = Character.toString(this.maximumCardinality);
		} else {
			super.getAttributes(types, names, values, enabled);
		}
	}

	public String[] getComboValues(String name) {
		if (mxResources.get("minimumCardinality") == name) {
			String[] values = { "0", "1" };
			return values;
		}
		if (mxResources.get("maximumCardinality") == name) {
			String[] values = { "n", "1" };
			return values;
		}
		return null;
	}

	public int windowHeight() {
		if (this.block) {
			return 280;
		}
		return super.windowHeight();
	}

	public void createHandlers(JComponent[] components) {
		if (this.block) {
			int componentsNumber = components.length;
			final JComboBox maximumCardinalityCombo = (JComboBox) components[(componentsNumber - 1)];
			final JComboBox minimumCardinalityCombo = (JComboBox) components[(componentsNumber - 2)];
			final JCheckBox multiValuedCheck = (JCheckBox) components[(componentsNumber - 3)];
			final JCheckBox optionalCheck = (JCheckBox) components[(componentsNumber - 4)];

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
	}

	public boolean usesComponentHandler(String componentName) {
		return (componentName == mxResources.get("multiValued")) || (componentName == mxResources.get("optional"));
	}

	// Some needs for the NoSQL Conversion. @Author: Fabio Volkmann Coelho.
	/**
	 * Get the disjunction of this block.
	 * 
	 * @return true if the block has a disjunction within
	 */
	public boolean getDisjunction() {
		return this.disjunction;
	}

	/**
	 * Set the block to have or not the disjunction parameter.
	 * 
	 * @param isDisjunction
	 *            boolean.
	 */
	public void setDisjunction(boolean isDisjunction) {
		this.disjunction = isDisjunction;
	}
	
}
