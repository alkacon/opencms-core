package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Content definition for "clickable" and user requestable XML body files.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/01/14 15:45:21 $
 */
public class CmsXmlControlFile extends A_CmsXmlContent implements I_CmsLogChannels {

    /**
     * Default constructor.
     */
    public CmsXmlControlFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlControlFile(A_CmsObject cms, String filename) throws CmsException {
        super();
        init(cms, filename);
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlControlFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }        
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "PAGE";
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms XML page file";
    }        
    
    /**
     * Gets the template class defined in the body file.
     * @return Name of the template class.
     * @exception CmsException
     */
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
    
    /**
     * Gets the filename of the master template file defined in
     * the body file.
     * @return Filename of the template file.
     * @exception CmsException
     */
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
    
    /**
     * Checks if the body file contains a definition of the 
     * template class name for a given subelement definition.
     * @param elementName Name of the subelement.
     * @return <code>true<code> if a definition exists, <code>false</code> otherwise.
     */
    public boolean isElementClassDefined(String elementName) {
        return this.hasData("ELEMENTDEF." + elementName + ".CLASS");
    }

    /**
     * Checks if the body file contains a definition of the 
     * template file name for a given subelement definition.
     * @param elementName Name of the subelement.
     * @return <code>true<code> if a definition exists, <code>false</code> otherwise.
     */
    public boolean isElementTemplateDefined(String elementName) {
        return this.hasData("ELEMENTDEF." + elementName + ".TEMPLATE");
    }
    
    /**
     * Gets the template class of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Name of the template class.
     */
    public String getElementClass(String elementName) {
        return getDataValue("ELEMENTDEF." + elementName + ".CLASS"); 
    }

    /**
     * Gets the filename of the master template file of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Filename of the template file.
     */
    public String getElementTemplate(String elementName) {
        return getDataValue("ELEMENTDEF." + elementName + ".TEMPLATE"); 
    }
        
    /**
     * Gets an enumeration of all names of the subelement definition in the
     * body file.
     * @return Enumeration with of names.
     * @exception CmsException
     */
    public Enumeration getElementDefinitions() throws CmsException {        
        Document domDoc = getXmlDocument();
        NodeList elementDefTags = domDoc.getElementsByTagName("ELEMENTDEF");
        return getNamesFromNodeList(elementDefTags);
    }
  
    /**
     * Gets an enumeration of all parameter names of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Enumeration of all names.
     * @exception CmsException
     */
    public Enumeration getParameterNames(String elementName) throws CmsException {
        Element elementDefinition = getData("elementdef." + elementName);
        NodeList parameterTags = elementDefinition.getElementsByTagName("PARAMETER");
        return getNamesFromNodeList(parameterTags);            
    }
    
    /**
     * Gets the value of a single parameter of a given subelement definition.
     * @param elementName Name of the subelement.
     * @param parameterName Name of the requested parameter.
     */
    public String getParameter(String elementName, String parameterName) {
        return getDataValue("ELEMENTDEF." + elementName + ".PARAMETER." + parameterName);        
    }
    
    /**
     * Internal utility method to extract the values of the "name" attribute
     * from all nodes of a given nodelist.
     * @param nl NodeList to extract.
     * @return Enumeration of all "name" attributes.
     * @exception CmsException
     */
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
