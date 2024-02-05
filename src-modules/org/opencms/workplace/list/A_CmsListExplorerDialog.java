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

package org.opencms.workplace.list;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsTouch;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Provides a list dialog for resources.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsListExplorerDialog extends A_CmsListDialog {

    /** List action id constant. */
    public static final String LIST_ACTION_EDIT = "eae";

    /** List action id constant. */
    public static final String LIST_ACTION_LOCKICON = "eal";

    /** List action id constant. */
    public static final String LIST_ACTION_PROJSTATEICON = "eaps";

    /** List action id constant. */
    public static final String LIST_ACTION_TYPEICON = "eai";

    /** List column id constant. */
    public static final String LIST_COLUMN_DATECREATE = "ecdc";

    /** List column id constant. */
    public static final String LIST_COLUMN_DATEEXP = "ecde";

    /** List column id constant. */
    public static final String LIST_COLUMN_DATELASTMOD = "ecdl";

    /** List column id constant. */
    public static final String LIST_COLUMN_DATEREL = "ecdr";

    /** List column id constant. */
    public static final String LIST_COLUMN_EDIT = "ece";

    /** List column id constant. */
    public static final String LIST_COLUMN_LOCKEDBY = "eclb";

    /** List column id constant. */
    public static final String LIST_COLUMN_LOCKICON = "ecli";

    /** List column id constant. */
    public static final String LIST_COLUMN_NAME = "ecn";

    /** List column id constant. */
    public static final String LIST_COLUMN_PERMISSIONS = "ecp";

    /** List column id constant. */
    public static final String LIST_COLUMN_PROJSTATEICON = "ecpi";

    /** List column id constant. */
    public static final String LIST_COLUMN_ROOT_PATH = "crp";

    /** List column id constant. */
    public static final String LIST_COLUMN_SITE = "ecsi";

    /** List column id constant. */
    public static final String LIST_COLUMN_SIZE = "ecz";

    /** List column id constant. */
    public static final String LIST_COLUMN_STATE = "ecs";

    /** List column id constant. */
    public static final String LIST_COLUMN_TITLE = "ect";

    /** List column id constant. */
    public static final String LIST_COLUMN_TYPE = "ecy";

    /** List column id constant. */
    public static final String LIST_COLUMN_TYPEICON = "ecti";

    /** List column id constant. */
    public static final String LIST_COLUMN_USERCREATE = "ecuc";

    /** List column id constant. */
    public static final String LIST_COLUMN_USERLASTMOD = "ecul";

    /** List default action id constant. */
    public static final String LIST_DEFACTION_OPEN = "edo";

    /** Request parameter name for the show explorer flag. */
    public static final String PARAM_SHOW_EXPLORER = "showexplorer";

    /** Explorer list JSP path. */
    public static final String PATH_EXPLORER_LIST = PATH_DIALOGS + "list-explorer.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsListExplorerDialog.class);

    /** Column visibility flags container. */
    private Map<Integer, Boolean> m_colVisibilities;

    /** Stores the value of the request parameter for the show explorer flag. */
    private String m_paramShowexplorer;

    /** Instance resource util. */
    private CmsResourceUtil m_resourceUtil;

    /**
     * Creates a new explorer list ordered and searchable by name.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the displayed list
     * @param listName the name of the list
     */
    protected A_CmsListExplorerDialog(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        this(jsp, listId, listName, LIST_COLUMN_NAME, CmsListOrderEnum.ORDER_ASCENDING, LIST_COLUMN_NAME);
    }

    /**
     * Default constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the displayed list
     * @param listName the name of the list
     * @param sortedColId the a priory sorted column
     * @param sortOrder the order of the sorted column
     * @param searchableColId the column to search into
     */
    protected A_CmsListExplorerDialog(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        String sortedColId,
        CmsListOrderEnum sortOrder,
        String searchableColId) {

        super(jsp, listId, listName, sortedColId, sortOrder, searchableColId);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListIndepActions()
     */
    @Override
    public void executeListIndepActions() {

        if (getParamListAction().equals(CmsListIndependentAction.ACTION_EXPLORER_SWITCH_ID)) {
            Map<String, String[]> params = new HashMap<String, String[]>();
            // set action parameter to initial dialog call
            params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
            params.putAll(getToolManager().getCurrentTool(this).getHandler().getParameters(this));

            getSettings().setCollector(getCollector());
            getSettings().setExplorerMode(CmsExplorer.VIEW_LIST);
            getSettings().setExplorerProjectId(getProject().getUuid());
            setShowExplorer(true);
            try {
                getToolManager().jspForwardPage(this, PATH_EXPLORER_LIST, params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            super.executeListIndepActions();
        }
    }

    /**
     * Returns the collector to use to display the resources.<p>
     *
     * @return the collector to use to display the resources
     */
    public abstract I_CmsListResourceCollector getCollector();

    /**
     * Returns the Show explorer parameter value.<p>
     *
     * @return the Show explorer parameter value
     */
    public String getParamShowexplorer() {

        return m_paramShowexplorer;
    }

    /**
     * Returns an appropiate initialized resource util object.<p>
     *
     * @return a resource util object
     */
    public CmsResourceUtil getResourceUtil() {

        if (m_resourceUtil == null) {
            try {
                m_resourceUtil = new CmsResourceUtil(OpenCms.initCmsObject(getCms()));
                m_resourceUtil.setReferenceProject(getProject());
            } catch (CmsException ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            }
        }
        return m_resourceUtil;
    }

    /**
     * Returns an appropiate initialized resource util object for the given item.<p>
     *
     * @param item the item representing the resource
     *
     * @return a resource util object
     */
    public CmsResourceUtil getResourceUtil(CmsListItem item) {

        CmsResourceUtil resUtil = getResourceUtil();
        resUtil.setResource(getCollector().getResource(getCms(), item));
        return resUtil;
    }

    /**
     * Sets the Show explorer parameter value.<p>
     *
     * @param showExplorer the Show explorer parameter value to set
     */
    public void setParamShowexplorer(String showExplorer) {

        m_paramShowexplorer = showExplorer;
    }

    /**
     * Applies the column visibilities.<p>
     */
    protected void applyColumnVisibilities() {

        setColumnVisibilities();
        CmsListMetadata metadata = getList().getMetadata();
        metadata.getColumnDefinition(LIST_COLUMN_SITE).setVisible(isColumnVisible(LIST_COLUMN_SITE.hashCode()));
        metadata.getColumnDefinition(LIST_COLUMN_EDIT).setVisible(isColumnVisible(LIST_COLUMN_EDIT.hashCode()));
        metadata.getColumnDefinition(LIST_COLUMN_TYPEICON).setVisible(isColumnVisible(LIST_COLUMN_TYPEICON.hashCode()));
        metadata.getColumnDefinition(LIST_COLUMN_LOCKICON).setVisible(isColumnVisible(LIST_COLUMN_LOCKICON.hashCode()));
        metadata.getColumnDefinition(LIST_COLUMN_PROJSTATEICON).setVisible(
            isColumnVisible(LIST_COLUMN_PROJSTATEICON.hashCode()));
        metadata.getColumnDefinition(LIST_COLUMN_NAME).setVisible(isColumnVisible(LIST_COLUMN_NAME.hashCode()));
        metadata.getColumnDefinition(LIST_COLUMN_TITLE).setVisible(isColumnVisible(CmsUserSettings.FILELIST_TITLE));
        metadata.getColumnDefinition(LIST_COLUMN_TYPE).setVisible(isColumnVisible(CmsUserSettings.FILELIST_TYPE));
        metadata.getColumnDefinition(LIST_COLUMN_SIZE).setVisible(isColumnVisible(CmsUserSettings.FILELIST_SIZE));
        metadata.getColumnDefinition(LIST_COLUMN_PERMISSIONS).setVisible(
            isColumnVisible(CmsUserSettings.FILELIST_PERMISSIONS));
        metadata.getColumnDefinition(LIST_COLUMN_DATELASTMOD).setVisible(
            isColumnVisible(CmsUserSettings.FILELIST_DATE_LASTMODIFIED));
        metadata.getColumnDefinition(LIST_COLUMN_USERLASTMOD).setVisible(
            isColumnVisible(CmsUserSettings.FILELIST_USER_LASTMODIFIED));
        metadata.getColumnDefinition(LIST_COLUMN_DATECREATE).setVisible(
            isColumnVisible(CmsUserSettings.FILELIST_DATE_CREATED));
        metadata.getColumnDefinition(LIST_COLUMN_USERCREATE).setVisible(
            isColumnVisible(CmsUserSettings.FILELIST_USER_CREATED));
        metadata.getColumnDefinition(LIST_COLUMN_DATEREL).setVisible(
            isColumnVisible(CmsUserSettings.FILELIST_DATE_RELEASED));
        metadata.getColumnDefinition(LIST_COLUMN_DATEEXP).setVisible(
            isColumnVisible(CmsUserSettings.FILELIST_DATE_EXPIRED));
        metadata.getColumnDefinition(LIST_COLUMN_STATE).setVisible(isColumnVisible(CmsUserSettings.FILELIST_STATE));
        metadata.getColumnDefinition(LIST_COLUMN_LOCKEDBY).setVisible(
            isColumnVisible(CmsUserSettings.FILELIST_LOCKEDBY));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    @Override
    protected String defaultActionHtmlStart() {

        StringBuffer result = new StringBuffer(2048);
        result.append(htmlStart(null));
        result.append(getList().listJs());
        result.append(CmsListExplorerColumn.getExplorerStyleDef());
        result.append(bodyStart("dialog", null));
        result.append(dialogStart());
        result.append(dialogContentStart(getParamTitle()));
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeSelectPage()
     */
    @Override
    protected void executeSelectPage() {

        super.executeSelectPage();
        getSettings().setExplorerPage(getList().getCurrentPage());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillList()
     */
    @Override
    protected void fillList() {

        getListState().setPage(getSettings().getExplorerPage());
        super.fillList();
    }

    /**
     * Gets a map of additional request parameters which should be passed to the explorer.<p>
     *
     * @return the map of additional parameters to pass to the explorer
     */
    protected Map<String, String[]> getAdditionalParametersForExplorerForward() {

        return Collections.emptyMap();
    }

    /**
     * Returns the colVisibilities map.<p>
     *
     * @return the colVisibilities map
     */
    protected Map<Integer, Boolean> getColVisibilities() {

        return m_colVisibilities;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        if (getSettings().getExplorerMode() != null) {
            CmsListColumnDefinition nameCol = getList().getMetadata().getColumnDefinition(LIST_COLUMN_NAME);
            if (!(getSettings().getExplorerMode().equals(CmsExplorer.VIEW_GALLERY)
                || getSettings().getExplorerMode().equals(CmsExplorer.VIEW_LIST))) {
                nameCol.setName(
                    org.opencms.workplace.explorer.Messages.get().container(
                        org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0));
            } else {
                nameCol.setName(
                    org.opencms.workplace.explorer.Messages.get().container(
                        org.opencms.workplace.explorer.Messages.GUI_INPUT_PATH_0));
            }
        }
        return getCollector().getListItems(null);
    }

    /**
     * Returns the list state for initializing the collector.<p>
     *
     * @return the list state
     */
    protected CmsListState getListStateForCollector() {

        CmsListState lstate = new CmsListState();
        if (getList() != null) {
            lstate = getListState();
        }
        switch (getAction()) {
            //////////////////// ACTION: default actions
            case ACTION_LIST_SEARCH:
                if (getParamSearchFilter() == null) {
                    setParamSearchFilter("");
                }
                if (getParamSearchFilter().equals(lstate.getFilter())) {
                    lstate.setOrder(CmsListOrderEnum.ORDER_DESCENDING);
                } else {
                    lstate.setOrder(CmsListOrderEnum.ORDER_ASCENDING);
                }
                lstate.setFilter(getParamSearchFilter());
                break;
            case ACTION_LIST_SORT:
                lstate.setColumn(getParamSortCol());
                break;
            case ACTION_LIST_SELECT_PAGE:
                int page = Integer.valueOf(getParamPage()).intValue();
                lstate.setPage(page);
                break;
            default:
                // no op
        }
        return lstate;
    }

    /**
     * Returns the project to use as reference.<p>
     *
     * @return the project to use as reference
     */
    protected CmsProject getProject() {

        return getCms().getRequestContext().getCurrentProject();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        // this to show first the explorer view
        if (getShowExplorer()) {
            CmsUUID projectId = getProject().getUuid();
            Map<String, String[]> params = new HashMap<String, String[]>();
            // set action parameter to initial dialog call
            params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
            params.putAll(getToolManager().getCurrentTool(this).getHandler().getParameters(this));
            params.putAll(getAdditionalParametersForExplorerForward());
            getSettings().setExplorerProjectId(projectId);
            getSettings().setCollector(getCollector());
            getSettings().setExplorerMode(CmsExplorer.VIEW_LIST);
            try {
                setShowExplorer(true);
                getToolManager().jspForwardPage(this, PATH_DIALOGS + "list-explorer.jsp", params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            setShowExplorer(false);
        }
    }

    /**
     * Returns the visibility flag for a given column.<p>
     *
     * The default behavior is to show the same columns as the explorer view,
     * but this can be overwritten.<p>
     *
     * @param colFlag some {@link CmsUserSettings#FILELIST_TITLE} like value
     *              identifying the column to get the visibility flag for
     *
     * @return the visibility flag for the given column
     */
    protected boolean isColumnVisible(int colFlag) {

        Integer key = Integer.valueOf(colFlag);
        if (m_colVisibilities.containsKey(key)) {
            return m_colVisibilities.get(key).booleanValue();
        }
        return false;
    }

    /**
     * Adds the standard explorer view columns to the list.<p>
     *
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        setColumnVisibilities();

        // position 0: icon
        CmsListColumnDefinition typeIconCol = new CmsListColumnDefinition(LIST_COLUMN_TYPEICON);
        typeIconCol.setName(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_ICON_0));
        typeIconCol.setHelpText(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_ICON_HELP_0));
        typeIconCol.setWidth("20");
        typeIconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        typeIconCol.setListItemComparator(new CmsListItemActionIconComparator());

        // add resource icon action
        CmsListDirectAction resourceTypeIconAction = new CmsListResourceTypeIconAction(LIST_ACTION_TYPEICON);
        resourceTypeIconAction.setEnabled(false);
        typeIconCol.addDirectAction(resourceTypeIconAction);
        metadata.addColumn(typeIconCol);

        // position 1: edit button
        CmsListColumnDefinition editIconCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editIconCol.setName(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_EDIT_0));
        editIconCol.setHelpText(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_EDIT_HELP_0));
        editIconCol.setWidth("20");
        editIconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);

        // add enabled edit action
        CmsListDirectAction editAction = new CmsListEditResourceAction(LIST_ACTION_EDIT, LIST_COLUMN_NAME);
        editAction.setEnabled(true);
        editIconCol.addDirectAction(editAction);
        // add disabled edit action
        CmsListDirectAction noEditAction = new CmsListEditResourceAction(LIST_ACTION_EDIT + "d", LIST_COLUMN_NAME);
        noEditAction.setEnabled(false);
        editIconCol.addDirectAction(noEditAction);
        metadata.addColumn(editIconCol);

        // position 2: lock icon
        CmsListColumnDefinition lockIconCol = new CmsListColumnDefinition(LIST_COLUMN_LOCKICON);
        lockIconCol.setName(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_LOCK_0));
        lockIconCol.setWidth("20");
        lockIconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        lockIconCol.setListItemComparator(new CmsListItemActionIconComparator());

        // add lock icon action
        CmsListDirectAction resourceLockIconAction = new CmsListResourceLockAction(LIST_ACTION_LOCKICON);
        resourceLockIconAction.setEnabled(false);
        lockIconCol.addDirectAction(resourceLockIconAction);
        metadata.addColumn(lockIconCol);

        // position 3: project state icon, resource is inside or outside current project
        CmsListColumnDefinition projStateIconCol = new CmsListColumnDefinition(LIST_COLUMN_PROJSTATEICON);
        projStateIconCol.setName(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_PROJSTATE_0));
        projStateIconCol.setWidth("20");

        // add resource icon action
        CmsListDirectAction resourceProjStateAction = new CmsListResourceProjStateAction(LIST_ACTION_PROJSTATEICON);
        resourceProjStateAction.setEnabled(false);
        projStateIconCol.addDirectAction(resourceProjStateAction);
        metadata.addColumn(projStateIconCol);

        // position 4: name
        CmsListColumnDefinition nameCol = new CmsListExplorerColumn(LIST_COLUMN_NAME);
        nameCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_PATH_0));

        // add resource open action
        CmsListDefaultAction resourceOpenDefAction = new CmsListOpenResourceAction(
            LIST_DEFACTION_OPEN,
            LIST_COLUMN_ROOT_PATH);
        resourceOpenDefAction.setEnabled(true);
        nameCol.addDefaultAction(resourceOpenDefAction);
        metadata.addColumn(nameCol);
        nameCol.setPrintable(false);

        // position 4: root path for printing
        CmsListColumnDefinition rootPathCol = new CmsListExplorerColumn(LIST_COLUMN_ROOT_PATH);
        rootPathCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0));
        rootPathCol.setVisible(false);
        rootPathCol.setPrintable(true);
        metadata.addColumn(rootPathCol);

        // position 5: title
        CmsListColumnDefinition titleCol = new CmsListExplorerColumn(LIST_COLUMN_TITLE);
        titleCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0));
        metadata.addColumn(titleCol);

        // position 6: resource type
        CmsListColumnDefinition typeCol = new CmsListExplorerColumn(LIST_COLUMN_TYPE);
        typeCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_TYPE_0));
        metadata.addColumn(typeCol);

        // position 7: size
        CmsListColumnDefinition sizeCol = new CmsListExplorerColumn(LIST_COLUMN_SIZE);
        sizeCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_SIZE_0));
        metadata.addColumn(sizeCol);

        // position 8: permissions
        CmsListColumnDefinition permissionsCol = new CmsListExplorerColumn(LIST_COLUMN_PERMISSIONS);
        permissionsCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_PERMISSIONS_0));
        metadata.addColumn(permissionsCol);

        // position 9: date of last modification
        CmsListColumnDefinition dateLastModCol = new CmsListExplorerColumn(LIST_COLUMN_DATELASTMOD);
        dateLastModCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_DATELASTMODIFIED_0));
        dateLastModCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(dateLastModCol);

        // position 10: user who last modified the resource
        CmsListColumnDefinition userLastModCol = new CmsListExplorerColumn(LIST_COLUMN_USERLASTMOD);
        userLastModCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_USERLASTMODIFIED_0));
        metadata.addColumn(userLastModCol);

        // position 11: date of creation
        CmsListColumnDefinition dateCreateCol = new CmsListExplorerColumn(LIST_COLUMN_DATECREATE);
        dateCreateCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_DATECREATED_0));
        dateCreateCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(dateCreateCol);

        // position 12: user who created the resource
        CmsListColumnDefinition userCreateCol = new CmsListExplorerColumn(LIST_COLUMN_USERCREATE);
        userCreateCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_USERCREATED_0));
        metadata.addColumn(userCreateCol);

        // position 13: date of release
        CmsListColumnDefinition dateReleaseCol = new CmsListExplorerColumn(LIST_COLUMN_DATEREL);
        dateReleaseCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_DATERELEASED_0));
        dateReleaseCol.setFormatter(
            new CmsListDateMacroFormatter(
                Messages.get().container(Messages.GUI_LIST_DATE_FORMAT_1),
                new CmsMessageContainer(null, CmsTouch.DEFAULT_DATE_STRING),
                CmsResource.DATE_RELEASED_DEFAULT));
        metadata.addColumn(dateReleaseCol);

        // position 14: date of expiration
        CmsListColumnDefinition dateExpirationCol = new CmsListExplorerColumn(LIST_COLUMN_DATEEXP);
        dateExpirationCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_DATEEXPIRED_0));
        dateExpirationCol.setFormatter(
            new CmsListDateMacroFormatter(
                Messages.get().container(Messages.GUI_LIST_DATE_FORMAT_1),
                new CmsMessageContainer(null, CmsTouch.DEFAULT_DATE_STRING),
                CmsResource.DATE_EXPIRED_DEFAULT));
        metadata.addColumn(dateExpirationCol);

        // position 15: state (changed, unchanged, new, deleted)
        CmsListColumnDefinition stateCol = new CmsListExplorerColumn(LIST_COLUMN_STATE);
        stateCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_STATE_0));
        metadata.addColumn(stateCol);

        // position 16: locked by
        CmsListColumnDefinition lockedByCol = new CmsListExplorerColumn(LIST_COLUMN_LOCKEDBY);
        lockedByCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_LOCKEDBY_0));
        metadata.addColumn(lockedByCol);

        // position 17: site
        CmsListColumnDefinition siteCol = new CmsListExplorerColumn(LIST_COLUMN_SITE);
        siteCol.setName(
            org.opencms.workplace.explorer.Messages.get().container(
                org.opencms.workplace.explorer.Messages.GUI_LABEL_SITE_0));
        metadata.addColumn(siteCol);
    }

    /**
     * Sets the default column visibility flags from the user preferences.<p>
     */
    protected void setColumnVisibilities() {

        m_colVisibilities = new HashMap<Integer, Boolean>(16);
        // set explorer configurable column visibilities
        int preferences = new CmsUserSettings(getCms()).getExplorerSettings();
        setColumnVisibility(CmsUserSettings.FILELIST_TITLE, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_TYPE, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_SIZE, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_PERMISSIONS, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_DATE_LASTMODIFIED, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_USER_LASTMODIFIED, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_DATE_CREATED, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_USER_CREATED, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_DATE_RELEASED, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_DATE_EXPIRED, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_STATE, preferences);
        setColumnVisibility(CmsUserSettings.FILELIST_LOCKEDBY, preferences);
        // set explorer no configurable column visibilities
        m_colVisibilities.put(Integer.valueOf(LIST_COLUMN_TYPEICON.hashCode()), Boolean.TRUE);
        m_colVisibilities.put(Integer.valueOf(LIST_COLUMN_LOCKICON.hashCode()), Boolean.TRUE);
        m_colVisibilities.put(Integer.valueOf(LIST_COLUMN_PROJSTATEICON.hashCode()), Boolean.TRUE);
        m_colVisibilities.put(Integer.valueOf(LIST_COLUMN_NAME.hashCode()), Boolean.TRUE);
        m_colVisibilities.put(Integer.valueOf(LIST_COLUMN_EDIT.hashCode()), Boolean.FALSE);
        m_colVisibilities.put(
            Integer.valueOf(LIST_COLUMN_SITE.hashCode()),
            Boolean.valueOf(OpenCms.getSiteManager().getSites().size() > 1));
    }

    /**
     * Sets the given column visibility flag from the given preferences.<p>
     *
     * @param colFlag the flag that identifies the column to set the flag for
     * @param prefs the user preferences
     */
    protected void setColumnVisibility(int colFlag, int prefs) {

        Integer key = Integer.valueOf(colFlag);
        Boolean value = Boolean.valueOf((prefs & colFlag) > 0);
        m_colVisibilities.put(key, value);
    }

    /**
     * Sets the colVisibilities map.<p>
     *
     * @param colVisibilities the colVisibilities map to set
     */
    protected void setColVisibilities(Map<Integer, Boolean> colVisibilities) {

        m_colVisibilities = colVisibilities;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        metadata.addIndependentAction(CmsListIndependentAction.getDefaultExplorerSwitchAction());
    }

    /**
     * Returns the show explorer flag.<p>
     *
     * @return the show explorer flag
     */
    private boolean getShowExplorer() {

        if (getParamShowexplorer() != null) {
            return Boolean.valueOf(getParamShowexplorer()).booleanValue();
        }
        Map<?, ?> dialogObject = (Map<?, ?>)getSettings().getDialogObject();
        if (dialogObject == null) {
            return false;
        }
        Boolean storedParam = (Boolean)dialogObject.get(getClass().getName());
        if (storedParam == null) {
            return false;
        }
        return storedParam.booleanValue();
    }

    /**
     * Sets the show explorer flag.<p>
     *
     * @param showExplorer the show explorer flag
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setShowExplorer(boolean showExplorer) {

        Map dialogMap = (Map)getSettings().getDialogObject();
        if (dialogMap == null) {
            dialogMap = new HashMap();
            getSettings().setDialogObject(dialogMap);
        }
        dialogMap.put(getClass().getName(), Boolean.valueOf(showExplorer));
    }
}