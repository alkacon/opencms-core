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

package org.opencms.jsp.userdata;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;

import java.util.List;

import org.jsoup.nodes.Element;

/**
 * Shows email address / user name as a header.
 */
public class CmsUserDataHeader implements I_CmsUserDataDomain {

    /** The configuration (not needed). */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.add(paramName, paramValue);
    }

    /**
     * @see org.opencms.jsp.userdata.I_CmsUserDataDomain#appendInfoHtml(org.opencms.file.CmsObject, org.opencms.jsp.userdata.CmsUserDataRequestType, java.util.List, org.jsoup.nodes.Element)
     */
    public void appendInfoHtml(CmsObject cms, CmsUserDataRequestType reqType, List<CmsUser> user, Element element) {

        String username;
        if (user.size() == 0) {
            return;
        }
        switch (reqType) {
            case email:
                username = user.get(0).getEmail();
                break;
            case singleUser:
            default:
                username = user.get(0).getName();
                break;
        }
        element.appendElement("h1").attr("class", "udr-header").text(
            Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                Messages.GUI_USER_INFORMATION_FOR_1,
                username));
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_config;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // do nothing
    }

    /**
     * @see org.opencms.jsp.userdata.I_CmsUserDataDomain#initialize(org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject cms) {

        // do nothing
    }

    /**
     * @see org.opencms.jsp.userdata.I_CmsUserDataDomain#isAvailableForMode(org.opencms.jsp.userdata.I_CmsUserDataDomain.Mode)
     */
    public boolean isAvailableForMode(Mode mode) {

        return Mode.frontend == mode;
    }

    /**
     * @see org.opencms.jsp.userdata.I_CmsUserDataDomain#matchesUser(org.opencms.file.CmsObject, org.opencms.jsp.userdata.CmsUserDataRequestType, org.opencms.file.CmsUser)
     */
    public boolean matchesUser(CmsObject cms, CmsUserDataRequestType reqType, CmsUser user) {

        return true;
    }

}
