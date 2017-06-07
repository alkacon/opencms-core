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

package org.opencms.workplace.tools.searchindex;

import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.I_CmsSearchFieldMapping;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.tools.CmsToolDialog;

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
 * A list that displays all search indexes of the OpenCms installation and offers
 * operations on them.<p>
 *
 * @since 6.0.0
 */
public class CmsSearchIndexList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "ad";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_INDEXSOURCES = "ais";

    /** list action id constant. */
    public static final String LIST_ACTION_REBUILD = "ar";

    /** list action id constant. */
    public static final String LIST_ACTION_SEARCH = "as";

    /** list action id constant. */
    public static final String LIST_ACTION_SEARCHINDEX_OVERVIEW = "asio";

    /** list column id constant. */
    public static final String LIST_COLUMN_CONFIGURATION = "cc";

    /** list column id constant. */
    public static final String LIST_COLUMN_DELETE = "cad";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "cae";

    /** list column id constant. */
    public static final String LIST_COLUMN_INDEXSOURCES = "cis";

    /** list column id constant. */
    public static final String LIST_COLUMN_LOCALE = "cl";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_PROJECT = "cp";

    /** list column id constant. */
    public static final String LIST_COLUMN_REBUILD = "car";

    /** list column id constant. */
    public static final String LIST_COLUMN_REBUILDMODE = "cr";

    /** list column id constant. */
    public static final String LIST_COLUMN_SEARCH = "cas";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_FIELDCONFIGURATION = "df";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_INDEXSOURCE = "di";

    /** list id constant. */
    public static final String LIST_ID = "lssi";

    /** list action id constant. */
    public static final String LIST_MACTION_DELETE = "mad";

    /** list action id constant. */
    public static final String LIST_MACTION_REBUILD = "mar";

    /** The path to the searchindex list icon (edit column). */
    protected static final String LIST_ICON_INDEX = "tools/searchindex/icons/small/searchindex.png";

    /** The path to the indexsource list icon. */
    protected static final String LIST_ICON_INDEXSOURCE = "tools/searchindex/icons/small/indexsource.png";

    /** The path to the rebuild multiple indexes icon. */
    protected static final String LIST_ICON_REBUILD_MULTI = "tools/searchindex/icons/small/multi-rebuild.png";

    /** The path to the rebuild single indexes icon. */
    protected static final String LIST_ICON_REBUILD_SINGLE = "tools/searchindex/icons/small/rebuild.png";

    /** The path to the search (within indexsource) list icon. */
    protected static final String LIST_ICON_SEARCH = "buttons/preview.png";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSearchIndexList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID, Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_NAME_0));
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public CmsSearchIndexList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        this(jsp, listId, listName, LIST_COLUMN_NAME, CmsListOrderEnum.ORDER_ASCENDING, null);
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
    public CmsSearchIndexList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        String sortedColId,
        CmsListOrderEnum sortOrder,
        String searchableColId) {

        super(jsp, listId, listName, sortedColId, sortOrder, searchableColId);

    }

    /**
     * Public constructor.<p>
     *
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSearchIndexList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws IOException, ServletException, CmsRuntimeException {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        Iterator<CmsListItem> itItems = getSelectedItems().iterator();
        if (getParamListAction().equals(LIST_MACTION_DELETE)) {
            // execute the delete multiaction
            List<String> removedItems = new ArrayList<String>();
            while (itItems.hasNext()) {
                CmsListItem listItem = itItems.next();
                searchManager.removeSearchIndex(searchManager.getIndex((String)listItem.get(LIST_COLUMN_NAME)));
                removedItems.add(listItem.getId());
            }
            writeConfiguration(false);
        } else if (getParamListAction().equals(LIST_MACTION_REBUILD)) {
            // execute the rebuild multiaction
            StringBuffer items = new StringBuffer();
            while (itItems.hasNext()) {
                CmsListItem listItem = itItems.next();
                items.append(listItem.getId());
                if (itItems.hasNext()) {
                    items.append(',');
                }
            }
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(CmsRebuildReport.PARAM_INDEXES, new String[] {items.toString()});
            params.put(PARAM_ACTION, new String[] {DIALOG_INITIAL});
            params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
            getToolManager().jspForwardTool(this, "/searchindex/singleindex/rebuildreport", params);
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        String index = getSelectedItem().getId();

        getCms().getRequestContext().setAttribute(A_CmsEditSearchIndexDialog.PARAM_INDEXNAME, index);

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(CmsRebuildReport.PARAM_INDEXES, new String[] {index});
        params.put(PARAM_ACTION, new String[] {DIALOG_INITIAL});
        params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
        params.put(A_CmsEditSearchIndexDialog.PARAM_INDEXNAME, new String[] {index});

        String action = getParamListAction();
        if (action.equals(LIST_ACTION_DELETE)) {
            searchManager.removeSearchIndex(searchManager.getIndex(index));
            writeConfiguration(false);
        } else if (action.equals(LIST_ACTION_REBUILD)) {
            // forward to the rebuild index screen
            getToolManager().jspForwardTool(this, "/searchindex/singleindex/rebuildreport", params);
        } else if (action.equals(LIST_ACTION_SEARCH)) {
            // forward to the search screen
            getToolManager().jspForwardTool(this, "/searchindex/singleindex/search", params);
        } else if (action.equals(LIST_ACTION_EDIT)) {
            // forward to the edit index screen
            params.remove(PARAM_ACTION);
            getToolManager().jspForwardTool(this, "/searchindex/singleindex/edit", params);
        } else if (action.equals(LIST_ACTION_SEARCHINDEX_OVERVIEW)) {
            // forward to the index overview screen
            getToolManager().jspForwardTool(this, "/searchindex/singleindex", params);
        } else if (action.equals(LIST_ACTION_INDEXSOURCES)) {
            // forward to the index source assignment screen
            getToolManager().jspForwardTool(this, "/searchindex/singleindex/indexsources", params);
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List<CmsListItem> items = getList().getAllContent();
        Iterator<CmsListItem> itItems = items.iterator();
        CmsListItem item;
        while (itItems.hasNext()) {
            item = itItems.next();
            if (detailId.equals(LIST_DETAIL_INDEXSOURCE)) {
                fillDetailIndexSource(item, detailId);
            } else if (detailId.equals(LIST_DETAIL_FIELDCONFIGURATION)) {
                fillDetailFieldConfiguration(item, detailId);
            }
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() {

        List<CmsListItem> result = new ArrayList<CmsListItem>();
        // get content
        List<CmsSearchIndex> indexes = OpenCms.getSearchManager().getSearchIndexesAll();
        for (CmsSearchIndex index : indexes) {
            CmsListItem item = getList().newItem(index.getName());
            item.set(LIST_COLUMN_NAME, index.getName());
            item.set(LIST_COLUMN_CONFIGURATION, index.getFieldConfiguration().getName());
            item.set(LIST_COLUMN_REBUILDMODE, index.getRebuildMode());
            item.set(LIST_COLUMN_PROJECT, index.getProject());
            item.set(LIST_COLUMN_LOCALE, index.getLocale().toString());
            result.add(item);
        }
        return result;
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
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for edit
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_EDIT_NAME_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_EDIT_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        editCol.setSorteable(false);
        // add edit action
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_EDIT_HELP_0));
        editAction.setIconPath(LIST_ICON_INDEX);
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // create column for indexsource assignment
        CmsListColumnDefinition sourceCol = new CmsListColumnDefinition(LIST_COLUMN_INDEXSOURCES);
        sourceCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_INDEXSOURCES_NAME_0));
        sourceCol.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_INDEXSOURCES_NAME_HELP_0));
        sourceCol.setWidth("20");
        sourceCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        sourceCol.setSorteable(false);
        // add assign indexsource action
        CmsListDirectAction sourceAction = new CmsListDirectAction(LIST_ACTION_INDEXSOURCES);
        sourceAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_INDEXSOURCES_NAME_0));
        sourceAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_INDEXSOURCES_NAME_HELP_0));
        sourceAction.setIconPath(LIST_ICON_INDEXSOURCE);
        sourceCol.addDirectAction(sourceAction);
        // add it to the list definition
        metadata.addColumn(sourceCol);

        // create column for deletion
        CmsListColumnDefinition deleteCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        deleteCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_DELETE_NAME_0));
        deleteCol.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_DELETE_HELP_0));
        deleteCol.setWidth("20");
        deleteCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        deleteCol.setSorteable(false);
        // add delete action
        CmsListDirectAction deleteAction = new CmsListDirectAction(LIST_ACTION_DELETE);
        deleteAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_DELETE_NAME_0));
        deleteAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_DELETE_HELP_0));
        deleteAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_DELETE_CONF_0));
        deleteAction.setIconPath(ICON_DELETE);
        deleteCol.addDirectAction(deleteAction);
        // add it to the list definition
        metadata.addColumn(deleteCol);

        // rebuild column
        CmsListColumnDefinition rebuildCol = new CmsListColumnDefinition(LIST_COLUMN_REBUILD);
        rebuildCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_REBUILD_NAME_0));
        rebuildCol.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_REBUILD_HELP_0));
        rebuildCol.setWidth("20");
        rebuildCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        rebuildCol.setSorteable(false);
        // add search action
        CmsListDirectAction rebuildAction = new CmsListDirectAction(LIST_ACTION_REBUILD);
        rebuildAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_REBUILD_NAME_0));
        rebuildAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_REBUILD_HELP_0));
        rebuildAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_REBUILD_CONF_0));
        rebuildAction.setIconPath(LIST_ICON_REBUILD_SINGLE);
        rebuildCol.addDirectAction(rebuildAction);
        // add it to the list definition
        metadata.addColumn(rebuildCol);

        // search column
        CmsListColumnDefinition searchCol = new CmsListColumnDefinition(LIST_COLUMN_SEARCH);
        searchCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_SEARCH_NAME_0));
        searchCol.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_SEARCH_HELP_0));
        searchCol.setWidth("20");
        searchCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        searchCol.setSorteable(false);
        // add search action
        CmsListDirectAction searchAction = new CmsListDirectAction(LIST_ACTION_SEARCH) {

            @Override
            public boolean isEnabled() {

                return CmsSearchManager.isLuceneIndex((String)getItem().get(LIST_COLUMN_NAME));
            }
        };
        searchAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_SEARCH_NAME_0));
        searchAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_ACTION_SEARCH_HELP_0));
        searchAction.setIconPath(LIST_ICON_SEARCH);
        searchCol.addDirectAction(searchAction);
        // add it to the list definition
        metadata.addColumn(searchCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_NAME_0));
        nameCol.setWidth("35%");
        // a default action for the link to overview
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(LIST_ACTION_SEARCHINDEX_OVERVIEW);
        defEditAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_OVERVIEW_NAME_0));
        defEditAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_OVERVIEW_HELP_0));
        nameCol.addDefaultAction(defEditAction);
        metadata.addColumn(nameCol);

        // add column for field configuration
        CmsListColumnDefinition configCol = new CmsListColumnDefinition(LIST_COLUMN_CONFIGURATION);
        configCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        configCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_CONFIGURATION_0));
        configCol.setWidth("15%");
        metadata.addColumn(configCol);

        // add column for rebuild mode
        CmsListColumnDefinition rebuildModeCol = new CmsListColumnDefinition(LIST_COLUMN_REBUILDMODE);
        rebuildModeCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        rebuildModeCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_REBUILDMODE_0));
        rebuildModeCol.setWidth("5%");
        metadata.addColumn(rebuildModeCol);

        // add column for project
        CmsListColumnDefinition projectCol = new CmsListColumnDefinition(LIST_COLUMN_PROJECT);
        projectCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        projectCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_PROJECT_0));
        projectCol.setWidth("30%");
        metadata.addColumn(projectCol);

        // add column for locale
        CmsListColumnDefinition localeCol = new CmsListColumnDefinition(LIST_COLUMN_LOCALE);
        localeCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        localeCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_LOCALE_0));
        localeCol.setWidth("10%");
        metadata.addColumn(localeCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add index source details
        CmsListItemDetails indexDetails = new CmsListItemDetails(LIST_DETAIL_INDEXSOURCE);
        indexDetails.setAtColumn(LIST_COLUMN_NAME);
        indexDetails.setVisible(false);
        indexDetails.setShowActionName(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_INDEXSOURCE_SHOW_0));
        indexDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_INDEXSOURCE_SHOW_HELP_0));
        indexDetails.setHideActionName(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_INDEXSOURCE_HIDE_0));
        indexDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_INDEXSOURCE_HIDE_HELP_0));
        indexDetails.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_INDEXSOURCE_NAME_0));
        indexDetails.setFormatter(
            new CmsListItemDetailsFormatter(
                Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_INDEXSOURCE_NAME_0)));
        metadata.addItemDetails(indexDetails);

        // add field configuration details
        CmsListItemDetails configDetails = new CmsListItemDetails(LIST_DETAIL_FIELDCONFIGURATION);
        configDetails.setAtColumn(LIST_COLUMN_NAME);
        configDetails.setVisible(false);
        configDetails.setShowActionName(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_FIELDCONFIGURATION_SHOW_0));
        configDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_FIELDCONFIGURATION_SHOW_HELP_0));
        configDetails.setHideActionName(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_FIELDCONFIGURATION_HIDE_0));
        configDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_FIELDCONFIGURATION_HIDE_HELP_0));
        configDetails.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_FIELDCONFIGURATION_NAME_0));
        configDetails.setFormatter(
            new CmsListItemDetailsFormatter(
                Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_DETAIL_FIELDCONFIGURATION_NAME_0)));
        metadata.addItemDetails(configDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add delete multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETE);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_DELETE_NAME_0));
        deleteMultiAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_DELETE_HELP_0));
        deleteMultiAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_DELETE_CONF_0));
        deleteMultiAction.setIconPath(ICON_MULTI_DELETE);
        metadata.addMultiAction(deleteMultiAction);

        // add rebuild multi action
        CmsListMultiAction rebuildMultiAction = new CmsListMultiAction(LIST_MACTION_REBUILD);
        rebuildMultiAction.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_REBUILD_NAME_0));
        rebuildMultiAction.setHelpText(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_REBUILD_HELP_0));
        rebuildMultiAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_MACTION_REBUILD_CONF_0));
        rebuildMultiAction.setIconPath(LIST_ICON_REBUILD_MULTI);
        metadata.addMultiAction(rebuildMultiAction);

    }

    /**
     * Writes the updated search configuration back to the XML
     * configuration file and refreshes the complete list.<p>
     *
     * @param refresh if true, the list items are refreshed
     */
    protected void writeConfiguration(boolean refresh) {

        // update the XML configuration
        OpenCms.writeConfiguration(CmsSearchConfiguration.class);
        if (refresh) {
            refreshList();
        }
    }

    /**
     * Fills details of the field configuration into the given item. <p>
     *
     * @param item the list item to fill
     * @param detailId the id for the detail to fill
     */
    private void fillDetailFieldConfiguration(CmsListItem item, String detailId) {

        // search for the corresponding A_CmsSearchIndex:
        String idxName = (String)item.get(LIST_COLUMN_NAME);
        CmsSearchIndex idx = OpenCms.getSearchManager().getIndex(idxName);
        if (idx != null) {
            StringBuffer html = new StringBuffer();
            CmsSearchFieldConfiguration idxFieldConfiguration = idx.getFieldConfiguration();
            List<CmsSearchField> fields = idxFieldConfiguration.getFields();

            html.append("<ul>\n");
            html.append("  <li>\n").append("    ").append("name      : ").append(
                idxFieldConfiguration.getName()).append("\n");
            html.append("  </li>");
            html.append("  <li>\n").append("    ").append("fields : ").append("\n");
            html.append("    <ul>\n");

            Iterator<CmsSearchField> itFields = fields.iterator();
            while (itFields.hasNext()) {
                CmsLuceneField field = (CmsLuceneField)itFields.next();
                String fieldName = field.getName();
                boolean fieldStore = field.isStored();
                String fieldIndex = field.getIndexed();
                boolean fieldExcerpt = field.isInExcerpt();
                float fieldBoost = field.getBoost();
                String fieldDefault = field.getDefaultValue();

                html.append("  <li>\n").append("    ");
                html.append("name=").append(fieldName);
                if (fieldStore) {
                    html.append(", ").append("store=").append(fieldStore);
                }
                if (!fieldIndex.equals("false")) {
                    html.append(", ").append("index=").append(fieldIndex);
                }
                if (fieldExcerpt) {
                    html.append(", ").append("excerpt=").append(fieldExcerpt);
                }
                if (fieldBoost != CmsSearchField.BOOST_DEFAULT) {
                    html.append(", ").append("boost=").append(fieldBoost);
                }
                if (fieldDefault != null) {
                    html.append(", ").append("default=").append(field.getDefaultValue());
                }
                html.append("\n").append("    <ul>\n");

                Iterator<I_CmsSearchFieldMapping> itMappings = field.getMappings().iterator();
                while (itMappings.hasNext()) {
                    CmsSearchFieldMapping mapping = (CmsSearchFieldMapping)itMappings.next();
                    html.append("  <li>\n").append("    ");
                    html.append(mapping.getType().toString());
                    if (CmsStringUtil.isNotEmpty(mapping.getParam())) {
                        html.append("=").append(mapping.getParam()).append("\n");
                    }
                    html.append("  </li>");
                }
                html.append("    </ul>\n");
                html.append("  </li>");
            }
            html.append("    </ul>\n");
            html.append("  </li>");
            html.append("</ul>\n");
            item.set(detailId, html.toString());
        }
    }

    /**
     * Fills details of the index source into the given item. <p>
     *
     * @param item the list item to fill
     * @param detailId the id for the detail to fill
     *
     */
    private void fillDetailIndexSource(CmsListItem item, String detailId) {

        StringBuffer html = new StringBuffer();
        // search for the corresponding A_CmsSearchIndex:
        CmsSearchIndex idx = OpenCms.getSearchManager().getIndex((String)item.get(LIST_COLUMN_NAME));

        html.append("<ul>\n");
        // get the index sources (nice API)
        for (CmsSearchIndexSource idxSource : idx.getSources()) {
            html.append("  <li>\n").append("    ").append("name      : ").append(idxSource.getName()).append("\n");
            html.append("  </li>");

            html.append("  <li>\n").append("    ").append("indexer   : ").append(
                idxSource.getIndexerClassName()).append("\n");
            html.append("  </li>");

            html.append("  <li>\n").append("    ").append("resources : ").append("\n");
            html.append("    <ul>\n");
            List<String> resources = idxSource.getResourcesNames();
            Iterator<String> itResources = resources.iterator();
            while (itResources.hasNext()) {
                html.append("    <li>\n").append("      ").append(itResources.next()).append("\n");
                html.append("    </li>\n");
            }
            html.append("    </ul>\n");
            html.append("  </li>");

            html.append("  <li>\n").append("    ").append("doctypes : ").append("\n");
            html.append("    <ul>\n");
            resources = idxSource.getDocumentTypes();
            itResources = resources.iterator();
            while (itResources.hasNext()) {
                html.append("    <li>\n").append("      ").append(itResources.next()).append("\n");
                html.append("    </li>\n");
            }
            html.append("    </ul>\n");
            html.append("  </li>");
        }

        html.append("</ul>\n");
        item.set(detailId, html.toString());
    }
}