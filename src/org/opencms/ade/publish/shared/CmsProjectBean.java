/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.publish.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A project bean.<p>
 * 
 * @since 7.6 
 */
public class CmsProjectBean implements IsSerializable {

    /** The project id.*/
    private CmsUUID m_id;

    /** The project name.*/
    private String m_name;

    /** 
     * Creates a new project bean.<p> 
     *
     * @param id the project id
     * @param name the project name
     **/
    public CmsProjectBean(CmsUUID id, String name) {

        m_id = id;
        m_name = name;
    }

    /**
     * For serialization.<p>
     */
    protected CmsProjectBean() {

        // for serialization
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }
}
