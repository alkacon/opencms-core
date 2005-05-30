/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeXmlPage.java,v $
 * Date   : $Date: 2005/05/30 15:20:41 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsXmlPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.validation.I_CmsHtmlLinkValidatable;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "xmlpage".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.15 $
 * @since 5.1
 */
public class CmsResourceTypeXmlPage extends A_CmsResourceType implements I_CmsHtmlLinkValidatable {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTypeXmlPage.class);

    /** The type id of this resource type. */
    private static final int C_RESOURCE_TYPE_ID = 6;

    /** The name of this resource type. */
    private static final String C_RESOURCE_TYPE_NAME = "xmlpage";

    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The static type id of this resource type. */
    private static int m_staticTypeId;
    
    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public CmsResourceTypeXmlPage() {
        
        super();
        m_typeId = C_RESOURCE_TYPE_ID;
        m_typeName = C_RESOURCE_TYPE_NAME;
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
        
        return C_RESOURCE_TYPE_NAME;
    }
    
    /**
     * @see org.opencms.validation.I_CmsHtmlLinkValidatable#findLinks(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public List findLinks(CmsObject cms, CmsResource resource) {

        List links = new ArrayList();
        CmsFile file = null;
        CmsXmlPage xmlPage = null;
        List locales = null;
        List elementNames = null;
        String elementName = null;
        CmsLinkTable linkTable = null;
        CmsLink link = null;

        try {
            file = cms.readFile(cms.getRequestContext().removeSiteRoot(resource.getRootPath()), CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(org.opencms.db.Messages.get().container(
                    org.opencms.db.Messages.ERR_READ_RESOURCE_1,
                    cms.getSitePath(resource)), e);
            }

            return Collections.EMPTY_LIST;
        }

        try {
            xmlPage = CmsXmlPageFactory.unmarshal(cms, file);
            locales = xmlPage.getLocales();

            // iterate over all languages
            Iterator i = locales.iterator();
            while (i.hasNext()) {
                Locale locale = (Locale)i.next();
                elementNames = xmlPage.getNames(locale);

                // iterate over all body elements per language
                Iterator j = elementNames.iterator();
                while (j.hasNext()) {
                    elementName = (String)j.next();
                    linkTable = xmlPage.getLinkTable(elementName, locale);

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
        } catch (CmsXmlException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().container(
                    Messages.ERR_PROCESS_HTML_CONTENT_1,
                    cms.getSitePath(resource)), e);
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
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        return CmsXmlPageLoader.C_RESOURCE_LOADER_ID;
    }
    
    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String)
     */
    public void initConfiguration(String name, String id) throws CmsConfigurationException {
        
        if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) && m_staticFrozen) {
            // configuration already frozen
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_CONFIG_FROZEN_3,
                this.getClass().getName(),
                getStaticTypeName(),
                new Integer(getStaticTypeId())));
        }
        
        if (!C_RESOURCE_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_RESTYPE_CONFIG_NAME_3,
                this.getClass().getName(),
                C_RESOURCE_TYPE_NAME,
                name));
        }
        
        // freeze the configuration
        m_staticFrozen = true;
        
        super.initConfiguration(C_RESOURCE_TYPE_NAME, id);
        // set static members with values from the configuration        
        m_staticTypeId = m_typeId;
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
        securityManager.checkPermissions(cms.getRequestContext(), resource, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL);

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
        // xml is valid if no exception occured
        return super.writeFile(cms, securityManager, resource);
    }
    
}