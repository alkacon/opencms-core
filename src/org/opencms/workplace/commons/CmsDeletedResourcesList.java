/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.commons;

import org.opencms.file.CmsResource;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListPrintIAction;
import org.opencms.workplace.list.CmsListResourceIconAction;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Generates the list of deleted resources to be used by ajax in the dialog.<p>
 * 
 * @since 6.9.1
 */
public class CmsDeletedResourcesList extends A_CmsListDialog {

    /** Standard list button location. */
    public static final String ICON_LIST_WARNING = "list/warning.png";

    /** Standard list button location. */
    public static final String ICON_MULTI_RESTORE = "tools/ex_history/buttons/restore.png";

    /** List action id constant. */
    public static final String LIST_ACTION_CONFLICT = "drlac";

    /** List action id constant. */
    public static final String LIST_ACTION_ICON = "drlai";

    /** List column id constant. */
    public static final String LIST_COLUMN_CONFLICT = "drlcc";

    /** List column id constant. */
    public static final String LIST_COLUMN_DELETION_DATE = "drlcdd";

    /** List column id constant. */
    public static final String LIST_COLUMN_ICON = "drlci";

    /** List column id constant. */
    public static final String LIST_COLUMN_NAME = "drlcn";

    /** List column id constant. */
    public static final String LIST_COLUMN_TYPEID = "drlct";

    /** List column id constant. */
    public static final String LIST_COLUMN_VERSION = "drlcv";

    /** List id constant. */
    public static final String LIST_ID = "drl";

    /** List action id constant. */
    public static final String LIST_MACTION_RESTORE = "mr";

    /** Should deleted resources be displayed for the current folder or the subtree. */
    private boolean m_readTree;

