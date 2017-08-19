package ufsc.sisinf.brmodelo2all.control;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxRectangle;

import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.ColumnObject;
import ufsc.sisinf.brmodelo2all.model.objects.TableObject;
import ufsc.sisinf.brmodelo2all.ui.SqlEditor;

public class LogicalConversor {

	private final ModelingComponent logicalModelingComponent;
	private final SqlEditor sqlEditor;
	static final String SPACE = " ";
	static final String COMMA = ", ";
	static final String SEMICOLON = ";";
	static final String NOTNULL = "NOT NULL";
	static final String BREAKLINE = "\n";

	public LogicalConversor(final ModelingComponent logicalModelingComponent, final SqlEditor sqlEditor) {
		this.logicalModelingComponent = logicalModelingComponent;
		this.sqlEditor = sqlEditor;
	}

	public void convertModeling() {

		mxRectangle rect = logicalModelingComponent.getGraph().getGraphBounds();

		int x = (int) rect.getX();
		int y = (int) rect.getY();

		Rectangle ret = new Rectangle(x + 60000, y + 60000);

		Object[] cells = logicalModelingComponent.getCells(ret);

		for (Object cell : cells) {
			if (cell instanceof mxCell) {
				mxCell objectCell = (mxCell) cell;
				if (objectCell.getValue() instanceof TableObject) {
					TableObject tableObject = (TableObject) objectCell.getValue();
					sqlEditor.insertSqlInstruction("CREATE TABLE IF NOT EXISTS" + SPACE + tableObject.getName() + "("
							+ BREAKLINE + insertAttributes(tableObject) + ")" + SEMICOLON);
				}
			}
		}

		/* CREATE PRIMARY KEYS, FOREIGN KEY */
		String s = "";
		int controle = 0;
		ArrayList<String> primarias = new ArrayList<String>();
		String temp = "";
		for (Object cell : cells) {
			if (cell instanceof mxCell) {
				mxCell objectCell = (mxCell) cell;
				if (objectCell.getValue() instanceof TableObject) {
					TableObject tableObject = (TableObject) objectCell.getValue();
					List<Object> childObjects = tableObject.getChildObjects();

					for (Object attribute : childObjects) {
						mxCell childCell = (mxCell) attribute;
						ColumnObject column = (ColumnObject) childCell.getValue();
						if (/* column.isPrimaryKey() */column.getStyle().equals("primaryKey")) {
							// ALTER TABLE `tabela` ADD PRIMARY KEY ( `id` ) ;
							temp = tableObject.getName();
							primarias.add("`" + column.getName() + "`");

						}

						if (column.isForeignKey()) {
							// ALTER TABLE `clientes` ADD CONSTRAINT `fk_cidade`
							// FOREIGN KEY ( `codcidade` ) REFERENCES `cidade` (
							// `codcidade` ) ;
							TableObject foreingTable = getForeingTable(column, objectCell);

							if (foreingTable != null) {
								s += "ALTER TABLE `" + tableObject.getName() + "` ADD CONSTRAINT `fk_"
										+ column.getName() + "`" + " FOREIGN KEY (`" + column.getName()
										+ "`) REFERENCES `" + foreingTable.getName() + "` (`" + column.getName() + "`)"
										+ SEMICOLON + BREAKLINE;
							}
						}
					}

					if (primarias.size() == 1) {

						s += "ALTER TABLE `" + temp + "` ADD PRIMARY KEY (" + primarias.get(0) + ")" + SEMICOLON
								+ BREAKLINE;
						primarias.clear();

					} else {

						String temp2 = "";

						for (int c = 0; c < primarias.size(); c++) {

							if (primarias.get(c) != null) {

								temp2 += primarias.get(c);

								if (c + 1 != primarias.size()) {
									temp2 += ",";
								}

							}

						}

						s += "ALTER TABLE `" + temp + "` ADD PRIMARY KEY (" + temp2 + ")" + SEMICOLON + BREAKLINE;
						primarias.clear();
						temp = "";

					}

				}
			}
		}

		sqlEditor.insertSqlInstruction(s);
	}

	private String insertAttributes(TableObject tableObject) {
		List<Object> childObjects = tableObject.getChildObjects();

		String s = "";
		for (Object attribute : childObjects) {
			mxCell cell = (mxCell) attribute;

			ColumnObject column = (ColumnObject) cell.getValue();

			String name = column.getName();
			String type = column.getType();

			if (column.getOptional() == true)

			{
				s += "`" + name + "`" + SPACE + type + SPACE + "NULL" + COMMA + BREAKLINE;
			}

			else {

				s += "`" + name + "`" + SPACE + type + SPACE + NOTNULL + COMMA + BREAKLINE;

			}

		}
		return s;
	}

	private TableObject getForeingTable(ColumnObject column, mxCell objectCell) {

		int edgeCount = objectCell.getEdgeCount();
		for (int i = 0; i < edgeCount; i++) {
			mxCell edgeAt = (mxCell) objectCell.getEdgeAt(i);
			int childCount = edgeAt.getParent().getChildCount();

			for (int j = 0; j < childCount; j++) {
				mxICell children = edgeAt.getParent().getChildAt(j);
				if (children.getValue() instanceof TableObject) {
					TableObject table = (TableObject) children.getValue();
					List<Object> attributes = table.getChildObjects();

					for (Object att : attributes) {
						mxCell cell = (mxCell) att;
						if (cell.getValue() instanceof ColumnObject) {
							ColumnObject attribute = (ColumnObject) cell.getValue();
							if (attribute.isPrimaryKey() && column.getName().equals(attribute.getName())) {
								return table;
							}
						}
					}
				}
			}
		}

		return null;
	}
}
