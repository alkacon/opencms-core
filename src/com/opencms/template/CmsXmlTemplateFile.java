package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;

/**
 * Content definition for XML template files.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2000/01/21 10:35:18 $
 */
public class CmsXmlTemplateFile extends A_CmsXmlContent {

    /**
     * Default constructor.
     */
    public CmsXmlTemplateFile() throws CmsException {
        registerTag("ELEMENT", CmsXmlTemplateFile.class, "handleElementTag", C_REGISTER_MAIN_RUN);    
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlTemplateFile(A_CmsObject cms, String filename) throws CmsException {
        super();
        registerMyTags();
        init(cms, filename);
    }        

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlTemplateFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        registerMyTags();
        init(cms, file);
    }        
    
    /**
     * Registers the special tag <CODE>&lt;ELEMENT&gt;</CODE> for processing with
     * processNode().
     */
    private void registerMyTags() {
        registerTag("ELEMENT", CmsXmlTemplateFile.class, "handleElementTag", C_REGISTER_MAIN_RUN);    
    }
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "XMLTEMPLATE";
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms XML template file";
    }    

    /**
     * Handling of the <CODE>&lt;ELEMENT&gt;</CODE> tags.
     * Calls the user method <code>elementTag</code> that has to be
     * defined in the XML template class. 
     * 
     * @param n XML element containing the <code>&lt;PROCESS&gt;</code> tag.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     * @return Result of user method <code>templateElement()</code>.
     * @exception CmsException
     */
    public Object handleElementTag(Element n, Object callingObject, Object userObj) throws CmsException {
        String tagcontent = n.getAttribute("name");
        return callUserMethod("templateElement", tagcontent, callingObject, userObj); 
    }        
    
    /**
     * Gets the template class of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Name of the template class.
     */
    public String getSubtemplateClass(String name) throws CmsException {
        String className = getDataValue("ELEMENTDEF." + name + ".CLASS");
        return className;
    }

    /**
     * Gets the filename of the master template file of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Filename of the template file.
     */
    public String getSubtemplateFilename(String name) throws CmsException {
        String className = getDataValue("ELEMENTDEF." + name + ".TEMPLATE");
        return className;
    }    
    
    /**
     * Gets an enumeration of all used subelements in the default section
     * of a template file.
     * @return Enumeration of all subtemplate names.
     * @exception CmsException
     */
    public Enumeration getAllSubElements() throws CmsException {
        return getAllSubElements(null);
    }
    
    /**
     * Gets an enumeration of all used subelements in the given section
     * of a template file.
     * @param selector Section to be scanned for subelements
     * @return Enumeration of all subtemplate names.
     * @exception CmsException
     */
    public Enumeration getAllSubElements(String selector) throws CmsException {
        String templateDatablockName = getTemplateDatablockName(selector);
        Element templateElement = getData(templateDatablockName);
        NodeList nl = templateElement.getElementsByTagName("ELEMENT");
        return getNamesFromNodeList(nl);
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
    public String getParameter(String elementName, String parameterName) throws CmsException {
        return getDataValue("ELEMENTDEF." + elementName + ".PARAMETER." + parameterName);        
    }
    
    /**
     * Utility method to get the correct datablock name for a given selector.<BR>
     * If no selector is given or the selected section is not found, the template section
     * with no name will be returned. If even this is not found the section named "default"
     * will be returned.
     * 
     * @param templateSelector Name of the template section or null if the default section is requested.
     * @return Appropriate name of the template datablock.
     * @exception CmsException
     */
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
                throw new CmsException("Corrupt template file " + getAbsoluteFilename() + ". Cannot find default section.", CmsException.C_XML_TAG_MISSING);
            }
        }
        return templateDatablockName;
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
                throw new CmsException("Unnamed \"" + n.getNodeName() + "\" found in OpenCms control file " + getAbsoluteFilename() + ".", CmsException.C_XML_TAG_MISSING);
            }
            collectNames.addElement(name);
        }
        return collectNames.elements();
    }
}
