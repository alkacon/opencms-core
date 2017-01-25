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

package org.opencms.xml.containerpage;

import org.opencms.ade.containerpage.CmsModelGroupHelper;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsMacroResolver;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.EntityResolver;

/**
 * Implementation of a object used to access and manage the xml data of a container page.<p>
 *
 * In addition to the XML content interface. It also provides access to more comfortable beans.
 *
 * @since 7.5.2
 *
 * @see #getContainerPage(CmsObject)
 */
public class CmsXmlContainerPage extends CmsXmlContent {

    /** XML node name constants. */
    public enum XmlNode {

        /** Container attribute node name. */
        Attribute,
        /** Main node name. */
        Containers,
        /** The create new element node name. */
        CreateNew,
        /** Container elements node name. */
        Elements,
        /** Element formatter node name. */
        Formatter,
        /** The is root container node name. */
        IsRootContainer,
        /** Container attribute key node name. */
        Key,
        /** Container name node name. */
        Name,
        /** Parent element instance id node name. */
        ParentInstanceId,
        /** Container type node name. */
        Type,
        /** Element URI node name. */
        Uri,
        /** Container attribute value node name. */
        Value;
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContainerPage.class);

    /** The container page objects. */
    private Map<Locale, CmsContainerPageBean> m_cntPages;

    /**
     * Hides the public constructor.<p>
     */
    protected CmsXmlContainerPage() {

        // noop
    }

    /**
     * Creates a new container page based on the provided XML document.<p>
     *
     * The given encoding is used when marshalling the XML again later.<p>
     *
     * @param cms the cms context, if <code>null</code> no link validation is performed
     * @param document the document to create the container page from
     * @param encoding the encoding of the container page
     * @param resolver the XML entity resolver to use
     */
    protected CmsXmlContainerPage(CmsObject cms, Document document, String encoding, EntityResolver resolver) {

        // must set document first to be able to get the content definition
        m_document = document;
        // for the next line to work the document must already be available
        m_contentDefinition = getContentDefinition(resolver);
        // initialize the XML content structure
        initDocument(cms, m_document, encoding, m_contentDefinition);
    }

    /**
     * Create a new container page based on the given default content,
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
    protected CmsXmlContainerPage(CmsObject cms, Locale locale, String modelUri)
    throws CmsException {

        // init model from given modelUri
        CmsFile modelFile = cms.readFile(modelUri, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        CmsXmlContainerPage model = CmsXmlContainerPageFactory.unmarshal(cms, modelFile);

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
                LOG.error(e.getMessage(), e);
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
    protected CmsXmlContainerPage(
        CmsObject cms,
        Locale locale,
        String encoding,
        CmsXmlContentDefinition contentDefinition) {

        // content definition must be set here since it's used during document creation
        m_contentDefinition = contentDefinition;
        // create the XML document according to the content definition
        Document document = m_contentDefinition.createDocument(cms, this, CmsLocaleManager.MASTER_LOCALE);
        // initialize the XML content structure
        initDocument(cms, document, encoding, m_contentDefinition);
    }

    /**
     * Saves a container page bean to the in-memory XML structure and returns the changed content.<p>
     *
     * @param cms the current CMS context
     * @param cntPage the container page bean
     * @return the new content for the container page
     * @throws CmsException if something goes wrong
     */
    public byte[] createContainerPageXml(CmsObject cms, CmsContainerPageBean cntPage) throws CmsException {

        // make sure all links are validated
        writeContainerPage(cms, cntPage);
        checkLinkConcistency(cms);
        return marshal();

    }

