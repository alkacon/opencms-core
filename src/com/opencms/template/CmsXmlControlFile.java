/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsXmlControlFile.java,v $
* Date   : $Date: 2005/02/18 15:18:52 $
* Version: $Revision: 1.53 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.template;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.workplace.I_CmsWpConstants;

import com.opencms.template.cache.CmsElementDefinition;
import com.opencms.template.cache.CmsElementDefinitionCollection;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Content definition for "clickable" and user requestable XML body files.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.53 $ $Date: 2005/02/18 15:18:52 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsXmlControlFile extends A_CmsXmlContent {

    /**
     * Default constructor.
     */
    public CmsXmlControlFile() {
        super();
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     *
     * @param cms CmsObject object for accessing system resources.
     * @param file name of the body file that shoul be read.
     * @throws CmsException if something goes wrong
     */
    public CmsXmlControlFile(CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     *
     * @param cms CmsObject object for accessing system resources.
     * @param filename name of the body file that shoul be read.
     * @throws CmsException if something goes wrong
     */
    public CmsXmlControlFile(CmsObject cms, String filename) throws CmsException {
        super();
        init(cms, filename);
    }
    
    /**
     * Constructor for creating a new object containing the given content
     * for the given filename.
     *
     * @param cms for accessing system resources
     * @param filename name of the file that shoul be stored in this XML file cache
     * @param content XML file to parse
     * @throws CmsException if something goes wrong
     */
    public CmsXmlControlFile(CmsObject cms, String filename, String content) throws CmsException {
        super();
        init(cms, filename, content);
    }    

    /**
     * Used for setting element definition values.
     * Checks if the requested element definition already exists.
     * If so, nothing will happen. If not, a corresponding section
     * will be created using a hierarchical datablock tag
     * <code>&lt;ELEMENTDEF name="..."/&gt;</code>
     *
     * @param name Name of the element definition section.
     */
    private void createElementDef(String name) {
        if (!hasData("ELEMENTDEF." + name)) {
            Document doc = getXmlDocument();
            Element e = doc.createElement("ELEMENTDEF");
            e.setAttribute("name", name);
            setData("elementdef." + name, e);
        }
    }

    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms XML page file";
    }

    /**
     * Gets the template class of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Name of the template class.
     * @throws CmsException if something goes wrong
     */
    public String getElementClass(String elementName) throws CmsException {
        return getDataValue("ELEMENTDEF." + elementName + ".CLASS");
    }

    /**
     * Gets an enumeration of all names of the subelement definition in the
     * body file.
     * @return Enumeration with of names.
     * @throws CmsException if something goes wrong
     */
    public Enumeration getElementDefinitions() throws CmsException {
        NodeList elementDefTags = getXmlDocument().getDocumentElement().getChildNodes();
        return getNamesFromNodeList(elementDefTags, "ELEMENTDEF", false);
    }

    /**
     * Gets the value of a single parameter of a given subelement definition.
     * @param elementName Name of the subelement.
     * @param parameterName Name of the requested parameter.
     * @return the element parameter value
     * @throws CmsException if something goes wrong
     */
    public String getElementParameter(String elementName, String parameterName) throws CmsException {
        return getDataValue("ELEMENTDEF." + elementName + ".PARAMETER." + parameterName);
    }

    /**
     * Gets an enumeration of all parameter names of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Enumeration of all names.
     * @throws CmsException if something goes wrong
     */
    public Enumeration getElementParameterNames(String elementName) throws CmsException {
        Element elementDefinition = getData("elementdef." + elementName);
        NodeList parameterTags = elementDefinition.getChildNodes();
        return getNamesFromNodeList(parameterTags, "PARAMETER", false);
    }

    /**
     * Get a hashtable containing all parameters and thies values of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Enumeration of all names.
     * @throws CmsException if something goes wrong
     */
    public Hashtable getElementParameters(String elementName) throws CmsException {
        Hashtable result = new Hashtable();
        Element elementDefinition = getData("elementdef." + elementName);
        NodeList parameterTags = elementDefinition.getChildNodes();

        int numElements = parameterTags.getLength();
        for (int i = 0; i < numElements; i++) {
            Node n = parameterTags.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().toLowerCase().equals("parameter")) {
                String name = ((Element)n).getAttribute("name");
                if (name != null && !"".equals(name)) {
                    result.put(name, getTagValue((Element)n));
                }
            }
        }
        return result;
    }

    /**
     * Gets the filename of the master template file of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Filename of the template file.
     * @throws CmsException if something goes wrong
     */
    public String getElementTemplate(String elementName) throws CmsException {
        //return getDataValue("ELEMENTDEF." + elementName + ".TEMPLATE");
        String result = getDataValue("ELEMENTDEF." + elementName + ".TEMPLATE");
           return result;
    }

    /**
     * Gets the filename of the master template file of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Filename of the template file.
     * @throws CmsException if something goes wrong
     */
    public String getElementTemplSelector(String elementName) throws CmsException {
        return getDataValue("ELEMENTDEF." + elementName + ".TEMPLATESELECTOR");
    }

    /**
     * Gets the filename of the master template file defined in
     * the body file.
     * @return Filename of the template file.
     * @throws CmsException if something goes wrong
     */
    public String getMasterTemplate() throws CmsException {
        String result = getDataValue("mastertemplate");
        if (result == null || "".equals(result)) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("<MASTERTEMPLATE> tag not found in file " + getAbsoluteFilename());
            }
            removeFromFileCache();
            throw new CmsException("\"MASTERTEMPLATE\" definition tag not found in file " + getAbsoluteFilename() + ".", CmsException.C_XML_TAG_MISSING);
        }
        return result;
    }

    /**
     * Internal utility method to extract the values of the "name" attribute
     * from defined nodes of a given nodelist.
     * @param nl NodeList to extract.
     * @param tag Name of the tag whose "name" attribute should be extracted
     * @param unnamedAllowed Indicates if unnamed tags are allowed or an exception should
     * be thrown.
     * @return Enumeration of all "name" attributes.
     * @throws CmsException
     */
    private Enumeration getNamesFromNodeList(NodeList nl, String tag, boolean unnamedAllowed) throws CmsException {
        int numElements = nl.getLength();
        Vector collectNames = new Vector();
        for (int i = 0; i < numElements; i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().toLowerCase().equals(tag.toLowerCase())) {
                String name = ((Element)n).getAttribute("name");
                if (name == null || "".equals(name)) {

                    // unnamed element found.
                    if (unnamedAllowed) {
                        name = "(default)";
                    } else {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Unnamed <" + n.getNodeName() + "> found in OpenCms control file " + getAbsoluteFilename());
                        }
                        throw new CmsException("Unnamed \"" + n.getNodeName() + "\" found in OpenCms control file " + getAbsoluteFilename(), CmsException.C_XML_TAG_MISSING);
                    }
                }
                collectNames.addElement(name);
            }
        }
        return collectNames.elements();
    }

    /**
     * Gets the value of a single parameter of the master template.
     * @param parameterName Name of the requested parameter.
     * @return the parameter value
     * @throws CmsException if something goes wrong
     */
    public String getParameter(String parameterName) throws CmsException {
        return getDataValue("PARAMETER." + parameterName);
    }

    /**
     * Gets an enumeration of all parameter names of the master template.
     * @return Enumeration of all names.
     * @throws CmsException if something goes wrong
     */
    public Enumeration getParameterNames() throws CmsException {
        NodeList parameterTags = getXmlDocument().getDocumentElement().getChildNodes();
        return getNamesFromNodeList(parameterTags, "PARAMETER", false);
    }

    /**
     * Gets the template class defined in the body file.
     * @return Name of the template class.
     * @throws CmsException if something goes wrong
     */
    public String getTemplateClass() throws CmsException {
        return getDataValue("class");
    }

    /**
     * Gets the expected tagname for the XML documents of this content type.<p>
     * 
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "PAGE";
    }

    /**
     * Checks if the body file contains a definition of the
     * template class name for a given subelement definition.
     * @param elementName Name of the subelement.
     * @return <code>true</code> if a definition exists, <code>false</code> otherwise.
     */
    public boolean isElementClassDefined(String elementName) {
        return this.hasData("ELEMENTDEF." + elementName + ".CLASS");
    }

    /**
     * Checks if the body file contains a definition of the
     * template file name for a given subelement definition.
     * @param elementName Name of the subelement.
     * @return <code>true</code> if a definition exists, <code>false</code> otherwise.
     */
    public boolean isElementTemplateDefined(String elementName) {
        return this.hasData("ELEMENTDEF." + elementName + ".TEMPLATE");
    }

    /**
     * Checks if the body file contains a definition of the
     * template selector for a given subelement definition.
     * @param elementName Name of the subelement.
     * @return <code>true</code> if a definition exists, <code>false</code> otherwise.
     */
    public boolean isElementTemplSelectorDefined(String elementName) {
        return this.hasData("ELEMENTDEF." + elementName + ".TEMPLATESELECTOR");
    }

    /**
     * Sets the template class of a given subelement definition.
     * @param elementName Name of the subelement.
     * @param classname Classname to be set.
     */
    public void setElementClass(String elementName, String classname) {
        createElementDef(elementName);
        setData("ELEMENTDEF." + elementName + ".CLASS", classname);
    }

    /**
     * Set the value of a single parameter of a given subelement definition.
     * @param elementName Name of the subelement.
     * @param parameterName Name of the requested parameter.
     * @param parameterValue Value to be set
     */
    public void setElementParameter(String elementName, String parameterName, String parameterValue) {
        createElementDef(elementName);
        if (!hasData("ELEMENTDEF." + elementName + ".PARAMETER." + parameterName)) {
            Document doc = getXmlDocument();
            Element e = doc.createElement("PARAMETER");
            e.setAttribute("name", parameterName);
            e.appendChild(doc.createTextNode(parameterValue));
            setData("ELEMENTDEF." + elementName + ".PARAMETER." + parameterName, e);
        } else {
            setData("ELEMENTDEF." + elementName + ".PARAMETER." + parameterName, parameterValue);
        }
    }

    /**
     * Sets the filename of the master template file of a given subelement definition.
     * @param elementName Name of the subelement.
     * @param filename Filename to be set.
     */
    public void setElementTemplate(String elementName, String filename) {
        createElementDef(elementName);
        setData("ELEMENTDEF." + elementName + ".TEMPLATE", filename);
    }

    /**
     * Sets the filename of the master template file of a given subelement definition.
     * @param elementName Name of the subelement.
     * @param templateSelector Template selector to be set.
     */
    public void setElementTemplSelector(String elementName, String templateSelector) {
        createElementDef(elementName);
        setData("ELEMENTDEF." + elementName + ".TEMPLATESELECTOR", templateSelector);
    }

    /**
     * Sets the filename of the master template file defined in
     * the body file.
     * @param template Filename of the template file.
     */
    public void setMasterTemplate(String template) {
        setData("masterTemplate", template);
    }

    /**
     * Set the value of a single parameter of the master template.
     * @param parameterName Name of the requested parameter.
     * @param parameterValue Value to be set
     */
    public void setParameter(String parameterName, String parameterValue) {
        if (!hasData("PARAMETER." + parameterName)) {
            Document doc = getXmlDocument();
            Element e = doc.createElement("PARAMETER");
            e.setAttribute("name", parameterName);
            e.appendChild(doc.createTextNode(parameterValue));
            setData("PARAMETER." + parameterName, e);
        } else {
            setData("PARAMETER." + parameterName, parameterValue);
        }
    }

    /**
     * Set the template class used for the master Template.<p>
     * 
     * @param templateClass Name of the template class.
     */
    public void setTemplateClass(String templateClass) {
        setData("class", templateClass);
    }

    /**
     * Gets a collection of element definitions.<p>
     * 
     * @return a collection of element definitions
     * @throws CmsException if something goes wrong
     */
    public CmsElementDefinitionCollection getElementDefinitionCollection() throws CmsException {
        CmsElementDefinitionCollection result = new CmsElementDefinitionCollection();
        Enumeration elementDefinitions = getElementDefinitions();
        while (elementDefinitions.hasMoreElements()) {
            String elementName = (String)elementDefinitions.nextElement();

            String elementClass = null;
            String elementTemplate = null;
            String elementTs = null;
            if (isElementClassDefined(elementName)) {
                elementClass = getElementClass(elementName);
            }
            if (isElementTemplateDefined(elementName)) {
                elementTemplate = getElementTemplate(elementName);
            }
            if (isElementTemplSelectorDefined(elementName)) {
                elementTs = getElementTemplSelector(elementName);
            }
            Hashtable elementParameters = getElementParameters(elementName);
            if (elementClass == null) {
                elementClass = I_CmsConstants.C_XML_CONTROL_DEFAULT_CLASS;
            }
            if (elementTemplate != null) {
                elementTemplate = CmsLinkManager.getAbsoluteUri(elementTemplate, getAbsoluteFilename());
            }
            result.add(new CmsElementDefinition(elementName, elementClass, elementTemplate, elementTs, elementParameters));
        }
        return result;
    }

    /**
     * Validates a given body path.<p>
     * 
     * After a folder is moved or renamed, the XML control files still contain the old body path.
     * This method first tries to read the given body path from the XML control file. If this path
     * is invalid, it tries to read the body file in "/system/bodies/" + current folder + filename.
     * 
     * @param cms the user's CmsObject instance
     * @param bodyPath the body path that gets validated
     * @param page the page of which the body path gets validated
     * @return the original body path if it valid, or "/system/bodies/" + current folder + filename
     */
    public String validateBodyPath(CmsObject cms, String bodyPath, CmsResource page) {
        String validatedBodyPath = null;
        
        if (bodyPath==null || "".equals(bodyPath)) {
            return bodyPath;
        }
                
        try {
            cms.readResource(bodyPath, CmsResourceFilter.ALL);
            validatedBodyPath = bodyPath;
        } catch (CmsException e) {
            if (e.getType()==CmsException.C_NOT_FOUND) {
                String defaultBodyPath = I_CmsWpConstants.C_VFS_PATH_BODIES + CmsResource.getParentFolder(cms.getSitePath(page)).substring(1) + page.getName();
                try {
                    cms.readResource(defaultBodyPath, CmsResourceFilter.ALL);
                    validatedBodyPath = defaultBodyPath;
                    setElementTemplate(CmsXmlTemplate.C_BODY_ELEMENT, validatedBodyPath);
                } catch (CmsException e1) {
                    validatedBodyPath = null;
                }                
            }
        }
        
        return validatedBodyPath;
    }

}
