/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.shared.alias;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean which contains the information for creating an alias.<p>
 */
public class CmsAliasBean implements IsSerializable {

    /** The alias mode. */
    private CmsAliasMode m_mode;

    /** The alias site path. */
    private String m_sitePath;

    /**
     * Default constructor, used for serialization.<p>
     */
    public CmsAliasBean() {

        // do nothing
    }

    /**
     * Creates a new alias bean.<p>
     *
     * @param sitePath the site path of the alias
     * @param mode the alias mode
     */
    public CmsAliasBean(String sitePath, CmsAliasMode mode) {

        m_sitePath = sitePath;
        m_mode = mode;
    }

    /**
     * Returns the alias mode.<p>
     *
     * @return the alias mode
     */
    public CmsAliasMode getMode() {

        return m_mode;
    }

    /**
     * Returns the site-relative alias path.<p>
     *
     * @return the site relative alias path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Sets the alias mode.<p>
     *
     * @param mode the alias mode
     */
    public void setMode(CmsAliasMode mode) {

        m_mode = mode;
    }

    /**
     * Sets the alias site path.<p>
     *
     * @param sitePath the alias site path
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

}
