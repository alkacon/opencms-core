/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/page/Attic/CmsXmlPage.java,v $
 * Date   : $Date: 2003/12/12 08:43:19 $
 * Version: $Revision: 1.9 $
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
import org.opencms.util.CmsLinkProcessor;
import org.opencms.util.CmsLinkTable;
import org.opencms.util.CmsStringSubstitution;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.workplace.I_CmsWpConstants;

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
 * @version $Revision: 1.9 $
 */
public class CmsXmlPage {
    
    /** Name of the root document node */
    private static final String C_DOCUMENT_NODE = "page";
    
    /** Link to the external document type of this xml page */
    private static final String C_DOCUMENT_TYPE = "/system/dtds/page.dtd";
    
    /** Reference for named elements */
    private Map m_bookmarks = null;

    /** The document object of the page */
    private Document m_document = null;
    
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
        element.addElement("editdata");
        element.addElement("displaydata");

        setBookmark (language+"_"+name, element);
    }
    
    /**
     * Returns the bookmarked element for the given key.<p>
     * 
     * @param key key of the element
     * @return the bookemarked element
     */
    protected Element getBookmark (String key) {        
        return (Element) m_bookmarks.get(key);
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
     * @return the display content
     * 
     * @throws CmsPageException if something goes wrong
     */
    public String getContent(CmsObject cms, String name, String language) 
        throws CmsPageException {

        Element element = getBookmark(language+"_"+name);
        Element displaydata = element.element("displaydata");
        String content = displaydata.getText();
        
        CmsLinkTable linkTable = getLinkTable(name, language);
        if (!linkTable.isEmpty()) {
            
            CmsLinkProcessor macroReplacer = new CmsLinkProcessor(linkTable);
        
            try {
            
                content = macroReplacer.processLinks(cms, content);
            } catch (Exception exc) {
                throw new CmsPageException ("HTML data processing failed", exc);
            }
        } 
            
        return content;
    }    
    
    /**
     * Returns the data of an element.<p>
     * 
     * @param name name of the element
     * @param language language of the element
     * @return the character data of the element
     */
    public String getElementData(String name, String language) {

        Element element = getBookmark(language+"_"+name);
        
        if (element != null) {
            
            Element editdata = element.element("editdata");
            
            // set the context & servlet path in editor content
            String content = CmsStringSubstitution.substitute(editdata.getText(), I_CmsWpConstants.C_MACRO_OPENCMS_CONTEXT + "/", OpenCms.getOpenCmsContext() + "/");
 
            return content;
        } else {
            return null;
        }
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
            String language = name.substring(0, name.indexOf("_"));
            languages.add(language);
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

        Element element = getBookmark(language+"_"+name);
        Element links = element.element("links");
        
        CmsLinkTable linkTable = new CmsLinkTable();
        
        for (Iterator i = links.elementIterator("link"); i.hasNext();) {
                    
            Element lelem = (Element)i.next();
            linkTable.addLink(lelem.attribute("name").getValue(), 
                    lelem.attribute("type").getValue(), 
                    lelem.attribute("target").getValue(), 
                    Boolean.valueOf(lelem.attribute("internal").getValue()).booleanValue());
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
    
        return getBookmark(language+"_"+name) != null;
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
    
            setBookmark(elementLang+"_"+elementName, elem);              
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
    public CmsXmlPage read(CmsObject cms, CmsFile file) 
        throws CmsPageException {

        byte[] content = file.getContents();
        
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
            return read(cms, xmlData);            
        } else {
            // file is empty
            return new CmsXmlPage();
        }
    }    

    /**
     * Reads the xml contents from a string into the page.<p>
     * 
     * @param cms the current cms object
     * @param xmlData the xml data
     * @return the page initialized with the given xml data
     * @throws CmsPageException if something goes wrong
     */
    public CmsXmlPage read(CmsObject cms, String xmlData) 
        throws CmsPageException {        
        try {
            SAXReader reader = new SAXReader();
            reader.setEntityResolver(new CmsEntityResolver(cms));
            Document document = reader.read(new StringReader(xmlData));            
            // Document document = DocumentHelper.parseText(xmlData);
            return new CmsXmlPage(document);
        } catch (DocumentException e) {
            throw new CmsPageException("Reading xml page from a String failed", e);
        }
    }
    
    /**
     * Removes a bookmark with a given key.<p>
     * 
     * @param key the key for the bookmark
     * @return the element removed from the bookmarks or null
     */
    protected Element removeBookmark(String key) {
        return (Element)m_bookmarks.remove(key);
    }
    
    /**
     * Removes an existing element with the given name and language.<p>
     * 
     * @param name name of the element
     * @param language language of the element
     */
    public void removeElement(String name, String language) {

        
        Element elements = m_document.getRootElement().element("elements");
        
        Element element = removeBookmark(language+"_"+name);
        elements.remove(element);
    }

    /**
     * Adds a bookmark for the given element.<p>
     * 
     * @param key the key of the bookmark
     * @param element the element to bookmark
     */
    protected void setBookmark (String key, Element element) {
        
        m_bookmarks.put(key, element);
    }
    
    /**
     * Sets the data of an already existing element.<p>
     * The data will be enclosed as CDATA within the xml page structure.
     * When setting the element data, the content of this element will be
     * processed automatically.
     * 
     * @param name name of the element
     * @param language language of the element
     * @param content character data (CDATA) of the element
     * 
     * @throws CmsPageException if something goes wrong
     */
    public void setElementData(String name, String language, String content) 
        throws CmsPageException {
        
        Element element = getBookmark(language+"_"+name);
        Element editdata = element.element("editdata");
        Element displaydata = element.element("displaydata");
        Element links = element.element("links");
        
        String cdata = CmsStringSubstitution.substituteContextPath(content, OpenCms.getOpenCmsContext()  + "/");
        editdata.setContent(null);
        editdata.addCDATA(cdata);

        // TODO: convert editdata to displaydata
        CmsLinkTable linkTable = new CmsLinkTable();
        try {

            CmsLinkProcessor linkReplacer = new CmsLinkProcessor(linkTable);
        
            displaydata.setContent(null);
            displaydata.addCDATA(linkReplacer.replaceLinks(content));
            
        } catch (Exception exc) {
            throw new CmsPageException ("HTML data processing failed", exc);
        }
        
        links.setContent(null);
        for (Iterator i = linkTable.iterator(); i.hasNext();) {
            CmsLinkTable.CmsLink link = linkTable.getLink((String)i.next());
            links.addElement("link")
                .addAttribute("name", link.getName())
                .addAttribute("type", link.getType())
                .addAttribute("target", link.getTarget())
                .addAttribute("internal", Boolean.toString(link.isInternal()));
        }
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
            writer.write(m_document);
            writer.close();
            
        } catch (Exception exc) {
            throw new CmsPageException("Writing xml page failed", exc);
        }
        
        return out;
    }
}
