/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/page/CmsXmlPage.java,v $
 * Date   : $Date: 2004/06/08 14:13:59 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.page;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.util.XMLErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Implementation of a page object used to access and manage xml data.<p>
 * 
 * This implementation consists of several named elements optionally available for 
 * various languages. The data of each element is accessible via its name and language. 
 * 
 * The content of each element is stored as CDATA, links within the 
 * content are processed and are seperately accessible as entries of a CmsLinkTable.
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 */
public class CmsXmlPage {
    
    /** Name of the name attribute of the elements node */
    private static final String C_ATTRIBUTE_ENABLED = "enabled";    

    /** Name of the internal attribute of the link node */
    private static final String C_ATTRIBUTE_INTERNAL = "internal";

    /** Name of the language attribute of the elements node */
    private static final String C_ATTRIBUTE_LANGUAGE = "language";

    /** Name of the name attribute of the elements node */
    private static final String C_ATTRIBUTE_NAME = "name";

    /** Name of the type attribute of the elements node */
    private static final String C_ATTRIBUTE_TYPE = "type";
    
    /** Name of the root document node */
    private static final String C_DOCUMENT_NODE = "page";

    /** Name of the anchor node */
    private static final String C_NODE_ANCHOR = "anchor";
    
    /** Name of the element node */
    private static final String C_NODE_CONTENT = "content";

    /** Name of the element node */
    private static final String C_NODE_ELEMENT = "element";
    
    /** Name of the elements node */
    public static final String C_NODE_ELEMENTS = "elements";

    /** Name of the link node */
    public static final String C_NODE_LINK = "link";
    
    /** Name of the links node */
    public static final String C_NODE_LINKS = "links";
    
    /** Name of the query node */
    private static final String C_NODE_QUERY = "query";
    
    /** Name of the target node */
    private static final String C_NODE_TARGET = "target";
        
    /** Property to check if relative links are allowed */
    private static final String C_PROPERTY_ALLOW_RELATIVE = "allowRelativeLinks";

    /** The DTD address of the OpenCms xmlpage */
    public static final String C_XMLPAGE_DTD_SYSTEM_ID = CmsConfigurationManager.C_DEFAULT_DTD_PREFIX + "xmlpage.dtd";    
    
    /** Indicates if relative Links are allowed */
    private boolean m_allowRelativeLinks;
    
    /** Reference for named elements in the page */
    private Map m_bookmarks;
    
    /** The document object of the page */
    private Document m_document;
    
    /** The encoding to use for this xml page */    
    private String m_encoding;
    
    /** The file that contains the page data (note: is not set when creating an empty or document based CmsXmlPage) */
    private CmsFile m_file;

    /** Set of locales contained in this page */
    private Set m_locales;
    
    /** Maps element names to available locales */
    private Map m_elementLocales;
    
    /**
     * Creates a new CmsXmlPage based on the provided document and encoding.<p>
     * 
     * The encoding is used when saving/serializing the XML document.<p>
     * 
     * @param document the document to create the CmsXmlPage from
     * @param encoding the encoding of the xml page
     */
    public CmsXmlPage(Document document, String encoding) {
        m_document = document;
        m_encoding = encoding;
        initBookmarks();
    }

    /**
     * Creates a new empty CmsXmlPage with the provided encoding.<p>
     * 
     * The page is initialized according to the minimal neccessary xml structure.
     * The encoding is used when saving/serializing the XML document.<p>
     * 
     * @param encoding the encoding of the xml page
     */
    public CmsXmlPage(String encoding) {
        m_encoding = encoding;
        initDocument();
        initBookmarks();
    }
    
