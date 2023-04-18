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

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;

import java.util.List;

import org.jsoup.nodes.Element;

/**
 * Interface for user data domains.<p>
 *
 * A user data domain can provide information about users which have requested their user data.
 * Several user data domains can be configured in opencms-system.xml. When requesting information,
 * there are two types of scenarios:
 *
 * <ul>
 * <li>The user requests data with his email address.
 * <li>The user requests data with his user name and password.
 * </ul>
 *
 * In the first case, more than one user can match the email address, in the second case at most one user is found.
 * For each of the users, they will be matched against each of the user data domains (a user can match more than one domain).
 * Then the domain produces the user data for the list of matching users by appending it to an HTML document.
 */
public interface I_CmsUserDataDomain extends I_CmsConfigurationParameterHandler {

    /**
     * Describes the different places from which the user data domain plugins can be used.
     */
    enum Mode {
        /** The frontend-based way to query user data. */
        frontend,

        /** The workplace-based way to query user data. */
        workplace;
    }

    /**
     * Appends the user data to the given HTML element.
     *
     * @param cms the CMS context
     * @param reqType the request type (email or single user)
     * @param user the list of users (if the request type is singleUser, this has only one element)
     * @param element the HTML element to append the data to
     */
    void appendInfoHtml(CmsObject cms, CmsUserDataRequestType reqType, List<CmsUser> user, Element element);

    /**
     * Writes additional information related to an email address which is not directly associated with a specific OpenCms user.
     *
     * @param cms the CMS context
     * @param email the email address to check
     * @param searchStrings additional search strings entered by the user
     * @param element the element which the additional information should be appended to
     *
     */
    default void appendlInfoForEmail(CmsObject cms, String email, List<String> searchStrings, Element element) {

        // do nothing
    }

    /**
     * Initializes the domain with an admin CmsObject.
     *
     * @param cms a CmsObject
     */
    void initialize(CmsObject cms);

    /**
     * Checks if the plugin is available in the given mode.
     *
     * @param mode the mode
     *
     * @return true if the plugin is available
     */
    default boolean isAvailableForMode(Mode mode) {

        return true;
    }

    /**
     * Checks if the user matches the domain for the given user data request type.
     *
     * @param cms the CMS context
     * @param reqType the user data request type (email or single user)
     * @param user the user
     *
     * @return true if the domain matches the user
     */
    boolean matchesUser(CmsObject cms, CmsUserDataRequestType reqType, CmsUser user);

}
