/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/page/Attic/CmsXmlPage.java,v $
 * Date   : $Date: 2004/01/22 18:10:38 $
 * Version: $Revision: 1.22 $
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
package org.opencms.page;

import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

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
 * @version $Revision: 1.22 $
 */
public class CmsXmlPage {
    
    /** Property to check if relative links are allowed */
    private static final String C_PROPERTY_ALLOW_RELATIVE = "allowRelativeLinks";
    
    /** Name of the root document node */
    private static final String C_DOCUMENT_NODE = "page";
    
    /** Link to the external document type of this xml page */
    private static final String C_DOCUMENT_TYPE = "/system/shared/page.dtd";
    
    /** Reference for named elements */
    private Map m_bookmarks = null;

    /** The document object of the page */
    private Document m_document = null;
    
    /** The file that contains the page data (note: is not set when creating an empty or document based CmsXmlPage) */
    private CmsFile m_file = null;
    
    /** Indicates if relative Links are allowed */
    private boolean m_allowRelativeLinks = false;
    
    /**
     * Creates a new empty CmsXmlPage.<p>
     * 
     * The page is initialized according to the minimal neccessary xml structure.
     */
    public CmsXmlPage() {
        initDocument();
        initBookmarks();
    }
    
    /**
     * Creates a new CmsXmlPage based on the provided document.<p>
     * 
     * @param document the document to create the CmsXmlPage from
     */
    public CmsXmlPage(Document document) {
        m_document = document;
        initBookmarks();
    }
    