    /**
     * Reads the xml contents of a file into the page.<p>
     * 
     * @param cms the current cms object
     * @param file the file with xml data
     * @return the concrete PageObject instanciated with the xml data
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage read(CmsObject cms, CmsFile file) throws CmsXmlException {
        return read(cms, file, true);
    }
        
    /**
     * Reads the xml contents of a file into the page, using wither the encoding set
     * in the xml file header, or the encoding set in the VFS file property.<p>
     * 
     * If you are not sure about the implications of the encoding issues, 
     * use {@link #read(byte[], String)} instead.<p>
     * 
     * @param cms the current cms object
     * @param file the file with xml data
     * @param keepEncoding if true, the encoding spefified in the xml file head is used, 
     *    otherwise the encoding from the file poperty is used
     * @return the concrete PageObject instanciated with the xml data
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage read(CmsObject cms, CmsFile file, boolean keepEncoding) throws CmsXmlException {

        CmsXmlPage newPage = null;
        
        byte[] content = file.getContents();

        String allowRelative;
        try {
            allowRelative = cms.readPropertyObject(cms.readAbsolutePath(file), C_PROPERTY_ALLOW_RELATIVE, false).getValue("false");
        } catch (CmsException e) {
            allowRelative = Boolean.toString(false);
        }
        
        String encoding;
        try { 
            encoding = cms.readPropertyObject(cms.readAbsolutePath(file), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());
        } catch (CmsException e) {
            encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        }        
                
        if (content.length > 0) {
            // content is initialized
            if (keepEncoding) {
                // use the encoding from the content
                newPage = read(content, encoding);
            } else {
                // use the encoding from the file property
                // this usually only triggered by a save operation                
                try {
                    String contentStr = new String(content, encoding);
                    newPage = read(contentStr, encoding); 
                } catch (UnsupportedEncodingException e) {
                    throw new CmsXmlException("Invalid encoding selected for xmlPage: " + encoding, e);
                }                
            }
        } else {
            // content is empty
            newPage = new CmsXmlPage(encoding);
        }
        
        newPage.m_file = file;
        newPage.m_allowRelativeLinks = Boolean.valueOf(allowRelative).booleanValue();
        
        return newPage;
    }    

    /**
     * Reads the xml contents from a string into the page.<p>
     * 
     * @param xmlData the xml data in a String 
     * @param encoding the encoding to use when serializing/saving the xml page
     * @return the page initialized with the given xml data
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage read(String xmlData, String encoding) throws CmsXmlException {        
        try {
            byte[] xmlBytes = xmlData.getBytes(encoding);
            return read(xmlBytes, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new CmsXmlException("Reading xml page from a String failed: " + e.getMessage(), e);
        }
    }

    /**
     * Reads the xml contents from a byte array into the page.<p>
     * 
     * @param xmlData the xml data in a byte array
     * @param encoding the encoding to use when serializing/saving the xml page
     * @return the page initialized with the given xml data
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage read(byte[] xmlData, String encoding) throws CmsXmlException {
        try {
            SAXReader reader = new SAXReader();
            reader.setEntityResolver(CmsXmlEntityResolver.getResolver());
            reader.setMergeAdjacentText(true);
            Document document = reader.read(new ByteArrayInputStream(xmlData));
            return new CmsXmlPage(document, encoding);
        } catch (DocumentException e) {
            throw new CmsXmlException("Reading xml page from a byte array failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Adds a new empty element with the given name and language.<p>
     *  
     * @param name name of the element, must be unique
     * @param locale locale of the element
     */
    public void addElement(String name, Locale locale) {
        Element elements = m_document.getRootElement().element(C_NODE_ELEMENTS);        
        Element element = elements.addElement(C_NODE_ELEMENT)
              .addAttribute(C_ATTRIBUTE_NAME, name)
              .addAttribute(C_ATTRIBUTE_LANGUAGE, locale.toString());       
        element.addElement(C_NODE_LINKS);
        element.addElement(C_NODE_CONTENT);
        setBookmark(name, locale, true, element);
    }

    /**
     * Returns if relative links are accepted (and left unprocessed).<p>
     * 
     * @return true if relative links are allowed
     */
    public boolean getAllowRelativeLinks() {
        return m_allowRelativeLinks;
    }
    
    /**
     * Returns the display content (processed data) of an element.<p>
     * 
     * @param cms the cms object
     * @param name name of the element
     * @param locale locale of the element
     * @return the display content or the empty string "" if the element dos not exist
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public String getContent(CmsObject cms, String name, Locale locale) throws CmsXmlException {    
        return getContent(cms, name, locale, false);
    }

    /**
     * Returns the display content (processed data) of an element.<p>
     * 
     * @param cms the cms object
     * @param name name of the element
     * @param locale locale of the element
     * @param forEditor indicates that link processing should be done for editing purposes
     * @return the display content or the empty string "" if the element dos not exist
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public String getContent(CmsObject cms, String name, Locale locale, boolean forEditor) throws CmsXmlException {

        Element element = getBookmark(name, locale);        
        String content = "";
        
        if (element != null) {

            Element data = element.element(C_NODE_CONTENT);
            Attribute enabled = element.attribute(C_ATTRIBUTE_ENABLED);
            
            if (enabled == null || "true".equals(enabled.getValue())) {
            
                content = data.getText();
                
                CmsLinkTable linkTable = getLinkTable(name, locale);
                if (!linkTable.isEmpty()) {
                    
                    CmsLinkProcessor macroReplacer = new CmsLinkProcessor(linkTable);
                
                    try {                    
                        content = macroReplacer.processLinks(cms, content, getEncoding(), forEditor);
                    } catch (Exception exc) {
                        throw new CmsXmlException ("HTML data processing failed", exc);
                    }
                } 
            }
        }
        
        return content;
    }
    
    /**
     * Returns the encoding used for the page content.<p>
     * 
     * @return the encoding used for the page content
     */
    public String getEncoding() {
        return m_encoding;
    }
    
