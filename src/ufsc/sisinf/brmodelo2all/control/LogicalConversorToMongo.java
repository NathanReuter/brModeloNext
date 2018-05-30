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

import javax.swing.*;

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


// TODO GENERATE INSTRUCTION FOR DISJUNCTION

public class LogicalConversorToMongo {

    private final ModelingComponent logicalModelingComponent;
    private final NoSqlEditor sqlEditor;
    private NosqlConfigurationData configData;
    private List<String> listWithRequired = new ArrayList<String>();
//    CodeBuilder KEYWORDS
    static final String SPACE = " ";
    static final String TAB = "  ";
    static final String TABL2 = TAB + TAB;
    static final String TABL3 = TABL2 + TAB;
    static final String TABL4 = TABL3 + TAB;
    static final String TABL5 = TABL4 + TAB;
    static final String COMMA = ", ";
    static final String SEMICOLON = ";";
    static final String COLON = ":";
    static final String BREAKLINE = "\n";
    static final String OPENBRACES = "{";
    static final String CLOSEBRACES = "}";
    static final String OPENPARENTHESES = "(";
    static final String CLOSEPARENTHESES = ")";
    static final String OPENBRACKETS = "[";
    static final String CLOSEBRACKTS = "]";
    static final String CREATECOLLECTIONCOMMAND = "db.createCollection";
    static final String VALIDATOR = "validator";
    static final String QUOTATIONMARK = mxResources.get("quotationMark");
    static final String SELECTDB = "use";
    static final String JSONSCHEMA = "$jsonSchema";
    static final String VALITATIONACTION = "validationAction";
    static final String VALIDATIONLEVEL = "validationLevel";
    static final String BSONTYPE = "bsonType";
    static final String TYPEOBJECT = "object";
    static final String TYPEOBJECTID = "objectId";
    static final String PROPERTIES = "properties";
    private String jsonSchemaIntruction = "";
    //    ConfigVariables
    private String dbName;
    private String mongoActionLevel;
    private String mongoValitationLevel;
    private Boolean oneSingleCollection;

    public LogicalConversorToMongo(final ModelingComponent logicalModelingComponent, final NoSqlEditor sqlEditor) {
        this.logicalModelingComponent = logicalModelingComponent;
        this.sqlEditor = sqlEditor;
        configData = NosqlConfigurationData.getInstance();
    }

    private void updateConfigVariables () {
        dbName = configData.getDbName();
        mongoActionLevel = configData.getMongoValidationActions().toLowerCase();
        mongoValitationLevel = configData.getMongoValidationLevel().toLowerCase();
        oneSingleCollection = configData.isMongoIsUniqueCollection();
    }

    public void convertModeling() {
        updateConfigVariables();
        mxRectangle rect = logicalModelingComponent.getGraph().getGraphBounds();
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        Rectangle ret = new Rectangle(x + 60000, y + 60000);
        Object[] cells = logicalModelingComponent.getCells(ret);
        String instruction = AppConstants.MONGO_HELP_INSTRUCTIONS + BREAKLINE + BREAKLINE + startDB(dbName);

        if (oneSingleCollection) {
            String uniqueCollectionName = JOptionPane.showInputDialog("Digite o nome da coleção única");
            mxCell parrentObjectCell = new mxCell();
            parrentObjectCell.setValue(uniqueCollectionName);

            for (Object cell : cells) {
                if (cell instanceof mxCell) {
                    mxCell objectCell = (mxCell) cell;

                    if (objectCell.getValue() instanceof Collection) {
                        parrentObjectCell.insert((mxCell) cell);
                    }

                }
            }

            instruction += addColletion(parrentObjectCell);
        } else {
            for (Object cell : cells) {
                if (cell instanceof mxCell) {
                    mxCell objectCell = (mxCell) cell;

                    if (objectCell.getValue() instanceof Collection) {
                        instruction += addColletion(objectCell);
                    }

                }
            }
        }

        sqlEditor.insertSqlInstruction(instruction);
    }

    private String startDB(String name) {
        return  SELECTDB.concat(" ").concat(name).concat(BREAKLINE);
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
                                            + TABL3 + generateJSONSchemaInstructions(objectCell, TABL3)
                                        + BREAKLINE + TABL2 + CLOSEBRACES
                                + BREAKLINE + TAB + CLOSEBRACES
                            + COMMA + BREAKLINE
                            + TAB + VALITATIONACTION + COLON + SPACE + QUOTATIONMARK + mongoActionLevel + QUOTATIONMARK
                            + COMMA + BREAKLINE
                            + TAB + VALIDATIONLEVEL + COLON + SPACE + QUOTATIONMARK + mongoValitationLevel + QUOTATIONMARK + BREAKLINE
                        + CLOSEBRACES
                + CLOSEPARENTHESES + SEMICOLON + BREAKLINE + BREAKLINE;

        return newColletionInstruction;
    }


