/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsXmlSitemap.java,v $
 * Date   : $Date: 2010/05/18 12:58:17 $
 * Version: $Revision: 1.23 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.xml.sitemap;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentMacroVisitor;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collections;
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

/**
 * Implementation of a object used to access and manage the xml data of a sitemaps.<p>
 * 
 * In addition to the XML content interface. It also provides access to more comfortable beans. 
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.23 $ 
 * 
 * @since 7.5.2
 * 
 * @see #getSitemap(CmsObject, Locale)
 */
public class CmsXmlSitemap extends CmsXmlContent {

    /** XML node name constants. */
    public enum XmlNode {

        /** Entry point node name. */
        EntryPoint,
        /** Entry ID node name. */
        Id,
        /** Entry name node name. */
        Name,
        /** Site entry node name. */
        SiteEntry,
        /** Title node name. */
        Title,
        /** Vfs File node name. */
        VfsFile;
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlSitemap.class);

    /** The sitemap objects. */
    private Map<Locale, CmsSitemapBean> m_sitemaps;

    /**
     * Hides the public constructor.<p>
     */
    protected CmsXmlSitemap() {

        // noop
    }

    /**
     * Creates a new sitemap based on the provided XML document.<p>
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param cms the cms context, if <code>null</code> no link validation is performed 
     * @param document the document to create the sitemap from
     * @param encoding the encoding of the sitemap
     * @param resolver the XML entity resolver to use
     */
    protected CmsXmlSitemap(CmsObject cms, Document document, String encoding, EntityResolver resolver) {

        // must set document first to be able to get the content definition
        m_document = document;
        // for the next line to work the document must already be available
        m_contentDefinition = getContentDefinition(resolver);
        // initialize the XML content structure
        initDocument(cms, m_document, encoding, m_contentDefinition);
    }

