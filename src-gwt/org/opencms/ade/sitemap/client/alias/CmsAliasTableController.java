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

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationReply;
import org.opencms.gwt.shared.alias.CmsAliasEditValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasInitialFetchResult;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.gwt.shared.alias.CmsAliasSaveValidationRequest;
import org.opencms.gwt.shared.alias.CmsAliasTableRow;
import org.opencms.gwt.shared.alias.CmsRewriteAliasTableRow;
import org.opencms.gwt.shared.alias.CmsRewriteAliasValidationReply;
import org.opencms.gwt.shared.alias.CmsRewriteAliasValidationRequest;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is the controller class for the alias editor which is responsible for reacting to edit operations on the client
 * by triggering the corresponding validation or save actions on the server.<p>
 */
public class CmsAliasTableController {

    /** The set of ids of deleted rows. */
    protected Set<CmsUUID> m_deletedIds = new HashSet<CmsUUID>();

    /** The view. */
    protected CmsAliasView m_view;

    /** The URL from which the list of aliases can be downloaded. */
    String m_downloadUrl;

    /** The initial data loaded from the server. */
    List<CmsAliasTableRow> m_initialData;

    /** The site root. */
    String m_siteRoot;

    /**
     * Gets the service to use for validating/saving aliases.<p>
     *
     * @return the service used for validating/saving aliases
     */
    protected static I_CmsSitemapServiceAsync getService() {

        return CmsSitemapView.getInstance().getController().getService();
    }

    /**
     * Method which should be called when the selection of the rewrite table has been changed.<p>
     *
     * @param selectedSet the set of selected rewrite table entries
     */
    public void changeRewriteSelection(Set<CmsRewriteAliasTableRow> selectedSet) {

        boolean selectionEmpty = selectedSet.isEmpty();
        m_view.setRewriteDeleteButtonEnabled(!selectionEmpty);

    }

    /**
     * This method is called when the selection of the alias table changes.<p>
     *
     * @param selectedSet the set of selected rows
     */
    public void changeSelection(Set<CmsAliasTableRow> selectedSet) {

        boolean selectionEmpty = selectedSet.isEmpty();
        m_view.setDeleteButtonEnabled(!selectionEmpty);
    }

    /**
     * This method is called when the user wants to delete rewrites aliases.<p>
     *
     * @param rowsToDelete the rows the user wants to delete
     */
    public void deleteRewrites(List<CmsRewriteAliasTableRow> rowsToDelete) {

        List<CmsRewriteAliasTableRow> liveData = m_view.getRewriteData();
        liveData.removeAll(rowsToDelete);
        m_view.getRewriteTable().getSelectionModel().clear();
        updateValidationStatus();
    }

    /**
     * This method is called when the user deletes a set of rows.<p>
     *
     * @param rowsToDelete the list of rows which should be deleted
     */
    public void deleteRows(List<CmsAliasTableRow> rowsToDelete) {

        List<CmsAliasTableRow> liveData = m_view.getLiveData();
        for (CmsAliasTableRow row : liveData) {
            CmsUUID structureId = row.getStructureId();
            if (structureId != null) {
                m_deletedIds.add(row.getStructureId());
            }
        }
        // prevent selection model from going out of synch
        m_view.getTable().getSelectionModel().clear();
        liveData.removeAll(rowsToDelete);
        updateValidationStatus();
    }

    /**
     * Triggers the download of the current aliases.<p>
     */
    public void download() {

        CmsDomUtil.openWindow(m_downloadUrl + "?site=" + m_siteRoot, "_blank", "");
    }

    /**
     * This method is called after the mode of an alias has been edited.<p>
     *
     * @param row the edited row
     * @param mode the new alias mode
     */
    public void editAliasMode(CmsAliasTableRow row, CmsAliasMode mode) {

        row.setMode(mode);
        row.setEdited(true);
    }

    /**
     * This method is called after the alias path of an alias has been edited.<p>
     *
     * @param row the edited row
     * @param path the new alias path
     */
    public void editAliasPath(CmsAliasTableRow row, String path) {

        row.editAliasPath(path);
        row.setEdited(true);
        validate();

    }

