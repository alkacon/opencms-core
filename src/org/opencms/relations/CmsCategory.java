/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/relations/CmsCategory.java,v $
 * Date   : $Date: 2007/05/09 14:55:27 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.relations;

import org.opencms.util.CmsUUID;

/**
 * Represents a category, that is just a folder under /system/categories/.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.9.2
 */
public class CmsCategory {

    /** The description of the category. */
    private String m_description;
    /** The path of the category. */
    private String m_path;
    /** The structure id of the resource that this category represents. */
    private CmsUUID m_structureId;
    /** The title of the category. */
    private String m_title;

    /**
     * Deafult constructor.<p>
     * 
     * @param structureId the structure id of the resource that this category represents
     * @param path the path of the category
     * @param title the title of the category
     * @param description the description of the category
     */
    public CmsCategory(CmsUUID structureId, String path, String title, String description) {

        m_structureId = structureId;
        m_path = path;
        m_title = title;
        m_description = description;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (!(obj instanceof CmsCategory)) {
            return false;
        }
        CmsCategory compareCategory = (CmsCategory)obj;
        if (!compareCategory.getId().equals(m_structureId)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the id.<p>
     * 
     * @return the id
     */
    public CmsUUID getId() {

        return m_structureId;
    }

    /**
     * Returns the path.<p>
     * 
     * @return the path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_structureId.hashCode();
    }
}