    private String generateJSONSchemaInstructions(mxCell objectCell, String INDENT) {
        String initialTemplete = PROPERTIES + COLON + OPENBRACES
                    + getCellChild(objectCell, INDENT)
                + BREAKLINE + INDENT + CLOSEBRACES + COMMA
                + generateRequeredObjectsInstruction(listWithRequired, INDENT)
                + generateRequiredDisjunctionInstructions(objectCell, INDENT);
//        Clean instructions
        jsonSchemaIntruction = "";

        return initialTemplete;
    }

    private String getCellChild(mxICell objectCell, String INDENT) {
        Collection block;
        /*Para cada celula filha */
        for (int i = 0; i < objectCell.getChildCount(); i++) {
//            SE FOR COLEÇÃO ou Bloco
            if (objectCell.getChildAt(i).getValue() instanceof Collection) {
                block = (Collection) objectCell.getChildAt(i).getValue();
                block.setDisjunction(false);
                if (!oneSingleCollection) {
                    ((Collection) objectCell.getValue()).setDisjunction(false);
                }

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

        checkChildCardinality(objectCell, INDENT);

        return jsonSchemaIntruction;
    }

    private void checkChildCardinality(mxICell objectCell, String INDENT) {
//        Para cada filho
        for (int i = 0; i < objectCell.getChildCount(); i++) {
            if (objectCell.getChildAt(i).getValue() instanceof NoSqlAttributeObject) {
                NoSqlAttributeObject attribute = (NoSqlAttributeObject) objectCell.getChildAt(i).getValue();
                cardinalitiesCases(attribute.getMinimumCardinality(), attribute.getMaximumCardinality(),
                        objectCell.getChildAt(i), INDENT);
            }

            if (objectCell.getChildAt(i).getValue() instanceof Collection) {
                Collection block = (Collection) objectCell.getChildAt(i).getValue();
                cardinalitiesCases(block.getMinimumCardinality(), block.getMaximumCardinality(),
                        objectCell.getChildAt(i), INDENT);
            }
        }
    }

    private void cardinalitiesCases(char minimum, char maximum, mxICell objectCell, String INDENT) {
        if (objectCell.getValue() instanceof NoSqlAttributeObject) {
            if (((NoSqlAttributeObject) objectCell.getValue()).isIdentifierAttribute() || ((NoSqlAttributeObject) objectCell.getValue()).isReferenceAttribute()) {
                if (objectCell.equals(objectCell.getParent().getChildAt(objectCell.getParent().getChildCount() - 1))) {
                    addToRequiredList(objectCell);
                }
            }
        }

        if (minimum == '1' && maximum == '1') {
            // (1,1)
            if (objectCell.getValue() instanceof Collection) {
                addBlock(objectCell, INDENT);
            }

            if (objectCell.getValue() instanceof NoSqlAttributeObject){
                addAttribute(objectCell, INDENT);
            }
        } else if (minimum == '1' && maximum != '1' && maximum != 'n') {
            // (1,n) n == number
            if (objectCell.getValue() instanceof Collection)
                addBlockWithArray(objectCell, INDENT);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                addAttributeWithArray(objectCell, INDENT);
        } else if (minimum == '0' && maximum == '1') {
            // (0,1)
            if (objectCell.getValue() instanceof Collection)
                addBlock(objectCell, INDENT);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                addAttribute(objectCell, INDENT);
        } else if (minimum == '0' && maximum != '1' && maximum != 'n') {
            // (0,n) n == number
            if (objectCell.getValue() instanceof Collection)
                addBlockWithArray(objectCell, INDENT);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                addAttributeWithArray(objectCell, INDENT);
        } else if ((minimum == '1' && maximum == 'n') || (minimum == '0' && maximum == 'n')) {
            if (objectCell.getValue() instanceof Collection) {
                addBlockWithArray(objectCell, INDENT);
            }
            if (objectCell.getValue() instanceof NoSqlAttributeObject) {
                addAttributeWithArray(objectCell, INDENT);
            }
        }

        if (objectCell.equals(getLastChild(objectCell))) {
            addToRequiredList(objectCell);
        }
    }

    public String filterType (String type) {
        if (type.equalsIgnoreCase("integer")) {
            return "number";
        } else if (type.equalsIgnoreCase("boolean")) {
            return "bool";
        }

        return type.toLowerCase();
    }

    private void addAttribute(mxICell objectCell, String INDENT) {
        NoSqlAttributeObject attributeObject = (NoSqlAttributeObject) objectCell.getValue();
        String attributeName = objectCell.getValue().toString();
        String attributeType = filterType(attributeObject.getType());

        if (attributeObject.isIdentifierAttribute()) {
            attributeType = TYPEOBJECTID;
            attributeName = "_" + attributeObject.getName();

        } else if (attributeObject.isReferenceAttribute()) {
            attributeType = TYPEOBJECTID;
        }

                jsonSchemaIntruction += BREAKLINE + INDENT + TAB + attributeName + COLON + SPACE
                + OPENBRACES
                    + SPACE + BSONTYPE + COLON + SPACE + QUOTATIONMARK
                    + attributeType + QUOTATIONMARK
                + CLOSEBRACES + COMMA;
    }

    private void addAttributeWithArray(mxICell objectCell, String INDENT) {
        NoSqlAttributeObject attributeObject = (NoSqlAttributeObject) objectCell.getValue();
        String maximumLine = attributeObject.getMaximumCardinality() == 'n' ? "" : BREAKLINE + INDENT + TABL2  + "maxItems" + COLON + attributeObject.getMaximumCardinality() + COMMA;
        String attributeType = (attributeObject.isIdentifierAttribute() || attributeObject.isReferenceAttribute())
                ? TYPEOBJECTID : filterType(attributeObject.getType());;

        jsonSchemaIntruction += BREAKLINE + INDENT + TAB + objectCell.getValue().toString()  + COLON + SPACE
                + OPENBRACES
                    + BREAKLINE + INDENT + TABL2 +  BSONTYPE  + COLON + SPACE + QUOTATIONMARK + "array" + QUOTATIONMARK + COMMA
                    + BREAKLINE + INDENT + TABL2  + "minItems"  + COLON + attributeObject.getMinimumCardinality() + COMMA
                    + maximumLine
                    + BREAKLINE + INDENT + TABL2 + "items" + COLON
                        + OPENBRACES
                        + BREAKLINE + INDENT + TABL3 + BSONTYPE
                        + COLON + SPACE + QUOTATIONMARK + attributeType + QUOTATIONMARK
                        + BREAKLINE + INDENT + TABL2
                    + CLOSEBRACES
                    + BREAKLINE
                + INDENT + TAB + CLOSEBRACES + COMMA;
    }

    private void addBlockWithArray(mxICell objectCell, String INDENT) {
        Collection block = (Collection) objectCell.getValue();
        String maximumLine = block.getMaximumCardinality() == 'n' ? "" : BREAKLINE + TABL5 + "maxItems"  + COLON + block.getMaximumCardinality() + COMMA;

        jsonSchemaIntruction += BREAKLINE + INDENT + TAB + objectCell.getValue().toString() + COLON + SPACE
                + OPENBRACES
                + BREAKLINE + INDENT + TABL2 + BSONTYPE  + COLON + SPACE + QUOTATIONMARK + "array" + QUOTATIONMARK+ COMMA
                + BREAKLINE + INDENT + TABL2 + "minItems"  + COLON + block.getMinimumCardinality() + COMMA
                + maximumLine
                + BREAKLINE + INDENT + TABL2 + "items" + COLON + OPENBRACKETS
                    + BREAKLINE + INDENT + TABL2 + OPENBRACES
                    + BREAKLINE + INDENT + TABL3 +  BSONTYPE + COLON + SPACE + QUOTATIONMARK + "object" + QUOTATIONMARK + COMMA
                    + BREAKLINE + INDENT + TABL3 + "properties"
                    + COLON + OPENBRACES;

            getCellChild(objectCell, INDENT + TABL3);
            jsonSchemaIntruction +=  BREAKLINE + INDENT + TABL3 + CLOSEBRACES + COMMA;


            if (!listWithRequired.isEmpty()) {
                jsonSchemaIntruction += generateRequeredObjectsInstruction(listWithRequired, INDENT + TABL2);
            } else if (((Collection) objectCell.getValue()).getDisjunction()) {
                jsonSchemaIntruction += generateRequiredDisjunctionInstructions(objectCell, INDENT + TABL2);
            }

            jsonSchemaIntruction += BREAKLINE + INDENT + TABL3  + "additionalProperties"  + " : false" + COMMA
                    + BREAKLINE + INDENT + TABL2 + CLOSEBRACES
                    + BREAKLINE + INDENT + TAB + CLOSEBRACKTS + COMMA
                    + BREAKLINE + INDENT + TAB + CLOSEBRACES + COMMA;
    }

    private void addBlock(mxICell objectCell, String INDENT) {
        jsonSchemaIntruction += BREAKLINE + INDENT + TAB + objectCell.getValue().toString() + COLON + SPACE
                + OPENBRACES
                    + BREAKLINE + INDENT + TABL2  + BSONTYPE + COLON + SPACE + QUOTATIONMARK + "object" + QUOTATIONMARK + COMMA
                + BREAKLINE  + INDENT + TABL2  + "properties"  + COLON + OPENBRACES;
        // If the block has child encapsulate the block or attribute inside this
        // block.
        getCellChild(objectCell, INDENT + TABL2 );
        jsonSchemaIntruction += BREAKLINE + INDENT + TABL2 + CLOSEBRACES + COMMA;

        if (!listWithRequired.isEmpty()) {
            jsonSchemaIntruction += generateRequeredObjectsInstruction(listWithRequired, INDENT + TABL2);
        } else if (((Collection) objectCell.getValue()).getDisjunction()) {
            jsonSchemaIntruction += generateRequiredDisjunctionInstructions(objectCell, INDENT + TABL2);
        }

        jsonSchemaIntruction += BREAKLINE  + INDENT + TABL2 + "additionalProperties" + " : false" + COMMA
                + BREAKLINE + INDENT + TAB + CLOSEBRACES + COMMA;
    }

    private void addToList(List<String> list, String value) {
        if (!list.contains(value)) {
            list.add(value);
        }
    }

    private void addToRequiredList(mxICell objectCell) {
        Collection childBlock;
        for (int i = 0; i < objectCell.getParent().getChildCount(); i++) {
            if (objectCell.getParent().getChildAt(i).getValue() instanceof Collection) {
                childBlock = (Collection) objectCell.getParent().getChildAt(i).getValue();
                if ((childBlock.getMinimumCardinality() == 49 && !childBlock.getDisjunction()))
                    addToList(listWithRequired, objectCell.getParent().getChildAt(i).getValue().toString());
            }
            if (objectCell.getParent().getChildAt(i).getValue() instanceof NoSqlAttributeObject) {
                NoSqlAttributeObject attribute = (NoSqlAttributeObject) objectCell.getParent().getChildAt(i).getValue();
                if ((attribute.getMinimumCardinality() == 49)) {
                    addToList(listWithRequired,
                            (attribute.isIdentifierAttribute() ? "_" : "") +
                                    ((NoSqlAttributeObject) objectCell.getParent().getChildAt(i).getValue()).getName());
                }
            }
        }
    }

    private String generateRequeredObjectsInstruction(List<String> requiredList, String INDENT) {
        String instruction = BREAKLINE + INDENT + "required" + COLON + SPACE + "[";

        for (int i = 0; i < listWithRequired.size(); i++) {
            if (i != listWithRequired.size() - 1) {
                instruction += QUOTATIONMARK + listWithRequired.get(i) + QUOTATIONMARK + COMMA;
            } else {
                instruction += QUOTATIONMARK + listWithRequired.get(i) + QUOTATIONMARK;
            }
        }

        instruction += "]" + COMMA;
        requiredList.clear();

        return instruction;
    }

    private mxICell getLastChild(mxICell objectCell) {
        int lastChild = objectCell.getParent().getChildCount() - 1;

        while (((ModelingObject) objectCell.getParent().getChildAt(lastChild).getValue()).getClass()
                .equals(DisjunctionObject.class)) {
            lastChild--;
        }

        return objectCell.getParent().getChildAt(lastChild);
    }

    private String generateRequiredDisjunctionInstructions(mxICell objectCell, String INDENT) {
        List<Object> collectionChild = new ArrayList<Object>();
        String instruction = "";

        for (int i = 0; i < objectCell.getChildCount(); i++) {
            collectionChild.add(objectCell.getChildAt(i));
        }

        for (Object child : collectionChild) {
            if (((mxICell) child).getValue() instanceof DisjunctionObject) {
                instruction += BREAKLINE + INDENT + "oneOf" + " : [" + BREAKLINE;

                for (Object disjunctionChild : ((DisjunctionObject) ((mxICell) child).getValue()).getChildList()) {
                    instruction += INDENT + TAB + OPENBRACES  + "required" + " : [" + QUOTATIONMARK
                            + disjunctionChild.toString() + QUOTATIONMARK + "]" + CLOSEBRACES + COMMA + BREAKLINE;
                }

                instruction += INDENT + "]" + COMMA;
            }
        }

        return instruction;
    }
}
