/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsPublishList.java,v $
 * Date   : $Date: 2004/01/28 09:32:23 $
 * Version: $Revision: 1.1 $
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

package org.opencms.db;

import org.opencms.util.CmsUUID;

import com.opencms.file.CmsResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A list of Cms file resources of a project or a direct published file resource (and optionally 
 * it's siblings) that actually get published.<p>
 * 
 * Only classes inside the org.opencms.db package can add or remove elements to or from this list. 
 * This allows the Cms app to pass the list around between classes, but with restricted access to 
 * create this list.<p>
 * 
 * This list contains only file resources, no folders. Folders to be published are still identified 
 * in the project driver's publishProject method.<p>
 * 
 * {@link org.opencms.db.CmsDriverManager#getPublishList(CmsRequestContext, CmsResource, boolean, I_CmsReport)}
 * creates Cms publish lists.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2004/01/28 09:32:23 $
 * @since 5.3.0
 * @see org.opencms.db.CmsDriverManager#getPublishList(com.opencms.file.CmsRequestContext, CmsResource, boolean, org.opencms.report.I_CmsReport)
 */
public class CmsPublishList {

    /** Flag indicating if this is a publish list for a direct published file *OR* folder.<p> */
    private boolean m_isDirectPublish;

    /** Flag indicating if this is a publish list for a direct published file.<p> */
    private boolean m_isDirectPublishFile;

    /** The parent structure ID of a direct published resource.<p> */
    private CmsUUID m_parentStructureId;

    /** The publish history ID.<p> */
    private CmsUUID m_publishHistoryId;

    /** The publish list.<p> */
    private List m_resourceList;

    /** The resource name of a direct published resource.<p> */
    private String m_resourceName;

    /**
     * Constructs an empty publish list for the resources of a project to be published.<p>
     */
    public CmsPublishList() {
        this(null, false);
    }

    /**
     * Constructs an empty publish list with additional information for a direct published resource.<p>
     * 
     * @param directPublishResource a Cms resource to be published directly
     * @param isDirectPublishFile true if a Cms file gets published directly and the type of the current project is switched to {@link com.opencms.core.I_CmsConstants.C_PROJECT_TYPE_DIRECT_PUBLISH}
     */
    public CmsPublishList(CmsResource directPublishResource, boolean isDirectPublishFile) {
        m_resourceList = (List) new ArrayList();
        m_isDirectPublish = directPublishResource != null;

        if (m_isDirectPublish) {
            m_parentStructureId = directPublishResource.getParentStructureId();
            m_resourceName = directPublishResource.getRootPath();
            m_isDirectPublishFile = isDirectPublishFile;
            m_isDirectPublish = true;
        } else {
            m_parentStructureId = CmsUUID.getNullUUID();
            m_resourceName = null;
            m_isDirectPublishFile = false;
            m_isDirectPublish = false;
        }

        m_publishHistoryId = new CmsUUID();
    }

    /**
     * Adds a Cms resource to the publish list.<p>
     * 
     * @param resource a Cms resource
     * @see List#add(java.lang.Object)
     */
    protected void add(CmsResource resource) {
        // it is essential that this method is only visible within the db package!
        m_resourceList.add(resource);
    }

    /**
     * Appends all of the elements in the specified collection to the end of this publish list.<p>
     * 
     * @param collection a collection whose elements are to be added to this list
     * @return true if this publish list changed as a result of the call
     * @see List#addAll(java.util.Collection)
     */
    protected boolean addAll(Collection collection) {
        // it is essential that this method is only visible within the db package!
        return m_resourceList.addAll(collection);
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            if (m_resourceList != null) {
                m_resourceList.clear();
            }
            m_resourceList = null;
        } catch (Throwable t) {
            // ignore
        }

        super.finalize();
    }

    /**
     * Returns the parent structure Id if a resource gets published directly, or the null Cms UUID.<p>
     * 
     * @return the parent structure Id if a resource gets published directly, or the null Cms UUID
     */
    public CmsUUID getDirectPublishParentStructureId() {
        return m_parentStructureId;
    }

    /**
     * Returns the resource name of a direct published resource, or null.<p>
     * 
     * @return the resource name
     */
    public String getDirectPublishResourceName() {
        String resourceName = null;

        if (m_isDirectPublish && m_resourceName != null) {
            resourceName = m_resourceName;
        }

        return resourceName;
    }

    /**
     * Returns the publish history Id for this publish list.<p>
     * 
     * @return the publish history Id
     */
    public CmsUUID getPublishHistoryId() {
        return m_publishHistoryId;
    }

    /**
     * Returns an unmodifiable list of this this publish list.<p>
     * 
     * @return the list with the Cms resources in this publish list
     */
    public List getResourceList() {
        return Collections.unmodifiableList(m_resourceList);
    }

    /**
     * Returns the list with the Cms resources.<p>
     * 
     * @return the list with the Cms resources
     */
    protected List getResourceListInstance() {
        // it is essential that this method is only visible within the db package!
        return m_resourceList;
    }

    /**
     * Checks if this is a publish list for a direct published file *OR* folder.<p>
     * 
     * @return true if this is a publish list for a direct published file *OR* folder
     * @see #isDirectPublishFile()
     */
    public boolean isDirectPublish() {
        return m_isDirectPublish;
    }

    /**
     * Checks if this is a publish list for a direct published file.<p>
     * 
     * @return true if this is a publish list for a direct published file
     * @see #isDirectPublish()
     */
    public boolean isDirectPublishFile() {
        return m_isDirectPublishFile;
    }

    /**
     * Removes a Cms resource from the publish list.<p>
     * 
     * @param resource a Cms resource
     * @return true if this publish list contains the specified resource
     * @see List#remove(java.lang.Object)
     */
    protected boolean remove(CmsResource resource) {
        // it is essential that this method is only visible within the db package!
        return m_resourceList.remove(resource);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return m_resourceList.toString();
    }

}
