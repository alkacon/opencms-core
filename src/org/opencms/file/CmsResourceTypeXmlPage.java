/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/Attic/CmsResourceTypeXmlPage.java,v $
 * Date   : $Date: 2004/04/29 10:21:05 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file;

import org.opencms.loader.CmsXmlPageLoader;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.page.CmsXmlPage;
import org.opencms.page.CmsXmlPageException;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.validation.I_CmsHtmlLinkValidatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Describes the resource type "xmlpage".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * @since 5.1
 */
public class CmsResourceTypeXmlPage extends A_CmsResourceType implements I_CmsHtmlLinkValidatable {

    /** The type id of this resource */
    public static final int C_RESOURCE_TYPE_ID = 10;
    
    /** The name of this resource */
    public static final String C_RESOURCE_TYPE_NAME = "xmlpage";
             
    /**
     * @see org.opencms.file.I_CmsResourceType#createResource(org.opencms.file.CmsObject, java.lang.String, java.util.Map, byte[], java.lang.Object)
     */
    public CmsResource createResource(CmsObject cms, String resourcename, Map properties, byte[] contents, Object parameter) throws CmsException {
        CmsFile file = cms.doCreateFile(resourcename, contents, C_RESOURCE_TYPE_NAME, properties);
        cms.doLockResource(resourcename, CmsLock.C_MODE_COMMON);

        contents = null;
        return file;
    }  
    
    /**
     * Creates a resource for the specified template.<p>
     * 
     * @param cms the cms context
     * @param resourcename the name of the resource to create
     * @param properties properties for the new resource
     * @param contents content for the new resource
     * @param masterTemplate template for the new resource
     * @return the created resource 
     * @throws CmsException if something goes wrong
     */
    public CmsResource createResourceForTemplate(CmsObject cms, String resourcename, Hashtable properties, byte[] contents, String masterTemplate) throws CmsException {        
        properties.put(I_CmsConstants.C_PROPERTY_TEMPLATE, masterTemplate);
        CmsFile resource = (CmsFile)createResource(cms, resourcename, properties, contents, null);                
        return resource;
    }
    
    /**
     * @see org.opencms.validation.I_CmsHtmlLinkValidatable#findLinks(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public List findLinks(CmsObject cms, CmsResource resource) {
        List links = (List) new ArrayList();
        CmsFile file = null;
        CmsXmlPage xmlPage = null;
        List locales = null;
        List elementNames = null;
        String elementName = null;
        CmsLinkTable linkTable = null;
        String linkName = null;
        CmsLink link = null;

        try {
            file = cms.readFile(cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error reading file content of " + resource.getRootPath(), e);
            }

            return Collections.EMPTY_LIST;
        }

        try {
            xmlPage = CmsXmlPage.read(cms, file);
            locales = xmlPage.getLocales();

            // iterate over all languages
            Iterator i = locales.iterator();
            while (i.hasNext()) {
                Locale locale = (Locale)i.next();
                elementNames = xmlPage.getNames(locale);

                // iterate over all body elements per language
                Iterator j = elementNames.iterator();
                while (j.hasNext()) {
                    elementName = (String) j.next();
                    linkTable = xmlPage.getLinkTable(elementName, locale);

                    // iterate over all links inside a body element
                    Iterator k = linkTable.iterator();
                    while (k.hasNext()) {
                        linkName = (String) k.next();
                        link = linkTable.getLink(linkName);

                        // external links are ommitted
                        if (link.isInternal()) {
                            links.add(link.getTarget());
                        }
                    }
                }
            }
        } catch (CmsXmlPageException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error processing HTML content of " + resource.getRootPath(), e);
            }

            return Collections.EMPTY_LIST;
        }

        return links;
    }
    
    /**
     * @see org.opencms.file.I_CmsResourceType#getCachePropertyDefault()
     */
    public String getCachePropertyDefault() {
        return "element;locale;";
    }        

    /**
     * @see org.opencms.file.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {
        return CmsXmlPageLoader.C_RESOURCE_LOADER_ID;
    }    
    
    /**
     * @see org.opencms.file.I_CmsResourceType#getResourceType()
     */
    public int getResourceType() {
        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see org.opencms.file.A_CmsResourceType#getResourceTypeName()
     */
    public String getResourceTypeName() {
        return C_RESOURCE_TYPE_NAME;
    }
    
    /**
     * @see org.opencms.file.I_CmsResourceType#isDirectEditable()
     */
    public boolean isDirectEditable() {
        return true;
    }
    
    /**
     * @see org.opencms.file.I_CmsResourceType#writeFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    public void writeFile(CmsObject cms, CmsFile file) throws CmsException {     
        validateXmlStructure(cms, file);
        cms.doWriteFile(file);
    }
    
    /**
     * Validates the xml structure of a given xmlpage file.<p>
     * 
     * @param cms the current cms objecz
     * @param file the file to validate the xmlcontent of
     * @throws CmsException if validation has failed
     */
    private void validateXmlStructure(CmsObject cms, CmsFile file) throws CmsException {
        try {
            CmsXmlPage xmlpage= CmsXmlPage.read(cms, file);
            // validate the xml structure           
            xmlpage.validateXmlStructure();
        } catch (CmsXmlPageException e) {   
            // there was an error during validation, so throw an CmsException that it can
            // be displayed in an error dialog box
            throw new CmsException(e.getMessage() , CmsException.C_XML_CORRUPT_INTERNAL_STRUCTURE);
        }
    }
    
}