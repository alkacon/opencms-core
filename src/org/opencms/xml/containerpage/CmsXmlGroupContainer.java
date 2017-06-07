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

package org.opencms.xml.containerpage;

import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentMacroVisitor;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
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
import org.xml.sax.EntityResolver;

/**
 * Implementation of a object used to access and manage the xml data of a group container.<p>
 *
 * In addition to the XML content interface. It also provides access to more comfortable beans.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlGroupContainer extends CmsXmlContent {

    /** XML node name constants. */
    public enum XmlNode {

        /** Container description node name. */
        Description,
        /** Container elements node name. */
        Element,
        /** Main node name. */
        GroupContainers,
        /** Container title node name. */
        Title,
        /** Container type node name. */
        Type,
        /** File list URI node name. */
        Uri;
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlGroupContainer.class);

    /** The group container objects. */
    private Map<Locale, CmsGroupContainerBean> m_groupContainers;

    /**
     * Hides the public constructor.<p>
     */
    protected CmsXmlGroupContainer() {

        // do nothing
    }

    /**
     * Creates a new group container based on the provided XML document.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * @param cms the cms context, if <code>null</code> no link validation is performed
     * @param document the document to create the container page from
     * @param encoding the encoding of the container page
     * @param resolver the XML entity resolver to use
     */
    protected CmsXmlGroupContainer(CmsObject cms, Document document, String encoding, EntityResolver resolver) {

        // must set document first to be able to get the content definition
        m_document = document;
        // for the next line to work the document must already be available
        m_contentDefinition = getContentDefinition(resolver);
        // initialize the XML content structure
        initDocument(cms, m_document, encoding, m_contentDefinition);
    }

    /**
     * Create a new group container based on the given default content,
     * that will have all language nodes of the default content and ensures the presence of the given locale.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param modelUri the absolute path to the container page file acting as model
     *
     * @throws CmsException in case the model file is not found or not valid
     */
    protected CmsXmlGroupContainer(CmsObject cms, Locale locale, String modelUri)
    throws CmsException {

        // init model from given modelUri
        CmsFile modelFile = cms.readFile(modelUri, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        CmsXmlGroupContainer model = CmsXmlGroupContainerFactory.unmarshal(cms, modelFile);

        // initialize macro resolver to use on model file values
        CmsMacroResolver macroResolver = CmsMacroResolver.newInstance().setCmsObject(cms);

        // content definition must be set here since it's used during document creation
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
     * Create a new container page based on the given content definition,
     * that will have one language node for the given locale all initialized with default values.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param encoding the encoding to use when marshalling the container page later
     * @param contentDefinition the content definition to create the content for
     */
    protected CmsXmlGroupContainer(
        CmsObject cms,
        Locale locale,
        String encoding,
        CmsXmlContentDefinition contentDefinition) {

        // content definition must be set here since it's used during document creation
        m_contentDefinition = contentDefinition;
        // create the XML document according to the content definition
        Document document = m_contentDefinition.createDocument(cms, this, locale);
        // initialize the XML content structure
        initDocument(cms, document, encoding, m_contentDefinition);
    }

    /**
     * Removes all locales from the element group XML.<p>
     *
     * @throws CmsXmlException if something goes wrong
     */
    public void clearLocales() throws CmsXmlException {

        for (Locale locale : getLocales()) {
            removeLocale(locale);
        }
    }

    /**
     * Returns the group container bean for the given locale.<p>
     *
     * @param cms the cms context
     *
     * @return the group container bean
     */
    public CmsGroupContainerBean getGroupContainer(CmsObject cms) {

        if (m_groupContainers.containsKey(CmsLocaleManager.MASTER_LOCALE)) {
            return m_groupContainers.get(CmsLocaleManager.MASTER_LOCALE);
        } else if (!m_groupContainers.isEmpty()) {
            return m_groupContainers.get(m_groupContainers.keySet().iterator().next());
        } else {
            return null;
        }
    }

    /**
     * @see org.opencms.xml.content.CmsXmlContent#isAutoCorrectionEnabled()
     */
    @Override
    public boolean isAutoCorrectionEnabled() {

        return true;
    }

    /**
     * Saves given container page in the current locale, and not only in memory but also to VFS.<p>
     *
     * @param cms the current cms context
     * @param groupContainer the group-container page to save
     * @param locale the locale to save
     *
     * @throws CmsException if something goes wrong
     */
    public void save(CmsObject cms, CmsGroupContainerBean groupContainer, Locale locale) throws CmsException {

        CmsFile file = getFile();

        // lock the file
        cms.lockResourceTemporary(cms.getSitePath(file));

        // wipe the locale
        if (hasLocale(locale)) {
            removeLocale(locale);
        }

        addLocale(cms, locale);

        // add the nodes to the raw XML structure
        Element parent = getLocaleNode(locale);
        saveGroupContainer(cms, parent, groupContainer);

        // generate bookmarks
        initDocument(m_document, m_encoding, m_contentDefinition);

        // write to VFS
        file.setContents(marshal());
        cms.writeFile(file);
    }

    /**
     * Fills a {@link CmsXmlVfsFileValue} with the resource identified by the given id.<p>
     *
     * @param cms the current CMS context
     * @param element the XML element to fill
     * @param res the resource to use
     */
    protected void fillResource(CmsObject cms, Element element, CmsResource res) {

        String xpath = element.getPath();
        int pos = xpath.lastIndexOf("/" + XmlNode.GroupContainers.name() + "/");
        if (pos > 0) {
            xpath = xpath.substring(pos + 1);
        }
        CmsRelationType type = getHandler().getRelationType(xpath);
        CmsXmlVfsFileValue.fillEntry(element, res.getStructureId(), res.getRootPath(), type);
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
        m_groupContainers = new HashMap<Locale, CmsGroupContainerBean>();
        clearBookmarks();

        // initialize the bookmarks
        for (Iterator<Element> itGroupContainers = CmsXmlGenericWrapper.elementIterator(
            m_document.getRootElement()); itGroupContainers.hasNext();) {
            Element cntPage = itGroupContainers.next();

            try {
                Locale locale = CmsLocaleManager.getLocale(
                    cntPage.attribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE).getValue());

                addLocale(locale);
                Element groupContainer = cntPage.element(XmlNode.GroupContainers.name());

                // container itself
                int cntIndex = CmsXmlUtils.getXpathIndexInt(groupContainer.getUniquePath(cntPage));
                String cntPath = CmsXmlUtils.createXpathElement(groupContainer.getName(), cntIndex);
                I_CmsXmlSchemaType cntSchemaType = definition.getSchemaType(groupContainer.getName());
                I_CmsXmlContentValue cntValue = cntSchemaType.createValue(this, groupContainer, locale);
                addBookmark(cntPath, locale, true, cntValue);
                CmsXmlContentDefinition cntDef = ((CmsXmlNestedContentDefinition)cntSchemaType).getNestedContentDefinition();

                //title
                Element title = groupContainer.element(XmlNode.Title.name());
                addBookmarkForElement(title, locale, groupContainer, cntPath, cntDef);

                //description
                Element description = groupContainer.element(XmlNode.Description.name());
                addBookmarkForElement(description, locale, groupContainer, cntPath, cntDef);

                // types
                Set<String> types = new HashSet<String>();
                for (Iterator<Element> itTypes = CmsXmlGenericWrapper.elementIterator(
                    groupContainer,
                    XmlNode.Type.name()); itTypes.hasNext();) {
                    Element type = itTypes.next();
                    addBookmarkForElement(type, locale, groupContainer, cntPath, cntDef);
                    String typeName = type.getTextTrim();
                    if (!CmsStringUtil.isEmptyOrWhitespaceOnly(typeName)) {
                        types.addAll(CmsContainer.splitType(typeName));
                    }
                }

                List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
                // Elements
                for (Element element : CmsXmlGenericWrapper.elementIterable(groupContainer, XmlNode.Element.name())) {
                    // element itself
                    int elemIndex = CmsXmlUtils.getXpathIndexInt(element.getUniquePath(groupContainer));
                    String elemPath = CmsXmlUtils.concatXpath(
                        cntPath,
                        CmsXmlUtils.createXpathElement(element.getName(), elemIndex));
                    I_CmsXmlSchemaType elemSchemaType = cntDef.getSchemaType(element.getName());
                    I_CmsXmlContentValue elemValue = elemSchemaType.createValue(this, element, locale);
                    addBookmark(elemPath, locale, true, elemValue);
                    CmsXmlContentDefinition elemDef = ((CmsXmlNestedContentDefinition)elemSchemaType).getNestedContentDefinition();

                    // uri
                    Element uri = element.element(XmlNode.Uri.name());
                    addBookmarkForElement(uri, locale, element, elemPath, elemDef);
                    Element uriLink = uri.element(CmsXmlPage.NODE_LINK);
                    CmsUUID elementId = null;
                    if (uriLink == null) {
                        // this can happen when adding the elements node to the xml content
                        // it is not dangerous since the link has to be set before saving
                    } else {
                        elementId = new CmsLink(uriLink).getStructureId();
                    }

                    //                    Element createNewElement = element.element(CmsXmlContainerPage.XmlNode.CreateNew.name());
                    //                    boolean createNew = (createNewElement != null)
                    //                        && Boolean.parseBoolean(createNewElement.getStringValue());

                    // propeties
                    Map<String, String> propertiesMap = CmsXmlContentPropertyHelper.readProperties(
                        this,
                        locale,
                        element,
                        elemPath,
                        elemDef);

                    if (elementId != null) {
                        elements.add(new CmsContainerElementBean(elementId, null, propertiesMap, false));
                    }
                }
                m_groupContainers.put(
                    locale,
                    new CmsGroupContainerBean(title.getText(), description.getText(), elements, types));
            } catch (NullPointerException e) {
                LOG.error(
                    org.opencms.xml.content.Messages.get().getBundle().key(
                        org.opencms.xml.content.Messages.LOG_XMLCONTENT_INIT_BOOKMARKS_0),
                    e);
            }
        }
    }

    /**
     * Adds the given container page to the given element.<p>
     *
     * @param cms the current CMS object
     * @param parent the element to add it
     * @param groupContainer the container page to add
     *
     * @throws CmsException if something goes wrong
     */
    protected void saveGroupContainer(CmsObject cms, Element parent, CmsGroupContainerBean groupContainer)
    throws CmsException {

        parent.clearContent();
        Element groupContainerElem = parent.addElement(XmlNode.GroupContainers.name());

        groupContainerElem.addElement(XmlNode.Title.name()).addCDATA(groupContainer.getTitle());
        groupContainerElem.addElement(XmlNode.Description.name()).addCDATA(groupContainer.getDescription());

        for (String type : groupContainer.getTypes()) {
            groupContainerElem.addElement(XmlNode.Type.name()).addCDATA(type);
        }

        // the elements
        for (CmsContainerElementBean element : groupContainer.getElements()) {
            CmsResource res = cms.readResource(element.getId(), CmsResourceFilter.IGNORE_EXPIRATION);
            if (OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName().equals(
                CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME)) {
                LOG.warn(Messages.get().container(Messages.LOG_WARN_ELEMENT_GROUP_INSIDE_ELEMENT_GROUP_0));
                continue;
            }
            Element elemElement = groupContainerElem.addElement(XmlNode.Element.name());

            // the element
            Element uriElem = elemElement.addElement(XmlNode.Uri.name());
            fillResource(cms, uriElem, res);

            // the properties
            Map<String, String> properties = element.getIndividualSettings();
            Map<String, CmsXmlContentProperty> propertiesConf = OpenCms.getADEManager().getElementSettings(cms, res);

            CmsXmlContentPropertyHelper.saveProperties(cms, elemElement, properties, propertiesConf);
        }
    }

    /**
     * @see org.opencms.xml.content.CmsXmlContent#setFile(org.opencms.file.CmsFile)
     */
    @Override
    protected void setFile(CmsFile file) {

        // just for visibility from the factory
        super.setFile(file);
    }
}
