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
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * A list that displays information about the <code>{@link org.opencms.search.fields.CmsLuceneFieldConfiguration}</code>
 * that are members of the <code>{@link org.opencms.search.CmsSearchIndex}</code>
 * in the current request scope (param "searchindex").<p>
 *
 * This list is stand-alone displayable (not to embed in another dialog) and
 * offers single actions within the rows related to the current selected field configuration
 * which has to be found by the <b>request parameter <code></code></b>.
 *
 * @since 6.5.5
 */
public class CmsSearchFieldConfigurationList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_DELETE = "ade";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_FIELD = "af";

    /** list action id constant. */
    public static final String LIST_ACTION_OVERVIEW_FIELDCONFIGURATION = "aofc";

    /** list column id constant. */
    public static final String LIST_COLUMN_DELETE = "cde";

    /** list column id constant. */
    public static final String LIST_COLUMN_DESCRIPTION = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "ce";

    /** list column id constant. */
    public static final String LIST_COLUMN_FIELD = "cf";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_FIELDCONFIGURATION = "df";

    /** list id constant. */
    public static final String LIST_ID = "lsfc";

    /** list action id constant. */
    public static final String LIST_MACTION_DELETECONFIGURATION = "mad";

    /** The path to the fieldconfiguration list icon. */
    protected static final String LIST_ICON_FIELDCONFIGURATION_EDIT = "tools/searchindex/icons/small/fieldconfiguration-edit.png";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchFieldConfigurationList.class);

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSearchFieldConfigurationList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID, Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATIONS_NAME_0));
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public CmsSearchFieldConfigurationList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

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
    public CmsSearchFieldConfigurationList(
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
    public CmsSearchFieldConfigurationList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        if (getParamListAction().equals(LIST_MACTION_DELETECONFIGURATION)) {
            // execute the delete multiaction
            Iterator<CmsListItem> itItems = getSelectedItems().iterator();
            CmsListItem listItem;
            CmsSearchFieldConfiguration fieldconfig;
            while (itItems.hasNext()) {
                listItem = itItems.next();
                fieldconfig = searchManager.getFieldConfiguration((String)listItem.get(LIST_COLUMN_NAME));
                searchManager.removeSearchFieldConfiguration(fieldconfig);
            }
            refreshList();
            writeConfiguration(false);
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException {

        String fieldConfiguration = getSelectedItem().getId();
        Map<String, String[]> params = new HashMap<String, String[]>();
        String action = getParamListAction();

        params.put(A_CmsFieldConfigurationDialog.PARAM_FIELDCONFIGURATION, new String[] {fieldConfiguration});
        params.put(PARAM_ACTION, new String[] {DIALOG_INITIAL});
        params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
        if (action.equals(LIST_ACTION_EDIT)) {
            // forward to the edit indexsource screen
            getToolManager().jspForwardTool(this, "/searchindex/fieldconfigurations/fieldconfiguration/edit", params);
        } else if (action.equals(LIST_ACTION_FIELD)) {
            // forward to the new field screen
            getToolManager().jspForwardTool(
                this,
                "/searchindex/fieldconfigurations/fieldconfiguration/newfield",
                params);
        } else if (action.equals(LIST_ACTION_DELETE)) {
            // forward to the delete field configuration screen
            getToolManager().jspForwardTool(this, "/searchindex/fieldconfigurations/fieldconfiguration/delete", params);
        } else if (action.equals(LIST_ACTION_OVERVIEW_FIELDCONFIGURATION)) {
            // forward to the field configuration overview screen
            getToolManager().jspForwardTool(this, "/searchindex/fieldconfigurations/fieldconfiguration", params);
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
            if (detailId.equals(LIST_DETAIL_FIELDCONFIGURATION)) {
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
        CmsSearchManager manager = OpenCms.getSearchManager();

        // get content
        List<CmsSearchFieldConfiguration> configs = new LinkedList<CmsSearchFieldConfiguration>(
            manager.getFieldConfigurationsLucene());
        Iterator<CmsSearchFieldConfiguration> itConfigs = configs.iterator();
        CmsSearchFieldConfiguration config;
        while (itConfigs.hasNext()) {
            try {
                config = itConfigs.next();
                CmsListItem item = getList().newItem(config.getName());
                item.set(LIST_COLUMN_NAME, config.getName());
                item.set(LIST_COLUMN_DESCRIPTION, config.getDescription());
                result.add(item);
            } catch (Throwable g) {
                CmsMessageContainer msg = Messages.get().container(
                    Messages.LOG_ERR_LIST_ITEM_SKIPPED_2,
                    getList().getName().key(getLocale()),
                    "Name");
                if (LOG.isWarnEnabled()) {
                    LOG.warn(msg.key(getLocale()));
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for edit
        CmsListColumnDefinition editCol = new CmsListColumnDefinition(LIST_COLUMN_EDIT);
        editCol.setName(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_COL_EDIT_NAME_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_COL_EDIT_NAME_HELP_0));
        editCol.setWidth("5");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        editCol.setSorteable(false);
        // add dummy icon
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_COL_EDIT_NAME_HELP_0));
        editAction.setIconPath(LIST_ICON_FIELDCONFIGURATION_EDIT);
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // create column for new field
        CmsListColumnDefinition fieldCol = new CmsListColumnDefinition(LIST_COLUMN_FIELD);
        fieldCol.setName(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_COL_FIELD_NAME_0));
        fieldCol.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_COL_FIELD_NAME_HELP_0));
        fieldCol.setWidth("5");
        fieldCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        fieldCol.setSorteable(false);
        // add dummy icon
        CmsListDirectAction fieldAction = new CmsListDirectAction(LIST_ACTION_FIELD);
        fieldAction.setName(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_ACTION_FIELD_NAME_0));
        fieldAction.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_COL_FIELD_NAME_HELP_0));
        fieldAction.setIconPath(ICON_ADD);
        fieldCol.addDirectAction(fieldAction);
        // add it to the list definition
        metadata.addColumn(fieldCol);

        // create column for deletion
        CmsListColumnDefinition deleteCol = new CmsListColumnDefinition(LIST_COLUMN_DELETE);
        deleteCol.setName(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_COL_DELETE_NAME_0));
        deleteCol.setHelpText(Messages.get().container(Messages.GUI_GROUPS_FIELDCONFIGURATION_TOOL_DELETE_HELP_0));
        deleteCol.setWidth("10");
        deleteCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        deleteCol.setSorteable(false);
        // add delete action
        CmsListDirectAction deleteAction = new CmsListDirectAction(LIST_ACTION_DELETE);
        deleteAction.setName(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_ACTION_DELETE_NAME_0));
        deleteAction.setHelpText(Messages.get().container(Messages.GUI_GROUPS_FIELDCONFIGURATION_TOOL_DELETE_HELP_0));
        deleteAction.setIconPath(ICON_DELETE);
        deleteCol.addDirectAction(deleteAction);
        // add it to the list definition
        metadata.addColumn(deleteCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_NAME_0));
        nameCol.setWidth("50%");
        // add overview action
        CmsListDefaultAction defEditAction = new CmsListDefaultAction(LIST_ACTION_OVERVIEW_FIELDCONFIGURATION);
        defEditAction.setName(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_COL_OVERVIEW_NAME_0));
        defEditAction.setHelpText(
            Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_COL_OVERVIEW_NAME_HELP_0));
        nameCol.addDefaultAction(defEditAction);
        metadata.addColumn(nameCol);

        // add column for description
        CmsListColumnDefinition descriptionCol = new CmsListColumnDefinition(LIST_COLUMN_DESCRIPTION);
        descriptionCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        descriptionCol.setName(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_COL_DESCRIPTION_0));
        descriptionCol.setWidth("50%");
        metadata.addColumn(descriptionCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add field configuration details
        CmsListItemDetails configDetails = new CmsListItemDetails(LIST_DETAIL_FIELDCONFIGURATION);
        configDetails.setAtColumn(LIST_COLUMN_NAME);
        configDetails.setVisible(false);
        configDetails.setShowActionName(
            Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_DETAIL_FIELDS_SHOW_0));
        configDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_DETAIL_FIELDS_SHOW_HELP_0));
        configDetails.setHideActionName(
            Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_DETAIL_FIELDS_HIDE_0));
        configDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_DETAIL_FIELDS_HIDE_HELP_0));
        configDetails.setName(Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_DETAIL_FIELDS_NAME_0));
        configDetails.setFormatter(
            new CmsListItemDetailsFormatter(
                Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_DETAIL_FIELDS_NAME_0)));
        metadata.addItemDetails(configDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add add multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETECONFIGURATION);
        deleteMultiAction.setName(
            Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_MACTION_DELETECONFIGURATION_NAME_0));
        deleteMultiAction.setHelpText(
            Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_MACTION_DELETECONFIGURATION_NAME_HELP_0));
        deleteMultiAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_LIST_FIELDCONFIGURATION_MACTION_DELETECONFIGURATION_CONF_0));
        deleteMultiAction.setIconPath(ICON_MULTI_DELETE);
        metadata.addMultiAction(deleteMultiAction);
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

        StringBuffer html = new StringBuffer();
        // search for the corresponding A_CmsSearchIndex:
        String idxConfigName = (String)item.get(LIST_COLUMN_NAME);

        CmsSearchFieldConfiguration idxFieldConfiguration = OpenCms.getSearchManager().getFieldConfiguration(
            idxConfigName);
        List<CmsSearchField> fields = idxFieldConfiguration.getFields();

        html.append("<ul>\n");
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
        html.append("</ul>\n");
        item.set(detailId, html.toString());
    }
}
