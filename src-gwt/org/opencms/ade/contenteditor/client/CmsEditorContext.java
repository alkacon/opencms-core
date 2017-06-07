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

package org.opencms.ade.contenteditor.client;

import java.util.HashMap;
import java.util.Map;

/**
 * This class can be used by code which uses the Acacia editor to pass additional information to the editor on startup.<p>
 */
public class CmsEditorContext {

    /** The HTML context info. */
    private String m_htmlContextInfo;

    /** The parameters for the publish function in the Acacia editor. */
    private Map<String, String> m_publishParameters = new HashMap<String, String>();

    /**
     * Default constructor.<p>
     */
    public CmsEditorContext() {

        // do nothing
    }

    /**
     * Returns the HTML context info.<p>
     *
     * @return the HTML context info
     */
    public String getHtmlContextInfo() {

        return m_htmlContextInfo;
    }

    /**
     * Gets the additional publish parameters which should be used for the publish functionality in the Acacia editor.<p>
     *
     * @return the additional publish parameters
     */
    public Map<String, String> getPublishParameters() {

        return m_publishParameters;
    }

    /**
     * Sets the HTML context info.<p>
     *
     * @param htmlContextInfo the HTML context info to set
     */
    public void setHtmlContextInfo(String htmlContextInfo) {

        m_htmlContextInfo = htmlContextInfo;
    }

    /**
     * Sets the additional publish parameters for the publish functionality in the Acacia editor.<p>
     *
     * @param publishParams the additional publish parameters
     */
    public void setPublishParameters(Map<String, String> publishParams) {

        m_publishParameters = publishParams;
    }
}
