/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mysql/CmsVfsDriver.java,v $
 * Date   : $Date: 2004/01/06 16:51:37 $
 * Version: $Revision: 1.26 $
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

package org.opencms.db.mysql;

import com.opencms.core.CmsException;
import com.opencms.file.CmsResource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * MySQL implementation of the VFS driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.26 $ $Date: 2004/01/06 16:51:37 $
 * @since 5.1
 */
public class CmsVfsDriver extends org.opencms.db.generic.CmsVfsDriver {        

    /**
     * @see org.opencms.db.I_CmsVfsDriver#initQueries()
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.mysql.CmsSqlManager();
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readProperties(int, com.opencms.file.CmsResource, int)
     */
    public Map readProperties(int projectId, CmsResource resource, int resourceType) throws CmsException {
        Map original = super.readProperties(projectId, resource, resourceType);
        if (CmsSqlManager.singleByteEncoding()) {
            return original;
        }
        HashMap result = new HashMap(original.size());
        Iterator keys = original.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            result.put(key, CmsSqlManager.unescape((String) original.get(key)));
        }
        original.clear();
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readProperty(java.lang.String, int, com.opencms.file.CmsResource, int)
     */
    public String readProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException {
        return CmsSqlManager.unescape(super.readProperty(meta, projectId, resource, resourceType));
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeProperty(java.lang.String, int, java.lang.String, com.opencms.file.CmsResource, int, boolean)
     */
    public void writeProperty(String meta, int projectId, String value, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException {
        super.writeProperty(meta, projectId, CmsSqlManager.escape(value), resource, resourceType, addDefinition);
    }

}