    /**
     * Returns the file with the xml page content or <code>null</code> if not set.<p>
     * 
     * @return the file with the xml page content
     */
    public CmsFile getFile() {
        return m_file;
    }

    /**
     * Returns the link table of an element.<p>
     * 
     * @param name name of the element
     * @param locale locale of the element
     * @return the link table
     */
    public CmsLinkTable getLinkTable(String name, Locale locale) {

        Element element = getBookmark(name, locale);
        Element links = element.element(C_NODE_LINKS);
        
        CmsLinkTable linkTable = new CmsLinkTable();
        
        if (links != null) {
            for (Iterator i = links.elementIterator(C_NODE_LINK); i.hasNext();) {
                        
                Element lelem = (Element)i.next();
                Attribute lname = lelem.attribute(C_ATTRIBUTE_NAME);
                Attribute type = lelem.attribute(C_ATTRIBUTE_TYPE);
                Attribute internal = lelem.attribute(C_ATTRIBUTE_INTERNAL);
                
                Element target = lelem.element(C_NODE_TARGET);
                Element anchor = lelem.element(C_NODE_ANCHOR);
                Element query  = lelem.element(C_NODE_QUERY);
                
                CmsLink link = new CmsLink(
                        lname.getValue(), 
                        type.getValue(), 
                        (target != null) ? target.getText() : null, 
                        (anchor != null) ? anchor.getText() : null, 
                        (query  != null) ? query.getText()  : null, 
                        Boolean.valueOf(internal.getValue()).booleanValue());
                
                linkTable.addLink(link);
            }        
        }    
        return linkTable;
    }
    
    /**
     * Returns a List of all locales that have at last one element in this page.<p>
     * 
     * @return a List of all locales that have at last one element in this page
     */
    public List getLocales() {    
        return new ArrayList(m_locales);
    }
    
    /**
     * Returns a List of all locales that have the named element set in this page.<p>
     * 
     * @param element the element to look up the locale List for
     * @return a List of all locales that have the named element set in this page
     */
    public List getLocales(String element) {
        Object result = m_elementLocales.get(element);
        if (result == null) {
            return Collections.EMPTY_LIST;
        }        
        return new ArrayList((Set)result);
    }
    
    /**
     * Returns all available elements for a given language.<p>
     * 
     * @param locale the locale
     * @return list of available elements
     */
    public List getNames(Locale locale) {        
        List names = new ArrayList();
        String localeName = locale.toString();

        for (Iterator i = getBookmarks().iterator(); i.hasNext();) {
            String name = (String)i.next();
            if (name.startsWith(localeName + "|")) {
                names.add(name.substring(localeName.length() + 1));
            }
        }
        return names;
    }

    /**
     * Returns the raw (unprocessed) content of an element.<p>
     * 
     * @param name  name of the element
     * @param locale locale of the element
     * @return the raw (unprocessed) content
     */
    public String getRawContent(String name, Locale locale) {
        
        Element element = getBookmark(name, locale);        
        String content = "";
        
        if (element != null) {

            Element data = element.element(C_NODE_CONTENT);
            Attribute enabled = element.attribute(C_ATTRIBUTE_ENABLED);
            
            if (enabled == null || "true".equals(enabled.getValue())) {
                
                content = data.getStringValue();
                // content = data.getText();
            }
        }
        
        return content;
    }

    /**
     * Checks if the page object contains a name specified by name and language.<p>
     * 
     * @param name the name of the element
     * @param locale the locale of the element
     * @return true if this element exists
     */
    public boolean hasElement(String name, Locale locale) {    
        return getBookmark(name, locale) != null;
    }

    /**
     * Checks if the element of a page object is enabled.<p>
     * 
     * @param name the name of the element
     * @param locale the locale of the element
     * @return true if the element exists and is not disabled
     */
    public boolean isEnabled(String name, Locale locale) {

        Element element = getBookmark(name, locale);

        if (element != null) {
            Attribute enabled = element.attribute(C_ATTRIBUTE_ENABLED);            
            return (enabled == null || Boolean.valueOf(enabled.getValue()).booleanValue());
        }
        
        return false;
    }

