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
import org.opencms.ui.login.CmsLoginHelper;
import org.opencms.util.CmsMacroResolver;
import org.opencms.workplace.CmsWorkplaceLoginHandler;

/**
 * Class to send email to user in case of password reset or creating new user.<p>
 */
public class CmsSendPasswordNotification extends A_CmsNotification {

    /** Field name. */
    private static final String FIELD_CHANGE_PASSWORD = "TextChangePassword";

    /** Field name. */
    private static final String FIELD_KEEP_PASSWORD = "TextKeepPassword";

    /**Is user new? */
    private boolean m_new;

    /**Is password temporal? */
    private boolean m_tempPassword;

    /**
     * Public constructor.<p>
     *
     * @param cms CmsObject
     * @param password password
     * @param receiver User
     * @param ou the user OU
     * @param adminUser User
     * @param newUser boolean
     * @param tempPassword <code>true</code> to use a temporary password
     */
    public CmsSendPasswordNotification(
        CmsObject cms,
        String password,
        CmsUser receiver,
        String ou,
        CmsUser adminUser,
        boolean newUser,
        boolean tempPassword) {

        super(cms, receiver);
        m_new = newUser;
        m_tempPassword = tempPassword;
        addMacro("password", password);
        String link = OpenCms.getLinkManager().getWorkplaceLink(cms, CmsWorkplaceLoginHandler.LOGIN_HANDLER, false)
            + "?"
            + CmsLoginHelper.PARAM_USERNAME
            + "="
            + receiver.getSimpleName()
            + "&"
            + CmsLoginHelper.PARAM_OUFQN
            + "="
            + receiver.getOuFqn();
        addMacro(CmsNotificationMacroResolver.WORKPLACE_LOGIN_LINK, "<a href =\"" + link + "\">" + link + "</a>");
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
     * Public constructor.<p>
     *
     * @param cms CmsObject
     * @param password password
     * @param receiver User
     * @param ou the user OU
     * @param adminUser User
     * @param link not used
     * @param newUser boolean
     * @param tempPassword <code>true</code> to use a temporary password
     */
    @Deprecated
    public CmsSendPasswordNotification(
        CmsObject cms,
        String password,
        CmsUser receiver,
        String ou,
        CmsUser adminUser,
        String link,
        boolean newUser,
        boolean tempPassword) {

        this(cms, password, receiver, ou, adminUser, newUser, tempPassword);

    }

    /**
     * @see org.opencms.notification.A_CmsNotification#appendXMLContent(java.lang.StringBuffer)
     */
    @Override
    protected void appendXMLContent(StringBuffer msg) {

        String xmlName = m_tempPassword ? FIELD_CHANGE_PASSWORD : FIELD_KEEP_PASSWORD;

        // append header from xmlcontent
        msg.append(
            CmsMacroResolver.resolveMacros(m_mailContent.getStringValue(m_cms, xmlName, m_locale), m_macroResolver));

        msg.append("\n<br/><br/>\n");

        // append footer from xmlcontent
        msg.append(
            CmsMacroResolver.resolveMacros(m_mailContent.getStringValue(m_cms, "Footer", m_locale), m_macroResolver));

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
            return OpenCms.getSystemInfo().getConfigFilePath(m_cms, "notification/password-new-user-notification");
        }
        return OpenCms.getSystemInfo().getConfigFilePath(
            m_cms,
            "notification/password-new-password-from-admin-notification");
    }

}
