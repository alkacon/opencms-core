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

package org.opencms.file.types;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsXmlContentLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsPermissionSet;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.I_CmsPreEditorActionDefinition;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlVarLinkValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "xmlcontent".<p>
 *
 * @since 6.0.0
 */
public class CmsResourceTypeXmlContent extends A_CmsResourceTypeLinkParseable {

    /** The name for the choose model file form action. */
    public static final String DIALOG_CHOOSEMODEL = "choosemodel";

    /** Configuration key for the (optional) schema. */
    public static final String CONFIGURATION_SCHEMA = "schema";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeXmlContent.class);

    /** The (optional) schema of this resource. */
    private String m_schema;

    /**
     * Returns the possible model files for the new resource.<p>
     *
     * @param cms the current users context to work with
     * @param currentFolder the folder
     * @param newResourceTypeName the resource type name for the new resource to create
     * @return the possible model files for the new resource
     */
    public static List<CmsResource> getModelFiles(CmsObject cms, String currentFolder, String newResourceTypeName) {

        try {

            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(newResourceTypeName);
            I_CmsPreEditorActionDefinition preEditorAction = OpenCms.getWorkplaceManager().getPreEditorConditionDefinition(
                resType);
            // get the global master folder if configured
            String masterFolder = preEditorAction.getConfiguration().getString(
                CmsDefaultXmlContentHandler.APPINFO_MODELFOLDER,
                null);
            // get the schema for the resource type to create
            String schema = resType.getConfiguration().get(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA);
            CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, schema);
            // get the content handler for the resource type to create
            I_CmsXmlContentHandler handler = contentDefinition.getContentHandler();
            String individualModelFolder = handler.getModelFolder();
            if (CmsStringUtil.isNotEmpty(individualModelFolder)) {
                masterFolder = individualModelFolder;
            }

            if (CmsStringUtil.isNotEmpty(masterFolder)) {
                // store the original URI
                String uri = cms.getRequestContext().getUri();
                try {
                    // set URI to current folder
                    cms.getRequestContext().setUri(currentFolder);
                    CmsMacroResolver resolver = CmsMacroResolver.newInstance().setCmsObject(cms);
                    // resolve eventual macros
                    masterFolder = resolver.resolveMacros(masterFolder);
                } finally {
                    // switch back to stored URI
                    cms.getRequestContext().setUri(uri);
                }

                if (CmsStringUtil.isNotEmpty(masterFolder) && cms.existsResource(masterFolder)) {
                    // folder for master files exists, get all files of the same resource type
                    CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(
                        resType.getTypeId());
                    return cms.readResources(masterFolder, filter, false);
                }
            }
        } catch (Throwable t) {
            // error determining resource type, should never happen
        }
        return Collections.emptyList();
    }

    /**
     * Returns <code>true</code> in case the given resource is an XML content.<p>
     *
     * @param resource the resource to check
     *
     * @return <code>true</code> in case the given resource is an XML content
     *
     * @since 7.0.2
     */
    public static boolean isXmlContent(CmsResource resource) {

        boolean result = false;
        if (resource != null) {
            // avoid array index out of bound exception:
            if (!resource.isFolder()) {
                result = OpenCms.getResourceManager().getResourceType(resource) instanceof CmsResourceTypeXmlContent;
            }
        }
        return result;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    @Override
    public void addConfigurationParameter(String paramName, String paramValue) {

        super.addConfigurationParameter(paramName, paramValue);
        if (CONFIGURATION_SCHEMA.equalsIgnoreCase(paramName)) {
            m_schema = paramValue.trim();
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, java.lang.String, byte[], java.util.List)
     */
    @Override
    public CmsResource createResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        byte[] content,
        List<CmsProperty> properties)
    throws CmsException {

        boolean hasModelUri = false;
        CmsXmlContent newContent = null;
        if ((m_schema != null) && ((content == null) || (content.length == 0))) {
            // unmarshal the content definition for the new resource
            CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, m_schema);

            // read the default locale for the new resource
            Locale locale = getLocaleForNewContent(cms, securityManager, resourcename, properties);
            String modelUri = (String)cms.getRequestContext().getAttribute(CmsRequestContext.ATTRIBUTE_MODEL);

            // must set URI of OpenCms user context to parent folder of created resource,
            // in order to allow reading of properties for default values
            CmsObject newCms = OpenCms.initCmsObject(cms);
            newCms.getRequestContext().setUri(CmsResource.getParentFolder(resourcename));
            if (modelUri != null) {
                // create the new content from the model file
                newContent = CmsXmlContentFactory.createDocument(newCms, locale, modelUri);
                hasModelUri = true;
            } else {
                // create the new content from the content definition
                newContent = CmsXmlContentFactory.createDocument(
                    newCms,
                    locale,
                    OpenCms.getSystemInfo().getDefaultEncoding(),
                    contentDefinition);
            }
            // get the bytes from the created content
            content = newContent.marshal();
        }

        // now create the resource using the super class
        CmsResource resource = super.createResource(cms, securityManager, resourcename, content, properties);

        // a model file was used, call the content handler for post-processing
        if (hasModelUri) {
            CmsFile file = cms.readFile(resource);
            newContent = CmsXmlContentFactory.unmarshal(cms, file);
            newContent.setAutoCorrectionEnabled(true);
            resource = newContent.getHandler().prepareForWrite(cms, newContent, file);
        }

        return resource;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getCachePropertyDefault()
     */
    @Override
    public String getCachePropertyDefault() {

        return "element;locale;";
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getConfiguration()
     */
    @Override
    public CmsParameterConfiguration getConfiguration() {

        CmsParameterConfiguration result = new CmsParameterConfiguration();
        CmsParameterConfiguration additional = super.getConfiguration();
        if (additional != null) {
            result.putAll(additional);
        }
        if (m_schema != null) {
            result.put(CONFIGURATION_SCHEMA, m_schema);
        }
        return result;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getFormattersForResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public CmsFormatterConfiguration getFormattersForResource(CmsObject cms, CmsResource resource) {

        CmsFormatterConfiguration result = null;
        CmsXmlContentDefinition cd = null;
        try {
            cd = CmsXmlContentDefinition.getContentDefinitionForResource(cms, resource);
            result = cd.getContentHandler().getFormatterConfiguration(cms, resource);
        } catch (CmsException e) {
            // no content definition found, use the preview formatter
        }
        if (result == null) {
            LOG.warn(
                Messages.get().getBundle().key(
                    Messages.LOG_WARN_NO_FORMATTERS_DEFINED_1,
                    cd == null ? resource.getRootPath() : cd.getSchemaLocation()));
            result = CmsFormatterConfiguration.EMPTY_CONFIGURATION;
        }
        return result;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getGalleryPreviewProvider()
     */
    @Override
    public String getGalleryPreviewProvider() {

        if (m_galleryPreviewProvider == null) {
            m_galleryPreviewProvider = getConfiguration().getString(
                CONFIGURATION_GALLERY_PREVIEW_PROVIDER,
                DEFAULT_GALLERY_PREVIEW_PROVIDER);
        }
        return m_galleryPreviewProvider;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CmsXmlContentLoader.RESOURCE_LOADER_ID;
    }

    /**
     * Returns the configured xsd schema uri.<p>
     *
     * @return the configured xsd schema uri, or <code>null</code> if not set
     */
    public String getSchema() {

        return m_schema;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initialize(org.opencms.file.CmsObject)
     */
    @Override
    public void initialize(CmsObject cms) {

        super.initialize(cms);
        if (m_schema != null) {
            // unmarshal the XML schema, this is required to update the resource bundle cache
            try {
                if (cms.existsResource(m_schema)) {
                    CmsXmlContentDefinition.unmarshal(cms, m_schema);
                } else {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_WARN_SCHEMA_RESOURCE_DOES_NOT_EXIST_2,
                            m_schema,
                            getTypeName()));
                }
            } catch (Throwable e) {
                // unable to unmarshal the XML schema configured
                LOG.error(Messages.get().getBundle().key(Messages.ERR_BAD_XML_SCHEMA_2, m_schema, getTypeName()), e);
            }
        }
    }

    /**
     * @see org.opencms.relations.I_CmsLinkParseable#parseLinks(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    public List<CmsLink> parseLinks(CmsObject cms, CmsFile file) {

        if (file.getLength() == 0) {
            return Collections.emptyList();
        }
        CmsXmlContent xmlContent;
        long requestTime = cms.getRequestContext().getRequestTime();
        try {
            // prevent the check rules to remove the broken links
            cms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
            xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    org.opencms.db.Messages.get().getBundle().key(
                        org.opencms.db.Messages.ERR_READ_RESOURCE_1,
                        cms.getSitePath(file)),
                    e);
            }
            return Collections.emptyList();
        } finally {
            cms.getRequestContext().setRequestTime(requestTime);
        }
        // using linked set to keep the link order
        Set<CmsLink> links = new LinkedHashSet<CmsLink>();

        // add XSD link
        CmsLink xsdLink = getXsdLink(cms, xmlContent);
        if (xsdLink != null) {
            links.add(xsdLink);
        }

        // iterate over all languages
        List<Locale> locales = xmlContent.getLocales();
        Iterator<Locale> i = locales.iterator();
        while (i.hasNext()) {
            Locale locale = i.next();
            List<I_CmsXmlContentValue> values = xmlContent.getValues(locale);

            // iterate over all body elements per language
            Iterator<I_CmsXmlContentValue> j = values.iterator();
            while (j.hasNext()) {
                I_CmsXmlContentValue value = j.next();
                if (value instanceof CmsXmlHtmlValue) {
                    CmsXmlHtmlValue htmlValue = (CmsXmlHtmlValue)value;
                    CmsLinkTable linkTable = htmlValue.getLinkTable();

                    // iterate over all links inside a body element
                    Iterator<CmsLink> k = linkTable.iterator();
                    while (k.hasNext()) {
                        CmsLink link = k.next();

                        // external links are omitted
                        if (link.isInternal()) {
                            link.checkConsistency(cms);
                            links.add(link);
                        }
                    }
                } else if (value instanceof CmsXmlVfsFileValue) {
                    CmsXmlVfsFileValue refValue = (CmsXmlVfsFileValue)value;
                    CmsLink link = refValue.getLink(cms);
                    if (link != null) {
                        links.add(link);
                    }
                } else if (value instanceof CmsXmlVarLinkValue) {
                    CmsXmlVarLinkValue refValue = (CmsXmlVarLinkValue)value;
                    CmsLink link = refValue.getLink(cms);
                    if ((link != null) && link.isInternal()) {
                        links.add(link);
                    }
                }
            }
        }
        return new ArrayList<CmsLink>(links);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writeFile(org.opencms.file.CmsObject, CmsSecurityManager, CmsFile)
     */
    @Override
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource) throws CmsException {

        // check if the user has write access and if resource is locked
        // done here so that all the XML operations are not performed if permissions not granted
        securityManager.checkPermissions(
            cms.getRequestContext(),
            resource,
            CmsPermissionSet.ACCESS_WRITE,
            true,
            CmsResourceFilter.ALL);
        // read the XML content, use the encoding set in the property
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, resource, false);
        // call the content handler for post-processing
        resource = xmlContent.getHandler().prepareForWrite(cms, xmlContent, resource);

        // now write the file
        return super.writeFile(cms, securityManager, resource);
    }

    /**
     * Gets the locale which should be used for creating an empty content.<p>
     *
     * @param cms the current CMS context
     * @param securityManager the security manager
     * @param resourcename the name of the resource to create
     * @param properties the properties for the resource to create
     *
     * @return the locale to use
     */
    protected Locale getLocaleForNewContent(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        List<CmsProperty> properties) {

        Locale locale = (Locale)(cms.getRequestContext().getAttribute(CmsRequestContext.ATTRIBUTE_NEW_RESOURCE_LOCALE));
        if (locale != null) {
            return locale;
        }
        List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales(
            cms,
            CmsResource.getParentFolder(resourcename));
        return locales.get(0);
    }

    /**
     * Creates a new link object for the schema definition.<p>
     *
     * @param cms the current CMS context
     * @param xmlContent the xml content to crete the link for
     *
     * @return the generated link
     */
    protected CmsLink getXsdLink(CmsObject cms, CmsXmlContent xmlContent) {

        String schema = xmlContent.getContentDefinition().getSchemaLocation();
        if (schema.startsWith(CmsXmlEntityResolver.OPENCMS_SCHEME)) {
            if (CmsXmlEntityResolver.isInternalId(schema)) {
                return null;
            }
            schema = schema.substring(CmsXmlEntityResolver.OPENCMS_SCHEME.length() - 1);
        } else if (CmsXmlEntityResolver.isCachedSystemId(schema)) {
            // schema may not exist as a VFS file because it has just been cached (some test cases do this)
            return null;
        }
        try {
            CmsResource schemaRes = cms.readResource(cms.getRequestContext().removeSiteRoot(schema));
            CmsLink xsdLink = new CmsLink(
                null,
                CmsRelationType.XSD,
                schemaRes.getStructureId(),
                schemaRes.getRootPath(),
                true);
            return xsdLink;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
