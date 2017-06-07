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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools;

import org.opencms.file.CmsObject;

/**
 * Admin tool handler for tool roots.<p>
 *
 * @since 6.0.0
 */
public class CmsToolRootHandler extends A_CmsToolHandler {

    /** The key to access this tool root. */
    private String m_key;
    /** The uri where to look for the tools for this root. */
    private String m_uri;

    /**
     * Returns the key.<p>
     *
     * @return the key
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Returns the uri.<p>
     *
     * @return the uri
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        return true;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    public boolean isVisible(CmsObject cms) {

        return true;
    }

    /**
     * Sets the key.<p>
     *
     * @param key the key to set
     */
    public void setKey(String key) {

        m_key = key;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#setup(org.opencms.file.CmsObject, org.opencms.workplace.tools.CmsToolRootHandler, java.lang.String)
     */
    @Override
    public boolean setup(CmsObject cms, CmsToolRootHandler root, String resourcePath) {

        setDisabledHelpText(getHelpText());
        setIconPath("admin/images/deficon.png");
        setSmallIconPath(getIconPath());
        setPath("/");
        setGroup("");
        setPosition(1);
        setLink(cms, resourcePath);
        return true;
    }

    /**
     * Sets the uri.<p>
     *
     * @param uri the uri to set
     */
    public void setUri(String uri) {

        m_uri = uri;
    }
}
