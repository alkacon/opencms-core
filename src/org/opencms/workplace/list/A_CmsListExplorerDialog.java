/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/A_CmsListExplorerDialog.java,v $
 * Date   : $Date: 2006/01/11 17:07:03 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsResourceUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.WorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides a list dialog for resources.<p> 
 *
 * @author  Michael Moossen 
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsListExplorerDialog extends A_CmsListDialog {

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

    /** Explorer list JSP path. */
    private static final String PATH_EXPLORER_LIST = PATH_DIALOGS + "list-explorer.html";

    /** Flag to indicate if the resource names should be shown relative to the current site or as root paths. */
    protected boolean m_showSitePath;

    /** Column visibility flags container. */
    private Map m_colVisibilities;

    /** Mapping from resource ids (as {@link String} objects) to {@link CmsResource} objects. */
    private Map m_resources;

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
    public void executeListIndepActions() {

        if (getParamListAction().equals(CmsListIndependentAction.ACTION_EXPLORER_SWITCH_ID)) {
            Map params = new HashMap();
            // set action parameter to initial dialog call
            params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);
            params.putAll(getToolManager().getCurrentTool(this).getHandler().getParameters(this));

            getSettings().setCollector(new CmsListResourcesCollector(getResources()));
            getSettings().setExplorerMode(CmsExplorer.VIEW_LIST);
            try {
                getToolManager().jspForwardPage(this, PATH_EXPLORER_LIST, params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        super.executeListIndepActions();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getList()
     */
    public CmsHtmlList getList() {

        // assure we have the cms object
        CmsHtmlList list = super.getList();
        if (list != null) {
            CmsListColumnDefinition colName = list.getMetadata().getColumnDefinition(LIST_COLUMN_NAME);
            if (colName != null) {
                ((CmsListOpenResourceAction)colName.getDefaultAction(LIST_DEFACTION_OPEN)).setCms(getCms());
            }
        }
        return list;
    }

    /**
     * Returns a resource given an item id.<p>
     * 
     * @param id item id
     * 
     * @return the cached resource assigned to the given list item id
     */
    public CmsResource getResource(String id) {

        return (CmsResource)m_resources.get(id);
    }

    /**
     * Adds the standard explorer view columns to the list.<p>
     * 
     * @param metadata the list metadata
     */
    protected void addExplorerColumns(CmsListMetadata metadata) {

        metadata.setVolatile(true);
        // position 1: icon
        CmsListColumnDefinition typeIconCol = new CmsListColumnDefinition(LIST_COLUMN_TYPEICON);
        typeIconCol.setName(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_ICON_0));
        typeIconCol.setHelpText(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_ICON_HELP_0));
        typeIconCol.setWidth("20");
        typeIconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        typeIconCol.setListItemComparator(new CmsListItemActionIconComparator());
        typeIconCol.setVisible(isColumnVisible(LIST_COLUMN_TYPEICON.hashCode()));

        // add resource icon action
        CmsListDirectAction resourceTypeIconAction = new CmsListResourceTypeIconAction(LIST_ACTION_TYPEICON, getCms(), this);
        resourceTypeIconAction.setEnabled(false);
        typeIconCol.addDirectAction(resourceTypeIconAction);
        metadata.addColumn(typeIconCol);

        // position 2: lock icon
        CmsListColumnDefinition lockIconCol = new CmsListColumnDefinition(LIST_COLUMN_LOCKICON);
        lockIconCol.setName(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_LOCK_0));
        lockIconCol.setWidth("20");
        lockIconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        lockIconCol.setListItemComparator(new CmsListItemActionIconComparator());
        lockIconCol.setVisible(isColumnVisible(LIST_COLUMN_LOCKICON.hashCode()));

        // add lock icon action
        CmsListDirectAction resourceLockIconAction = new CmsListResourceLockAction(LIST_ACTION_LOCKICON, getCms(), this);
        resourceLockIconAction.setEnabled(false);
        lockIconCol.addDirectAction(resourceLockIconAction);
        metadata.addColumn(lockIconCol);

        // position 3: project state icon, resource is inside or outside current project        
        CmsListColumnDefinition projStateIconCol = new CmsListColumnDefinition(LIST_COLUMN_PROJSTATEICON);
        projStateIconCol.setName(Messages.get().container(Messages.GUI_EXPLORER_LIST_COLS_PROJSTATE_0));
        projStateIconCol.setWidth("20");
        projStateIconCol.setVisible(isColumnVisible(LIST_COLUMN_PROJSTATEICON.hashCode()));

        // add resource icon action
        CmsListDirectAction resourceProjStateAction = new CmsListResourceProjStateAction(
            LIST_ACTION_PROJSTATEICON,
            getCms(),
            this);
        resourceProjStateAction.setEnabled(false);
        projStateIconCol.addDirectAction(resourceProjStateAction);
        metadata.addColumn(projStateIconCol);

        // position 4: name
        CmsListColumnDefinition nameCol = new CmsListExplorerColumn(LIST_COLUMN_NAME);
        nameCol.setName(WorkplaceMessages.get().container("input.name"));
        nameCol.setVisible(isColumnVisible(LIST_COLUMN_NAME.hashCode()));

        // add resource open action
        CmsListDefaultAction resourceOpenDefAction = new CmsListOpenResourceAction(
            LIST_DEFACTION_OPEN,
            getCms(),
            LIST_COLUMN_NAME);
        resourceOpenDefAction.setEnabled(true);
        nameCol.addDefaultAction(resourceOpenDefAction);
        metadata.addColumn(nameCol);

        // position 5: title
        CmsListColumnDefinition titleCol = new CmsListExplorerColumn(LIST_COLUMN_TITLE);
        titleCol.setName(WorkplaceMessages.get().container("input.title"));
        titleCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_TITLE));
        metadata.addColumn(titleCol);

        // position 6: resource type
        CmsListColumnDefinition typeCol = new CmsListExplorerColumn(LIST_COLUMN_TYPE);
        typeCol.setName(WorkplaceMessages.get().container("input.type"));
        typeCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_TYPE));
        metadata.addColumn(typeCol);

        // position 7: size
        CmsListColumnDefinition sizeCol = new CmsListExplorerColumn(LIST_COLUMN_SIZE);
        sizeCol.setName(WorkplaceMessages.get().container("input.size"));
        sizeCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_SIZE));
        metadata.addColumn(sizeCol);

        // position 8: permissions
        CmsListColumnDefinition permissionsCol = new CmsListExplorerColumn(LIST_COLUMN_PERMISSIONS);
        permissionsCol.setName(WorkplaceMessages.get().container("input.permissions"));
        permissionsCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_PERMISSIONS));
        metadata.addColumn(permissionsCol);

        // position 9: date of last modification
        CmsListColumnDefinition dateLastModCol = new CmsListExplorerColumn(LIST_COLUMN_DATELASTMOD);
        dateLastModCol.setName(WorkplaceMessages.get().container("input.datelastmodified"));
        dateLastModCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_DATE_LASTMODIFIED));
        dateLastModCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(dateLastModCol);

        // position 10: user who last modified the resource
        CmsListColumnDefinition userLastModCol = new CmsListExplorerColumn(LIST_COLUMN_USERLASTMOD);
        userLastModCol.setName(WorkplaceMessages.get().container("input.userlastmodified"));
        userLastModCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_USER_LASTMODIFIED));
        metadata.addColumn(userLastModCol);

        // position 11: date of creation
        CmsListColumnDefinition dateCreateCol = new CmsListExplorerColumn(LIST_COLUMN_DATECREATE);
        dateCreateCol.setName(WorkplaceMessages.get().container("input.datecreated"));
        dateCreateCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_DATE_CREATED));
        dateCreateCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(dateCreateCol);

        // position 12: user who created the resource
        CmsListColumnDefinition userCreateCol = new CmsListExplorerColumn(LIST_COLUMN_USERCREATE);
        userCreateCol.setName(WorkplaceMessages.get().container("input.usercreated"));
        userCreateCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_USER_CREATED));
        metadata.addColumn(userCreateCol);

        // position 13: date of release
        CmsListColumnDefinition dateReleaseCol = new CmsListExplorerColumn(LIST_COLUMN_DATEREL);
        dateReleaseCol.setName(WorkplaceMessages.get().container("input.datereleased"));
        dateReleaseCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_DATE_RELEASED));
        dateReleaseCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter(CmsResource.DATE_RELEASED_DEFAULT));
        metadata.addColumn(dateReleaseCol);

        // position 14: date of expiration
        CmsListColumnDefinition dateExpirationCol = new CmsListExplorerColumn(LIST_COLUMN_DATEEXP);
        dateExpirationCol.setName(WorkplaceMessages.get().container("input.dateexpired"));
        dateExpirationCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_DATE_EXPIRED));
        dateExpirationCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter(CmsResource.DATE_EXPIRED_DEFAULT));
        metadata.addColumn(dateExpirationCol);

        // position 15: state (changed, unchanged, new, deleted)
        CmsListColumnDefinition stateCol = new CmsListExplorerColumn(LIST_COLUMN_STATE);
        stateCol.setName(WorkplaceMessages.get().container("input.state"));
        stateCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_STATE));
        metadata.addColumn(stateCol);

        // position 16: locked by
        CmsListColumnDefinition lockedByCol = new CmsListExplorerColumn(LIST_COLUMN_LOCKEDBY);
        lockedByCol.setName(WorkplaceMessages.get().container("input.lockedby"));
        lockedByCol.setVisible(isColumnVisible(CmsUserSettings.FILELIST_LOCKEDBY));
        metadata.addColumn(lockedByCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#defaultActionHtmlStart()
     */
    protected String defaultActionHtmlStart() {

        StringBuffer result = new StringBuffer(2048);
        result.append(htmlStart(null));
        result.append(getList().listJs(getLocale()));
        result.append(CmsListExplorerColumn.getExplorerStyleDef());
        result.append(bodyStart("dialog", null));
        result.append(dialogStart());
        result.append(dialogContentStart(getParamTitle()));
        return result.toString();
    }

    /**
     * Returns a list of list items from a list of resources.<p>
     * 
     * @param resources a list of {@link CmsResource} objects
     * 
     * @return a list of {@link CmsListItem} objects
     */
    protected List getListItemsFromResources(List resources) {

        List ret = new ArrayList();
        m_resources = new HashMap();
        CmsResourceUtil resUtil = new CmsResourceUtil(getCms());
        getCms().getRequestContext().saveSiteRoot();
        try {
            getCms().getRequestContext().setSiteRoot("/");
            // get content
            Iterator itRes = resources.iterator();
            while (itRes.hasNext()) {
                CmsResource resource = (CmsResource)itRes.next();
                String path = getCms().getSitePath(resource);
                m_resources.put(resource.getResourceId().toString(), resource);
                resUtil.setResource(resource);
                CmsListItem item = getList().newItem(resource.getResourceId().toString());
                item.set(LIST_COLUMN_NAME, m_showSitePath ? path : resource.getRootPath());
                item.set(LIST_COLUMN_TITLE, resUtil.getTitle());
                item.set(LIST_COLUMN_TYPE, resUtil.getResourceTypeName());
                item.set(LIST_COLUMN_SIZE, resUtil.getSizeString());
                item.set(LIST_COLUMN_PERMISSIONS, resUtil.getPermissions());
                item.set(LIST_COLUMN_DATELASTMOD, new Date(resource.getDateLastModified()));
                item.set(LIST_COLUMN_USERLASTMOD, resUtil.getUserLastModified());
                item.set(LIST_COLUMN_DATECREATE, new Date(resource.getDateCreated()));
                item.set(LIST_COLUMN_USERCREATE, resUtil.getUserCreated());
                item.set(LIST_COLUMN_DATEREL, new Date(resource.getDateReleased()));
                item.set(LIST_COLUMN_DATEEXP, new Date(resource.getDateExpired()));
                item.set(LIST_COLUMN_STATE, resUtil.getStateName());
                item.set(LIST_COLUMN_LOCKEDBY, resUtil.getLockedByName());
                ret.add(item);
            }
        } finally {
            getCms().getRequestContext().restoreSiteRoot();
        }
        return ret;
    }

    /**
     * Returns the list of resources to show in the explorer view.<p>
     * 
     * @return a list of {@link org.opencms.file.CmsResource} objects
     */
    protected List getResources() {

        List ret = new ArrayList(getList().getCurrentPageItems().size());
        Iterator it = getList().getCurrentPageItems().iterator();
        while (it.hasNext()) {
            CmsListItem item = (CmsListItem)it.next();
            ret.add(m_resources.get(item.getId()));
        }
        return ret;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        addMessages(WorkplaceMessages.get().getBundleName());
        super.initMessages();
    }

    /**
     * Returns the visibility flag for a given column.<p>
     * 
     * The default behaviour is to show the same columns as the explorer view,
     * but this can be overwritten.<p>
     * 
     * @param colFlag some {@link CmsUserSettings#FILELIST_TITLE} like value 
     *              indentifying the column to get the visibility flag for
     *  
     * @return the visibility flag for the given column
     */
    protected boolean isColumnVisible(int colFlag) {

        if (m_colVisibilities.get(new Integer(colFlag)) instanceof Boolean) {
            return ((Boolean)m_colVisibilities.get(new Integer(colFlag))).booleanValue();
        }
        return false;
    }

    /**
     * Sets the default column visibility flags from the user preferences.<p>
     */
    protected void setColumnVisibilities() {

        m_colVisibilities = new HashMap(16);
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
        m_colVisibilities.put(new Integer(LIST_COLUMN_TYPEICON.hashCode()), Boolean.TRUE);
        m_colVisibilities.put(new Integer(LIST_COLUMN_LOCKICON.hashCode()), Boolean.TRUE);
        m_colVisibilities.put(new Integer(LIST_COLUMN_PROJSTATEICON.hashCode()), Boolean.TRUE);
        m_colVisibilities.put(new Integer(LIST_COLUMN_NAME.hashCode()), Boolean.TRUE);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        metadata.addIndependentAction(CmsListIndependentAction.getDefaultExplorerSwitchAction());
    }

    /**
     * Sets the given column visibility flag from the given preferences.<p>
     * 
     * @param colFlag the flag that identifies the column to set the flag for
     * @param prefs the user preferences
     */
    private void setColumnVisibility(int colFlag, int prefs) {

        Integer key = new Integer(colFlag);
        Boolean value = new Boolean((prefs & colFlag) > 0);
        m_colVisibilities.put(key, value);
    }
}