/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/A_CmsXmlDocument.java,v $
 * Date   : $Date: 2004/10/15 12:22:00 $
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

package org.opencms.xml;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * @version $Revision: 1.5 $
 * @since 5.3.5
 */
public abstract class A_CmsXmlDocument implements I_CmsXmlDocument {

    /** The document object of the document. */
    protected Document m_document;

    /** Maps element names to available locales. */
    protected Map m_elementLocales;

    /** Maps locales to avaliable element names. */
    protected Map m_elementNames;

    /** The encoding to use for this xml document. */
    protected String m_encoding;

    /** The content conversion to use for this xml document. */
    protected String m_conversion;    
    
    /** The file that contains the document data (note: is not set when creating an empty or document based document). */
    protected CmsFile m_file;

    /** Set of locales contained in this document. */
    protected Set m_locales;

    /** Reference for named elements in the document. */
    private Map m_bookmarks;

    /**
     * Default constructor for a XML document
     * that initializes some internal values.<p> 
     */
    protected A_CmsXmlDocument() {

        m_bookmarks = new HashMap();
        m_locales = new HashSet();
    }

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
     * Returns the content converison used for the page content.<p>
     * 
     * @return the content converison used for the page content
     */
    public String getConversion() {

        return m_conversion;
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
     * @see org.opencms.xml.I_CmsXmlDocument#getIndexCount(java.lang.String, java.util.Locale)
     */
    public int getIndexCount(String name, Locale locale) {

        List elements = getBookmark(name, locale);
        if (elements == null) {
            return -1;
        } else {
            return elements.size();
        }
    }

    /**
     * Returns a link processor for the values of this XML element.<p>
     * 
     * @param cms an initialized CmsObject that provides the context for the link processor
     * @param linkTable the link table to use
     * 
     * @return a link processor for the values of this XML element
     */
    public abstract CmsLinkProcessor getLinkProcessor(CmsObject cms, CmsLinkTable linkTable);

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
     * @see org.opencms.xml.I_CmsXmlDocument#getStringValue(org.opencms.file.CmsObject, java.lang.String, java.util.Locale)
     */
    public String getStringValue(CmsObject cms, String name, Locale locale) throws CmsXmlException {

        return getStringValue(cms, name, locale, 0);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getStringValue(CmsObject, java.lang.String, Locale, int)
     */
    public String getStringValue(CmsObject cms, String name, Locale locale, int index) throws CmsXmlException {

        List values = getBookmark(name, locale);
        if (values != null) {
            Object value = values.get(index);
            if (value != null) {
                return ((I_CmsXmlContentValue)value).getStringValue(cms, this);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValue(java.lang.String, java.util.Locale)
     */
    public I_CmsXmlContentValue getValue(String name, Locale locale) {

        return getValue(name, locale, 0);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValue(java.lang.String, java.util.Locale, int)
     */
    public I_CmsXmlContentValue getValue(String name, Locale locale, int index) {

        List values = getBookmark(name, locale);
        if (values != null) {
            Object value = values.get(index);
            if (value != null) {
                return (I_CmsXmlContentValue)value;
            }
        }

        return null;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValues(java.lang.String, java.util.Locale)
     */
    public List getValues(String name, Locale locale) {

        return getBookmark(name, locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#hasValue(java.lang.String, java.util.Locale)
     */
    public boolean hasValue(String name, Locale locale) {

        return hasValue(name, locale, 0);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#hasValue(java.lang.String, java.util.Locale, int)
     */
    public boolean hasValue(String name, Locale locale, int index) {

        List elements = getBookmark(name, locale);
        return ((elements != null) && (elements.size() > index));
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
     * Sets the content conversion mode for this document.<p>
     * 
     * @param conversion the conversion mode to set for this document
     */
    public void setConversion(String conversion) {
        m_conversion = conversion;
    }
    
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        try {
            return CmsXmlUtils.marshal(m_document, m_encoding);
        } catch (CmsXmlException e) {
            throw new RuntimeException("Writing XML document to a String failed", e);
        }
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

        if (m_file != null) {
            byte[] xmlData = null;
            // file is set, use bytes from file directly
            xmlData = m_file.getContents();
            CmsXmlUtils.validateXmlStructure(xmlData, m_encoding, resolver);
        } else {
            CmsXmlUtils.validateXmlStructure(m_document, m_encoding, resolver);
        }
    }
    
    /**
     * Corrects the structure of this XML content.<p>
     * 
     * @param cms the current cms object
     * @return the file that contains the corrected XML structure
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public CmsFile correctXmlStructure(CmsObject cms) throws CmsXmlException {

        // iterate over all locales
        Iterator i = m_locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            List names = getNames(locale);

            // iterate over all nodes per language
            Iterator j = names.iterator();
            while (j.hasNext()) {
                String name = (String)j.next();
                List elements = getBookmark(name, locale);
                
                // iterate over all elements of this name
                for (int pos=0; pos<elements.size(); pos++) {
                 
                    I_CmsXmlContentValue value = getValue(name, locale, pos);
                    String content = value.getStringValue(cms, this);
                    value.setStringValue(cms, this, content);
                }
            }
        }
        
        // write the modifed xml back to the VFS file 
        m_file.setContents(marshal());
        return m_file;
    }    

    /**
     * Adds a bookmark for the given value.<p>
     * 
     * @param name the name of the value
     * @param locale the locale of the value
     * @param enabled if true, the value is enabled, if false it is disabled
     * @param value the value to bookmark
     */
    protected void addBookmark(String name, Locale locale, boolean enabled, Object value) {

        m_locales.add(locale);
        
        List list;
        Object o;
        
        // all bookmarks are stored in lists
        o = getBookmark(name, locale);
        if (o == null) {
            // no bookmark defined yet, initialize new List
            list = new ArrayList(1);
        } else {
            // expand existing list
            list = (List)o;
        }
        list.add(value);                
        m_bookmarks.put(getBookmarkName(name, locale), list);
        
        // update mapping of element name to locale
        if (enabled) {
            // only include enabled elements
            o = m_elementLocales.get(name);
            if (o != null) {
                ((Set)o).add(locale);
            } else {
                Set set = new HashSet();
                set.add(locale);
                m_elementLocales.put(name, set);
            }
        }
        // update mapping of locales to element names
        o = m_elementNames.get(locale);
        if (o != null) {
            ((Set)o).add(name);
        } else {
            Set set = new HashSet();
            set.add(name);
            m_elementNames.put(locale, set);
        }
    }

    /**
     * Returns the bookmarked lists of values for the given name.<p>
     * 
     * @param name the name to get the bookmark for
     * @param locale the locale to get the bookmark for
     * @return the bookmarked list of values
     */
    protected List getBookmark(String name, Locale locale) {

        return (List)m_bookmarks.get(getBookmarkName(name, locale));
    }

    /**
     * Returns the names of all bookmarked elements.<p>
     * 
     * @return the names of all bookmarked elements
     */
    protected Set getBookmarks() {

        return m_bookmarks.keySet();
    }

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
    protected List removeBookmark(String name, Locale locale) {

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
        return (List)m_bookmarks.remove(getBookmarkName(name, locale));
    }  
}