    /**
     * Removes an existing element with the given name and locale.<p>
     * 
     * @param name name of the element
     * @param locale the locale of the element
     */
    public void removeElement(String name, Locale locale) {        
        Element elements = m_document.getRootElement().element(C_NODE_ELEMENTS);        
        Element element = removeBookmark(name, locale);
        elements.remove(element);
    }
    
    /**
     * Sets the data of an already existing element.<p>
     * 
     * The data will be enclosed as CDATA within the xml page structure.
     * When setting the element data, the content of this element will be
     * processed automatically.<p>
     * 
     * @param cms the cms object
     * @param name name of the element
     * @param locale locale of the element
     * @param content character data (CDATA) of the element
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public void setContent(CmsObject cms, String name, Locale locale, String content) throws CmsXmlException {
        
        Element element = getBookmark(name, locale);
        Element data = element.element(C_NODE_CONTENT);
        Element links = element.element(C_NODE_LINKS);
        CmsLinkTable linkTable = new CmsLinkTable();
        
        // ensure all chars in the given content are valid chars for the selected charset
        content = CmsEncoder.adjustHtmlEncoding(content, getEncoding());
        
        try {

            CmsLinkProcessor linkReplacer = new CmsLinkProcessor(linkTable);
        
            data.setContent(null);
            if (!m_allowRelativeLinks && m_file != null) {
                String relativeRoot = CmsResource.getParentFolder(cms.readAbsolutePath(m_file));
                data.addCDATA(linkReplacer.replaceLinks(cms, content, getEncoding(), relativeRoot));
            } else {
                data.addCDATA(linkReplacer.replaceLinks(cms, content, getEncoding(), null));
            }
                        
        } catch (Exception exc) {
            throw new CmsXmlException ("HTML data processing failed", exc);
        }
        
        links.setContent(null);
        for (Iterator i = linkTable.iterator(); i.hasNext();) {
            CmsLink link = linkTable.getLink((String)i.next());
            
            Element linkElement = links.addElement(C_NODE_LINK)
                .addAttribute(C_ATTRIBUTE_NAME, link.getName())
                .addAttribute(C_ATTRIBUTE_TYPE, link.getType())
                .addAttribute(C_ATTRIBUTE_INTERNAL, Boolean.toString(link.isInternal()));
                
            linkElement.addElement(C_NODE_TARGET)
                .addCDATA(link.getTarget());
            
            if (link.getAnchor() != null) {
                linkElement.addElement(C_NODE_ANCHOR)
                    .addCDATA(link.getAnchor());
            }
            
            if (link.getQuery() != null) {
                linkElement.addElement(C_NODE_QUERY)
                    .addCDATA(link.getQuery());
            }
        }
    }

    /**
     * Sets the enabled flag of an already existing element.<p>
     * 
     * Note: if isEnabled is set to true, the attribute is removed
     * since true is the default
     * 
     * @param name name name of the element
     * @param locale locale of the element
     * @param isEnabled enabled flag for the element
     */
    public void setEnabled(String name, Locale locale, boolean isEnabled) {

        Element element = getBookmark(name, locale);
        Attribute enabled = element.attribute(C_ATTRIBUTE_ENABLED);
        
        if (enabled == null) {
            element.addAttribute(C_ATTRIBUTE_ENABLED, Boolean.toString(isEnabled));
        } else if (isEnabled) {
            element.remove(enabled);
        } else {
            enabled.setValue(Boolean.toString(isEnabled));
        }
    }
    