    /**
     * This method is called when the user wants to add a new alias entry.<p>
     *
     * @param aliasPath the alias path
     * @param resourcePath the resource site path
     * @param mode the alias mode
     */
    public void editNewAlias(String aliasPath, String resourcePath, CmsAliasMode mode) {

        CmsAliasTableRow row = new CmsAliasTableRow();
        row.setEdited(true);
        row.setAliasPath(aliasPath);
        row.setResourcePath(resourcePath);
        row.setMode(mode);
        validateNew(row);
    }

    /**
     * This method is called when the user adds a new rewrite alias.<p>
     *
     * @param rewriteRegex the rewrite pattern
     * @param rewriteReplacement the rewrite replacement string
     *
     * @param mode the rewrite mode
     */
    public void editNewRewrite(String rewriteRegex, String rewriteReplacement, CmsAliasMode mode) {

        CmsRewriteAliasTableRow row = new CmsRewriteAliasTableRow(
            new CmsUUID(CmsClientStringUtil.randomUUID()),
            rewriteRegex,
            rewriteReplacement,
            mode);
        m_view.clearRewriteNew();
        m_view.getRewriteData().add(0, row);
        validateRewrite();
    }

    /**
     * This method is called when the user has edited the resource path of an alias.<p>
     *
     * @param row the alias the table row
     * @param path the new path
     */
    public void editResourcePath(CmsAliasTableRow row, String path) {

        row.setEdited(true);
        row.editResourcePath(path);
        validate();
    }

    /**
     * This method is called when the user has edited a rewrite alias.<p>
     *
     * @param object the edited rewrite alias
     */
    public void editRewriteAlias(CmsRewriteAliasTableRow object) {

        validateRewrite();
    }

