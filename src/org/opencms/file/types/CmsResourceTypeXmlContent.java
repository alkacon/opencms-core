/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeXmlContent.java,v $
 * Date   : $Date: 2007/01/03 10:05:22 $
 * Version: $Revision: 1.23 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsXmlContentLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.validation.I_CmsXmlDocumentLinkValidatable;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "xmlcontent".<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.23 $ 
 * 
 * @since 6.0.0 
 */
public class CmsResourceTypeXmlContent extends A_CmsResourceType implements I_CmsXmlDocumentLinkValidatable {

    /** Configuration key for the (optional) schema. */
    public static final String CONFIGURATION_SCHEMA = "schema";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeXmlContent.class);

    /** The (optional) schema of this resource. */
    private String m_schema;

    /**
     * @see org.opencms.file.types.A_CmsResourceType#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        super.addConfigurationParameter(paramName, paramValue);
        if (CONFIGURATION_SCHEMA.equalsIgnoreCase(paramName)) {
            m_schema = paramValue.trim();
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, java.lang.String, byte[], java.util.List)
     */
    public CmsResource createResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        byte[] content,
        List properties) throws CmsException {

        if ((m_schema != null) && ((content == null) || (content.length == 0))) {
            // unmarshal the content definition for the new resource
            CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, m_schema);

            // read the default locale for the new resource
            Locale locale = (Locale)OpenCms.getLocaleManager().getDefaultLocales(
                cms,
                CmsResource.getParentFolder(resourcename)).get(0);

            // create the new content from the content defintion
            CmsXmlContent newContent = CmsXmlContentFactory.createDocument(
                cms,
                locale,
                OpenCms.getSystemInfo().getDefaultEncoding(),
                contentDefinition);
            // get the bytes from the created content
            content = newContent.marshal();
        }

        // now create the resource using the super class
        return super.createResource(cms, securityManager, resourcename, content, properties);
    }

    /**
     * @see org.opencms.validation.I_CmsXmlDocumentLinkValidatable#findLinks(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public List findLinks(CmsObject cms, CmsResource resource) {

        List links = new ArrayList();
        CmsFile file = null;
        CmsXmlContent xmlcontent = null;
        List locales = null;
        List elementNames = null;
        CmsLinkTable linkTable = null;
        CmsLink link = null;

        try {
            file = cms.readFile(
                cms.getRequestContext().removeSiteRoot(resource.getRootPath()),
                CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(org.opencms.db.Messages.get().getBundle().key(
                    org.opencms.db.Messages.ERR_READ_RESOURCE_1,
                    cms.getSitePath(resource)), e);
            }

            return Collections.EMPTY_LIST;
        }

        try {
            xmlcontent = CmsXmlContentFactory.unmarshal(cms, file);
            locales = xmlcontent.getLocales();

            // iterate over all languages
            Iterator i = locales.iterator();
            while (i.hasNext()) {
                Locale locale = (Locale)i.next();
                elementNames = xmlcontent.getValues(locale);

                // iterate over all body elements per language
                Iterator j = elementNames.iterator();
                while (j.hasNext()) {
                    I_CmsXmlContentValue value = (I_CmsXmlContentValue)j.next();

                    if (value instanceof CmsXmlHtmlValue) {
                        CmsXmlHtmlValue htmlValue = (CmsXmlHtmlValue)value;
                        linkTable = htmlValue.getLinkTable();

                        // iterate over all links inside a body element
                        Iterator k = linkTable.iterator();
                        while (k.hasNext()) {
                            link = (CmsLink)k.next();

                            // external links are ommitted
                            if (link.isInternal()) {
                                links.add(link.getTarget());
                            }
                        }
                    } else if (value instanceof CmsXmlVfsFileValue) {
                        CmsXmlVfsFileValue refValue = (CmsXmlVfsFileValue)value;

                        linkTable = refValue.getLinkTable();

                        // iterate over all links inside a body element
                        Iterator k = linkTable.iterator();
                        while (k.hasNext()) {
                            link = (CmsLink)k.next();

                            // external links are ommitted
                            if (link.isInternal()) {
                                links.add(link.getTarget());
                            }
                        }
                    }
                }
            }
        } catch (CmsXmlException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.ERR_PROCESS_HTML_CONTENT_1, cms.getSitePath(resource)),
                    e);
            }

            return Collections.EMPTY_LIST;
        }

        return links;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getCachePropertyDefault()
     */
    public String getCachePropertyDefault() {

        return "element;locale;";
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getConfiguration()
     */
    public Map getConfiguration() {

        Map result = new TreeMap();
        if (m_schema != null) {
            result.put(CONFIGURATION_SCHEMA, m_schema);
        }
        Map additional = super.getConfiguration();
        if (additional != null) {
            result.putAll(additional);
        }
        return result;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        return CmsXmlContentLoader.RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#isDirectEditable()
     */
    public boolean isDirectEditable() {

        return true;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writeFile(org.opencms.file.CmsObject, CmsSecurityManager, CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource) throws CmsException {

        // check if the user has write access and if resource is locked
        // done here so that all the XML operations are not performed if permissions not granted
        securityManager.checkPermissions(
            cms.getRequestContext(),
            resource,
            CmsPermissionSet.ACCESS_WRITE,
            true,
            CmsResourceFilter.ALL);
        // read the xml content, use the encoding set in the property       
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, resource, false);
        // call the content handler for post-processing
        resource = xmlContent.getContentDefinition().getContentHandler().prepareForWrite(cms, xmlContent, resource);
        // now write the file
        return super.writeFile(cms, securityManager, resource);
    }

}