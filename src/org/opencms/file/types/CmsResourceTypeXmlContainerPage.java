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

import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsXmlContainerPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.security.CmsPermissionSet;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "containerpage".<p>
 *
 * It is just a xml content with a fixed schema.<p>
 *
 * @since 7.6
 */
public class CmsResourceTypeXmlContainerPage extends CmsResourceTypeXmlContent {

    /** The configuration resource type name. */
    public static final String CONFIGURATION_TYPE_NAME = "sitemap_config";

    /** The group container resource type name. */
    public static final String GROUP_CONTAINER_TYPE_NAME = "groupcontainer";

    /** The inherit configuration resource type name. */
    public static final String INHERIT_CONTAINER_CONFIG_TYPE_NAME = "inheritance_config";

    /** The resource type name for inherited container references.  */
    public static final String INHERIT_CONTAINER_TYPE_NAME = "inheritance_group";

    /** The model group resource type name. */
    public static final String MODEL_GROUP_TYPE_NAME = "modelgroup";

    /** The name of this resource type. */
    public static final String RESOURCE_TYPE_NAME = "containerpage";

    /** A variable containing the actual configured type id of container pages. */
    private static int containerPageTypeId;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeXmlContainerPage.class);

    /** Fixed schema for container pages. */
    private static final String SCHEMA = "/system/modules/org.opencms.ade.containerpage/schemas/container_page.xsd";

    /** The serial version id. */
    private static final long serialVersionUID = -6211941269510267155L;

    /**
     * Default constructor that sets the fixed schema for container pages.<p>
     */
    public CmsResourceTypeXmlContainerPage() {

        super();
        m_typeName = RESOURCE_TYPE_NAME;
        addConfigurationParameter(CONFIGURATION_SCHEMA, SCHEMA);
    }

    /**
     * Returns the container-page type id.<p>
     *
     * @return the container-page type id
     *
     * @throws CmsLoaderException if the type is not configured
     */
    @SuppressWarnings("deprecation")
    public static int getContainerPageTypeId() throws CmsLoaderException {

        if (containerPageTypeId == 0) {
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(getStaticTypeName());
            if (resType != null) {
                containerPageTypeId = resType.getTypeId();
            }
        }
        return containerPageTypeId;
    }

    /**
     * Returns the container-page type id, but returns -1 instead of throwing an exception when an error happens.<p>
     *
     * @return the container-page type id
     */
    public static int getContainerPageTypeIdSafely() {

        try {
            return getContainerPageTypeId();
        } catch (CmsLoaderException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            return -1;
        }
    }

    /**
     * Returns the static type name of this (default) resource type.<p>
     *
     * @return the static type name of this (default) resource type
     */
    public static String getStaticTypeName() {

        return RESOURCE_TYPE_NAME;
    }

    /**
     * Returns <code>true</code> in case the given resource is a container page.<p>
     *
     * Internally this checks if the type id for the given resource is
     * identical type id of the container page.<p>
     *
     * @param resource the resource to check
     *
     * @return <code>true</code> in case the given resource is a container page
     */
    public static boolean isContainerPage(CmsResource resource) {

        boolean result = false;
        if (resource != null) {
            result = (resource.getTypeId() == getContainerPageTypeIdSafely())
                || (OpenCms.getResourceManager().getResourceType(resource) instanceof CmsResourceTypeXmlContainerPage);
        }

        return result;

    }

    /**
     * Checks whether the given resource is a model reuse group.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     *
     * @return <code>true</code> in case the resource is a model reuse group
     */
    public static boolean isModelCopyGroup(CmsObject cms, CmsResource resource) {

        boolean result = false;
        if (isModelGroup(resource)) {
            try {
                CmsProperty tempElementsProp = cms.readPropertyObject(
                    resource,
                    CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                    false);
                if (!tempElementsProp.isNullProperty()
                    && CmsContainerElement.USE_AS_COPY_MODEL.equals(tempElementsProp.getValue())) {
                    result = true;
                }
            } catch (CmsException e) {
                LOG.warn(e.getMessage(), e);
            }

        }
        return result;
    }

    /**
     * Checks whether the given resource is a model group.<p>
     *
     * @param resource the resource
     *
     * @return <code>true</code> in case the resource is a model group
     */
    public static boolean isModelGroup(CmsResource resource) {

        return OpenCms.getResourceManager().getResourceType(resource).getTypeName().equals(MODEL_GROUP_TYPE_NAME);
    }

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#createResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, java.lang.String, byte[], java.util.List)
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
        CmsXmlContainerPage newContent = null;
        if ((getSchema() != null) && ((content == null) || (content.length == 0))) {
            // unmarshal the content definition for the new resource
            CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, getSchema());

            // read the default locale for the new resource
            Locale locale = OpenCms.getLocaleManager().getDefaultLocales(
                cms,
                CmsResource.getParentFolder(resourcename)).get(0);

            String modelUri = (String)cms.getRequestContext().getAttribute(CmsRequestContext.ATTRIBUTE_MODEL);

            // must set URI of OpenCms user context to parent folder of created resource,
            // in order to allow reading of properties for default values
            CmsObject newCms = OpenCms.initCmsObject(cms);
            newCms.getRequestContext().setUri(CmsResource.getParentFolder(resourcename));
            if (modelUri != null) {
                // create the new content from the model file
                newContent = CmsXmlContainerPageFactory.createDocument(newCms, locale, modelUri);
                hasModelUri = true;
            } else {
                // create the new content from the content definition
                newContent = CmsXmlContainerPageFactory.createDocument(
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
            newContent = CmsXmlContainerPageFactory.unmarshal(cms, resource);
            resource = newContent.getHandler().prepareForWrite(cms, newContent, newContent.getFile());
        }

        return resource;
    }

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CmsXmlContainerPageLoader.CONTAINER_PAGE_RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    @Override
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        if (!RESOURCE_TYPE_NAME.equals(name) && !MODEL_GROUP_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_INVALID_RESTYPE_CONFIG_NAME_3,
                    this.getClass().getName(),
                    RESOURCE_TYPE_NAME,
                    name));
        }
        super.initConfiguration(name, id, className);
    }

    /**
     * @see org.opencms.relations.I_CmsLinkParseable#parseLinks(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    @Override
    public List<CmsLink> parseLinks(CmsObject cms, CmsFile file) {

        if (file.getLength() == 0) {
            return Collections.emptyList();
        }
        CmsXmlContainerPage xmlContent;
        long requestTime = cms.getRequestContext().getRequestTime();
        try {
            // prevent the check rules to remove the broken links
            cms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
            xmlContent = CmsXmlContainerPageFactory.unmarshal(cms, file);
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

        Set<CmsLink> links = new HashSet<CmsLink>();

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
                if (!(value instanceof CmsXmlVfsFileValue)) {
                    // filter only relations relevant fields
                    // container pages do not have XmlHtml nor VarFiles
                    continue;
                }
                CmsXmlVfsFileValue refValue = (CmsXmlVfsFileValue)value;
                CmsLink link = refValue.getLink(cms);
                if (link != null) {
                    links.add(link);
                }
            }
        }
        return new ArrayList<CmsLink>(links);
    }

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#writeFile(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsFile)
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
        CmsXmlContainerPage xmlContent = CmsXmlContainerPageFactory.unmarshal(cms, resource, false, true);
        // call the content handler for post-processing
        resource = xmlContent.getHandler().prepareForWrite(cms, xmlContent, resource);

        // now write the file
        CmsFile file = securityManager.writeFile(cms.getRequestContext(), resource);
        I_CmsResourceType type = getResourceType(file);
        // update the relations after writing!!
        List<CmsLink> links = null;
        if (type instanceof I_CmsLinkParseable) { // this check is needed because of type change
            // if the new type is link parseable
            links = ((I_CmsLinkParseable)type).parseLinks(cms, file);
        }
        // this has to be always executed, even if not link parseable to remove old links
        securityManager.updateRelationsForResource(cms.getRequestContext(), file, links, true);
        return file;
    }
}