    /**
     * Create a new sitemap based on the given default content,
     * that will have all language nodes of the default content and ensures the presence of the given locale.<p> 
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param modelUri the absolute path to the sitemap file acting as model
     * 
     * @throws CmsException in case the model file is not found or not valid
     */
    protected CmsXmlSitemap(CmsObject cms, Locale locale, String modelUri)
    throws CmsException {

        // init model from given modelUri
        CmsFile modelFile = cms.readFile(modelUri, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        CmsXmlSitemap model = CmsXmlSitemapFactory.unmarshal(cms, modelFile);

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
     * Create a new sitemap based on the given content definition,
     * that will have one language node for the given locale all initialized with default values.<p> 
     * 
     * The given encoding is used when marshalling the XML again later.<p>
     * 
     * @param cms the current users OpenCms content
     * @param locale the locale to generate the default content for
     * @param encoding the encoding to use when marshalling the sitemap later
     * @param contentDefinition the content definition to create the content for
     */
    protected CmsXmlSitemap(CmsObject cms, Locale locale, String encoding, CmsXmlContentDefinition contentDefinition) {

        // content definition must be set here since it's used during document creation
        m_contentDefinition = contentDefinition;
        // create the XML document according to the content definition
        Document document = m_contentDefinition.createDocument(cms, this, locale);
        // initialize the XML content structure
        initDocument(cms, document, encoding, m_contentDefinition);
    }

    /**
     * No validation since rescursive schema.<p>
     * 
     * @see org.opencms.xml.content.CmsXmlContent#addValue(org.opencms.file.CmsObject, java.lang.String, java.util.Locale, int)
     */
    @Override
    public I_CmsXmlContentValue addValue(CmsObject cms, String path, Locale locale, int index)
    throws CmsIllegalArgumentException, CmsRuntimeException {

        // get the schema type of the requested path           
        I_CmsXmlSchemaType type = m_contentDefinition.getSchemaType(path);
        if (type == null) {
            throw new CmsIllegalArgumentException(org.opencms.xml.content.Messages.get().container(
                org.opencms.xml.content.Messages.ERR_XMLCONTENT_UNKNOWN_ELEM_PATH_SCHEMA_1,
                path));
        }

        Element parentElement;
        String elementName;
        CmsXmlContentDefinition contentDefinition;
        if (CmsXmlUtils.isDeepXpath(path)) {
            // this is a nested content definition, so the parent element must be in the bookmarks
            String parentPath = CmsXmlUtils.createXpath(CmsXmlUtils.removeLastXpathElement(path), 1);
            Object o = getBookmark(parentPath, locale);
            if (o == null) {
                throw new CmsIllegalArgumentException(org.opencms.xml.content.Messages.get().container(
                    org.opencms.xml.content.Messages.ERR_XMLCONTENT_UNKNOWN_ELEM_PATH_1,
                    path));
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

        // read the XML siblings from the parent node
        List<Element> siblings = CmsXmlGenericWrapper.elements(parentElement, elementName);

        int insertIndex;

        if (contentDefinition.getChoiceMaxOccurs() > 0) {
            // for a choice sequence we do not check the index position, we rather do a full XML validation afterwards

            insertIndex = index;
        } else if (siblings.size() > 0) {
            // we want to add an element to a sequence, and there are elements already of the same type

            if (siblings.size() >= type.getMaxOccurs()) {
                // must not allow adding an element if max occurs would be violated
                throw new CmsRuntimeException(org.opencms.xml.content.Messages.get().container(
                    org.opencms.xml.content.Messages.ERR_XMLCONTENT_ELEM_MAXOCCURS_2,
                    elementName,
                    new Integer(type.getMaxOccurs())));
            }

            if (index > siblings.size()) {
                // index position behind last element of the list
                throw new CmsRuntimeException(org.opencms.xml.content.Messages.get().container(
                    org.opencms.xml.content.Messages.ERR_XMLCONTENT_ADD_ELEM_INVALID_IDX_3,
                    new Integer(index),
                    new Integer(siblings.size())));
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
                throw new CmsRuntimeException(org.opencms.xml.content.Messages.get().container(
                    org.opencms.xml.content.Messages.ERR_XMLCONTENT_ADD_ELEM_INVALID_IDX_2,
                    new Integer(index),
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

        I_CmsXmlContentValue newValue;
        if (contentDefinition.getChoiceMaxOccurs() > 0) {
            // for a choice we do a full XML validation
            try {
                // append the new element at the calculated position
                newValue = addValue(cms, parentElement, type, locale, insertIndex);
                // validate the XML structure to see if the index position was valid
                // CmsXmlUtils.validateXmlStructure(m_document, m_encoding, new CmsXmlEntityResolver(cms));                
                // can not be validated since recursive schema
            } catch (Exception e) {
                throw new CmsRuntimeException(org.opencms.xml.content.Messages.get().container(
                    org.opencms.xml.content.Messages.ERR_XMLCONTENT_ADD_ELEM_INVALID_IDX_CHOICE_3,
                    new Integer(insertIndex),
                    elementName,
                    parentElement.getUniquePath()));
            }
        } else {
            // just append the new element at the calculated position
            newValue = addValue(cms, parentElement, type, locale, insertIndex);
        }

        // re-initialize this XML content 
        initDocument(m_document, m_encoding, m_contentDefinition);

        // return the value instance that was stored in the bookmarks 
        // just returning "newValue" isn't enough since this instance is NOT stored in the bookmarks
        return getBookmark(getBookmarkName(newValue.getPath(), locale));
    }

    /**
     * Applies the given changes in the current locale, and only in memory.<p>
     * 
     * @param cms the current cms context
     * @param changes the changes to apply
     * 
     * @throws CmsException if something goes wrong
     */
    public void applyChanges(CmsObject cms, List<I_CmsSitemapChange> changes) throws CmsException {

        Locale locale = cms.getRequestContext().getLocale();

        // create the locale
        if (!hasLocale(locale)) {
            addLocale(cms, locale);
        }

        Set<String> modified = new HashSet<String>();
        // apply the changes to the raw XML structure
        for (I_CmsSitemapChange change : changes) {
            switch (change.getType()) {
                case DELETE:
                    modified.addAll(deleteEntry(cms, (CmsSitemapChangeDelete)change));
                    break;
                case NEW:
                    modified.add(newEntry(cms, (CmsSitemapChangeNew)change));
                    break;
                case MOVE:
                    modified.addAll(moveEntry(cms, (CmsSitemapChangeMove)change));
                    break;
                case EDIT:
                    modified.add(editEntry(cms, (CmsSitemapChangeEdit)change));
                    break;
                default:

            }
        }

        // generate bookmarks
        initDocument(m_document, m_encoding, m_contentDefinition);

        // event handling
        List<String> result = new ArrayList<String>(modified);
        Collections.sort(result);
        OpenCms.fireCmsEvent(new CmsEvent(
            I_CmsEventListener.EVENT_SITEMAP_CHANGED,
            Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCES, result)));
    }

    /**
     * Returns the sitemap bean for the given locale.<p>
     *
     * @param cms the cms context
     * @param locale the locale to use
     *
     * @return the sitemap bean
     */
    public CmsSitemapBean getSitemap(CmsObject cms, Locale locale) {

        Locale theLocale = locale;
        CmsSitemapBean result = m_sitemaps.get(theLocale);
        if (result != null) {
            return result;
        }
        LOG.warn(Messages.get().container(
            Messages.LOG_SITEMAP_LOCALE_NOT_FOUND_2,
            getFile().getRootPath(),
            theLocale.toString()).key());
        theLocale = OpenCms.getLocaleManager().getDefaultLocale(cms, getFile());
        result = m_sitemaps.get(theLocale);
        if (result != null) {
            return result;
        }
        if (getLocales().isEmpty()) {
            // no locale not found!!
            LOG.error(Messages.get().container(
                Messages.LOG_SITEMAP_LOCALE_NOT_FOUND_2,
                getFile().getRootPath(),
                theLocale).key());
            return null;
        }
        // use first locale
        return m_sitemaps.get(getLocales().get(0));
    }

    /**
     * @see org.opencms.xml.content.CmsXmlContent#isAutoCorrectionEnabled()
     */
    @Override
    public boolean isAutoCorrectionEnabled() {

        return true;
    }

    /**
     * @see org.opencms.xml.A_CmsXmlDocument#validateXmlStructure(org.xml.sax.EntityResolver)
     */
    @Override
    public void validateXmlStructure(EntityResolver resolver) {

        // can not be validated since recursive schema
        return;
    }

    /**
     * Low level deletion of a sitemap entry.<p>
     * 
     * @param cms the CMS context
     * @param change the change to apply
     * 
     * @return the site path of the entries that need to be re-indexed
     * 
     * @throws CmsException if something goes wrong
     */
    protected List<String> deleteEntry(CmsObject cms, CmsSitemapChangeDelete change) throws CmsException {

        Element entryElement = getElement(cms, change.getSitePath());
        if (entryElement == null) {
            // element not found
            String sitemapPath = (getFile() == null) ? null : cms.getSitePath(getFile());
            throw new CmsSitemapEntryNotFoundException(change.getSitePath(), sitemapPath);
        }

        entryElement.detach();

        // return touched entries
        return getSubTreePaths(
            CmsResource.getParentFolder(cms.getRequestContext().addSiteRoot(change.getSitePath())),
            entryElement);
    }

    /**
     * Low level edition of a sitemap entry.<p>
     * 
     * @param cms the CMS context
     * @param change the change to apply
     * 
     * @return the site path of the entries that need to be re-indexed
     * 
     * @throws CmsException if something goes wrong
     */
    protected String editEntry(CmsObject cms, CmsSitemapChangeEdit change) throws CmsException {

        Element entryElement = getElement(cms, change.getSitePath());
        if (entryElement == null) {
            // element not found
            throw new CmsSitemapEntryNotFoundException(change.getSitePath(), getFile() == null
            ? null
            : cms.getSitePath(getFile()));
        }

        // the title
        if (change.getTitle() != null) {
            Element titleElement = entryElement.element(XmlNode.Title.name());
            if (!titleElement.getText().equals(change.getTitle())) {
                titleElement.clearContent();
                titleElement.addCDATA(change.getTitle());
            }
        }

        // the vfs reference
        if (change.getVfsPath() != null) {
            Element vfsElement = entryElement.element(XmlNode.VfsFile.name());
            fillResource(cms, vfsElement, change.getVfsPath());
        }

        // the properties
        if (change.getProperties() != null) {
            CmsXmlContentPropertyHelper.saveProperties(
                cms,
                entryElement,
                CmsXmlContentDefinition.getContentHandlerForResource(cms, getFile()).getProperties(),
                change.getProperties());
        }

        return cms.getRequestContext().addSiteRoot(change.getSitePath());
    }

    /**
     * Fills a {@link CmsXmlVfsFileValue} with the resource identified by the given id.<p>
     * 
     * @param cms the current CMS context
     * @param element the XML element to fill
     * @param resourcePath the site path identifying the resource to use
     * 
     * @throws CmsException if the resource can not be read
     */
    protected void fillResource(CmsObject cms, Element element, String resourcePath) throws CmsException {

        String xpath = element.getPath();
        int pos = xpath.lastIndexOf("/" + XmlNode.SiteEntry.name() + "/");
        if (pos > 0) {
            xpath = xpath.substring(pos + 1);
        }
        CmsRelationType type = getContentDefinition().getContentHandler().getRelationType(xpath);
        CmsResource res = cms.readResource(resourcePath);
        CmsXmlVfsFileValue.fillEntry(element, res.getStructureId(), res.getRootPath(), type);
    }

    /**
     * Returns the request content value if available, if not a new one will be created.<p>
     * 
     * @param cms the current cms context
     * @param path the value's path
     * @param locale the value's locale
     * @param index the value's index
     * 
     * @return the request content value
     */
    protected I_CmsXmlContentValue getContentValue(CmsObject cms, String path, Locale locale, int index) {

        I_CmsXmlContentValue idValue = getValue(path, locale, index);
        if (idValue == null) {
            idValue = addValue(cms, path, locale, index);
        }
        return idValue;
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
        m_sitemaps = new HashMap<Locale, CmsSitemapBean>();
        clearBookmarks();

        // initialize the bookmarks
        for (Iterator<Element> itSitemaps = CmsXmlGenericWrapper.elementIterator(m_document.getRootElement()); itSitemaps.hasNext();) {
            Element sitemap = itSitemaps.next();

            try {
                Locale locale = CmsLocaleManager.getLocale(sitemap.attribute(
                    CmsXmlContentDefinition.XSD_ATTRIBUTE_VALUE_LANGUAGE).getValue());

                addLocale(locale);
                // get entry point
                Element entryPoint = sitemap.element(XmlNode.EntryPoint.name());
                String entryPointPath = null;
                if (entryPoint != null) {
                    addBookmarkForElement(entryPoint, locale, sitemap, null, definition);
                    Element linkEntryPoint = entryPoint.element(CmsXmlPage.NODE_LINK);
                    if (linkEntryPoint == null) {
                        // this can happen when adding the entry node to the xml content
                        // it is not dangerous since the link has to be set before saving 
                    } else {
                        entryPointPath = new CmsLink(linkEntryPoint).getUri();
                    }
                } else {
                    entryPointPath = "/";
                }

                // get the entries
                List<CmsSitemapEntry> entries = readSiteEntries(sitemap, "", definition, locale, entryPointPath);
                // create the sitemap
                m_sitemaps.put(locale, new CmsSitemapBean(locale, entryPointPath, entries));
            } catch (NullPointerException e) {
                LOG.error(org.opencms.xml.content.Messages.get().getBundle().key(
                    org.opencms.xml.content.Messages.LOG_XMLCONTENT_INIT_BOOKMARKS_0), e);
            }
        }
    }

    /**
     * Low level rename/move of a sitemap entry.<p>
     * 
     * @param cms the CMS context
     * @param change the change to apply
     * 
     * @return the site path of the entries that need to be re-indexed
     * 
     * @throws CmsException if something goes wrong
     */
    protected List<String> moveEntry(CmsObject cms, CmsSitemapChangeMove change) throws CmsException {

        Element entryElement = getElement(cms, change.getSourcePath());
        if (entryElement == null) {
            // element not found
            String sitemapPath = (getFile() == null) ? null : cms.getSitePath(getFile());
            throw new CmsSitemapEntryNotFoundException(change.getSourcePath(), sitemapPath);
        }

        String srcParentPath = CmsResource.getParentFolder(change.getSourcePath());
        String destParentPath = CmsResource.getParentFolder(change.getDestinationPath());
        Element newParent = getElement(cms, destParentPath);
        if (newParent == null) {
            // new parent entry not found
            String sitemapPath = (getFile() == null) ? null : cms.getSitePath(getFile());
            throw new CmsSitemapEntryNotFoundException(destParentPath, sitemapPath);
        }

        // move
        if (!srcParentPath.equals(destParentPath)) {
            // detach from old place
            entryElement.detach();
            // attach at new place
            entryElement.setParent(newParent);
            // at the right position
            List<Element> siblings = CmsCollectionsGenericWrapper.list(newParent.elements());
            siblings.add(change.getDestinationPosition(), entryElement);
        } else {
            // check position
            List<Element> siblings = CmsCollectionsGenericWrapper.list(newParent.elements());
            if (siblings.indexOf(entryElement) != change.getDestinationPosition()) {
                siblings.remove(entryElement);
                siblings.add(change.getDestinationPosition(), entryElement);
            }
        }

        // rename
        String srcName = CmsResource.getName(change.getSourcePath());
        String destName = CmsResource.getName(change.getDestinationPath());
        if (!srcName.equals(destName)) {
            Element nameElement = entryElement.element(XmlNode.Name.name());
            nameElement.clearContent();
            if (destName.endsWith("/")) {
                destName = destName.substring(0, destName.length() - 1);
            }
            nameElement.addCDATA(destName);
        }

        // return touched entries
        return getSubTreePaths(cms.getRequestContext().addSiteRoot(destParentPath), entryElement);
    }

    /**
     * Low level insertion of a sitemap entry.<p>
     * 
     * @param cms the CMS context
     * @param change the change to apply
     * 
     * @return the site path of the entries that need to be re-indexed
     * 
     * @throws CmsException if something goes wrong
     */
    protected String newEntry(CmsObject cms, CmsSitemapChangeNew change) throws CmsException {

        Element parent = getElement(cms, CmsResource.getParentFolder(change.getSitePath()));
        if (parent == null) {
            // parent entry not found
            String sitemapPath = (getFile() == null) ? null : cms.getSitePath(getFile());
            throw new CmsSitemapEntryNotFoundException(change.getSitePath(), sitemapPath);
        }
        // the entry
        Element entryElement = parent.addElement(XmlNode.SiteEntry.name());
        entryElement.addElement(XmlNode.Id.name()).addCDATA(new CmsUUID().toString());
        String name = CmsResource.getName(change.getSitePath());
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        entryElement.addElement(XmlNode.Name.name()).addCDATA(name);
        entryElement.addElement(XmlNode.Title.name()).addCDATA(change.getTitle());

        // the vfs reference
        Element vfsFile = entryElement.addElement(XmlNode.VfsFile.name());
        fillResource(cms, vfsFile, change.getVfsPath());

        // the properties
        CmsXmlContentPropertyHelper.saveProperties(
            cms,
            entryElement,
            CmsXmlContentDefinition.getContentHandlerForResource(cms, getFile()).getProperties(),
            change.getProperties());

        return cms.getRequestContext().addSiteRoot(change.getSitePath());
    }

    /**
     * Recursive method to retrieve the site entries with sub entries from the raw XML structure.<p>
     * 
     * @param rootElem the root element
     * @param rootPath the root element path
     * @param rootDef the root content definition
     * @param locale the current locale
     * @param parentUri the parent URI
     * 
     * @return the site entries with sub entries
     */
    protected List<CmsSitemapEntry> readSiteEntries(
        Element rootElem,
        String rootPath,
        CmsXmlContentDefinition rootDef,
        Locale locale,
        String parentUri) {

        List<CmsSitemapEntry> entries = new ArrayList<CmsSitemapEntry>();
        for (Iterator<Element> itCnts = CmsXmlGenericWrapper.elementIterator(rootElem, XmlNode.SiteEntry.name()); itCnts.hasNext();) {
            Element entry = itCnts.next();

            // entry itself
            int entryIndex = CmsXmlUtils.getXpathIndexInt(entry.getUniquePath(rootElem));
            String entryPath = CmsXmlUtils.createXpathElement(entry.getName(), entryIndex);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(rootPath)) {
                entryPath = CmsXmlUtils.concatXpath(rootPath, entryPath);
            }
            I_CmsXmlSchemaType entrySchemaType = rootDef.getSchemaType(entry.getName());
            I_CmsXmlContentValue entryValue = entrySchemaType.createValue(this, entry, locale);
            addBookmark(entryPath, locale, true, entryValue);
            CmsXmlContentDefinition entryDef = ((CmsXmlNestedContentDefinition)entrySchemaType).getNestedContentDefinition();

            // id
            Element id = entry.element(XmlNode.Id.name());
            if (id == null) {
                // create element if missing
                id = entry.addElement(XmlNode.Id.name());
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(id.getTextTrim())) {
                // create a new id if missing
                id.addCDATA(new CmsUUID().toString());
            }
            if (!CmsUUID.isValidUUID(id.getTextTrim())) {
                // create a new valid id if it is not valid
                id.clearContent();
                id.addCDATA(new CmsUUID().toString());
            }
            addBookmarkForElement(id, locale, entry, entryPath, entryDef);
            CmsUUID entryId = new CmsUUID(id.getTextTrim());

            // name
            Element name = entry.element(XmlNode.Name.name());
            addBookmarkForElement(name, locale, entry, entryPath, entryDef);
            String entryName = name.getTextTrim();

            // title
            Element title = entry.element(XmlNode.Title.name());
            addBookmarkForElement(title, locale, entry, entryPath, entryDef);
            String titleValue = title.getTextTrim();

            // vfs file
            Element uri = entry.element(XmlNode.VfsFile.name());
            addBookmarkForElement(uri, locale, entry, entryPath, entryDef);
            Element linkUri = uri.element(CmsXmlPage.NODE_LINK);
            CmsUUID uriId = null;
            if (linkUri == null) {
                // this can happen when adding the entry node to the xml content
                // it is not dangerous since the link has to be set before saving 
            } else {
                uriId = new CmsLink(linkUri).getStructureId();
            }

            // properties
            Map<String, String> propertiesMap = CmsXmlContentPropertyHelper.readProperties(
                this,
                locale,
                entry,
                entryPath,
                entryDef);

            String path = parentUri + entryName;
            if (!path.endsWith("/")) {
                path += "/";
            }
            List<CmsSitemapEntry> subEntries = readSiteEntries(entry, entryPath, entryDef, locale, path);

            entries.add(new CmsSitemapEntry(
                entryId,
                path,
                uriId,
                entryName,
                titleValue,
                propertiesMap,
                subEntries,
                null));
        }
        return entries;
    }

    /**
     * @see org.opencms.xml.content.CmsXmlContent#setFile(org.opencms.file.CmsFile)
     */
    @Override
    protected void setFile(CmsFile file) {

        // just for visibility from the factory
        super.setFile(file);
    }

    /**
     * Locates a DOM element for the given sitemap path.<p>
     * 
     * @param cms the CMS context 
     * @param sitePath the site path
     * 
     * @return the corresponding DOM element or <code>null</code> if not found
     */
    private Element getElement(CmsObject cms, String sitePath) {

        // check entry point
        String entryPoint = getValue(XmlNode.EntryPoint.name(), cms.getRequestContext().getLocale()).getStringValue(cms);
        if (!sitePath.startsWith(entryPoint)) {
            return null;
        }
        Element parent = getLocaleNode(cms.getRequestContext().getLocale());
        String[] pathEntries = CmsStringUtil.splitAsArray(sitePath.substring(entryPoint.length()), '/');

        // handle special case of root node in root sitemap
        if (parent.elements(XmlNode.SiteEntry.name()).size() == 1) {
            Element element = parent.element(XmlNode.SiteEntry.name());
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(element.elementText(XmlNode.Name.name()))) {
                parent = element;
            }
        }

        // tree level iteration
        for (String name : pathEntries) {
            boolean found = false;
            // level entry iteration
            for (Element element : CmsCollectionsGenericWrapper.<Element> list(parent.elements(XmlNode.SiteEntry.name()))) {
                if (element.elementText(XmlNode.Name.name()).equals(name)) {
                    found = true;
                    parent = element;
                    break;
                }
            }
            if (!found) {
                return null;
            }
        }
        return parent;
    }

    /**
     * Returns the list of all paths in the element tree, being the given element child of the given parent path.<p>
     * 
     * @param parentPath the parent path to start with
     * @param entryElement the element to start with
     * 
     * @return the list of paths
     */
    private List<String> getSubTreePaths(String parentPath, Element entryElement) {

        String srcPath = parentPath;
        List<String> result = new ArrayList<String>();
        if (srcPath.endsWith("/")) {
            srcPath = srcPath.substring(0, srcPath.length() - 1);
        }
        Map<String, List<Element>> subEntries = new HashMap<String, List<Element>>();
        subEntries.put(srcPath, Collections.singletonList(entryElement));
        while (!subEntries.isEmpty()) {
            String path = subEntries.keySet().iterator().next();
            List<Element> entries = subEntries.remove(path);
            for (Element entry : entries) {
                String entryPath = path + "/" + entry.elementText(XmlNode.Name.name());
                if (entryPath.endsWith("/")) {
                    entryPath = entryPath.substring(0, entryPath.length() - 1);
                }
                result.add(entryPath + "/");
                // continue recursion
                List<Element> children = CmsCollectionsGenericWrapper.<Element> list(entry.elements(XmlNode.SiteEntry.name()));
                if (!children.isEmpty()) {
                    subEntries.put(entryPath, children);
                }
            }
        }
        return result;
    }
}