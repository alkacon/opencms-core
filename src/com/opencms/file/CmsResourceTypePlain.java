/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypePlain.java,v $
* Date   : $Date: 2004/01/22 14:12:02 $
* Version: $Revision: 1.40 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file;

import org.opencms.loader.CmsDumpLoader;
import org.opencms.lock.CmsLock;

import com.opencms.core.CmsException;

import java.util.Map;

/**
 * Implementation of a "basic and plain" resource type for any kind of resource.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 */
public class CmsResourceTypePlain extends A_CmsResourceType /*implements I_CmsHtmlLinkValidatable*/ {
        
    /** The type id of this resource */
    public static final int C_RESOURCE_TYPE_ID = 3;
    
    /** The name of this resource */
    public static final String C_RESOURCE_TYPE_NAME = "plain";
    
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
        return CmsDumpLoader.C_RESOURCE_LOADER_ID;
    }     
    
    /**
     * @see com.opencms.file.I_CmsResourceType#createResource(com.opencms.file.CmsObject, java.lang.String, java.util.Map, byte[], java.lang.Object)
     */
    public CmsResource createResource(CmsObject cms, String resourcename, Map properties, byte[] contents, Object parameter) throws CmsException {
        CmsResource res = cms.doCreateFile(resourcename, contents, getResourceTypeName(), properties);
        contents = null;
        // TODO: Move locking of resource to CmsObject or CmsDriverManager
        cms.doLockResource(cms.readAbsolutePath(res), false, CmsLock.C_MODE_COMMON);
        return res;
    }    
    
    /**
     * @see org.opencms.validation.I_CmsHtmlLinkValidatable#findLinks(com.opencms.file.CmsObject, com.opencms.file.CmsResource)
     */
    /*
    public List findLinks(CmsObject cms, CmsResource resource) {
        List links = (List) new ArrayList();
        String link = null;
        Pattern pattern = null;
        Matcher matcher = null;
        String encoding = null;
        String defaultEncoding = null;
        CmsFile file = null;
        String content = null;

        try {
            file = cms.readFile(cms.getRequestContext().removeSiteRoot(resource.getRootPath()));
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error reading file content of " + resource.getRootPath(), e);
            }

            return Collections.EMPTY_LIST;
        }

        try {
            defaultEncoding = cms.getRequestContext().getEncoding();
            encoding = cms.readProperty(resource.getPath(), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true, defaultEncoding);
            content = new String(file.getContents(), encoding);
        } catch (Exception e) {
            content = new String(file.getContents());
        }

        // regex pattern to find all src attribs in img tags, plus all href attribs in anchor tags
        // don't forget to update the group index on the matcher after changing the regex!
        int flags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
        pattern = Pattern.compile("<(img|a)(\\s+)(.*?)(src|href)=(\"|\')(.*?)(\"|\')(.*?)>", flags);

        matcher = pattern.matcher(content);
        while (matcher.find()) {
            link = matcher.group(6);

            if (link.length() > 0 && !link.startsWith("]") && !link.endsWith("[") && link.startsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                // skip href or src attribs split inside CDATA sections by the XML template mechanism
                // skip also URI pointing to external targets outside the OpenCms VFS
                links.add(link);
            }
        }

        return links;
    }
    */
    
}
