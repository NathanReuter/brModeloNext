package ufsc.sisinf.brmodelo2all.model.objects;

import javax.swing.JOptionPane;

import com.mxgraph.util.mxResources;

import ufsc.sisinf.brmodelo2all.model.Cardinality;
import ufsc.sisinf.brmodelo2all.util.AppConstants;

public class ConnectorObject extends ModelingObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 5677278255836890443L;
	private Cardinality cardinality = Cardinality.ZERO_ONE;
	private int maximum;

	private boolean weakEntity = false;

	// Ao alterar o número de atributos desta classe, alterar
	// NUMBER_OF_ATTRIBUTES

	private final int NUMBER_OF_ATTRIBUTES = 3;

	public ConnectorObject(String name) {
		super(name);
	}

	public ConnectorObject(Cardinality cardinality) {
		super("");
		this.cardinality = cardinality;
	}

	public ConnectorObject(Cardinality value, String string) {
		super(string);
		this.cardinality = value;
	}

	public String toString() {

		char[] f = Cardinality.getText(cardinality).toCharArray();

		if (getMaximum() != 0 && f[3] == 'n') {

			return "(" + f[1] + "," + getMaximum() + "" + ")" + " " + super.getName();

		}

		return Cardinality.getText(cardinality) + " " + super.getName();
	}

	public void setCardinality(Cardinality cardinality) {
		this.cardinality = cardinality;
	}

	public Cardinality getCardinality() {
		return cardinality;
	}

	public void setWeakEntity(boolean weakEntity) {
		this.weakEntity = weakEntity;
	}

	public boolean isWeakEntity() {
		return weakEntity;
	}

	public int attributesCount() {
		return super.attributesCount() + NUMBER_OF_ATTRIBUTES;
	}

	public void getAttributes(int types[], String names[], String values[], boolean enabled[]) {
		super.getAttributes(types, names, values, enabled);

		int i = super.attributesCount();

		types[i] = AppConstants.CHECK_BOX;
		names[i] = mxResources.get("weakEntity");
		enabled[i] = true;
		values[i++] = weakEntity ? "true" : "false";

		types[i] = AppConstants.COMBO_BOX;
		names[i] = mxResources.get("cardinality");
		enabled[i] = true;
		values[i++] = Cardinality.getText(cardinality);

		types[i] = AppConstants.TEXT_FIELD;
		names[i] = "Indicar a cardinalidade máxima(n)";
		enabled[i] = true;
		values[i] = getMaximum() + "";

	}

	public int getMaximum() {

		return maximum;

	}

	public void setAttributes(String values[]) {
		super.setAttributes(values);

		setWeakEntity(Boolean.parseBoolean(values[2].toString()));
		setCardinality(Cardinality.getValue(values[3].toString()));
		try {
			setMaximum(Integer.parseInt(values[4].toString()));
		} catch (NumberFormatException x) {

			JOptionPane.showMessageDialog(null, "A cardinalidade deve ser um número e maior que zero!");

		}

	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public int windowHeight() {
		return 300;
	}

	public String getToolTip() {
		String tip = "Tipo: Cardinalidade<br>";

		tip += super.getToolTip();
		tip += mxResources.get("weakEntity") + ": ";
		tip += weakEntity ? mxResources.get("yes") : mxResources.get("no");
		tip += "<br>";
		tip += mxResources.get("cardinality") + ": ";
		tip += Cardinality.getText(cardinality);
		tip += "<br>";

		return tip;
	}

	public String[] getComboValues(String name) {

		if (mxResources.get("cardinality") == name) {
			String[] values = { "(0,1)", "(0,n)", "(1,1)", "(1,n)" };
			return values;
		}

		return null;
	}

	public String getStyle() {
		return weakEntity ? "weakEntity" : "entityRelationConnector";
	}

	public String getNameLabel() {
		return mxResources.get("paper");
	}
}