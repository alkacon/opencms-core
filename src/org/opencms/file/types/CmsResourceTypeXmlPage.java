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

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsXmlPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.security.CmsPermissionSet;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "xmlpage".<p>
 *
 * @since 6.0.0
 */
public class CmsResourceTypeXmlPage extends A_CmsResourceTypeLinkParseable {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeXmlPage.class);

    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The static type id of this resource type. */
    private static int m_staticTypeId;

    /** The type id of this resource type. */
    private static final int RESOURCE_TYPE_ID = 6;

    /** The name of this resource type. */
    private static final String RESOURCE_TYPE_NAME = "xmlpage";

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public CmsResourceTypeXmlPage() {

        super();
        m_typeId = RESOURCE_TYPE_ID;
        m_typeName = RESOURCE_TYPE_NAME;
    }

    /**
     * Returns the static type id of this (default) resource type.<p>
     *
     * @return the static type id of this (default) resource type
     */
    public static int getStaticTypeId() {

        return m_staticTypeId;
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
     * Returns <code>true</code> in case the given resource is an XML page.<p>
     *
     * Internally this checks if the type id for the given resource is
     * identical type id of the XML page.<p>
     *
     * @param resource the resource to check
     *
     * @return <code>true</code> in case the given resource is an XML page
     *
     * @since 7.0.2
     */
    public static boolean isXmlPage(CmsResource resource) {

        boolean result = false;
        if (resource != null) {
            result = resource.getTypeId() == m_staticTypeId;
        }
        return result;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#createResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, java.lang.String, byte[], java.util.List)
     */
    @Override
    public CmsResource createResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        byte[] content,
        List<CmsProperty> properties) throws CmsException {

        if (content == null) {
            try {
                CmsResource defaultBody = cms.readResource(
                    "/system/modules/org.opencms.workplace/default_bodies/default",
                    CmsResourceFilter.IGNORE_EXPIRATION);
                CmsFile defaultBodyFile = cms.readFile(defaultBody);
                content = defaultBodyFile.getContents();
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return super.createResource(cms, securityManager, resourcename, content, properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getCachePropertyDefault()
     */
    @Override
    public String getCachePropertyDefault() {

        return "element;locale;";
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CmsXmlPageLoader.RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    @Override
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) && m_staticFrozen) {
            // configuration already frozen
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_CONFIG_FROZEN_3,
                    this.getClass().getName(),
                    getStaticTypeName(),
                    new Integer(getStaticTypeId())));
        }

        if (!RESOURCE_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_INVALID_RESTYPE_CONFIG_NAME_3,
                    this.getClass().getName(),
                    RESOURCE_TYPE_NAME,
                    name));
        }

        // freeze the configuration
        m_staticFrozen = true;

        super.initConfiguration(RESOURCE_TYPE_NAME, id, className);
        // set static members with values from the configuration
        m_staticTypeId = m_typeId;
    }

    /**
     * @see org.opencms.relations.I_CmsLinkParseable#parseLinks(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    public List<CmsLink> parseLinks(CmsObject cms, CmsFile file) {

        // use a linked set to keep the link order
        Set<CmsLink> links = new LinkedHashSet<CmsLink>();
        try {
            CmsXmlPage xmlPage = CmsXmlPageFactory.unmarshal(cms, file);
            List<Locale> locales = xmlPage.getLocales();

            // iterate over all languages
            Iterator<Locale> i = locales.iterator();
            while (i.hasNext()) {
                Locale locale = i.next();
                List<String> elementNames = xmlPage.getNames(locale);

                // iterate over all body elements per language
                Iterator<String> j = elementNames.iterator();
                while (j.hasNext()) {
                    String elementName = j.next();
                    CmsLinkTable linkTable = xmlPage.getLinkTable(elementName, locale);

                    // iterate over all links inside a body element
                    Iterator<CmsLink> k = linkTable.iterator();
                    while (k.hasNext()) {
                        CmsLink link = k.next();
                        if (link.isInternal()) {
                            link.checkConsistency(cms);
                            links.add(link);
                        }
                    }
                }
            }
        } catch (CmsXmlException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.ERR_PROCESS_HTML_CONTENT_1, cms.getSitePath(file)),
                    e);
            }

            return Collections.emptyList();
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

        // empty file content is allowed
        if (resource.getLength() > 0) {
            // read the xml page, use the encoding set in the property
            CmsXmlPage xmlPage = CmsXmlPageFactory.unmarshal(cms, resource, false);
            // validate the xml structure before writing the file
            // an exception will be thrown if the structure is invalid
            xmlPage.validateXmlStructure(new CmsXmlEntityResolver(cms));
            // read the content-conversion property
            String contentConversion = CmsHtmlConverter.getConversionSettings(cms, resource);
            xmlPage.setConversion(contentConversion);
            // correct the HTML structure
            resource = xmlPage.correctXmlStructure(cms);
        }

        // xml is valid if no exception occurred
        return super.writeFile(cms, securityManager, resource);
    }
}