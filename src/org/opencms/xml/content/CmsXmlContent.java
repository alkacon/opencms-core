/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.content;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeLocaleIndependentXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlAdeConfiguration;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLinkProcessor;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.I_CmsXmlContentHandler.SynchronizationMode;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Implementation of a XML content object,
 * used to access and manage structured content.<p>
 *
 * Use the {@link org.opencms.xml.content.CmsXmlContentFactory} to generate an
 * instance of this class.<p>
 *
 * @since 6.0.0
 */
public class CmsXmlContent extends A_CmsXmlDocument {

    /** The name of the XML content auto correction runtime attribute, this must always be a Boolean. */
    public static final String AUTO_CORRECTION_ATTRIBUTE = CmsXmlContent.class.getName() + ".autoCorrectionEnabled";

    /** The name of the version attribute. */
    public static final String A_VERSION = "version";

    /** The property to set to enable xerces schema validation. */
    public static final String XERCES_SCHEMA_PROPERTY = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";

    /**
     * Comparator to sort values according to the XML element position.<p>
     */
    private static final Comparator<I_CmsXmlContentValue> COMPARE_INDEX = new Comparator<I_CmsXmlContentValue>() {

        public int compare(I_CmsXmlContentValue v1, I_CmsXmlContentValue v2) {

            return v1.getIndex() - v2.getIndex();
        }
    };

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContent.class);

    /** Flag to control if auto correction is enabled when saving this XML content. */
    protected boolean m_autoCorrectionEnabled;

    /** The XML content definition object (i.e. XML schema) used by this content. */
    protected CmsXmlContentDefinition m_contentDefinition;

    /** Flag which records whether a version transformation was used when this content object was created. */
    private boolean m_isTransformedVersion;

    /** Indicates whether any broken links have been invalidated in the content. */
    protected boolean m_hasInvalidatedBrokenLinks;

    /**
     * Hides the public constructor.<p>
     */
    protected CmsXmlContent() {

        // noop
    }

    /**
     * Creates a new XML content based on the provided XML document.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * @param cms the cms context, if <code>null</code> no link validation is performed
     * @param document the document to create the xml content from
     * @param encoding the encoding of the xml content
     * @param resolver the XML entitiy resolver to use
     */
    protected CmsXmlContent(CmsObject cms, Document document, String encoding, EntityResolver resolver) {

        // must set document first to be able to get the content definition
        m_document = document;

        // for the next line to work the document must already be available
        m_contentDefinition = getContentDefinition(resolver);
        if (getSchemaVersion() < m_contentDefinition.getVersion()) {
            m_document = CmsVersionTransformer.transformDocumentToCurrentVersion(cms, document, m_contentDefinition);
            m_isTransformedVersion = true;
        }

        // initialize the XML content structure
        initDocument(cms, m_document, encoding, m_contentDefinition);
        if (m_isTransformedVersion) {
            visitAllValuesWith(value -> {
                if (value.isSimpleType()) {
                    // make sure values are in 'correct' format (e.g. using CDATA for text content)
                    value.setStringValue(cms, value.getStringValue(cms));
                }
            });
        }
    }

    /**
     * Create a new XML content based on the given default content,
     * that will have all language nodes of the default content and ensures the presence of the given locale.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param modelUri the absolute path to the XML content file acting as model
     *
     * @throws CmsException in case the model file is not found or not valid
     */
    protected CmsXmlContent(CmsObject cms, Locale locale, String modelUri)
    throws CmsException {

        // init model from given modelUri
        CmsFile modelFile = cms.readFile(modelUri, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        CmsXmlContent model = CmsXmlContentFactory.unmarshal(cms, modelFile);

        // initialize macro resolver to use on model file values
        CmsMacroResolver macroResolver = CmsMacroResolver.newInstance().setCmsObject(cms);
        macroResolver.setKeepEmptyMacros(true);

        // content defition must be set here since it's used during document creation
        m_contentDefinition = model.getContentDefinition();
        // get the document from the default content
        Document document = (Document)model.m_document.clone();
        // initialize the XML content structure
        initDocument(cms, document, model.getEncoding(), m_contentDefinition);
        // resolve eventual macros in the nodes
        visitAllValuesWith(new CmsXmlContentMacroVisitor(cms, macroResolver));
        if (!hasLocale(locale)) {
            // required locale not present, add it
            try {
                addLocale(cms, locale);
            } catch (CmsXmlException e) {
                // this can not happen since the locale does not exist
            }
        }
    }

    /**
     * Create a new XML content based on the given content definiton,
     * that will have one language node for the given locale all initialized with default values.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param encoding the encoding to use when marshalling the XML content later
     * @param contentDefinition the content definiton to create the content for
     */
    protected CmsXmlContent(CmsObject cms, Locale locale, String encoding, CmsXmlContentDefinition contentDefinition) {

        // content defition must be set here since it's used during document creation
        m_contentDefinition = contentDefinition;
        // create the XML document according to the content definition
        Document document = m_contentDefinition.createDocument(cms, this, locale);
        // initialize the XML content structure
        initDocument(cms, document, encoding, m_contentDefinition);
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#addLocale(org.opencms.file.CmsObject, java.util.Locale)
     */
    public void addLocale(CmsObject cms, Locale locale) throws CmsXmlException {

        if (hasLocale(locale)) {
            throw new CmsXmlException(
                org.opencms.xml.page.Messages.get().container(
                    org.opencms.xml.page.Messages.ERR_XML_PAGE_LOCALE_EXISTS_1,
                    locale));
        }
        // add element node for Locale
        m_contentDefinition.createLocale(cms, this, m_document.getRootElement(), locale);
        // re-initialize the bookmarks
        initDocument(cms, m_document, m_encoding, m_contentDefinition);
    }

    /**
     * Adds a new XML content value for the given element name and locale at the given index position
     * to this XML content document.<p>
     *
     * @param cms the current users OpenCms context
     * @param path the path to the XML content value element
     * @param locale the locale where to add the new value
     * @param index the index where to add the value (relative to all other values of this type)
     *
     * @return the created XML content value
     *
     * @throws CmsIllegalArgumentException if the given path is invalid
     * @throws CmsRuntimeException if the element identified by the path already occurred {@link I_CmsXmlSchemaType#getMaxOccurs()}
     *         or the given <code>index</code> is invalid (too high).
     */
    public I_CmsXmlContentValue addValue(CmsObject cms, String path, Locale locale, int index)
    throws CmsIllegalArgumentException, CmsRuntimeException {

        // get the schema type of the requested path
        I_CmsXmlSchemaType type = m_contentDefinition.getSchemaType(path);
        if (type == null) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_XMLCONTENT_UNKNOWN_ELEM_PATH_SCHEMA_1, path));
        }

        Element parentElement;
        String elementName;
        CmsXmlContentDefinition contentDefinition;
        if (CmsXmlUtils.isDeepXpath(path)) {
            // this is a nested content definition, so the parent element must be in the bookmarks
            String parentPath = CmsXmlUtils.createXpath(CmsXmlUtils.removeLastXpathElement(path), 1);
            Object o = getBookmark(parentPath, locale);
            if (o == null) {
                throw new CmsIllegalArgumentException(
                    Messages.get().container(Messages.ERR_XMLCONTENT_UNKNOWN_ELEM_PATH_1, path));
            }
            CmsXmlNestedContentDefinition parentValue = (CmsXmlNestedContentDefinition)o;
            parentElement = parentValue.getElement();
            elementName = CmsXmlUtils.getLastXpathElement(path);
            contentDefinition = parentValue.getNestedContentDefinition();
        } else {
            // the parent element is the locale element
            parentElement = getLocaleNode(locale);
            elementName = CmsXmlUtils.removeXpathIndex(path);
            contentDefinition = m_contentDefinition;
        }

        int insertIndex;

        if (contentDefinition.getChoiceMaxOccurs() > 0) {
            // for a choice sequence with maxOccurs we do not check the index position, we rather check if maxOccurs has already been hit
            // additionally we ensure that the insert index is not too big
            List<?> choiceSiblings = parentElement.content();
            int numSiblings = choiceSiblings != null ? choiceSiblings.size() : 0;

            if ((numSiblings >= contentDefinition.getChoiceMaxOccurs()) || (index > numSiblings)) {
                throw new CmsRuntimeException(
                    Messages.get().container(
                        Messages.ERR_XMLCONTENT_ADD_ELEM_INVALID_IDX_CHOICE_3,
                        Integer.valueOf(index),
                        elementName,
                        parentElement.getUniquePath()));
            }
            insertIndex = index;

        } else {
            // read the XML siblings from the parent node
            List<Element> siblings = CmsXmlGenericWrapper.elements(parentElement, elementName);

            if (siblings.size() > 0) {
                // we want to add an element to a sequence, and there are elements already of the same type

                if (siblings.size() >= type.getMaxOccurs()) {
                    // must not allow adding an element if max occurs would be violated
                    throw new CmsRuntimeException(
                        Messages.get().container(
                            Messages.ERR_XMLCONTENT_ELEM_MAXOCCURS_2,
                            elementName,
                            Integer.valueOf(type.getMaxOccurs())));
                }

                if (index > siblings.size()) {
                    // index position behind last element of the list
                    throw new CmsRuntimeException(
                        Messages.get().container(
                            Messages.ERR_XMLCONTENT_ADD_ELEM_INVALID_IDX_3,
                            Integer.valueOf(index),
                            Integer.valueOf(siblings.size())));
                }

                // check for offset required to append beyond last position
                int offset = (index == siblings.size()) ? 1 : 0;
                // get the element from the parent at the selected position
                Element sibling = siblings.get(index - offset);
                // check position of the node in the parent node content
                insertIndex = sibling.getParent().content().indexOf(sibling) + offset;
            } else {
                // we want to add an element to a sequence, but there are no elements of the same type yet

                if (index > 0) {
                    // since the element does not occur, index must be 0
                    throw new CmsRuntimeException(
                        Messages.get().container(
                            Messages.ERR_XMLCONTENT_ADD_ELEM_INVALID_IDX_2,
                            Integer.valueOf(index),
                            elementName));
                }

                // check where in the type sequence the type should appear
                int typeIndex = contentDefinition.getTypeSequence().indexOf(type);
                if (typeIndex == 0) {
                    // this is the first type, so we just add at the very first position
                    insertIndex = 0;
                } else {

                    // create a list of all element names that should occur before the selected type
                    List<String> previousTypeNames = new ArrayList<String>();
                    for (int i = 0; i < typeIndex; i++) {
                        I_CmsXmlSchemaType t = contentDefinition.getTypeSequence().get(i);
                        previousTypeNames.add(t.getName());
                    }

                    // iterate all elements of the parent node
                    Iterator<Node> i = CmsXmlGenericWrapper.content(parentElement).iterator();
                    int pos = 0;
                    while (i.hasNext()) {
                        Node node = i.next();
                        if (node instanceof Element) {
                            if (!previousTypeNames.contains(node.getName())) {
                                // the element name is NOT in the list of names that occurs before the selected type,
                                // so it must be an element that occurs AFTER the type
                                break;
                            }
                        }
                        pos++;
                    }
                    insertIndex = pos;
                }
            }
        }

        // just append the new element at the calculated position
        I_CmsXmlContentValue newValue = addValue(cms, parentElement, type, locale, insertIndex);

        // re-initialize this XML content
        initDocument(m_document, m_encoding, m_contentDefinition);

        // return the value instance that was stored in the bookmarks
        // just returning "newValue" isn't enough since this instance is NOT stored in the bookmarks
        return getBookmark(getBookmarkName(newValue.getPath(), locale));
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public CmsXmlContent clone() {

        CmsXmlContent clone = new CmsXmlContent();
        clone.m_autoCorrectionEnabled = m_autoCorrectionEnabled;
        clone.m_contentDefinition = m_contentDefinition;
        clone.m_conversion = m_conversion;
        clone.m_document = (Document)(m_document.clone());
        clone.m_encoding = m_encoding;
        clone.m_file = m_file;
        clone.initDocument();
        return clone;
    }

    /**
     * Copies the content of the given source locale to the given destination locale in this XML document.<p>
     *
     * @param source the source locale
     * @param destination the destination loacle
     * @param elements the set of elements to copy
     * @throws CmsXmlException if something goes wrong
     */
    public void copyLocale(Locale source, Locale destination, Set<String> elements) throws CmsXmlException {

        if (!hasLocale(source)) {
            throw new CmsXmlException(
                Messages.get().container(org.opencms.xml.Messages.ERR_LOCALE_NOT_AVAILABLE_1, source));
        }
        if (hasLocale(destination)) {
            throw new CmsXmlException(
                Messages.get().container(org.opencms.xml.Messages.ERR_LOCALE_ALREADY_EXISTS_1, destination));
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
                sourceElement = createDeepElementCopy(element, elements);
                // there can be only one node for the locale
                break;
            }
        }

        if (sourceElement == null) {
            // should not happen since this was checked already, just to make sure...
            throw new CmsXmlException(
                Messages.get().container(org.opencms.xml.Messages.ERR_LOCALE_NOT_AVAILABLE_1, source));
        }

        // switch locale value in attribute of copied node
        sourceElement.addAttribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE, destination.toString());
        // attach the copied node to the root node
        rootNode.add(sourceElement);

        // re-initialize the document bookmarks
        initDocument(m_document, m_encoding, getContentDefinition());
    }

    /**
     * Returns all simple type sub values.<p>
     *
     * @param value the value
     *
     * @return the simple type sub values
     */
    public List<I_CmsXmlContentValue> getAllSimpleSubValues(I_CmsXmlContentValue value) {

        List<I_CmsXmlContentValue> result = new ArrayList<I_CmsXmlContentValue>();
        for (I_CmsXmlContentValue subValue : getSubValues(value.getPath(), value.getLocale())) {
            if (subValue.isSimpleType()) {
                result.add(subValue);
            } else {
                result.addAll(getAllSimpleSubValues(subValue));
            }
        }
        return result;
    }

    /**
     * Returns the list of choice options for the given xpath in the selected locale.<p>
     *
     * In case the xpath does not select a nested choice content definition,
     * or in case the xpath does not exist at all, <code>null</code> is returned.<p>
     *
     * @param xpath the xpath to check the choice options for
     * @param locale the locale to check
     *
     * @return the list of choice options for the given xpath
     */
    public List<I_CmsXmlSchemaType> getChoiceOptions(String xpath, Locale locale) {

        I_CmsXmlSchemaType type = m_contentDefinition.getSchemaType(xpath);
        if (type == null) {
            // the xpath is not valid in the document
            return null;
        }
        if (!type.isChoiceType() && !type.isChoiceOption()) {
            // type is neither defining a choice nor part of a choice
            return null;
        }

        if (type.isChoiceType()) {
            // the type defines a choice sequence
            CmsXmlContentDefinition cd = ((CmsXmlNestedContentDefinition)type).getNestedContentDefinition();
            return cd.getTypeSequence();
        }

        // type must be a choice option
        I_CmsXmlContentValue value = getValue(xpath, locale);
        if ((value == null) || (value.getContentDefinition().getChoiceMaxOccurs() > 1)) {
            // value does not exist in the document or is a multiple choice value
            return type.getContentDefinition().getTypeSequence();
        }

        // value must be a single choice that already exists in the document, so we must return null
        return null;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getContentDefinition()
     */
    public CmsXmlContentDefinition getContentDefinition() {

        return m_contentDefinition;
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#getHandler()
     */
    public I_CmsXmlContentHandler getHandler() {

        return getContentDefinition().getContentHandler();
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#getLinkProcessor(org.opencms.file.CmsObject, org.opencms.staticexport.CmsLinkTable)
     */
    public CmsLinkProcessor getLinkProcessor(CmsObject cms, CmsLinkTable linkTable) {

        // initialize link processor
        String relativeRoot = null;
        if (m_file != null) {
            relativeRoot = CmsResource.getParentFolder(cms.getSitePath(m_file));
        }
        return new CmsLinkProcessor(cms, linkTable, getEncoding(), relativeRoot);
    }

    /**
     * Returns the XML root element node for the given locale.<p>
     *
     * @param locale the locale to get the root element for
     *
     * @return the XML root element node for the given locale
     *
     * @throws CmsRuntimeException if no language element is found in the document
     */
    public Element getLocaleNode(Locale locale) throws CmsRuntimeException {

        String localeStr = locale.toString();
        Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(m_document.getRootElement());
        while (i.hasNext()) {
            Element element = i.next();
            if (localeStr.equals(element.attributeValue(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE))) {
                // language element found, return it
                return element;
            }
        }
        // language element was not found
        throw new CmsRuntimeException(Messages.get().container(Messages.ERR_XMLCONTENT_MISSING_LOCALE_1, locale));
    }

    /**
     * Gets the schema version (or 0 if no schema version is set).
     *
     * @return the schema version
     */
    public int getSchemaVersion() {

        return CmsXmlUtils.getSchemaVersion(m_document);
    }

    /**
     * Returns all simple type values below a given path.<p>
     *
     * @param elementPath the element path
     * @param locale the content locale
     *
     * @return the simple type values
     */
    public List<I_CmsXmlContentValue> getSimpleValuesBelowPath(String elementPath, Locale locale) {

        List<I_CmsXmlContentValue> result = new ArrayList<I_CmsXmlContentValue>();
        for (I_CmsXmlContentValue value : getValuesByPath(elementPath, locale)) {
            if (value.isSimpleType()) {
                result.add(value);
            } else {
                result.addAll(getAllSimpleSubValues(value));
            }
        }

        return result;
    }

    /**
     * Returns the list of sub-value for the given xpath in the selected locale.<p>
     *
     * @param path the xpath to look up the sub-value for
     * @param locale the locale to use
     *
     * @return the list of sub-value for the given xpath in the selected locale
     */
    @Override
    public List<I_CmsXmlContentValue> getSubValues(String path, Locale locale) {

        List<I_CmsXmlContentValue> result = new ArrayList<I_CmsXmlContentValue>();
        String bookmark = getBookmarkName(CmsXmlUtils.createXpath(path, 1), locale);
        int depth = CmsResource.getPathLevel(bookmark) + 1;
        Iterator<String> i = getBookmarks().iterator();
        while (i.hasNext()) {
            String bm = i.next();
            if (bm.startsWith(bookmark) && (CmsResource.getPathLevel(bm) == depth)) {
                result.add(getBookmark(bm));
            }
        }
        if (result.size() > 0) {
            Collections.sort(result, COMPARE_INDEX);
        }
        return result;
    }

    /**
     * Returns all values of the given element path.<p>
     *
     * @param elementPath the element path
     * @param locale the content locale
     *
     * @return the values
     */
    public List<I_CmsXmlContentValue> getValuesByPath(String elementPath, Locale locale) {

        String[] pathElements = elementPath.split("/");
        List<I_CmsXmlContentValue> values = getValues(pathElements[0], locale);
        for (int i = 1; i < pathElements.length; i++) {
            List<I_CmsXmlContentValue> subValues = new ArrayList<I_CmsXmlContentValue>();
            for (I_CmsXmlContentValue value : values) {
                subValues.addAll(getValues(CmsXmlUtils.concatXpath(value.getPath(), pathElements[i]), locale));
            }
            if (subValues.isEmpty()) {
                values = Collections.emptyList();
                break;
            }
            values = subValues;
        }
        return values;
    }

    /**
     * Returns the value sequence for the selected element xpath in this XML content.<p>
     *
     * If the given element xpath is not valid according to the schema of this XML content,
     * <code>null</code> is returned.<p>
     *
     * @param xpath the element xpath to get the value sequence for
     * @param locale the locale to get the value sequence for
     *
     * @return the value sequence for the selected element name in this XML content
     */
    public CmsXmlContentValueSequence getValueSequence(String xpath, Locale locale) {

        I_CmsXmlSchemaType type = m_contentDefinition.getSchemaType(xpath);
        if (type == null) {
            return null;
        }
        return new CmsXmlContentValueSequence(xpath, locale, this);
    }

    /**
     * Returns <code>true</code> if choice options exist for the given xpath in the selected locale.<p>
     *
     * In case the xpath does not select a nested choice content definition,
     * or in case the xpath does not exist at all, <code>false</code> is returned.<p>
     *
     * @param xpath the xpath to check the choice options for
     * @param locale the locale to check
     *
     * @return <code>true</code> if choice options exist for the given xpath in the selected locale
     */
    public boolean hasChoiceOptions(String xpath, Locale locale) {

        List<I_CmsXmlSchemaType> options = getChoiceOptions(xpath, locale);
        if ((options == null) || (options.size() <= 1)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if any broken links have been invalidated in this content.
     *
     * @return true if broken links have been invalidated
     */
    public boolean hasInvalidatedBrokenLinks() {

        return m_hasInvalidatedBrokenLinks;
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#isAutoCorrectionEnabled()
     */
    @Override
    public boolean isAutoCorrectionEnabled() {

        return m_autoCorrectionEnabled;
    }

    /**
     * Checks if the content is locale independent.<p>
     *
     * @return true if the content is locale independent
     */
    public boolean isLocaleIndependent() {

        CmsFile file = getFile();
        if (CmsResourceTypeXmlContainerPage.isContainerPage(file)
            || OpenCms.getResourceManager().matchResourceType(
                CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME,
                file.getTypeId())
            || OpenCms.getResourceManager().matchResourceType(
                CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME,
                file.getTypeId())) {
            return true;
        }

        try {
            I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(file);
            if ((resourceType instanceof CmsResourceTypeLocaleIndependentXmlContent)
                || (resourceType instanceof CmsResourceTypeXmlAdeConfiguration)) {
                return true;
            }
        } catch (Exception e) {
            // ignore
        }
        return false;

    }

    /**
     * Checks if a version transformation was used when creating this content object.
     *
     * @return true if a version transformation was used when creating this content object
     */
    public boolean isTransformedVersion() {

        return m_isTransformedVersion;
    }

    /**
     * Removes an existing XML content value of the given element name and locale at the given index position
     * from this XML content document.<p>
     *
     * @param name the name of the XML content value element
     * @param locale the locale where to remove the value
     * @param index the index where to remove the value (relative to all other values of this type)
     */
    public void removeValue(String name, Locale locale, int index) {

        // first get the value from the selected locale and index
        I_CmsXmlContentValue value = getValue(name, locale, index);

        if (!value.isChoiceOption()) {
            // check for the min / max occurs constrains
            List<I_CmsXmlContentValue> values = getValues(name, locale);
            if (values.size() <= value.getMinOccurs()) {
                // must not allow removing an element if min occurs would be violated
                throw new CmsRuntimeException(
                    Messages.get().container(
                        Messages.ERR_XMLCONTENT_ELEM_MINOCCURS_2,
                        name,
                        Integer.valueOf(value.getMinOccurs())));
            }
        }

        // detach the value node from the XML document
        value.getElement().detach();

        // re-initialize this XML content
        initDocument(m_document, m_encoding, m_contentDefinition);
    }

    /**
     * Resolves the mappings for all values of this XML content.<p>
     *
     * @param cms the current users OpenCms context
     */
    public void resolveMappings(CmsObject cms) {

        // iterate through all initialized value nodes in this XML content
        CmsXmlContentMappingVisitor visitor = new CmsXmlContentMappingVisitor(cms, this);
        visitAllValuesWith(visitor);
    }

    /**
     * Sets the flag to control if auto correction is enabled when saving this XML content.<p>
     *
     * @param value the flag to control if auto correction is enabled when saving this XML content
     */
    public void setAutoCorrectionEnabled(boolean value) {

        m_autoCorrectionEnabled = value;
    }

    /**
     * Synchronizes the locale independent fields for the given locale.<p>
     *
     * @param cms the cms context
     * @param skipPaths the paths to skip
     * @param sourceLocale the source locale
     */
    public void synchronizeLocaleIndependentValues(CmsObject cms, Collection<String> skipPaths, Locale sourceLocale) {

        if (getContentDefinition().getContentHandler().hasSynchronizedElements() && (getLocales().size() > 1)) {
            for (Map.Entry<String, SynchronizationMode> syncEntry : getContentDefinition().getContentHandler().getSynchronizations(
                true).asMap().entrySet()) {
                String elementPath = syncEntry.getKey();
                SynchronizationMode syncMode = syncEntry.getValue();
                if (syncMode == SynchronizationMode.none) {
                    continue;
                }
                synchronizeElement(cms, elementPath, skipPaths, sourceLocale, syncMode);
            }
        }
    }

    /**
     * @see org.opencms.xml.I_CmsXmlDocument#validate(org.opencms.file.CmsObject)
     */
    public CmsXmlContentErrorHandler validate(CmsObject cms) {

        // iterate through all initialized value nodes in this XML content
        CmsXmlContentValidationVisitor visitor = new CmsXmlContentValidationVisitor(cms);
        visitAllValuesWith(visitor);

        return visitor.getErrorHandler();
    }

    /**
     * Visits all values of this XML content with the given value visitor.<p>
     *
     * Please note that the order in which the values are visited may NOT be the
     * order they appear in the XML document. It is ensured that the parent
     * of a nested value is visited before the element it contains.<p>
     *
     * @param visitor the value visitor implementation to visit the values with
     */
    public void visitAllValuesWith(I_CmsXmlContentValueVisitor visitor) {

        List<String> bookmarks = new ArrayList<String>(getBookmarks());
        Collections.sort(bookmarks);

        for (int i = 0; i < bookmarks.size(); i++) {

            String key = bookmarks.get(i);
            I_CmsXmlContentValue value = getBookmark(key);
            visitor.visit(value);
        }
    }

    /**
     * Creates a new bookmark for the given element.<p>
     *
     * @param element the element to create the bookmark for
     * @param locale the locale
     * @param parent the parent node of the element
     * @param parentPath the parent's path
     * @param parentDef the parent's content definition
     */
    protected void addBookmarkForElement(
        Element element,
        Locale locale,
        Element parent,
        String parentPath,
        CmsXmlContentDefinition parentDef) {

        int elemIndex = CmsXmlUtils.getXpathIndexInt(element.getUniquePath(parent));
        String elemPath = CmsXmlUtils.concatXpath(
            parentPath,
            CmsXmlUtils.createXpathElement(element.getName(), elemIndex));
        I_CmsXmlSchemaType elemSchemaType = parentDef.getSchemaType(element.getName());
        I_CmsXmlContentValue elemValue = elemSchemaType.createValue(this, element, locale);
        addBookmark(elemPath, locale, true, elemValue);
    }

    /**
     * Adds a bookmark for the given value.<p>
     *
     * @param value the value to bookmark
     * @param path the lookup path to use for the bookmark
     * @param locale the locale to use for the bookmark
     * @param enabled if true, the value is enabled, if false it is disabled
     */
    protected void addBookmarkForValue(I_CmsXmlContentValue value, String path, Locale locale, boolean enabled) {

        addBookmark(path, locale, enabled, value);
    }

    /**
     * Adds a new XML schema type with the default value to the given parent node.<p>
     *
     * @param cms the cms context
     * @param parent the XML parent element to add the new value to
     * @param type the type of the value to add
     * @param locale the locale to add the new value for
     * @param insertIndex the index in the XML document where to add the XML node
     *
     * @return the created XML content value
     */
    protected I_CmsXmlContentValue addValue(
        CmsObject cms,
        Element parent,
        I_CmsXmlSchemaType type,
        Locale locale,
        int insertIndex) {

        // first generate the XML element for the new value
        Element element = type.generateXml(cms, this, parent, locale);
        // detach the XML element from the appended position in order to insert it at the required position
        element.detach();
        // add the XML element at the required position in the parent XML node
        CmsXmlGenericWrapper.content(parent).add(insertIndex, element);
        // create the type and return it
        I_CmsXmlContentValue value = type.createValue(this, element, locale);
        // generate the default value again - required for nested mappings because only now the full path is available
        String defaultValue = m_contentDefinition.getContentHandler().getDefault(cms, value, locale);
        if (defaultValue != null) {
            // only if there is a default value available use it to overwrite the initial default
            value.setStringValue(cms, defaultValue);
        }
        // finally return the value
        return value;
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#getBookmark(java.lang.String)
     */
    @Override
    protected I_CmsXmlContentValue getBookmark(String bookmark) {

        // allows package classes to directly access the bookmark information of the XML content
        return super.getBookmark(bookmark);
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#getBookmarks()
     */
    @Override
    protected Set<String> getBookmarks() {

        // allows package classes to directly access the bookmark information of the XML content
        return super.getBookmarks();
    }

    /**
     * Returns the content definition object for this xml content object.<p>
     *
     * @param resolver the XML entity resolver to use, required for VFS access
     *
     * @return the content definition object for this xml content object
     *
     * @throws CmsRuntimeException if the schema location attribute (<code>systemId</code>)cannot be found,
     *         parsing of the schema fails, an underlying IOException occurs or unmarshalling fails
     *
     */
    protected CmsXmlContentDefinition getContentDefinition(EntityResolver resolver) throws CmsRuntimeException {

        String schemaLocation = m_document.getRootElement().attributeValue(
            I_CmsXmlSchemaType.XSI_NAMESPACE_ATTRIBUTE_NO_SCHEMA_LOCATION);
        // Note regarding exception handling:
        // Since this object already is a valid XML content object,
        // it must have a valid schema, otherwise it would not exist.
        // Therefore the exceptions should never be really thrown.
        if (schemaLocation == null) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_XMLCONTENT_MISSING_SCHEMA_0));
        }

        try {
            return CmsXmlContentDefinition.unmarshal(schemaLocation, resolver);
        } catch (SAXException e) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_XML_SCHEMA_PARSE_1, schemaLocation), e);
        } catch (IOException e) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_XML_SCHEMA_IO_1, schemaLocation), e);
        } catch (CmsXmlException e) {
            throw new CmsRuntimeException(
                Messages.get().container(Messages.ERR_XMLCONTENT_UNMARSHAL_1, schemaLocation),
                e);
        }
    }

    /**
     * Initializes an XML document based on the provided document, encoding and content definition.<p>
     *
     * Checks the links and removes invalid ones in the initialized document.<p>
     *
     * @param cms the current users OpenCms content
     * @param document the base XML document to use for initializing
     * @param encoding the encoding to use when marshalling the document later
     * @param definition the content definition to use
     */
    protected void initDocument(CmsObject cms, Document document, String encoding, CmsXmlContentDefinition definition) {

        initDocument(document, encoding, definition);
        // check invalid links
        if (cms != null) {
            // this will remove all invalid links
            getHandler().invalidateBrokenLinks(cms, this);
        }
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#initDocument(org.dom4j.Document, java.lang.String, org.opencms.xml.CmsXmlContentDefinition)
     */
    @Override
    protected void initDocument(Document document, String encoding, CmsXmlContentDefinition definition) {

        m_document = document;
        m_contentDefinition = definition;
        m_encoding = CmsEncoder.lookupEncoding(encoding, encoding);
        m_elementLocales = new HashMap<String, Set<Locale>>();
        m_elementNames = new HashMap<Locale, Set<String>>();
        m_locales = new HashSet<Locale>();
        clearBookmarks();

        // initialize the bookmarks
        for (Iterator<Element> i = CmsXmlGenericWrapper.elementIterator(m_document.getRootElement()); i.hasNext();) {
            Element node = i.next();
            try {
                Locale locale = CmsLocaleManager.getLocale(
                    node.attribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE).getValue());

                addLocale(locale);
                processSchemaNode(node, null, locale, definition);
            } catch (NullPointerException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_XMLCONTENT_INIT_BOOKMARKS_0), e);
            }
        }

    }

    /**
     * Processes a document node and extracts the values of the node according to the provided XML
     * content definition.<p>
     *
     * @param root the root node element to process
     * @param rootPath the Xpath of the root node in the document
     * @param locale the locale
     * @param definition the XML content definition to use for processing the values
     */
    protected void processSchemaNode(Element root, String rootPath, Locale locale, CmsXmlContentDefinition definition) {

        // iterate all XML nodes
        List<Node> content = CmsXmlGenericWrapper.content(root);
        for (int i = content.size() - 1; i >= 0; i--) {
            Node node = content.get(i);
            if (!(node instanceof Element)) {
                // this node is not an element, so it must be a white space text node, remove it
                node.detach();
            } else {
                // node must be an element
                Element element = (Element)node;
                String name = element.getName();
                int xpathIndex = CmsXmlUtils.getXpathIndexInt(element.getUniquePath(root));

                // build the Xpath expression for the current node
                String path;
                if (rootPath != null) {
                    StringBuffer b = new StringBuffer(rootPath.length() + name.length() + 6);
                    b.append(rootPath);
                    b.append('/');
                    b.append(CmsXmlUtils.createXpathElement(name, xpathIndex));
                    path = b.toString();
                } else {
                    path = CmsXmlUtils.createXpathElement(name, xpathIndex);
                }

                // create a XML content value element
                I_CmsXmlSchemaType schemaType = definition.getSchemaType(name);

                if (schemaType != null) {
                    // directly add simple type to schema
                    I_CmsXmlContentValue value = schemaType.createValue(this, element, locale);
                    addBookmark(path, locale, true, value);

                    if (!schemaType.isSimpleType()) {
                        // recurse for nested schema
                        CmsXmlNestedContentDefinition nestedSchema = (CmsXmlNestedContentDefinition)schemaType;
                        processSchemaNode(element, path, locale, nestedSchema.getNestedContentDefinition());
                    }
                } else {
                    // unknown XML node name according to schema
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_XMLCONTENT_INVALID_ELEM_2,
                                name,
                                definition.getSchemaLocation()));
                    }
                }
            }
        }
    }

    /**
     * Sets the file this XML content is written to.<p>
     *
     * @param file the file this XML content content is written to
     */
    protected void setFile(CmsFile file) {

        m_file = file;
    }

    /**
     * Ensures the parent values to the given path are created.<p>
     *
     * @param cms the cms context
     * @param valuePath the value path
     * @param locale the content locale
     */
    private void ensureParentValues(CmsObject cms, String valuePath, Locale locale) {

        if (valuePath.contains("/")) {
            String parentPath = valuePath.substring(0, valuePath.lastIndexOf("/"));
            if (!hasValue(parentPath, locale)) {
                ensureParentValues(cms, parentPath, locale);
                int index = CmsXmlUtils.getXpathIndexInt(parentPath) - 1;
                addValue(cms, parentPath, locale, index);
            }
        }
    }

    /**
     * Removes all surplus values of locale independent fields in the other locales.<p>
     *
     * @param elementPath the element path
     * @param valueCount the value count
     * @param sourceLocale the source locale
     */
    private void removeSurplusValuesInOtherLocales(String elementPath, int valueCount, Locale sourceLocale) {

        for (Locale locale : getLocales()) {
            if (locale.equals(sourceLocale)) {
                continue;
            }
            List<I_CmsXmlContentValue> localeValues = getValues(elementPath, locale);
            for (int i = valueCount; i < localeValues.size(); i++) {
                removeValue(elementPath, locale, 0);
            }
        }
    }

    /**
     * Removes all values of the given path in the other locales.<p>
     *
     * @param elementPath the element path
     * @param sourceLocale the source locale
     */
    private void removeValuesInOtherLocales(String elementPath, Locale sourceLocale) {

        for (Locale locale : getLocales()) {
            if (locale.equals(sourceLocale)) {
                continue;
            }
            while (hasValue(elementPath, locale)) {
                removeValue(elementPath, locale, 0);
            }
        }
    }

    /**
     * Sets the value in all other locales.<p>
     *
     * @param cms the cms context
     * @param value the value
     * @param requiredParent the path to the required parent value
     */
    private void setValueForOtherLocales(CmsObject cms, I_CmsXmlContentValue value, String requiredParent) {

        if (!value.isSimpleType()) {
            throw new IllegalArgumentException();
        }
        for (Locale locale : getLocales()) {
            if (locale.equals(value.getLocale())) {
                continue;
            }
            String valuePath = value.getPath();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(requiredParent) || hasValue(requiredParent, locale)) {
                ensureParentValues(cms, valuePath, locale);
                if (hasValue(valuePath, locale)) {
                    I_CmsXmlContentValue localeValue = getValue(valuePath, locale);
                    localeValue.setStringValue(cms, value.getStringValue(cms));
                } else {
                    int index = CmsXmlUtils.getXpathIndexInt(valuePath) - 1;
                    I_CmsXmlContentValue localeValue = addValue(cms, valuePath, locale, index);
                    localeValue.setStringValue(cms, value.getStringValue(cms));
                }
            }
        }
    }

    /**
     * Synchronizes the values for the given element path.<p>
     *
     * @param cms the cms context
     * @param elementPath the element path
     * @param skipPaths the paths to skip
     * @param sourceLocale the source locale
     * @param syncMode the synchronization mode
     */
    private void synchronizeElement(
        CmsObject cms,
        String elementPath,
        Collection<String> skipPaths,
        Locale sourceLocale,
        SynchronizationMode syncMode) {

        if (syncMode == SynchronizationMode.none) {
            return;
        }

        if (elementPath.contains("/")) {
            String parentPath = CmsXmlUtils.removeLastXpathElement(elementPath);
            List<I_CmsXmlContentValue> parentValues = getValuesByPath(parentPath, sourceLocale);
            String elementName = CmsXmlUtils.getLastXpathElement(elementPath);
            for (I_CmsXmlContentValue parentValue : parentValues) {
                String valuePath = CmsXmlUtils.concatXpath(parentValue.getPath(), elementName);
                boolean skip = false;
                for (String skipPath : skipPaths) {
                    if (valuePath.startsWith(skipPath)) {
                        skip = true;
                        break;
                    }
                }
                if (!skip) {
                    if (hasValue(valuePath, sourceLocale)) {
                        List<I_CmsXmlContentValue> subValues = getValues(valuePath, sourceLocale);
                        removeSurplusValuesInOtherLocales(elementPath, subValues.size(), sourceLocale);
                        for (I_CmsXmlContentValue value : subValues) {
                            if (value.isSimpleType()) {
                                setValueForOtherLocales(
                                    cms,
                                    value,
                                    syncMode == SynchronizationMode.strong
                                    ? null // strong -> auto-create parent values
                                    : CmsXmlUtils.removeLastXpathElement(valuePath));
                            } else {
                                List<I_CmsXmlContentValue> simpleValues = getAllSimpleSubValues(value);
                                for (I_CmsXmlContentValue simpleValue : simpleValues) {
                                    setValueForOtherLocales(cms, simpleValue, parentValue.getPath());
                                }
                            }
                        }
                    } else {
                        removeValuesInOtherLocales(valuePath, sourceLocale);
                    }
                }
            }
        } else {
            if (hasValue(elementPath, sourceLocale)) {
                List<I_CmsXmlContentValue> subValues = getValues(elementPath, sourceLocale);
                removeSurplusValuesInOtherLocales(elementPath, subValues.size(), sourceLocale);
                for (I_CmsXmlContentValue value : subValues) {
                    if (value.isSimpleType()) {
                        setValueForOtherLocales(cms, value, null);
                    } else {
                        List<I_CmsXmlContentValue> simpleValues = getAllSimpleSubValues(value);
                        for (I_CmsXmlContentValue simpleValue : simpleValues) {
                            setValueForOtherLocales(cms, simpleValue, null);
                        }
                    }
                }
            } else {
                removeValuesInOtherLocales(elementPath, sourceLocale);
            }
        }

        // this handles the case where a elementPath is missing in the source locale because its parent value is missing
        if (syncMode == SynchronizationMode.strong) {
            if (getValuesByPath(elementPath, sourceLocale).size() == 0) {
                boolean minOccursWarning = false;
                boolean changed = false;
                for (Locale locale : getLocales()) {
                    if (!locale.equals(sourceLocale)) {
                        List<I_CmsXmlContentValue> candidatesForRemoval = getValuesByPath(elementPath, locale);
                        for (I_CmsXmlContentValue candidate : candidatesForRemoval) {
                            if (candidate.getMinOccurs() > 0) {
                                // it makes no sense to remove only part of the values
                                minOccursWarning = true;
                                break;
                            } else {
                                candidate.getElement().detach();
                                changed = true;
                            }
                        }
                    }
                }
                if (changed) {
                    initDocument(m_document, m_encoding, m_contentDefinition);
                }
                if (minOccursWarning) {
                    String schema = getContentDefinition().getSchemaLocation();
                    LOG.warn(
                        " synchronization setting 'strong' for '"
                            + elementPath
                            + "' in '"
                            + schema
                            + "' is incorrect because it is a required value in an optional nested content.");
                }
            }
        }
    }

}
