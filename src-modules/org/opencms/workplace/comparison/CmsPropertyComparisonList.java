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

package org.opencms.workplace.comparison;

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsHistoryList;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * List for property comparison including columns for property name and the values. <p>
 *
 * @since 6.0.0
 */
public class CmsPropertyComparisonList extends A_CmsListDialog {

    /** view first file action constant. */
    public static final String LIST_ACTION_VIEW1 = "v1";

    /** view second file action constant. */
    public static final String LIST_ACTION_VIEW2 = "v2";

    /** list action id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_PROPERTY_NAME = "cp";

    /** list action id constant. */
    public static final String LIST_COLUMN_TYPE = "cz";

    /** list column id constant. */
    public static final String LIST_COLUMN_VERSION_1 = "cv";

    /** list column id constant. */
    public static final String LIST_COLUMN_VERSION_2 = "cw";

    /** list default action id constant. */
    public static final String LIST_DEFACTION_VIEW = "dv";

    /** list independent action id constant. */
    public static final String LIST_DETAIL_TYPE = "dt";

    /** list independent action id constant. */
    public static final String LIST_IACTION_SHOW = "is";

    /** List id constant. */
    public static final String LIST_ID = "hipcl";

    /** request parameter indicating whether attributes, elements or properties are compared.<p> */
    public static final String PARAM_COMPARE = "compare";

