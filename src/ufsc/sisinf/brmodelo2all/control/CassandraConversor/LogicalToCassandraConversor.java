package ufsc.sisinf.brmodelo2all.control.CassandraConversor;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxRectangle;
import ufsc.sisinf.brmodelo2all.control.NosqlConfigurationData;
import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.Collection;
import ufsc.sisinf.brmodelo2all.model.objects.DisjunctionObject;
import ufsc.sisinf.brmodelo2all.model.objects.NoSqlAttributeObject;
import ufsc.sisinf.brmodelo2all.ui.NoSqlEditor;

import java.awt.*;

public class LogicalToCassandraConversor {

    private ModelingComponent logicalModelingComponent;
    private NoSqlEditor sqlEditor;
    private NosqlConfigurationData configData;
    private CassandraInstructionsBuilder instructionsBuilder;

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
                instructions += verifyCellObjects(objectCell);
            }
        }

        sqlEditor.insertSqlInstruction(instructions);
    }



    public String verifyCellObjects(mxCell objectCell) {
        System.out.println(objectCell.getValue());
        String instructions = "";
        CassandraObjectData cassandraObjectData = new CassandraObjectData();

        if (objectCell.getChildCount() > 0) {
            for (mxICell childrenCell : getCellChild(objectCell)) {
                if (childrenCell.getValue() instanceof Collection ||
                        childrenCell.getValue() instanceof DisjunctionObject) {
                    instructions += verifyCellObjects((mxCell) childrenCell);
                } else if (childrenCell.getValue() instanceof NoSqlAttributeObject) {
                    NoSqlAttributeObject attributeObject = (NoSqlAttributeObject) childrenCell.getValue();
                    String attributeName = childrenCell.getValue().toString();
                    String attributeType = attributeObject.getType();

                    cassandraObjectData.addAttributes(new CassandraAttribute(attributeName,
                            CassandraAttribute.typeConverter(attributeType)));
                }
            }
        }

        if (objectCell.getValue() instanceof Collection) {
            cassandraObjectData.setObjectName(((Collection) objectCell.getValue()).getName());
            instructions += instructionsBuilder.genTablesInstructions(cassandraObjectData);
        } else if (objectCell.getValue() instanceof NoSqlAttributeObject) {

        }

        return instructions;
    }
}
