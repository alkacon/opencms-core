/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeXmlContent.java,v $
 * Date   : $Date: 2009/10/20 07:38:54 $
 * Version: $Revision: 1.32.2.2 $
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

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsXmlContentLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.security.CmsPermissionSet;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "xmlcontent".<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.32.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsResourceTypeXmlContent extends A_CmsResourceTypeLinkParseable {

    /** Configuration key for the (optional) schema. */
    public static final String CONFIGURATION_SCHEMA = "schema";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeXmlContent.class);

    /** The (optional) schema of this resource. */
    private String m_schema;

    /**
     * Returns <code>true</code> in case the given resource is an XML content.<p>
     * 
     * Internally this checks if the content loader for the given resource is 
     * identical to the XML content loader.<p>
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
                try {
                    result = OpenCms.getResourceManager().getLoader(resource) instanceof CmsXmlContentLoader;
                } catch (CmsLoaderException e) {
                    // result will be false
                }
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
        List<CmsProperty> properties) throws CmsException {

        boolean hasModelUri = false;
        CmsXmlContent newContent = null;
        if ((m_schema != null) && ((content == null) || (content.length == 0))) {
            // unmarshal the content definition for the new resource
            CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, m_schema);

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
            resource = newContent.getContentDefinition().getContentHandler().prepareForWrite(cms, newContent, file);
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
    public Map<String, String> getConfiguration() {

        Map<String, String> result = new TreeMap<String, String>();
        if (m_schema != null) {
            result.put(CONFIGURATION_SCHEMA, m_schema);
        }
        Map<String, String> additional = super.getConfiguration();
        if (additional != null) {
            result.putAll(additional);
        }
        return result;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CmsXmlContentLoader.RESOURCE_LOADER_ID;
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
        resource = xmlContent.getContentDefinition().getContentHandler().prepareForWrite(cms, xmlContent, resource);

        // now write the file
        return super.writeFile(cms, securityManager, resource);
    }

    /**
     * Returns the configured xsd schema uri.<p>
     * 
     * @return the configured xsd schema uri, or <code>null</code> if not set
     */
    protected String getSchema() {

        return m_schema;
    }
}