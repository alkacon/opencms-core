/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/page/CmsXmlPage.java,v $
 * Date   : $Date: 2004/06/13 23:43:43 $
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
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.xml.sax.EntityResolver;

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
public class CmsXmlPage extends A_CmsXmlDocument {
    
    /** Name of the name attribute of the elements node. */
    private static final String C_ATTRIBUTE_ENABLED = "enabled";    

    /** Name of the internal attribute of the link node. */
    private static final String C_ATTRIBUTE_INTERNAL = "internal";

    /** Name of the language attribute of the elements node. */
    private static final String C_ATTRIBUTE_LANGUAGE = "language";

    /** Name of the name attribute of the elements node. */
    private static final String C_ATTRIBUTE_NAME = "name";

    /** Name of the type attribute of the elements node. */
    private static final String C_ATTRIBUTE_TYPE = "type";
    
    /** Name of the root document node. */
    private static final String C_DOCUMENT_NODE = "page";

    /** Name of the anchor node. */
    private static final String C_NODE_ANCHOR = "anchor";
    
    /** Name of the element node. */
    private static final String C_NODE_CONTENT = "content";

    /** Name of the element node. */
    private static final String C_NODE_ELEMENT = "element";
    
    /** Name of the elements node. */
    public static final String C_NODE_ELEMENTS = "elements";

    /** Name of the link node. */
    public static final String C_NODE_LINK = "link";
    
    /** Name of the links node. */
    public static final String C_NODE_LINKS = "links";
    
    /** Name of the query node. */
    private static final String C_NODE_QUERY = "query";
    
    /** Name of the target node. */
    private static final String C_NODE_TARGET = "target";
        
    /** Property to check if relative links are allowed. */
    private static final String C_PROPERTY_ALLOW_RELATIVE = "allowRelativeLinks";

    /** The DTD address of the OpenCms xmlpage. */
    public static final String C_XMLPAGE_DTD_SYSTEM_ID = CmsConfigurationManager.C_DEFAULT_DTD_PREFIX + "xmlpage.dtd";    
    
    /** Indicates if relative Links are allowed. */
    private boolean m_allowRelativeLinks;
    
    /**
     * Creates a new CmsXmlPage based on the provided document and encoding.<p>
     * 
     * The encoding is used for marshalling the XML document later.<p>
     * 
     * @param document the document to create the CmsXmlPage from
     * @param encoding the encoding of the xml page
     */
    public CmsXmlPage(Document document, String encoding) {
        
        initDocument(document, encoding, null);
    }

    /**
     * Creates an empty CmsXmlPage with the provided encoding.<p>
     * 
     * The page is initialized according to the minimal neccessary xml structure.
     * The encoding is used for marshalling the XML document later.<p>
     * 
     * @param encoding the encoding of the xml page
     */
    public CmsXmlPage(String encoding) {
        
        initDocument(createValidDocument(), encoding, null);
    }
    
    /**
     * Creates a new instance for later initializing using unmarshal helpers.<p> 
     */
    private CmsXmlPage() {
        // noop
    }

    /**
     * Factory method to unmarshal (read) a XML page instance from a byte array
     * that contains XML data.<p>
     * 
     * When unmarshalling, the encoding is read directly from the XML header. 
     * The given encoding is used only when marshalling the XML again later.<p>
     * 
     * @param xmlData the XML data in a byte array
     * @param encoding the encoding to use when marshalling the XML page later
     * @param resolver the XML entitiy resolver to use
     * @return a XML page instance unmarshalled from the byte array
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage unmarshal(byte[] xmlData, String encoding, EntityResolver resolver) throws CmsXmlException {
        
        return new CmsXmlPage(CmsXmlUtils.unmarshalHelper(xmlData, resolver), encoding);
    }
    
    /**
     * Factory method to unmarshal (read) a XML page instance from a OpenCms VFS file
     * that contains XML data.<p>
     * 
     * @param cms the current cms object
     * @param file the file with the XML data to unmarshal
     * @return a XML page instance unmarshalled from the provided file
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage unmarshal(CmsObject cms, CmsFile file) throws CmsXmlException {
        
        return unmarshal(cms, file, true);
    }
        
    /**
     * Factory method to unmarshal (read) a XML page instance from a OpenCms VFS file
     * that contains XML data, using wither the encoding set
     * in the XML file header, or the encoding set in the VFS file property.<p>
     * 
     * If you are not sure about the implications of the encoding issues, 
     * use {@link #unmarshal(CmsObject, CmsFile) } instead.<p>
     * 
     * @param cms the current cms object
     * @param file the file with the XML data to unmarshal
     * @param keepEncoding if true, the encoding spefified in the XML header is used, 
     *    otherwise the encoding from the VFS file property is used
     * @return a XML page instance unmarshalled from the provided file
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage unmarshal(CmsObject cms, CmsFile file, boolean keepEncoding) throws CmsXmlException {
        
        byte[] content = file.getContents();

        String fileName = cms.readAbsolutePath(file);
        boolean allowRelative = false;
        try {
            allowRelative = Boolean.valueOf(cms.readPropertyObject(fileName, C_PROPERTY_ALLOW_RELATIVE, false).getValue()).booleanValue();
        } catch (CmsException e) {
            // allowRelative will be false
        }
        
        String encoding = null;
        try { 
            encoding = cms.readPropertyObject(fileName, I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true).getValue();
        } catch (CmsException e) {
            // encoding will be null 
        }        
        if (encoding == null) {
            encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        } else {
            encoding = CmsEncoder.lookupEncoding(encoding, null);
            if (encoding == null) {
                throw new CmsXmlException("Invalid content-encoding property set for xml page '" + fileName + "'");
            }
        }
                 
        CmsXmlPage newPage;        
        if (content.length > 0) {
            // content is initialized
            if (keepEncoding) {
                // use the encoding from the content
                newPage = unmarshal(content, encoding, new CmsXmlEntityResolver(cms));
            } else {
                // use the encoding from the file property
                // this usually only triggered by a save operation                
                try {
                    String contentStr = new String(content, encoding);
                    newPage = unmarshal(contentStr, encoding, new CmsXmlEntityResolver(cms)); 
                } catch (UnsupportedEncodingException e) {
                    // this will not happen since the encodig has already been validated
                    throw new CmsXmlException("Invalid content-encoding property set for xml page '" + fileName + "'", e);
                }                
            }
        } else {
            // content is empty
            newPage = new CmsXmlPage(encoding);
        }
        
        newPage.m_file = file;
        newPage.m_allowRelativeLinks = allowRelative;
        
        return newPage;
    }    

    /**
     * Factory method to unmarshal (read) a XML page instance from a String
     * that contains XML data.<p>
     * 
     * When unmarshalling, the encoding is read directly from the XML header. 
     * The given encoding is used only when marshalling the XML again later.<p>
     * 
     * @param xmlData the XML data in a String
     * @param encoding the encoding to use when marshalling the XML page later
     * @param resolver the XML entitiy resolver to use
     * @return a XML page instance unmarshalled from the String
     * @throws CmsXmlException if something goes wrong
     */
    public static CmsXmlPage unmarshal(String xmlData, String encoding, EntityResolver resolver) throws CmsXmlException {  
        
        return new CmsXmlPage(CmsXmlUtils.unmarshalHelper(xmlData, resolver), encoding);
    }
    
