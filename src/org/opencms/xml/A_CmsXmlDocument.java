/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/A_CmsXmlDocument.java,v $
 * Date   : $Date: 2004/06/13 23:43:31 $
 * Version: $Revision: 1.2 $
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
package org.opencms.xml;

import org.opencms.file.CmsFile;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.xml.sax.EntityResolver;

/**
 * Provides basic XML document handling functions useful when dealing
 * with XML documents that are stored in the OpenCms VFS.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.3.5
 */
public abstract class A_CmsXmlDocument {

    /** Reference for named elements in the document. */
    protected Map m_bookmarks;
    
    /** The document object of the document. */
    protected Document m_document;
    
    /** Maps element names to available locales. */
    protected Map m_elementLocales;
    
    /** Maps locales to avaliable element names. */
    protected Map m_elementNames;
    
    /** The encoding to use for this xml document. */
    protected String m_encoding;
    
    /** The file that contains the document data (note: is not set when creating an empty or document based document). */
    protected CmsFile m_file;
    
    /** Set of locales contained in this document. */
    protected Set m_locales;

    /**
     * Creates the bookmark name for a localized element to be used in the bookmark lookup table.<p>
     * 
     * @param name the element name
     * @param locale the element locale 
     * @return the bookmark name for a localized element
     */
    protected static final String getBookmarkName(String name, Locale locale) {
        
        StringBuffer result = new StringBuffer(64);
        result.append(locale.toString());
        result.append('|');
        result.append(name);
        return result.toString();
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
     * Returns a List of all locales that have at last one element in this page.<p>
     * 
     * @return a List of all locales that have at last one element in this page
     */
    public List getLocales() { 
        
        return new ArrayList(m_locales);
    }

    /**
     * Returns a List of all locales that have the named element set in this document.<p>
     * 
     * If no locale for the given element name is available, an empty list is returned.<p>
     * 
     * @param element the element to look up the locale List for
     * @return a List of all Locales that have the named element set in this document
     */
    public List getLocales(String element) {
        
        Object result = m_elementLocales.get(element);
        if (result == null) {
            return Collections.EMPTY_LIST;
        }        
        return new ArrayList((Set)result);
    }

    /**
     * Returns all available elements names for a given locale used in this document.<p>
     * 
     * If no element for the given locale is available, an empty list is returned.<p>
     * 
     * @param locale the locale
     * @return list of available element names (Strings)
     */
    public List getNames(Locale locale) {
        
        Object o = m_elementNames.get(locale);
        if (o != null) {
            return new ArrayList((Set)o);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Checks if the document contains a name specified by name and language.<p>
     * 
     * @param name the name of the element the check
     * @param locale the locale of the element the check
     * @return true if this element exists
     */
    public boolean hasElement(String name, Locale locale) {  
        
        return getBookmark(name, locale) != null;
    }

    /**
     * Marshals (writes) the content of the current XML document 
     * into a byte array using the selected encoding.<p>
     * 
     * @return the content of the current XML document written into a byte array
     * @throws CmsXmlException if something goes wrong
     */
    public byte[] marshal() throws CmsXmlException {
              
        return ((ByteArrayOutputStream)marshal(new ByteArrayOutputStream(), m_encoding)).toByteArray(); 
    }

    /**
     * Validates the xml structure of the document with the DTD ot schema used by the document.<p>
     * 
     * This is required in case someone modifies the xml structure of a  
     * document using the "edit control code" option.<p>
     * 
     * @param resolver the XML entitiy resolver to use
     * @throws CmsXmlException if the validation fails
     */    
    public void validateXmlStructure(EntityResolver resolver) throws CmsXmlException {

        byte[] xmlData = null;
        if (m_file != null) {
            // file is set, use bytes from file directly
            xmlData = m_file.getContents();
        }
        CmsXmlUtils.validateXmlStructure(xmlData, m_document, getEncoding(), resolver);
    }
    
    
    /**
     * Adds a bookmark for the given element.<p>
     * 
     * @param name the name of the element
     * @param locale the locale of the element
     * @param enabled if true, the element is enabled, if false it is disabled
     * @param element the element to bookmark
     */
    protected void addBookmark(String name, Locale locale, boolean enabled, Object element) {
        
        m_locales.add(locale);
        m_bookmarks.put(getBookmarkName(name, locale), element);
        // update mapping of element name to locale
        if (enabled) {
            // only include enabled elements
            Object o = m_elementLocales.get(name);
            if (o != null) {
                ((Set)o).add(locale);
            } else {
                Set set = new HashSet();
                set.add(locale);
                m_elementLocales.put(name, set);
            }
        }
        // update mapping of locales to element names
        Object o = m_elementNames.get(locale);
        if (o != null) {
            ((Set)o).add(name);
        } else {
            Set set = new HashSet();
            set.add(name);
            m_elementNames.put(locale, set);
        }        
    }

    /**
     * Returns the bookmarked element for the given element name.<p>
     * 
     * @param name the name of the element
     * @param locale the locale of the element
     * @return the bookmarked element
     */
    protected Object getBookmark(String name, Locale locale) {    
        
        return m_bookmarks.get(getBookmarkName(name, locale));
    }

    /**
     * Returns the names of all bookmarked elements.<p>
     * 
     * @return the names of all bookmarked elements
     */
    protected Set getBookmarks() {
        
        return (m_bookmarks != null)? m_bookmarks.keySet() : new HashSet(); 
    }

    /**
     * Initializes the bookmarks according to the named elements in the document.<p>
     */    
    protected abstract void initBookmarks();

    /**
     * Initializes an A_CmsXmlDocument based on the provided document and encoding.<p>
     * 
     * @param document the base XML document to use for initializing
     * @param encoding the encoding to use when marshalling the document later
     * @param resolver the XML entitiy resolver to use
     */    
    protected abstract void initDocument(Document document, String encoding, EntityResolver resolver);

    /**
     * Marshals (writes) the content of the current XML document 
     * into an output stream.<p>
     * 
     * @param out the output stream to write to
     * @param encoding the encoding to use
     * @return the output stream with the xml content
     * @throws CmsXmlException if something goes wrong
     */
    protected OutputStream marshal(OutputStream out, String encoding) throws CmsXmlException {

        return CmsXmlUtils.marshal(m_document, out, encoding);        
    }

    /**
     * Removes the bookmark for an element with the given name and locale.<p>
     * 
     * @param name the name of the element
     * @param locale the locale of the element
     * @return the element removed from the bookmarks or null
     */
    protected Object removeBookmark(String name, Locale locale) {
        
        // remove mapping of element name to locale
        Object o;
        o = m_elementLocales.get(name);
        if (o != null) {
            ((Set)o).remove(locale);
        }
        // remove mapping of locale to element name
        o = m_elementNames.get(locale);
        if (o != null) {
            ((Set)o).remove(name);
        }        
        // remove the bookmark and return the removed element
        return m_bookmarks.remove(getBookmarkName(name, locale));
    }
}
