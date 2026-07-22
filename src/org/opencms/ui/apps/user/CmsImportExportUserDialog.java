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

    public static final String DIALOG_HEIGHT = "650px";

    static final Log LOG = CmsLog.getLog(CmsImportExportUserDialog.class);

    private static final long serialVersionUID = -2055302491540892101L;

    protected Label m_uploadname;
    Button m_startImport;
    private Panel m_includeTechnicalFieldsPanel;
    private CheckBox m_includeTechnicalFields;
    private Button m_cancel;
    private CmsObject m_cms;
    private Button m_download;
    private VerticalLayout m_exportGroups;
    private CmsEditableGroup m_exportGroupsGroup;
    private VerticalLayout m_exportRoles;
    private CmsEditableGroup m_exportRolesGroup;
    private Button m_generateButton;
    private boolean m_groupEditable = true;
    private CmsUUID m_groupID;
    private ByteArrayOutputStream m_importFileStream;
    private VerticalLayout m_importGroups;
    private CmsEditableGroup m_importGroupsGroup;
    private CheckBox m_importPasswords;
    private VerticalLayout m_importRoles;
    private CmsEditableGroup m_importRolesGroup;
    private TextField m_password;
    List<CmsUser> m_userImportList;
    private CheckBox m_sendMail;
    private TabSheet m_tab;
    private Upload m_upload;

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
                    if (!exportUsers.containsKey(user.getId())) {
                        exportUsers.put(user.getId(), user);
                    }
                }
            }
        }
        return exportUsers;
    }

    public static CmsImportExportUserDialog getExportUserDialogForGroup(
        CmsUUID groupID,
        String ou,
        Window window,
        boolean allowTechnicalFieldsExport) {

        return new CmsImportExportUserDialog(ou, groupID, window, allowTechnicalFieldsExport);
    }

    public static CmsImportExportUserDialog getExportUserDialogForOU(
        String ou,
        Window window,
        boolean allowTechnicalFieldsExport) {

        return new CmsImportExportUserDialog(ou, null, window, allowTechnicalFieldsExport);
    }

    public void fetchPassword(String password) {

        m_password.setValue(password);
    }

    public OutputStream receiveUpload(String filename, String mimeType) {

        m_importFileStream = new ByteArrayOutputStream();
        return m_importFileStream;
    }

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
        return select;
    }

    protected ComboBox<CmsRole> getRoleComboBox(String ou) {

        ComboBox<CmsRole> box = new ComboBox<CmsRole>();
        CmsUserEditDialog.iniRole(A_CmsUI.getCmsObject(), ou, box, null);
        box.setSelectedItem(CmsRole.EDITOR.forOrgUnit(ou));
        return box;
    }

    protected List<CmsUser> getUsersFromFile() {

        if (m_importFileStream == null) {
            throw new IllegalArgumentException("No CSV import data is available.");
        }

        return CmsCsvImportUtils.readUsers(
            m_importFileStream.toByteArray(),
            m_password.getValue(),
            m_importPasswords.getValue().booleanValue());
    }

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

    @Override
    Button getCloseButton() {

        return m_cancel;
    }

    @Override
    Button getDownloadButton() {

        return m_download;
    }

    @Override
    Map<CmsUUID, CmsUser> getUserToExport() {

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

    @Override
    boolean isExportWithTechnicalFields() {

        return m_includeTechnicalFields.getValue().booleanValue();
    }

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

    private List<CmsRole> getRolesList(VerticalLayout parent, boolean importCase) {

        List<CmsRole> result = new ArrayList<CmsRole>();
        CmsEditableGroup editableGroup = importCase ? m_importRolesGroup : m_exportRolesGroup;
        for (I_CmsEditableGroupRow row : editableGroup.getRows()) {
            result.add(((ComboBox<CmsRole>)row.getComponent()).getValue());
        }
        return result;
    }

    private void setButtonVisibility(int tab) {

        m_download.setVisible(tab == 1);
        m_startImport.setVisible(tab == 0);
    }
}