    /**
     * Validates the HTML code of each content element of the page.<p>
     * 
     * @param cms the current cms object
     * @return the corrected CmsFile
     * @throws CmsXmlException if validation fails
     */
    public CmsFile correctHtmlStructure(CmsObject cms) throws CmsXmlException {

        // we must loop through all locales and elements to check all the content elements
        // if they contain correct HTML
        List elementNames;
        String elementName;
        String content;       
        
        // iterate over all locales
        Iterator i = m_locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            elementNames = getNames(locale);

            // iterate over all body elements per language
            Iterator j = elementNames.iterator();
            while (j.hasNext()) {
                elementName = (String) j.next();
                // get the content of this element
                // by accessing it that way, it will get a processed content string
                // which contains links and valid html
                content = getContent(cms, elementName, locale, false);
                // put the new content into the element
                // saving the content will process and validate the content string again
                setContent(cms, elementName, locale, content);                                  
            }
        }
        // write the modifed xml back to the xmlpage 
        return write();
    }    
    
    /**
     * Validates the xml structure of the page with the xmlpage dtd.<p>
     * 
     * This is required in case someone modifies the xml structure of a  
     * xmlpage file using the "edit control code" option.<p>
     * 
     * @throws CmsXmlException if the validation fails
     */
    public void validateXmlStructure() throws CmsXmlException  {

        XMLReader reader;
        try {
            reader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        } catch (SAXException e) {
            // xerces parser not available - no schema validation possible
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Could not initialize Xerces SAX reader for validation", e);
            }
            // no validation of the content is possible
            return;
        }
        // turn on validation
        try {
            reader.setFeature("http://xml.org/sax/features/validation", true);
            // turn on schema validation
            reader.setFeature("http://apache.org/xml/features/validation/schema", true);
            // configure namespace support
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
        } catch (SAXNotRecognizedException e) {
            // should not happen as Xerces 2 support this feature
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Required SAX reader feature not recognized", e);
            }
            // no validation of the content is possible
            return;
        } catch (SAXNotSupportedException e) {
            // should not happen as Xerces 2 support this feature
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Required SAX reader feature not supported", e);
            }
            // no validation of the content is possible
            return;
        }

        // set the resolver for the "opencms://" URIs
        reader.setEntityResolver(CmsXmlEntityResolver.getResolver());

        // add an error handler which turns any errors into XML
        XMLErrorHandler errorHandler = new XMLErrorHandler();
        reader.setErrorHandler(errorHandler);

        String content = null;
        try {
            // generate a new byte array from the content and parse this
            byte[] contentBytes = ((ByteArrayOutputStream)write(new ByteArrayOutputStream(512), getEncoding())).toByteArray();
            content = new String(contentBytes, getEncoding());
            reader.parse(new InputSource(new ByteArrayInputStream(contentBytes)));
        } catch (IOException e) {
            // should not happen since we read form a byte array
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Could not read XML from byte array", e);
            }
            return;
        } catch (SAXException e) {
            // should not happen since all errors are handled in the XML error handler
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Unexpected SAX exception while parsing content", e);
            }
            return;
        }

        if (errorHandler.getErrors().elements().size() > 0) {                        
            // there was at last one validation error, so throw an exception
            StringWriter out = new StringWriter(256);
            OutputFormat format = OutputFormat.createPrettyPrint();            
            XMLWriter writer = new XMLWriter(out, format);   
            try {
                writer.write(errorHandler.getErrors());
                writer.close();
            } catch (IOException e) {
                // should not happen since we write to a StringWriter
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Unexpected IO exception while writing to StringWriter", e);
                }
            }
            if (content != null) {
                out.write("\n\nThe verfified XML page content was:");
                out.write("\n-------------------------------------------------------------------\n");
                out.write(content);
                out.write("\n-------------------------------------------------------------------\n");
            }
            throw new CmsXmlException("XML validation error:\n" + out.toString() + "\n");
        }        
    }
        
    /**
     * Writes the xml contents into the assigned CmsFile,
     * using currently selected encoding.<p>
     * 
     * @return the assigned file with the xml content
     * @throws CmsXmlException if something goes wrong
     */
    public CmsFile write() throws CmsXmlException {        
        return write(m_file, m_encoding);
    }
    
    /**
     * Writes the xml contents into the CmsFile,
     * using currently selected encoding.<p>
     * 
     * @param file the file to write the xml
     * @return the file with the xml content
     * @throws CmsXmlException if something goes wrong
     */
    public CmsFile write(CmsFile file) throws CmsXmlException {        
        return write(file, m_encoding);
    }
    
    /**
     * Writes the xml contents into the CmsFile.<p>
     * 
     * @param file the file to write the xml
     * @param encoding the encoding to use
     * @return the file with the xml content
     * @throws CmsXmlException if something goes wrong
     */
    public CmsFile write(CmsFile file, String encoding) throws CmsXmlException {        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        file.setContents(((ByteArrayOutputStream)write(out, encoding)).toByteArray());
        
        return file;
    }

    /**
     * Writes the xml contents into an output stream.<p>
     * 
     * @param out the output stream to write to
     * @param encoding the encoding to use
     * @return the output stream with the xml content
     * @throws CmsXmlException if something goes wrong
     */
    public OutputStream write(OutputStream out, String encoding) throws CmsXmlException {        
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding(encoding);
            
            XMLWriter writer = new XMLWriter(out, format);
            writer.setEscapeText(false);
            
            // ensure xml page has proper system doc type set
            DocumentType type = m_document.getDocType();
            if (type != null) {
                String systemId = type.getSystemID();
                if ((systemId != null) && systemId.endsWith(CmsXmlEntityResolver.C_XMLPAGE_DTD_OLD_SYSTEM_ID)) {
                    m_document.addDocType(C_DOCUMENT_NODE, "", C_XMLPAGE_DTD_SYSTEM_ID);
                }
            }
            
            writer.write(m_document);
            writer.close();
            
        } catch (Exception exc) {
            throw new CmsXmlException("Writing xml page failed", exc);
        }
        
        return out;
    }
    
    /**
     * Writes the xml contents in the assigned CmsFile using the given encoding.<p>
     * 
     * @param encoding the encoding to use
     * @return the assigned file with the xml content
     * @throws CmsXmlException if something goes wrong
     */
    public CmsFile write(String encoding) throws CmsXmlException {        
        return write(m_file, encoding);
    }
    
    /**
     * Returns the bookmarked element for the given key.<p>
     * 
     * @param name the name of the element
     * @param locale the locale of the element
     * @return the bookemarked element
     */
    protected Element getBookmark(String name, Locale locale) {     
        if (locale != null) {
            return (Element)m_bookmarks.get(locale.toString() + "|" + name);
        } else {
            return (Element)m_bookmarks.get(name);
        }
    }
    
    /**
     * Returns all keys for bookmarked elements.<p>
     * 
     * @return the keys of bookmarked elements
     */
    protected Set getBookmarks() {
        return (m_bookmarks != null)? m_bookmarks.keySet() : new HashSet(); 
    }
    
    /**
     * Initializes the bookmarks according to the named elements in the document.<p>
     */
    protected void initBookmarks() {

        m_bookmarks = new HashMap();
        m_locales = new HashSet();
        m_elementLocales = new HashMap();
        
        for (Iterator i = m_document.getRootElement().element(C_NODE_ELEMENTS).elementIterator(C_NODE_ELEMENT); i.hasNext();) {
   
            Element elem = (Element)i.next();
            try {
                String elementName = elem.attributeValue(C_ATTRIBUTE_NAME);
                String elementLang = elem.attributeValue(C_ATTRIBUTE_LANGUAGE);
                String elementEnabled = elem.attributeValue(C_ATTRIBUTE_ENABLED);
                boolean enabled = (elementEnabled==null)?true:Boolean.valueOf(elementEnabled).booleanValue();
                setBookmark(elementName, CmsLocaleManager.getLocale(elementLang), enabled, elem);              
            } catch (NullPointerException e) {
                OpenCms.getLog(this).error("Error while initalizing xmlPage bookmarks", e);                
            }    
        }
    }
    
    /**
     * Initializes the internal document object.<p>
     */
    protected void initDocument() {        
        m_document = DocumentHelper.createDocument(DocumentHelper.createElement(C_DOCUMENT_NODE));
        m_document.addDocType(C_DOCUMENT_NODE, "", C_XMLPAGE_DTD_SYSTEM_ID);
        m_document.getRootElement().addElement(C_NODE_ELEMENTS);
    }
    
    /**
     * Removes a bookmark with a given key.<p>
     * 
     * @param name the name of the element
     * @param locale the locale of the element
     * @return the element removed from the bookmarks or null
     */
    protected Element removeBookmark(String name, Locale locale) {
        if (locale != null) {
            return (Element)m_bookmarks.remove(locale.toString() + "|" + name);
        } else {
            int warning = 0;
            return (Element)m_bookmarks.remove(name);
        }
    }
    
    /**
     * Adds a bookmark for the given element.<p>
     * 
     * @param name the name of the element
     * @param locale the locale of the element
     * @param element the element to bookmark
     */
    protected void setBookmark(String name, Locale locale, boolean enabled, Element element) {        
        if (locale != null) {
            m_locales.add(locale);
            m_bookmarks.put(locale.toString() + "|" + name, element);
            if (enabled) {
                Object o = m_elementLocales.get(name);
                if (o != null) {
                    Set set = (Set)o;
                    set.add(locale);
                } else {
                    Set set = new HashSet();
                    set.add(locale);
                    m_elementLocales.put(name, set);
                }
            }
        } else {
            int warning = 0;
            m_bookmarks.put(name, element);
        }
    }            
    
}