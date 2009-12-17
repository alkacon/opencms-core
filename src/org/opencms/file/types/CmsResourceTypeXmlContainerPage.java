/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeXmlContainerPage.java,v $
 * Date   : $Date: 2009/12/17 12:36:25 $
 * Version: $Revision: 1.4 $
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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsXmlContainerPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.security.CmsPermissionSet;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlVarLinkValue;
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
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 7.6 
 */
public class CmsResourceTypeXmlContainerPage extends CmsResourceTypeXmlContent {

    /** The configuration resource type id. */
    public static final int CONFIGURATION_TYPE_ID = 14;

    /** The sub container resource type id. */
    public static final int SUB_CONTAINER_TYPE_ID = 17;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeXmlContainerPage.class);

    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The type id of this resource type. */
    private static final int RESOURCE_TYPE_ID = 13;

    /** The name of this resource type. */
    private static final String RESOURCE_TYPE_NAME = "containerpage";

    /** Fixed schema for container pages. */
    private static final String SCHEMA = "/system/workplace/editors/ade/schemas/container_page.xsd";

    /**
     * Default constructor that sets the fixed schema for container pages.<p>
     */
    public CmsResourceTypeXmlContainerPage() {

        super();
        m_typeName = RESOURCE_TYPE_NAME;
        m_typeId = CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_ID;
        addConfigurationParameter(CONFIGURATION_SCHEMA, SCHEMA);
    }

    /**
     * Returns the static type id of this (default) resource type.<p>
     * 
     * @return the static type id of this (default) resource type
     */
    public static int getStaticTypeId() {

        return RESOURCE_TYPE_ID;
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
            result = (resource.getTypeId() == RESOURCE_TYPE_ID);
        }
        return result;
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
        List<CmsProperty> properties) throws CmsException {

        boolean hasModelUri = false;
        CmsXmlContainerPage newContent = null;
        if ((getSchema() != null) && ((content == null) || (content.length == 0))) {
            // unmarshal the content definition for the new resource
            CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, getSchema());

            // read the default locale for the new resource
            Locale locale = OpenCms.getLocaleManager().getDefaultLocales(cms, CmsResource.getParentFolder(resourcename)).get(
                0);

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
            resource = newContent.getContentDefinition().getContentHandler().prepareForWrite(
                cms,
                newContent,
                newContent.getFile());
        }

        return resource;
    }

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CmsXmlContainerPageLoader.RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    @Override
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) && m_staticFrozen) {
            // configuration already frozen
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_CONFIG_FROZEN_3,
                this.getClass().getName(),
                getStaticTypeName(),
                new Integer(getStaticTypeId())));
        }

        if (!RESOURCE_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_RESTYPE_CONFIG_NAME_3,
                this.getClass().getName(),
                RESOURCE_TYPE_NAME,
                name));
        }

        if (!id.equals("" + RESOURCE_TYPE_ID)) {
            // default resource type MUST have id equals RESOURCE_TYPE_ID
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_RESTYPE_CONFIG_ID_3,
                this.getClass().getName(),
                RESOURCE_TYPE_NAME,
                name));
        }

        // freeze the configuration
        m_staticFrozen = true;

        super.initConfiguration(RESOURCE_TYPE_NAME, "" + RESOURCE_TYPE_ID, className);
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
                LOG.error(org.opencms.db.Messages.get().getBundle().key(
                    org.opencms.db.Messages.ERR_READ_RESOURCE_1,
                    cms.getSitePath(file)), e);
            }
            return Collections.emptyList();
        } finally {
            cms.getRequestContext().setRequestTime(requestTime);
        }

        Set<CmsLink> links = new HashSet<CmsLink>();
        List<Locale> locales = xmlContent.getLocales();

        // iterate over all languages
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
        CmsXmlContainerPage xmlContent = CmsXmlContainerPageFactory.unmarshal(cms, resource, false);
        // call the content handler for post-processing
        resource = xmlContent.getContentDefinition().getContentHandler().prepareForWrite(cms, xmlContent, resource);

        // now write the file
        CmsFile file = securityManager.writeFile(cms.getRequestContext(), resource);
        I_CmsResourceType type = getResourceType(file);
        // update the relations after writing!!
        List<CmsLink> links = null;
        if (type instanceof I_CmsLinkParseable) {
            // if the new type is link parseable
            links = ((I_CmsLinkParseable)type).parseLinks(cms, file);
        }
        securityManager.updateRelationsForResource(cms.getRequestContext(), file, links);
        return file;
    }
}