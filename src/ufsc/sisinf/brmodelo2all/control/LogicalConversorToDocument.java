package ufsc.sisinf.brmodelo2all.control;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import ufsc.sisinf.brmodelo2all.model.ModelingComponent;
import ufsc.sisinf.brmodelo2all.model.objects.Collection;
import ufsc.sisinf.brmodelo2all.model.objects.DisjunctionObject;
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
    static final String TABL4 = TABL3 + TAB;
    static final String TABL5 = TABL4 + TAB;
    static final String TABL6 = TABL5 + TAB;
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
    static final String TYPE = "type";
    static final String EXISTS = "$exists";
    static final String ALL = "$all";
    static final String TRUE = "true";
    static final String FALSE = "false";
    static final String JSONSCHEMA = "$jsonSchema";
    static final String VALITATIONACTION = "validationAction";
    static final String VALIDATIONLEVEL = "validationLevel";
    static final String BSONTYPE = "bsonType";
    static final String TYPEOBJECT = "object";
    static final String PROPERTIES = "properties";
    private String jsonSchemaIntruction = "";
    private String mongoActionLevel = AppConstants.MONGO_DEFAULT_ACTION_LEVEL;
    private String mongoValitationLevel = AppConstants.MONGO_DEFAULT_VALIDATION_LEVEL;
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
        String instruction = startDB("NovoDB");

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
                                            + BREAKLINE + TABL3 + BSONTYPE + COLON + SPACE + QUOTATIONMARK + TYPEOBJECT + QUOTATIONMARK +  COMMA + BREAKLINE
                                            + TABL3 + generateJSONSchemaInstructions(objectCell)
                                        + BREAKLINE + CLOSEBRACES
                                + BREAKLINE + TAB + CLOSEBRACES
                            + COMMA + BREAKLINE
                            + TAB + VALITATIONACTION + COLON + SPACE + QUOTATIONMARK + mongoActionLevel + QUOTATIONMARK
                            + COMMA + BREAKLINE
                            + TAB + VALIDATIONLEVEL + COLON + SPACE + QUOTATIONMARK + mongoValitationLevel + QUOTATIONMARK + BREAKLINE
                        + CLOSEBRACES
                + CLOSEPARENTHESES + SEMICOLON + BREAKLINE + BREAKLINE;

        return newColletionInstruction;
    }


    private String generateJSONSchemaInstructions(mxCell objectCell) {
        //        addCollectionAttributes (aka)

        String initialTemplete = PROPERTIES + COLON + OPENBRACES
                    + getCellChild(objectCell)
                + CLOSEBRACES;
        return initialTemplete;
    }

    private String getCellChild(mxICell objectCell) {
        Collection block;
        /*Para cada celula filha */
        for (int i = 0; i < objectCell.getChildCount(); i++) {
//            SE FOR COLEÇÃO ou Bloco
            if (objectCell.getChildAt(i).getValue() instanceof Collection) {
                block = (Collection) objectCell.getChildAt(i).getValue();
                block.setDisjunction(false);
                ((Collection) objectCell.getValue()).setDisjunction(false);
            }

//          Se for tipo disjunção
            if (objectCell.getChildAt(i).getValue() instanceof DisjunctionObject) {
                for (Collection childOfDisjunction : ((DisjunctionObject) objectCell.getChildAt(i).getValue())
                        .getChildList()) {
                    childOfDisjunction.setDisjunction(true);
                }
                ((Collection) objectCell.getValue()).setDisjunction(true);
            }
        }

        checkChildCardinality(objectCell);

        return jsonSchemaIntruction;
    }

    private void checkChildCardinality(mxICell objectCell) {
//        Para cada filho
        for (int i = 0; i < objectCell.getChildCount(); i++) {
            if (objectCell.getChildAt(i).getValue() instanceof NoSqlAttributeObject) {
                NoSqlAttributeObject attribute = (NoSqlAttributeObject) objectCell.getChildAt(i).getValue();
                cardinalitiesCases(attribute.getMinimumCardinality(), attribute.getMaximumCardinality(),
                        objectCell.getChildAt(i));
            }

            if (objectCell.getChildAt(i).getValue() instanceof Collection) {
                Collection block = (Collection) objectCell.getChildAt(i).getValue();
                cardinalitiesCases(block.getMinimumCardinality(), block.getMaximumCardinality(),
                        objectCell.getChildAt(i));
            }
        }
    }

    private void cardinalitiesCases(char minimum, char maximum, mxICell objectCell) {
        // Caso seja um atributo Identificador, (ID), nao escreva nada.
        if (objectCell.getValue() instanceof NoSqlAttributeObject) {
            if (((NoSqlAttributeObject) objectCell.getValue()).isIdentifierAttribute()) {
                // Obs
                if (objectCell.equals(objectCell.getParent().getChildAt(objectCell.getParent().getChildCount() - 1)))
//                    addToRequiredList(objectCell);
                return;
            }
            if (((NoSqlAttributeObject) objectCell.getValue()).isReferenceAttribute()) {
                // Obs
                if (objectCell.equals(objectCell.getParent().getChildAt(objectCell.getParent().getChildCount() - 1)))
//                    addToRequiredList(objectCell);
                addAttributeRef((NoSqlAttributeObject) objectCell.getValue());
                return;
            }
        }

        if (minimum == '1' && maximum == '1') {
            // (1,1)
            if (objectCell.getValue() instanceof Collection) {
                addBlockWithArray(objectCell);
            }

            if (objectCell.getValue() instanceof NoSqlAttributeObject){
                addAttributeWithArray(objectCell);
            }
        } else if (minimum == '1' && maximum != '1' && maximum != 'n') {
            // (1,n) n == number
            if (objectCell.getValue() instanceof Collection)
                addBlock(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                addAttributeWithArray(objectCell);
        } else if (minimum == '0' && maximum == '1') {
            // (0,1)
            if (objectCell.getValue() instanceof Collection)
                addBlockWithArray(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                addAttributeWithArray(objectCell);
        } else if (minimum == '0' && maximum != '1' && maximum != 'n') {
            // (0,n) n == number
            if (objectCell.getValue() instanceof Collection)
                addBlock(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                addAttributeWithArray(objectCell);
        } else if (minimum == '1' && maximum == 'n') {
            if (objectCell.getValue() instanceof Collection)
                addBlock(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                addAttribute(objectCell);
        } else if (minimum == '0' && maximum == 'n') {
            if (objectCell.getValue() instanceof Collection)
                addBlock(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                addAttribute(objectCell);
        }
    }

    public void addAttribute(mxICell objectCell) {

    }

    public void addAttributeRef(NoSqlAttributeObject objectCell) {

    }

    public void addAttributeWithArray(mxICell objectCell) {
        NoSqlAttributeObject attributeObject = (NoSqlAttributeObject) objectCell.getValue();

        jsonSchemaIntruction += BREAKLINE + TABL4 + objectCell.getValue().toString()  + COLON + SPACE
                + OPENBRACES
                    + BREAKLINE + TABL5 +  BSONTYPE  + COLON + SPACE + QUOTATIONMARK + "array" + QUOTATIONMARK + COMMA
                    + BREAKLINE + TABL5  + "minimum"  + COLON + attributeObject.getMinimumCardinality() + COMMA
                    + BREAKLINE + TABL5  + "maximum" + COLON + attributeObject.getMaximumCardinality() + COMMA
                    + BREAKLINE + TABL5 + "items" + COLON
                        + OPENBRACES
                        + BREAKLINE + TABL6 + TYPE
                        + COLON + SPACE + QUOTATIONMARK + attributeObject.getType() + QUOTATIONMARK + BREAKLINE + TABL5
                    + CLOSEBRACES
                    + BREAKLINE
                + TABL4 + CLOSEBRACES + COMMA;
    }

    public void addBlockWithArray(mxICell objectCell) {
        Collection block = (Collection) objectCell.getValue();

        jsonSchemaIntruction += BREAKLINE + TABL4 + objectCell.getValue().toString() + COLON + SPACE
                + OPENBRACES
                + blockIdIntruction(objectCell)

                + BREAKLINE + TABL5 + TYPE  + COLON + SPACE + QUOTATIONMARK + "array" + QUOTATIONMARK+ COMMA
                + BREAKLINE + TABL5 + "minimum"  + COLON + block.getMinimumCardinality() + COMMA
                + BREAKLINE + TABL5 + "maximum"  + COLON + block.getMaximumCardinality() + COMMA
                + BREAKLINE + TABL5 + "items" + COLON + OPENBRACKETS
                    + BREAKLINE + TABL5 + OPENBRACES
                    + BREAKLINE + TABL6 + TAB+  TYPE + COLON + SPACE + QUOTATIONMARK + "object" + QUOTATIONMARK + COMMA
                    + BREAKLINE + TABL6 + "properties"
                    + COLON + OPENBRACES;

            getCellChild(objectCell);
            jsonSchemaIntruction +=  BREAKLINE + TABL6 + CLOSEBRACES + COMMA;
    //        if (listWithRequired.size() > 0)
    //            requiredObjects();
    //        if (((Collection) objectCell.getValue()).getDisjunction())
    //            requiredForDisjunction(objectCell);

            jsonSchemaIntruction += BREAKLINE + TABL5  + "additionalProperties"  + " : false" + COMMA
                    + BREAKLINE + TABL5 + CLOSEBRACES
                    + BREAKLINE + TABL4 + CLOSEBRACKTS + COMMA
                    + BREAKLINE + TABL4 + CLOSEBRACES + COMMA;
    }

    public void addBlock(mxICell objectCell) {

    }

    public String blockIdIntruction(mxICell objectCell) {
        if (objectCell.getChildCount() > 0) {
            for (int i = 0; i < objectCell.getChildCount(); i++) {
                if (objectCell.getChildAt(i).getValue() instanceof NoSqlAttributeObject) {
                    NoSqlAttributeObject attribute = (NoSqlAttributeObject) objectCell.getChildAt(i).getValue();
                    if (attribute.isIdentifierAttribute())
                        return BREAKLINE + QUOTATIONMARK + "id" + QUOTATIONMARK + SPACE + COLON + SPACE
                            + QUOTATIONMARK + "#"
                            + ((NoSqlAttributeObject) objectCell.getChildAt(i).getValue()).getName() + QUOTATIONMARK
                            + COMMA;
                }
            }
        }

        return "";
    }
}
