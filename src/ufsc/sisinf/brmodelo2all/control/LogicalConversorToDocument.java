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
    static final String TAB = "    ";
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
//    static final String TYPE = "type";
    static final String COLON = ":";
    static final String QUOTATIONMARK = mxResources.get("quotationMark");
    static final String SELECTDB = "use";
    static final String TYPE = "$type";
    static final String EXISTS = "$exists";
    static final String ALL = "$all";
    static final String TRUE = "true";
    static final String FALSE = "false";
    private String instruction = "";
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
        startDB("NovoDB");
        for (Object cell : cells) {
            if (cell instanceof mxCell) {
                mxCell objectCell = (mxCell) cell;
                if (objectCell.getValue() instanceof Collection)
                    addCollection2(objectCell);
            }
        }
        sqlEditor.insertSqlInstruction(instruction);
    }

    private void startDB(String name) {
        instruction += SELECTDB.concat(" ").concat(name).concat(BREAKLINE);
    }

    private String addCollectionAttributes(mxCell objectCell) {
        return SPACE + VALIDATOR + COLON + BREAKLINE
                + OPENBRACES
                    + SPACE + OR + COLON + SPACE
                        + OPENBRACKETS
                            + getChild(objectCell)
                        + BREAKLINE + CLOSEBRACKTS
                + CLOSEBRACES;
    }

    private void addCollection2(mxCell objectCell) {
        instruction += CREATECOLLECTIONCOMMAND
                + OPENPARENTHESES
                    + QUOTATIONMARK + objectCell.getValue() + QUOTATIONMARK + COMMA + BREAKLINE
                        + OPENBRACES
                            + addCollectionAttributes(objectCell)
                        + CLOSEBRACES
                + CLOSEPARENTHESES + SEMICOLON + BREAKLINE + BREAKLINE;
    }

    private void addCollection(mxCell objectCell) {
        instruction += OPENBRACES;
        instruction += BREAKLINE + QUOTATIONMARK + TYPE + QUOTATIONMARK + COLON + SPACE + QUOTATIONMARK + "object"
                + QUOTATIONMARK + COMMA + BREAKLINE + QUOTATIONMARK + "properties" + QUOTATIONMARK + COLON + OPENBRACES
                + BREAKLINE + QUOTATIONMARK + objectCell.getValue() + QUOTATIONMARK + COLON + SPACE + OPENBRACES;
        getId(objectCell);
        instruction += BREAKLINE + QUOTATIONMARK + TYPE + QUOTATIONMARK + COLON + SPACE + QUOTATIONMARK + "object"
                + QUOTATIONMARK + COMMA + BREAKLINE + QUOTATIONMARK + "properties" + QUOTATIONMARK + COLON + OPENBRACES;
        getChild(objectCell);
        instruction += BREAKLINE + CLOSEBRACES + COMMA;
        if (listWithRequired.size() > 0)
            requiredObjects();
        if (((Collection) objectCell.getValue()).getDisjunction())
            requiredForDisjunction(objectCell);
        instruction += BREAKLINE + CLOSEBRACES + BREAKLINE + CLOSEBRACES + BREAKLINE + CLOSEBRACES;
    }

    /**
     * Verifica se a celula possui filhos, se possuir, para cada filho verificar
     * se eh do modo collection (Blocos/Colecao) ou NoSqlAttributeObject
     * (Attributos), eh verificado a cardinalidade de cada filho e passado para
     * o metodo cardinalitiesCases, onde sera criado o codigo correspondente para cada
     * cardinalidade do noSQL correspondente.
     *
     * @param objectCell
     *            Uma celula do Modelo Logico NoSql
     */
    private String getChild(mxICell objectCell) {
        Collection block;
        String partialInstructions = "";

        if (objectCell.getChildCount() > 0) {
            for (int i = 0; i < objectCell.getChildCount(); i++) {
                if (objectCell.getChildAt(i).getValue() instanceof Collection) {
                    block = (Collection) objectCell.getChildAt(i).getValue();
                    block.setDisjunction(false);
                    ((Collection) objectCell.getValue()).setDisjunction(false);
                }
                if (objectCell.getChildAt(i).getValue() instanceof DisjunctionObject) {
                    for (Collection childOfDisjunction : ((DisjunctionObject) objectCell.getChildAt(i).getValue())
                            .getChildList()) {
                        childOfDisjunction.setDisjunction(true);
                    }
                    ((Collection) objectCell.getValue()).setDisjunction(true);
                }
            }

            for (int i = 0; i < objectCell.getChildCount(); i++) {
                if (objectCell.getChildAt(i).getValue() instanceof NoSqlAttributeObject) {
                    NoSqlAttributeObject attribute = (NoSqlAttributeObject) objectCell.getChildAt(i).getValue();
                    partialInstructions += cardinalitiesCases(attribute.getMinimumCardinality(), attribute.getMaximumCardinality(),
                            objectCell.getChildAt(i));
                }

                if (objectCell.getChildAt(i).getValue() instanceof Collection) {
                    block = (Collection) objectCell.getChildAt(i).getValue();
                    partialInstructions += cardinalitiesCases(block.getMinimumCardinality(), block.getMaximumCardinality(),
                            objectCell.getChildAt(i));
                }
            }
        }

        return partialInstructions;
    }

    /**
     * @param minimum
     *            Valor da cardinalidade minima em char
     * @param maximum
     *            Valor da cardinalidade maxima em char
     * @param objectCell
     *            O objeto para verificar a cardinalidade
     */
    private String cardinalitiesCases(char minimum, char maximum, mxICell objectCell) {
        // Caso seja um atributo Identificador, (ID), nao escreva nada.

        String parcialInstruction = "";

        /*
		 * Obs: Se o objeto atual for o ultimo filho da colecao, busca todos os
		 * blocos e atributos que sao required e adiciona na lista. Se for
		 * utilizado para cada bloco ele ira criar varios required oq nao eh
		 * permitido pelo JSON-Schema.
		 */
//        int lastChild = objectCell.getParent().getChildCount() - 1;
//        while (((ModelingObject) objectCell.getParent().getChildAt(lastChild).getValue()).getClass()
//                .equals(DisjunctionObject.class))
//            lastChild--;
//
//        if (objectCell.equals(objectCell.getParent().getChildAt(lastChild))) {
//            addToRequiredList(objectCell);
//        }

        if (objectCell.getValue() instanceof NoSqlAttributeObject) {
//            if (((NoSqlAttributeObject) objectCell.getValue()).isIdentifierAttribute()) {
                // Obs
//                addToRequiredList(objectCell);
//                if (objectCell.equals(objectCell.getParent().getChildAt(objectCell.getParent().getChildCount() - 1))) {
//                }
//            }
            if (((NoSqlAttributeObject) objectCell.getValue()).isReferenceAttribute()) {
                // Obs
                addToRequiredList(objectCell);
//                if (objectCell.equals(objectCell.getParent().getChildAt(objectCell.getParent().getChildCount() - 1))) {
//                }

                addAttributeRef((NoSqlAttributeObject) objectCell.getValue());

            }
        }

        addToRequiredList(objectCell);

        if (minimum == '1' && maximum == '1') {
            // (1,1)
            if (objectCell.getValue() instanceof Collection)
                addBlockWithArray(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                parcialInstruction = addAttributeWithArray2(objectCell);
        }
        if (minimum == '1' && maximum != '1' && maximum != 'n') {
            // (1,n) n == number
            if (objectCell.getValue() instanceof Collection)
                addBlock(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                parcialInstruction = addAttributeWithArray2(objectCell);
        }
        if (minimum == '0' && maximum == '1') {
            // (0,1)
            if (objectCell.getValue() instanceof Collection)
                addBlockWithArray(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                parcialInstruction = addAttributeWithArray2(objectCell);
        }
        if (minimum == '0' && maximum != '1' && maximum != 'n') {
            // (0,n) n == number
            if (objectCell.getValue() instanceof Collection)
                addBlock(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                parcialInstruction = addAttributeWithArray2(objectCell);
        }
        // (1,n)
        if (minimum == '1' && maximum == 'n') {
            if (objectCell.getValue() instanceof Collection)
                addBlock(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                addAttribute(objectCell);
        }
        // (0,n)
        if (minimum == '0' && maximum == 'n') {
            if (objectCell.getValue() instanceof Collection)
                addBlock(objectCell);
            if (objectCell.getValue() instanceof NoSqlAttributeObject)
                addAttribute(objectCell);
        }

        return parcialInstruction;
    }

    /**
     * O metodo verifica se a cardinalidade minima e igual a 1, se for, e
     * necessario adicionar a lista de objetos que sao obrigados a ter no minimo
     * 1 "required".
     *
     * @param objectCell
     *            objeto que sera verificado se sua cardinalidade e necessario
     *            utilizar o required na conversao No-SQL
     */
    private void addToRequiredList(mxICell objectCell) {
        Collection childBlock;

        for (int i = 0; i < objectCell.getParent().getChildCount(); i++) {
            String ObjectName = objectCell.getParent().getChildAt(i).getValue().toString();

            if (objectCell.getParent().getChildAt(i).getValue() instanceof Collection) {
                childBlock = (Collection) objectCell.getParent().getChildAt(i).getValue();
                if ((childBlock.getMinimumCardinality() == 49 && !childBlock.getDisjunction()) && listWithRequired.contains(ObjectName))
                    listWithRequired.add(ObjectName);
            }

            if (objectCell.getParent().getChildAt(i).getValue() instanceof NoSqlAttributeObject) {
                NoSqlAttributeObject attribute = (NoSqlAttributeObject) objectCell.getParent().getChildAt(i).getValue();
                if ((attribute.getMinimumCardinality() == 49 || attribute.isIdentifierAttribute()) && !listWithRequired.contains(ObjectName)) {
                    listWithRequired.add(ObjectName);
                }
            }
        }
    }

    /**
     * Adiciona o valor do bloco no formato JSON-Schema, se possuir ID, o getId
     * ira adiciona-lo, caso possua algum atributo, ou bloco dentro, o getChild
     * ira adicionar os atributos/blocos que estao dentro atraves da
     * recursividade. Caso algum deles possua cardinalidade minima 1 sera
     * inserido o required verificando se ha algum valor na lista dos requireds.
     *
     * @param objectCell
     *            block with instanceOf Collection
     */
    private void addBlock(mxICell objectCell) {
        instruction += BREAKLINE + QUOTATIONMARK + objectCell.getValue().toString() + QUOTATIONMARK + COLON + SPACE
                + OPENBRACES + BREAKLINE + QUOTATIONMARK + TYPE + QUOTATIONMARK + COLON + SPACE + QUOTATIONMARK
                + "object" + QUOTATIONMARK + COMMA;
        getId(objectCell);
        instruction += BREAKLINE + QUOTATIONMARK + "properties" + QUOTATIONMARK + COLON + OPENBRACES;
        // If the block has child encapsulate the block or attribute inside this
        // block.
        getChild(objectCell);
        instruction += BREAKLINE + CLOSEBRACES + COMMA;
        if (listWithRequired.size() > 0)
            requiredObjects();
        if (((Collection) objectCell.getValue()).getDisjunction())
            requiredForDisjunction(objectCell);
        instruction += BREAKLINE + QUOTATIONMARK + "additionalProperties" + QUOTATIONMARK + " : false" + COMMA;
        instruction += BREAKLINE + CLOSEBRACES + COMMA;

    }

    private void addBlockWithArray(mxICell objectCell) {
        Collection block = (Collection) objectCell.getValue();
        instruction += BREAKLINE + QUOTATIONMARK + objectCell.getValue().toString() + QUOTATIONMARK + COLON + SPACE
                + OPENBRACES;
        getId(objectCell);
        instruction += BREAKLINE + QUOTATIONMARK + TYPE + QUOTATIONMARK + COLON + SPACE + QUOTATIONMARK + "array"
                + QUOTATIONMARK + COMMA + BREAKLINE + QUOTATIONMARK + "minItems" + QUOTATIONMARK + COLON
                + block.getMinimumCardinality() + COMMA + BREAKLINE + QUOTATIONMARK + "maxItems" + QUOTATIONMARK + COLON
                + block.getMaximumCardinality() + COMMA + BREAKLINE + QUOTATIONMARK + "items" + QUOTATIONMARK + COLON
                + "[" + BREAKLINE + OPENBRACES + BREAKLINE + QUOTATIONMARK + TYPE + QUOTATIONMARK + COLON + SPACE
                + QUOTATIONMARK + "object" + QUOTATIONMARK + COMMA + BREAKLINE + QUOTATIONMARK + "properties"
                + QUOTATIONMARK + COLON + OPENBRACES;
        // If the block has child encapsulate the block or attribute inside this
        // block.
        getChild(objectCell);
        instruction += BREAKLINE + CLOSEBRACES + COMMA;
        if (listWithRequired.size() > 0)
            requiredObjects();
        if (((Collection) objectCell.getValue()).getDisjunction())
            requiredForDisjunction(objectCell);
        instruction += BREAKLINE + QUOTATIONMARK + "additionalProperties" + QUOTATIONMARK + " : false" + COMMA;
        instruction += BREAKLINE + CLOSEBRACES + BREAKLINE + "]" + COMMA + BREAKLINE + CLOSEBRACES + COMMA;

    }

    /**
     * Cria a seguinte sequencia nas instrucoes: Nome do objeto que esta vindo
     * (AtributoNoSQL), e o tipo do objeto (string)________________________
     * "AtributoNoSQL": { "type": "string"},
     *
     * @param objectCell
     *            objeto do qual sera identificado o nome e tipo.
     */
    private void addAttribute(mxICell objectCell) {
        NoSqlAttributeObject attributeObject = (NoSqlAttributeObject) objectCell.getValue();
        instruction += BREAKLINE + QUOTATIONMARK + objectCell.getValue().toString() + QUOTATIONMARK + COLON + SPACE
                + OPENBRACES + SPACE + QUOTATIONMARK + TYPE + QUOTATIONMARK + COLON + SPACE + QUOTATIONMARK
                + attributeObject.getType() + QUOTATIONMARK + CLOSEBRACES + COMMA;
    }

    private void addAttributeWithArray(mxICell objectCell) {
        NoSqlAttributeObject attributeObject = (NoSqlAttributeObject) objectCell.getValue();
        instruction += BREAKLINE + QUOTATIONMARK + objectCell.getValue().toString() + QUOTATIONMARK + COLON + SPACE
                + OPENBRACES + BREAKLINE + QUOTATIONMARK + TYPE + QUOTATIONMARK + COLON + SPACE + QUOTATIONMARK
                + "array" + QUOTATIONMARK + COMMA + BREAKLINE + QUOTATIONMARK + "minItems" + QUOTATIONMARK + COLON
                + attributeObject.getMinimumCardinality() + COMMA + BREAKLINE + QUOTATIONMARK + "maxItems"
                + QUOTATIONMARK + COLON + attributeObject.getMaximumCardinality() + COMMA + BREAKLINE + QUOTATIONMARK
                + "items" + QUOTATIONMARK + COLON + OPENBRACES + BREAKLINE + QUOTATIONMARK + TYPE + QUOTATIONMARK
                + COLON + SPACE + QUOTATIONMARK + attributeObject.getType() + QUOTATIONMARK + BREAKLINE + CLOSEBRACES
                + BREAKLINE + CLOSEBRACES + COMMA;
    }

    private String addAttributeWithArray2(mxICell objectCell) {
        NoSqlAttributeObject attributeObject = (NoSqlAttributeObject) objectCell.getValue();
        String existsLine;

        if (listWithRequired.contains(attributeObject.toString())) {
            existsLine =  EXISTS + COLON + SPACE + TRUE;
        } else {
            existsLine =  EXISTS + COLON + SPACE + FALSE;
        }

        return TAB + BREAKLINE + OPENBRACES
                    + attributeObject.toString() + COLON + SPACE + OPENBRACES
                        + TYPE + COLON + SPACE + QUOTATIONMARK + attributeObject.getType() + QUOTATIONMARK + COMMA
                        + existsLine
                    + CLOSEBRACES
                + CLOSEBRACES + COMMA;
    }

    /**
     * Como nao eh possivel colocar varios required no codigo do JSON Schema,
     * foi necessario inserir todos os atributos e blocos que sao de
     * cardinalidade minima 1 em uma lista, para que so no final do bloco ou
     * colecao essa lista de required seja escrita.
     *
     * Enquanto nao for o penultimo da lista, insere uma "," depois do valor.
     * Depois que foi escrito a lista de required inicializa uma lista nova.
     */
    private void requiredObjects() {
        instruction += BREAKLINE + QUOTATIONMARK + "required" + QUOTATIONMARK + COLON + SPACE + "[";
        for (int i = 0; i < listWithRequired.size(); i++) {
            if (i != listWithRequired.size() - 1) {
                instruction += QUOTATIONMARK + listWithRequired.get(i) + QUOTATIONMARK + COMMA;
            } else {
                instruction += QUOTATIONMARK + listWithRequired.get(i) + QUOTATIONMARK;
            }
        }
        instruction += "]" + COMMA;
        listWithRequired = new ArrayList<String>();
    }

    /*
     * Para cada uma dessas colecoes veja se possui um bloco com disjuncao, se
     * possuir insere o oneOf e para cada um dos blocos que possuem disjuncao
     * insere um required e o nome do bloco.
     */
    private void requiredForDisjunction(mxICell objectCell) {
        List<Object> collectionChild = new ArrayList<Object>();
        for (int i = 0; i < objectCell.getChildCount(); i++) {
            collectionChild.add(objectCell.getChildAt(i));
        }
        for (Object child : collectionChild) {
            if (((mxICell) child).getValue() instanceof DisjunctionObject) {
                instruction += BREAKLINE + QUOTATIONMARK + "oneOf" + QUOTATIONMARK + " : [" + BREAKLINE;
                for (Object disjunctionChild : ((DisjunctionObject) ((mxICell) child).getValue()).getChildList()) {
                    instruction += OPENBRACES + QUOTATIONMARK + "required" + QUOTATIONMARK + " : [" + QUOTATIONMARK
                            + disjunctionChild.toString() + QUOTATIONMARK + "]" + CLOSEBRACES + COMMA + BREAKLINE;
                }
                instruction += "]" + COMMA;
            }
        }
    }

    /**
     * Como as refs, s�o sempre atributos, � feita a convers�o para
     * NoSqlAttributeObject sem a necessidade de conferir se � a instancia
     * correta, refValue � o nome do atributoIdentificador ao qual a ref foi
     * criada. Caso a referencia seja do tipo (0,1) ou (1,1) � necess�rio
     * cria-lo como um array, pois � necess�rio um controle sobre a quantida de
     * referencia que ser� usada dentro do atributo, caso contr�rio � criado
     * como uma propriedade.
     *
     * @param objectCell
     *            objeto que ser� verificado se � uma referencia
     */
    private void addAttributeRef(NoSqlAttributeObject objectCell) {
        NoSqlAttributeObject attributeObject = objectCell;
        String refValue = objectCell.getReferencedObject().toString();
        if (refValue.substring(0, 3).equals("ID_"))
            refValue = refValue.substring(3);

        if (attributeObject.getMaximumCardinality() != 'n')
            instruction += BREAKLINE + QUOTATIONMARK + objectCell.toString() + QUOTATIONMARK + COLON + SPACE
                    + OPENBRACES + BREAKLINE + QUOTATIONMARK + "type" + QUOTATIONMARK + COLON + SPACE + QUOTATIONMARK
                    + "array" + QUOTATIONMARK + COMMA + BREAKLINE + QUOTATIONMARK + "minItems" + QUOTATIONMARK + COLON
                    + attributeObject.getMinimumCardinality() + COMMA + BREAKLINE + QUOTATIONMARK + "maxItems"
                    + QUOTATIONMARK + COLON + attributeObject.getMaximumCardinality() + COMMA + BREAKLINE
                    + QUOTATIONMARK + "items" + QUOTATIONMARK + COLON + SPACE + OPENBRACES + BREAKLINE + QUOTATIONMARK
                    + "$ref" + QUOTATIONMARK + COLON + SPACE + QUOTATIONMARK + "#" + pathOfParentsObject((ModelingObject) objectCell) + refValue + QUOTATIONMARK
                    + BREAKLINE + CLOSEBRACES + COMMA + BREAKLINE + CLOSEBRACES + COMMA;
        else
            // (1,n)
            // (0,n)
            instruction += BREAKLINE + QUOTATIONMARK + objectCell.toString() + QUOTATIONMARK + COLON + SPACE
                    + OPENBRACES + BREAKLINE + QUOTATIONMARK + "$ref" + QUOTATIONMARK + COLON + SPACE + QUOTATIONMARK
                    + "#" + pathOfParentsObject((ModelingObject) objectCell) + refValue + QUOTATIONMARK + BREAKLINE + CLOSEBRACES + COMMA;
    }

    private String pathOfParentsObject(ModelingObject objectCell) {
        path = "";
        if(objectCell.getParentObject() != null){
            pathOfParentsObject((ModelingObject) ((mxICell) objectCell.getParentObject()).getValue());
            path += ((mxICell) objectCell.getParentObject()).getValue().toString() + "/";
        }
        return path;
    }

    /**
     * Verifica se o parametro possui um atributo identificador, que eh o id
     * utilizado para referenciar blocos ou colecoes. Se possuir, cria a
     * instrucao referente. Caso o bloco ou colecao nao possua filhos(outros
     * blocos), nao ha motivo para verificar a existencia do ID.
     *
     * @param objectCell
     */
    private void getId(mxICell objectCell) {
        if (objectCell.getChildCount() > 0)
            for (int i = 0; i < objectCell.getChildCount(); i++) {
                if (objectCell.getChildAt(i).getValue() instanceof NoSqlAttributeObject) {
                    NoSqlAttributeObject attribute = (NoSqlAttributeObject) objectCell.getChildAt(i).getValue();
                    if (attribute.isIdentifierAttribute())
                        instruction += BREAKLINE + QUOTATIONMARK + "id" + QUOTATIONMARK + SPACE + COLON + SPACE
                                + QUOTATIONMARK + "#"
                                + ((NoSqlAttributeObject) objectCell.getChildAt(i).getValue()).getName() + QUOTATIONMARK
                                + COMMA;
                }
            }
    }
}
