package ufsc.sisinf.brmodelo2all.control.CassandraConversor;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxRectangle;
import ufsc.sisinf.brmodelo2all.control.NosqlConfigurationData;
import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.Collection;
import ufsc.sisinf.brmodelo2all.model.objects.NoSqlAttributeObject;
import ufsc.sisinf.brmodelo2all.ui.NoSqlEditor;

import java.awt.*;

public class LogicalToCassandraConversor {

    private ModelingComponent logicalModelingComponent;
    private NoSqlEditor sqlEditor;
    private NosqlConfigurationData configData;
    private CassandraInstructionsBuilder instructionsBuilder;
    private enum TableType {TABLE, NEWTYPE};

    public LogicalToCassandraConversor(final ModelingComponent logicalModelingComponent, final NoSqlEditor sqlEditor) {
        this.logicalModelingComponent = logicalModelingComponent;
        this.sqlEditor = sqlEditor;
        configData = NosqlConfigurationData.getInstance();
        instructionsBuilder = new CassandraInstructionsBuilder();
    }

    private Object[] getCellsComponents () {
        mxRectangle rect = logicalModelingComponent.getGraph().getGraphBounds();
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        Rectangle ret = new Rectangle(x + 60000, y + 60000);

        return logicalModelingComponent.getCells(ret);
    }

    private mxICell[] getCellChild(mxICell objectCell) {
        mxICell[] child = new mxICell[objectCell.getChildCount()];
        for (int i = 0; i < objectCell.getChildCount(); i++) {
            child[i] = objectCell.getChildAt(i);
        }

        return child;
    }

    public void convertModeling() {
        Object[] cells = getCellsComponents();
        String instructions = instructionsBuilder.genInitialDBInstructions();

        for (Object cell : cells) {
            if (cell instanceof mxCell) {
                mxCell objectCell = (mxCell) cell;
                instructions += verifyCellObjects(objectCell, TableType.TABLE);
            }
        }

        sqlEditor.insertSqlInstruction(instructions);
    }

    private boolean collectionsHasIdentifier (mxICell collection) {
        if (collection.getChildCount() > 0) {
            for (mxICell children : getCellChild(collection)) {
                if (children.getValue() instanceof NoSqlAttributeObject) {
                    if (((NoSqlAttributeObject) children.getValue()).isIdentifierAttribute()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void configCassandraAttributes (NoSqlAttributeObject attributeObject, CassandraObjectData cassandraObjectData) {
        String attributeName = attributeObject.getName();
        String attributeType = attributeObject.getType();

        if (attributeObject.isIdentifierAttribute()) {
            attributeType = "id";
            cassandraObjectData.setPrimaryKey(attributeName);
        } else if (attributeObject.isReferenceAttribute()) {
            attributeType = "id";
        }

        cassandraObjectData.addAttributes(new CassandraAttribute(attributeName,
                CassandraAttribute.typeConverter(attributeType),
                hashMultipleCardinalities(null, attributeObject)));
    }

    public boolean hashMultipleCardinalities (Collection collectionObject, NoSqlAttributeObject attributeObject) {
        return (collectionObject != null && collectionObject.getMaximumCardinality() != 49)
                || (attributeObject != null && attributeObject.getMaximumCardinality() != 49);
    }

    /* Disjunction cases can't be modeled in cassandra collum type or required statements*/

    public String verifyCellObjects(mxCell objectCell, TableType tableType) {
        String instructions = "";
        CassandraObjectData cassandraObjectData = new CassandraObjectData();

        if (objectCell.getChildCount() > 0) {
            for (mxICell childrenCell : getCellChild(objectCell)) {
                if (childrenCell.getValue() instanceof Collection) {
                    if (collectionsHasIdentifier(childrenCell)) {
                        instructions += verifyCellObjects((mxCell) childrenCell, tableType.TABLE);
                    } else {
                        Collection collection = (Collection) childrenCell.getValue();
                        cassandraObjectData.addAttributes(new CassandraAttribute(collection.getName(),
                                CassandraAttribute.CassandraTypes.NEWTYPE,
                                hashMultipleCardinalities(collection, null)));
                        instructions += verifyCellObjects((mxCell) childrenCell, tableType.NEWTYPE);
                    }
                } else if (childrenCell.getValue() instanceof NoSqlAttributeObject) {
                    configCassandraAttributes((NoSqlAttributeObject) childrenCell.getValue(), cassandraObjectData);
                }
            }
        }

        if (objectCell.getValue() instanceof Collection) {
            cassandraObjectData.setObjectName(((Collection) objectCell.getValue()).getName());

            if (tableType.equals(TableType.TABLE)) {
                instructions += instructionsBuilder.genTablesInstructions(cassandraObjectData);
            } else {
                instructions += instructionsBuilder.genTypeInstructions(cassandraObjectData);
            }
        }

        return instructions;
    }
}
