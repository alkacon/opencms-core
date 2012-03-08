/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

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

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.EntityResolver;

/**
 * Provides basic XML document handling functions useful when dealing
 * with XML documents that are stored in the OpenCms VFS.<p>
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsXmlDocument implements I_CmsXmlDocument {

    /** The content conversion to use for this XML document. */
    protected String m_conversion;

    /** The document object of the document. */
    protected Document m_document;

    /** Maps element names to available locales. */
    protected Map<String, Set<Locale>> m_elementLocales;

    /** Maps locales to available element names. */
    protected Map<Locale, Set<String>> m_elementNames;

    /** The encoding to use for this XML document. */
    protected String m_encoding;

    /** The file that contains the document data (note: is not set when creating an empty or document based document). */
    protected CmsFile m_file;

    /** Set of locales contained in this document. */
    protected Set<Locale> m_locales;

    /** Reference for named elements in the document. */
    private Map<String, I_CmsXmlContentValue> m_bookmarks;

    /**
     * Default constructor for a XML document
     * that initializes some internal values.<p> 
     */
    protected A_CmsXmlDocument() {

        m_bookmarks = new HashMap<String, I_CmsXmlContentValue>();
        m_locales = new HashSet<Locale>();
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
        result.append('/');
        result.append(locale.toString());
        result.append('/');
        result.append(name);
        return result.toString();
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#copyLocale(java.util.List, java.util.Locale)
     */
    public void copyLocale(List<Locale> possibleSources, Locale destination) throws CmsXmlException {

        if (hasLocale(destination)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_LOCALE_ALREADY_EXISTS_1, destination));
        }
        Iterator<Locale> i = possibleSources.iterator();
        Locale source = null;
        while (i.hasNext() && (source == null)) {
            // check all locales and try to find the first match
            Locale candidate = i.next();
            if (hasLocale(candidate)) {
                // locale has been found
                source = candidate;
            }
        }
        if (source != null) {
            // found a locale, copy this to the destination
            copyLocale(source, destination);
        } else {
            // no matching locale has been found
            throw new CmsXmlException(Messages.get().container(
                Messages.ERR_LOCALE_NOT_AVAILABLE_1,
                CmsLocaleManager.getLocaleNames(possibleSources)));
        }
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#copyLocale(java.util.Locale, java.util.Locale)
     */
    public void copyLocale(Locale source, Locale destination) throws CmsXmlException {

        if (!hasLocale(source)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_LOCALE_NOT_AVAILABLE_1, source));
        }
        if (hasLocale(destination)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_LOCALE_ALREADY_EXISTS_1, destination));
        }

        Element sourceElement = null;
        Element rootNode = m_document.getRootElement();
        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(rootNode);
        String localeStr = source.toString();
        while (i.hasNext()) {
            Element element = i.next();
            String language = element.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE, null);
            if ((language != null) && (localeStr.equals(language))) {
                // detach node with the locale
                sourceElement = element.createCopy();
                // there can be only one node for the locale
                break;
            }
        }

        if (sourceElement == null) {
            // should not happen since this was checked already, just to make sure...
            throw new CmsXmlException(Messages.get().container(Messages.ERR_LOCALE_NOT_AVAILABLE_1, source));
        }

        // switch locale value in attribute of copied node
        sourceElement.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE, destination.toString());
        // attach the copied node to the root node
        rootNode.add(sourceElement);

        // re-initialize the document bookmarks
        initDocument(m_document, m_encoding, getContentDefinition());
    }

    /**
     * Corrects the structure of this XML document.<p>
     * 
     * @param cms the current OpenCms user context
     * 
     * @return the file that contains the corrected XML structure
     * 
     * @throws CmsXmlException if something goes wrong
     */
    public CmsFile correctXmlStructure(CmsObject cms) throws CmsXmlException {

        // apply XSD schema translation
        Attribute schema = m_document.getRootElement().attribute(
            I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION);
        if (schema != null) {
            String schemaLocation = schema.getValue();
            String translatedSchema = OpenCms.getResourceManager().getXsdTranslator().translateResource(schemaLocation);
            if (!schemaLocation.equals(translatedSchema)) {
                schema.setValue(translatedSchema);
            }
        }

        // iterate over all locales
        Iterator<Locale> i = m_locales.iterator();
        while (i.hasNext()) {
            Locale locale = i.next();
            List<String> names = getNames(locale);
            List<I_CmsXmlContentValue> validValues = new ArrayList<I_CmsXmlContentValue>();

            // iterate over all nodes per language
            Iterator<String> j = names.iterator();
            while (j.hasNext()) {

                // this step is required for values that need a processing of their content
                // an example for this is the HTML value that does link replacement                
                String name = j.next();
                I_CmsXmlContentValue value = getValue(name, locale);
                if (value.isSimpleType()) {
                    String content = value.getStringValue(cms);
                    value.setStringValue(cms, content);
                }

                // save valid elements for later check
                validValues.add(value);
            }

            if (isAutoCorrectionEnabled()) {
                // full correction of XML

                List<Element> roots = new ArrayList<Element>();
                List<CmsXmlContentDefinition> rootCds = new ArrayList<CmsXmlContentDefinition>();
                List<Element> validElements = new ArrayList<Element>();

                // gather all XML content definitions and their parent nodes                                
                Iterator<I_CmsXmlContentValue> it = validValues.iterator();
                while (it.hasNext()) {
                    // collect all root elements, also for the nested content definitions
                    I_CmsXmlContentValue value = it.next();
                    Element element = value.getElement();
                    validElements.add(element);
                    if (element.supportsParent()) {
                        // get the parent XML node
                        Element root = element.getParent();
                        if ((root != null) && !roots.contains(root)) {
                            // this is a parent node we do not have already in our storage
                            CmsXmlContentDefinition rcd = value.getContentDefinition();
                            if (rcd != null) {
                                // this value has a valid XML content definition
                                roots.add(root);
                                rootCds.add(rcd);
                            } else {
                                // no valid content definition for the XML value
                                throw new CmsXmlException(Messages.get().container(
                                    Messages.ERR_CORRECT_NO_CONTENT_DEF_3,
                                    value.getName(),
                                    value.getTypeName(),
                                    value.getPath()));
                            }
                        }
                    }
                }

                for (int le = 0; le < roots.size(); le++) {
                    // iterate all XML content root nodes and correct each XML subtree

                    Element root = roots.get(le);
                    CmsXmlContentDefinition cd = rootCds.get(le);

                    // step 1: first sort the nodes according to the schema, this takes care of re-ordered elements
                    List<List<Element>> nodeLists = new ArrayList<List<Element>>();
                    for (I_CmsXmlSchemaType type : cd.getTypeSequence()) {
                        List<Element> elements = CmsXmlGenericWrapper.elements(root, type.getName());
                        int maxOccures = cd.getChoiceMaxOccurs() > 0 ? cd.getChoiceMaxOccurs() : type.getMaxOccurs();
                        if (elements.size() > maxOccures) {
                            // to many nodes of this type appear according to the current schema definition
                            for (int lo = (elements.size() - 1); lo >= type.getMaxOccurs(); lo--) {
                                elements.remove(lo);
                            }
                        }
                        nodeLists.add(elements);
                    }

                    // step 2: clear the list of nodes (this will remove all invalid nodes)
                    List<Element> nodeList = CmsXmlGenericWrapper.elements(root);
                    nodeList.clear();
                    Iterator<List<Element>> in = nodeLists.iterator();
                    while (in.hasNext()) {
                        // now add all valid nodes in the right order
                        List<Element> elements = in.next();
                        nodeList.addAll(elements);
                    }

                    // step 3: now append the missing elements according to the XML content definition
                    cd.addDefaultXml(cms, this, root, locale);
                }
            }
        }

        // write the modified XML back to the VFS file 
        if (m_file != null) {
            // make sure the file object is available
            m_file.setContents(marshal());
        }
        return m_file;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getBestMatchingLocale(java.util.Locale)
     */
    public Locale getBestMatchingLocale(Locale locale) {

        // the requested locale is the match we want to find most
        if (hasLocale(locale)) {
            // check if the requested locale is directly available
            return locale;
        }
        if (locale.getVariant().length() > 0) {
            // locale has a variant like "en_EN_whatever", try only with language and country 
            Locale check = new Locale(locale.getLanguage(), locale.getCountry(), "");
            if (hasLocale(check)) {
                return check;
            }
        }
        if (locale.getCountry().length() > 0) {
            // locale has a country like "en_EN", try only with language
            Locale check = new Locale(locale.getLanguage(), "", "");
            if (hasLocale(check)) {
                return check;
            }
        }
        return null;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getConversion()
     */
    public String getConversion() {

        return m_conversion;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getEncoding()
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getFile()
     */
    public CmsFile getFile() {

        return m_file;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getIndexCount(java.lang.String, java.util.Locale)
     */
    public int getIndexCount(String path, Locale locale) {

        List<I_CmsXmlContentValue> elements = getValues(path, locale);
        if (elements == null) {
            return 0;
        } else {
            return elements.size();
        }
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getLocales()
     */
    public List<Locale> getLocales() {

        return new ArrayList<Locale>(m_locales);
    }

    /**
     * Returns a List of all locales that have the named element set in this document.<p>
     * 
     * If no locale for the given element name is available, an empty list is returned.<p>
     * 
     * @param path the element to look up the locale List for
     * @return a List of all Locales that have the named element set in this document
     */
    public List<Locale> getLocales(String path) {

        Set<Locale> locales = m_elementLocales.get(CmsXmlUtils.createXpath(path, 1));
        if (locales != null) {
            return new ArrayList<Locale>(locales);
        }
        return Collections.emptyList();
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getNames(java.util.Locale)
     */
    public List<String> getNames(Locale locale) {

        Set<String> names = m_elementNames.get(locale);
        if (names != null) {
            return new ArrayList<String>(names);
        }
        return Collections.emptyList();
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getStringValue(org.opencms.file.CmsObject, java.lang.String, java.util.Locale)
     */
    public String getStringValue(CmsObject cms, String path, Locale locale) {

        I_CmsXmlContentValue value = getValueInternal(CmsXmlUtils.createXpath(path, 1), locale);
        if (value != null) {
            return value.getStringValue(cms);
        }
        return null;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getStringValue(CmsObject, java.lang.String, Locale, int)
     */
    public String getStringValue(CmsObject cms, String path, Locale locale, int index) {

        // directly calling getValueInternal() is more efficient then calling getStringValue(CmsObject, String, Locale)
        // since the most costs are generated in resolving the xpath name
        I_CmsXmlContentValue value = getValueInternal(CmsXmlUtils.createXpath(path, index + 1), locale);
        if (value != null) {
            return value.getStringValue(cms);
        }
        return null;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getSubValues(java.lang.String, java.util.Locale)
     */
    public List<I_CmsXmlContentValue> getSubValues(String path, Locale locale) {

        List<I_CmsXmlContentValue> result = new ArrayList<I_CmsXmlContentValue>();
        String bookmark = getBookmarkName(CmsXmlUtils.createXpath(path, 1), locale);
        I_CmsXmlContentValue value = getBookmark(bookmark);
        if ((value != null) && !value.isSimpleType()) {
            // calculate level of current bookmark
            int depth = CmsResource.getPathLevel(bookmark) + 1;
            Iterator<String> i = getBookmarks().iterator();
            while (i.hasNext()) {
                String bm = i.next();
                if (bm.startsWith(bookmark) && (CmsResource.getPathLevel(bm) == depth)) {
                    // add only values directly below the value
                    result.add(getBookmark(bm));
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValue(java.lang.String, java.util.Locale)
     */
    public I_CmsXmlContentValue getValue(String path, Locale locale) {

        return getValueInternal(CmsXmlUtils.createXpath(path, 1), locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValue(java.lang.String, java.util.Locale, int)
     */
    public I_CmsXmlContentValue getValue(String path, Locale locale, int index) {

        return getValueInternal(CmsXmlUtils.createXpath(path, index + 1), locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValues(java.util.Locale)
     */
    public List<I_CmsXmlContentValue> getValues(Locale locale) {

        List<I_CmsXmlContentValue> result = new ArrayList<I_CmsXmlContentValue>();

        // bookmarks are stored with the locale as first prefix
        String prefix = '/' + locale.toString() + '/';

        // it's better for performance to iterate through the list of bookmarks directly
        Iterator<Map.Entry<String, I_CmsXmlContentValue>> i = m_bookmarks.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, I_CmsXmlContentValue> entry = i.next();
            if (entry.getKey().startsWith(prefix)) {
                result.add(entry.getValue());
            }
        }

        // sort the result
        Collections.sort(result);

        return result;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getValues(java.lang.String, java.util.Locale)
     */
    public List<I_CmsXmlContentValue> getValues(String path, Locale locale) {

        List<I_CmsXmlContentValue> result = new ArrayList<I_CmsXmlContentValue>();
        String bookmark = getBookmarkName(CmsXmlUtils.createXpath(CmsXmlUtils.removeXpathIndex(path), 1), locale);
        I_CmsXmlContentValue value = getBookmark(bookmark);
        if (value != null) {
            if (value.getContentDefinition().getChoiceMaxOccurs() > 1) {
                // selected value belongs to a xsd:choice
                String parent = CmsXmlUtils.removeLastXpathElement(bookmark);
                int depth = CmsResource.getPathLevel(bookmark);
                Iterator<String> i = getBookmarks().iterator();
                while (i.hasNext()) {
                    String bm = i.next();
                    if (bm.startsWith(parent) && (CmsResource.getPathLevel(bm) == depth)) {
                        result.add(getBookmark(bm));
                    }
                }
            } else {
                // selected value belongs to a xsd:sequence
                int index = 1;
                String bm = CmsXmlUtils.removeXpathIndex(bookmark);
                while (value != null) {
                    result.add(value);
                    index++;
                    String subpath = CmsXmlUtils.createXpathElement(bm, index);
                    value = getBookmark(subpath);
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#hasLocale(java.util.Locale)
     */
    public boolean hasLocale(Locale locale) {

        if (locale == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_NULL_LOCALE_0));
        }

        return m_locales.contains(locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#hasValue(java.lang.String, java.util.Locale)
     */
    public boolean hasValue(String path, Locale locale) {

        return null != getBookmark(CmsXmlUtils.createXpath(path, 1), locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#hasValue(java.lang.String, java.util.Locale, int)
     */
    public boolean hasValue(String path, Locale locale, int index) {

        return null != getBookmark(CmsXmlUtils.createXpath(path, index + 1), locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#initDocument()
     */
    public void initDocument() {

        initDocument(m_document, m_encoding, getContentDefinition());
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#isEnabled(java.lang.String, java.util.Locale)
     */
    public boolean isEnabled(String path, Locale locale) {

        return hasValue(path, locale);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#isEnabled(java.lang.String, java.util.Locale, int)
     */
    public boolean isEnabled(String path, Locale locale, int index) {

        return hasValue(path, locale, index);
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
     * @see org.opencms.xml.I_CmsXmlDocument#moveLocale(java.util.Locale, java.util.Locale)
     */
    public void moveLocale(Locale source, Locale destination) throws CmsXmlException {

        copyLocale(source, destination);
        removeLocale(source);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#removeLocale(java.util.Locale)
     */
    public void removeLocale(Locale locale) throws CmsXmlException {

        if (!hasLocale(locale)) {
            throw new CmsXmlException(Messages.get().container(Messages.ERR_LOCALE_NOT_AVAILABLE_1, locale));
        }

        Element rootNode = m_document.getRootElement();
        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(rootNode);
        String localeStr = locale.toString();
        while (i.hasNext()) {
            Element element = i.next();
            String language = element.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE, null);
            if ((language != null) && (localeStr.equals(language))) {
                // detach node with the locale
                element.detach();
                // there can be only one node for the locale
                break;
            }
        }

        // re-initialize the document bookmarks
        initDocument(m_document, m_encoding, getContentDefinition());
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
    @Override
    public String toString() {

        try {
            return CmsXmlUtils.marshal(m_document, m_encoding);
        } catch (CmsXmlException e) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_WRITE_XML_DOC_TO_STRING_0), e);
        }
    }

    /**
     * Validates the XML structure of the document with the DTD or XML schema used by the document.<p>
     * 
     * This is required in case someone modifies the XML structure of a  
     * document using the "edit control code" option.<p>
     * 
     * @param resolver the XML entity resolver to use
     * @throws CmsXmlException if the validation fails
     */
    public void validateXmlStructure(EntityResolver resolver) throws CmsXmlException {

        if (m_file != null) {
            // file is set, use bytes from file directly
            CmsXmlUtils.validateXmlStructure(m_file.getContents(), resolver);
        } else {
            // use XML document - note that this will be copied in a byte[] array first
            CmsXmlUtils.validateXmlStructure(m_document, m_encoding, resolver);
        }
    }

    /**
     * Adds a bookmark for the given value.<p>
     * 
     * @param path the lookup path to use for the bookmark
     * @param locale the locale to use for the bookmark
     * @param enabled if true, the value is enabled, if false it is disabled
     * @param value the value to bookmark
     */
    protected void addBookmark(String path, Locale locale, boolean enabled, I_CmsXmlContentValue value) {

        // add the locale (since the locales are a set adding them more then once does not matter)
        addLocale(locale);

        // add a bookmark to the provided value 
        m_bookmarks.put(getBookmarkName(path, locale), value);

        Set<Locale> sl;
        // update mapping of element name to locale
        if (enabled) {
            // only include enabled elements
            sl = m_elementLocales.get(path);
            if (sl != null) {
                sl.add(locale);
            } else {
                Set<Locale> set = new HashSet<Locale>();
                set.add(locale);
                m_elementLocales.put(path, set);
            }
        }
        // update mapping of locales to element names
        Set<String> sn = m_elementNames.get(locale);
        if (sn == null) {
            sn = new HashSet<String>();
            m_elementNames.put(locale, sn);
        }
        sn.add(path);
    }

    /**
     * Adds a locale to the set of locales of the XML document.<p>
     * 
     * @param locale the locale to add
     */
    protected void addLocale(Locale locale) {

        // add the locale to all locales in this dcoument
        m_locales.add(locale);
    }

    /**
     * Clears the XML document bookmarks.<p>
     */
    protected void clearBookmarks() {

        m_bookmarks.clear();
    }

    /**
     * Creates a partial deep element copy according to the set of element paths.<p>
     * Only elements contained in that set will be copied.
     * 
     * @param element the element to copy
     * @param copyElements the set of paths for elements to copy
     * 
     * @return a partial deep copy of <code>element</code>
     */
    protected Element createDeepElementCopy(Element element, Set<String> copyElements) {

        return createDeepElementCopyInternal(null, null, element, copyElements);
    }

    /**
     * Returns the bookmarked value for the given bookmark,
     * which must be a valid bookmark name. 
     * 
     * Use {@link #getBookmarks()} to get the list of all valid bookmark names.<p>
     * 
     * @param bookmark the bookmark name to look up 
     * @return the bookmarked value for the given bookmark
     */
    protected I_CmsXmlContentValue getBookmark(String bookmark) {

        return m_bookmarks.get(bookmark);
    }

    /**
     * Returns the bookmarked value for the given name.<p>
     * 
     * @param path the lookup path to use for the bookmark
     * @param locale the locale to get the bookmark for
     * @return the bookmarked value
     */
    protected I_CmsXmlContentValue getBookmark(String path, Locale locale) {

        return m_bookmarks.get(getBookmarkName(path, locale));
    }

    /**
     * Returns the names of all bookmarked elements.<p>
     * 
     * @return the names of all bookmarked elements
     */
    protected Set<String> getBookmarks() {

        return m_bookmarks.keySet();
    }

    /**
     * Internal method to look up a value, requires that the name already has been 
     * "normalized" for the bookmark lookup. 
     * 
     * This is required to find names like "title/subtitle" which are stored
     * internally as "title[0]/subtitle[0]" in the bookmarks. 
     * 
     * @param path the path to look up 
     * @param locale the locale to look up
     *  
     * @return the value found in the bookmarks 
     */
    protected I_CmsXmlContentValue getValueInternal(String path, Locale locale) {

        return getBookmark(path, locale);
    }

    /**
     * Initializes an XML document based on the provided document, encoding and content definition.<p>
     * 
     * @param document the base XML document to use for initializing
     * @param encoding the encoding to use when marshalling the document later
     * @param contentDefinition the content definition to use
     */
    protected abstract void initDocument(Document document, String encoding, CmsXmlContentDefinition contentDefinition);

    /**
     * Returns <code>true</code> if the auto correction feature is enabled for saving this XML content.<p>
     * 
     * @return <code>true</code> if the auto correction feature is enabled for saving this XML content
     */
    protected boolean isAutoCorrectionEnabled() {

        // by default, this method always returns false
        return false;
    }

    /**
     * Marshals (writes) the content of the current XML document 
     * into an output stream.<p>
     * 
     * @param out the output stream to write to
     * @param encoding the encoding to use
     * @return the output stream with the XML content
     * @throws CmsXmlException if something goes wrong
     */
    protected OutputStream marshal(OutputStream out, String encoding) throws CmsXmlException {

        return CmsXmlUtils.marshal(m_document, out, encoding);
    }

    /**
     * Removes the bookmark for an element with the given name and locale.<p>
     * 
     * @param path the lookup path to use for the bookmark
     * @param locale the locale of the element
     * @return the element removed from the bookmarks or null
     */
    protected I_CmsXmlContentValue removeBookmark(String path, Locale locale) {

        // remove mapping of element name to locale
        Set<Locale> sl;
        sl = m_elementLocales.get(path);
        if (sl != null) {
            sl.remove(locale);
        }
        // remove mapping of locale to element name
        Set<String> sn = m_elementNames.get(locale);
        if (sn != null) {
            sn.remove(path);
        }
        // remove the bookmark and return the removed element
        return m_bookmarks.remove(getBookmarkName(path, locale));
    }

    /**
     * Creates a partial deep element copy according to the set of element paths.<p>
     * Only elements contained in that set will be copied.
     * 
     * @param parentPath the path of the parent element or <code>null</code>, initially
     * @param parent the parent element
     * @param element the element to copy
     * @param copyElements the set of paths for elements to copy
     * 
     * @return a partial deep copy of <code>element</code>
     */
    private Element createDeepElementCopyInternal(
        String parentPath,
        Element parent,
        Element element,
        Set<String> copyElements) {

        String elName = element.getName();
        if (parentPath != null) {
            Element first = element.getParent().element(elName);
            int elIndex = (element.getParent().indexOf(element) - first.getParent().indexOf(first)) + 1;
            elName = parentPath + (parentPath.length() > 0 ? "/" : "") + elName.concat("[" + elIndex + "]");
        }

        if ((parentPath == null) || copyElements.contains(elName)) {
            // this is a content element we want to copy
            Element copy = element.createCopy();
            // copy.detach();
            if (parentPath != null) {
                parent.add(copy);
            }

            // check if we need to copy subelements, too
            boolean copyNested = (parentPath == null);
            for (Iterator<String> i = copyElements.iterator(); !copyNested && i.hasNext();) {
                String path = i.next();
                copyNested = !elName.equals(path) && path.startsWith(elName);
            }

            if (copyNested) {
                copy.clearContent();
                for (Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(element); i.hasNext();) {
                    Element el = i.next();
                    createDeepElementCopyInternal((parentPath == null) ? "" : elName, copy, el, copyElements);
                }
            }

            return copy;
        } else {
            return null;
        }
    }
}