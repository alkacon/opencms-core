/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/mysql/CmsVfsDriver.java,v $
 * Date   : $Date: 2004/04/01 04:49:40 $
 * Version: $Revision: 1.30 $
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

import org.opencms.db.CmsProperty;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * MySQL implementation of the VFS driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.30 $ $Date: 2004/04/01 04:49:40 $
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
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObject(java.lang.String, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public CmsProperty readPropertyObject(String key, CmsProject currentProject, CmsResource resource) throws CmsException {
        CmsProperty property = super.readPropertyObject(key, currentProject, resource);
        
        property.setStructureValue(CmsSqlManager.unescape(property.getStructureValue()));
        property.setResourceValue(CmsSqlManager.unescape(property.getResourceValue()));
        
        return property;
    }
    
    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObject(org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.db.CmsProperty)
     */
    public void writePropertyObject(CmsProject currentProject, CmsResource resource, CmsProperty property) throws CmsException {
        property.setStructureValue(CmsSqlManager.escape(property.getStructureValue()));
        property.setResourceValue(CmsSqlManager.escape(property.getResourceValue()));
        
        super.writePropertyObject(currentProject, resource, property);
    }
    
    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObjects(org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public List readPropertyObjects(CmsProject currentProject, CmsResource resource) throws CmsException {

        List properties = super.readPropertyObjects(currentProject, resource);
        CmsProperty property = null;

        for (int i = 0; i < properties.size(); i++) {
            property = (CmsProperty)properties.get(i);

            property.setStructureValue(CmsSqlManager.unescape(property.getStructureValue()));
            property.setResourceValue(CmsSqlManager.unescape(property.getResourceValue()));
        }

        return properties;
    }    

}
