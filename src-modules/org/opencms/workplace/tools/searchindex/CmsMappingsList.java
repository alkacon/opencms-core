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
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
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
 * A list that displays the mappings of a request parameter given
 * <code>{@link org.opencms.search.fields.CmsLuceneField}</code> ("field").
 *
 * This list is no stand-alone page but has to be embedded in another dialog
 * (see <code> {@link org.opencms.workplace.tools.searchindex.A_CmsEmbeddedListDialog}</code>. <p>
 *
 * @since 6.5.5
 */
public class CmsMappingsList extends A_CmsEmbeddedListDialog {

    /** list column id constant. */
    public static final String LIST_ACTION_EDIT = "ae";

    /** list column id constant. */
    public static final String LIST_ACTION_EDITTYPE = "aet";

    /** list column id constant. */
    public static final String LIST_ACTION_EDITVALUE = "aev";

    /** list column id constant. */
    public static final String LIST_COLUMN_DEFAULT = "cd";

    /** list column id constant. */
    public static final String LIST_COLUMN_ICON = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_TYPE = "ct";

    /** list column id constant. */
    public static final String LIST_COLUMN_VALUE = "cv";

    /** list id constant. */
    public static final String LIST_ID = "lsfcfm";

    /** list action id constant. */
    public static final String LIST_MACTION_DELETEMAPPING = "mad";

    /** The path to the field configuration list icon. */
    protected static final String LIST_ICON_MAPPING = "tools/searchindex/icons/small/fieldconfiguration-mapping.png";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMappingsList.class);

    /** Stores the value of the request parameter for the field. */
    private String m_paramField;

    /** Stores the value of the request parameter for the field configuration. */
    private String m_paramFieldconfiguration;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsMappingsList(CmsJspActionElement jsp) {

        this(jsp, LIST_ID, Messages.get().container(Messages.GUI_LIST_MAPPINGS_NAME_0));
    }

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     * @param listId the id of the list
     * @param listName the list name
     */
    public CmsMappingsList(CmsJspActionElement jsp, String listId, CmsMessageContainer listName) {

        this(jsp, listId, listName, LIST_COLUMN_TYPE, CmsListOrderEnum.ORDER_ASCENDING, null);
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
    public CmsMappingsList(
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
        if (getParamListAction().equals(LIST_MACTION_DELETEMAPPING)) {
            // execute the delete multi action, first search for the field to edit
            List<CmsSearchField> fields = searchManager.getFieldConfiguration(m_paramFieldconfiguration).getFields();
            Iterator<CmsSearchField> itFields = fields.iterator();
            while (itFields.hasNext()) {
                CmsLuceneField curField = (CmsLuceneField)itFields.next();
                if (curField.getName().equals(m_paramField)) {
                    // we found the field to edit
                    List<I_CmsSearchFieldMapping> deleteMappings = new ArrayList<I_CmsSearchFieldMapping>();
                    Iterator<CmsListItem> itItems = getSelectedItems().iterator();
                    while (itItems.hasNext()) {
                        // iterate all selected mappings
                        CmsListItem listItem = itItems.next();
                        Iterator<I_CmsSearchFieldMapping> itMappings = curField.getMappings().iterator();
                        while (itMappings.hasNext()) {
                            // iterate all field mappings
                            CmsSearchFieldMapping curMapping = (CmsSearchFieldMapping)itMappings.next();
                            String itemValue = (String)listItem.get(LIST_COLUMN_VALUE);
                            String itemType = (String)listItem.get(LIST_COLUMN_TYPE);
                            // match the selected mapping
                            if (curMapping.getType().toString().equals(itemType)
                                && (((curMapping.getParam() == null) && (itemValue == null))
                                    || (curMapping.getParam().equals(itemValue)))) {
                                // mark for deletion
                                deleteMappings.add(curMapping);
                            }
                        }
                    }
                    // delete the marked mappings
                    Iterator<I_CmsSearchFieldMapping> itMappings = deleteMappings.iterator();
                    while (itMappings.hasNext()) {
                        CmsSearchFieldMapping mapping = (CmsSearchFieldMapping)itMappings.next();
                        searchManager.removeSearchFieldMapping(curField, mapping);
                    }
                    break;
                }
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
    public void executeListSingleActions() throws ServletException, IOException {

        CmsListItem item = getSelectedItem();
        Map<String, String[]> params = new HashMap<String, String[]>();
        String action = getParamListAction();

        params.put(A_CmsMappingDialog.PARAM_FIELD, new String[] {m_paramField});
        params.put(A_CmsMappingDialog.PARAM_FIELDCONFIGURATION, new String[] {m_paramFieldconfiguration});
        params.put(A_CmsMappingDialog.PARAM_TYPE, new String[] {item.get(LIST_COLUMN_TYPE).toString()});
        params.put(A_CmsMappingDialog.PARAM_PARAM, new String[] {item.get(LIST_COLUMN_VALUE).toString()});

        params.put(PARAM_ACTION, new String[] {DIALOG_INITIAL});
        params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});

        if (action.equals(LIST_ACTION_EDIT)
            || action.equals(LIST_ACTION_EDITTYPE)
            || action.equals(LIST_ACTION_EDITVALUE)) {
            // forward to the edit mapping screen
            getToolManager().jspForwardTool(
                this,
                "/searchindex/fieldconfigurations/fieldconfiguration/field/editmapping",
                params);
        }
        listSave();
    }

    /**
     * Returns the request parameter "field".<p>
     *
     * @return the request parameter "field"
     */
    public String getParamField() {

        return m_paramField;
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
     * Sets the request parameter "field". <p>
     *
     * Method intended for workplace-proprietary automatic filling of
     * request parameter values to dialogs, not for manual invocation. <p>
     *
     * @param field the request parameter "field" to set
     */
    public void setParamField(String field) {

        m_paramField = field;
    }

    /**
     * Sets the request parameter "fieldconfiguration". <p>
     *
     * Method intended for workplace-proprietary automatic filling of
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

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() {

        List<CmsListItem> result = new ArrayList<CmsListItem>();
        // get content
        List<I_CmsSearchFieldMapping> mappings = getMappings();
        Iterator<I_CmsSearchFieldMapping> itMappings = mappings.iterator();
        CmsSearchFieldMapping mapping;
        while (itMappings.hasNext()) {
            mapping = (CmsSearchFieldMapping)itMappings.next();
            CmsListItem item = getList().newItem(mapping.getType().toString());
            String defaultValue = mapping.getDefaultValue();
            String param = mapping.getParam();
            if (defaultValue == null) {
                defaultValue = "-";
            }
            if (param == null) {
                param = "-";
            }
            item.set(LIST_COLUMN_VALUE, param);
            item.set(LIST_COLUMN_TYPE, mapping.getType().toString());
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

        // create dummy column for corporate design reasons
        CmsListColumnDefinition dummyCol = new CmsListColumnDefinition(LIST_COLUMN_ICON);
        dummyCol.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_MAPPING_0));
        dummyCol.setHelpText(Messages.get().container(Messages.GUI_LIST_MAPPING_COL_EDIT_NAME_HELP_0));
        dummyCol.setWidth("20");
        dummyCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        dummyCol.setSorteable(false);
        // add dummy icon
        CmsListDirectAction editAction = new CmsListDirectAction(LIST_ACTION_EDIT);
        editAction.setName(Messages.get().container(Messages.GUI_LIST_MAPPING_COL_EDIT_NAME_0));
        editAction.setHelpText(Messages.get().container(Messages.GUI_LIST_MAPPING_COL_EDIT_NAME_HELP_0));
        editAction.setIconPath(LIST_ICON_MAPPING);
        dummyCol.addDirectAction(editAction);
        // add it to the list definition
        metadata.addColumn(dummyCol);

        // add column for value
        CmsListColumnDefinition valueCol = new CmsListColumnDefinition(LIST_COLUMN_VALUE);
        valueCol.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        valueCol.setName(Messages.get().container(Messages.GUI_LIST_MAPPING_COL_VALUE_0));
        valueCol.setWidth("33%");
        valueCol.setSorteable(true);
        CmsListDefaultAction editValueAction = new CmsListDefaultAction(LIST_ACTION_EDITVALUE);
        editValueAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_MAPPING_0));
        editValueAction.setHelpText(Messages.get().container(Messages.GUI_LIST_MAPPING_COL_EDIT_NAME_HELP_0));
        valueCol.addDefaultAction(editValueAction);
        metadata.addColumn(valueCol);

        // add column for type
        CmsListColumnDefinition typeCol = new CmsListColumnDefinition(LIST_COLUMN_TYPE);
        typeCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        typeCol.setName(Messages.get().container(Messages.GUI_LIST_MAPPING_COL_TYPE_0));
        typeCol.setWidth("33%");
        CmsListDefaultAction editTypeAction = new CmsListDefaultAction(LIST_ACTION_EDITTYPE);
        editTypeAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_COL_MAPPING_0));
        editTypeAction.setHelpText(Messages.get().container(Messages.GUI_LIST_MAPPING_COL_EDIT_NAME_HELP_0));
        typeCol.addDefaultAction(editTypeAction);
        metadata.addColumn(typeCol);

        // add column for default
        CmsListColumnDefinition defaultCol = new CmsListColumnDefinition(LIST_COLUMN_DEFAULT);
        defaultCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        defaultCol.setName(Messages.get().container(Messages.GUI_LIST_MAPPING_COL_DEFAULT_0));
        defaultCol.setWidth("33%");
        metadata.addColumn(defaultCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // empty
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add add multi action
        CmsListMultiAction deleteMultiAction = new CmsListMultiAction(LIST_MACTION_DELETEMAPPING);
        deleteMultiAction.setName(Messages.get().container(Messages.GUI_LIST_FIELD_MACTION_DELETEMAPPING_NAME_0));
        deleteMultiAction.setHelpText(
            Messages.get().container(Messages.GUI_LIST_FIELD_MACTION_DELETEMAPPING_NAME_HELP_0));
        deleteMultiAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_LIST_FIELD_MACTION_DELETEMAPPING_CONF_0));
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
     * Returns the configured mappings of the current field.
     *
     * @return the configured mappings of the current field
     */
    private List<I_CmsSearchFieldMapping> getMappings() {

        CmsSearchManager manager = OpenCms.getSearchManager();
        CmsSearchFieldConfiguration fieldConfig = manager.getFieldConfiguration(getParamFieldconfiguration());
        CmsLuceneField field;
        List<I_CmsSearchFieldMapping> result = null;
        Iterator<CmsSearchField> itFields;
        if (fieldConfig != null) {
            itFields = fieldConfig.getFields().iterator();
            while (itFields.hasNext()) {
                field = (CmsLuceneField)itFields.next();
                if (field.getName().equals(getParamField())) {
                    result = field.getMappings();
                }
            }
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
