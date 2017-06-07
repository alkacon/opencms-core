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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.searchindex.sourcesearch;

import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Explorer dialog for the content search result list.<p>
 *
 * @since 7.5.3
 */
public class CmsSourceSearchFilesDialog extends A_CmsListExplorerDialog {

    /** list column id constant. */
    public static final String LIST_COLUMN_FILES = "cf";

    /** list independent action constant. */
    public static final String LIST_IACTION_FILTER = "iaf";

    /** list id constant. */
    public static final String LIST_ID = "lcs";

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /** The content sraech file list. */
    private Collection<CmsResource> m_files;

    /** Stores the value of the request parameter for the project id. */
    private String m_paramProjectid;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSourceSearchFilesDialog(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_SOURCESEARCH_FILES_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSourceSearchFilesDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListIndepActions()
     */
    @Override
    public void executeListIndepActions() {

        if (getParamListAction().equals(LIST_IACTION_FILTER)) {
            // forward to the editor
            getList().setCurrentPage(1);
            m_collector = null;
            refreshList();
        } else {
            super.executeListIndepActions();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    @Override
    public I_CmsListResourceCollector getCollector() {

        m_collector = new CmsSourceSearchCollector(this);
        return m_collector;
    }

    /**
     * Gets the content search result list.<p>
     *
     * @return the content search result list
     */
    public Collection<CmsResource> getFiles() {

        return m_files;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getList()
     */
    @Override
    public CmsHtmlList getList() {

        return super.getList();
    }

    /**
     * Returns the project id parameter value.<p>
     *
     * @return the project id parameter value
     */
    public String getParamProjectid() {

        return m_paramProjectid;
    }

    /**
     * Returns an appropiate initialized resource util object for the given item.<p>
     *
     * @param item the item representing the resource
     *
     * @return a resource util object
     */
    @Override
    public CmsResourceUtil getResourceUtil(CmsListItem item) {

        CmsResourceUtil resUtil = getResourceUtil();
        resUtil.setResource(getCollector().getResource(getCms(), item));
        return resUtil;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#refreshList()
     */
    @Override
    public synchronized void refreshList() {

        if (LIST_IACTION_FILTER.equals(getParamListAction())) {
            if (m_collector != null) {
                // refresh only if really necessary
                return;
            }
        }
        super.refreshList();
    }

    /** Sets the content search result list.
     *
     * @param files the found files
     */
    public void seFiles(Collection<CmsResource> files) {

        m_files = files;
    }

    /**
     * Sets the project id parameter value.<p>
     *
     * @param projectId the project id parameter value
     */
    public void setParamProjectid(String projectId) {

        m_paramProjectid = projectId;
    }

    /**
     * Returns a list item created from the resource information, differs between valid resources and invalid resources.<p>
     *
     * @param resource the resource to create the list item from
     * @param list the list
     * @param showPermissions if to show permissions
     * @param showDateLastMod if to show the last modification date
     * @param showUserLastMod if to show the last modification user
     * @param showDateCreate if to show the creation date
     * @param showUserCreate if to show the creation date
     * @param showDateRel if to show the date released
     * @param showDateExp if to show the date expired
     * @param showState if to show the state
     * @param showLockedBy if to show the lock user
     * @param showSite if to show the site
     *
     * @return a list item created from the resource information
     */
    protected CmsListItem createResourceListItem(
        CmsResource resource,
        CmsHtmlList list,
        boolean showPermissions,
        boolean showDateLastMod,
        boolean showUserLastMod,
        boolean showDateCreate,
        boolean showUserCreate,
        boolean showDateRel,
        boolean showDateExp,
        boolean showState,
        boolean showLockedBy,
        boolean showSite) {

        CmsListItem item = list.newItem(resource.getStructureId().toString());
        // get an initialized resource utility
        CmsResourceUtil resUtil = getResourceUtil();
        resUtil.setResource(resource);
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_NAME, resUtil.getPath());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_ROOT_PATH, resUtil.getFullPath());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_TITLE, resUtil.getTitle());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_TYPE, resUtil.getResourceTypeName());
        item.set(A_CmsListExplorerDialog.LIST_COLUMN_SIZE, resUtil.getSizeString());
        if (showPermissions) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_PERMISSIONS, resUtil.getPermissionString());
        }
        if (showDateLastMod) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATELASTMOD, new Date(resource.getDateLastModified()));
        }
        if (showUserLastMod) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_USERLASTMOD, resUtil.getUserLastModified());
        }
        if (showDateCreate) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATECREATE, new Date(resource.getDateCreated()));
        }
        if (showUserCreate) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_USERCREATE, resUtil.getUserCreated());
        }
        if (showDateRel) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATEREL, new Date(resource.getDateReleased()));
        }
        if (showDateExp) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_DATEEXP, new Date(resource.getDateExpired()));
        }
        if (showState) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_STATE, resUtil.getStateName());
        }
        if (showLockedBy) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_LOCKEDBY, resUtil.getLockedByName());
        }
        if (showSite) {
            item.set(A_CmsListExplorerDialog.LIST_COLUMN_SITE, resUtil.getSiteTitle());
        }
        return item;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // no-details
    }

    /**
     * Returns a list of list items from a list of resources.<p>
     *
     * @return a list of {@link CmsListItem} objects
     */
    @Override
    protected List<CmsListItem> getListItems() {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        applyColumnVisibilities();
        CmsHtmlList list = getList();

        CmsListColumnDefinition colPermissions = list.getMetadata().getColumnDefinition(
            A_CmsListExplorerDialog.LIST_COLUMN_PERMISSIONS);
        boolean showPermissions = (colPermissions.isVisible() || colPermissions.isPrintable());
        CmsListColumnDefinition colDateLastMod = list.getMetadata().getColumnDefinition(
            A_CmsListExplorerDialog.LIST_COLUMN_DATELASTMOD);
        boolean showDateLastMod = (colDateLastMod.isVisible() || colDateLastMod.isPrintable());
        CmsListColumnDefinition colUserLastMod = list.getMetadata().getColumnDefinition(
            A_CmsListExplorerDialog.LIST_COLUMN_USERLASTMOD);
        boolean showUserLastMod = (colUserLastMod.isVisible() || colUserLastMod.isPrintable());
        CmsListColumnDefinition colDateCreate = list.getMetadata().getColumnDefinition(
            A_CmsListExplorerDialog.LIST_COLUMN_DATECREATE);
        boolean showDateCreate = (colDateCreate.isVisible() || colDateCreate.isPrintable());
        CmsListColumnDefinition colUserCreate = list.getMetadata().getColumnDefinition(
            A_CmsListExplorerDialog.LIST_COLUMN_USERCREATE);
        boolean showUserCreate = (colUserCreate.isVisible() || colUserCreate.isPrintable());
        CmsListColumnDefinition colDateRel = list.getMetadata().getColumnDefinition(
            A_CmsListExplorerDialog.LIST_COLUMN_DATEREL);
        boolean showDateRel = (colDateRel.isVisible() || colDateRel.isPrintable());
        CmsListColumnDefinition colDateExp = list.getMetadata().getColumnDefinition(
            A_CmsListExplorerDialog.LIST_COLUMN_DATEEXP);
        boolean showDateExp = (colDateExp.isVisible() || colDateExp.isPrintable());
        CmsListColumnDefinition colState = list.getMetadata().getColumnDefinition(
            A_CmsListExplorerDialog.LIST_COLUMN_STATE);
        boolean showState = (colState.isVisible() || colState.isPrintable());
        CmsListColumnDefinition colLockedBy = list.getMetadata().getColumnDefinition(
            A_CmsListExplorerDialog.LIST_COLUMN_LOCKEDBY);
        boolean showLockedBy = (colLockedBy.isVisible() || colLockedBy.isPrintable());
        CmsListColumnDefinition colSite = list.getMetadata().getColumnDefinition(
            A_CmsListExplorerDialog.LIST_COLUMN_SITE);
        boolean showSite = (colSite.isVisible() || colSite.isPrintable());

        // get content
        Iterator<CmsResource> itRes = m_files.iterator();
        while (itRes.hasNext()) {

            CmsResource resource = itRes.next();

            CmsListItem item = createResourceListItem(
                resource,
                list,
                showPermissions,
                showDateLastMod,
                showUserLastMod,
                showDateCreate,
                showUserCreate,
                showDateRel,
                showDateExp,
                showState,
                showLockedBy,
                showSite);

            ret.add(item);
        }
        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getProject()
     */
    @Override
    protected CmsProject getProject() {

        CmsUUID projectId = new CmsUUID(getParamProjectid());
        try {
            return getCms().readProject(projectId);
        } catch (CmsException e) {
            return super.getProject();
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        super.setIndependentActions(metadata);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no LMAs
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        try {
            getCms().readProject(new CmsUUID(getParamProjectid()));
        } catch (Exception e) {
            m_paramProjectid = getCms().getRequestContext().getCurrentProject().getUuid().toString();
        }

    }
}