    /** The maximum length of properties and attributes to be displayed.<p> */
    protected static final int TRIM_AT_LENGTH = 60;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPropertyComparisonList.class);

    /** Parameter value for the structure id of the first file. */
    private String m_paramId1;

    /** Parameter value for the structure id of the second file. */
    private String m_paramId2;

    /** Parameter value for the version of the first file. */
    private String m_paramVersion1;

    /** Parameter value for the version of the second file. */
    private String m_paramVersion2;

    /** The first resource to compare. */
    private CmsResource m_resource1;

    /** The second resource to compare. */
    private CmsResource m_resource2;

    /** The type of the files. */
    private int m_resourceType;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsPropertyComparisonList(CmsJspActionElement jsp) {

        this(LIST_ID, jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPropertyComparisonList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the displayed list
     * @param listName the name of the list
     * @param sortedColId the a priory sorted column
     * @param sortOrder the order of the sorted column
     * @param searchableColId the column to search into
     */
    protected CmsPropertyComparisonList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        String sortedColId,
        CmsListOrderEnum sortOrder,
        String searchableColId) {

        super(jsp, listId, listName, sortedColId, sortOrder, searchableColId);
    }

    /**
     * Protected constructor.<p>
     *
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsPropertyComparisonList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_COMPARE_PROPERTIES_0),
            LIST_COLUMN_PROPERTY_NAME,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
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
    public void executeListSingleActions() throws IOException, ServletException {

        // forward to the edit module screen
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(CmsHistoryList.PARAM_VERSION_1, new String[] {getParamVersion1()});
        params.put(CmsHistoryList.PARAM_VERSION_2, new String[] {getParamVersion2()});
        params.put(CmsHistoryList.PARAM_ID_1, new String[] {getParamId1()});
        params.put(CmsHistoryList.PARAM_ID_2, new String[] {getParamId2()});
        params.put(PARAM_COMPARE, new String[] {"properties"});
        params.put(PARAM_RESOURCE, new String[] {getParamResource()});
        // forward to the difference screen
        getToolManager().jspForwardTool(this, "/history/comparison/difference", params);
    }

    /**
     * Returns the paramId1.<p>
     *
     * @return the paramId1
     */
    public String getParamId1() {

        return m_paramId1;
    }

    /**
     * Returns the paramId2.<p>
     *
     * @return the paramId2
     */
    public String getParamId2() {

        return m_paramId2;
    }

    /**
     * Returns the paramNewversionid.<p>
     *
     * @return the paramNewversionid
     */
    public String getParamVersion1() {

        return m_paramVersion1;
    }

    /**
     * Returns the paramOldversionid.<p>
     *
     * @return the paramOldversionid
     */
    public String getParamVersion2() {

        return m_paramVersion2;
    }

    /**
     * Returns the resource 1.<p>
     *
     * @return the resource 1
     */
    public CmsResource getResource1() {

        return m_resource1;
    }

    /**
     * Returns the resource 2.<p>
     *
     * @return the resource 2
     */
    public CmsResource getResource2() {

        return m_resource2;
    }

    /**
     * Returns the resourceType.<p>
     *
     * @return the resourceType
     */
    public int getResourceType() {

        return m_resourceType;
    }

    /**
     * Sets the paramId1.<p>
     *
     * @param paramId1 the paramId1 to set
     */
    public void setParamId1(String paramId1) {

        m_paramId1 = paramId1;
    }

    /**
     * Sets the paramId2.<p>
     *
     * @param paramId2 the paramId2 to set
     */
    public void setParamId2(String paramId2) {

        m_paramId2 = paramId2;
    }

    /**
     * Sets the paramNewversionid.<p>
     *
     * @param paramNewversionid the paramNewversionid to set
     */
    public void setParamVersion1(String paramNewversionid) {

        m_paramVersion1 = paramNewversionid;
    }

    /**
     * Sets the paramOldversionid.<p>
     *
     * @param paramOldversionid the paramOldversionid to set
     */
    public void setParamVersion2(String paramOldversionid) {

        m_paramVersion2 = paramOldversionid;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // no details
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        Iterator<CmsAttributeComparison> diffs = CmsResourceComparison.compareProperties(
            getCms(),
            getResource1(),
            getParamVersion1(),
            getResource2(),
            getParamVersion2()).iterator();
        while (diffs.hasNext()) {
            CmsAttributeComparison comparison = diffs.next();
            CmsListItem item = getList().newItem(comparison.getName());
            item.set(LIST_COLUMN_PROPERTY_NAME, comparison.getName());
            item.set(LIST_COLUMN_VERSION_1, CmsStringUtil.trimToSize(comparison.getVersion1(), TRIM_AT_LENGTH));
            item.set(LIST_COLUMN_VERSION_2, CmsStringUtil.trimToSize(comparison.getVersion2(), TRIM_AT_LENGTH));
            if (CmsResourceComparison.TYPE_ADDED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_ADDED_0));
            } else if (CmsResourceComparison.TYPE_REMOVED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_REMOVED_0));
            } else if (CmsResourceComparison.TYPE_CHANGED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_CHANGED_0));
            } else {
                if (!getList().getMetadata().getItemDetailDefinition(LIST_IACTION_SHOW).isVisible()) {
                    // do not display entry
                    continue;
                } else {
                    item.set(LIST_COLUMN_TYPE, key(Messages.GUI_COMPARE_UNCHANGED_0));
                }
            }
            ret.add(item);
        }
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_VERSION_1).setName(
            Messages.get().container(
                Messages.GUI_COMPARE_VERSION_1,
                CmsHistoryListUtil.getDisplayVersion(getParamVersion1(), getLocale())));
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_VERSION_2).setName(Messages.get().container(
            Messages.GUI_COMPARE_VERSION_1,
            CmsHistoryListUtil.getDisplayVersion(getParamVersion2(), getLocale())));
        return ret;
    }

    /**
     *
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        try {
            m_resource1 = CmsResourceComparisonDialog.readResource(
                getCms(),
                new CmsUUID(getParamId1()),
                getParamVersion1());
            m_resource2 = CmsResourceComparisonDialog.readResource(
                getCms(),
                new CmsUUID(getParamId2()),
                getParamVersion2());
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        m_resourceType = m_resource1.getTypeId();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for icon
        CmsListColumnDefinition iconCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        iconCol.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_ICON_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(true);

        // add state error action
        CmsListDirectAction addedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_ADDED) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_TYPE).toString();
                return key(Messages.GUI_COMPARE_ADDED_0).equals(type);
            }
        };
        addedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_ADDED_0));
        addedAction.setIconPath("tools/ex_history/buttons/added.png");
        addedAction.setEnabled(true);
        iconCol.addDirectAction(addedAction);

        // add state error action
        CmsListDirectAction removedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_REMOVED) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_TYPE).toString();
                return key(Messages.GUI_COMPARE_REMOVED_0).equals(type);
            }
        };
        removedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_REMOVED_0));
        removedAction.setIconPath("tools/ex_history/buttons/removed.png");
        removedAction.setEnabled(true);
        iconCol.addDirectAction(removedAction);

        // add state error action
        CmsListDirectAction changedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_CHANGED) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_TYPE).toString();
                return key(Messages.GUI_COMPARE_CHANGED_0).equals(type);
            }
        };
        changedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_CHANGED_0));
        changedAction.setIconPath("tools/ex_history/buttons/changed.png");
        changedAction.setEnabled(true);
        iconCol.addDirectAction(changedAction);

        // add state error action
        CmsListDirectAction unchangedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_UNCHANGED) {

            /**
             * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_TYPE).toString();
                return key(Messages.GUI_COMPARE_UNCHANGED_0).equals(type);
            }
        };
        unchangedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_UNCHANGED_0));
        unchangedAction.setIconPath("tools/ex_history/buttons/unchanged.png");
        unchangedAction.setEnabled(true);
        iconCol.addDirectAction(unchangedAction);
        metadata.addColumn(iconCol);
        iconCol.setPrintable(false);

        // add column for type
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_TYPE);
        typeCol.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_STATUS_0));
        typeCol.setWidth("10%");
        CmsListDefaultAction typeColAction = new CmsListDefaultAction(CmsElementComparisonList.LIST_ACTION_STATUS);
        typeColAction.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_STATUS_0));
        typeColAction.setEnabled(true);
        // set action for the name column
        typeCol.addDefaultAction(typeColAction);
        metadata.addColumn(typeCol);
        typeCol.setPrintable(true);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_PROPERTY_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_PROPERTY_NAME_0));
        nameCol.setWidth("20%");
        metadata.addColumn(nameCol);
        nameCol.setPrintable(true);

        // add column for first value
        CmsListColumnDefinition version1Col = new CmsListColumnDefinition(LIST_COLUMN_VERSION_1);
        version1Col.setName(
            Messages.get().container(
                Messages.GUI_COMPARE_VERSION_1,
                CmsHistoryListUtil.getDisplayVersion(getParamVersion1(), getLocale())));
        version1Col.setWidth("35%");
        version1Col.setSorteable(false);
        metadata.addColumn(version1Col);
        version1Col.setPrintable(true);

        // add column for second value
        CmsListColumnDefinition version2Col = new CmsListColumnDefinition(LIST_COLUMN_VERSION_2);
        version2Col.setName(
            Messages.get().container(
                Messages.GUI_COMPARE_VERSION_1,
                CmsHistoryListUtil.getDisplayVersion(getParamVersion2(), getLocale())));
        version2Col.setWidth("35%");
        version2Col.setSorteable(false);
        metadata.addColumn(version2Col);
        version2Col.setPrintable(true);
    }

    /**
     *
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add event details
        CmsListItemDetails eventDetails = new CmsListItemDetails(LIST_IACTION_SHOW);
        eventDetails.setVisible(false);
        eventDetails.setShowActionName(Messages.get().container(Messages.GUI_COMPARE_SHOW_ALL_PROPERTIES_0));
        eventDetails.setHideActionName(Messages.get().container(Messages.GUI_COMPARE_HIDE_IDENTICAL_PROPERTIES_0));
        metadata.addItemDetails(eventDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no-op
    }
}