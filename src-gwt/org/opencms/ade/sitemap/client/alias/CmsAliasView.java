/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.sitemap.client.alias;

import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messagePage;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messagePermanentRedirect;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messageRedirect;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messageRowCount;

import org.opencms.ade.sitemap.client.alias.rewrite.CmsRewriteAliasTable;
import org.opencms.ade.sitemap.client.alias.simple.CmsAliasCellTable;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.gwt.shared.alias.CmsAliasTableRow;
import org.opencms.gwt.shared.alias.CmsRewriteAliasTableRow;
import org.opencms.gwt.shared.alias.CmsRewriteAliasValidationReply;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.RowCountChangeEvent;

/**
 * The widget containing the table and the buttons used for editing aliases.<p>
 */
public class CmsAliasView extends Composite {

    /** The UiBinder interface for this widget. */
    protected interface I_CmsAliasViewUiBinder extends UiBinder<Widget, CmsAliasView> {
        // empty
    }

    /** The 'Cancel' button. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** The alias table controller. */
    protected CmsAliasTableController m_controller;

    /** Text container for the row count. */
    @UiField
    protected HasText m_countLabel;

    /** The 'Delete' button. */
    @UiField
    protected CmsPushButton m_deleteButton;

    /** The 'Download' button. */
    @UiField
    protected CmsPushButton m_downloadButton;

    /** The text box for entering the path for a new alias. */
    @UiField
    protected CmsTextBox m_newAliasPath;

    /** The button for adding a new alias. */
    @UiField
    protected CmsPushButton m_newButton;

    /** The select box for selecting the mode of a new alias. */
    @UiField
    protected CmsSelectBox m_newMode;

    /** The text box for entering the resource path for a new alias. */
    @UiField
    protected CmsTextBox m_newResourcePath;

    /** The button for adding a new rewrite alias. */
    @UiField
    protected CmsPushButton m_newRewriteButton;

    /** The select box for selecting a mode for new rewrite aliases. */
    @UiField
    protected CmsSelectBox m_newRewriteMode;

    /** The text box for entering a new rewrite pattern. */
    @UiField
    protected CmsTextBox m_newRewriteRegex;

    /** The text box for entering a new rewrite replacement string. */
    @UiField
    protected CmsTextBox m_newRewriteReplacement;

    /** The button for deleting rewrite aliases. */
    @UiField
    protected CmsPushButton m_rewriteDeleteButton;

    /**
     * The container for the rewrite table.<p>
     */
    @UiField
    protected Panel m_rewriteTableContainer;

    /** The button for saving the alias table. */
    @UiField
    protected CmsPushButton m_saveButton;

    /** The panel containing the alias cell table. */
    @UiField
    protected Panel m_tableContainer;

    /** The button for importing alias CSV files. */
    @UiField
    protected CmsPushButton m_uploadButton;

    /** The popup in which this view is displayed. */
    private CmsPopup m_popup;

    /** The cell table for editing rewrite aliases. */
    private CmsRewriteAliasTable m_rewriteTable;

    /** The table containing the alias data. */
    private CmsAliasCellTable m_table;

    /**
     * Creates new view instance.<p>
     *
     * @param controller the controller for the view
     */
    @SuppressWarnings("unchecked")
    public CmsAliasView(CmsAliasTableController controller) {

        initWidget(((UiBinder<Widget, CmsAliasView>)GWT.create(I_CmsAliasViewUiBinder.class)).createAndBindUi(this));
        Map<String, String> items = new LinkedHashMap<String, String>();
        items.put(CmsAliasMode.permanentRedirect.toString(), messagePermanentRedirect());
        items.put(CmsAliasMode.redirect.toString(), messageRedirect());
        items.put(CmsAliasMode.page.toString(), messagePage());
        m_newMode.setItems(items);

        Map<String, String> rewriteItems = new LinkedHashMap<String, String>();
        rewriteItems.put(CmsAliasMode.permanentRedirect.toString(), messagePermanentRedirect());
        rewriteItems.put(CmsAliasMode.redirect.toString(), messageRedirect());
        rewriteItems.put(CmsAliasMode.passthrough.toString(), CmsAliasMessages.messagePassthrough());
        m_newRewriteMode.setItems(rewriteItems);

        m_controller = controller;
        m_deleteButton.setEnabled(false);
        m_rewriteDeleteButton.setEnabled(false);
        m_table = new CmsAliasCellTable(controller);
        m_tableContainer.add(m_table);
        m_rewriteTable = new CmsRewriteAliasTable(controller);
        m_rewriteTableContainer.add(m_rewriteTable);
        setNewButtonStyle(m_newButton);
        setNewButtonStyle(m_newRewriteButton);
        m_table.addRowCountChangeHandler(new RowCountChangeEvent.Handler() {

            public void onRowCountChange(RowCountChangeEvent event) {

                String message = messageRowCount(event.getNewRowCount());
                m_countLabel.setText(message);
            }
        });
        setWidth("1150px"); //$NON-NLS-1$
    }

