package com.opencms.template;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsXmlControlFile.java,v $
 * Date   : $Date: 2000/08/08 14:08:29 $
 * Version: $Revision: 1.16 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import com.opencms.file.*;
import com.opencms.core.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;

/**
 * Content definition for "clickable" and user requestable XML body files.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.16 $ $Date: 2000/08/08 14:08:29 $
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
	 * @param cms CmsObject object for accessing system resources.
	 * @param filename Name of the body file that shoul be read.
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
	 * @param filename Name of the body file that shoul be read.
	 */        
	public CmsXmlControlFile(CmsObject cms, String filename) throws CmsException {
		super();
		init(cms, filename);
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
		if(! hasData("ELEMENTDEF." + name)) {
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
	 */
	public String getElementClass(String elementName) throws CmsException {
		return getDataValue("ELEMENTDEF." + elementName + ".CLASS"); 
	}
	/**
	 * Gets an enumeration of all names of the subelement definition in the
	 * body file.
	 * @return Enumeration with of names.
	 * @exception CmsException
	 */
	public Enumeration getElementDefinitions() throws CmsException {        
		NodeList elementDefTags = getXmlDocument().getDocumentElement().getChildNodes();
		return getNamesFromNodeList(elementDefTags, "ELEMENTDEF", false);
	}
	/**
	 * Gets the value of a single parameter of a given subelement definition.
	 * @param elementName Name of the subelement.
	 * @param parameterName Name of the requested parameter.
	 */
	public String getElementParameter(String elementName, String parameterName) throws CmsException { 
		return getDataValue("ELEMENTDEF." + elementName + ".PARAMETER." + parameterName);        
	}
	/**
	 * Gets an enumeration of all parameter names of a given subelement definition.
	 * @param elementName Name of the subelement.
	 * @return Enumeration of all names.
	 * @exception CmsException
	 */
	public Enumeration getElementParameterNames(String elementName) throws CmsException {
		Element elementDefinition = getData("elementdef." + elementName);
		NodeList parameterTags = elementDefinition.getChildNodes();
		return getNamesFromNodeList(parameterTags, "PARAMETER", false);            
	}
	/**
	 * Gets the filename of the master template file of a given subelement definition.
	 * @param elementName Name of the subelement.
	 * @return Filename of the template file.
	 */
	public String getElementTemplate(String elementName) throws CmsException {
		return getDataValue("ELEMENTDEF." + elementName + ".TEMPLATE"); 
	}
	/**
	 * Gets the filename of the master template file of a given subelement definition.
	 * @param elementName Name of the subelement.
	 * @return Filename of the template file.
	 */
	public String getElementTemplSelector(String elementName) throws CmsException {
		return getDataValue("ELEMENTDEF." + elementName + ".TEMPLATESELECTOR"); 
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
	 * @exception CmsException
	 */
	private Enumeration getNamesFromNodeList(NodeList nl, String tag, boolean unnamedAllowed) throws CmsException {
		int numElements = nl.getLength();
		Vector collectNames = new Vector();

		for(int i=0; i<numElements; i++) {
			Node n = (Node)nl.item(i);                        
			if(n.getNodeType() == n.ELEMENT_NODE 
					&& n.getNodeName().toLowerCase().equals(tag.toLowerCase())) {
				String name = ((Element)n).getAttribute("name");
				if(name == null || "".equals(name)) {
					// unnamed element found.
					if(unnamedAllowed) {                        
						name = "(default)";
					} else {
						if(A_OpenCms.isLogging()) {
							A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlControlFile] unnamed <" + n.getNodeName() + "> found in OpenCms control file " + getAbsoluteFilename() + ".");
						}
						throw new CmsException("Unnamed \"" + n.getNodeName() + "\" found in OpenCms control file " + getAbsoluteFilename() + ".", CmsException.C_XML_TAG_MISSING);
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
	 */
	public String getParameter(String parameterName) throws CmsException {
		return getDataValue("PARAMETER." + parameterName);        
	}
	/**
	 * Gets an enumeration of all parameter names of the master template.
	 * @return Enumeration of all names.
	 * @exception CmsException
	 */
	public Enumeration getParameterNames() throws CmsException {
		NodeList parameterTags = getXmlDocument().getDocumentElement().getChildNodes();
		return getNamesFromNodeList(parameterTags, "PARAMETER", false);            
	}
	/**
	 * Gets the template class defined in the body file.
	 * @return Name of the template class.
	 * @exception CmsException
	 */
	public String getTemplateClass() throws CmsException {
		String result = getDataValue("class");
		// checking the value is not required here.
		// the launcher takes another class if no classname was found here
		return result;
	}
	/**
	 * Gets the expected tagname for the XML documents of this content type
	 * @return Expected XML tagname.
	 */
	public String getXmlDocumentTagName() {
		return "PAGE";
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
	 * Checks if the body file contains a definition of the 
	 * template selector for a given subelement definition.
	 * @param elementName Name of the subelement.
	 * @return <code>true<code> if a definition exists, <code>false</code> otherwise.
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
	public void setElementParameter(String elementName, String parameterName, String parameterValue) throws CmsException {
		createElementDef(elementName);
		if(! hasData("ELEMENTDEF." + elementName + ".PARAMETER." + parameterName)) {
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
	 * @filename Filename to be set.
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
	 * @exception CmsException
	 */
	public void setMasterTemplate(String template) {
		setData("masterTemplate", template);
	}
	/**
	 * Set the value of a single parameter of the master template.
	 * @param parameterName Name of the requested parameter.
	 * @param parameterValue Value to be set
	 */
	public void setParameter(String parameterName, String parameterValue) throws CmsException {
		if(! hasData("PARAMETER." + parameterName)) {
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
	 * Set the template class used for the master Template
	 * @param templateClass Name of the template class.
	 */
	public void setTemplateClass(String templateClass) {
		setData("class", templateClass);
	}
}
