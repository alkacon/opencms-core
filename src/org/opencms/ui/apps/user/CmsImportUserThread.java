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

package org.opencms.ui.apps.user;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.apps.CmsPageEditorConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Class for the import user thread.<p>
 */
public class CmsImportUserThread extends A_CmsReportThread {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportUserThread.class);

    /**List of user. */
    private List<CmsUser> m_userList;

    /**List of roles. */
    private List<CmsRole> m_roleList;

    /**List of groups. */
    private List<String> m_groupList;

    /**Current ou. */
    private String m_ou;

    /**indicates if mail to user should be send. */
    private boolean m_sendMail;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param ou ou name
     * @param userList List of user to import
     * @param groups groups to add user to
     * @param roles roles to add user to
     * @param sendmail send mail to user?
     */
    protected CmsImportUserThread(
        CmsObject cms,
        String ou,
        List<CmsUser> userList,
        List<String> groups,
        List<CmsRole> roles,
        boolean sendmail) {

        super(cms, "importUser");
        m_userList = userList;
        m_roleList = roles;
        m_groupList = groups;
        m_ou = ou;
        m_sendMail = sendmail;
        initHtmlReport(A_CmsUI.get().getLocale());
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        getReport().println(Messages.get().container(Messages.RPT_USERIMPORT_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        getReport().println();
        getReport().println(
            Messages.get().container(Messages.RPT_USERIMPORT_FILE_CONTAINS_1, String.valueOf(m_userList.size())),
            I_CmsReport.FORMAT_DEFAULT);

        Iterator<CmsUser> itUsers = m_userList.iterator();
        while (itUsers.hasNext()) {
            CmsUser user = itUsers.next();
            try {
                importUser(user);
            } catch (RuntimeException e) {
                String userName = user == null ? "<null>" : String.valueOf(user.getName());
                LOG.error("Unexpected error while importing user '" + userName + "' into OU '" + m_ou + "'.", e);
                getReport().println(
                    "Unable to import user '" + userName + "'. See the OpenCms log for details.",
                    I_CmsReport.FORMAT_ERROR);
            }
        }
        getReport().println(Messages.get().container(Messages.RPT_USERIMPORT_END_0), I_CmsReport.FORMAT_DEFAULT);
    }

    /**
     * Imports a single user.<p>
     *
     * @param user the user data parsed from the CSV file
     */
    private void importUser(CmsUser user) {

        if (user == null) {
            LOG.error("Unable to import null user into OU '" + m_ou + "'.");
            getReport().println("Unable to import an empty user record.", I_CmsReport.FORMAT_ERROR);
            return;
        }

        String userName = user.getName();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userName)) {
            LOG.error("Unable to import user without name into OU '" + m_ou + "'.");
            getReport().println("Unable to import user: the user name is empty.", I_CmsReport.FORMAT_ERROR);
            return;
        }

        if (isAlreadyAvailable(userName)) {
            getReport().println(
                Messages.get().container(Messages.RPT_USERIMPORT_IMPORT_ALREADY_IN_OU_1, userName),
                I_CmsReport.FORMAT_ERROR);
            return;
        }

        String sourcePassword = user.getPassword();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(sourcePassword)) {
            LOG.error("Unable to import user '" + userName + "': no password was supplied by the CSV import.");
            getReport().println(
                "Unable to import user '" + userName + "': password is empty.",
                I_CmsReport.FORMAT_ERROR);
            return;
        }

        String password = sourcePassword;
        String mailPassword = sourcePassword;
        if (password.indexOf('_') == -1) {
            try {
                password = OpenCms.getPasswordHandler().digest(password);
            } catch (CmsPasswordEncryptionException e) {
                LOG.error("Unable to encrypt password for user '" + userName + "'.", e);
                getReport().println(
                    "Unable to import user '" + userName + "': password encryption failed.",
                    I_CmsReport.FORMAT_ERROR);
                return;
            }
        } else {
            password = password.substring(password.indexOf('_') + 1);
            mailPassword = "your old password";
        }

        CmsUser createdUser;
        try {
            createdUser = getCms().importUser(
                new CmsUUID().toString(),
                m_ou + userName,
                password,
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getFlags(),
                System.currentTimeMillis(),
                user.getAdditionalInfo());
        } catch (CmsException e) {
            LOG.error("Unable to create user '" + userName + "' in OU '" + m_ou + "'.", e);
            getReport().println(
                "Unable to create user '" + userName + "'. See the OpenCms log for details.",
                I_CmsReport.FORMAT_ERROR);
            return;
        }

        addGroups(createdUser);
        addRoles(createdUser);
        saveUserSettings(createdUser);

        if (m_sendMail) {
            try {
                CmsUserEditDialog.sendMail(getCms(), mailPassword, user, m_ou, true, false);
            } catch (RuntimeException e) {
                LOG.error("User '" + createdUser.getName() + "' was created, but the notification email could not be sent.", e);
                getReport().println(
                    "User '" + createdUser.getName() + "' was created, but the notification email failed.",
                    I_CmsReport.FORMAT_WARNING);
            }
        }

        getReport().println(
            Messages.get().container(Messages.RPT_USERIMPORT_IMPORT_SUCCESFULL_1, createdUser.getName()),
            I_CmsReport.FORMAT_OK);
    }

    /**
     * Adds the imported user to the selected groups.<p>
     *
     * @param createdUser the imported user
     */
    private void addGroups(CmsUser createdUser) {

        Iterator<String> itGroups = m_groupList.iterator();
        while (itGroups.hasNext()) {
            String groupName = itGroups.next();
            try {
                getCms().addUserToGroup(createdUser.getName(), groupName);
            } catch (CmsException e) {
                LOG.error(
                    "Unable to add imported user '" + createdUser.getName() + "' to group '" + groupName + "'.",
                    e);
                getReport().println(
                    "User '" + createdUser.getName() + "' could not be added to group '" + groupName + "'.",
                    I_CmsReport.FORMAT_WARNING);
            }
        }
    }

    /**
     * Adds the imported user to the selected roles.<p>
     *
     * @param createdUser the imported user
     */
    private void addRoles(CmsUser createdUser) {

        Iterator<CmsRole> itRoles = m_roleList.iterator();
        while (itRoles.hasNext()) {
            CmsRole role = itRoles.next();
            try {
                OpenCms.getRoleManager().addUserToRole(getCms(), role.forOrgUnit(m_ou), createdUser.getName());
            } catch (CmsException e) {
                LOG.error(
                    "Unable to add imported user '" + createdUser.getName() + "' to role '" + role + "'.",
                    e);
                getReport().println(
                    "User '" + createdUser.getName() + "' could not be added to role '" + role + "'.",
                    I_CmsReport.FORMAT_WARNING);
            }
        }
    }

    /**
     * Saves the default workplace settings for the imported user.<p>
     *
     * @param createdUser the imported user
     */
    private void saveUserSettings(CmsUser createdUser) {

        try {
            String startAppId = CmsPageEditorConfiguration.APP_ID;
            if (OpenCms.getRoleManager().hasRole(getCms(), createdUser.getName(), CmsRole.WORKPLACE_USER)) {
                startAppId = CmsFileExplorerConfiguration.APP_ID;
            }
            CmsUserSettings settings = new CmsUserSettings(createdUser);
            settings.setStartView(startAppId);
            settings.setStartProject("Offline");
            settings.save(getCms());
        } catch (CmsException e) {
            LOG.error("Unable to save settings for imported user '" + createdUser.getName() + "'.", e);
            getReport().println(
                "User '" + createdUser.getName() + "' was created, but its workplace settings could not be saved.",
                I_CmsReport.FORMAT_WARNING);
        }
    }

    /**
     * Checks if the given user name is already available inside the current ou.<p>
     *
     * @param userName the user name to check
     * @return <code>true</code> if the user name is already available, otherwise return <code>false</code>
     */
    protected boolean isAlreadyAvailable(String userName) {

        List<CmsUser> availableUsers;
        try {
            availableUsers = OpenCms.getOrgUnitManager().getUsers(getCms(), m_ou, false);
        } catch (CmsException e) {
            LOG.error("Unable to read users from OU '" + m_ou + "' while checking user '" + userName + "'.", e);
            availableUsers = new ArrayList<CmsUser>();
        }
        Iterator<CmsUser> itAvailableUsers = availableUsers.iterator();
        while (itAvailableUsers.hasNext()) {
            if (userName.equals(itAvailableUsers.next().getSimpleName())) {
                return true;
            }
        }
        return false;
    }
}
