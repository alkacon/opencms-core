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

package org.opencms.ui.apps.user;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.notification.A_CmsNotification;
import org.opencms.notification.CmsNotificationMacroResolver;

/**
 * Class to send email to user in case of password reset or creating new user.<p>
 */
public class CmsSendPasswordNotification extends A_CmsNotification {

    /**Is user new? */
    private boolean m_new;

    /**
     * Public constructor.<p>
     *
     * @param cms CmsObject
     * @param password password
     * @param receiver User
     * @param ou
     * @param adminUser User
     * @param link to login
     * @param newUser boolean
     */
    public CmsSendPasswordNotification(
        CmsObject cms,
        String password,
        CmsUser receiver,
        String ou,
        CmsUser adminUser,
        String link,
        boolean newUser) {

        super(cms, receiver);
        m_new = newUser;
        addMacro("password", password);
        addMacro(CmsNotificationMacroResolver.RECEIVER_OU_FQN, ou);
        try {
            addMacro(
                CmsNotificationMacroResolver.RECEIVER_OU,
                OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou).getDisplayName(
                    new CmsUserSettings(receiver).getLocale()));

        } catch (CmsException e) {
            addMacro(CmsNotificationMacroResolver.RECEIVER_OU, receiver.getOuFqn());
        }

    }

    /**
     * @see org.opencms.notification.A_CmsNotification#generateHtmlMsg()
     */
    @Override
    protected String generateHtmlMsg() {

        return "";
    }

    /**
     * @see org.opencms.notification.A_CmsNotification#getNotificationContent()
     */
    @Override
    protected String getNotificationContent() {

        if (m_new) {
            return "/system/config/notification/password-new-user-notification";
        }
        return "/system/config/notification/password-new-password-from-admin-notification";
    }

}
