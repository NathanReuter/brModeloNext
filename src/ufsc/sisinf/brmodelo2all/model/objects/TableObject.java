package ufsc.sisinf.brmodelo2all.model.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;

import ufsc.sisinf.brmodelo2all.util.AppConstants;

public class TableObject extends ModelingObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 6240155455942061860L;
	private int conversionOrder = -1;
	private final int NUMBER_OF_ATTRIBUTES = 3;

	public TableObject(String name) {
		super(name);
	}

	public void setConversionOrder(int conversionOrder) {
		this.conversionOrder = conversionOrder;
	}

	public int getConversionOrder() {
		return conversionOrder;
	}

	public int attributesCount() {
		return super.attributesCount() + NUMBER_OF_ATTRIBUTES;
	}

	public void getAttributes(int types[], String names[], String values[], boolean enabled[]) {
		super.getAttributes(types, names, values, enabled);

		int i = super.attributesCount();

		types[i] = -1;
		names[i] = null;
		enabled[i] = true;
		values[i++] = mxResources.get("fieldsNumber") + ": " + Integer.toString(getChildObjects().size());
		types[i] = -1;
		names[i] = null;
		enabled[i] = true;
		values[i] = mxResources.get("keys") + ": [";
		values[i] += commaSeparatedPrimaryKeys();
		values[i++] += "]";
		types[i] = AppConstants.COMBO_BOX;
		names[i] = mxResources.get("conversionOrder");
		enabled[i] = true;
		values[i] = Integer.toString(conversionOrder);
	}

	public String commaSeparatedPrimaryKeys() {
		String result = "";
		Iterator<Object> iterator = getChildObjects().iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			ColumnObject columnObject = (ColumnObject) ((mxCell) object).getValue();
			if (columnObject.isPrimaryKey()) {
				if (result != "") {
					result += ",";
				}

				result += columnObject.getName();
			}
		}

		return result;
	}

	public String getToolTip() {
		String tip = "Tipo: Tabela<br>";

		tip += super.getToolTip();
		tip += mxResources.get("fieldsNumber") + ": ";
		tip += Integer.toString(getChildObjects().size());
		tip += "<br>";
		tip += mxResources.get("keys") + ": [";
		tip += commaSeparatedPrimaryKeys();
		tip += "]";
		tip += "<br>";

		return tip;
	}

	public int windowHeight() {
		return super.windowHeight() + 50;
	}

	public String[] getComboValues(String name) {

		if (mxResources.get("conversionOrder") == name) {
			String[] values = { "Sem ordem" };
			return values;
		}

		return null;
	}

	public String getStyle() {
		return "table";
	}

	public List<Object> getColumnObjects() {
		List<Object> columns = new ArrayList<>();
		List<Object> childObjects = this.getChildObjects();

		for (Object object : childObjects) {
			mxCell cell = (mxCell) object;
			if (cell.getValue() instanceof ColumnObject) {
				columns.add(object);
			}
		}
		return columns;
	}

	public List<Object> getPrimaryKeys() {
		List<Object> columnObjects = this.getColumnObjects();
		List<Object> primaryKeys = new ArrayList<>();

		for (Object object : columnObjects) {
			if (((mxCell) object).getValue() instanceof ColumnObject) {
				ColumnObject column = (ColumnObject) ((mxCell) object).getValue();
				if (column.isPrimaryKey()) {
					primaryKeys.add(object);
				}
			}
		}
		return primaryKeys;
	}

	public List<String> getPrimarias() {
		List<Object> columnObjects = this.getColumnObjects();
		List<String> primaryKeys = new ArrayList<>();

		for (Object object : columnObjects) {
			if (((mxCell) object).getValue() instanceof ColumnObject) {
				ColumnObject column = (ColumnObject) ((mxCell) object).getValue();
				if (column.isPrimaryKey()) {
					primaryKeys.add(column.getName());
				}
			}
		}
		return primaryKeys;
	}

	public List<Object> getForeignKeys() {
		List<Object> columnObjects = this.getColumnObjects();
		List<Object> primaryKeys = new ArrayList<>();

		for (Object object : columnObjects) {
			if (((mxCell) object).getValue() instanceof ColumnObject) {
				ColumnObject column = (ColumnObject) ((mxCell) object).getValue();
				if (column.isForeignKey()) {
					primaryKeys.add(object);
				}
			}
		}
		return primaryKeys;
	}

	public List<Object> getBothKeys() {
		List<Object> columnObjects = this.getColumnObjects();
		List<Object> primaryKeys = new ArrayList<>();

		for (Object object : columnObjects) {
			if (((mxCell) object).getValue() instanceof ColumnObject) {
				ColumnObject column = (ColumnObject) ((mxCell) object).getValue();
				if (column.isForeignKey() && column.isPrimaryKey()) {
					primaryKeys.add(object);
				}
			}
		}
		return primaryKeys;
	}
}