    /**
     * Clears the input fields used to add new aliases.<p>
     */
    public void clearNew() {

        m_newAliasPath.setFormValueAsString(""); //$NON-NLS-1$
        m_newAliasPath.setErrorMessage(null);
        m_newResourcePath.setFormValueAsString(""); //$NON-NLS-1$
        m_newResourcePath.setErrorMessage(null);
    }

    /**
     * Clears the text boxes for adding new rewrites.<p>
     */
    public void clearRewriteNew() {

        m_newRewriteRegex.setFormValueAsString("");
        m_newRewriteReplacement.setFormValueAsString("");
    }

    /**
     * Clears the validation errors for the text boxes used to add new aliases.<p>
     */
    public void clearValidationsForNew() {

        m_newResourcePath.setErrorMessage(null);
        m_newAliasPath.setErrorMessage(null);
    }

    /**
     * Hides the popup.<p>
     */
    public void close() {

        m_popup.hide();
    }

    /**
     * Gets the buttons which should be displayed in the button bar of the popup containing this view.<p>
     *
     * @return the buttons for the popup button bar
     */
    public List<CmsPushButton> getButtonBar() {

        List<CmsPushButton> buttons = new ArrayList<CmsPushButton>();
        buttons.add(m_cancelButton);
        buttons.add(m_saveButton);
        buttons.add(m_downloadButton);
        buttons.add(m_uploadButton);
        return buttons;
    }

    /**
     * Gets the list of rows used by the data provider.<p>
     *
     * @return the list of rows used by the data provider
     */
    public List<CmsAliasTableRow> getLiveData() {

        return m_table.getLiveDataList();
    }

    /**
     * Gets the rewrite alias data.<p>
     *
     * @return the rewrite alias list
     */
    public List<CmsRewriteAliasTableRow> getRewriteData() {

        return m_rewriteTable.getLiveDataList();
    }

    /**
     * Gets the rewrite alias cell table.<p>
     *
     * @return the rewrite alias cell table
     */
    public CmsRewriteAliasTable getRewriteTable() {

        return m_rewriteTable;
    }

    /**
     * Gets the cell table used to edit the alias data.<p>
     *
     * @return the alias cell table
     */
    public CmsAliasCellTable getTable() {

        return m_table;
    }

    /**
     * Replaces the contents of the live data row list with another list of rows.<p>
     *
     * @param data the new list of rows to be placed into the live data list
     * @param rewriteData the list of rewrite alias data
     */
    public void setData(List<CmsAliasTableRow> data, List<CmsRewriteAliasTableRow> rewriteData) {

        m_table.getLiveDataList().clear();
        m_table.getLiveDataList().addAll(data);
        m_rewriteTable.getLiveDataList().clear();
        m_rewriteTable.getLiveDataList().addAll(rewriteData);
    }

    /**
     * Enables or disables the delete button.<p>
     *
     * @param enabled if true, the delete button will be enabled, else disabled
     */
    public void setDeleteButtonEnabled(boolean enabled) {

        m_deleteButton.setEnabled(enabled);
    }

    /**
     * Sets the validation error message for the alias path text box.<p>
     *
     * @param error the validation error message
     */
    public void setNewAliasPathError(String error) {

        m_newAliasPath.setErrorMessage(error);

    }

    /**
     * Sets the validation error message for the resource path text box.<p>
     *
     * @param error the validation error message
     */
    public void setNewAliasResourceError(String error) {

        m_newResourcePath.setErrorMessage(error);
    }

    /**
     * Sets the popup used to display this widget.<p>
     *
     * @param popup the popup instance
     */
    public void setPopup(CmsPopup popup) {

        m_popup = popup;
    }

    /**
     * Enables or disables the delete button for rewrite aliases.<p>
     *
     * @param enabled true if the delete button should be enabled
     */
    public void setRewriteDeleteButtonEnabled(boolean enabled) {

        m_rewriteDeleteButton.setEnabled(enabled);
    }

