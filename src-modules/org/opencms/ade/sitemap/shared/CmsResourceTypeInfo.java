/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsResourceTypeInfo.java,v $
 * Date   : $Date: 2010/12/17 08:45:29 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import java.io.Serializable;

/**
 * A bean representing a resource type for use in the detail page creation menu.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsResourceTypeInfo implements Serializable {

    /** ID for serialization. */
    private static final long serialVersionUID = -4731814848380350682L;

    /** The id. */
    private int m_id;

    /** The name. */
    private String m_name;

    /** The title. */
    private String m_title;

    /**
     * Instantiates a new resource type information bean.
     *
     * @param id the id
     * @param name the name
     * @param title the title
     */
    public CmsResourceTypeInfo(int id, String name, String title) {

        super();
        m_id = id;
        m_name = name;
        m_title = title;
    }

    /**
     * Empty default constructor for serialization.<p>
     */
    protected CmsResourceTypeInfo() {

        // do nothing
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
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {

        return m_name;
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
