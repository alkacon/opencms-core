/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean that holds the informations of a principal to show the availability dialog.<p>
 *
 * @since 8.0.0
 */
public class CmsPrincipalBean implements IsSerializable {

    /** The description. */
    private String m_description;

    /** The group flag. */
    private boolean m_isGroup;

    /** The name of the principal. */
    private String m_name;

    /**
     * The default constructor.<p>
     */
    public CmsPrincipalBean() {

        // noop
    }

    /**
     * The public constructor.<p>
     *
     * @param name the name of the principal
     * @param description the description
     * @param isGruop the group flag
     */
    public CmsPrincipalBean(String name, String description, boolean isGruop) {

        m_name = name;
        m_description = description;
        m_isGroup = isGruop;
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
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the isGroup.<p>
     *
     * @return the isGroup
     */
    public boolean isGroup() {

        return m_isGroup;
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the isGroup.<p>
     *
     * @param isGroup the isGroup to set
     */
    public void setGroup(boolean isGroup) {

        m_isGroup = isGroup;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }
}
