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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.commons.CmsHistoryList;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListIndependentAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.xml.types.CmsXmlDateTimeValue;

import java.io.IOException;
import java.text.DateFormat;
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

import org.apache.commons.logging.Log;

/**
 * Element comparison list view. <p>
 *
 * @since 6.0.0
 */
public class CmsElementComparisonList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_ICON = "ai";

    /** list action id constant. */
    public static final String LIST_ACTION_STATUS = "at";

    /** list column id constant. */
    public static final String LIST_COLUMN_ATTRIBUTE = "ca";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOCALE = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_STATUS = "cs";

    /** list column id constant. */
    public static final String LIST_COLUMN_TYPE = "cy";

    /** list column id constant. */
    public static final String LIST_COLUMN_VERSION_1 = "cv";

    /** list column id constant. */
    public static final String LIST_COLUMN_VERSION_2 = "cw";

    /** list default action id constant. */
    public static final String LIST_DEFACTION_VIEW = "dv";

    /** list independent action id constant. */
    public static final String LIST_DETAIL_TYPE = "dt";

    /** list independent action id constant. */
    public static final String LIST_IACTION_COMPARE_ALL = "ava";

    /** list independent action id constant. */
    public static final String LIST_IACTION_SHOW = "isy";

    /** List id constant. */
    public static final String LIST_ID = "hiecl";

    /** Request parameter name for the element. */
    public static final String PARAM_ELEMENT = "element";

    /** Request parameter name for the locale. */
    public static final String PARAM_LOCALE = "locale";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsElementComparisonList.class);

    /** Parameter value for the structure id of the first file. */
    private String m_paramId1;

    /** Parameter value for the structure id of the second file. */
    private String m_paramId2;

    /** Parameter value for the version of the first file. */
    private String m_paramVersion1;

    /** Parameter value for the version of the second file. */
    private String m_paramVersion2;

    /** flag indicating whether xml contents are compared.<p> */
    private boolean m_xmlContentComparisonMode;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsElementComparisonList(CmsJspActionElement jsp) {

        this(LIST_ID, jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsElementComparisonList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Protected constructor.<p>
     *
     * @param listId the id of the specialized list
     * @param jsp an initialized JSP action element
     */
    protected CmsElementComparisonList(String listId, CmsJspActionElement jsp) {

        super(
            jsp,
            listId,
            Messages.get().container(Messages.GUI_COMPARE_CONTENT_0),
            LIST_COLUMN_LOCALE,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    public static String formatContentValueForDiffTable(
        CmsObject cms,
        CmsElementComparison comparison,
        String origValue) {

        String result = CmsStringUtil.escapeHtml(
            CmsStringUtil.substitute(
                CmsStringUtil.trimToSize(origValue, CmsPropertyComparisonList.TRIM_AT_LENGTH),
                "\n",
                ""));

        // formatting DateTime
        if (comparison instanceof CmsXmlContentElementComparison) {
            if (((CmsXmlContentElementComparison)comparison).getType().equals(CmsXmlDateTimeValue.TYPE_NAME)) {
                if (CmsStringUtil.isNotEmpty(result)) {

                    result = CmsDateUtil.getDateTime(
                        new Date(Long.parseLong(result)),
                        DateFormat.SHORT,
                        cms.getRequestContext().getLocale());
                }
            }
        }
        return result;
    }

    /**
     *
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListIndepActions()
     */
    @Override
    public void executeListIndepActions() {

        if (getParamListAction().equals(LIST_IACTION_COMPARE_ALL)) {
            // called if all elements are to be compared
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(CmsHistoryList.PARAM_VERSION_1, new String[] {getParamVersion1()});
            params.put(CmsHistoryList.PARAM_VERSION_2, new String[] {getParamVersion2()});
            params.put(CmsHistoryList.PARAM_ID_1, new String[] {getParamId1()});
            params.put(CmsHistoryList.PARAM_ID_2, new String[] {getParamId2()});
            params.put(
                CmsPropertyComparisonList.PARAM_COMPARE,
                new String[] {CmsResourceComparisonDialog.COMPARE_ALL_ELEMENTS});
            params.put(PARAM_RESOURCE, new String[] {getParamResource()});
            // forward to the element difference screen
            try {
                getToolManager().jspForwardTool(this, "/history/comparison/difference", params);
            } catch (Exception e) {
                LOG.debug(e.getMessage(), e);
            }

        }
        super.executeListIndepActions();
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

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(CmsHistoryList.PARAM_VERSION_1, new String[] {getParamVersion1()});
        params.put(CmsHistoryList.PARAM_VERSION_2, new String[] {getParamVersion2()});
        params.put(CmsHistoryList.PARAM_ID_1, new String[] {getParamId1()});
        params.put(CmsHistoryList.PARAM_ID_2, new String[] {getParamId2()});
        params.put(PARAM_LOCALE, new String[] {getSelectedItem().get(LIST_COLUMN_LOCALE).toString()});
        params.put(PARAM_ELEMENT, new String[] {getSelectedItem().get(LIST_COLUMN_ATTRIBUTE).toString()});
        params.put(PARAM_RESOURCE, new String[] {getParamResource()});
        // forward to the element difference screen
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

        // no-op
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> result = new ArrayList<CmsListItem>();
        CmsFile resource1 = CmsResourceComparisonDialog.readFile(
            getCms(),
            new CmsUUID(getParamId1()),
            getParamVersion1());
        CmsFile resource2 = CmsResourceComparisonDialog.readFile(
            getCms(),
            new CmsUUID(getParamId2()),
            getParamVersion2());
        Iterator<CmsElementComparison> diffs = new CmsXmlDocumentComparison(
            getCms(),
            resource1,
            resource2).getElements().iterator();
        while (diffs.hasNext()) {
            CmsElementComparison comparison = diffs.next();
            String locale = comparison.getLocale().toString();
            String attribute = comparison.getName();
            CmsListItem item = getList().newItem(locale + attribute);
            item.set(LIST_COLUMN_LOCALE, locale);
            item.set(LIST_COLUMN_ATTRIBUTE, attribute);
            if (comparison instanceof CmsXmlContentElementComparison) {
                m_xmlContentComparisonMode = true;
                item.set(LIST_COLUMN_TYPE, ((CmsXmlContentElementComparison)comparison).getType());
            }
            if (CmsResourceComparison.TYPE_ADDED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_STATUS, key(Messages.GUI_COMPARE_ADDED_0));
            } else if (CmsResourceComparison.TYPE_REMOVED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_STATUS, key(Messages.GUI_COMPARE_REMOVED_0));
            } else if (CmsResourceComparison.TYPE_CHANGED.equals(comparison.getStatus())) {
                item.set(LIST_COLUMN_STATUS, key(Messages.GUI_COMPARE_CHANGED_0));
            } else {
                if (!getList().getMetadata().getItemDetailDefinition(LIST_IACTION_SHOW).isVisible()) {
                    // do not display entry
                    continue;
                } else {
                    item.set(LIST_COLUMN_STATUS, key(Messages.GUI_COMPARE_UNCHANGED_0));
                }
            }
            String value1 = CmsStringUtil.escapeHtml(
                CmsStringUtil.substitute(
                    CmsStringUtil.trimToSize(comparison.getVersion1(), CmsPropertyComparisonList.TRIM_AT_LENGTH),
                    "\n",
                    ""));

            // formatting DateTime
            if (comparison instanceof CmsXmlContentElementComparison) {
                if (((CmsXmlContentElementComparison)comparison).getType().equals(CmsXmlDateTimeValue.TYPE_NAME)) {
                    if (CmsStringUtil.isNotEmpty(value1)) {
                        value1 = CmsDateUtil.getDateTime(
                            new Date(Long.parseLong(value1)),
                            DateFormat.SHORT,
                            getCms().getRequestContext().getLocale());
                    }
                }
            }
            item.set(LIST_COLUMN_VERSION_1, value1);

            String origValue = comparison.getVersion2();
            String value2 = formatContentValueForDiffTable(getCms(), comparison, origValue);
            item.set(LIST_COLUMN_VERSION_2, value2);
            result.add(item);
        }

        getList().getMetadata().getColumnDefinition(LIST_COLUMN_VERSION_1).setName(
            Messages.get().container(
                Messages.GUI_COMPARE_VERSION_1,
                CmsHistoryListUtil.getDisplayVersion(getParamVersion1(), getLocale())));
        getList().getMetadata().getColumnDefinition(LIST_COLUMN_VERSION_2).setName(Messages.get().container(
            Messages.GUI_COMPARE_VERSION_1,
            CmsHistoryListUtil.getDisplayVersion(getParamVersion2(), getLocale())));
        return result;
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

            @Override
            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_STATUS).toString();
                return key(Messages.GUI_COMPARE_ADDED_0).equals(type);
            }
        };
        addedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_ADDED_0));
        addedAction.setIconPath("tools/ex_history/buttons/added.png");
        addedAction.setEnabled(true);
        iconCol.addDirectAction(addedAction);

        // add state error action
        CmsListDirectAction removedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_REMOVED) {

            @Override
            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_STATUS).toString();
                return key(Messages.GUI_COMPARE_REMOVED_0).equals(type);
            }
        };
        removedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_REMOVED_0));
        removedAction.setIconPath("tools/ex_history/buttons/removed.png");
        removedAction.setEnabled(true);
        iconCol.addDirectAction(removedAction);

        // add state error action
        CmsListDirectAction changedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_CHANGED) {

            @Override
            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_STATUS).toString();
                return key(Messages.GUI_COMPARE_CHANGED_0).equals(type);
            }
        };
        changedAction.setName(Messages.get().container(Messages.GUI_COMPARE_ELEM_CHANGED_0));
        changedAction.setIconPath("tools/ex_history/buttons/changed.png");
        changedAction.setEnabled(true);
        iconCol.addDirectAction(changedAction);

        // add state error action
        CmsListDirectAction unchangedAction = new CmsListDirectAction(CmsResourceComparison.TYPE_UNCHANGED) {

            @Override
            public boolean isVisible() {

                String type = getItem().get(LIST_COLUMN_STATUS).toString();
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
        CmsListColumnDefinition statusCol = new CmsListColumnDefinition(LIST_COLUMN_STATUS);
        statusCol.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_STATUS_0));
        statusCol.setWidth("10%");

        CmsListDefaultAction statusColAction = new CmsListDefaultAction(LIST_ACTION_STATUS);
        statusColAction.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_STATUS_0));
        statusColAction.setEnabled(true);
        statusCol.addDefaultAction(statusColAction);
        metadata.addColumn(statusCol);
        statusCol.setPrintable(true);

        // add column for locale
        CmsListColumnDefinition localeCol = new CmsListColumnDefinition(LIST_COLUMN_LOCALE);
        localeCol.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_LOCALE_0));
        localeCol.setWidth("10%");
        metadata.addColumn(localeCol);
        localeCol.setPrintable(true);

        // add column for element name
        CmsListColumnDefinition attCol = new CmsListColumnDefinition(LIST_COLUMN_ATTRIBUTE);
        attCol.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_NAME_0));
        attCol.setWidth("10%");
        metadata.addColumn(attCol);
        attCol.setPrintable(true);

        CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_TYPE);
        typeCol.setName(Messages.get().container(Messages.GUI_COMPARE_COLS_TYPE_0));
        typeCol.setWidth("10%");
        // display column only if xml content is compared
        typeCol.setVisible(m_xmlContentComparisonMode);
        metadata.addColumn(typeCol);
        typeCol.setPrintable(true);

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
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        CmsListIndependentAction compare = new CmsListIndependentAction(LIST_IACTION_COMPARE_ALL);
        compare.setName(Messages.get().container(Messages.GUI_COMPARE_COMPARE_ALL_0));
        compare.setIconPath("tools/ex_history/buttons/compare.png");
        compare.setEnabled(true);
        metadata.addIndependentAction(compare);

        // add event details
        CmsListItemDetails eventDetails = new CmsListItemDetails(LIST_IACTION_SHOW);
        eventDetails.setVisible(false);
        eventDetails.setShowActionName(Messages.get().container(Messages.GUI_COMPARE_SHOW_ALL_ELEMENTS_0));
        eventDetails.setHideActionName(Messages.get().container(Messages.GUI_COMPARE_HIDE_IDENTICAL_ELEMENTS_0));
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