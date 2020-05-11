package org.incredible.certProcessor.views;

import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.ParserVisitor;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.BaseVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class HTMLTemplateValidator {

    private static Logger logger = LoggerFactory.getLogger(HTMLTemplateValidator.class);

    private Set<String> htmlTemplateVariable;

    /**
     * variables present in html template
     */
    private static Set<String> htmlReferenceVariable = new HashSet<>();


    public HTMLTemplateValidator(String htmlString) {
        this.htmlTemplateVariable = storeAllHTMLTemplateVariables(htmlString);
    }


    public Boolean validate() throws Exception {
        HashSet<String> invalidVariables = new HashSet<>();
        Iterator<String> iterator = htmlTemplateVariable.iterator();
        List<String> allVars = HTMLVars.get();
        while (iterator.hasNext()) {
            String htmlVar = iterator.next();
            if (!allVars.contains(htmlVar)) {
                invalidVariables.add(htmlVar);
            }
        }
        if (invalidVariables.isEmpty()) {
            logger.info("HTML template is valid");
            htmlTemplateVariable.clear();
            return true;
        } else {
            htmlTemplateVariable.clear();
            throw new Exception("HTML template is not valid, due to contains following invalid variables" + invalidVariables);
        }
    }


    /**
     * to check file is  exists or not
     *
     * @param file
     * @return
     */
    public static Boolean isFileExists(File file) {
        boolean isExits = false;
        if (file.exists()) {
            isExits = true;
        } else {
            isExits = false;
        }
        return isExits;
    }


    private static ParserVisitor visitor = new BaseVisitor() {
        @Override
        public Object visit(final ASTReference node, final Object data) {
            htmlReferenceVariable.add(node.literal());
            return null;
        }
    };

    /**
     * to get all the reference variables present in htmlString
     *
     * @param htmlString html file read in the form of string
     * @return set of reference variables
     */
    public static Set<String> storeAllHTMLTemplateVariables(String htmlString) {
        RuntimeInstance runtimeInstance = new RuntimeInstance();
        SimpleNode node = null;
        try {
            node = runtimeInstance.parse(htmlString, null);
        } catch (ParseException e) {
            logger.debug("exception while storing template variables");
        }
        visitor.visit(node, null);
        return htmlReferenceVariable;
    }


}