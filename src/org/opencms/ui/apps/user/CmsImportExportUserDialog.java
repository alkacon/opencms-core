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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.editablegroup.CmsEditableGroup;
import org.opencms.ui.components.editablegroup.I_CmsEditableGroupRow;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.base.Supplier;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/** Dialog for CSV user import and export. */
public final class CmsImportExportUserDialog extends A_CmsImportExportUserDialog
implements Receiver, I_CmsPasswordFetcher {

    /**The dialog height. */
    public static final String DIALOG_HEIGHT = "650px";

    /** Log instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsImportExportUserDialog.class);

    /**vaadin serial id. */
    private static final long serialVersionUID = -2055302491540892101L;

    /**Label to show uploaded file. */
    protected Label m_uploadname;

    /**Start import button. */
    Button m_startImport;

    /**Vaadin Component. */
    private Panel m_includeTechnicalFieldsPanel;

    /**Vaadin Component. */
    private CheckBox m_includeTechnicalFields;

    /**Cancel button. */
    private Button m_cancel;

    /**CmsObject. */
    private CmsObject m_cms;

    /**Download button for export. */
    private Button m_download;

    /**Layout for groups. */
    private VerticalLayout m_exportGroups;

    /**Groups. */
    private CmsEditableGroup m_exportGroupsGroup;

    /**Layout for roles. */
    private VerticalLayout m_exportRoles;

    /**Roles. */
    private CmsEditableGroup m_exportRolesGroup;

    /**Generate password button. */
    private Button m_generateButton;

    /**Should the group field be editable? */
    private boolean m_groupEditable = true;

    /**ID of group. */
    private CmsUUID m_groupID;

    /**Stream for upload file. */
    private ByteArrayOutputStream m_importFileStream;

    /**Layout for groups.*/
    private VerticalLayout m_importGroups;

    /**Groups. */
    private CmsEditableGroup m_importGroupsGroup;

    /**Should password be imported? */
    private CheckBox m_importPasswords;

    /**Layout for roles. */
    private VerticalLayout m_importRoles;

    /**Roles. */
    private CmsEditableGroup m_importRolesGroup;

    /**Password for imported user. */
    private TextField m_password;

    /**List of user to import. */
    List<CmsUser> m_userImportList;

    /**Should the user get an email? */
    private CheckBox m_sendMail;

    /**Tab with import and export sheet. */
    private TabSheet m_tab;

    /**Upload for import. */
    private Upload m_upload;

    /**
     * public constructor.<p>
     *
     * @param ou ou name
     * @param groupID id of group
     * @param window window
     * @param allowTechnicalFieldsExport flag indicates if technical field export option should be available
     */
    private CmsImportExportUserDialog(
        final String ou,
        CmsUUID groupID,
        Window window,
        boolean allowTechnicalFieldsExport) {

        setHeight(DIALOG_HEIGHT);
        m_groupID = groupID;
        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
        } catch (CmsException e) {
            LOG.error("Unable to initialize the CMS context for CSV user import/export.", e);
        }
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_includeTechnicalFieldsPanel.setVisible(allowTechnicalFieldsExport);
        m_includeTechnicalFields.addValueChangeListener(new ValueChangeListener<Boolean>() {

            public void valueChange(ValueChangeEvent event) {

                initDownloadButton();
            }
        });
        m_importPasswords.setValue(Boolean.TRUE);
        m_sendMail.setValue(Boolean.TRUE);
        setButtonVisibility(0);
        m_tab.addSelectedTabChangeListener(
            event -> setButtonVisibility(m_tab.getTabPosition(m_tab.getTab(m_tab.getSelectedTab()))));
        m_password.setValue(CmsGeneratePasswordDialog.getRandomPassword());
        m_startImport.setEnabled(false);
        m_startImport.addClickListener(event -> importUserFromFile());
        m_generateButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 4128513094772586752L;

            public void buttonClick(ClickEvent event) {

                final Window windowDialog = CmsBasicDialog.prepareWindow(CmsBasicDialog.DialogWidth.content);
                windowDialog.setCaption(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GEN_PASSWORD_CAPTION_0));
                CmsGeneratePasswordDialog dialog = new CmsGeneratePasswordDialog(
                    CmsImportExportUserDialog.this,
                    new Runnable() {

                        public void run() {

                            windowDialog.close();
                        }
                    });
                windowDialog.setContent(dialog);
                A_CmsUI.get().addWindow(windowDialog);
            }
        });
        m_upload.setReceiver(this);
        m_upload.addSucceededListener(new Upload.SucceededListener() {

            private static final long serialVersionUID = -6865652127878123021L;

            public void uploadSucceeded(SucceededEvent event) {

                try {
                    m_userImportList = getUsersFromFile();
                    m_startImport.setEnabled(!m_userImportList.isEmpty());
                    m_uploadname.setValue(event.getFilename());
                } catch (RuntimeException e) {
                    //wrong csv columns
                    LOG.error("Invalid CSV user import file '" + event.getFilename() + "'.", e);
                    m_startImport.setEnabled(false);
                    m_uploadname.setValue("");
                    CmsConfirmationDialog.show(
                        CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_INVALID_FILE_0),
                        CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_INVALID_CSV_0),
                        new Runnable() {

                            public void run() {

                                // nothing to do
                            }
                        });
                }
            }
        });

        if (groupID == null) {
            m_importGroupsGroup = new CmsEditableGroup(m_importGroups, new Supplier<Component>() {

                public Component get() {

                    return getGroupSelect(ou, true, null);
                }
            }, CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_ADD_GROUP_0));
            m_importGroupsGroup.init();
            m_exportGroupsGroup = new CmsEditableGroup(m_exportGroups, new Supplier<Component>() {

                public Component get() {

                    return getGroupSelect(ou, true, null);
                }
            }, CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_ADD_GROUP_0));
            m_exportGroupsGroup.init();
        } else {
            m_exportGroups.addComponent(getGroupSelect(ou, false, groupID));
            m_importGroups.addComponent(getGroupSelect(ou, false, groupID));
        }

        m_importRolesGroup = new CmsEditableGroup(m_importRoles, new Supplier<Component>() {

            public Component get() {

                return getRoleComboBox(ou);
            }
        }, CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_ADD_ROLE_0));
        m_importRolesGroup.init();
        m_exportRolesGroup = new CmsEditableGroup(m_exportRoles, new Supplier<Component>() {

            public Component get() {

                return getRoleComboBox(ou);
            }
        }, CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_ADD_ROLE_0));
        m_exportRolesGroup.init();
        super.init(ou, window);
    }

    /**
     * Returns a map with the users to export added.<p>
     * @param cms CmsObject
     * @param ou ou name
     * @param exportUsers the map to add the users
     * @return a map with the users to export added
     * @throws CmsException if getting users failed
     */
    public static Map<CmsUUID, CmsUser> addExportAllUsers(CmsObject cms, String ou, Map<CmsUUID, CmsUser> exportUsers)
    throws CmsException {

        List<CmsUser> users = OpenCms.getOrgUnitManager().getUsers(cms, ou, false);
        if (users != null) {
            for (CmsUser user : users) {
                if (!exportUsers.containsKey(user.getId())) {
                    exportUsers.put(user.getId(), user);
                }
            }
        }
        return exportUsers;
    }

    /**
     * Returns a map with the users to export added.<p>
     * @param cms CmsObject
     * @param groups the selected groups
     * @param exportUsers the map to add the users
     *
     * @return a map with the users to export added
     *
     * @throws CmsException if getting groups or users of group failed
     */
    public static Map<CmsUUID, CmsUser> addExportUsersFromGroups(
        CmsObject cms,
        List<String> groups,
        Map<CmsUUID, CmsUser> exportUsers)
    throws CmsException {

        if (groups != null) {
            for (String group : groups) {
                for (CmsUser user : cms.getUsersOfGroup(group)) {
                    if (!exportUsers.containsKey(user.getId())) {
                        exportUsers.put(user.getId(), user);
                    }
                }
            }
        }
        return exportUsers;
    }

    /**
     * Returns a map with the users to export added.<p>
     * @param cms CmsObject
     * @param ou ou name
     *
     * @param roles the selected roles
     * @param exportUsers the map to add the users
     *
     * @return a map with the users to export added
     *
     * @throws CmsException if getting roles or users of role failed
     */
    public static Map<CmsUUID, CmsUser> addExportUsersFromRoles(
        CmsObject cms,
        String ou,
        List<String> roles,
        Map<CmsUUID, CmsUser> exportUsers)
    throws CmsException {

        if (roles != null) {
            for (String role : roles) {
                List<CmsUser> roleUsers = OpenCms.getRoleManager().getUsersOfRole(
                    cms,
                    CmsRole.valueOfGroupName(role).forOrgUnit(ou),
                    true,
                    false);
                for (CmsUser user : roleUsers) {
                    // contains
                    if (!exportUsers.containsKey(user.getId())) {
                        exportUsers.put(user.getId(), user);
                    }
                }
            }
        }
        return exportUsers;
    }

    /**
     * Gets an dialog instance for fixed group.<p>
     *
     * @param groupID id
     * @param ou ou name
     * @param window window
     * @param allowTechnicalFieldsExport flag indicates if technical field export option should be available
     * @return an instance of this class
         */
    public static CmsImportExportUserDialog getExportUserDialogForGroup(
        CmsUUID groupID,
        String ou,
        Window window,
        boolean allowTechnicalFieldsExport) {

        return new CmsImportExportUserDialog(ou, groupID, window, allowTechnicalFieldsExport);
    }

    /**
     * Gets an dialog instance for fixed group.<p>
     *
     * @param ou ou name
     * @param window window
     * @param allowTechnicalFieldsExport flag indicates if technical field export option should be available
     * @return an instance of this class
     */
    public static CmsImportExportUserDialog getExportUserDialogForOU(
        String ou,
        Window window,
        boolean allowTechnicalFieldsExport) {

        return new CmsImportExportUserDialog(ou, null, window, allowTechnicalFieldsExport);
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsPasswordFetcher#fetchPassword(java.lang.String)
     */
    public void fetchPassword(String password) {

        m_password.setValue(password);
    }

    /**
     * @see com.vaadin.ui.Upload.Receiver#receiveUpload(java.lang.String, java.lang.String)
     */
    public OutputStream receiveUpload(String filename, String mimeType) {

        m_importFileStream = new ByteArrayOutputStream();
        return m_importFileStream;
    }

    /**
     * Get a principle select for choosing groups.<p>
     *
     * @param ou name
     * @param enabled enabled?
     * @param groupID default value
     * @return CmsPrinicpalSelect
     */
    protected CmsPrincipalSelect getGroupSelect(String ou, boolean enabled, CmsUUID groupID) {

        CmsPrincipalSelect select = new CmsPrincipalSelect();
        select.setOU(ou);
        select.setEnabled(enabled);
        select.setRealPrincipalsOnly(true);
        select.setPrincipalType(I_CmsPrincipal.PRINCIPAL_GROUP);
        select.setWidgetType(WidgetType.groupwidget);
        if (groupID != null) {
            try {
                select.setValue(m_cms.readGroup(groupID).getName());
            } catch (CmsException e) {
                LOG.error("Unable to read group", e);
            }
        }
        //OU Change enabled because ou-user can be part of other ou-groups
        return select;
    }

    /**
     * Get ComboBox for selecting roles.<p>
     *
     * @param ou name
     * @return ComboBox
     */
    protected ComboBox<CmsRole> getRoleComboBox(String ou) {

        ComboBox<CmsRole> box = new ComboBox<CmsRole>();
        CmsUserEditDialog.iniRole(A_CmsUI.getCmsObject(), ou, box, null);
        box.setSelectedItem(CmsRole.EDITOR.forOrgUnit(ou));
        return box;
    }

    /**
     * Reads user from import file.<p>
     *
     * @return List of user (with passwords)
     */
    protected List<CmsUser> getUsersFromFile() {

        if (m_importFileStream == null) {
            throw new IllegalArgumentException("No CSV import data is available.");
        }

        return CmsCsvImportUtils.readUsers(
            m_importFileStream.toByteArray(),
            m_password.getValue(),
            m_importPasswords.getValue().booleanValue());
    }

    /**
     * Import user from file.
     */
    protected void importUserFromFile() {

        CmsImportUserThread thread = new CmsImportUserThread(
            m_cms,
            m_ou,
            m_userImportList,
            getGroupsList(m_importGroups, true),
            getRolesList(m_importRoles, true),
            m_sendMail.getValue().booleanValue());
        thread.start();
        CmsShowReportDialog dialog = new CmsShowReportDialog(thread, new Runnable() {

            public void run() {

                m_window.close();
            }
        });
        m_window.setContent(dialog);
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsImportExportUserDialog#getCloseButton()
     */
    @Override
    Button getCloseButton() {

        return m_cancel;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsImportExportUserDialog#getDownloadButton()
     */
    @Override
    Button getDownloadButton() {

        return m_download;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsImportExportUserDialog#getUserToExport()
     */
    @Override
    Map<CmsUUID, CmsUser> getUserToExport() {

        // get the data object from session
        List<String> groups = getGroupsList(m_exportGroups, false);
        Iterator<I_CmsEditableGroupRow> it = m_exportRolesGroup.getRows().iterator();
        List<String> roles = new ArrayList<String>();
        while (it.hasNext()) {
            CmsRole role = (CmsRole)((ComboBox)it.next().getComponent()).getValue();
            roles.add(role.getGroupName());
        }
        Map<CmsUUID, CmsUser> exportUsers = new HashMap<CmsUUID, CmsUser>();
        try {
            if ((groups.size() < 1) && (roles.size() < 1)) {
                exportUsers = addExportAllUsers(m_cms, m_ou, exportUsers);
            } else {
                exportUsers = addExportUsersFromGroups(m_cms, groups, exportUsers);
                exportUsers = addExportUsersFromRoles(m_cms, m_ou, roles, exportUsers);
            }
        } catch (CmsException e) {
            LOG.error("Unable to get export user list.", e);
        }
        return exportUsers;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsImportExportUserDialog#isExportWithTechnicalFields()
     */
    @Override
    boolean isExportWithTechnicalFields() {

        return m_includeTechnicalFields.getValue().booleanValue();
    }

    /**
     * Gets selected groups in List.<p>
     *
     * @param parent layout
     * @param importCase boolean
     * @return List of group names
     */
    private List<String> getGroupsList(VerticalLayout parent, boolean importCase) {

        List<String> result = new ArrayList<String>();
        if (m_groupID != null) {
            try {
                result.add(m_cms.readGroup(m_groupID).getName());
            } catch (CmsException e) {
                LOG.error("Unable to read group", e);
            }
            return result;
        }
        if (m_groupEditable) {
            CmsEditableGroup editableGroup = importCase ? m_importGroupsGroup : m_exportGroupsGroup;
            for (I_CmsEditableGroupRow row : editableGroup.getRows()) {
                String groupName = ((CmsPrincipalSelect)row.getComponent()).getValue();
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(groupName)) {
                    result.add(groupName);
                }
            }
        } else {
            TextField component = (TextField)parent.getComponent(0);
            result.add(component.getValue());
        }
        return result;
    }

    /**
     * Get selected roles list.<p>
     *
     * @param parent layout
     * @param importCase boolean
     * @return List of roles
     */
    private List<CmsRole> getRolesList(VerticalLayout parent, boolean importCase) {

        List<CmsRole> result = new ArrayList<CmsRole>();
        CmsEditableGroup editableGroup = importCase ? m_importRolesGroup : m_exportRolesGroup;
        for (I_CmsEditableGroupRow row : editableGroup.getRows()) {
            result.add(((ComboBox<CmsRole>)row.getComponent()).getValue());
        }
        return result;
    }

    /**
     * Set the visibility of the buttons.<p>
     *
     * @param tab which is selected.
     */
    private void setButtonVisibility(int tab) {

        m_download.setVisible(tab == 1);
        m_startImport.setVisible(tab == 0);
    }
}