    /**
     * Adds a new empty element with the given name and language.<p>
     *  
     * @param name name of the element, must be unique
     * @param language language of the element
     */
    public void addElement(String name, String language) {

        Element elements = m_document.getRootElement().element("elements");
        
        Element element = elements.addElement("element")
              .addAttribute("name", name)
              .addAttribute("language", language);
       
        element.addElement("links");
        element.addElement("content");

        setBookmark (name, language, element);
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
     * Returns the bookmarked element for the given key.<p>
     * 
     * @param name the name of the element
     * @param language the language of the element
     * @return the bookemarked element
     */
    protected Element getBookmark (String name, String language) {        
        return (Element) m_bookmarks.get(language + "_" + name);
    }
    
    /**
     * Returns all keys for bookmarked elements.<p>
     * 
     * @return the keys of bookmarked elements
     */
    protected Set getBookmarks() {        
        return m_bookmarks.keySet(); 
    }
    
    /**
     * Returns the display content (processed data) of an element.<p>
     * 
     * @param cms the cms object
     * @param name name of the element
     * @param language language of the element
     * @return the display content or the empty string "" if the element dos not exist
     * 
     * @throws CmsPageException if something goes wrong
     */
    public String getContent(CmsObject cms, String name, String language)
        throws CmsPageException {
    
        return getContent(cms, name, language, false);
    }

    /**
     * Returns the display content (processed data) of an element.<p>
     * 
     * @param cms the cms object
     * @param name name of the element
     * @param language language of the element
     * @param forEditor indicates that link processing should be done for editing purposes
     * @return the display content or the empty string "" if the element dos not exist
     * 
     * @throws CmsPageException if something goes wrong
     */
    public String getContent(CmsObject cms, String name, String language, boolean forEditor) 
        throws CmsPageException {

        Element element = getBookmark(name, language);        
        String content = "";
        
        if (element != null) {

            Element data = element.element("content");
            Attribute enabled = element.attribute("enabled");
            
            if (enabled == null || enabled.getValue().equals("true")) {
            
                content = data.getText();
                
                CmsLinkTable linkTable = getLinkTable(name, language);
                if (!linkTable.isEmpty()) {
                    
                    CmsLinkProcessor macroReplacer = new CmsLinkProcessor(linkTable);
                
                    try {
                    
                        content = macroReplacer.processLinks(cms, content, forEditor);
                    } catch (Exception exc) {
                        throw new CmsPageException ("HTML data processing failed", exc);
                    }
                } 
            }
        }
        
        return content;
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
     * Returns all languages with available elements.<p>
     * 
     * @return list of languages with available elements
     */
    public Set getLanguages() {
    
        Set languages = new HashSet();
        for (Iterator i = getBookmarks().iterator(); i.hasNext();) {
            String name = (String)i.next();
            if (name.indexOf("_") >= 0) {
                String language = name.substring(0, name.indexOf("_"));
                languages.add(language);
            }
        }
        return languages;
    }

    /**
     * Returns the link table of an element.<p>
     * 
     * @param name name of the element
     * @param language language of the element
     * @return the link table
     */
    public CmsLinkTable getLinkTable(String name, String language) {

        Element element = getBookmark(name, language);
        Element links = element.element("links");
        
        CmsLinkTable linkTable = new CmsLinkTable();
        
        for (Iterator i = links.elementIterator("link"); i.hasNext();) {
                    
            Element lelem = (Element)i.next();
            Attribute lname = lelem.attribute("name");
            Attribute type = lelem.attribute("type");
            Attribute internal = lelem.attribute("internal");
            
            Element target = lelem.element("target");
            Element anchor = lelem.element("anchor");
            Element query  = lelem.element("query");
            
            CmsLink link = new CmsLink(
                    lname.getValue(), 
                    type.getValue(), 
                    (target != null) ? target.getText() : null, 
                    (anchor != null) ? anchor.getText() : null, 
                    (query  != null) ? query.getText()  : null, 
                    Boolean.valueOf(internal.getValue()).booleanValue());
            
            linkTable.addLink(link);
        }        
        
        return linkTable;
    }
    
    /**
     * Returns all available elements for a given language.<p>
     * 
     * @param language language
     * @return list of available elements
     */
    public List getNames(String language) {
        
        List names = new ArrayList();
        for (Iterator i = getBookmarks().iterator(); i.hasNext();) {
            String name = (String)i.next();
            if (name.startsWith(language+"_")) {
                names.add(name.substring(language.length()+1));
            }
        }
        return names;
    }

    /**
     * Checks if the page object contains a name specified by name and language.<p>
     * 
     * @param name the name of the element
     * @param language the language of the element
     * @return true if this element exists
     */
    public boolean hasElement(String name, String language) {
    
        return getBookmark(name, language) != null;
    }

    /**
     * Checks if the element of a page object is enabled.<p>
     * 
     * @param name the name of the element
     * @param language the language of the element
     * @return true if the element exists and is not disabled
     */
    public boolean isEnabled(String name, String language) {

        Element element = getBookmark(name, language);

        if (element != null) {

            Attribute enabled = element.attribute("enabled");
            
            return (enabled == null || Boolean.valueOf(enabled.getValue()).booleanValue());
        }
        
        return false;
    }
    
    /**
     * Initializes the bookmarks according to the named elements in the document.<p>
     */
    protected void initBookmarks() {

        m_bookmarks = new HashMap();
        
        for (Iterator i = m_document.getRootElement().element("elements").elementIterator("element"); i.hasNext();) {
   
            Element elem = (Element)i.next();
            String elementName = elem.attribute("name").getValue();
            String elementLang = elem.attribute("language").getValue();
    
            setBookmark(elementName, elementLang, elem);              
        }
    }
    
    /**
     * Initializes the internal document object.<p>
     */
    protected void initDocument() {
        
        m_document = DocumentHelper.createDocument(DocumentHelper.createElement(C_DOCUMENT_NODE));
        m_document.addDocType(C_DOCUMENT_NODE, "", C_DOCUMENT_TYPE);
        m_document.getRootElement().addElement("elements");
    }
    
    /**
     * Reads the xml contents of a file into the page.<p>
     * 
     * @param cms the current cms object
     * @param file the file with xml data
     * @return the concrete PageObject instanciated with the xml data
     * @throws CmsPageException if something goes wrong
     */
    public static CmsXmlPage read(CmsObject cms, CmsFile file) 
        throws CmsPageException {

        CmsXmlPage newPage = null;
        
        byte[] content = file.getContents();

        String allowRelative;
        try {
            allowRelative = cms.readProperty(cms.readAbsolutePath(file), C_PROPERTY_ALLOW_RELATIVE, false, "false");
        } catch (CmsException e) {
            allowRelative = "false";
        }
        
        if (content.length > 0) {
            // content is initialized
            String encoding;
            try { 
                encoding = cms.readProperty(cms.readAbsolutePath(file), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, false, OpenCms.getDefaultEncoding());
            } catch (CmsException e) {
                encoding = OpenCms.getDefaultEncoding();
            }
            
            String xmlData;
            try {
                xmlData = new String(content, encoding);
            } catch (UnsupportedEncodingException e) {
                xmlData = new String(content);
            }            
            newPage = read(cms, xmlData);
            newPage.m_file = file;
            newPage.m_allowRelativeLinks = "true".equals(allowRelative);
            
        } else {
            // file is empty
            newPage = new CmsXmlPage();
            newPage.m_file = file;
            newPage.m_allowRelativeLinks = "true".equals(allowRelative);
        }
        
        return newPage;
    }    

    /**
     * Reads the xml contents from a string into the page.<p>
     * 
     * @param cms the current cms object
     * @param xmlData the xml data
     * @return the page initialized with the given xml data
     * @throws CmsPageException if something goes wrong
     */
    public static CmsXmlPage read(CmsObject cms, String xmlData) 
        throws CmsPageException {        
        try {
            SAXReader reader = new SAXReader();
            reader.setEntityResolver(new CmsEntityResolver(cms));

            // TODO: check why this does not work ...
            // try {
            //    reader.setFeature("http://xml.org/sax/features/resolve-dtd-uris", false);
            //} catch (SAXException exc) {
            //    if (OpenCms.getLog(this).isDebugEnabled()) {
            //        OpenCms.getLog(this).debug("Cannot disable sax feature resolve-dtd-uris", exc);
            //    }
            //}
            
            Document document = reader.read(new StringReader(xmlData));            
            // Document document = DocumentHelper.parseText(xmlData);
            return new CmsXmlPage(document);
        } catch (DocumentException e) {
            throw new CmsPageException("Reading xml page from a String failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Removes a bookmark with a given key.<p>
     * 
     * @param name the name of the element
     * @param language the language of the element
     * @return the element removed from the bookmarks or null
     */
    protected Element removeBookmark(String name, String language) {
        return (Element)m_bookmarks.remove(language + "_" + name);
    }
    
    /**
     * Removes an existing element with the given name and language.<p>
     * 
     * @param name name of the element
     * @param language language of the element
     */
    public void removeElement(String name, String language) {

        
        Element elements = m_document.getRootElement().element("elements");
        
        Element element = removeBookmark(name, language);
        elements.remove(element);
    }

    /**
     * Adds a bookmark for the given element.<p>
     * 
     * @param name the name of the element
     * @param language the language of the element
     * @param element the element to bookmark
     */
    protected void setBookmark (String name, String language, Element element) {
        
        if (language != null) {
            m_bookmarks.put(language + "_" + name, element);
        } else {
            m_bookmarks.put(name, element);
        }
    }
            
    
    /**
     * Sets the data of an already existing element.<p>
     * The data will be enclosed as CDATA within the xml page structure.
     * When setting the element data, the content of this element will be
     * processed automatically.
     * 
     * @param cms the cms object
     * @param name name of the element
     * @param language language of the element
     * @param content character data (CDATA) of the element
     * 
     * @throws CmsPageException if something goes wrong
     */
    public void setContent(CmsObject cms, String name, String language, String content) 
        throws CmsPageException {
        
        Element element = getBookmark(name, language);
        Element data = element.element("content");
        Element links = element.element("links");
        CmsLinkTable linkTable = new CmsLinkTable();
        
        try {

            CmsLinkProcessor linkReplacer = new CmsLinkProcessor(linkTable);
        
            data.setContent(null);
            if (!m_allowRelativeLinks && m_file != null) {
                String relativeRoot = CmsResource.getParentFolder(cms.readAbsolutePath(m_file));
                data.addCDATA(linkReplacer.replaceLinks(cms, content, relativeRoot));
            } else {
                data.addCDATA(linkReplacer.replaceLinks(cms, content, null));
            }
                        
        } catch (Exception exc) {
            throw new CmsPageException ("HTML data processing failed", exc);
        }
        
        links.setContent(null);
        for (Iterator i = linkTable.iterator(); i.hasNext();) {
            CmsLink link = linkTable.getLink((String)i.next());
            
            Element linkElement = links.addElement("link")
                .addAttribute("name", link.getName())
                .addAttribute("type", link.getType())
                .addAttribute("internal", Boolean.toString(link.isInternal()));
                
            linkElement.addElement("target")
                .addCDATA(link.getTarget());
            
            if (link.getAnchor() != null) {
                linkElement.addElement("anchor")
                    .addCDATA(link.getAnchor());
            }
            
            if (link.getQuery() != null) {
                linkElement.addElement("query")
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
     * @param language language of the element
     * @param isEnabled enabled flag for the element
     */
    public void setEnabled(String name, String language, boolean isEnabled) {

        Element element = getBookmark(name, language);
        Attribute enabled = element.attribute("enabled");
        
        if (enabled == null) {
            element.addAttribute("enabled", Boolean.toString(isEnabled));
        } else if (isEnabled) {
            element.remove(enabled);
        } else {
            enabled.setValue(Boolean.toString(isEnabled));
        }
    }
    
    /**
     * Writes the xml contents into the assigned CmsFile,
     * using the opencms default encoding.<p>
     * 
     * @return the assigned file with the xml content
     * @throws CmsPageException if something goes wrong
     */
    public CmsFile write() 
        throws CmsPageException {
        
        return write(m_file, OpenCms.getDefaultEncoding());
    }
    
    /**
     * Writes the xml contents into the CmsFile,
     * using the opencms default encoding.<p>
     * 
     * @param file the file to write the xml
     * @return the file with the xml content
     * @throws CmsPageException if something goes wrong
     */
    public CmsFile write(CmsFile file) 
        throws CmsPageException {
        
        return write(file, OpenCms.getDefaultEncoding());
    }
    
    /**
     * Writes the xml contents in the assigned CmsFile using the given encoding.<p>
     * 
     * @param encoding the encoding to use
     * @return the assigned file with the xml content
     * @throws CmsPageException if something goes wrong
     */
    public CmsFile write(String encoding) 
        throws CmsPageException {
        
        return write(m_file, encoding);
    }
    
    /**
     * Writes the xml contents into the CmsFile.<p>
     * 
     * @param file the file to write the xml
     * @param encoding the encoding to use
     * @return the file with the xml content
     * @throws CmsPageException if something goes wrong
     */
    public CmsFile write(CmsFile file, String encoding) 
        throws CmsPageException {
        
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
     * @throws CmsPageException if something goes wrong
     */

    public OutputStream write(OutputStream out, String encoding)
        throws CmsPageException {
        
        try {
            
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding(encoding);
            
            XMLWriter writer = new XMLWriter(out, format);
            writer.setEscapeText(false);
            writer.write(m_document);
            writer.close();
            
        } catch (Exception exc) {
            throw new CmsPageException("Writing xml page failed", exc);
        }
        
        return out;
    }
}
