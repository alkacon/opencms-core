/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/Attic/CmsVfsIndexResource.java,v $
 * Date   : $Date: 2004/11/19 09:06:48 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search;

import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;

/**
 * Contains the data of a VFS Cms resource specified by a Lucene 
 * search result document.<p>
 * 
 * @version $Revision: 1.8 $ $Date: 2004/11/19 09:06:48 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 */
public class CmsVfsIndexResource extends A_CmsIndexResource {

    /** 
     * Creates a new instance to wrap the given <code>CmsResource</code>.<p>
     * 
     * @param res the data object
     */
    public CmsVfsIndexResource(CmsResource res) {

        m_data = res;
        m_id = res.getResourceId();
        m_name = res.getName();
        
        // TODO: needs a better way to identify the generic type of a resource
        try {
            I_CmsResourceType resourceType = OpenCms.getResourceManager().getResourceType(res.getTypeId());
            I_CmsResourceType genericType = (I_CmsResourceType)resourceType.getClass().newInstance();
            m_type = genericType.getTypeId();
        } catch (Exception exc) {
            m_type = res.getTypeId();
        }
        
        // TODO: Add check for encoding property or otherwise care about the encoding here
        m_mimeType = OpenCms.getResourceManager().getMimeType(res.getName(), null);
        m_path = res.getRootPath();
    }

    /**
     * @see org.opencms.search.A_CmsIndexResource#getDocumentKey(boolean)
     */
    public String getDocumentKey(boolean withMimeType) {

        return "VFS" + getType() + ((withMimeType) ? ":" + getMimetype() : "");
    }
}