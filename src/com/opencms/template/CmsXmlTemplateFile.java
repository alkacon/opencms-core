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
 * @version $Revision: 1.8 $ $Date: 2000/02/15 13:09:32 $
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
    
 
    public void setEditedTemplateContent(String content, String templateSelector, boolean html) throws CmsException {
        System.err.println("*** NOW WRITING BACK CONTENT. HTML = " + html);
        System.err.println("*** TEMPLATE SELECTOR IS: " + templateSelector);
        System.err.println(content);
        System.err.println("-----------------");
        String datablockName = this.getTemplateDatablockName(templateSelector);
        System.err.println("*** DATABLOCK NAME IS: " + datablockName);
        Element data = getData(datablockName);

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
            //content = replace(content, "]", "><![CDATA[");             
            tempXmlString.append(content);
            tempXmlString.append("]]>");
        } else {
            tempXmlString.append(content);
        }            
        tempXmlString.append("</template>\n");
        tempXmlString.append("</" + getXmlDocumentTagName() + ">\n");
        
        System.err.println(new String(tempXmlString));
        
        I_CmsXmlParser parser = getXmlParser();
        StringReader parserReader = new StringReader(tempXmlString.toString());
        Document tempDoc = null;
        try {
            tempDoc = parser.parse(parserReader);            
        } catch(Exception e) {
            throwException("PARSING ERROR!", CmsException.C_XML_PARSING_ERROR);
        }
        
        Element templateNode = (Element)tempDoc.getDocumentElement().getFirstChild();
        System.err.println("### NOW SETTING DATABLOCK WITH NEW CONTENT: " + datablockName);
        setData(datablockName, templateNode);
    }
                                        
    public String getTextEditableTemplateContent(Object callingObject, Hashtable parameters, String templateSelector) throws CmsException {
        String datablockName = this.getTemplateDatablockName(templateSelector);
        Element data = getData(datablockName);
        StringBuffer result = new StringBuffer();
        
        Document tempDoc = (Document)getXmlDocument().cloneNode(true);
        Element rootElem = tempDoc.getDocumentElement();
        
        while(rootElem.hasChildNodes()) {
            rootElem.removeChild(rootElem.getFirstChild());
        }
        data = (Element)getXmlParser().importNode(tempDoc, data);
        rootElem.appendChild(data);       
        StringWriter out = new StringWriter();        
        getXmlParser().getXmlText(tempDoc, out);
        String xmlString = out.toString();
        
        int endOpeningXmlTag = xmlString.indexOf(">");
        int endOpeningDocTag = xmlString.indexOf(">", endOpeningXmlTag + 1);
        int endOpeningBodyTag = xmlString.indexOf(">", endOpeningDocTag + 1) + 1;
        
        int startClosingDocTag = xmlString.lastIndexOf("<");
        int startClosingBodyTag = xmlString.lastIndexOf("<", startClosingDocTag - 1);
        
        xmlString = xmlString.substring(endOpeningBodyTag, startClosingBodyTag);
        xmlString = xmlString.trim();
                
        return xmlString;
    }
        
    public String getEditableTemplateContent(Object callingObject, Hashtable parameters, String templateSelector) throws CmsException {

            System.err.println("### And still the content is:");
            System.err.println(getXmlText());
        
        String datablockName = this.getTemplateDatablockName(templateSelector);
        Element data = getData(datablockName);
        System.err.println("+++++++++ " + getDataValue(datablockName));
        StringBuffer result = new StringBuffer();
        
        Document tempDoc = (Document)getXmlDocument().cloneNode(true);
        Element rootElem = tempDoc.getDocumentElement();
        
        while(rootElem.hasChildNodes()) {
            rootElem.removeChild(rootElem.getFirstChild());
        }
        data = (Element)getXmlParser().importNode(tempDoc, data);
        rootElem.appendChild(data);       
        
        // Scan for cdatas
        Node n = data;
        Vector cdatas = new Vector();
        while(n != null) {
            if(n.getNodeType() == n.CDATA_SECTION_NODE) {
                cdatas.addElement(n.getNodeValue());                
                n.setNodeValue("");
            }
            n = treeWalker(n);
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
        
        int cdataStart = xmlString.indexOf("<![CDATA[");
        int currentPos = 0;
        int loop = 0;
        
        result.append("<HTML>\n<HEAD></HEAD>\n");
        result.append("<BODY " + getProcessedDataValue("bodytag", callingObject, parameters) + ">\n");
                
        while(cdataStart != -1) {
            result.append(xmlString.substring(currentPos, cdataStart).replace('<', '[').replace('>', ']'));
            result.append((String)cdatas.elementAt(loop++));
            cdataStart = xmlString.indexOf("<![CDATA[", cdataStart + 1);
            currentPos = xmlString.indexOf("]]>", currentPos + 1) + 3;
        }
        result.append(xmlString.substring(currentPos).replace('<', '[').replace('>', ']'));
        
        result.append("\n</BODY>\n</HTML>");        
        return result.toString();
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
                        name = "-- unnamed --";
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
        int lastindex = 0;
        while(index != -1) {
            System.err.println("### substring: lastindex " + lastindex + "    index " + index);
            String sub = s.substring(lastindex, index);
            tempContent.append(sub);
            if(s.charAt(index) == '[') {                
                tempContent.append("]]><");
            } else {
                tempContent.append("><![CDATA[");
            }
            lastindex = index + 1;
            //index = s.indexOf(search, index+1);
            index = min(s.indexOf("[", index+1), s.indexOf("]", index+1));
        }
        System.err.println("### substring: lastindex " + lastindex);
        tempContent.append(s.substring(lastindex));
        return new String(tempContent);
    }
        
}
