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

package org.opencms.notification;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.workplace.CmsWorkplaceLoginHandler;

/**
 * MacroResolver for Notifications.<p>
 */
public class CmsNotificationMacroResolver extends CmsMacroResolver {

    /**Macro for Receiver. */
    public static final String RECEIVER_SIMPLENAME = "receiver.simplename";

    /**Macro for Receiver. */
    public static final String RECEIVER_EMAIL = "receiver.email";

    /**Macro for Receiver. */
    public static final String RECEIVER_FIRSTNAME = "receiver.firstname";

    /**Macro for Receiver. */
    public static final String RECEIVER_LASTNAME = "receiver.lastname";

    /**Macro for Receiver. */
    public static final String RECEIVER_OU = "receiver.ou";

    /**Macro for Receiver. */
    public static final String RECEIVER_OU_FQN = "receiver.ou-fqn";

    /**Macro for Receiver. */
    public static final String RECEIVER_ADDRESS = "receiver.address";

    /**Macro for Receiver. */
    public static final String RECEIVER_CITY = "receiver.city";

    /**Macro for Receiver. */
    public static final String RECEIVER_COUNTRY = "receiver.country";

    /**Macro for Receiver. */
    public static final String RECEIVER_INSTITUTION = "receiver.institution";

    /**Macro for Receiver. */
    public static final String RECEIVER_FULLNAME = "receiver.fullname";

    /**Macro for Author. */
    public static final String AUTHOR_SIMPLENAME = "author.simplename";

    /**Macro for Workplace. */
    public static final String WORKPLACE_URL = "workplace.url";

    /**Macro for Workplace. */
    public static final String WORKPLACE_LOGIN_URL = "workplace.login-url";

    /** Macro for workplace url.*/
    public static final String WORKPLACE_LOGIN_LINK = "workplace.login-url.html";

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param receiver CmsUser who receives the Notification
     */
    public CmsNotificationMacroResolver(CmsObject cms, CmsUser receiver) {

        //Receiver information
        addMacro(RECEIVER_SIMPLENAME, receiver.getSimpleName());
        addMacro(RECEIVER_FIRSTNAME, receiver.getFirstname());
        addMacro(RECEIVER_LASTNAME, receiver.getLastname());
        addMacro(RECEIVER_EMAIL, receiver.getEmail());
        addMacro(RECEIVER_OU_FQN, receiver.getOuFqn());
        try {
            addMacro(
                RECEIVER_OU,
                OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, receiver.getOuFqn()).getDisplayName(
                    new CmsUserSettings(receiver).getLocale()));

        } catch (CmsException e) {
            addMacro(RECEIVER_OU, receiver.getOuFqn());
        }
        addMacro(RECEIVER_ADDRESS, receiver.getAddress());
        addMacro(RECEIVER_CITY, receiver.getCity());
        addMacro(RECEIVER_COUNTRY, receiver.getCountry());
        addMacro(RECEIVER_INSTITUTION, receiver.getInstitution());
        addMacro(RECEIVER_FULLNAME, receiver.getFirstname() + " " + receiver.getLastname());

        //Author
        addMacro(AUTHOR_SIMPLENAME, cms.getRequestContext().getCurrentUser().getSimpleName());

        //Workplace
        addMacro(WORKPLACE_URL, OpenCms.getSiteManager().getWorkplaceServer());
        addMacro(
            WORKPLACE_LOGIN_URL,
            OpenCms.getLinkManager().getWorkplaceLink(cms, CmsWorkplaceLoginHandler.LOGIN_HANDLER, false));
        addMacro(
            WORKPLACE_LOGIN_LINK,
            "<a href =\""
                + OpenCms.getLinkManager().getWorkplaceLink(cms, CmsWorkplaceLoginHandler.LOGIN_HANDLER, false)
                + "\">"
                + OpenCms.getLinkManager().getWorkplaceLink(cms, CmsWorkplaceLoginHandler.LOGIN_HANDLER, false)
                + "</a>");
    }
}