    /**
     * Returns an initialized document object.<p>
     * 
     * @return an initialized document object
     */
    protected static Document createValidDocument() {        
        
        Document doc = DocumentHelper.createDocument(DocumentHelper.createElement(C_DOCUMENT_NODE));
        doc.addDocType(C_DOCUMENT_NODE, "", C_XMLPAGE_DTD_SYSTEM_ID);
        doc.getRootElement().addElement(C_NODE_ELEMENTS);
        return doc;
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
        addBookmark(name, locale, true, element);
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
        // write the modifed xml back to the VFS file 
        m_file.setContents(marshal());
        return m_file;
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

        Element element = (Element)getBookmark(name, locale);        
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
     * Returns the link table of an element.<p>
     * 
     * @param name name of the element
     * @param locale locale of the element
     * @return the link table
     */
    public CmsLinkTable getLinkTable(String name, Locale locale) {

        Element element = (Element)getBookmark(name, locale);
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
     * Returns the raw (unprocessed) content of an element.<p>
     * 
     * @param name  name of the element
     * @param locale locale of the element
     * @return the raw (unprocessed) content
     */
    public String getRawContent(String name, Locale locale) {
        
        Element element = (Element)getBookmark(name, locale);        
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
     * Checks if the element of a page object is enabled.<p>
     * 
     * @param name the name of the element
     * @param locale the locale of the element
     * @return true if the element exists and is not disabled
     */
    public boolean isEnabled(String name, Locale locale) {

        Element element = (Element)getBookmark(name, locale);

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
        Element element = (Element)removeBookmark(name, locale);
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
        
        Element element = (Element)getBookmark(name, locale);
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

        Element element = (Element)getBookmark(name, locale);
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
     * Initializes the bookmarks according to the named elements in the document.<p>
     */
    protected void initBookmarks() {

        m_bookmarks = new HashMap();
        m_locales = new HashSet();
        m_elementLocales = new HashMap();
        m_elementNames = new HashMap();
        
        for (Iterator i = m_document.getRootElement().element(C_NODE_ELEMENTS).elementIterator(C_NODE_ELEMENT); i.hasNext();) {
   
            Element elem = (Element)i.next();
            try {
                String elementName = elem.attributeValue(C_ATTRIBUTE_NAME);
                String elementLang = elem.attributeValue(C_ATTRIBUTE_LANGUAGE);
                String elementEnabled = elem.attributeValue(C_ATTRIBUTE_ENABLED);
                boolean enabled = (elementEnabled==null)?true:Boolean.valueOf(elementEnabled).booleanValue();
                addBookmark(elementName, CmsLocaleManager.getLocale(elementLang), enabled, elem);       

            } catch (NullPointerException e) {
                OpenCms.getLog(this).error("Error while initalizing xmlPage bookmarks", e);                
            }    
        }
    }
    
    /**
     * @see org.opencms.xml.A_CmsXmlDocument#initDocument(org.dom4j.Document, java.lang.String, org.xml.sax.EntityResolver)
     */
    protected void initDocument(Document document, String encoding, EntityResolver resolver) {        
        
        m_encoding = CmsEncoder.lookupEncoding(encoding, encoding);
        m_document = document;     
        initBookmarks();
    }
        
    /**
     * Writes the xml contents into an output stream.<p>
     * 
     * @param out the output stream to write to
     * @param encoding the encoding to use
     * @return the output stream with the xml content
     * @throws CmsXmlException if something goes wrong
     */
    protected OutputStream marshal(OutputStream out, String encoding) throws CmsXmlException {        

        // ensure xml page has proper system doc type set
        DocumentType type = m_document.getDocType();
        if (type != null) {
            String systemId = type.getSystemID();
            if ((systemId != null) && systemId.endsWith(CmsXmlEntityResolver.C_XMLPAGE_DTD_OLD_SYSTEM_ID)) {
                m_document.addDocType(C_DOCUMENT_NODE, "", C_XMLPAGE_DTD_SYSTEM_ID);
            }
        }
        
        return CmsXmlUtils.marshal(m_document, out, encoding);        
    }
}