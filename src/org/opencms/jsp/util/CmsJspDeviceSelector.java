/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import org.opencms.util.CmsRequestUtil;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the detection for different devices, so that the
 * <code>&lt;cms:device type="..."&gt;</code>-Tag can detect which device sends the HTTP request.<p>
 *
 * @since 8.0.0
 */
public class CmsJspDeviceSelector implements I_CmsJspDeviceSelector {

    /** Constant for desktop detection. */
    public static final String C_DESKTOP = "desktop";

    /** Constant for mobile detection. */
    public static final String C_MOBILE = "mobile";

    /** The list of types supported by this device selector implementation. */
    public static final List<String> TYPES = Arrays.asList(new String[] {C_MOBILE, C_DESKTOP});

    /** The user agent info. */
    private UAgentInfo m_userAgentInfo;

    /**
     * @see org.opencms.jsp.util.I_CmsJspDeviceSelector#getDeviceType(javax.servlet.http.HttpServletRequest)
     */
    public String getDeviceType(HttpServletRequest req) {

        m_userAgentInfo = new UAgentInfo(
            req.getHeader(CmsRequestUtil.HEADER_USER_AGENT),
            req.getHeader(CmsRequestUtil.HEADER_ACCEPT));
        if (m_userAgentInfo.detectMobileQuick()) {
            return C_MOBILE;
        }
        return C_DESKTOP;
    }

    /**
     * @see org.opencms.jsp.util.I_CmsJspDeviceSelector#getDeviceTypes()
     */
    public List<String> getDeviceTypes() {

        return TYPES;
    }

    /**
     * Returns the User Agent info.<p>
     *
     * @return the information about the user agent
     */
    public UAgentInfo getUserAgentInfo() {

        return m_userAgentInfo;
    }
}
