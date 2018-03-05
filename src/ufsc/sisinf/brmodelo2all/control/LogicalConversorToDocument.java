package ufsc.sisinf.brmodelo2all.control;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.Collection;
import ufsc.sisinf.brmodelo2all.model.objects.DisjunctionObject;
import ufsc.sisinf.brmodelo2all.model.objects.ModelingObject;
import ufsc.sisinf.brmodelo2all.model.objects.NoSqlAttributeObject;
import ufsc.sisinf.brmodelo2all.ui.NoSqlEditor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import ufsc.sisinf.brmodelo2all.util.AppConstants;

/**
 * Esta classe ira transformar os blocos/colecoes/atributos do modelo logico do
 * NoSQL para instrucoes shell do banco de documento MongoDB. Os blocos e colecoes sao do tipo
 * Collection, os atributos, tanto Id quanto Ref, sao do tipo
 * NoSqlAttributeObject Como todos os objetos que vem para a conversao sao do
 * tipo mxICell, eh necessario verificar o seu valor para saber de qual tipo ele
 * pertence
 *
 * Apos transformar tudo em JSON-Schema, retorna o valor da instruction para o
 * sqlEditor.
 *
 * Tudo que possui cardinalidade maximo diferente de n, eh criado um array para
 * controlar a quantia de items dentro desse bloco ou atributo, pois o type
 * object nao possui um controle sobre a quantia de objetos que vao dentro da
 * propriedade. Enquanto o array possui. Portanto para casos que nao eh
 * necessario ter controle de quantos objetos irao ser inseridos eh utilizado o
 * object com properties.
 *
 * @author Nathan Reuter Godinho
 *
 */

public class LogicalConversorToDocument {

    private final ModelingComponent logicalModelingComponent;
    private final NoSqlEditor sqlEditor;
    static final String SPACE = " ";
    static final String TAB = "  ";
    static final String TABL2 = TAB + TAB;
    static final String TABL3 = TABL2 + TAB;
    static final String COMMA = ", ";
    static final String SEMICOLON = ";";
    static final String NOTNULL = "NOT NULL";
    static final String BREAKLINE = "\n";
    static final String OPENBRACES = "{";
    static final String CLOSEBRACES = "}";
    static final String OPENPARENTHESES = "(";
    static final String CLOSEPARENTHESES = ")";
    static final String OPENBRACKETS = "[";
    static final String CLOSEBRACKTS = "]";
    static final String CREATECOLLECTIONCOMMAND = "db.createCollection";
    static final String VALIDATOR = "validator";
    static final String OR = "$or";
    static final String COLON = ":";
    static final String QUOTATIONMARK = mxResources.get("quotationMark");
    static final String SELECTDB = "use";
    static final String TYPE = "$type";
    static final String EXISTS = "$exists";
    static final String ALL = "$all";
    static final String TRUE = "true";
    static final String FALSE = "false";
    static final String JSONSCHEMA = "$jsonSchema";
    static final String VALITATIONACTION = "validationAction";
    static final String VALIDATIONLEVEL = "validationLevel";
    static final String BSONTYPE = "bsonType";
    static final String TYPEOBJECT = "object";
    private String instruction = "";
    private String mongoActionLevel = AppConstants.MONGO_DEFAULT_ACTION_LEVEL;
    private String mongoValitationLevel = AppConstants.MONGO_DEFAULT_VALITION_LEVEL;
    private List<String> listWithRequired = new ArrayList<String>();
    private String path;

    public LogicalConversorToDocument(final ModelingComponent logicalModelingComponent, final NoSqlEditor sqlEditor) {
        this.logicalModelingComponent = logicalModelingComponent;
        this.sqlEditor = sqlEditor;
    }

    public void convertModeling() {
        mxRectangle rect = logicalModelingComponent.getGraph().getGraphBounds();
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        Rectangle ret = new Rectangle(x + 60000, y + 60000);
        Object[] cells = logicalModelingComponent.getCells(ret);
        instruction += startDB("NovoDB");

        for (Object cell : cells) {
            if (cell instanceof mxCell) {
                mxCell objectCell = (mxCell) cell;

                if (objectCell.getValue() instanceof Collection) {
                    instruction += addColletion(objectCell);
                }

            }
        }

        sqlEditor.insertSqlInstruction(instruction);
    }

    private String startDB(String name) {
        return SELECTDB.concat(" ").concat(name).concat(BREAKLINE);
    }

    private String addColletion(mxCell objectCell) {
        String newColletionInstruction = CREATECOLLECTIONCOMMAND
                + OPENPARENTHESES
                    + QUOTATIONMARK + objectCell.getValue() + QUOTATIONMARK + COMMA
                        + OPENBRACES
                            + BREAKLINE  + TAB + VALIDATOR + COLON
                                + OPENBRACES
                                    + BREAKLINE + TABL2 + JSONSCHEMA + COLON + SPACE
                                        + OPENBRACES
                                            + BREAKLINE + TABL3 + BSONTYPE + COLON + SPACE + TYPEOBJECT + COMMA + BREAKLINE
                                            + TABL3 + generateJSONSchemaInstructions(objectCell)
                                        + CLOSEBRACES
                                + BREAKLINE + TAB + CLOSEBRACES
                            + COMMA + BREAKLINE
                            + TAB +VALIDATIONLEVEL + COLON + SPACE + QUOTATIONMARK + mongoActionLevel + QUOTATIONMARK
                            + COMMA + BREAKLINE
                            + TAB + VALITATIONACTION + COLON + SPACE + QUOTATIONMARK + mongoValitationLevel + QUOTATIONMARK + BREAKLINE
                        + CLOSEBRACES
                + CLOSEPARENTHESES + SEMICOLON + BREAKLINE + BREAKLINE;

        return newColletionInstruction;
    }


    private String generateJSONSchemaInstructions(mxCell objectCell) {
        //        addCollectionAttributes (aka)
        return "";
    }
}
