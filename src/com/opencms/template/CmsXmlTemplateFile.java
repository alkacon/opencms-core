package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;

public class CmsXmlTemplateFile extends A_CmsXmlContent {

    public CmsXmlTemplateFile() throws CmsException {
        registerTag("ELEMENT", CmsXmlTemplateFile.class, "handleElementTag", C_REGISTER_MAIN_RUN);    
    }
    
    public CmsXmlTemplateFile(A_CmsObject cms, String filename) throws CmsException {
        super();
        registerMyTags();
        init(cms, filename);
    }        

    public CmsXmlTemplateFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        registerMyTags();
        init(cms, file);
    }        
    
    private void registerMyTags() {
        registerTag("ELEMENT", CmsXmlTemplateFile.class, "handleElementTag", C_REGISTER_MAIN_RUN);    
    }
    
    public String getXmlDocumentTagName() {
        return "XMLTEMPLATE";
    }
    
    public String getContentDescription() {
        return "OpenCms XML template file";
    }
    
    public Object handleElementTag(Element n, Object callingObject, Object userObj) throws Throwable {
        String tagcontent = n.getAttribute("name");
        return callUserMethod("templateElement", tagcontent, callingObject, userObj); 
    }        
    
    public String getSubtemplateClass(String name) {
        String className = getDataValue("ELEMENTDEF." + name + ".CLASS");
        return className;
    }

    public String getSubtemplateFilename(String name) {
        String className = getDataValue("ELEMENTDEF." + name + ".TEMPLATE");
        return className;
    }    
    
    public Enumeration getAllSubElements() throws CmsException {
        return getAllSubElements(null);
    }
    
    public Enumeration getAllSubElements(String selector) throws CmsException {
        String templateDatablockName = getTemplateDatablockName(selector);
        Element templateElement = getData(templateDatablockName);
        NodeList nl = templateElement.getElementsByTagName("ELEMENT");
        return getNamesFromNodeList(nl);
    }
    
    public String getTemplateDatablockName(String templateSelector) throws CmsException {
        String templateDatablockName = null;
        if(templateSelector != null && ! "".equals(templateSelector)) {            
            if(hasData("template." + templateSelector)) {
                templateDatablockName = "template." + templateSelector;
            } else {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "cannot load selected template file section " + templateSelector + " in template file " + getFilename());
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "choosing default template.");
            }
        }
        if(templateDatablockName == null) {
            if(hasData("TEMPLATE")) {
                templateDatablockName = "TEMPLATE";
            } else if(hasData("TEMPLATE.default")) {
                templateDatablockName = "TEMPLATE.default";
            } else {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "template definition file " + getAbsoluteFilename() + " is corrupt. cannot find default section.");
                throw new CmsException("Corrupt template file " + getAbsoluteFilename() + ". Cannot find default section.");
            }
        }
        return templateDatablockName;
    }
    
        
    
    private Enumeration getNamesFromNodeList(NodeList nl) throws CmsException {
        int numElements = nl.getLength();
        Vector collectNames = new Vector();

        for(int i=0; i<numElements; i++) {
            Element n = (Element)nl.item(i);
            String name = n.getAttribute("name");
            if(name == null || "".equals(name)) {
                // unnamed element found.
                // this is bad. throw an exception.
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlControlFile] unnamed <" + n.getNodeName() + "> found in OpenCms control file " + getAbsoluteFilename() + ".");
                }
                throw new CmsException("Unnamed \"" + n.getNodeName() + "\" found in OpenCms control file " + getAbsoluteFilename() + ".");
            }
            collectNames.addElement(name);
        }
        return collectNames.elements();
    }
}
