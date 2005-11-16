/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsPropertyComparisonList.java,v $
 * Date   : $Date: 2005/11/16 12:12:55 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.workplace.comparison;

import org.opencms.file.CmsFile;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.commons.CmsHistoryList;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
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

/**
 * List for property comparison including columns for property name and the values. <p>
 * 
 * @author Jan Baudisch  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsPropertyComparisonList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

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

    private String m_paramTagId1;

    private String m_paramTagId2;

    private String m_paramVersion1;

    private String m_paramVersion2;

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
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        // forward to the edit module screen  
        Map params = new HashMap();
        params.put(CmsHistoryList.PARAM_TAGID_1, getParamTagId1());
        params.put(CmsHistoryList.PARAM_TAGID_2, getParamTagId2());
        params.put(CmsHistoryList.PARAM_VERSION_1, getParamVersion1());
        params.put(CmsHistoryList.PARAM_VERSION_2, getParamVersion2());
        params.put(PARAM_COMPARE, "properties");
        params.put(PARAM_RESOURCE, getParamResource());
        // forward to the difference screen
        getToolManager().jspForwardTool(this, "/history/comparison/difference", params);
        listSave();
    }

    /**
     * Returns the paramTagId1.<p>
     *
     * @return the paramTagId1
     */
    public String getParamTagId1() {

        return m_paramTagId1;
    }

    /**
     * Returns the paramTagId2.<p>
     *
     * @return the paramTagId2
     */
    public String getParamTagId2() {

        return m_paramTagId2;
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
     * Sets the paramTagId1.<p>
     *
     * @param paramTagId1 the paramTagId1 to set
     */
    public void setParamTagId1(String paramTagId1) {

        m_paramTagId1 = paramTagId1;
    }

    /**
     * Sets the paramTagId2.<p>
     *
     * @param paramTagId2 the paramTagId2 to set
     */
    public void setParamTagId2(String paramTagId2) {

        m_paramTagId2 = paramTagId2;
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
    protected void fillDetails(String detailId) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() throws CmsException {

        List ret = new ArrayList();
        CmsFile resource1;
        CmsFile resource2;
        if (CmsHistoryList.OFFLINE_PROJECT.equals(getParamVersion1())) {
            resource1 = getCms().readFile(getParamResource());
        } else {
            resource1 = getCms().readBackupFile(getParamResource(), Integer.parseInt(getParamTagId1()));
        }
        if (CmsHistoryList.OFFLINE_PROJECT.equals(getParamVersion2())) {
            resource2 = getCms().readFile(getParamResource());
        } else {
            resource2 = getCms().readBackupFile(getParamResource(), Integer.parseInt(getParamTagId2()));
        }
        Iterator diffs = new CmsXmlDocumentComparison(getCms(), resource1, resource2).getComparedProperties().iterator();
        while (diffs.hasNext()) {
            CmsAttributeComparison comparison = (CmsAttributeComparison)diffs.next();
            CmsListItem item = getList().newItem(comparison.getName());
            item.set(LIST_COLUMN_PROPERTY_NAME, comparison.getName());
            item.set(LIST_COLUMN_VERSION_1, CmsStringUtil.trimToSize(comparison.getVersion1(), TRIM_AT_LENGTH));
            item.set(LIST_COLUMN_VERSION_2, CmsStringUtil.trimToSize(comparison.getVersion2(), TRIM_AT_LENGTH));
            item.set(LIST_COLUMN_TYPE, comparison.getType());
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
        iconCol.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_ICON_0));
        iconCol.setWidth("20");
        iconCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        iconCol.setSorteable(true);

        // add state error action
        CmsListDirectAction addedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_ADDED) {

            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_TYPE).toString();
                return CmsResourceComparison.TYPE_ADDED.equals(type);
            }
        };
        addedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_ADDED_0));
        addedAction.setIconPath("tools/history/buttons/added.png");
        addedAction.setEnabled(true);
        iconCol.addDirectAction(addedAction);

        // add state error action
        CmsListDirectAction removedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_REMOVED) {

            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_TYPE).toString();
                return CmsResourceComparison.TYPE_REMOVED.equals(type);
            }
        };
        removedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_REMOVED_0));
        removedAction.setIconPath("tools/history/buttons/removed.png");
        removedAction.setEnabled(true);
        iconCol.addDirectAction(removedAction);

        // add state error action
        CmsListDirectAction changedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_CHANGED) {

            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_TYPE).toString();
                return CmsResourceComparison.TYPE_CHANGED.equals(type);
            }
        };
        changedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_CHANGED_0));
        changedAction.setIconPath("tools/history/buttons/changed.png");
        changedAction.setEnabled(true);
        iconCol.addDirectAction(changedAction);

        // add state error action
        CmsListDirectAction unchangedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_UNCHANGED) {

            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_TYPE).toString();
                return CmsResourceComparison.TYPE_UNCHANGED.equals(type);
            }
        };
        unchangedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_UNCHANGED_0));
        unchangedAction.setIconPath("tools/history/buttons/unchanged.png");
        unchangedAction.setEnabled(true);
        iconCol.addDirectAction(unchangedAction);
        metadata.addColumn(iconCol);

        // add column for type
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_TYPE);
        typeCol.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_STATUS_0));
        typeCol.setWidth("10%");
        metadata.addColumn(typeCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_PROPERTY_NAME);
        nameCol.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_PROPERTY_NAME_0));
        nameCol.setWidth("20%");
        metadata.addColumn(nameCol);

        // add column for first value
        CmsListColumnDefinition version1Col = new CmsListColumnDefinition(LIST_COLUMN_VERSION_1);
        version1Col.setName(Messages.get().container(Messages.GUI_COMPARE_VERSION_1, getParamVersion1()));
        version1Col.setWidth("35%");
        version1Col.setSorteable(false);
        metadata.addColumn(version1Col);

        // add column for second value
        CmsListColumnDefinition version2Col = new CmsListColumnDefinition(LIST_COLUMN_VERSION_2);
        version2Col.setName(Messages.get().container(Messages.GUI_COMPARE_VERSION_1, getParamVersion2()));
        version2Col.setWidth("35%");
        version2Col.setSorteable(false);
        metadata.addColumn(version2Col);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setIndependentActions(CmsListMetadata metadata) {

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // no-op
    }
}