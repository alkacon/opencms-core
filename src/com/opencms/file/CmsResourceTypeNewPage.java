/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypeNewPage.java,v $
 * Date   : $Date: 2003/11/14 10:09:09 $
 * Version: $Revision: 1.8 $
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

package com.opencms.file;

import org.opencms.loader.CmsPageLoader;
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.linkmanagement.CmsPageLinks;

import java.util.Hashtable;
import java.util.Map;

/**
 * Describes the resource type "newpage".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.8 $
 * @since 5.1
 */
public class CmsResourceTypeNewPage extends A_CmsResourceType {

    /** The type id of this resource */
    public static final int C_RESOURCE_TYPE_ID = 9;
    
    /** The name of this resource */
    public static final String C_RESOURCE_TYPE_NAME = "newpage";
    
    /**
     * @see com.opencms.file.I_CmsResourceType#getResourceType()
     */
    public int getResourceType() {
        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see com.opencms.file.A_CmsResourceType#getResourceTypeName()
     */
    public String getResourceTypeName() {
        return C_RESOURCE_TYPE_NAME;
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {
        return CmsPageLoader.C_RESOURCE_LOADER_ID;
    }    
             
    /**
     * @see com.opencms.file.I_CmsResourceType#createResource(com.opencms.file.CmsObject, java.lang.String, java.util.Map, byte[], java.lang.Object)
     */
    public CmsResource createResource(CmsObject cms, String resourcename, Map properties, byte[] contents, Object parameter) throws CmsException {
        if (contents == null) {
            contents = (CmsResourceTypeNewPage.getDefaultBodyStart() + new String(contents) + CmsResourceTypeNewPage.getDefaultBodyEnd()).getBytes();
        }

        CmsFile file = cms.doCreateFile(resourcename, contents, C_RESOURCE_TYPE_NAME, properties);
        cms.doLockResource(resourcename, false);

        // linkmanagement: create the links of the new page (for the case that the content was not empty
        if (contents.length > 1) {
            CmsPageLinks linkObject = cms.getPageLinks(resourcename);
            cms.createLinkEntrys(linkObject.getResourceId(), linkObject.getLinkTargets());
        }
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
     * Returns the default body start string for a new XML template.<p>
     * 
     * @return the default body start string for a new XML template 
     */
    public static String getDefaultBodyStart() {
        return "<?xml version=\"1.0\" encoding=\"" + OpenCms.getDefaultEncoding() + "\"?>\n<XMLTEMPLATE>\n<TEMPLATE>\n<![CDATA[\n";
    }

    /**
     * Returns the default body end string for a new XML template.<p>
     * 
     * @return the default body end string for a new XML template 
     */
    public static String getDefaultBodyEnd() {
        return "]]></TEMPLATE>\n</XMLTEMPLATE>";
    }
}