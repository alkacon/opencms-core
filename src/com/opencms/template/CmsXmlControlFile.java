package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.lang.reflect.*;
import java.util.*;

public class CmsXmlControlFile extends A_CmsXmlContent implements I_CmsLogChannels {

    public CmsXmlControlFile() throws CmsException {
        super();
    }
    
    public CmsXmlControlFile(A_CmsObject cms, String filename) throws CmsException {
        super();
        init(cms, filename);
    }

    public CmsXmlControlFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }        
    
    public String getXmlDocumentTagName() {
        return "PAGE";
    }
    
    public String getContentDescription() {
        return "OpenCms XML page file";
    }        
    
    public String getTemplateClass() throws CmsException {
        String result = getDataValue("class");
        if(result == null || "".equals(result)) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlControlFile] <CLASS> tag not found in file " + getAbsoluteFilename() + ".");
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlControlFile] Document has errors. Removing from cache.");
            clearFileCache(this);
            throw new CmsException("\"CLASS\" definition tag not found in file " + getAbsoluteFilename() + ".");
        }
        return result;
    }
    
    public String getMasterTemplate() throws CmsException {
        String result = getDataValue("mastertemplate");
        if(result == null || "".equals(result)) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlControlFile] <MASTERTEMPLATE> tag not found in file " + getAbsoluteFilename() + ".");
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlControlFile] Document has errors. Removing from cache.");
            clearFileCache(this);
            throw new CmsException("\"MASTERTEMPLATE\" definition tag not found in file " + getAbsoluteFilename() + ".");
        }
        return result;
    }
    
    public boolean isElementClassDefined(String elementName) {
        return this.hasData("ELEMENTDEF." + elementName + ".CLASS");
    }

    public boolean isElementTemplateDefined(String elementName) {
        return this.hasData("ELEMENTDEF." + elementName + ".TEMPLATE");
    }
    
    public String getElementClass(String elementName) {
        return getDataValue("ELEMENTDEF." + elementName + ".CLASS"); 
    }

    public String getElementTemplate(String elementName) {
        return getDataValue("ELEMENTDEF." + elementName + ".TEMPLATE"); 
    }
        
    public Enumeration getElementDefinitions() throws CmsException {        
        Document domDoc = getXmlDocument();
        NodeList elementDefTags = domDoc.getElementsByTagName("ELEMENTDEF");
        return getNamesFromNodeList(elementDefTags);
    }
  
    public Enumeration getParameterNames(String elementName) throws CmsException {
        Element elementDefinition = getData("elementdef." + elementName);
        NodeList parameterTags = elementDefinition.getElementsByTagName("PARAMETER");
        return getNamesFromNodeList(parameterTags);            
    }
    
    public String getParameter(String elementName, String parameterName) {
        return getDataValue("ELEMENTDEF." + elementName + ".PARAMETER." + parameterName);        
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
