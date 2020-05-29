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
import org.opencms.main.OpenCms;
import org.opencms.ui.dialogs.Messages;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceMessages;

import java.util.List;

import org.jsoup.nodes.Element;

/**
 * User data domain that only matches users requesting their information via user name and password.
 * <p>Produces HTML for standard OpenCms user information like name, description, address, institution etc.
 */
public class CmsDefaultUserDataDomain implements I_CmsUserDataDomain {

    /** The configuration. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.put(paramName, paramValue);
    }

    /**
     * @see org.opencms.jsp.userdata.I_CmsUserDataDomain#appendInfoHtml(org.opencms.file.CmsObject, org.opencms.jsp.userdata.CmsUserDataRequestType, java.util.List, org.jsoup.nodes.Element)
     */
    public void appendInfoHtml(CmsObject cms, CmsUserDataRequestType reqType, List<CmsUser> users, Element element) {

        Element main = element.appendElement("div");
        String headerText = org.opencms.jsp.userdata.Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
            org.opencms.jsp.userdata.Messages.GUI_DEFAULT_USERDATA_SECTION_0);
        main.appendElement("h2").text(headerText);
        Element ul = main.appendElement("ul");
        for (CmsUser user : users) {
            Element section = ul.appendElement("li").appendElement("dl");
            Element dl = section.appendElement("dl");

            addField(cms, dl, org.opencms.ui.apps.Messages.GUI_USERMANAGEMENT_USER_LOGIN_NAME_0, user.getSimpleName());
            addField(
                cms,
                dl,
                org.opencms.ui.apps.Messages.GUI_USERMANAGEMENT_USER_DESCRIPTION_0,
                user.getDescription(cms.getRequestContext().getLocale()));
            addField(
                cms,
                dl,
                org.opencms.ui.apps.Messages.GUI_USERMANAGEMENT_USER_OU_0,
                CmsStringUtil.joinPaths(user.getOuFqn(), "/"));
            addField(cms, dl, Messages.GUI_USER_DATA_FIRSTNAME_0, user.getFirstname());
            addField(cms, dl, Messages.GUI_USER_DATA_LASTNAME_0, user.getLastname());
            addField(cms, dl, Messages.GUI_USER_DATA_ADDRESS_0, user.getAddress());
            addField(cms, dl, Messages.GUI_USER_DATA_CITY_0, user.getCity());
            addField(cms, dl, Messages.GUI_USER_DATA_COUNTRY_0, user.getCountry());
            addField(cms, dl, Messages.GUI_USER_DATA_EMAIL_0, user.getEmail());
            addField(cms, dl, Messages.GUI_USER_DATA_INSTITUTION_0, user.getInstitution());
            addField(cms, dl, Messages.GUI_USER_DATA_ZIPCODE_0, user.getZipcode());
        }
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

        // Not used
    }

    /**
     * @see org.opencms.jsp.userdata.I_CmsUserDataDomain#initialize(org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject cms) {

        // does nothing
    }

    /**
     * @see org.opencms.jsp.userdata.I_CmsUserDataDomain#matchesUser(org.opencms.file.CmsObject, org.opencms.jsp.userdata.CmsUserDataRequestType, org.opencms.file.CmsUser)
     */
    public boolean matchesUser(CmsObject cms, CmsUserDataRequestType reqType, CmsUser user) {

        return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getClass().getName() + "[" + m_config + "]";
    }

    /**
     * Adds dt/dt tags with a given field name and field value to a parent dl element.
     *
     * @param cms the CMS context
     * @param dl the parent element
     * @param key the key
     * @param value the value
     */
    private boolean addField(CmsObject cms, Element dl, String key, String value) {

        CmsWorkplaceMessages messages = OpenCms.getWorkplaceManager().getMessages(cms.getRequestContext().getLocale());
        String keyText = messages.key(key);
        if (!CmsStringUtil.isEmpty(value)) {
            dl.appendElement("dt").text(keyText);
            dl.appendElement("dd").text(value);
            return true;
        }
        return false;
    }
}