    /**
     * Gets the container page content as a bean.<p>
     *
     * @param cms the current CMS context
     * @return the bean containing the container page data
     */
    public CmsContainerPageBean getContainerPage(CmsObject cms) {

        Locale masterLocale = CmsLocaleManager.MASTER_LOCALE;
        Locale localeToLoad = null;
        // always use master locale if possible, otherwise use the first locale.
        // this is important for 'legacy' container pages which were created before container pages became locale independent
        if (m_cntPages.containsKey(masterLocale)) {
            localeToLoad = masterLocale;
        } else if (!m_cntPages.isEmpty()) {
            localeToLoad = m_cntPages.keySet().iterator().next();
        }
        if (localeToLoad == null) {
            return null;
        } else {
            return m_cntPages.get(localeToLoad);
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
     * @param cntPage the container page to save
     *
     * @throws CmsException if something goes wrong
     */
    public void save(CmsObject cms, CmsContainerPageBean cntPage) throws CmsException {

        save(cms, cntPage, false);
    }

    /**
     * Saves given container page in the current locale, and not only in memory but also to VFS.<p>
     *
     * @param cms the current cms context
     * @param cntPage the container page to save
     * @param ifChangedOnly <code>true</code> to only write the file if the content has changed
     *
     * @throws CmsException if something goes wrong
     */
    public void save(CmsObject cms, CmsContainerPageBean cntPage, boolean ifChangedOnly) throws CmsException {

        CmsFile file = getFile();
        byte[] data = createContainerPageXml(cms, cntPage);
        if (ifChangedOnly && Arrays.equals(file.getContents(), data)) {
            return;
        }
        // lock the file
        cms.lockResourceTemporary(file);
        file.setContents(data);
        cms.writeFile(file);
    }

    /**
     * Saves a container page in in-memory XML structure.<p>
     *
     * @param cms the current CMS context
     * @param cntPage the container page bean to save
     *
     * @throws CmsException if something goes wrong
     */
    public void writeContainerPage(CmsObject cms, CmsContainerPageBean cntPage) throws CmsException {

        // keep unused containers
        CmsContainerPageBean savePage = cleanupContainersContainers(cms, cntPage);
        savePage = removeEmptyContainers(cntPage);
        // Replace existing locales with master locale
        for (Locale locale : getLocales()) {
            removeLocale(locale);
        }
        Locale masterLocale = CmsLocaleManager.MASTER_LOCALE;
        addLocale(cms, masterLocale);

        // add the nodes to the raw XML structure
        Element parent = getLocaleNode(masterLocale);
        saveContainerPage(cms, parent, savePage);
        initDocument(m_document, m_encoding, m_contentDefinition);
    }

    /**
     * Checks the link consistency for a given locale and reinitializes the document afterwards.<p>
     *
     * @param cms the cms context
     */
    protected void checkLinkConcistency(CmsObject cms) {

        Locale masterLocale = CmsLocaleManager.MASTER_LOCALE;

        for (I_CmsXmlContentValue contentValue : getValues(masterLocale)) {
            if (contentValue instanceof CmsXmlVfsFileValue) {
                CmsLink link = ((CmsXmlVfsFileValue)contentValue).getLink(cms);
                link.checkConsistency(cms);
            }
        }
        initDocument();
    }

    /**
     * Removes all empty containers and merges the containers of the current document that are not used in the given container page with it.<p>
     *
     * @param cms the current CMS context
     * @param cntPage the container page to merge
     *
     * @return a new container page with the additional unused containers
     */
    protected CmsContainerPageBean cleanupContainersContainers(CmsObject cms, CmsContainerPageBean cntPage) {

        // get the used containers first
        Map<String, CmsContainerBean> currentContainers = cntPage.getContainers();
        List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
        for (String cntName : cntPage.getNames()) {
            CmsContainerBean container = currentContainers.get(cntName);
            if (!container.getElements().isEmpty()) {
                containers.add(container);
            }
        }

        // now get the unused containers
        CmsContainerPageBean currentContainerPage = getContainerPage(cms);
        if (currentContainerPage != null) {
            for (String cntName : currentContainerPage.getNames()) {
                if (!currentContainers.containsKey(cntName)) {
                    CmsContainerBean container = currentContainerPage.getContainers().get(cntName);
                    if (!container.getElements().isEmpty()) {
                        containers.add(container);
                    }
                }
            }
        }

        // check if any nested containers have lost their parent element

        // first collect all present elements
        Map<String, CmsContainerElementBean> pageElements = new HashMap<String, CmsContainerElementBean>();
        Map<String, String> parentContainers = new HashMap<String, String>();
        for (CmsContainerBean container : containers) {
            for (CmsContainerElementBean element : container.getElements()) {
                try {
                    element.initResource(cms);

                    if (!CmsModelGroupHelper.isModelGroupResource(element.getResource())) {
                        pageElements.put(element.getInstanceId(), element);
                        parentContainers.put(element.getInstanceId(), container.getName());
                    }
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        Iterator<CmsContainerBean> cntIt = containers.iterator();
        while (cntIt.hasNext()) {
            CmsContainerBean container = cntIt.next();
            // check all unused nested containers if their parent element is still part of the page
            if (!currentContainers.containsKey(container.getName())
                && (container.isNestedContainer() && !container.isRootContainer())) {
                boolean remove = !pageElements.containsKey(container.getParentInstanceId())
                    || container.getElements().isEmpty();
                if (!remove) {
                    // check if the parent element formatter is set to strictly render all nested containers
                    CmsContainerElementBean element = pageElements.get(container.getParentInstanceId());
                    String settingsKey = CmsFormatterConfig.getSettingsKeyForContainer(
                        parentContainers.get(element.getInstanceId()));
                    String formatterId = element.getIndividualSettings().get(settingsKey);
                    if (CmsUUID.isValidUUID(formatterId)) {
                        I_CmsFormatterBean formatterBean = OpenCms.getADEManager().getCachedFormatters(
                            false).getFormatters().get(new CmsUUID(formatterId));
                        remove = (formatterBean instanceof CmsFormatterBean)
                            && ((CmsFormatterBean)formatterBean).isStrictContainers();
                    }
                }
                if (remove) {
                    // remove the sub elements from the page list
                    for (CmsContainerElementBean element : container.getElements()) {
                        pageElements.remove(element.getInstanceId());
                    }
                    // remove the container
                    cntIt.remove();
                }
            }
        }

        return new CmsContainerPageBean(containers);
    }

    /**
     * Fills a {@link CmsXmlVfsFileValue} with the resource identified by the given id.<p>
     *
     * @param cms the current CMS context
     * @param element the XML element to fill
     * @param resourceId the ID identifying the resource to use
     *
     * @return the resource
     *
     * @throws CmsException if the resource can not be read
     */
    protected CmsResource fillResource(CmsObject cms, Element element, CmsUUID resourceId) throws CmsException {

        String xpath = element.getPath();
        int pos = xpath.lastIndexOf("/" + XmlNode.Containers.name() + "/");
        if (pos > 0) {
            xpath = xpath.substring(pos + 1);
        }
        CmsRelationType type = getHandler().getRelationType(xpath);
        CmsResource res = cms.readResource(resourceId, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsXmlVfsFileValue.fillEntry(element, res.getStructureId(), res.getRootPath(), type);
        return res;
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
        m_cntPages = new LinkedHashMap<Locale, CmsContainerPageBean>();
        clearBookmarks();

        // initialize the bookmarks
        for (Iterator<Element> itCntPages = CmsXmlGenericWrapper.elementIterator(
            m_document.getRootElement()); itCntPages.hasNext();) {
            Element cntPage = itCntPages.next();

            try {
                Locale locale = CmsLocaleManager.getLocale(
                    cntPage.attribute(CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE).getValue());

                addLocale(locale);

                List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
                for (Iterator<Element> itCnts = CmsXmlGenericWrapper.elementIterator(
                    cntPage,
                    XmlNode.Containers.name()); itCnts.hasNext();) {
                    Element container = itCnts.next();

                    // container itself
                    int cntIndex = CmsXmlUtils.getXpathIndexInt(container.getUniquePath(cntPage));
                    String cntPath = CmsXmlUtils.createXpathElement(container.getName(), cntIndex);
                    I_CmsXmlSchemaType cntSchemaType = definition.getSchemaType(container.getName());
                    I_CmsXmlContentValue cntValue = cntSchemaType.createValue(this, container, locale);
                    addBookmark(cntPath, locale, true, cntValue);
                    CmsXmlContentDefinition cntDef = ((CmsXmlNestedContentDefinition)cntSchemaType).getNestedContentDefinition();

                    // name
                    Element name = container.element(XmlNode.Name.name());
                    addBookmarkForElement(name, locale, container, cntPath, cntDef);

                    // type
                    Element type = container.element(XmlNode.Type.name());
                    addBookmarkForElement(type, locale, container, cntPath, cntDef);

                    // parent instance id
                    Element parentInstance = container.element(XmlNode.ParentInstanceId.name());
                    if (parentInstance != null) {
                        addBookmarkForElement(parentInstance, locale, container, cntPath, cntDef);
                    }

                    Element isRootContainer = container.element(XmlNode.IsRootContainer.name());
                    if (isRootContainer != null) {
                        addBookmarkForElement(isRootContainer, locale, container, cntPath, cntDef);
                    }

                    List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();
                    // Elements
                    for (Iterator<Element> itElems = CmsXmlGenericWrapper.elementIterator(
                        container,
                        XmlNode.Elements.name()); itElems.hasNext();) {
                        Element element = itElems.next();

                        // element itself
                        int elemIndex = CmsXmlUtils.getXpathIndexInt(element.getUniquePath(container));
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
                        Element createNewElement = element.element(XmlNode.CreateNew.name());
                        boolean createNew = (createNewElement != null)
                            && Boolean.parseBoolean(createNewElement.getStringValue());

                        // formatter
                        Element formatter = element.element(XmlNode.Formatter.name());
                        addBookmarkForElement(formatter, locale, element, elemPath, elemDef);
                        Element formatterLink = formatter.element(CmsXmlPage.NODE_LINK);
                        CmsUUID formatterId = null;
                        if (formatterLink == null) {
                            // this can happen when adding the elements node to the xml content
                            // it is not dangerous since the link has to be set before saving
                        } else {
                            formatterId = new CmsLink(formatterLink).getStructureId();
                        }

                        // the properties
                        Map<String, String> propertiesMap = CmsXmlContentPropertyHelper.readProperties(
                            this,
                            locale,
                            element,
                            elemPath,
                            elemDef);

                        if (elementId != null) {
                            elements.add(new CmsContainerElementBean(elementId, formatterId, propertiesMap, createNew));
                        }
                    }
                    CmsContainerBean newContainerBean = new CmsContainerBean(
                        name.getText(),
                        type.getText(),
                        parentInstance != null ? parentInstance.getText() : null,
                        (isRootContainer != null) && Boolean.valueOf(isRootContainer.getText()).booleanValue(),
                        elements);
                    containers.add(newContainerBean);
                }

                m_cntPages.put(locale, new CmsContainerPageBean(containers));
            } catch (NullPointerException e) {
                LOG.error(
                    org.opencms.xml.content.Messages.get().getBundle().key(
                        org.opencms.xml.content.Messages.LOG_XMLCONTENT_INIT_BOOKMARKS_0),
                    e);
            }
        }
    }

    /**
     * Removes all empty containers to clean up container page XML.<p>
     *
     * @param cntPage the container page bean
     *
     * @return the newly generated result
     */
    protected CmsContainerPageBean removeEmptyContainers(CmsContainerPageBean cntPage) {

        List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();
        for (CmsContainerBean container : cntPage.getContainers().values()) {
            if (container.getElements().size() > 0) {
                containers.add(container);
            }
        }
        return new CmsContainerPageBean(containers);
    }

    /**
     * Adds the given container page to the given element.<p>
     *
     * @param cms the current CMS object
     * @param parent the element to add it
     * @param cntPage the container page to add
     *
     * @throws CmsException if something goes wrong
     */
    protected void saveContainerPage(CmsObject cms, Element parent, CmsContainerPageBean cntPage) throws CmsException {

        parent.clearContent();

        // save containers in a defined order
        List<String> containerNames = new ArrayList<String>(cntPage.getNames());
        Collections.sort(containerNames);

        for (String containerName : containerNames) {
            CmsContainerBean container = cntPage.getContainers().get(containerName);

            // the container
            Element cntElement = parent.addElement(XmlNode.Containers.name());
            cntElement.addElement(XmlNode.Name.name()).addCDATA(container.getName());
            cntElement.addElement(XmlNode.Type.name()).addCDATA(container.getType());
            if (container.isNestedContainer()) {
                cntElement.addElement(XmlNode.ParentInstanceId.name()).addCDATA(container.getParentInstanceId());
            }
            if (container.isRootContainer()) {
                cntElement.addElement(XmlNode.IsRootContainer.name()).addText(Boolean.TRUE.toString());
            }

            // the elements
            for (CmsContainerElementBean element : container.getElements()) {
                Element elemElement = cntElement.addElement(XmlNode.Elements.name());

                // the element
                Element uriElem = elemElement.addElement(XmlNode.Uri.name());
                CmsResource uriRes = fillResource(cms, uriElem, element.getId());
                Element formatterElem = elemElement.addElement(XmlNode.Formatter.name());
                fillResource(cms, formatterElem, element.getFormatterId());
                if (element.isCreateNew()) {
                    Element createNewElem = elemElement.addElement(XmlNode.CreateNew.name());
                    createNewElem.addText(Boolean.TRUE.toString());
                }
                // the properties
                Map<String, String> properties = element.getIndividualSettings();
                Map<String, CmsXmlContentProperty> propertiesConf = OpenCms.getADEManager().getElementSettings(
                    cms,
                    uriRes);

                CmsXmlContentPropertyHelper.saveProperties(cms, elemElement, properties, propertiesConf);
            }
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
