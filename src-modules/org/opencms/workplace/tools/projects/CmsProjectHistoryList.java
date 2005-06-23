/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsProjectHistoryList.java,v $
 * Date   : $Date: 2005/06/23 09:05:01 $
 * Version: $Revision: 1.6 $
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

import org.opencms.file.CmsBackupProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Main project management view.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsProjectHistoryList extends A_CmsListDialog {

    /** list action constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list column id constant. */
    public static final String LIST_COLUMN_CREATION = "cc";

    /** list column id constant. */
    public static final String LIST_COLUMN_DESCRIPTION = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_MANAGER = "cm";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_OWNER = "co";

    /** list column id constant. */
    public static final String LIST_COLUMN_PUBLISHED_BY = "cb";

    /** list column id constant. */
    public static final String LIST_COLUMN_PUBLISHED_DATE = "cp";

    /** list column id constant. */
    public static final String LIST_COLUMN_USER = "cu";

    /** list detail constant. */
    public static final String LIST_DETAIL_RESOURCES = "dr";

    /** list id constant. */
    public static final String LIST_ID = "lph";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsProjectHistoryList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_PROJECTHISTORY_LIST_NAME_0),
            LIST_COLUMN_PUBLISHED_DATE,
            CmsListOrderEnum.ORDER_DESCENDING,
            null);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsProjectHistoryList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
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

        throwListUnsupportedActionException();
    }

    /**
     * This method should handle every defined list single action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     * 
     * @throws CmsRuntimeException to signal that an action is not supported or in case an action failed
     */
    public void executeListSingleActions() throws CmsRuntimeException {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // get content
        List projects = getList().getAllContent();
        Iterator itProjects = projects.iterator();
        while (itProjects.hasNext()) {
            CmsListItem item = (CmsListItem)itProjects.next();
            try {
                if (detailId.equals(LIST_DETAIL_RESOURCES)) {
                    CmsBackupProject project = getCms().readBackupProject(new Integer(item.getId()).intValue());
                    StringBuffer html = new StringBuffer(512);
                    Iterator resources = project.getProjectResources().iterator();
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
        List projects = getCms().getAllBackupProjects();
        Iterator itProjects = projects.iterator();
        while (itProjects.hasNext()) {
            CmsBackupProject project = (CmsBackupProject)itProjects.next();
            CmsListItem item = getList().newItem(new Integer(project.getId()).toString());
            item.set(LIST_COLUMN_NAME, project.getName());
            item.set(LIST_COLUMN_DESCRIPTION, project.getDescription());
            try {
                item.set(LIST_COLUMN_OWNER, project.getOwnerName());
            } catch (Exception e) {
                // ignore
            }
            try {
                item.set(LIST_COLUMN_MANAGER, project.getManagerGroupName());
            } catch (Exception e) {
                // ignore
            }
            try {
                item.set(LIST_COLUMN_USER, project.getGroupName());
            } catch (Exception e) {
                // ignore
            }
            try {
                item.set(LIST_COLUMN_PUBLISHED_DATE, new Date(project.getPublishingDate()));
            } catch (Exception e) {
                // ignore
            }
            try {
                item.set(LIST_COLUMN_PUBLISHED_BY, project.getPublishedByName());
            } catch (Exception e) {
                // ignore
            }
            item.set(LIST_COLUMN_CREATION, new Date(project.getDateCreated()));
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

        // create column for icon
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_ICON_0));
        iconCol.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_ICON_HELP_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);
        // add files action
        CmsListDirectAction iconAction = new CmsListDirectAction(LIST_ACTION_ICON);
        iconAction.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_ICON_NAME_0));
        iconAction.setHelpText(Messages.get().container(Messages.GUI_PROJECTS_LIST_ACTION_ICON_HELP_0));
        iconAction.setIconPath(CmsProjectsList.PATH_BUTTONS + "project.png");
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        // add it to the list definition
        metadata.addColumn(iconCol);

        // create column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_NAME_0));
        nameCol.setWidth("10%");
        // add it to the list definition
        metadata.addColumn(nameCol);

        // add column for description
        CmsListColumnDefinition descriptionCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descriptionCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_DESCRIPTION_0));
        descriptionCol.setWidth("30%");
        descriptionCol.setTextWrapping(true);
        metadata.addColumn(descriptionCol);

        // add column for published date
        CmsListColumnDefinition publishingDateCol = new CmsListColumnDefinition(LIST_COLUMN_PUBLISHED_DATE);
        publishingDateCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_PUBLISHED_DATE_0));
        publishingDateCol.setWidth("10%");
        CmsListDateMacroFormatter publishingDateFormatter = new CmsListDateMacroFormatter(Messages.get().container(
            Messages.GUI_PROJECTS_LIST_COLS_PUBLISHING_FORMAT_1), Messages.get().container(
            Messages.GUI_PROJECTS_LIST_COLS_PUBLISHING_NEVER_0));
        publishingDateCol.setFormatter(publishingDateFormatter);
        metadata.addColumn(publishingDateCol);

        // add column for published by
        CmsListColumnDefinition publishedByCol = new CmsListColumnDefinition(LIST_COLUMN_PUBLISHED_BY);
        publishedByCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_PUBLISHED_BY_0));
        publishedByCol.setWidth("10%");
        metadata.addColumn(publishedByCol);

        // add column for owner user
        CmsListColumnDefinition ownerCol = new CmsListColumnDefinition(LIST_COLUMN_OWNER);
        ownerCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_OWNER_0));
        ownerCol.setWidth("10%");
        metadata.addColumn(ownerCol);

        // add column for manager group
        CmsListColumnDefinition managerCol = new CmsListColumnDefinition(LIST_COLUMN_MANAGER);
        managerCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_MANAGER_0));
        managerCol.setWidth("10%");
        metadata.addColumn(managerCol);

        // add column for user group
        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_USER_0));
        userCol.setWidth("10%");
        metadata.addColumn(userCol);

        // add column for creation date
        CmsListColumnDefinition creationCol = new CmsListColumnDefinition(LIST_COLUMN_CREATION);
        creationCol.setName(Messages.get().container(Messages.GUI_PROJECTS_LIST_COLS_CREATION_0));
        creationCol.setWidth("10%");
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

        //noop
    }

}