    /** The name of the resource to display the deleted entries. */
    private String m_resourcename;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param resourcename the name of the resource to display deleted entries for
     * @param readTree display deleted resources for the subtree
     */
    public CmsDeletedResourcesList(CmsJspActionElement jsp, String resourcename, boolean readTree) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_DELETED_RESOURCES_LIST_NAME_0),
            null,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);

        // set the style to show common workplace dialog layout
        setParamStyle("");

        // prevent paging, usually there are only few model files
        getList().setMaxItemsPerPage(Integer.MAX_VALUE);

        // hide print button
        getList().getMetadata().getIndependentAction(CmsListPrintIAction.LIST_ACTION_ID).setVisible(false);

        // suppress the box around the list
        getList().setBoxed(false);

        // hide title of the list
        //getList().setShowTitle(false);

        m_readTree = readTree;
        m_resourcename = resourcename;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    public void executeListMultiActions() throws CmsRuntimeException {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws CmsRuntimeException {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();

        List list = getCms().readDeletedResources(m_resourcename, m_readTree);
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            I_CmsHistoryResource res = (I_CmsHistoryResource)iter.next();

            CmsListItem item = getList().newItem(res.getStructureId().toString());
            String resourcePath = getCms().getSitePath((CmsResource)res);
            item.set(LIST_COLUMN_NAME, m_resourcename + "|" + resourcePath);
            item.set(LIST_COLUMN_DELETION_DATE, new Date(res.getDateLastModified()));
            item.set(LIST_COLUMN_VERSION, String.valueOf(res.getVersion()));
            item.set(LIST_COLUMN_TYPEID, String.valueOf(res.getTypeId()));
            ret.add(item);
        }

        return ret;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setColumns(CmsListMetadata metadata) {

        // add column icon
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_DELETED_RESOURCES_LIST_COLS_ICON_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(false);

        // add icon action
        CmsListResourceIconAction iconAction = new CmsListResourceIconAction(
            LIST_ACTION_ICON,
            LIST_COLUMN_TYPEID,
            getCms());
        iconAction.setName(Messages.get().container(Messages.GUI_DELETED_RESOURCES_LIST_ACTION_ICON_0));
        iconAction.setEnabled(false);
        iconCol.addDirectAction(iconAction);
        metadata.addColumn(iconCol);

        // add column for conflict
        CmsListColumnDefinition conflictCol = new CmsListColumnDefinition(LIST_COLUMN_CONFLICT);
        conflictCol.setName(Messages.get().container(Messages.GUI_DELETED_RESOURCES_LIST_COLS_CONFLICT_0));
        conflictCol.setWidth("20");
        conflictCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        conflictCol.setSorteable(false);

        // add conflict action
        CmsListDirectAction conflictAction = new CmsListResourceIconAction(LIST_ACTION_CONFLICT, null, getCms()) {

            /**
             * @see org.opencms.workplace.list.CmsListResourceIconAction#getIconPath()
             */
            public String getIconPath() {

                return ICON_LIST_WARNING;
            }

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            public boolean isVisible() {

                String path = (String)getItem().get(LIST_COLUMN_NAME);
                return getCms().existsResource(path);
            }

        };
        conflictAction.setName(Messages.get().container(Messages.GUI_DELETED_RESOURCES_LIST_ACTION_WARNING_0));
        conflictAction.setEnabled(false);
        conflictCol.addDirectAction(conflictAction);
        metadata.addColumn(conflictCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_DELETED_RESOURCES_LIST_COLS_NAME_0));
        nameCol.setSorteable(false);
        nameCol.setWidth("60%");
        nameCol.setFormatter(new I_CmsListFormatter() {

            /**
             * @see org.opencms.workplace.list.I_CmsListFormatter#format(java.lang.Object, java.util.Locale)
             */
            public String format(Object data, Locale locale) {

                String[] dataArray = CmsStringUtil.splitAsArray((String)data, "|");
                String resourceName = dataArray[0];
                String resourcePath = dataArray[1];

                String orgResourcePath = resourcePath;

                while (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resourcePath)) {

                    try {
                        getCms().readResource(getCms().getRequestContext().removeSiteRoot(resourcePath));
                        break;
                    } catch (CmsException e) {
                        resourcePath = CmsResource.getParentFolder(resourcePath);
                    }
                }

                if (resourcePath != null) {
                    resourcePath = resourcePath.substring(resourceName.length());
                } else {
                    resourcePath = "";
                }
                orgResourcePath = orgResourcePath.substring(resourceName.length());

                StringBuffer ret = new StringBuffer();
                ret.append(resourcePath);
                ret.append("<span style=\"color:#0000aa;\">");
                ret.append(orgResourcePath.substring(resourcePath.length()));
                ret.append("</span>");

                return ret.toString();
            }

        });
        metadata.addColumn(nameCol);

        // add column for deletion date
        CmsListColumnDefinition delDateCol = new CmsListColumnDefinition(LIST_COLUMN_DELETION_DATE);
        delDateCol.setName(Messages.get().container(Messages.GUI_DELETED_RESOURCES_LIST_COLS_DEL_DATE_0));
        delDateCol.setSorteable(false);
        delDateCol.setWidth("20%");
        delDateCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(delDateCol);

        // add column for version
        CmsListColumnDefinition versionCol = new CmsListColumnDefinition(LIST_COLUMN_VERSION);
        versionCol.setName(Messages.get().container(Messages.GUI_DELETED_RESOURCES_LIST_COLS_VERSION_0));
        versionCol.setSorteable(false);
        versionCol.setWidth("20%");
        versionCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        metadata.addColumn(versionCol);

        // add column for typeid (invisible)
        CmsListColumnDefinition typeidCol = new CmsListColumnDefinition(LIST_COLUMN_TYPEID);
        typeidCol.setName(Messages.get().container(Messages.GUI_DELETED_RESOURCES_LIST_COLS_TYPEID_0));
        typeidCol.setVisible(false);
        metadata.addColumn(typeidCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add restore multi action
        CmsListMultiAction restoreMultiAction = new CmsListMultiAction(LIST_MACTION_RESTORE);
        restoreMultiAction.setName(Messages.get().container(Messages.GUI_DELETED_RESOURCES_LIST_MACTION_RESTORE_NAME_0));
        restoreMultiAction.setIconPath(ICON_MULTI_RESTORE);
        restoreMultiAction.setHelpText(Messages.get().container(
            Messages.GUI_DELETED_RESOURCES_LIST_MACTION_RESTORE_HELP_0));
        restoreMultiAction.setVisible(false);
        metadata.addMultiAction(restoreMultiAction);
    }

}
