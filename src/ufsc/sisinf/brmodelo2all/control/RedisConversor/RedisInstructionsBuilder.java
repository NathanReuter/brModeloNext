package ufsc.sisinf.brmodelo2all.control.RedisConversor;
import ufsc.sisinf.brmodelo2all.control.NosqlConfigurationData;
import ufsc.sisinf.brmodelo2all.util.AppConstants;

import java.util.List;

public class RedisInstructionsBuilder {
    private NosqlConfigurationData configData;

    /* Main Redis keyword*/
    static final String LOCAL = "local";
    static final String FUNCTION = "function";
    static final String IF = "if";
    static final String ELSEIF = "elseif";
    static final String THEN = "then";
    static final String END = "end";
    static final String TRUE = "true";
    static final String RETURN = "return";


    /* Language Tokens*/
    static final String COMMA = ", ";
    static final String BREAKLINE = "\n";
    static final String OPENBRACES = "{";
    static final String CLOSEBRACES = "}";
    static final String OPENPARENTHESES = "(";
    static final String CLOSEPARENTHESES = ")";
    static final String SPACE = " ";
    static final String EQUAL = "=";
    static final String ONEQUOTE = "'";
    static final String TAB = "  ";


    /* ConfigVariables */
    private String dbName;

    public RedisInstructionsBuilder() {
        this.configData = NosqlConfigurationData.getInstance();
        dbName = configData.getDbName();
    }

    private String surroundWithQuotes(String word) {
        return ONEQUOTE + word + ONEQUOTE;
    }
    private String surroundWithChevron(String word) {
        return OPENBRACES + word + CLOSEBRACES;
    }
    private String surroundWithParentheses(String word) {
        return OPENPARENTHESES + word + CLOSEPARENTHESES;
    }

    public String genInitialSupportInstructions () {
        return AppConstants.Redis_HELP_INSTRUCTIONS + BREAKLINE + BREAKLINE +

                "local function split(inputstr, sep)\n" +
                "        if sep == nil then\n" +
                "                sep = \"%s\"\n" +
                "        end\n" +
                "        local t={} ; local i=1\n" +
                "        for str in string.gmatch(inputstr, \"([^\"..sep..\"]+)\") do\n" +
                "                t[i] = str\n" +
                "                i = i + 1\n" +
                "        end\n" +
                "        return t\n" +
                "end\n" +
                "\n" +
                "local function findCollection(colToInsert)\n" +
                "\tlocal colName = split(colToInsert, \":\")[1]\n" +
                "\treturn colName\n" +
                "end\n" +
                "\n" +
                "local function CheckRequiredAttributes(colection_required, length)\n" +
                "\tlocal isCorrect = true\n" +
                "\t\n" +
                "\tif (table.getn(KEYS) - 1  < length) then\n" +
                "\t\treturn false\n" +
                "\tend\n" +
                "\n" +
                " \tfor _,key in ipairs(KEYS) do \n" +
                " \t\tif (_ > 1) then\n" +
                " \t\t\tif (not colection_required[key]) then\n" +
                " \t\t\t\tisCorrect = false\n" +
                " \t\t\tend\n" +
                " \t\tend\n" +
                " \tend\n" +
                "\n" +
                "\treturn isCorrect\n" +
                "end" +
                "\n" +
                "\n" +
                "local function prettyPrinter(result)\n" +
                "  if (result) then \n" +
                "    return 'Schema is Correct'\n" +
                "  end\n" +
                "\n" +
                "  return 'Error in validation'\n" +
                "end" + BREAKLINE + BREAKLINE;
    }

    private String genRequirementsList(List<String> attributes) {
        String instructions = "";

        for (String att : attributes) {
            instructions += att + SPACE + EQUAL + SPACE + TRUE + COMMA + SPACE;
        }

        return instructions;
    }

    private String genIndividualCases(List<RedisObjectData> objectsData) {
        String instruction = "";
        Boolean isFirst = true;

        for (RedisObjectData objectData : objectsData) {
            if (isFirst) {
              instruction = TAB + IF + SPACE + surroundWithParentheses("colName == " + surroundWithQuotes(objectData.getObjectName())) + SPACE + THEN + SPACE + BREAKLINE
                + TAB + TAB + LOCAL + SPACE + "required" + SPACE + EQUAL + SPACE + surroundWithChevron(genRequirementsList(objectData.getAttributes())) + BREAKLINE
                      + TAB + TAB + RETURN + SPACE + "CheckRequiredAttributes" + surroundWithParentheses("required" + COMMA + objectData.getAttributes().size())
                + BREAKLINE;
              isFirst = false;
            } else {
                instruction += TAB + ELSEIF + SPACE + surroundWithParentheses("colName == " + surroundWithQuotes(objectData.getObjectName())) + SPACE + THEN + SPACE + BREAKLINE
                        + TAB + TAB + LOCAL + SPACE + "required" + SPACE + EQUAL + SPACE + surroundWithChevron(genRequirementsList(objectData.getAttributes())) + BREAKLINE
                        +  TAB + TAB + RETURN + SPACE + "CheckRequiredAttributes" + surroundWithParentheses("required" + COMMA + objectData.getAttributes().size())
                        + BREAKLINE;
            }

        }
        instruction += TAB + END;

        return instruction;
    }

    public String genCheckCollectionsCase(List<RedisObjectData> objectsData) {
        return LOCAL + SPACE + FUNCTION + SPACE +  "checkCollectionCase" + SPACE + surroundWithParentheses("colName") + BREAKLINE
                + genIndividualCases(objectsData)
                + BREAKLINE + END + BREAKLINE + BREAKLINE;
    }

    public String genExecutionInstruction() {
        return RETURN + SPACE + "prettyPrinter(checkCollectionCase(findCollection(KEYS[1])));";
    }
}
