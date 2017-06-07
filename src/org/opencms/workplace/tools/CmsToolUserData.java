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

import java.util.HashMap;
import java.util.Map;

/**
 * Store for some administration view parameters,
 * for each user, used by the <code>{@link CmsToolManager}</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsToolUserData {

    /** base tool for the user, root-based. */
    private Map<String, String> m_baseTools;

    /** Current used tool paths, root-based. */
    private Map<String, String> m_currentToolPaths;

    /** root key for the user. */
    private String m_rootKey;

    /**
     * Default Constructor.<p>
     */
    public CmsToolUserData() {

        m_baseTools = new HashMap<String, String>();
        m_currentToolPaths = new HashMap<String, String>();
    }

    /**
     * Returns the base tool.<p>
     *
     * @param rootKey the tool root
     *
     * @return the base tool
     */
    public String getBaseTool(String rootKey) {

        return m_baseTools.get(rootKey);
    }

    /**
     * Returns the current tool path.<p>
     *
     * @param rootKey the tool root
     *
     * @return the current tool path
     */
    public String getCurrentToolPath(String rootKey) {

        return m_currentToolPaths.get(rootKey);
    }

    /**
     * Returns the root key.<p>
     *
     * @return the root key
     */
    public String getRootKey() {

        return m_rootKey;
    }

    /**
     * Sets the base tool.<p>
     *
     * @param rootKey the tool root
     * @param baseTool the base tool to set
     */
    public void setBaseTool(String rootKey, String baseTool) {

        m_baseTools.put(rootKey, baseTool);
    }

    /**
     * Sets the current tool path.<p>
     *
     * @param rootKey the tool root
     * @param currentToolPath the current tool path to set
     */
    public void setCurrentToolPath(String rootKey, String currentToolPath) {

        m_currentToolPaths.put(rootKey, currentToolPath);
    }

    /**
     * Sets the root key.<p>
     *
     * @param key the root key to set
     */
    public void setRootKey(String key) {

        m_rootKey = key;
    }

}