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

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Client-side bean which holds information about a client variant of a template context.<p>
 */
public class CmsClientVariantInfo implements IsSerializable {

    /** The name. */
    private String m_name;

    /** The nice name. */
    private String m_niceName;

    /** The parameters. */
    private Map<String, String> m_parameters;

    /** The screen height. */
    private int m_screenHeight;

    /** The screen width. */
    private int m_screenWidth;

    /**
     * Creates a new instance.<p>
     *
     * @param name
     * @param niceName
     * @param screenWidth
     * @param screenHeight
     * @param parameters
     */
    public CmsClientVariantInfo(
        String name,
        String niceName,
        int screenWidth,
        int screenHeight,
        Map<String, String> parameters) {

        m_name = name;
        m_niceName = niceName;
        m_screenWidth = screenWidth;
        m_screenHeight = screenHeight;
        m_parameters = parameters;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsClientVariantInfo() {

        // for serialization
    }

    /**
     * Gets the internal name.<p>
     *
     * @return the internal name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the nice name.<p>
     *
     * @return the nice name
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * Gets the parameters.<p>
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {

        return m_parameters;
    }

    /**
     * Gets the screen height.<p>
     *
     * @return the screen height
     */
    public int getScreenHeight() {

        return m_screenHeight;
    }

    /**
     * Gets the screen width.<p>
     *
     * @return the screen width
     */
    public int getScreenWidth() {

        return m_screenWidth;
    }

}
