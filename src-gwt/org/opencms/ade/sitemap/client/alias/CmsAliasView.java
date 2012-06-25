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

import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messageButtonCancel;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messageButtonDelete;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messageButtonDownload;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messageButtonNew;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messageButtonSave;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messageButtonUpload;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messagePage;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messagePermanentRedirect;
import static org.opencms.ade.sitemap.client.alias.CmsAliasMessages.messageRedirect;

import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.gwt.shared.alias.CmsAliasTableRow;

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
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

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
        m_controller = controller;
        m_newButton.setText(messageButtonNew());
        m_saveButton.setText(messageButtonSave());
        m_saveButton.setUseMinWidth(true);
        m_cancelButton.setText(messageButtonCancel());
        m_cancelButton.setUseMinWidth(true);
        m_deleteButton.setText(messageButtonDelete());
        m_deleteButton.setEnabled(false);
        m_downloadButton.setText(messageButtonDownload());
        m_uploadButton.setText(messageButtonUpload());
        m_newButton.setSize(Size.big);
        m_table = new CmsAliasCellTable(controller);
        m_tableContainer.add(m_table);
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
     */
    public void setData(List<CmsAliasTableRow> data) {

        m_table.getLiveDataList().clear();
        m_table.getLiveDataList().addAll(data);
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

}
