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
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.I_CmsSearchFieldMapping;
import org.opencms.util.CmsStringUtil;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;

/**
 * A list that displays the fields of a request parameter given
 * <code>{@link org.opencms.search.fields.CmsLuceneFieldConfiguration}</code> ("fieldconfiguration").
 *
 * This list is no stand-alone page but has to be embedded in another dialog
 * (see <code> {@link org.opencms.workplace.tools.searchindex.A_CmsEmbeddedListDialog}</code>. <p>
 *
 * @since 6.5.5
 */
public class CmsFieldsList extends A_CmsEmbeddedListDialog {

    /** Standard list button location. */
    public static final String ICON_FALSE = "list/multi_deactivate.png";

    /** Standard list button location. */
    public static final String ICON_TRUE = "list/multi_activate.png";

    /** list action id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list action id constant. */
    public static final String LIST_ACTION_EXCERPT_FALSE = "aef";

    /** list action id constant. */
    public static final String LIST_ACTION_EXCERPT_TRUE = "aet";

    /** list action id constant. */
    public static final String LIST_ACTION_INDEX_FALSE = "aif";

    /** list action id constant. */
    public static final String LIST_ACTION_INDEX_TRUE = "ait";

    /** list action id constant. */
    public static final String LIST_ACTION_MAPPING = "am";

    /** list action id constant. */
    public static final String LIST_ACTION_OVERVIEW_FIELD = "aof";

    /** list action id constant. */
    public static final String LIST_ACTION_STORE_FALSE = "asf";

    /** list action id constant. */
    public static final String LIST_ACTION_STORE_TRUE = "ast";

    /** list column id constant. */
    public static final String LIST_COLUMN_BOOST = "cb";

    /** list column id constant. */
    public static final String LIST_COLUMN_DEFAULT = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_DISPLAY = "cdi";

    /** list column id constant. */
    public static final String LIST_COLUMN_EDIT = "ced";

    /** list column id constant. */
    public static final String LIST_COLUMN_EXCERPT = "ce";

    /** list column id constant. */
    public static final String LIST_COLUMN_EXCERPT_HIDE = "ceh";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_INDEX = "cx";

    /** list column id constant. */
    public static final String LIST_COLUMN_MAPPING = "cm";

    /** list column id constant. */
    public static final String LIST_COLUMN_NAME = "cn";

    /** list column id constant. */
    public static final String LIST_COLUMN_STORE = "cs";

    /** list column id constant. */
    public static final String LIST_COLUMN_STORE_HIDE = "csh";

    /** list item detail id constant. */
    public static final String LIST_DETAIL_FIELD = "df";

    /** list id constant. */
    public static final String LIST_ID = "lsfcf";

    /** list action id constant. */
    public static final String LIST_MACTION_DELETEFIELD = "mad";

