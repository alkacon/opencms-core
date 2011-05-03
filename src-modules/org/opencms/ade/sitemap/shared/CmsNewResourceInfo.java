/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsNewResourceInfo.java,v $
 * Date   : $Date: 2011/05/03 10:49:06 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.shared;

import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * A bean representing a resource type for use in the detail page creation menu.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsNewResourceInfo implements Serializable {

    /** ID for serialization. */
    private static final long serialVersionUID = -4731814848380350682L;

    /** The structure id of the copy resource. */
    private CmsUUID m_copyResourceId;

    private String m_description;

    /** The id. */
    private int m_id;

    /** The type name. */
    private String m_typeName;

    /** The title. */
    private String m_title;

    /**
     * Instantiates a new resource type information bean.
     *
     * @param id the id
     * @param typeName the type name
     * @param title the title
     * @param description the description
     * @param copyResourceId the structure id of the copy resource
     */
    public CmsNewResourceInfo(int id, String typeName, String title, String description, CmsUUID copyResourceId) {

        m_id = id;
        m_typeName = typeName;
        m_title = title;
        m_copyResourceId = copyResourceId;
        m_description = description;
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
     * Empty default constructor for serialization.<p>
     */
    protected CmsNewResourceInfo() {

        // do nothing
    }

    /**
     * Returns the structure id of the copy resource.<p>
     *
     * @return the structure id of the copy resource
     */
    public CmsUUID getCopyResourceId() {

        return m_copyResourceId;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {

        return m_id;
    }

    /**
     * Gets the type name.
     *
     * @return the type name
     */
    public String getTypeName() {

        return m_typeName;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

}
