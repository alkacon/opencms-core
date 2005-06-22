/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsProjectsList.java,v $
 * Date   : $Date: 2005/06/22 14:33:36 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.projects;

import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Main project management view.<p>
 * 
 * @author Michael Moossen  
 * @version $Revision: 1.10 $
 * @since 5.7.3
 */
public class CmsProjectsList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "ad";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_FILES = "af";

    /** list action id constant. */
    public static final String LIST_ACTION_LOCK = "al";

    /** list action id constant. */
    public static final String LIST_ACTION_PUBLISH = "ap";

    /** list action id constant. */
    public static final String LIST_ACTION_PUBLISH_ENABLED = "ape";

    /** list action id constant. */
    public static final String LIST_ACTION_PUBLISH_DISABLED = "apd";

    /** list action id constant. */
    public static final String LIST_ACTION_UNLOCK = "au";

    /** list column id constant. */
    public static final String LIST_COLUMN_CREATION = "cc";

    /** list column id constant. */
    public static final String LIST_COLUMN_DELETE = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_DESCRIPTION = "cr";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** list column id constant. */
    public static final String LIST_COLUMN_FILES = "cf";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOCK = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_MANAGER = "cm";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_OWNER = "co";

    /** list column id constant. */
    public static final String LIST_COLUMN_PUBLISH = "cp";

    /** list column id constant. */
    public static final String LIST_COLUMN_USER = "cu";

    /** list action id constant. */
    public static final String LIST_DEFACTION_FILES = "df";

    /** list detail constant. */
    public static final String LIST_DETAIL_RESOURCES = "dr";

    /** list id constant. */
    public static final String LIST_ID = "lp";

    /** list action id constant. */
    public static final String LIST_MACTION_DELETE = "md";

    /** list action id constant. */
    public static final String LIST_MACTION_UNLOCK = "mu";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/projects/buttons/";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsProjectsList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_PROJECTS_LIST_NAME_0),
            LIST_COLUMN_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsProjectsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Deletes the project and closes the dialog.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void actionDeleteProject() throws Exception {

        String pId = getJsp().getRequest().getParameter(CmsEditProjectDialog.PARAM_PROJECTID);
        getCms().deleteProject(new Integer(pId).intValue());
        refreshList();
        actionCloseDialog();
    }

    /**
     * This method should handle every defined list multi action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     * 
     * @throws CmsRuntimeException to signal that an action is not supported
     * 
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        if (getParamListAction().equals(LIST_MACTION_DELETE)) {
            // execute the delete multiaction
            List removedItems = new ArrayList();
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    int pId = new Integer(listItem.getId()).intValue();
                    getCms().deleteProject(pId);
                    removedItems.add(listItem.getId());
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_SELECTED_PROJECTS_0), e);
            } finally {
                getList().removeAllItems(removedItems, getLocale());
            }
        } else if (getParamListAction().equals(LIST_MACTION_UNLOCK)) {
            // execute the unlock multiaction
            try {
                Iterator itItems = getSelectedItems().iterator();
                while (itItems.hasNext()) {
                    CmsListItem listItem = (CmsListItem)itItems.next();
                    int pId = new Integer(listItem.getId()).intValue();
                    getCms().unlockProject(pId);
                }
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_UNLOCK_SELECTED_PROJECTS_0), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        Integer projectId = new Integer(getSelectedItem().getId());
        String projectName = getSelectedItem().get(LIST_COLUMN_NAME).toString();

        Map params = new HashMap();
        params.put(CmsEditProjectDialog.PARAM_PROJECTNAME, projectName);
        params.put(CmsEditProjectDialog.PARAM_PROJECTID, projectId.toString());
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

        if (getParamListAction().equals(LIST_DEFACTION_FILES)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/projects/files", params);
        } else if (getParamListAction().equals(LIST_ACTION_EDIT)) {
            getToolManager().jspForwardTool(this, "/projects/edit", params);
        } else if (getParamListAction().equals(LIST_ACTION_FILES)) {
            getToolManager().jspForwardTool(this, "/projects/files", params);
        } else if (getParamListAction().equals(LIST_ACTION_PUBLISH_ENABLED)) {
            getToolManager().jspForwardTool(this, "/projects/publish", params);
        } else if (getParamListAction().equals(LIST_ACTION_DELETE)) {
            // execute the delete action
            try {
                getCms().deleteProject(projectId.intValue());
                getList().removeItem(projectId.toString(), getLocale());
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_DELETE_PROJECT_1, projectName), e);
            }
        } else if (getParamListAction().equals(LIST_ACTION_LOCK)) {
            // noop
        } else if (getParamListAction().equals(LIST_ACTION_UNLOCK)) {
            // execute the unlock action
            try {
                getCms().unlockProject(projectId.intValue());
            } catch (CmsException e) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_UNLOCK_PROJECT_1, projectName), e);
            }
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        List projects = getList().getAllContent();
        Iterator itProjects = projects.iterator();
        while (itProjects.hasNext()) {
            CmsListItem item = (CmsListItem)itProjects.next();
            try {
                if (detailId.equals(LIST_DETAIL_RESOURCES)) {
                    CmsProject project = getCms().readProject(new Integer(item.getId()).intValue());
                    StringBuffer html = new StringBuffer(512);
                    Iterator resources = getCms().readProjectResources(project).iterator();
                    while (resources.hasNext()) {
                        html.append(resources.next().toString());
                        html.append("<br>");
                    }
                    item.set(LIST_DETAIL_RESOURCES, html.toString());
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();
        // get content
        List projects = getCms().getAllManageableProjects();
        Iterator itProjects = projects.iterator();
        while (itProjects.hasNext()) {
            CmsProject project = (CmsProject)itProjects.next();
            CmsListItem item = getList().newItem(new Integer(project.getId()).toString());
            item.set(LIST_COLUMN_NAME, project.getName());
            item.set(LIST_COLUMN_DESCRIPTION, project.getDescription());
            try {
                item.set(LIST_COLUMN_OWNER, getCms().readUser(project.getOwnerId()).getName());
            } catch (Exception e) {
                // ignore
            }
            try {
                item.set(LIST_COLUMN_MANAGER, getCms().readGroup(project.getManagerGroupId()).getName());
            } catch (Exception e) {
                // ignore
            }
            try {
                item.set(LIST_COLUMN_USER, getCms().readGroup(project.getGroupId()).getName());
            } catch (Exception e) {
                // ignore
            }
            item.set(LIST_COLUMN_CREATION, new Date(project.getDateCreated()));
            StringBuffer html = new StringBuffer(512);
            Iterator resources = getCms().readProjectResources(project).iterator();
            while (resources.hasNext()) {
                html.append(resources.next().toString());
                html.append("<br>");
            }
            item.set(LIST_DETAIL_RESOURCES, html.toString());
            ret.add(item);
        }

        return ret;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // create column for files
        CmsListColumnDefinition filesCol = new CmsListColumnDefinition(LIST_COLUMN_FILES);
        filesCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_FILES_0));
        filesCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_FILES_HELP_0));
        filesCol.setWidth("20");
        filesCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        filesCol.setSorteable(false);
        // add files action
        CmsListDirectAction filesAction = new CmsListDirectAction(LIST_ACTION_FILES);
        filesAction.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_FILES_NAME_0));
        filesAction.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_FILES_HELP_0));
        filesAction.setIconPath(PATH_BUTTONS + "project.png");
        filesCol.addDirectAction(filesAction);
        // add it to the list definition
        metadata.addColumn(filesCol);

        // create column for lock/unlock
        CmsListColumnDefinition lockCol = new CmsListColumnDefinition(LIST_COLUMN_LOCK);
        lockCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_LOCK_0));
        lockCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_LOCK_HELP_0));
        lockCol.setWidth("20");
        lockCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        lockCol.setListItemComparator(new CmsListItemActionIconComparator());
        // lock action
        CmsListDirectAction lockAction = new CmsListDirectAction(LIST_ACTION_LOCK);
        lockAction.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_LOCK_NAME_0));
        lockAction.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_LOCK_HELP_0));
        lockAction.setConfirmationMessage(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_LOCK_CONF_0));
        lockAction.setIconPath(PATH_BUTTONS + "project_lock.png");
        lockAction.setEnabled(false);
        // unlock action
        CmsListDirectAction unlockAction = new CmsListDirectAction(LIST_ACTION_UNLOCK);
        unlockAction.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_UNLOCK_NAME_0));
        unlockAction.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_UNLOCK_HELP_0));
        unlockAction.setConfirmationMessage(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_UNLOCK_CONF_0));
        unlockAction.setIconPath(PATH_BUTTONS + "project_unlock.png");
        // adds a lock/unlock direct action
        CmsProjectLockAction projectAction = new CmsProjectLockAction(LIST_ACTION_LOCK, getCms());
        projectAction.setFirstAction(lockAction);
        projectAction.setSecondAction(unlockAction);
        lockCol.addDirectAction(projectAction);
        // add it to the list definition
        metadata.addColumn(lockCol);

        // create column for publishing
        CmsListColumnDefinition publishCol = new CmsListColumnDefinition(LIST_COLUMN_PUBLISH);
        publishCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_PUBLISH_0));
        publishCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_PUBLISH_HELP_0));
        publishCol.setWidth("20");
        publishCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        publishCol.setSorteable(false);
        // publish enabled action
        CmsListDirectAction publishEnabledAction = new CmsListDirectAction(LIST_ACTION_PUBLISH_ENABLED);
        publishEnabledAction.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_PUBLISH_ENABLED_NAME_0));
        publishEnabledAction.setHelpText(Messages.get().container(
            Messages.GUI_PROJECTS_LIST_ACTION_PUBLISH_ENABLED_HELP_0));
        publishEnabledAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_PROJECTS_LIST_ACTION_PUBLISH_ENABLED_CONF_0));
        publishEnabledAction.setIconPath(PATH_BUTTONS + "project_publish.png");
        // publish disabled action
        CmsListDirectAction publishDisabledAction = new CmsListDirectAction(LIST_ACTION_PUBLISH_DISABLED);
        publishDisabledAction.setName(Messages.get().container(
            Messages.GUI_PROJECTS_LIST_ACTION_PUBLISH_DISABLED_NAME_0));
        publishDisabledAction.setHelpText(Messages.get().container(
            Messages.GUI_PROJECTS_LIST_ACTION_PUBLISH_DISABLED_HELP_0));
        publishDisabledAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_PROJECTS_LIST_ACTION_PUBLISH_DISABLED_CONF_0));
        publishDisabledAction.setIconPath(PATH_BUTTONS + "project_publish_disabled.png");
        publishDisabledAction.setEnabled(false);
        // adds a publish enabled/disabled direct action
        CmsPublishProjectAction publishAction = new CmsPublishProjectAction(LIST_ACTION_PUBLISH, getCms());
        publishAction.setFirstAction(publishEnabledAction);
        publishAction.setSecondAction(publishDisabledAction);
        publishCol.addDirectAction(publishAction);
        // add it to the list definition
        metadata.addColumn(publishCol);

        // create column for edition
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_EDIT_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);
        // add edit action
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_EDIT_HELP_0));
        editAction.setIconPath(PATH_BUTTONS + "project_edit.png");
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // create column for deletion
        CmsListColumnDefinition deleteCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        deleteCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_DELETE_0));
        deleteCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_DELETE_HELP_0));
        deleteCol.setWidth("20");
        deleteCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        deleteCol.setSorteable(false);
        // add delete action
        CmsListDirectAction deleteAction = new CmsListDirectAction(LIST_ACTION_DELETE);
        deleteAction.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_DELETE_NAME_0));
        deleteAction.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_DELETE_HELP_0));
        deleteAction.setConfirmationMessage(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_DELETE_CONF_0));
        deleteAction.setIconPath(ICON_DELETE);
        deleteCol.addDirectAction(deleteAction);
        // add it to the list definition
        metadata.addColumn(deleteCol);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_NAME_0));
        nameCol.setWidth("15%");
        // create default edit action
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(LIST_DEFACTION_FILES);
        defEditAction.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_DEFACTION_EDIT_NAME_0));
        defEditAction.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_DEFACTION_EDIT_HELP_0));
        nameCol.setDefaultAction(defEditAction);
        // add it to the list definition
        metadata.addColumn(nameCol);

        // add column for description
        CmsListColumnDefinition descriptionCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descriptionCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_DESCRIPTION_0));
        descriptionCol.setWidth("35%");
        descriptionCol.setTextWrapping(true);
        metadata.addColumn(descriptionCol);

        // add column for owner user
        CmsListColumnDefinition ownerCol = new CmsListColumnDefinition(LIST_COLUMN_OWNER);
        ownerCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_OWNER_0));
        ownerCol.setWidth("12%");
        metadata.addColumn(ownerCol);

        // add column for manager group
        CmsListColumnDefinition managerCol = new CmsListColumnDefinition(LIST_COLUMN_MANAGER);
        managerCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_MANAGER_0));
        managerCol.setWidth("12%");
        metadata.addColumn(managerCol);

        // add column for user group
        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_USER_0));
        userCol.setWidth("12%");
        metadata.addColumn(userCol);

        // add column for creation date
        CmsListColumnDefinition creationCol = new CmsListColumnDefinition(LIST_COLUMN_CREATION);
        creationCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_CREATION_0));
        creationCol.setWidth("14%");
        CmsListDateMacroFormatter creationDateFormatter = new CmsListDateMacroFormatter(Messages.get().container(
            Messages.GUI_PROJECTS_LIST_COLS_CREATION_FORMAT_1), Messages.get().container(
            Messages.GUI_PROJECTS_LIST_COLS_CREATION_NEVER_0));
        creationCol.setFormatter(creationDateFormatter);
        metadata.addColumn(creationCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add publishing info details
        CmsListItemDetails resourcesDetails = new CmsListItemDetails(LIST_DETAIL_RESOURCES);
        resourcesDetails.setAtColumn(LIST_COLUMN_NAME);
        resourcesDetails.setVisible(false);
        resourcesDetails.setShowActionName(Messages.get().container(Messages.GUI_PROJECTS_DETAIL_SHOW_RESOURCES_NAME_0));
        resourcesDetails.setShowActionHelpText(Messages.get().container(
            Messages.GUI_PROJECTS_DETAIL_SHOW_RESOURCES_HELP_0));
        resourcesDetails.setHideActionName(Messages.get().container(Messages.GUI_PROJECTS_DETAIL_HIDE_RESOURCES_NAME_0));
        resourcesDetails.setHideActionHelpText(Messages.get().container(
            Messages.GUI_PROJECTS_DETAIL_HIDE_RESOURCES_HELP_0));
        resourcesDetails.setName(Messages.get().container(Messages.GUI_PROJECTS_DETAIL_RESOURCES_NAME_0));
        resourcesDetails.setFormatter(new CmsListItemDetailsFormatter(Messages.get().container(
            Messages.GUI_PROJECTS_DETAIL_RESOURCES_NAME_0)));
        metadata.addItemDetails(resourcesDetails);

        // makes the list searchable
        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_NAME));
        searchAction.addColumn(metadata.getColumnDefinition(LIST_COLUMN_DESCRIPTION));
        metadata.setSearchAction(searchAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add the unlock project multi action
        CmsListMultiAction unlockProject = new CmsListMultiAction(LIST_MACTION_UNLOCK);
        unlockProject.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_MACTION_UNLOCK_NAME_0));
        unlockProject.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_MACTION_UNLOCK_HELP_0));
        unlockProject.setConfirmationMessage(Messages.get().container(Messages.GUI_PROJECTS_LIST_MACTION_UNLOCK_CONF_0));
        unlockProject.setIconPath(PATH_BUTTONS + "project_unlock.png");
        metadata.addMultiAction(unlockProject);

        // add delete multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETE);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_MACTION_DELETE_NAME_0));
        deleteMultiAction.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_MACTION_DELETE_HELP_0));
        deleteMultiAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_PROJECTS_LIST_MACTION_DELETE_CONF_0));
        deleteMultiAction.setIconPath(ICON_MULTI_DELETE);
        metadata.addMultiAction(deleteMultiAction);
    }
}