    /**
     * Enables or disables the save button.<p>
     *
     * @param enabled true if the save button should be enabled, false if it should be disabled
     */
    public void setSaveButtonEnabled(boolean enabled) {

        m_saveButton.setEnabled(enabled);
    }

    /**
     * Ensures that rows with errors will be placed at the top of the table.<p>
     */
    public void sortByErrors() {

        ColumnSortList columnSort = m_table.getColumnSortList();
        columnSort.clear();
        columnSort.push(m_table.getErrorColumn());
        columnSort.push(m_table.getErrorColumn());
        ColumnSortEvent.fire(m_table, columnSort);
    }

    /**
     * Updates the view after the rewrite aliases have been validated.<p>
     *
     * @param result the result of the rewrite alias validation
     */
    public void update(CmsRewriteAliasValidationReply result) {

        Map<CmsUUID, String> errors = result.getErrors();
        for (CmsRewriteAliasTableRow row : getRewriteData()) {
            String error = errors.get(row.getId());
            row.setError(error);
        }
        m_rewriteTable.redraw();
    }

    /**
     * Updates the table data with a new list of rows.<p>
     *
     * Rows in the table for which a row with the same key is also contained in the 'data' parameter
     * will be updated, the other rows from the 'data' list will be added.<p>
     *
     * @param data the list of rows to update
     */
    public void update(List<CmsAliasTableRow> data) {

        Map<String, CmsAliasTableRow> currentRowsByKey = new HashMap<String, CmsAliasTableRow>();
        for (CmsAliasTableRow row : m_table.getLiveDataList()) {
            currentRowsByKey.put(row.getKey(), row);
        }
        List<CmsAliasTableRow> rowsToAdd = new ArrayList<CmsAliasTableRow>();
        for (CmsAliasTableRow updateRow : data) {
            String key = updateRow.getKey();
            CmsAliasTableRow existingRow = currentRowsByKey.get(key);
            if (existingRow != null) {
                existingRow.update(updateRow);
            } else {
                rowsToAdd.add(updateRow);
            }
        }
        m_table.getLiveDataList().addAll(rowsToAdd);
        m_table.redraw();
    }

    /**
     * The event handler for the button to delete rewrite aliases.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_rewriteDeleteButton")
    protected void onClickDeleteRewrite(ClickEvent e) {

        List<CmsRewriteAliasTableRow> rowsToDelete = m_rewriteTable.getSelectedRows();
        m_controller.deleteRewrites(rowsToDelete);
    }

    /**
     * The click handler for the 'Cancel' button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_cancelButton")
    void onClickCancel(ClickEvent e) {

        m_popup.hide();
    }

    /**
     * The click handler for the 'Delete' button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_deleteButton")
    void onClickDelete(ClickEvent e) {

        List<CmsAliasTableRow> rowsToDelete = m_table.getSelectedRows();
        m_controller.deleteRows(rowsToDelete);
    }

    /**
     * The click handler for the 'Download' button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_downloadButton")
    void onClickDownload(ClickEvent e) {

        m_controller.download();
    }

    /**
     * The click handler for the 'New' button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_newButton")
    void onClickNew(ClickEvent e) {

        String aliasPath = m_newAliasPath.getText();
        String resourcePath = m_newResourcePath.getText();
        m_controller.editNewAlias(aliasPath, resourcePath, CmsAliasMode.valueOf(m_newMode.getFormValueAsString()));
    }

    /**
     * The event handler for the button for adding new rewrite aliases.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_newRewriteButton")
    void onClickNewRewrite(ClickEvent e) {

        String rewriteRegex = m_newRewriteRegex.getText();
        String rewriteReplacement = m_newRewriteReplacement.getText();
        String mode = m_newRewriteMode.getFormValueAsString();
        m_controller.editNewRewrite(rewriteRegex, rewriteReplacement, CmsAliasMode.valueOf(mode));
    }

    /**
     * The click handler for the 'Save' button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_saveButton")
    void onClickSave(ClickEvent e) {

        m_controller.save();
    }

    /**
     * The click handler for the upload button.<p>
     *
     * @param e the click handler for the upload button
     */
    @UiHandler("m_uploadButton")
    void onClickUpload(ClickEvent e) {

        m_popup.hide();
        CmsImportView.showPopup();

    }

    /**
     * Styles a button for adding new aliases.<p>
     *
     * @param newButton the button to style
     */
    private void setNewButtonStyle(CmsPushButton newButton) {

        newButton.setImageClass(I_CmsButton.ADD_SMALL);
        newButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
    }

}
