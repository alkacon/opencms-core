/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsXmlTemplateFile.java,v $
 * Date   : $Date: 2000/04/05 08:45:55 $
 * Version: $Revision: 1.18 $
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

package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;
import java.io.*;

/**
 * Content definition for XML template files.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.18 $ $Date: 2000/04/05 08:45:55 $
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
     * Gets the template selector of the master template file of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Filename of the template file.
     */
    public String getSubtemplateSelector(String name) throws CmsException {
        String templateSelector = getDataValue("ELEMENTDEF." + name + ".TEMPLATESELECTOR");
        return templateSelector;
    }    

    /**
     * Checks if there is a template selector defined in the subelement definition of
     * this template file.
     * @param elementName Name of the subelement.
     * @return <code>true</code>, if there exists a template selector, <code>false</code> otherwise.
     */
    public boolean hasSubtemplateSelector(String name) throws CmsException {
        return hasData("ELEMENTDEF." + name + ".TEMPLATESELECTOR");
    }    
    
    /**
     * Gets an enumeration of all used subelements in the default section
     * of a template file.
     * @return Vector of all subtemplate names.
     * @exception CmsException
     */
    public Vector getAllSubElements() throws CmsException {
        return getAllSubElements(null);
    }
    
    /**
     * Gets an enumeration of all used subelements in the given section
     * of a template file.
     * @param selector Section to be scanned for subelements
     * @return Vector of all subtemplate names.
     * @exception CmsException
     */
    public Vector getAllSubElements(String selector) throws CmsException {
        String templateDatablockName = getTemplateDatablockName(selector);
        Element templateElement = getData(templateDatablockName);
        NodeList nl = templateElement.getChildNodes();
        return getNamesFromNodeList(nl, "ELEMENT", false);
    }
    
    public Vector getAllSections() throws CmsException {
        NodeList nl = ((Element)getXmlDocument().getDocumentElement()).getChildNodes();             
        return getNamesFromNodeList(nl, "TEMPLATE", true);
    }

    public int createNewSection(String sectionName) {
        String tempName = sectionName;
        int loop = 0;
        while(hasData("template." + tempName)) {
            tempName = sectionName + (++loop);        
        }        
        
        Element newData = getXmlDocument().createElement("template");
        newData.setAttribute("name", tempName);
        setData("template." + tempName, newData);
        
        return loop;
    
    }
    
    public void setSectionTitle(String sectionName, String title) throws CmsException {
        String datablockName = getTemplateDatablockName(sectionName);
        Element data = null;
        try {
            data = getData(datablockName);
        } catch(Exception e) {
            // The given section doesn't exist. Ignore.
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, "Cannot set title for template section \"" + sectionName + "\" in file " 
                + getAbsoluteFilename() + ". Section doesn't exist.");
            }
            return;
        }
        data.setAttribute("title", title);
    }

    public String getSectionTitle(String sectionName) throws CmsException {
        String datablockName = getTemplateDatablockName(sectionName);
        String result = null;
        try {
            Element data = getData(datablockName);
            result = data.getAttribute("title");
        } catch(Exception e) {
            // The given section doesn't exist. Ignore.
            result = "";
        }
        return result;
    }
    
    /**
     * Gets an enumeration of all parameter names of a given subelement definition.
     * @param elementName Name of the subelement.
     * @return Vector of all names.
     * @exception CmsException
     */
    public Vector getParameterNames(String elementName) throws CmsException {
        Element elementDefinition = getData("elementdef." + elementName);
        NodeList parameterTags = elementDefinition.getChildNodes();
        return getNamesFromNodeList(parameterTags, "PARAMETER", false);            
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
     * Checks if the section with the given name is defined
     * in this XML template file.
     * @param name Name of the requested section.
     * @return <code>true</code> if a section exists, <code>false</code> otherwise.
     */
    public boolean hasSection(String name) {
        return hasData("template." + name);
    }
        
    public Element getBodyTag() throws CmsException {
        Element result = null;
        if(hasData("bodyTag")) {
            result = getData("bodytag");
        } else {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, getClassName() + "Cannot find \"bodytag\" tag in XML template file " + getFilename() + ".");
            }
        }
        return result;
    }
    
    public void setBodyTag(Element data) throws CmsException {
        setData("bodytag", data);
    }
    
    /**
     * Gets the processed data of the appropriate <code>&lt;TEMPLATE&gt;</code> section of
     * this workplace template file.
     * <P>
     * The correct datablock name for the template datablock will be taken
     * from <code>getTemplateDatablockName</code>.
     * 
     * @param callingObject reference to the calling object. Used to look up user methods while processing.
     * @param parameters hashtable containing all user parameters.
     * @param templateSelector Name of the template section or null if the default section is requested.
     * @return Processed template data.
     * @exception CmsException
     * @see #getTemplateDatablockName
     */
    public String getProcessedTemplateContent(Object callingObject, Hashtable parameters, String templateSelector) throws CmsException {
        String datablockName = this.getTemplateDatablockName(templateSelector);
        return getProcessedDataValue(datablockName, callingObject, parameters);
    }        

    /**
     * Gets the data of the appropriate <code>&lt;TEMPLATE&gt;</code> section of
     * this workplace template file.
     * <P>
     * The correct datablock name for the template datablock will be taken
     * from <code>getTemplateDatablockName</code>.
     * 
     * @param callingObject reference to the calling object. Used to look up user methods while processing.
     * @param parameters hashtable containing all user parameters.
     * @param templateSelector Name of the template section or null if the default section is requested.
     * @return Processed template data.
     * @exception CmsException
     * @see #getTemplateDatablockName
     */
    public String getTemplateContent(Object callingObject, Hashtable parameters, String templateSelector) throws CmsException {
        String datablockName = this.getTemplateDatablockName(templateSelector);
        return getDataValue(datablockName);
    }        
    
 
    public void setEditedTemplateContent(String content, String templateSelector, 
                                         boolean html) throws CmsException {
        String datablockName = this.getTemplateDatablockName(templateSelector);
		// TODO:is this needed? Element data = getData(datablockName);

        if(html) {
            int startIndex = content.indexOf("<BODY");
            startIndex = content.indexOf(">", startIndex + 1) + 1;
            int endIndex = content.lastIndexOf("</BODY>");          
            if(startIndex > 0) {
                content = content.substring(startIndex, endIndex);
            }
        }
                
        StringBuffer tempXmlString = new StringBuffer();
        tempXmlString.append("<?xml version=\"1.0\"?>\n");
        tempXmlString.append("<" + getXmlDocumentTagName() + ">");
        tempXmlString.append("<template>\n");
        
        if(html) {
            tempXmlString.append("<![CDATA[");
            content = replace(content, "[", "]]><");
            tempXmlString.append(content.trim());
            tempXmlString.append("]]>");
        } else {
            tempXmlString.append(content);
        }            
        tempXmlString.append("</template>\n");
        tempXmlString.append("</" + getXmlDocumentTagName() + ">\n");
        
        I_CmsXmlParser parser = getXmlParser();
        StringReader parserReader = new StringReader(tempXmlString.toString());
        Document tempDoc = null;
        try {
            tempDoc = parser.parse(parserReader);            
        } catch(Exception e) {
            throwException("PARSING ERROR!", CmsException.C_XML_PARSING_ERROR);
        }
        
        Element templateNode = (Element)tempDoc.getDocumentElement().getFirstChild();
        setData(datablockName, templateNode);
    }
                                        

    
    public String getEditableTemplateContent(Object callingObject, Hashtable parameters, String templateSelector, 
                                             boolean html, String style) throws CmsException {

        Vector cdatas = new Vector();
                
        String datablockName = this.getTemplateDatablockName(templateSelector);
        Element data = getData(datablockName);
        StringBuffer result = new StringBuffer();
        
        if(style == null) {
            style = "";
        }
        
        Document tempDoc = (Document)getXmlDocument().cloneNode(true);
        Element rootElem = tempDoc.getDocumentElement();
        
        while(rootElem.hasChildNodes()) {
            rootElem.removeChild(rootElem.getFirstChild());
        }
        data = (Element)getXmlParser().importNode(tempDoc, data);
        rootElem.appendChild(data);       
        
        if(html) {
            // Scan for cdatas
            Node n = data;
            while(n != null) {
                if(n.getNodeType() == n.CDATA_SECTION_NODE) {
                    cdatas.addElement(n.getNodeValue());                
                    n.setNodeValue("");
                }
                n = treeWalker(n);
            }
        }
        
        StringWriter out = new StringWriter();        
        getXmlParser().getXmlText(tempDoc, out);
        String xmlString = out.toString();
        
        int endOpeningXmlTag = xmlString.indexOf(">");
        int endOpeningDocTag = xmlString.indexOf(">", endOpeningXmlTag + 1);
        int endOpeningBodyTag = xmlString.indexOf(">", endOpeningDocTag + 1) + 1;
        
        int startClosingDocTag = xmlString.lastIndexOf("<");
        int startClosingBodyTag = xmlString.lastIndexOf("<", startClosingDocTag - 1);
        
        if(startClosingBodyTag <= endOpeningBodyTag) {
            xmlString = "";
        } else {
            xmlString = xmlString.substring(endOpeningBodyTag, startClosingBodyTag);
            xmlString = xmlString.trim();
        }
        
        if(html) {            
            int cdataStart = xmlString.indexOf("<![CDATA[");
            int currentPos = 0;
            int loop = 0;
        
            result.append("<HTML>\n<HEAD>\n");
            result.append("<link rel=stylesheet type=\"text/css\" href=\"" + style + "\">\n");
            result.append("</HEAD>\n");
            result.append("<BODY " + getProcessedDataValue("bodytag", callingObject, parameters) + ">\n");
                
            while(cdataStart != -1) {                
                String tempString = xmlString.substring(currentPos, cdataStart);
                tempString = myReplace(tempString);
                //result.append(xmlString.substring(currentPos, cdataStart).replace('<', '[').replace('>', ']'));
                result.append(tempString);
                result.append((String)cdatas.elementAt(loop++));
                cdataStart = xmlString.indexOf("<![CDATA[", cdataStart + 1);
                currentPos = xmlString.indexOf("]]>", currentPos + 1) + 3;
            }
            String tempString = xmlString.substring(currentPos);
            tempString = myReplace(tempString);
            //result.append(xmlString.substring(currentPos).replace('<', '[').replace('>', ']'));
            result.append(tempString);
                                   
            result.append("\n</BODY>\n</HTML>");        
            xmlString = result.toString();
        }

        return xmlString;
    }
    
            
    /**
     * Gets the processed data of the default <code>&lt;TEMPLATE&gt;</code> section of
     * this workplace template file.
     * <P>
     * The correct datablock name for the template datablock will be taken
     * from <code>getTemplateDatablockName</code>.
     * 
     * @param callingObject reference to the calling object. Used to look up user methods while processing.
     * @param parameters hashtable containing all user parameters.
     * @return Processed template data.
     * @exception CmsException
     * @see #getTemplateDatablockName
     */
    public String getProcessedTemplateContent(Object callingObject, Hashtable parameters) throws CmsException {
        return getProcessedTemplateContent(callingObject, parameters, null);
    }        
     
    /**
     * Checks if this Template owns a datablock with the given key.
     * @param key Datablock key to be checked.
     * @return true if a datablock is found, false otherwise.
     */
    public boolean hasData(String key) {
        return super.hasData(key);
    }    
                    
	/**
	 * Gets a complete datablock from the datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @return Complete DOM element of the datablock for the given key 
	 * or null if no datablock is found for this key.
	 */
	public Element getData(String tag) throws CmsException {
        return super.getData(tag);
    }

	/**
	 * Gets the text and CDATA content of a datablock from the 
	 * datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @return Datablock content for the given key or null if no datablock
	 * is found for this key.
	 */
    public String getDataValue(String tag) throws CmsException {
        return super.getDataValue(tag);
    }    
    
  	/**
	 * Gets a processed datablock from the datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
	public Element getProcessedData(String tag) throws CmsException {
        return super.getProcessedData(tag);
    }
   
  	/**
	 * Gets a processed datablock from the datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @param callingObject Object that should be used to look up user methods.
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
    public Element getProcessedData(String tag, Object callingObject) throws CmsException {
        return super.getProcessedData(tag, callingObject);
    }
    
  	/**
	 * Gets a processed datablock from the datablock hashtable.
	 * <P>
	 * The userObj Object is passed to all called user methods.
	 * By using this, the initiating class can pass customized data to its methods.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @param callingObject Object that should be used to look up user methods.
	 * @param userObj any object that should be passed to user methods
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
	public Element getProcessedData(String tag, Object callingObject, Object userObj) 
            throws CmsException {
        return super.getProcessedData(tag, callingObject, userObj);
    }

  	/**
	 * Gets the text and CDATA content of a processed datablock from the 
	 * datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
	public String getProcessedDataValue(String tag) throws CmsException {
        return super.getProcessedDataValue(tag);
    }

  	/**
	 * Gets the text and CDATA content of a processed datablock from the 
	 * datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @param callingObject Object that should be used to look up user methods.
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
    public String getProcessedDataValue(String tag, Object callingObject) throws CmsException {
        return super.getProcessedDataValue(tag, callingObject);
    }

  	/**
	 * Gets the text and CDATA content of a processed datablock from the 
	 * datablock hashtable.
	 * <P>
	 * The userObj Object is passed to all called user methods.
	 * By using this, the initiating class can pass customized data to its methods.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @param callingObject Object that should be used to look up user methods.
	 * @param userObj any object that should be passed to user methods
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
	public String getProcessedDataValue(String tag, Object callingObject, Object userObj) 
            throws CmsException {
        return super.getProcessedDataValue(tag, callingObject, userObj);
	}    

   /**
    * Creates a datablock consisting of a single TextNode containing 
    * data and stores this block into the datablock-hashtable.
    * 
    * @param tag Key for this datablock.
    * @param data String to be put in the datablock.
    */
    public void setData(String tag, String data) {
        super.setData(tag, data);
    }

    /**
     * Stores a given datablock element in the datablock hashtable.
     * 
     * @param tag Key for this datablock.
     * @param data DOM element node for this datablock.
     */
    public void setData(String tag, Element data) { 
        super.setData(tag, data);
    }
        
    /**
     * Remove a datablock from the internal hashtable and
     * from the XML document
     * @param tag Key of the datablock to delete.
     */    
    public void removeData(String tag) {
        super.removeData(tag);
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
    private String getTemplateDatablockName(String templateSelector) throws CmsException {
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
     * from defined nodes of a given nodelist.
     * @param nl NodeList to extract.
     * @param tag Name of the tag whose "name" attribute should be extracted
     * @param unnamedAllowed Indicates if unnamed tags are allowed or an exception should
     * be thrown.
     * @return Enumeration of all "name" attributes.
     * @exception CmsException
     */
    private Vector getNamesFromNodeList(NodeList nl, String tag, boolean unnamedAllowed) throws CmsException {
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
        return collectNames;
    }
    
    
    private int min(int a, int b) {
        if(a==-1) {
            return b;
        }
        if(b==-1) {
            return a;
        }
        return a<b?a:b;
    }
    
    private String replace(String s, String search, String replace) {
        StringBuffer tempContent = new StringBuffer();                
        //int index = s.indexOf(search);
        int index = min(s.indexOf("["), s.indexOf("]"));
        index = min(index, s.indexOf("&quot;"));
        int lastindex = 0;
        while(index != -1) {
            
            String sub = s.substring(lastindex, index);
            tempContent.append(sub);
            if(s.charAt(index) == ']') {                
                tempContent.append("><![CDATA[");
                lastindex = index + 1;
            } else if(s.charAt(index) == '[') {
                tempContent.append("]]><");
                lastindex = index + 1;
            } else {
                tempContent.append("\"");
                lastindex = index + 6;
            }
            //index = s.indexOf(search, index+1);
            index = min(s.indexOf("[", index+1), min(s.indexOf("]", index+1), s.indexOf("&quot;", index+1)));
        }
        tempContent.append(s.substring(lastindex));
        return new String(tempContent);
    }

    private String myReplace(String s) {
        StringBuffer tempContent = new StringBuffer();                
        //int index = s.indexOf(search);
        int index = min(s.indexOf("<"), s.indexOf(">"));
        index = min(index, s.indexOf("\""));
        int lastindex = 0;
        while(index != -1) {
            String sub = s.substring(lastindex, index);
            tempContent.append(sub);
            if(s.charAt(index) == '>') {                
                //tempContent.append("]</CODE>");
                tempContent.append("]");
                lastindex = index + 1;
            } else if(s.charAt(index) == '<'){
                //tempContent.append("<CODE>[");
                tempContent.append("[");
                lastindex = index + 1;
            } else {
                tempContent.append("&quot;");
                lastindex = index + 1;
            }

            //index = s.indexOf(search, index+1);
            //index = min(s.indexOf("<", index+1), s.indexOf(">", index+1));
            index = min(s.indexOf("<", index+1), min(s.indexOf(">", index+1), s.indexOf("\"", index+1)));
        }
        tempContent.append(s.substring(lastindex));
        return new String(tempContent);
    }        
}
