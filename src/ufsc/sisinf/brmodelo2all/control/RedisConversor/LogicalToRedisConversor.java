package ufsc.sisinf.brmodelo2all.control.RedisConversor;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxRectangle;
import java.util.ArrayList;
import java.util.List;
import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.Collection;
import ufsc.sisinf.brmodelo2all.model.objects.NoSqlAttributeObject;
import ufsc.sisinf.brmodelo2all.ui.NoSqlEditor;

import java.awt.*;

public class LogicalToRedisConversor {

    private ModelingComponent logicalModelingComponent;
    private NoSqlEditor sqlEditor;
    private RedisInstructionsBuilder instructionsBuilder;

    public LogicalToRedisConversor(final ModelingComponent logicalModelingComponent, final NoSqlEditor sqlEditor) {
        this.logicalModelingComponent = logicalModelingComponent;
        this.sqlEditor = sqlEditor;
        instructionsBuilder = new RedisInstructionsBuilder();
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
        String instructions = instructionsBuilder.genInitialSupportInstructions();
        List<RedisObjectData> objectsData = new ArrayList<RedisObjectData>();

        for (Object cell : cells) {
            if (cell instanceof mxCell) {
                mxCell objectCell = (mxCell) cell;
                objectsData.add(verifyCellObjects(objectCell));
            }
        }

        instructions += instructions.concat(instructionsBuilder.genCheckCollectionsCase(objectsData));
        sqlEditor.insertSqlInstruction(instructions.concat(instructionsBuilder.genExecutionInstruction()));
    }

    private RedisObjectData verifyCellObjects (mxCell objectCell) {
        RedisObjectData objectData = new RedisObjectData();
        objectData.setObjectName(((Collection) objectCell.getValue()).getName());

        for (mxICell childrenCell : getCellChild(objectCell)) {
            if (childrenCell.getValue() instanceof Collection) {
                Collection childBlock = (Collection) childrenCell.getValue();
                if ((childBlock.getMinimumCardinality() == 49)) {
                    objectData.addAttributes(childBlock.getName());
                }
            } else if (childrenCell.getValue() instanceof NoSqlAttributeObject) {
                NoSqlAttributeObject attribute = (NoSqlAttributeObject) childrenCell.getValue();
                if ((attribute.getMinimumCardinality() == 49) || attribute.isIdentifierAttribute()) {
                    objectData.addAttributes(((NoSqlAttributeObject) childrenCell.getValue()).getName());
                }
            }
        }

        return objectData;
    }
}