    /** The path to the fieldconfiguration list icon. */
    protected static final String LIST_ICON_FIELD_EDIT = "tools/searchindex/icons/small/fieldconfiguration-editfield.png";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFieldsList.class);

    /** Stores the value of the request parameter for the search index source name. */
    private String m_paramFieldconfiguration;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsFieldsList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID, Messages.get().container(Messages.GUI_LIST_FIELDS_NAME_0));
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public CmsFieldsList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

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
    public CmsFieldsList(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        String sortedColId,
        CmsListOrderEnum sortOrder,
        String searchableColId) {

        super(jsp, listId, listName, sortedColId, sortOrder, searchableColId);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        CmsSearchManager searchManager = OpenCms.getSearchManager();
        if (getParamListAction().equals(LIST_MACTION_DELETEFIELD)) {
            // execute the delete multiaction
            Iterator<CmsListItem> itItems = getSelectedItems().iterator();
            CmsListItem listItem;
            CmsLuceneField field;
            List<CmsSearchField> deleteFields = new ArrayList<CmsSearchField>();
            List<CmsSearchField> fields = searchManager.getFieldConfiguration(m_paramFieldconfiguration).getFields();
            Iterator<CmsSearchField> itFields;

            while (itItems.hasNext()) {
                listItem = itItems.next();
                itFields = fields.iterator();
                while (itFields.hasNext()) {
                    String item = (String)listItem.get(LIST_COLUMN_NAME);
                    CmsLuceneField curField = (CmsLuceneField)itFields.next();
                    String fieldName = curField.getName();
                    if (item.equals(fieldName)) {
                        deleteFields.add(curField);
                    }
                }

            }

            itFields = deleteFields.iterator();
            while (itFields.hasNext()) {
                field = (CmsLuceneField)itFields.next();
                searchManager.removeSearchFieldConfigurationField(
                    searchManager.getFieldConfiguration(m_paramFieldconfiguration),
                    field);
            }

            refreshList();
            if (checkWriteConfiguration(fields)) {
                writeConfiguration(false);
            }
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws ServletException, IOException {

        String field = getSelectedItem().getId();
        Map<String, String[]> params = new HashMap<String, String[]>();
        String action = getParamListAction();

        CmsSearchFieldConfiguration fieldConfig = OpenCms.getSearchManager().getFieldConfiguration(
            m_paramFieldconfiguration);
        Iterator<CmsSearchField> itFields = fieldConfig.getFields().iterator();
        CmsLuceneField fieldObject = null;
        while (itFields.hasNext()) {
            CmsLuceneField curField = (CmsLuceneField)itFields.next();
            if (curField.getName().equals(field)) {
                fieldObject = curField;
            }
        }

        params.put(A_CmsFieldDialog.PARAM_FIELD, new String[] {field});
        params.put(A_CmsFieldDialog.PARAM_FIELDCONFIGURATION, new String[] {m_paramFieldconfiguration});
        params.put(PARAM_ACTION, new String[] {DIALOG_INITIAL});
        params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
        if (action.equals(LIST_ACTION_EDIT)) {
            // forward to the edit indexsource screen
            getToolManager().jspForwardTool(
                this,
                "/searchindex/fieldconfigurations/fieldconfiguration/field/edit",
                params);
        } else if (action.equals(LIST_ACTION_MAPPING)) {
            // forward to the new mapping screen
            getToolManager().jspForwardTool(
                this,
                "/searchindex/fieldconfigurations/fieldconfiguration/field/newmapping",
                params);
        } else if (action.equals(LIST_ACTION_OVERVIEW_FIELD)) {
            // forward to the field configuration overview screen
            getToolManager().jspForwardTool(this, "/searchindex/fieldconfigurations/fieldconfiguration/field", params);
        } else if (action.equals(LIST_ACTION_EXCERPT_FALSE)) {
            // execute the excerpt false action
            if (fieldObject != null) {
                fieldObject.setInExcerpt(true);
                writeConfiguration(true);
            }
        } else if (action.equals(LIST_ACTION_INDEX_FALSE)) {
            // execute the excerpt false action
            if (fieldObject != null) {
                fieldObject.setIndexed(true);
                writeConfiguration(true);
            }
        } else if (action.equals(LIST_ACTION_STORE_FALSE)) {
            // execute the excerpt false action
            if (fieldObject != null) {
                fieldObject.setStored(true);
                writeConfiguration(true);
            }
        } else if (action.equals(LIST_ACTION_EXCERPT_TRUE)) {
            // execute the excerpt false action
            if (fieldObject != null) {
                fieldObject.setInExcerpt(false);
                writeConfiguration(true);
            }
        } else if (action.equals(LIST_ACTION_INDEX_TRUE)) {
            // execute the excerpt false action
            if (fieldObject != null) {
                fieldObject.setIndexed(false);
                writeConfiguration(true);
            }
        } else if (action.equals(LIST_ACTION_STORE_TRUE)) {
            // execute the excerpt false action
            if (fieldObject != null) {
                fieldObject.setStored(false);
                writeConfiguration(true);
            }
        }
        listSave();
    }

    /**
     * Returns the request parameter "fieldconfiguration".<p>
     *
     * @return the request parameter "fieldconfiguration"
     */
    public String getParamFieldconfiguration() {

        return m_paramFieldconfiguration;
    }

    /**
     * Sets the request parameter "fieldconfiguration". <p>
     *
     * Method intended for workplace-properietary automatic filling of
     * request parameter values to dialogs, not for manual invocation. <p>
     *
     * @param fieldconfiguration the request parameter "fieldconfiguration" to set
     */
    public void setParamFieldconfiguration(String fieldconfiguration) {

        m_paramFieldconfiguration = fieldconfiguration;
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
            if (detailId.equals(LIST_DETAIL_FIELD)) {
                fillDetailField(item, detailId);
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
        List<CmsSearchField> fields = getFields();
        Iterator<CmsSearchField> itFields = fields.iterator();
        CmsLuceneField field;
        while (itFields.hasNext()) {
            field = (CmsLuceneField)itFields.next();
            CmsListItem item = getList().newItem(field.getName());
            String defaultValue = field.getDefaultValue();
            if (defaultValue == null) {
                defaultValue = "-";
            }
            item.set(LIST_COLUMN_NAME, field.getName());
            item.set(LIST_COLUMN_DISPLAY, resolveMacros(field.getDisplayName()));
            item.set(LIST_COLUMN_BOOST, new Float(field.getBoost()).toString());
            item.set(LIST_COLUMN_INDEX, field.getIndexed());
            item.set(LIST_COLUMN_EXCERPT_HIDE, Boolean.valueOf(field.isInExcerpt()));
            item.set(LIST_COLUMN_STORE_HIDE, Boolean.valueOf(field.isStored()));
            item.set(LIST_COLUMN_DEFAULT, defaultValue);

            result.add(item);
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
        editCol.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_EDIT_NAME_0));
        editCol.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELD_COL_EDIT_NAME_HELP_0));
        editCol.setWidth("20");
        editCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        editCol.setSorteable(false);
        // add dummy icon
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELD_COL_EDIT_NAME_HELP_0));
        editAction.setIconPath(LIST_ICON_FIELD_EDIT);
        editCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(editCol);

        // create column for new mapping
        CmsListColumnDefinition mappingCol = new CmsListColumnDefinition(LIST_COLUMN_MAPPING);
        mappingCol.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_MAPPING_0));
        mappingCol.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELD_COL_MAPPING_HELP_0));
        mappingCol.setWidth("20");
        mappingCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        mappingCol.setSorteable(false);
        // add mapping action
        CmsListDirectAction mappingAction = new CmsListDirectAction(LIST_ACTION_MAPPING);
        mappingAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_MAPPING_0));
        mappingAction.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELD_COL_MAPPING_HELP_0));
        mappingAction.setIconPath(ICON_ADD);
        mappingCol.addDirectAction(mappingAction);
        // add it to the list definition
        metadata.addColumn(mappingCol);

        // add column for name
        CmsListColumnDefinition nameCol = new CmsListColumnDefinition(LIST_COLUMN_NAME);
        nameCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        nameCol.setName(Messages.get().container(Messages.GUI_LIST_SEARCHINDEX_COL_NAME_0));
        nameCol.setSorteable(true);
        nameCol.setWidth("45%");
        // add overview action
        CmsListDefaultAction overviewAction = new CmsListDefaultAction(LIST_ACTION_OVERVIEW_FIELD);
        overviewAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_OVERVIEW_NAME_0));
        overviewAction.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELD_COL_OVERVIEW_NAME_HELP_0));
        nameCol.addDefaultAction(overviewAction);
        metadata.addColumn(nameCol);

        // add column for display
        CmsListColumnDefinition displayCol = new CmsListColumnDefinition(LIST_COLUMN_DISPLAY);
        displayCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        displayCol.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_DISPLAY_0));
        displayCol.setWidth("35%");
        metadata.addColumn(displayCol);

        // add hide column for store
        CmsListColumnDefinition storeHideCol = new CmsListColumnDefinition(LIST_COLUMN_STORE_HIDE);
        storeHideCol.setVisible(false);
        metadata.addColumn(storeHideCol);

        // add hide column for excerpt
        CmsListColumnDefinition excerptHideCol = new CmsListColumnDefinition(LIST_COLUMN_EXCERPT_HIDE);
        excerptHideCol.setVisible(false);
        metadata.addColumn(excerptHideCol);

        // add column for store
        CmsListColumnDefinition storeCol = new CmsListColumnDefinition(LIST_COLUMN_STORE);
        storeCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        storeCol.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_STORE_0));
        // true action
        CmsListDirectAction storeTrueAction = new CmsListDirectAction(LIST_ACTION_STORE_TRUE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    return ((Boolean)getItem().get(LIST_COLUMN_STORE_HIDE)).booleanValue();
                }
                return super.isVisible();
            }
        };
        storeTrueAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_TRUE_NAME_0));
        storeTrueAction.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_TRUE_HELP_0));
        storeTrueAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_TRUE_CONF_0));
        storeTrueAction.setIconPath(ICON_TRUE);
        // false action
        CmsListDirectAction storeFalseAction = new CmsListDirectAction(LIST_ACTION_STORE_FALSE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    return !((Boolean)getItem().get(LIST_COLUMN_STORE_HIDE)).booleanValue();
                }
                return super.isVisible();
            }
        };
        storeFalseAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_FALSE_NAME_0));
        storeFalseAction.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_FALSE_HELP_0));
        storeFalseAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_FALSE_CONF_0));
        storeFalseAction.setIconPath(ICON_FALSE);

        storeCol.addDirectAction(storeTrueAction);
        storeCol.addDirectAction(storeFalseAction);
        metadata.addColumn(storeCol);

        // add colum for excerpt
        CmsListColumnDefinition excerptCol = new CmsListColumnDefinition(LIST_COLUMN_EXCERPT);
        excerptCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        excerptCol.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_EXCERPT_0));
        // true action
        CmsListDirectAction excerptTrueAction = new CmsListDirectAction(LIST_ACTION_EXCERPT_TRUE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    return ((Boolean)getItem().get(LIST_COLUMN_EXCERPT_HIDE)).booleanValue();
                }
                return super.isVisible();
            }
        };
        excerptTrueAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_TRUE_NAME_0));
        excerptTrueAction.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_TRUE_HELP_0));
        excerptTrueAction.setConfirmationMessage(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_TRUE_CONF_0));
        excerptTrueAction.setIconPath(ICON_TRUE);
        // false action
        CmsListDirectAction excerptFalseAction = new CmsListDirectAction(LIST_ACTION_EXCERPT_FALSE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    return !((Boolean)getItem().get(LIST_COLUMN_EXCERPT_HIDE)).booleanValue();
                }
                return super.isVisible();
            }
        };
        excerptFalseAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_FALSE_NAME_0));
        excerptFalseAction.setHelpText(Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_FALSE_HELP_0));
        excerptFalseAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_LIST_FIELD_ACTION_FALSE_CONF_0));
        excerptFalseAction.setIconPath(ICON_FALSE);

        excerptCol.addDirectAction(excerptTrueAction);
        excerptCol.addDirectAction(excerptFalseAction);
        metadata.addColumn(excerptCol);

        // add column for index
        CmsListColumnDefinition indexCol = new CmsListColumnDefinition(LIST_COLUMN_INDEX);
        indexCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        indexCol.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_INDEX_0));
        indexCol.setWidth("10%");
        metadata.addColumn(indexCol);

        // add column for boost
        CmsListColumnDefinition boostCol = new CmsListColumnDefinition(LIST_COLUMN_BOOST);
        boostCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        boostCol.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_BOOST_0));
        boostCol.setWidth("5%");
        metadata.addColumn(boostCol);

        // add column for default
        CmsListColumnDefinition defaultCol = new CmsListColumnDefinition(LIST_COLUMN_DEFAULT);
        defaultCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        defaultCol.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_DEFAULT_0));
        defaultCol.setWidth("5%");
        metadata.addColumn(defaultCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // add field configuration details
        CmsListItemDetails configDetails = new CmsListItemDetails(LIST_DETAIL_FIELD);
        configDetails.setAtColumn(LIST_COLUMN_NAME);
        configDetails.setVisible(false);
        configDetails.setShowActionName(Messages.get().container(Messages.GUI_LIST_FIELD_DETAIL_MAPPINGS_SHOW_0));
        configDetails.setShowActionHelpText(
            Messages.get().container(Messages.GUI_LIST_FIELD_DETAIL_MAPPINGS_SHOW_HELP_0));
        configDetails.setHideActionName(Messages.get().container(Messages.GUI_LIST_FIELD_DETAIL_MAPPINGS_HIDE_0));
        configDetails.setHideActionHelpText(
            Messages.get().container(Messages.GUI_LIST_FIELD_DETAIL_MAPPINGS_HIDE_HELP_0));
        configDetails.setName(Messages.get().container(Messages.GUI_LIST_FIELD_DETAIL_MAPPINGS_NAME_0));
        configDetails.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_LIST_FIELD_DETAIL_MAPPINGS_NAME_0)));
        metadata.addItemDetails(configDetails);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add add multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETEFIELD);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_MACTION_DELETEFIELD_NAME_0));
        deleteMultiAction.setHelpText(
            Messages.get().container(Messages.GUI_LIST_FIELD_MACTION_DELETEFIELD_NAME_HELP_0));
        deleteMultiAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_LIST_FIELD_MACTION_DELETEFIELD_CONF_0));
        deleteMultiAction.setIconPath(ICON_MULTI_DELETE);
        metadata.addMultiAction(deleteMultiAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // will throw NPE if something wrong
        OpenCms.getSearchManager().getFieldConfiguration(getParamFieldconfiguration()).getFields();
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
     * Checks the configuration to write.<p>
     *
     * @param fields list of fields of the current field configuration
     * @return true if configuration is valid, otherwise false
     */
    private boolean checkWriteConfiguration(List<CmsSearchField> fields) {

        if (fields == null) {
            return false;
        }
        Iterator<CmsSearchField> itFields = fields.iterator();
        while (itFields.hasNext()) {
            CmsLuceneField curField = (CmsLuceneField)itFields.next();
            if (curField.getMappings().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fills details of the field into the given item. <p>
     *
     * @param item the list item to fill
     * @param detailId the id for the detail to fill
     */
    private void fillDetailField(CmsListItem item, String detailId) {

        StringBuffer html = new StringBuffer();
        // search for the corresponding A_CmsSearchIndex:
        String idxFieldName = (String)item.get(LIST_COLUMN_NAME);

        List<CmsSearchField> fields = OpenCms.getSearchManager().getFieldConfiguration(
            m_paramFieldconfiguration).getFields();
        Iterator<CmsSearchField> itFields = fields.iterator();
        CmsLuceneField idxField = null;
        while (itFields.hasNext()) {
            CmsLuceneField curField = (CmsLuceneField)itFields.next();
            if (curField.getName().equals(idxFieldName)) {
                idxField = curField;
            }
        }

        if (idxField != null) {
            html.append("<ul>\n");
            Iterator<I_CmsSearchFieldMapping> itMappings = idxField.getMappings().iterator();
            while (itMappings.hasNext()) {
                CmsSearchFieldMapping mapping = (CmsSearchFieldMapping)itMappings.next();
                html.append("  <li>\n").append("    ");
                html.append(mapping.getType().toString());
                if (CmsStringUtil.isNotEmpty(mapping.getParam())) {
                    html.append("=").append(mapping.getParam()).append("\n");
                }
                html.append("  </li>");
            }
            html.append("</ul>\n");
        }
        item.set(detailId, html.toString());
    }

    /**
     * Returns the configured fields of the current field configuration.
     *
     * @return the configured fields of the current field configuration
     */
    private List<CmsSearchField> getFields() {

        CmsSearchManager manager = OpenCms.getSearchManager();
        CmsSearchFieldConfiguration fieldConfig = manager.getFieldConfiguration(getParamFieldconfiguration());
        List<CmsSearchField> result;
        if (fieldConfig != null) {
            result = fieldConfig.getFields();
        } else {
            result = Collections.emptyList();
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_SEARCHINDEX_EDIT_MISSING_PARAM_1,
                        A_CmsFieldConfigurationDialog.PARAM_FIELDCONFIGURATION));
            }
        }
        return result;
    }
}
