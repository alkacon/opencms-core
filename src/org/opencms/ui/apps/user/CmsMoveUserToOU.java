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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Dialog to change OU of given user.<p>
 */
public class CmsMoveUserToOU extends CmsBasicDialog {

    /**
     * OU ComboBox validator.<p>
     */
    class OUValidator implements Validator {

        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            String ou = (String)value;

            if (m_user.getOuFqn().equals(ou)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_MOVE_OU_SAME_OU_0));
            }
            try {
                m_cms.readUser(ou + m_user.getSimpleName());
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_MOVE_OU_USERNAME_COLLISION_0));
            } catch (CmsException e) {
                //Ok, no user found
            }

        }

    }

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsMoveUserToOU.class.getName());

    /**vaadin serial id.*/
    private static final long serialVersionUID = 1L;

    /**CmsObject. */
    protected CmsObject m_cms;

    /**User. */
    CmsUser m_user;

    /**Vaadin component. */
    private Button m_cancel;

    /**Vaadin component. */
    private VerticalLayout m_error;

    /**Vaadin component. */
    private CmsGroupsOfUserTable m_groupTable;

    /**Vaadin component. */
    private Button m_ok;

    /**Vaadin component. */
    private ComboBox m_ou;

    /**Vaadin component. */
    private CheckBox m_removeAll;

    /**
     * Public constructor.<p>
     *
     * @param cms CmsObject
     * @param userID ID of user
     * @param window Window holding the dialog
     * @param app to be updated
     */
    public CmsMoveUserToOU(CmsObject cms, CmsUUID userID, final Window window, final CmsAccountsApp app) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_cms = cms;
        try {
            m_user = cms.readUser(userID);
            displayResourceInfoDirectly(Collections.singletonList(CmsAccountsApp.getPrincipalInfo(m_user)));
            List<CmsGroup> groups = cms.getGroupsOfUser(m_user.getName(), true);
            if (!groups.isEmpty()) {
                m_error.setVisible(true);
                m_groupTable.init(app, m_cms, m_user, groups);
                m_ok.setEnabled(false);
                m_removeAll.addValueChangeListener(new ValueChangeListener() {

                    private static final long serialVersionUID = 1L;

                    public void valueChange(ValueChangeEvent event) {

                        setOkState();
                    }
                });
            }
            m_ou.setContainerDataSource(CmsVaadinUtils.getOUComboBox(m_cms, "", LOG, false).getContainerDataSource());
            m_ou.setItemCaptionPropertyId("desc");
            m_ou.setNewItemsAllowed(false);
            m_ou.setFilteringMode(FilteringMode.CONTAINS);
            m_ou.select("");
            m_ou.setNullSelectionAllowed(false);
        } catch (CmsException e) {
            LOG.error("Cannot read user with id " + userID, e);
        }

        m_ou.addValidator(new OUValidator());
        m_ok.addClickListener(e -> submit(window, app));
        m_cancel.addClickListener(e -> window.close());
    }

    /**
     * En/disables the ok button.<p>
     */
    protected void setOkState() {

        m_ok.setEnabled(m_removeAll.getValue().booleanValue());
    }

    /**
     * Perform ok action.<p>
     *
     * @param window Window
     * @param app app
     */
    protected void submit(Window window, CmsAccountsApp app) {

        if (m_ou.isValid()) {
            try {
                //First read out groups without removing them. Remove may change roles..
                List<CmsGroup> userGroups = new ArrayList<CmsGroup>();
                for (CmsGroup group : m_cms.getGroupsOfUser(m_user.getName(), true)) {
                    userGroups.add(group);
                }
                List<CmsRole> directOURoles = new ArrayList<CmsRole>();
                List<CmsRole> otherRoles = new ArrayList<CmsRole>();
                for (CmsRole role : OpenCms.getRoleManager().getRolesOfUser(
                    m_cms,
                    m_user.getName(),
                    m_user.getOuFqn(),
                    true,
                    true,
                    false)) {
                    //OU specific roles: save them to add them to new ou later
                    if ((m_user.getOuFqn()).equals(role.getOuFqn())) {
                        directOURoles.add(role);
                        OpenCms.getRoleManager().removeUserFromRole(m_cms, role, m_user.getName());
                        continue;
                    }
                    //Sub OUs: just delete
                    if (role.getOuFqn().startsWith(m_user.getOuFqn())) {
                        OpenCms.getRoleManager().removeUserFromRole(m_cms, role, m_user.getName());
                        continue;
                    }
                    //Other role from complete different OU: preserve
                    otherRoles.add(role);
                }
                for (CmsGroup group : userGroups) {
                    try {
                        m_cms.removeUserFromGroup(m_user.getName(), group.getName());
                    } catch (CmsIllegalArgumentException e) {
                        //Group not there.. happens if the Admin group gets removed
                        LOG.error("User cannot be removed from group", e);
                    }
                }
                OpenCms.getOrgUnitManager().setUsersOrganizationalUnit(
                    m_cms,
                    (String)m_ou.getValue(),
                    m_user.getName());
                //Reload moved user
                m_user = m_cms.readUser(m_user.getId());
                for (CmsRole role : directOURoles) {
                    OpenCms.getRoleManager().addUserToRole(
                        m_cms,
                        role.forOrgUnit((String)m_ou.getValue()),
                        m_user.getName());
                }
                for (CmsRole role : otherRoles) {
                    OpenCms.getRoleManager().addUserToRole(m_cms, role, m_user.getName());
                }
            } catch (CmsException e) {
                LOG.error("Unable to move the user to anouther OU", e);
            }
            window.close();
            app.reload();
        }
    }
}
