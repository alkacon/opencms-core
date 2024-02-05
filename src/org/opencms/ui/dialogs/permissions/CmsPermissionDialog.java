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

package org.opencms.ui.dialogs.permissions;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.contextmenu.CmsStandardVisibilityCheck;
import org.opencms.ui.dialogs.permissions.CmsPermissionView.PermissionChangeHandler;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.I_PrincipalSelectHandler;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.commons.Messages;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * The permission dialog.<p>
 */
public class CmsPermissionDialog extends CmsBasicDialog implements PermissionChangeHandler, I_PrincipalSelectHandler {

    /** The possible types of new access control entries. */
    protected static final String[] PRINCIPAL_TYPES = {
        I_CmsPrincipal.PRINCIPAL_GROUP,
        I_CmsPrincipal.PRINCIPAL_USER,
        CmsRole.PRINCIPAL_ROLE,
        CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
        CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME};

    /** The possible type values of access control entries. */
    protected static final int[] PRINCIPAL_TYPES_INT = {
        CmsAccessControlEntry.ACCESS_FLAGS_GROUP,
        CmsAccessControlEntry.ACCESS_FLAGS_USER,
        CmsAccessControlEntry.ACCESS_FLAGS_ROLE,
        CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS,
        CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL};

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPermissionDialog.class);

    /** The serial version id. */
    private static final long serialVersionUID = 2397141190651779325L;

    /**Set of permissions to save on ok-Button click. */
    private Set<CmsPermissionBean> m_permissionToChange = new HashSet<CmsPermissionBean>();

    /** The permission view accordion. */
    private Accordion m_accordion;

    /** The close button. */
    private Button m_closeButton;

    /** The ok button.*/
    private Button m_okButton;

    /** The cms context. */
    private CmsObject m_cms;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The editable flag. */
    private boolean m_editable = true;

    /** The inherited permissions panel. */
    private VerticalLayout m_inheritedPermissions;

    /** The lock action record. */
    private CmsLockActionRecord m_lockActionRecord;

    /** main layout. */
    private VerticalLayout m_main;

    /** The principal select widget. */
    private CmsPrincipalSelect m_principalSelect;

    /** The resource. */
    private CmsResource m_resource;

    /**Parent path map. */
    private Map<CmsUUID, String> m_parents;

    /** The resource permissions panel. */
    private VerticalLayout m_resourcePermissions;

    /** The tab for setting permissions. */
    private VerticalLayout m_setPermissionTab;

    /** The user permission panel. */
    private VerticalLayout m_userPermissions;

    /**
     * Constructor.<p>
     *
     * @param context the dialog context.<p>
     */
    public CmsPermissionDialog(I_CmsDialogContext context) {

        m_context = context;
        m_cms = context.getCms();
        m_editable = CmsStandardVisibilityCheck.PERMISSIONS.getVisibility(context).isActive();
        m_resource = context.getResources().get(0);
        boolean editRoles = CmsWorkplace.canEditPermissionsForRoles(m_cms, m_resource.getRootPath());
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_main.setHeightUndefined();
        if (m_editable) {
            m_principalSelect.setMargin(true);
            m_principalSelect.setSelectHandler(this);
            m_principalSelect.setRoleSelectionAllowed(editRoles);
        } else {
            m_principalSelect.setVisible(false);
        }
        displayResourceInfo(Collections.singletonList(m_resource));
        displayUserPermissions(m_cms.getRequestContext().getCurrentUser());
        displayInheritedPermissions();
        displayResourcePermissions();
        m_closeButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                close();
            }

        });
        m_accordion.addSelectedTabChangeListener(new SelectedTabChangeListener() {

            private static final long serialVersionUID = 1L;

            public void selectedTabChange(SelectedTabChangeEvent event) {

                onViewChange();
            }
        });

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -8061995491261573917L;

            public void buttonClick(ClickEvent event) {

                savePermissions();
                close();
            }

        });

        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                close();
            }

            @Override
            protected void ok() {

                // nothing to do
            }
        });
        m_accordion.setSelectedTab(m_setPermissionTab);
        m_principalSelect.setPrincipalType(I_CmsPrincipal.PRINCIPAL_GROUP);

    }

    /**
     * @see org.opencms.ui.dialogs.permissions.CmsPermissionView.PermissionChangeHandler#deletePermissionSet(java.lang.String, java.lang.String)
     */
    public void deletePermissionSet(String principalType, String principalName) {

        CmsPermissionBean bean = new CmsPermissionBean(principalType, principalName);
        m_permissionToChange.remove(bean);
        m_permissionToChange.add(bean);
        refreshOwnEntries();
    }

    /**
     * @see org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.I_PrincipalSelectHandler#onPrincipalSelect(java.lang.String, java.lang.String)
     */
    public void onPrincipalSelect(String principalType, String principalName) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(principalName)) {
            String permissionString = "";
            if (m_resource.isFolder()) {
                permissionString = "+i";
            }

            try {
                // lock resource if autolock is enabled
                ensureLock();
                CmsPermissionBean bean = null;
                if (principalName.equals(CmsVaadinUtils.getMessageText(Messages.GUI_LABEL_ALLOTHERS_0))) {
                    bean = new CmsPermissionBean(
                        "Unknown",
                        CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
                        permissionString);
                    bean.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS);

                } else if (principalName.equals(CmsVaadinUtils.getMessageText(Messages.GUI_LABEL_OVERWRITEALL_0))) {
                    bean = new CmsPermissionBean(
                        "Unknown",
                        CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME,
                        permissionString);
                    bean.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL);

                } else {
                    if (principalType.equalsIgnoreCase(CmsRole.PRINCIPAL_ROLE)) {
                        // if role, first check if we have to translate the role name
                        CmsRole role = CmsRole.valueOfRoleName(principalName);
                        if (role == null) {
                            // we need translation
                            Iterator<CmsRole> it = CmsRole.getSystemRoles().iterator();
                            while (it.hasNext()) {
                                role = it.next();
                                if (role.getName(getLocale()).equalsIgnoreCase(principalName)) {
                                    principalName = role.getRoleName();
                                    break;
                                }
                            }
                        }
                    }
                    bean = new CmsPermissionBean(principalType, principalName, permissionString);
                }
                m_permissionToChange.remove(bean);
                m_permissionToChange.add(bean);
                refreshOwnEntries();
                onViewChange();

            } catch (CmsException e) {
                m_context.error(e);
            }
        }
    }

    /**
     * @see org.opencms.ui.dialogs.permissions.CmsPermissionView.PermissionChangeHandler#onViewChange()
     */
    public void onViewChange() {

        CmsVaadinUtils.centerWindow(m_main);
    }

    /**
     * @see org.opencms.ui.dialogs.permissions.CmsPermissionView.PermissionChangeHandler#setPermissions(org.opencms.ui.dialogs.permissions.CmsPermissionBean)
     */
    public void setPermissions(CmsPermissionBean bean) {

        m_permissionToChange.remove(bean);
        m_permissionToChange.add(bean);

    }

    /**
     * Creates an HTML input form for the current access control entry.<p>
     *
     * @param entry the current access control entry
     * @param editable boolean to determine if the form is editable
     * @param extendedView boolean to determine if the view is selectable with DHTML
     * @param inheritRes the resource name from which the ace is inherited
     * @return StringBuffer with HTML code of the form
     */
    protected CmsPermissionView buildPermissionEntryForm(
        CmsAccessControlEntry entry,
        boolean editable,
        boolean extendedView,
        CmsUUID inheritRes) {

        return new CmsPermissionView(
            entry,
            editable,
            m_resource.isFolder(),
            inheritRes == null ? null : m_parents.get(inheritRes),
            this);
    }

    /**
     * Returns the resource on which the specified access control entry was set.<p>
     *
     * @param entry the current access control entry
     * @param parents the parent resources to determine the connected resource
     * @return the resource name of the corresponding resource
     */
    protected String getConnectedResource(CmsAccessControlEntry entry, Map<CmsUUID, String> parents) {

        CmsUUID resId = entry.getResource();
        String resName = parents.get(resId);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resName)) {
            return resName;
        }
        return resId.toString();
    }

    /**
     * Closes the dialog.<p>
     */
    void close() {

        try {
            clearLock();
            m_context.finish(Collections.singletonList(m_resource.getStructureId()));
        } catch (CmsException e) {
            m_context.error(e);
        }
    }

    /**
     * Displays the inherited permissions.<p>
     */
    void displayInheritedPermissions() {

        // store all parent folder ids together with path in a map
        m_parents = new HashMap<CmsUUID, String>();
        String sitePath = m_cms.getSitePath(m_resource);
        String path = CmsResource.getParentFolder(sitePath);
        List<CmsResource> parentResources = new ArrayList<CmsResource>();
        try {
            // get all parent folders of the current file
            parentResources = m_cms.readPath(path, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        Iterator<CmsResource> k = parentResources.iterator();
        while (k.hasNext()) {
            // add the current folder to the map
            CmsResource curRes = k.next();
            m_parents.put(curRes.getResourceId(), curRes.getRootPath());
        }

        ArrayList<CmsAccessControlEntry> inheritedEntries = new ArrayList<CmsAccessControlEntry>();
        try {
            Iterator<CmsAccessControlEntry> itAces = m_cms.getAccessControlEntries(path, true).iterator();
            while (itAces.hasNext()) {
                CmsAccessControlEntry curEntry = itAces.next();
                inheritedEntries.add(curEntry);
            }
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
        }
        addEntryTableToLayout(inheritedEntries, m_inheritedPermissions, false, true);

        //        buildInheritedList(inheritedEntries, parents);
    }

    /**
     * Displays the resource permissions.<p>
     */
    void displayResourcePermissions() {

        refreshOwnEntries();
    }

    /**
     * Displays the user permissions.<p>
     *
     * @param user the selected user
     */
    void displayUserPermissions(CmsUser user) {

        CmsPermissionView view = buildPermissionEntryForm(user.getId(), buildPermissionsForCurrentUser(), false, false);
        view.hideDeniedColumn();
        m_userPermissions.addComponent(view);
    }

    /**
     * Saves the permission.<p>
     */
    void savePermissions() {

        try {
            ensureLock();
            for (CmsPermissionBean bean : m_permissionToChange) {
                if (bean.isDeleted()) {
                    m_cms.rmacc(m_cms.getSitePath(m_resource), bean.getPrincipalType(), bean.getPrincipalName());

                } else {
                    if (bean.getPermissionString() == null) {
                        m_cms.chacc(
                            m_cms.getSitePath(m_resource),
                            bean.getPrincipalType(),
                            bean.getPrincipalName(),
                            bean.getAllowed(),
                            bean.getDenied(),
                            bean.getFlags());
                    } else {
                        m_cms.chacc(
                            m_cms.getSitePath(m_resource),
                            bean.getPrincipalType(),
                            bean.getPrincipalName(),
                            bean.getPermissionString());
                    }
                }
            }
        } catch (CmsException e) {
            m_context.error(e);
        }
    }

    /**
     * Adds list of entries to layout.<p>
     *
     * @param entries the ace list
     * @param layout layout
     * @param editable boolean
     * @param inheritedRes boolean
     */
    private void addEntryTableToLayout(
        List<CmsAccessControlEntry> entries,
        VerticalLayout layout,
        boolean editable,
        boolean inheritedRes) {

        final CmsPermissionViewTable table = new CmsPermissionViewTable(
            m_cms,
            entries,
            editable,
            inheritedRes,
            m_parents,
            this);
        HorizontalLayout hl = new HorizontalLayout();
        Label label = new Label(
            CmsVaadinUtils.getMessageText(
                Messages.GUI_PERMISSION_COUNT_1,
                Integer.valueOf(table.getContainerDataSource().size())));
        label.addStyleName("o-report");
        hl.addComponent(label);
        TextField tableFilter = new TextField();
        tableFilter.setIcon(FontOpenCms.FILTER);
        tableFilter.setInputPrompt(CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_EXPLORER_FILTER_0));
        tableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        tableFilter.setWidth("200px");
        tableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                table.filterTable(event.getText());
            }
        });
        hl.addComponent(tableFilter);
        hl.setWidth("100%");
        hl.setExpandRatio(label, 1);
        hl.setMargin(true);
        hl.setComponentAlignment(tableFilter, com.vaadin.ui.Alignment.MIDDLE_RIGHT);
        if (table.getContainerDataSource().size() == 0) {
            layout.addComponent(CmsVaadinUtils.getInfoLayout(Messages.GUI_PERMISSION_EMPTY_0));
        } else {
            layout.addComponent(hl);
            layout.addComponent(table);
            CmsVaadinUtils.centerWindow(this);
        }
    }

    /**
     * @see #buildPermissionEntryForm(CmsAccessControlEntry, boolean, boolean, CmsUUID)
     *
     * @param id the UUID of the principal of the permission set
     * @param curSet the current permission set
     * @param editable boolean to determine if the form is editable
     * @param extendedView boolean to determine if the view is selectable with DHTML
     * @return String with HTML code of the form
     */
    private CmsPermissionView buildPermissionEntryForm(
        CmsUUID id,
        CmsPermissionSet curSet,
        boolean editable,
        boolean extendedView) {

        String fileName = m_cms.getSitePath(m_resource);
        int flags = 0;
        try {
            I_CmsPrincipal p;
            try {
                p = CmsPrincipal.readPrincipalIncludingHistory(m_cms, id);
            } catch (CmsException e) {
                p = null;
            }
            if ((p != null) && p.isGroup()) {
                flags = CmsAccessControlEntry.ACCESS_FLAGS_GROUP;
            } else if ((p != null) && p.isUser()) {
                flags = CmsAccessControlEntry.ACCESS_FLAGS_USER;
            } else if ((p == null) && id.equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID)) {
                flags = CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS;
            } else if ((p == null) && id.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID)) {
                flags = CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL;
            } else {
                // check if it is the case of a role
                CmsRole role = CmsRole.valueOfId(id);
                if (role != null) {
                    flags = CmsAccessControlEntry.ACCESS_FLAGS_ROLE;
                }
            }

            CmsResource res = m_cms.readResource(fileName, CmsResourceFilter.ALL);
            CmsAccessControlEntry entry = new CmsAccessControlEntry(res.getResourceId(), id, curSet, flags);
            return buildPermissionEntryForm(entry, editable, extendedView, null);
        } catch (CmsException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
            return null;
        }
    }

    /**
     * Returns the actual real permissions (including role, and any other special check) for the current user.<p>
     *
     * @return the actual real permissions for the current user
     */
    private CmsPermissionSet buildPermissionsForCurrentUser() {

        CmsResourceUtil resUtil = new CmsResourceUtil(m_cms, m_resource);
        return resUtil.getPermissionSet();
    }

    /**
     * Clears the resource lock.<p>
     *
     * @throws CmsException in case reading the resource fails
     */
    private void clearLock() throws CmsException {

        if ((m_lockActionRecord != null) && (m_lockActionRecord.getChange() == LockChange.locked)) {
            CmsResource updatedRes = m_cms.readResource(m_resource.getStructureId(), CmsResourceFilter.ALL);
            try {
                m_cms.unlockResource(updatedRes);
            } catch (CmsLockException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
            m_lockActionRecord = null;
        }
    }

    /**
     * Locks the current resource.<p>
     *
     * @throws CmsException in case locking the resource fails
     */
    private void ensureLock() throws CmsException {

        if (m_lockActionRecord == null) {
            m_lockActionRecord = CmsLockUtil.ensureLock(m_cms, m_resource, true);
        }
    }

    /**
     * Refreshes the display of the resource permission display.<p>
     */
    @SuppressWarnings("unchecked")
    private void refreshOwnEntries() {

        m_resourcePermissions.removeAllComponents();
        String sitePath = m_cms.getSitePath(m_resource);

        // create new ArrayLists in which inherited and non inherited entries are stored
        ArrayList<CmsAccessControlEntry> ownEntries = new ArrayList<CmsAccessControlEntry>();
        try {
            Iterator<CmsAccessControlEntry> itAces = m_cms.getAccessControlEntries(sitePath, false).iterator();
            HashSet<CmsPermissionBean> newBeans = (HashSet<CmsPermissionBean>)((HashSet<CmsPermissionBean>)(m_permissionToChange)).clone();
            while (itAces.hasNext()) {
                CmsAccessControlEntry curEntry = itAces.next();
                if (!curEntry.isInherited()) {
                    // add the entry to the own rights list
                    CmsPermissionBean bean = CmsPermissionBean.getBeanForPrincipal(
                        m_permissionToChange,
                        CmsPermissionBean.getPrincipalNameFromACE(m_cms, curEntry));
                    if (bean == null) {
                        ownEntries.add(curEntry);
                    } else {
                        if (!bean.isDeleted()) {
                            CmsAccessControlEntry entry = bean.toAccessControlEntry(m_cms, m_resource.getStructureId());
                            if (entry != null) {
                                ownEntries.add(entry);
                            }
                        }
                        //No new entry -> remove from new list
                        newBeans.remove(bean);
                    }
                }
            }
            for (CmsPermissionBean newBean : newBeans) {
                if (!newBean.isDeleted()) {
                    CmsAccessControlEntry entry = newBean.toAccessControlEntry(m_cms, m_resource.getStructureId());
                    if (entry != null) {
                        ownEntries.add(entry);
                    }
                }
            }
        } catch (CmsException e) {
            // can usually be ignored
        }

        addEntryTableToLayout(ownEntries, m_resourcePermissions, true, false);
    }
}