    /**
     * Loads the initial data from the server.<p>
     *
     * @param afterLoad the action that should be executed after loading
     */
    public void load(final Runnable afterLoad) {

        CmsRpcAction<CmsAliasInitialFetchResult> action = new CmsRpcAction<CmsAliasInitialFetchResult>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                getService().getAliasTable(this);
                start(0, true);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            public void onResponse(CmsAliasInitialFetchResult aliasTable) {

                stop(false);

                String lockOwner = aliasTable.getAliasTableLockOwner();
                if (lockOwner != null) {

                    String errorMessage = CmsAliasMessages.messageAliasTableLocked(lockOwner);
                    String title = CmsAliasMessages.messageAliasTableLockedTitle();
                    CmsAlertDialog alert = new CmsAlertDialog(title, errorMessage);
                    alert.center();
                } else {

                    m_downloadUrl = aliasTable.getDownloadUrl();
                    m_initialData = aliasTable.getRows();
                    m_siteRoot = CmsCoreProvider.get().getSiteRoot();
                    List<CmsAliasTableRow> copiedData = copyData(m_initialData);
                    List<CmsRewriteAliasTableRow> rewriteData = aliasTable.getRewriteAliases();
                    m_view.setData(copiedData, rewriteData);
                    if (afterLoad != null) {
                        afterLoad.run();
                    }
                }
            }
        };
        action.execute();
    }

    /**
     * This method is called when the user wants to save the aliases.<p>
     */
    public void save() {

        CmsRpcAction<CmsAliasEditValidationReply> action = new CmsRpcAction<CmsAliasEditValidationReply>() {

            @Override
            public void execute() {

                start(200, false);

                CmsAliasSaveValidationRequest saveRequest = new CmsAliasSaveValidationRequest();
                saveRequest.setSiteRoot(m_siteRoot);
                List<CmsAliasTableRow> rows = m_view.getLiveData();
                saveRequest.setEditedData(rows);
                saveRequest.setRewriteData(m_view.getRewriteData());
                saveRequest.getDeletedIds().addAll(m_deletedIds);
                saveRequest.setOriginalData(m_initialData);
                getService().saveAliases(saveRequest, this);
            }

            @Override
            public void onResponse(CmsAliasEditValidationReply result) {

                stop(false);
                if (result == null) {
                    m_view.close();
                } else if (result.hasErrors()) {
                    m_view.update(result.getChangedRows());
                }
            }
        };
        action.execute();
    }

    /**
     * Sets the alias editor view.<p>
     *
     * @param view the view object
     */
    public void setView(CmsAliasView view) {

        m_view = view;
    }

    /**
     * Enables or disables the save button of the view depending on whether there are validation errors.<p>
     */
    protected void updateValidationStatus() {

        boolean hasErrors = false;
        for (CmsAliasTableRow row : m_view.getLiveData()) {
            hasErrors |= row.hasErrors();
        }
        for (CmsRewriteAliasTableRow row : m_view.getRewriteData()) {
            hasErrors |= (row.getError() != null);
        }

        m_view.setSaveButtonEnabled(!hasErrors);
        if (hasErrors) {
            m_view.sortByErrors();
        }
    }

    /**
     * Triggers server-side validatiom of the alias table.<p>
     */
    protected void validate() {

        CmsRpcAction<CmsAliasEditValidationReply> action = new CmsRpcAction<CmsAliasEditValidationReply>() {

            @Override
            public void execute() {

                CmsAliasEditValidationRequest validationRequest = new CmsAliasEditValidationRequest();

                List<CmsAliasTableRow> rows = m_view.getLiveData();
                validationRequest.setEditedData(rows);
                validationRequest.setOriginalData(m_initialData);
                getService().validateAliases(validationRequest, this);
            }

            @Override
            public void onResponse(CmsAliasEditValidationReply result) {

                stop(false);
                List<CmsAliasTableRow> changedRows = result.getChangedRows();
                m_view.update(changedRows);
                updateValidationStatus();
            }

        };
        action.execute();
    }

    /**
     * Triggers server-side validation of the alias table and of a new entry which should be added to it.<p>
     *
     * @param newEntry the new entry
     */
    protected void validateNew(final CmsAliasTableRow newEntry) {

        CmsRpcAction<CmsAliasEditValidationReply> action = new CmsRpcAction<CmsAliasEditValidationReply>() {

            @Override
            public void execute() {

                start(200, true);
                CmsAliasEditValidationRequest validationRequest = new CmsAliasEditValidationRequest();
                List<CmsAliasTableRow> rows = m_view.getLiveData();
                validationRequest.setEditedData(rows);
                validationRequest.setNewEntry(newEntry);
                validationRequest.setOriginalData(m_initialData);
                getService().validateAliases(validationRequest, this);
            }

            @Override
            public void onResponse(CmsAliasEditValidationReply result) {

                stop(false);
                List<CmsAliasTableRow> tableRows = result.getChangedRows();
                CmsAliasTableRow validatedNewEntry = result.getValidatedNewEntry();
                if (validatedNewEntry.hasErrors()) {
                    m_view.setNewAliasPathError(validatedNewEntry.getAliasError());
                    m_view.setNewAliasResourceError(validatedNewEntry.getPathError());
                } else {
                    m_view.clearNew();
                    tableRows.add(validatedNewEntry);
                }
                m_view.update(tableRows);
                updateValidationStatus();
            }
        };
        action.execute();
    }

    /**
     * Triggers server-side validation for the rewrite aliases.<p>
     */
    protected void validateRewrite() {

        CmsRpcAction<CmsRewriteAliasValidationReply> action = new CmsRpcAction<CmsRewriteAliasValidationReply>() {

            @Override
            public void execute() {

                start(200, true);
                List<CmsRewriteAliasTableRow> rowsToValidate = new ArrayList<CmsRewriteAliasTableRow>();
                rowsToValidate.addAll(m_view.getRewriteData());

                CmsRewriteAliasValidationRequest request = new CmsRewriteAliasValidationRequest(rowsToValidate);
                getService().validateRewriteAliases(request, this);
            }

            @Override
            public void onResponse(CmsRewriteAliasValidationReply result) {

                stop(false);
                m_view.update(result);
                updateValidationStatus();
            }

        };
        action.execute();
    }

    /**
     * Copies a list of rows.<p>
     *
     * @param data the original data
     *
     * @return the copied data
     */
    List<CmsAliasTableRow> copyData(List<CmsAliasTableRow> data) {

        List<CmsAliasTableRow> result = new ArrayList<CmsAliasTableRow>();
        for (CmsAliasTableRow row : data) {
            CmsAliasTableRow copiedRow = row.copy();
            result.add(copiedRow);
        }
        return result;
    }

}
